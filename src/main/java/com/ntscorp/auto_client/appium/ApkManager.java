package com.ntscorp.auto_client.appium;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.ntscorp.auto_client.getset.TestInfo;

public class ApkManager {
	
	/**
	 * URL 주소를 통해 APK를 다운로드 하는 메소드
	 * APK는 PC의 C:\\apkDownload 경로에 다운로드 됨
	 * 
	 * @param URL
	 */
	public static void downloadApkFile(URL url) {	
		//URL url;
		OutputStream outStream = null;
		InputStream inputStream = null;
		URLConnection urlConnection = null;
		
		int fileSize = 1024;
		int byteWritten = 0;
		new File(TestInfo.getApkDir()).mkdirs();
		
		try {
			//url = new URL(Formatter.PropertyGetSet.apkAddr);
			byte[] buf;
			int byteRead;
						
			urlConnection = url.openConnection();
			inputStream = urlConnection.getInputStream();
			
			buf = new byte [fileSize];
			String date = getSimpleDate();

			outStream = new BufferedOutputStream(new FileOutputStream(TestInfo.getApkDir() + "\\" + getSimpleDate() + "_" + date + ".apk"));
			TestInfo.setApkName(getNewestFile(TestInfo.getApkDir()).getName());
			
			while ((byteRead = inputStream.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
				byteWritten += byteRead;
			}
			System.out.println(">>> APK Download Size (bytes)  : " + byteWritten);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				inputStream.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getSimpleDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat form = new SimpleDateFormat("yyMMdd");
		return form.format(cal.getTime());	
	}

	public File getNewestFile(String filePath, String ext) {
	    File theNewestFile = null;
	    File dir = new File(filePath);
	    FileFilter fileFilter = new WildcardFileFilter("*." + ext);
	    File[] files = dir.listFiles(fileFilter);

	    if (files.length > 0) {
	        /** The newest file comes first **/
	        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
	        theNewestFile = files[0];
	    }
	    return theNewestFile;
	}
	
	public static File getNewestFile(String dir) {
	    File fl = new File(dir);
	    File[] files = fl.listFiles(new FileFilter() {          
	        public boolean accept(File file) {
	            return file.isFile();
	        }
	    });
	    long lastMod = Long.MIN_VALUE;
	    File choice = null;
	    for (File file : files) {
	        if (file.lastModified() > lastMod) {
	            choice = file;
	            lastMod = file.lastModified();
	        }
	    }
	    return choice;
	}
}
