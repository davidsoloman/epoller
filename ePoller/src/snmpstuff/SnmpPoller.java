/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package snmpstuff;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import pojos.Parameter;
import pojos.Device;

import storage.TextFileLogger;

public class SnmpPoller {

	private CommunityTarget target;
	private Address addr;
	private ResponseEvent response;
	private PDU pdu;
	private Snmp snmp;

	private Device currentDevice;
	private Parameter currentParameter;

	@SuppressWarnings("rawtypes")
	private TransportMapping transport;

	public void setup() throws IOException {

		Properties properties = new Properties();
		properties.load(new FileInputStream("configuration.properties"));

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

		snmp = new Snmp(transport);
		snmp.listen();
	}

	public void setDevice(Device newDevice) {
		currentDevice = newDevice;
		addr.setValue(currentDevice.getIp() + "/161");
	}

	public void setParameter(Parameter newParameter) {
		currentParameter = newParameter;
	}

	public String doSingleRequest() {

		String plainTextResult = null;

		pdu.clear();
		pdu.add(new VariableBinding(new OID(currentParameter.getOid())));

		long responseTime = System.currentTimeMillis();

		try {
			response = snmp.send(pdu, target);
			responseTime = System.currentTimeMillis() - responseTime;
			if (response != null) {

				PDU rawResponse = response.getResponse();

				if (rawResponse != null) {
					if (rawResponse.getErrorStatusText().equalsIgnoreCase("success")) {
						PDU pduresponse = response.getResponse();
						plainTextResult = pduresponse.getVariableBindings().firstElement().toString();
						if (plainTextResult.contains("=")) {
							int len = plainTextResult.indexOf("=");
							plainTextResult = plainTextResult.substring(len + 1, plainTextResult.length());
							plainTextResult = plainTextResult.trim() + "," + responseTime;
						} else
							TextFileLogger.printlnToFile(LOG_FILE, getCurrentDate() + "," + "BAD RESPONSE: " + rawResponse.toString() + " Request: " + pdu.toString() + " Device: " + currentDevice + " Time:" + responseTime);
					} else
						TextFileLogger.printlnToFile(LOG_FILE, getCurrentDate() + "," + "BAD RESPONSE: " + rawResponse.toString() + " Request: " + pdu.toString() + " Device: " + currentDevice + " Time:" + responseTime);
				} else
					TextFileLogger.printlnToFile(LOG_FILE, getCurrentDate() + "," + "TIMEOUT: Request: " + pdu.toString() + " Device: " + currentDevice + " Time:" + responseTime);
			} else
				TextFileLogger.printlnToFile(LOG_FILE, getCurrentDate() + "," + "NETWORK ERROR: Request: " + pdu.toString() + " Device: " + currentDevice + " Time:" + responseTime);

			if (plainTextResult == null)
				plainTextResult = "," + responseTime;

		} catch (IOException e) {
			// TODO redirect this stacktrace to another log file or similar
			e.printStackTrace();
			plainTextResult = "," + (System.currentTimeMillis() - responseTime);
		}

		return plainTextResult;
	}

	public static String getCurrentDate() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp.toString();
	}

	public void close() throws IOException {
		snmp.close();
	}

	// Constants
	private final String LOG_FILE = "general.log";
}