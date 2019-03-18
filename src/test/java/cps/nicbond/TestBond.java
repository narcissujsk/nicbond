package cps.nicbond;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
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
        String ip = "192.168.195.158";
        String username = "administrator";
        String password = "123456a?";
        List<NicDetailedRO> nics = PowerShellInspur.Get_NetAdapter(ip, username, password);
        System.out.println(nics);
    }
    @Test
    public void getNic3() {
        PowerShellResponse response = PowerShell.executeSingleCommand("Get-Process");

        System.out.println(response.getCommandOutput());
    }

}
