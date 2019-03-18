package cps.nicbond;

import cps.nicbond.ironic.imp.PowerShellInspur;
import cps.nicbond.ironic.other.NicDetailedRO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NicbondApplicationTests {

    @Test
    public void contextLoads() {
    }


    @Test
    public void setNicTeam() {
        String ip = "192.168.136.128";
        String username = "administrator";
        String password = "1qaz2wsxa?";
        String name = "nics";
        Map<String, Object> map = new HashMap<String, Object>();
        // map.put("TeamingMode", "Static");
        // map.put("LoadBalancingAlgorithm", "TransportPorts");
        map.put("TeamingMode", "0");
        map.put("LoadBalancingAlgorithm", "0");
        String nics = PowerShellInspur.Set_NetLbfoTeam(ip, username, password, name, map);
        System.out.println(nics);
    }

    @Test
    public void getNic() {
        String ip = "192.168.136.139";
        String username = "administrator";
        String password = "Lc13yfwpW";
        List<NicDetailedRO> nics = PowerShellInspur.Get_NetAdapter(ip, username, password);
        System.out.println(nics);
    }

    @Test
    public void getNicBySSH() {
        String ip = "192.168.136.143";
        String username = "administrator";
        String password = "Lc13yfwpW";
        List<NicDetailedRO> nics = PowerShellInspur.Get_NetAdapterBySSH(ip, username, password);
        System.out.println(nics);
    }

    @Test
    public void setInhostBondAndIp2() {
        // 6c:92:bf:22:f4:f9
        // mac2=00:e0:ed:40:40:34,
        // mac1=6c:92:bf:22:f4:f9,
        // password=Lc13yfwpW,
        // sship=172.23.45.206,
        // netmask=255.255.255.0,
        // isSetBondIp=yes,
        // bondMode=1,
        // dns=114.114.114.114,
        // ipaddr=10.0.7.99,
        // gateway=172.23.60.254,
        // username=administrator

        String sship = "172.23.45.206";
        String username = "administrator";
        String password = "Lc13yfwpW";
        String netmask = "255.255.255.0";
        String gateway = "172.23.60.254";
        String ipaddr = "172.23.60.207";
        String dns = "114.114.114.114";
        // String mac1 = "00-50-56-AF-7A-91";
        // String mac2 = "00-50-56-AF-12-F0";
        String mac1 = "6c:92:bf:22:f4:f9";
        String mac2 = "00:e0:ed:40:40:34";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("sship", sship);
        map.put("username", username);
        map.put("password", password);
        map.put("netmask", netmask);
        map.put("gateway", gateway);
        map.put("ipaddr", ipaddr);
        map.put("dns", dns);
        map.put("mac1", mac1);
        map.put("mac2", mac2);
        Map<String, Object> re = PowerShellInspur.setBondAndIp(map);
        System.out.println(re);

    }

}
