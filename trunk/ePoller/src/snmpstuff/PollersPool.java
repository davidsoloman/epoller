/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package snmpstuff;

import java.io.IOException;
import java.util.ArrayList;

/*
 * WARNING!!!!
 * This is a primitive implementation of an object pool that is going to be replaced soon,
 * if the pool size is too small for your network then ePoller will hang!
 */
public class PollersPool {
	
	private ArrayList<SnmpPoller> pollers;
	private int poolSize, lastPollerUsed;
	
	public PollersPool(int size) throws IOException {
		
		poolSize=size;
		pollers= new ArrayList<SnmpPoller>(0);
		
		for(int i=0;i<poolSize; i++)
		{
			SnmpPoller snmpPoller= new SnmpPoller();
			snmpPoller.setup();
			pollers.add(snmpPoller);
		}
	}
	
	public SnmpPoller getPoller()
	{
		do{
			if(!(lastPollerUsed<poolSize))
				lastPollerUsed=0;
			
			if(pollers.get(lastPollerUsed).isBusy()==false)
				return pollers.get(lastPollerUsed);
			
			lastPollerUsed++;
		}while(true);
	}
	
	public int getPoolSize()
	{
		return poolSize;
	}

}
