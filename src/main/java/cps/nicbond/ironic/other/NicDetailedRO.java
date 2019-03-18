package cps.nicbond.ironic.other;

import org.json.JSONObject
;

public class NicDetailedRO {
	private String lldp;
	private String product;
	private String vendor;
	private String name;
	private boolean has_carrier;
	private String switch_port_descr;
	private String switch_chassis_descr;
	private String ipv4_address;
	private String client_id;
	private String mac_address;
	private JSONObject NicDetailedROJSONObject;

	/*
	 * 
	 * "lldp": null, "product": "0x10fb", "vendor": "0x8086", "name": "eth3",
	 * "has_carrier": false, "ipv4_address": null, "biosdevname": null,
	 * "client_id": null, "mac_address": "00:1b:21:89:2a:c5"
	 * 
	 */
	public String getLldp() {
		return lldp;
	}

	public void setLldp(String lldp) {
		this.lldp = lldp;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the has_carrier
	 */
	public boolean isHas_carrier() {
		return has_carrier;
	}

	/**
	 * @param has_carrier
	 *            the has_carrier to set
	 */
	public void setHas_carrier(boolean has_carrier) {
		this.has_carrier = has_carrier;
	}

	public String getSwitch_port_descr() {
		return switch_port_descr;
	}

	public void setSwitch_port_descr(String switch_port_descr) {
		this.switch_port_descr = switch_port_descr;
	}

	public String getSwitch_chassis_descr() {
		return switch_chassis_descr;
	}

	public void setSwitch_chassis_descr(String switch_chassis_descr) {
		this.switch_chassis_descr = switch_chassis_descr;
	}

	public String getIpv4_address() {
		return ipv4_address;
	}

	public void setIpv4_address(String ipv4_address) {
		this.ipv4_address = ipv4_address;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getMac_address() {
		return mac_address;
	}

	public JSONObject getNicDetailedROJSONObject() {
		return NicDetailedROJSONObject;
	}

	public void setNicDetailedROJSONObject(JSONObject nicDetailedROJSONObject) {
		NicDetailedROJSONObject = nicDetailedROJSONObject;
	}

	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}

	



	@Override
	public String toString() {
		return "NicDetailedRO [name=" + name + ", ipv4_address=" + ipv4_address + ", mac_address=" + mac_address + "]";
	}

	public void getNicDetailedRO(JSONObject nicInf) {
		this.setNicDetailedROJSONObject(nicInf);
		if (!(nicInf.isNull("lldp"))) {
			this.setLldp(nicInf.getString("lldp"));
		}
		if (!(nicInf.isNull("product"))) {
			this.setProduct(nicInf.getString("product"));
		}

		if (!(nicInf.isNull("vendor"))) {
			this.setVendor(nicInf.getString("vendor"));
		}
		if (!(nicInf.isNull("name"))) {
			this.setName(nicInf.getString("name"));
		}
		if (!(nicInf.isNull("has_carrier"))) {
			this.setHas_carrier(nicInf.getBoolean("has_carrier"));
		}
		if (!(nicInf.isNull("switch_port_descr"))) {
			this.setSwitch_port_descr(nicInf.getString("switch_port_descr"));
		}
		if (!(nicInf.isNull("switch_chassis_descr"))) {
			this.setSwitch_chassis_descr(nicInf.getString("switch_chassis_descr"));
		}
		if (!(nicInf.isNull("ipv4_address"))) {
			this.setIpv4_address(nicInf.getString("ipv4_address"));
		}
		if (!(nicInf.isNull("client_id"))) {
			this.setClient_id(nicInf.getString("client_id"));
		}
		if (!(nicInf.isNull("mac_address"))) {
			this.setMac_address(nicInf.getString("mac_address"));
		}
	}
}
