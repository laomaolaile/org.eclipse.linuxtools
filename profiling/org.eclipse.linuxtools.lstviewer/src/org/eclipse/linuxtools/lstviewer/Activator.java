package org.eclipse.linuxtools.lstviewer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin implements IStartup {
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lstviewer";
	private static Activator plugin;


	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		plugin.getPreferenceStore().setToDefault("enableExtlibFilter");
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

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
