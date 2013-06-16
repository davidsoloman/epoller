package main;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import pojos.Parameter;

import util.CSVImporter;
import util.TextWriter;

public final class DeviceManager {

	public static HashMap<String, String> devices;
	public static ArrayList<Parameter> parameters;

	private static HashMap<String, Integer> lines;

	static {
		lines = new HashMap<String, Integer>();
	}

	public static void loadDevices() throws IOException {
		devices = CSVImporter.loadDevices("devices.csv");
	}

	public static void loadParameters() throws IOException {
		parameters = CSVImporter.loadParameters("parameters.csv");
	}

	public static synchronized void writeData(String deviceIP, String data, long latency) {
		
		if (lines.get(deviceIP) == null) {
			lines.put(deviceIP, 0);
			TextWriter.printToFile(devices.get(deviceIP), getCurrentDate() + "," + data + "," + latency);
		} else {
			int fetchedParams = lines.get(deviceIP);
			if (fetchedParams < parameters.size()-2) {
				lines.put(deviceIP, fetchedParams + 1);
				TextWriter.printToFile(devices.get(deviceIP), "," + data + "," + latency);
			} else {
				lines.remove(deviceIP);
				TextWriter.printlnToFile(devices.get(deviceIP), "," + data + "," + latency);
			}
		}
	}

	public static String getCurrentDate() {
		return new Timestamp(System.currentTimeMillis()).toString();
	}

}
