package com.net;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.cfg.cfg;
import com.list.sch;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;
import com.ntms.usbCopy;

import android.annotation.SuppressLint;
import android.os.StatFs;
import android.util.Log;
import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

@SuppressLint("SimpleDateFormat")
public class ftp extends Thread {

	public final int D_LAST = 0;
	public final int D_FIRST = 1;
	public final int D_CLEAR = 2;

	private ArrayList<String> dwnInfo = new ArrayList<String>();
	private ArrayList<HashMap<String, Object>> dwnList = new ArrayList<HashMap<String, Object>>();
	private HashMap<String, Object> curFile = null;
	private List<String> storPath = null;

	private String curDwnXml = null;
	private String server = null;
	private String port = null;
	private String ftpUsr = null;
	private String ftpPwd = null;
	private boolean tryTimes = true;
	private int spdLimit = 0;

	private FTPClient client = null;
	private FtpListener ftpLsn = null;
	private boolean needClear = false;
	public static int dwnSize = 0;
	public static int dwnSpd = 0;

	@Override
	public void run() {

		initClient();

		String dstPath = getPropertyPath() + "/ntms/";// 主文件夹
		// 子文件夹
		baseFun.checkDir(dstPath + "image/");
		baseFun.checkDir(dstPath + "text/");
		baseFun.checkDir(dstPath + "audio/");
		baseFun.checkDir(dstPath + "video/");
		baseFun.checkDir(dstPath + "task/");
		baseFun.checkDir(dstPath + "update/");

		while (baseFun.exitPlay != 1) {

			// ------------------------------------------------------------------------------------
			if (dwnList != null && dwnList.size() < 1 && storPath != null) {
				if (sch.taskLst != null && sch.taskLst.size() > 0) {

					for (int j = sch.taskLst.size() - 1; j >= 0; j--) {
						int k = 0;
						String absPath = null;
						String xmlFile = sch.taskLst.get(j);
						if (xmlFile == null || !xmlFile.contains(".xml")) {
							continue;
						}
						for (k = 0; k < storPath.size(); k++) {
							absPath = storPath.get(k) + baseFun.getSubPath(mediaType.Xml, 1) + xmlFile;
							File file = new File(absPath);
							if (file.exists()) {
								k = -1;
								break;
							}
						}
						if (k != -1) {
							curDwnXml = xmlFile;
							addDownList(xmlFile, xmlFile, mediaType.Xml, 1, D_FIRST);
							continue;
						}
						curDwnXml = xmlFile;
						if (absPath == null) {
							continue;
						}
						ArrayList<String> flist = baseFun.getFileLst(absPath);// sch.schFile);
						if (flist != null && flist.size() > 0) {
							for (int i = 0; i < flist.size(); i++) {
								String fname = flist.get(i);
								int ftype = baseFun.getMdeiaType(fname);
								for (k = 0; k < storPath.size(); k++) {
									File file = new File(storPath.get(k) + baseFun.getSubPath(ftype, 1) + fname);
									if (file.exists()) {
										k = -1;
										break;
									}
								}
								if (k != -1) {
									addDownList(fname, fname, ftype, ftype == mediaType.Xml ? 1 : 0,
											ftype == mediaType.Video ? D_LAST : D_FIRST);
								}
							}
						}
					}
				} else {
					curDwnXml = sch.schFile;
					ArrayList<String> flist = baseFun.getFileLst(sch.schFile);
					if (flist != null && flist.size() > 0) {
						for (int i = 0; i < flist.size(); i++) {
							String fname = flist.get(i);
							int k = 0, ftype = baseFun.getMdeiaType(fname);
							for (k = 0; k < storPath.size(); k++) {
								File file = new File(storPath.get(k) + baseFun.getSubPath(ftype, 1) + fname);
								if (file.exists()) {
									k = -1;
									break;
								}
							}
							if (k != -1) {
								addDownList(fname, fname, ftype, ftype == mediaType.Xml ? 1 : 0,
										ftype == mediaType.Video ? D_LAST : D_FIRST);
							}
						}
					}
				}
			}
			// ------------------------------------------------------------------------------------

			try {
				if (dwnList != null && dwnList.size() > 0) {
					curFile = dwnList.get(0);
					dwnList.remove(curFile);

					downFile(curFile);

					String fname = (String) curFile.get("filename");

					if (baseFun.getMdeiaType(fname) == mediaType.Xml) {

						ArrayList<String> flist = baseFun.getFileLst(dstPath + "task/" + fname);
						if (flist != null && flist.size() > 0) {
							for (int i = 0; i < flist.size(); i++) {
								String name = flist.get(i);
								int k = 0, type = baseFun.getMdeiaType(name);
								if (storPath != null) {
									for (k = 0; k < storPath.size(); k++) {
										File file = new File(storPath.get(k) + baseFun.getSubPath(type, 1) + name);
										if (file.exists()) {
											k = -1;
											break;
										}
									}
								}
								if (k != -1) {
									addDownList(fname, fname, type, type == mediaType.Xml ? 1 : 0,
											type == mediaType.Video ? D_LAST : D_FIRST);
								}
							}
						}
					} else if (baseFun.getMdeiaType(fname) == mediaType.Update) {
						if (fname.endsWith(".zip")) {// ??
							baseFun.renameFile(dstPath + "update/" + fname, dstPath + "update/ota.zip");
							usbCopy.checkOtaFile(dstPath + "update/ota.zip");
						} else if (fname.endsWith(".apk")) {
							baseFun.renameFile(dstPath + "update/" + fname, dstPath + "update/ntms.apk");
							baseFun.runCmd("/system/bin/sh /data/data/com.ntms/files/upt");
						}

					} else if (dwnList.size() < 1) {
						sch.reload = 1;
					}
				} else {
					curFile = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			synchronized (currentThread()) {

				if (dwnList != null && dwnList.size() < 1) {
					Wait(3000 * 30);
				} else {
					Wait(3000);
				}
			}
		}
	}

	private synchronized void downFile(HashMap<String, Object> file) {

		String fileName = (String) file.get("filename");
		int fileType = (Integer) (file.get("filetype"));
		int recover = (Integer) file.get("force");
		long restartSize = 0;

		setFtpAddress();

		if (recover != 1 && storPath != null) {
			for (int k = 0; k < storPath.size(); k++) {
				String filelocalPath = storPath.get(k) + baseFun.getSubPath(fileType, 1) + fileName;
				if (baseFun.checkFile(filelocalPath)) {// 检查文件是否存在
					ftpLsn.OnFileDownloadFinished(fileName, fileType, true);
					return;
				}
			}
		}
		try {
			if (client.isConnected()) {
				client.disconnect(false);
			}
			needClear = false;
			String path = getPropertyPath();
			client.connect(server, Integer.valueOf(port));// 链接Server
			infoLog("ftp conn:" + server + ":" + String.valueOf(port));
			client.login(ftpUsr, ftpPwd);// 登录，用户名，密码
			client.changeDirectory(baseFun.getSubPath(fileType, 0));
			infoLog("ftp login");

			file.put("filesize", client.fileSize(fileName));// client.fileSize(fileName)获得FTP文件size
			infoLog("file(" + fileName + ")size:" + file.get("filesize"));

			file.put("tmpFile", path + baseFun.getSubPath(fileType, 1) + fileName + "!");
			file.put("desFile", path + baseFun.getSubPath(fileType, 1) + file.get("localfilename"));

			File tmpFile = new File((String) file.get("tmpFile"));
			if (tmpFile.exists()) {
				restartSize = tmpFile.length();
			}
			infoLog("down start:" + fileName + "(" + String.valueOf(restartSize) + ")");
			file.put("downloadedsize", restartSize);
			file.put("lasttransfer", new Date().getTime());

			baseFun.appendLogInfo("down file :" + fileName, 4);
			client.download(fileName, tmpFile, restartSize, new ftpTransferListener());

			client.disconnect(true);
			baseFun.appendLogInfo("down file :" + fileName + " ok", 4);

			infoLog("Download end");
			return;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			tryTimes = !tryTimes;
		} catch (IOException e) {
			if (e.toString().contains("SocketTimeout")) {
				infoLog("ftp timeout:" + server + ":" + String.valueOf(port));
			}
			tryTimes = !tryTimes;
		} catch (FTPIllegalReplyException e) {
			e.printStackTrace();
		} catch (FTPException e) {
			e.printStackTrace();
			tryTimes = !tryTimes;
		} catch (FTPDataTransferException e) {
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (!needClear) {
				if (file.containsKey("error")) {
					int errorCnt = (Integer) file.get("error");
					if (errorCnt < 4) {
						file.put("error", errorCnt + 1);
						dwnList.add(file);
					} else {
						infoLog("download fail,cancel:" + fileName);
						if (ftpLsn != null) {
							ftpLsn.OnFileDownloadFinished(fileName, fileType, false);
						}
					}
				} else {
					dwnList.add(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		curFile = null;
	}

	public class ftpTransferListener implements FTPDataTransferListener {

		public void started() {

		}

		public synchronized void transferred(int length) {
			long downloadedsize = (Long) curFile.get("downloadedsize");
			downloadedsize = downloadedsize + length;
			curFile.put("downloadedsize", downloadedsize);// HashMap<Key,Value>

			long lasttransfer = (Long) curFile.get("lasttransfer");
			long currentTime = new Date().getTime();
			if (spdLimit > 0) {
				if (currentTime > lasttransfer) {
					double speed = length / (currentTime - lasttransfer);
					if (speed > spdLimit) {
						long waitTime = length / spdLimit - (currentTime - lasttransfer);
						if (waitTime > 0) {
							Wait(waitTime);
						}
					}
				}
			}
			if (currentTime > lasttransfer) {
				dwnSpd = (int) (length / (currentTime - lasttransfer));
				curFile.put("lasttransfer", new Date().getTime());
			}
		}

		public void completed() {
			try {
				if (curFile != null) {
					String fileName = (String) curFile.get("filename");
					int type = (Integer) curFile.get("filetype");
					String tmpFileName = (String) curFile.get("tmpFile");
					String desFileName = (String) curFile.get("desFile");
					File tmpFile = new File(tmpFileName);
					File desFile = new File(desFileName);
					if (storPath != null) {
						for (int i = 0; i < storPath.size(); i++) {
							File file = new File(
									storPath.get(i) + baseFun.getSubPath(type, 1) + curFile.get("localfilename"));
							if (file.exists()) {
								file.delete();
							}
						}
					}
					if (tmpFile.exists()) {
						tmpFile.renameTo(desFile);
						baseFun.sync();
					}
					infoLog("downend:" + desFileName);

					if (ftpLsn != null) {
						ftpLsn.OnFileDownloadFinished(fileName, type, true);// 文件下载完毕
					}
				}
			} catch (Exception e) {
			}
		}

		public void aborted() {

		}

		public void failed() {

		}
	}

	public void addDownList(String remotefilename, String localfilename, int filetype, int force, int dMode) {

		HashMap<String, Object> file = new HashMap<String, Object>();
		file.put("filename", remotefilename);
		file.put("localfilename", localfilename);
		file.put("filetype", filetype);
		file.put("force", force);
		file.put("error", 0);

		if (filetype == mediaType.Xml) {
			curDwnXml = localfilename;
		}
		if (dwnList != null) {
			switch (dMode) {
			case D_LAST:
				dwnList.add(file);
				break;
			case D_FIRST:
				dwnList.add(0, file);
				try {
					client.abortCurrentDataTransfer(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case D_CLEAR:
				dwnList.clear();
				dwnList.add(file);
				if (client != null) {
					try {
						needClear = true;
						client.abortCurrentDataTransfer(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
			dwnSize = dwnList.size();
		}
	}

	public String getDownXml() {
		String ret = " ";
		if (curDwnXml != null) {

			ret = curDwnXml.substring(curDwnXml.lastIndexOf("/") + 1);
		}
		return ret;
	}

	public String getFileStatus() {
		String ret = " ";
		if (curFile != null) {
			ret = "" + curFile.get("filename");
		}
		return ret;
	}

	public String getProgress() {
		String ret = "";
		if (curFile != null) {
			try {
				if (curFile.containsKey("filesize") && curFile.containsKey("downloadedsize")) {
					long filesize = (Long) curFile.get("filesize");
					long downloadedsize = (Long) curFile.get("downloadedsize");
					// ret = downloadedsize + "/" + filesize;
					ret = "" + (downloadedsize * 100 / filesize);// 下载的进度百分比
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------

	public ArrayList<String> getDownloadInfo() {
		return dwnInfo;
	}

	public void initClient() {
		try {
			storPath = baseFun.getStoragePath();
			infoLog("Download nothing");
			client = new FTPClient();
			client.setPassive(true);
			client.setAutoNoopTimeout(30000);
		} catch (Exception e) {
		}
	}

	private void setFtpAddress() {
		if (tryTimes) {
			server = cfg.para.mftp;
			port = cfg.para.mftpport;
			ftpUsr = cfg.para.mftpuser;
			ftpPwd = cfg.para.mftppwd;
		} else {
			if (cfg.para.sftp != null && cfg.para.sftpport != null) {
				server = cfg.para.sftp;
				port = cfg.para.sftpport;
				ftpUsr = cfg.para.sftpuser;
				ftpPwd = cfg.para.sftppwd;
			}
		}
		try {
			if (cfg.para.ftpSpd > 60) {
				spdLimit = cfg.para.ftpSpd;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void infoLog(String log) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log = "[" + sdf.format(new Date()) + "] " + log;

		Log.i("ftp", log);
		if (dwnInfo.size() > 10)
			dwnInfo.remove(9);

		if (dwnInfo.size() <= 0)
			dwnInfo.add(log);
		else
			dwnInfo.add(1, log);
	}

	public String getPropertyPath() {

		String path = "/";
		if (storPath != null) {
			try {
				for (int i = 0; i < storPath.size(); i++) {
					StatFs sf = new StatFs(
							storPath.get(i));/** StatFs存储卡操作,获取手机内部可用空间大小 */
					long availCount = sf.getAvailableBlocks();// 块数
					long blockSize = sf.getBlockSize();// 块的大小
					double free = availCount * blockSize / 1024.0 / 1024.0;// 可用空间大小
					if (free > 500) {
						path = storPath.get(i);
						break;
					}
				}
			} catch (Exception e) {

			}
		}
		return path;
	}

	public void reconnect() {
		if (client != null) {
			try {
				client.abortCurrentDataTransfer(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setFtpListener(FtpListener l) {
		ftpLsn = l;
	}

	public int getDownListSize() {
		return dwnList.size();
	}

	interface FtpListener {
		public void OnFileDownloadFinished(String fileName, int type, boolean ok);
	}

	private void Wait(long millis) {
		try {
			wait(millis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// ----------------------------------------------------------------------------------------------------------------------------------------

}