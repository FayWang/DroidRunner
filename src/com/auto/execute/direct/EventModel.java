package com.auto.execute.direct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.graphics.Point;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.android.hierarchyviewerlib.device.ViewNode;

public class EventModel {
	private static TouchPressType type = com.android.chimpchat.core.TouchPressType.DOWN_AND_UP;

	private static AdbChimpDevice device;
	static int pHeight = 800;;
	static int pWidth = 480;
	static int dHeight = 0;
	static int dWidth = 0;

	/**
	 * Constructs the hierarchy viewer for the specified device.
	 * 
	 * @param device
	 *            The Android device to connect to.
	 */
	@SuppressWarnings("static-access")
	public EventModel(AdbChimpDevice device) {
		this.device = device;
	}

	public boolean installApp(String apk) {
		boolean installed = false;
		try {
			installed = device.installPackage(apk);
			System.out.println("installed app: " + apk);
		} catch (Exception ex) {
			System.out.println("install apk raised exception: " + ex);
		}
		return installed;
	}

	public boolean launchActivity(String packagename, String component)
			throws InterruptedException {
		boolean neeLaunch = true;
		// set launch permission
		String action = "android.intent.action.MAIN";
		Collection<String> categories = new ArrayList<String>();
		categories.add("android.intent.category.LAUNCHER");
		Thread.sleep(3000);
		try {
			device.startActivity(null, action, null, null, categories,
					new HashMap<String, Object>(), packagename + "/"
							+ component, 0);
			neeLaunch = false;
			System.out.println("start activity: " + component);
		} catch (Exception ex) {
			System.out.println("start activity raised exception: " + ex);
		}
		Thread.sleep(3000);
		return neeLaunch;
	}

	public boolean removeApp(String packagename) {
		boolean uninstalled = false;
		try {
			uninstalled = device.removePackage(packagename);
			System.out.println("uninstalled package: " + packagename);
		} catch (Exception ex) {
			System.out.println("uninstall apk raised exception: " + ex);
		}
		return uninstalled;
	}

	public void touch(int x, int y) {
		try {
			device.touch(x, y, type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void touchById(String id, ViewNode rootNode) {
		try {
			ViewNode node = WidgetModel.findViewById(id, rootNode);
			Point p = WidgetModel.getAbsoluteCenterOfView(node);
			device.touch(p.x, p.y, type);
			Thread.sleep(1000);
		} catch (Exception ex) {
			System.out.println("touch by id InterruptedException:" + ex);
		}
	}

	public void touchByCode(String hashCode, ViewNode rootNode) {
		try {
			ViewNode node = WidgetModel.findViewByCode(hashCode, rootNode);
			Point p = WidgetModel.getAbsoluteCenterOfView(node);
			device.touch(p.x, p.y, type);
			Thread.sleep(1000);
		} catch (Exception ex) {
			System.out.println("touch by hashCode InterruptedException:" + ex);
		}
	}

	public void touchNode(String str, ViewNode rootNode) {
		int delimIndex = str.indexOf('@');
		String id = str.substring(0, delimIndex);
		String hashCode = str.substring(delimIndex + 1);
		// System.out.println("id:" + id + " hashCode:" + hashCode);
		if (id.equals("NO_ID"))
			touchByCode(hashCode, rootNode);
		else
			touchById(id, rootNode);
	}

	public void touchDialogNode(String info, ViewNode rootNode) {
		int delimIndex = info.indexOf('@');
		String id = info.substring(0, delimIndex);
		String hashCode = info.substring(delimIndex + 1);
		ViewNode node = null;
		// System.out.println("id:" + id + " hashCode:" + hashCode);
		try {
			if (id.equals("NO_ID"))
				node = WidgetModel.findViewByCode(hashCode, rootNode);
			else
				node = WidgetModel.findViewById(id, rootNode);
			Point p = WidgetModel.getAbsoluteCenterOfView(node);
			// System.out.println("relative point x:" + p.x);
			// System.out.println("relative point y:" + p.y);
			p.x = ((pWidth - dWidth) / 2) + p.x;
			p.y = ((pHeight - dHeight) / 2) + p.y;
			// System.out.println("absolute point x:" + p.x);
			// System.out.println("absolute point y:" + p.y);
			device.touch(p.x, p.y, type);
			Thread.sleep(1000);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public boolean matchedDialog(ViewNode rootNode) {
		boolean isDialog = false;
		if (rootNode == null) {
			System.out.println("can't load child node with id of root node");
		} else {
			System.out.println("dWidth:" + dWidth + "," + "dHeight:" + dHeight);
			dWidth = rootNode.width;
			dHeight = rootNode.height;
			if (dWidth <= pWidth && dHeight <= pHeight)
				isDialog = true;
			else if (dWidth <= pHeight && dHeight <= pWidth) {
				isDialog = true;
				int temp = pWidth;
				pWidth = pHeight;
				pHeight = temp;
			}
			System.out.println("pWidth:" + pWidth + "," + "pHeight:" + pHeight);
		}
		if (isDialog)
			System.out.println("is dialog window node: " + rootNode.name);
		return isDialog;
	}

	public void pressBack() {
		try {
			device.press("KEYCODE_BACK", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressMenu() {
		try {
			device.press("KEYCODE_MENU", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressHome() {
		try {
			device.press("KEYCODE_HOME", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void sendRandomEventA() {
		Random rd1 = new Random();
		int randomE = rd1.nextInt(4);
		int startX = rd1.nextInt(pWidth / 2);
		int startY = rd1.nextInt(pHeight / 2) + 25;
		System.out.println("send random event:");
		switch (randomE) {
		case (0):
			pressBack();
			System.out.println("press back");
			break;
		case (1):
			device.touch(rd1.nextInt(pWidth), rd1.nextInt(pHeight) + 25, type);
			System.out.println("touch");
			break;
		case (2):
			device.drag(startX, startY, (startX + rd1.nextInt(pWidth / 2)),
					(startY + rd1.nextInt(pHeight / 2)), 5, 2000);
			System.out.println("drag");
			break;
		case (3):
			pressMenu();
			System.out.println("press menu");
			break;
		default:
			System.out.println("default");
			break;
		}

	}

	public void sendRandomEvent() {
		Random rd1 = new Random();
		int randomE = rd1.nextInt(2);
		int startX = rd1.nextInt(pWidth / 2);
		int startY = rd1.nextInt(pHeight / 2) + 25;
		System.out.println("send random event:");
		switch (randomE) {
		case (0):
			device.touch(rd1.nextInt(pWidth), rd1.nextInt(pHeight) + 25, type);
			System.out.println("touch");
			break;
		case (1):
			device.drag(startX, startY, (startX + rd1.nextInt(pWidth / 2)),
					(startY + rd1.nextInt(pHeight / 2)), 2, 2000);
			System.out.println("drag");
			break;
		default:
			System.out.println("default");
			break;
		}

	}

}
