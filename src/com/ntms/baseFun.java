package com.ntms;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.list.sch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

@SuppressLint({ "SimpleDateFormat", "SdCardPath", "DefaultLocale" })
public class baseFun {

	public final class mediaType {
		public final static int Video = 1;
		public final static int Audio = 2;
		public final static int Image = 3;
		public final static int Text = 4;
		public final static int Task = 5;
		public final static int Xml = 6;
		public final static int Update = 7;
	}

	public static ArrayList<String> opLog = null;
	public static ArrayList<String> plLog = null;
	public static ArrayList<String> playDur = null;

	public static List<String> strPath = null;
	public static int exitPlay = 0;
	public static int pausePlay = 0;
	public static int vErrCnt = 0;
	public static int cfgRuning = 0;
	public static int uiStatus = 0;
	public static int updateMute = 0;
	public static int fullTag = 0;
	public static int savdurCnt = 0;

	public static String apkPath = null;
	public static String curTxt = null;
	public static String curAud = null;
	public static String curImg = null;
	public static String curVid = null;
	public static String extPath = null;
	public static String durPath = "/mnt/sdcard/ntms/dur.lst";

	public static void appendPlayDur(String filename, int dur) {

		if (playDur == null) {
			playDur = new ArrayList<String>();
		}

		Log.i("comm", "[v] " + filename + "00000000000-" + dur + "------0000");

		if (filename != null && dur > 0 && dur < 10000) {
			filename += "=";
			for (int i = 0; i < playDur.size(); i++) {
				String str = playDur.get(i);
				if (str != null && str.startsWith(filename)) {
					try {
						int ndur = Integer.parseInt(str.substring(filename.length())) + dur;
						str = filename + ndur;
						playDur.remove(i);
						playDur.add(0, str);
						savdurCnt++;
						if (savdurCnt > 5) {
							savdurCnt = 0;
							savePlayDur(durPath);
						}
						return;
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
			}
			playDur.add(0, filename + "=" + dur);
			savdurCnt++;
			if (savdurCnt > 5) {
				savdurCnt = 0;
				savePlayDur(durPath);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void initPlayDur(String filename) {

		if (filename == null || checkFile(filename) == false) {
			return;
		}
		if (playDur == null) {
			playDur = new ArrayList<String>();
		} else {
			playDur.clear();
		}

		ArrayList<String> flist = new ArrayList<String>();

		if (strPath != null && strPath.size() > 0 && sch.taskLst != null && sch.taskLst.size() > 0) {
			for (int j = sch.taskLst.size() - 1; j >= 0; j--) {
				String xmlFile = sch.taskLst.get(j);
				if (xmlFile == null || !xmlFile.contains(".xml")) {
					continue;
				}
				for (int k = 0; k < strPath.size(); k++) {
					String absPath = strPath.get(k) + baseFun.getSubPath(mediaType.Xml, 1) + xmlFile;
					File file = new File(absPath);
					if (file.exists()) {
						ArrayList<String> lst = baseFun.getFileLst(absPath);
						flist.addAll(lst);
						break;
					}
				}
			}
		}

		FileInputStream fis;
		try {
			fis = new FileInputStream(filename);
			DataInputStream dataIO = new DataInputStream(fis);
			String strLine = null;
			while ((strLine = dataIO.readLine()) != null) {
				strLine = strLine.replace("\n", "");
				if (flist != null) {
					for (int k = 0; k < flist.size(); k++) {
						String str = flist.get(k);
						if (str != null && strLine.contains(str + "=")) {
							playDur.add(strLine);
						}
					}
				} else {
					playDur.add(strLine);
				}
			}
			dataIO.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void savePlayDur(String filename) {

		if (playDur != null && playDur.size() > 1) {
			try {
				FileOutputStream fout = new FileOutputStream(filename);
				String wstr = "";
				for (int i = 0; i < playDur.size(); i++) {
					String durstr = playDur.get(i);
					if (durstr != null && durstr.length() > 3) {
						wstr += durstr + "\n";
					}
					if (i > 1024)
						break;
				}
				byte[] bytes = wstr.getBytes();
				fout.write(bytes);
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void appendLogInfo(String str, int type) {// 添加日志信息

		if (type == 1 || type == -1) {// play log
			if (plLog == null) {
				plLog = new ArrayList<String>();
			}
			if (str != null && type != -1) {
				plLog.add(str);
			}
			if (plLog.size() > 32 || str == null || type == -1) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
				String filename = fmt.format(cal.getTime());
				try {
					checkDir("/mnt/sdcard/pllog/");
					FileWriter writer = new FileWriter("/mnt/sdcard/pllog/" + filename + ".txt", true);
					for (int i = 0; i < plLog.size(); i++) {
						String wstr = plLog.get(i);
						if (wstr != null) {
							writer.write("\r\n" + wstr + "\r\n");// 往filename.txt中写内容
						}
					}
					writer.close();
				} catch (IOException e) {
				}
				plLog.clear();
			}
		}
		if (type == 4 || type == -1) {
			if (opLog == null) {
				opLog = new ArrayList<String>();
			}
			if (str != null && type != -1) {
				opLog.add("[" + getTimeStr(0) + "] " + str);
			}
			if (opLog.size() > 16 || str == null || type == -1) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy_MM_dd");
				String filename = fmt.format(cal.getTime());
				try {
					checkDir("/mnt/sdcard/oplog/");// 检查或创建文件夹
					FileWriter writer = new FileWriter("/mnt/sdcard/oplog/" + filename + ".txt", true);
					for (int i = 0; i < opLog.size(); i++) {
						String wstr = opLog.get(i);
						if (wstr != null) {
							writer.write("\r\n" + wstr + "\r\n\r\n");
						}
					}
					writer.close();
				} catch (IOException e) {
				}
				opLog.clear();
			}
		}
	}

	public static int chkLogSum(String Path) {
		if (Path == null) {
			return 0;
		}
		List<String> lstFile = new ArrayList<String>();
		try {
			File[] files = new File(Path).listFiles();
			if (files != null && files.length > 40) {
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					if (!f.isDirectory() && f.getPath().indexOf("/.") == -1) {
						lstFile.add(f.getPath());
					}
				}
				if (!lstFile.isEmpty()) {
					Collections.sort(lstFile);
					if (lstFile.size() > 30) {
						for (int i = 0; i < lstFile.size() - 30; i++) {
							removeFile(lstFile.get(i));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 0;
	}

	public static String getSubPath(int type, int mode) {
		if (mode == 0) {
			if (type == mediaType.Audio) {
				return "/audio/";
			} else if (type == mediaType.Video) {
				return "/video/";
			} else if (type == mediaType.Image) {
				return "/image/";
			} else if (type == mediaType.Task) {
				return "/task/";
			} else if (type == mediaType.Text) {
				return "/text/";
			} else if (type == mediaType.Update) {
				return "/update/";
			} else if (type == mediaType.Xml) {
				return "/task/";
			}
			return "/";
		} else {
			if (type == mediaType.Audio) {
				return "/ntms/audio/";
			} else if (type == mediaType.Video) {
				return "/ntms/video/";
			} else if (type == mediaType.Image) {
				return "/ntms/image/";
			} else if (type == mediaType.Task) {
				return "/ntms/task/";
			} else if (type == mediaType.Text) {
				return "/ntms/text/";
			} else if (type == mediaType.Update) {
				return "/ntms/update/";
			} else if (type == mediaType.Xml) {
				return "/ntms/task/";
			}
			return "/ntms/";
		}
	}

	public static void showToastInfo(Context context, String showStr) {

		if (context != null) {
			Toast toast = null;
			if (toast == null) {
				toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
				toast.setDuration(Toast.LENGTH_LONG);
			}
			if (toast != null) {
				toast.setText(showStr);
				toast.show();
			}
		}
	}

	public static String getTimeStr(int mode) {
		try {
			Calendar c = Calendar.getInstance();
			c.setTimeZone(TimeZone.getDefault());

			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			int min = c.get(Calendar.MINUTE);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int sec = c.get(Calendar.SECOND);
			if (mode == 0) {
				String str = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day) + " "
						+ (hour < 10 ? "0" + hour : hour) + ":" + (min < 10 ? "0" + min : min) + ":"
						+ (sec < 10 ? "0" + sec : sec);
				return str;
			} else if (mode == 1) {
				String str = (hour < 10 ? "0" + hour : hour) + ":" + (min < 10 ? "0" + min : min) + ":"
						+ (sec < 10 ? "0" + sec : sec) + " ";
				return str;
			} else {
				String str = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
				return str;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static boolean checkDir(String path) {
		File dirFile = null;
		try {
			dirFile = new File(path);
			if (!(dirFile.exists()) && !(dirFile.isDirectory())) {
				boolean creadok = dirFile.mkdirs();// 创建文件夹
				if (creadok) {
					System.out.println(" ok:creat folder");
				} else {
					System.out.println(" err:creat folder");
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void deleteFolder(File file) {

		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFolder(files[i]);
				}
			}
			file.delete();
		}
	}

	public static boolean checkFile(String strFile) {

		File file = new File(strFile);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static long getFileLen(String strFile) {

		if (strFile == null) {
			return 0;
		}
		File file = new File(strFile);
		if (file.exists()) {
			return file.length();
		} else {
			return 0;
		}
	}

	public static boolean removeFile(String strFile) {

		File file = new File(strFile);
		if (file.exists()) {
			file.delete();
			return true;
		} else {
			return false;
		}
	}

	public static int copyFile(String oldPath, String newPath) {

		int rt = 0;
		long fileLen = 0;

		if (oldPath == null || newPath == null) {
			return 0;
		}
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);

			if (oldfile.exists()) {
				fileLen = oldfile.length();
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);

				if (fileLen != 0) {
					byte[] buffer = new byte[1444];
					while ((byteread = inStream.read(buffer)) != -1) {
						fs.write(buffer, 0, byteread);
					}
				}
				if (inStream != null) {
					inStream.close();
				}
				if (fs != null) {
					fs.close();
					fs.getFD().sync();
				}
				rt = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rt;
	}

	public static List<String> getStoragePath() {
		if (strPath == null) {
			List<String> paths = new ArrayList<String>();
			String extFileStatus = Environment.getExternalStorageState();// 获取SD卡状态
			File extFile = Environment.getExternalStorageDirectory();// 获取扩展存储设备的文件目录
			if (extFileStatus.endsWith(Environment.MEDIA_MOUNTED) && extFile.exists() && extFile.isDirectory()
					&& extFile.canWrite()) {
				paths.add(extFile.getAbsolutePath());
			}
			try {
				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec("mount");
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				int mountPathIndex = 1;
				while ((line = br.readLine()) != null) {
					if ((!line.contains("fat") && !line.contains("fuse") && !line.contains("storage"))
							|| line.contains("secure") || line.contains("asec") || line.contains("firmware")
							|| line.contains("private") || line.contains("shell") || line.contains("obb")
							|| line.contains("legacy") || line.contains("data") || line.contains("bootloader")) {
						continue;
					}
					String[] parts = line.split(" ");
					int length = parts.length;
					if (mountPathIndex >= length) {
						continue;
					}
					String mountPath = parts[mountPathIndex];
					if (!mountPath.contains("/") || mountPath.contains("data") || mountPath.contains("Data")) {
						continue;
					}
					File mountRoot = new File(mountPath);
					if (!mountRoot.exists() || !mountRoot.isDirectory() || !mountRoot.canWrite()) {
						continue;
					}
					boolean equalsToPrimarySD = mountPath.equals(extFile.getAbsolutePath());
					if (equalsToPrimarySD) {
						continue;
					}
					paths.add(mountPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			strPath = paths;

			return strPath;
		} else {
			return strPath;
		}
	}

	@SuppressLint("DefaultLocale")
	public static int getMdeiaType(String byName) {

		if (byName.toLowerCase().endsWith(".jpg") || byName.toLowerCase().endsWith(".jpeg")) {
			return mediaType.Image;
		} else if (byName.toLowerCase().endsWith(".bmp") || byName.toLowerCase().endsWith(".png")) {
			return mediaType.Image;
		} else if (byName.toLowerCase().endsWith(".txt")) {
			return mediaType.Text;
		} else if (byName.toLowerCase().endsWith(".mp3") || byName.toLowerCase().endsWith(".ac3")
				|| byName.toLowerCase().endsWith(".wma")) {
			return mediaType.Audio;
		} else if (byName.toLowerCase().endsWith(".xml")) {
			return mediaType.Xml;
		} else if (byName.toLowerCase().endsWith(".zip") || byName.toLowerCase().endsWith(".apk")
				|| byName.toLowerCase().endsWith(".rar")) {
			return mediaType.Update;
		} else {
			return mediaType.Video;
		}
	}

	public static ArrayList<String> getFileLst(String sch) {

		if (checkFile(sch) == false) {
			return null;
		}
		ArrayList<String> lst = new ArrayList<String>();
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse("file://" + sch);
			Element root = document.getDocumentElement();

			NodeList bodyNodes = root.getElementsByTagName("Item");
			for (int i = 0; i < bodyNodes.getLength(); i++) {
				Element itemEle = (Element) bodyNodes.item(i);
				if (itemEle.getNodeName().compareTo("Item") == 0) {
					String name = itemEle.getAttribute("Name");

					if (getMdeiaType(name) == mediaType.Video) {
						int k = 0;
						for (k = 0; k < lst.size(); k++) {
							if (lst.get(k) != null && lst.get(k).compareTo(name) == 0) {
								k = -1;
								break;
							}
						}
						if (k != -1)
							lst.add(name);
					} else {
						int k = 0;
						for (k = 0; k < lst.size(); k++) {
							if (lst.get(k) != null && lst.get(k).compareTo(name) == 0) {
								k = -1;
								break;
							}
						}
						if (k != -1)
							lst.add(0, name);
					}
				}
			}
		} catch (Exception e) {
		}
		return lst;
	}

	public static long getTickCount() {

		// return System.currentTimeMillis();
		// return SystemClock.uptimeMillis();

		return SystemClock.elapsedRealtime();
	}

	public static long getDiskSize(String path) {
		try {
			StatFs sf = new StatFs(path);
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();

			return (long) (blockSize * blockCount / 1024 / 1024);
		} catch (Exception e) {
		}
		return 0;
	}

	public static String getDiskSpace() {// 获取磁盘空间
		String diskSpace = "";
		List<String> storagePath = getStoragePath();
		double free = 0, total = 0;
		for (int i = 0; i < storagePath.size(); i++) {
			StatFs sf = new StatFs(storagePath.get(i));
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();

			free += availCount * blockSize / 1024.0 / 1024.0;
			total += blockSize * blockCount / 1024.0 / 1024.0;
		}
		DecimalFormat df = new DecimalFormat("######0.00");// 十进制格式
		if ((free / 1024) > 1) {
			free = free / 1024.0;
			diskSpace = df.format(free) + "G/";// 磁盘大小单位为：G
		} else {
			diskSpace = df.format(free) + "M/";// 磁盘大小单位为：M
		}
		if ((total / 1024) > 1) {
			total = total / 1024.0;
			diskSpace = diskSpace + df.format(total) + "G";
		} else {
			diskSpace = diskSpace + df.format(total) + "M";
		}
		return diskSpace;
	}

	public static void setVolume(Context ctx, int volume) {
		if (volume >= 0 && volume < 100) {
			AudioManager mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume * 15 / 100, 0);
		}
	}

	public static String getIpAddress() {
		try {
			String ipv4;
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {// 如果不进行下一个判断，返回的是IPv6的IP地址
						if (!inetAddress.isLoopbackAddress()
								&& InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {
							// Log.d("IPV4", ipv4);
							return ipv4;
						}
					}
				}
			}
		} catch (SocketException ex) {
		}
		return "";
	}

	public static void zipFileRead(String file) {
		// 解压缩文件
		String saveRootDirectory = "/mnt/sdcard/";
		try {
			ZipFile zipFile = new ZipFile(file);
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipElement = (ZipEntry) enu.nextElement();
				InputStream read = zipFile.getInputStream(zipElement);
				String fileName = zipElement.getName();
				if (fileName != null && fileName.contains("build.prop")) {// (fileName
																			// !=
																			// null
																			// &&
																			// fileName.indexOf(".")
																			// !=
																			// -1)
																			// {
					unZipFile(zipElement, read, saveRootDirectory);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	public static void unZipFile(ZipEntry ze, InputStream read, String saveRootDirectory)
			throws FileNotFoundException, IOException {

		String fileName = ze.getName();
		File file = new File(saveRootDirectory + "build.prop");
		if (!file.exists()) {
			File rootDirectoryFile = new File(file.getParent());
			if (!rootDirectoryFile.exists()) {
				boolean ifSuccess = rootDirectoryFile.mkdirs();
				if (ifSuccess) {
					System.out.println("folder make ok!");
				} else {
					System.out.println("make folder fail!");
				}
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedOutputStream write = new BufferedOutputStream(new FileOutputStream(file));
		int cha = 0;
		while ((cha = read.read()) != -1) {
			write.write(cha);
		}
		write.flush();
		write.close();
		read.close();
	}

	public static String getOtaDate(String fileName) {
		String datStr = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() > 4 && line.contains("ro.build.description=")) {// ro.build.display.id=sugar_ref001-eng
																					// 4.2.2
																					// JDQ39
																					// 20150602
																					// test-keys
					String[] str = line.split(" ");
					if (str != null && str.length >= 2)
						datStr = str[str.length - 2];
					break;
				}
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return datStr;
	}

	public static String getUpdateVer(String fileName) {
		String rt = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() > 4 && line.contains("ro.build.version.release=")) {
					String[] str = line.split("=");
					rt = str[str.length - 1];
					break;
				}
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return rt;
	}

	public static String getSysType(String fileName) {
		String rt = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.length() > 4 && line.contains("ro.product.name=")) {
					if (line.contains("rk3288 "))
						rt = "rk3288";
					else
						rt = "NG"; // orther
					break;
				}
			}
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return rt;
	}

	public static void renameFile(String oldname, String newname) {

		if (!oldname.equals(newname)) {
			File oldfile = new File(oldname);
			File newfile = new File(newname);
			if (!oldfile.exists()) {
				return;
			}
			if (newfile.exists()) {
				System.out.println(newname + "exists!");
			}
			oldfile.renameTo(newfile);
		}
	}

	public static String readTxtFile(String fileName) {

		if (fileName == null || checkFile(fileName) == false) {
			return " ";
		}
		String res = null;
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public static void copyBinary(int id, String path, Context ctx) {
		System.out.println("copy -> " + path);
		try {
			InputStream ins = ctx.getResources().openRawResource(id);
			int size = ins.available();

			// Read the entire resource into a local byte buffer.
			byte[] buffer = new byte[size];
			ins.read(buffer);
			ins.close();

			FileOutputStream fos = new FileOutputStream(path);
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			System.out.println("public void createBinary() error! : " + e.getMessage());
		}
	}

	public static void runCmd(String cmdStr) {

		String[] cmd = new String[3];
		File file = new File("/system/xbin/su");
		if (file.exists()) {
			cmd[0] = "/system/xbin/su";
			cmd[1] = "-c";
			cmd[2] = cmdStr;

			Process pr = null;
			try {
				pr = Runtime.getRuntime().exec(cmd);
				pr.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void execShell(String cmd) {

		File file = new File("/system/xbin/su");
		if (file.exists()) {
			try {
				Process p = Runtime.getRuntime().exec("su");
				OutputStream outputStream = p.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
				dataOutputStream.writeBytes(cmd);
				dataOutputStream.flush();
				dataOutputStream.close();
				outputStream.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static String getMediaPath(String name, int type) {

		if (strPath == null) {
			try {
				strPath = getStoragePath();
			} catch (Exception e) {
			}
		}
		if (strPath != null) {
			for (int k = 0; k < strPath.size(); k++) {
				String curFile = strPath.get(k) + getSubPath(type, 1) + name;
				File file = new File(curFile);
				if (file.exists()) {
					return curFile;
				}
			}
		}
		return null;
	}

	public static String getMacStr() {

		String ethMac = null;
		if (ethMac == null) {
			ethMac = getMac();
			if (ethMac != null && ethMac.length() == 12) {
				ethMac = ethMac.substring(0, 2) + ":" + ethMac.substring(2, 4) + ":" + ethMac.substring(4, 6) + ":"
						+ ethMac.substring(6, 8) + ":" + ethMac.substring(8, 10) + ":" + ethMac.substring(10, 12);
			}
		}
		if (ethMac == null) {
			return "c8:dd:c9:b5:64:da";
		}
		return ethMac.toLowerCase();
	}

	public static String InputStreamTOString(InputStream in) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int count = -1;
		try {
			while ((count = in.read(data, 0, 4096)) != -1)
				outStream.write(data, 0, count);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = null;
		try {
			return new String(outStream.toByteArray(), "utf-8");// "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String httpGetStr(String pathUrl, int shwStr) {
		if (pathUrl == null) {
			return null;
		}
		if (!pathUrl.startsWith("http://") && !pathUrl.startsWith("HTTP://")) {
			pathUrl = "http://" + pathUrl;
		}
		// try {
		// pathUrl = URLEncoder.encode(pathUrl,"utf-8");
		// } catch (UnsupportedEncodingException e1) {
		// e1.printStackTrace();
		// }
		Log.i("SY-net", "GET>>>>" + pathUrl + "\n");
		try {
			InputStream inputStream = null;
			URL url = new URL(pathUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

			httpConn.setConnectTimeout(3000);
			httpConn.setDoInput(true);
			httpConn.setUseCaches(false);
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("Charset", "UTF-8");

			int responseCode = httpConn.getResponseCode();

			if (HttpURLConnection.HTTP_OK == responseCode) {
				inputStream = httpConn.getInputStream();
				String str = InputStreamTOString(inputStream);
				if (shwStr == 1) {
					Log.i("ntms-net", "Http GetStr>>>>" + str + "\n\n");
				}
				if (inputStream != null) {
					inputStream.close();
				}
				return str;
			} else {
				Log.i("ntms-net", "===Http Get Fail...===\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String getUtf8Code(String str) {

		String strCode = getEncoding(str);

		if (strCode == null || strCode.length() < 1 || strCode.equals("UTF-8")) {
			return str;
		}

		System.out.println("==========" + strCode + "============");

		String utf8 = null;

		try {
			utf8 = new String(str.getBytes(strCode), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return utf8;
	}

	public static String getEncoding(String str) {
		String encode = "gb2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "iso-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}

		encode = "utf-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "gbk";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "";
	}

	public static void poweron(Context ctx, String date, String time) {

		Intent intent = new Intent("zysd.alarm.poweron.time");
		intent.putExtra("poweronday", date);// "2014-06-11");
		intent.putExtra("powerontime", time);// "23:59");
		ctx.sendBroadcast(intent);
	}

	public static void poweroff(Context ctx, String date, String time) {
		Intent intent = new Intent("zysd.alarm.poweroff.time");
		intent.putExtra("poweroffday", date);// "2014-06-11");
		intent.putExtra("powerofftime", time);// "21:18");
		ctx.sendBroadcast(intent);
	}

	public static void cancelPowerOff(Context ctx) {
		Intent intent = new Intent("zysd.alarm.poweroff.cancel");
		ctx.sendBroadcast(intent);
	}

	public static void shutdownnow(Context ctx) {
		Intent intent = new Intent("shutdown.zysd.now");
		ctx.sendBroadcast(intent);
	}

	public static void rebootnow(Context ctx) {
		Intent intent = new Intent("reboot.zysd.now");
		ctx.sendBroadcast(intent);
	}

	// 如果想将一个方法作为本地方法的话，就必须声明该方法为native的，并且不能实现
	public native static String getMac();// 所有native关键词修饰的都是对本地的声明

	public native static String getGateway(String dev);

	public native static String getNetmask(String dev);

	public native static int writeCom(String dev, int baund, String cmdStr);

	public native static int sync();

	static {
		System.loadLibrary("ntjni");// 加载动态库（在对本地方法使用之前，在static{}中进行初始化）
	}
}
