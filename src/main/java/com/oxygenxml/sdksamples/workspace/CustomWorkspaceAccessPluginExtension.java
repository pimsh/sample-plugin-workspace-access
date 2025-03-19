package com.oxygenxml.sdksamples.workspace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.server.Operation;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.loading.PrivateClassLoader;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Segment;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

import com.oxygenxml.editor.swtutil.td;

import net.sf.saxon.s9api.Processor;
import ro.sync.basic.io.FilePathToURI;
import ro.sync.basic.util.URLUtil;
import ro.sync.basic.xml.BasicXmlUtil;
import ro.sync.basic.xml.XMLConstants;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.commons.operations.DeleteElementOperation;
import ro.sync.ecss.extensions.commons.operations.MoveCaretOperation;
import ro.sync.ecss.extensions.commons.operations.TransformOperation;
import ro.sync.ecss.extensions.commons.operations.XSLTOperation;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.editor.state.CaretStateInfo;
import ro.sync.exml.editor.xmleditor.operations.context.RelativeInsertPosition;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSTextBasedEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.actions.ActionsProvider;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextDocumentController;
import ro.sync.exml.workspace.api.editor.page.text.xml.TextOperationException;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.XPathException;
import ro.sync.exml.workspace.api.editor.transformation.TransformationFeedback;
import ro.sync.exml.workspace.api.editor.transformation.TransformationScenarioInvoker;
import ro.sync.exml.workspace.api.editor.transformation.TransformationScenarioNotFoundException;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;
import ro.sync.exml.workspace.api.util.XMLUtilAccess;
import ro.sync.io.XSLTExtensionIOUtil;
import ro.sync.util.editorvars.EditorVariables;
import ro.sync.util.xslt.XPathElementsAndAttributesExtractor;
import ro.sync.exml.plugin.transform.*;

public class CustomWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {
	
  private JTextArea customMessagesArea;
  
  public static String stylesheetsFolderPath;
  
  public static File configFile;
  
  public static HashMap<String, String> settingsMap = new HashMap<String, String>();
  
  public static Collection<File> allStylesheets = new ArrayList<File>();
  
  public static Collection<Action> allActions = new ArrayList<Action>();
  
  public static HashMap<Action, String> allActionsMap = new HashMap<Action, String>();
  
  public static StandalonePluginWorkspace pluginWorkspaceAccess;
  
  public static Processor processor = new Processor(true);
  
  public void writeToSettingsMap() {
		BufferedWriter bf = null;
		
		try {
			bf = new BufferedWriter(new FileWriter(configFile));
			for (Map.Entry<String, String> entry: settingsMap.entrySet()) {
				bf.write(entry.getKey() + " - " + entry.getValue());
				bf.newLine();
			}
		} catch (IOException e1) {
			pluginWorkspaceAccess.showInformationMessage("map writer can't: " + e1.getMessage());
		}
		
		finally {
			try {
				bf.close();
			} catch (IOException e1) {
				pluginWorkspaceAccess.showInformationMessage("map writer can't close: " + e1.getMessage());
			}
		}
	}
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
//	  pluginWorkspaceAccess.showInformationMessage("SAXON EDITION: " + processor.getSaxonEdition() + "\n" + processor.getXmlVersion() + "\n" + processor.getUnderlyingConfiguration() + "\n");
	  
	  WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
	  final ro.sync.exml.workspace.api.standalone.actions.ActionsProvider actionsProvider = pluginWorkspaceAccess.getActionsProvider();
	  //The "ro.sync.exml.options.APIAccessibleOptionTags" contains all accessible keys.
	  //		  pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
	String userHomePath = System.getProperty("user.home").replace("\\", "/");
	File dir = new File(userHomePath + "/OxygenPluginConfig");
	if (!dir.exists()){
	    dir.mkdirs();
	}
	  
	String configFileName = "/" + "plugin1-" + "config" + "-" + System.getProperty("user.name");
	configFile = new File(userHomePath + "/OxygenPluginConfig/" + configFileName);
	
	try {
		if(configFile.createNewFile()) {
			pluginWorkspaceAccess.showInformationMessage("config created: " + configFile.getAbsolutePath().replace("\\", "/"));
			if(settingsMap.isEmpty()) {
				settingsMap.put("stylesheetsFolderPath", (userHomePath + "/OxygenPluginConfig").replace("\\", "/"));
				settingsMap.put("saxonVersion", "PE");
				BufferedWriter bf = null;
				
				try {
					bf = new BufferedWriter(new FileWriter(configFile));
					for (Map.Entry<String, String> entry: settingsMap.entrySet()) {
						bf.write(entry.getKey() + " - " + entry.getValue());
						bf.newLine();
					}
				} catch (IOException e1) {
					pluginWorkspaceAccess.showInformationMessage("map writer can't: " + e1.getMessage());
				}
				
				finally {
					try {
						bf.close();
					} catch (IOException e1) {
						pluginWorkspaceAccess.showInformationMessage("map writer can't close: " + e1.getMessage());
					}
				}
			}
			
		}
	} catch (IOException e) {
		pluginWorkspaceAccess.showInformationMessage("createNewFile: " + e.getMessage());
	}
	
	final Action selectionSourceAction = createShowSelectionAction(pluginWorkspaceAccess);
	final Action backupAction = createBackupAction(pluginWorkspaceAccess);
	final Action settingsAction = createSettingsAction(pluginWorkspaceAccess);
	
	// reading from the config into the settingsMap
	BufferedReader br = null;
	try {
		br = new BufferedReader(new FileReader(configFile));
		String line = null;
		while((line = br.readLine()) != null) {
			String[] parts = line.split("-");
			String name = parts[0].trim();
			if(!parts[1].trim().equals("")) {
				String value = parts[1].trim();
					if(!name.equals("") && !value.equals(""))
						settingsMap.put(name, value);
					else {
						pluginWorkspaceAccess.showInformationMessage("config file value(s) empty");
						settingsMap.put(name, "PLACEHOLDER");
					}
			}
			else {
				pluginWorkspaceAccess.showInformationMessage("config file value(s) empty");
				settingsMap.put(name, "PLACEHOLDER");
			}
			
		}
	} catch (IOException e) {
		pluginWorkspaceAccess.showInformationMessage("reader can't read: " + e.getMessage());
	}
	
