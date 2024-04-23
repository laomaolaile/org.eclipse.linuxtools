package org.eclipse.linuxtools.lstviewer;

/**
 * Bucket structure.
 * used to display bucket info relative to each symbol.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class Lst {


    public final long startAddr;

    public final int line;
    
    public final String inscode;

    public final String instraction;
    
    public final String explain;

    /**
     * Constructor
     * @param startAddr
     * @param endAddr
     * @param time
     */
    public Lst(long startAddr, int line, String inscode, String instraction,String explain) {
        this.startAddr = startAddr;
        this.line = line;
        this.inscode   = inscode;
        this.instraction       = instraction;
        this.explain       = explain;
    }
    
    public long getStartAddr(){
    	return this.startAddr;
    }
    
    public int getLine(){
    	return this.line;
    }
    
    public String getInscode(){
    	return this.inscode;
    }
    
    public String getInstraction(){
    	return this.instraction;
    }
    public String getExplain(){
    	return this.explain;
    }

}
