/**
 * 
 */
package com.auto.execute.random;

/**
 * @author Fay Wang
 * 
 */
public class ValueModel {
	public final String adbLocation = "D://Program Files//Android//android-sdk//platform-tools//adb.exe";;
	public String subDir;
	public String apkpath;
	public String apkoutpath;
	public String namepath;
	public String packpath;
	public String comppath;
	public String failedpath;
	public String travindex;
	public String intervalfile;

	/**
	 * 
	 */
	public ValueModel() {
		// TODO Auto-generated constructor stub
		//current sub directory
		this.subDir = "photograph";
		//current base directory of apk files
		String baseDir = "K:/android_apps/normal/apks/";
		//current base directory of apkout files
		String baseoutDir = "K:/android_apps/normal/apkout/";
		//current path of testing apk files
		this.apkpath = baseDir + this.subDir + "/";
		//current path of apkout files
		this.apkoutpath = baseoutDir + this.subDir;
		//generated path of apks name list 
		this.namepath = "K:/android_apps/normal/name-photo.txt";
		//generated path of packages name list 
		this.packpath = "K:/android_apps/normal/pack-photo.txt";
		//generated path of components name list 
		this.comppath = "K:/android_apps/normal/comp-photo.txt";
		//generated path of testing failed apk path
		this.failedpath = "K:/android_apps/normal/failed.txt";
		//path of current traversed index 
		this.travindex = "K:/android_apps/normal/index.txt";
		//path of time interval inference file
		this.intervalfile = "K:/android_apps/normal/executetime.xls";
	}

}
