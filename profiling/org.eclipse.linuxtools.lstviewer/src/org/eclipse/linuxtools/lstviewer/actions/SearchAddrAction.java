package org.eclipse.linuxtools.lstviewer.actions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.linuxtools.lstviewer.Activator;
import org.eclipse.linuxtools.lstviewer.CustomTextView;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class SearchAddrAction extends SelectionListenerAction {
	private CustomTextView customTextView;  
	
	static ImageDescriptor search_icon = ImageDescriptor
            .createFromURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry("icons/" + "search.png"));

	public SearchAddrAction(Shell shell,CustomTextView customTextView) {
		super("Find keywords in Text File"); //$NON-NLS-1$
		this.customTextView = customTextView;
		setToolTipText("Find keywords in Text File"); //$NON-NLS-1$
		setDisabledImageDescriptor(search_icon);
		setImageDescriptor(search_icon);
	}

	@Override
	public void run() {
		AddressBarContributionItem addressBar = this.customTextView.getAddressBar();
		
		if (addressBar != null && addressBar.isEnabled()) {
			String addrTxt = addressBar.getText();

			if (addrTxt == null || addrTxt.trim().length() == 0)
				return;
			addrTxt = addrTxt.trim();

			if (addrTxt.equals("Enter Addr here")) {
				return;
			}
			
			if (addrTxt.startsWith("0x")) {
				addrTxt = addrTxt.substring(2);
            }
			customTextView.highlightKeyword(addrTxt);
		}

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection);
	}

}
