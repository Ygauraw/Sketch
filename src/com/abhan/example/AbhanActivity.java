package com.abhan.example;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class AbhanActivity extends Activity {
	private static final boolean DEBUG = true;
	private static final String TAG = "Abhan";
	private static final String FILE_SEPERATOR = "/";
	private static final String FILE_NAME = FILE_SEPERATOR + "abhan.jpeg";
	private ImageView mImageView = null;
	private int srcWidth = -1, srcHeight = -1;
	private final int CAPTURE_CODE = 103, SELECT_CODE = 102;
	private Uri mOutPutUri = null;
	private String mPath = null;
	private boolean isImage = false;
	private Bitmap abhanBitmap = null, initBitmap = null;
	private Button btnCreateSketch = null;
	
	public Bitmap getRoundedBitmap(Bitmap bitmap) {
		final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
		final int color = Color.RED;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		bitmap.recycle();
		return output;
	}

	public void captureImage(final View view) {
		reset();
		if (mPath != null && mPath.length() > 0) {
			mPath = mPath + FILE_SEPERATOR + FILE_NAME;
			mOutPutUri = Uri.fromFile(new File(mPath));
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mOutPutUri);
			startActivityForResult(intent, CAPTURE_CODE);
		}
	}
	
	public void chooseImage(final View view) {
		reset();
		if (mPath != null && mPath.length() > 0) {
			Intent intent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			//			intent.setType("image/*");
			startActivityForResult(intent, SELECT_CODE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_abhan);
		mImageView = (ImageView) findViewById(R.id.image);
		btnCreateSketch = (Button) findViewById(R.id.button);
		mPath = Utils.getDataPath(TAG);
		initBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.android1);
		reset();
		disableButton();
		btnCreateSketch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (abhanBitmap != null && isImage) {
					disableButton();
					new ImageProcessingTask().execute(abhanBitmap);
				}
			}
		});
	}
	
	private void reset() {
		isImage = false;
		if (initBitmap != null) {
			final int shadowSize = getResources().getDimensionPixelSize(R.dimen.shadow_size);
			final int shadowColor = getResources().getColor(R.color.shadow_color);
			mImageView.setImageDrawable(new RoundedAvatarDrawable(initBitmap, shadowSize,
					shadowColor));
		}
	}
	
	private void enableButton() {
		btnCreateSketch.setEnabled(true);
		btnCreateSketch.setBackgroundColor(getResources().getColor(R.color.darkslate));
		btnCreateSketch.setTextColor(getResources().getColor(R.color.white));
	}
	
	private void disableButton() {
		btnCreateSketch.setEnabled(false);
		btnCreateSketch.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
		btnCreateSketch.setTextColor(getResources().getColor(R.color.white));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*Uri finalUri = null;*/
		if (resultCode == Activity.RESULT_OK) {
			/*if (data != null) {
				finalUri = data.getData();
				if (DEBUG) {
					android.util.Log.i(TAG, "Content " + finalUri);
				}
			} else {
				finalUri = mOutPutUri;
				if (DEBUG) {
					android.util.Log.i(TAG, "File " + finalUri);
				}
			}
			if (finalUri != null) {
				getRealPath(finalUri);
			}*/
			if (requestCode == CAPTURE_CODE) {
				if (DEBUG) {
					android.util.Log.i(TAG, "Capture");
				}
				getOrientation(mPath);
			} else if (requestCode == SELECT_CODE) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					final String filePath = cursor.getString(columnIndex);
					getOrientation(filePath);
				}
				cursor.close();
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			isImage = false;
			if (DEBUG) {
				android.util.Log.w(TAG, "RESULT_CANCELED");
			}
		}
	}
	
	/*private String getRealPath(final Uri passedUri) {
		String realPath = null;
		if (passedUri.toString().startsWith("file:")) {
			File mFile = new File(passedUri.toString());
			realPath = mFile.getAbsolutePath();
		} else if (passedUri.toString().startsWith(
				"content:")) {
			String[] projections = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(passedUri,
					projections, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				realPath = cursor.getString(columnIndex);
			}
			cursor.close();
		}
		if (DEBUG) {
			android.util.Log.w(TAG, "realPath " + realPath);
		}
		return realPath;
	}*/

	private void getOrientation(final String filePath) {
		int rotation = -1;
		ExifInterface exif;
		long fileSize = new File(filePath).length();
		if (DEBUG) {
			android.util.Log.i(TAG, "fileSize " + fileSize + " filePath: " + filePath);
		}
		try {
			exif = new ExifInterface(filePath);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (exifOrientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotation = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotation = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotation = 270;
					break;
				case ExifInterface.ORIENTATION_NORMAL:
				case ExifInterface.ORIENTATION_UNDEFINED:
					rotation = 0;
					break;
			}
			if (DEBUG) {
				android.util.Log.i(TAG, "Exif:rotation " + rotation);
			}
			if (rotation != -1) {
				processImage(filePath, rotation);
			} else {
				Cursor mediaCursor = getContentResolver().query(mOutPutUri,
						new String[] { MediaStore.Images.ImageColumns.ORIENTATION,
								MediaStore.MediaColumns.SIZE },
						null, null, null);
				if (mediaCursor != null && mediaCursor.getCount() != 0) {
					while (mediaCursor.moveToNext()) {
						long size = mediaCursor.getLong(1);
						if (DEBUG) {
							android.util.Log.i(TAG, "Media:size " + size);
						}
						if (size == fileSize) {
							rotation = mediaCursor.getInt(0);
							break;
						}
					}
					if (DEBUG) {
						android.util.Log.i(TAG, "Media:rotation " + rotation);
					}
					processImage(filePath, rotation);
				}
			}
		} catch (IOException exception) {
			isImage = false;
			exception.printStackTrace();
		}
	}

	private void processImage(final String filePath, final int rotation) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		options.inPurgeable = true;
		abhanBitmap = BitmapFactory.decodeFile(filePath, options);
		if (abhanBitmap != null) {
			srcWidth = abhanBitmap.getWidth();
			srcHeight = abhanBitmap.getHeight();
			Matrix matrix = new Matrix();
			matrix.postRotate(rotation);
			abhanBitmap = Bitmap
					.createBitmap(abhanBitmap, 0, 0, srcWidth, srcHeight, matrix, false);
			abhanBitmap = abhanBitmap.copy(Bitmap.Config.ARGB_8888, true);
			if (DEBUG) {
				android.util.Log.i(TAG, "srcWidth " + srcWidth);
				android.util.Log.i(TAG, "srcHeight " + srcHeight);
			}
			if (srcWidth > 0 && srcHeight > 0) {
				mImageView.setImageBitmap(abhanBitmap);
				isImage = true;
				enableButton();
			}
		}
	}

	private class ImageProcessingTask extends AsyncTask<Bitmap, Void, Bitmap> {
		private ProgressDialog abhanDialog = null;
		private Bitmap returnedBitmap = null;
		
		@Override
		protected void onPreExecute() {
			returnedBitmap = null;
			abhanDialog = new ProgressDialog(AbhanActivity.this);
			abhanDialog.setMessage(getString(R.string.please_wait));
			abhanDialog.setCancelable(false);
			abhanDialog.show();
		}
		
		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			final Bitmap sketched = AbhanSketch.createSketch(params[0]);
			final Bitmap gaussianBitmap = AbhanEffects.applyGaussianBlur(sketched);
			final Bitmap sepiaBitmap = AbhanEffects.sepiaTonnedBitmap(gaussianBitmap, 151, 0.71,
					0.71, 0.76);
			returnedBitmap = AbhanEffects.sharpenBitmap(sepiaBitmap, 0.81);
			return returnedBitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (abhanDialog != null && abhanDialog.isShowing()) {
				abhanDialog.cancel();
			}
			if (result != null) {
				mImageView.setImageBitmap(result);
				isImage = false;
				enableButton();
				final boolean isFileDeleted = Utils.deleteFile(mPath);
				if (DEBUG) {
					android.util.Log.i(TAG, "File Deleted: " + isFileDeleted);
				}
			}
		}
	}
}