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
package org.eclipse.linuxtools.internal.gprof.symbolManager;

import org.riscvstudio.ide.tools.riscv.texteditor.Lst;

/**
 * Bucket structure.
 * used to display bucket info relative to each symbol.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class Bucket {

    /** Start address of this bucket */
    public final long startAddr;
    /** End address of this bucket */
    public final long endAddr;
    /** time spent in this bucket */
    public final int  time;

	public final Lst lst;

    /**
     * Constructor
     * @param startAddr
     * @param endAddr
     * @param time
     */
	public Bucket(long startAddr, long endAddr, int time, Lst lst) {
        this.startAddr = startAddr;
        this.endAddr   = endAddr;
        this.time       = time;
		this.lst = lst;
    }

}
