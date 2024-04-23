package org.eclipse.linuxtools.lstviewer.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.linuxtools.lstviewer.Activator;
import org.eclipse.linuxtools.lstviewer.Common;
import org.eclipse.linuxtools.lstviewer.CustomTextView;
import org.eclipse.linuxtools.lstviewer.dialog.OpenTextDialog;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class OpenTextAction extends SelectionListenerAction {
	
	private CustomTextView customTextView;  
	
	static ImageDescriptor search_icon = ImageDescriptor
            .createFromURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("icons/" + "open.png"));

	public OpenTextAction(Shell shell,CustomTextView customTextView) {
		super("Open"); //$NON-NLS-1$

		setToolTipText("Open Text File"); //$NON-NLS-1$
		setDisabledImageDescriptor(search_icon);
		setImageDescriptor(search_icon);
		
		this.customTextView = customTextView;
	}

	@Override
	public void run() {
		
		IFile  openfile = customTextView.getOpenFilePath();
		
		String textStr = null;
		if(openfile != null) {
			textStr = customTextView.getOpenFilePath().getLocationURI().getPath();
		}
		
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        OpenTextDialog textdialog = new OpenTextDialog(shell, textStr);
        if (textdialog.open() != Window.OK) {
            return;
        }
        String textPath = textdialog.gettextaryFile();
        
        IFile f = Common.findFileFromPath(textPath);
        if (f != null) {
        	customTextView.openFile(Common.findFileFromPath(textPath));
        }

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection);
	}

}
