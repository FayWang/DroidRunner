package com.auto.execute.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.read.biff.BiffException;

public class WriteData {
	// add is for overlap when false
	public static void writeFromBuffer(List<String> list, String outPath,
			boolean add) {
		try {
			File file = new File(outPath);
			BufferedWriter out = new BufferedWriter(new FileWriter(file, add));
			for (int i = 0; i < list.size(); i++) {
				out.write(String.valueOf(list.get(i)));
				out.newLine();
			}
			out.close();
			out = null;
			file = null;
		} catch (Exception ex) {

		}
	}

	public static void writeFromBuffer(String[] str, String outPath, boolean add) {
		try {
			File file = new File(outPath);
			BufferedWriter out = new BufferedWriter(new FileWriter(file, add));
			for (int i = 0; i < str.length; i++) {
				out.write(String.valueOf(str[i]));
				out.newLine();
			}
			out.close();
			out = null;
			file = null;
		} catch (Exception ex) {

		}
	}

	public static void writeFromBuffer(String str, String outPath, boolean add) {
		try {
			File file = new File(outPath);
			BufferedWriter out = new BufferedWriter(new FileWriter(file, add));
			if (!str.isEmpty()) {
				out.write(str);
				out.newLine();
			}
			out.close();
			out = null;
			file = null;
		} catch (Exception ex) {

		}
	}

	public static void writeFromBuffer(Integer num, String outPath, boolean add) {
		try {
			File file = new File(outPath);
			BufferedWriter out = new BufferedWriter(new FileWriter(file, add));
			if (!String.valueOf(num).isEmpty()) {
				out.write(String.valueOf(num));
				// out.newLine();
			}
			out.close();
			out = null;
			file = null;
		} catch (Exception ex) {

		}
	}

	public static List<String> removeDuplicate(List<String> arlList) {
		HashSet<String> h = new HashSet<String>(arlList);
		arlList.clear();
		arlList.addAll(h);
		return arlList;
	}

	public static String matchPackage(String str) {
		String regex = "([\\w-]+\\.)+[\\w-]+";
		String temp = null;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			temp = matcher.group();
			return temp;
		} else {
			return str;
		}
	}

	public static boolean matchPackage1(String str) {
		String regex = "([\\w-]+\\.)+[\\w-]+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws BiffException, IOException {
		String path = "E:\\test817.txt";
		String temp = "cn.lieche.main/cn.lieche.main.mainActivity";
		String[] str = new String[5];
		System.out.println(matchPackage(temp));
		// writeFromBuffer(str,path,false);
		// for(int i=0;i< str.length;i++){
		// str[i]=String.valueOf(i);
		// }
		// writeFromBuffer(str,path,false);
		// System.out.print(String.valueOf(temp).substring(6));

		System.out.println(matchPackage1("geinimi.custom.Ad3004_30040001"));
	}
}
