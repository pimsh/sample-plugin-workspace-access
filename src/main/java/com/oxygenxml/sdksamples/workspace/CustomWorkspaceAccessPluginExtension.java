package com.oxygenxml.sdksamples.workspace;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import com.google.common.io.Files;
import com.ibm.icu.text.Edits.Iterator;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.exml.editor.EditorPageConstants;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.listeners.WSEditorChangeListener;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ViewComponentCustomizer;
import ro.sync.exml.workspace.api.standalone.ViewInfo;
import ro.sync.exml.workspace.api.standalone.actions.MenusAndToolbarsContributorCustomizer;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

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
    java.util.Iterator<File> iterator1 = allStylesheets.iterator();
    //pluginWorkspaceAccess.showInformationMessage(String.valueOf(allStylesheets));
    
    // a collection of all actions built from the allStylesheets
    Collection<Action> allActions = new ArrayList<Action>();
    
    
    int number = 0;
    while (iterator1.hasNext())
    {	
    	// the current stylesheet loaded in to create an action
    	File currentFile = iterator1.next();
    	number += 1;
    	//pluginWorkspaceAccess.showInformationMessage(number + ". " + currentFile.getName());
    	
    	// creating an action from the stylesheet
    	Action newAction = createNewAction(pluginWorkspaceAccess, number, currentFile.getName(), currentFile);
    	
    	// adding it to an array of action (to thwack them all into a dropdown later)
    	allActions.add(newAction);
    }
    
    java.util.Iterator<Action> iterator2 = allActions.iterator();
    
    while(iterator2.hasNext()) {
    	Action currentAction = iterator2.next();
    	//pluginWorkspaceAccess.showInformationMessage(String.valueOf(currentAction));
    }
    
	//Mount the action on the contextual menus for the Text and Author modes.
	pluginWorkspaceAccess.addMenusAndToolbarsContributorCustomizer(new MenusAndToolbarsContributorCustomizer() {
				/**
				 * Customize the author popup menu.
				 */
				@Override
				public void customizeAuthorPopUpMenu(JPopupMenu popup,
						AuthorAccess authorAccess) {
					// Add our custom action
					popup.add(selectionSourceAction);
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
					popup.add(selectionSourceAction);
					popup.add(anotherAction);
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
			  java.util.Iterator<Action> iterator3 = allActions.iterator();
			    
			    while(iterator3.hasNext()) {
			    	Action currentAction = iterator3.next();
			    	mySecondMenu.add(currentAction);
			    }
			  mySecondMenu.add(selectionSourceAction);
			  mySecondMenu.add(anotherAction);
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
			 
			 pluginWorkspaceAccess.showInformationMessage(file1.getPath() + " " + file2.getPath());
			 
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
		        pluginWorkspaceAccess.showInformationMessage(sc.next());
			} catch (FileNotFoundException e) {
				pluginWorkspaceAccess.showInformationMessage("NO FILE TO COPY.");
				e.printStackTrace();
			}
		 }
	 };
 }
 
 // adding the files from the folder to an array
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
	
	private AbstractAction createNewAction(final StandalonePluginWorkspace pluginWorkspaceAccess, int actionNumber, String actionName, File actionFile) {
		//pluginWorkspaceAccess.showInformationMessage(actionName + " " + actionNumber + " created");
		return new AbstractAction(actionNumber + ". " + actionName) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//pluginWorkspaceAccess.showInformationMessage("action" + actionNumber + " clicked");
				Scanner sc;
				try {
					sc = new Scanner(actionFile);
			        pluginWorkspaceAccess.showInformationMessage(sc.next());
				} catch (FileNotFoundException ex) {
					pluginWorkspaceAccess.showInformationMessage("NO FILE TO COPY.");
					ex.printStackTrace();
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
				  // The action is available only in Author mode.
				  if(editorAccess != null){
					  if (EditorPageConstants.PAGE_AUTHOR.equals(editorAccess.getCurrentPageID())) {
						  WSAuthorEditorPage authorPageAccess = (WSAuthorEditorPage) editorAccess.getCurrentPage();
						  AuthorDocumentController controller = authorPageAccess.getDocumentController();
						  if (authorPageAccess.hasSelection()) {
							  AuthorDocumentFragment selectionFragment;
							  try {
								  // Create fragment from selection
								  selectionFragment = controller.createDocumentFragment(
										  authorPageAccess.getSelectionStart(),
										  authorPageAccess.getSelectionEnd() - 1
										  );
								  // Serialize
								  String serializeFragmentToXML = controller.serializeFragmentToXML(selectionFragment);
								  // Show fragment
								  pluginWorkspaceAccess.showInformationMessage(serializeFragmentToXML);
							  } catch (BadLocationException e) {
								  pluginWorkspaceAccess.showErrorMessage("Show Selection Source operation failed: " + e.getMessage());
							  }
						  } else {
							  // No selection
							  pluginWorkspaceAccess.showInformationMessage("No selection available.");
						  }
					  } else if (EditorPageConstants.PAGE_TEXT.equals(editorAccess.getCurrentPageID())) {
						  WSTextEditorPage textPage = (WSTextEditorPage) editorAccess.getCurrentPage();
						  if (textPage.hasSelection()) {
							  pluginWorkspaceAccess.showInformationMessage(textPage.getSelectedText());
						  } else {
							  // No selection
							  pluginWorkspaceAccess.showInformationMessage("NOTHING SELECTED.");
						  }
					  }
				  }
			  }
		  };
	}
  
  /**
   * @see ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension#applicationClosing()
   */
  public boolean applicationClosing() {
	  //You can reject the application closing here
    return true;
  }
}