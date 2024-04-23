package org.eclipse.linuxtools.lstviewer.actions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.linuxtools.lstviewer.Activator;
import org.eclipse.linuxtools.lstviewer.CustomTextView;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SearchTextAction extends SelectionListenerAction {
	private final Shell shell;
	private CustomTextView customTextView;  
	
	static ImageDescriptor search_icon = ImageDescriptor
            .createFromURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("icons/" + "search.png"));

	public SearchTextAction(Shell shell,CustomTextView customTextView) {
		super("Find keywords in Text File"); //$NON-NLS-1$
		this.shell = shell;
		
		this.customTextView = customTextView;

		setToolTipText("Find keywords in Text File"); //$NON-NLS-1$
		setDisabledImageDescriptor(search_icon);
		
		setImageDescriptor(search_icon);
	}

	@Override
	public void run() {
		InputDialog dialog = new InputDialog(this.shell, "Search", "Enter the text to search:", "", null);
        if (dialog.open() == Window.OK) {
            String searchText = dialog.getValue();
            customTextView.highlightKeyword(searchText);
        }
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection);
	}

}
