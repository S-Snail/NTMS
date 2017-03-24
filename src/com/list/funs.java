package com.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import net.sf.json.JSONObject;

import com.list.list.Item;
import com.list.list.ProgramList;

public class funs {

	public static int copyFile(String oldPath, String newPath) { 
		
		int rt = 0;
		long fileLen = 0;
		
		if(oldPath==null || newPath==null){
			return 0;
		}
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);

			if (oldfile.exists()) {

				fileLen = oldfile.length();

				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);

				if (fileLen != 0) {
					byte[] buffer = new byte[1444];
					while ((byteread = inStream.read(buffer)) != -1) {
						fs.write(buffer, 0, byteread);//将参数buffer的从偏移量0开始的byteread个字节写到输出流
					}
				}
				if (inStream != null) {
					inStream.close();
				}
				if (fs != null) {
					fs.close();
				}
				rt = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rt;
	}  

	public static String longToDate(long timeValue){
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getDefault());
		c.setTimeInMillis(timeValue*1000);//该方法只有一个参数，即距离1970年1月1日0时的毫秒数，调用这个方法，则会根据你传入的毫秒数对日历对象中的变量进行相应设置
		 
		int year = c.get(Calendar.YEAR);
		int month =c.get(Calendar.MONTH)+1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		int hour= c.get(Calendar.HOUR_OF_DAY);
		int min= c.get(Calendar.MINUTE);
		int sec= c.get(Calendar.SECOND);

		String str=year + "-" + (month<10?"0"+month:month) + "-" + (day<10?"0"+day:day) +" "+ (hour<10?"0"+hour:hour) 
				+":"+ (min<10?"0"+min:min) 
				+":"+ (sec<10?"0"+sec:sec) 
				;
		return str;
	}

	public static long dateToLong(String dateValue){
		
		return parseTime(dateValue,0);
	}
	
	private static long getMtimeInMillis(int mode){
	
		Calendar c = Calendar.getInstance();
		
		int year=c.get(Calendar.YEAR);
		int mon=c.get(Calendar.MONTH);
		int day=c.get(Calendar.DAY_OF_MONTH);
		
		if(mode==0) c.clear();
		
		c.setTimeZone(TimeZone.getDefault());
		c.set(Calendar.YEAR,year);
		c.set(Calendar.MONTH,mon);
		c.set(Calendar.DAY_OF_MONTH,day);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.SECOND,0);
		
		return c.getTimeInMillis();		
	}
	
	public static long getCurrentMtime(int mode){
		
		try{			
			return (System.currentTimeMillis() - getMtimeInMillis(mode));

		}catch(Exception e){
			
		}
		return 0;
	}
	
	public static long currentTime(){
		
		Calendar c = Calendar.getInstance(); 
		c.setTimeZone(TimeZone.getDefault());

		int hh =c.get(Calendar.HOUR_OF_DAY);
		int min=c.get(Calendar.MINUTE);
		int sec=c.get(Calendar.SECOND);

		return  hh*3600+min*60+sec;
	}
	
	public static long getCurrentTime(int mode){
		try{
			if(mode==0){//wallclock
				return  currentTime();
			}else{//clock
				return System.currentTimeMillis()/1000;
			}
		}catch(Exception e){
			
		}
		return 0;
	}

	public static boolean checkFile(String strFile){

		File file = new File(strFile);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static long getTimeInMillis(int year,int month,int day,int hour,int min,int sec){	

		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getDefault());
		
		if(year!=0){
			c.set(Calendar.YEAR,year);
		}
		if(month>0){
			month--;
		}
		c.set(Calendar.MONTH,month);
		c.set(Calendar.DAY_OF_MONTH,day);
		c.set(Calendar.MINUTE,min);
		c.set(Calendar.HOUR_OF_DAY,hour);
		c.set(Calendar.SECOND,sec);//
		
		return (c.getTimeInMillis()/1000);		
	}
	
	public static long parseTime(String strTime,int mode){//2015-04-08 12:30:30
		
		try{
			if(strTime!=null && strTime.length()>=5){
	
				if((strTime.length()==8 || strTime.length()==5) && strTime.contains(":")){
					String[] str = strTime.split("\\:");//以：作为分隔符
					try{
						if(str.length==2){
							return Integer.parseInt(str[0])*3600+Integer.parseInt(str[1])*60;
						}else if(str.length==3){
							return Integer.parseInt(str[0])*3600+Integer.parseInt(str[1])*60+Integer.parseInt(str[2]);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					String[] str = strTime.split(" ");

					if(str!=null && str.length>0){
						int year=0,month=0,day=0,hour=0,min=0,sec=0; 
						
						String[] d=str[0].split("-");
						if(mode==1){
							hour=23;min=59;sec=59;
						}
						if(d!=null && d.length==2){
							month=Integer.parseInt(d[0]);
							day=Integer.parseInt(d[1]);
						}else if(d!=null && d.length==3){
							year=Integer.parseInt(d[0]);
							month=Integer.parseInt(d[1]);
							day=Integer.parseInt(d[2]);
						}
						if(str.length>1){
							String[] t=str[1].split("\\:");
							if(t.length==2){
								hour=Integer.parseInt(t[0]);
								min=Integer.parseInt(t[1]);
							}
						}
						return getTimeInMillis(year,month,day,hour,min,sec);
					}
				}
			}	
		}catch(Exception e){
		}
		return 0;
	}

	public static  ArrayList<ProgramList> parseList(String lstStr){
		
		String[]  itmStr=lstStr.split("\\^") ;
		try{
			if(itmStr!=null && itmStr.length>0){
				ArrayList<ProgramList> list=new ArrayList<ProgramList>();
				
				for(int i=0;i<itmStr.length;i++){
					String[]  lst=itmStr[i].split("\\|");
	
					if(lst!=null && lst.length>1){
				    	JSONObject jsnObj=JSONObject.fromObject("{"+lst[0]+"}");  
						if(jsnObj==null){
							continue;
						}
						ProgramList pLst=new ProgramList();
						try{
							//pLst.EndDate=jsnObj.getString("EndDate");
							//pLst.EndTime=jsnObj.getString("EndTime");
							pLst.StartDate=jsnObj.getString("StartDate");
							pLst.StartTime=jsnObj.getString("StartTime");
						}catch (Exception x){
						}
						for(int k=1;k<lst.length;k++){
							try{
								JSONObject jObj=JSONObject.fromObject("{"+lst[k]+"}");  
								if(jObj==null){
									continue;
								}
								Item pItm=new Item();
								pItm.AudioName=jObj.getString("AudioName");
								pItm.AudioPlayMode=jObj.getString("AudioPlayMode");
								pItm.Name=jObj.getString("Name");
								pItm.Rssaddr=jObj.getString("Rssaddr");
								pItm.Rssitem=jObj.getString("Rssitem");
								pItm.Cycle=jObj.getInt("Cycle");
								pItm.Effect=jObj.getInt("Effect");
								pItm.EffectSpeed=jObj.getInt("EffectSpeed");
								pItm.PlayTime=jObj.getInt("PlayTime");
								pItm.Transparent=jObj.getInt("Transparent");
								pItm.MoveSpeed=jObj.getInt("MoveSpeed");
								pItm.MoveStyle=jObj.getInt("MoveStyle");
		
								pLst.lst.add(pItm);
							}catch (Exception e){	
							}
						}
						list.add(pLst);
					}
				}
				return list;
			}
		}catch (Exception e){	
		}		
		return null;
	}

}