package com.auto.execute.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.auto.execute.direct.ValueModel;

public class GetSpecificCharacter {

	private static ValueModel vModel;

	public static List<File> getSpecFiles(List<File> fileList,
			String baseDirName, String pattern, String regEx) {
		String tempName = null;
		File baseDir = new File(baseDirName);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			System.out.println("can't find files from: " + baseDirName
					+ ", is not a directory!");
		} else {
			String[] filelist = baseDir.list();
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(baseDirName + "\\" + filelist[i]);
				if (!readfile.isDirectory()) {
					tempName = readfile.getName();
					if (wildcardMatch(pattern, tempName)) {
						String path = readfile.getAbsolutePath();
						if (findStr(path, regEx)) {
							fileList.add(readfile.getAbsoluteFile());
						}
					}
				} else
					getSpecFiles(fileList, baseDirName + "\\" + filelist[i],
							pattern, regEx);
			}
		}
		return fileList;
	}

	public static boolean wildcardMatch(String pattern, String str) {
		int patternLength = pattern.length();
		int strLength = str.length();
		int strIndex = 0;
		char ch;
		for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if (ch == '*') {
				while (strIndex < strLength) {
					if (wildcardMatch(pattern.substring(patternIndex + 1),
							str.substring(strIndex))) {
						return true;
					}
					strIndex++;
				}
			} else if (ch == '?') {
				strIndex++;
				if (strIndex > strLength) {
					return false;
				}
			} else {
				if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return (strIndex == strLength);
	}

	public static Boolean findStr(String path, String regEx) {
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fin = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin,
					"UTF-8"));
			while (br.read() != -1) {
				sb.append(br.readLine());
			}
			Pattern pattern = Pattern.compile(regEx);
			Matcher mat = pattern.matcher(sb.toString().trim());
			while (mat.find()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Document doc = null;

	public void init(String xmlFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse(new File(xmlFile));
	}

	public String readXML(String xmlFile, String tagName, String property)
			throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		NodeList databaseList = doc.getElementsByTagName(tagName);
		return String.valueOf(databaseList.getLength());
	}

	public int readXMLTag(String xmlFile, String tagName) throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		NodeList databaseList = doc.getElementsByTagName(tagName);
		return databaseList.getLength();
	}

	public String readXMLPro(String xmlFile, String tagName, String property)
			throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		String value = null;

		NodeList databaseList = doc.getElementsByTagName(tagName);
		for (int j = 0; j < databaseList.getLength(); j++) {
			Node childNode = databaseList.item(j);
			NamedNodeMap nodeMap = childNode.getAttributes();
			value = nodeMap.getNamedItem(property).getNodeValue();
		}
		return value;
	}

	public boolean readXMLInternetPermission(String xmlFile) throws Exception {
		boolean internet = false;
		String tagName = "uses-permission";
		String property = "android:name";
		String value = "android.permission.INTERNET";
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();

		NodeList databaseList = doc.getElementsByTagName(tagName);
		for (int j = 0; j < databaseList.getLength(); j++) {
			Node childNode = databaseList.item(j);
			NamedNodeMap nodeMap = childNode.getAttributes();
			String temp = nodeMap.getNamedItem(property).getNodeValue();
			if (temp.equals(value)) {
				internet = true;
				break;
			}
		}
		return internet;
	}

	public String readXMLComp(String xmlFile, String tagName, String property,
			String propertyValue) throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		String compName = null;

		NodeList databaseList = doc.getElementsByTagName(tagName);
		for (int j = 0; j < databaseList.getLength(); j++) {
			Node childNode = databaseList.item(j);
			// 取得节点的属性值
			NamedNodeMap nodeMap = childNode.getAttributes();
			String temp = nodeMap.getNamedItem(property).toString();
			if (temp.contains(propertyValue)) {
				Node preNode = childNode.getParentNode().getParentNode();
				NamedNodeMap preNodeMap = preNode.getAttributes();
				compName = preNodeMap.getNamedItem(property).getNodeValue();
				break;
			}
		}
		return compName;
	}

	public String readXMLComp2(String xmlFile, String tagName,
			String property1, String property2, String propertyValue)
			throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		String compName = null;

		NodeList databaseList = doc.getElementsByTagName(tagName);
		for (int j = 0; j < databaseList.getLength(); j++) {
			Node childNode = databaseList.item(j);
			// 取得节点的属性值
			NamedNodeMap nodeMap = childNode.getAttributes();
			String temp = nodeMap.getNamedItem(property1).toString();
			if (temp.contains(propertyValue)) {
				compName = nodeMap.getNamedItem(property2).getNodeValue();
				break;
			}
		}
		return compName;
	}

	public String readXMLCompM(String xmlFile, String property, String str)
			throws Exception {
		this.init(xmlFile);
		Element root = doc.getDocumentElement();
		root.getTagName();
		String compName = null;

		NodeList databaseList = doc.getElementsByTagName("category");
		for (int j = 0; j < databaseList.getLength(); j++) {
			Node childNode = databaseList.item(j);
			// 取得节点的属性值
			NamedNodeMap nodeMap = childNode.getAttributes();
			String temp2 = nodeMap.getNamedItem(property).toString();
			if (temp2.contains("android.intent.category.LAUNCHER")) {
				Node preNode = childNode.getParentNode().getParentNode();
				if (preNode.getNodeName().equals("activity")) {
					NamedNodeMap preNodeMap = preNode.getAttributes();
					compName = preNodeMap.getNamedItem(property).getNodeValue();
					if (!compName.equals(str))
						return compName;
				}
				break;
			}
		}
		return compName;
	}

	// example of get number of specific tag with XML
	public static List<Integer> getTagNumber(String baseDIR, String regEx,
			String tagName) {
		GetSpecificCharacter parse = new GetSpecificCharacter();
		List<Integer> list = new ArrayList<Integer>();
		List<File> fileList = new ArrayList<File>();
		List<String> fileNameList = new ArrayList<String>();
		String fileName = "AndroidManifest.xml";

		fileList = getSpecFiles(fileList, baseDIR, fileName, regEx);
		if (fileList.size() == 0) {
			System.out.println("No File Found.");
		} else {
			for (int i = 0; i < fileList.size(); i++) {
				String tempPath = fileList.get(i).toString();
				int startPoint = baseDIR.length() + 1;
				int endPoint = tempPath.length() - fileName.length() - 1;
				try {
					fileNameList.add(tempPath.substring(startPoint, endPoint));
					list.add(parse.readXMLTag(tempPath, tagName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	// example of get property value of specific tag with XML
	public static List<String> getTagPro(String baseDIR, String regEx,
			String tagName, String outPath, String property) {
		GetSpecificCharacter parse = new GetSpecificCharacter();
		List<String> list = new ArrayList<String>();
		List<File> fileList = new ArrayList<File>();
		List<String> fileNameList = new ArrayList<String>();
		String fileName = "AndroidManifest.xml";

		fileList = getSpecFiles(fileList, baseDIR, fileName, regEx);
		if (fileList.size() == 0) {
			System.out.println("No File Found.");
		} else {
			for (int i = 0; i < fileList.size(); i++) {
				String tempPath = fileList.get(i).toString();
				int startPoint = baseDIR.length() + 1;
				int endPoint = tempPath.length() - fileName.length() - 1;
				try {
					fileNameList.add(tempPath.substring(startPoint, endPoint));
					list.add(parse.readXMLPro(tempPath, tagName, property));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static boolean matchPackage(String str) {
		String regex = "([\\w-]+\\.)+[\\w-]+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		vModel = new ValueModel();
		GetSpecificCharacter parse = new GetSpecificCharacter();

		List<File> fileList = new ArrayList<File>();
		List<String> fileNameList = new ArrayList<String>();
		List<String> packageList = new ArrayList<String>();
		List<String> componentList = new ArrayList<String>();
		List<String> noPropertyList = new ArrayList<String>();
		List<String> activityCount = new ArrayList<String>();

		String baseDIR = vModel.apkoutpath;

		String fileName = "AndroidManifest.xml";
		String tagName = "manifest";
		String tagActivity = "activity";
		String property = "package";
		String tagNameComp = "action";
		String propertyComp = "android:name";
		String propertyValue = "android.intent.action.MAIN";
		String extension = ".apk";

		fileList = getSpecFiles(fileList, baseDIR, fileName, property);
		if (fileList.size() == 0) {
			System.out.println("No File Found.");
		} else {
			for (int i = 0; i < fileList.size(); i++) {
				String filePath = fileList.get(i).toString();
				int startPoint = baseDIR.length() + 1;
				int endPoint = filePath.length() - fileName.length() - 1;

				try {
					// String tempComp = null;
					String tempName = filePath.substring(startPoint, endPoint);
					String tempPack = parse.readXMLPro(filePath, tagName,
							property);
					String tempComp = parse.readXMLComp(filePath, tagNameComp,
							propertyComp, propertyValue);
					// String tempComp2 = parse.readXMLComp2(filePath,
					// "activity", propertyComp,"android:launchMode", "1");
					// if (tempComp1 != null || !tempComp1.isEmpty())
					// tempComp = tempComp1;
					// else if (tempComp2 != null || !tempComp2.isEmpty())
					// tempComp = tempComp2;
					if (tempComp == null || tempComp.isEmpty()) {
						System.out.println("no main launch activity: "
								+ filePath);
						noPropertyList.add(tempName);
					} else {
						// if (parse.readXMLInternetPermission(filePath)) {
						fileNameList.add(tempName + extension);
						packageList.add(tempPack);
						if (tempComp.contains(tempPack))
							componentList.add(tempComp);
						else if (tempComp.substring(0, 1).contains("."))
							componentList.add(tempPack + tempComp);
						else {
							if (matchPackage(tempComp)) {
								// System.out.println(tempComp);
								componentList.add(tempComp);
							} else
								componentList.add(tempPack + "." + tempComp);
						}
						activityCount.add(parse.readXML(filePath, tagActivity,
								propertyComp));
						// } else
						// System.out.println("no internet permission: "
						// + filePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		String outPathName = vModel.namepath;
		String outPathPack = vModel.packpath;
		String outPathComp = vModel.comppath;
		String outPathCount = vModel.activityCount;
		// String outPathNoPro = "K:/android_apps/malware/malware-no-comp.txt";
		WriteData.writeFromBuffer(fileNameList, outPathName, false);
		WriteData.writeFromBuffer(packageList, outPathPack, false);
		WriteData.writeFromBuffer(componentList, outPathComp, false);
		WriteData.writeFromBuffer(activityCount, outPathCount, false);
		// WriteData.writeFromBuffer(noPropertyList, outPathNoPro, false);
		System.out.println("total file number: " + fileNameList.size());
	}
}
