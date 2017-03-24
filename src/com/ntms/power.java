package com.ntms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.cfg.cfg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

@SuppressLint("Wakelock")
public class power extends Thread {

	private final static String TAG = "power";

	private WakeLock wlock = null;
	private Context mContext = null;
	private static power pwrThread = null;
	private static int bgSta = 0;
	private Handler mHandler = null;
	public static int pwrThdAct = -1;

	public static power newInstance(Context context, Handler mainHandler) {
		pwrThread = new power(context, mainHandler);
		return pwrThread;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("HandlerLeak")
	public power(final Context context, Handler mainHandler) {
		mContext = context;

		Log.i(TAG, "=================POWER THREAD START===================");

		baseFun.appendLogInfo("power thread start", 4);
		baseFun.appendLogInfo(null, -1);

		PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		wlock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "SY");
		wlock.setReferenceCounted(false);
		wlock.acquire();

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case -1:
					break;
				}
			}
		};
	}

	public void sendMessage(int id) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	private boolean checkPower(String[] datePw, String[] weekPw, String[] holiPw) {

		if (datePw == null && weekPw == null && holiPw == null) {
			return true;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getDefault());

		int min = c.get(Calendar.MINUTE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 0)
			week = 7;
		week--;

		int i = 0;

		try {
			if (holiPw != null && holiPw.length > 0) {

				for (i = 0; i < holiPw.length; i++) {
					if (holiPw[i] != null && holiPw[i].length() > 12 && !holiPw[i].contains("00-00 00:00-00:00")) {

						String[] str = holiPw[i].split(" ");
						if (str.length < 2)
							continue;
						String[] strX = str[1].split("-");

						if (strX.length >= 2 && !str[1].contains("00:00-00:00")) {

							if (chkDate(c, str[0]) && !strX[0].equals(strX[1])) {
								Log.i(TAG, "===HOLIDAY======" + str[0] + " " + strX[0] + "-" + strX[1] + "===========");
								if (chkPowerTime(hour * 60 + min, strX[0], strX[1]) == 1) {
									return true;
								}
								return false;
							}
						}
					}
				}
			}

			if ((week >= 0 && week <= 6) && weekPw != null && weekPw.length > 0) {
				for (i = 0; i < weekPw.length; i++) {
					if (weekPw[i] != null && weekPw[i].length() > 4 && i == week
							&& !weekPw[i].contains("00:00-00:00")) {
						String[] str = weekPw[i].split("-");
						if (str.length == 2 && !str[0].equals(str[1])) {
							Log.i(TAG, week + "====WEEK=====" + str[0] + "-" + str[1] + "===========");
							if (chkPowerTime(hour * 60 + min, str[0], str[1]) == 1) {
								return true;
							} else {
								return false;
							}
						}
					}
				}
			}

			if (datePw != null && datePw.length > 0) {
				int valid = 0;
				for (i = 0; i < datePw.length; i++) {
					if (datePw[i] != null && datePw[i].length() > 4) {
						String[] str = datePw[i].split("-");
						if (str.length == 2 && !str[0].equals(str[1])) {
							// Log.i(TAG,"====DAY====="+str[0]+"-"+str[1]+"===========");
							if (chkPowerTime(hour * 60 + min, str[0], str[1]) == 1) {
								return true;
							}
							valid = 1;
						}
					}
				}
				if (valid == 1) {
					return false;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception run power monitor!");
			System.out.println(e);
		}
		return true;
	}

	@SuppressLint("SimpleDateFormat")
	private String chkTomorrow(String[] datePw, String[] weekPw, String[] holiPw) {

		if (datePw == null && weekPw == null && holiPw == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getDefault());
		c.setTimeInMillis(System.currentTimeMillis() + 24 * 3600 * 1000);

		int week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 0)
			week = 7;
		week--;
		int i = 0;

		try {
			if (holiPw != null && holiPw.length > 0) {
				for (i = 0; i < holiPw.length; i++) {
					if (holiPw[i] != null && holiPw[i].length() > 12 && !holiPw[i].contains("00-00 00:00-00:00")) {
						String[] str = holiPw[i].split(" ");
						if (str.length < 2)
							continue;
						String[] strX = str[1].split("-");

						if (strX.length >= 2 && !str[1].contains("00:00-00:00") && chkDate(c, str[0])
								&& !strX[0].equals(strX[1])) {
							// --------------------------------------------------------------------------------------------------
							for (int k = 0; k < holiPw.length; k++) {
								if (holiPw[k] != null && holiPw[k].length() > 12
										&& !holiPw[k].contains("00-00 00:00-00:00")) {
									str = holiPw[k].split(" ");
									if (str.length < 2)
										continue;
									strX = str[1].split("-");
									if (strX.length >= 2 && !str[1].contains("00:00-00:00")) {
										if (chkDate(c, str[0]) && !strX[0].equals(strX[1])) {
											Date date = new Date();
											date.setTime(c.getTimeInMillis());
											SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
											return sdfD.format(date) + "|" + strX[0];
										}
									}
								}
							}
							// --------------------------------------------------------------------------------------------------
						}
					}
				}
			}

			if ((week >= 0 && week <= 6) && weekPw != null && weekPw.length > 0) {
				for (i = 0; i < weekPw.length; i++) {
					if (weekPw[i] != null && weekPw[i].length() > 4 && i == week
							&& !weekPw[i].contains("00:00-00:00")) {
						week++;
						if (week > 6)
							week = 0;
						// --------------------------------------------------------------------------------------------------
						for (int k = 0; k < weekPw.length; k++) {
							if (weekPw[k] != null && weekPw[k].length() > 4 && k == week
									&& !weekPw[k].contains("00:00-00:00")) {
								String[] str = weekPw[k].split("-");
								if (str.length == 2 && !str[0].equals(str[1])) {
									Date date = new Date();
									date.setTime(c.getTimeInMillis());
									SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
									return sdfD.format(date) + "|" + str[0];
								}
							}
						}
						// --------------------------------------------------------------------------------------------------
					}
				}
			}

			if (datePw != null && datePw.length > 0) {
				for (i = 0; i < datePw.length; i++) {
					if (datePw[i] != null && datePw[i].length() > 4) {
						String[] str = datePw[i].split("-");
						if (str.length == 2 && !str[0].equals(str[1])) {
							Date date = new Date();
							date.setTime(c.getTimeInMillis());
							SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
							return sdfD.format(date) + "|" + str[0];
						}
					}
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Exception run power monitor!");
			System.out.println(e);
		}
		return null;
	}

	@SuppressLint("SimpleDateFormat")
	private String getPowerOn(String[] datePw, String[] weekPw, String[] holiPw) {

		if (datePw == null && weekPw == null && holiPw == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getDefault());

		int min = c.get(Calendar.MINUTE);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int week = c.get(Calendar.DAY_OF_WEEK) - 1;
		if (week == 0)
			week = 7;
		week--;

		int i = 0;

		try {

			if (datePw != null && datePw.length > 0) {
				for (i = 0; i < datePw.length; i++) {
					if (datePw[i] != null && datePw[i].length() > 4) {
						String[] str = datePw[i].split("-");
						if (str.length == 2 && !str[0].equals(str[1])
								&& chkPowerTime(hour * 60 + min, str[0], str[1]) == 1) {
							for (int k = i + 1; k < datePw.length - 1; k++) {
								if (datePw[k] != null && datePw[k].length() > 4) {
									str = datePw[k].split("-");
									if (str.length == 2 && !str[0].equals(str[1])) {
										Date date = new Date();
										SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
										return sdfD.format(date) + "|" + str[0];
									}
								}
							}
							break;
						}
					}
				}
			}
			return chkTomorrow(datePw, weekPw, holiPw);

		} catch (Exception e) {
			Log.d(TAG, "Exception run power monitor!");
			System.out.println(e);
		}
		return null;
	}

	private boolean chkDate(Calendar c, String date) {

		try {
			if (c != null || date != null) {
				String[] dstr = date.split("-");
				int y = c.get(Calendar.YEAR);
				int m = c.get(Calendar.MONTH) + 1;
				int d = c.get(Calendar.DATE);

				if (dstr.length == 3 && Integer.parseInt(dstr[0]) == y && Integer.parseInt(dstr[1]) == m
						&& Integer.parseInt(dstr[2]) == d) {
					return true;
				} else if (dstr.length == 2 && Integer.parseInt(dstr[0]) == m && Integer.parseInt(dstr[1]) == d) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Parse date str error!");
		}
		return false;
	}

	private int chkPowerTime(int curTime, String bTime, String eTime) {

		if (curTime == 0 || bTime == null || eTime == null) {
			return 0;
		}
		try {
			String[] bstr = bTime.split(":");
			String[] estr = eTime.split(":");
			if (bstr != null && bstr.length >= 2 && estr != null && estr.length >= 2) {
				int t0 = Integer.parseInt(bstr[0]) * 60 + Integer.parseInt(bstr[1]);
				int t1 = Integer.parseInt(estr[0]) * 60 + Integer.parseInt(estr[1]);

				if (t0 != t1 && curTime >= t0 && curTime < t1) {// on time
					return 1;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Parse time str error!");
		}
		return 0;
	}

	public void reboot() {

		baseFun.appendLogInfo("it's time to power on,reboot", 4);
		baseFun.appendLogInfo(null, -1);
		baseFun.rebootnow(mContext);
		baseFun.runCmd("reboot");
	}

	public void sleep(int dur) {

		for (int i = 0; i < dur / 5; i++) {
			pwrThdAct = 0;
			try {
				SystemClock.sleep(5 * 1000);
			} catch (Exception e) {
			}
			if (baseFun.exitPlay == 1) {
				pwrThdAct = -1;
				break;
			}
		}
	}

	@SuppressLint({ "SdCardPath", "SimpleDateFormat" })
	@Override
	public void run() {

		sleep(2 * 60);
		for (;;) {
			try {
				if (checkPower(cfg.para.dayOnoff, cfg.para.weekOnoff, cfg.para.dateOnoff)) {

					Log.i(TAG, "\n===============POWER STATUS: ON=================\n");
					if (bgSta > 0) {
						baseFun.savdurCnt = 0;
						baseFun.savePlayDur(baseFun.durPath);
						sleep(30);
						reboot();
					}
				} else {

					Log.i(TAG, "\n===============POWER STATUS: OFF================\n");
					if (baseFun.cfgRuning == 0) {
						if ((++bgSta) < 32) {
							sendMessage(-1);
							// ================================================================
							try {
								String Str = getPowerOn(cfg.para.dayOnoff, cfg.para.weekOnoff, cfg.para.dateOnoff);
								if (Str != null) {
									String[] tstr = Str.split("\\|");
									if (tstr != null && tstr.length == 2) {
										Log.i(TAG, "=============POWER ON:" + tstr[0] + " " + tstr[1]
												+ "================");
										for (int k = 0; k < 2; k++) {
											baseFun.poweron(mContext, tstr[0], tstr[1]);
											SystemClock.sleep(15 * 1000);
										}
									}
								}

								SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
								SimpleDateFormat sdfT = new SimpleDateFormat("HH:mm");
								baseFun.poweroff(mContext, sdfD.format(new Date()), sdfT.format(new Date()));
							} catch (Exception e) {
								e.printStackTrace();
							}
							// ================================================================
							if (bgSta == 1) {
								// baseFun.writeCom("/dev/ttyCom1",9600,"sdfsd");
								baseFun.appendLogInfo("it's time to power off", 4);
								baseFun.appendLogInfo(null, -1);
							}
							baseFun.pausePlay = 1;

							Message message = Message.obtain(MainActivity.mHandler, MainActivity.PAUSE_PLAY);
							MainActivity.mHandler.sendMessage(message);
						}
					}
				}
				if (baseFun.exitPlay == 1) {
					baseFun.savdurCnt = 0;
					baseFun.savePlayDur(baseFun.durPath);
					pwrThdAct = -1;
					break;
				}
				sleep(45);

			} catch (Exception e) {
				baseFun.appendLogInfo("run power thread err", 4);
				baseFun.appendLogInfo(null, -1);
			}
		}
		baseFun.appendLogInfo("run power thread exit exception", 4); // 20150921
		baseFun.appendLogInfo(null, -1);
	}
}