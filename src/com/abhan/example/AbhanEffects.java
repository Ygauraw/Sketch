package com.abhan.example;

import java.nio.IntBuffer;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;

public class AbhanEffects {
	public static Bitmap convertToGrayScale(final Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();
		Bitmap grayed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(grayed);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bmpOriginal, 0, 0, paint);
		return grayed;
	}
	
	public static Bitmap invertImageColors(Bitmap bmpGrayed) {
		Bitmap inverted = Bitmap.createBitmap(bmpGrayed.getWidth(), bmpGrayed.getHeight(),
				bmpGrayed.getConfig());
		int A, R, G, B;
		int pixelColor;
		int height = bmpGrayed.getHeight();
		int width = bmpGrayed.getWidth();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixelColor = bmpGrayed.getPixel(x, y);
				A = Color.alpha(pixelColor);
				R = 255 - Color.red(pixelColor);
				G = 255 - Color.green(pixelColor);
				B = 255 - Color.blue(pixelColor);
				inverted.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return inverted;
	}
	
	public static Bitmap applyGaussianBlur(Bitmap bmpInverted) {
		//To make more light replace 3 with 4 and 100 with 127
		double[][] gaussianBlurConfig = new double[][] { { -1, 0, -1 }, { 0, 3, 0 }, { -1, 2, -1 } };
		ConvolutionMatrix convlutionMatrix = new ConvolutionMatrix(3);
		convlutionMatrix.applyConfig(gaussianBlurConfig);
		convlutionMatrix.Factor = 1;
		convlutionMatrix.Offset = 100;
		return ConvolutionMatrix.computeConvolution3x3(bmpInverted, convlutionMatrix);
	}
	
	public static Bitmap colorDodgeBlend(Bitmap bmpGaussian, Bitmap bmpGrayed) {
		Bitmap base = bmpGaussian.copy(Bitmap.Config.ARGB_8888, true);
		Bitmap blend = bmpGrayed.copy(Bitmap.Config.ARGB_8888, false);
		IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
		base.copyPixelsToBuffer(buffBase);
		buffBase.rewind();
		IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
		blend.copyPixelsToBuffer(buffBlend);
		buffBlend.rewind();
		IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
		buffOut.rewind();
		while (buffOut.position() < buffOut.limit()) {
			int filterInt = buffBlend.get();
			int srcInt = buffBase.get();
			int redValueFilter = Color.red(filterInt);
			int greenValueFilter = Color.green(filterInt);
			int blueValueFilter = Color.blue(filterInt);
			int redValueSrc = Color.red(srcInt);
			int greenValueSrc = Color.green(srcInt);
			int blueValueSrc = Color.blue(srcInt);
			int redValueFinal = colorDodge(redValueFilter, redValueSrc);
			int greenValueFinal = colorDodge(greenValueFilter, greenValueSrc);
			int blueValueFinal = colorDodge(blueValueFilter, blueValueSrc);
			int pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal);
			float[] hsv = new float[3];
			Color.colorToHSV(pixel, hsv);
			hsv[1] = 0.0f;
			float top = 0.87f;
			if (hsv[2] <= top) {
				hsv[2] = 0.0f;
			} else {
				hsv[2] = 1.0f;
			}
			pixel = Color.HSVToColor(hsv);
			buffOut.put(pixel);
		}
		buffOut.rewind();
		base.copyPixelsFromBuffer(buffOut);
		blend.recycle();
		return base;
	}
	
	private static int colorDodge(int in1, int in2) {
		float image = in2;
		float mask = in1;
		return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));
	}
	
	public static Bitmap getCartoon(Bitmap realBitmap, Bitmap dodgeBlendBitmap,
			int hueIntervalSize, int saturationIntervalSize, int valueIntervalSize,
			int saturationPercent, int valuePercent) {
		Bitmap base = fastBlur(realBitmap, 3).copy(Bitmap.Config.ARGB_8888, true);
		Bitmap dodge = dodgeBlendBitmap.copy(Bitmap.Config.ARGB_8888, false);
		try {
			int realColor;
			int color;
			float top = 0.87f;
			IntBuffer templatePixels = IntBuffer.allocate(dodge.getWidth() * dodge.getHeight());
			IntBuffer scaledPixels = IntBuffer.allocate(base.getWidth() * base.getHeight());
			IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
			base.copyPixelsToBuffer(scaledPixels);
			dodge.copyPixelsToBuffer(templatePixels);
			templatePixels.rewind();
			scaledPixels.rewind();
			buffOut.rewind();
			while (buffOut.position() < buffOut.limit()) {
				color = (templatePixels.get());
				realColor = scaledPixels.get();
				float[] realHSV = new float[3];
				Color.colorToHSV(realColor, realHSV);
				realHSV[0] = getRoundedValue(realHSV[0], hueIntervalSize);
				realHSV[2] = (getRoundedValue(realHSV[2] * 100, valueIntervalSize) / 100)
						* (valuePercent / 100);
				realHSV[2] = realHSV[2] < 1.0 ? realHSV[2] : 1.0f;
				realHSV[1] = realHSV[1] * (saturationPercent / 100);
				realHSV[1] = realHSV[1] < 1.0 ? realHSV[1] : 1.0f;
				float[] HSV = new float[3];
				Color.colorToHSV(color, HSV);
				boolean putBlackPixel = HSV[2] <= top;
				realColor = Color.HSVToColor(realHSV);
				if (putBlackPixel) {
					buffOut.put(color);
				} else {
					buffOut.put(realColor);
				}
			}
			dodge.recycle();
			buffOut.rewind();
			base.copyPixelsFromBuffer(buffOut);
		} catch (Exception e) {}
		return base;
	}

	private static Bitmap fastBlur(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		if (radius < 1) { return (null); }
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}
	
	private static float getRoundedValue(float value, int intervalSize) {
		float result = Math.round(value);
		int mod = ((int) result) % intervalSize;
		result += mod < (intervalSize / 2) ? -mod : intervalSize - mod;
		return result;
	}
	
	public static Bitmap highlightImage(Bitmap passedBitmap) {
		Bitmap highlightedBitmap = Bitmap.createBitmap(passedBitmap.getWidth() + 96,
				passedBitmap.getHeight() + 96, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(highlightedBitmap);
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		Paint ptBlur = new Paint();
		ptBlur.setMaskFilter(new BlurMaskFilter(15, Blur.NORMAL));
		int[] offsetXY = new int[2];
		Bitmap bmAlpha = passedBitmap.extractAlpha(ptBlur, offsetXY);
		Paint ptAlphaColor = new Paint();
		ptAlphaColor.setColor(0xFFFFFFFF);
		canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
		bmAlpha.recycle();
		canvas.drawBitmap(passedBitmap, 0, 0, null);
		return highlightedBitmap;
	}
	
	public static Bitmap gammaCorrection(Bitmap passedBitmap, double red, double green, double blue) {
		Bitmap gammaCorrectedBitmap = Bitmap.createBitmap(passedBitmap.getWidth(),
				passedBitmap.getHeight(), passedBitmap.getConfig());
		int width = passedBitmap.getWidth();
		int height = passedBitmap.getHeight();
		int A, R, G, B;
		int pixel;
		final int MAX_SIZE = 256;
		final double MAX_VALUE_DBL = 255.0;
		final int MAX_VALUE_INT = 255;
		final double REVERSE = 1.0;
		int[] gammaR = new int[MAX_SIZE];
		int[] gammaG = new int[MAX_SIZE];
		int[] gammaB = new int[MAX_SIZE];
		for (int i = 0; i < MAX_SIZE; ++i) {
			gammaR[i] = Math.min(MAX_VALUE_INT,
					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / red)) + 0.5));
			gammaG[i] = Math.min(MAX_VALUE_INT,
					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / green)) + 0.5));
			gammaB[i] = Math.min(MAX_VALUE_INT,
					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / blue)) + 0.5));
		}
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = passedBitmap.getPixel(x, y);
				A = Color.alpha(pixel);
				R = gammaR[Color.red(pixel)];
				G = gammaG[Color.green(pixel)];
				B = gammaB[Color.blue(pixel)];
				gammaCorrectedBitmap.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return gammaCorrectedBitmap;
	}
	
	public static Bitmap sharpenBitmap(Bitmap passedBitmap, double weight) {
		double[][] SharpConfig = new double[][] { { 0, -2, 0 }, { -2, weight, -2 }, { 0, -2, 0 } };
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(SharpConfig);
		convMatrix.Factor = weight - 8;
		return ConvolutionMatrix.computeConvolution3x3(passedBitmap, convMatrix);
	}
	
	public static Bitmap smoothBitmap(Bitmap passedBitmap, double value) {
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.setAll(1);
		convMatrix.Matrix[1][1] = value;
		convMatrix.Factor = value + 8;
		convMatrix.Offset = 1;
		return ConvolutionMatrix.computeConvolution3x3(passedBitmap, convMatrix);
	}
	
	public static Bitmap embossedBitmap(Bitmap passedBitmap) {
		double[][] EmbossConfig = new double[][] { { -1, 0, -1 }, { 0, 4, 0 }, { -1, 0, -1 } };
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(EmbossConfig);
		convMatrix.Factor = 1;
		convMatrix.Offset = 127;
		return ConvolutionMatrix.computeConvolution3x3(passedBitmap, convMatrix);
	}
	
	public static Bitmap engravedBitmap(Bitmap passedBitmap) {
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.setAll(0);
		convMatrix.Matrix[0][0] = -2;
		convMatrix.Matrix[1][1] = 2;
		convMatrix.Factor = 1;
		convMatrix.Offset = 95;
		return ConvolutionMatrix.computeConvolution3x3(passedBitmap, convMatrix);
	}
	
	public static Bitmap removedMeanBitmap(Bitmap passedBitmap) {
		double[][] MeanRemovalConfig = new double[][] { { -1, -1, -1 }, { -1, 9, -1 },
				{ -1, -1, -1 } };
		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
		convMatrix.applyConfig(MeanRemovalConfig);
		convMatrix.Factor = 1;
		convMatrix.Offset = 0;
		return ConvolutionMatrix.computeConvolution3x3(passedBitmap, convMatrix);
	}
	
	public static Bitmap filteredColorBitmap(Bitmap passedBitmap, double red, double green,
			double blue) {
		int width = passedBitmap.getWidth();
		int height = passedBitmap.getHeight();
		Bitmap abhan = Bitmap.createBitmap(width, height, passedBitmap.getConfig());
		int A, R, G, B;
		int pixel;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = passedBitmap.getPixel(x, y);
				A = Color.alpha(pixel);
				R = (int) (Color.red(pixel) * red);
				G = (int) (Color.green(pixel) * green);
				B = (int) (Color.blue(pixel) * blue);
				abhan.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return abhan;
	}
	
	public static Bitmap contrastedBitmap(Bitmap passedBitmap, double value) {
		int width = passedBitmap.getWidth();
		int height = passedBitmap.getHeight();
		Bitmap abhan = Bitmap.createBitmap(width, height, passedBitmap.getConfig());
		int A, R, G, B;
		int pixel;
		double contrast = Math.pow((100 + value) / 100, 2);
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = passedBitmap.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
				if (R < 0) {
					R = 0;
				} else if (R > 255) {
					R = 255;
				}
				G = Color.red(pixel);
				G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
				if (G < 0) {
					G = 0;
				} else if (G > 255) {
					G = 255;
				}
				B = Color.red(pixel);
				B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
				if (B < 0) {
					B = 0;
				} else if (B > 255) {
					B = 255;
				}
				abhan.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return abhan;
	}
	
	public static Bitmap sepiaTonnedBitmap(Bitmap passedBitmap, int depth, double red,
			double green, double blue) {
		int width = passedBitmap.getWidth();
		int height = passedBitmap.getHeight();
		Bitmap abhan = Bitmap.createBitmap(width, height, passedBitmap.getConfig());
		final double GS_RED = 0.3;
		final double GS_GREEN = 0.59;
		final double GS_BLUE = 0.11;
		int A, R, G, B;
		int pixel;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = passedBitmap.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);
				B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
				R += (depth * red);
				if (R > 255) {
					R = 255;
				}
				G += (depth * green);
				if (G > 255) {
					G = 255;
				}
				B += (depth * blue);
				if (B > 255) {
					B = 255;
				}
				abhan.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return abhan;
	}
}