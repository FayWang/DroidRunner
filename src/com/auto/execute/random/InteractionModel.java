package com.auto.execute.random;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	private static AdbChimpDevice mDevice;
	private static String packageName;
	private static boolean needLaunch;
	private static int count;
	private static int iIndex;
	private static Map<String, Integer> mapPackages;
	private static Process p;
	private static List<String> list_failed;
	private static List<String> list_iIndex;
	private static int apkNo;

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
			stopConnection(device);
		}
	}

	/**
	 * Constructs the hierarchy viewer for the specified device.
	 * 
	 * @param device
	 *            The Android device to connect to.
	 */
	private static IDevice initConnection() throws InterruptedException {
		System.out.println("==>init hierarchyviewer device connection......");
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
		// System.out.println("init_connection done......");

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
		System.out.println("tested apks number: " + apkNo);
		WriteData.writeFromBuffer(list_failed, vModel.failedpath, false);
		list_iIndex.add(String.valueOf(iIndex));
		list_iIndex.add(String.valueOf(count));
		WriteData.writeFromBuffer(list_iIndex, vModel.travindex, false);
		System.out.println("stop_connection done......");

	}

	public static Window getFocusedWindow(String packageName) {
		needLaunch = true;
		Window windowN = null;
		int id = DeviceBridge.getFocusedWindow(device);
		Window[] windows = DeviceBridge.loadWindows(device);
		for (Window w : windows) {
			if (w.getHashCode() == id)
				windowN = w;
			if (w.getTitle().contains(packageName))
				needLaunch = false;
		}
		return windowN;
	}

	public static int getInterval(List<Integer> list_interval,
			List<Integer> list_count) {
		int interval = 0;
		Random random = new Random();
		while (list_count.get(iIndex) == 0) {
			iIndex = iIndex + 1;
			if (iIndex >= list_count.size())
				break;
		}
		if (iIndex >= list_count.size()) {
			System.out.println("traverse done!");
		} else {
			interval = (list_interval.get(iIndex) - 1) * 10
					+ random.nextInt(10);
			if (count == list_count.get(iIndex)) {
				iIndex++;
				count = 1;
				// stopConnection(device);
			}
		}
		return interval;
	}

	private static boolean handleSuRequest() {
		boolean handeld = false;
		// System.out.println("==>handleSuRequest");
		Window window = getFocusedWindow(packageName);
		// System.out.println("current focused window:" +
		// String.valueOf(window));
		try {
			if (window.toString().contains("com.noshufou.android.su.SuRequest")) {
				handeld = su(window);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
		return handeld;
	}

	private static boolean su(Window window) {
		boolean handeld = false;
		ViewNode rootNode = DeviceBridge.loadWindowData(window);
		ViewNode allowNode = wModel.findViewById("id/allow");
		// System.out.println("allowNode: " + allowNode);
		if (allowNode != null) {
			String info = allowNode.id + "@" + allowNode.hashCode;
			if (eModel.matchedDialog(rootNode)) {
				boolean touched = eModel.touchDialogNode(info, rootNode);
				if (!touched) {
					mDevice.dispose();
					mDevice = new AdbChimpDevice(device);
					eModel = new EventModel(mDevice);
				}
				handeld = true;
			}
		}
		return handeld;
	}

	public static Process autoTcpdump(String packName) throws IOException {
		String tcpdumpcomm = "adb shell su -c '/system/xbin/tcpdump -p -vv -s 0 -w /sdcard/pcaps/normal/"
				+ vModel.subDir + "/" + packName + ".pcap'";
		// String tcpdumpcomm =
		// "adb shell /system/xbin/tcpdump -p -vv -s 0 -w /sdcard/pcaps/normal/interval-13/"
		// + vModel.subDir + "/" + packName + ".pcap";
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
		// Thread.sleep(1000);
		try {
			p = autoTcpdump(packageNameP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int retries = 5;
		boolean handeld = false;
		do {
			Thread.sleep(1000);
			System.out.println("Waiting for su request....");
			handeld = handleSuRequest();
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

	public static void main(String[] args) throws InterruptedException,
			IOException {
		// TODO Auto-generated method stub
		vModel = new ValueModel();

		List<String> list_apkname = new ArrayList<String>();
		List<String> list_component = new ArrayList<String>();
		List<String> list_package = new ArrayList<String>();
		list_failed = new ArrayList<String>();
		list_iIndex = new ArrayList<String>();

		List<Integer> list_interval = new ArrayList<Integer>();
		List<Integer> list_count = new ArrayList<Integer>();

		mapPackages = new HashMap<String, Integer>();

		list_apkname = ReadData.readText(vModel.namepath);
		list_component = ReadData.readText(vModel.comppath);
		list_package = ReadData.readText(vModel.packpath);
		list_interval = ReadData.getExcelListInt(vModel.intervalfile, 0, 0);
		list_count = ReadData.getExcelListInt(vModel.intervalfile, 0, 2);

		// System.out.println(list_interval.get(12));
		iIndex = Integer.valueOf(ReadData.readText(vModel.travindex).get(0));
		count = Integer.valueOf(ReadData.readText(vModel.travindex).get(1));
		System.out.println(iIndex + ":" + list_interval.get(iIndex));
		apkNo = 0;

		initConnection();
		setupViewServer();
		if (DeviceBridge.loadViewServerInfo(device) == null)
			stopConnection(device);

		wModel = new WidgetModel(device);
		mDevice = new AdbChimpDevice(device);
		eModel = new EventModel(mDevice);
		eModel.removeApp(list_package.get(list_apkname.size()-73));
		for (int i = list_apkname.size()-74; i > 0; i--) {
			apkNo++;
			count++;
			System.out.println(apkNo);
			needLaunch = true;
			packageName = list_package.get(i);
			p = null;
			boolean installed = eModel.installApp(vModel.apkpath
					+ list_apkname.get(i));
			if (installed) {
				updateCountAndDump();
				needLaunch = eModel.launchActivity(packageName,
						list_component.get(i));
//				int interval = getInterval(list_interval, list_count);
//				System.out.println(interval);
//				if (interval == 0) {
//					break;
//				}
				Random random = new Random();
				long startTime = System.currentTimeMillis();
				int interval = 13 * 10 + random.nextInt(10);
				long endTime = startTime + interval * 1000;
				while (System.currentTimeMillis() < endTime) {
					System.out.println("==>start new turn");
					if (needLaunch)
						needLaunch = eModel.launchActivity(packageName,
								list_component.get(i));

					DeviceBridge.initDebugBridge(vModel.adbLocation);
					setupViewServer();
					Thread.sleep(3000);

					Window window = getFocusedWindow(packageName);
					try {
						if (!needLaunch) {
							// alert running failed window
							if (String.valueOf(window).isEmpty()
									|| String.valueOf(window)
											.contains("很抱歉！")) {
								Thread.sleep(10 * 1000);
							} else {
								if (String.valueOf(window).contains(
										"com.android.settings")
										|| String.valueOf(window).contains(
												"com.android.systemui"))
									eModel.pressBack();
								else if (String.valueOf(window).contains(
										"com.noshufou.android.su"))
									su(window);
								else {
									eModel.sendRandomEvent();
								}
								boolean touched = mDevice.touched;
								System.out.println("touched:" + touched);
								if (!touched) {
									mDevice.dispose();
									mDevice = new AdbChimpDevice(device);
									eModel = new EventModel(mDevice);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(e);
					}
				}
				System.out.println("==>times up");
				p.destroy();
				eModel.removeApp(packageName);
				// break;
			} else {
				System.out.println("install failed: " + list_apkname.get(i));
				list_failed.add(vModel.apkpath + list_apkname.get(i));
			}
		}
		System.out.println("done!");
		stopConnection(device);
		System.exit(0);
	}
}
