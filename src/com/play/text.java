package com.play;

import java.util.ArrayList;
import java.util.Vector;

import com.list.funs;
import com.list.list.ProgramList;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class text extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private static String TAG = "text";
	private SurfaceHolder mSurfaceHolder = null;
	private boolean isMove = false;
	private static final int FLAG = 0;
	private String content = null;
	private String rssStr = null;
	private String bgColor = "#FF886E";
	private String fontColor = "#EEBB66";
	private int fontAlpha = 255;
	private float fontSize = 20f;

	private boolean loop = true;
	private float x = 0;
	private float y = 0;
	private int width = 0;
	private int height = 0;

	private Handler mHandler = null;
	private Paint paint = null;
	private int isbgtext = 0;
	Vector<String> m_String = new Vector<String>();

	private int m_iRealLine = 0;
	private int txtIdx = 0;
	private int lstIdx = 0;
	private int w = 0, h = 0;
	private int mode = 0;

	ArrayList<ProgramList> pList = null;

	private long logDur = 0;
	private String logName = null;

	public text(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("HandlerLeak")
	public text(Context context, int width, int height, int left, int top, String listStr, String id, String color,
			String bgColor, int mode) {
		super(context);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		this.isbgtext = 0;
		this.x = this.width = width;
		this.y = this.height = height;
		this.w = width;
		this.h = height;
		this.mode = mode;

		if (color != null) {
			if (color.startsWith("#")) {
				this.fontColor = color;
			} else {
				try {
					long clr = Long.parseLong(color);
					this.fontColor = "#" + Long.toHexString(clr);
				} catch (Exception e) {
				}
			}
		}
		if (bgColor != null) {
			if (bgColor.startsWith("#")) {
				this.bgColor = bgColor;
			} else {
				try {
					long clr = Long.parseLong(bgColor);
					this.bgColor = "#" + Long.toHexString(clr);
				} catch (Exception e) {
				}
			}
		}

		if (this.w * this.h == 1920 * 1080 || this.w * this.h == 1280 * 720 || this.w * this.h == 1366 * 768) {
			baseFun.fullTag = 1;
			this.isbgtext = 1;
		}
		if (this.isbgtext == 0 && mode == 0) {
			setBackgroundColor(Color.parseColor(this.bgColor));
		}
		if (this.width > this.height) {
			fontSize = this.height - 10;
		} else {
			fontSize = this.width - 10;
		}
		if (fontSize > 68)
			fontSize = 68;

		Log.i(TAG, ">>>>>>" + listStr + " : " + fontSize);

		if (this.mode == 0) {
			if (listStr != null)
				pList = funs.parseList(listStr);
		} else {
			fontColor = "#FF0000";
			content = rssStr = listStr;
		}

		paint = new Paint();
		paint.setAntiAlias(true);// 消除锯齿
		paint.setTypeface(Typeface.SANS_SERIF);// 设置字体
		paint.setTextSize(fontSize);
		paint.setAlpha(fontAlpha);

		setZOrderOnTop(true);// 当SurfaceView和GLSurfaceView同时在一个布局里面，
								// 如果想让SurfaveView显示图片或者视频必须要调用SurfaceView.setZOrderOnTop(true)，
								// 必须把SurfaceView置于Activity显示窗口的最顶层才能正常显示

		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case FLAG:
					getNextText(1);
					break;
				default:
					break;
				}
			}
		};
		getNextText(0);// can't move

		Log.i(TAG, ">>>>>>" + this.width + " : " + this.height);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.loop = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(this.width, widthMeasureSpec);
		int height = getDefaultSize(this.height, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.isMove = true;
		new Thread(this).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.loop = false;
	}

	public void stopPlay() {
		if (isbgtext == 1) {
			setBackgroundColor(Color.BLACK);
		}
		this.loop = false;
	}

	public void sendMessage(int id) {
		if (mode == 1 && rssStr == null) {
			this.loop = false;
			return;
		}
		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	private int chkCurLst() {

		for (int i = 0; i < pList.size(); i++) {
			long cur = funs.getCurrentTime(1);
			long ed = funs.parseTime(pList.get(i).EndDate, 1);
			long es = funs.parseTime(pList.get(i).StartDate, 0);
			if ((cur < ed && cur >= es) || (ed == 0 && es == 0)) {
				long t = funs.getCurrentTime(0);
				if (t < funs.parseTime(pList.get(i).EndTime, 0) && t >= funs.parseTime(pList.get(i).StartTime, 0)) {
					return i;
				}
			}
		}
		return 0;
	}

	public void getNextText(int mode) {

		if (rssStr != null) {
			paint.setTypeface(Typeface.DEFAULT);
			paint.setTextSize(fontSize);
			paint.setColor(Color.parseColor(fontColor));
			content = rssStr;
			return;
		}

		if (this.mode == 1) {
			return;
		}
		content = " ";

		if (pList != null) {
			int idx = chkCurLst();
			if (idx != lstIdx) {
				lstIdx = idx;
				txtIdx = 0;
			}
			if (txtIdx >= pList.get(lstIdx).lst.size() || txtIdx < 0)
				txtIdx = 0;
		}
		if (logName != null && logDur > 1) {
			baseFun.appendPlayDur(logName, (int) (baseFun.getTickCount() - logDur));
			logName = null;
			logDur = 0;
		}
		String fileStr = pList.get(lstIdx).lst.get(txtIdx).Name;
		String rssAdrr = pList.get(lstIdx).lst.get(txtIdx).Rssaddr;

		logName = pList.get(lstIdx).lst.get(txtIdx).Name;
		logDur = baseFun.getTickCount();

		txtIdx++;

		if (fileStr != null || rssAdrr != null) {
			try {
				String str = baseFun.readTxtFile(baseFun.getMediaPath(fileStr, mediaType.Text));
				if (rssAdrr != null && rssAdrr.length() > 8) {
					str = baseFun.httpGetStr(rssAdrr, 1);
				}

				if (str != null && str.compareTo(" ") != 0) {
					String logStr = baseFun.getTimeStr(1) + fileStr;
					baseFun.appendLogInfo(logStr, 1);
					baseFun.curTxt = fileStr;
				}
				content = str;
				Log.i(TAG, "MSG Content: " + content);
				if (content.length() > 256) {
					if (fontSize > 132)
						fontSize = 132;
				} else {
					if (fontSize > 256)
						fontSize = 256;
				}
				paint.setTypeface(Typeface.DEFAULT);
				paint.setTextSize(fontSize);
				paint.setColor(Color.parseColor(fontColor));

				if (isbgtext == 1) {
					if (bgColor != null)
						setBackgroundColor(Color.parseColor(bgColor));
					else
						setBackgroundColor(Color.parseColor("#000000"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void draw() {

		try {
			Canvas canvas = mSurfaceHolder.lockCanvas();
			if (mSurfaceHolder == null || canvas == null) {
				return;
			}
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

			if (this.width >= 300 && this.height >= 300) {// bulletin

				if (fontSize > 132)
					fontSize = 132;
				paint.setTextSize(fontSize);

				FontMetrics fm = paint.getFontMetrics();

				int iX = 4, iY = (int) (fontSize - fm.top - fm.bottom) / 2,
						m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + 4;

				if (m_String.isEmpty()) {

					char ch;
					int iW = 0, istart = 0;
					if (content == null || content.length() < 1) {
						content = "NULL";
					}
					for (int i = 0; i < content.length(); i++) {
						ch = content.charAt(i);
						float[] widths = new float[1];
						String srt = String.valueOf(ch);
						paint.getTextWidths(srt, widths);
						if (ch == '\n' || ch == '\r') {
							m_iRealLine++;
							m_String.addElement(content.substring(istart, i));
							istart = (i++);
							iW = m_iFontHeight / 2; // iW = 0;

						} else {
							iW += (int) (Math.ceil(widths[0]));
							if (iW > this.width) {
								m_iRealLine++;
								m_String.addElement(content.substring(istart, i));
								istart = (i--);
								iW = 0;
							} else {
								if (i == (content.length() - 1)) {
									m_iRealLine++;
									m_String.addElement(content.substring(istart, content.length()));
								}
							}
						}
					}
				}

				for (int i = 0, j = 0; i < m_iRealLine; i++, j++) {
					canvas.drawText((String) (m_String.elementAt(i)), iX, y + iY + m_iFontHeight * j, paint);
				}
				mSurfaceHolder.unlockCanvasAndPost(canvas);
				canvas = null;

				if (isMove) {
					float conlen = m_iFontHeight * (m_iRealLine + 1);

					if (y < -conlen) {
						m_String.clear();
						m_iRealLine = 0;
						y = height;
						sendMessage(FLAG);
						SystemClock.sleep(100);
					}
					y -= 2;
				}

			} else {

				if (this.width < this.height / 2) {// horizon

					FontMetrics fontMetrics = paint.getFontMetrics();

					if (content == null || content.length() < 1) {
						content = "NULL";
					}
					x = Math.abs(width - (Math.abs(fontMetrics.top) - Math.abs(fontMetrics.bottom))) / 2;

					canvas.rotate(90, x, y);
					canvas.drawText(content, x, y, paint);
					canvas.rotate(-90, x, y);
					mSurfaceHolder.unlockCanvasAndPost(canvas);
					canvas = null;
					if (isMove) {
						float conlen = paint.measureText(content);
						if (y < -conlen) {
							y = height;
							sendMessage(FLAG);
							SystemClock.sleep(100);
						}
						y -= 2;
					}
				} else {

					FontMetrics fontMetrics = paint.getFontMetrics();

					y = (height - fontMetrics.top - fontMetrics.bottom) / 2;

					if (content == null || content.length() < 1) {
						content = "NULL";
					}
					canvas.drawText(content, x, y, paint);
					mSurfaceHolder.unlockCanvasAndPost(canvas);
					canvas = null;
					if (isMove) {
						float conlen = paint.measureText(content);
						if (x < -conlen) {
							x = width;
							sendMessage(FLAG);
							SystemClock.sleep(100);
						}
						x -= 2;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			loop = false;
		}
	}

	@Override
	public void run() {

		SystemClock.sleep(700);
		while (loop) {
			synchronized (mSurfaceHolder) {
				draw();
			}
		}
	}
}
