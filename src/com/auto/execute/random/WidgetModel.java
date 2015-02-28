package com.auto.execute.random;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.ViewNode;
import com.android.hierarchyviewerlib.device.ViewNode.Property;
import com.android.hierarchyviewerlib.device.Window;

public class WidgetModel {
	private static IDevice mDevice;
	private static Window appWindow;
	private static Window appWindowN;

	public boolean activityBroken;
	public boolean hasTabHost;
	public List<String> listWidget;
	public ViewNode tabNode;
	public List<String> tabWidget;
	public Set<ViewNode> tabViews;

	@SuppressWarnings("static-access")
	public WidgetModel(IDevice device) {
		this.mDevice = device;
		appWindow = null;
		appWindowN = null;
	}

	/**
	 * @return the boolean value of activityBroken
	 */
	public boolean getAppBroked() {
		return activityBroken;
	}

	/**
	 * @param boolean value the activityBroken to set
	 */
	public void setAppBroken(boolean broken) {
		this.activityBroken = broken;
	}

	/**
	 * @return the boolean value of hasTabHost
	 */
	public boolean hasTabHost() {
		return hasTabHost;
	}

	/**
	 * @param boolean value the hasTabHost to set
	 */
	public void setTabHost(boolean hasTabHost) {
		this.hasTabHost = hasTabHost;
	}

	/**
	 * @return the widget id list
	 */
	public ViewNode getTabNode() {
		return tabNode;
	}

	/**
	 * the widget id to set
	 */
	public void setTabNode(ViewNode tabNode) {
		this.tabNode = tabNode;
	}

	/**
	 * @return the widget id list
	 */
	public List<String> getTabWidget() {
		return tabWidget;
	}

	/**
	 * the widget id to set
	 */
	public void setTabWidget(List<String> tabWidget) {
		this.tabWidget = tabWidget;
	}

	/**
	 * @return the widget id list
	 */
	public Set<ViewNode> getTabViews() {
		return tabViews;
	}

	/**
	 * the widget id to set
	 */
	public void setTabViews(Set<ViewNode> tabViews) {
		this.tabViews = tabViews;
	}

	/**
	 * @return the widget id list
	 */
	public List<String> getWidgetList() {
		return listWidget;
	}

	/**
	 * the widget id to set
	 */
	public void setWidgetList(List<String> list) {
		this.listWidget = list;
	}

	public void traverseTabHost(ViewNode rootNode) {
		setTabNode(null);
		setTabWidget(new ArrayList<String>());
		setTabViews(new HashSet<ViewNode>());
		if (rootNode.name.contains("TabHost")) {
			setTabHost(true);
			tabNode = rootNode;
			return;
		}
		for (ViewNode child : rootNode.children) {
			traverseTabHost(child);
		}
		setTabNode(tabNode);
	}

	public void traverseTabWidget(ViewNode tabNode) {
		if (tabNode.name.contains("TabWidget")) {
			traverseView(tabNode);
			tabWidget = getWidgetList();
		}
		for (ViewNode child : tabNode.children) {
			traverseTabWidget(child);
		}
		setTabWidget(tabWidget);
	}

	public void traverseTabView(ViewNode tabNode) {
		String regex = "com.android.internal.policy.impl.PhoneWindow$DecorView";
		//System.out.println("traverse node:" + tabNode);
		if (regex.equals(tabNode.name)) {
			if (tabViews.size() > 0) {
				if (!tabViews.contains(tabNode))
					tabViews.add(tabNode);
			} else
				tabViews.add(tabNode);
		}
		for (ViewNode child : tabNode.children) {
			traverseTabView(child);
		}
		setTabViews(tabViews);
	}

	public void traverseView(ViewNode view) {
		String temp = getTargetWidget(view);
		if (temp != null) {
			try {
				listWidget.add(temp);
			} catch (Exception ex) {
				System.out.println("add widget list raised exception:" + ex);
			}
		}
		for (ViewNode child : view.children) {
			traverseView(child);
		}
		setWidgetList(listWidget);
	}

	public static String getTargetWidget(ViewNode node) {
		String widgetInfo = null;
		Property widgetPro1 = node.namedProperties.get("getVisibility()");
		Property widgetPro2 = node.namedProperties.get("isClickable()");
		Property widgetPro3 = node.namedProperties.get("isEnabled()");
		if (widgetPro1.toString().equals("getVisibility()=VISIBLE")) {
			if (widgetPro2.toString().equals("isClickable()=true")) {
				if (widgetPro3.toString().equals("isEnabled()=true")) {
					widgetInfo = node.id + "@" + node.hashCode;
				}
			}
		}
		return widgetInfo;
	}

	public void traverseButton(ViewNode view) {
		String buttonInfo = getTargetButton(view);
		if (buttonInfo != null) {
			listWidget.add(buttonInfo);
		}
		for (ViewNode child : view.children) {
			traverseButton(child);
		}
		setWidgetList(listWidget);
	}

	public static String getTargetButton(ViewNode node) {
		String buttonInfo = null;
		if (node.name.contains("Button")) {
			Property widgetPro1 = node.namedProperties.get("getVisibility()");
			Property widgetPro2 = node.namedProperties.get("isClickable()");
			Property widgetPro3 = node.namedProperties.get("isEnabled()");
			if (widgetPro1.toString().equals("getVisibility()=VISIBLE")) {
				if (widgetPro2.toString().equals("isClickable()=true")) {
					if (widgetPro3.toString().equals("isEnabled()=true")) {
						buttonInfo = node.id + "@" + node.hashCode;
						System.out.println("get widget info for " + node.name
								+ ":\n" + buttonInfo);
					}
				}
			}
		}
		return buttonInfo;
	}

