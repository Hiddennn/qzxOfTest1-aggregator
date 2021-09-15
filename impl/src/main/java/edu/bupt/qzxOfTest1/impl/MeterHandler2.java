/*
 * Copyright © 2017 zhiyifang and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package edu.bupt.qzxOfTest1.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MeterHandler2 {
    
    private final SalMeterService salMeterService;
    private long meterId;
    private String nodeId;

    private static final Logger LOG = LoggerFactory.getLogger(MeterHandler2.class);
    private final DataBroker dataBroker;
    private final long MIN_METER_ID_PICA8 = 1;
    private final long MAX_METER_ID_PICA8 = 256;
    private final String DEFAULT_METER_NAME = "alto-spce rate limiting";
    private final String DEFAULT_METER_CONTAINER = "alto-spce rate limiting container";

    private HashMap<NodeRef, List<Boolean>> switchToMeterIdListMap = new HashMap<>();

    private HashMap<NodeRef, HashMap<EndpointPairAndRequirement, Long>> switchToPerFlowToMeterIdMap = new HashMap<>();
    
    public MeterHandler2(SalMeterService salMeterService, DataBroker dataBroker) {
        this.salMeterService = salMeterService;

        this.dataBroker = dataBroker;
    }

    public MeterHandler2 setMeterId(long meterId) {
        this.meterId = meterId;
        return this;
    }

    public MeterHandler2 setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public long addDropMeter(String src, String dst, long dropRate, long dropBurstSize, NodeConnectorRef nodeConnectorRef) {
        LOG.info("In MeterManager.addDropMeter");
        List<Boolean> perSwitchMeterList;
        HashMap<EndpointPairAndRequirement, Long> perSwitchPerFlowToMeterIdMap;
        if (!switchToMeterIdListMap.containsKey(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)))) {
            perSwitchMeterList = new LinkedList<>();
            for (int i=0 ; i<=MAX_METER_ID_PICA8; ++i) {
                //false stands for meterId == i is free. We must use i from 1 not from 0.
                perSwitchMeterList.add(false);
            }
            switchToMeterIdListMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchMeterList);
        }
        if (!switchToPerFlowToMeterIdMap.containsKey(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)))) {
            perSwitchPerFlowToMeterIdMap = new HashMap<>();
            switchToPerFlowToMeterIdMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchPerFlowToMeterIdMap);
        }

        perSwitchMeterList = switchToMeterIdListMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));
        perSwitchPerFlowToMeterIdMap = switchToPerFlowToMeterIdMap.get(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)));

        int firstFreeMeterId = 1;
        while (perSwitchMeterList.get(firstFreeMeterId)) {
            ++firstFreeMeterId;
        }

        Meter meter = createDropMeter(dropRate, dropBurstSize, firstFreeMeterId);
        writeMeterToConfigData(buildMeterPath(firstFreeMeterId, nodeConnectorRef),meter);
        perSwitchMeterList.set(firstFreeMeterId, true);
        EndpointPairAndRequirement epr = new EndpointPairAndRequirement(src, dst, dropRate, dropBurstSize);
        perSwitchPerFlowToMeterIdMap.put(epr, (long)firstFreeMeterId);

        switchToMeterIdListMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchMeterList);
        switchToPerFlowToMeterIdMap.put(new NodeRef(InstanceIdentifierUtils.generateNodeInstanceIdentifier(nodeConnectorRef)), perSwitchPerFlowToMeterIdMap);
        return firstFreeMeterId;
    }

    private InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter> buildMeterPath(long meterIdLong, NodeConnectorRef nodeConnectorRef) {
        MeterId meterId = new MeterId(meterIdLong);
        MeterKey meterKey = new MeterKey(meterId);
        return InstanceIdentifierUtils.generateMeterInstanceIdentifier(nodeConnectorRef, meterKey);
    }

    private Meter createDropMeter(long dropRate, long dropBurstSize, long meterId) {
        //LOG.info("nodeConnectorRef is" + nodeConnectorRef.toString());
        DropBuilder dropBuilder = new DropBuilder();
        dropBuilder
                .setDropBurstSize(dropBurstSize)
                .setDropRate(dropRate);

        MeterBandHeaderBuilder mbhBuilder = new MeterBandHeaderBuilder()
                .setBandType(dropBuilder.build())
                .setBandId(new BandId(0L))
                .setMeterBandTypes(new MeterBandTypesBuilder()
                        .setFlags(new MeterBandType(true, false, false)).build())
                .setBandRate(dropRate)
                .setBandBurstSize(dropBurstSize);

        LOG.info("In createDropMeter, MeterBandHeaderBuilder is" + mbhBuilder.toString());

        List<MeterBandHeader> mbhList = new LinkedList<>();
        mbhList.add(mbhBuilder.build());

        MeterBandHeadersBuilder mbhsBuilder = new MeterBandHeadersBuilder()
                .setMeterBandHeader(mbhList);

        LOG.info("In createDropMeter, MeterBandHeader is " + mbhBuilder.build().toString());
        MeterBuilder meterBuilder = new MeterBuilder()
                .setFlags(new MeterFlags(true, true, false, false))
                .setMeterBandHeaders(mbhsBuilder.build())
                .setMeterId(new MeterId(meterId))
                .setMeterName(DEFAULT_METER_NAME)
                .setContainerName(DEFAULT_METER_CONTAINER);
        return meterBuilder.build();
    }

    private void writeMeterToConfigData(InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter> meterPath,
                                        Meter meter) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder mb = new  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder(meter);

        WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, meterPath, mb.build(), true);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Transaction failed: {}", e.toString());
        }
        LOG.info("Transaction succeed");
    }

    // 指定S1交换机
    private NodeRef createNode(String node_id) {
        System.out.println("create note " + node_id);
        NodeId nodeId = new NodeId(node_id);
        NodeKey nodeKey = new NodeKey(nodeId);
        InstanceIdentifier<Node> identifier = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeKey).build();
        NodeRef nodeRef = new NodeRef(identifier);
        return nodeRef;
    }

    //创建Meter
    private MeterBuilder createMeter(long meterId) {
        MeterBuilder meterBuilder = new MeterBuilder();
        meterBuilder.setContainerName("meter_container");
        meterBuilder.setKey(new MeterKey(new MeterId(meterId)));
        meterBuilder.setMeterId(new MeterId(meterId));
        meterBuilder.setMeterName("meterName");
        meterBuilder.setFlags(new MeterFlags(true, false, false, false));

        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        List<MeterBandHeader> meterBandHeaders = new ArrayList<>();
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        meterBandHeaderBuilder.setBandRate((long) 123);
        meterBandHeaderBuilder.setBandBurstSize((long) 233);
        DscpRemarkBuilder dscpRemarkBuilder = new DscpRemarkBuilder();
        dscpRemarkBuilder.setDscpRemarkBurstSize((long) 5);
        dscpRemarkBuilder.setDscpRemarkRate((long) 12);
        dscpRemarkBuilder.setPrecLevel((short) 1);
        meterBandHeaderBuilder.setBandType(dscpRemarkBuilder.build());
        MeterBandTypesBuilder meterBandTypesBuilder = new MeterBandTypesBuilder();
        MeterBandType meterBandType = new MeterBandType(false, true, false);
        meterBandTypesBuilder.setFlags(meterBandType);
        meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesBuilder.build());
        meterBandHeaderBuilder.setBandId(new BandId((long) 0));
        meterBandHeaders.add(meterBandHeaderBuilder.build());
        meterBandHeadersBuilder.setMeterBandHeader(meterBandHeaders);
        meterBuilder.setMeterBandHeaders(meterBandHeadersBuilder.build());
        System.out.println("createMeter");

        return meterBuilder;
    }

    public void addMeter() {
        AddMeterInputBuilder addMeterInputBuilder = new AddMeterInputBuilder(
                createMeter(meterId).build());
        InstanceIdentifier<Meter> identifier = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeId)))
                .augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(new MeterId(meterId)));
        addMeterInputBuilder.setMeterRef(new MeterRef(identifier));
        addMeterInputBuilder.setNode(createNode(nodeId));

        ListenableFuture<RpcResult<AddMeterOutput>> result = JdkFutureAdapters
                .listenInPoolThread(salMeterService.addMeter(addMeterInputBuilder.build()));
        Futures.addCallback(result, new FutureCallback<RpcResult<AddMeterOutput>>() {

            @Override
            public void onFailure(final Throwable throwable) {
                System.out.println("failure...");
            }

            @Override
            public void onSuccess(RpcResult<AddMeterOutput> result) {
                System.out.println("success...");
            }
        });
    }
}
