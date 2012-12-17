/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pojos.Parameter;
import pojos.Device;
import snmpstuff.PollersPool;
import snmpstuff.SnmpPoller;
import storage.TextFileLogger;

import xml.CsvImporter;

public class Launcher {

	public static void main(String[] args) {

		try {
			String classpath = System.getProperty("java.class.path");
			int jarPos = classpath.indexOf("ePoller.jar");
			int jarPathPos = classpath.lastIndexOf(File.pathSeparatorChar, jarPos) + 1;
			path = classpath.substring(jarPathPos, jarPos);
			
			Properties properties = new Properties();
			properties.load(new FileInputStream(path + "configuration.properties"));
			
			textFileLogger = new TextFileLogger();
			
			System.out.println("Reading devices file ... ");
			devices = CsvImporter.getDevices(path + "devices.csv");
			System.out.println("done. " + devices.size() + " devices found.");
			System.out.print("Reading parameters file ... ");
			parameters = CsvImporter.getParameters(path + "parameters.csv");
			System.out.println("done. " + parameters.size() + " parameters found.");
			
			if (devices.size() < 1 || parameters.size() < 1) {
				System.out.println("For this programan to work, you need at least one parameter and one device.");
				System.out.println("Bye.");
				System.exit(0);
			} else {
				System.out.print("Initializing output files ... ");
				textFileLogger.initOutputFiles(devices, parameters);
				System.out.println("done.");

				System.out.print("Setting up the pollers pool ... ");
				pollersPool = new PollersPool(Integer.parseInt(properties.getProperty("poolsize")));
				System.out.println("done.");
				
				System.out.print("Scheduling the queries ... ");
				
				long frequency = Long.parseLong(properties.getProperty("frequency"));
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(pollersPool.getPoolSize());
				int counter = 1;
				int request_interval=Integer.parseInt(properties.getProperty("request_interval"));
				for (int i = 0; i < devices.size(); i++) {
					executor.scheduleAtFixedRate(new customTimerTask(i), counter * request_interval, frequency, TimeUnit.MILLISECONDS);
					counter++;
				}
				System.out.println("ePoller started succesfully.");
			}
		} catch (IOException e) {
			System.out.println("Are all the required configuration files present?.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static class customTimerTask implements Runnable {

		private SnmpPoller snmpPoller;
		private int device;
		private String aux;
		
		public customTimerTask(int currentDevice) {
			device = currentDevice;
		}
		
		@Override
		public void run() {
			
			snmpPoller = pollersPool.getPoller();
			snmpPoller.setDevice(devices.get(device));
			
			snmpPoller.setParameter(parameters.get(0));
			
			aux = SnmpPoller.getCurrentDate() + ",";
			
			for (int j = 0; j < parameters.size(); j++) {
				snmpPoller.setParameter(parameters.get(j));
				aux = aux + snmpPoller.doSingleRequest() + ",";
			}
			
			snmpPoller.release();	
			textFileLogger.printlnToFile(devices.get(device).toString(), aux.substring(0, aux.length()-1));
		}
	}
	
	private static PollersPool pollersPool;
	private static TextFileLogger textFileLogger;
	
	private static ArrayList<Device> devices;
	private static ArrayList<Parameter> parameters;
	
	public static String path;
}
