package com.ntms;

import java.lang.Thread.UncaughtExceptionHandler;
import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("SdCardPath")
public class Gexception implements UncaughtExceptionHandler {

	private static Gexception INSTANCE = new Gexception();
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	public Context ctx=null;
	
	private Gexception() {
	}

	public static Gexception getInstance() {
		return INSTANCE;
	}

	public void init(Context ctx) {
		this.ctx=ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
 
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
			
		System.out.println("=========================uncaughtException catched===========================");
		if (!handleException(ex) && mDefaultHandler != null) {
			
			mDefaultHandler.uncaughtException(thread, ex);
			
		} else {			
			try {
				Thread.sleep(300 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			

		}
		System.out.println("=========================uncaughtException ignore==========================");
	}

	private boolean handleException(Throwable ex) {

		if (ex == null) {
			return true;
		}

		return true;
	}
}