	finally {
		if(br !=null) {
			try {
				br.close();
			} catch (IOException e) {
				pluginWorkspaceAccess.showInformationMessage("reader can't close: " + e.getMessage());
			}
		}
		
	}
	
	stylesheetsFolderPath = settingsMap.get("stylesheetsFolderPath");
	
	// if config is empty - scanning the config folder for stylesheets as a default one
	try {
		Scanner sc = new Scanner(configFile);
		if (sc.hasNext())
			addTree(new File(stylesheetsFolderPath), allStylesheets);
		else
			addTree(new File(System.getProperty("user.home") + "/OxygenPluginConfig"), allStylesheets);
	} catch (FileNotFoundException e) {
		pluginWorkspaceAccess.showInformationMessage(e.getMessage());
	}
//	if(configFile.length() != 0)
//		addTree(new File(stylesheetsFolderPath), allStylesheets);
//	else
//		addTree(new File(System.getProperty("user.home") + "/OxygenPluginConfig"), allStylesheets);
//    addTree(new File("C:/Users/imsh/testFolda"), allStylesheets);
    
    // loop through the FILES collection
    java.util.Iterator<File> iterator1 = allStylesheets.iterator();
    
    // loop through files collection adding them as actions to the actions collection
    int number = 0;
    while (iterator1.hasNext())
    {	
    	// the current stylesheet loaded in to create an action
    	File currentFile = iterator1.next();
    	
    	// class for Source
    	class CurrentSource implements javax.xml.transform.Source {
    		String id = new String();
			@Override
			public void setSystemId(String systemId) {
				id = systemId;
			}

			@Override
			public String getSystemId() {
				return id;
			}
    	}
    	
    	// tryna make a source for the thing
    	CurrentSource currentSource = new CurrentSource();
    	currentSource.setSystemId(currentFile.getPath());
    	
    	number += 1;
    	
    	// creating an action from the file while also loading it in as both a file and a source
    	Action newAction = createNewAction(pluginWorkspaceAccess, number, currentFile.getName(), currentFile, currentSource);
    	// adding it to an array of actions (to thwack them all in a dropdown later)
    	allActions.add(newAction);
    	allActionsMap.put(newAction, currentFile.getName());
    }

    //Mount the action on the contextual menus for the Text and Author modes.
	pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {

				@Override
				public void customizeTextPopUpMenu(JPopupMenu popup,
						WSTextEditorPage textPage) {
//					java.util.Iterator<Action> iterator3 = allActions.iterator();
//					  // loopin through the actions collection adding them to the dropdown
//					  int number = 1;
//					    while(iterator3.hasNext()) {
//					    	Action currentAction = iterator3.next();
////					    	actionsProvider.registerAction(currentAction.toString(), currentAction, "alt shift " + number);
////					    	actionsProvider.registerAction("action" + number, currentAction, "");
//					    	actionsProvider.registerAction(allActionsMap.get(currentAction), currentAction, "");
//					    	popup.add(currentAction);
//						    number++;
//					    }
//					    	
//					  actionsProvider.registerAction("settingsAction", settingsAction, "");
//					  popup.add(settingsAction);
//					  actionsProvider.registerAction("backupAction", backupAction, "");
//					  popup.add(backupAction);
				}
			});

	  pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {
		  /**
		   * @see ro.sync.exml.workspace.api.standalone.MenuBarCustomizer#customizeMainMenu(javax.swing.JMenuBar)
		   */
		  public void customizeMainMenu(JMenuBar mainMenuBar) {
			  JMenu mySecondMenu = new JMenu("Menu2");
			  MenuScroller.setScrollerFor(mySecondMenu, 15, 31, 0, 2);
			  mySecondMenu.setOpaque(true);
			  // iterator for the actions collection
			  java.util.Iterator<Action> iterator3 = allActions.iterator();
			  // loopin through the actions collection adding them to the dropdown
			  int number = 1;
			    while(iterator3.hasNext()) {
			    	Action currentAction = iterator3.next();
//			    	actionsProvider.registerAction("action" + number, currentAction, "alt shift " + number);
			    	actionsProvider.registerAction(allActionsMap.get(currentAction), currentAction, "");
			    	mySecondMenu.add(currentAction);
				    number++;
			    }
			    	
			  actionsProvider.registerAction("settingsAction", settingsAction, "");
			  mySecondMenu.add(settingsAction);
			  actionsProvider.registerAction("backupAction", backupAction, "");
			  mySecondMenu.add(backupAction);
			  mainMenuBar.add(mySecondMenu, mainMenuBar.getMenuCount() - 1);
		  }
	  });

	  pluginWorkspaceAccess.addEditorChangeListener(
			  new WSEditorChangeListener() {
				  @Override
				  public boolean editorAboutToBeOpenedVeto(URL editorLocation) {
					  //You can reject here the opening of an URL if you want
					  return true;
				  }
				  @Override
				  public void editorOpened(URL editorLocation) {
					  checkActionsStatus(editorLocation);
				  }

				  // Check actions status
				  private void checkActionsStatus(URL editorLocation) {
					  WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
					  if (editorAccess != null) {
						  selectionSourceAction.setEnabled(
								  EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())
								  || EditorPageConstants.PAGE_TEXT.equals(editorAccess.getCurrentPageID()));
					  }
				  }

				  @Override
				  public void editorClosed(URL editorLocation) {
					  //An edited XML document has been closed.
				  }

				  /**
				   * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorAboutToBeClosed(java.net.URL)
				   */
				  @Override
				  public boolean editorAboutToBeClosed(URL editorLocation) {
					  //You can veto the closing of an XML document.
					  //Allow close
					  return true;
				  }

				  /**
				   * The editor was relocated (Save as was called).
				   * 
				   * @see ro.sync.exml.workspace.api.listeners.WSEditorChangeListener#editorRelocated(java.net.URL, java.net.URL)
				   */
				  @Override
				  public void editorRelocated(URL previousEditorLocation, URL newEditorLocation) {
					  //
				  }

				  @Override
				  public void editorPageChanged(URL editorLocation) {
					  checkActionsStatus(editorLocation);
				  }

				  @Override
				  public void editorSelected(URL editorLocation) {
					  checkActionsStatus(editorLocation);
				  }

				  @Override
				  public void editorActivated(URL editorLocation) {
					  checkActionsStatus(editorLocation);
				  }
			  }, 
			  StandalonePluginWorkspace.MAIN_EDITING_AREA);

	  //You can use this callback to populate your custom toolbar (defined in the plugin.xml) or to modify an existing Oxygen toolbar 
	  // (add components to it or remove them) 
	  pluginWorkspaceAccess.addToolbarComponentsCustomizer(new ToolbarComponentsCustomizer() {
		  /**
		   * @see ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer#customizeToolbar(ro.sync.exml.workspace.api.standalone.ToolbarInfo)
		   */
		  
		  public void customizeToolbar(ToolbarInfo toolbarInfo) {
			  //The toolbar ID is defined in the "plugin.xml"
			  if("TESTBAR".equals(toolbarInfo.getToolbarID())) {
				  List<JComponent> comps = new ArrayList<JComponent>(); 
				  JComponent[] initialComponents = toolbarInfo.getComponents();
				  boolean hasInitialComponents = initialComponents != null && initialComponents.length > 0; 
				  if (hasInitialComponents) {
					  // Add initial toolbar components
					  for (JComponent toolbarItem : initialComponents) {
						  comps.add(toolbarItem);
					  }
				  }
				  
				  //Add your own toolbar button using our "ro.sync.exml.workspace.api.standalone.ui.ToolbarButton" API component
				  ToolbarButton customButton = new ToolbarButton(selectionSourceAction, true);
//				  comps.add(customButton);
				  toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
//				  ToolbarButton customButton2 = new ToolbarButton(backupAction, true);
//				  comps.add(customButton2);
//				  toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
			  } 
		  }
	  });
	  
	  pluginWorkspaceAccess.addViewComponentCustomizer(new ViewComponentCustomizer() {
		  /**
		   * @see ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer#customizeView(ro.sync.exml.workspace.api.standalone.ViewInfo)
		   */
		  public void customizeView(ViewInfo viewInfo) {
			  if(
					  //The view ID defined in the "plugin.xml"
					  "SampleWorkspaceAccessID".equals(viewInfo.getViewID())) {
				  customMessagesArea = new JTextArea("Messages:");
				  viewInfo.setComponent(new JScrollPane(customMessagesArea));
				  viewInfo.setTitle("Custom Messages");
				  //You can have images located inside the JAR library and use them...
//				  viewInfo.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/customMessage.png").toString()));
			  } 
		  }
	  }); 
  }
  // making a backup right next to the document with a date (up to seconds) in its name
 @SuppressWarnings({ "unused", "serial" })
