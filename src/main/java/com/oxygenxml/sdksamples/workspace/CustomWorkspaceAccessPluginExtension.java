package com.oxygenxml.sdksamples.workspace;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.management.loading.PrivateClassLoader;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

import com.google.common.io.Files;
import com.ibm.icu.text.Edits.Iterator;
import com.oxygenxml.editor.swtutil.td;

import ro.sync.basic.io.FilePathToURI;
import ro.sync.basic.util.URLUtil;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.ecss.component.AuthorClipboardObject;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorAccessDeprecated;
import ro.sync.ecss.extensions.api.AuthorChangeTrackingController;
import ro.sync.ecss.extensions.api.AuthorClipboardAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorListener;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.AuthorResourceBundle;
import ro.sync.ecss.extensions.api.AuthorReviewController;
import ro.sync.ecss.extensions.api.AuthorViewToModelInfo;
import ro.sync.ecss.extensions.api.ClassPathResourcesAccess;
import ro.sync.ecss.extensions.api.OptionsStorage;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorOutlineAccess;
import ro.sync.ecss.extensions.api.access.AuthorTableAccess;
import ro.sync.ecss.extensions.api.access.AuthorUtilAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;
import ro.sync.ecss.extensions.api.access.AuthorXMLUtilAccess;
import ro.sync.ecss.extensions.api.highlights.Highlight;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.structure.AuthorPopupMenuCustomizer;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
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
import ro.sync.util.editorvars.EditorVariables;

/**
 * Plugin extension - workspace access extension.
 */

public class CustomWorkspaceAccessPluginExtension implements WorkspaceAccessPluginExtension {
/**
   * The custom messages area. A sample component added to your custom view.
   */
  private JTextArea customMessagesArea;
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationStarted(ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace)
   */
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
	  //You can set or read global options.
	  //The "ro.sync.exml.options.APIAccessibleOptionTags" contains all accessible keys.
	  //		  pluginWorkspaceAccess.setGlobalObjectProperty("can.edit.read.only.files", Boolean.FALSE);
	  // Check In action

	  //You can access the content inside each opened WSEditor depending on the current editing page (Text/Grid or Author).  
	  // A sample action which will be mounted on the main menu, toolbar and contextual menu.

	final Action selectionSourceAction = createShowSelectionAction(pluginWorkspaceAccess);
	final Action anotherAction = createAnotherAction(pluginWorkspaceAccess);
	
	// collecting all the found files and showing them in an infomessage
	Collection<File> allStylesheets = new ArrayList<File>();
    addTree(new File("C:/Users/imsh/testFolda"), allStylesheets);
    
    // an iterator to loop through the FILES collection
    java.util.Iterator<File> iterator1 = allStylesheets.iterator();
    //pluginWorkspaceAccess.showInformationMessage(String.valueOf(allStylesheets));
    //pluginWorkspaceAccess.showInformationMessage(FilePathToURI.filepath2URI(iterator1.next().getPath()));
    
    // a collection for the ACTIONS to be made of the files collection
    Collection<Action> allActions = new ArrayList<Action>();
    
    // loopin through files collection adding them as actions to the actions collection
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
    	//pluginWorkspaceAccess.showInformationMessage(number + ". " + currentFile.getName());
    	
    	// creating an action from the file while also loading it in as both a file and a source
    	Action newAction = createNewAction(pluginWorkspaceAccess, number, currentFile.getName(), currentFile, currentSource);
    	
    	// adding it to an array of actions (to thwack them all into a dropdown later)
    	allActions.add(newAction);
    }
    
