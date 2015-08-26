package com.application.areca.launcher.gui.composites;

import java.io.File;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.handler.DefaultArchiveHandler;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.system.viewer.ViewerHandlerHelper;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */
public class PropertiesComposite 
extends Composite { 
    private static final ResourceManager RM = ResourceManager.instance();
    
    private Table table;
    private TableViewer viewer;
    private Application application = Application.getInstance();
    private Link lnk;
    
    public PropertiesComposite(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout lt = new GridLayout(1, false);
        lt.marginHeight = 0;
        lt.marginWidth = 0;
        setLayout(lt);

        viewer = new TableViewer(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

        table = viewer.getTable();
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        TableColumn col1 = new TableColumn (table, SWT.NONE);
        col1.setWidth(AbstractWindow.computeWidth(100));
        col1.setMoveable(true);
        TableColumn col2 = new TableColumn (table, SWT.NONE);
        col2.setWidth(AbstractWindow.computeWidth(250));
        col2.setMoveable(true);

        lnk = new Link(this, SWT.NONE);
        lnk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
        // REFRESH DATA
        this.refresh();
    }
    
    public void refresh() {
    	// reset properties table
        table.removeAll();
        
        // Reset edition link
        lnk.setText("");
        Listener[] listeners = lnk.getListeners(SWT.Selection);
        for (int i=0; i<listeners.length; i++) {
        	lnk.removeListener(SWT.Selection, listeners[i]);
        }
        
        // Fill data
        if (application.isCurrentObjectTarget()) {
            fillData((FileSystemTarget)application.getCurrentTarget());
        } else if (application.isCurrentObjectTargetGroup()) {
            fillData(application.getCurrentTargetGroup());
        }
    }
    
    private void fillGenericData(WorkspaceItem item) {
    	final String conf = 
    		item.getLoadedFrom() == null ? 
    		FileSystemManager.getAbsolutePath(item.computeConfigurationFile(application.getWorkspace().getPathFile())) : 
    		item.getLoadedFrom().toString();
        this.addProperty(RM.getLabel("property.conf.label"), conf);
        
        if (item.getLoadedFrom().isBackupCopy()) {
        	this.addProperty(RM.getLabel("property.backup.label"), "true");
        }
        
        // Add link to configuration file
        lnk.setText("<A HREF=\"\">" + conf + "</A>");
        lnk.addListener (SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                try {
                    try {
						ViewerHandlerHelper.getViewerHandler().open(new File(conf));
					} catch (Exception e) {
						if (Application.getInstance().isCurrentObjectTarget()) {
							Application.getInstance().showEditTargetXML(application.getInstance().getCurrentTarget());
						} else {
							throw e;
						}
					}
                } catch (Exception e) {
                    Logger.defaultLogger().error(e);
                }
            }
        });
    }
    
    private void fillData(TargetGroup group) {
        this.addProperty(RM.getLabel("property.element.label"), group.getName());
        fillGenericData(group);
        this.addProperty(RM.getLabel("property.targets.label"), String.valueOf(group.size()));
    }
    
    private void fillData(FileSystemTarget tg) {
        try {
            String tgName = null;
            if (tg.getName() == null) {
                tgName = RM.getLabel("property.targetwithid.label") + tg.getId();
            } else {
                tgName = tg.getName();
            }
            
            this.addProperty(RM.getLabel("property.element.label"), tgName);
            if (tg.getId() >= 0) {
            	this.addProperty(RM.getLabel("property.id.label"), String.valueOf(tg.getId()));
            }
            this.addProperty(RM.getLabel("property.uid.label"), String.valueOf(tg.getUid()));
            fillGenericData(tg);
            if (tg.getComments() != null && tg.getComments().length() != 0) {
                this.addProperty(RM.getLabel("property.description.label"), tg.getComments());
            }  
            this.addProperty(RM.getLabel("property.source.label"), tg.getSourcesRoot());

            if (tg.getMedium() != null && tg.getMedium() instanceof AbstractIncrementalFileSystemMedium) {
                AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)tg.getMedium();
                
                this.addProperty(RM.getLabel("property.directory.label"), medium.getFileSystemPolicy().getDisplayableParameters(false));
                
                if (medium.isImage()) {
                    this.addProperty(RM.getLabel("property.type.label"), RM.getLabel("targetedition.storagetype.image"));
                } else if (medium.getHandler() instanceof DefaultArchiveHandler) {
                    this.addProperty(RM.getLabel("property.type.label"), RM.getLabel("targetedition.storagetype.multiple"));
                } else {
                    this.addProperty(RM.getLabel("property.type.label"), RM.getLabel("targetedition.storagetype.delta"));                	
                }
            }
        } catch (Throwable e) {
            application.handleException(e);
        }
    }
    
    private void addProperty(String label, String value) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, label + " : ");
        item.setText(1, AbstractWindow.configureForTable(value));
    }
}
