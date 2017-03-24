package com.ntms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

@SuppressLint("SdCardPath")
public class daemon extends Thread {// daemon守护进程

	private static daemon wThread = null;
	public static Context mContext = null;
	public static int hwWTD = 0, wtdThdRun = 0;

	public static daemon newInstance(Context context, Handler mainHandler) {
		wThread = new daemon(context, mainHandler);
		return wThread;
	}

	public daemon(Context context, Handler mainHandler) {
		mContext = context;
	}

	public static void closeWTD(Context mContext) {
		if (mContext != null) {

		}
	}

	public void feedWTD(int val) {
		if (mContext != null) {

		}
	}

	// --------------------------------------
	protected static SocketChannel client = null;// 多路复用器
	private static Selector selector = null;
	private static boolean connsta = false;

	private static void close() {
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
	}

	private static synchronized void connectToServer() {

		try {
			connsta = false;
			selector = Selector.open();// 创建选择器
			if (selector == null) {
				return;
			}
			InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 3333);
			client = SocketChannel.open();
			if (client == null) {
				return;
			}
			client.socket().connect(socketAddress, 10000);
			client.socket().setKeepAlive(true);
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ, new ByteArrayOutputStream());

			if (client.isConnected()) {
				Log.i("daemon", "======Connected======");
				connsta = true;
			} else {
				Log.i("daemon", "======Connecte fail======");
				client = null;
				close();
			}
		} catch (Exception e) {
			close();
		}
	}

	private static boolean writeMsg(String cmd) {

		if (cmd != null && cmd.length() > 4 && client != null && client.isConnected()) {
			Log.i("daemon", "snd Str:" + cmd);
			ByteBuffer buffer = ByteBuffer.wrap(cmd.getBytes());
			int size = buffer.remaining();
			try {
				int actually = client.write(buffer);
				if (actually == size) {
					return true;
				}
			} catch (Exception e) {
				close();
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void reportSta(String str, int mode) {

		if (str != null) {
			if (client == null || connsta == false) {
				connectToServer();
			}
			if (client != null && connsta == true) {
				if (str.contains("<Key")) {
					writeMsg(str);
					return;
				}
				if (mode == 0) {
					writeMsg("<Sta>" + str + "</Sta>");
				}
			}
		}
	}

	// --------------------------------------

	@Override
	public void run() {

		hwWTD = wtdThdRun = 0;
		try {
			int uiErr = 0, pwdErr = 0;
			try {
				baseFun.chkLogSum("/mnt/sdcard/oplog/");
				baseFun.chkLogSum("/mnt/sdcard/pllog/");

				String dstFile = mContext.getFilesDir().getAbsolutePath() + "/upt";
				if (baseFun.checkFile(dstFile) == false) {
					baseFun.copyBinary(R.raw.upt, dstFile, mContext);
				}

				dstFile = mContext.getFilesDir().getAbsolutePath() + "/daemon";
				if (baseFun.checkFile(dstFile) == false) {
					baseFun.copyBinary(R.raw.daemon, dstFile, mContext);
					try {
						Runtime.getRuntime().exec("chmod 777 " + dstFile);
					} catch (Exception e) {
					}
				}
				baseFun.execShell(dstFile + " &");

			} catch (Exception e) {
				e.printStackTrace();
			}

			// ======================================================================================================
			while (baseFun.exitPlay != 1) {

				baseFun.uiStatus = 1;
				reportSta("1", 0);

				if (baseFun.pausePlay != 1) {
					MainActivity.sendMessage(MainActivity.DETECT_STA);
				}
				power.pwrThdAct = 1;

				if (hwWTD == 1) {
					wtdThdRun = 1;
					for (int i = 0; i < 3; i++) {
						if (baseFun.exitPlay != 1) {
							feedWTD(2);
							SystemClock.sleep(5 * 1000);
						} else {
							break;
						}
					}
				} else {
					hwWTD = 1;
					Log.i("daemon", "Open wtd fail");

					SystemClock.sleep(5 * 1000);
				}

				if (baseFun.uiStatus == 1) {
					if (baseFun.pausePlay != 1 && uiErr++ > 16) {
						hwWTD = 0;
						baseFun.appendLogInfo("player ui maybe died,reboot", 4);
						baseFun.appendLogInfo(null, -1);
						baseFun.rebootnow(mContext);
						baseFun.runCmd("reboot");
					}
				} else {
					uiErr = 0;
				}

				pwdErr = power.pwrThdAct == 1 ? (pwdErr + 1) : 0;
				if (pwdErr > 16) {
					pwdErr = 0;
					baseFun.appendLogInfo("power thread maybe exception,stop feed wtd", 4);
					baseFun.appendLogInfo(null, -1);
					hwWTD = 0;
				}
				Log.i("daemon", "=================Power Thread Status: " + pwdErr + "==================");
			}
			if (baseFun.exitPlay == 1) {
				baseFun.appendLogInfo("close wtd by exit or update", 4);
				baseFun.appendLogInfo(null, -1);
				baseFun.savdurCnt = 0;
				baseFun.savePlayDur(baseFun.durPath);
				reportSta("0", 0);
			}
			// ======================================================================================================
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
