package org.eclipse.linuxtools.lstviewer.dialog;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.lstviewer.Common;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * This dialog box is opened when user clicks on a gmon file. it alows the user to choose the textary file who produced
 * the gmon file.
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 *
 */
public class OpenTextDialog extends Dialog {

    /* Inputs */
    private Text fileText;
    private String textValue;

    /* error label */
    private Label errorLabel;

    /* validation boolean */
    private boolean textaryValid;

	private IPath textPath;
	private String textFile;

    /**
     * Constructor
     *
     * @param parentShell
     * @param textPath
     *            the path to a textary file.
     */
    public OpenTextDialog(Shell parentShell, String defaulttextValue) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        
        this.textPath = null;
        if(defaulttextValue != null) {
        	this.textPath = Common.findFileFromPath(defaulttextValue).getFullPath();
        }
		
		this.textFile = defaulttextValue;

    }

    /**
     * Gets the textary file selected by the user
     *
     * @return a path to a textary file
     */
    public String gettextaryFile() {
        return textValue;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control composite = super.createContents(parent);
        validateText();
        return composite;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        this.getShell().setText("Open a Text file");
        
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
		
		{
			GridData data = new GridData(GridData.FILL_BOTH);

			Label textLabel = new Label(composite, SWT.NONE);
			textLabel.setText("Please enter the file whick want to open");
			data = new GridData();
			data.horizontalSpan = 3;
			textLabel.setLayoutData(data);

			fileText = new Text(composite, SWT.BORDER);
			if(this.textFile != null) fileText.setText(this.textFile);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 300;
			fileText.setLayoutData(data);
			fileText.addModifyListener(new textaryModifyListener());

			Composite cbtext = new Composite(composite, SWT.NONE);
			data = new GridData(GridData.HORIZONTAL_ALIGN_END);
			cbtext.setLayoutData(data);
			cbtext.setLayout(new GridLayout(2, true));
			Button textBrowseWorkspaceButton = new Button(cbtext, SWT.PUSH);
			textBrowseWorkspaceButton.setText("Workspace");
			textBrowseWorkspaceButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(
					e -> handleBrowseWorkspace("Workspace", fileText)));
			Button textBrowseFileSystemButton = new Button(cbtext, SWT.PUSH);
			textBrowseFileSystemButton.setText("File System");
			textBrowseFileSystemButton.addSelectionListener(SelectionListener
					.widgetSelectedAdapter(e -> handleBrowse("File System", fileText)));

			composite.layout();
		}
		{
			/* 2sd line */
			GridData data = new GridData(GridData.FILL_BOTH);
			errorLabel = new Label(composite, SWT.NONE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			errorLabel.setLayoutData(data);
			errorLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		}

        return composite;
    }

	private void validateText() {
        textValue = fileText.getText();
        IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
        try {
            textValue = mgr.performStringSubstitution(textValue, false);
        } catch (CoreException e) {
            // do nothing: never occurs
        }

        File f = new File(textValue);
        if (f.exists()) {
            textaryValid = true;
            getButton(IDialogConstants.OK_ID).setEnabled(textaryValid);
            errorLabel.setText(""); //$NON-NLS-1$
        } else {
            textaryValid = false;
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            if (!textValue.equals("")) { //$NON-NLS-1$
                errorLabel.setText("\"" + fileText.getText() + "\" " + " doesn't exist"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                errorLabel.setText("Please enter the file whick want to open.");
            }
            return;
        }
    }

    private class textaryModifyListener implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            validateText();
        }

    }


    @SuppressWarnings("unused")
	private void handleBrowseWorkspace(String msg, Text text) {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setTitle(msg);
        dialog.setMessage(msg);
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
        dialog.setAllowMultiple(false);
        IContainer c = null;
        if(this.textPath != null ) ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(this.textPath);
        if (c != null) {
            dialog.setInitialSelection(c.getProject());
        }
        dialog.setValidator(selection -> {
		    if (selection.length != 1) {
				return Status.error(""); //$NON-NLS-1$
		    }
		    if (!(selection[0] instanceof IFile)) {
				return Status.error(""); //$NON-NLS-1$
		    }
			return Status.OK_STATUS;
		});
        if (dialog.open() == IDialogConstants.OK_ID) {
            IResource resource = (IResource) dialog.getFirstResult();
            text.setText("${resource_loc:" + resource.getFullPath() + "}"); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    private void handleBrowse(String msg, Text text) {
        FileDialog dialog = new FileDialog(this.getShell(), SWT.OPEN);
        dialog.setText(msg);
        String t = text.getText();
        IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
        try {
            t = mgr.performStringSubstitution(t, false);
        } catch (CoreException e) {
            // do nothing: never occurs
        }
        File f = new File(t);
        t = f.getParent();
        if (t == null || t.length() == 0) {
           if(this.textPath != null) t = this.textPath.removeLastSegments(1).toOSString();
        }
        dialog.setFilterPath(t);
        String s = dialog.open();
        if (s != null) {
            text.setText(s);
        }
    }

}
