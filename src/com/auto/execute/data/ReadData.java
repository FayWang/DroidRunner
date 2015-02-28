package com.auto.execute.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ReadData {

	public static final List<String> readText(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath)));
		List<String> list = new ArrayList<String>();

		for (String line = br.readLine(); line != null; line = br.readLine()) {
			list.add(line);
			//System.out.println(list.size());
		}
		br.close();
		
		return list;

	}

	public static final List<String> readF2(String filePath) throws IOException {
		FileReader fr = new FileReader(filePath);
		BufferedReader bufferedreader = new BufferedReader(fr);
		String instring;
		List<String> list = new ArrayList<String>();
		while ((instring = bufferedreader.readLine().trim()) != null) {
			if (0 != instring.length()) {
				list.add(instring);
				System.out.println(instring);
			}
		}
		fr.close();
		
		return list;
	}
	
	public static List<String> getExcelList(String path, int sheetIndx,
			int colNum) {
		File file = new File(path);
		Workbook rwb = null;
		Cell cell = null;
		List<String> list2 = new ArrayList<String>();

		try {
			InputStream is = new FileInputStream(file);
			rwb = Workbook.getWorkbook(is);

			Sheet rs = rwb.getSheet(sheetIndx);
			for (int j = 0; j < rs.getRows(); j++) {
				cell = rs.getCell(colNum, j);
				String content = cell.getContents();
				list2.add(content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list2;

	}
	
	public static List<Integer> getExcelListInt(String path, int sheetIndx,
			int colNum) {
		File file = new File(path);
		Workbook rwb = null;
		Cell cell = null;
		List<Integer> list2 = new ArrayList<Integer>();

		try {
			InputStream is = new FileInputStream(file);
			rwb = Workbook.getWorkbook(is);

			Sheet rs = rwb.getSheet(sheetIndx);
			for (int j = 1; j < rs.getRows(); j++) {
				cell = rs.getCell(colNum, j);
				Integer content = Integer.valueOf(cell.getContents());
				list2.add(content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list2;

	}
	
	public static void main(String[] args) throws BiffException, IOException {
		String path = "G:/VirusShare_Android_20130506/11030620526608edb46af14f28/apkinfo.xls";
		//readText(path);
		System.out.println(getExcelList(path,0,0));
	}
}
