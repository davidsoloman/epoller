package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import au.com.bytecode.opencsv.CSVReader;


public final class CSVImporter {
	
	public static ConcurrentHashMap<String, String> loadDevices(String url) throws IOException {
		ConcurrentHashMap<String, String> devices = new ConcurrentHashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			devices.put(nextLine[0], nextLine[1]);	// O -> IP , 1 -> Device Name 
		}
		
		return devices;
	}
	
	public static ConcurrentHashMap<String, String> loadParameters(String url) throws IOException {
		ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 2) {
				parameters.put(nextLine[0], nextLine[1]);
			}
		}
		
		return parameters;
	}
	
	public static ConcurrentHashMap<String, String> loadTraps(String url) throws IOException {
		ConcurrentHashMap<String, String> traps = new ConcurrentHashMap<String, String>();
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
