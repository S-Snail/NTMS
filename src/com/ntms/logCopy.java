package com.ntms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

@SuppressLint("SdCardPath")
public class logCopy extends Thread {

	private static logCopy cpyThread = null;
	private static String mPath = "/mnt/sdcard/";
	private static String mDst = "/mnt/usbhost4/";
	public static int copyNum = 0;
	public static int copySum = 0;
	public static int copyEnd = 0;
	public static int logThread = 0;
	public static int copyStart = 0;
	public static int copyProg = 0;
	public static String copyTitle = null;
	public static String copyName = null;
	public static String extDisk = null;

	public static logCopy newInstance(String pathStr) {
		copyEnd = 0;
		cpyThread = new logCopy();
		return cpyThread;
	}

	public logCopy() {

	}

	public static int getCopyFileNum(String srcPath) {
		try {
			File[] files = new File(srcPath).listFiles();
			if (files != null) {
				return files.length;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int copyFile(String oldPath, String newPath) {
		int rt = 0;
		long fileLen = 0;
		long shw = 0;

		try {
			long bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			File newFile = null;

			if (oldfile.exists()) {
				if (MainActivity.mHandler != null) {
					int idx = oldPath.lastIndexOf("/");
					String fileName = oldPath.substring(idx + 1);
					if (fileName.length() > 32) {
						fileName = fileName.substring(0, 20) + "..." + fileName.substring(fileName.length() - 5);
					}
					copyNum = copyNum + 1;
					copyTitle = copyNum + "/" + copySum;
					copyName = fileName;

				}
				fileLen = oldfile.length();

				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);

				if (fileLen != 0) {
					byte[] buffer = new byte[1444];
					while ((byteread = inStream.read(buffer)) != -1) {
						bytesum += byteread;
						shw += byteread;
						if (shw > 1024 * 16 && MainActivity.mHandler != null) {
							shw = 0;
							if (fileLen != 0) {
								copyProg = (int) (bytesum * 100 / fileLen);
							}
						}
						fs.write(buffer, 0, byteread);
					}
				} else {
					Log.i("copy", "====file " + oldPath + " length is 0====");
				}
				if (inStream != null) {
					inStream.close();
				}
				if (fs != null) {
					fs.close();
					newFile = new File(newPath);
					if (newFile != null) {
						if (copyNum >= copySum) {
							copyEnd++;
							copyName = "";
							copyProg = 100;
							copyTitle = copyNum + "/" + copySum;
							Log.i("copy", "================TASK COPY END====================");
						}
					}
				}
				rt = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rt;
	}

	public static int copyTaskFile(String srcPath, String dstPath, int mode) {

		int rt = 0;

		if (srcPath == null || dstPath == null) {
			return 0;
		}
		String srcFile = null;
		String dstFile = null;
		try {
			File[] files = new File(srcPath).listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					if (f.isDirectory() == false && f.getPath().indexOf("/.") == -1) {
						srcFile = srcPath + f.getName();
						dstFile = dstPath + f.getName();

						Log.i("copy", "===Now copy file: " + f.getName() + "===");
						copyFile(srcFile, dstFile);
						rt++;
					} else {
						if (f.getPath().indexOf("/.") >= 0 && f.getName().length() > 2) {
							copyNum = copyNum + 1;
						}
					}
				}
				return rt;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean checkFile(String strFile) {

		File file = new File(strFile);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressLint("SdCardPath")
	@Override
	public void run() {
		try {
			if (copyStart == 1) {

				if (baseFun.extPath != null)
					mDst = baseFun.extPath;
				else
					mDst = "/mnt/extsd/";

				if (baseFun.getDiskSize(mDst) < 500) {
					copyStart = 0;
					Message message = Message.obtain(MainActivity.mHandler, MainActivity.SHOW_TIP);
					Bundle b = new Bundle();
					b.putString("msg", "please insert ext disk");
					message.setData(b);
					MainActivity.mHandler.sendMessage(message);
					return;
				}
				String mac = baseFun.getMac();

				mDst = mDst + mac + "/";
				copyNum = copySum = copyEnd = 0;
				logThread = 1;

				copySum += getCopyFileNum(mPath + "oplog/");
				copySum += getCopyFileNum(mPath + "pllog/");

				if (copySum > 0) {
					baseFun.checkDir(mDst + "op/");
					baseFun.checkDir(mDst + "play/");

					copyTaskFile(mPath + "oplog/", mDst + "op/", 1);
					copyTaskFile(mPath + "pllog/", mDst + "play/", 1);
				}
				copyStart = logThread = 0;

				baseFun.sync();
				Message message = Message.obtain(MainActivity.mHandler, MainActivity.SHOW_TIP);
				Bundle b = new Bundle();
				b.putString("msg", "copy log end");
				message.setData(b);
				MainActivity.mHandler.sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}