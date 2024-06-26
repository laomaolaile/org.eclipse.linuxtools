package org.eclipse.linuxtools.lstviewer.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.lstviewer.Common;
import org.eclipse.linuxtools.lstviewer.CustomTextView;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action performed when user clicks on a gmon file
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class OpenLstAction implements IEditorLauncher {

	@Override
	public void open(IPath file) {
		if (file != null) {

			IFile f = Common.findFileFromPath(file.toString());

			if (f != null && f.exists()) {
				IViewPart viewEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.findView("org.eclipse.linuxtools.lstviewer.CustomTextView"); //$NON-NLS-1$

				if (viewEditor == null) {
					try {
						viewEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.showView("org.eclipse.linuxtools.lstviewer.CustomTextView"); //$NON-NLS-1$
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (viewEditor instanceof CustomTextView) {
					CustomTextView customTextView = (CustomTextView) viewEditor;
					if (customTextView != null) {
						customTextView.openFile(f);
					}
				}
			}

		}

	}

}
