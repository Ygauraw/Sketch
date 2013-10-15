package com.abhan.example;

public class AbhanNativeFilter {
	static {
		System.loadLibrary("AbhanFilter");
	}
	
	public static native int[] sketchFilter(int[] pixels, int width, int height);
}