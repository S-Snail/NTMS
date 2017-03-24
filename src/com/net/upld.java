package com.net;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.ntms.baseFun;

public class upld extends  Thread {

    private FTPClient   client = null;
    private FtpListener ftpListener = null;

    private String fileName=null;
    private String svrAddr=null;
    private String user=null;
    private String passwd=null;
    private int    port=21;

	public upld(String fileName,String svrAddr,String user,String passwd,int port) {
		this.fileName=fileName;
		this.svrAddr=svrAddr;
		this.user=user;
		this.passwd=passwd;
		this.port=port;
	}
    
/*   
新闻:     xxx一段字符串
天气:     2016年01月05日 星期二 小雨 6℃~9℃
*/

    @Override
    public void run() {
    	
    	if(fileName==null || svrAddr==null || user==null || passwd==null){
    		return;
    	}
        try {
        	if(!fileName.endsWith(".txt")){
        		baseFun.runCmd("/system/bin/screecap -p "+fileName);
        		if(baseFun.checkFile(fileName)==false){
        			Log.i("ftp","capture screen fail....");
        			return;
        		}
        	}

        	client = new FTPClient();
 	        client.setPassive(true);
 	        client.setAutoNoopTimeout(30000);	            	
	        try{
	            if(client.isConnected()){
	                client.disconnect(false);
	            }
	            client.connect(svrAddr,port);
	            client.login(user, passwd);
	            client.changeDirectory(fileName.endsWith(".txt")?"/resourceFolder/text/":"/resourceFolder/image/");
	            File file = new File(fileName);

	            if(file.exists()){
	            	client.upload(file, 0, new MyTransferListener());
	            }
	            
	        } catch (IOException e) { 
	            e.printStackTrace(); 
	        } finally { 
	            if(client.isConnected()) { 
	                try { 
	                	client.disconnect(true); 
	                } catch (IOException ioe) { 
	                } 
	            } 
	        }
        }catch ( Exception e) {
            e.printStackTrace();
        }
    }

    public class MyTransferListener implements FTPDataTransferListener {

        public void started() {
        	
        }
        public synchronized void transferred(int length) {
        	
        }
        public void completed() {
            if( ftpListener != null ) {
                ftpListener.OnUpldFinished(true);
            }
        }
        public void aborted() {
        	
        }
        public void failed() {
        	
        }
    }
    
    public void setFtpListener(FtpListener l) {
        ftpListener = l;
    }

    interface FtpListener {
        public void OnUpldFinished(boolean ok);
    }	    
}
