package com.play;

import java.util.ArrayList;

import com.list.funs;
import com.list.list.ProgramList;
import com.list.sch;
import com.ntms.baseFun;
import com.ntms.baseFun.mediaType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

@SuppressLint("DefaultLocale")
public class video extends SurfaceView {

	private static final String TAG = "Video";
	private static final int SET_SRC = 0;

	private Uri uri = null;
	private Context content = null;
	private MediaPlayer mediaPlayer = null;

	private OnCompletionListener onCompletionListener = null;
	private OnPreparedListener onPreparedListener = null;
	private OnErrorListener onErrorListener = null;
	private SurfaceHolder surfaceHolder = null;
	private img2 picview = null;

	private boolean errorFlag = true;
	private long videoBeginTime = 0;
	private long videoDuration = 0;
	private int videoWidth = 0, videoHeight = 0;
	private int msgThreadRuning = 0;
	private int videoErrCnt = 0;
	private int lstIdx = 0;
	private int exit = 0;
	private int vidIdx = 0;
	private int dur = 0;
	private int vol = -1;

	private ErrorCountDownTimer errorCountDownTimer = null;
	private Handler mHandler = null;
	ArrayList<ProgramList> pList = null;

	private long logDur = 0;
	private String logName = null;

	chkVideoStatus thd = new chkVideoStatus();

