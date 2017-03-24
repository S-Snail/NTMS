package com.cfg;

import java.io.File;

import com.ntms.MainActivity;
import com.ntms.R;
import com.ntms.baseFun;
import com.ntms.logCopy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("SdCardPath")
public class set {

	private static class BtnView {
		public Button btnSetting;
		public Button btnExit;
		public Button btnClose;
		public Button btnTime;
		public Button btnOnOffWeek;
		public Button btnOnOffDay;
		public Button btnNet;
		public Button btnFtp;
		public Button btnFile;
		public Button btnSet;
	}

	private static class timeView {
		public CheckBox chkDisplay;
		public EditText edtDate;
		public EditText edtTime;
		public CheckBox chkOnOff;
		public CheckBox chkSyncMode;
		public EditText edtVolTime;
		public TextView txtVol;
		public TextView txtFwver;
		// DISPLAY ON
		// DATE 2015/04/08
		// TIME 10:00
		// ON/OFF ON
		// SYNC MODE NO
		// AUTO VOL TIME 10:00
		// AUTO VOLUME 50
		// FIRMWARE VER 3.30-151228
	}

	private static class weekView {
		public CheckBox chkAuto;
		public EditText[] edtWeekb = new EditText[7];
		public EditText[] edtWeeke = new EditText[7];
		// AUTO ON/OFF OFF
		// SUNDAY 00:00 00:00
		// MONDAY 00:00 00:00
		// TUSDAY 00:00 00:00
		// WENDESDAY 00:00 00:00
		// THURSDAY 00:00 00:00
		// FRIDAY 00:00 00:00
		// SATURDAY 00:00 00:00
	}

	private static class dayView {
		public CheckBox chkAuto;
		public EditText[] edtDayb = new EditText[7];
		public EditText[] edtDaye = new EditText[7];
		// AUTO ON/OFF OFF
		// ON/OFF 1 00:00 00:00
		// ON/OFF 2 00:00 00:00
		// ON/OFF 3 00:00 00:00
		// ON/OFF 4 00:00 00:00
		// ON/OFF 5 00:00 00:00
		// ON/OFF 6 00:00 00:00
		// ON/OFF 7 00:00 00:00
	}

	private static class netView {
		public EditText edtSvrIp;
		public EditText edtSvrPort;
		public TextView txtLocalIp;
		public TextView txtNetmask;
		public TextView txtGateWay;
		public Button btnSet;
		// SERVER IP 1920.168.1.1
		// SERVER PORT 8900
		// DHCP DISABLE
		// LOCAL IP 192.168.1.1
		// NETMASK
		// GATEWAY
		// PING
	}

	private static class ftpView {
		public EditText edtFtp1;
		public EditText edtPort1;
		public EditText edtUsr1;
		public EditText edtPasswd1;
		public EditText edtFtp2;
		public EditText edtPort2;
		public EditText edtUsr2;
		public EditText edtPasswd2;
		// FTP1 IP
		// FTP1 PORT
		// FTP1 USER
		// FTP1 PASSWORD
		//
		// FTP2 IP
		// FTP2 PORT
		// FTP2 USER
		// FTP2 PASSWORD
	}

	private static class fileView {
		public Button btnCpy;
		public Button btnClear;
		public Button btnCpLog;
		public TextView txtDskSize;
		public TextView txtDskUsg;
		public CheckBox chkValid;
		public CheckBox chkCpyOn;
		public CheckBox chkLogOn;
		// COPY DISK
		// CLEAR DISK
		// COPY PLAYLOG
		// DISK SIZE
		// DISK USAGE
		// FILE VALIDATION OFF
		// AUOT COPY ON
		// SAVE PLAYLOG OFF
	}

	private static class setView {
		public EditText edtVol;
		public EditText edtPwd;
		public EditText edtPwd1;
		public TextView txtResolution;
		public TextView txtRotate;// 循环播放
		public Button btnUpdate;// 固件：驱动程序
		// VOLUME 50
		// NEW PASSWD
		// CONFIRM PASSWD
		// RESOLUTION
		// ROTATION 0
		// UPDATE FIRMWARE
	}

	private static RelativeLayout layTime = null;
	private static RelativeLayout layWeek = null;
	private static RelativeLayout layDay = null;
	private static RelativeLayout layNet = null;
	private static RelativeLayout layFtp = null;
	private static RelativeLayout layFile = null;
	private static RelativeLayout laySet = null;
	private static RelativeLayout layTitle = null;

	private static timeView timeV = new timeView();
	private static weekView weekV = new weekView();
	private static dayView dayV = new dayView();
	private static netView netV = new netView();
	private static ftpView ftpV = new ftpView();
	private static fileView fileV = new fileView();
	private static setView setV = new setView();
	private static BtnView btnView = new BtnView();

