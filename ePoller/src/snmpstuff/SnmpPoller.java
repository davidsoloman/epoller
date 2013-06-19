package snmpstuff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import main.DeviceManager;

import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.PduHandle;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

public class SnmpPoller implements CommandResponder {

	private Address addr;
	private CommunityTarget target;
	private PDU pdu;
	private Snmp snmp;

	@SuppressWarnings("rawtypes")
	private TransportMapping transport;

	private ThreadPool threadPool;
	private MultiThreadedMessageDispatcher multiThreadedMessageDispatcher;
	private MessageDispatcherImpl dispatcherImpl;

	private String deviceIP;

	private HashMap<Integer, Long> requestIDs;

	public SnmpPoller() throws FileNotFoundException, IOException {

		Properties properties = new Properties();
		properties.load(new FileInputStream("configuration.properties"));

		threadPool = ThreadPool.create("getsender", Integer.parseInt(properties.getProperty("pool_size")));

		dispatcherImpl = new MessageDispatcherImpl();
		dispatcherImpl.addCommandResponder(this);

		multiThreadedMessageDispatcher = new MultiThreadedMessageDispatcher(threadPool, dispatcherImpl);
		multiThreadedMessageDispatcher.addMessageProcessingModel(new MPv2c());

		addr = new UdpAddress("127.0.0.1/161");

		target = new CommunityTarget();
		target.setCommunity(new OctetString(properties.getProperty("community")));
		target.setAddress(addr);
		target.setVersion(SnmpConstants.version2c);
		target.setTimeout(Long.parseLong(properties.getProperty("timeout")));
		target.setRetries(Integer.parseInt(properties.getProperty("retries")));

		transport = new DefaultUdpTransportMapping();

		pdu = new PDU();
		pdu.setType(PDU.GET);
		
		for(int i=0;i<DeviceManager.parameters.size();i++){
			VariableBinding variable= new VariableBinding(new OID(DeviceManager.parameters.get(i).getOid()));
			pdu.addOID(variable);
		}

		snmp = new Snmp(multiThreadedMessageDispatcher, transport);
		snmp.listen();

		requestIDs = new HashMap<Integer, Long>();
	}

	public void setDevice(String ip) {
		deviceIP = ip;
		addr.setValue(deviceIP + "/161");
	}

	public void doRequest() {

		try {
			requestIDs.put(multiThreadedMessageDispatcher.sendPdu(target, pdu, false).getTransactionID(), System.currentTimeMillis());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		snmp.close();
	}

	@Override
	public void processPdu(CommandResponderEvent event) {

		PduHandle pduHandle=event.getPduHandle();
		int requestID = pduHandle.getTransactionID();

		Long latency = requestIDs.get(requestID);

		if (latency != null) {

			latency = System.currentTimeMillis() - latency;
			
			PDU rawResponse = event.getPDU();

			if (rawResponse.getErrorStatusText().equalsIgnoreCase("success"))
			{
				String result = "";
				
				for(VariableBinding param: rawResponse.getVariableBindings())
					result=result+param.getVariable()+",";
				
				String deviceIP= event.getPeerAddress().toString();
				deviceIP=deviceIP.substring(0, deviceIP.indexOf("/"));
				
				DeviceManager.writeData(deviceIP, result, latency);
			}
			
			requestIDs.remove(requestID);
		}
	}
}
