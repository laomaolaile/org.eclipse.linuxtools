package org.eclipse.linuxtools.internal.gprof;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.internal.callgraph.core.CallgraphCorePlugin;

public final class PluginConstants {

	private static String STAP_GRAPH_DEFAULT_IO_PATH = ""; //$NON-NLS-1$

	public static String getDefaultIOPath(IProject project) {
		if (project != null) {
			URI locationURI = project.getLocationURI();
			File file = new File(locationURI);
			if (locationURI != null) {
				STAP_GRAPH_DEFAULT_IO_PATH = file + "/callgraph.out"; //$NON-NLS-1$
			}
		} else {
			STAP_GRAPH_DEFAULT_IO_PATH = CallgraphCorePlugin.getDefault().getStateLocation().toString()
					+ "/callgraph.out"; //$NON-NLS-1$
		}

		return STAP_GRAPH_DEFAULT_IO_PATH;
	}
}

