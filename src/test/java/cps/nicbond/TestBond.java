package cps.nicbond;

import cps.nicbond.ironic.imp.PowerShellInspur;
import cps.nicbond.ironic.other.NicDetailedRO;
import org.junit.Test;

import java.util.List;

/**
 * @program: nicbond
 * @description:
 * @author: jiangsk@inspur.com
 * @create: 2019-03-18 11:27
 **/
public class TestBond {

    @Test
    public void getNic() {
        String ip = "192.168.136.139";
        String username = "administrator";
        String password = "Lc13yfwpW";
        List<NicDetailedRO> nics = PowerShellInspur.Get_NetAdapter(ip, username, password);
        System.out.println(nics);
    }
}
