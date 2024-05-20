package org.eclipse.linuxtools.internal.gprof;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.callgraph.core.CallgraphCorePlugin;

public final class PluginConstants {

	private static String STAP_GRAPH_DEFAULT_IO_PATH = ""; //$NON-NLS-1$

	public static String getDefaultIOPath(IProject project) {
		if (project != null) {
			URI locationURI = project.getLocationURI();
			if (locationURI != null) {
				STAP_GRAPH_DEFAULT_IO_PATH = locationURI.getPath() + "/callgraph.out"; //$NON-NLS-1$
			}
		} else {
			STAP_GRAPH_DEFAULT_IO_PATH = CallgraphCorePlugin.getDefault().getStateLocation().toString()
					+ "/callgraph.out"; //$NON-NLS-1$
		}

		if (STAP_GRAPH_DEFAULT_IO_PATH.startsWith("/")) {
			STAP_GRAPH_DEFAULT_IO_PATH = STAP_GRAPH_DEFAULT_IO_PATH.substring(1); // 删除第一个字符（即前导斜杠）
		}

		return STAP_GRAPH_DEFAULT_IO_PATH;
	}
}

