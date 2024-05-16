package org.eclipse.linuxtools.internal.gprof.utils;

import org.eclipse.linuxtools.internal.gprof.view.histogram.TreeElement;

public class GprofCallGraphNode {

	private String nodeName;

	private int nodeCall;

	private int samples;

	private TreeElement root;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getNodeCall() {
		return nodeCall;
	}

	public void setNodeCall(int nodeCall) {
		this.nodeCall = nodeCall;
	}

	public int getSamples() {
		return samples;
	}

	public void setSamples(int samples) {
		this.samples = samples;
	}

	public TreeElement getRoot() {
		return root;
	}

	public void setRoot(TreeElement root) {
		this.root = root;
	}

}
