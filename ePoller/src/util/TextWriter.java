/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import pojos.Parameter;

public final class TextWriter {
	
	public static void initOutputFiles(HashMap<String, String> devices, ArrayList<Parameter> params) throws IOException {
		File outputFile = new File("data");
		RandomAccessFile emptyRandomAccessFile;

		outputFile.mkdir();

		for(String deviceName: devices.values())
		{
			outputFile = new File("data/" + deviceName + ".csv");
			if (!outputFile.exists()) {
				emptyRandomAccessFile = new RandomAccessFile(outputFile, "rw");

				emptyRandomAccessFile.writeBytes("timestamp,");
				for (int j = 0; j < params.size() - 1; j++)
					emptyRandomAccessFile.writeBytes(params.get(j).getName() + ",latency,");

				emptyRandomAccessFile.writeBytes(params.get(params.size() - 1).getName() + ",latency");
				emptyRandomAccessFile.writeBytes("\n");
				emptyRandomAccessFile.close();
			}
		}
	}

	public static synchronized void printlnToFile(String file, String value) {
		
		try {	
			if(!file.endsWith(".log"))
				file="data/" +file+".csv";
				
			RandomAccessFile randomAccessFile = new RandomAccessFile(new File(file), "rw");
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(value + "\n");
			randomAccessFile.close();
		} catch (IOException e) {
			System.out.println("There was an error writing to the file: " + file);
			e.printStackTrace();
		}
	}
	
	public static synchronized void printToFile(String file, String value) {
		
		try {
			
			if(!file.endsWith(".log"))
				file="data/" +file+".csv";
			
			RandomAccessFile randomAccessFile = new RandomAccessFile(new File(file), "rw");
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(value);
			randomAccessFile.close();
		} catch (IOException e) {
			System.out.println("There was an error writing to the file: " + file);
			e.printStackTrace();
		}
	}
}