//    java.util.Iterator<Action> iterator2 = allActions.iterator();
//    
//    while(iterator2.hasNext()) {
//    	Action currentAction = iterator2.next();
//    	//pluginWorkspaceAccess.showInformationMessage(String.valueOf(currentAction));
//    }
    
	//Mount the action on the contextual menus for the Text and Author modes.
	pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {
				/**
				 * Customize the author popup menu.
				 */
				@Override
				public void customizeAuthorPopUpMenu(JPopupMenu popup,
						AuthorAccess authorAccess) {
					
					// banana
					//pluginWorkspaceAccess.showInformationMessage("before gettin the url");
					URL editorURL = authorAccess.getEditorAccess().getEditorLocation();
					// Add our custom action
//					popup.add(selectionSourceAction);
				}

				@Override
				public void customizeTextPopUpMenu(JPopupMenu popup,
						WSTextEditorPage textPage) {
					// Add our custom action
//				    java.util.Iterator<Action> iterator3 = allActions.iterator();
//				    
//				    while(iterator3.hasNext()) {
//				    	Action currentAction = iterator3.next();
//				    	popup.add(currentAction);
//				    }
//					popup.add(selectionSourceAction);
//					popup.add(anotherAction);
					
				}
			});

	  // Create your own main menu and add it to Oxygen or remove one of Oxygen's menus...
	  pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {
		  /**
		   * @see ro.sync.exml.workspace.api.standalone.MenuBarCustomizer#customizeMainMenu(javax.swing.JMenuBar)
		   */
		  public void customizeMainMenu(JMenuBar mainMenuBar) {
//			  JMenu myFirstMenu = new JMenu("Menu1");
//			  myFirstMenu.add(selectionSourceAction);
//			  // Add your menu before the Help menu
//			  mainMenuBar.add(myFirstMenu, mainMenuBar.getMenuCount() - 2);
			  
			  JMenu mySecondMenu = new JMenu("Menu2");
			  
			  // iterator for the actions collection
			  java.util.Iterator<Action> iterator3 = allActions.iterator();
			    
			  // loopin through the actions collection adding them to the dropdown
			    while(iterator3.hasNext()) {
			    	Action currentAction = iterator3.next();
			    	mySecondMenu.add(currentAction);
			    }
			  //mySecondMenu.add(selectionSourceAction);
			  //mySecondMenu.add(anotherAction);
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
			  if("SampleWorkspaceAccessToolbarID".equals(toolbarInfo.getToolbarID())) {
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
				  comps.add(customButton);
				  toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
				  ToolbarButton customButton2 = new ToolbarButton(anotherAction, true);
				  comps.add(customButton2);
				  toolbarInfo.setComponents(comps.toArray(new JComponent[0]));
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
private AbstractAction createAnotherAction(final StandalonePluginWorkspace pluginWorkspaceAccess) {
	 return new AbstractAction("backup + read") {
		 public void actionPerformed(ActionEvent actionEvent) {
			 WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
			 WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
			 
			 // getting a date for the rename
			 String date = getDate();
			 
			 // editorAccess.save();
			 //current path where the file sits
			 String path = String.valueOf(editorAccess.getEditorLocation());
			 // cutting off the "file:" (oxygen does that) from the path
			 File file1 = new File(path.substring(6, path.length()));
			 File file2 = new File(path.substring(6, path.length()-8).concat('.' + date + path.substring(path.length()-8, path.length())));
			 
//			 pluginWorkspaceAccess.showInformationMessage(file1.getPath() + " " + file2.getPath());
			 
			 try {
				copyFileUsingChannel(file1, file2);
				
			} catch (IOException e) {
				pluginWorkspaceAccess.showInformationMessage("DOESN'T WORK.");
				e.printStackTrace();
			}
			
			// reading the first line of the copy cuz why not
	        Scanner sc;
			try {
				sc = new Scanner(file1);
//		        pluginWorkspaceAccess.showInformationMessage(sc.next());
			} catch (FileNotFoundException e) {
//				pluginWorkspaceAccess.showInformationMessage("NO FILE TO READ.");
				e.printStackTrace();
			}
		 }
	 };
 }
 
// adding ALL the files from the folder to an array
	static void addTree(File file, Collection<File> all) {
	    File[] children = file.listFiles();
	    if (children != null) {
	        for (File child : children) {
	            all.add(child);
	            addTree(child, all);
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
//					Object [] nodes = xmltextPage.evaluateXPath("/book/bookinfo[1]/keywordset[1]/keyword[11]");
//					Object [] nodes = xmltextPage.evaluateXPath(stringThere);
//					Object [] allNodes = xmltextPage.evaluateXPath("//node()");
					
						if(nodes.length > 0) {
							// we just cast an object as a node and it works
							currentNode = (Node) nodes[0];
//							pluginWorkspaceAccess.showInformationMessage("action1 " + currentNode.toString() +  currentNode.getParentNode() + currentNode.getParentNode().getParentNode() + currentNode.getTextContent());
//							pluginWorkspaceAccess.showInformationMessage(currentNode.getTextContent());
						}
//					pluginWorkspaceAccess.showInformationMessage(xmltextPage.findElementsByXPath(".").toString());
				} catch (XPathException e11) {
					// TODO Auto-generated catch block
					e11.printStackTrace();
				}
				ThingWindow thingWindow = new ThingWindow(pluginWorkspaceAccess, currentNode, xmlSrc, xslSrc, textPage);
				  thingWindow.popItUp();
				  
			}
		};
	}
	
	private AbstractAction createShowSelectionAction(
			final StandalonePluginWorkspace pluginWorkspaceAccess) {
		return new AbstractAction("action1") {

			public void actionPerformed(ActionEvent actionevent) {
				  //Get the current opened XML document
				  WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
				  // get the textpages and stuff
						  WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
						  WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
						  TextDocumentController tdController = (TextDocumentController) xmltextPage.getDocumentController();
						  Node currentNode = null;
						  try {
							// this, for some reason, returns the current thing the caret is sitting on, only one element in there
							Object [] nodes = xmltextPage.evaluateXPath(".");
//							Object [] nodes = xmltextPage.evaluateXPath("/book/bookinfo[1]/keywordset[1]/keyword[11]");
//							Object [] nodes = xmltextPage.evaluateXPath(stringThere);
//							Object [] allNodes = xmltextPage.evaluateXPath("//node()");
							
								if(nodes.length > 0) {
									// we just cast an object as a node and it works
									currentNode = (Node) nodes[0];
//									pluginWorkspaceAccess.showInformationMessage("action1 " + currentNode.toString() +  currentNode.getParentNode() + currentNode.getParentNode().getParentNode() + currentNode.getTextContent());
//									pluginWorkspaceAccess.showInformationMessage(currentNode.getTextContent());
								}
//							pluginWorkspaceAccess.showInformationMessage(xmltextPage.findElementsByXPath(".").toString());
						} catch (XPathException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						  ThingWindow thingWindow = new ThingWindow(pluginWorkspaceAccess, currentNode);
						  thingWindow.popItUp();
						  
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
	
	private static String ENTER = "Enter";
	private static String SHOW = "Show";
    static JButton enterButton;
    static JButton showButton;
    public static JTextArea output;
    public static JTextField input;
    static JFrame frame;
    static JPanel panel;
    public static String testString = "test";
    
	public class ThingWindow {
		StandalonePluginWorkspace pluginWorkspaceAccess;
		Node node;
		Source xmlSrc;
		Source xslSrc;
		WSTextEditorPage textPage;
		public ThingWindow (StandalonePluginWorkspace pluginWorkspaceAccess, Node node) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.node = node;
		}
		
		public ThingWindow (StandalonePluginWorkspace pluginWorkspaceAccess, Node node, Source xmlSrc, Source xslSrc, WSTextEditorPage textPage) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.node = node;
			this.xmlSrc = xmlSrc;
			this.xslSrc = xslSrc;
			this.textPage = textPage;		}
		
		public void popItUp(){
			frame = new JFrame("Test");
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setOpaque(true);
	        ButtonListener buttonListener = new ButtonListener(pluginWorkspaceAccess, xmlSrc, xslSrc, textPage);
	        output = new JTextArea(15, 50);
	        output.setWrapStyleWord(true);
	        output.setEditable(false);
	        JScrollPane scroller = new JScrollPane(output);
	        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        JPanel inputpanel = new JPanel();
	        inputpanel.setLayout(new FlowLayout());
	        input = new JTextField(20);
	        enterButton = new JButton("Enter");
	        showButton = new JButton("Show");
	        enterButton.setActionCommand(ENTER);
	        showButton.setActionCommand(SHOW);
	        enterButton.addActionListener(buttonListener);
	        showButton.addActionListener(buttonListener);
	        // enterButton.setEnabled(false);
	        input.setActionCommand(ENTER);
	        input.setActionCommand(SHOW);
	        input.addActionListener(buttonListener);
	        DefaultCaret caret = (DefaultCaret) output.getCaret();
	        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	        panel.add(scroller);
	        inputpanel.add(input);
	        inputpanel.add(enterButton);
	        inputpanel.add(showButton);
	        panel.add(inputpanel);
	        frame.getContentPane().add(BorderLayout.CENTER, panel);
	        frame.pack();
	        frame.setLocationByPlatform(true);
	        // Center of screen
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	        frame.setResizable(false);
	        input.requestFocus();
	        String string1 = EditorVariables.USER_HOME_DIR;
	        output.setText(System.getProperty("user.name") + " " + System.getProperty("user.home") + " " + string1);
	        output.append("\n");
	        output.append(node.toString() + " " + node.getParentNode() + " " + node.getTextContent());
	        output.append("\n");
	        output.append(xmlSrc.toString());
	        
		}
	};
	
	public static class ButtonListener implements ActionListener
    {	
		StandalonePluginWorkspace pluginWorkspaceAccess;
		AbstractAction action;
		Source xmlSrc;
		Source xslSrc;
		WSTextEditorPage textPage;
		public ButtonListener (StandalonePluginWorkspace pluginWorkspaceAccess) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
		}
		
		public ButtonListener (StandalonePluginWorkspace pluginWorkspaceAccess, Source xmlSrc, Source xslSrc, WSTextEditorPage textPage) {
			this.pluginWorkspaceAccess = pluginWorkspaceAccess;
			this.xmlSrc = xmlSrc;
			this.xslSrc = xslSrc;
			this.textPage = textPage;
		}
		public void actionPerformed(ActionEvent ev)
        {
            if (!input.getText().trim().equals(""))
            {
                String cmd = ev.getActionCommand();
                if (ENTER.equals(cmd))
                {	
                   WSEditor editorAccess = pluginWorkspaceAccess.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
  				   WSXMLTextEditorPage xmltextPage = (WSXMLTextEditorPage) editorAccess.getCurrentPage();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//                    output.append(input.getText());
//                    if (input.getText().trim().equals(testString)) output.append(" = " + testString);
//                    else output.append(" != " + testString);
                    output.append("\n");
                }
            }
            
            StringWriter sw = new StringWriter();
		    
		  try {
			  // transformation itself
			Transformer transformer1 = pluginWorkspaceAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0],XMLUtilAccess.TRANSFORMER_SAXON_6);
			// results are being put into a StringWriter
			transformer1.transform(xmlSrc, new StreamResult(sw));
		} catch (TransformerConfigurationException e) {
			pluginWorkspaceAccess.showInformationMessage(e.getMessage());
			e.printStackTrace();
		} catch (TransformerException e) {
			pluginWorkspaceAccess.showInformationMessage(e.getMessage());
			e.printStackTrace();
		}
//		pluginWorkspaceAccess.showInformationMessage(sw.toString() + textPage.getDocument().getLength());
//		pluginWorkspaceAccess.showInformationMessage(textPage.getDocument().getText(0, textPage.getDocument().getLength()));
		try {
			int length = textPage.getDocument().getLength();
			textPage.select(0, length);
			textPage.deleteSelection();
			textPage.getDocument().insertString(0, sw.toString(), null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  if (textPage.hasSelection()) {
			  pluginWorkspaceAccess.showInformationMessage(textPage.getSelectedText());
			  }
            input.setText("");
            input.requestFocus();
            
        }
		
    }
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
	  //You can reject the application closing here
    return true;
  }
  
  public class XSLTReportOperation extends AuthorOperationWithResult {
	  
	  @Override
	  public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
	      throws AuthorOperationException {
	    
	    AuthorAccess authorAccess = model.getAuthorAccess();
	    //Source xslSrc = new SAXSource(new org.xml.sax.InputSource(getScriptLocation(args)));
	    Source xslSrc = new SAXSource(new org.xml.sax.InputSource("C:/Users/imsh/testFolda/beispiel.xsl"));
	    Transformer transformer;
	    try {
	      transformer = authorAccess.getXMLUtilAccess().createXSLTTransformer(xslSrc, new URL[0], 
	          XMLUtilAccess.TRANSFORMER_SAXON_6); //TRANSFORMER_SAXON_PROFESSIONAL_EDITION
	    } catch (TransformerConfigurationException e) {
	      throw new IllegalStateException(e);
	    }
	    
	    AuthorEditorAccess editorAccess = authorAccess.getEditorAccess();
	    Reader docReader = editorAccess.createContentReader();
	    org.xml.sax.InputSource is = new org.xml.sax.InputSource(docReader);
	    is.setSystemId(editorAccess.getEditorLocation().toExternalForm());
	    Source xmlSrc = new SAXSource(is);
	    StringWriter sw = new StringWriter();
	    
	    try {
	      transformer.transform(xmlSrc, new StreamResult(sw ));
	    } catch (TransformerException e) {
	      return "FAILURE";
	    }
	    
	    return sw.toString();
	  }

	  private String getScriptLocation(ArgumentsMap args) {
	    String scriptLocation = (String) args.getArgumentValue("script");
	    File script = new File(XSLTReportPlugin.baseDir, scriptLocation);
	    try {
	      if (contains(XSLTReportPlugin.baseDir, script)) {
	        return URLUtil.correct(script).toExternalForm();
	      }
	    } catch (IOException e) {
	      throw new IllegalArgumentException("The 'script' file is not located in the plugin's folder.", e);
	    }
	    return null;
	  }

	  private boolean contains(File folder, File file) throws IOException {
	    return file.getCanonicalPath().startsWith(folder.getCanonicalPath() + File.separator);
	  }

	}

	}
