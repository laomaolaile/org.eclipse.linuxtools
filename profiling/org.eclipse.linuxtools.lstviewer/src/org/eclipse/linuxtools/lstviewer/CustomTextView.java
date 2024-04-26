package org.eclipse.linuxtools.lstviewer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.lstviewer.actions.AddressBarContributionItem;
import org.eclipse.linuxtools.lstviewer.actions.OpenTextAction;
import org.eclipse.linuxtools.lstviewer.actions.SearchAddrAction;
import org.eclipse.linuxtools.lstviewer.actions.SearchTextAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;  
  
public class CustomTextView extends ViewPart {
  
	private SourceViewer sourceViewer;
	private IDocument document;
	private StyledText styledText;
	private Shell shell;
	
	private LstObject lstobject;
	
	private IFile openFilePath;
	
    private IAction openAction;  
    private IAction searchAction; 
    private IAction searchAddrAction;
    
    AddressBarContributionItem fAddressBar;
    
    private Menu contextMenu;  

    private String isAddrregex = "[0-9A-Fa-f]{8}|[0-9A-Fa-f]{16}"; 
    private String isLstRecordregex = "([0-9A-Fa-f]{8}|[0-9A-Fa-f]{16}):(\\s|\\t)+";
    
    
    
    Color fontcolor = new Color(new RGB(120, 120, 120));  
    Color backgroundcolor = new Color(new RGB(243,243,243)); 
    
    Color rhighlightBGColor = new Color(new RGB(255,255,255));  
    Color rhighlightFontColor = new Color(new RGB(0, 0, 0)); 
    
    Color highlightBGColor = new Color(new RGB(0,120,215));  
    Color highlightFontColor = new Color(new RGB(255, 255, 255)); 
    
  

