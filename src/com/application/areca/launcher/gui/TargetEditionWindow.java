package com.application.areca.launcher.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ArchiveFilter;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.filter.AbstractArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.EncryptionConfiguration;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalTGZMedium;
import com.application.areca.impl.IncrementalZip64Medium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.policy.DefaultFileSystemPolicy;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FTPFileSystemPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaPreferences;
import com.application.areca.launcher.gui.common.LocalPreferences;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.plugins.StoragePlugin;
import com.application.areca.plugins.StoragePluginRegistry;
import com.application.areca.plugins.StorageSelectionHelper;
import com.application.areca.postprocess.PostProcessor;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.history.History;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class TargetEditionWindow
extends AbstractWindow {

    private static final ResourceManager RM = ResourceManager.instance();
    private static final String TITLE = RM.getLabel("targetedition.dialog.title");
    private static final String PLUGIN_HD = "hd";
    
    protected AbstractRecoveryTarget target;
    protected FileSystemPolicy currentPolicy = null;
    protected boolean hasBeenSaved = false;
    protected ArrayList lstEncryptionAlgorithms = new ArrayList();
    
    protected Button btnSave;
    
    protected Text txtTargetName;
    protected Text txtBaseDir;
    protected Text txtDesc;
    
    protected Text txtMediumPath;
    protected Button rdFile;
    protected Button btnMediumPath;
    
    protected Map strRadio = new HashMap();
    protected Map strText = new HashMap();
    protected Map strButton = new HashMap();
    protected String currentFileSystemPolicyId = null;
    
    protected Group grpCompression;
    protected Group grpEncryption;
    protected Group grpFileManagement;
    protected Button rdDir;
    protected Button rdZip;
    protected Button rdZip64;
    //protected Button rdTgz;
    protected Button chkTrackDirectories;
    protected Button chkTrackPermissions;
    protected Button chkEncrypted;
    protected Button chkIncremental;
    protected Text txtEncryptionKey;
    protected Combo cboEncryptionAlgorithm;
    protected Label lblEncryptionKey;
    protected Label lblEncryptionAlgorithm;
    
    protected Table tblFilters;
    protected Button btnAddFilter;
    protected Button btnRemoveFilter;
    protected Button btnModifyFilter;
 
    protected Table tblProc;  
    protected Button btnAddProc;
    protected Button btnRemoveProc;
    protected Button btnModifyProc;
    
    public TargetEditionWindow(AbstractRecoveryTarget target) {
        super();
        this.target = target;
    }

    protected Control createContents(Composite parent) {
        application.enableWaitCursor();
        Composite ret = new Composite(parent, SWT.NONE);
        try {
            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            ret.setLayout(layout);

            CTabFolder tabs = new CTabFolder(ret, SWT.BORDER);
            tabs.setSimple(Application.SIMPLE_SUBTABS);
            GridData dt1 = new GridData();
            dt1.grabExcessHorizontalSpace = true;
            dt1.grabExcessVerticalSpace = true;
            dt1.horizontalAlignment = SWT.FILL;
            dt1.verticalAlignment = SWT.FILL;
            tabs.setLayoutData(dt1);

            initGeneralTab(initTab(tabs, RM.getLabel("targetedition.maingroup.title")));
            initAdvancedTab(initTab(tabs, RM.getLabel("targetedition.advancedgroup.title")));
            initFiltersTab(initTab(tabs, RM.getLabel("targetedition.filtersgroup.title")));
            initProcessorsTab(initTab(tabs, RM.getLabel("targetedition.postprocessing.title")));

            SavePanel pnlSave = new SavePanel(this);
            Composite save = pnlSave.buildComposite(ret);
            GridData dt2 = new GridData();
            dt2.grabExcessHorizontalSpace = true;
            dt2.horizontalAlignment = SWT.FILL;
            save.setLayoutData(dt2);
            btnSave = pnlSave.getBtnSave();
            
            initValues();
            
            tabs.setSelection(0);
        } finally {
            application.disableWaitCursor();
        }
        return ret;
    }
    
    private Composite initTab(CTabFolder tabs, String title) {
        CTabItem itm = new CTabItem(tabs, SWT.NONE);
        itm.setText(title + "    ");
        Composite composite = new Composite(tabs, SWT.NONE);
        itm.setControl(composite);
        return composite;
    }
    
    private GridLayout initLayout(int nbCols) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 10;
        layout.numColumns = nbCols;
        layout.marginHeight = 10;
        layout.verticalSpacing = 10;
        layout.horizontalSpacing = 10;
        return layout;
    }
    
    private void initGeneralTab(Composite composite) {
        composite.setLayout(initLayout(1));

        // NAME
        Group grpTargetName = new Group(composite, SWT.NONE);
        grpTargetName.setText(RM.getLabel("targetedition.targetnamefield.label"));
        grpTargetName.setLayout(new GridLayout(1, false));
        grpTargetName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        txtTargetName = new Text(grpTargetName, SWT.BORDER);
        txtTargetName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        monitorControl(txtTargetName);
        
        // SOURCE
        Group grpBaseDir = new Group(composite, SWT.NONE);
        grpBaseDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        grpBaseDir.setText(RM.getLabel("targetedition.sourcedirfield.label"));
        grpBaseDir.setToolTipText(RM.getLabel("targetedition.sourcedirfield.tooltip"));
        GridLayout lytBaseDir = new GridLayout(2, false);
        grpBaseDir.setLayout(lytBaseDir);
        txtBaseDir = new Text(grpBaseDir, SWT.BORDER);
        txtBaseDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtBaseDir);
        
        Button btnBaseDir = new Button(grpBaseDir, SWT.PUSH);
        btnBaseDir.setText(RM.getLabel("common.browseaction.label"));
        btnBaseDir.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String dir = txtBaseDir.getText();
                if (Utils.isEmpty(dir)) {
                    dir = LocalPreferences.instance().get("target.lastsourcedir");
                }
                
                String path = Application.getInstance().showDirectoryDialog(dir, TargetEditionWindow.this);
                if (path != null) {
                    LocalPreferences.instance().set("target.lastsourcedir", path);
                    txtBaseDir.setText(path);
                }
            }
        });
        
        // PATH (FILE)
        Group grpPath = new Group(composite, SWT.NONE);
        grpPath.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        grpPath.setText(RM.getLabel("targetedition.storagedirfield.label"));
        grpPath.setToolTipText(RM.getLabel("targetedition.storagedirfield.tooltip"));
        grpPath.setLayout(new GridLayout(3, false));
        
        rdFile = new Button(grpPath, SWT.RADIO);
        rdFile.setText(RM.getLabel("targetedition.storage.file"));
        rdFile.setToolTipText(RM.getLabel("targetedition.storage.file.tt"));
        txtMediumPath = new Text(grpPath, SWT.BORDER);
        txtMediumPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        monitorControl(txtMediumPath);
        btnMediumPath = new Button(grpPath, SWT.PUSH);
        btnMediumPath.setText(RM.getLabel("common.browseaction.label"));
        btnMediumPath.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                String dir = txtMediumPath.getText();
                if (Utils.isEmpty(dir)) {
                    dir = LocalPreferences.instance().get("target.lasttargetdir");
                }
                String path = Application.getInstance().showDirectoryDialog(dir, TargetEditionWindow.this);
                if (path != null) {
                    LocalPreferences.instance().set("target.lasttargetdir", path);
                    txtMediumPath.setText(path);
                }
            }
        });
        rdFile.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                processSelection(PLUGIN_HD, "");
            }
        });
        this.strButton.put(PLUGIN_HD, btnMediumPath);
        this.strText.put(PLUGIN_HD, txtMediumPath);
        this.strRadio.put(PLUGIN_HD, rdFile);
        
        // Plugins
        Iterator iter = StoragePluginRegistry.getInstance().getDisplayable().iterator();
        while (iter.hasNext()) {
            final StoragePlugin plugin = (StoragePlugin)iter.next();
            Button rd = new Button(grpPath, SWT.RADIO);
            rd.setText(plugin.getDisplayName() == null ? "UNDEFINED" : plugin.getDisplayName());
            rd.setToolTipText(plugin.getToolTip() == null ? "" : plugin.getToolTip());
            rd.addListener(SWT.Selection, new Listener(){
                public void handleEvent(Event event) {
                    StorageSelectionHelper helper = plugin.getStorageSelectionHelper(); 
                    helper.setWindow(TargetEditionWindow.this);
                    helper.handleSelection();
                    processSelection(plugin.getId(), "");
                }
            });
            this.strRadio.put(plugin.getId(), rd);
            
            final Text text = new Text(grpPath, SWT.BORDER);
            text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            text.setEditable(false);
            monitorControl(text);
            this.strText.put(plugin.getId(), text);
            
            Button btn = new Button(grpPath, SWT.PUSH);
            btn.setText(RM.getLabel("common.browseaction.label"));
            btn.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    StorageSelectionHelper helper = plugin.getStorageSelectionHelper(); 
                    helper.setWindow(TargetEditionWindow.this);
                    FileSystemPolicy newPolicy = helper.handleConfiguration();
                    if (newPolicy != null) {
                        currentPolicy = newPolicy;
                        text.setText(currentPolicy.getDisplayableParameters());
                        registerUpdate();                
                    } 
                }
            });
            this.strButton.put(plugin.getId(), btn);
        }
        
        txtTargetName.forceFocus();
    }

    private void initAdvancedTab(Composite composite) {
        composite.setLayout(initLayout(2));

        // DESC
        Group grpDesc = new Group(composite, SWT.NONE);
        grpDesc.setText(RM.getLabel("targetedition.descriptionfield.label"));
        grpDesc.setLayout(new GridLayout(1, false));
        grpDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        txtDesc = new Text(grpDesc, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        monitorControl(txtDesc);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        dt.widthHint = 500;
        dt.heightHint = 50;
        txtDesc.setLayoutData(dt);
        
        // COMPRESSION
        grpCompression = new Group(composite, SWT.NONE);
        grpCompression.setText(RM.getLabel("targetedition.compression.label"));
        RowLayout lytCompression = new RowLayout(SWT.VERTICAL);
        grpCompression.setLayout(lytCompression);
        grpCompression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        rdDir = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdDir);
        rdDir.setText(RM.getLabel("targetedition.compression.none"));
        rdDir.setToolTipText(RM.getLabel("targetedition.compression.none.tt"));
        
        rdZip = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdZip);
        rdZip.setText(RM.getLabel("targetedition.compression.zip"));
        rdZip.setToolTipText(RM.getLabel("targetedition.compression.zip.tt"));
        
        //rdTgz = new Button(grpCompression, SWT.RADIO);
        //monitorControl(rdTgz);
        //rdTgz.setText(RM.getLabel("targetedition.compression.tgz"));
        //rdTgz.setToolTipText(RM.getLabel("targetedition.compression.tgz.tt"));
        
        rdZip64 = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdZip64);
        rdZip64.setText(RM.getLabel("targetedition.compression.zip64"));
        rdZip64.setToolTipText(RM.getLabel("targetedition.compression.zip64.tt"));
        
        // FILE MANAGEMENT
        grpFileManagement = new Group(composite, SWT.NONE);
        grpFileManagement.setText(RM.getLabel("targetedition.filemanagement.label"));
        RowLayout lytFileManagement = new RowLayout(SWT.VERTICAL);
        grpFileManagement.setLayout(lytFileManagement);
        grpFileManagement.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        chkIncremental = new Button(grpFileManagement, SWT.CHECK);
        monitorControl(chkIncremental);
        chkIncremental.setText(RM.getLabel("targetedition.incremental.label"));
        chkIncremental.setToolTipText(RM.getLabel("targetedition.incremental.tooltip"));
        
        chkTrackDirectories = new Button(grpFileManagement, SWT.CHECK);
        monitorControl(chkTrackDirectories);
        chkTrackDirectories.setText(RM.getLabel("targetedition.trackdirs.label"));
        chkTrackDirectories.setToolTipText(RM.getLabel("targetedition.trackdirs.tooltip"));
        
        chkTrackPermissions = new Button(grpFileManagement, SWT.CHECK);
        monitorControl(chkTrackPermissions);
        chkTrackPermissions.setText(RM.getLabel("targetedition.trackperms.label"));
        chkTrackPermissions.setToolTipText(RM.getLabel("targetedition.trackperms.tooltip"));
        
        // ENCRYPTION
        grpEncryption = new Group(composite, SWT.NONE);
        grpEncryption.setText(RM.getLabel("targetedition.encryption.label"));
        grpEncryption.setLayout(new GridLayout(2, false));
        grpEncryption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        chkEncrypted = new Button(grpEncryption, SWT.CHECK);
        chkEncrypted.setText(RM.getLabel("targetedition.encryption.label"));
        chkEncrypted.setToolTipText(RM.getLabel("targetedition.encryption.tooltip"));
        monitorControl(chkEncrypted);
        chkEncrypted.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
        chkEncrypted.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                resetEcryptionKey();
            }
        });
        
        lblEncryptionAlgorithm = new Label(grpEncryption, SWT.NONE);
        lblEncryptionAlgorithm.setText(RM.getLabel("targetedition.algorithmfield.label"));
        lblEncryptionAlgorithm.setToolTipText(RM.getLabel("targetedition.algorithmfield.tooltip", new Object[] {EncryptionConfiguration.getParameters(EncryptionConfiguration.RECOMMENDED_ALGORITHM).getAlgorithm()}));
        cboEncryptionAlgorithm = new Combo(grpEncryption, SWT.READ_ONLY);
        monitorControl(cboEncryptionAlgorithm);
        cboEncryptionAlgorithm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Iterator algIter = EncryptionConfiguration.getAvailableNonDeprecatedAlgorithms().iterator();
        while (algIter.hasNext()) {
            String id = (String)algIter.next();
            EncryptionConfiguration conf = EncryptionConfiguration.getParameters(id);
            lstEncryptionAlgorithms.add(conf);
            cboEncryptionAlgorithm.add(conf.getFullName());
        }
        
        lblEncryptionKey = new Label(grpEncryption, SWT.NONE);
        lblEncryptionKey.setText(RM.getLabel("targetedition.keyfield.label"));
        lblEncryptionKey.setToolTipText(RM.getLabel("targetedition.keyfield.tooltip"));
        txtEncryptionKey = new Text(grpEncryption, SWT.BORDER);
        monitorControl(txtEncryptionKey);
        txtEncryptionKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }
    
    private void initFiltersTab(Composite composite) {
        composite.setLayout(initLayout(4));
        
        tblFilters = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        tblFilters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        
        TableColumn col1 = new TableColumn(tblFilters, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.filterstable.type.label"));
        col1.setWidth(100);
        col1.setMoveable(true);
        
        TableColumn col2 = new TableColumn(tblFilters, SWT.NONE);
        col2.setText(RM.getLabel("targetedition.filterstable.parameters.label"));
        col2.setWidth(300);
        col2.setMoveable(true);
        
        TableColumn col3 = new TableColumn(tblFilters, SWT.NONE);
        col3.setText(RM.getLabel("targetedition.filterstable.mode.label"));
        col3.setWidth(100);
        col3.setMoveable(true);
        
        tblFilters.setHeaderVisible(true);
        tblFilters.setLinesVisible(AbstractWindow.getTableLinesVisible());
        
        btnAddFilter = new Button(composite, SWT.PUSH);
        btnAddFilter.setText(RM.getLabel("targetedition.addfilteraction.label"));
        btnAddFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                ArchiveFilter newFilter = showFilterEditionFrame(null);
                if (newFilter != null) {
                    addFilter(newFilter);
                    registerUpdate();                
                }
            }
        });
        
        btnModifyFilter = new Button(composite, SWT.PUSH);
        btnModifyFilter.setText(RM.getLabel("targetedition.editfilteraction.label"));
        btnModifyFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblFilters.getSelectionIndex() != -1) {
                    TableItem item = tblFilters.getItem(tblFilters.getSelectionIndex());
                    ArchiveFilter filter = (ArchiveFilter)item.getData();
                    showFilterEditionFrame(filter);
                    updateFilter(item, filter);
                }
            }
        });
        
        btnRemoveFilter = new Button(composite, SWT.PUSH);
        btnRemoveFilter.setText(RM.getLabel("targetedition.removefilteraction.label"));
        btnRemoveFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblFilters.getSelectionIndex() != -1) {
                    int result = application.showConfirmDialog(
                            RM.getLabel("targetedition.removefilteraction.confirm.message"),
                            RM.getLabel("targetedition.removefilteraction.confirm.title"));
                    
                    if (result == SWT.YES) {
                        tblFilters.remove(tblFilters.getSelectionIndex());
                        registerUpdate();                  
                    }
                }
            }
        });
        
        tblFilters.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateFilterListState();
            }
        });
    }

    private void initProcessorsTab(Composite composite) {
        composite.setLayout(initLayout(4));
        
        tblProc = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        tblProc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        
        TableColumn col1 = new TableColumn(tblProc, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.proctable.type.label"));
        col1.setWidth(200);
        col1.setMoveable(true);
        
        TableColumn col2 = new TableColumn(tblProc, SWT.NONE);
        col2.setText(RM.getLabel("targetedition.proctable.parameters.label"));
        col2.setWidth(300);
        col2.setMoveable(true);
        
        tblProc.setHeaderVisible(true);
        tblProc.setLinesVisible(AbstractWindow.getTableLinesVisible());
 
        btnAddProc = new Button(composite, SWT.PUSH);
        btnAddProc.setText(RM.getLabel("targetedition.addprocaction.label"));
        btnAddProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                PostProcessor newproc = showProcEditionFrame(null);
                if (newproc != null) {
                    addProcessor(newproc);
                    registerUpdate();                
                }
            }
        });
        
        btnModifyProc = new Button(composite, SWT.PUSH);
        btnModifyProc.setText(RM.getLabel("targetedition.editprocaction.label"));
        btnModifyProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblProc.getSelectionIndex() != -1) {
                    TableItem item = tblProc.getItem(tblProc.getSelectionIndex());
                    PostProcessor proc = (PostProcessor)item.getData();
                    showProcEditionFrame(proc);
                    updateProcessor(item, proc);
                }
            }
        });
        
        btnRemoveProc = new Button(composite, SWT.PUSH);
        btnRemoveProc.setText(RM.getLabel("targetedition.removeprocaction.label"));
        btnRemoveProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblProc.getSelectionIndex() != -1) {
                    int result = application.showConfirmDialog(
                            RM.getLabel("targetedition.removeprocaction.confirm.message"),
                            RM.getLabel("targetedition.confirmremoveproc.title"));
                    
                    if (result == SWT.YES) {
                        tblProc.remove(tblProc.getSelectionIndex());
                        registerUpdate();                  
                    }
                }
            }
        });
        
        tblProc.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateProcListState();
            }
        });
    }

    protected void registerUpdate() {
        super.registerUpdate();
        
        updateFilterListState();
        updateProcListState();
    }
    
    protected void updateFilterListState() {
        int index = this.tblFilters.getSelectionIndex();
        this.btnRemoveFilter.setEnabled(index != -1);
        this.btnModifyFilter.setEnabled(index != -1);     
    }
    
    protected void updateProcListState() {
        int index =  this.tblProc.getSelectionIndex();
        this.btnRemoveProc.setEnabled(index != -1);
        this.btnModifyProc.setEnabled(index != -1);       
    }
    
    private void initValues() {
        // Tracks wether there is a backward compatibility error
        // Indeed, an error can occur if the user tries to open a target which was created with the first versions of
        // Areca and whose backups are stored at the ROOT of the filesystem (althought it was asked to store them
        // in a SUBDIRECTORY).
        // In this case, Areca refuses to edit this target. The user must fix the problem by hand or delete his target and recreate 
        // a valid one.
        boolean backwardCompatibilityError = false;

        // INIT VALUES
        if (target != null) {
            txtTargetName.setText(target.getTargetName());
            txtBaseDir.setText(FileSystemManager.getAbsolutePath(((FileSystemRecoveryTarget)target).getSourcePath()));
            txtDesc.setText(target.getComments());
            
            AbstractIncrementalFileSystemMedium fMedium = (AbstractIncrementalFileSystemMedium)target.getMedium();
            
            chkIncremental.setSelection(! fMedium.isOverwrite());
            chkTrackDirectories.setSelection(fMedium.isTrackDirectories());
            chkTrackPermissions.setSelection(fMedium.isTrackPermissions());
            
            if (IncrementalZipMedium.class.isAssignableFrom(fMedium.getClass())) {
                rdZip.setSelection(true);
            } else if (IncrementalZip64Medium.class.isAssignableFrom(fMedium.getClass())) {
                rdZip64.setSelection(true);
            } else if (IncrementalTGZMedium.class.isAssignableFrom(fMedium.getClass())) {
                //rdTgz.setSelection(true);
            } else {
                rdDir.setSelection(true);
            }
            
            if ( fMedium.getFileSystemPolicy() instanceof DefaultFileSystemPolicy) {
                DefaultFileSystemPolicy policy = (DefaultFileSystemPolicy)fMedium.getFileSystemPolicy();
                this.rdFile.setSelection(true);
                File tmpF = FileSystemManager.getParentFile(new File(policy.getBaseArchivePath()));
                File mainStorageDirectory = FileSystemManager.getParentFile(tmpF);
                if (mainStorageDirectory == null) {
                    processSelection(PLUGIN_HD, FileSystemManager.getAbsolutePath(tmpF));
                    backwardCompatibilityError = true; // ERROR : archives stored at the root.
                } else {
                    processSelection(PLUGIN_HD, FileSystemManager.getAbsolutePath(mainStorageDirectory));               
                }
            } else {
                FileSystemPolicy clone = (FileSystemPolicy)fMedium.getFileSystemPolicy().duplicate();
                String id = clone.getId();
                Button rd = (Button)this.strRadio.get(id);
                rd.setSelection(true);
                processSelection(id, clone.getDisplayableParameters());
                this.currentPolicy = clone;
            }

            chkEncrypted.setSelection(fMedium.getEncryptionPolicy().isEncrypted());
            if (fMedium.getEncryptionPolicy().isEncrypted()) {
                txtEncryptionKey.setText(fMedium.getEncryptionPolicy().getEncryptionKey());
                String algoId = fMedium.getEncryptionPolicy().getEncryptionAlgorithm();
                if (EncryptionConfiguration.getAvailableNonDeprecatedAlgorithms().contains(algoId)) {
                    for (int i=0; i<lstEncryptionAlgorithms.size(); i++) {
                        EncryptionConfiguration conf = (EncryptionConfiguration)lstEncryptionAlgorithms.get(i);
                        if (conf.getId().equals(algoId)) {
                            cboEncryptionAlgorithm.select(i);
                            break;
                        }
                    }
                } else {
                    cboEncryptionAlgorithm.deselectAll();
                }
            }

            // INIT FILTERS
            Iterator filters = target.getFilterIterator();
            int index = this.tblFilters.getSelectionIndex();
            while (filters.hasNext()) {
                ArchiveFilter filter = (ArchiveFilter)filters.next();
                addFilter(filter);
            } 
            if (index != -1) {
                this.tblFilters.setSelection(index);
            }

            // INIT PROCS
            Iterator processors = target.getPostProcessors().iterator();
            index = this.tblProc.getSelectionIndex();
            while (processors.hasNext()) {
                PostProcessor proc = (PostProcessor)processors.next();

                TableItem item = new TableItem(tblProc, SWT.NONE);
                item.setText(0, ProcessorRepository.getName(proc.getClass()));
                item.setText(1, proc.getParametersSummary());
                item.setData(proc);
            } 
            if (index != -1) {
                this.tblProc.setSelection(index);
            }     
        } else {
            // Default settings
            rdZip.setSelection(true);
            rdFile.setSelection(true);
            chkIncremental.setSelection(true);
            processSelection(PLUGIN_HD, ArecaPreferences.getDefaultArchiveStorage());
            
            // Default filters
            FileExtensionArchiveFilter filter = new FileExtensionArchiveFilter();
            filter.acceptParameters("*.tmp, *.temp");
            filter.setExclude(true);
            addFilter(filter);
        }
        
        this.resetEcryptionKey();
        
        // FREEZE
        if (isFrozen(true)) {
            Iterator iter = this.strRadio.keySet().iterator();
            while (iter.hasNext()) {
                String id = (String)iter.next();
                Button rd = (Button)strRadio.get(id);
                Button btn = (Button)strButton.get(id);
                Text txt = (Text)strText.get(id);

                btn.setEnabled(false);
                txt.setEnabled(false);
                rd.setEnabled(false);
            }

            grpCompression.setEnabled(false);
            grpEncryption.setEnabled(false);
            grpFileManagement.setEnabled(false);
            
            rdDir.setEnabled(false);
            rdZip.setEnabled(false);
            rdZip64.setEnabled(false);
            //rdTgz.setEnabled(false);
            chkEncrypted.setEnabled(false);
            chkIncremental.setEnabled(false);
            cboEncryptionAlgorithm.setEnabled(false);
            lblEncryptionAlgorithm.setEnabled(false);
            lblEncryptionKey.setEnabled(false);
            txtEncryptionKey.setEnabled(false);
            chkTrackDirectories.setEnabled(false);
            chkTrackPermissions.setEnabled(false);
        }    

        if (backwardCompatibilityError) {
            throw new IllegalArgumentException(RM.getLabel("targetedition.backwardcompatibility"));
        }
    }
    
    private ArchiveFilter showFilterEditionFrame(ArchiveFilter filter) {
        FilterEditionWindow frm = new FilterEditionWindow(filter, (FileSystemRecoveryTarget)this.getTarget());
        showDialog(frm);
        ArchiveFilter ft = frm.getCurrentFilter();
        return ft;
    }
    
    private PostProcessor showProcEditionFrame(PostProcessor proc) {
        ProcessorEditionWindow frm = new ProcessorEditionWindow(proc, (FileSystemRecoveryTarget)this.getTarget());
        showDialog(frm);
        PostProcessor prc = frm.getCurrentProcessor();
        return prc;
    }
    
    private void addFilter(ArchiveFilter filter) {
        TableItem item = new TableItem(tblFilters, SWT.NONE);
        updateFilter(item, filter);
    }
    
    private void updateFilter(TableItem item, ArchiveFilter filter) {
        String filterExclude = RM.getLabel(
                ((AbstractArchiveFilter)filter).isExclude() ? "filteredition.exclusion.label" : "filteredition.inclusion.label"
        );
        item.setText(0, FilterRepository.getName(filter.getClass()));
        item.setText(1, filter.getStringParameters() == null ? "" : filter.getStringParameters());
        item.setText(2, filterExclude);
        item.setData(filter);
    }
    
    private void addProcessor(PostProcessor proc) {
        TableItem item = new TableItem(tblProc, SWT.NONE);
        updateProcessor(item, proc);
    }
    
    private void updateProcessor(TableItem item, PostProcessor proc) {
        item.setText(0, ProcessorRepository.getName(proc.getClass()));
        item.setText(1, proc.getParametersSummary());
        item.setData(proc);
    }
    
    private void processSelection(String refId, String s) {
        this.currentPolicy = null;
        this.currentFileSystemPolicyId = refId;
        
        Iterator iter = this.strRadio.keySet().iterator();
        while (iter.hasNext()) {
            String id = (String)iter.next();
            Button rd = (Button)strRadio.get(id);
            Button btn = (Button)strButton.get(id);
            Text txt = (Text)strText.get(id);
            
            if (refId.equals(id)) {
                btn.setEnabled(true);
                txt.setEnabled(true);
                if (s != null) {
                    txt.setText(s);
                }
            } else {
                btn.setEnabled(false);
                txt.setEnabled(false);
                txt.setText("");
            }
        }
    }

    protected boolean checkBusinessRules() {
        // - NOM CIBLE
        this.resetErrorState(txtTargetName);        
        if (this.txtTargetName.getText() == null || this.txtTargetName.getText().length() == 0) {
            this.setInError(txtTargetName);
            return false;
        }  
        
        // - REPERTOIRE SOURCE + valider qu'il existe
        this.resetErrorState(txtBaseDir);
        if (this.txtBaseDir.getText() == null || this.txtBaseDir.getText().length() == 0) {
            this.setInError(txtBaseDir);
            return false;
        } else {
            if (! FileSystemManager.exists(new File(txtBaseDir.getText()))) {
                this.setInError(txtBaseDir);
                return false;
            }
        }
        
        // - EMPLACEMENT medium + valider qu'il n'est pas un sous répertoire du répertoire source
        Text txt = (Text)this.strText.get(this.currentFileSystemPolicyId);
        Button rd = (Button)this.strRadio.get(this.currentFileSystemPolicyId);
        this.resetErrorState(txt);
        if (rd.getSelection()) {
            if (PLUGIN_HD.equals(this.currentFileSystemPolicyId)) {
                if (this.txtMediumPath.getText() == null || this.txtMediumPath.getText().length() == 0) {
                    this.setInError(txtMediumPath);
                    return false;
                } else {
                    File baseDir = new File(this.txtBaseDir.getText());
                    File backupDir = new File(this.txtMediumPath.getText());
                    FileTool tool = new FileTool();
                    if (tool.isParentOf(baseDir, backupDir)) {
                        this.setInError(txtMediumPath);
                        return false;           
                    }
                }  
            } else if (currentPolicy == null) {
                this.setInError(txt);
                return false;
            }
        }
        
        // CRYPTAGE
        this.resetErrorState(cboEncryptionAlgorithm);
        if (
                this.chkEncrypted.getSelection()
                && (! this.isFrozen(false))
                && (this.cboEncryptionAlgorithm.getSelectionIndex() == -1)
        ) {
            this.setInError(cboEncryptionAlgorithm);
            return false;
        }    
        
        this.resetErrorState(txtEncryptionKey);
        if (this.chkEncrypted.getSelection() && (
                this.txtEncryptionKey.getText() == null 
                || this.txtEncryptionKey.getText().length() == 0           
        )) {
            this.setInError(txtEncryptionKey);
            return false;
        }    
        
        return true;        
    }
    
    private void resetEcryptionKey() {
        if (this.chkEncrypted.getSelection()) {
            this.txtEncryptionKey.setEditable(true);
            this.cboEncryptionAlgorithm.setEnabled(true);
            
            this.lblEncryptionAlgorithm.setEnabled(true);
            this.lblEncryptionKey.setEnabled(true);
        } else {
            this.txtEncryptionKey.setEditable(false);
            this.txtEncryptionKey.setBackground(null);
            if (txtEncryptionKey.getText() != null && txtEncryptionKey.getText().length() != 0) {
                this.txtEncryptionKey.setText("");
            }
            
            this.cboEncryptionAlgorithm.setEnabled(false);
            this.cboEncryptionAlgorithm.setBackground(null);
            
            this.lblEncryptionAlgorithm.setEnabled(false);
            this.lblEncryptionKey.setEnabled(false);
        }
    }
    
    /**
     * Indique si certaines zones sont désactivées ou non
     * @return
     */
    protected boolean isFrozen(boolean showWarning) {
        if (target == null) {
            return false;
        } else {
            try {
                return (((AbstractFileSystemMedium)target.getMedium()).listArchives(null, null).length != 0);
            } catch (Throwable e) {
                if (showWarning) {
                    this.application.handleException(RM.getLabel("targetedition.frozen.message"), e);
                } else {
                    Logger.defaultLogger().error(e);
                }
                return false;
            }
        }
    }
    
    public AbstractRecoveryTarget getTarget() {
        return target;
    }
    
    public AbstractRecoveryTarget getTargetIfValidated() {
        if (this.hasBeenSaved) {
            return target;
        } else {
            return null;
        }
    }

    public String getTitle() {
        return TITLE;
    }

    protected void saveChanges() {
        try {
            FileSystemRecoveryTarget newTarget = new FileSystemRecoveryTarget();
            newTarget.setProcess(application.getCurrentProcess());
            newTarget.setSourcePath(new File(this.txtBaseDir.getText()));

            String archivePrefix; // Necessary for backward compatibility
            String storageSubDirectory; // Necessary for backward compatibility
            if (target != null) {
                newTarget.setId(target.getId());
                newTarget.setUid(target.getUid());

                // Necessary for backward compatibility
                File tmpF = new File(((AbstractFileSystemMedium)target.getMedium()).getBaseArchivePath());
                File fStorageSubDirectoryFile = FileSystemManager.getParentFile(tmpF);
                archivePrefix = FileSystemManager.getName(tmpF);
                storageSubDirectory = FileSystemManager.getName(fStorageSubDirectoryFile);
            } else {
                newTarget.setId(application.getCurrentProcess().getNextFreeTargetId());

                // Should be the standard behaviour, but a workaround is necessary for backward compatibility
                archivePrefix = DefaultFileSystemPolicy.DEFAULT_ARCHIVE_NAME;
                storageSubDirectory = DefaultFileSystemPolicy.STORAGE_DIRECTORY_PREFIX + newTarget.getUid();
            }
            
            newTarget.setComments(this.txtDesc.getText());
            newTarget.setTargetName(txtTargetName.getText());
            
            if (isFrozen(false)) {
                newTarget.setMedium(target.getMedium(), false);
            } else {
                boolean isEncrypted = this.chkEncrypted.getSelection();
                EncryptionPolicy encrArgs = new EncryptionPolicy();
                encrArgs.setEncrypted(isEncrypted);
                if (isEncrypted) {
                    String encryptionKey = this.txtEncryptionKey.getText();
                    EncryptionConfiguration config = (EncryptionConfiguration)lstEncryptionAlgorithms.get(cboEncryptionAlgorithm.getSelectionIndex());
                    encrArgs.setEncryptionAlgorithm(config.getId());
                    encrArgs.setEncryptionKey(encryptionKey);
                }

                AbstractFileSystemMedium medium = null;
                FileSystemPolicy storagePolicy;
                if (this.currentPolicy != null) {
                    storagePolicy = this.currentPolicy;
                } else {
                    storagePolicy = new DefaultFileSystemPolicy();
                    ((DefaultFileSystemPolicy)storagePolicy).setId(PLUGIN_HD);
                    String basePath = this.txtMediumPath.getText() + "/" + storageSubDirectory + "/" + archivePrefix;
                    ((DefaultFileSystemPolicy)storagePolicy).setBaseArchivePath(basePath);
                }
                storagePolicy.validate(false);
                
                // Permet de sauvegarder l'historique pour le réécrire suite au changement de driver.
                History historyBck = null;
                if (target != null) {
                    // Suppression de l'historique de la target; celui ci sera réécrit après la réinitialisation du driver
                    historyBck = this.target.getHistory();
                    if (historyBck != null) {
                        try {
                            historyBck.clearData();
                        } catch (Exception e) {
                            Logger.defaultLogger().error("Error trying to clear the target's history", e);
                            // Non - blocking error.
                        }
                    }
                }
                
                if (this.rdDir.getSelection()) {
                    medium = new IncrementalDirectoryMedium();                    
                } else if (this.rdZip.getSelection()) {
                    medium = new IncrementalZipMedium();                        
                } else if (this.rdZip64.getSelection()) {
                    medium = new IncrementalZip64Medium();                        
                //} else if (this.rdTgz.getSelection()) {
                //    medium = new IncrementalTGZMedium();
                }
                ((AbstractIncrementalFileSystemMedium)medium).setFileSystemPolicy(storagePolicy);
                ((AbstractIncrementalFileSystemMedium)medium).setEncryptionPolicy(encrArgs);
                ((AbstractIncrementalFileSystemMedium)medium).setOverwrite(! this.chkIncremental.getSelection());
                ((AbstractIncrementalFileSystemMedium)medium).setTrackDirectories(this.chkTrackDirectories.getSelection());
                ((AbstractIncrementalFileSystemMedium)medium).setTrackPermissions(this.chkTrackPermissions.getSelection());
               
                newTarget.setMedium(medium, false);
                medium.install();
                
                if (historyBck != null) {
                    // Réécriture de l'historique
                    newTarget.getHistory().importHistory(historyBck);
                }
            }
            
            for (int i=0; i<this.tblFilters.getItemCount(); i++) {
                newTarget.addFilter((ArchiveFilter)this.tblFilters.getItem(i).getData());
            }
            
            for (int i=0; i<this.tblProc.getItemCount(); i++) {
                newTarget.getPostProcessors().addPostProcessor((PostProcessor)this.tblProc.getItem(i).getData());
            }
            
            this.target = newTarget;
        } catch (Exception e) {
            this.application.handleException(RM.getLabel("error.updateprocess.message", new Object[] {e.getMessage()}), e);
        }
        
        this.hasBeenSaved = true;
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
        btnSave.setEnabled(rulesSatisfied);
    }
    
    private FTPFileSystemPolicy showFTPEditionFrame(FTPFileSystemPolicy policy) {
        FTPEditionWindow frm = new FTPEditionWindow(policy);
        showDialog(frm);
        
        FTPFileSystemPolicy ft = frm.getCurrentPolicy();
        return ft;
    }
    
    private void showDialog(AbstractWindow window) {
        window.setModal(this);
        window.setBlockOnOpen(true);
        window.open();
    }
}
