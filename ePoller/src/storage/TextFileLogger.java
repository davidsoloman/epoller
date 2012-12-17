/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import pojos.Parameter;
import pojos.Device;

public class TextFileLogger {

	public TextFileLogger() {
	}
	
	public void initOutputFiles(ArrayList<Device> devices, ArrayList<Parameter> params) throws IOException
	{
		File outputFile= new File("data");
		RandomAccessFile emptyRandomAccessFile;
		
		outputFile.mkdir();
		
		for(int i=0;i<devices.size();i++)
		{
			outputFile=new File("data/"+devices.get(i).toString()+".csv");
			if(!outputFile.exists())
			{
				emptyRandomAccessFile= new RandomAccessFile(outputFile, "rw");
				
				emptyRandomAccessFile.writeBytes("timestamp,");
				for(int j=0;j<params.size()-1;j++)
					emptyRandomAccessFile.writeBytes(params.get(j).getName() + ",latency,");
				
				emptyRandomAccessFile.writeBytes(params.get(params.size()-1).getName()+ ",latency");
				emptyRandomAccessFile.writeBytes("\n");
				emptyRandomAccessFile.close();
			}
		}
	}

	public synchronized void printlnToFile(String file, String value) {
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(new File("data/"+file+".csv"), "rw");
			randomAccessFile.seek(randomAccessFile.length());
			randomAccessFile.writeBytes(value + "\n");
			randomAccessFile.close();
		} catch (IOException e) {
			System.out.println("There was an error writing to the file: " + file);
			e.printStackTrace();
		}
	}
}
