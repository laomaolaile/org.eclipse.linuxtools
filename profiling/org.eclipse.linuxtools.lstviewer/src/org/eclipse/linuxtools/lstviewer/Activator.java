package org.eclipse.linuxtools.lstviewer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class Activator extends AbstractUIPlugin implements IStartup {
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lstviewer";
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}


	public static void log(IStatus status) {
		log(status, false);
	}

	public static void log(IStatus status, boolean pop) {
		getDefault().getLog().log(status);
		if (pop) {
			// ErrorDialog.openError(new Shell(), null, null, status);
		}
	}

	@Override
	public void earlyStartup() {
		// TODO Auto-generated method stub
		
	}





}