	private static void getAllVal() {

		String vol = setV.edtVol.getText().toString();
		if (vol != null) {
			try {
				cfg.para.volume = Integer.parseInt(vol);
			} catch (Exception e) {
			}
		}
		String pwd0 = setV.edtPwd.getText().toString();
		String pwd1 = setV.edtPwd1.getText().toString();
		if (pwd0 != null && pwd1 != null && pwd0.compareTo(pwd1) == 0) {
			cfg.para.cfgpasswd = pwd0;// 改密码
		}
		String voltime = timeV.edtVolTime.getText().toString();
		if (voltime != null) {
			cfg.para.voltime = voltime;// 设置时间
		}
		// --------------------------------------------------------------------------
		String svrip = netV.edtSvrIp.getText().toString();
		if (svrip != null) {
			cfg.para.mip = svrip;// 更改IP
		}
		String svrport = netV.edtSvrPort.getText().toString();
		if (svrport != null) {
			cfg.para.mipport = svrport;// 更改端口号
		}
		// --------------------------------------------------------------------------
		String ftp1 = ftpV.edtFtp1.getText().toString();
		if (ftp1 != null) {
			cfg.para.mftp = ftp1;// 设置ftp
		}
		String port1 = ftpV.edtPort1.getText().toString();
		if (port1 != null) {
			cfg.para.mftpport = port1;// 设置ftp端口号
		}
		String usr1 = ftpV.edtUsr1.getText().toString();
		if (usr1 != null) {
			cfg.para.mftpuser = usr1;// 设置用户1
		}
		String fpwd1 = ftpV.edtPasswd1.getText().toString();
		if (fpwd1 != null) {
			cfg.para.mftppwd = fpwd1;// 设置密码1
		}
		// --------------------------------------------------------------------------
		String ftp2 = ftpV.edtFtp2.getText().toString();
		if (ftp2 != null) {
			cfg.para.sftp = ftp1;
		}
		String port2 = ftpV.edtPort2.getText().toString();
		if (port2 != null) {
			cfg.para.sftpport = port1;
		} // 使用ftp1和port1
		String usr2 = ftpV.edtUsr2.getText().toString();
		if (usr2 != null) {
			cfg.para.sftpuser = usr2;// 设置用户2
		}
		String fpwd2 = ftpV.edtPasswd2.getText().toString();
		if (fpwd1 != null) {
			cfg.para.sftppwd = fpwd2;// 设置密码2
		}
		// --------------------------------------------------------------------------
		for (int i = 0; i < 7; i++) {
			try {
				String wb = weekV.edtWeekb[i].getText().toString();
				String we = weekV.edtWeeke[i].getText().toString();
				if (wb != null && we != null) {
					if (i == 0) {
						cfg.para.weekOnoff[6] = wb + "-" + we;
					} else {
						cfg.para.weekOnoff[i - 1] = wb + "-" + we;
					}
				}
				String db = dayV.edtDayb[i].getText().toString();
				String de = dayV.edtDaye[i].getText().toString();
				if (db != null && de != null) {
					cfg.para.dayOnoff[i] = db + "-" + de;
				}
			} catch (Exception e) {
			}
		}
		Log.i("set", "save all setting");
	}

