package com.play;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.net.comm;
import com.ntms.baseFun;


public class showstr extends SurfaceView implements SurfaceHolder.Callback,Runnable{  

	@SuppressWarnings("unused")
	private Handler mHandler=null;
	private SurfaceHolder mSurfaceHolder;
	private Paint   paint = null;
    private String color="#FEFEFE";
    private String bgcolor="#000000";
    
	private int iShow = 0;
	private int iRun = 0;
	@SuppressWarnings("unused")
	private int iWidth = 0;
	private int iHeight = 0;
	private int type=0;
	
	public showstr(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    @SuppressLint("HandlerLeak")
	public showstr(Context context,int w,int h,int type,String color,String bgColor) {  
        super(context);
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
	
        this.iShow =1;
        this.iWidth = w;
        this.iHeight = h;

        if(type>=0 && type<=4){
        	this.type=type;
        }
		if(color!=null && color.length()==7){
			this.color=color;
		}
		if(bgColor!=null && bgColor.compareTo("#000000")!=0) {    
			this.bgcolor=bgColor;
		}
		if(baseFun.fullTag!=1){
			try{
				setBackgroundColor(Color.parseColor(this.bgcolor));
			}catch (Exception e){
		   	}
		}
		setZOrderOnTop(true);
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		if(paint==null){
			paint = new Paint();
		}
		try{	
			paint.setTextSize(this.iHeight*4/5);			   	
			paint.setAntiAlias(true);
			paint.setTypeface(Typeface.SANS_SERIF);	
			paint.setColor(Color.parseColor(this.color));
			paint.setAlpha(255);
	   	}catch (Exception e){
	   	}
    }  
    
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {  
    	this.iShow=1; this.iRun=1;
    }  
    
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {  
    	this.iRun=1;
    	new Thread(this).start();
    } 
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {  
    	this.iRun=0;
    }  
    
    public void stopPlay(){
    	this.iRun=0;
    }   
  
    private void showStaticString(String str){
    
        Canvas mCanvas = null; 
  	    if(mSurfaceHolder!=null && str!=null) {	    
            try {            	
                mCanvas=mSurfaceHolder.lockCanvas();                
                if(mCanvas!=null){	  
                	 mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);	
	     			 paint.setAntiAlias(true);//Ïû³ý¾â³Ý
					 paint.setColor(Color.parseColor(this.color));
          			 mCanvas.drawText(str,4,this.iHeight*4/5,paint); 
                }
                if(mCanvas!=null){
                	mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }catch(Exception e){
            	if(mCanvas!=null){
            		mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            	}
            }
  	    }
    }
        
	@Override  
 	public void run() {

		while (this.iRun == 1) {
			
			synchronized (mSurfaceHolder) {
	 			if (this.iShow == 1){
	 				if(type==0){
	 					showStaticString(comm.weatherStr);	
	 					comm.weatherStr=null;
	 					type=1;
	 				}else{
	 					showStaticString(comm.rssStr);
	 					comm.rssStr=null;
	 					type=0;
	 				}
	 			}else{
	 				SystemClock.sleep(50*1000);
	 			}
			}
		}
 	}  
}