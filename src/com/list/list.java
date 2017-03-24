package com.list;

import java.util.ArrayList;

public class list {

	public final class AreaType {
		public final static int Video = 1;
		public final static int Img = 2;
		public final static int Text = 3;
		public final static int Clock = 4;
		public final static int Mixed = 5;
		public final static int Dtv = 6;
		public final static int Audio = 7;
		public final static int Index = 8;
		public final static int Weather = 9;
	}

	public static class Item {

		public String AudioName = null;
		public String AudioPlayMode = null;
		public String Name = null;
		public String Rssaddr = null;
		public String Rssitem = null;

		public int Cycle = 0;
		public int Effect = 0;
		public int EffectSpeed = 0;
		public int PlayTime = 0;
		public int Volume = 0;
		public int Transparent = 0;
		public int MoveSpeed = 0;
		public int MoveStyle = 0;

	}

	public static class ProgramList {

		public String EndDate = null;
		public String EndTime = null;
		public String StartDate = null;
		public String StartTime = null;
		public ArrayList<Item> lst = new ArrayList<Item>();
	}

	public static class Area {

		public int X = 0;
		public int Y = 0;
		public int W = 0;
		public int H = 0;
		public int Type = 0;
		public int Freq = 0;
		public int Prog = 0;
		public int Volume = 0;

		public String Id = null;
		public String FontColor = null;
		public String WinColor = null;

		public ArrayList<ProgramList> list = new ArrayList<ProgramList>();
	}

	public static class Template {

		public String BgImgName = null;
		public String EndDate = null;
		public String EndTime = null;
		public String ID = null;
		public String StartDate = null;
		public String StartTime = null;
		public String Week = null;

		public ArrayList<Area> area = new ArrayList<Area>();
	}

	String EndTime = null;
	String InsertionPlay = null;
	String InsertionPlayTime = null;
	String Name = null;
	String StartTime = null;
	String TemplateTimeMode = null;
	String Ver = null;

	ArrayList<Template> template = new ArrayList<Template>();

}