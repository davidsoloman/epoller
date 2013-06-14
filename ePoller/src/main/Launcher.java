/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pojos.Parameter;
import snmpstuff.SnmpCollector;

import storage.TextFileLogger;

import xml.CsvImporter;

public class Launcher {

	public static void main(String[] args) {

		try {
			
			Properties properties = new Properties();
			properties.load(new FileInputStream("configuration.properties"));

			
			System.out.println("Reading devices file ... ");
			devices = CsvImporter.getDevices2("devices.csv");
			System.out.println("done. " + devices.size() + " devices found.");
			
			
			System.out.print("Reading parameters file ... ");
			parameters = CsvImporter.getParameters("parameters.csv");
			System.out.println("done. " + parameters.size() + " parameters found.");
			

			if (devices.size() < 1 || parameters.size() < 1) {
				System.out.println("For this programan to work, you need at least one parameter and one device. Bye.");
				System.exit(0);
			} else {
				System.out.print("Initializing output files ... ");
				TextFileLogger.initOutputFiles(devices, parameters);
				System.out.println("done.");

				snmpCollector= new SnmpCollector();

				System.out.print("Scheduling the queries ... ");

				long frequency = Long.parseLong(properties.getProperty("frequency"));
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
				int counter = 1;
				int request_interval = Integer.parseInt(properties.getProperty("request_interval"));
				
				for(String ip: devices.keySet())
				{
					executor.scheduleAtFixedRate(new customTimerTask(ip), counter * request_interval, frequency, TimeUnit.MILLISECONDS);
					counter++;
				}
				
				System.out.println("ePoller started succesfully.");
				
				/*System.out.println("Starting trap receiver ...");
				SnmpTrapReceiver multithreadedtrapreceiver = new SnmpTrapReceiver();
				multithreadedtrapreceiver.run();
				System.out.println("Done.");*/
				
			}
		} catch (IOException e) {
			System.out.println("Are all the required configuration files present?.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static class customTimerTask implements Runnable {

		private String ip;
		private String aux;

		public customTimerTask(String device) {
			ip = device;
		}

		@Override
		public void run() {

			try {
				snmpCollector.setDevice(ip);
				aux = SnmpCollector.getCurrentDate() + ",";

				for (int j = 0; j < parameters.size(); j++) {
					snmpCollector.setParameter(parameters.get(j));
					aux = aux + snmpCollector.doSingleRequest() + ",";
				}

				TextFileLogger.printlnToFile(devices.get(ip).toString(), aux.substring(0, aux.length() - 1));
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static SnmpCollector snmpCollector;

	private static HashMap<String, String> devices;
	private static ArrayList<Parameter> parameters;
}
