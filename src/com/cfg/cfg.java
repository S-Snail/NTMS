package com.cfg;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ntms.baseFun;

import android.annotation.SuppressLint;
import android.util.Log;

public class cfg {

	public static class Para {
		public String[] dayOnoff = new String[7];
		public String[] weekOnoff = new String[7];
		public String[] dateOnoff = new String[7];

		public String sysversion = "rk3288+android-4.4";// ����

		public String appversion = "V0.93";
		public String passwd = "000000";
		public String city = "shanghai,songjiang";
		public String ntpserver = "120.25.250.211";// "202.112.29.82";

		public String mip = "120.25.250.211";// "121.40.140.146";//
												// "121.40.251.178";
		public String mipport = "6601";
		public String mftp = "120.25.250.211";// "121.40.140.146";//
												// "121.40.251.178";
		public String mftpport = "2121";
		public String mftpuser = "ntms";
		public String mftppwd = "ntms";

		public String sip = "120.25.250.211";// "121.40.140.146";//
												// "121.40.251.178";
		public String sipport = "6601";
		public String sftp = "120.25.250.211";// "121.40.140.146";//
												// "121.40.251.178";
		public String sftpport = "2121";
		public String sftpuser = "ntms";
		public String sftppwd = "ntms";

		public String rssitem = "";
		public String rssaddr = "";
		public String svrmac = "";
		public String cfgpasswd = "000000";

		public int volume = 0;
		public int screenOrient = 0;
		public int ftpSpd = 0;
		public int beatTime = 0;// ����ʱ��
		public int logEnable = 0;// ������־
		public int pwrEnable = 1;
		public int autoCopy = 1;

		// ----------------------------------------------------------------------------------------
		public int weekPwrOn = 1;
		public int dispOn = 1;
		public int syncOn = 0;
		public int filevalid = 0;
		public int onOff = 1;
		public String voltime = "00:00";
	}

	@SuppressLint("SdCardPath")
	public static String cfgXml = "/mnt/sdcard/ntms/cfg.xml";
	public static Para para = new Para();
	public static final String TAG = "Cfg";
	public static boolean loaded = false;

