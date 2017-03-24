package com.play;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.ntms.baseFun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class img2 extends SurfaceView implements SurfaceHolder.Callback, Runnable { // mix

	private static final String IMG_TAG = "img";
	private static final int SHOW_IMG = 0;
	private static final int DIS_VIS = 1;
	private static final int ENB_VIS = 2;

	public int exit = 0, fst = 0, clear = 0;

	private SurfaceHolder mSurfaceHolder = null;
	private Handler mHandler = null;
	private Paint paint = new Paint();
	private Matrix matrix = new Matrix();
	private Options opt = new Options();
	private Rect rt = null;
	private String fileName = null;

	public img2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("HandlerLeak")
	public img2(Context context, int w, int h, int left, int top, String id) {
		super(context);

		this.exit = 0;
		rt = new Rect(0, 0, w, h);
		if (mSurfaceHolder == null) {
			mSurfaceHolder = this.getHolder();
			mSurfaceHolder.addCallback(this);
		}
		this.setZOrderMediaOverlay(true);

		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW_IMG:
					break;

				case DIS_VIS:// 20150731

					SystemClock.sleep(500);
					setImgVisibility(0);
					break;

				case ENB_VIS:// 20150731

					setImgVisibility(1);
					break;

				default:
					break;
				}
			}
		};
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		this.exit = 0;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		this.exit = 0;
		new Thread(this).start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		this.exit = 1;
	}

	public void sendMessage(int id) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler, id);
			mHandler.sendMessage(message);
		}
	}

	public void imgSleep(int dur) {
		try {
			long startT = baseFun.getTickCount();
			this.exit = 0;
			while (this.exit != 1) {
				long nowT = baseFun.getTickCount();
				if (Math.abs(nowT - startT) >= dur || this.exit == 1) {
					return;
				} else {
					if (Math.abs(nowT - startT) >= dur - 200) {
						SystemClock.sleep(dur - (nowT - startT));
						return;
					} else {
						SystemClock.sleep(200);
					}
				}
			}
		} catch (Exception e) {

		}
	}

	public void stopPlay() {
		this.exit = 1;
	}

	private Bitmap getImageBmap(String filePath) {
		if (filePath == null) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			Log.i(IMG_TAG, ">>>>>>>Load Mix Image " + filePath);
			bitmap = getBitmap(filePath);
			if (bitmap != null && rt != null) {
				bitmap = getScaleBitmap(bitmap, rt.width(), rt.height());
			}
		} catch (Exception e) {
			Log.e(IMG_TAG, "getNextImage " + e.getMessage());
		}
		return bitmap;
	}

	private Bitmap getBitmap(String path) {

		Bitmap bmp = null;
		FileInputStream fs = null;

		if (opt == null)
			opt = new Options();

		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inSampleSize = 1;

		if (path == null || baseFun.checkFile(path) == false) {
			return null;
		}
		try {
			fs = new FileInputStream(path);
			if (fs != null) {
				try {
					bmp = BitmapFactory.decodeStream(fs, null, opt);
				} catch (OutOfMemoryError e) {
					Log.e(IMG_TAG, "getBitmap " + e.getMessage());
				} finally {
					try {
						fs.close();
					} catch (IOException e) {
						Log.e(IMG_TAG, "getBitmap " + e.getMessage());
					}
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(IMG_TAG, "getBitmap " + e.getMessage());
		}
		return bmp;
	}

	private Bitmap getScaleBitmap(Bitmap bitmap, int w, int h) {

		int width = bitmap.getWidth();
		int hight = bitmap.getHeight();

		if (w == width && h == hight) {
			return bitmap;
		}
		float wScale = ((float) w / width);
		float hScale = ((float) h / hight);

		matrix.reset();
		matrix.postScale(wScale, hScale);

		return Bitmap.createBitmap(bitmap, 0, 0, width, hight, matrix, true);
	}

	private int imageNoTrans(Canvas mCanvas, Bitmap bitmap, Rect rt, int arg) {

		mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		paint.reset();

		mCanvas.drawBitmap(bitmap, 0, 0, paint);

		return 0;
	}

	public void setImgVisibility(int mode) {// 20150731

		if (mode == 0) {
			clear = 1; // clearImage();
		} else {

		}
	}

	public void hideImg(int mode) {
		sendMessage(mode == 0 ? DIS_VIS : ENB_VIS);
	}

	@Override
	public void run() {

		while (this.exit != 1) {

			SystemClock.sleep(100);

			// ------------------------------------------------------------------------------------------------------
			if (fst == 0 && this.fileName != null) {
				fst = 1; // showImage(this.fileName);
				int iExcep = 0;
				if (this.fileName != null) {
					if (paint == null)
						paint = new Paint();
					if (matrix == null)
						matrix = new Matrix();

					Bitmap bitmap = getImageBmap(this.fileName);

					if (bitmap != null && mSurfaceHolder != null) {
						Canvas mCanvas = null;
						if (bitmap != null && mSurfaceHolder != null) {
							synchronized (mSurfaceHolder) {
								try {
									mCanvas = mSurfaceHolder.lockCanvas();
									if (mCanvas != null) {
										imageNoTrans(mCanvas, bitmap, rt, 0);
									}
									if (mCanvas != null) {
										mSurfaceHolder.unlockCanvasAndPost(mCanvas);
									}
								} catch (Exception e) {
									iExcep = 1;
									if (mCanvas != null) {
										mSurfaceHolder.unlockCanvasAndPost(mCanvas);
									}
									Log.e(IMG_TAG, "showImage " + e.getMessage());
								}
							}
						}
						if (bitmap != null) {
							if (!bitmap.isRecycled()) {
								bitmap.recycle();
								bitmap = null; // System.gc();
							}
							bitmap = null;
						}
						if (iExcep == 1) {

						}
					}
				}
			}
			// ------------------------------------------------------------------------------------------------------
			if (clear == 1) {// clear
				clear = 0;
				if (mSurfaceHolder != null) {
					Canvas mCanvas = null;
					synchronized (mSurfaceHolder) {
						try {
							mCanvas = mSurfaceHolder.lockCanvas();
							if (mCanvas != null) {
								mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
								mSurfaceHolder.unlockCanvasAndPost(mCanvas);
							}
						} catch (Exception e) {
							if (mCanvas != null) {
								mSurfaceHolder.unlockCanvasAndPost(mCanvas);
							}
						}
					}
				}
			}
			// ------------------------------------------------------------------------------------------------------
		}
	}

	public int showImage(String fileName) {

		int iExcep = 0;
		if (paint == null)
			paint = new Paint();
		if (matrix == null)
			matrix = new Matrix();

		this.fileName = fileName;
		Bitmap bitmap = getImageBmap(fileName);

		if (bitmap == null || mSurfaceHolder == null) {
			return 0;
		}
		Canvas mCanvas = null;

		if (bitmap != null && mSurfaceHolder != null) {
			synchronized (mSurfaceHolder) {
				try {
					mCanvas = mSurfaceHolder.lockCanvas();
					if (mCanvas != null) {
						imageNoTrans(mCanvas, bitmap, rt, 0);
					}
					if (mCanvas != null) {
						mSurfaceHolder.unlockCanvasAndPost(mCanvas);
					}
				} catch (Exception e) {
					iExcep = 1;
					if (mCanvas != null) {
						mSurfaceHolder.unlockCanvasAndPost(mCanvas);
					}
					Log.e(IMG_TAG, "showImage " + e.getMessage());
				}
			}
		}
		if (bitmap != null) {
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null; // System.gc();
			}
			bitmap = null;
		}
		if (iExcep == 1) {
			return 0;
		}
		return 1;
	}
}
