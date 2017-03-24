package com.play;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ntms.baseFun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class clock extends SurfaceView implements SurfaceHolder.Callback,Runnable{  

	@SuppressWarnings("unused")
	private Handler mHandler=null;
	private SurfaceHolder mSurfaceHolder;
    private Matrix matrix = new Matrix();
	private Paint   paint = null;
    private String subMode="hh:mm:ss";
    private String color="#FEFEFE";
    private String bgcolor="#000000";
    
	private int iShow = 0;
	private int iRun = 0;
	private int iWidth = 0;
	private int iHeight = 0;
	private int type=0;
	
	public clock(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    @SuppressLint("HandlerLeak")
	public clock(Context context,int w,int h,String subMode,String color,String bgColor) {  
        super(context);  
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		
        this.iShow =1;   
        this.iWidth = w;
        this.iHeight = h;

        if(subMode!=null && subMode.length()>4){
        	this.subMode=subMode;
        }
		if(color!=null && color.length()==7){
			this.color=color;
		}
		try{
			//if(this.iHeight<this.iWidth*2/3){
				type=2;
				if(bgColor!=null && bgColor.compareTo("#000000")!=0) {    
					this.bgcolor=bgColor;
				}
				if(baseFun.fullTag!=1){
					setBackgroundColor(Color.parseColor(this.bgcolor));
				}
				Log.i("clock","============Show Time String["+this.bgcolor+"]["+this.color+"]=================");
			//}else{
			//	if(this.iHeight>this.iWidth) this.iHeight=this.iWidth; else this.iWidth=this.iHeight;
			//	w=this.iWidth; h=this.iHeight;
			//}
		}catch (Exception e){
	   		
	   	}	
		setZOrderOnTop(true);
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		
		if(paint==null){
			paint = new Paint();
		}
		try{
			if(type==2){
			   	if(subMode.length()==5) {
			   		paint.setTextSize(2*(this.iWidth-8)/5);
		    	}else if(subMode.length()==8){
		    		paint.setTextSize(2*(this.iWidth-8)/8);
				}else if(subMode.length()==10){
					paint.setTextSize(2*(this.iWidth-8)/10);
				}	
				paint.setAntiAlias(true);
				paint.setTypeface(Typeface.SANS_SERIF);	
				paint.setColor(Color.parseColor(this.color));
				paint.setAlpha(255);
			}else{
				this.color="#010101";
				paint.setTextSize(this.iWidth/16);			   	
				paint.setAntiAlias(true);
				paint.setTypeface(Typeface.SANS_SERIF);	
				paint.setColor(Color.parseColor(color));
				paint.setAlpha(255);
			}
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

	public Bitmap getXBitmap(Resources res, int id,int w,int h,int mode) {
		
		Bitmap      bmp = null;
		try {
			bmp = BitmapFactory.decodeResource(res, id);
		} catch (OutOfMemoryError e) {
			Log.e("ImageSurfaceView", "getBitmap " + e.getMessage());
		} finally {

		}
		if(bmp!=null){
			return getXScaleBitmap(bmp,w,h,mode);
		}
		return bmp;
	}

    private Bitmap getXScaleBitmap(Bitmap bitmap ,int w,int h,int mode){
    	
		int width = bitmap.getWidth();
		int hight = bitmap.getHeight();

		if (w == width && h == hight) {
			return bitmap;
		}

		if (mode == 0 || mode==1) {
			float wScale = ((float) w / width);
			float hScale = ((float) h / hight);
			matrix.reset();
			matrix.postScale(wScale, hScale);

			return Bitmap.createBitmap(bitmap, 0, 0, width, hight, matrix, true);
		} else {
			w=w*3/5; h=h*3/5;
			float hScale = ((float) h / hight);
			matrix.reset();
			matrix.postScale(hScale, hScale);

			return Bitmap.createBitmap(bitmap, 0, 0, width, hight, matrix, true);
		}
    }

    @SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private String getTimeString(int mode){
    	     
		long onTime=System.currentTimeMillis();
		Date dNow  = new Date(onTime);
		if(mode==0){
	    	if(subMode.length()==5) {
				SimpleDateFormat f = new SimpleDateFormat("HH:mm");
				return f.format(dNow);
	    	}else if(subMode.length()==8){
				SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
				return f.format(dNow);
			}else if(subMode.length()==10){
				SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
				return f.format(dNow);
			}
		}else if(mode==2){
			
			SimpleDateFormat f = new SimpleDateFormat("HH  mm");
			return f.format(dNow);
			
		}else if(mode==1){
			int m=dNow.getMinutes();
			int h=dNow.getHours();
			if(m==0){
				m=59; if(h==0) h=23; else h=h-1;
			}else{
				m=m-1;
			}
			return (h<10?"0"+h:h)+"  "+(m<10?"0"+m:m);
		}
    	return "08:00";
    }

    @SuppressWarnings("unused")
	@SuppressLint("SimpleDateFormat")
	private String getTimeStringX(Calendar cal,int mode){
    	
    	if(mode==3 && cal!=null){     
			int wk=cal.get(Calendar.DAY_OF_WEEK);
			
			wk = wk - 1;
			if(wk == 0){
				  wk = 7;
			}
			if(wk==1){
				return "星期一";
			}else if(wk==2){
				return "星期二";
			}else if(wk==3){
				return "星期三";				
			}else if(wk==4){
				return "星期四";				
			}else if(wk==5){
				return "星期五";
			}else if(wk==6){
				return "星期六";	
			}else if(wk==7){
				return "星期日";				
			}else{
				return "星期X";
			}
    	}else{
    		long onTime=System.currentTimeMillis();
    		Date dNow  = new Date(onTime);
    		
	    	if(mode==0) {
				SimpleDateFormat f = new SimpleDateFormat("HH:mm");
				return f.format(dNow);
	    	}else if(mode==1){
				SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
				return f.format(dNow);
			}else if(mode==2){
				SimpleDateFormat f = new SimpleDateFormat("MM月dd日");
				return f.format(dNow);
			}
    	}
    	return "08:00";
    }
    
    private void showTString(){
    
        Canvas mCanvas = null; 
  	    if(mSurfaceHolder!=null) {	    
            try {            	
                mCanvas=mSurfaceHolder.lockCanvas();                
                if(mCanvas!=null){	  
                	mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);	

	     			 if(subMode.length()==5) {
	    			   		paint.setTextSize(2*(this.iWidth-8)/5);
	     			 }else if(subMode.length()==8){
	    		    		paint.setTextSize(2*(this.iWidth-8)/8);
	    			 }else if(subMode.length()==10){
	    					paint.setTextSize(2*(this.iWidth-8)/10);
	    			 }
	     			 paint.setAntiAlias(true);
					 paint.setColor(Color.parseColor(this.color));
          			 mCanvas.drawText(getTimeString(0),4,this.iHeight*4/5,paint); 
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
	 				showTString();	 				
	 			}else{
	 				SystemClock.sleep(500);
	 			}
			}
		}

 	}  
}  







