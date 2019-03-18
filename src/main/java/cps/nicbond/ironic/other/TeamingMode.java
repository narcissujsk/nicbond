package cps.nicbond.ironic.other;

/**
 * 
 * windows server 2012 网卡绑定的成组模式
 * 可用的有三个值 </br>
 * TeamingMode(成组模式 ): SwitchIndependent(1) Static(0) Lacp(2)</br>
 * The Windows PowerShell options for teaming mode are:</br>
•	SwitchIndependent</br>
•	Static</br>
•	LACP</br>

 *主备模式可使用Switch Independent configuration / Address Hash distribution
 *
 */
public enum TeamingMode {
	STATIC("Static", 0, "静态成组"),
	SWITCHINDEPENDENT("SwitchIndependent", 1, "交换机独立"),
	LACP("LACP", 2, "链路聚合");
	private String name;
	private String value;
	private int num;

	TeamingMode(String value, int num, String name){
		this.setValue(value);
		this.setNum(num);
		this.setName(name);
	}
	
	 public String toString() {  
         return value;  
     }  

	public String getValue() {
		return value;
	}
	public static String getValue(int num){
		for(TeamingMode c : TeamingMode.values()){
			if(c.getNum()==num){
				return c.getValue();
			}
		}
		return "Lacp";
	}
	public static int getNum(String value){
		for(TeamingMode c : TeamingMode.values()){
			if(c.getValue().equals(value)){
				return c.getNum();
			}
		}
		return 2;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num2) {
		this.num = num2;
	}

	
}
