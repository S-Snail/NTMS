package com.play;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.list.funs;
import com.list.list.ProgramList;
import com.ntms.MainActivity;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


@SuppressLint({ "ViewConstructor", "HandlerLeak", "SdCardPath" })
public class img extends SurfaceView implements SurfaceHolder.Callback,Runnable{  
	
	private static final String  IMG_TAG="img";	
	private static final int     LOADIMG=0;

	private int    dur  = 10 *1000;
	private int    sDur = 10 *1000;
	private int    run  = 0;
	private int    exit = 0;
	private int    imgIdx=0;
	private int    isBg  =0;
	private int    vol   =0;
	private int    lstIdx=0;
	
	private SurfaceHolder mSurfaceHolder=null;
	private Handler  mHandler=null;
	private Paint    paint=new Paint();
	private Matrix   matrix=new Matrix();
	private Options  opt=new Options();
	private Rect     rt = null;
	private Bitmap   bitmapA=null;
	private int      w=0,h=0;

	private long     logDur=0;
	private String   logName=null;

	ArrayList<ProgramList> pList=null;
	
	public img(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    public img(Context context,int w,int h,int left,int top,String listStr,int isBg,String id,int vol) {  
        super(context);
		this.exit=0; 
		this.run=1;
		this.isBg=isBg;	
		this.w=w;
		this.h=h;
		this.vol=vol;

		if(this.w*this.h==1920*1080 || this.w*this.h==1280*720 || this.w*this.h==1366*768){
			baseFun.fullTag=1;
		}
		if(listStr!=null)
			pList=funs.parseList(listStr);

		rt=new Rect(0, 0, w, h);
		if (mSurfaceHolder == null){
			mSurfaceHolder = this.getHolder();
			mSurfaceHolder.addCallback(this);
		}
		if(isBg==0){
			this.setZOrderMediaOverlay(true);
		}
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LOADIMG:
					bitmapA=getNextImage(1);
					break;
				default:
					break;
				}
			}
		};
    }  
    
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {  

    	this.exit=0; this.run=1;
    } 
    
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {  
    	
    	this.exit=0; 
    	new Thread(this).start();
    } 
    
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) { 
    	
    	run=0; this.exit=1;  	
    }  
 
	@Override
	public void run() {
		
		int errCnt=0,mode=0;
		int loadTag=0,waitCnt=0,showRt=0;//20141204
		Bitmap bitmap=null;
			
		bitmap=getNextImage(0);

		while (this.exit != 1) {

			long useTime = baseFun.getTickCount(),btime=useTime,wtime=0;
			
			dur = sDur; waitCnt=loadTag=0;
			try{
				if(bitmap != null) {
					if(this.exit!=1){
						//---------------------------------------------------------------------------
						int iCnt=0,iExcep=0;
				        if(bitmap!=null && mSurfaceHolder!=null){
					        Canvas mCanvas = null; 
					       	run=1;  
					        while(run != 0 && this.exit != 1){					        	
					        	if(paint==null)  paint=new Paint();        	
					    	    if(matrix==null) matrix=new Matrix();  
					    	    if(bitmap != null && mSurfaceHolder!=null) {
					    	    	 synchronized (mSurfaceHolder) {  	    
							            try {            	
							                mCanvas=mSurfaceHolder.lockCanvas();                
							                if(mCanvas!=null){	                	 
						                		 if(mode==1){	 
						                			 run=imageTransRotate(mCanvas,bitmap,rt,iCnt);
						                		 }else if(mode==2){
						                			 run=imageTransScaleIn(mCanvas,bitmap,rt,iCnt);  
						                		 }else if(mode==3){	 
						                			 run=imageTransSkew(mCanvas,bitmap,rt,iCnt);  
						                		 }else{
						                			 run=imageNoTrans(mCanvas,bitmap,rt,iCnt);
						                		 }
						                		 iCnt++;
							                }
							                if(mCanvas!=null){
							                	mSurfaceHolder.unlockCanvasAndPost(mCanvas);
							                }
							            }catch(Exception e){
							            	run=0; 
							            	iExcep=1;
							            	if(mCanvas!=null){
							            		mSurfaceHolder.unlockCanvasAndPost(mCanvas);
							            	}
							                Log.e(IMG_TAG,"showImage "+e.getMessage());	
							            }
						        	} 
					    	    }else{
					    	    	run=0;
					    	    }
					        }
				       		if((mode++)>3){
				       			mode=0;
				       		}
					        if(bitmap!=null){
					        	if(!bitmap.isRecycled()){  
					        		bitmap.recycle(); bitmap=null; 
					        	}
					        	bitmap=null;
					        }
					        if(iExcep==1){
					        	showRt=0;
					        }
					        showRt=1;
				        }
				    }         
					//---------------------------------------------------------------------------
					if(showRt==0){
						if((errCnt++)>6){
							Log.i(IMG_TAG,"==lockCanvas error["+errCnt+"]==");
						}
					}else{
						errCnt=0;
					}
				}
			
				if(loadTag==0){
					sendMessage(LOADIMG);

					while(bitmap!=null)	{
						if(this.exit==1) break; if((waitCnt++)>10) break; SystemClock.sleep(200);				
					}
					SystemClock.sleep(200);
					wtime=baseFun.getTickCount()-btime;		
					if(wtime<3500){
						imgSleep(3500-(int)wtime); 
					}
					SystemClock.sleep(100);
					useTime = baseFun.getTickCount() - useTime;				
					if(dur > useTime) {
						dur = dur - (int)useTime;
					}else{
						dur=0;
					}		
					if(showRt==0 || dur<=0){
						dur = 100;	
					}
					if(this.exit==1){
						break; 
					}
					imgSleep(dur); 

			    	if(logName!=null && logDur>1){
			    		baseFun.appendPlayDur(logName,(int)(baseFun.getTickCount()-logDur));
			    		logName=null;
			    		logDur=0;
			    	}
				}
				if(bitmapA!=null){
					bitmap=bitmapA; bitmapA=null;
				}else{
					imgIdx--;
					bitmap= getNextImage(1);
					Log.i(IMG_TAG,"Get Image bitmap fail....");
				}
				if(this.exit==1){
					break;
				}
			}catch(Exception e) {
			}
		}
	}

	private void imgSleep(int dur){
		try{
			if(isBg==1){			
				while(this.exit!=1){
					SystemClock.sleep(200);
					if(bitmapA==null){
						SystemClock.sleep(400); break;
					}
				}
			}else{
				long startT=baseFun.getTickCount();
				while(this.exit!=1){
					long nowT=baseFun.getTickCount();
					if(Math.abs(nowT-startT)>=dur || this.exit==1){
						return;
					}else{
						if(Math.abs(nowT-startT)>=dur-200){
							SystemClock.sleep(dur-(nowT-startT)); return;
						}else{
							SystemClock.sleep(200);
						}
					}
				}
			}
		}catch(Exception e){
		}
	}
	
    public void stopPlay(){
    	this.exit=1;
    }
      
    private int chkCurLst(){
    	
    	for(int i=0;i<pList.size();i++){
    		long cur=funs.getCurrentTime(1);
    		long ed=funs.parseTime(pList.get(i).EndDate,1);
    		long es=funs.parseTime(pList.get(i).StartDate,0);

    		if((cur<ed && cur>=es) || (ed==0 && es==0)){
    			long t=funs.getCurrentTime(0);
    			if(t<funs.parseTime(pList.get(i).EndTime,0) && t>=funs.parseTime(pList.get(i).StartTime,0)){
    				return i;
    			}
    		}
    	}
    	return 0;
    }
    
    private Bitmap getNextImage(int mode) {

    	if(pList==null || this.exit==1){
    		return null;
    	}else{    		
    		int idx=chkCurLst();
    		if(idx!=lstIdx){
    			lstIdx=idx;  imgIdx=0;
    		}
    		if(imgIdx>=pList.get(lstIdx).lst.size() || imgIdx<0) imgIdx=0;
    	}
		Bitmap bitmap=null;
		try{
			String filePath=baseFun.getMediaPath(pList.get(lstIdx).lst.get(imgIdx).Name,mediaType.Image);
			String audioName=pList.get(lstIdx).lst.get(imgIdx).AudioName;
			if(audioName!=null){
				audioName=baseFun.getMediaPath(audioName,mediaType.Audio);
				if(baseFun.checkFile(audioName)){
					Bundle b = new Bundle();  
					b.putString("path",audioName); 
					b.putInt("volume",vol); 
					Message message = Message.obtain(MainActivity.mHandler, MainActivity.NEW_VIEW);
			        message.setData(b);
					MainActivity.mHandler.sendMessage(message);
				}
			}
			
			sDur=pList.get(lstIdx).lst.get(imgIdx).PlayTime*1000;
			
			if(sDur<4000) sDur=4000;

			if(filePath==null || baseFun.checkFile(filePath)==false){
				String logStr=baseFun.getTimeStr(1)+pList.get(lstIdx).lst.get(imgIdx).Name+" not exsit";
				baseFun.appendLogInfo(logStr,1);
				baseFun.curImg=pList.get(lstIdx).lst.get(imgIdx).Name;
			}else{
				String logStr=baseFun.getTimeStr(1)+pList.get(lstIdx).lst.get(imgIdx).Name;
				baseFun.appendLogInfo(logStr,1);
				baseFun.curImg=pList.get(lstIdx).lst.get(imgIdx).Name;
			}
			logName=pList.get(lstIdx).lst.get(imgIdx).Name;
			logDur=baseFun.getTickCount();
					
			Log.i(IMG_TAG,"==========Image ["+sDur+"] "+pList.get(lstIdx).lst.get(imgIdx).Name+"=============");			
			imgIdx++;
			bitmap=getBitmap(filePath); 

	    	if(bitmap!=null && rt!=null){  
	        	bitmap = getScaleBitmap(bitmap,rt.width(),rt.height());
	    	}  
		}catch(Exception e) {
			Log.e(IMG_TAG,"getNextImage "+e.getMessage());
		}
		return bitmap;
	}
	
	public void sendMessage(int id) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	private Bitmap getBitmap(String path) {
		
		Bitmap         bmp = null;
		FileInputStream fs = null;
		
	    if(opt==null) opt=new Options();
	    
		opt.inPurgeable      = true;
		opt.inInputShareable = true;
		opt.inPreferredConfig= Bitmap.Config.RGB_565;
		opt.inSampleSize     = 1;

		if(path==null || baseFun.checkFile(path)==false){	
			return null;
		}
		try {
			fs = new FileInputStream(path);
			if(fs != null) {
				try {
					bmp = BitmapFactory.decodeStream(fs,null,opt);
				} catch (OutOfMemoryError e) {
					Log.e(IMG_TAG,"getBitmap "+e.getMessage());
				}finally {
					try {
						fs.close();
					}catch (IOException e) {
						Log.e(IMG_TAG,"getBitmap "+e.getMessage());	
					}
				}
			}	
		}catch(FileNotFoundException e) {
			Log.e(IMG_TAG,"getBitmap "+e.getMessage());	
		}
		return bmp;
	}

	private Bitmap getScaleBitmap(Bitmap bitmap ,int w,int h){
    	if(this.exit!=1){
	        int width = bitmap.getWidth();
	        int hight = bitmap.getHeight();
	        
	        if(w==width && h==hight){
	        	return bitmap;
	        }
	        float  wScale = ((float)w/width); 
	        float  hScale = ((float)h/hight); 
	        
	        matrix.reset();
	        matrix.postScale(wScale, hScale); 
	        
	        return Bitmap.createBitmap(bitmap,0,0,width,hight,matrix,true);
    	}else{
    		return null;
    	}
    }
    
    private int imageTransScaleIn(Canvas mCanvas,Bitmap bitmap,Rect rt,int arg){
    	  
    	if(arg<12){
    		 paint.reset();
    		 mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
	    	 matrix.reset();
	         matrix.postScale((float)(36-arg)/24,(float)(36-arg)/24,rt.width()/2,rt.height()/2);
	         mCanvas.drawBitmap(bitmap, matrix, paint);   

	         return 33;
    	}else{
    		paint.reset(); 
    		mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		    mCanvas.drawBitmap(bitmap, 0,0, paint);   
	        
    		return 0;
    	}
   }  
    
    private int imageTransSkew(Canvas mCanvas,Bitmap bitmap,Rect rt,int arg){
    	  
    	if(arg<20){
    		//	mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    		matrix.reset();    
    		matrix.postSkew((10-arg)*0.1f,(10-arg)*0.1f,rt.width()/2,rt.height()/2);
    		paint.reset();
	        mCanvas.drawBitmap(bitmap, matrix, paint);   
	        return 33;
    	}else{
    		paint.reset(); 
    		mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		    mCanvas.drawBitmap(bitmap, 0,0, paint);    	        
 	        return 0;
    	}
    }
    
    private int imageTransRotate(Canvas mCanvas,Bitmap bitmap,Rect rt,int arg){
    	  
    	if(arg<6){
    		//	mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    		matrix.reset();       
	        matrix.postRotate(300+10*arg,rt.width()/2,rt.height()/2);
	    	paint.reset(); 
	        mCanvas.drawBitmap(bitmap, matrix, paint);   
	        return 33;
    	}else{
    		paint.reset();
    		mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		    mCanvas.drawBitmap(bitmap, 0,0, paint);
 	        return 0;
    	}
    }
    
    public int imageTransFadein(Canvas mCanvas,Bitmap bitmap,Rect rt,int arg){
    	  
    	if(arg<12){
    		 mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    		 paint.reset();  
	         paint.setAlpha((arg+4)*16);
	         mCanvas.drawBitmap(bitmap, 0,0, paint);   
	        
	         return 90;
    	}else{
    		 paint.reset();
	         paint.setAlpha(255);
	         mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		     mCanvas.drawBitmap(bitmap, 0,0, paint);   
	
     		 return 0;
    	}
    }

    private int imageNoTrans(Canvas mCanvas,Bitmap bitmap,Rect rt,int arg){
    	
    	mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    	paint.reset();

	    mCanvas.drawBitmap(bitmap, 0,0, paint);  
	
	    return 0;
    } 
}  
