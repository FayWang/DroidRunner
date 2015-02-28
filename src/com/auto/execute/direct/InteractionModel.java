package com.auto.execute.direct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.CORBA.portable.OutputStream;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.ViewNode;
import com.android.hierarchyviewerlib.device.Window;
import com.auto.execute.data.ReadData;
import com.auto.execute.data.WriteData;

public class InteractionModel {
	public static final String TAG = "Interaction model:";
	private static IDevice device;
	private static String packageName;
	private static ViewNode currentView;
	private static Map<String, List<String>> mapViews;
	private static Map<String, List<String>> mapTabViews;
	private static Map<String, String> mapTabWidgets;
	private static Map<String, Integer> mapExecutedNo;
	private static boolean needLaunch;
	private static Integer tabNum;
	private static List<String> tabWidgets;
	private static boolean visited;
	private static Map<String, Integer> mapPackages;
	private static Process p;
	private static List<String> list_failed;
	private static List<String> coverage_activites;
	private static List<String> coverage_count;

	private static EventModel eModel;
	private static WidgetModel wModel;
	private static ValueModel vModel;

	static class DeviceListener implements
			AndroidDebugBridge.IDebugBridgeChangeListener,
			AndroidDebugBridge.IDeviceChangeListener {
		private ArrayList<IDevice> devices;

		public DeviceListener() {
			devices = new ArrayList<IDevice>();
		}

		public boolean deviceConnected() {
			return devices.size() > 0;
		}

		public IDevice getDevices(int index) {
			return devices.get(index);
		}

		public void bridgeChanged(AndroidDebugBridge bridge) {
			System.out.print("bridge connected\n");
		}

		public void deviceChanged(IDevice device, int changeMask) {
			System.out.print(device + " status changed to ");
			System.out.print(changeMask);
			System.out.print("\n");
		}

		public void deviceConnected(IDevice device) {
			System.out.print(device + " connected\n");
			devices.add(device);
		}

		public void deviceDisconnected(IDevice device) {
			System.out.print(device);
			System.out.print(" disconnected\n");
			try {
				initConnection();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// stopConnection(device);
		}
	}

	/**
	 * Constructs the hierarchy viewer for the specified device.
	 * 
	 * @param device
	 *            The Android device to connect to.
	 */
	private static IDevice initConnection() throws InterruptedException {
		System.out.println("init hierarchyviewer device connection......");
		DeviceListener deviceListener = new DeviceListener();
		DeviceBridge.initDebugBridge(vModel.adbLocation);
		int retries = 5;
		do {
			Thread.sleep(500);
			System.out.println("Waiting for device....");
		} while (--retries > 0 && DeviceBridge.getDevices().length == 0);
		if (DeviceBridge.getDevices().length == 0) {
			System.out.println("No device found!");
			System.exit(0);
			return null;
		}
		for (IDevice device : DeviceBridge.getDevices()) {
			System.out.println("online device: " + device);
			DeviceBridge.setupDeviceForward(device);
		}
		DeviceBridge.startListenForDevices(deviceListener);
		device = DeviceBridge.getDevices()[0];
		System.out.println("connect device: " + device);
		System.out.println("init_connection done......");

		if (!DeviceBridge.isViewServerRunning(device)) {
			DeviceBridge.stopViewServer(device);
		}

		return device;
	}

	private static void setupViewServer() {
		if (!DeviceBridge.isViewServerRunning(device)) {
			if (!DeviceBridge.startViewServer(device)) {
				// TODO: Get rid of this delay.
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				if (!DeviceBridge.startViewServer(device)) {
					System.out.println("Unable to debug device " + device);
					stopConnection(device);
					throw new RuntimeException(
							"Could not connect to the view server");
				}
				return;
			}
		}
	}

	private static void initDevice() {
		if (DeviceBridge.loadViewServerInfo(device) == null)
			stopConnection(device);

		wModel = new WidgetModel(device);
		System.out.println("wModel:" + wModel);
		AdbChimpDevice mDevice = new AdbChimpDevice(device);
		System.out.println(mDevice);
		eModel = new EventModel(mDevice);
		System.out.println("eModel:" + eModel);
	}

	/**
	 * Quit the hierarchy viewer for the specified device.
	 * 
	 * @param device
	 *            The Android device to connect to.
	 */
	private static void stopConnection(IDevice device) {
		System.out.println("stop_connection......");
		if (device != null) {
			DeviceBridge.stopViewServer(device);
			System.out.println("stopViewServer!");
			DeviceBridge.terminate();
		}
		WriteData.writeFromBuffer(list_failed, vModel.failedpath, true);
		if (p != null)
			p.destroy();
		System.exit(0);
		System.out.println("stop_connection done......");
	}

	private static void handleWindowState(int state, Window window)
			throws IOException {
		// TODO Auto-generated method stub

		switch (state) {
		case 0:
			handleState_0();
			break;
		case 1:
			handleState_1(window);
			break;
		case 2:
			handleState_2();
			break;
		default:
			System.out.println("default");
			break;
		}
	}

	private static void handleState_0() {
		System.out.println("no foucused window......");
		eModel.pressBack();
	}

	private static void handleState_1(Window window) throws IOException {
		System.out.println("new activity is running......");
		if (wModel.activityBroken) {
			System.out.println("target activity is broken......");
			needLaunch = true;
			return;
		}
		needLaunch = false;

		String temp = String.valueOf(window);
		if (temp.equals("com.dell.launcher/com.dell.launcher.Launcher")) {
			needLaunch = true;
			return;
		}
		if (temp.contains("com.android.packageinstaller.UninstallAppProgress")
				|| temp.contains("com.android.settings")) {
			eModel.pressBack();
			return;
		}
		if (temp.contains("com.android.packageinstaller.PackageInstallerActivity")
				|| temp.equals("璧勮垂鎻愮ず")
				|| temp.contains("com.noshufou.android.su.SuRequest")
				|| temp.equalsIgnoreCase("sorry!")) {
			setHashMapB(currentView);
			executeTouch(currentView);
			return;
		}
		if (!temp.contains(packageName)) {
			eModel.pressBack();
			return;
		}
		if (currentView != null) {
			setHashMap(currentView);
			executeTouch(currentView);
		} else
			eModel.pressBack();
	}

	private static void handleState_2() throws IOException {
		System.out.println("no new target activity running......");
		if (currentView != null) {
			setHashMap(currentView);
		}
		executeTouch(currentView);
	}

	private static boolean handleSuRequest() {
		boolean handeld = false;
		System.out.println("==>handleSuRequest");
		Window window = wModel.getFocusedWindow(packageName);
		System.out.println("current focused window:" + String.valueOf(window));
		if (window.toString().contains("com.noshufou.android.su.SuRequest")) {
			ViewNode rootNode = DeviceBridge.loadWindowData(window);
			ViewNode allowNode = wModel.findViewById("id/allow");
			System.out.println("allowNode: " + allowNode);
			if (allowNode != null) {
				String info = allowNode.id + "@" + allowNode.hashCode;
				if (eModel.matchedDialog(rootNode))
					eModel.touchDialogNode(info, rootNode);
				else {
					eModel.touchNode(info, rootNode);
				}
				handeld = true;
			}
		}
		return handeld;
	}

	public static void setHashMapB(ViewNode rootNode) throws IOException {
		System.out.println("==>set button hash map");
		String viewName = String.valueOf(rootNode);
		System.out.println(mapViews.containsKey(viewName));
		try {
			if (!mapViews.containsKey(viewName)) {
				System.out.println("new view:" + viewName);
				wModel.setWidgetList(new ArrayList<String>());
				wModel.traverseButton(rootNode);
				mapViews.put(viewName, wModel.getWidgetList());
				mapExecutedNo.put(viewName, 0);
			}
		} catch (Exception ex) {
			System.out.println("set hash map raised exception:" + ex);
		}
	}

	public static void setHashMap(ViewNode rootNode) throws IOException {
		System.out.println("==>set hash map");
		String viewName = String.valueOf(rootNode);
		System.out.println(mapViews.containsKey(viewName));
		try {
			if (!mapViews.containsKey(viewName)) {
				System.out.println("new view:" + viewName);
				wModel.setWidgetList(new ArrayList<String>());
				wModel.traverseView(rootNode);
				mapViews.put(viewName, wModel.getWidgetList());
				mapExecutedNo.put(viewName, 0);
			}
		} catch (Exception ex) {
			System.out.println("set hash map raised exception:" + ex);
		}
	}

	public static void handleTabView(ViewNode tabNode) throws IOException {
		System.out.println("==>handle tab view");
		getTabs(tabNode);
		String tab = touchTabWidget(tabNode);
		Window window = wModel.getFocusedWindow(packageName);
		if (wModel.getWindowState() == 2) {
			tabNode = updateView(window);
			setHashMapTab(tabNode);
			executeTouchTab(tab, currentView);
		}
	}

	public static ViewNode updateView(Window window) throws IOException {
		try {
			currentView = DeviceBridge.loadWindowData(window);
			System.out.println("currentView:" + currentView);
		} catch (Exception e) {

		}
		ViewNode tabNode = null;
		if (currentView != null) {
			wModel.traverseTabHost(currentView);
			tabNode = wModel.getTabNode();
		}
		return tabNode;
	}

	public static void getTabs(ViewNode rootNode) {
		wModel.traverseTabWidget(rootNode);
		tabWidgets = wModel.getTabWidget();
		System.out.println("tabWidgets: " + tabWidgets);
	}

	public static List<String> setHashMapTab(ViewNode rootNode)
			throws IOException {
		System.out.println("==>set tab hash map");
		List<String> tabs = new ArrayList<String>();
		Set<ViewNode> tabViews = new HashSet<ViewNode>();

		tabs = tabWidgets;
		try {
			wModel.setWidgetList(new ArrayList<String>());
			wModel.traverseTabView(rootNode);
			tabViews = wModel.getTabViews();
			// System.out.println("tabViews: " + tabViews);
			for (ViewNode tabView : tabViews) {
				String viewName = String.valueOf(tabView);
				if (!mapTabViews.containsKey(viewName)) {
					System.out.println("new tab view:" + tabView);
					wModel.setWidgetList(new ArrayList<String>());
					wModel.traverseView(tabView);
					mapTabViews.put(viewName, wModel.getWidgetList());
					int index = mapTabWidgets.size();
					String tagWidget = tabs.get(index);
					if (!mapTabWidgets.containsKey(tagWidget)) {
						mapTabWidgets.put(tagWidget, viewName);
						mapExecutedNo.put(viewName, 0);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("set hash map raised exception:" + ex);
		}
		return tabWidgets;
	}

	public static void executeTouch(ViewNode rootNode) {
		System.out.println("==>execute touch");
		int index = 0;
		String viewName = String.valueOf(rootNode);
		// System.out.println(mapViews.get(viewName));
		if (!mapViews.get(viewName).isEmpty()) {
			index = mapExecutedNo.get(viewName);
			String info = mapViews.get(viewName).get(index);
			if (info != null) {
				if (eModel.matchedDialog(rootNode))
					eModel.touchDialogNode(info, rootNode);
				else {
					eModel.touchNode(info, rootNode);
				}
				index++;
				if (index == mapViews.get(viewName).size()) {
					if (index == 1)
						eModel.sendRandomEvent();
					else {
						mapExecutedNo.put(viewName, 0);
						eModel.pressBack();
					}
				} else
					mapExecutedNo.put(viewName, index);
			}
		} else
			eModel.sendRandomEvent();
	}

	public static void executeTouchTab(String tabWidget, ViewNode tabNode) {
		System.out.println("==>execute touch tab view");
		int index = 0;
		String viewName = mapTabWidgets.get(tabWidget);
		// System.out.println("tab viewName: " + viewName);
		List<String> widgets = mapTabViews.get(viewName);
		// System.out.println("tab view widget list: " + widgets);
		if (viewName != null && !widgets.isEmpty()) {
			index = mapExecutedNo.get(viewName);
			String info = widgets.get(index);
			if (info != null) {
				if (eModel.matchedDialog(tabNode))
					eModel.touchDialogNode(info, tabNode);
				else {
					eModel.touchNode(info, tabNode);
				}
				index++;
				if (index == widgets.size()) {
					visited = true;
					mapExecutedNo.put(viewName, 0);
				} else
					mapExecutedNo.put(viewName, index);
			}
		} else {
			eModel.sendRandomEvent();
			visited = true;
		}
	}

	public static String touchTabWidget(ViewNode tabNode) {
		System.out.println("==>execute touch tab widget");
		String tabWidgetN = null;
		String tabWidget = tabWidgets.get(tabNum);
		if (visited) {
			if (tabNum == (tabWidgets.size() - 1))
				tabNum = 0;
			else
				tabNum++;
			visited = false;
		}
		tabWidgetN = tabWidgets.get(tabNum);
		if (tabWidget != null && !tabWidget.equals(tabWidgetN)) {
			if (eModel.matchedDialog(tabNode))
				eModel.touchDialogNode(tabWidgetN, tabNode);
			else {
				eModel.touchNode(tabWidgetN, tabNode);
			}
		}
		System.out.println("current tabWidget: " + tabWidgetN + ", is tab: "
				+ (tabNum + 1));
		return tabWidgetN;
	}

	// 这是方法，添加到你的应用程序中即可，比较简陋，呵呵！
	public static void execShell(String cmd) {
		try {
			// 权限设置
			Process p = Runtime.getRuntime().exec("adb shell su");
			// 获取输出流
			OutputStream outputStream = (OutputStream) p.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(
					outputStream);
			// 将命令写入
			dataOutputStream.writeBytes(cmd);
			// 提交命令
			dataOutputStream.flush();
			// 关闭流操作
			dataOutputStream.close();
			outputStream.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Process autoTcpdump(String packageName) throws IOException {
		// String
		// tcpdumpcomm="adb shell /system/xbin/tcpdump -p -vv -s 0 -w /sdcard/tmpfiles/"+packageName
		// +".pcap";
		String tcpdumpcomm = "adb shell su -c '/system/xbin/tcpdump -p -vv -s 0 -w /sdcard/pcaps/"
				+ packageName + ".pcap'";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(tcpdumpcomm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e);
		}
		return p;
	}

	public static void updateCountAndDump() throws InterruptedException {
		int count = 1;
		if (mapPackages.containsKey(packageName)) {
			count = mapPackages.get(packageName);
			count++;
			mapPackages.put(packageName, count);
		} else
			mapPackages.put(packageName, count);

		String packageNameP = packageName + "_" + count;
		// eModel.pressHome();
		Thread.sleep(1000);
		try {
			p = autoTcpdump(packageNameP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int retries = 8;
		boolean handeld = false;
		do {
			Thread.sleep(1000);
			// System.out.println("Waiting for su request....");
			try {
				handeld = handleSuRequest();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (--retries > 0 && !handeld);
		if (handeld) {
			p.destroy();
			try {
				p = autoTcpdump(packageNameP);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void isCoverageActivity(String window) {
		System.out.println(window);
		if (window.contains(packageName)) {
			if (!coverage_activites.contains(window))
				coverage_activites.add(window);
		}
	}

	public static void main(String[] args) throws InterruptedException,
			IOException {
		// TODO Auto-generated method stub

		vModel = new ValueModel();

		List<String> list_apkname = new ArrayList<String>();
		List<String> list_component = new ArrayList<String>();
		List<String> list_package = new ArrayList<String>();
		list_failed = new ArrayList<String>();
		coverage_count = new ArrayList<String>();

		list_apkname = ReadData.readText(vModel.namepath);
		list_component = ReadData.readText(vModel.comppath);
		list_package = ReadData.readText(vModel.packpath);

		initConnection();
		setupViewServer();
		initDevice();

		// if (DeviceBridge.loadViewServerInfo(device) == null)
		// stopConnection(device);

		mapPackages = new HashMap<String, Integer>();

		for (int i = 0; i < list_apkname.size(); i++) {
		//for (int i = 0; i < 15; i++) {
			coverage_activites = new ArrayList<String>();
			currentView = null;
			mapViews = new HashMap<String, List<String>>();
			mapTabViews = new HashMap<String, List<String>>();
			mapTabWidgets = new HashMap<String, String>();
			mapExecutedNo = new HashMap<String, Integer>();
			needLaunch = true;
			visited = false;
			tabNum = 0;
			tabWidgets = new ArrayList<String>();
			packageName = list_package.get(i);
			p = null;
			boolean installed = eModel.installApp(vModel.apkpath
					+ list_apkname.get(i));
			//boolean installed = true;
			if (installed) {
				updateCountAndDump();
				needLaunch = eModel.launchActivity(packageName,
						list_component.get(i));
				long startTime = System.currentTimeMillis();
				long endTime = startTime + 5 * 60 * 1000;
				if (!needLaunch) {
					while (System.currentTimeMillis() < endTime) {
						System.out.println("==>start new turn");
						if (needLaunch)
							needLaunch = eModel.launchActivity(packageName,
									list_component.get(i));

						DeviceBridge.initDebugBridge(vModel.adbLocation);
						setupViewServer();
						Thread.sleep(3000);

						Window window = wModel.getFocusedWindow(packageName);
						System.out.println("current focused window:"
								+ String.valueOf(window));
						if (window != null || !String.valueOf(window).isEmpty()) {
							isCoverageActivity(String.valueOf(window));
							if (!wModel.getAppBroked()) {
								ViewNode tabNode = updateView(window);
								if (currentView != null) {
									int state = wModel.getWindowState();
									if (tabNode != null && wModel.hasTabHost) {
										System.out.println("has tab host: "
												+ tabNode);
										handleTabView(tabNode);
									} else
										handleWindowState(state, window);
								}
							} else {
								System.out.println("need relauch activity:"
										+ packageName);
								needLaunch = true;
							}
						}
					}
					System.out.println("times up");
					System.out.println(coverage_activites.size());
					WriteData.writeFromBuffer(
							String.valueOf(coverage_activites.size()),
							vModel.coverateCount, true);
					p.destroy();
					eModel.removeApp(packageName);
					Thread.sleep(5000);
				} else
					System.out.println("can't lauch activity: " + packageName);
			} else {
				System.out.println("install failed: " + list_apkname.get(i));
				list_failed.add(vModel.apkpath + list_apkname.get(i));
			}
		}

		System.out.println("done!");
		stopConnection(device);
	}
}
