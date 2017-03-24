package com.list;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.list.list.Area;
import com.list.list.AreaType;
import com.list.list.Item;
import com.list.list.ProgramList;
import com.list.list.Template;
import com.ntms.MainActivity;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

@SuppressLint("SdCardPath")
public class sch extends Thread {

	private static final String TAG = "sch";
	public static final String tskFile = "/mnt/sdcard/ntms/task/tasklst.xml";
	public static ArrayList<String> taskLst = null;
	private static List<String> storPath = null;
	private static list lst = null;
	public static String schFile = "/mnt/sdcard/ntms/task/plc.xml";
	private static sch schThd = null;
	private static int idxTmplt = -1;
	public static int reload = 0;

	public static sch newInstance() {
		schThd = new sch();
		return schThd;
	}

	public sch() {

	}

	@Override
	public void run() {

		int cnt = 0;

		while (baseFun.exitPlay != 1 && baseFun.pausePlay != 1) {

			if (reload == 1) {
				cnt = 0;
			}
			if (cnt == 0 && (taskLst = readTaskXml(tskFile)) != null) {
				if (taskLst.size() > 0) {
					Log.i("sch", ">>>>" + taskLst);
					if (checkCurXml(taskLst))
						reload = 1;
				}
			}

			if (lst == null || reload == 1) {
				reload = 0;
				loadSch(schFile);
				showSch(lst);
			}
			if (lst != null) {
				runSch(lst);
			}
			if (cnt++ > 30) {
				cnt = 0;
			}
			SystemClock.sleep(10 * 1000);
		}
	}

	private static void sendMsg(Bundle b, int mode) {

		if (mode == 0) {
			MainActivity.sendMessage(MainActivity.CHG_LAYOUT);
		} else if (mode == 2) {
			Message message = Message.obtain(MainActivity.mHandler, MainActivity.NEW_VIEW);
			message.setData(b);
			MainActivity.mHandler.sendMessage(message);
		}
	}

	// =======================================TASK
	// LIST=================================================

