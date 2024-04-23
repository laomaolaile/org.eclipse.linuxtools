package org.eclipse.linuxtools.lstviewer;


import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


public class LstOperation {

	public static void openSourceCode(IFile openFilePath, long addr) {
		IProject project = openFilePath.getProject();
		
		if(project != null) {
			String binaryPath = Common.getDefaultBinary(project);
			
			if(binaryPath != null) {
				IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));
		        IAddress parentAddress = binary.getAddressFactory().createAddress(Long.toString(addr));
		        if(parentAddress != null) {
		        	// TODO Auto-generated method stub
					String lintName = STSymbolManager.sharedInstance.getFileName(binary, parentAddress, project);
			        int lintNum = STSymbolManager.sharedInstance.getLineNumber(binary, parentAddress, project);
			        if(lintName != null) {
			        	setAttributeByLine(project,lintName,lintNum);
			        }
		        }
			}
		}
	}
	
	
	public static void setAttributeByLine(IProject project,String filePath, int lineNumber) {

        if (filePath != null && !filePath.isEmpty()) {
            try {
                IFile file = Common.findFileFromPath(filePath);
                IMarker marker = file.createMarker(IMarker.TEXT);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IDE.openEditor(page, marker);
                marker.delete();
            } catch (CoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}
