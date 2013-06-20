package snmpstuff;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import main.DeviceManager;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import util.TextWriter;

public class SnmpPoller implements ResponseListener {

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

	private HashMap<String, Long> requestIDs;
	private Random random;

	public SnmpPoller() throws FileNotFoundException, IOException {

		Properties properties = new Properties();
		properties.load(new FileInputStream("configuration.properties"));

		threadPool = ThreadPool.create("getsender", Integer.parseInt(properties.getProperty("pool_size")));

		dispatcherImpl = new MessageDispatcherImpl();

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

		for (int i = 0; i < DeviceManager.parameters.size(); i++) {
			VariableBinding variable = new VariableBinding(new OID(DeviceManager.parameters.get(i).getOid()));
			pdu.addOID(variable);
		}

		snmp = new Snmp(multiThreadedMessageDispatcher, transport);
		snmp.listen();

		requestIDs = new HashMap<String, Long>();
		random= new Random();
	}

	public void setDevice(String ip) {
		deviceIP = ip;
		addr.setValue(deviceIP + "/161");
	}

	public void doRequest() {

		try {
			String myRequestID = deviceIP + "/" + random.nextInt();
			snmp.send(pdu, target, myRequestID, this);
			requestIDs.put(myRequestID, System.currentTimeMillis());
		} catch (IOException e) {
			e.printStackTrace();
			//TODO Log to separate file
		}
	}

	@Override
	public void onResponse(ResponseEvent responseEvent) {

		String requestID = (String) responseEvent.getUserObject();

		Long latency = requestIDs.get(requestID);

		if (latency != null) {

			latency = System.currentTimeMillis() - latency;

			PDU rawResponse = responseEvent.getResponse();

			String result = "";

			if (rawResponse != null) {
				
				if (rawResponse.getErrorStatus() == SnmpConstants.SNMP_ERROR_SUCCESS)
					for (VariableBinding param : rawResponse.getVariableBindings())
						result = result + param.getVariable() + ",";
				else
					// Bad response
					TextWriter.printlnToFile("bad_responses.log", rawResponse.toString());
			} else
				// Timeout
				for (int i = 0; i < DeviceManager.parameters.size(); i++)
					result = result+",";
			
			String deviceIP = requestID.substring(0, requestID.indexOf("/"));

			DeviceManager.writeData(deviceIP, result, latency);

			requestIDs.remove(requestID);
		}
	}

	public void close() throws IOException {
		snmp.close();
	}
}
