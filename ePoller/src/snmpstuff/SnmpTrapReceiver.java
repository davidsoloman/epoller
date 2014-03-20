package snmpstuff;

import main.DeviceManager;

import org.snmp4j.Snmp;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OctetString;
import org.snmp4j.mp.MPv3;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.security.USM;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;

import util.TextWriter;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.IOException;

public class SnmpTrapReceiver implements CommandResponder {

	private MultiThreadedMessageDispatcher dispatcher;
	private Snmp snmp = null;
	private Address listenAddress;
	private ThreadPool threadPool;

	private String traps_file;

	public void run() {
		try {
			init();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void init() throws UnknownHostException, IOException {

		Properties properties = new Properties();
		properties.load(new FileInputStream("configuration.properties"));

		traps_file = properties.getProperty("traps_log");

		threadPool = ThreadPool.create("Trap", 2);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
		listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", "udp:" + properties.getProperty("interface") + "/162"));

		TransportMapping<UdpAddress> transport;
		transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);

		snmp = new Snmp(dispatcher, transport);
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
		snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);
		snmp.listen();

		snmp.addCommandResponder(this);
	}

	public void processPdu(CommandResponderEvent event) {
		
		String trapSenderIP, trapSenderName;

		trapSenderIP = event.getPeerAddress().toString();
		trapSenderIP = trapSenderIP.substring(0, trapSenderIP.indexOf('/'));
		trapSenderName = DeviceManager.getDeviceName(trapSenderIP);

		boolean isValidTrap = false;
		
		String logMessage = DeviceManager.getCurrentDate() + " " + trapSenderIP + " " + trapSenderName;

		Vector<? extends VariableBinding> oids = event.getPDU().getVariableBindings();
		if (oids != null && !oids.isEmpty()) {
			Iterator<? extends VariableBinding> iterator = oids.iterator();
			VariableBinding variableBinding;
			while (iterator.hasNext()) {

				variableBinding = iterator.next();

				String oidCode = variableBinding.getOid().toString();
				
				//is it a trap?
				String oidName = DeviceManager.getTrapName(oidCode);
				
				if (oidName != null) {
					isValidTrap = true;
					logMessage = logMessage + " " + oidName;
				} else {
					//is it a parameter?
					oidCode=oidCode.substring(0,oidCode.length()-2);
					oidName = DeviceManager.getParameterName(oidCode);
					if (oidName != null) {
						isValidTrap = true;
						String value = variableBinding.getVariable().toString();
						logMessage = logMessage + " " + oidName + "=" + value;
					}
				}
			}
		}

		if (isValidTrap)
			TextWriter.printlnToFile(traps_file, logMessage);
	}
}