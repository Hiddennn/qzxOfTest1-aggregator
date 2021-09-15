/*
 * Copyright © 2017 Copyright(c) qzx and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package edu.bupt.qzxOfTest1.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.bupt.qzxOfTest1.cli.api.QzxOfTest1CliCommands;

public class QzxOfTest1CliCommandsImpl implements QzxOfTest1CliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(QzxOfTest1CliCommandsImpl.class);
    private final DataBroker dataBroker;

    public QzxOfTest1CliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("QzxOfTest1CliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}