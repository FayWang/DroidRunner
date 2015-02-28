/**
 * 
 */
package com.auto.execute.direct;

/**
 * @author Fay Wang
 * 
 */
public class ValueModel {
	public String adbLocation = "D://Program Files//Android//android-sdk//platform-tools//adb.exe";
	public String subDir;
	public String apkinfopath;
	public String apkpath;
	public String apkoutpath;
	public String namepath;
	public String packpath;
	public String comppath;
	public String failedpath;
	public String travindex;
	public String intervalfile;
	public String activityCount;
	public String coverateCount;

	/**
	 * 
	 */
	public ValueModel() {
		// TODO Auto-generated constructor stub
		// current sub directory
		this.subDir = "";
		// current path of total apk info
		this.apkinfopath = "G:/VirusShare_Android_20130506/11030620526608edb46af14f28/apkinfo.xls";
		// current base directory of apk files
		String baseDir = "G:/apks528/apks/";
		// current base directory of apkout files
		String baseoutDir = "G:/apks528";
		// current path of testing apk files
		this.apkpath = baseDir;
		// current path of apkout files
		this.apkoutpath = baseoutDir + this.subDir;
		// generated path of apks name list
		this.namepath = "G:/apks528/apks.txt";
		// generated path of packages name list
		this.packpath = "G:/apks528/package.txt";
		// generated path of components name list
		this.comppath = "G:/apks528/component.txt";
		// generated activity count path
		this.activityCount = "G:/apks528/count.txt";
		// generated activity count path
		this.coverateCount = "G:/apks528/coverage.txt";
		// generated path of testing failed apk path
		this.failedpath = "G:/apks528/failed.txt";
		// path of current traversed index
		this.travindex = "G:/apks528/index.txt";
		// path of time interval inference file
		this.intervalfile = "G:/apks528/executetime.xls";
	}

}