private AbstractAction createBackupAction(final StandalonePluginWorkspace pluginWorkspaceAccess) {
	 return new AbstractAction("Backup") {
		 public void actionPerformed(ActionEvent actionEvent) {
			 WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
			 WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
			 
			 // getting a date for the rename
			 String date = getDate();
			 // editorAccess.save();
			 // current path where the file sits
			 String path = String.valueOf(editorAccess.getEditorLocation());
			 String userDesktopPath = (System.getProperty(("user.home")) + "\\Desktop").replace("\\", "/");
//			 String userDesktopPath = String.valueOf(System.getProperty(("user.home").replace("\\", "/") + "/Desktop"));
			 // cutting off the "file:"
			 File file1 = new File(path.substring(6, path.length()));
			 File file2 = new File(path.substring(6, path.length()-8).concat('.' + date + path.substring(path.length()-8, path.length())));
//			 File file3 = new File(userDesktopPath + "/BACKUP." + date);
//			 File file3 = new File(userDesktopPath + ".BACKUP." + date);
			 
//			 TODO MAKE IT STREAM THE LOCALLY OPEN STRING (if the local backup be empty)
			 File file3 = new File(userDesktopPath + "/" + path.substring(path.lastIndexOf("/"), path.length()-8).concat('.' + date + path.substring(path.length()-8, path.length())));
			 
			 try {
				copyFileUsingChannel(file1, file2);
				copyFileUsingChannel(file1, file3);
				
			} catch (IOException e) {
				pluginWorkspaceAccess.showInformationMessage(e.getMessage());
			}
			 
			pluginWorkspaceAccess.showInformationMessage("Backups created: " + file2.getPath().replace("\\", "/") + "\n" +  file3.getPath().replace("\\", "/"));
		 }
	 };
 }
 
 
 private AbstractAction createSettingsAction(final StandalonePluginWorkspace pluginWorkspaceAccess) {
	return new AbstractAction("Settings") {

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsWindow settingsWindow = new SettingsWindow(pluginWorkspaceAccess);
			settingsWindow.popItUp();
		}
	};
	 
 }
 
// adding ALL the files from the folder to an array
	static void addTree(File file, Collection<File> all) {
	    File[] children = file.listFiles();
	    if (children != null) {
	        for (File child : children) {
	        	if (child.getName().endsWith(".xsl")) {
	        		all.add(child);
		            addTree(child, all);
	        	}
	        		
//	            all.add(child);
//	            addTree(child, all);
	        }
	    }
	}
 
// getting a date WITHOUT COLONS (which are a problem here for some reason)
	private static String getDate() {
	 String pattern = "dd-MM-yyyy-hh-mm-ss";
	 SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
	 String date = simpleDateFormat.format(new Date());
	 return date;
}
 