	public Integer getWindowState() {
		int state = 0;
		if (appWindowN == null)
			return state;
		else {
			if (appWindow == null) {
				appWindow = appWindowN;
				state = 1;
			} else if (appWindow.toString().equals(appWindowN.toString()))
				state = 2;
			else {
				appWindow = appWindowN;
				state = 1;
			}
		}
		return state;
	}

	/**
	 * Find a view by id.
	 * 
	 * @param id
	 *            id for the view.
	 * @return view with the specified ID, or {@code null} if no view found.
	 */

	public ViewNode findViewById(String id) {
		ViewNode rootNode = DeviceBridge.loadWindowData(new Window(mDevice, "",
				0xffffffff));
		if (rootNode == null) {
			throw new RuntimeException("Could not dump view");
		}
		return findViewById(id, rootNode);
	}

	/**
	 * Find a view by ID, starting from the given root node
	 * 
	 * @param id
	 *            ID of the view you're looking for
	 * @param rootNode
	 *            the ViewNode at which to begin the traversal
	 * @return view with the specified ID, or {@code null} if no view found.
	 */

	public static ViewNode findViewById(String id, ViewNode rootNode) {
		if (rootNode.id.equals(id)) {
			return rootNode;
		}

		for (ViewNode child : rootNode.children) {
			ViewNode found = findViewById(id, child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Find a view by hashCode.
	 * 
	 * @param hashCode
	 *            hashCode for the view.
	 * @return view with the specified hashCode, or {@code null} if no view
	 *         found.
	 */

	public ViewNode findViewByCode(String hashCode) {
		ViewNode rootNode = DeviceBridge.loadWindowData(new Window(mDevice, "",
				0xffffffff));
		if (rootNode == null) {
			throw new RuntimeException("Could not dump view");
		}
		return findViewByCode(hashCode, rootNode);
	}

	/**
	 * Find a view by hashCode, starting from the given root node
	 * 
	 * @param hashCode
	 *            hashCode of the view you're looking for
	 * @param rootNode
	 *            the ViewNode at which to begin the traversal
	 * @return view with the specified hashCode, or {@code null} if no view
	 *         found.
	 */

	public static ViewNode findViewByCode(String hashCode, ViewNode rootNode) {
		if (rootNode.hashCode.equals(hashCode)) {
			return rootNode;
		}

		for (ViewNode child : rootNode.children) {
			ViewNode found = findViewByCode(hashCode, child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Gets the window that currently receives the focus.
	 * 
	 * @return the window that currently receives the focus.
	 */
	public Window getFocusedWindow(String packageName) {
		setAppBroken(true);
		int id = DeviceBridge.getFocusedWindow(mDevice);
		Window[] windows = DeviceBridge.loadWindows(mDevice);
		for (Window w : windows) {
			if (w.getHashCode() == id)
				appWindowN = w;
			if (w.getTitle().contains(packageName))
				setAppBroken(false);
		}
		setWidgetList(new ArrayList<String>());
		return appWindowN;
	}

	/**
	 * Gets the window that currently receives the focus.
	 * 
	 * @return name of the window that currently receives the focus.
	 */
	public String getFocusedWindowName() {
		int id = DeviceBridge.getFocusedWindow(mDevice);
		Window[] windows = DeviceBridge.loadWindows(mDevice);
		for (Window w : windows) {
			if (w.getHashCode() == id) {
				appWindowN = w;
				return w.getTitle();
			}
		}
		System.out.println("current focused window:" + appWindowN);
		return null;
	}

	/**
	 * Gets the absolute x/y position of the view node.
	 * 
	 * @param node
	 *            view node to find position of.
	 * @return point specifying the x/y position of the node.
	 */
	public static Point getAbsolutePositionOfView(ViewNode node) {
		int x = node.left;
		int y = node.top;
		ViewNode p = node.parent;
		while (p != null) {
			x += p.left - p.scrollX;
			y += p.top - p.scrollY;
			p = p.parent;
		}
		return new Point(x, y);
	}

	/**
	 * Gets the absolute x/y center of the specified view node.
	 * 
	 * @param node
	 *            view node to find position of.
	 * @return absolute x/y center of the specified view node.
	 */
	public static Point getAbsoluteCenterOfView(ViewNode node) {
		Point point = getAbsolutePositionOfView(node);
		return new Point(point.x + (node.width / 2), point.y
				+ (node.height / 2));
	}

	/**
	 * Gets the visibility of a given element.
	 * 
	 * @param selector
	 *            selector for the view.
	 * @return True if the element is visible.
	 */
	public boolean visible(ViewNode node) {
		boolean ret = (node != null)
				&& node.namedProperties.containsKey("getVisibility()")
				&& "VISIBLE".equalsIgnoreCase(node.namedProperties
						.get("getVisibility()").value);
		return ret;

	}

	/**
	 * Gets the text of a given element.
	 * 
	 * @param selector
	 *            selector for the view.
	 * @return the text of the given element.
	 */
	public String getText(ViewNode node) {
		if (node == null) {
			throw new RuntimeException("Node not found");
		}
		ViewNode.Property textProperty = node.namedProperties.get("text:mText");
		if (textProperty == null) {
			throw new RuntimeException("No text property on node");
		}
		return textProperty.value;
	}

	public static String getTime() {
		String time = "";
		Date now = new Date();
		DateFormat d = DateFormat.getDateTimeInstance();
		time = d.format(now);
		return time;
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
}
