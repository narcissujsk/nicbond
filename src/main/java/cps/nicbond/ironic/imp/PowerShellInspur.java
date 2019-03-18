package cps.nicbond.ironic.imp;


import com.jcraft.jsch.JSchException;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import cps.nicbond.ironic.other.NicDetailedRO;
import cps.nicbond.ironic.util.SSHHelperForWin;
import cps.nicbond.ironic.util.SSHResInfo;
import org.json.JSONArray;
import org.json.JSONObject
        ;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*import net.sf.json.JSONArray;
import net.sf.json.JSONObject;*/

/**
 * <b>使用管理员模式运行powershell,</br>
 * 开启远程连接 Enable-PSRemoting –force</br>
 * 信任远程主机 winrm s winrm/config/client '@{TrustedHosts="*"}'</br>
 * TeamingMode(成组模式 ): SwitchIndependent(1) Static(0) Lacp(2)</br>
 * LoadBalancingAlgorithm(负载均衡模式) : Dynamic(5) TransportPorts(0) </b>
 */
public class PowerShellInspur {
    private static final Logger logger = Logger.getLogger(PowerShellInspur.class);
    private static final String TeamingMode = "SwitchIndependent";
    private static final String LoadBalancingAlgorithm = "TransportPorts";

    public static void main(String[] args) {
        // List<NicDetailedRO> list = Get_NetAdapter("192.168.136.129", "administrator",
        // "1qaz2wsxa?");
        // System.out.println(list);
        Map<String, Object> re = New_NetLbfoTeam("192.168.136.128", "administrator", "1qaz2wsxa?", "Ethernet1",
                "Ethernet2", "nicteam");
        System.out.println(re);
    }

