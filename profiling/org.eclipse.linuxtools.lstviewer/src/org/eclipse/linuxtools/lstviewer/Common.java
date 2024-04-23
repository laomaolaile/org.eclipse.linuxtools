package org.eclipse.linuxtools.lstviewer;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Common {
	
	public static IFile findFileFromPath(String textfile)
    {
		Path file = new Path(textfile);
		// can't use a null or empty path below
    	if (file == null || file.isEmpty()) {
    		return null;
    	}
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        /*
         * Find resource to which this GCOV file maps to in workspace
         */
        IFile files[] = root.findFilesForLocationURI(URIUtil.toURI(file));
        if ((files.length == 0) || (files.length > 1))
        {
            /*
             * Nothing found or more than one GCOV output file
             * Let user decide what to use as textary
             */
            return (null);
        }
        return files[0];
    }
	
	public static String getDefaultBinary(IProject project) {
        if (project != null && project.exists()) {
            ICProject cproject = CoreModel.getDefault().create(project);
            if (cproject != null) {
                try {
                    IBinary[] b = cproject.getBinaryContainer()
                            .getBinaries();
                    if (b != null && b.length > 0 && b[0] != null) {
                        IResource r = b[0].getResource();
                        return r.getLocation().toOSString();
                    }
				} catch (CModelException e) {
                }
            }
        }
        return ""; //$NON-NLS-1$
    }
	
	public String getDefaultBinary(IPath file) {
        IProject project = null;
        IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(file);
        if (c != null) {
            project = c.getProject();
            if (project != null && project.exists()) {
                ICProject cproject = CoreModel.getDefault().create(project);
                if (cproject != null) {
                    try {
                        IBinary[] b = cproject.getBinaryContainer()
                                .getBinaries();
                        if (b != null && b.length > 0 && b[0] != null) {
                            IResource r = b[0].getResource();
                            return r.getLocation().toOSString();
                        }
					} catch (CModelException e) {
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

}