	@Override
	public void createPartControl(Composite parent) {
		
		this.shell = parent.getShell();
		CompositeRuler ruler = new CompositeRuler();
		LineNumberRulerColumn lineCol = new LineNumberRulerColumn();

		Font font = new Font(parent.getDisplay(), new FontData("Monospace", 12, SWT.NORMAL)); //$NON-NLS-1$

		lineCol.setForeground(fontcolor);
		lineCol.setFont(font);
		lineCol.setBackground(backgroundcolor);

		ruler.addDecorator(0, lineCol);

		sourceViewer = new SourceViewer(parent, ruler, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		sourceViewer.configure(new SourceViewerConfiguration());
		styledText = sourceViewer.getTextWidget();
		styledText.setFont(font);
		styledText.setEditable(false);

		styledText.setSelectionBackground(highlightBGColor);
		
		IActionBars bars = getViewSite().getActionBars();  
        IToolBarManager toolbarmanager = bars.getToolBarManager(); 

        fillLocalToolBar(toolbarmanager);
        
        MenuManager menuManager = new MenuManager("#PopupMenu");  


        contextMenu = menuManager.createContextMenu(styledText);  
        contextMenu.setVisible(true);

        styledText.addKeyListener(new KeyAdapter() {
			@Override  
            public void keyPressed(KeyEvent e) {  
                if ((e.stateMask & SWT.CTRL) != 0 && e.keyCode == 'f') {  
                	InputDialog dialog = new InputDialog(shell, "Search", "Enter the text to search:", "", null);
                    if (dialog.open() == Window.OK) {
                        String searchText = dialog.getValue();
                        highlightKeyword(searchText);
                    }
                } 
            }  
        });  
        
        styledText.addMouseListener(new MouseListener() {  
            @Override  
            public void mouseUp(MouseEvent e) {  
                if (styledText.getSelectionCount() > 0) {  
                   openSourceCode(styledText.getSelectionText());
                   activateMyView();
                   contextMenu.setVisible(true);  
                   contextMenu.setLocation(e.x, e.y);  
                }  
            }  
  
            @Override  
            public void mouseDown(MouseEvent e) {  
            	if (e.button == 3) {
            		Point point = new Point(e.x, e.y);
            		controlMenuShown(contextMenu, styledText, point); 
                } 
            }  
  
            @Override  
            public void mouseDoubleClick(MouseEvent e) {  
            }
  
        }); 


	}
	
	protected void fillLocalToolBar(IToolBarManager manager) {

		openAction = new OpenTextAction(this.shell,this);
        searchAction = new SearchTextAction(this.shell,this);
        searchAddrAction = new SearchAddrAction(this.shell,this);

		final int ADDRESS_BAR_WIDTH = 190;
		ToolBar toolbar = ((ToolBarManager) manager).getControl();

		fAddressBar = new AddressBarContributionItem(searchAddrAction);
		fAddressBar.createAddressBox(toolbar, ADDRESS_BAR_WIDTH,"Enter Addr here","Specified Addr is invalid");
		
		manager.add(fAddressBar);
		manager.add(openAction);  
		manager.add(searchAction); 

	}
	
	public AddressBarContributionItem getAddressBar() {
		return fAddressBar;
	}
	
	private void fillContextMenu(IMenuManager manager) {  
        manager.add(new Separator());  
        manager.add(new Action("Copy") {  
            @Override  
            public void run() {
                copyTextToClipboard(styledText); 
            }  
        });  
    }
	
	private void controlMenuShown(Menu menu, Control control, Point point) {  
        menu.setLocation(control.toDisplay(point.x, point.y));  
        menu.setVisible(true);  
    } 
	
	private static void copyTextToClipboard(StyledText styledText) {  
        String selectedText = styledText.getSelectionText();
        if (selectedText != null && !selectedText.isEmpty()) {  
            Clipboard clipboard = new Clipboard(Display.getDefault());  
            TextTransfer textTransfer = TextTransfer.getInstance();  
            clipboard.setContents(new Object[] {selectedText}, new Transfer[] {textTransfer});  
            clipboard.dispose(); 
        }  
    }
	
	public void activateMyView() {  
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
        if (window != null) {  
            IWorkbenchPage[] pages = window.getPages();
            for(IWorkbenchPage page : pages ) {
            	if(page.findView("org.eclipse.linuxtools.lstviewer.CustomTextView") != null) {
            		IViewPart viewPart = null;
            		try {
    					viewPart = page.showView("org.eclipse.linuxtools.lstviewer.CustomTextView");
    				} catch (PartInitException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}  
    		        if (viewPart != null) {  
    		            page.activate(viewPart);  
    		            viewPart.setFocus();
    		        }  
            	}
            }
        }  
    }  
	
	public LstObject openFile(IFile filePath) {
		
		if(styledText.getCharCount() > 0 && this.openFilePath != null && (this.openFilePath ==filePath || this.openFilePath.getFullPath().equals(filePath.getFullPath()))) {
			return this.lstobject;
		}
		
		document = new Document();
		LstFileContent lst = new LstFileContent();
		if (filePath != null && this.openFilePath !=filePath) {
			this.openFilePath = filePath;
			try (InputStream contentStream = filePath.getContents()) {
				byte[] contentBytes = contentStream.readAllBytes();
				String content = new String(contentBytes);
				document = new Document(content);
				lst.readLstFile(filePath);
				this.lstobject = lst.getLstObject();
			} catch (CoreException | IOException e) {
				e.printStackTrace();
			}
		}
		sourceViewer.setDocument(document);
		return this.lstobject;
	}
	

	public void highlightKeyword(String keyword) {
		
		disposeHighlightColor();
		
        int lineNumber = 0;  
        int flineNumber = 0;  
        int lineOffset = 0;
        int nextLineOffset = 0;
        String lineContent = null;
        
        StyleRange styleRange = new StyleRange();  
    	
    	styleRange.background = highlightBGColor; // 设置背景色为红色  
    	styleRange.foreground = highlightFontColor;
    	
        
        while (nextLineOffset < document.getLength()) {  
			if (styledText != null && lineNumber >= 0 && lineNumber <= styledText.getLineCount()) {
				lineOffset = styledText.getOffsetAtLine(lineNumber);
				nextLineOffset = (lineNumber < styledText.getLineCount())
						? styledText.getOffsetAtLine(lineNumber+1)
						: styledText.getCharCount();
				lineContent = styledText.getTextRange(lineOffset, nextLineOffset - lineOffset);
			}
			
			if(lineContent.length() > 0) {
				int position = 0;
				boolean s = true;
				while (s && position < lineContent.length()) {  
					position = lineContent.indexOf(keyword,position);  
			        if (position != -1) {  
			        	styleRange.start = lineOffset+position; // 样式应用的起始位置  
			        	styleRange.length = keyword.length(); // 样式应用的长度  
			        	styledText.setStyleRange(styleRange);
			        	position = position + keyword.length();
			        	flineNumber = flineNumber>0 ? flineNumber : lineNumber;
			        } else {  
			        	s = false; 
			        }
		        }
			}
			lineNumber++;
        }
        
        gotoLine(flineNumber);
    }   
  
    // 记得在不再需要highlightColor时释放它  
    public void disposeHighlightColor() {  
    	StyleRange styleRange = new StyleRange();  
    	
    	styleRange.background = rhighlightBGColor; 
    	styleRange.foreground = rhighlightFontColor;
    	styleRange.start = 0; 
    	styleRange.length = document.getLength(); 
    	styledText.setStyleRange(styleRange);
    }
	
	public int findLineNumberForAddr(String searchString) {
		disposeHighlightColor();
		
		int lineNumber = 0;
		int lineOffset = 0;
		int nextLineOffset = 0;

		Pattern pattern = Pattern.compile(isAddrregex);  
		if(searchString != null) {
	        Matcher matcher = pattern.matcher(searchString);  
	        if (matcher.matches()) {
	        	Map<Long, Lst> lspmap =this.lstobject.getLstMap();
	        	long addr = Long.parseLong(searchString, 16);
	        	if (lspmap.containsKey(addr)) {
	        		Lst lst = lspmap.get(addr);
	        		if(lst.getLine() >=0) {
	        			lineNumber = lst.getLine();
	        		}
	        	}

	        }  
		}
		
		if(lineNumber > 0 && styledText.getCharCount() > 0) {
			lineOffset = styledText.getOffsetAtLine(lineNumber);
			nextLineOffset = (lineNumber < styledText.getLineCount())
					? styledText.getOffsetAtLine(lineNumber+1)
					: styledText.getCharCount();
			
			StyleRange styleRange = new StyleRange();  
	    	styleRange.start = lineOffset; 
	    	styleRange.length = nextLineOffset - lineOffset;
	    	styleRange.background = highlightBGColor;
	    	styleRange.foreground = highlightFontColor;
	    	  
	    	// 将样式应用到 StyledText 组件上  
	    	styledText.setStyleRange(styleRange);
		}
    	return lineNumber;
		
		
        
    }  
	
	public void gotoLine(int lineNumber) {
		
		int lineOn = getCaretLineNumber();

		int gotoline = lineOn > lineNumber ? -5 : 5;
		if(lineNumber > gotoline) {
			gotoline = lineNumber + gotoline;
		}
		
		if(styledText.getCharCount() > 0 && gotoline >= 0) {
			int lineOffset = 0;
			try {
				lineOffset = document.getLineOffset(gotoline);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			styledText.setCaretOffset(lineOffset);
			styledText.showSelection();
		}
		
	}
	
	
	
	public String getTextByLine(int lineNumber) {
		String lineContent = null;
		if (styledText != null && lineNumber >= 0 && lineNumber <= styledText.getLineCount()) {
			int lineOffset = styledText.getOffsetAtLine(lineNumber - 1);
			int nextLineOffset = (lineNumber < styledText.getLineCount())
					? styledText.getOffsetAtLine(lineNumber)
					: styledText.getCharCount();
			lineContent = styledText.getTextRange(lineOffset, nextLineOffset - lineOffset);
		}
		return lineContent;

	}
	
	public int getSelectLineNumber() {
		int lineNumber = 0;
		if (styledText != null) {
			Point caretLocation = styledText.getSelection();

			if (caretLocation != null) {
				lineNumber = styledText.getLineAtOffset(caretLocation.x);
				if (lineNumber != -1) {
					lineNumber = lineNumber + 1; 
				}
			}
		}
		return lineNumber;
	}
	
	public IFile getOpenFilePath() {
		return this.openFilePath;
	}
	
	public void openSourceCode(String keyworld) {
		long addr = 0;
		Pattern pattern = Pattern.compile(isAddrregex);
		Pattern pattern2 = Pattern.compile(isLstRecordregex);
		
		
		
		if(keyworld != null) {
			Matcher matcher = pattern.matcher(keyworld);  
			
			Matcher matcher2 = pattern2.matcher(keyworld);
					
	        if (matcher.matches()) {
	        	addr = Long.parseLong(keyworld, 16);
        	}else if (matcher2.find()) {  
        	    String hexNumber = matcher2.group(1); 
        	    addr = Long.parseLong(hexNumber, 16);
        	}
        }
		
		if(addr > 0) {
			LstOperation.openSourceCode(this.openFilePath , addr);
		}
		
		

	}

	
	private int getCaretLineNumber() {  
        // 获取光标的位置（以像素为单位）  
        Point caretLocation = styledText.getCaret().getLocation();
        if (caretLocation != null) {  
            // 将光标位置转换为文档中的偏移量  
            int offset = styledText.getOffsetAtPoint(caretLocation);  
            if (document != null) {  
                // 将偏移量转换为行号  
                try {  
                    int line = document.getLineOfOffset(offset);  
                    return line;  
                } catch (BadLocationException e) {  
                    // 处理异常情况  
                    e.printStackTrace();  
                }  
            }  
        }  
  
        // 如果没有获取到行号，返回-1或其他合适的默认值  
        return -1;  
    }

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}