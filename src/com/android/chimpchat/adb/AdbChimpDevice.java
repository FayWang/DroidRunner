/*
 * Copyright (C) 2011 0xlab - http://0xlab.org/
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Add Push/Pull interface by Wei-Ning Huang <azhuang@0xlab.org>
 */
package com.android.chimpchat.adb;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.android.chimpchat.ChimpManager;
import com.android.chimpchat.adb.LinearInterpolator.Point;
import com.android.chimpchat.core.IChimpImage;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.IChimpView;
import com.android.chimpchat.core.IMultiSelector;
import com.android.chimpchat.core.ISelector;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class AdbChimpDevice implements IChimpDevice {
	private static final Logger LOG = Logger.getLogger(AdbChimpDevice.class
			.getName());

	private static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];
	private static final long MANAGER_CREATE_TIMEOUT_MS = 30 * 1000; // 30
																		// seconds
	private static final long MANAGER_CREATE_WAIT_TIME_MS = 1000; // wait 1
																	// second

	private final ExecutorService executor = Executors
			.newSingleThreadExecutor();

	private final IDevice device;
	private ChimpManager manager;
	public boolean touched;

	public AdbChimpDevice(IDevice device) {
		this.device = device;
		this.manager = createManager("127.0.0.1", 12345);
		this.touched = false;
		Preconditions.checkNotNull(this.manager);
	}

	@Override
	public ChimpManager getManager() {
		return manager;
	}

	@Override
	public void dispose() {
		try {
			manager.quit();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error getting the manager to quit", e);
		}
		manager.close();
		executor.shutdown();
		manager = null;
	}

	@Override
	public HierarchyViewer getHierarchyViewer() {
		return new HierarchyViewer(device);
	}

	private void executeAsyncCommand(final String command,
			final LoggingOutputReceiver logger) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					device.executeShellCommand(command, logger);
				} catch (TimeoutException e) {
					LOG.log(Level.SEVERE, "Error starting command: " + command,
							e);
					throw new RuntimeException(e);
				} catch (AdbCommandRejectedException e) {
					LOG.log(Level.SEVERE, "Error starting command: " + command,
							e);
					throw new RuntimeException(e);
				} catch (ShellCommandUnresponsiveException e) {
					// This happens a lot
					LOG.log(Level.INFO, "Error starting command: " + command, e);
					throw new RuntimeException(e);
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Error starting command: " + command,
							e);
					throw new RuntimeException(e);
				}
			}
		});
	}

	private ChimpManager createManager(String address, int port) {
		try {
			device.createForward(port, port);
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Timeout creating adb port forwarding", e);
			return null;
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Adb rejected adb port forwarding command: "
					+ e.getMessage(), e);
			return null;
		} catch (IOException e) {
			LOG.log(Level.SEVERE,
					"Unable to create adb port forwarding: " + e.getMessage(),
					e);
			return null;
		}

		String command = "monkey --port " + port;
		executeAsyncCommand(command, new LoggingOutputReceiver(LOG, Level.FINE));

		// Sleep for a second to give the command time to execute.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.log(Level.SEVERE, "Unable to sleep", e);
		}

		InetAddress addr;
		try {
			addr = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			LOG.log(Level.SEVERE,
					"Unable to convert address into InetAddress: " + address, e);
			return null;
		}

		// We have a tough problem to solve here. "monkey" on the device gives
		// us no indication
		// when it has started up and is ready to serve traffic. If you try too
		// soon, commands
		// will fail. To remedy this, we will keep trying until a single command
		// (in this case,
		// wake) succeeds.
		boolean success = false;
		ChimpManager mm = null;
		long start = System.currentTimeMillis();

		while (!success) {
			long now = System.currentTimeMillis();
			long diff = now - start;
			if (diff > MANAGER_CREATE_TIMEOUT_MS) {
				LOG.severe("Timeout while trying to create chimp mananger");
				return null;
			}

			try {
				Thread.sleep(MANAGER_CREATE_WAIT_TIME_MS);
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "Unable to sleep", e);
			}

			Socket monkeySocket;
			try {
				monkeySocket = new Socket(addr, port);
			} catch (IOException e) {
				LOG.log(Level.FINE, "Unable to connect socket", e);
				success = false;
				continue;
			}

			try {
				mm = new ChimpManager(monkeySocket);
			} catch (IOException e) {
				LOG.log(Level.SEVERE,
						"Unable to open writer and reader to socket");
				continue;
			}

			try {
				mm.wake();
			} catch (IOException e) {
				LOG.log(Level.FINE, "Unable to wake up device", e);
				success = false;
				continue;
			}
			success = true;
		}

		return mm;
	}

	@Override
	public IChimpImage takeSnapshot() {
		try {
			return new AdbChimpImage(device.getScreenshot());
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Unable to take snapshot", e);
			return null;
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Unable to take snapshot", e);
			return null;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to take snapshot", e);
			return null;
		}
	}

	@Override
	public String getSystemProperty(String key) {
		return device.getProperty(key);
	}

	@Override
	public String getProperty(String key) {
		try {
			return manager.getVariable(key);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to get variable: " + key, e);
			return null;
		}
	}

	@Override
	public Collection<String> getPropertyList() {
		try {
			return manager.listVariable();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to get variable list", e);
			return null;
		}
	}

	@Override
	public void wake() {
		try {
			manager.wake();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to wake device (too sleepy?)", e);
		}
	}

	private String shell(String... args) {
		StringBuilder cmd = new StringBuilder();
		for (String arg : args) {
			cmd.append(arg).append(" ");
		}
		return shell(cmd.toString());
	}

	@Override
	public String shell(String cmd) {
		CommandOutputCapture capture = new CommandOutputCapture();
		try {
			device.executeShellCommand(cmd, capture);
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
			return null;
		} catch (ShellCommandUnresponsiveException e) {
			LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
			return null;
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
			return null;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
			return null;
		}
		return capture.toString();
	}

	public boolean pushFile(String localFilePath, String remoteFilePath) {
		try {
			device.pushFile(localFilePath, remoteFilePath);
		} catch (SyncException e) {
			LOG.log(Level.SEVERE, "Error pushing file: " + localFilePath, e);
			return false;
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Error pushing file: " + localFilePath, e);
			return false;
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Error pushing file: " + localFilePath, e);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error pushing file: " + localFilePath, e);
			return false;
		}
		return true;
	}

	public boolean pullFile(String remoteFilePath, String localFilePath) {
		try {
			device.pullFile(remoteFilePath, localFilePath);
		} catch (SyncException e) {
			LOG.log(Level.SEVERE, "Error pulling file: " + remoteFilePath, e);
			return false;
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Error pulling file: " + remoteFilePath, e);
			return false;
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Error pulling file: " + remoteFilePath, e);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error pulling file: " + remoteFilePath, e);
			return false;
		}
		return true;
	}

	@Override
	public boolean installPackage(String path) {
		try {
			String result = device.installPackage(path, true);
			if (result != null) {
				LOG.log(Level.SEVERE, "Got error installing package: " + result);
				return false;
			}
			return true;
		} catch (InstallException e) {
			LOG.log(Level.SEVERE, "Error installing package: " + path, e);
			return false;
		}
	}

	@Override
	public boolean removePackage(String packageName) {
		try {
			String result = device.uninstallPackage(packageName);
			if (result != null) {
				LOG.log(Level.SEVERE, "Got error uninstalling package "
						+ packageName + ": " + result);
				return false;
			}
			return true;
		} catch (InstallException e) {
			LOG.log(Level.SEVERE, "Error installing package: " + packageName, e);
			return false;
		}
	}

	@Override
	public void press(String keyName, TouchPressType type) {
		try {
			switch (type) {
			case DOWN_AND_UP:
				touched = manager.press(keyName);
				break;
			case DOWN:
				manager.keyDown(keyName);
				break;
			case UP:
				manager.keyUp(keyName);
				break;
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error sending press event: " + keyName + " "
					+ type, e);
		}
	}

	@Override
	public void type(String string) {
		try {
			manager.type(string);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error Typing: " + string, e);
		}
	}

	@Override
	public void touch(int x, int y, TouchPressType type) {
		try {
			switch (type) {
			case DOWN:
				manager.touchDown(x, y);
				break;
			case UP:
				manager.touchUp(x, y);
				break;
			case DOWN_AND_UP:
				touched = manager.tap(x, y);
				break;
			}
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Error sending touch event: " + x + " " + y
					+ " " + type, e);
		}
	}

	@Override
	public void reboot(String into) {
		try {
			device.reboot(into);
		} catch (TimeoutException e) {
			LOG.log(Level.SEVERE, "Unable to reboot device", e);
		} catch (AdbCommandRejectedException e) {
			LOG.log(Level.SEVERE, "Unable to reboot device", e);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Unable to reboot device", e);
		}
	}

	@Override
	public void startActivity(String uri, String action, String data,
			String mimetype, Collection<String> categories,
			Map<String, Object> extras, String component, int flags) {
		List<String> intentArgs = buildIntentArgString(uri, action, data,
				mimetype, categories, extras, component, flags);
		shell(Lists.asList("am", "start",
				intentArgs.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(
				ZERO_LENGTH_STRING_ARRAY));
	}

	@Override
	public void broadcastIntent(String uri, String action, String data,
			String mimetype, Collection<String> categories,
			Map<String, Object> extras, String component, int flags) {
		List<String> intentArgs = buildIntentArgString(uri, action, data,
				mimetype, categories, extras, component, flags);
		shell(Lists.asList("am", "broadcast",
				intentArgs.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(
				ZERO_LENGTH_STRING_ARRAY));
	}

	private static boolean isNullOrEmpty(@Nullable String string) {
		return string == null || string.length() == 0;
	}

	private List<String> buildIntentArgString(String uri, String action,
			String data, String mimetype, Collection<String> categories,
			Map<String, Object> extras, String component, int flags) {
		List<String> parts = Lists.newArrayList();

		// from adb docs:
		// <INTENT> specifications include these flags:
		// [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>]
		// [-c <CATEGORY> [-c <CATEGORY>] ...]
		// [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]
		// [--esn <EXTRA_KEY> ...]
		// [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]
		// [-e|--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]
		// [-n <COMPONENT>] [-f <FLAGS>]
		// [<URI>]

		if (!isNullOrEmpty(action)) {
			parts.add("-a");
			parts.add(action);
		}

		if (!isNullOrEmpty(data)) {
			parts.add("-d");
			parts.add(data);
		}

		if (!isNullOrEmpty(mimetype)) {
			parts.add("-t");
			parts.add(mimetype);
		}

		// Handle categories
		for (String category : categories) {
			parts.add("-c");
			parts.add(category);
		}

		// Handle extras
		for (Entry<String, Object> entry : extras.entrySet()) {
			// Extras are either boolean, string, or int. See which we have
			Object value = entry.getValue();
			String valueString;
			String arg;
			if (value instanceof Integer) {
				valueString = Integer.toString((Integer) value);
				arg = "--ei";
			} else if (value instanceof Boolean) {
				valueString = Boolean.toString((Boolean) value);
				arg = "--ez";
			} else {
				// treat is as a string.
				valueString = value.toString();
				arg = "--es";
			}
			parts.add(arg);
			parts.add(entry.getKey());
			parts.add(valueString);
		}

		if (!isNullOrEmpty(component)) {
			parts.add("-n");
			parts.add(component);
		}

		if (flags != 0) {
			parts.add("-f");
			parts.add(Integer.toString(flags));
		}

		if (!isNullOrEmpty(uri)) {
			parts.add(uri);
		}

		return parts;
	}

	@Override
	public Map<String, Object> instrument(String packageName,
			Map<String, Object> args) {
		List<String> shellCmd = Lists.newArrayList("am", "instrument", "-w",
				"-r", packageName);
		String result = shell(shellCmd.toArray(ZERO_LENGTH_STRING_ARRAY));
		return convertInstrumentResult(result);
	}

	/**
	 * Convert the instrumentation result into it's Map representation.
	 * 
	 * @param result
	 *            the result string
	 * @return the new map
	 */
	@VisibleForTesting
	/* package */static Map<String, Object> convertInstrumentResult(
			String result) {
		Map<String, Object> map = Maps.newHashMap();
		Pattern pattern = Pattern.compile("^INSTRUMENTATION_(\\w+): ",
				Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(result);

		int previousEnd = 0;
		String previousWhich = null;

		while (matcher.find()) {
			if ("RESULT".equals(previousWhich)) {
				String resultLine = result.substring(previousEnd,
						matcher.start()).trim();
				// Look for the = in the value, and split there
				int splitIndex = resultLine.indexOf("=");
				String key = resultLine.substring(0, splitIndex);
				String value = resultLine.substring(splitIndex + 1);

				map.put(key, value);
			}

			previousEnd = matcher.end();
			previousWhich = matcher.group(1);
		}
		if ("RESULT".equals(previousWhich)) {
			String resultLine = result.substring(previousEnd, matcher.start())
					.trim();
			// Look for the = in the value, and split there
			int splitIndex = resultLine.indexOf("=");
			String key = resultLine.substring(0, splitIndex);
			String value = resultLine.substring(splitIndex + 1);

			map.put(key, value);
		}
		return map;
	}

	@Override
	public void drag(int startx, int starty, int endx, int endy, int steps,
			long ms) {
		final long iterationTime = ms / steps;

		LinearInterpolator lerp = new LinearInterpolator(steps);
		LinearInterpolator.Point start = new LinearInterpolator.Point(startx,
				starty);
		LinearInterpolator.Point end = new LinearInterpolator.Point(endx, endy);
		lerp.interpolate(start, end, new LinearInterpolator.Callback() {
			@Override
			public void step(Point point) {
				try {
					touched = manager.touchMove(point.getX(), point.getY());
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Error sending drag start event", e);
				}

				try {
					Thread.sleep(iterationTime);
				} catch (InterruptedException e) {
					LOG.log(Level.SEVERE, "Error sleeping", e);
				}
			}

			@Override
			public void start(Point point) {
				try {
					touched = manager.touchDown(point.getX(), point.getY());
					touched = manager.touchMove(point.getX(), point.getY());
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Error sending drag start event", e);
				}

				try {
					Thread.sleep(iterationTime);
				} catch (InterruptedException e) {
					LOG.log(Level.SEVERE, "Error sleeping", e);
				}
			}

			@Override
			public void end(Point point) {
				try {
					touched = manager.touchMove(point.getX(), point.getY());
					touched = manager.touchUp(point.getX(), point.getY());
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Error sending drag end event", e);
				}
			}
		});
	}

	@Override
	public IChimpView getRootView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChimpView getView(ISelector arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getViewIdList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IChimpView> getViews(IMultiSelector arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void press(PhysicalButton arg0, TouchPressType arg1) {
		// TODO Auto-generated method stub

	}
}
