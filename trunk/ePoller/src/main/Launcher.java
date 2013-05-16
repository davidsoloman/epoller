/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

import pojos.Parameter;
import pojos.Device;
import snmpstuff.SnmpPoller;
import snmpstuff.SnmpPollerFactory;
import snmpstuff.SnmpTrapReceiver;
import storage.TextFileLogger;

import xml.CsvImporter;

public class Launcher {

	public static void main(String[] args) {

		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("configuration.properties"));

			System.out.println("Reading devices file ... ");
			devices = CsvImporter.getDevices("devices.csv");
			System.out.println("done. " + devices.size() + " devices found.");
			System.out.print("Reading parameters file ... ");
			parameters = CsvImporter.getParameters("parameters.csv");
			System.out.println("done. " + parameters.size() + " parameters found.");

			if (devices.size() < 1 || parameters.size() < 1) {
				System.out.println("For this programan to work, you need at least one parameter and one device.");
				System.out.println("Bye.");
				System.exit(0);
			} else {
				System.out.print("Initializing output files ... ");
				TextFileLogger.initOutputFiles(devices, parameters);
				System.out.println("done.");

				System.out.print("Setting up the pollers pool ... ");
				pool = new StackObjectPool<SnmpPoller>(new SnmpPollerFactory());
				System.out.println("done.");

				System.out.print("Scheduling the queries ... ");

				long frequency = Long.parseLong(properties.getProperty("frequency"));
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(20);
				int counter = 1;
				int request_interval = Integer.parseInt(properties.getProperty("request_interval"));
				for (int i = 0; i < devices.size(); i++) {
					executor.scheduleAtFixedRate(new customTimerTask(i), counter * request_interval, frequency, TimeUnit.MILLISECONDS);
					counter++;
				}
				System.out.println("ePoller started succesfully.");
				
				System.out.println("Starting trap receiver ...");
				SnmpTrapReceiver multithreadedtrapreceiver = new SnmpTrapReceiver();
				multithreadedtrapreceiver.run();
				System.out.println("Done.");
				
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

			try {
				snmpPoller = pool.borrowObject();
				TextFileLogger.printlnToFile(SnmpPollerFactory.POOL_LOG, SnmpPoller.getCurrentDate()+", active objects in pool: "+pool.getNumActive());
				TextFileLogger.printlnToFile(SnmpPollerFactory.POOL_LOG, SnmpPoller.getCurrentDate()+", idle objects in pool: "+pool.getNumIdle());
				snmpPoller.setDevice(devices.get(device));

				snmpPoller.setParameter(parameters.get(0));

				aux = SnmpPoller.getCurrentDate() + ",";

				for (int j = 0; j < parameters.size(); j++) {
					snmpPoller.setParameter(parameters.get(j));
					aux = aux + snmpPoller.doSingleRequest() + ",";
				}

				pool.returnObject(snmpPoller);
				TextFileLogger.printlnToFile(devices.get(device).toString(), aux.substring(0, aux.length() - 1));
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static ObjectPool<SnmpPoller> pool;

	private static ArrayList<Device> devices;
	private static ArrayList<Parameter> parameters;
}