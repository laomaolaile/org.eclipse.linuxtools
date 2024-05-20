package org.eclipse.linuxtools.internal.gprof.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.gprof.PluginConstants;
import org.eclipse.linuxtools.internal.gprof.parser.GmonDecoder;
import org.eclipse.linuxtools.internal.gprof.view.CallGraphContentProvider;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGArc;
import org.eclipse.linuxtools.internal.gprof.view.histogram.CGCategory;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFile;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistFunction;
import org.eclipse.linuxtools.internal.gprof.view.histogram.HistRoot;
import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;

public class GprofCallGraph {

	private static LinkedHashMap<String, Object> allParentNode = new LinkedHashMap<>();
	private static LinkedHashMap<String, Object> allChildrenNode = new LinkedHashMap<>();
	private static CallGraphContentProvider cgprovider = CallGraphContentProvider.sharedInstance;
	private static int i = 1;
	private static int calltime = 1;
	private static File callGraphFile = null;
	private static IProject project;

	public static void makeData(GmonDecoder decoder) throws IOException {
		allParentNode.clear();
		allChildrenNode.clear();

		project = decoder.getProject();
		HistRoot root = decoder.getRootNode();

		getChildren(root);

		removeRootNode();

		callGraphFile = mkCallGraphFile();

		if (callGraphFile == null)
			return;

		appendStartToFile();

		for (Map.Entry<String, Object> entry : allParentNode.entrySet()) {
			prantData(getHistFunctionFromRoot(entry.getValue()), false);
		}
	}

	public static void getParent(Object node) {
		Object[] cgchildrens = cgprovider.getChildren(node);

		String key = null;
		for (Object children : cgchildrens) {
			GprofCallGraphNode nodeinfo = getNodeInfo(children);
			key = nodeinfo.getNodeName();

			if (key != null && !key.equals("parents") && !key.equals("children")) { //$NON-NLS-1$ //$NON-NLS-2$
				allParentNode.put(key, children);
			}

		}
	}

	public static void getChildren(Object node) {
		Object[] cgchildrens = cgprovider.getChildren(node);

		if (cgchildrens == null)
			return;

		String key = null;
		for (Object children : cgchildrens) {

			GprofCallGraphNode nodeinfo = getNodeInfo(children);

			key = nodeinfo.getNodeName();

			if (key != null && !key.equals("parents") && !key.equals("children") && hasParent(children)) { //$NON-NLS-1$ //$NON-NLS-2$
				allChildrenNode.put(key, children);
			}

			if (key.equals("parents")) { //$NON-NLS-1$
				getParent(children);
			} else {
				getChildren(children);
			}

		}
	}

	public static boolean hasParent(Object node) {

		if (node == null)
			return false;

		Object[] cgchildrens = cgprovider.getChildren(node);

		String key = null;

		if (cgchildrens != null && cgchildrens.length > 0) {
			for (Object children : cgchildrens) {
				GprofCallGraphNode nodeinfo = getNodeInfo(children);
				key = nodeinfo.getNodeName();
				if (key.equals("parents")) //$NON-NLS-1$
					return true;
			}
		}

		return false;
	}

	public static void removeRootNode() {
		// 遍历allParentNode的键集合
		for (String key : new LinkedHashSet<>(allParentNode.keySet())) { // 使用LinkedHashSet保持迭代顺序
			// 检查键是否也存在于allChildrenNode中
			if (allChildrenNode.containsKey(key)) {
				// 如果存在，则从allParentNode中删除该键值对
				allParentNode.remove(key);
			}
		}
	}

