/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import snmpstuff.SnmpPoller;
import util.TextWriter;



public class Launcher {

	public static void main(String[] args) {

		try {
			
			Properties properties = new Properties();
			properties.load(new FileInputStream("configuration.properties"));

			
			System.out.println("Reading devices file ... ");
			DeviceManager.loadDevices();
			System.out.println(DeviceManager.devices.size() + " devices found.");
			
			
			System.out.print("Reading parameters file ... ");
			DeviceManager.loadParameters();
			System.out.println(DeviceManager.parameters.size() + " parameters found.");
			

			if (DeviceManager.devices.size() < 1 || DeviceManager.parameters.size() < 1) {
				System.out.println("For this programan to work, you need at least one parameter and one device. Bye.");
				System.exit(0);
			} else {
				System.out.print("Initializing output files ... ");
				TextWriter.initOutputFiles(DeviceManager.devices, DeviceManager.parameters);
				System.out.println("done.");

				snmpCollector= new SnmpPoller();

				System.out.print("Scheduling the queries ... ");

				long frequency = Long.parseLong(properties.getProperty("frequency"));
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
				int counter = 1;
				int request_interval = Integer.parseInt(properties.getProperty("request_interval"));
				
				for(String ip: DeviceManager.devices.keySet())
				{
					executor.scheduleAtFixedRate(new customTimerTask(ip), counter * request_interval, frequency, TimeUnit.MILLISECONDS);
					counter++;
				}
				
				System.out.println("ePoller started succesfully.");
				
			}
		} catch (IOException e) {
			System.out.println("Are all the required configuration files present?.");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*System.out.println("Starting trap receiver ...");
		SnmpTrapReceiver multithreadedtrapreceiver = new SnmpTrapReceiver();
		multithreadedtrapreceiver.run();
		System.out.println("Done.");*/
	}

	private static class customTimerTask implements Runnable {

		private String deviceIP;

		public customTimerTask(String device) {
			deviceIP = device;
		}

		@Override
		public void run() {

			try {
				snmpCollector.setDevice(deviceIP);

				for (int j = 0; j < DeviceManager.parameters.size(); j++) {
					snmpCollector.setParameter(DeviceManager.parameters.get(j));
					snmpCollector.doSingleRequest();
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static SnmpPoller snmpCollector;
}