	// 设置set界面
	private static int genSetView(Context baseCtx, RelativeLayout lay, int width, int height) {

		setV.edtVol = new EditText(baseCtx);
		setV.edtPwd = new EditText(baseCtx);
		setV.edtPwd1 = new EditText(baseCtx);
		setV.txtResolution = new TextView(baseCtx);
		setV.txtRotate = new TextView(baseCtx);
		setV.btnUpdate = new Button(baseCtx);

		setV.btnUpdate.setTextColor(Color.BLACK);
		setV.btnUpdate.setText("Run");
		setV.btnUpdate.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
		setV.btnUpdate.setBackgroundResource(R.drawable.btn);
		setV.btnUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/**
				 * ？？？
				 */
				baseFun.runCmd("mkdir -p /cache/recovery");
				baseFun.runCmd("echo \"--update_package=/mnt/sdcard/ota.zip\" > /cache/recovery/command");
				baseFun.runCmd("reboot recovery");
			}
		});

		lay.removeAllViews();
		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				setV.edtVol.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				setV.edtVol.setVisibility(View.VISIBLE);
				setV.edtVol.setTextColor(Color.BLACK);
				setV.edtVol.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				setV.edtVol.setText("" + cfg.para.volume);// 0
				setV.edtVol.setInputType(InputType.TYPE_CLASS_NUMBER);
				setV.edtVol.setBackgroundResource(R.drawable.shape);
			} else if (i == 1) {
				setV.edtPwd.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				setV.edtPwd.setVisibility(View.VISIBLE);
				setV.edtPwd.setTextColor(Color.BLACK);
				setV.edtPwd.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				setV.edtPwd.setInputType(0x81);// 设置EditText仅为密码输入方式
				setV.edtPwd.setBackgroundResource(R.drawable.shape);
			} else if (i == 2) {
				setV.edtPwd1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				setV.edtPwd1.setInputType(0x81);// 仅为密码输入方式
				setV.edtPwd1.setVisibility(View.VISIBLE);
				setV.edtPwd1.setTextColor(Color.BLACK);
				setV.edtPwd1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				setV.edtPwd1.setBackgroundResource(R.drawable.shape);
			} else if (i == 3) {
				setV.txtResolution.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				setV.txtResolution.setVisibility(View.VISIBLE);
				setV.txtResolution.setTextColor(Color.BLACK);
				setV.txtResolution.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				setV.txtResolution.setText(MainActivity.wWidth + "x" + MainActivity.wHeight);
			} else if (i == 4) {
				setV.txtRotate.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				setV.txtRotate.setVisibility(View.VISIBLE);
				setV.txtRotate.setTextColor(Color.BLACK);
				setV.txtRotate.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				setV.txtRotate.setText("" + MainActivity.sAngle);
			}
			int lw = width / 2 - 30;
			if (i == 5)
				lw = 58;
			int lh = height / 10;
			if (i == 5)
				lh = 32;
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(lw, lh);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					if (i == 0)
						lay.addView(setV.edtVol, txParams);
					else if (i == 1)
						lay.addView(setV.edtPwd, txParams);
					else if (i == 2)
						lay.addView(setV.edtPwd1, txParams);
					else if (i == 3)
						lay.addView(setV.txtResolution, txParams);//
					else if (i == 4)
						lay.addView(setV.txtRotate, txParams);//
					else if (i == 5)
						lay.addView(setV.btnUpdate, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	// 设置File界面
	private static int genFileView(Context baseCtx, RelativeLayout lay, int width, int height) {

		String[] tstDsk = null;

		fileV.btnCpy = new Button(baseCtx);
		fileV.btnClear = new Button(baseCtx);
		fileV.btnCpLog = new Button(baseCtx);
		fileV.txtDskSize = new TextView(baseCtx);
		fileV.txtDskUsg = new TextView(baseCtx);
		fileV.chkValid = new CheckBox(baseCtx);
		fileV.chkCpyOn = new CheckBox(baseCtx);
		fileV.chkLogOn = new CheckBox(baseCtx);

		fileV.btnCpLog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logCopy.copyStart = 1;
				logCopy.newInstance("").start();
			}
		});
		fileV.btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				baseFun.deleteFolder(new File("/mnt/sdcard/ntms/"));// 删除文件夹，递归算法
				baseFun.deleteFolder(new File("/mnt/sdcard/oplog/"));
				baseFun.deleteFolder(new File("/mnt/sdcard/pllog/"));
			}
		});
		fileV.chkValid = new CheckBox(baseCtx);// 有效的
		fileV.chkValid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.filevalid = isChecked ? 1 : 0;
			}
		});
		fileV.chkCpyOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.autoCopy = isChecked ? 1 : 0;
			}
		});
		fileV.chkLogOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.logEnable = isChecked ? 1 : 0;
			}
		});
		String dskStr = baseFun.getDiskSpace();// 获取磁盘空间
		if (dskStr != null) {
			tstDsk = dskStr.split("/");// 拆分字符串
		}

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				fileV.btnCpy.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				fileV.btnCpy.setBackgroundResource(R.drawable.btn);

			} else if (i == 1) {
				fileV.btnClear.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				fileV.btnClear.setBackgroundResource(R.drawable.btn);
				fileV.btnClear.setTextColor(Color.BLACK);
				fileV.btnClear.setText("Run");
			} else if (i == 2) {
				fileV.btnCpLog.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				fileV.btnCpLog.setBackgroundResource(R.drawable.btn);
				fileV.btnCpLog.setTextColor(Color.BLACK);
				fileV.btnCpLog.setText("Run");
			} else if (i == 3) {
				fileV.txtDskSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				fileV.txtDskSize.setVisibility(View.VISIBLE);
				fileV.txtDskSize.setTextColor(Color.BLACK);
				fileV.txtDskSize.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				fileV.txtDskSize.setText((tstDsk != null && tstDsk.length == 2) ? tstDsk[1] : "0M");
			} else if (i == 4) {
				fileV.txtDskUsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				fileV.txtDskUsg.setVisibility(View.VISIBLE);
				fileV.txtDskUsg.setTextColor(Color.BLACK);
				fileV.txtDskUsg.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				fileV.txtDskUsg.setText((tstDsk != null && tstDsk.length == 2) ? tstDsk[0] : "0M");
			} else if (i == 5) {
				fileV.chkValid.setChecked(cfg.para.filevalid == 1 ? true : false);
			} else if (i == 6) {
				fileV.chkCpyOn.setChecked(cfg.para.autoCopy == 1 ? true : false);
			} else if (i == 7) {
				fileV.chkLogOn.setChecked(cfg.para.logEnable == 1 ? true : false);
			}
			int lw = width / 2 - 30;
			if (i == 1 || i == 2)
				lw = 58;
			int lh = height / 10;
			if (i == 1 || i == 2)
				lh = 32;
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(lw, lh);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					if (i == 0) {
					} // lay.addView(fileV.btnCpy,txParams);
					else if (i == 1)
						lay.addView(fileV.btnClear, txParams);
					else if (i == 2)
						lay.addView(fileV.btnCpLog, txParams);
					else if (i == 3)
						lay.addView(fileV.txtDskSize, txParams);
					else if (i == 4)
						lay.addView(fileV.txtDskUsg, txParams);
					else if (i == 5)
						lay.addView(fileV.chkValid, txParams);
					else if (i == 6)
						lay.addView(fileV.chkCpyOn, txParams);
					else if (i == 7)
						lay.addView(fileV.chkLogOn, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	private static int genFtpView(Context baseCtx, RelativeLayout lay, int width, int height) {

		ftpV.edtFtp1 = new EditText(baseCtx);
		ftpV.edtPort1 = new EditText(baseCtx);
		ftpV.edtUsr1 = new EditText(baseCtx);
		ftpV.edtPasswd1 = new EditText(baseCtx);
		ftpV.edtFtp2 = new EditText(baseCtx);
		ftpV.edtPort2 = new EditText(baseCtx);
		ftpV.edtUsr2 = new EditText(baseCtx);
		ftpV.edtPasswd2 = new EditText(baseCtx);

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				ftpV.edtFtp1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtFtp1.setVisibility(View.VISIBLE);
				ftpV.edtFtp1.setTextColor(Color.BLACK);
				ftpV.edtFtp1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtFtp1.setText(cfg.para.mftp);
				ftpV.edtFtp1.setBackgroundResource(R.drawable.shape);
			} else if (i == 1) {
				ftpV.edtPort1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtPort1.setVisibility(View.VISIBLE);
				ftpV.edtPort1.setTextColor(Color.BLACK);
				ftpV.edtPort1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtPort1.setInputType(InputType.TYPE_CLASS_NUMBER);
				ftpV.edtPort1.setText(cfg.para.mftpport);
				ftpV.edtPort1.setBackgroundResource(R.drawable.shape);
			} else if (i == 2) {
				ftpV.edtUsr1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtUsr1.setVisibility(View.VISIBLE);
				ftpV.edtUsr1.setTextColor(Color.BLACK);
				ftpV.edtUsr1.setInputType(InputType.TYPE_CLASS_NUMBER);
				ftpV.edtUsr1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtUsr1.setText(cfg.para.mftpuser);
				ftpV.edtUsr1.setBackgroundResource(R.drawable.shape);
			} else if (i == 3) {
				ftpV.edtPasswd1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtPasswd1.setVisibility(View.VISIBLE);
				ftpV.edtPasswd1.setTextColor(Color.BLACK);
				ftpV.edtPasswd1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtPasswd1.setText(cfg.para.mftppwd);
				ftpV.edtPasswd1.setBackgroundResource(R.drawable.shape);
			} else if (i == 4) {
				ftpV.edtFtp2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtFtp2.setVisibility(View.VISIBLE);
				ftpV.edtFtp2.setTextColor(Color.BLACK);
				ftpV.edtFtp2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtFtp2.setText(cfg.para.sftp);
				ftpV.edtFtp2.setBackgroundResource(R.drawable.shape);
			} else if (i == 5) {
				ftpV.edtPort2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtPort2.setVisibility(View.VISIBLE);
				ftpV.edtPort2.setTextColor(Color.BLACK);
				ftpV.edtPort2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtPort2.setInputType(InputType.TYPE_CLASS_NUMBER);
				ftpV.edtPort2.setText(cfg.para.sftpport);
				ftpV.edtPort2.setBackgroundResource(R.drawable.shape);
			} else if (i == 6) {
				ftpV.edtUsr2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtUsr2.setVisibility(View.VISIBLE);
				ftpV.edtUsr2.setTextColor(Color.BLACK);
				ftpV.edtUsr2.setInputType(InputType.TYPE_CLASS_NUMBER);
				ftpV.edtUsr2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtUsr2.setText(cfg.para.sftpuser);
				ftpV.edtUsr2.setBackgroundResource(R.drawable.shape);
			} else if (i == 7) {
				ftpV.edtPasswd2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				ftpV.edtPasswd2.setVisibility(View.VISIBLE);
				ftpV.edtPasswd2.setTextColor(Color.BLACK);
				ftpV.edtPasswd2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				ftpV.edtPasswd2.setText(cfg.para.sftppwd);
				ftpV.edtPasswd2.setBackgroundResource(R.drawable.shape);
			}
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 30, height / 10);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					if (i == 0)
						lay.addView(ftpV.edtFtp1, txParams);
					else if (i == 1)
						lay.addView(ftpV.edtPort1, txParams);
					else if (i == 2)
						lay.addView(ftpV.edtUsr1, txParams);
					else if (i == 3)
						lay.addView(ftpV.edtPasswd1, txParams);
					else if (i == 4)
						lay.addView(ftpV.edtFtp2, txParams);
					else if (i == 5)
						lay.addView(ftpV.edtPort2, txParams);
					else if (i == 6)
						lay.addView(ftpV.edtUsr2, txParams);
					else if (i == 7)
						lay.addView(ftpV.edtPasswd2, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	private static int genNetView(final Context baseCtx, RelativeLayout lay, int width, int height) {

		netV.edtSvrIp = new EditText(baseCtx);
		netV.edtSvrPort = new EditText(baseCtx);
		netV.txtLocalIp = new TextView(baseCtx);
		netV.txtNetmask = new TextView(baseCtx);
		netV.txtGateWay = new TextView(baseCtx);
		netV.btnSet = new Button(baseCtx);

		netV.btnSet.setText("Set");
		netV.btnSet.setTextColor(Color.BLACK);
		netV.btnSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("android.settings.WIFI_SETTINGS");// 启动手机WiFi设置
				baseCtx.startActivity(intent);
			}
		});

		lay.removeAllViews();
		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				netV.edtSvrIp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				netV.edtSvrIp.setVisibility(View.VISIBLE);
				netV.edtSvrIp.setTextColor(Color.BLACK);
				netV.edtSvrIp.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				netV.edtSvrIp.setText(cfg.para.mip);
				netV.edtSvrIp.setBackgroundResource(R.drawable.shape);
			} else if (i == 1) {
				netV.edtSvrPort.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				netV.edtSvrPort.setVisibility(View.VISIBLE);
				netV.edtSvrPort.setTextColor(Color.BLACK);
				netV.edtSvrPort.setInputType(InputType.TYPE_CLASS_NUMBER);
				netV.edtSvrPort.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				netV.edtSvrPort.setText(cfg.para.mipport);
				netV.edtSvrPort.setBackgroundResource(R.drawable.shape);
			} else if (i == 3) {
				netV.txtLocalIp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				netV.txtLocalIp.setVisibility(View.VISIBLE);
				netV.txtLocalIp.setTextColor(Color.BLACK);
				netV.txtLocalIp.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				netV.txtLocalIp.setText(baseFun.getIpAddress());
			} else if (i == 4) {
				netV.txtNetmask.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				netV.txtNetmask.setVisibility(View.VISIBLE);
				netV.txtNetmask.setTextColor(Color.BLACK);
				netV.txtNetmask.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				netV.txtNetmask.setText(baseFun.getMac());
			} else if (i == 5) {
				netV.txtGateWay.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				netV.txtGateWay.setVisibility(View.VISIBLE);
				netV.txtGateWay.setTextColor(Color.BLACK);
				netV.txtGateWay.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				netV.txtGateWay.setText(baseFun.getGateway("eth0"));
			}
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 30, height / 10);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					if (i == 0)
						lay.addView(netV.edtSvrIp, txParams);
					else if (i == 1)
						lay.addView(netV.edtSvrPort, txParams);
					else if (i == 3)
						lay.addView(netV.txtLocalIp, txParams);
					else if (i == 4)
						lay.addView(netV.txtNetmask, txParams);
					else if (i == 5)
						lay.addView(netV.txtGateWay, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	private static int genDayView(Context baseCtx, RelativeLayout lay, int width, int height) {

		dayV.chkAuto = new CheckBox(baseCtx);
		dayV.chkAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.pwrEnable = isChecked ? 1 : 0;
			}
		});

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				dayV.chkAuto.setChecked(cfg.para.pwrEnable == 1 ? true : false);
			} else {
				dayV.edtDayb[i - 1] = new EditText(baseCtx);
				dayV.edtDaye[i - 1] = new EditText(baseCtx);

				dayV.edtDayb[i - 1].setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				dayV.edtDayb[i - 1].setVisibility(View.VISIBLE);
				dayV.edtDayb[i - 1].setTextColor(Color.BLACK);
				dayV.edtDayb[i - 1].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				dayV.edtDayb[i - 1].setInputType(InputType.TYPE_CLASS_DATETIME);
				dayV.edtDayb[i - 1].setBackgroundResource(R.drawable.shape);

				dayV.edtDaye[i - 1].setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				dayV.edtDaye[i - 1].setVisibility(View.VISIBLE);
				dayV.edtDaye[i - 1].setTextColor(Color.BLACK);
				dayV.edtDaye[i - 1].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				dayV.edtDaye[i - 1].setInputType(InputType.TYPE_CLASS_DATETIME);
				dayV.edtDaye[i - 1].setBackgroundResource(R.drawable.shape);

				String str = cfg.para.dateOnoff[i - 1];
				if (str != null) {
					String[] wk = str.split("-");
					if (wk != null && wk.length == 2) {
						dayV.edtDayb[i - 1].setText(wk[0]);
						dayV.edtDaye[i - 1].setText(wk[1]);
					}
				}
			}
			if (i == 0) {
				RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 30, height / 10);
				txParams.leftMargin = 0;
				txParams.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(dayV.chkAuto, txParams);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				RelativeLayout.LayoutParams txParamsb = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamsb.leftMargin = 0;
				txParamsb.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(dayV.edtDayb[i - 1], txParamsb);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				RelativeLayout.LayoutParams txParamse = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamse.leftMargin = 90;
				txParamse.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(dayV.edtDaye[i - 1], txParamse);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				TextView tv = new TextView(baseCtx);
				tv.setText(" - ");
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
				RelativeLayout.LayoutParams txParamsm = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamsm.leftMargin = 78;
				txParamsm.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(tv, txParamsm);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return 0;
	}

	private static int genWeekView(Context baseCtx, RelativeLayout lay, int width, int height) {

		weekV.chkAuto = new CheckBox(baseCtx);
		weekV.chkAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.weekPwrOn = isChecked ? 1 : 0;
			}
		});

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				weekV.chkAuto.setChecked(cfg.para.weekPwrOn == 1 ? true : false);
			} else {
				weekV.edtWeekb[i - 1] = new EditText(baseCtx);
				weekV.edtWeeke[i - 1] = new EditText(baseCtx);

				weekV.edtWeekb[i - 1].setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				weekV.edtWeekb[i - 1].setVisibility(View.VISIBLE);
				weekV.edtWeekb[i - 1].setTextColor(Color.BLACK);
				weekV.edtWeekb[i - 1].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				weekV.edtWeekb[i - 1].setInputType(InputType.TYPE_CLASS_DATETIME);
				weekV.edtWeekb[i - 1].setBackgroundResource(R.drawable.shape);

				weekV.edtWeeke[i - 1].setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				weekV.edtWeeke[i - 1].setVisibility(View.VISIBLE);
				weekV.edtWeeke[i - 1].setTextColor(Color.BLACK);
				weekV.edtWeeke[i - 1].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				weekV.edtWeeke[i - 1].setInputType(InputType.TYPE_CLASS_DATETIME);
				weekV.edtWeeke[i - 1].setBackgroundResource(R.drawable.shape);
				String str = cfg.para.weekOnoff[i - 1];
				if (str != null) {
					String[] wk = str.split("-");
					if (wk != null && wk.length == 2) {
						weekV.edtWeekb[i - 1].setText(wk[0]);
						weekV.edtWeeke[i - 1].setText(wk[1]);
					}
				}
			}
			if (i == 0) {
				RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 30, height / 10);
				txParams.leftMargin = 0;
				txParams.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(weekV.chkAuto, txParams);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				RelativeLayout.LayoutParams txParamsb = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamsb.leftMargin = 0;
				txParamsb.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(weekV.edtWeekb[i - 1], txParamsb);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				RelativeLayout.LayoutParams txParamse = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamse.leftMargin = 90;
				txParamse.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(weekV.edtWeeke[i - 1], txParamse);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				TextView tv = new TextView(baseCtx);
				tv.setText(" - ");
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
				RelativeLayout.LayoutParams txParamsm = new RelativeLayout.LayoutParams(width / 6, height / 10);
				txParamsm.leftMargin = 78;
				txParamsm.topMargin = height / 10 * i;
				if (lay != null) {
					try {
						lay.addView(tv, txParamsm);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return 0;
	}

	private static int genTimeView(Context baseCtx, RelativeLayout lay, int width, int height) {

		timeV.chkDisplay = new CheckBox(baseCtx);
		timeV.edtDate = new EditText(baseCtx);
		timeV.edtTime = new EditText(baseCtx);
		timeV.chkOnOff = new CheckBox(baseCtx);
		timeV.chkSyncMode = new CheckBox(baseCtx);
		timeV.edtVolTime = new EditText(baseCtx);
		timeV.txtVol = new TextView(baseCtx);
		timeV.txtFwver = new TextView(baseCtx);

		timeV.chkDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.dispOn = isChecked ? 1 : 0;
			}
		});
		timeV.chkOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.syncOn = isChecked ? 1 : 0;
			}
		});
		timeV.chkSyncMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cfg.para.onOff = isChecked ? 1 : 0;
			}
		});

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			if (i == 0) {
				timeV.chkDisplay.setChecked(cfg.para.dispOn == 1 ? true : false);
			} else if (i == 1) {
				timeV.edtDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				timeV.edtDate.setVisibility(View.VISIBLE);
				timeV.edtDate.setTextColor(Color.BLACK);
				timeV.edtDate.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				timeV.edtDate.setText(baseFun.getTimeStr(2));
				timeV.edtDate.setInputType(InputType.TYPE_CLASS_DATETIME);
				timeV.edtDate.setBackgroundResource(R.drawable.shape);
			} else if (i == 2) {
				timeV.edtTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				timeV.edtTime.setVisibility(View.VISIBLE);
				timeV.edtTime.setTextColor(Color.BLACK);
				timeV.edtTime.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				timeV.edtTime.setText(baseFun.getTimeStr(1));
				timeV.edtTime.setInputType(InputType.TYPE_CLASS_DATETIME);
				timeV.edtTime.setBackgroundResource(R.drawable.shape);
			} else if (i == 3) {
				timeV.chkOnOff.setChecked(cfg.para.onOff == 1 ? true : false);
			} else if (i == 4) {
				timeV.chkSyncMode.setChecked(cfg.para.syncOn == 1 ? true : false);
			} else if (i == 5) {
				timeV.edtVolTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				timeV.edtVolTime.setVisibility(View.VISIBLE);
				timeV.edtVolTime.setTextColor(Color.BLACK);
				timeV.edtVolTime.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				timeV.edtVolTime.setText(cfg.para.voltime);
				timeV.edtVolTime.setInputType(InputType.TYPE_CLASS_DATETIME);
				timeV.edtVolTime.setBackgroundResource(R.drawable.shape);
			} else if (i == 6) {
				timeV.txtVol.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				timeV.txtVol.setVisibility(View.VISIBLE);
				timeV.txtVol.setTextColor(Color.BLACK);
				timeV.txtVol.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				timeV.txtVol.setText("" + cfg.para.volume);
			} else if (i == 7) {
				timeV.txtFwver.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
				timeV.txtFwver.setVisibility(View.VISIBLE);
				timeV.txtFwver.setTextColor(Color.BLACK);
				timeV.txtFwver.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				timeV.txtFwver.setText(cfg.para.sysversion);
			}
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 30, height / 10);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					if (i == 0)
						lay.addView(timeV.chkDisplay, txParams);
					else if (i == 1)
						lay.addView(timeV.edtDate, txParams);
					else if (i == 2)
						lay.addView(timeV.edtTime, txParams);
					else if (i == 3)
						lay.addView(timeV.chkOnOff, txParams);
					else if (i == 4)
						lay.addView(timeV.chkSyncMode, txParams);
					else if (i == 5)
						lay.addView(timeV.edtVolTime, txParams);
					else if (i == 6)
						lay.addView(timeV.txtVol, txParams);
					else if (i == 7)
						lay.addView(timeV.txtFwver, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	private static int genTitleView(Context baseCtx, RelativeLayout lay, int width, int height, int laymode) {

		lay.removeAllViews();
		for (int i = 0; i < 8; i++) {
			TextView view = new TextView(baseCtx);
			if (laymode == 0) {
				if (i == 0)
					view.setText("DISPLAY");
				else if (i == 1)
					view.setText("DATE");
				else if (i == 2)
					view.setText("TIME");
				else if (i == 3)
					view.setText("ON/OFF");
				else if (i == 4)
					view.setText("SYNC MODE");
				else if (i == 5)
					view.setText("AUTO VOL TIME");
				else if (i == 6)
					view.setText("AUTO VOLUME");
				else if (i == 7)
					view.setText("FIRMWARE VER");
				else
					return 0;
			} else if (laymode == 1) {
				if (i == 0)
					view.setText("AUTO ON/OFF");
				else if (i == 1)
					view.setText("SUNDAY");
				else if (i == 2)
					view.setText("MONDAY");
				else if (i == 3)
					view.setText("TUSDAY");
				else if (i == 4)
					view.setText("WENDESDAY");
				else if (i == 5)
					view.setText("THURSDAY");
				else if (i == 6)
					view.setText("FRIDAY");
				else if (i == 7)
					view.setText("SATURDAY");
				else
					return 0;
			} else if (laymode == 2) {
				if (i == 0)
					view.setText("AUTO ON/OFF");
				else if (i == 1)
					view.setText("ON/OFF 1");
				else if (i == 2)
					view.setText("ON/OFF 2");
				else if (i == 3)
					view.setText("ON/OFF 3");
				else if (i == 4)
					view.setText("ON/OFF 4");
				else if (i == 5)
					view.setText("ON/OFF 5");
				else if (i == 6)
					view.setText("ON/OFF 6");
				else if (i == 7)
					view.setText("ON/OFF 7");
				else
					return 0;
			} else if (laymode == 3) {
				if (i == 0)
					view.setText("SERVER IP");
				else if (i == 1)
					view.setText("SERVER PORT");
				else if (i == 2)
					view.setText("DHCP");
				else if (i == 3)
					view.setText("LOCAL IP");
				else if (i == 4)
					view.setText("NETMASK");
				else if (i == 5)
					view.setText("GATEWAY");
				// else if(i==6) view.setText("PING");
				else
					return 0;
			} else if (laymode == 4) {
				if (i == 0)
					view.setText("FTP1 IP");
				else if (i == 1)
					view.setText("FTP1 PORT");
				else if (i == 2)
					view.setText("FTP1 USER");
				else if (i == 3)
					view.setText("FTP1 PASSWORD");
				else if (i == 4)
					view.setText("FTP2 IP");
				else if (i == 5)
					view.setText("FTP2 PORT");
				else if (i == 6)
					view.setText("FTP2 USER");
				else if (i == 7)
					view.setText("FTP2 PASSWORD");
				else
					return 0;
			} else if (laymode == 5) {
				if (i == 0)
					view.setText("COPY DISK");
				else if (i == 1)
					view.setText("CLEAR DISK");
				else if (i == 2)
					view.setText("COPY PLAYLOG");
				else if (i == 3)
					view.setText("DISK SIZE");
				else if (i == 4)
					view.setText("DISK USAGE");
				else if (i == 5)
					view.setText("FILE VALIDATION");
				else if (i == 6)
					view.setText("AUTO COPY");
				else if (i == 7)
					view.setText("SAVE PLAYLOG");
				else
					return 0;
			} else if (laymode == 6) {
				if (i == 0)
					view.setText("VOLUME");
				else if (i == 1)
					view.setText("NEW PASSWD");
				else if (i == 2)
					view.setText("CONFIRM PASSWD");
				else if (i == 3)
					view.setText("RESOLUTION");
				else if (i == 4)
					view.setText("ROTATION");
				else if (i == 5)
					view.setText("UPDATE FIRMWARE");
				else
					return 0;
			} else {
				return 0;
			}
			view.setVisibility(View.VISIBLE);
			view.setTextColor(Color.BLACK);
			view.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
			RelativeLayout.LayoutParams txParams = new RelativeLayout.LayoutParams(width / 2 - 40, height / 10);
			txParams.leftMargin = 0;
			txParams.topMargin = height / 10 * i;
			if (lay != null) {
				try {
					lay.addView(view, txParams);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	private static void setContainerVisab(Context ctx, int width, int height, int idx) {

		if (idx >= 0 && idx < 7) {

			if (btnView != null) {
				btnView.btnTime.setTextColor(idx == 0 ? Color.BLACK : Color.GRAY);
				btnView.btnOnOffWeek.setTextColor(idx == 1 ? Color.BLACK : Color.GRAY);
				btnView.btnOnOffDay.setTextColor(idx == 2 ? Color.BLACK : Color.GRAY);
				btnView.btnNet.setTextColor(idx == 3 ? Color.BLACK : Color.GRAY);
				btnView.btnFtp.setTextColor(idx == 4 ? Color.BLACK : Color.GRAY);
				btnView.btnFile.setTextColor(idx == 5 ? Color.BLACK : Color.GRAY);
				btnView.btnSet.setTextColor(idx == 6 ? Color.BLACK : Color.GRAY);

				btnView.btnTime.setBackgroundResource(idx == 0 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnOnOffWeek.setBackgroundResource(idx == 1 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnOnOffDay.setBackgroundResource(idx == 2 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnNet.setBackgroundResource(idx == 3 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnFtp.setBackgroundResource(idx == 4 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnFile.setBackgroundResource(idx == 5 ? R.drawable.shapex : R.drawable.shapey);
				btnView.btnSet.setBackgroundResource(idx == 6 ? R.drawable.shapex : R.drawable.shapey);
			}
			if (layTime != null)
				layTime.setVisibility(idx == 0 ? View.VISIBLE : View.GONE);
			if (layWeek != null)
				layWeek.setVisibility(idx == 1 ? View.VISIBLE : View.GONE);
			if (layDay != null)
				layDay.setVisibility(idx == 2 ? View.VISIBLE : View.GONE);
			if (layNet != null)
				layNet.setVisibility(idx == 3 ? View.VISIBLE : View.GONE);
			if (layFtp != null)
				layFtp.setVisibility(idx == 4 ? View.VISIBLE : View.GONE);
			if (layFile != null)
				layFile.setVisibility(idx == 5 ? View.VISIBLE : View.GONE);
			if (laySet != null)
				laySet.setVisibility(idx == 6 ? View.VISIBLE : View.GONE);
		}
	}

	private static View genCfgView(final Context baseCtx, final int width, final int height) {

		View convView = new RelativeLayout(baseCtx);
		LayoutInflater listContainer = LayoutInflater.from(baseCtx);
		if (listContainer == null) {
			return null;
		}
		convView = listContainer.inflate(R.layout.cfg_ntms, null);

		layTime = (RelativeLayout) convView.findViewById(R.id.layTime);
		layWeek = (RelativeLayout) convView.findViewById(R.id.layOnOffWeek);
		layDay = (RelativeLayout) convView.findViewById(R.id.layOnOffDay);
		layNet = (RelativeLayout) convView.findViewById(R.id.layNet);
		layFtp = (RelativeLayout) convView.findViewById(R.id.layFtp);
		layFile = (RelativeLayout) convView.findViewById(R.id.layFile);
		laySet = (RelativeLayout) convView.findViewById(R.id.laySet);
		layTitle = (RelativeLayout) convView.findViewById(R.id.layTitle);

		btnView.btnSetting = (Button) convView.findViewById(R.id.btnSetting);
		btnView.btnExit = (Button) convView.findViewById(R.id.btnExit);
		btnView.btnClose = (Button) convView.findViewById(R.id.btnColse);
		btnView.btnTime = (Button) convView.findViewById(R.id.btnTime);
		btnView.btnOnOffWeek = (Button) convView.findViewById(R.id.btnOnOffWeek);
		btnView.btnOnOffDay = (Button) convView.findViewById(R.id.btnOnOffDay);
		btnView.btnNet = (Button) convView.findViewById(R.id.btnNet);
		btnView.btnFtp = (Button) convView.findViewById(R.id.btnFtp);
		btnView.btnFile = (Button) convView.findViewById(R.id.btnFile);
		btnView.btnSet = (Button) convView.findViewById(R.id.btnSet);

		convView.setTag(btnView);

		btnView.btnSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message message = Message.obtain(MainActivity.mHandler, MainActivity.START_SETTING);
				MainActivity.mHandler.sendMessage(message);
			}
		});
		btnView.btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getAllVal();
				cfg.saveConfig(cfg.cfgXml);
				MainActivity.tFtp.reconnect();
				Message message = Message.obtain(MainActivity.mHandler, MainActivity.CFG_EXIT);
				MainActivity.mHandler.sendMessage(message);
			}
		});
		btnView.btnExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Message message = Message.obtain(MainActivity.mHandler, MainActivity.EXIT_PLAY);
				MainActivity.mHandler.sendMessage(message);
			}
		});
		btnView.btnTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 0);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 0);
			}
		});
		btnView.btnOnOffWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 1);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 1);
			}
		});
		btnView.btnOnOffDay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 2);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 2);
			}
		});
		btnView.btnNet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 3);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 3);
			}
		});
		btnView.btnFtp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 4);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 4);
			}
		});
		btnView.btnFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 5);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 5);
			}
		});
		btnView.btnSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContainerVisab(baseCtx, width, height, 6);
				if (layTitle != null)
					genTitleView(baseCtx, layTitle, width, height, 6);
			}
		});

		try {
			if (layTitle != null)
				genTitleView(baseCtx, layTitle, width, height, 0);
			if (layTime != null)
				genTimeView(baseCtx, layTime, width, height);
			if (layWeek != null)
				genWeekView(baseCtx, layWeek, width, height);
			if (layDay != null)
				genDayView(baseCtx, layDay, width, height);
			if (layNet != null)
				genNetView(baseCtx, layNet, width, height);
			if (layFtp != null)
				genFtpView(baseCtx, layFtp, width, height);
			if (layFile != null)
				genFileView(baseCtx, layFile, width, height);
			if (laySet != null)
				genSetView(baseCtx, laySet, width, height);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convView;
	}

	public static View showSetView(final Context basCtx, RelativeLayout lay, final int width, final int height,
			int left, int top) {

		if (basCtx == null || lay == null) {
			Log.d("set", "Exception new setView,null + null!");
			return null;
		}
		View subView = null;
		RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(width, height);
		try {
			lay.removeAllViews();
			subView = genCfgView(basCtx, width, height);

			btParams.leftMargin = left;
			btParams.topMargin = top;
			try {
				lay.addView(subView, btParams);
				subView.setVisibility(View.VISIBLE);
				lay.bringChildToFront(subView);// 将View显示在屏幕最前方
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.d("set", "Exception new setView!");
		}
		return subView;
	}
}
