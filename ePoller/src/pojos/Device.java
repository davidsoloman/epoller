/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package pojos;

public class Device {

    private String ip, name;

    public Device() {
    }

    public String getIp() {
	return ip;
    }

    public void setIp(String ip) {
	this.ip = ip;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	if (name.length() > 0)
	    this.name = name;
    }

    @Override
    public String toString() {
	if (null != name)
	    return name;
	else
	    return ip;
    }

}
