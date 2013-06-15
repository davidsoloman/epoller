package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import pojos.Parameter;

import util.CSVImporter;
import util.TextWriter;

public final class DeviceManager {

	public static HashMap<String, String> devices;
	public static ArrayList<Parameter> parameters;

	private static HashMap<String, Integer> lines;
	
	public DeviceManager() {
		lines= new HashMap<String, Integer>();
	}

	public static void loadDevices() throws IOException {
		devices = CSVImporter.loadDevices("devices.csv");
	}

	public static void loadParameters() throws IOException {
		parameters = CSVImporter.loadParameters("parameters.csv");
	}

	public static synchronized void writeData(String deviceIP, String data, long latency) {

		if (lines.get(deviceIP) == null) {
			TextWriter.printToFile(DeviceManager.devices.get(deviceIP), TextWriter.getCurrentDate() + "," + data + "," + latency);
			lines.put(deviceIP, 1);
		} else {
			int fetchedParams = lines.get(deviceIP);
			if (fetchedParams < DeviceManager.parameters.size())
			{
				TextWriter.printToFile(DeviceManager.devices.get(deviceIP), data + "," + latency);
				lines.put(deviceIP, lines.get(deviceIP) + 1);
			}
			else {
				TextWriter.printlnToFile(DeviceManager.devices.get(deviceIP), data + "," + latency);
				lines.remove(deviceIP);
			}
		}
	}

}