// copying a file, rename is in the path
	private static void copyFileUsingChannel(File source, File dest) throws IOException {
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    try {
	        sourceChannel = new FileInputStream(source).getChannel();
	        destChannel = new FileOutputStream(dest).getChannel();
	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	       }finally{
	           sourceChannel.close();
	           destChannel.close();
	   }
	}

	/**
	 * Create the Swing action which shows the current selection.
	 * 
	 * @param pluginWorkspaceAccess The plugin workspace access.
	 * @return The "HELLO" action
	 */
	@SuppressWarnings("serial")
	private AbstractAction createNewAction(final StandalonePluginWorkspace pluginWorkspaceAccess, int actionNumber, String actionName, File actionFile, javax.xml.transform.Source actionSource) {
		return new AbstractAction(actionNumber + ". " + actionName) {
			@Override
			public void actionPerformed(ActionEvent e) {
				WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
				WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
				WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
				Source xslSrc = new SAXSource(new org.xml.sax.InputSource(actionFile.getPath().replace("\\", "/")));
				Reader docReader = editorAccess.createContentReader();
			    org.xml.sax.InputSource is = new org.xml.sax.InputSource(docReader);
			    is.setSystemId(editorAccess.getEditorLocation().toExternalForm());
			    Source xmlSrc = new SAXSource(is);
			    StringWriter sw = new StringWriter();
			    Node currentNode = null;
			    try {
					// this, for some reason, returns the current thing the caret is sitting on, only one element in there
					Object [] nodes = xmltextPage.evaluateXPath(".");
					
						if(nodes.length > 0) {
							// we just cast an object as a node and it works
							currentNode = (Node) nodes[0];
						}
				} catch (XPathException e11) {
					pluginWorkspaceAccess.showInformationMessage(e11.getMessage());
				}
			    
			    try {
					Pattern p = Pattern.compile("param name=\".*?\"", Pattern.CASE_INSENSITIVE);
//					Pattern p = Pattern.compile("xsl\\:param", Pattern.CASE_INSENSITIVE);
//					Pattern p = Pattern.compile("param", Pattern.CASE_INSENSITIVE);
					BufferedReader bf = new BufferedReader(new FileReader(actionFile));
					int lineCounter = 0;
					String lineBf;
					ArrayList<Object> matches = new ArrayList<Object>();
					ArrayList<String> names = new ArrayList<>();
					
//					while((lineBf = bf.readLine()) != null) {
//						lineCounter++;
//						Matcher m = p.matcher(lineBf);
//						
//						while (m.find()) {
//							// adding an element to an array that'd provide us with the number of the params
//							matches.add(m.start());
//							// adding a name of the param in a dumb way
//							names.add(lineBf.substring(m.start()+12, m.end()-1));
//							
//							// should have the parser find the 'name="..."' in a string with param, no matter where it sits
////							names.add(lineBf.substring(m.start()+12, m.end()-1) + " " + lineCounter + " " + lineBf.indexOf("name=\""));
////							names.add(lineBf.substring(m.start()+12, m.end()-1) + " " + lineBf.indexOf("name=\"") + lineBf.lastIndexOf("name=\".*?\""));
//							
//						}
//					}
					
					Scanner sc;
					try {
						sc = new Scanner(actionFile);
						while (sc.hasNextLine()) {
							String line = sc.nextLine();
							if(line.contains("xsl:param")) {
								Pattern p1 = Pattern.compile("name=\".*?\"", Pattern.CASE_INSENSITIVE);
								Matcher m = p1.matcher(line);
								while (m.find()) {
									matches.add(m.start());
									names.add(line.substring(m.start() + 6, m.end() - 1));
								}
							}
						}
					} catch (FileNotFoundException e1) {
						pluginWorkspaceAccess.showInformationMessage(e1.getMessage());
					}
					
					if (matches.size() > 0) {
						ThingWindow thingWindow = new ThingWindow(pluginWorkspaceAccess, currentNode, xmlSrc, xslSrc, actionFile, textPage, matches.size(), names);
						  thingWindow.popItUp();
					}
					
					else {
						
						applyStylesheet(pluginWorkspaceAccess, xmlSrc, xslSrc, xmltextPage, null, names);
						
//						  try {
//							  // transformation itself
//							Transformer transformer1 = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_PROFESSIONAL_EDITION); //TRANSFORMER_SAXON_6
//							// results are being put into a StringWriter
//							transformer1.transform(xmlSrc, new StreamResult(sw));
////							transformer1.transform(xmlSrc, new StreamResult(textPage.toString()));
//						} catch (TransformerException ex1) {
//							pluginWorkspaceAccess.showInformationMessage(ex1.getMessage());
//						} 
//							int length = textPage.getDocument().getLength();
//							textPage.select(0, length);
//							textPage.deleteSelection();
//							textPage.getDocument().insertString(0, sw.toString(), null);

					}
					
				} catch (IOException er) {
					pluginWorkspaceAccess.showInformationMessage(er.getMessage());
				}
			}
		};
	}
	
	private AbstractAction createShowSelectionAction(
			final StandalonePluginWorkspace pluginWorkspaceAccess) {
		return new AbstractAction("action1") {

			public void actionPerformed(ActionEvent actionevent) {
				  //Get the current opened XML document
				  WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
						  WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
						  WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
						  TextDocumentController tdController = (TextDocumentController) xmltextPage.getDocumentController();
						  Node currentNode = null;
						  try {
							// this, for some reason, returns the current thing the caret is sitting on, only one element in there
							Object [] nodes = xmltextPage.evaluateXPath(".");
							
								if(nodes.length > 0) {
									// we just cast an object as a node and it works
									currentNode = (Node) nodes[0];
//									pluginWorkspaceAccess.showInformationMessage(currentNode.getTextContent());
								}
//							pluginWorkspaceAccess.showInformationMessage(xmltextPage.findElementsByXPath(".").toString());
						} catch (XPathException e) {
							pluginWorkspaceAccess.showInformationMessage(e.getMessage());
						}
						  
//						  BasicXmlUtil.setTextContent(currentNode, "BLA");
						  ThingWindow thingWindow = new ThingWindow(pluginWorkspaceAccess, currentNode);
						  thingWindow.popItUpLite();
						  
//						  ThingWindow thingWindow = new ThingWindow(pluginWorkspaceAccess);
//						  thingWindow.popItUp();
						  
//						   loading the stylesheet as a source
//						  Source xslSrc = new SAXSource(new org.xml.sax.InputSource("C:/Users/imsh/testFolda/beispiel.xsl"));
//						  // grabbing a reader (Create a reader over the whole editor's content (exactly the XML content which gets saved on disk). The unsaved changes are 								included.)
//						    Reader docReader = editorAccess.createContentReader();
//						    // something about it being a saxon thing now
//						    org.xml.sax.InputSource is = new org.xml.sax.InputSource(docReader);
//						    is.setSystemId(editorAccess.getEditorLocation().toExternalForm());
//						    Source xmlSrc = new SAXSource(is);
//						    StringWriter sw = new StringWriter();
//						    
//						  try {
//							  // transformation itself
//							Transformer transformer1 = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_6);
//							// results are being put into a StringWriter
//							transformer1.transform(xmlSrc, new StreamResult(sw));
//						} catch (TransformerConfigurationException e) {
//							pluginWorkspaceAccess.showInformationMessage(e.getMessage());
//							e.printStackTrace();
//						} catch (TransformerException e) {
//							pluginWorkspaceAccess.showInformationMessage(e.getMessage());
//							e.printStackTrace();
//						}
////						pluginWorkspaceAccess.showInformationMessage(sw.toString() + textPage.getDocument().getLength());
////						pluginWorkspaceAccess.showInformationMessage(textPage.getDocument().getText(0, textPage.getDocument().getLength()));
//						try {
//							int length = textPage.getDocument().getLength();
//							textPage.select(0, length);
//							textPage.deleteSelection();
//							textPage.getDocument().insertString(0, sw.toString(), null);
//						} catch (BadLocationException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						  if (textPage.hasSelection()) {
//							  pluginWorkspaceAccess.showInformationMessage(textPage.getSelectedText());
//							  }
						  } 
		  };
	}
	
	private static String APPLY = "Apply";
	private static String SHOW = "Show";
    static JButton enterButton;
    static JButton showButton;
    public static JTextArea output;
    public static JTextField input;
    
    public static JTextField newField;
    
    static JFrame frame;
    static JPanel panel;
    
	public class ThingWindow {
		StandalonePluginWorkspace pluginWorkspaceAccess;
		Node node;
		Source xmlSrc;
		Source xslSrc;
		File actionFile;
		WSTextEditorPage textPage;
		int paramCount;
		ArrayList<String> paramNames;
		
		public ThingWindow (StandalonePluginWorkspace pluginWorkspaceAccess, Node node) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.node = node;
		}
		
		public ThingWindow (StandalonePluginWorkspace pluginWorkspaceAccess, Node node, Source xmlSrc, Source xslSrc, File actionFile, WSTextEditorPage textPage, int paramCount, ArrayList<String> paramNames) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.node = node;
			this.xmlSrc = xmlSrc;
			this.xslSrc = xslSrc;
			this.textPage = textPage;
			this.actionFile = actionFile;
			this.paramCount = paramCount;
			this.paramNames = paramNames;
		}
		
		public void popItUp(){
			frame = new JFrame(actionFile.getName());
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.setResizable(true);
	        panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setOpaque(true);
	        output = new JTextArea(15, 50);
	        output.setWrapStyleWord(true);
	        output.setEditable(false);
	        JScrollPane scroller = new JScrollPane(output);
	        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	        JPanel inputpanel = new JPanel();
//	        inputpanel.setLayout(new FlowLayout());
	        inputpanel.setLayout(new BoxLayout(inputpanel, BoxLayout.Y_AXIS));
	        
	     // new map - parameter name as key, jtextfield as value so that we'd access their content later
	        HashMap<String, JTextField> map = new HashMap<String, JTextField>();
	        ThingButtonListener buttonListener = new ThingButtonListener(pluginWorkspaceAccess, xmlSrc, xslSrc, actionFile, textPage, map, paramNames);
	        JTextField fieldToFocusOn = null;
	        newField= new JTextField(10);
	        for (int i = 0; i < paramCount; i++) {
	        	String str1 = paramNames.get(i);
//	        	inputpanel.add(new JLabel(paramNames.get(i)));
	        	inputpanel.add(new JLabel(str1));
	        	JTextField newField = new JTextField();
				inputpanel.add(newField);
				if(str1.toUpperCase().contains("xpath".toUpperCase())) {
					newField.setText(BasicXmlUtil.getXPathForNode(node));
	        		newField.addActionListener(buttonListener);
	        		newField.addActionListener(new ThingInputListener(pluginWorkspaceAccess, xmlSrc, xslSrc, textPage, map, paramNames));
	        		fieldToFocusOn = newField;
	        	}
				
	        	
//	        	if(str1.toUpperCase().contains("xpath".toUpperCase())) {
//	        		newField.setText(BasicXmlUtil.getXPathForNode(node));
//	        		newField.addActionListener(new ThingInputListener(pluginWorkspaceAccess));
//	        		newField.addActionListener(buttonListener);
//	        	}
//	        	newField.setActionCommand(APPLY);
//				inputpanel.add(newField);
				// store pairs of name-textfield in the map
				map.put(paramNames.get(i), newField);
			}
	        
	        
	        enterButton = new JButton("Apply");
	        showButton = new JButton("Show");
	        enterButton.setActionCommand(APPLY);
	        showButton.setActionCommand(SHOW);
	        enterButton.addActionListener(buttonListener);
	        showButton.addActionListener(buttonListener);
	        // enterButton.setEnabled(false);
	        input = new JTextField(20);
	        input.setActionCommand(APPLY);
//	        input.setActionCommand(SHOW);
//	        input.addActionListener(buttonListener);
	        DefaultCaret caret = (DefaultCaret) output.getCaret();
	        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	        panel.add(scroller);
//	        inputpanel.add(input);
	        inputpanel.add(enterButton);
//	        inputpanel.add(showButton);
	        
	        panel.add(inputpanel);
	        frame.getContentPane().add(BorderLayout.CENTER, panel);
	        frame.pack();
	        fieldToFocusOn.requestFocus();
	        frame.setLocationByPlatform(true);
	        // Center of the DEFAULT screen
//	        frame.setLocationRelativeTo(null);
//	        frame.setLocationRelativeTo(customMessagesArea);
	        frame.setVisible(true);
//	        input.requestFocus();
//	        input.setText("BLA");
	        output.setText(System.getProperty("user.name") + " " + System.getProperty("user.home"));
	        output.append("\n");
	        output.append(BasicXmlUtil.getXPathForNode(node));
	        output.append("\n");
	        
//	        output.append(node.toString() + " " + node.getParentNode() + " " + node.getTextContent());
//	        output.append("\n");
//	        output.append(String.valueOf(paramCount) + " param");
	        
		}
	
		public void popItUpLite() {
			frame = new JFrame("action1");
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.setResizable(true);
	        panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setOpaque(true);
	        output = new JTextArea(15, 50);
	        output.setWrapStyleWord(true);
	        output.setEditable(false);
	        JScrollPane scroller = new JScrollPane(output);
	        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	        JPanel inputpanel = new JPanel();
//	        inputpanel.setLayout(new FlowLayout());
	        inputpanel.setLayout(new BoxLayout(inputpanel, BoxLayout.Y_AXIS));
	        
	        enterButton = new JButton("Apply");
	        showButton = new JButton("Show");
	        enterButton.setActionCommand(APPLY);
	        showButton.setActionCommand(SHOW);
	        showButton.addActionListener(new ThingButtonListener(pluginWorkspaceAccess));
	        // enterButton.setEnabled(false);
	        input = new JTextField(20);
	        input.setActionCommand(APPLY);
	        input.setActionCommand(SHOW);
	        DefaultCaret caret = (DefaultCaret) output.getCaret();
	        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	        panel.add(scroller);
//	        inputpanel.add(input);
	        inputpanel.add(enterButton);
	        inputpanel.add(showButton);
	        
	        panel.add(inputpanel);
	        frame.getContentPane().add(BorderLayout.CENTER, panel);
	        frame.pack();
	        frame.setLocationByPlatform(true);
//	        frame.setLocationRelativeTo(null);
//	        frame.setLocationRelativeTo(customMessagesArea);
	        frame.setVisible(true);
	        input.requestFocus();
	        output.append(BasicXmlUtil.getXPathForNode(node));
	        output.append("\n");
		}
	};
	
	public static class ThingButtonListener implements ActionListener
	    {	
			StandalonePluginWorkspace pluginWorkspaceAccess;
			AbstractAction action;
			Source xmlSrc;
			Source xslSrc;
			File actionFile;
			WSTextEditorPage textPage;
			HashMap<String, JTextField> map;
			ArrayList<String> paramNames;
			
			public ThingButtonListener (StandalonePluginWorkspace pluginWorkspaceAccess) {
				this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			}
			
			public ThingButtonListener (StandalonePluginWorkspace pluginWorkspaceAccess, Source xmlSrc, Source xslSrc, File actionFile, WSTextEditorPage textPage, HashMap<String, JTextField> map, ArrayList<String> paramNames) {
				this.pluginWorkspaceAccess = pluginWorkspaceAccess;
				this.xmlSrc = xmlSrc;
				this.xslSrc = xslSrc;
				this.actionFile = actionFile;
				this.textPage = textPage;
				this.map = map;
				this.paramNames = paramNames;
			}
			
			public void actionPerformed(ActionEvent ev)
	        {	
				String cmd = ev.getActionCommand();
				// the run button action
				if (APPLY.equals(cmd))
	            {	
					applyStylesheet(pluginWorkspaceAccess, xmlSrc, xslSrc, textPage, map, paramNames);
					
//					WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
//					WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
//					 try {
//					  // only one element in there
//					Object [] nodes = xmltextPage.evaluateXPath(input.getText());
//						if(nodes.length > 0) {
//							// we just cast an object as a node and it works
//							Node currentNode = (Node) nodes[0];
//	//						output.append(currentNode.toString() +  currentNode.getParentNode() + currentNode.getParentNode().getParentNode() + currentNode.getTextContent());
//	//						output.append("\n");
//							
//						}
//				} catch (XPathException e) {
//					pluginWorkspaceAccess.showInformationMessage("debug 1 " + e.getMessage());
//				}
					 
//					 StringWriter sw = new StringWriter();
					    
//					  try {
//						  // transformation itself
//						Transformer transformerPE = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_PROFESSIONAL_EDITION);
//	//					Transformer transformerEE = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_ENTERPRISE_EDITION);
//	//					if(settingsMap.get("saxonVersion").equals("PE")) {
//	//						pluginWorkspaceAccess.showInformationMessage("PE");
//	//					}
//	//					if(settingsMap.get("saxonVersion").equals("6")) {
//	//						pluginWorkspaceAccess.showInformationMessage("6");
//	//					}
//						
//						transformerPE.clearParameters();
//	//					transformerEE.clearParameters();
//						// loop through the map of name-textfield here
//						for (int i = 0; i < map.size(); i++) {
//							if(!map.get(paramNames.get(i)).getText().trim().equals("")) {
//								transformerPE.setParameter(paramNames.get(i), map.get(paramNames.get(i)).getText());
//	//							transformerEE.setParameter(paramNames.get(i), map.get(paramNames.get(i)).getText());
//	//							output.append("\n");
//	//							output.append(paramNames.get(i) + " " + transformerPE.getParameter(paramNames.get(i)).toString());
//							}
//						}					
//						
//	//					if(!input.getText().trim().equals("")) {
//	//						transformer1.setParameter("element_xpath", input.getText());
//	//					}
//						
//	//					output.append((String) transformer1.getParameter("element_xpath"));
//	//					pluginWorkspaceAccess.showInformationMessage(transformer1.getParameter("element_xpath").toString());
//						// results are being put into a StringWriter
////					transformerEE.transform(xmlSrc, new StreamResult(sw));
//						transformerPE.transform(xmlSrc, new StreamResult(textPage.toString()));
//
//						int length = textPage.getDocument().getLength();
////						textPage.getDocument().getText(0, textPage.getDocument().getLength()).replaceAll(textPage.getDocument().getText(0, length), sw.toString());
////						pluginWorkspaceAccess.showInformationMessage(textPage.getDocument().getText(0, length));
////						pluginWorkspaceAccess.showInformationMessage(sw.toString());
//						
//						
//						textPage.select(0, length);
////						textPage.deleteSelection();
////						textPage.getDocument().insertString(0, sw.toString(), null);
//						
//	//					transformer1.clearParameters();
//						
//					} catch (TransformerException e) {
//						pluginWorkspaceAccess.showInformationMessage("debug 2 " + e.getMessage());
//					}
					  
	            }
				
				// the Show button action
				if (SHOW.equals(cmd) && !input.getText().trim().equals("")) {
					WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
					WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
					// might be better to use the initial action file here instead, since it's getting converted every run anyway
					// OR might be better to edit the converted varying one so that the changes wouldn't remain, hm
	//			        pluginWorkspaceAccess.showInformationMessage(sc.next());
					try {
						  // only one element in there
						Object [] nodes = xmltextPage.evaluateXPath(input.getText());
							if(nodes.length > 0) {
								// we just cast an object as a node and it works
								Node currentNode = (Node) nodes[0];
								output.append(currentNode.toString() +  currentNode.getParentNode() + currentNode.getParentNode().getParentNode() + currentNode.getTextContent());
								output.append("\n");
								
							}
					} catch (XPathException e) {
						pluginWorkspaceAccess.showInformationMessage("debug 3 " + e.getMessage());
					}
					
					for (int i = 0; i < map.size(); i++) {
						if(!input.getText().trim().equals("")) {
	//						output.append(map.get(paramNames.get(i)).getText());
	//						output.append(transformer1.getParameter("element_xpath").toString());
						}
					}		
					
				}
				
	            if (!input.getText().trim().equals(""))
	            {	
	//            	String target = "xsl:param";
	            	String target = input.getText();
						
	            	Scanner sc;
					try {
						Pattern p = Pattern.compile(target, Pattern.CASE_INSENSITIVE);
						BufferedReader bf = new BufferedReader(new FileReader(actionFile));
						int lineCounter = 0;
						String lineBf;
						while((lineBf = bf.readLine()) != null) {
							lineCounter++;
							Matcher m = p.matcher(lineBf);
							
							while (m.find()) {
	//							output.append(target + " found at " + m.start() + "-" + m.end() + " on line " + lineCounter + " "  + "\n");
							}
						}
						
	//					sc = new Scanner(actionFile);
	//					int lineNumber = 0;
	//					while (sc.hasNextLine()) {
	//						String line = sc.nextLine();
	//						lineNumber++;
	//						if(line.contains("param")) {
	//							output.append(line);
	//						}
	//					}
						output.append("\n");
						
	//					output.append(sc.next() + " " + sc.next());
					} catch (IOException e) {
						pluginWorkspaceAccess.showInformationMessage("debug 4 " + e.getMessage());
					}
	                output.append("\n");
	            }
	            
	//            StringWriter sw = new StringWriter();
	//		    
	//		  try {
	//			  // transformation itself
	//			Transformer transformer1 = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_6);
	//			// results are being put into a StringWriter
	//			transformer1.transform(xmlSrc, new StreamResult(sw));
	//		} catch (TransformerConfigurationException e) {
	//			pluginWorkspaceAccess.showInformationMessage(e.getMessage());
	//			e.printStackTrace();
	//		} catch (TransformerException e) {
	//			pluginWorkspaceAccess.showInformationMessage(e.getMessage());
	//			e.printStackTrace();
	//		}
	////		pluginWorkspaceAccess.showInformationMessage(sw.toString() + textPage.getDocument().getLength());
	////		pluginWorkspaceAccess.showInformationMessage(textPage.getDocument().getText(0, textPage.getDocument().getLength()));
	//		try {
	//			int length = textPage.getDocument().getLength();
	//			textPage.select(0, length);
	//			textPage.deleteSelection();
	//			textPage.getDocument().insertString(0, sw.toString(), null);
	//		} catch (BadLocationException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		  if (textPage.hasSelection()) {
	//			  pluginWorkspaceAccess.showInformationMessage(textPage.getSelectedText());
	//			  }
	        }
			
	    }

	public static class ThingInputListener implements ActionListener {
		StandalonePluginWorkspace pluginWorkspaceAccess;
		Source xmlSrc;
		Source xslSrc;
		WSTextEditorPage textPage;
		HashMap<String, JTextField> map;
		ArrayList<String> paramNames;
		
		public ThingInputListener (StandalonePluginWorkspace pluginWorkspaceAccess, Source xmlSrc, Source xslSrc, WSTextEditorPage textPage, HashMap<String, JTextField> map, ArrayList<String> paramNames) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.xmlSrc = xmlSrc;
			this.xslSrc = xslSrc;
			this.textPage = textPage;
			this.map = map;
			this.paramNames = paramNames;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			applyStylesheet(pluginWorkspaceAccess, xmlSrc, xslSrc, textPage, map, paramNames);
		}
	}

	public class SettingsWindow {
		
		StandalonePluginWorkspace pluginWorkspaceAccess;
		JComboBox comboBox1;
		
		public SettingsWindow (StandalonePluginWorkspace pluginWorkspaceAccess) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
		}
		public void popItUp() {
			
			frame = new JFrame("Settings");
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.setResizable(true);
	        panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setOpaque(true);
	        panel.setSize(100, 50);
	        output = new JTextArea(15, 50);
	        output.setWrapStyleWord(true);
	        output.setEditable(false);
	        enterButton = new JButton("Apply");
	        enterButton.setActionCommand(APPLY);
	        enterButton.addActionListener(new SettingsButtonListener(pluginWorkspaceAccess));
	        input = new JTextField(20);
	        
	        String s1[] = { "PE (recommended)", "6", "HE", "EE", "Xalan" };
	        comboBox1 = new JComboBox(s1);
	        comboBox1.setSelectedItem(s1);
	        comboBox1.addItemListener(new BoxItemListener());
	        
	        input.addActionListener(new SettingsInputListener());
	        panel.add(new JLabel("Stylesheets folder: "));
	        panel.add(input);
	        panel.add(enterButton);
	        panel.add(output);
	        panel.add(new JLabel("Saxon version: "));
	        panel.add(comboBox1);
	        frame.getContentPane().add(BorderLayout.CENTER, panel);
	        frame.pack();
	        frame.setLocationByPlatform(true);
//	        frame.setLocationRelativeTo(null);
//	        frame.setLocationRelativeTo(customMessagesArea);
	        frame.setVisible(true);
//	        output.setText("C:/Users/imsh/testFolda" + "\n" + "C:/Users/imsh/Desktop/sample" + "\n");
	        output.append("Stylesheets folder: " + settingsMap.get("stylesheetsFolderPath"));
//	        output.append("the name of the <xsl:param> has to be on the same line as the opening tag");
		}
	}
	public static class SettingsButtonListener implements ActionListener {
		StandalonePluginWorkspace pluginWorkspaceAccess;
		
		public SettingsButtonListener (StandalonePluginWorkspace pluginWorkspaceAccess) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!input.getText().trim().equals("")) {
				settingsMap.put("stylesheetsFolderPath", input.getText().replace("\\", "/").replace("\"", ""));
				output.append("\n");
				output.append("path set to: " + settingsMap.get("stylesheetsFolderPath"));
				output.append("\n");
				input.setText("");
				
//				settingsMap.put("changed", "yes");
				BufferedWriter bf = null;
				
				try {
					bf = new BufferedWriter(new FileWriter(configFile));
					for (Map.Entry<String, String> entry: settingsMap.entrySet()) {
						bf.write(entry.getKey() + " - " + entry.getValue());
						bf.newLine();
					}
				} catch (IOException e1) {
					output.append("map writer can't: " + e1.getMessage());
				}
				
				finally {
					try {
						bf.close();
					} catch (IOException e1) {
						output.append("map writer can't close: " + e1.getMessage());
					}
				}
			}
			else {
//				settingsMap.put("changed", "no");
			}
			
			try {
				Scanner sc = new Scanner(configFile);
				if (sc.hasNext())
					addTree(new File(stylesheetsFolderPath), allStylesheets);
				else
					addTree(new File(System.getProperty("user.home") + "/OxygenPluginConfig"), allStylesheets);
			} catch (FileNotFoundException e1231) {
				output.append(e1231.getMessage());
			}
		}
		
	}
	public static class SettingsInputListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
