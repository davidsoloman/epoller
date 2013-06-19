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

	public static void loadDevices() throws IOException {
		devices = CSVImporter.loadDevices("devices.csv");
	}

	public static void loadParameters() throws IOException {
		parameters = CSVImporter.loadParameters("parameters.csv");
	}

	public static synchronized void writeData(String deviceIP, String data, long latency) {
		TextWriter.printlnToFile(devices.get(deviceIP), getCurrentDate() + "," + data + latency);
	}

	public static String getCurrentDate() {
		return new Timestamp(System.currentTimeMillis()).toString();
	}

}
