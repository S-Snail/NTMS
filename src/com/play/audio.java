package com.play;

import com.ntms.MainActivity;
import com.ntms.baseFun;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;

public class audio {

	private static final String AUD_TAG = "audio";
	private int vol = -1;

	private MediaPlayer mediaPlayer;
	private OnCompletionListener onCompletionListener;
	private OnPreparedListener onPreparedListener;
	private OnErrorListener onErrorListener;

	private ErrorCountDownTimer errorCountDownTimer;
	private boolean errorFlag = true;

	@SuppressLint("HandlerLeak")
	public audio(int vol) {
		this.vol = vol;
	}

	public void sendMessage() {
		if (MainActivity.mHandler != null) {
			Message message = Message.obtain(MainActivity.mHandler, MainActivity.END_AUDIO);
			MainActivity.mHandler.sendMessage(message);
		}
	}

	public void setAudioSrc(String path) {

		if (errorCountDownTimer != null) {
			errorCountDownTimer.cancel();
			errorCountDownTimer = null;
		}
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(preparedListener);
		mediaPlayer.setOnCompletionListener(completionListener);
		mediaPlayer.setOnErrorListener(errorListener);

		errorFlag = true;
		if (path != null) {
			Log.i(AUD_TAG, "==========Audio: " + path + "=============");
			try {
				String logStr = baseFun.getTimeStr(1) + path.substring(path.lastIndexOf("/") + 1);
				baseFun.appendLogInfo(logStr, 1);
				baseFun.curAud = path.substring(path.lastIndexOf("/") + 1);// lastIndexOf返回一个指定字符产值最后出现的位置；
			} catch (Exception e) {
			}
			try {
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();
			} catch (Exception e) {
				errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
				e.printStackTrace();
			}
		}
	}

	public void stopPlay() {

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
	}

	public void setVolume(float leftVolume, float rightVolume) {
		if (mediaPlayer != null) {
			mediaPlayer.setVolume(leftVolume, rightVolume);
		}
	}

	private OnPreparedListener preparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			if (onPreparedListener != null) {
				onPreparedListener.onPrepared(mediaPlayer);
			}
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					if (vol > 0 && vol < 100) {
						setVolume(vol / 100, vol / 100);// 设置音量
					} else {
						setVolume(0.1f, 0.1f);
					}
					Log.i(AUD_TAG, "==========Set Volume===========");
				}
			});
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Log.i(AUD_TAG, "==========Audio End===========");
					sendMessage();
				}
			});
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.i(AUD_TAG, "========Audio Error===========");
					sendMessage();
					return false;
				}
			});
			mediaPlayer.start();
		}
	};

	private OnCompletionListener completionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (onCompletionListener != null) {
				onCompletionListener.onCompletion(mediaPlayer);
			}
		}
	};

	private OnErrorListener errorListener = new OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
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

	public void start() {
		mediaPlayer.start();
	}

	public void reset() {
		onPreparedListener = null;
		onCompletionListener = null;
		onErrorListener = null;
		errorFlag = true;
		if (errorCountDownTimer != null) {
			errorCountDownTimer.cancel();
			errorCountDownTimer = null;
		}
		mediaPlayer.reset();
	}

	class ErrorCountDownTimer extends CountDownTimer {
		private int framework_err;
		private int impl_err;

		public ErrorCountDownTimer(long millisInFuture, long countDownInterval, int framework_err, int impl_err) {
			super(millisInFuture, countDownInterval);// countDownInterval倒计时时间间隔
			this.framework_err = framework_err;
			this.impl_err = impl_err;
		}

		@Override
		public void onFinish() {
			errorCountDownTimer = null;
			if (onErrorListener != null) {
				onErrorListener.onError(mediaPlayer, framework_err, impl_err);// (MediaRecorder遇到错误，发生的错误类型，额外的代码（特定于错误类型）)
			}
		}

		@Override
		public void onTick(long arg0) {
		}
	}
}
