package cps.nicbond.ironic.other;


import org.json.JSONObject;

public class LinkRO {
	private String href;
	private String rel;
	public LinkRO() {
		super();
		// TODO Auto-generated constructor stub
		this.href = null;
		this.rel = null;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}
	
	public void getLinkRO(JSONObject link)
	{
		if (!link.isNull("href")) {
			this.setHref(link.getString("href"));
		}
		if (!link.isNull("rel")) {
			this.setRel(link.getString("rel"));
		}
	}
}
