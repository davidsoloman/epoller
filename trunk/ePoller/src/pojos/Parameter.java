/*
 * @author Julian Peña - julian.orlando.pena@gmail.com
 */

package pojos;

public class Parameter {
	
	private String name, oid;
	
	public Parameter() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}
	
	@Override
	public String toString() {
		return name+","+oid;
	}

}
