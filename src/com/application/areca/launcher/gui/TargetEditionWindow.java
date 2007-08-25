package com.application.areca.launcher.gui;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ResourceManager;
import com.application.areca.Utils;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.impl.AbstractFileSystemMedium;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.EncryptionConfiguration;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.policy.DefaultFileSystemPolicy;
import com.application.areca.impl.policy.EncryptionPolicy;
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
import com.myJava.file.archive.zip64.ZipConstants;
import com.myJava.util.history.History;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -3366468978279844961
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
    protected Group grpZipOptions;
    protected Button rdDir;
    protected Button rdZip;
    protected Button rdZip64;
    protected Button chkTrackDirectories;
    protected Button chkTrackPermissions;
    protected Button chkEncrypted;
    protected Button chkMultiVolumes;
    protected Button chkFollowLinks;
    protected Button chkIncremental;
    protected Text txtEncryptionKey;
    protected Text txtMultiVolumes;
    protected Combo cboEncryptionAlgorithm;
    protected Label lblEncryptionKey;
    protected Label lblMultiVolumesUnit;
    protected Label lblEncryptionAlgorithm;
    protected Label lblZipComment;
    protected Text txtZipComment;
    protected Label lblEncoding;
    protected Combo cboEncoding;
    
    protected Tree treFilters;
    protected Button btnAddFilter;
    protected Button btnRemoveFilter;
    protected Button btnModifyFilter;
    protected FilterGroup mdlFilters;
 
    protected Table tblProc;  
    protected Button btnAddProc;
    protected Button btnRemoveProc;
    protected Button btnModifyProc;
    
    protected Table tblSources;  
    protected Button btnAddSource;
    protected Button btnRemoveSource;
    protected Button btnModifySource;
    
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
            initSourcesTab(initTab(tabs, RM.getLabel("targetedition.sourcesgroup.title")));
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
        Application.setTabLabel(itm, title, false);
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
        
        // DESC
        Group grpDesc = new Group(composite, SWT.NONE);
        grpDesc.setText(RM.getLabel("targetedition.descriptionfield.label"));
        grpDesc.setLayout(new GridLayout(1, false));
        grpDesc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        txtDesc = new Text(grpDesc, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        monitorControl(txtDesc);
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        dt.widthHint = AbstractWindow.computeWidth(500);
        dt.heightHint = AbstractWindow.computeHeight(50);
        txtDesc.setLayoutData(dt);
        
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
    
    private void initSourcesTab(Composite composite) {
        composite.setLayout(initLayout(4));
        
        TableViewer viewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE);
        tblSources = viewer.getTable();
        tblSources.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        
        TableColumn col1 = new TableColumn(tblSources, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.sourcedirfield.label"));
        col1.setWidth(400);
        col1.setMoveable(true);
        
        tblSources.setHeaderVisible(true);
        tblSources.setLinesVisible(AbstractWindow.getTableLinesVisible());
 
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                editCurrentSource();
            }
        });
        
        btnAddSource = new Button(composite, SWT.PUSH);
        btnAddSource.setText(RM.getLabel("targetedition.addprocaction.label"));
        btnAddSource.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                File newFile = showSourceEditionFrame(null);
                if (newFile != null) {
                    addSource(newFile);
                    registerUpdate();                
                }
            }
        });
        
        btnModifySource = new Button(composite, SWT.PUSH);
        btnModifySource.setText(RM.getLabel("targetedition.editprocaction.label"));
        btnModifySource.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                editCurrentSource();
            }
        });
        
        btnRemoveSource = new Button(composite, SWT.PUSH);
        btnRemoveSource.setText(RM.getLabel("targetedition.removeprocaction.label"));
        btnRemoveSource.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (tblSources.getSelectionIndex() != -1) {
                    int result = application.showConfirmDialog(
                            RM.getLabel("targetedition.removesourceaction.confirm.message"),
                            RM.getLabel("targetedition.confirmremovesource.title"));
                    
                    if (result == SWT.YES) {
                        tblSources.remove(tblSources.getSelectionIndex());
                        registerUpdate();                  
                    }
                }
            }
        });
        
        tblSources.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateSourceListState();
            }
        });
    }
    
    private void editCurrentSource() {
        if (tblSources.getSelectionIndex() != -1) {
            TableItem item = tblSources.getItem(tblSources.getSelectionIndex());
            File source = (File)item.getData();
            updateSource(item, showSourceEditionFrame(source));
            registerUpdate();  
        }
    }

    private void enableZipOptions(boolean enable) {
        this.chkMultiVolumes.setEnabled(enable);
        if (! enable) {
            this.chkMultiVolumes.setSelection(false);
        }
        this.lblZipComment.setEnabled(enable);
        this.txtZipComment.setEnabled(enable);
        this.lblEncoding.setEnabled(enable);
        this.cboEncoding.setEnabled(enable);
        this.resetMVData();
    }
    
    private void initAdvancedTab(Composite composite) {
        composite.setLayout(initLayout(2));
        
        // COMPRESSION
        grpCompression = new Group(composite, SWT.NONE);
        grpCompression.setText(RM.getLabel("targetedition.compression.label"));
        RowLayout lytCompression = new RowLayout(SWT.VERTICAL);
        grpCompression.setLayout(lytCompression);
        grpCompression.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        rdDir = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdDir);
        rdDir.setText(RM.getLabel("targetedition.compression.none"));
        rdDir.setToolTipText(RM.getLabel("targetedition.compression.none.tt"));
        rdDir.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                enableZipOptions(false);
            }
        });
        
        rdZip = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdZip);
        rdZip.setText(RM.getLabel("targetedition.compression.zip"));
        rdZip.setToolTipText(RM.getLabel("targetedition.compression.zip.tt"));
        rdZip.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                enableZipOptions(true);
            }
        });
        
        rdZip64 = new Button(grpCompression, SWT.RADIO);
        monitorControl(rdZip64);
        rdZip64.setText(RM.getLabel("targetedition.compression.zip64"));
        rdZip64.setToolTipText(RM.getLabel("targetedition.compression.zip64.tt"));
        rdZip64.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                enableZipOptions(true);
            }
        });
        
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
        
        chkFollowLinks = new Button(grpFileManagement, SWT.CHECK);
        monitorControl(chkFollowLinks);
        chkFollowLinks.setText(RM.getLabel("targetedition.followlinks.label"));
        chkFollowLinks.setToolTipText(RM.getLabel("targetedition.followlinks.tooltip"));
        if (OSTool.isSystemWindows()) {
            chkFollowLinks.setVisible(false);
        }
        
        // ZIP OPTIONS
        grpZipOptions = new Group(composite, SWT.NONE);
        grpZipOptions.setText(RM.getLabel("targetedition.zipoptions.label"));
        grpZipOptions.setLayout(new GridLayout(3, false));
        grpZipOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        lblZipComment = new Label(grpZipOptions, SWT.NONE);
        lblZipComment.setText(RM.getLabel("targetedition.zipcomment.label"));
        lblZipComment.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        txtZipComment =  new Text(grpZipOptions, SWT.BORDER);
        monitorControl(txtZipComment);
        GridData dtComment = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        txtZipComment.setLayoutData(dtComment);
        
        lblEncoding = new Label(grpZipOptions, SWT.NONE);
        lblEncoding.setText(RM.getLabel("targetedition.encoding.label") + "            ");
        lblEncoding.setToolTipText(RM.getLabel("targetedition.encoding.tt"));
        cboEncoding = new Combo(grpZipOptions, SWT.READ_ONLY);
        cboEncoding.setToolTipText(RM.getLabel("targetedition.encoding.tt"));
        for (int i=0; i<OSTool.getCharsets().length; i++) {
            cboEncoding.add(OSTool.getCharsets()[i].name());
        }
        monitorControl(cboEncoding);
        cboEncoding.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        new Label(grpZipOptions, SWT.NONE);
        
        chkMultiVolumes = new Button(grpZipOptions, SWT.CHECK);
        chkMultiVolumes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        chkMultiVolumes.setText(RM.getLabel("targetedition.mv.label"));
        chkMultiVolumes.setToolTipText(RM.getLabel("targetedition.mv.tooltip"));
        monitorControl(chkMultiVolumes);
        chkMultiVolumes.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                resetMVData();
            }
        });
        
        txtMultiVolumes = new Text(grpZipOptions, SWT.BORDER);
        txtMultiVolumes.setToolTipText(RM.getLabel("targetedition.mv.size.tt"));
        monitorControl(txtMultiVolumes);
        GridData dtMV = new GridData(SWT.FILL, SWT.CENTER, false, false);
        txtMultiVolumes.setLayoutData(dtMV);
        lblMultiVolumesUnit = new Label(grpZipOptions, SWT.NONE);
        lblMultiVolumesUnit.setText(RM.getLabel("targetedition.mv.unit.label"));
        lblMultiVolumesUnit.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        
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
    
    private TreeItem transfered;
    
    private void initFiltersTab(Composite composite) {
        composite.setLayout(initLayout(4));
        
        TreeViewer viewer = new TreeViewer(composite, SWT.BORDER | SWT.SINGLE);
        treFilters = viewer.getTree();
        treFilters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        
        TreeColumn col1 = new TreeColumn(treFilters, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.filterstable.type.label"));
        col1.setWidth(200);
        col1.setMoveable(true);
        
        TreeColumn col2 = new TreeColumn(treFilters, SWT.NONE);
        col2.setText(RM.getLabel("targetedition.filterstable.parameters.label"));
        col2.setWidth(200);
        col2.setMoveable(true);
        
        TreeColumn col3 = new TreeColumn(treFilters, SWT.NONE);
        col3.setText(RM.getLabel("targetedition.filterstable.mode.label"));
        col3.setWidth(100);
        col3.setMoveable(true);
        
        treFilters.setHeaderVisible(true);
        treFilters.setLinesVisible(AbstractWindow.getTableLinesVisible());

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                editCurrentFilter();
            }
        });
        
        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        final int operation = DND.DROP_MOVE;

        final DragSource source = new DragSource(treFilters, operation);
        source.setTransfer(types);
        source.addDragListener(
            new DragSourceAdapter() {
                public void dragStart(DragSourceEvent event) {   
                    TreeItem[] selection = treFilters.getSelection();
                    if (selection.length > 0 && selection[0].getParentItem() != null) {
                        event.doit = true;
                        transfered = selection[0];
                    } else {
                        event.doit = false;
                    }
                };
                public void dragSetData(DragSourceEvent event) {
                    event.data = "dummy data";
                }
            }
        );

        DropTarget target = new DropTarget(treFilters, operation);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
                event.detail = DND.DROP_NONE;
                event.feedback = DND.FEEDBACK_NONE;
            }

            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;

                TreeItem selected = getSelected(event);
                if (selected != null && (selected.getData() instanceof FilterGroup) && (! contains(transfered, selected))) {
                    event.feedback |= DND.FEEDBACK_SELECT;
                    event.detail = operation;
                } else {
                    event.feedback |= DND.FEEDBACK_NONE;
                    event.detail = DND.DROP_NONE;
                }
            }

            public void drop(DropTargetEvent event) {
                TreeItem targetItem = getSelected(event);
                if (targetItem != null) {
                    FilterGroup target = (FilterGroup)targetItem.getData();
                    ArchiveFilter filter = (ArchiveFilter)transfered.getData();
                    FilterGroup parent = (FilterGroup)transfered.getParentItem().getData();
                    
                    parent.remove(filter);
                    target.addFilter(filter);

                    updateFilterData(null);
                    expandAll(treFilters.getItem(0));
                }
            }
        });
        
        btnAddFilter = new Button(composite, SWT.PUSH);
        btnAddFilter.setText(RM.getLabel("targetedition.addfilteraction.label"));
        btnAddFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (treFilters.getSelectionCount() != 0) {
                    TreeItem parentItem = treFilters.getSelection()[0];
                    ArchiveFilter parent = (ArchiveFilter)parentItem.getData();
                    
                    if (parent instanceof FilterGroup) {
                        ArchiveFilter newFilter = showFilterEditionFrame(null);
                        
                        if (newFilter != null) {
                            ((FilterGroup)parent).addFilter(newFilter);
                            
                            addFilter(parentItem, newFilter);
                            expandAll(treFilters.getItem(0));
                            registerUpdate();  
                        }
                    }
                }
            }
        });
        
        btnModifyFilter = new Button(composite, SWT.PUSH);
        btnModifyFilter.setText(RM.getLabel("targetedition.editfilteraction.label"));
        btnModifyFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                editCurrentFilter();
            }
        });
        
        btnRemoveFilter = new Button(composite, SWT.PUSH);
        btnRemoveFilter.setText(RM.getLabel("targetedition.removefilteraction.label"));
        btnRemoveFilter.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                if (treFilters.getSelectionCount() != 0) {
                    TreeItem item = treFilters.getSelection()[0];
                    TreeItem parentItem = item.getParentItem();
                    
                    if (parentItem != null) {
                        int result = application.showConfirmDialog(
                                RM.getLabel("targetedition.removefilteraction.confirm.message"),
                                RM.getLabel("targetedition.removefilteraction.confirm.title"));

                        if (result == SWT.YES) {
                            FilterGroup fg = (FilterGroup)parentItem.getData();
                            ArchiveFilter filter = (ArchiveFilter)item.getData();
                            fg.remove(filter);

                            updateFilterData(parentItem);
                            expandAll(treFilters.getItem(0));
                            registerUpdate();   
                        }                  
                    }
                }
            }
        });
        
        treFilters.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateFilterListState();
            }
        });
    }
    
    private void editCurrentFilter() {
        if (treFilters.getSelectionCount() != 0) {
            TreeItem item = treFilters.getSelection()[0];
            ArchiveFilter filter = (ArchiveFilter)item.getData();
            showFilterEditionFrame(filter);

            updateFilterData(item.getParentItem());
            expandAll(treFilters.getItem(0));
            registerUpdate();  
        }
    }
    
    private TreeItem getSelected(DropTargetEvent event) {
        try {
            TreeItem selected = (TreeItem)event.item;
            if (! OSTool.isSystemWindows()) {
                // There is a bug in SWT under Linux :
                // If the Tree has been configured to display column headers ("setHeaderVisible(true)"), 
                // the "item" attribute of the DropTargetEvent references the TreeItem UNDER the actually selected item !
                // That's why we must "play" with the item's coordinates :(
                int x = selected.getBounds().x + 2;
                int y = selected.getBounds().y - selected.getBounds().height + 2;
                
                selected = treFilters.getItem(new Point(x, y));
            }
            return selected;
        } catch (RuntimeException e) {
            return null;
        }
    }
    
    private static boolean contains(TreeItem parent, TreeItem child) {
        TreeItem item = child;
        while (item != null) {
            if (item.getData().equals(parent.getData())) {
                return true;
            }
            item = item.getParentItem();
        }
        return false;
    }

    private void initProcessorsTab(Composite composite) {
        composite.setLayout(initLayout(4));
        
        TableViewer viewer = new TableViewer(composite, SWT.BORDER | SWT.SINGLE);
        tblProc = viewer.getTable();
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
 
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                editCurrentProcessor();
            }
        });
        
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
                editCurrentProcessor();
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
    
    private void editCurrentProcessor() {
        if (tblProc.getSelectionIndex() != -1) {
            TableItem item = tblProc.getItem(tblProc.getSelectionIndex());
            PostProcessor proc = (PostProcessor)item.getData();
            showProcEditionFrame(proc);
            updateProcessor(item, proc);
            registerUpdate();    
        }
    }

    protected void registerUpdate() {
        super.registerUpdate();
        
        updateFilterListState();
        updateProcListState();
    }
    
    protected void updateFilterListState() {
        boolean selected = (this.treFilters.getSelectionCount() > 0);
        
        this.btnRemoveFilter.setEnabled(selected && this.treFilters.getSelection()[0].getParentItem() != null);
        this.btnModifyFilter.setEnabled(selected);        
        this.btnAddFilter.setEnabled(selected && this.treFilters.getSelection()[0].getData() instanceof FilterGroup);
    }
    
    protected void updateSourceListState() {
        int index =  this.tblSources.getSelectionIndex();
        this.btnRemoveSource.setEnabled(index != -1);
        this.btnModifySource.setEnabled(index != -1);       
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
            txtDesc.setText(target.getComments());
            
            AbstractIncrementalFileSystemMedium fMedium = (AbstractIncrementalFileSystemMedium)target.getMedium();
            
            chkIncremental.setSelection(! fMedium.isOverwrite());
            chkTrackDirectories.setSelection(fMedium.isTrackDirectories());
            chkTrackPermissions.setSelection(fMedium.isTrackPermissions());
            chkFollowLinks.setSelection( ! ((FileSystemRecoveryTarget)target).isTrackSymlinks());
            
            if (IncrementalZipMedium.class.isAssignableFrom(fMedium.getClass())) {
                if (((IncrementalZipMedium)fMedium).isUseZip64()) {
                    rdZip64.setSelection(true);
                } else {
                    rdZip.setSelection(true);
                }
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

            if (fMedium instanceof IncrementalZipMedium) {
                IncrementalZipMedium zMedium = (IncrementalZipMedium)fMedium;
                if (zMedium.isMultiVolumes()) {
                    chkMultiVolumes.setSelection(true);
                    txtMultiVolumes.setText("" + zMedium.getVolumeSize());
                }
                
                if (zMedium.getComment() != null) {
                    txtZipComment.setText(zMedium.getComment());
                }
                
                selectEncoding(zMedium.getCharset() != null ? zMedium.getCharset().name() : ZipConstants.DEFAULT_CHARSET);
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
            
            // INIT SOURCES
            Iterator sources = ((FileSystemRecoveryTarget)target).getSources().iterator();
            while (sources.hasNext()) {
                addSource((File)sources.next());
            }

            // INIT FILTERS
            this.mdlFilters = (FilterGroup)target.getFilterGroup().duplicate();
            addFilter(null, this.mdlFilters);

            // INIT PROCS
            Iterator processors = target.getPostProcessors().iterator();
            int index = this.tblProc.getSelectionIndex();
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
            chkTrackDirectories.setSelection(true);
            chkTrackPermissions.setSelection(true);
            selectEncoding(ZipConstants.DEFAULT_CHARSET);
            if (OSTool.isSystemWindows()) {
                this.chkFollowLinks.setSelection(true);
            }
            processSelection(PLUGIN_HD, ArecaPreferences.getDefaultArchiveStorage());
            
            // Default filters
            this.mdlFilters = new FilterGroup();
            mdlFilters.setAnd(true);
            mdlFilters.setExclude(false);
            
            FileExtensionArchiveFilter filter1 = new FileExtensionArchiveFilter();
            filter1.acceptParameters("*.tmp, *.temp");
            filter1.setExclude(true);
            mdlFilters.addFilter(filter1);
            
            LockedFileFilter filter2 = new LockedFileFilter();
            filter2.setExclude(true);
            mdlFilters.addFilter(filter2);
            
            addFilter(null, mdlFilters);
        }
        
        expandAll(treFilters.getItem(0));
        this.resetEcryptionKey();
        this.resetMVData();
        enableZipOptions(! (rdDir.getSelection()));
        
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
            grpZipOptions.setEnabled(false);
            grpFileManagement.setEnabled(false);
            
            rdDir.setEnabled(false);
            rdZip.setEnabled(false);
            rdZip64.setEnabled(false);
            chkEncrypted.setEnabled(false);
            chkMultiVolumes.setEnabled(false);
            chkIncremental.setEnabled(false);
            cboEncryptionAlgorithm.setEnabled(false);
            lblEncryptionAlgorithm.setEnabled(false);
            lblEncryptionKey.setEnabled(false);
            txtEncryptionKey.setEnabled(false);
            txtMultiVolumes.setEnabled(false);
            lblMultiVolumesUnit.setEnabled(false);
            chkTrackDirectories.setEnabled(false);
            chkTrackPermissions.setEnabled(false);
            chkFollowLinks.setEnabled(false);
            lblZipComment.setEnabled(false);
            txtZipComment.setEnabled(false);
            lblEncoding.setEnabled(false);
            cboEncoding.setEnabled(false);
        }    

        if (backwardCompatibilityError) {
            throw new IllegalArgumentException(RM.getLabel("targetedition.backwardcompatibility"));
        }
    }
    
    private void selectEncoding(String encoding) {
        if (encoding != null) {
            for (int i=0; i<cboEncoding.getItemCount(); i++) {
                if (cboEncoding.getItem(i).equals(encoding)) {
                    cboEncoding.select(i);
                    break;
                }
            }
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
    
    private File showSourceEditionFrame(File source) {
        SourceEditionWindow frm = new SourceEditionWindow(source, (FileSystemRecoveryTarget)this.getTarget());
        showDialog(frm);
        return frm.getSource();
    }
    
    private void addFilter(TreeItem parent, ArchiveFilter filter) {
        TreeItem item;
        if (parent == null) {
            item = new TreeItem(treFilters, SWT.NONE);
        } else {
            item = new TreeItem(parent, SWT.NONE);
        }
        item.setData(filter);

        updateFilterData(item);
    }
    
    private void updateFilterData(TreeItem item) {
        if (item == null) {
            item = treFilters.getItem(0);
        }
        
        boolean isFirst = (
                item.getParentItem() == null 
                || item.getParentItem().getItemCount() == 0 
                || item.getParentItem().getItem(0).getData().equals(item.getData())
        );
                
        item.removeAll();
        
        ArchiveFilter filter = (ArchiveFilter)item.getData();
        TreeItem parent = item.getParentItem();
        
        String prefix = "";
        if (! isFirst) {
                prefix = (RM.getLabel(((FilterGroup)parent.getData()).isAnd() ? "common.operator.and" : "common.operator.or") + " ");
        }
        
        String filterExclude = RM.getLabel(
                filter.isExclude() ? "filteredition.exclusion.label" : "filteredition.inclusion.label"
        );
        item.setText(0, prefix + FilterRepository.getName(filter.getClass()));
        item.setText(1, filter.getStringParameters() == null ? "" : filter.getStringParameters());
        item.setText(2, filterExclude);
        
        if (filter instanceof FilterGroup) {
            Iterator iter = ((FilterGroup)filter).getFilterIterator();
            while (iter.hasNext()) {
                ArchiveFilter child = (ArchiveFilter)iter.next();
                addFilter(item, child);
            }
        }
    }
    
    private static void expandAll(TreeItem item) {
        item.setExpanded(true);
        TreeItem[] children = item.getItems();
        for (int i=0; i<children.length; i++) {
            expandAll(children[i]);
        }
    }

    private void addSource(File source) {
        TableItem item = new TableItem(tblSources, SWT.NONE);
        updateSource(item, source);
    }
    
    private void updateSource(TableItem item, File source) {
        item.setText(0, FileSystemManager.getAbsolutePath(source));
        item.setData(source);
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
        
        // - EMPLACEMENT medium + valider qu'il n'est pas un sous répertoire des répertoires sources
        Text txt = (Text)this.strText.get(this.currentFileSystemPolicyId);
        Button rd = (Button)this.strRadio.get(this.currentFileSystemPolicyId);
        this.resetErrorState(txt);
        if (rd.getSelection()) {
            if (PLUGIN_HD.equals(this.currentFileSystemPolicyId)) {
                if (this.txtMediumPath.getText() == null || this.txtMediumPath.getText().length() == 0) {
                    this.setInError(txtMediumPath);
                    return false;
                } else {
                    for (int i=0; i<this.tblSources.getItemCount(); i++) {
                        File src  =(File)this.tblSources.getItem(i).getData();
                        
                        File backupDir = new File(this.txtMediumPath.getText());
                        FileTool tool = FileTool.getInstance();
                        if (tool.isParentOf(src, backupDir)) {
                            this.setInError(txtMediumPath);
                            return false;           
                        }
                    }
                }  
            } else if (currentPolicy == null) {
                this.setInError(txt);
                return false;
            }
        }
        
        // MULTI-VOLUMES
        this.resetErrorState(txtMultiVolumes);
        if (this.chkMultiVolumes.getSelection()) {
            if (
                this.txtMultiVolumes.getText() == null 
                || this.txtMultiVolumes.getText().length() == 0    
            ) {
                this.setInError(txtMultiVolumes);
                return false;
            } else {
                try {
                    Long.parseLong(this.txtMultiVolumes.getText());
                } catch (NumberFormatException e) {
                    this.setInError(txtMultiVolumes);
                    return false;
                }
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
    
    private void resetMVData() {
        if (this.chkMultiVolumes.getSelection()) {
            this.txtMultiVolumes.setEditable(true);
            this.txtMultiVolumes.setEnabled(true);
            this.lblMultiVolumesUnit.setEnabled(true);
        } else {
            this.txtMultiVolumes.setEditable(false);
            this.txtMultiVolumes.setBackground(null);
            this.txtMultiVolumes.setEnabled(false);
            if (txtMultiVolumes.getText() != null && txtMultiVolumes.getText().length() != 0) {
                this.txtMultiVolumes.setText("");
            }
            this.lblMultiVolumesUnit.setEnabled(false);
        }
    }
    
    private void resetEcryptionKey() {
        if (this.chkEncrypted.getSelection()) {
            this.txtEncryptionKey.setEditable(true);
            this.txtEncryptionKey.setEnabled(true);
            this.cboEncryptionAlgorithm.setEnabled(true);
            this.lblEncryptionAlgorithm.setEnabled(true);
            this.lblEncryptionKey.setEnabled(true);
        } else {
            this.txtEncryptionKey.setEditable(false);
            this.txtEncryptionKey.setEnabled(false);
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
            newTarget.setTrackSymlinks( ! this.chkFollowLinks.getSelection());
            
            // Sources
            HashSet sources = new HashSet();
            for (int i=0; i<this.tblSources.getItemCount(); i++) {
                sources.add((File)this.tblSources.getItem(i).getData());
            }
            newTarget.setSources(sources);
            
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
                } else if (this.rdZip.getSelection() || this.rdZip64.getSelection()) {
                    medium = new IncrementalZipMedium();
                    IncrementalZipMedium zMedium = (IncrementalZipMedium)medium;
                    zMedium.setUseZip64(this.rdZip64.getSelection());

                    zMedium.setMultiVolumes(this.chkMultiVolumes.getSelection());
                    if (this.chkMultiVolumes.getSelection()) {
                        zMedium.setVolumeSize(Long.parseLong(txtMultiVolumes.getText()));
                    }
                    zMedium.setComment(this.txtZipComment.getText());
                    
                    if (cboEncoding.getSelectionIndex() != -1) {
                        zMedium.setCharset(Charset.forName(cboEncoding.getItem(cboEncoding.getSelectionIndex())));
                    }
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
                    try {
                        newTarget.getHistory().importHistory(historyBck);
                    } catch (Throwable e) {
                        Logger.defaultLogger().error("Error during user action history import.", e);
                    }
                }
            }
            newTarget.setFilterGroup(this.mdlFilters);
            
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
    
    private void showDialog(AbstractWindow window) {
        window.setModal(this);
        window.setBlockOnOpen(true);
        window.open();
    }
}
