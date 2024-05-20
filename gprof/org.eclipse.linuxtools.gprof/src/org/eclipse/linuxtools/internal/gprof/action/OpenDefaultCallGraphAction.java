/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.eclipse.linuxtools.internal.gprof.Activator;
import org.eclipse.linuxtools.internal.gprof.PluginConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * This action changes the content provider of
 * the {@link org.eclipse.linuxtools.internal.gprof.view.GmonView}
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class OpenDefaultCallGraphAction extends Action {

	private final IProject project;

    /**
	 * Constructor
	 * 
	 * @param name     of the action
	 * @param iconPath the icon path
	 * @param viewer   TreeViewer
	 * @param project  the new content provider for the given TreeViewer
	 */
	public OpenDefaultCallGraphAction(String name, String iconPath, IProject project) {
        super(name, AS_RADIO_BUTTON);
        this.setImageDescriptor(Activator.getImageDescriptor(iconPath));
        this.setToolTipText(name);
		this.project = project;
    }

    @Override
    public void run() {

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CallGraphConstants.VIEW_ID);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // $NON-NLS-1$

		if (this.project != null) {
			StapGraphParser new_parser = new StapGraphParser();
			new_parser.setViewID(CallGraphConstants.VIEW_ID);
			new_parser.setSourcePath(PluginConstants.getDefaultIOPath(project));
			new_parser.schedule();
		}

    }

}
