package cps.nicbond.ironic.other;

/**
 * 
 * windows server 2012 网卡绑定的负载平衡模式
 * 
 * LoadBalancingAlgorithm(负载均衡模式) : Dynamic(5) TransportPorts(0) HyperVPort(4)
 *  * 主备模式可使用Switch Independent configuration / Address Hash distribution
 *
 */
public enum LoadBalancingAlgorithm {
	
	TRANSPORTPORTS("TransportPorts", 0, "地址哈希"),
	HYPERVPORT("HyperVPort", 4, "Hyper-v端口"),
	IPADDRESSES("IPAddresses",-1, "IPAddresses"),
	MACADDRESSES("MacAddresses",-1, "MacAddresses"),
	DYNAMIC("Dynamic", 5, "动态");
	
	private String name;
	private String value;
	private int num;

	LoadBalancingAlgorithm(String value, int num, String name){
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
		for(LoadBalancingAlgorithm c : LoadBalancingAlgorithm.values()){
			if(c.getNum()==num){
				return c.getValue();
			}
		}
		return "TransportPorts";
	}
	public static int getNum(String value){
		for(LoadBalancingAlgorithm c : LoadBalancingAlgorithm.values()){
			if(c.getValue().equals(value)){
				return c.getNum();
			}
		}
		return 1;
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
