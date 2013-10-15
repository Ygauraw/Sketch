package com.abhan.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.os.Environment;

public class Utils {
	static String PNG = "png";
	static String JPG = "jpg";
	static String JPEG = "jpeg";
	static String PDF = "pdf";
	static String GIF = "gif";
	static String TXT = "txt";
	static String BMP = "bmp";
	
	public static boolean isSDCardMounted() {
		boolean isMounted = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			isMounted = true;
		} else if (Environment.MEDIA_BAD_REMOVAL.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_CHECKING.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_NOFS.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_REMOVED.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_UNMOUNTABLE.equals(state)) {
			isMounted = false;
		} else if (Environment.MEDIA_UNMOUNTED.equals(state)) {
			isMounted = false;
		}
		return isMounted;
	}
	
	public static boolean isDirectoryExists(final String filePath) {
		boolean isDirectoryExists = false;
		File mFilePath = new File(filePath);
		if (mFilePath.exists()) {
			isDirectoryExists = true;
		} else {
			isDirectoryExists = mFilePath.mkdirs();
		}
		return isDirectoryExists;
	}
	
	public static boolean directoryCreated(final String directoryName) {
		boolean isDirectoryCreated = false;
		if (isSDCardMounted()) {
			final File createdDirectory = new File(directoryName);
			if (!createdDirectory.exists()) {
				isDirectoryCreated = createdDirectory.mkdirs();
			} else {
				isDirectoryCreated = true;
			}
		} else {
			isDirectoryCreated = false;
		}
		return isDirectoryCreated;
	}

	public static String getDataPath(final String mDirName) {
		String returnedPath = null;
		if (isSDCardMounted()) {
			final String mSDCardDirPath = Environment.getExternalStorageDirectory() + "/"
					+ mDirName;
			if (isDirectoryExists(mSDCardDirPath)) { return mSDCardDirPath; }
		}
		return returnedPath;
	}
	
	public static boolean deleteFile(final String filePath) {
		boolean isFileExists = false;
		File mFilePath = new File(filePath);
		if (mFilePath.exists()) {
			mFilePath.delete();
			isFileExists = true;
		}
		return isFileExists;
	}

	public static boolean hasImageCaptureBug() {
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");
		return devices.contains(android.os.Build.BRAND + File.separator + android.os.Build.PRODUCT
				+ File.separator + android.os.Build.DEVICE);
	}
	
	public static String removeSpaces(String passedString) {
		passedString = passedString.trim();
		int index;
		String returnedString = "";
		while ((index = passedString.indexOf(" ")) != -1) {
			returnedString += passedString.substring(0, index);
			returnedString += "";
			passedString = passedString.substring(index + 1, passedString.length());
		}
		if (returnedString == "") {
			returnedString = passedString;
		} else {
			returnedString += passedString;
		}
		return returnedString;
	}
	
	public static boolean isEmailValid(String email) {
		String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
				+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
				+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
				+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
				+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|" + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			return true;
		}
		else return false;
	}
	
	public static boolean writeToFile(final Context context, final String fileName,
			final String fileContent, final String directoryName) {
		boolean isFileCreated = false;
		if (directoryCreated(directoryName)) {
			final String filePath = directoryName + File.separator + fileName;
			try {
				final FileWriter fileWriter = new FileWriter(new File(filePath), false);
				final BufferedWriter writer = new BufferedWriter(fileWriter);
				writer.write(fileContent);
				writer.newLine();
				writer.flush();
				writer.close();
				isFileCreated = true;
			} catch (IOException e) {
				isFileCreated = false;
			}
		}
		return isFileCreated;
	}
	
	public static ArrayList<String> returnAllFileNames(final String directoryName) {
		ArrayList<String> fileNameList = new ArrayList<String>();
		final File dir = new File(directoryName);
		for (final File imgFile : dir.listFiles()) {
			if (accept(imgFile)) {
				fileNameList.add(imgFile.getName());
			}
		}
		return fileNameList;
	}
	
	private static boolean accept(File file) {
		if (file != null) {
			if (file.isDirectory()) { return false; }
			String extension = getExtension(file);
			if (extension != null && isAndroidSupported(extension)) { return true; }
		}
		return false;
	}
	
	private static String getExtension(File file) {
		if (file != null) {
			String filename = file.getName();
			int dot = filename.lastIndexOf('.');
			if (dot > 0 && dot < filename.length() - 1)
				return filename.substring(dot + 1).toLowerCase();
		}
		return null;
	}
	
	private static boolean isAndroidSupported(String ext) {
		return ext.equalsIgnoreCase(GIF) || ext.equalsIgnoreCase(PNG) || ext.equalsIgnoreCase(JPG)
				|| ext.equalsIgnoreCase(BMP) || ext.equalsIgnoreCase(JPEG)
				|| ext.equalsIgnoreCase(PDF) || ext.equalsIgnoreCase(TXT);
	}
}