    /**
     * 查询网卡信息
     *
     * @param ip
     * @param username
     * @param password
     * @return
     */
    public static List<NicDetailedRO> Get_NetAdapter(String ip, String username, String password) {
        PowerShell powerShell = null;
        List<NicDetailedRO> nicslist = null;
        try {
            powerShell = PowerShell.openSession();
            logger.info("try to get nics info from " + ip + " by powershell username=" + username + " password="
                    + password);
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            // 获取本机信任主机列表
            // commamd.append("$curValue = (get-item
            // wsman:\\localhost\\Client\\TrustedHosts).value; ");
            // 如果远程主机不在列表中,添加进去
            // commamd.append("winrm set winrm/config/client '@{TrustedHosts=\"*\"}'; ");
            // commamd.append("if($curValue -contains $ip ){ }else {winrm set
            // winrm/config/client '@{TrustedHosts=\"$curValue, $ip\"}' }");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append("$nics=Invoke-Command -Session $session -Script{Get-NetAdapter}; ");
            commamd.append("$nics | ConvertTo-Json; ");
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "30000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            nicslist = new ArrayList<NicDetailedRO>();
            String nics = response.getCommandOutput().trim();
            logger.info(nics);
            if (nics.startsWith("[")) {
                JSONArray json = new JSONArray(nics); // 首先把字符串转成 JSONArray 对象
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject job = json.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        NicDetailedRO nic = new NicDetailedRO();
                        nic.setName(job.getString("InterfaceAlias"));
                        nic.setMac_address(job.getString("MacAddress"));
                        nicslist.add(nic);
                    }
                }
            }
        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return nicslist;
    }

    /**
     * TeamingMode(成组模式 ): SwitchIndependent(交换机独立)Static(静态)Lacp(Lacp)</br>
     * LoadBalancingAlgorithm(负载均衡模式) :
     * Dynamic(动态)TransportPorts(地址哈希)HyperVPort(Hyper-v端口)</br>
     * winserver2012 默认 SwitchIndependent-Dynamic
     *
     * @param ip
     * @param username
     * @param password
     * @param nic1
     * @param nic2
     * @param teamname
     * @return
     */
    public static Map<String, Object> New_NetLbfoTeam(String ip, String username, String password, String nic1,
                                                      String nic2, String teamname) {
        // $uname="administrator";
        // $ip="192.168.136.128";
        // $pwd=ConvertTo-SecureString "1qaz2wsxa?" -AsPlainText -Force;
        // $cred=New-Object System.Management.Automation.PSCredential($uname,$pwd);
        // $session = New-PsSession $ip -Credential $cred;
        // $team=Invoke-Command -Session $session -Script{ New-NetLbfoTeam –Name
        // $args[2] –TeamMembers $args[0] ,$args[1] * -Confirm:$false} -Args
        // "Ethernet1","Ethernet0","name"|;
        // $team | ConvertTo-Json;
        //
        Map<String, Object> result = new HashMap<String, Object>();
        PowerShell powerShell = null;
        String team = null;
        try {
            powerShell = PowerShell.openSession();
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append(
                    "Invoke-Command -Session $session -Script{ New-NetLbfoTeam –Name   $args[2]  –TeamMembers  $args[0] ,$args[1] -Confirm:$false} -Args \""
                            + nic1 + "\",\"" + nic2 + "\",\"" + teamname + "\" | ConvertTo-Json;");
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "60000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            team = response.getCommandOutput().trim();
            logger.info(team);
            result.put("Name", teamname);
            if (team.startsWith("{")) {
                JSONObject teamjson = new JSONObject(team);
                result.put("TeamingMode", teamjson.getString("tm"));
                result.put("LoadBalancingAlgorithm", teamjson.getString("lba"));
            }
        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return result;
    }

    /**
     * @param ip
     * @param username
     * @param password
     * @param nic1
     * @param nic2
     * @param teamname
     * @param TeamingMode            Lacp
     * @param LoadBalancingAlgorithm Dynamic
     * @return
     */
    public static Map<String, Object> New_NetLbfoTeam(String ip, String username, String password, String nic1,
                                                      String nic2, String teamname, String TeamingMode, String LoadBalancingAlgorithm) {
        // $uname="administrator";
        // $ip="192.168.136.128";
        // $pwd=ConvertTo-SecureString "1qaz2wsxa?" -AsPlainText -Force;
        // $cred=New-Object System.Management.Automation.PSCredential($uname,$pwd);
        // $session = New-PsSession $ip -Credential $cred;
        // $team=Invoke-Command -Session $session -Script{ New-NetLbfoTeam –Name
        // $args[2] –TeamMembers $args[0] ,$args[1] * -Confirm:$false} -Args
        // "Ethernet1","Ethernet0","name"|;
        // $team | ConvertTo-Json;
        //
        String psstring = " New-NetLbfoTeam –Name  " + teamname + "–TeamMembers  \"" + nic1 + "\" ,\"" + nic2
                + "\" -TeamingMode " + TeamingMode + "  -LoadBalancingAlgorithm " + LoadBalancingAlgorithm
                + " -Confirm:$false | ConvertTo-Json ";
        Map<String, Object> result = new HashMap<String, Object>();
        PowerShell powerShell = null;
        String team = null;
        try {
            powerShell = PowerShell.openSession();
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append("Invoke-Command -Session $session -Script{ " + psstring + "} ");
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "60000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            team = response.getCommandOutput().trim();
            logger.info(team);
            result.put("Name", teamname);
            if (team.startsWith("{")) {
                JSONObject teamjson = new JSONObject(team);
                result.put("TeamingMode", teamjson.getString("tm"));
                result.put("LoadBalancingAlgorithm", teamjson.getString("lba"));
            }

        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return result;
    }

    public static String Get_NetLbfoTeam(String ip, String username, String password, String teamname) {
        PowerShell powerShell = null;
        String team = null;
        String psstr = "Get-NetLbfoTeam  -Name " + teamname;
        try {
            powerShell = PowerShell.openSession();
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append("Invoke-Command -Session $session -Script{  " + psstr + " | ConvertTo-Json } ");
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "60000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            team = response.getCommandOutput().trim();
            logger.info(team);
        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return team;
    }

    public static String Remove_NetLbfoTeam(String ip, String username, String password, String nicname) {

        PowerShell powerShell = null;
        String team = null;
        try {
            powerShell = PowerShell.openSession();
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append("Invoke-Command -Session $session -Script{ Remove-NetLbfoTeam -Name " + nicname
                    + " -Confirm:$false } ");
            // Invoke-Command -Session $session -Script{ Remove-NetLbfoTeam -Name nic}
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "60000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            team = response.getCommandOutput().trim();
            logger.info(team);
        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return team;
    }

    public static String Remove_NetLbfoTeamBySSH(String ip, String username, String password, String nicname) {
        String psString = " Remove-NetLbfoTeam -Name " + nicname + " -Confirm:$false ";
        String result = null;
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd("powershell " + psString);
            result = resInfo.getOutRes().trim();
            logger.info(psString + ":" + resInfo);
            helper.close();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    /**
     * 可以设置的值TeamingMode/LoadBalancingAlgorithm</br>
     * TeamingMode(成组模式 ): SwitchIndependent(1) Static(0) Lacp(2)</br>
     * LoadBalancingAlgorithm(负载均衡模式) : Dynamic(5) TransportPorts(0)
     * HyperVPort(4)</br>
     * setMap.put("TeamingMode", "Static"); setMap.put("LoadBalancingAlgorithm",
     * "TransportPorts");</br>
     * 或者 setMap.put("TeamingMode","0"); setMap.put("LoadBalancingAlgorithm", "0");
     *
     * @param ip
     * @param username
     * @param password
     * @param setMap
     * @return
     */
    public static String Set_NetLbfoTeam(String ip, String username, String password, String name,
                                         Map<String, Object> setMap) {
        PowerShell powerShell = null;
        String team = null;
        String psstring = "Set-NetLbfoTeam    -Name " + name + " -Confirm:$false ";
        if (setMap == null || setMap.size() == 0) {
            return null;
        } else {
            if (setMap.containsKey("TeamingMode")) {
                psstring += "  -TeamingMode " + setMap.get("TeamingMode");
            }
            if (setMap.containsKey("LoadBalancingAlgorithm")) {
                psstring += "  -LoadBalancingAlgorithm " + setMap.get("LoadBalancingAlgorithm");
            }
        }
        try {
            powerShell = PowerShell.openSession();
            StringBuilder commamd = new StringBuilder();
            commamd.append("$uname=\"" + username + "\"; ");
            commamd.append("$ip=\"" + ip + "\"; ");
            commamd.append("$pwd=ConvertTo-SecureString  \"" + password + "\" -AsPlainText -Force; ");
            commamd.append("$cred=New-Object System.Management.Automation.PSCredential($uname,$pwd); ");
            commamd.append("$session = New-PsSession $ip -Credential $cred; ");
            commamd.append("Invoke-Command -Session $session -Script{" + psstring + "  } ");
            Map<String, String> myConfig = new HashMap<String, String>();
            myConfig.put("maxWait", "60000");
            myConfig.put("remoteMode", "true");
            powerShell.configuration(myConfig);
            logger.info(commamd.toString());
            PowerShellResponse response = powerShell.executeCommand(commamd.toString());
            team = response.getCommandOutput().trim();
            logger.info(team);

        } catch (PowerShellNotAvailableException e) {
            e.printStackTrace();
        } finally {
            if (powerShell != null) {
                powerShell.close();
            }
        }
        return team;
    }

    /**
     * 查询网卡信息
     *
     * @param ip
     * @param username
     * @param password
     * @return
     */
    public static List<NicDetailedRO> Get_NetAdapterBySSH(String ip, String username, String password) {
        List<NicDetailedRO> nicslist = new ArrayList<NicDetailedRO>();
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd("powershell Invoke-Expression 'Get-NetAdapter |   ConvertTo-Json'    ");
            // logger.info("ssh Res Info:" + resInfo.getOutRes().toString());
            String nics = resInfo.getOutRes().trim();
            if (nics.startsWith("[")) {
                JSONArray json = new JSONArray(nics); // 首先把字符串转成 JSONArray 对象
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject job = json.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        NicDetailedRO nic = new NicDetailedRO();
                        nic.setName(job.getString("InterfaceAlias"));
                        nic.setMac_address(job.getString("MacAddress"));
                        nicslist.add(nic);
                    }
                }
            }
            helper.close();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nicslist;
    }

    /**
     * 根据名称查找网卡绑定
     *
     * @param ip
     * @param username
     * @param password
     * @param teamname
     * @return
     */
    public static Map<String, Object> Get_NetLbfoTeamBySSH(String ip, String username, String password, String teamname) {
        String team = null;
        String psstr = "powershell Invoke-Expression 'Get-NetLbfoTeam  -Name " + teamname + " | ConvertTo-Json' ";
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("isSuccess", false);
        result.put("msg", "找不到网卡绑定" + teamname);
        logger.info(psstr);
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd(psstr);
            team = resInfo.getOutRes().trim();
            logger.info(team);
            //result.put("Name", teamname);

            if (team.startsWith("{")) {
                result.put("isSuccess", true);
                result.put("msg", "找到网卡绑定" + teamname);
                JSONObject teamjson = new JSONObject(team);
                result.put("TeamingMode", cps.nicbond.ironic.other.TeamingMode.getValue(teamjson.getInt("tm")));
                result.put("LoadBalancingAlgorithm",
                        cps.nicbond.ironic.other.LoadBalancingAlgorithm.getValue(teamjson.getInt("lba")));
                result.put("Name", teamjson.get("TeamNics"));
            }
            helper.close();
        } catch (JSchException e) {
            result.put("isSuccess", false);
            result.put("msg", "找不到网卡绑定" + teamname);
            e.printStackTrace();
        } catch (Exception e) {
            result.put("isSuccess", false);
            result.put("msg", "找不到网卡绑定" + teamname);
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    /**
     * for windows server 2012
     *
     * @param ip
     * @param username
     * @param password
     * @param nic1
     * @param nic2
     * @param teamname
     * @param TeamingMode
     * @param LoadBalancingAlgorithm
     * @return
     */
    public static Map<String, Object> New_NetLbfoTeamBySSH(String ip, String username, String password, String nic1,
                                                           String nic2, String teamname, String TeamingMode, String LoadBalancingAlgorithm) {
        String psstring = " New-NetLbfoTeam –Name  " + teamname + " –TeamMembers  '" + nic1 + "' ,'" + nic2
                + "' -TeamingMode " + TeamingMode + "  -LoadBalancingAlgorithm " + LoadBalancingAlgorithm
                + " -Confirm:$false | ConvertTo-Json ";
        Map<String, Object> result = new HashMap<String, Object>();
        logger.info("开始设置网卡绑定");
        logger.info(psstring);
        String team = null;
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd("powershell " + psstring);
            team = resInfo.getOutRes().trim();
            // logger.info(team);
            result.put("Name", teamname);
            result.put("isSuccess", true);
            result.put("msg", "设置网卡绑定成功");
            if (team.startsWith("{")) {
                JSONObject teamjson = new JSONObject(team);
                result.put("TeamingMode", cps.nicbond.ironic.other.TeamingMode.getValue(teamjson.getInt("tm")));
                result.put("LoadBalancingAlgorithm",
                        cps.nicbond.ironic.other.LoadBalancingAlgorithm.getValue(teamjson.getInt("lba")));
            }
            helper.close();
        } catch (JSchException e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        } finally {

        }
        return result;
    }

    /**
     * 设置网卡绑定的IP地址(不是静态ip)
     *
     * @param ip             管理网IP
     * @param username       用户名
     * @param password       密码
     * @param interfaceAlias 网卡名称
     * @param ipaddr         IP地址
     * @param prefixLength   子网掩码长度
     * @param defaultGateway 网关
     * @return
     */
    public static Map<String, Object> New_NetIPAddressBySSH(String ip, String username, String password,
                                                            String interfaceAlias, String ipaddr, int prefixLength, String defaultGateway) {
        // New-NetIPAddress -InterfaceAlias nics -IPAddress 192.168.1.1 -PrefixLength 24
        // -DefaultGateway 192.168.1.255
        String psstring = " New-NetIPAddress  -InterfaceAlias '" + interfaceAlias + "' -IPAddress  '" + ipaddr
                + "' -PrefixLength " + prefixLength + "  -DefaultGateway '" + defaultGateway + "'  | ConvertTo-Json ";
        Map<String, Object> result = new HashMap<String, Object>();
        logger.info(psstring);
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd("powershell " + psstring);
            logger.info(resInfo);
            int exitStuts = resInfo.getExitStuts();
            if (exitStuts == 0) {
                result.put("isSuccess", true);
                result.put("msg", "设置网卡ip成功");
            } else {
                result.put("isSuccess", false);
                result.put("msg", "设置网卡ip失败");
            }
            helper.close();
        } catch (JSchException e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        } finally {
        }
        return result;

    }

    /**
     * 设置网卡的dns
     *
     * @param ip
     * @param username       用户名
     * @param password       密码
     * @param interfaceAlias
     * @param dns
     * @return
     */
    public static Map<String, Object> Set_DnsClientServerAddressBySSH(String ip, String username, String password,
                                                                      String interfaceAlias, String dns) {
        String psstring = "  Set-DnsClientServerAddress -InterfaceAlias " + interfaceAlias + " -ServerAddresses " + dns
                + " ;$?";
        Map<String, Object> result = new HashMap<String, Object>();
        logger.info("开始设置dns InterfaceAlias=" + interfaceAlias + "  dns=" + dns + "  ip=" + ip + "  username="
                + username + "  password=" + password);
        logger.info(psstring);
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo = helper.sendCmd("powershell " + psstring);
            logger.info(resInfo);
            int exitStuts = resInfo.getExitStuts();
            if (exitStuts == 0) {
                result.put("isSuccess", true);
                result.put("msg", "设置dns成功");
            } else {
                result.put("isSuccess", false);
                result.put("msg", "设置dns失败");
            }
            helper.close();
        } catch (JSchException e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            result.put("isSuccess", true);
            result.put("msg", e.getMessage());
            e.printStackTrace();
        }
        return result;

    }

    /**
     * @param ip
     * @param username
     * @param password
     * @param interfaceAlias
     * @param ipaddr
     * @param subnetMask
     * @param defaultGateway
     * @return
     */
    public static Map<String, Object> setStaticIP(String ip, String username, String password, String interfaceAlias,
                                                  String ipaddr, String subnetMask, String defaultGateway) {
        String cmdString = "netsh interface ip set address " + interfaceAlias + " static " + ipaddr + "  " + subnetMask
                + "  " + defaultGateway + "  1";
        logger.info("开始设置ip " + "  ip=" + ip + "  username=" + username + "  password=" + password + "  interfaceAlias="
                + interfaceAlias + "  ipaddr=" + ipaddr + "  subnetMask=" + subnetMask + " gateway" + defaultGateway);
        logger.info(cmdString);
        Map<String, Object> result = new HashMap<String, Object>();
        int exitStuts = -1;
        try {
            SSHHelperForWin helper = new SSHHelperForWin(ip, 22, username, password);
            SSHResInfo resInfo;
            resInfo = helper.sendCmd(cmdString);

            exitStuts = resInfo.getExitStuts();
            if (exitStuts == 0) {
                result.put("isSuccess", true);
                result.put("msg", "设置网卡ip成功");
            } else {
                result.put("isSuccess", false);
                result.put("msg", "设置网卡ip失败");
            }
            helper.close();
        } catch (Exception e) {
            result.put("isSuccess", false);
            result.put("msg", "设置网卡ip失败");
            e.printStackTrace();
        }
        return result;

        // netsh interface ip set address ebond0 static 172.23.60.206 255.255.255.0
        // 172.23.60.254 1
    }

    /**
     * winserver 2012 设置网卡绑定并设置IP地址
     *
     * @param paraMap
     * @return
     */
    public static Map<String, Object> setBondAndIp(Map<String, Object> paraMap) {
        logger.info("setBondForWinServer:" + paraMap);
        Map<String, Object> result = new HashMap<String, Object>();
        String sship = (String) paraMap.get("sship");// 管理网IP
        String password = (String) paraMap.get("password");// 用户名administrator
        String username = (String) paraMap.get("username");// 密码
        String ipaddr = (String) paraMap.get("ipaddr");
        String netmask = (String) paraMap.get("netmask");
        String gateway = (String) paraMap.get("gateway");
        String dns = (String) paraMap.get("dns");
        String mac1 = (String) paraMap.get("mac1");
        mac1 = mac1.replaceAll(":", "-");
        //windows系统mac地址分隔符是"-" linux系统mac地址分隔符为":"
        String mac2 = (String) paraMap.get("mac2");
        mac2 = mac2.replaceAll(":", "-");
        String nic1 = null;
        String nic2 = null;
        boolean foundMac1 = false;
        boolean foundMac2 = false;

        /**
         * 获取服务器上的所有网卡 失败直接返回,成功继续执行
         */
        List<NicDetailedRO> nics = Get_NetAdapterBySSH(sship, username, password);
        if (nics == null || nics.size() == 0) {
            result.put("isSuccess", false);
            result.put("msg", "cannot Get_NetAdapterBySSH ");
            return result;
        } else {
            logger.info("Get_NetAdapterBySSH:" + nics);
        }

        /**
         * 根据参数中的mac地址,找到对应的网卡名称 失败直接返回,成功继续执行
         */
        for (NicDetailedRO nicDetailedRO : nics) {
            String mac = nicDetailedRO.getMac_address();
            if (mac.equalsIgnoreCase(mac1)) {
                nic1 = nicDetailedRO.getName();
                foundMac1 = true;
            } else if (mac.equalsIgnoreCase(mac2)) {
                nic2 = nicDetailedRO.getName();
                foundMac2 = true;
            }

        }
        if (foundMac1 && foundMac2) {
            logger.info("foundNic:" + nic1 + " " + nic2);
            result.put("msg", " found nic by macaddress ");
        } else {
            result.put("isSuccess", false);
            result.put("msg", "cannot found nic by macaddress ");
            return result;
        }
        /**
         * 设置网卡绑定
         */
        Map<String, Object> nicbondResult = New_NetLbfoTeamBySSH(sship, username, password, nic1, nic2, "ebond0",
                TeamingMode, LoadBalancingAlgorithm);
        result.putAll(nicbondResult);
        boolean isSuccess = (Boolean) nicbondResult.get("isSuccess");
        if (isSuccess) {
            logger.info("网卡绑定创建成功");
            /**
             * 创建网卡绑定完成后,设置静态IP地址
             */
            Map<String, Object> setIPResult = setStaticIP(sship, username, password, "ebond0", ipaddr, netmask, gateway);
            isSuccess = (Boolean) nicbondResult.get("isSuccess");
            result.putAll(setIPResult);
            if (isSuccess) {
                logger.info("设置静态IP地址成功:" + setIPResult);
                Set_DnsClientServerAddressBySSH(sship, username, password, "ebond0", dns);
            } else {
                logger.error("setStaticIP error:" + setIPResult);
            }

        } else {
            logger.error("New_NetLbfoTeamBySSH error:" + nicbondResult);
        }
        return result;

    }
}