	public static void saveConfig(String fileName) {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element root = document.createElement("root");
			document.appendChild(root);
			// -----------------------------------------------------------------
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "city");
				propertyEle.appendChild(document.createTextNode("" + para.city));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "rssitem");
				propertyEle.appendChild(document.createTextNode("" + para.rssitem));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "svrmac");
				propertyEle.appendChild(document.createTextNode("" + para.svrmac));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "cfgpasswd");
				propertyEle.appendChild(document.createTextNode("" + para.cfgpasswd));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "rssaddr");
				propertyEle.appendChild(document.createTextNode("" + para.rssaddr));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "volume");
				propertyEle.appendChild(document.createTextNode("" + para.volume));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "log");
				propertyEle.appendChild(document.createTextNode("" + para.logEnable));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "powerenable");
				propertyEle.appendChild(document.createTextNode("" + para.pwrEnable));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "autocopy");
				propertyEle.appendChild(document.createTextNode("" + para.autoCopy));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "appversion");
				propertyEle.appendChild(document.createTextNode("" + para.appversion));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "weekPwrOn");
				propertyEle.appendChild(document.createTextNode("" + para.weekPwrOn));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "dispOn");
				propertyEle.appendChild(document.createTextNode("" + para.dispOn));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "syncOn");
				propertyEle.appendChild(document.createTextNode("" + para.syncOn));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "filevalid");
				propertyEle.appendChild(document.createTextNode("" + para.filevalid));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "onOff");
				propertyEle.appendChild(document.createTextNode("" + para.onOff));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "voltime");
				propertyEle.appendChild(document.createTextNode("" + para.voltime));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "ftpspeed");
				propertyEle.appendChild(document.createTextNode("" + para.ftpSpd));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "beatTime");
				propertyEle.appendChild(document.createTextNode("" + para.beatTime));
				root.appendChild(propertyEle);
			}
			for (int i = 0; i < 7; i++) {
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "holiday" + (i + 1));
				if (para.dateOnoff[i] != null && !para.dateOnoff[i].contains("null"))
					propertyEle.appendChild(document.createTextNode("" + para.dateOnoff[i]));
				else
					propertyEle.appendChild(document.createTextNode("00-00 00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "sunday");

				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[0] != null && !para.weekOnoff[0].contains("null"))
								? "" + para.weekOnoff[0] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "monday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[1] != null && !para.weekOnoff[1].contains("null"))
								? "" + para.weekOnoff[1] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "tuesday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[2] != null && !para.weekOnoff[2].contains("null"))
								? "" + para.weekOnoff[2] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "wednesday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[3] != null && !para.weekOnoff[3].contains("null"))
								? "" + para.weekOnoff[3] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "thursday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[4] != null && !para.weekOnoff[4].contains("null"))
								? "" + para.weekOnoff[4] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "friday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[5] != null && !para.weekOnoff[5].contains("null"))
								? "" + para.weekOnoff[5] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "saturday");
				propertyEle.appendChild(
						document.createTextNode((para.weekOnoff[6] != null && !para.weekOnoff[6].contains("null"))
								? "" + para.weekOnoff[6] : "00:00-00:00"));
				root.appendChild(propertyEle);
			}
			for (int i = 0; i < 7; i++) {
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "day" + (i + 1));
				if (para.dayOnoff[i] != null && !para.dayOnoff[i].contains("null"))
					propertyEle.appendChild(document.createTextNode("" + para.dayOnoff[i]));
				else
					propertyEle.appendChild(document.createTextNode("00:00-00:00"));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "screen");
				propertyEle.appendChild(document.createTextNode("" + para.screenOrient));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "settingpassword");
				propertyEle.appendChild(document.createTextNode("" + para.passwd));// ��������
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "ntpserver");
				propertyEle.appendChild(document.createTextNode("" + para.ntpserver));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mip");
				propertyEle.appendChild(document.createTextNode("" + para.mip));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mipport");
				propertyEle.appendChild(document.createTextNode("" + para.mipport));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "sip");
				propertyEle.appendChild(document.createTextNode("" + para.sip));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "sipport");
				propertyEle.appendChild(document.createTextNode("" + para.sipport));
				root.appendChild(propertyEle);
			}

			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mainftpaddr");
				propertyEle.appendChild(document.createTextNode("" + para.mftp));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mainftpport");
				propertyEle.appendChild(document.createTextNode("" + para.mftpport));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mainftpuser");
				propertyEle.appendChild(document.createTextNode("" + para.mftpuser));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "mainftppasswd");
				propertyEle.appendChild(document.createTextNode("" + para.mftppwd));
				root.appendChild(propertyEle);
			}

			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "subftpaddr");
				propertyEle.appendChild(document.createTextNode("" + para.sftp));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "subftpport");
				propertyEle.appendChild(document.createTextNode("" + para.sftpport));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "subftpuser");
				propertyEle.appendChild(document.createTextNode("" + para.sftpuser));
				root.appendChild(propertyEle);
			}
			{
				Element propertyEle = document.createElement("property");
				propertyEle.setAttribute("name", "subftppasswd");
				propertyEle.appendChild(document.createTextNode("" + para.sftppwd));
				root.appendChild(propertyEle);
			}
			// ----------------------------------------

			File configFile = new File(fileName);
			if (configFile.exists()) {
				configFile.delete();
			}

			Properties properties = new Properties();// 属性集合对象
			properties.setProperty(OutputKeys.INDENT, "yes");// indent
																// 指定了当输出结果树时，Transformer
																// 是否可以添加额外的空白；其值必须为
																// yes 或 no。ָ
			properties.setProperty(OutputKeys.MEDIA_TYPE, "xml");// media-type
																	// 指定了输出结果树得到的数据的介质类型（MIME
																	// 内容类型）
			properties.setProperty(OutputKeys.VERSION, "1.0");// version
																// 指定了输出方法的版本。
			properties.setProperty(OutputKeys.ENCODING, "utf-8");// encoding
																	// 指定了首选的字符编码，Transformer
																	// 应使用此编码将字符序列编码作为字节序列进行编码
			properties.setProperty(OutputKeys.METHOD, "xml");// method = "xml" |
																// "html" |
																// "text" |
																// expanded
																// name。
			properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");// ָomit-xml-declaration
																			// 指定了
																			// XSLT
																			// 处理器是否应输出
																			// XML
																			// 声明，其值必须为
																			// yes
																			// 或
																			// no
			TransformerFactory transfactory = TransformerFactory.newInstance();//
			Transformer transformer = transfactory.newTransformer();
			transformer.setOutputProperties(properties);//
			DOMSource domSource = new DOMSource(document);

			StreamResult result = new StreamResult(configFile);// StreamResult转换结果的持有者、构造方法：从File构造构造StreamResult
			transformer.transform(domSource, result);// XML Sourceת����Result

			baseFun.appendLogInfo("save config xm ", 4);
			baseFun.appendLogInfo(null, -1);
		} catch (Exception e) {
		}
	}

	public static void loadConfig(String fileName) {

		File configFile = new File(fileName);
		if (!configFile.exists() || configFile.length() < 32) {
			if (baseFun.checkFile(fileName) == false) {
				saveConfig(fileName);
				return;
			}
		}
		/** 解析XML文件 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileName));
			Element root = document.getDocumentElement();
			NodeList propertyNodes = root.getElementsByTagName("property");

			for (int i = 0; i < propertyNodes.getLength(); i++) {
				Element propertyEle = (Element) propertyNodes.item(i);
				String property = propertyEle.getAttribute("name");
				if (propertyEle.getFirstChild() != null) {
					String value = propertyEle.getFirstChild().getNodeValue();
					if (value == null || value.equals("null"))
						continue;

					Log.i(TAG, ">>>>>" + property + " : " + value);

					if (property.equals("city"))
						para.city = value;
					else if (property.equals("volume"))
						para.volume = Integer.parseInt(value);
					else if (property.equals("log"))
						para.logEnable = Integer.parseInt(value);
					else if (property.equals("powerenable"))
						para.pwrEnable = Integer.parseInt(value);
					else if (property.equals("autocopy"))
						para.autoCopy = Integer.parseInt(value);
					else if (property.equals("ftpspeed"))
						para.ftpSpd = Integer.parseInt(value);
					else if (property.equals("beatTime"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("rssitem"))
						para.rssitem = value;
					else if (property.equals("rssaddr"))
						para.rssaddr = value;
					else if (property.equals("svrmac"))
						para.svrmac = value;
					else if (property.equals("cfgpasswd"))
						para.cfgpasswd = value;

					else if (property.equals("weekPwrOn"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("dispOn"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("syncOn"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("filevalid"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("onOff"))
						para.beatTime = Integer.parseInt(value);
					else if (property.equals("voltime"))
						para.cfgpasswd = value;

					else if (property.equals("holiday1"))
						para.dateOnoff[0] = value;
					else if (property.equals("holiday2"))
						para.dateOnoff[1] = value;
					else if (property.equals("holiday3"))
						para.dateOnoff[2] = value;
					else if (property.equals("holiday4"))
						para.dateOnoff[3] = value;
					else if (property.equals("holiday5"))
						para.dateOnoff[4] = value;
					else if (property.equals("holiday6"))
						para.dateOnoff[5] = value;
					else if (property.equals("holiday7"))
						para.dateOnoff[6] = value;

					else if (property.equals("sunday"))
						para.weekOnoff[0] = value;
					else if (property.equals("monday"))
						para.weekOnoff[1] = value;
					else if (property.equals("tuesday"))
						para.weekOnoff[2] = value;
					else if (property.equals("wednesday"))
						para.weekOnoff[3] = value;
					else if (property.equals("thursday"))
						para.weekOnoff[4] = value;
					else if (property.equals("friday"))
						para.weekOnoff[5] = value;
					else if (property.equals("saturday"))
						para.weekOnoff[6] = value;

					else if (property.equals("day1"))
						para.dayOnoff[0] = value;
					else if (property.equals("day2"))
						para.dayOnoff[1] = value;
					else if (property.equals("day3"))
						para.dayOnoff[2] = value;
					else if (property.equals("day4"))
						para.dayOnoff[3] = value;
					else if (property.equals("day5"))
						para.dayOnoff[4] = value;
					else if (property.equals("day6"))
						para.dayOnoff[5] = value;
					else if (property.equals("day7"))
						para.dayOnoff[6] = value;

					else if (property.equals("screen"))
						para.screenOrient = Integer.parseInt(value);
					else if (property.equals("settingpassword"))
						para.passwd = value;
					else if (property.equals("ntpserver"))
						para.ntpserver = value;

					else if (property.equals("mip"))
						para.mip = value;
					else if (property.equals("mipport"))
						para.mipport = value;
					else if (property.equals("sip"))
						para.sip = value;
					else if (property.equals("sipport"))
						para.sipport = value;

					else if (property.equals("mainftpaddr"))
						para.mftp = value;
					else if (property.equals("mainftpport"))
						para.mftpport = value;
					else if (property.equals("mainftpuser"))
						para.mftpuser = value;
					else if (property.equals("mainftppasswd"))
						para.mftppwd = value;

					else if (property.equals("subftpaddr"))
						para.sftp = value;
					else if (property.equals("subftpport"))
						para.sftpport = value;
					else if (property.equals("subftpuser"))
						para.sftpuser = value;
					else if (property.equals("subftppasswd"))
						para.sftppwd = value;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
