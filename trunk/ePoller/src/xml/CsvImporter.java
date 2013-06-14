/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package xml;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

import pojos.Parameter;

public class CsvImporter {
	
	public static HashMap<String, String> getDevices2(String url) throws IOException {
		HashMap<String, String> devices = new HashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			devices.put(nextLine[0], nextLine[1]);	// O -> IP , 1 -> Device Name 
		}
		return devices;
	}

	public static ArrayList<Parameter> getParameters(String url) throws IOException {
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 2) {
				Parameter aux = new Parameter();
				aux.setName(nextLine[0]);
				aux.setOid(nextLine[1]);
				parameters.add(aux);
			}
		}
		return parameters;
	}

}
