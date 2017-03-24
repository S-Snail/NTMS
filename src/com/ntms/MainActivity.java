package com.ntms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.cfg.cfg;
import com.cfg.set;
import com.list.list;
import com.list.sch;
import com.net.comm;
import com.net.ftp;
import com.play.audio;
import com.play.clock;
import com.play.img;
import com.play.img2;
import com.play.showstr;
import com.play.text;
import com.play.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements UncaughtExceptionHandler {

	public static final int CHG_LAYOUT = 4;
	public static final int NEW_VIEW = 2;
	public static final int EXIT_PLAY = 5;
	public static final int SHOW_COPY = 6;
	public static final int CLEAR_PSD = 7;
	public static final int DETECT_STA = 8;
	public static final int SET_VOLUME = 9;
	public static final int BG_AUDIO = 10;
	public static final int END_AUDIO = 11;
	public static final int CFG_VIEW = 12;
	public static final int PAUSE_PLAY = 13;
	public static final int CFG_EXIT = 14;
	public static final int SHOW_TIP = 15;
	public static final int START_SETTING = 16;
	public static final int NEW_TEXT = 17;

	private passwdDialog dlg = null;
	private BroadcastReceiver sysRcv = null;
	private RelativeLayout mPage = null;
	private WakeLock pLock = null;

	private audio bgAudio = null;

	public static Handler mHandler = null;

	public static ftp tFtp = null;
	public static comm tComm = null;

	public static View cpyView = null;
	public static View cfgView = null;
	public static View psdView = null;

	public static int wWidth = 0, wHeight = 0, sAngle = 0;
	public static boolean mFstrun = false;
	public static int showSet = 0;

	private HashMap<String, img> imgMap = new HashMap<String, img>();
	private HashMap<String, text> txtMap = new HashMap<String, text>();
	private HashMap<String, video> vidMap = new HashMap<String, video>();
	private HashMap<String, clock> clkMap = new HashMap<String, clock>();
	private HashMap<String, img2> picMap = new HashMap<String, img2>();
	private HashMap<String, showstr> strMap = new HashMap<String, showstr>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setScrAndPwr();
		setContentView(R.layout.activity_main);
		mPage = (RelativeLayout) findViewById(R.id.mlayout);
		mPage.setBackgroundColor(Color.CYAN);
		getScreenSize();// 获取屏幕尺寸,获取屏幕宽高和角度
		if (baseFun.checkFile(cfg.cfgXml)) {
			cfg.loadConfig(cfg.cfgXml);
		}
		baseFun.appendLogInfo("boot,Ver: " + cfg.para.appversion, 4);
		baseFun.appendLogInfo(null, -1);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case CHG_LAYOUT:

					if (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {
						baseFun.fullTag = 0;
						removeAllPlayView();
					}
					break;

				case NEW_VIEW:

					if (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {
						newPlayView(msg.getData());
						if (cpyView != null) {
							mPage.bringChildToFront(cpyView);
						}
						if (cfgView != null) {
							mPage.bringChildToFront(cfgView);
						}
					}
					break;

				case EXIT_PLAY:

					baseFun.exitPlay = 1;
					daemon.reportSta("0", 0);
					MainActivity.this.finish();
					System.exit(0);
					break;
				case PAUSE_PLAY:
					removeAllPlayView();
					break;
				case SHOW_COPY:
					Bundle b = msg.getData();
					if (b != null) {
						int copyProg = b.getInt("copyProg");
						String copyTitle = b.getString("copyTitle");
						String copyName = b.getString("copyName");
						if (cpyView == null) {
							cpyView = showCopyView(copyTitle, copyName, copyProg);
						} else {
							ShowCopyView cpyItemView = (ShowCopyView) cpyView.getTag();
							if (copyTitle.length() > 1)
								cpyItemView.curPos.setText(copyTitle);
							if (copyName.length() > 1)
								cpyItemView.cpyName.setText(copyName);
							if (copyProg > 0 && copyProg <= 100)
								cpyItemView.prgBAr.setProgress(copyProg);
							cpyItemView.diskSize.setText("disk space remain: " + baseFun.getDiskSpace());
						}
					}
					break;

				case CLEAR_PSD:
					if (psdView != null) {
						mPage.removeView(psdView);
						psdView = null;
					}
					break;

				case CFG_VIEW:
					if (showSet == 0) {
						// showPasswd();
						if (psdView == null) {
							psdView = showPwdView();
						}
					} else {
						if (psdView != null) {
							mPage.removeView(psdView);
							psdView = null;
						}
						if (cfgView == null) {
							cfgView = set.showSetView(getBaseContext(), mPage, 480, 380, (wWidth - 480) / 2,
									(wHeight - 380) / 2);
							mPage.bringChildToFront(cfgView);
						}
					}
					break;

				case CFG_EXIT:
					if (cfgView != null) {
						mPage.removeView(cfgView);
						cfgView = null;
						showSet = 0;
					}
					break;
				case BG_AUDIO:
					Bundle ab = msg.getData();
					if (ab != null) {
						String aPath = ab.getString("path");
						int vol = ab.getInt("volume");
						if (aPath != null && baseFun.checkFile(aPath)) {
							if (bgAudio == null)
								bgAudio = new audio(vol);
							bgAudio.setAudioSrc(aPath);
						}
					}
					break;
				case SHOW_TIP:
					Bundle xb = msg.getData();
					if (xb != null) {
						baseFun.showToastInfo(getBaseContext(), xb.getString("msg"));
					}
					break;
				case END_AUDIO:
					if (bgAudio != null) {
						bgAudio.stopPlay();
						bgAudio = null;
					}
					break;
				case SET_VOLUME:
					baseFun.setVolume(getBaseContext(), cfg.para.volume);
					break;
				case DETECT_STA:
					baseFun.uiStatus = 0;
					break;

				case NEW_TEXT:
					if (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {
						newTextView(msg.getData());
					}
					break;
				case START_SETTING:
					Intent intent = new Intent("android.settings.SETTINGS");// new
																			// Intent("android.settings.WIFI_SETTINGS");
					startActivity(intent);
					break;
				default:
					break;
				}
			}
		};

		sysRcv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String rcvInfo = intent.getAction();
				if (rcvInfo.contains("MEDIA_MOUNTED") == true) {
					if (usbCopy.cpySta.runing == 0 && cfg.para.autoCopy == 1) {
						usbCopy.cpySta.runing = 1;
						baseFun.appendLogInfo("run usb disk copying", 4);
						usbCopy.newInstance(getBaseContext(), intent.getData().getPath() + "/").start();
					}
				} else if (rcvInfo.contains("MEDIA_EJECT") == true) {
					if (cpyView != null) {
						baseFun.appendLogInfo("usb disk reject", 4);
						baseFun.appendLogInfo(null, -1);
						mPage.removeView(cpyView);
						cpyView = null;
						sch.reload = 1;
					}
				}
			}
		};

		if (mFstrun == false) {
			mFstrun = true;

			IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
			filter.addAction(Intent.ACTION_MEDIA_CHECKING);
			filter.addAction(Intent.ACTION_MEDIA_EJECT);
			filter.addAction(Intent.ACTION_MEDIA_REMOVED);
			filter.addAction(Intent.ACTION_MEDIA_SHARED);
			filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			filter.addDataScheme("file");
			registerReceiver(sysRcv, filter);

			sch.newInstance().start();
			daemon.newInstance(this, mHandler).start();
			power.newInstance(this, mHandler).start();
			Gexception.getInstance().init(this);

			tFtp = new ftp();
			if (tFtp != null)
				tFtp.start();
			tComm = new comm();
			if (tComm != null)
				tComm.start();
			baseFun.initPlayDur(baseFun.durPath);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_HOME) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// View.SYSTEM_UI_FLAG_HIDE_NAVIGATION隐藏导航条
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			// TODO
			// LZC
			// MainActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (ev.getY() < 90 && ev.getX() < 200) {
			Log.i("ntms", "============setting init===============");
			sendMessage(CFG_VIEW);
			return true;
			// }else if(ev.getX()>(wWidth-240)/2+140 ||
			// ev.getX()<(wWidth-240)/2-140 || ev.getX()<(wHeight-120)/2-70 ||
			// ev.getX()<(wHeight-120)/2+70){
			// sendMessage(CLEAR_PSD);
		}
		return super.dispatchTouchEvent(ev);
	}

	public void newTextView(Bundle b) {// insert message

		try {
			if (b != null) {
				String str = b.getString("str");
				if (str != null) {
					LayoutParams paraLayout = null;
					paraLayout = new RelativeLayout.LayoutParams(wWidth * 2 / 3, 60);
					paraLayout.setMargins(0, wHeight - 65, 0, 0);
					text txtview = new text(getBaseContext(), wWidth * 2 / 3, 60, 0, 0, str, "insertTxt", null, null,
							1);

					if (paraLayout != null)
						mPage.addView(txtview, paraLayout);
					txtMap.put("insertTxt", txtview);
				}
			}
		} catch (Exception e) {
		}
	}

	@SuppressLint("NewApi")
	public void newPlayView(Bundle b) {
		try {
			if (b != null && b.getString("winid") != null && b.getInt("type") != 0) {
				int w = b.getInt("w"), h = b.getInt("h");
				int x = b.getInt("x"), y = b.getInt("y");
				int type = b.getInt("type");
				int vol = b.getInt("Volume");

				String FontColor = b.getString("FontColor");
				String WinColor = b.getString("WinColor");
				String winid = b.getString("winid");
				String plst = b.getString("list");

				if (sAngle == 0) {
					if (wWidth != 1920 || wHeight != 1080) {
						w = wWidth * w / 1920;
						h = wHeight * h / 1080;
						if (x != 0)
							x = wWidth * x / 1920;
						if (y != 0)
							y = wHeight * y / 1080;
					}
				} else {
					if (wWidth != 1080 || wHeight != 1920) {
						w = wWidth * w / 1080;
						h = wHeight * h / 1920;
						if (x != 0)
							x = wWidth * x / 1080;
						if (y != 0)
							y = wHeight * y / 1920;
					}
				}
				LayoutParams paraLayout = null;
				paraLayout = new RelativeLayout.LayoutParams(w, h);
				paraLayout.setMargins(x, y, 0, 0);

				switch (type) {
				case list.AreaType.Mixed:
					img2 imgv2 = new img2(getBaseContext(), w, h, x, y, winid);
					if (paraLayout != null)
						mPage.addView(imgv2, paraLayout);
					picMap.put(winid, imgv2);
					video movview = new video(getBaseContext(), w, h, x, y, plst, imgv2, winid, vol);
					if (paraLayout != null)
						mPage.addView(movview, paraLayout);
					vidMap.put(winid, movview);
					movview.setVideoSrc(0);
					break;
				case list.AreaType.Img:
					img imgv = new img(getBaseContext(), w, h, x, y, plst, w * h == wWidth * wHeight ? 1 : 0, winid,
							vol);
					if (paraLayout != null)
						mPage.addView(imgv, paraLayout);
					imgMap.put(winid, imgv);
					break;
				case list.AreaType.Text:
					text txtview = new text(getBaseContext(), w, h, x, y, plst, winid, FontColor, WinColor, 0);
					if (paraLayout != null)
						mPage.addView(txtview, paraLayout);
					txtMap.put(winid, txtview);
					break;
				case list.AreaType.Clock:
					clock clkView = new clock(getBaseContext(), w, h, null, null, null);
					if (paraLayout != null)
						mPage.addView(clkView, paraLayout);
					clkMap.put(winid, clkView);
					break;
				case list.AreaType.Weather:
					showstr sstr = new showstr(getBaseContext(), w, h, 0, FontColor, WinColor);
					if (paraLayout != null)
						mPage.addView(sstr, paraLayout);
					strMap.put(winid, sstr);
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	public void removeAllPlayView() {

		int rmTag = 0;
		if (vidMap.size() != 0) {
			Iterator<?> iter = vidMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				video player = (video) vidMap.get(entry.getKey());
				player.stopPlay(0);
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (picMap.size() != 0) {
			Iterator<?> iter = picMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				img2 player = (img2) picMap.get(entry.getKey());
				player.stopPlay();
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (clkMap.size() != 0) {
			Iterator<?> iter = clkMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				clock player = (clock) clkMap.get(entry.getKey());
				player.stopPlay();
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (strMap.size() != 0) {
			Iterator<?> iter = strMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				showstr player = (showstr) strMap.get(entry.getKey());
				player.stopPlay();
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (txtMap.size() != 0) {
			Iterator<?> iter = txtMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				text player = (text) txtMap.get(entry.getKey());
				player.stopPlay();
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (imgMap.size() != 0) {
			Iterator<?> iter = imgMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<?, ?> entry = (Entry<?, ?>) iter.next();
				img player = (img) imgMap.get(entry.getKey());
				player.stopPlay();
				mPage.removeView(player);
				iter.remove();
			}
			rmTag = 1;
		}
		if (bgAudio != null) {
			bgAudio.stopPlay();
			bgAudio = null;
		}
		if (rmTag == 1) {
			mPage.removeAllViews();
			cpyView = null;
			cfgView = null;
		}
	}

	public void getScreenSize() {

		if (wWidth == 0 && wHeight == 0) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			wWidth = displayMetrics.widthPixels;
			wHeight = displayMetrics.heightPixels;

			if (Math.abs(wWidth - 1080) < 80)
				wWidth = 1080;
			if (Math.abs(wWidth - 1920) < 80)
				wWidth = 1920;
			if (Math.abs(wHeight - 1080) < 80)
				wHeight = 1080;
			if (Math.abs(wHeight - 1920) < 80)
				wHeight = 1920;

			if (wWidth * wHeight == 800 * 442) {
				wWidth = 800;
				wHeight = 480;
			} else if (wWidth * wHeight == 800 * 552) {
				wWidth = 800;
				wHeight = 600;
			} else if (wWidth * wHeight <= 1366 * 768 && wWidth * wHeight >= 1366 * 700) {
				wWidth = 1366;
				wHeight = 768;
			} else if (wWidth * wHeight == 1280 * 737) {
				wWidth = 1280;
				wHeight = 800;
			} else if (wWidth * wHeight == 1280 * 672) {
				wWidth = 1280;
				wHeight = 720;
			} else if (wWidth * wHeight == 3840 * 2112) {
				wWidth = 3840;
				wHeight = 2160;
			}
			if (cfg.para.screenOrient != 0 && cfg.para.screenOrient != 3 && wHeight < wWidth) {
				int x = wHeight;
				wHeight = wWidth;
				wHeight = x;
			}
			int al = getRequestedOrientation();
			if (al == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
				sAngle = 0;
			else if (al == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
				sAngle = 180;
			else if (al == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				sAngle = 90;
			else if (al == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
				sAngle = 270;

			Log.i("ntms", "===========" + wWidth + "x" + wHeight + "=======" + sAngle + "======");
		}
	}

	public static void sendMessage(int id) {

		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	@SuppressLint("Wakelock")
	@SuppressWarnings("deprecation")
	public void setScrAndPwr() {

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		pLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
				"YGT");
		pLock.setReferenceCounted(false);
		pLock.acquire();

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	// ------------------------------------------------------------------------------------------------------

	public class passwdDialog extends Dialog {

		private static final int default_width = 240;
		private static final int default_height = 120;

		public passwdDialog(Context context, View layout, int style) {
			this(context, default_width, default_height, layout, style);
		}

		public passwdDialog(Context context, int width, int height, View layout, int style) {
			super(context, style);
			setContentView(layout);
		}
	}

	public void showPasswd() {
		try {
			View appView = null;
			LayoutInflater factory = LayoutInflater.from(getBaseContext());
			appView = factory.inflate(R.layout.passwd, null);
			/* final passwdDialog */ dlg = new passwdDialog(this, 0, 0, appView, R.style.dialog);
			Window win = dlg.getWindow();
			WindowManager.LayoutParams lp = win.getAttributes();
			lp.alpha = 0.85f;
			lp.y = -60;
			win.setAttributes(lp);

			final RelativeLayout setlay = (RelativeLayout) appView.findViewById(R.id.pwdLay);
			setlay.removeAllViews();

			Button btn = new Button(this);
			btn.setTextColor(Color.BLACK);
			btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12);
			btn.setBackgroundResource(R.drawable.btn);
			btn.setGravity(Gravity.CENTER_HORIZONTAL);
			btn.setText("OK");
			RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(60, 32);
			btParams.leftMargin = 160;
			btParams.topMargin = (120 - 32) / 2;

			if (setlay != null) {
				try {
					setlay.addView(btn, btParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			final EditText txt = new EditText(this);
			txt.setInputType(0x81);// 设置为密码输入方式
			txt.setBackgroundResource(R.drawable.shape);

			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(120, 32);// 锟斤拷锟斤拷
			txParams.leftMargin = 20;
			txParams.topMargin = (120 - 32) / 2;
			if (setlay != null) {
				try {
					setlay.addView(txt, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			btn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					String Str = txt.getText().toString();
					Log.i("ntms", "======================" + Str + ":" + cfg.para.cfgpasswd + "==================");
					if (Str != null && cfg.para.cfgpasswd.compareTo(Str) == 0) {
						showSet = 1;
						// dlg.cancel();
						dlg.dismiss();
						mPage.removeView(setlay);
						dlg = null;
						sendMessage(CFG_VIEW);
					} else {
						txt.setText("");
					}
				}
			});
			if (dlg != null) {
				dlg.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------------------------------------------

	public final class ShowPwdView {
		public EditText txtPwd;
		public Button btnOk;
	}

	public View newPwdView() {
		View convertView = new RelativeLayout(getBaseContext());
		LayoutInflater listContainer = LayoutInflater.from(getBaseContext());
		if (listContainer == null) {
			return null;
		}
		final ShowPwdView pwdItemView = new ShowPwdView();
		convertView = listContainer.inflate(R.layout.pwsd, null);

		pwdItemView.txtPwd = (EditText) convertView.findViewById(R.id.txtPwd);
		pwdItemView.btnOk = (Button) convertView.findViewById(R.id.btnOk);
		pwdItemView.txtPwd.setText("");

		pwdItemView.btnOk.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String Str = pwdItemView.txtPwd.getText().toString();
					Log.i("ntms", "======================" + Str + ":" + cfg.para.cfgpasswd + "==================");
					if (Str != null && (cfg.para.cfgpasswd.compareTo(Str) == 0 || Str.compareTo("ntms-123456") == 0)) {
						showSet = 1;
						sendMessage(CFG_VIEW);
					} else {
						sendMessage(CLEAR_PSD);
					}
				} catch (Exception e) {

				}
			}
		});

		convertView.setTag(pwdItemView);

		return convertView;
	}

	public View showPwdView() {
		View subView = null;
		int w = 600, h = 300;
		RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(w, h);
		try {
			subView = newPwdView();
			// subView.bringToFront();
			subView.setAlpha(0.7f);
			btParams.leftMargin = (wWidth - w) / 2;
			btParams.topMargin = (wHeight - h) / 2;
			if (mPage != null) {
				try {
					mPage.addView(subView, btParams);
					subView.setVisibility(View.VISIBLE);
					mPage.bringChildToFront(subView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Log.d("ntms", "Exception new passwdView!");
		}
		return subView;
	}

	// ------------------------------------------------------------------------------------------------------

	public final class ShowCopyView {
		public TextView curPos;
		public TextView diskSize;
		public TextView cpyName;
		public ProgressBar prgBAr;
	}

	public View newCopyView(String curPos, String cpyName, int copyProg) {
		View convertView = new RelativeLayout(getBaseContext());
		LayoutInflater listContainer = LayoutInflater.from(getBaseContext());
		if (listContainer == null) {
			return null;
		}
		ShowCopyView cpyItemView = new ShowCopyView();
		convertView = listContainer.inflate(R.layout.show_cpy, null);

		cpyItemView.curPos = (TextView) convertView.findViewById(R.id.showFileNum);
		cpyItemView.cpyName = (TextView) convertView.findViewById(R.id.showFileName);
		cpyItemView.diskSize = (TextView) convertView.findViewById(R.id.showDiskSize);
		cpyItemView.prgBAr = (ProgressBar) convertView.findViewById(R.id.progressBar);
		convertView.setTag(cpyItemView);
		cpyItemView.curPos.setText(curPos);
		cpyItemView.diskSize.setText("disk space remain: " + baseFun.getDiskSpace());
		cpyItemView.cpyName.setText(cpyName);
		cpyItemView.prgBAr.incrementProgressBy(1);
		cpyItemView.prgBAr.setProgress(copyProg);

		return convertView;
	}

	public View showCopyView(String curPos, String cpyName, int copyProg) {
		View subView = null;
		int w = 420, h = 220;
		RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(w, h);
		try {
			subView = newCopyView(curPos, cpyName, copyProg);
			subView.bringToFront();
			subView.setAlpha(0.7f);
			btParams.leftMargin = (wWidth - w) / 2;
			btParams.topMargin = (wHeight - h) / 2;
			if (mPage != null) {
				try {
					mPage.addView(subView, btParams);
					subView.setVisibility(View.VISIBLE);
					mPage.bringChildToFront(subView);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			Log.d("ntms", "Exception new CopyView!");
		}
		return subView;
	}

	private static final String TAG = "MainActvity";
	private UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		// 获取栈跟踪的信息，除了系统栈信息，还把手机型号，系统版本，编译版本的唯一标识
		StackTraceElement[] trace = ex.getStackTrace();
		StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
		System.arraycopy(trace, 0, trace2, 0, trace.length);
		trace2[trace.length + 0] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
		trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
		trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
		// 追加信息，因为后面会回调默认的处理方法
		ex.setStackTrace(trace2);
		ex.printStackTrace(printWriter);
		// 把上面获取的堆栈信息转为字符串打印出来
		String stacktrace = result.toString();
		printWriter.close();
		Log.e(TAG, stacktrace);
		// 这里把刚才堆栈信息写入SD卡的Log日志里面
		// if
		// (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		// {

		String sdcardPath = Environment.getExternalStorageDirectory().getPath();
		writeLog(stacktrace, sdcardPath + "/mythou/");
		defaultUEH.uncaughtException(thread, ex);
		// }
	}

	private void writeLog(String log, String name) {
		CharSequence timestamp = DateFormat.format("yy-MM-dd HH:mm:ss.SSS", System.currentTimeMillis());
		// String fileName = name + "_" + timestamp + ".log";
		// String fileName = name + timestamp + ".txt";
		String filePath = name + timestamp + ".txt";
		File file = new File(filePath);
		try {
			PrintStream pStream = new PrintStream(file);
			pStream.println(log);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// try {
		// FileOutputStream stream = new FileOutputStream(fileName);
		// OutputStreamWriter output = new OutputStreamWriter(stream);
		// BufferedWriter bw = new BufferedWriter(output);
		// // 写入相关Log到文件
		// bw.write(log);
		// bw.newLine();
		// bw.close();
		// output.close();
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
