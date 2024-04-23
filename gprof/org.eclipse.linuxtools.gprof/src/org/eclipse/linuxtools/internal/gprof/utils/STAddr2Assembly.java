/*******************************************************************************
 * Copyright (c) 2009, 2019 STMicroelectronics and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.utils;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gprof.Activator;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.riscvstudio.ide.tools.riscv.texteditor.CustomTextView;
import org.riscvstudio.ide.tools.riscv.texteditor.LstObject;


/**
 * This class provides a support for link-to-source
 *
 */
public final class STAddr2Assembly {

	private static CustomTextView customTextView;

	private STAddr2Assembly() {
    }

    /**
	 * Open a C Editor at the given location.
	 *
	 * @param project   The parent project.
	 * @param sourceLoc The location of the source file.
	 * @param addr      The line to open at.
	 * @return <code>true</code> if the link-to-source was successful,
	 *         <code>false</code> otherwise
	 */
	public static LstObject openSourceFileAtLocation(IProject project, IPath sourceLoc, String addr) {
		return openFileImpl(project, sourceLoc, addr);
    }

	private static LstObject openFileImpl(IProject project, IPath sourceLoc, String addr) {
		if (sourceLoc == null || "??".equals(sourceLoc.toString())) { //$NON-NLS-1$
			return null;
        }
		IFile f = getFileForPath(sourceLoc, project);
        if (f != null && f.exists()) {
			IViewPart viewEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView("org.riscvstudio.ide.tools.riscv.texteditor.CustomTextView"); //$NON-NLS-1$

			if (viewEditor == null) {
				try {
					viewEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView("org.riscvstudio.ide.tools.riscv.texteditor.CustomTextView"); //$NON-NLS-1$
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (viewEditor instanceof CustomTextView) {
				customTextView = (CustomTextView) viewEditor;
				if (customTextView != null) {
					LstObject lstObject = customTextView.openFile(f);

					if (addr != null) {
						customTextView.gotoLine(customTextView.findLineNumberForAddr(addr));
					}

					return lstObject;
				}
			}
        }
		return null;
    }




    /**
     * @param path The path of the file.
     * @param project The project to look into.
     * @return The file if found, null otherwise.
     * @since 5.0
     */
    public static IFile getFileForPath(IPath path, IProject project) {
        IFile f = getFileForPathImpl(path, project);
        if (f == null) {
            Set<IProject> allProjects = new HashSet<>();
            try {
                getAllReferencedProjects(allProjects, project);
            } catch (CoreException e) {
                Activator.getDefault().getLog().log(e.getStatus());
            }
            if (allProjects != null) {
                for (IProject project2 : allProjects) {
                    f = getFileForPathImpl(path, project2);
                    if (f != null) {
                        break;
                    }
                }
            }
        }
        return f;
    }

	private static IFile getFileForPathImpl(IPath path, IProject project) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        if (path.isAbsolute()) {
        	//FIXME EK-LINUXTOOLS: return root.getFileForLocation(path);
            return STSymbolManager.sharedInstance.findFileFromPath(path);
        }
        if (project != null && project.exists()) {

            ICProject cproject = CoreModel.getDefault().create(project);
            if (cproject != null) {
                try {
                    ISourceRoot[] roots = cproject.getAllSourceRoots();
                    for (ISourceRoot sourceRoot : roots) {
                        IContainer r = sourceRoot.getResource();
                        IResource res = r.findMember(path);
                        if (res != null && res.exists() && res instanceof IFile file) {
                            return file;
                        }
                    }

                    IOutputEntry entries[] = cproject.getOutputEntries();
                    for (IOutputEntry pathEntry : entries) {
                        IPath p = pathEntry.getPath();
                        IResource r = root.findMember(p);
                        if (r instanceof IContainer parent) {
                            IResource res = parent.findMember(path);
                            if (res != null && res.exists() && res instanceof IFile file) {
                                return file;
                            }
                        }
                    }

                } catch (CModelException e) {
                    Activator.getDefault().getLog().log(e.getStatus());
                }
            }
        }
        return null;
    }

    private static void getAllReferencedProjects(Set<IProject> all, IProject project) throws CoreException {
        if (project != null) {
            IProject[] refs = project.getReferencedProjects();
            for (IProject ref : refs) {
                if (!all.contains(ref) && ref.exists() && ref.isOpen()) {
                    all.add(ref);
                    getAllReferencedProjects(all, ref);
                }
            }
        }
    }

}