	public static boolean checkCurXml(ArrayList<String> List) {

		if (List == null || List.size() < 1) {
			return false;
		}
		if (storPath == null) {
			try {
				storPath = baseFun.getStoragePath();
			} catch (Exception e) {
			}
		}
		for (int j = 0; j < List.size(); j++) {
			int k = 0;
			String curFile = null;
			String xmlFile = List.get(j);

			String curXml = schFile.substring(schFile.lastIndexOf("/") + 1);
			if (xmlFile == null || !xmlFile.contains(".xml") || curXml.compareTo(xmlFile) == 0) {
				continue;
			}

			if (storPath != null) {
				for (k = 0; k < storPath.size(); k++) {
					curFile = storPath.get(k) + baseFun.getSubPath(mediaType.Xml, 1) + xmlFile;
					File file = new File(curFile);
					if (file.exists()) {
						k = -1;
						break;
					}
				}
				if (k != -1)
					continue;
				ArrayList<String> flist = baseFun.getFileLst(curFile);
				if (flist != null && flist.size() > 0) {
					for (int i = 0; i < flist.size(); i++) {
						String fname = flist.get(i);
						int ftype = baseFun.getMdeiaType(fname); // 判别文件类型
						for (k = 0; k < storPath.size(); k++) {
							File file = new File(storPath.get(k) + baseFun.getSubPath(ftype, 1) + fname);
							if (!file.exists()) {
								Log.i("sch", "file not exsit: " + fname);
								k = -1;
								break;
							}
						}
						if (k != -1) {
							if (taskLst == null) {
								taskLst = new ArrayList<String>();
							}
							taskLst.clear();// clear()用来清除数组中、或者列表中的数据的，为了避免数据的叠加，就需要在加载前用数组.clear()清除数据
							taskLst.add(xmlFile);
							saveTaskXml(tskFile, taskLst);// ？？？？

							schFile = curFile;
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static ArrayList<String> readTaskXml(String xmlPath) {

		XmlPullParser xmlParser = Xml.newPullParser();
		File file = new File(xmlPath);
		if (!file.exists()) {
			Log.d(TAG, "file :" + xmlPath + " is not exist!");
			return null;
		}
		ArrayList<String> List = new ArrayList<String>();
		int eventType = 0;
		try {
			FileInputStream fis = new FileInputStream(file);
			xmlParser.setInput(fis, "UTF-8");
			eventType = xmlParser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if ("task".equals(xmlParser.getName())) {
						String str = xmlParser.nextText();
						if (str != null && str.contains(".xml"))
							List.add(str);
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				default:
					break;
				}
				eventType = xmlParser.next();
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG, "file not found exception!");
		} catch (XmlPullParserException e) {
			Log.d(TAG, "XmlPullParserException!");
		} catch (IOException e) {
			Log.d(TAG, "IOException!");
		}
		return List;
	}

	public static boolean saveTaskXml(String xmlPath, ArrayList<String> List) {
		if (List == null || List.size() < 1) {
			baseFun.removeFile(xmlPath);
			return false;
		}
		FileOutputStream fos = null;
		File file = null;
		try {
			file = new File(xmlPath);
			if (!file.createNewFile()) {
				file.delete();
				if (!file.createNewFile())
					return false;
			}
		} catch (IOException e) {
			Log.i(TAG, "create xml file failed!");
		}
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		XmlSerializer serializer = Xml.newSerializer();// 新建一个Serializer
		try {
			serializer.setOutput(fos, "UTF-8");// 用给定的编码设置二进制输出流
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			serializer.startDocument("UTF-8", true);// 表示文档开始
			serializer.startTag(null, "resource");// 设置开始标签
			for (int i = 0; i < List.size(); i++) {// 遍历链表
				serializer.startTag(null, "task");// 开始标签
				serializer.text(List.get(i));
				serializer.endTag(null, "task");// 相对应的结束标签
			}
			serializer.endTag(null, "resource");
			serializer.endDocument();// 表示文档结束

			fos.flush();
			fos.close();
		} catch (IOException e) {
			Log.d(TAG, "IOException write xml file failed!");
			return false;
		} catch (Exception e) {
			Log.d(TAG, "Exception write xml file failed!");
			return false;
		}
		return true;
	}

	// =======================================RUN
	// LIST==================================================

	private static int runSch(list lst) {

		if (lst == null || lst.template.size() < 1) {
			return -1;
		}
		long genBtime = funs.parseTime(lst.StartTime, 0);
		long genEtime = funs.parseTime(lst.EndTime, 0);
		long genCtime = funs.getCurrentTime(0);

		if ((genCtime > genEtime || genCtime < genBtime) && genBtime > 0 && genEtime > 0) {
			return -1;
		}
		int idx = -1;
		long curTime = funs.getCurrentTime(1);

		for (int i = 0; i < lst.template.size(); i++) {
			long tmpBtime = funs.parseTime(lst.template.get(i).StartDate + " " + lst.template.get(i).StartTime, 0);
			long tmpEtime = funs.parseTime(lst.template.get(i).EndDate + " " + lst.template.get(i).EndTime, 0);
			if ((tmpBtime <= curTime && curTime < tmpEtime) || (tmpBtime == 0 && tmpEtime == 0)) {
				idx = i;
				break;
			}
		}
		if (idx != -1 && idx != idxTmplt) {
			idxTmplt = idx;
			Template tmplt = lst.template.get(idx);
			if (tmplt != null) {
				sendMsg(null, 0);
				for (int i = 0; i < tmplt.area.size(); i++) {
					Area area = tmplt.area.get(i);
					if (area != null) {
						Bundle b = new Bundle();
						b.putString("winid", area.Id);
						b.putString("FontColor", area.FontColor);
						b.putString("WinColor", area.WinColor);
						b.putInt("x", area.X);
						b.putInt("y", area.Y);
						b.putInt("w", area.W);
						b.putInt("h", area.H);
						b.putInt("type", area.Type);
						b.putInt("Freq", area.Freq);
						b.putInt("Prog", area.Prog);
						b.putInt("Volume", area.Volume);

						String str = null;
						for (int k = 0; k < area.list.size(); k++) {
							String str0 = "\"EndTime\":\"" + area.list.get(k).EndTime + "\"," + "\"StartTime\":\""
									+ area.list.get(k).StartTime + "\"";
							String str1 = getItemLst(area.list.get(k).lst);
							if (str == null) {
								str = str0 + "|" + str1;
							} else {
								str += "^" + str0 + "|" + str1;
							}
						}
						if (str != null) {
							b.putString("list", str);
						}
						sendMsg(b, 2);
					}
				}
			}
		}
		return -1;
	}

	private static String getItemLst(ArrayList<Item> lst) {

		String str = null;
		if (lst != null && lst.size() > 0) {
			for (int i = 0; i < lst.size(); i++) {
				String subStr = null;
				subStr = "\"AudioName\":\"" + lst.get(i).AudioName + "\",";
				subStr += "\"AudioPlayMode\":\"" + lst.get(i).AudioPlayMode + "\",";
				subStr += "\"Name\":\"" + lst.get(i).Name + "\",";
				subStr += "\"Rssaddr\":\"" + lst.get(i).Rssaddr + "\",";
				subStr += "\"Rssitem\":\"" + lst.get(i).Rssitem + "\",";
				subStr += "\"Cycle\":\"" + lst.get(i).Cycle + "\",";
				subStr += "\"Effect\":\"" + lst.get(i).Effect + "\",";//
				subStr += "\"EffectSpeed\":\"" + lst.get(i).EffectSpeed + "\",";//
				subStr += "\"PlayTime\":\"" + lst.get(i).PlayTime + "\",";
				subStr += "\"Volume\":\"" + lst.get(i).Volume + "\",";
				subStr += "\"Transparent\":\"" + lst.get(i).Transparent + "\",";
				subStr += "\"MoveSpeed\":\"" + lst.get(i).MoveSpeed + "\",";
				subStr += "\"MoveStyle\":\"" + lst.get(i).MoveStyle + "\"";

				if (str == null) {
					str = subStr;
				} else {
					str += "|" + subStr;
				}
			}
		}
		return str;
	}

	// =======================================LOAD
	// LIST==================================================

	private static void showSch(list lst) {

		if (lst == null || lst.template.size() < 1) {
			Log.i(TAG, "\n==========List is null=================\n");
			Log.i("List is null", "sch_369");
		}
		if (lst != null) {
			Log.i(TAG, "\n>>>>" + lst.StartTime + " " + lst.EndTime + " " + lst.Name + "\n");

			for (int i = 0; i < lst.template.size(); i++) {
				Log.i(TAG, "\n>>>>" + lst.template.get(i).StartDate + " " + lst.template.get(i).EndDate + "\n");
			}
		}
	}

	private static int loadSch(String schFile) {

		if (schFile == null || baseFun.checkFile(schFile) == false) {
			Log.i(TAG, "\n\n=======================Sch is null=========================\n\n");
			return 0;
		} else {
			Log.i(TAG, "\n\n======================Loading sch...=======================\n\n");
			baseFun.appendLogInfo("load playlist: " + schFile, 4);
			if (lst == null) {
				lst = new list();
			} else {
				lst.template.clear();
			}

			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = builder.parse("file://" + schFile);
				Element root = document.getDocumentElement();

				try {
					NodeList itemGnl = root.getElementsByTagName("General");
					if (itemGnl != null) {
						Element item = (Element) itemGnl.item(0);
						if (item != null && item.getNodeName().compareTo("General") == 0) {
							lst.EndTime = item.getAttribute("EndTime");
							lst.InsertionPlay = item.getAttribute("InsertionPlay");
							lst.InsertionPlayTime = item.getAttribute("InsertionPlayTime");
							lst.Name = item.getAttribute("Name");
							lst.StartTime = item.getAttribute("StartTime");
							lst.TemplateTimeMode = item.getAttribute("TemplateTimeMode");
							lst.Ver = item.getAttribute("Ver");
						}
					}
				} catch (Exception e) {
				}

				NodeList itemTpl = root.getElementsByTagName("Template");
				if (itemTpl != null) {
					for (int i = 0; i < itemTpl.getLength(); i++) {
						Node n = itemTpl.item(i);
						if (n.hasChildNodes() && n.getNodeName().compareTo("Template") == 0) {
							try {
								Template tmpl = praseXml(n.getChildNodes());
								if (tmpl != null) {
									lst.template.add(tmpl);
								}
							} catch (Exception e) {
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	private static Template praseXml(NodeList nl) {

		Template tmpl = new Template();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().compareTo("General") == 0) {
				NamedNodeMap attributes = n.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String nodeName = attribute.getNodeName();
					if (nodeName.equals("BgImgName"))
						tmpl.BgImgName = attribute.getNodeValue();
					if (nodeName.equals("EndDate"))
						tmpl.EndDate = attribute.getNodeValue();
					if (nodeName.equals("EndTime"))
						tmpl.EndTime = attribute.getNodeValue();
					if (nodeName.equals("ID"))
						tmpl.ID = attribute.getNodeValue();
					if (nodeName.equals("StartDate"))
						tmpl.StartDate = attribute.getNodeValue();
					if (nodeName.equals("StartTime"))
						tmpl.StartTime = attribute.getNodeValue();
					if (nodeName.equals("Week"))
						tmpl.Week = attribute.getNodeValue();
				}
			} else {
				int areaType = -1;

				if (n.getNodeName().compareTo("MixedArea") == 0) {
					areaType = AreaType.Mixed;
				} else if (n.getNodeName().compareTo("TextArea") == 0) {
					areaType = AreaType.Text;
				} else if (n.getNodeName().compareTo("ImgArea") == 0) {
					areaType = AreaType.Img;
				} else if (n.getNodeName().compareTo("IndexArea") == 0) {
					areaType = AreaType.Index;
				} else if (n.getNodeName().compareTo("DTVArea") == 0 || n.getNodeName().compareTo("ClockArea") == 0) {
					Area area = new Area();
					area.Type = n.getNodeName().compareTo("ClockArea") == 0 ? AreaType.Clock : AreaType.Dtv;
					NamedNodeMap attributes = n.getAttributes();
					for (int j = 0; j < attributes.getLength(); j++) {
						Node attribute = attributes.item(j);
						String nodeName = attribute.getNodeName();
						if (nodeName.equals("Height"))
							area.H = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("Width"))
							area.W = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("ID"))
							area.Id = attribute.getNodeValue();
						if (nodeName.equals("X"))
							area.X = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("Y"))
							area.Y = Integer.parseInt(attribute.getNodeValue());
					}
					tmpl.area.add(area);
				} else if (n.getNodeName().compareTo("WeatherArea") == 0) {
					Area area = new Area();
					area.Type = AreaType.Weather;
					NamedNodeMap attributes = n.getAttributes();
					for (int j = 0; j < attributes.getLength(); j++) {
						Node attribute = attributes.item(j);
						String nodeName = attribute.getNodeName();
						if (nodeName.equals("Height"))
							area.H = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("Width"))
							area.W = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("ID"))
							area.Id = attribute.getNodeValue();
						if (nodeName.equals("X"))
							area.X = Integer.parseInt(attribute.getNodeValue());
						if (nodeName.equals("Y"))
							area.Y = Integer.parseInt(attribute.getNodeValue());
					}
					tmpl.area.add(area);
				}
				try {
					if (areaType != -1 && n.hasChildNodes()) {
						Area area = praseArea(n.getChildNodes());
						if (area != null) {
							area.Type = areaType;
							tmpl.area.add(area);
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return tmpl;
	}

	private static Area praseArea(NodeList nl) {

		Area area = new Area();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().compareTo("Font") == 0) {
				NamedNodeMap attributes = n.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String nodeName = attribute.getNodeName();
					if (nodeName.equals("Color"))
						area.FontColor = attribute.getNodeValue();
					if (nodeName.equals("WinColor"))
						area.WinColor = attribute.getNodeValue();
				}
			} else if (n.getNodeName().compareTo("Window") == 0) {
				NamedNodeMap attributes = n.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String nodeName = attribute.getNodeName();
					if (nodeName.equals("Height"))
						area.H = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("Width"))
						area.W = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("ID"))
						area.Id = attribute.getNodeValue();
					if (nodeName.equals("X"))
						area.X = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("Y"))
						area.Y = Integer.parseInt(attribute.getNodeValue());
				}
			} else if (n.getNodeName().compareTo("ProgramList") == 0) {
				try {
					ProgramList lst = parseList(n.getChildNodes());
					if (lst != null) {
						area.list.add(lst);
					}
				} catch (Exception x) {

				}
			}
		}
		return area;
	}

	private static ProgramList parseList(NodeList nl) {

		ProgramList lst = new ProgramList();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().compareTo("Time") == 0) {
				NamedNodeMap attributes = n.getAttributes();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String nodeName = attribute.getNodeName();
					if (nodeName.equals("EndTime"))
						lst.EndTime = attribute.getNodeValue();
					if (nodeName.equals("StartTime"))
						lst.StartTime = attribute.getNodeValue();
				}
			} else if (n.getNodeName().compareTo("Item") == 0) {
				NamedNodeMap attributes = n.getAttributes();
				Item itm = new Item();
				for (int j = 0; j < attributes.getLength(); j++) {
					Node attribute = attributes.item(j);
					String nodeName = attribute.getNodeName();

					if (nodeName.equals("AudioName"))
						itm.AudioName = attribute.getNodeValue();
					if (nodeName.equals("AudioPlayMode"))
						itm.AudioPlayMode = attribute.getNodeValue();
					if (nodeName.equals("Name"))
						itm.Name = attribute.getNodeValue();
					if (nodeName.equals("Rssaddr"))
						itm.Rssaddr = attribute.getNodeValue();
					if (nodeName.equals("Rssitem"))
						itm.Rssitem = attribute.getNodeValue();

					if (nodeName.equals("Cycle"))
						itm.Cycle = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("Effect"))
						itm.Effect = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("EffectSpeed"))
						itm.EffectSpeed = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("PlayTime"))
						itm.PlayTime = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("Volume"))
						itm.Volume = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("Transparent"))
						itm.Transparent = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("MoveSpeed"))
						itm.MoveSpeed = Integer.parseInt(attribute.getNodeValue());
					if (nodeName.equals("MoveStyle"))
						itm.MoveStyle = Integer.parseInt(attribute.getNodeValue());
				}
				lst.lst.add(itm);
			}
		}
		return lst;
	}

	// =======================================================================================================================
}
