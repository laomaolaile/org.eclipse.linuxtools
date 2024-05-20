/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.action;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


public class OpenCallGraphAction implements IEditorLauncher {

    @Override
	public void open(IPath path) {
		File file = path.toFile();

		if (file.exists()) {

			String filePath = file.getPath();
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(CallGraphConstants.VIEW_ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // $NON-NLS-1$
			StapGraphParser new_parser = new StapGraphParser();
			new_parser.setViewID(CallGraphConstants.VIEW_ID);
			new_parser.setSourcePath(filePath);
			new_parser.schedule();
		}
    }
}
