package org.eclipse.linuxtools.lstviewer;


import java.io.IOException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.binutils.utils.STBinutilsFactoryManager;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;


public class LstOperation {

	public static void openSourceCode(IFile openFilePath, long addr) {
		IProject project = openFilePath.getProject();
		
		Addr2line addr2line = null;
		
		if(project != null) {
			String binaryPath = Common.getDefaultBinary(project);
			
			if(binaryPath != null) {
				IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binaryPath));
				
				if(binary != null) {
	    			try {
	    				addr2line = STBinutilsFactoryManager.getAddr2line(binary.getCPU(), binary.getPath().toOSString(), project);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    		}

		        IAddress parentAddress = binary.getAddressFactory().createAddress(Long.toString(addr));
		        if(parentAddress != null && addr2line != null) {
		        	// TODO Auto-generated method stub
					String lineName = STSymbolManager.sharedInstance.getFileName(binary, parentAddress, project);
			        int lineNum = STSymbolManager.sharedInstance.getLineNumber(binary, parentAddress, project);
			        if(lineName != null) {
			        	setAttributeByLine(project,lineName,lineNum);
			        }
		        }
			}
		}
	}
	
	
	public static void setAttributeByLine(IProject project, String filePath, int lineNumber) {

        if (filePath != null && !filePath.isEmpty()) {
            try {
                IFile file = Common.findFileFromPath(filePath);
                
                if(file != null) {
                	IMarker marker = file.createMarker(IMarker.TEXT);
                    marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    IDE.openEditor(page, marker);
                    marker.delete();
                }
            } catch (CoreException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}
