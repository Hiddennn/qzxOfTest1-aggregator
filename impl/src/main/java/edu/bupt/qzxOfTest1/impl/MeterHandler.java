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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.*;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MeterHandler {

    Logger LOG = LoggerFactory.getLogger(FlowHandler.class);
    private final SalMeterService salMeterService;

    private Long burstSize, bandRate;
    private static Long meterId = (long) 1;

    public MeterHandler(SalMeterService salMeterService) {
        this.salMeterService = salMeterService;
    }

    public Long getBurstSize() {
        return burstSize;
    }

    public Long getBandRate() {
        return bandRate;
    }

    public static Long getMeterId() {
        return meterId;
    }

    public MeterHandler setBurstSize(Long value) {
        this.burstSize = value;
        return this;
    }

    public MeterHandler setBandRate(Long value) {
        this.bandRate = value;
        return this;
    }

    public void sendMeterToNode(String node) {
        LOG.info("send meter to node" + node);

        InstanceIdentifier<Node> nodeInstanceIdentifier = getNodeInstanceId(node);
        writeMeter(nodeInstanceIdentifier);
    }

    private MeterBandHeaders createMeterBandHeaders() {
		MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        DropBuilder dropBuilder =  new DropBuilder()
                .setDropBurstSize(this.getBurstSize()).setDropRate(this.getBandRate());

        meterBandHeaderBuilder
                .setBandId(new BandId((long) 0))
		        .setBandBurstSize(this.getBurstSize())
		        .setBandRate(this.getBandRate())
		        .setMeterBandTypes(new MeterBandTypesBuilder()
                    .setFlags(new MeterBandType(true, false, false)).build())
		        .setBandType(dropBuilder.build());
		List<MeterBandHeader> myMeterBandHeaderList = Arrays.asList(meterBandHeaderBuilder.build());
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        meterBandHeadersBuilder.setMeterBandHeader(myMeterBandHeaderList);
        return meterBandHeadersBuilder.build();
    }

    private void writeMeter(InstanceIdentifier<Node> nodeInstanceId) {
        AddMeterInputBuilder builder = new AddMeterInputBuilder();
        builder.setFlags(new MeterFlags(true, true, false, false))
                .setMeterBandHeaders(createMeterBandHeaders())
                .setMeterId(new MeterId(meterId))
                .setMeterName("DropMeter")
                .setNode(new NodeRef(nodeInstanceId))
                .setMeterRef(new MeterRef(this.getMeterInstanceId(nodeInstanceId)))
                .setTransactionUri(new Uri(String.valueOf(meterId)))
                .setContainerName("abc");

        ListenableFuture<RpcResult<AddMeterOutput>> result = JdkFutureAdapters
                .listenInPoolThread(salMeterService.addMeter(builder.build()));
        Futures.addCallback(result, new FutureCallback<RpcResult<AddMeterOutput>>() {

            @Override
            public void onFailure(final Throwable throwable) {
                System.out.println("failed to send meter" + meterId);
            }

            @Override
            public void onSuccess(RpcResult<AddMeterOutput> result) {
                System.out.println("succeed to send meter" + meterId);
            }
        });

        meterId++;
    }

    private InstanceIdentifier<Node> getNodeInstanceId(String node) {
        NodeId nodeId = new NodeId(new Uri(node));
        NodeKey nodeKey = new NodeKey(nodeId);
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
                .build();
    }

    private InstanceIdentifier<Meter> getMeterInstanceId(InstanceIdentifier<Node> nodeId) {
        MeterKey meterKey = new MeterKey(new MeterId(meterId));
        return nodeId.augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
    }

}
