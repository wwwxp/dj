package com.tydic.dcm.ftran.impl;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.log4j.Logger;

public class ProtocolCmdListenerImpl implements ProtocolCommandListener {

	private static Logger logger = Logger.getLogger(ProtocolCmdListenerImpl.class);
	private String devId;

	public ProtocolCmdListenerImpl(String devId) {
		this.devId = devId;
	}

	@Override
	public void protocolCommandSent(ProtocolCommandEvent event) {
		logger.debug("devId ---> " + devId + ", Send Cmd ---> " + event.getMessage().replaceAll("\r\n", ""));
	}

	@Override
	public void protocolReplyReceived(ProtocolCommandEvent event) {
		logger.debug("devId ---> " + devId + ", Recv Cmd ---> " + event.getMessage().replaceAll("\r\n", ""));
	}

}
