package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import au.com.bytecode.opencsv.CSVReader;


public final class CSVImporter {
	
	public static HashMap<String, String> loadDevices(String url) throws IOException {
		HashMap<String, String> devices = new HashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			devices.put(nextLine[0], nextLine[1]);	// O -> IP , 1 -> Device Name 
		}
		
		return devices;
	}
	
	public static LinkedHashMap<String, String> loadParameters(String url) throws IOException {
		LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 2) {
				parameters.put(nextLine[0], nextLine[1]);
			}
		}
		
		return parameters;
	}
	
	public static HashMap<String, String> loadTraps(String url) throws IOException {
		HashMap<String, String> traps = new HashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 2) {
				traps.put(nextLine[0], nextLine[1]);
			}
		}
		
		return traps;
	}

}
