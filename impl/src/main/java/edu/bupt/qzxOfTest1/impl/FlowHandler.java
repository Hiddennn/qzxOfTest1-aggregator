/*
 * Copyright © 2017 zhiyifang and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package edu.bupt.qzxOfTest1.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.service.rev150305.SalQueueService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FlowHandler {
	Logger LOG = LoggerFactory.getLogger(FlowHandler.class);
	private final DataBroker dataBroker;
	private final SalFlowService salFlowService;
	private static final String FLOW_ID_PREFIX = "USEC-";
	private static int flowNo = 0;
	private String srcMac, dstMac, srcIp, dstIp;
	private String inPort, outPort;
	private String matchType, instructionType;
	private Long queueId;


	public FlowHandler(DataBroker dataBroker, SalFlowService salFlowService) {
		this.dataBroker = dataBroker;
		this.salFlowService = salFlowService;
	}


	public String getSrcMac() {
		return this.srcMac;
	}

	public String getDstMac() {
		return this.dstMac;
	}

	public String getSrcIp() {
		return this.srcIp;
	}

	public String getDstIp() { return this.dstIp; }

	public String getInPort() {
		return this.inPort;
	}

	public String getOutPort() {
		return this.outPort;
	}

	public Long getQueueId() {
		return this.queueId;
	}

	public String getMatchType() {
		return this.matchType;
	}

	public String getInstructionType() {
		return this.instructionType;
	}

	public FlowHandler setSrcMac(String value) {
		this.srcMac = value;
		return this;
	}

	public FlowHandler setDstMac(String value) {
		this.dstMac = value;
		return this;
	}

	public FlowHandler setSrcIp(String value) {
		this.srcIp = value;
		return this;
	}

	public FlowHandler setDstIp(String value) {
		this.dstIp = value;
		return this;
	}

	public FlowHandler setInPort(String value) {
		this.inPort = value;
		return this;
	}

	public FlowHandler setOutPort(String value) {
		this.outPort = value;
		return this;
	}

	public FlowHandler setQueueId(Long value) {
		this.queueId = value;
		return this;
	}

	public FlowHandler setMatchType(String value) {
		this.matchType = value;
		return this;
	}

	public FlowHandler setInstructionType(String value) {
		this.instructionType = value;
		return this;
	}


	public void sendFlowToAllNodes() {
		// 获取全部的节点
		List<Node> nodes = getAllNodes(dataBroker);
		// create match

		// 创建一个flow
		List<Instruction> instructions = new ArrayList<>();
		instructions.add(createDropInstruction(0));

		Flow flow = createFlow(createMacMatch(),instructions);
		for (Node node : nodes) {
			// eg. NodeKey[_id=Uri[_value=openflow:1]]
			NodeKey nodeKey = node.getKey();// 看Yang文件NodeKey的变量是NodeId
			// 寻找Nodes根节点下的子节点，由NodeKey来寻找Nodes下的子节点
			InstanceIdentifier<Node> nodeInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
					.build();
			// 对每个节点下发流表
			addFlow(nodeInstanceIdentifier, flow);
		}
	}


	public void sendFlowToNode(String node) {
		// select match
		String matchType = this.getMatchType();
		Match match = null;
		switch (matchType.toLowerCase()) {
			case "ip-match":
				match = createIpMatch();
				break;
			case "mac-match":
				match = createMacMatch();
				break;
			case "port-match":
				match = createPortMatch();
				break;
			default:
				throw new IllegalArgumentException("The match type is illegal");
		}
		// select instructions
		String instructionType = this.getInstructionType();
		List<Instruction> instructions = new ArrayList<>();
		switch (instructionType.toLowerCase()) {
			case "drop-action":
				instructions.add(createDropInstruction(0));
				break;
			case "meter-action":
				instructions.add(createOutputInstruction(0,this.getOutPort()));
				instructions.add(createMeterInstruction(1));
				break;
			case "queue-action":
				instructions.add(createSetQueueInstruction(0, this.getQueueId()));
				break;
			default:
				throw new IllegalArgumentException("The instruction type is illegal");
		}

		Flow flow = createFlow(match, instructions);
		InstanceIdentifier<Node> nodeInstanceIdentifier = getNodeInstanceId(node);

		addFlow(nodeInstanceIdentifier, flow);
	}

	// 创建flow
	private Flow createFlow(Match match, List<Instruction> instructions) {

		// 设置名字和tableID以及flowID
		FlowBuilder builder = new FlowBuilder();
		builder.setFlowName("prohibitFlow").setTableId(Short.valueOf("0"));
		builder.setId(new FlowId(Long.toString(builder.hashCode())));
		// 设置匹配域
		builder.setMatch(match);

		// 设置指令
		InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
		instructionsBuilder.setInstruction(instructions);
		// 设置指令
		builder.setInstructions(instructionsBuilder.build());
		// 设置其他项
		builder.setPriority(50);
		builder.setHardTimeout(9999);
		builder.setIdleTimeout(9999);
		return builder.build();
	}

	private Instruction createSetQueueInstruction(Integer order, Long queueId) {
		InstructionBuilder instructionBuilder = new InstructionBuilder();
		ApplyActionsCaseBuilder actionsCaseBuilder = new ApplyActionsCaseBuilder();
		ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();

		//set queue
		ActionBuilder queueActionBuilder = new ActionBuilder();
		queueActionBuilder.setAction(
				new SetQueueActionCaseBuilder().setSetQueueAction(
						new SetQueueActionBuilder().setQueueId(queueId)
								.build()).build()).setOrder(0);

		ActionBuilder outputActionBuilder = new ActionBuilder();
		outputActionBuilder.setAction(
				new OutputActionCaseBuilder().setOutputAction(
						new OutputActionBuilder().setMaxLength(60).setOutputNodeConnector(new Uri("NORMAL"))
								.build()).build()).setOrder(1);

		List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions = new ArrayList<>();
		actions.add(queueActionBuilder.build());
		actions.add(outputActionBuilder.build());
		actionsBuilder.setAction(actions);
		actionsCaseBuilder.setApplyActions(actionsBuilder.build());
		instructionBuilder.setInstruction(actionsCaseBuilder.build());
		instructionBuilder.setOrder(order);

		return instructionBuilder.build();

	}

	private Instruction createOutputInstruction(Integer order, String nodeConnector) {
		InstructionBuilder instructionBuilder = new InstructionBuilder();
		ApplyActionsCaseBuilder actionsCaseBuilder = new ApplyActionsCaseBuilder();
		ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(
				new OutputActionCaseBuilder().setOutputAction(
						new OutputActionBuilder().setMaxLength(60).setOutputNodeConnector(new Uri(nodeConnector))
								.build()).build()).setOrder(1);

		List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions = new ArrayList<>();
		actions.add(actionBuilder.build());
		actionsBuilder.setAction(actions);
		actionsCaseBuilder.setApplyActions(actionsBuilder.build());
		instructionBuilder.setInstruction(actionsCaseBuilder.build());
		instructionBuilder.setOrder(order);

		return instructionBuilder.build();
	}

	private Instruction createMeterInstruction(Integer order) {
		Long meterId = MeterHandler.getMeterId() - 1;
		InstructionBuilder instructionBuilder = new InstructionBuilder();
		MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
		MeterBuilder meterBuilder = new MeterBuilder();
		meterBuilder.setMeterId(new MeterId(meterId));
		meterCaseBuilder.setMeter(meterBuilder.build());
		instructionBuilder.setInstruction(meterCaseBuilder.build());
		instructionBuilder.setOrder(order);

		return instructionBuilder.build();
	}

	private Instruction createDropInstruction(Integer order) {
		InstructionBuilder instructionBuilder = new InstructionBuilder();
		ApplyActionsCaseBuilder actionsCaseBuilder = new ApplyActionsCaseBuilder();
		ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(new DropActionCaseBuilder().setDropAction(new DropActionBuilder().build()).build());
		List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions = new ArrayList<>();
		actions.add(actionBuilder.build());
		actionsBuilder.setAction(actions);
		actionsCaseBuilder.setApplyActions(actionsBuilder.build());
		instructionBuilder.setInstruction(actionsCaseBuilder.build());
		instructionBuilder.setOrder(order);

		return instructionBuilder.build();
	}

	private void addFlow(InstanceIdentifier<Node> nodeId,Flow flow) {

		LOG.info("Adding prohibit flows for node {} ", nodeId);
		// 根据nodeId获取tableId
		InstanceIdentifier<Table> tableId = getTableInstanceId(nodeId);
		// 创建一个FlowKey
		FlowKey flowKey = new FlowKey(new FlowId(FLOW_ID_PREFIX + flowNo++));
		// 在datastore中创建一个子路经
		InstanceIdentifier<Flow> flowId = tableId.child(Flow.class, flowKey);
		// 在这个子路经下添加一个流
		writeFlow(nodeId, tableId, flowId,flow);

		LOG.info("Added flows for node {} ", nodeId);
	}

	private Match createPortMatch() {

		String inPort = this.getInPort();
		if (StringUtils.isEmpty(inPort)) {
			throw new IllegalArgumentException("The inPort is null");
		}

		MatchBuilder matchBuilder = new MatchBuilder();
		//to do here
		NodeConnectorId nodeConnectorId = new NodeConnectorId(new Uri(inPort));
		matchBuilder.setInPort(nodeConnectorId);

		return matchBuilder.build();
	}

	private Match createIpMatch() {

		String dstIp = this.getDstIp();
		String srcIp = this.getSrcIp();
		if (StringUtils.isEmpty(dstIp) || StringUtils.isEmpty(srcIp)) {
			throw new IllegalArgumentException("The dstIp or srcIp is null");
		}

		MatchBuilder matchBuilder = new MatchBuilder();
		//设置以太网类型为ip
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
		ethernetMatchBuilder.setEthernetType(
				new EthernetTypeBuilder().setType(
						new EtherType((long) 2048)).build());

		Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
		ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(srcIp));
		ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(dstIp));

		matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
		matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
		return matchBuilder.build();
	}

	private Match createMacMatch() {

		String srcMAC = this.getSrcMac();
		String dstMAC = this.getDstMac();
		if (StringUtils.isEmpty(srcMAC) || StringUtils.isEmpty(dstMAC)) {
			throw new IllegalArgumentException("The dstMac or srcMac is null");
		}
		// 设置匹配域
		MatchBuilder matchBuilder = new MatchBuilder();
		// 以太网的匹配
		EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
		// 以太网的目的地址
		EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
		ethernetDestinationBuilder.setAddress(new MacAddress(dstMAC));
		ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());
		EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
		ethernetSourceBuilder.setAddress(new MacAddress(srcMAC));
		ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());
		matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());
		return matchBuilder.build();
	}

	private void writeFlow(InstanceIdentifier<Node> nodeInstanceId,
			InstanceIdentifier<Table> tableInstanceId, InstanceIdentifier<Flow> flowPath, Flow flow) {
		// 创建一个AddflowInputBuilder
		AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
		// 指定一个节点
		builder.setNode(new NodeRef(nodeInstanceId));
		// flow的路径
		builder.setFlowRef(new FlowRef(flowPath));
		// table的路径
		builder.setFlowTable(new FlowTableRef(tableInstanceId));
		builder.setTransactionUri(new Uri(flow.getId().getValue()));

		ListenableFuture<RpcResult<AddFlowOutput>> result = JdkFutureAdapters
				.listenInPoolThread(salFlowService.addFlow(builder.build()));
		Futures.addCallback(result, new FutureCallback<RpcResult<AddFlowOutput>>() {

			@Override
			public void onFailure(final Throwable throwable) {
				System.out.println("failed to send flow " + flowNo + FlowHandler.this.getInstructionType());
			}

			@Override
			public void onSuccess(RpcResult<AddFlowOutput> result) {
				System.out.println("succeed to send flow " + flowNo + FlowHandler.this.getInstructionType());
			}
		});
	}

	/**
	 * 根据nodeid获取tableId
	 * 
	 * @param nodeId
	 * @return
	 */
	private InstanceIdentifier<Table> getTableInstanceId(InstanceIdentifier<Node> nodeId) {

		// get flow table key
		// 获取0号流表
		short tableId = 0;
		TableKey flowTableKey = new TableKey(tableId);
		return nodeId.augmentation(FlowCapableNode.class).child(Table.class, flowTableKey);
	}

	private InstanceIdentifier<Node> getNodeInstanceId(String node) {
		NodeId nodeId = new NodeId(new Uri(node));
		NodeKey nodeKey = new NodeKey(nodeId);
		return InstanceIdentifier.builder(Nodes.class).child(Node.class, nodeKey)
				.build();
	}

	/**
	 * 读取inventory数据库获取所有的节点
	 * 
	 * @param dataBroker
	 * @return
	 */
	private List<Node> getAllNodes(DataBroker dataBroker) {

		// 读取inventory数据库
		InstanceIdentifier.InstanceIdentifierBuilder<Nodes> nodesInsIdBuilder = InstanceIdentifier
				.<Nodes>builder(Nodes.class);
		// 两种构建instanceIdentifier的方式
		// InstanceIdentifier<Nodes> nodesInsIdBuilder = InstanceIdentifier.
		// create(Nodes.class);

		// 所有节点信息
		Nodes nodes = null;
		// 创建读事务
		try (ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction()) {

			Optional<Nodes> dataObjectOptional = readOnlyTransaction
					.read(LogicalDatastoreType.OPERATIONAL, nodesInsIdBuilder.build()).get();
			// 如果数据不为空，获取到nodes
			if (dataObjectOptional.isPresent()) {
				nodes = dataObjectOptional.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Failed to read nodes from Operation data store.");
			throw new RuntimeException("Failed to read nodes from Operation data store.", e);
		}

		return nodes.getNode();
	}
}
