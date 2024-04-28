package org.eclipse.linuxtools.lstviewer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;

public class LstFileContent {
	
	
	private LstObject lst = new LstObject();
	
	private String regex = "([0-9A-Fa-f]{8}|[0-9A-Fa-f]{16}):(\\s|\\t)+(([0-9A-Fa-f]{4})|([0-9A-Fa-f]{8})|(([0-9A-Fa-f]{2}\\s){3}[0-9A-Fa-f]{2}))(\\s|\\t)+";
	
	private String instraregex = "<.*?>";
	
	public void readLstFile(IFile filePath) {

		if(filePath.exists()) {
			Map<Long, Lst> lstMap = new HashMap<>();
			
			String strfile = filePath.getRawLocation().toString();
			
			lst.setLstpath(strfile);
			
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Pattern pattern2 = Pattern.compile(instraregex, Pattern.CASE_INSENSITIVE);

			try (BufferedReader reader = new BufferedReader(new FileReader(strfile))) {  
	            String line; 
	            int l = 0;
	            while ((line = reader.readLine()) != null) {
	                if (line != null) { 
	                	Matcher matcher = pattern.matcher(line);
	                	while (matcher.find()) {
	                		long addr = Long.parseLong(matcher.group(1), 16);
	                		String inscode = matcher.group(3);
	                		
	                		String[] parts = line.split(inscode); 
	                		
	                		String instraction = null;
	                		
	                		String explain = null;
	                		
	                		if(parts.length > 1 && parts[1] != null) {
	                			String sparts = parts[1];
	                			
	                			Matcher matcher2 = pattern2.matcher(parts[1]);
	                			if(matcher2.find()) {
	                				explain = matcher2.group(0);
	                				sparts = sparts.replace(explain, "");
	                			}
	                			
	                			String[] instractions = sparts.split("#");
	                			
	                			if(instractions.length > 0)instraction = instractions[0].trim().replace("\t", "        ");;
	                			if(instractions.length > 1) explain = explain + "# " + instractions[1].trim().replace("\t", "        ");
	                		}

	                		lstMap.put(addr, new Lst(addr, l, inscode, instraction , explain));
	                	}
	                }
	                l++;
	            }  
	        } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

	        lst.setLstMap(lstMap);
		}

	}
	
	public LstObject getLstObject() {
		
		return this.lst;
	}

}