	@SuppressLint("HandlerLeak")
	public video(Context context, int width, int height, int left, int top, String listStr, img2 picview, String id,
			int vol) {
		super(context);
		this.content = context;
		this.picview = picview;
		this.exit = 0;

		this.setZOrderMediaOverlay(true);// 将其覆盖在其他媒体之上

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SET_SRC:
					setVideoSrc(1);
					break;
				default:
					break;
				}
			}
		};
		if (width * height == 1920 * 1080 || width * height == 1280 * 720 || width * height == 1366 * 768) {
			baseFun.fullTag = 1;
		}
		if (listStr != null)
			pList = funs.parseList(listStr);

		initVideoView();

		thd.start();
	}

	class chkVideoStatus extends Thread {

		public void run() {
			while (exit != 1) {
				try {
					if (videoBeginTime != 0 && videoDuration > 10 * 1000) {
						long expireTime = SystemClock.elapsedRealtime() - videoBeginTime;
						if (expireTime > videoDuration + 120 * 1000) {
							if ((baseFun.vErrCnt++) > 3) {
								baseFun.appendLogInfo("video player maybe died,reboot", 4);
								baseFun.appendLogInfo(null, -1);
								baseFun.rebootnow(content);
								baseFun.runCmd("reboot");
							} else {
								baseFun.appendLogInfo("video player maybe died,reload playlist again", 4);
								baseFun.appendLogInfo(null, -1);
								sch.reload = 1;
							}
						}
					}
				} catch (Exception e) {

				}
				SystemClock.sleep(3000);
			}
			Log.i("video", ">>>>Monitor status thread exit!");
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

	@SuppressLint("DefaultLocale")
	public void setVideoSrc(int mode) {

		if (pList == null || this.exit == 1) {
			return;
		} else {
			int idx = chkCurLst();
			if (idx != lstIdx) {
				lstIdx = idx;
				vidIdx = 0;
			}
			if (vidIdx >= pList.get(lstIdx).lst.size() || vidIdx < 0)
				vidIdx = 0;
		}
		try {
			String filePath = null;
			String fileStr = pList.get(lstIdx).lst.get(vidIdx).Name;
			if (fileStr == null) {
				SystemClock.sleep(500);
				sendMessage(SET_SRC);
				return;
			}

			if (fileStr.toLowerCase().endsWith(".jpeg") || fileStr.toLowerCase().endsWith(".jpg")
					|| fileStr.toLowerCase().endsWith(".png") || fileStr.toLowerCase().endsWith(".bmp")) {
				int sDur = pList.get(lstIdx).lst.get(vidIdx).PlayTime * 1000;
				if (sDur < 4000) {
					sDur = 4000;
				}
				this.dur = sDur;
				if (picview != null) {
					Log.i(TAG, ">>>>>>>>Mix Image[" + (vidIdx - 1) + "][" + dur + "] " + fileStr + "\n");
					filePath = baseFun.getMediaPath(pList.get(lstIdx).lst.get(vidIdx).Name, mediaType.Image);

					if (filePath == null || baseFun.checkFile(filePath) == false) {
						SystemClock.sleep(500);
						sendMessage(SET_SRC);
					} else {
						String logStr = baseFun.getTimeStr(1) + pList.get(lstIdx).lst.get(vidIdx).Name;
						baseFun.appendLogInfo(logStr, 1);
						baseFun.curImg = pList.get(lstIdx).lst.get(vidIdx).Name;
					}

					logName = pList.get(lstIdx).lst.get(vidIdx).Name;
					logDur = baseFun.getTickCount();

					if (picview.showImage(filePath) == 1) {
						if (this.getVisibility() == View.VISIBLE) {
							this.setVisibility(View.INVISIBLE);
						}
					} else {
						this.dur = 1000;
					}
				}
				if (filePath != null && baseFun.checkFile(filePath)) {
					setSrcThrd thd = new setSrcThrd();
					thd.start();
				}
			} else {
				int sDur = pList.get(lstIdx).lst.get(vidIdx).PlayTime * 1000;
				Log.i(TAG, ">>>>>>>>Mix Video[" + (vidIdx - 1) + "][" + sDur + "] " + fileStr + "\n");
				filePath = baseFun.getMediaPath(pList.get(lstIdx).lst.get(vidIdx).Name, mediaType.Video);

				if (filePath == null || baseFun.checkFile(filePath) == false) {
					if (vidIdx >= pList.get(lstIdx).lst.size()) {
						filePath = baseFun.getMediaPath(pList.get(lstIdx).lst.get(vidIdx).Name, mediaType.Video);
						if (filePath == null || baseFun.checkFile(filePath) == false) {
							SystemClock.sleep(500);
							sendMessage(SET_SRC);
						}
					} else {
						SystemClock.sleep(500);
						sendMessage(SET_SRC);
					}
				}
				String logStr = baseFun.getTimeStr(1) + pList.get(lstIdx).lst.get(vidIdx).Name;
				baseFun.appendLogInfo(logStr, 1);
				baseFun.curVid = pList.get(lstIdx).lst.get(vidIdx).Name;

				logName = pList.get(lstIdx).lst.get(vidIdx).Name;
				logDur = baseFun.getTickCount();

				this.vol = pList.get(lstIdx).lst.get(vidIdx).Volume;
				this.uri = Uri.parse(filePath);
				if (this.getVisibility() == View.INVISIBLE) {
					this.setVisibility(View.VISIBLE);
				}
				vidIdx++;
				openVideo();
				requestLayout();

				if (picview != null) { // 20150928
					picview.hideImg(0);
				}
				invalidate();// 废止
			}

		} catch (Exception e) {

		}
	}

	public void sendMessage(int id) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	public video(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.content = context;
		initVideoView();
	}

	public video(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.content = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(videoWidth, widthMeasureSpec);
		int height = getDefaultSize(videoHeight, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	public String getUri() {
		return uri.getPath();
	}

	@SuppressWarnings("deprecation")
	private void initVideoView() {
		videoWidth = 0;
		videoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		requestFocus();
		// getHolder().setFormat("#100010");//PixelFormat.TRANSLUCENT);
	}

	public void setVideoPath(String path) {

		if (path != null) {
			this.uri = Uri.parse(path);
			openVideo();
			requestLayout();
			invalidate();
		}
	}

	class setSrcThrd extends Thread {

		public void run() {
			if (msgThreadRuning == 0) {
				msgThreadRuning = 1;
				if (picview != null && dur > 2000) {
					if (logName != null && logDur > 1) {
						baseFun.appendPlayDur(logName, (int) (baseFun.getTickCount() - logDur));
						logName = null;
						logDur = 0;
					}
					picview.imgSleep(dur);
				}
				msgThreadRuning = 0;
				sendMessage(SET_SRC);
			}
		}
	}

	@SuppressLint("NewApi")
	private void openVideo() {

		if (uri == null || surfaceHolder == null) {
			return;
		}
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		content.sendBroadcast(i);

		errorFlag = true;
		if (errorCountDownTimer != null) {// 错误倒计时
			errorCountDownTimer.cancel();
			errorCountDownTimer = null;
		}
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnPreparedListener(preparedListener);
			mediaPlayer.setOnCompletionListener(completionListener);
			mediaPlayer.setOnErrorListener(errorListener);
			mediaPlayer.setDataSource(content, uri);
			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.setScreenOnWhilePlaying(true);
			mediaPlayer.prepareAsync();

		} catch (Exception e) {
			errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	public void setVolume(float leftVolume, float rightVolume) {
		if (mediaPlayer != null) {
			mediaPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	private OnPreparedListener preparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			videoWidth = mp.getVideoWidth();
			videoHeight = mp.getVideoHeight();

			if (videoWidth != 0 && videoHeight != 0) {
				getHolder().setFixedSize(videoWidth, videoHeight);
			}
			if (vol > 0 && vol < 100) {
				setVolume(vol / 100, vol / 100);
			} else {
				setVolume(0.1f, 0.1f);
			}
			start();
			if (onPreparedListener != null) {
				onPreparedListener.onPrepared(mediaPlayer);
			}
		}
	};

	private OnCompletionListener completionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			stopPlayback();

			if (logName != null && logDur > 1) {
				baseFun.appendPlayDur(logName, (int) (baseFun.getTickCount() - logDur));
				logName = null;
				logDur = 0;
			}

			videoErrCnt = 0;
			Log.i(TAG, "===========VIDEO END==============\n");
			setVideoSrc(1);
			baseFun.vErrCnt = 0;
			videoDuration = 0;
			if (onCompletionListener != null) {
				onCompletionListener.onCompletion(mediaPlayer);
			}
		}
	};

	private OnErrorListener errorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			stopPlayback();
			if ((videoErrCnt++) > 32) {
				baseFun.appendLogInfo("video player maybe exception,reload again", 4);
				baseFun.appendLogInfo(null, -1);
				videoErrCnt = 0;
				sch.reload = 1;
			}
			Log.i(TAG, "===========VIDEO ERR[" + videoErrCnt + "]==============\n");
			try {
				SystemClock.sleep(1000);
			} catch (Exception e) {
			}
			setVideoSrc(1);
			if (onErrorListener != null) {
				if (errorFlag == true) {
					errorFlag = false;
					errorCountDownTimer = new ErrorCountDownTimer(1 * 1000, 1000, framework_err, impl_err);
					errorCountDownTimer.start();
				}
			}
			return true;
		}
	};

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		}

		public void surfaceCreated(SurfaceHolder holder) {
			surfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			surfaceHolder = null;
			stopPlayback();
		}
	};

	private void start() {
		if (mediaPlayer != null) {

			mediaPlayer.start();
			videoDuration = mediaPlayer.getDuration();
			videoBeginTime = SystemClock.elapsedRealtime();

			// Log.i("ntms-Play","==Video Dur["+duration+"]==");
		} else {
			videoBeginTime = 0;
			videoDuration = 0;
		}
	}

	public void stopPlay(int mode) {
		if (mode == 0) {
			exit = 1;
		}
		stopPlayback();
	}

	private void stopPlayback() {
		errorFlag = true;
		if (errorCountDownTimer != null) {
			errorCountDownTimer.cancel();
			errorCountDownTimer = null;
		}
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		videoDuration = videoBeginTime = 0;
	}

	class ErrorCountDownTimer extends CountDownTimer {
		private int framework_err;
		private int impl_err;

		public ErrorCountDownTimer(long millisInFuture, long countDownInterval, int framework_err, int impl_err) {
			super(millisInFuture, countDownInterval);
			this.framework_err = framework_err;
			this.impl_err = impl_err;
		}

		@Override
		public void onFinish() {
			errorCountDownTimer = null;
			if (onErrorListener != null) {
				onErrorListener.onError(mediaPlayer, framework_err, impl_err);
			}
		}

		@Override
		public void onTick(long arg0) {
		}
	}
}
