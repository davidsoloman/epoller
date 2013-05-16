/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package snmpstuff;

import org.apache.commons.pool.BasePoolableObjectFactory;

import storage.TextFileLogger;

public class SnmpPollerFactory extends BasePoolableObjectFactory<SnmpPoller> {
	
	public SnmpPollerFactory() {
	}

	@Override
	public SnmpPoller makeObject() throws Exception {
		TextFileLogger.printlnToFile(POOL_LOG, SnmpPoller.getCurrentDate()+", creating an SnmpPoller");
		SnmpPoller snmpPoller = new SnmpPoller();
		snmpPoller.setup();
		return snmpPoller;
	}
	
	@Override
	public void activateObject(SnmpPoller obj) throws Exception {
		TextFileLogger.printlnToFile(POOL_LOG, SnmpPoller.getCurrentDate()+", activating an SnmpPoller");
		super.activateObject(obj);
		
	}
	
	@Override
	public void destroyObject(SnmpPoller obj) throws Exception {
		TextFileLogger.printlnToFile(POOL_LOG, SnmpPoller.getCurrentDate()+", destroying an SnmpPoller");
		super.destroyObject(obj);
	}
	
	@Override
	public void passivateObject(SnmpPoller obj) throws Exception {
		TextFileLogger.printlnToFile(POOL_LOG, SnmpPoller.getCurrentDate()+", pasivating an SnmpPoller");
		super.passivateObject(obj);
	}
	
	@Override
	public boolean validateObject(SnmpPoller obj) {
		TextFileLogger.printlnToFile(POOL_LOG, SnmpPoller.getCurrentDate()+", validating an SnmpPoller");
		return super.validateObject(obj);
	}
	
	public static final String POOL_LOG="pool_log";
}
