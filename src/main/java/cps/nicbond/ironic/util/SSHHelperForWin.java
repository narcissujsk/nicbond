package cps.nicbond.ironic.util;

import com.jcraft.jsch.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.nio.charset.Charset;

/**   java ssh登录linux以后的一些操作方式
* 
* @author 
* @date 
* @version V1.0   
*/
public class SSHHelperForWin{
     private final static Log logger = LogFactory.getLog(SSHHelperForWin.class);

     private String     charset = Charset.defaultCharset().toString();
     private Session session;

     public SSHHelperForWin(String host, Integer port, String user, String password) throws JSchException {
         connect(host, port, user, password);
     }

     /**
         * 连接sftp服务器
         * @param host 远程主机ip地址
         * @param port sftp连接端口，null 时为默认端口
         * @param user 用户名
         * @param password 密码
         * @return
         * @throws JSchException 
         */
        private Session connect(String host, Integer port, String user, String password) throws JSchException{
            try {
                JSch jsch = new JSch();
                if(port != null){
                    session = jsch.getSession(user, host, port.intValue());
                }else{
                    session = jsch.getSession(user, host);
                }
                session.setPassword(password);
                //设置第一次登陆的时候提示，可选值:(ask | yes | no)
                session.setConfig("StrictHostKeyChecking", "no");
                //30秒连接超时
                session.connect(60000);



            } catch (JSchException e) {
                e.printStackTrace();
                System.out.println("SFTPUitl 获取连接发生错误");
                throw e;
            }
            return session;
        }

        public SSHResInfo sendCmd(String command) throws Exception{
            return sendCmd(command, 200);
        }
        /*
        * 执行命令，返回执行结果
        * @param command 命令
        * @param delay 估计shell命令执行时间
        * @return String 执行命令后的返回
        * @throws IOException
        * @throws JSchException
        */
        public SSHResInfo sendCmd(String command,int delay) throws Exception{
            if(delay <50){
                delay = 50;
            }
            SSHResInfo result = null;
            byte[] tmp = new byte[1024]; //读数据缓存
            StringBuffer strBuffer = new StringBuffer();  //执行SSH返回的结果
            StringBuffer errResult=new StringBuffer();

            Channel channel = session.openChannel("exec");
            ChannelExec ssh = (ChannelExec) channel;
            //返回的结果可能是标准信息,也可能是错误信息,所以两种输出都要获取
            //一般情况下只会有一种输出.
            //但并不是说错误信息就是执行命令出错的信息,如获得远程java JDK版本就以
            //ErrStream来获得.
            InputStream stdStream = ssh.getInputStream();
            InputStream errStream = ssh.getErrStream();  

            ssh.setCommand(command);
            ssh.connect();

            try {


                //开始获得SSH命令的结果
                while(true){
                //获得错误输出
                    while(errStream.available() > 0){
                        int i = errStream.read(tmp, 0, 1024);
                        if(i < 0) break;
                        //errResult.append(new String(tmp, 0, i));
                       errResult.append(new String(tmp, 0, i,"GBK"));
                    }

                   //获得标准输出
                    while(stdStream.available() > 0){
                        int i = stdStream.read(tmp, 0, 1024);
                        if(i < 0) break;
                       //strBuffer.append(new String(tmp, 0, i));
                       strBuffer.append(new String(tmp, 0, i,"GBK"));
                    }
                    if(ssh.isClosed()){
                        int code = ssh.getExitStatus();
                        logger.info("exit-status: " + code);
                        result = new SSHResInfo(code, strBuffer.toString(), errResult.toString());
                        break;
                    }
                    try
                    {
                        Thread.sleep(delay);
                    }
                    catch(Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
            } finally {
                channel.disconnect();
            }

            return result;
        }

        /**
         * @param in
         * @param charset
         * @return
         * @throws IOException
         * @throws UnsupportedEncodingException
         */
        private String processStream(InputStream in, String charset) throws Exception {
            byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while (in.read(buf) != -1) {
                sb.append(new String(buf, charset));
            }
            return sb.toString();
        }

        public boolean deleteRemoteFIleOrDir(String remoteFile){
            ChannelSftp channel=null;  
             try {  
                 channel=(ChannelSftp) session.openChannel("sftp");  
                 channel.connect();  
                 SftpATTRS sftpATTRS= channel.lstat(remoteFile);  
                 if(sftpATTRS.isDir()){  
                     //目录  
                     logger.debug("remote File:dir");  
                     channel.rmdir(remoteFile);
                     return true;  
                 }else if(sftpATTRS.isReg()){  
                     //文件  
                     logger.debug("remote File:file");  
                     channel.rm(remoteFile);  
                     return true;  
                 }else{  
                     logger.debug("remote File:unkown");  
                     return false;  
                 }  
             }catch (JSchException e) {  
                 if(channel!=null){  
                     channel.disconnect();  
                     session.disconnect();  
                 }
                 logger.error("error",e);  
                return  false;  
             } catch (SftpException e) { 
                 logger.info("meg"+e.getMessage());
                 logger.error("SftpException",e);  
                 return false;
             }  

        }

        /** 
         * 判断linux下 某文件是否存在 
         */  
     public boolean detectedFileExist(String remoteFile) {  

     ChannelSftp channel=null;  
     try {  
         channel=(ChannelSftp) session.openChannel("sftp");  
         channel.connect();  
         SftpATTRS sftpATTRS= channel.lstat(remoteFile);  
         if(sftpATTRS.isDir()||sftpATTRS.isReg()){  
             //目录 和文件  
             logger.info("remote File:dir");  
             return true;  
         }else{  
             logger.info("remote File:unkown");  
             return false;  
         }  
     }catch (JSchException e) {  
         if(channel!=null){  
             channel.disconnect();  
             session.disconnect();  
     }  
        return  false;  
     } catch (SftpException e) {  
         logger.error(e.getMessage());  
     }  
     return false;  
     } 



     /**用完记得关闭，否则连接一直存在，程序不会退出
     * 
     */
    public void close(){
        if(session.isConnected())
        session.disconnect();
     }


}

/*
public class Test {

    public static void main(String[] args) {
        testSSH();
    }

    public static void testSSH(){
        try {
            //使用目标服务器机上的用户名和密码登陆
            SSHHelper helper = new SSHHelper(ip, 22, linux_username, linux_password);
            String command = "echo hello world!";
            try {
                SSHResInfo resInfo = helper.sendCmd(command);
                System.out.println(resInfo.toString());
                //System.out.println(helper.deleteRemoteFIleOrDir(command));
                //System.out.println(helper.detectedFileExist(command));
                helper.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
    }*/