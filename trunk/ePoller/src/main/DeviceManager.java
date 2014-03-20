package main;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;

import util.CSVImporter;
import util.TextWriter;

public final class DeviceManager {

	public static HashMap<String, String> devices;
	public static LinkedHashMap<String, String> parameters;
	public static HashMap<String, String> traps;

	public static void loadDevices() throws IOException {
		devices = CSVImporter.loadDevices("devices.csv");
	}
	
	public static void loadParameters() throws IOException {
		parameters = CSVImporter.loadParameters("parameters.csv");
	}
	
	public static void loadTraps() throws IOException {
		traps = CSVImporter.loadTraps("traps.csv");
	}
	
	public static String getDeviceName(String deviceIP){
		return devices.get(deviceIP);
	}
	
	public static String getParameterName(String OID){
		return parameters.get(OID);
	}
	
	public static String getTrapName(String OID){
		return traps.get(OID);
	}
	
	public static synchronized void writeData(String deviceIP, String data, long latency) {
		System.out.println(getCurrentDate() + "," + data + latency);
		TextWriter.printlnToFile(devices.get(deviceIP), getCurrentDate() + "," + data + latency);
	}
	
	public static String getCurrentDate() {
		return new Timestamp(System.currentTimeMillis()).toString();
	}

}
