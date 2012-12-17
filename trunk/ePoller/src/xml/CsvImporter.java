/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package xml;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

import pojos.Parameter;
import pojos.Device;

public class CsvImporter {

	public static ArrayList<Device> getDevices(String url) throws IOException {
		ArrayList<Device> devices = new ArrayList<Device>();
		CSVReader reader = new CSVReader(new FileReader(url), ',', '\"', 0);
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			Device aux = new Device();
			aux.setIp(nextLine[0]);
			aux.setName(nextLine[1]);
			devices.add(aux);
		}
		return devices;
	}

	public static ArrayList<Parameter> getParameters(String url)
			throws IOException {
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
