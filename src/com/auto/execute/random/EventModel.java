package com.auto.execute.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.graphics.Point;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import com.android.hierarchyviewerlib.device.ViewNode;
import com.auto.execute.direct.WidgetModel;

public class EventModel {
	private static TouchPressType type = com.android.chimpchat.core.TouchPressType.DOWN_AND_UP;

	private AdbChimpDevice device;
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
	public EventModel(AdbChimpDevice device) {
		this.device = device;
	}
	
	public void setDevice(AdbChimpDevice mDevice) {
		this.device = mDevice;
	}

	/**
	 * @return the widget id list
	 */
	public AdbChimpDevice getDevice() {
		return device;
	}

	public boolean installApp(String apk) {
		boolean installed = false;
		try {
			installed = getDevice().installPackage(apk);
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
			getDevice().startActivity(null, action, null, null, categories,
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
			uninstalled = getDevice().removePackage(packagename);
			System.out.println("uninstalled package: " + packagename);
		} catch (Exception ex) {
			System.out.println("uninstall apk raised exception: " + ex);
		}
		return uninstalled;
	}

	public void touch(int x, int y) {
		try {
			getDevice().touch(x, y, type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressBack() {
		try {
			getDevice().press("KEYCODE_BACK", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressMenu() {
		try {
			getDevice().press("KEYCODE_MENU", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressHome() {
		try {
			getDevice().press("KEYCODE_HOME", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void pressClear() {
		try {
			getDevice().press("KEYCODE_CLEAR", type);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
	}

	public void sendRandomEventA() {
		Random rd1 = new Random();
		int randomE = rd1.nextInt(4);
		int startX = rd1.nextInt(pWidth / 2);
		int startY = rd1.nextInt(pHeight / 2);
		System.out.println("send random event:");
		switch (randomE) {
		case (0):
			pressBack();
			//System.out.println("press back");
			break;
		case (1):
			getDevice().touch(rd1.nextInt(pWidth), rd1.nextInt(pHeight), type);
			//System.out.println("touch");
			break;
		case (2):
			getDevice().drag(startX, startY, (startX + rd1.nextInt(pWidth)),
					(startY + rd1.nextInt(pHeight)), 5, 2000);
			//System.out.println("drag");
			break;
		case (3):
			pressMenu();
			//System.out.println("press menu");
			break;
		default:
			//System.out.println("default");
			break;
		}

	}

	public void sendRandomEvent() {
		Random rd1 = new Random();
		int randomE = rd1.nextInt(3);
		int startX = rd1.nextInt(pWidth / 2);
		int startY = rd1.nextInt(pHeight / 2)+40;
		System.out.println("send random event:");
		switch (randomE) {
		case (0):
			getDevice().touch(rd1.nextInt(pWidth), rd1.nextInt(pHeight), type);
			//System.out.println("touch");
			break;
		case (1):
			getDevice().drag(startX, startY, (startX + rd1.nextInt(pWidth)),
					(startY + rd1.nextInt(pHeight)-40), 2, 1000);
			//System.out.println("drag");
			break;
		case (2):
			pressBack();
			//System.out.println("press back");
			break;
		default:
			//System.out.println("default");
			break;
		}

	}

	public boolean touchDialogNode(String info, ViewNode rootNode) {
		boolean touched = false;
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
			getDevice().touch(p.x, p.y, type);
			touched = device.touched;
			Thread.sleep(1000);
		} catch (Exception ex) {
			System.out.println("touch InterruptedException:" + ex);
		}
		return touched;
	}

	public boolean matchedDialog(ViewNode rootNode) {
		boolean isDialog = false;
		if (rootNode == null) {
			//System.out.println("can't load child node with id of root node");
		} else {
			//System.out.println("dWidth:" + dWidth + "," + "dHeight:" + dHeight);
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
			//System.out.println("pWidth:" + pWidth + "," + "pHeight:" + pHeight);
		}
//		if (isDialog)
//			System.out.println("is dialog window node: " + rootNode.name);
		return isDialog;
	}
}
