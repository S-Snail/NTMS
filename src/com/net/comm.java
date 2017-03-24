package com.net;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.cfg.cfg;
import com.list.sch;
import com.ntms.MainActivity;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
public class comm extends Thread {

	public static final int NOT_CONNECTED = 0;
	public static final int CONNECTED = 1;
	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 25000;
	private static final int RECONNECT_TIME = 5000;

	private static int tryTimes = 0;
	private static int rcvTout = 0;
	private static int beat = 1;
	public static int constatus = NOT_CONNECTED;
	private ArrayList<String> connInfo = new ArrayList<String>();
	private Selector selector = null;
	protected SocketChannel client = null;
	private boolean reconnect = false;
	public static String netMac = null;
	public static String weatherStr = null;
	public static String rssStr = null;
	public static String dateStr = null;

	// ----------------------------------------------------------------------------------------------------------

	private void run_cmd_20(String cmd, int type) {
		// 服务端 对23号下载设备日志文档进行更改 注: 下载设备日志文档(改) {msg:"<cmd id=20>"} {ret:"<cmd
		// id=20 message='日志内容'>"} new

		if (cmd.contains("sq=s")) {

			// int port=21;
			// if(cfg.para.mftpport!=null){
			// try{
			// port=Integer.parseInt(cfg.para.mftpport);
			// }catch(Exception x){
			//
			// }
			// }
			// Calendar cal = Calendar.getInstance();
			// cal.setTimeInMillis(System.currentTimeMillis()-24*3600*1000);
			// SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
			// String filename=fmt.format(cal.getTime());
			// String srcFile="/mnt/sdcard/pllog/"+filename+".txt";
			//
			// if(baseFun.checkFile(srcFile)){
			// new
			// upld(srcFile,cfg.para.mftp,cfg.para.mftpuser,cfg.para.mftppwd,port).start();
			// writeBuf("<cmd id=20 file="+filename+" >");
			// }else{
			// writeBuf("<cmd id=20 file= >");
			// }

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis() - 24 * 3600 * 1000);
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
			String filename = fmt.format(cal.getTime());
			String srcFile = "/mnt/sdcard/pllog/" + filename + ".txt";

			if (baseFun.checkFile(srcFile)) {
				try {
					String str = baseFun.readTxtFile(srcFile);
					/*
					 * Base64.encode(str.getBytes(),
					 * Base64.DEFAULT加密传入的数据，如果传入的数据是String类型的
					 * 使用str.getByts()即可，这里使用的是encode方式，返回的是byte类型的加密数据； 可使用new
					 * String 转为String类型 注：解密是decode：new
					 * String（Base.decode(str.getBytes(),Base64.Default));
					 * base64的作用不是加密，而是用来避免“字节”中不能转换成可显示字符的数值。
					 * 比如0-32的控制字符，空格，制表符都不能被打印在纸上，base64只使用大写小写数字标点。
					 * 可以打印在纸上，数据可以在传统平面媒介上携带。
					 */
					String rts = new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
					writeBuf("<cmd id=20 file=" + rts + " >");
				} catch (Exception x) {
					x.printStackTrace();
				}
			} else {
				writeBuf("<cmd id=20 file= >");
			}
		}
	}

	private void run_cmd_24(String cmd, int type) {// 截图功能

		if (cmd.contains("sq=s")) {
			int port = 21;
			if (cfg.para.mftpport != null) {
				try {
					port = Integer.parseInt(cfg.para.mftpport);
				} catch (Exception x) {
					x.printStackTrace();
				}
			}
			String Str = netMac + "_" + baseFun.getTimeStr(0);
			Str = Str.replace("-", "");
			Str = Str.replace(":", "");
			Str = Str.replace(" ", "");
			new upld("/mnt/sdcard/" + Str + ".png", cfg.para.mftp, cfg.para.mftpuser, cfg.para.mftppwd, port).start();

			writeBuf("<cmd id=24 file=" + Str + ".png sq=s>");
		}
	}

	private void run_cmd_100(String cmd, int type) {
		// writeBuf("< id=100 ret=ok mac="+netMac+" >");

		String _weatherStr = parseString(cmd, "message");
		if (_weatherStr != null && _weatherStr.length() > 1) {
			weatherStr = _weatherStr;
		}
	}

	private void run_cmd_101(String cmd, int type) {
		// writeBuf("< id=101 ret=ok mac="+netMac+" >");

		String _rssStr = parseString(cmd, "message");
		if (_rssStr != null && _rssStr.length() > 1) {
			rssStr = _rssStr;
		}
	}

	private void run_cmd_102(String cmd, int type) {
		// writeBuf("< id=102 ret=ok mac="+netMac+" >");

		String dstr = parseString(cmd, "message");
		if (dstr != null) {
			dstr = dstr.replace(":", "");
			dstr = dstr.replace("-", "");
			dstr = dstr.replace(" ", ".");
			if (dstr.length() == 15) {// 2016-01-05 16:32:24
				baseFun.runCmd("date -s " + dstr);
			}
		}
	}

	private void run_cmd_18(String cmd, int type) {// ret:"<cmd id=18 ret=ok
													// text=
													// task=1448437194954.xml
													// disk=7572 img= download=0
													// dlfile=
													// appver=4.3.1-140710
													// ip=192.168.1.111
													// preDownloadTask=predownload.xml
													// rootfs=4.2.0-130101 aud=
													// usage=1 video=
													// percents=0> "

		String str = "";
		if (sch.schFile != null && sch.schFile.lastIndexOf("/") > 0) {
			str += " task=" + sch.schFile.substring(sch.schFile.lastIndexOf("/") + 1);
		} else {
			str += " task=";
		}
		str += " disk=" + baseFun.getDiskSpace();
		str += " ip=" + baseFun.getIpAddress();
		str += " appver=" + cfg.para.appversion;
		str += " rootfs=" + cfg.para.sysversion;

		if (MainActivity.tFtp != null) {
			str += " percents=" + MainActivity.tFtp.getProgress();
			str += " dlfile=" + MainActivity.tFtp.getFileStatus();
			str += " preDownloadTask=" + MainActivity.tFtp.getDownXml();
		} else {
			str += " percents=";
			str += " dlfile=";
			str += " preDownloadTask=";
		}
		str += " download=0";
		str += " usage=1";

		if (baseFun.curTxt != null)
			str += " text=" + baseFun.curTxt;
		else
			str += " text=";
		if (baseFun.curAud != null)
			str += " aud=" + baseFun.curAud;
		else
			str += " aud=";
		if (baseFun.curImg != null)
			str += " img=" + baseFun.curImg;
		else
			str += " img=";
		if (baseFun.curVid != null)
			str += " video=" + baseFun.curVid;
		else
			str += " video=";

		writeBuf("< id=18 ret=ok" + str + " >");
	}

	private void run_cmd_19(String cmd, int type) { // {ret:"<cmd id=19
													// status=poweroff>"}
													// {ret:"<cmd id=19
													// task=1448437194954.xml
													// waiting=''
													// downloadFileBuf=''
													// download=0 dlfile=
													// percents=0>"} Object类型
		String task = " ";
		String file = " ";
		String prog = " ";
		if (MainActivity.tFtp != null) {
			file = MainActivity.tFtp.getFileStatus();
			prog = MainActivity.tFtp.getProgress();
		}
		if (sch.schFile != null && sch.schFile.lastIndexOf("/") > 0) {
			task = sch.schFile.substring(sch.schFile.lastIndexOf("/") + 1);
		}
		writeBuf("<cmd id=19 task=" + task + " waiting='' downloadFileBuf='' download=0 dlfile=" + file + " percents="
				+ prog + ">");
	}

	private void run_cmd_49(String cmd, int type) {// {msg:"<cmd id=49 rssitem=
													// serverip=192.168.1.105
													// ftp1port=2121 sq=s
													// serverport=6601
													// ftp1ip=192.168.1.105
													// ftp2passwd=ntms
													// ftp1passwd=ntms
													// ftp2ip=192.168.1.105
													// ftp2user=ntms
													// ftp1user=ntms rssaddr=
													// ftp2port=2121>"}

		if (cmd.contains("sq=q")) {

			String rcmd = "<cmd id=49 ret=ok";
			rcmd += " serverip=" + cfg.para.mip;
			rcmd += " serverport=" + cfg.para.mipport;

			rcmd += " ftp1ip=" + cfg.para.mftp;
			rcmd += " ftp1port=" + cfg.para.mftpport;
			rcmd += " ftp1user=" + cfg.para.mftpuser;
			rcmd += " ftp1passwd=" + cfg.para.mftppwd;

			rcmd += " ftp2ip=" + cfg.para.sftp;
			rcmd += " ftp2port=" + cfg.para.sftpport;
			rcmd += " ftp2user=" + cfg.para.sftpuser;
			rcmd += " ftp2passwd=" + cfg.para.sftppwd;

			rcmd += ">";
			writeBuf(rcmd);
		} else if (cmd.contains("sq=s")) {

			cfg.para.rssitem = parseString(cmd, "rssitem");
			cfg.para.rssaddr = parseString(cmd, "rssaddr");
			cfg.para.mip = parseString(cmd, "serverip");
			cfg.para.mipport = parseString(cmd, "serverport");

			cfg.para.mftp = parseString(cmd, "ftp1ip");
			cfg.para.mftpport = parseString(cmd, "ftp1port");
			cfg.para.mftpuser = parseString(cmd, "ftp1user");
			cfg.para.mftppwd = parseString(cmd, "ftp1passwd");

			cfg.para.sftp = parseString(cmd, "ftp2ip");
			cfg.para.sftpport = parseString(cmd, "ftp2port");
			cfg.para.sftpuser = parseString(cmd, "ftp2user");
			cfg.para.sftppwd = parseString(cmd, "ftp2passwd");

			cfg.para.rssitem = parseString(cmd, "rssitem");
			cfg.para.rssaddr = parseString(cmd, "rssaddr");

			cfg.saveConfig(cfg.cfgXml); // 保存设置.XML
			writeBuf("<cmd id=49 ret=ok>");

			reconnect = true;
		}
	}

	private void run_cmd_51(String cmd, int type) {// {msg:"<cmd id=51
													// on3=2008-01-01T00:45:00
													// on1=2008-01-01T00:00:00
													// on2=2008-01-01T00:30:00
													// sq=s
													// off1=2008-01-01T00:15:00
													// off2=2008-01-01T00:45:00
													// off3=2008-01-01T02:00:00>"}

		if (cmd.contains("sq=s")) {
			String str1 = parseString(cmd, "onoff1");
			if (str1 != null && str1.length() > 22)
				cfg.para.dateOnoff[0] = str1;
			String str2 = parseString(cmd, "onoff2");
			if (str2 != null && str2.length() > 22)
				cfg.para.dateOnoff[1] = str2;
			String str3 = parseString(cmd, "onoff3");
			if (str3 != null && str3.length() > 22)
				cfg.para.dateOnoff[2] = str3;
			String str4 = parseString(cmd, "onoff4");
			if (str4 != null && str4.length() > 22)
				cfg.para.dateOnoff[3] = str4;
			String str5 = parseString(cmd, "onoff5");
			if (str5 != null && str5.length() > 22)
				cfg.para.dateOnoff[4] = str5;
			String str6 = parseString(cmd, "onoff6");
			if (str6 != null && str6.length() > 22)
				cfg.para.dateOnoff[5] = str6;
			cfg.saveConfig(cfg.cfgXml);
			writeBuf("<cmd id=51 ret=ok>");
		} else {
			String str = "<cmd id=51 ret=ok" + " onoff1=" + cfg.para.dateOnoff[0];
			str += " onoff2=" + cfg.para.dateOnoff[1];
			str += " onoff3=" + cfg.para.dateOnoff[2];
			str += " onoff4=" + cfg.para.dateOnoff[3];
			str += " onoff5=" + cfg.para.dateOnoff[4];
			str += " onoff6=" + cfg.para.dateOnoff[5];

			str = str + ">";
			writeBuf(str);
		}
	}

	private void run_cmd_52(String cmd, int type) {// {msg:"<cmd id=52 sq=q>" }
													// {ret:"<cmd id=52 ret=ok
													// bytes=200>"}

		if (cmd.contains("sq=q")) {

			writeBuf("<cmd id=52 ret=ok bytes=" + cfg.para.ftpSpd + ">");

		} else if (cmd.contains("sq=s")) {
			try {
				cfg.para.ftpSpd = Integer.parseInt(parseString(cmd, "bytes"));
				cfg.saveConfig(cfg.cfgXml);
			} catch (Exception e) {
			}
			writeBuf("<cmd id=52 ret=ok>");
		}
	}

	private void run_cmd_54(String cmd, int type) {// {msg:"<cmd id=54 sq=q>" }
													// {ret:"<cmd id=54 ret=ok
													// vol=70>"}

		if (cmd.contains("sq=q")) {

			writeBuf("<cmd id=54 ret=ok vol=" + cfg.para.volume + ">");

		} else if (cmd.contains("sq=s")) {
			try {
				cfg.para.volume = Integer.parseInt(parseString(cmd, "vol"));
				cfg.saveConfig(cfg.cfgXml);

				Message message = Message.obtain(MainActivity.mHandler, MainActivity.SET_VOLUME);
				MainActivity.mHandler.sendMessage(message);

			} catch (Exception e) {
			}
			writeBuf("<cmd id=54 ret=ok>");
		}
	}

	private void run_cmd_70(String cmd, int type) {// <cmd id=71
													// file='1448437021519.xml'>

		String file = parseSString(cmd, "file");

		baseFun.appendLogInfo("recv new task:" + file, 4);
		baseFun.appendLogInfo(null, -1);

		if (file != null && file.contains(".xml") && MainActivity.tFtp != null) {
			MainActivity.tFtp.addDownList(file, file, mediaType.Xml, 1, MainActivity.tFtp.D_CLEAR);
			if (sch.taskLst == null) {
				sch.taskLst = new ArrayList<String>();
			}
			sch.taskLst.clear();
			sch.taskLst.add(file);
			sch.saveTaskXml(sch.tskFile, sch.taskLst);
			writeBuf("<cmd id=" + type + " ret=ok>");
		} else {
			printfInfo("cmd 70 or 71 maybe err!");
			writeBuf("<cmd id=" + type + " ret=err>");
		}
	}

	private void run_cmd_71(String cmd, int type) {
		run_cmd_70(cmd, type);
	}

	private void run_cmd_76(String cmd, int type) {// insert text {msg:"<cmd
													// id=76 text='字幕内容'>"}
													// {ret:"<cmd id=76
													// text='字幕内容'>"}

		String insertStr = parseSString(cmd, "text");

		// insertStr= baseFun.getUtf8Code(insertStr);//20160301

		Bundle b = new Bundle();
		b.putString("str", insertStr);
		Message message = Message.obtain(MainActivity.mHandler, MainActivity.NEW_TEXT);
		message.setData(b);
		MainActivity.mHandler.sendMessage(message);
		printfInfo("cmd is rss msd....");
		writeBuf(cmd);
	}

	private void run_cmd_66(String cmd, int type) { // {msg:"<cmd id=66>"}
													// {ret:<cmd id=66 ret=ok>}

		baseFun.appendLogInfo("reboot by svr", 4);
		baseFun.appendLogInfo(null, -1);
		writeBuf("<cmd id=66 ret=ok>");
		baseFun.runCmd("reboot");
	}

	private void run_cmd_35(String cmd, int type) {// {msg:"<cmd id=35>"}
													// {ret:"<cmd id=35
													// ret=ok>"}

		List<String> storagePath = baseFun.getStoragePath();
		for (int i = 0; i < storagePath.size(); i++) {
			baseFun.deleteFolder(new File(storagePath.get(i) + "/ntms/"));
		}
		baseFun.deleteFolder(new File("/mnt/sdcard/oplog/"));
		baseFun.deleteFolder(new File("/mnt/sdcard/pllog/"));

		writeBuf("<cmd id=35 ret=ok>");
	}

	private void run_cmd_58(String cmd, int type) {// {msg:"<cmd id=58 minute=0
													// second=0 sq=s month=11
													// year=2015 hour=0
													// day=27>"} {ret:"<cmd
													// id=58 ret=ok>"}

		String yy = parseString(cmd, "year");
		String MM = parseString(cmd, "month");
		if (MM.length() == 1)
			MM = "0" + MM;
		String dd = parseString(cmd, "day");
		if (dd.length() == 1)
			dd = "0" + dd;

		String hh = parseString(cmd, "hour");
		if (hh.length() == 1)
			hh = "0" + hh;
		String mm = parseString(cmd, "minute");
		if (mm.length() == 1)
			mm = "0" + mm;
		String ss = parseString(cmd, "second");
		if (ss.length() == 1)
			ss = "0" + ss;

		baseFun.runCmd("date -s " + yy + MM + dd + "." + hh + mm + ss);

		writeBuf("<cmd id=58 ret=ok>");
	}

	private void run_cmd_72(String cmd, int type) {// msg:"<cmd id=72
													// file='文件名'>"} {ret:"<cmd
													// id=72 ret=ok>"}

		String file = parseString(cmd, "file");
		if (MainActivity.tFtp != null && file != null) {
			MainActivity.tFtp.addDownList(file, file, mediaType.Update, 1, MainActivity.tFtp.D_FIRST);
		}

		writeBuf("<cmd id=72 ret=ok>");
	}

	private void run_cmd_65(String cmd, int type) {

		String val = parseString(cmd, "hbtimer");
		if (val != null) {
			try {
				cfg.para.beatTime = Integer.parseInt(val);
			} catch (Exception e) {
			}
		}
		String mac = parseString(cmd, "mac");
		if (mac != null) {

			// 判断
			mac = mac.toLowerCase();
			cfg.para.svrmac = mac;
		}
		cfg.saveConfig(cfg.cfgXml);

		writeBuf("<cmd id=65 ret=ok>");
	}

	private void run_cmd_103(String cmd, int type) {// {msg:"<cmd id=103 >"}
													// {ret:"<cmd id=103
													// message='文件名=播放时长,文件名=播放时长.......'>"}
													// new

		String str = null;
		if (baseFun.playDur != null) {
			for (int i = 0; i < baseFun.playDur.size(); i++) {
				String dur = baseFun.playDur.get(i);
				if (dur != null && dur.length() > 3) {
					str += dur + " ";
				}
			}
		}
		writeBuf("<cmd id=103 message='" + str + "' >");
	}

	private void run_cmd_65X() {
		writeBuf("<cmd id=65 mac=" + netMac + " hbtimer=30>");
		// writeBuf("<cmd id=65 mac=30:03:00:00:01:B7 hbtimer=30>");
	}

	private void run_cmd_17X() {
		writeBuf("<cmd id=17>");
	}
	// ----------------------------------------------------------------------------------------------------------------------

	class beatThd extends Thread {

		public void run() {
			while (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {
				beat = 1;
				try {
					SystemClock.sleep(15 * 1000);
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void run() {

		try {
			SystemClock.sleep(5 * 1000);
		} catch (Exception e) {
		}

		netMac = baseFun.getMacStr();
		// netMac="30:03:00:00:01:B7";
		beatThd thd = new beatThd();
		thd.start();

		while (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {

			switch (constatus) {
			case NOT_CONNECTED:
				connectToServer();
				if (constatus == CONNECTED) {
					run_cmd_65X();
				}
				break;
			case CONNECTED:
				rcvData();
				break;
			default:
				break;
			}
			try {
				SystemClock.sleep(5 * 1000);
			} catch (Exception e) {
			}
		}
	}

	public void reconnect() {
		infoLog("Reconnect");
		reconnect = true;
	}

	public ArrayList<String> getConnectInfo() {
		return connInfo;
	}

	public String getConnectStatus() {
		return String.valueOf(constatus);
	}

	public void close() {

		try {
			if (client != null) {
				client.socket().close();
				client.close();
				client = null;
			}
			if (selector != null) {
				selector.close();
				selector = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		constatus = NOT_CONNECTED;
		infoLog("Connect Failed!");
		Log.i("Connect Failed!", "comm_617");
	}

	private void Wait(long millis) {

		try {
			wait(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private final byte[] readBuf(SelectionKey selectionKey) throws IOException {

		if (selectionKey.isReadable()) {
			SocketChannel client = (SocketChannel) selectionKey.channel();
			ByteArrayOutputStream bos = (ByteArrayOutputStream) selectionKey.attachment();
			ByteBuffer buffer = ByteBuffer.allocate(10240);
			int actual = 0;
			while ((actual = client.read(buffer)) > 0) {
				buffer.flip();
				int limit = buffer.limit();
				byte b[] = new byte[limit];
				buffer.get(b);
				bos.write(b);
				buffer.clear();
			}
			if (actual < 0) {
				selectionKey.cancel();
				client.socket().close();
				client.close();
				throw new EOFException("Read EOF");
			}
			bos.flush();
			byte[] data = bos.toByteArray();
			bos.reset();
			return data;
		}
		return null;
	}

	private boolean writeBuf(String cmd) {

		printfInfo("snd: " + cmd.toString());
		Log.i("sng", "comm_662");
		if (cmd != null && cmd.length() > 4 && client != null && client.isConnected()) {
			ByteBuffer buffer = ByteBuffer.wrap(cmd.getBytes());// cmd.getBytes(Charset.forName("GBK")));
			int size = buffer.remaining();
			try {
				int actually = client.write(buffer);
				if (actually == size)
					return true;// 写完
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void closeKey(SelectionKey key) {
		if (key != null) {
			key.cancel();
			try {
				key.channel().close();
			} catch (Exception e) {
				Wait(RECONNECT_TIME);
			}
		}
		close();
	}

	private synchronized void connectToServer() {
		String ip = null;
		int port = 0;
		reconnect = false;
		try {
			if (tryTimes == 0) {
				ip = cfg.para.mip;
				if (cfg.para.mipport != null)
					port = Integer.valueOf(cfg.para.mipport);
				tryTimes = 1;
			} else {
				ip = cfg.para.sip;
				if (cfg.para.sipport != null)
					port = Integer.valueOf(cfg.para.sipport);
				tryTimes = 0;
			}
			if (port == 0 || ip == null) {
				return;
			}

			selector = Selector.open();
			if (selector == null) {
				Wait(RECONNECT_TIME);
				return;
			}
			InetSocketAddress socketAddress = new InetSocketAddress(ip, port);

			client = SocketChannel.open();
			if (client == null) {
				Wait(RECONNECT_TIME);
				return;
			}

			client.socket().connect(socketAddress, CONNECT_TIMEOUT);
			client.socket().setKeepAlive(true);
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ, new ByteArrayOutputStream());

			if (client.isConnected()) {
				constatus = CONNECTED;
				infoLog("Connect OK(" + ip + ":" + String.valueOf(port) + ")");
				Log.i("Connect OK", "Ok_comm_727");
				baseFun.appendLogInfo("Connect OK(" + ip + ":" + String.valueOf(port) + ")", 4);
				rcvTout = 0;
			} else {
				infoLog("Connect Error(" + ip + ":" + String.valueOf(port) + ")");
				close();
				Wait(RECONNECT_TIME);
			}
		} catch (Exception e) {
			infoLog("Connect Error(" + ip + ":" + String.valueOf(port) + ")");
			close();
			Wait(RECONNECT_TIME);
		}
	}

	private synchronized void rcvData() {
		SelectionKey key = null;
		try {
			while (selector.select(READ_TIMEOUT) > 0 && !reconnect) {
				rcvTout = 0;
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();
				while (iterator.hasNext()) {
					key = iterator.next();
					iterator.remove();

					byte[] data = readBuf(key);
					if (data != null) {
						printfInfo("rcv: " + new String(data));
						parseCmd(new String(data));// new String(data,
													// Charset.forName("GBK")));
					}

					if (beat == 1) {
						beat = 0;
						run_cmd_17X();
					}
					if (weatherStr == null) {
						weatherStr = "";
						writeBuf("< id=100 ret=ok mac=" + netMac + " >");
					}
					if (rssStr == null) {// send news request
						rssStr = "";
						writeBuf("< id=101 ret=ok mac=" + netMac + " >");
					}
					if (dateStr == null) {// send date request
						dateStr = "";
						writeBuf("< id=102 ret=ok mac=" + netMac + " >");
					}
				}
				if (baseFun.exitPlay == 1 || baseFun.pausePlay == 1) {
					close();
					break;
				}
			}
			if (reconnect) {
				close();
				return;
			}
			rcvTout++;
			infoLog("Recv Timeout(" + String.valueOf(rcvTout) + ")");
			if (rcvTout > 5) {
				close();
			}
		} catch (Exception e) {
			closeKey(key);
		}
	}

	private void infoLog(String log) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log = "[" + sdf.format(new Date()) + "] " + log;

		Log.i("comm", log);

		if (connInfo.size() > 10)
			connInfo.remove(9);
		if (connInfo.size() <= 0)
			connInfo.add(log);
		else
			connInfo.add(1, log);
	}

	private void printfInfo(String str) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.i("comm", "[" + sdf.format(new Date()) + "] " + str);
	}

	private String parseString(String Str, String tag) {

		if (Str == null || tag == null || Str.length() <= tag.length()) {
			return null;
		}
		try {
			int i = Str.indexOf(tag + "=");// 返回String对象Str中第一次出现子字符串tag+"="的位置（搜索String中的subString，默认从0开始）
			if (i < 0)
				return null;
			String temp = Str.substring(i);// 截取Str中从0到i的字符串赋值给temp
			if (temp != null) {
				int idx = temp.indexOf(" ");
				if (idx < 0) {
					idx = temp.indexOf(">");
				}
				if (idx > 0) {
					return temp.substring(tag.length() + 1, idx);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private String parseSString(String Str, String tag) {

		if (Str == null || tag == null || Str.length() <= tag.length()) {
			return null;
		}
		try {
			int i = Str.indexOf(tag + "=");
			if (i < 0)
				return null;
			String temp = Str.substring(i);
			if (temp != null) {
				int idx = temp.indexOf("'");
				int last = temp.lastIndexOf("'");
				if (idx > 0 && last > 0) {
					return temp.substring(idx + 1, last);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private void parseCmd(String cmd) {

		if (cmd == null || cmd.length() < 1) {
			return;
		}
		String id = parseString(cmd, "id");
		int type = -1;
		if (id == null) {
			return;
		}
		try {
			type = Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (type < 0)
			return;

		try {
			switch (type) {

			case 18:
				run_cmd_18(cmd, type);
				break;
			case 19:
				run_cmd_19(cmd, type);
				break;
			case 20:
				run_cmd_20(cmd, type);
				break;
			case 24:
				run_cmd_24(cmd, type);
				break;
			case 49:
				run_cmd_49(cmd, type);
				break;
			case 51:
				run_cmd_51(cmd, type);
				break;
			case 52:
				run_cmd_52(cmd, type);
				break;
			case 54:
				run_cmd_54(cmd, type);
				break;
			case 70:
				run_cmd_70(cmd, type);
				break;
			case 71:
				run_cmd_71(cmd, type);
				break;
			case 76:
				run_cmd_76(cmd, type);
				break;
			case 66:
				run_cmd_66(cmd, type);
				break;
			case 35:
				run_cmd_35(cmd, type);
				break;
			case 58:
				run_cmd_58(cmd, type);
				break;
			case 72:
				run_cmd_72(cmd, type);
				break;
			case 65:
				run_cmd_65(cmd, type);
				break;
			case 100:
				run_cmd_100(cmd, type);
				break;
			case 101:
				run_cmd_101(cmd, type);
				break;
			case 102:
				run_cmd_102(cmd, type);
				break;
			case 103:
				run_cmd_103(cmd, type);
				break;

			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------------
}
