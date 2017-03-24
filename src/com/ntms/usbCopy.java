package com.ntms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.cfg.cfg;
import com.list.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;


@SuppressLint("SdCardPath")
public class usbCopy extends Thread{
	
	public static class copySta{	
		public int    runing=0;		
	    public int    copySum=0;  
	    public int    copyNum=0; 
	    public int    copyProg=0;
	    public String copyName=null;    
	    public String copyPath=null;  
	    public String copyTitle=null; 
	}
	
	public  static copySta           cpySta=new copySta();
	private static usbCopy           cpyThread=null;
	private static String            mPath=null;
	public  static ArrayList<String> rmList=null;
    private static List<String>      storagePath=null;
	
	public static usbCopy newInstance(Context context,String pathStr) {
		baseFun.extPath=pathStr;
		mPath=pathStr;	
		cpyThread=new usbCopy();
		
		Log.i("copy","================NEW COPY THREAD====================");
		return cpyThread;
	}

	public usbCopy() {

	}
	
	public static void sendMessage(Bundle b) {
		
		if (MainActivity.mHandler != null) {
			Message message = Message.obtain(MainActivity.mHandler, MainActivity.SHOW_COPY);
			message.setData(b);
			MainActivity.mHandler.sendMessage(message);
		}
	}

	private static boolean  checkFile(String strFile){

		File file = new File(strFile);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private static int getCopyFileNum(String srcPath){
		try{
	        File[] files = new File(srcPath).listFiles();	        
	        if(files!=null){
	        	return files.length;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private static int copyFile(String oldPath, String newPath) { 
		
	   int rt=0;
	   long fileLen=0;
	   long shw=0;
	   long modifyTime=0;
		
       try {   
           long bytesum = 0;   
           int  byteread = 0;   
           File oldfile = new File(oldPath);
           File newFile =null;
           
           if(oldfile.exists()) {
        	   if (MainActivity.mHandler != null) {			
	       		    int idx=oldPath.lastIndexOf("/");
	       		    String fileName=oldPath.substring(idx+1);
	       		    if(fileName.length()>32){
	       		    	fileName=fileName.substring(0,20)+"..."+fileName.substring(fileName.length()-5);
	       		    }	    
	       		    cpySta.copyNum=cpySta.copyNum+1;
	       		    Bundle b = new Bundle();  
					b.putString("copyTitle","copy status: "+cpySta.copyNum+"/"+cpySta.copySum); 
					b.putString("copyName","copy file : "+fileName); 
					b.putInt("copyProg",1); 
	       			sendMessage(b);	
	       	   }
        	   fileLen=oldfile.length();
        	   modifyTime=oldfile.lastModified();
        	   
               InputStream inStream = new FileInputStream(oldPath);
               FileOutputStream fs = new FileOutputStream(newPath);  
               
               if(fileLen!=0){
	               byte[] buffer = new byte[1444];   
	               while ( (byteread = inStream.read(buffer)) != -1) {  
	                   bytesum += byteread;  
	                   shw+=byteread;
	                   if(shw>1024*16 && MainActivity.mHandler != null){
	                	   shw=0;
		                   if(fileLen!=0){
		                	    Bundle b = new Bundle();  
								b.putInt("copyProg",(int)(bytesum*100/fileLen)); 
								b.putString("copyTitle","copy status: "+cpySta.copyNum+"/"+cpySta.copySum); 
								b.putString("copyName",""); 
		                	    sendMessage(b);
		               	   }
	                   }
	                   fs.write(buffer, 0, byteread);   
	               }
               }else{
            	   Log.i("copy","====file "+oldPath+" length is 0====");
               }
               if(inStream!=null){
            	   inStream.close(); 
               }
               if(fs!=null){
            	   fs.close();
            	   newFile=new File(newPath); 
            	   if(newFile!=null && modifyTime!=0){
            		  if(cpySta.copyNum>= cpySta.copySum){
	            			Bundle b = new Bundle();  
	    					b.putInt("copyProg",100); 
	    					b.putString("copyTitle","copy status: "+cpySta.copyNum+"/"+cpySta.copySum); 
	    					b.putString("copyName","task copy end,remove external disk!"); 	 
	                	    Log.i("copy","================TASK COPY END====================");
	                	    sendMessage(b);
            		  }
            	   }
               } 
               rt=1;
           }   
       }catch (Exception e) {   
           e.printStackTrace();   	  
       } 
       return rt;
	}   
		
	private static boolean chkCpyFile(String srcFile,String dstFile){
		
		File f1= new File(srcFile);
		File f2= new File(dstFile);
		long len1=0,len2=0;
		if(f1.exists()){
			len1=f1.length();
		}
		if(f2.exists()){
			len2=f2.length();
		}				
		if(len1==len2 && len1>10*1024*1024){
			return false;
		}
		return true;
	}
	
	private static int copyTaskFile(String srcPath,String dstPath){

		int rt=0;
		
		if(srcPath==null || dstPath==null){
			return 0;
		}
		String srcFile=null;
		String dstFile=null;
		
    	try{
	        File[] files = new File(srcPath).listFiles();	        
	        if(files!=null){

		        for (int i = 0; i < files.length; i++){
		            File f = files[i];
		            
		            if (f.isDirectory()==false && f.getPath().indexOf("/.") == -1) {
		            	
		            	srcFile=srcPath+f.getName();
		            	dstFile=dstPath+f.getName();

		    			if(checkFile(dstFile)!=true){		    				
		    				Log.i("copy","===Now copy file: "+f.getName()+"===");		    						    				
		    				copyFile(srcFile,dstFile);
		    				rt++; 
		    			}else{
		    				if(chkCpyFile(srcFile,dstFile)==true){
			    				Log.i("copy","===Now copy file[replace]: "+f.getName()+"===");		    							    				
			    				copyFile(srcFile,dstFile);
			    				rt++; 
		    				}else{
		    					Log.i("copy","===File: "+f.getName()+" is exist===");	
		    					cpySta.copyNum=cpySta.copyNum+1;

		              		   if(cpySta.copyNum>= cpySta.copySum){//20150710
			            			Bundle b = new Bundle();  
			    					b.putInt("copyProg",100); 
			    					b.putString("copyTitle","copy status: "+cpySta.copyNum+"/"+cpySta.copySum); 
			    					b.putString("copyName","task copy end,remove external disk!");  
			                	    Log.i("copy","================TASK COPY END====================");
			                	    sendMessage(b);
		            		   }else{
			            			Bundle b = new Bundle();  
			    					b.putInt("copyProg",100); 
			    					b.putString("copyTitle","copy status: "+cpySta.copyNum+"/"+cpySta.copySum); 
			    					b.putString("copyName","copy file : "+f.getName());  
			    	       			sendMessage(b);	
		            		   }		    	       			
		    				   rt++;
		    				}
		    			}
		            }else{
		            	if(f.getPath().indexOf("/.") >=0 && f.getName().length()>2){
		            		cpySta.copyNum=cpySta.copyNum+1;
		            	}
		            }
		        }  
		        return rt;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return 0;
	}	
		
	public static void checkOtaFile(String fileName){

		try{
			File f1= new File(fileName);
			if(f1.exists()){
				baseFun.zipFileRead(fileName);//解压缩文件放在/mnt/sdcard/
				String oldVer=baseFun.getOtaDate("/system/build.prop");
				String newVer=baseFun.getOtaDate("/mnt/sdcard/build.prop");
				
				String oldSys=baseFun.getSysType("/system/build.prop");
				String newSys=baseFun.getSysType("/mnt/sdcard/build.prop");
				
				if(!oldSys.contains(newSys) || newSys==null){
					baseFun.appendLogInfo("ota package exception or new version as same as old version!",4);
					return;
				}
				Log.i("ota","\n============ota ver< "+oldVer+":"+newVer+" >==============\n");
				if(oldVer!=null && newVer!=null){
					if(oldVer.contains(newVer)){
						return;
					}else{
						baseFun.appendLogInfo(oldVer +"-->"+newVer,4);
						baseFun.appendLogInfo(null,-1);

						baseFun.runCmd("mkdir -p /cache/recovery");
						baseFun.runCmd("echo \"--update_package="+fileName+"\" > /cache/recovery/command");	
						baseFun.runCmd("reboot recovery");
						return;
					}
				}else{
					if(newVer!=null && !newVer.contains("NG")){
						baseFun.appendLogInfo(oldVer +"-->"+newVer,4);
						baseFun.appendLogInfo(null,-1);
						
						baseFun.runCmd("mkdir -p /cache/recovery");
						baseFun.runCmd("echo \"--update_package="+fileName+"\" > /cache/recovery/command");	
						baseFun.runCmd("reboot recovery");
						return;						
					}else{
						baseFun.appendLogInfo("ota package exception!",4);
					}
				}
			}
		}catch(Exception e) {   
           e.printStackTrace();   	  
        }
	}
	
    private String getPropertyPath() {
    	
        String path = "/";
        if(storagePath!=null){
	        for(int i=0; i<storagePath.size(); i++) {
	            StatFs sf = new StatFs(storagePath.get(i));
	            long availCount=sf.getAvailableBlocks();
	            long blockSize =sf.getBlockSize();
	            double free =availCount * blockSize/ 1024.0 /1024.0;
	            if( free > 500 ) {
	                path = storagePath.get(i);
	                break;
	            }
	        }
        }
        return path;
    }
	
	@SuppressLint("SdCardPath")
	@Override
	public void run() {
		
		int    newOta=0,newApk=0;
		String dstPath=null;

		storagePath=baseFun.getStoragePath();
		
		dstPath=getPropertyPath()+"/ntms/";
		
		if(baseFun.checkFile(mPath+"ntms.txt")==false){
		 	List<String> lstFolder = new ArrayList<String>();
	    	try{
		        File[] files = new File(mPath).listFiles();	        
		        if(files!=null){
			        for(int i = 0; i < files.length; i++){
			            File f = files[i];
			            if (f.isDirectory() && f.getPath().indexOf("/.") == -1) { lstFolder.add(f.getPath()); }
			        }       
					if (!lstFolder.isEmpty()) {			
						for (int i = 0; i < lstFolder.size(); i++) {
							String tmpFolder = lstFolder.get(i);
							if(checkFile(tmpFolder+"/ntms.txt")){
								mPath=tmpFolder+"/"; break;
							}
						}
					}		 
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	Log.i("copy","================New Path: "+mPath+"====================");
		}

		try{
			SystemClock.sleep(1000);
			
			if(cpySta.runing==1 && baseFun.checkFile(mPath+"ntms.txt")){
	
				cpySta.copyNum=cpySta.copySum=0;
				
				baseFun.checkDir(dstPath+"image/");
				baseFun.checkDir(dstPath+"text/");
				baseFun.checkDir(dstPath+"audio/");
				baseFun.checkDir(dstPath+"video/");
				baseFun.checkDir(dstPath+"task/");
				baseFun.checkDir(dstPath+"update/");
				
				cpySta.copySum+=getCopyFileNum(mPath+"image/"); 
				cpySta.copySum+=getCopyFileNum(mPath+"text/");
				cpySta.copySum+=getCopyFileNum(mPath+"audio/");
				cpySta.copySum+=getCopyFileNum(mPath+"video/");
				cpySta.copySum+=getCopyFileNum(mPath+"task/");
				cpySta.copySum+=getCopyFileNum(mPath+"update/");
				
				if(checkFile(mPath+"cfg.xml")){
					baseFun.appendLogInfo("usb copy new cfg.xml!",4);
					copyFile(mPath+"cfg.xml",cfg.cfgXml);
					cfg.loadConfig(cfg.cfgXml);	
					cpySta.copySum++;
				}
				if(checkFile(mPath+"update/ntms.apk")){
					newApk=1;
					baseFun.appendLogInfo("usb copy new ntms.apk!",4);
					copyFile(mPath+"update/ntms.apk",dstPath+"update/ntms.apk"); 	
					cpySta.copySum++;
				}
				if(checkFile(mPath+"update/ota.zip")){
					if(checkFile(dstPath+"update/ota.zip")==false){
						newOta=1;
						copyFile(mPath+"update/ota.zip",dstPath+"update/ota.zip"); 	
						cpySta.copySum++;
					}else{
						if(baseFun.getFileLen(mPath+"update/ota.zip")!=baseFun.getFileLen(dstPath+"update/ota.zip")){
							newOta=1;							
							copyFile(mPath+"update/ota.zip",dstPath+"update/ota.zip"); 
							cpySta.copySum++;
						}
					}
				}

				if(cpySta.copySum>0){					
					baseFun.appendLogInfo("usb copy new task!",4);					
					baseFun.checkDir(dstPath);

					copyTaskFile(mPath+"image/",dstPath+"image/");				
					copyTaskFile(mPath+"text/",dstPath+"text/");
					copyTaskFile(mPath+"audio/",dstPath+"audio/");					
					copyTaskFile(mPath+"video/",dstPath+"video/");
					copyTaskFile(mPath+"task/",dstPath+"task/");
					if(checkFile(mPath+"task/plc.xml")){
			        	if(sch.taskLst==null){
			        		sch.taskLst=new ArrayList<String>();
			        	}
			        	sch.taskLst.clear();
			        	sch.taskLst.add("plc.xml");
			        	sch.saveTaskXml(sch.tskFile,sch.taskLst);
					}
					if(newOta==1){
						baseFun.appendLogInfo("usb copy task end,check ota package!",4);
						checkOtaFile(dstPath+"update/ota.zip");
					}else{
						baseFun.appendLogInfo("usb copy task end!",4);
					}
					if(newApk==1){
						baseFun.appendLogInfo("found new apk,install now!",4);
						baseFun.appendLogInfo(null,-1);
						baseFun.runCmd("/system/bin/sh /data/data/com.ntms/files/upt");
					}
				}	
			}
			baseFun.sync();
			cpySta.runing=0; 
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}