/**
 * 
 */
package com.auto.execute.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Fay Wang
 * 
 */
public class GetFileName {

	// read all files include all child directory
	public static Map<Integer, String> readfile(String filepath,
			Map<Integer, String> pathMap) throws Exception {
		if (pathMap == null) {
			pathMap = new HashMap<Integer, String>();
		}
		File file = new File(filepath);
		if (!file.isDirectory()) {
			pathMap.put(pathMap.size(), file.getPath());

		} else if (file.isDirectory()) {
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(filepath + "/" + filelist[i]);
				if (!readfile.isDirectory()) {
					pathMap.put(pathMap.size(), readfile.getPath());

				} else if (readfile.isDirectory()) {
					readfile(filepath + "/" + filelist[i], pathMap);
				}
			}
		}
		return pathMap;
	}

	// only read the first directory files
	public static Map<Integer, String> readFirstDirFile(String filepath,
			Map<Integer, String> pathMap) throws Exception {
		if (pathMap == null) {
			pathMap = new HashMap<Integer, String>();
		}
		File file = new File(filepath);
		if (file.isDirectory()) {
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(filepath + "/" + filelist[i]);
				if (!readfile.isDirectory()) {
					pathMap.put(pathMap.size(), readfile.getPath());

				}
			}
		}
		return pathMap;
	}

	// get file name from the base file directory and write result
	public static List<String> getFileName(String baseDIR) {
		List<String> list = new ArrayList<String>();
		try {
			// Map<Integer, String> map = readfile(inPath, null);
			Map<Integer, String> map = readFirstDirFile(baseDIR, null);
			for (int i = 0; i < map.size(); i++) {
				int length = baseDIR.length() + 1;
				String temp = map.get(i).substring(length);
				list.add(temp);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return list;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String baseDIR = "E:/Download-APK/malware-unzip";
		String inPathNoPro = "E:/Download-APK/malware-dec-no-comp.txt";
		List<String> nameList = new ArrayList<String>();
		List<String> noPropList = new ArrayList<String>();
		nameList = getFileName(baseDIR);
		noPropList = ReadData.readText(inPathNoPro);
		Iterator<String> it = nameList.iterator();
		while (it.hasNext()) {
			String name = it.next();
			for (String nopropname : noPropList) {
				if (name.contains(nopropname))
					it.remove();
			}
			if (!name.endsWith(".apk") && !name.endsWith(".APK")) {
				it.remove();
				System.out.println(name);
			}
		}
		String outPath = "E:/Download-APK/malware-dec-apk.txt";
		WriteData.writeFromBuffer(nameList, outPath, false);
		System.out.println("total file number: " + nameList.size());
	}

}
