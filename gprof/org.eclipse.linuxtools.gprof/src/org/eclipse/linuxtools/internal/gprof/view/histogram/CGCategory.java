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
package org.eclipse.linuxtools.internal.gprof.view.histogram;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.gprof.symbolManager.CallGraphArc;

/**
 * Tree node displaying "parents" or "children". Used to distinguish input arcs from output arcs in viewer
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class CGCategory extends AbstractTreeElement {

    public final static String PARENTS = "parents"; //$NON-NLS-1$
    public final static String CHILDREN = "children"; //$NON-NLS-1$

    public final String category;
    private final LinkedList<TreeElement> children = new LinkedList<>();

    /**
     * Constructor
     *
     * @param parent
     *            the parent of this tree node
     * @param category
     *            the category (one of {@link #PARENTS}, {@link #CHILDREN} )
     * @param list
     *            the children (or parents) of the function
     */
    public CGCategory(HistFunction parent, String category, LinkedList<CallGraphArc> list) {
        super(parent);
        this.category = category;
        for (CallGraphArc arc : list) {
            children.addFirst(new CGArc(this, arc));
        }
    }

    @Override
    public LinkedList<? extends TreeElement> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return category;
    }

	@Override
	public String getAddr() {
		// TODO Auto-generated method stub
		return null;
	}
}
