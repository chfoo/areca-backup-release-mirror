package com.application.areca.launcher.gui;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.application.areca.AbstractTarget;
import com.application.areca.CheckParameters;
import com.application.areca.context.MaxCapacityList;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
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
public class CheckWindow 
extends AbstractWindow {
    private static final ResourceManager RM = ResourceManager.instance();
    
    private AbstractTarget target;
    private Text txtLocation;
    private Button btnBrowse;
    private Button chkCheckSelectedEntries;
    private Button radUseDefaultLocation;
    private Button radUseSpecificLocation;
    private Button btnSave;
    private Label result;
    private Table table;
    private TableViewer viewer;
    private Application.ProcessRunner runner;

    public CheckWindow(AbstractTarget target) {
    	super();
    	this.target = target;
    }
    
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 10;
        composite.setLayout(layout);

        final Group grpLocation = new Group(composite, SWT.NONE);
        grpLocation.setText(RM.getLabel("check.location.label"));
        grpLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout grpLayout = new GridLayout(3, false);
        grpLayout.verticalSpacing = 0;
        grpLocation.setLayout(grpLayout);
        
        radUseDefaultLocation = new Button(grpLocation, SWT.RADIO);
        radUseDefaultLocation.setText(RM.getLabel("check.default.label"));
        radUseDefaultLocation.setToolTipText(RM.getLabel("check.default.tt"));
        radUseDefaultLocation.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        monitorControl(SWT.Selection, radUseDefaultLocation);
        
        radUseSpecificLocation = new Button(grpLocation, SWT.RADIO);
        radUseSpecificLocation.setText(RM.getLabel("check.specific.label"));
        radUseSpecificLocation.setToolTipText(RM.getLabel("check.specific.tt"));
        radUseSpecificLocation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        monitorControl(SWT.Selection, radUseSpecificLocation);

        txtLocation = new Text(grpLocation, SWT.BORDER);
        GridData mainData2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mainData2.widthHint = computeWidth(200);
        txtLocation.setLayoutData(mainData2);       
        monitorControl(txtLocation);
        
        btnBrowse = new Button(grpLocation, SWT.PUSH);
        btnBrowse.setText(RM.getLabel("common.browseaction.label"));
        btnBrowse.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String path = Application.getInstance().showDirectoryDialog(txtLocation.getText(), CheckWindow.this);
                if (path != null) {
                    txtLocation.setText(path);
                }
            }
        });
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        AbstractIncrementalFileSystemMedium medium = (AbstractIncrementalFileSystemMedium)target.getMedium();
        if (! medium.isImage()) {
            chkCheckSelectedEntries = new Button(composite, SWT.CHECK);
            chkCheckSelectedEntries.setText(RM.getLabel("check.checkselected.label"));
            chkCheckSelectedEntries.setToolTipText(RM.getLabel("check.checkselected.tt"));
            chkCheckSelectedEntries.setLayoutData(new GridData());       
            monitorControl(SWT.Selection, chkCheckSelectedEntries);
            
            chkCheckSelectedEntries.setEnabled(medium.getHandler().autonomousArchives());
            chkCheckSelectedEntries.setSelection(medium.getHandler().autonomousArchives());
        }

        radUseDefaultLocation.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	switchLocation();
            }
        });
        
        radUseSpecificLocation.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	switchLocation();
            }
        });
        
        SavePanel pnlSave = new SavePanel(RM.getLabel("check.check.label"), this);
        pnlSave.setShowCancel(false);
        pnlSave.buildComposite(composite).setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));        
        btnSave = pnlSave.getBtnSave();
        
        // Result
        viewer = new TableViewer(composite, SWT.BORDER| SWT.SINGLE);
        table = viewer.getTable();
        table.setLinesVisible(AbstractWindow.getTableLinesVisible());
        table.setHeaderVisible(true);
        TableColumn col1 = new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        col1.setMoveable(true);
        TableColumn col2 = new org.eclipse.swt.widgets.TableColumn(table, SWT.NONE);
        col2.setMoveable(true);
        table.getColumn(0).setWidth(AbstractWindow.computeWidth(400));
        table.getColumn(1).setWidth(AbstractWindow.computeWidth(100));
        GridData dtTable = new GridData(SWT.FILL, SWT.FILL, true, true);
        dtTable.heightHint = computeHeight(200);
        table.setLayoutData(dtTable);
        
        result = new Label(composite, SWT.NONE);
        result.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // INIT DATA
        txtLocation.setText(ArecaUserPreferences.getCheckSpecificLocation(application.getCurrentWorkspaceItem().getUid()));
        
        if (ArecaUserPreferences.isCheckForceDefaultLocation(application.getCurrentWorkspaceItem().getUid())) {
            radUseDefaultLocation.setSelection(true);
        } else {
            radUseSpecificLocation.setSelection(true);
        }

        switchLocation();
        composite.pack();
        return composite;
    }
    
    public void closeInError(Exception e) {
    	SecuredRunner.execute(new Runnable() {
			public void run() {
		        application.disableWaitCursor(CheckWindow.this);
		        CheckWindow.this.close();
	    	}
    	});
    }
    
    public void setResult(final MaxCapacityList errorFiles, final MaxCapacityList uncheckedFiles, final MaxCapacityList unrecoveredFiles, final long nbChecked) {
    	final String invalidMsg = RM.getLabel("check.invalid.label");
    	final String uncheckedMsg = RM.getLabel("check.unchecked.label");
    	final String unrecoveredMsg = RM.getLabel("check.unrecovered.label");
    	final String truncatedList = RM.getLabel("check.truncated.label");
    	
    	SecuredRunner.execute(new Runnable() {
			public void run() {
		    	try {	
		            application.disableWaitCursor(CheckWindow.this);
		            
					// Unrecovered files
					Iterator iter = unrecoveredFiles.iterator();
					while (iter.hasNext()) {
					    TableItem item = new TableItem(table, SWT.NONE);
					    item.setText(0, (String)iter.next());
					    item.setText(1, unrecoveredMsg);
					}
		            
		            // Files in error
					iter = errorFiles.iterator();
					while (iter.hasNext()) {
					    TableItem item = new TableItem(table, SWT.NONE);
					    item.setText(0, (String)iter.next());
					    item.setText(1, invalidMsg);
					}
					
					// Unchecked files
					iter = uncheckedFiles.iterator();
					while (iter.hasNext()) {
					    TableItem item = new TableItem(table, SWT.NONE);
					    item.setText(0, (String)iter.next());
					    item.setText(1, uncheckedMsg);
					}
					
					if (uncheckedFiles.isSaturated() || unrecoveredFiles.isSaturated() || errorFiles.isSaturated()) {
					    TableItem item = new TableItem(table, SWT.NONE);
					    item.setText(0, truncatedList);
					    item.setText(1, "...");
					}
					
					if (! errorFiles.isEmpty() || ! unrecoveredFiles.isEmpty()) {
						result.setText(RM.getLabel("check.invalid.message"));
					} else if (! uncheckedFiles.isEmpty()) {
						result.setText(RM.getLabel("check.unchecked.message"));
					} else {
						result.setText(RM.getLabel("check.ok.message") + " (" + RM.getLabel("check.checked.label", new Object[] {"" + nbChecked}) + ")");
					}
				} catch (SWTException e) {
					// Widget disposed
					Logger.defaultLogger().error(e);
				}
			}
    	});
    }
    
    private void switchLocation() {
    	boolean defaultLocation = radUseDefaultLocation.getSelection();
    	txtLocation.setEnabled(! defaultLocation);
    	btnBrowse.setEnabled(! defaultLocation);
    	checkBusinessRules();
    }
    
    public String getTitle() {
        return RM.getLabel("check.dialog.title") + " (" + target.getName() + ")";
    }

	protected boolean checkBusinessRules() {
        this.resetErrorState(txtLocation); 
        if (radUseSpecificLocation.getSelection()) {
	        if (this.txtLocation.getText() == null || this.txtLocation.getText().length() == 0) {
	            this.setInError(txtLocation, RM.getLabel("error.field.mandatory"));
	            return false;
	        }
        }
        return true;
    }

    protected void saveChanges() { 
		ArecaUserPreferences.setCheckForceDefaultLocation(radUseDefaultLocation.getSelection(), application.getCurrentWorkspaceItem().getUid());
		ArecaUserPreferences.setCheckSpecificLocation(txtLocation.getText(), application.getCurrentWorkspaceItem().getUid());
		
    	this.viewer.setItemCount(0);
    	this.result.setText("");
        this.hasBeenUpdated = false;

        CheckParameters checkParams = new CheckParameters(
        		true,
        		this.chkCheckSelectedEntries != null && this.chkCheckSelectedEntries.getSelection(),
        		true,
        		this.radUseSpecificLocation.getSelection(),
        		this.txtLocation.getText()
        );

        this.runner = application.launchArchiveCheck(checkParams, target, this);
        if (runner != null) {
        	application.enableWaitCursor(this);
        }
    }

    protected void updateState(boolean rulesSatisfied) {
        this.btnSave.setEnabled(rulesSatisfied);
    }

	public boolean close() {
		boolean closed = super.close();
		if (closed && runner != null && runner.getChannel() != null && runner.getChannel().isRunning() && runner.getChannel().getTaskMonitor() != null) {
			// Cancel the current task
			runner.getChannel().getTaskMonitor().setCancelRequested();
		}
		return closed;
	}
}