	public static File mkCallGraphFile() {

		String callGraphPath = getDefaultIOPath();

		File file = new File(callGraphPath);

		if (file.exists()) {
			file.deleteOnExit();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	// 专门用于追加内容到文件的方法
	public static void appendStartToFile() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(callGraphFile, false))) {
			writer.write("PROBE_BEGIN"); //$NON-NLS-1$
			writer.newLine(); // 如果需要换行，可以调用newLine()方法
			writer.flush(); // 刷新缓冲区以确保内容被写入
		} catch (IOException e) {
			e.printStackTrace();
			// 处理异常，例如通过抛出异常或记录错误日志
		}
		try {
			project.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 专门用于追加内容到文件的方法
	public static void appendToFile(String content) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(callGraphFile, true))) {
			writer.write(content);
			writer.newLine(); // 如果需要换行，可以调用newLine()方法
			writer.flush(); // 刷新缓冲区以确保内容被写入
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			// 处理异常，例如通过抛出异常或记录错误日志
		}
	}

	public static void prantData(Object object, boolean callself) throws IOException {
		String nodeName = null;
		int calls = 1;
		int samples = 0;

		GprofCallGraphNode nodeinfo = getNodeInfo(object);

		nodeName = nodeinfo.getNodeName();
		calls = nodeinfo.getNodeCall();
		samples = nodeinfo.getSamples();

		calls = calls == 0 ? 1 : calls;

		if (nodeName != null) {
			// for (int i = 0; i < calls; i++) {
				StringBuilder scallgraph = new StringBuilder("<"); //$NON-NLS-1$
				scallgraph.append(nodeName);
				scallgraph.append(",,"); //$NON-NLS-1$
				scallgraph.append(i++);
				scallgraph.append(",,"); //$NON-NLS-1$
				scallgraph.append(calltime);
				scallgraph.append(",,"); //$NON-NLS-1$
				scallgraph.append(1);
				scallgraph.append(",,"); //$NON-NLS-1$
				scallgraph.append(calls);

				appendToFile(scallgraph.toString());
				if (!callself)
					getNodeChildren(object);

				calltime = calltime + samples;

				StringBuilder ecallgraph = new StringBuilder(">"); //$NON-NLS-1$
				ecallgraph.append(nodeName);
				ecallgraph.append(",,"); //$NON-NLS-1$
				ecallgraph.append(calltime);
				ecallgraph.append(",,"); //$NON-NLS-1$
				ecallgraph.append(1);
				scallgraph.append(",,"); //$NON-NLS-1$
				appendToFile(ecallgraph.toString());
				// }
			
		}
	}

	public static void getNodeChildren(Object node) throws IOException {

		node = getHistFunctionFromRoot(node);

		Object[] cgchildrens = cgprovider.getChildren(node);
		String nodeName = null;

		GprofCallGraphNode nodeinfo = getNodeInfo(node);

		nodeName = nodeinfo.getNodeName();

		String key = null;

		if (cgchildrens != null && cgchildrens.length > 0) {

			for (Object children : cgchildrens) {

				GprofCallGraphNode snodeinfo = getNodeInfo(children);

				key = snodeinfo.getNodeName();

				if (key != null && !key.equals("parents")) { //$NON-NLS-1$
					if (key.equals("children")) { //$NON-NLS-1$
						Object[] cchildrens = cgprovider.getChildren(children);
						for (Object clild : cchildrens) {

							String ckey = null;
							GprofCallGraphNode ssnodeinfo = getNodeInfo(clild);
							ckey = ssnodeinfo.getNodeName();
							if (ckey != null && ckey.equals(nodeName)) {
								prantData(clild, true);
							} else {
								prantData(clild, false);
							}
						}
					} else {
						prantData(children, false);
					}
				}

			}
		}
	}

	@SuppressWarnings("unused")
	public static Object getHistFunctionFromRoot(Object node) {

		TreeElement root = null;

		String nodeName = null;

		GprofCallGraphNode nodeinfo = getNodeInfo(node);

		nodeName = nodeinfo.getNodeName();

		root = nodeinfo.getRoot();

		Object[] cgchildrens = cgprovider.getChildren(root);
		for (Map.Entry<String, Object> entry : allParentNode.entrySet()) {
			String key = null;
			for (Object children : cgchildrens) {

				GprofCallGraphNode snodeinfo = getNodeInfo(children);

				key = snodeinfo.getNodeName();

				if (key != null && key.equals(nodeName)) {
					return children;
				}
			}

		}
		return null;

	}

	public static String getDefaultIOPath() {
		return PluginConstants.getDefaultIOPath(project); // $NON-NLS-1$

	}

	private static GprofCallGraphNode getNodeInfo(Object object) {
		GprofCallGraphNode cnode = new GprofCallGraphNode();
		if (object instanceof CGArc) {
			CGArc function = (CGArc) object;
			cnode.setNodeName(function.getFunctionName());
			cnode.setNodeCall(function.getCalls());
			cnode.setSamples(function.getSamples());
			cnode.setRoot(function.getRoot());

		} else if (object instanceof CGCategory) {
			CGCategory function = (CGCategory) object;
			cnode.setNodeName(function.getName());
			cnode.setNodeCall(function.getCalls());
			cnode.setSamples(function.getSamples());
			cnode.setRoot(function.getRoot());

		} else if (object instanceof HistFunction) {
			HistFunction function = (HistFunction) object;
			cnode.setNodeName(function.getName());
			cnode.setNodeCall(function.getCalls());
			cnode.setSamples(function.getSamples());
			cnode.setRoot(function.getRoot());

		} else if (object instanceof HistFile) {
			HistFile function = (HistFile) object;
			cnode.setNodeName(function.getName());
			cnode.setNodeCall(function.getCalls());
			cnode.setSamples(function.getSamples());
			cnode.setRoot(function.getRoot());
		}

		return cnode;
	}

}
