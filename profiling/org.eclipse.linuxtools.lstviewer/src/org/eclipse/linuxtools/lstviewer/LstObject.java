package org.eclipse.linuxtools.lstviewer;

import java.util.HashMap;
import java.util.Map;



public class LstObject {


	private String lstpath;
    
	private String lstname;

	private Map<Long, Lst> lstMap = new HashMap<>();

	public String getLstpath() {
		return lstpath;
	}

	public void setLstpath(String filePath) {
		this.lstpath = filePath;
	}

	public String getLstname() {
		return lstname;
	}

	public void setLstname(String lstname) {
		this.lstname = lstname;
	}

	public Map<Long, Lst> getLstMap() {
		return lstMap;
	}

	public void setLstMap(Map<Long, Lst> lstMap) {
		this.lstMap = lstMap;
	}  

    
}
