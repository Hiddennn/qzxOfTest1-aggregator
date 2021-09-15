/*
 * Copyright © 2017 Copyright(c) qzx and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package edu.bupt.qzxOfTest1.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.list.links.info.output.LinksInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.list.links.info.output.LinksInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.list.ports.info.output.PortsInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.list.ports.info.output.PortsInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;



public class QzxOfTest1Impl implements QzxOfTest1Service {

	private static final Logger LOG = LoggerFactory.getLogger(QzxOfTest1Impl.class);
	private static final String FLOWID = "flow:1";
	private final SalFlowService salFlowService;
	private final SalMeterService salMeterService;
	private final DataBroker dataBroker;
	
	public QzxOfTest1Impl(SalFlowService salFlowService, SalMeterService salMeterService, DataBroker dataBroker) {
		
		this.salFlowService = salFlowService;
		this.salMeterService = salMeterService;
		this.dataBroker = dataBroker;
	}

	@Override
	public Future<RpcResult<QzxSendFlowOutput>> qzxSendFlow(QzxSendFlowInput input) {

		// 下发meter
//		MeterHandler myMeterHandler = new MeterHandler(salMeterService);
//		myMeterHandler.setBandRate(input.getBandRate()).setBurstSize(input.getBandBurstSize());
//		myMeterHandler.sendMeterToNode(input.getSwitch());

		// 下发流表
		FlowHandler myFlowHandler = new FlowHandler(dataBroker, salFlowService);
		myFlowHandler
				.setSrcIp(input.getSrcIp()).setDstIp(input.getDstIp())
				.setSrcMac(input.getSrcMac()).setDstMac(input.getDstMac())
				.setInPort(input.getInPort()).setOutPort(input.getOutPort())
				.setQueueId(input.getQueueId())
				.setMatchType(input.getMatchType())
				.setInstructionType(input.getInstructionType());
		myFlowHandler.sendFlowToNode(input.getSwitch());

		// rpc result
		QzxSendFlowOutputBuilder qzxSendFlowOutputBuilder = new QzxSendFlowOutputBuilder();
		qzxSendFlowOutputBuilder.setResult("Send flow finished");
		QzxSendFlowOutput qzxSendFlowOutput = qzxSendFlowOutputBuilder.build();
		RpcResultBuilder<QzxSendFlowOutput> myRpcResultBuilder = null;
		if (qzxSendFlowOutput != null) {
			myRpcResultBuilder = RpcResultBuilder.success(qzxSendFlowOutput);
		}else {
			myRpcResultBuilder = RpcResultBuilder.<QzxSendFlowOutput>failed().withError(ErrorType.APPLICATION, "Invalid output value", "Output is null");
		}
		return Futures.immediateFuture(myRpcResultBuilder.build());
	}

	@Override
	public Future<RpcResult<TestRpcOutput>> testRpc() {
		
		TestRpcOutputBuilder outputBuilder = new TestRpcOutputBuilder();
		RpcResultBuilder<TestRpcOutput> myRpcResultBuilder = null;
		
		String outputString = "RPC's output string. test ";
		outputBuilder.setMyLeaf(outputString);
		TestRpcOutput output = outputBuilder.build();
		if (output != null) {
			myRpcResultBuilder = RpcResultBuilder.success(output);
		}else {
			myRpcResultBuilder = RpcResultBuilder.<TestRpcOutput>failed().withError(ErrorType.APPLICATION, "Invaild output value", "Output is null");
		}
		return Futures.immediateFuture(myRpcResultBuilder.build());
	}

	@Override
	public Future<RpcResult<ListLinksInfoOutput>> listLinksInfo() {
		final SettableFuture<RpcResult<ListLinksInfoOutput>> futureResult = SettableFuture.create();
		ListLinksInfoOutputBuilder outputBuilder = new ListLinksInfoOutputBuilder();
		final InstanceIdentifier.InstanceIdentifierBuilder<Topology> topologyId = InstanceIdentifier.builder(NetworkTopology.class).
				child(Topology.class, new TopologyKey(new TopologyId(new Uri(FLOWID))));
		InstanceIdentifier<Topology> topologyIId = topologyId.build();
		Topology topology = read(LogicalDatastoreType.OPERATIONAL, topologyIId);

		if (topology == null || topology.getLink() == null || topology.getLink().size() < 1) {
			futureResult.set(RpcResultBuilder.success(outputBuilder.build()).build());
			return futureResult;
		}
		List<LinksInfo> linkInfos = new ArrayList<>();
		topology.getLink().forEach(temp -> {
			LinksInfoBuilder lib = new LinksInfoBuilder();

			lib.setLinkId(temp.getLinkId())
					.setSrcDevice(temp.getSource().getSourceNode())
					.setSrcPort(new Uri(temp.getSource().getSourceTp().getValue()))
					.setDstDevice(temp.getDestination().getDestNode())
					.setDstPort(new Uri(temp.getDestination().getDestTp().getValue()));
			linkInfos.add(lib.build());
		});
		outputBuilder.setLinksInfo(linkInfos);
		futureResult.set(RpcResultBuilder.success(outputBuilder.build()).build());
		return futureResult;
	}

	@Override
	public Future<RpcResult<ListPortsInfoOutput>> listPortsInfo() {
		final SettableFuture<RpcResult<ListPortsInfoOutput>> futureResult = SettableFuture.create();
		ListPortsInfoOutputBuilder listPortsInfoOutputBuilder = new ListPortsInfoOutputBuilder();
		Nodes nodes = queryAllNode(LogicalDatastoreType.OPERATIONAL);
		if (nodes == null || nodes.getNode() == null || nodes.getNode().size() < 1) {
			futureResult.set(RpcResultBuilder.success(listPortsInfoOutputBuilder.build()).build());
			return futureResult;
		}
		List<PortsInfo> portsInfos = new ArrayList<>();
		nodes.getNode().forEach(tempNode -> {
			List<NodeConnector> nodeConnectors = filterNodeConnectors(tempNode);
			if (nodeConnectors == null || nodeConnectors.size() < 1) {
				return;
			}
			nodeConnectors.forEach(tempPort -> {
				PortsInfoBuilder pi = new PortsInfoBuilder();
				FlowCapableNodeConnector augmentation = tempPort.getAugmentation(FlowCapableNodeConnector.class);
				pi.setDeviceId(tempNode.getId())
						.setPortNumber(new String(augmentation.getPortNumber().getValue()))
						.setPortName(augmentation.getName())
						.setHardwareAddress(augmentation.getHardwareAddress().getValue())
						.setLinkDown(augmentation.getState().isLinkDown())
						.setMaximumSpeed(augmentation.getMaximumSpeed())
						.setCurrentSpeed(augmentation.getCurrentSpeed());

				portsInfos.add(pi.build());
			});
		});
		listPortsInfoOutputBuilder.setPortsInfo(portsInfos);
		futureResult.set(RpcResultBuilder.success(listPortsInfoOutputBuilder.build()).build());
		return futureResult;
	}

	private  <D extends DataObject> D read(final LogicalDatastoreType store, final InstanceIdentifier<D> path) {
		D result = null;
		final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
		com.google.common.base.Optional<D> optionalDataObject;
		final CheckedFuture<Optional<D>, ReadFailedException> future = transaction.read(store, path);
		try {
			optionalDataObject = future.checkedGet();
			if (optionalDataObject.isPresent()) {
				result = optionalDataObject.get();
			} else {
				LOG.debug("{}: Failed to read {}", Thread.currentThread().getStackTrace()[1], path);
			}
		} catch (final ReadFailedException e) {
			LOG.warn("Failed to read {} ", path, e);
		}
		transaction.close();
		return result;
	}

	private Nodes queryAllNode(LogicalDatastoreType configuration) {
		final InstanceIdentifier<Nodes> identifierNodes = InstanceIdentifier.create(Nodes.class);
		return read(configuration, identifierNodes);
	}
	private List<NodeConnector> filterNodeConnectors(Node node) {
		final List<NodeConnector> connectors = Lists.newArrayList();
		final List<NodeConnector> list = node.getNodeConnector();
		if (list != null && list.size() > 0) {
			for (final NodeConnector nodeConnector : list) {

				if (!nodeConnector.getId().getValue().endsWith("LOCAL")) {
					connectors.add(nodeConnector);
				}
			}
		}
		return connectors;
	}

}
