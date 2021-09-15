/*
 * Copyright © 2017 Copyright(c) qzx and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package edu.bupt.qzxOfTest1.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.qzxoftest1.rev150105.QzxOfTest1Service;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

public class QzxOfTest1Provider {

    private static final Logger LOG = LoggerFactory.getLogger(QzxOfTest1Provider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
//	private RpcRegistration<QzxOfTest1Service> rpcRegistration;
	private final SalFlowService salFlowService;
	private final SalMeterService salMeterService;

    public QzxOfTest1Provider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry, final SalFlowService salFlowService, final SalMeterService salMeterService) {
        this.dataBroker = dataBroker;
		this.rpcProviderRegistry = rpcProviderRegistry;
//		Preconditions.checkNotNull(salFlowService, "salFlowService should not be null");
		this.salFlowService = salFlowService;
		this.salMeterService = salMeterService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("QzxOfTest1Provider Session Initiated");
//        QzxOfTest1Impl qzxOfTest1Impl = new QzxOfTest1Impl(salFlowService, salMeterService);
//        rpcRegistration = rpcProviderRegistry.addRpcImplementation(QzxOfTest1Service.class, qzxOfTest1Impl);
    }
    
    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
//    	if (rpcRegistration != null) {
//			rpcRegistration.close();
//		}
        LOG.info("QzxOfTest1Provider Closed");
    }
}