//			output.setText(input.getText());
			if(!input.getText().trim().equals("")) {
				
				settingsMap.put("stylesheetsFolderPath", input.getText().replace("\\", "/").replace("\"", ""));
				output.append("\n");
				output.append("path set to: " + settingsMap.get("stylesheetsFolderPath"));
				output.append("\n");
				input.setText("");
				
//				settingsMap.put("changed", "yes");
				BufferedWriter bf = null;
				
				try {
					bf = new BufferedWriter(new FileWriter(configFile));
					for (Map.Entry<String, String> entry: settingsMap.entrySet()) {
						bf.write(entry.getKey() + " - " + entry.getValue());
						bf.newLine();
					}
				} catch (IOException e1) {
					output.append("map writer can't: " + e1.getMessage());
				}
				
				finally {
					try {
						bf.close();
					} catch (IOException e1) {
						output.append("map writer can't close: " + e1.getMessage());
					}
				}
			}
			else {
//				settingsMap.put("changed", "no");
			}
			
			try {
				Scanner sc = new Scanner(configFile);
				if (sc.hasNext())
					addTree(new File(stylesheetsFolderPath), allStylesheets);
				else
					addTree(new File(System.getProperty("user.home") + "/OxygenPluginConfig"), allStylesheets);
			} catch (FileNotFoundException e1231) {
				output.append(e1231.getMessage());
			}
			
		}
		
	}
	
	public static class BoxItemListener implements ItemListener{
		
		String saxonVersion;
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
		          saxonVersion = (String) e.getItem();
		          output.append("saxon version: " + saxonVersion + "\n");
		          settingsMap.put("saxonVersion", saxonVersion);
		       }
				BufferedWriter bf = null;
				
				try {
					bf = new BufferedWriter(new FileWriter(configFile));
					for (Map.Entry<String, String> entry: settingsMap.entrySet()) {
						bf.write(entry.getKey() + " - " + entry.getValue());
						bf.newLine();
					}
				} catch (IOException e1) {
					pluginWorkspaceAccess.showInformationMessage("map writer can't: " + e1.getMessage());
				}
				
				finally {
					try {
						bf.close();
					} catch (IOException e1) {
						pluginWorkspaceAccess.showInformationMessage("map writer can't close: " + e1.getMessage());
					}
				}
			}
		}
	
	public static void applyStylesheet(StandalonePluginWorkspace pluginWorkspaceAccess, Source xmlSrc, Source xslSrc, WSTextEditorPage textPage, HashMap<String, JTextField> map, ArrayList<String> paramNames) {
		 StringWriter sw = new StringWriter();
		    
		  try {
			Transformer transformerPE = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_PROFESSIONAL_EDITION);
			
			transformerPE.clearParameters();
			
//			if(map.size() > 0) {
			if(map != null) {
				for (int i = 0; i < map.size(); i++) {
					if(!map.get(paramNames.get(i)).getText().trim().equals("")) {
						transformerPE.setParameter(paramNames.get(i), map.get(paramNames.get(i)).getText());
					}
				}
			}
			transformerPE.transform(xmlSrc, new StreamResult(sw));
//			transformerPE.transform(xmlSrc, new StreamResult(textPage.toString()));
			
			int length = textPage.getDocument().getLength();
//			textPage.select(0, length);
//			textPage.deleteSelection();
			textPage.getDocument().remove(0, textPage.getDocument().getLength());
			textPage.getDocument().insertString(0, sw.toString(), null);
			
			
		} catch (TransformerException | BadLocationException e) {
			pluginWorkspaceAccess.showInformationMessage(xslSrc.getSystemId() + ": " + e.getMessage());
		}
	}
		
	public boolean applicationClosing() {
	  writeToSettingsMap();
    return true;
  }
	}
