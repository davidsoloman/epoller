/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package snmpstuff;

import org.apache.commons.pool.BasePoolableObjectFactory;

public class SnmpPollerFactory extends BasePoolableObjectFactory<SnmpPoller>{

    @Override
    public SnmpPoller makeObject() throws Exception {
	SnmpPoller snmpPoller= new SnmpPoller();
	snmpPoller.setup();
	return snmpPoller;
    }

}
