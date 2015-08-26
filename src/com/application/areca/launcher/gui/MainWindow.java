package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.application.areca.AbstractTarget;
import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.LocalPreferences;
import com.application.areca.launcher.gui.donation.GUIDonationHelper;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.composites.AbstractTabComposite;
import com.application.areca.launcher.gui.composites.HistoryComposite;
import com.application.areca.launcher.gui.composites.IndicatorsComposite;
import com.application.areca.launcher.gui.composites.LogComposite;
import com.application.areca.launcher.gui.composites.LogicalViewComposite;
import com.application.areca.launcher.gui.composites.PhysicalViewComposite;
import com.application.areca.launcher.gui.composites.ProgressComposite;
import com.application.areca.launcher.gui.composites.PropertiesComposite;
import com.application.areca.launcher.gui.composites.SearchComposite;
import com.application.areca.launcher.gui.composites.TargetTreeComposite;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.menus.MenuBuilder;
import com.application.areca.launcher.gui.menus.ToolBarBuilder;
import com.application.areca.metadata.trace.TraceEntry;
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
public class MainWindow extends AbstractWindow {
    private TargetTreeComposite pnlTree;
    private PropertiesComposite pnlProperties;
    private CTabFolder tabs;
    private SashForm leftSash;
    private SashForm mainSash;
    private ProgressComposite progressContainer;
    private int returnTabIndex = 0;
    private LogicalViewComposite logicalView;
    private SearchComposite searchView;
    private String workspacePath;
    
    private static final int TAB_PHYSICAL = 0;
    private static final int TAB_LOGICAL = 1;
    private static final int TAB_PROGRESS = 6;

    /**
     * @param display
     */
    public MainWindow() {
        super();
    }

    public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

	protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setMenuBar(MenuBuilder.buildMainMenu(shell));
        Application.getInstance().initMenus(shell);

        readShellPreferences(shell);
        
        shell.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
                application.checkSystem();
                application.checkVersion(false);
                GUIDonationHelper.handleDonationMessage();
                if (workspacePath != null) {
                	application.openWorkspace(workspacePath);
                }
            }
        });
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 0;        
        mainLayout.marginTop = 0;
        mainLayout.marginBottom = 5;
        mainLayout.verticalSpacing = 2;
        composite.setLayout(mainLayout);

        if (ArecaUserPreferences.isDisplayToolBar()) {
        	ToolBarBuilder.buildMainToolBar(composite);
        }

        // MAIN SASH
        mainSash = new SashForm(composite, SWT.HORIZONTAL);
        mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // LEFT SASH
        leftSash = new SashForm(mainSash, SWT.VERTICAL);

        // TREE
        pnlTree = new TargetTreeComposite(leftSash);
        
        // PROPERTIES
        pnlProperties = new PropertiesComposite(leftSash);

        leftSash.setWeights(new int[] {70, 30});

        // TABS
        tabs = new CTabFolder(mainSash, SWT.BORDER);
        tabs.setSimple(Application.SIMPLE_MAINTABS);

        progressContainer = new ProgressComposite(tabs);
        
        addFolderItem(RM.getLabel("mainpanel.physical.label"), ArecaImages.ICO_REF_TARGET, new PhysicalViewComposite(tabs));
        this.logicalView = new LogicalViewComposite(tabs);
        addFolderItem(RM.getLabel("mainpanel.logical.label"), ArecaImages.ICO_REF_TARGET, this.logicalView);
        addFolderItem(RM.getLabel("mainpanel.history.label"), ArecaImages.ICO_HISTORY, new HistoryComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.indicators.label"), ArecaImages.ICO_TARGET_NEW, new IndicatorsComposite(tabs));
        this.searchView = new SearchComposite(tabs);
        addFolderItem(RM.getLabel("mainpanel.search.label"), ArecaImages.ICO_FIND, searchView);
        addFolderItem(RM.getLabel("mainpanel.log.label"), ArecaImages.ICO_TAB_LOG, new LogComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.progress.label"), ArecaImages.ICO_CHANNEL, progressContainer);

        int selectedTab = ArecaUserPreferences.isDisplayLogicalViewOnStartup() ? TAB_LOGICAL : TAB_PHYSICAL;
        tabs.setSelection(selectedTab);
        application.getFolderMonitor().handleSelection(tabs.getItem(selectedTab));
        
        mainSash.setWeights(new int[] {30, 70});

        // Force colors loading
        Color c = Colors.C_BLACK;
        c.getBlue();

        readPreferences();
        
        return composite;
    }

    public SearchComposite getSearchView() {
        return searchView;
    }

    public void focusOnProgress() {
        int tab = this.application.getFolderMonitor().getCurrentSelection();
        if (tab != TAB_PROGRESS) {
            this.returnTabIndex = tab;
        }
        this.tabs.setSelection(TAB_PROGRESS);
        this.application.getFolderMonitor().handleSelection(this.tabs.getItem(TAB_PROGRESS));
    }
    
    public void focusOnLogicalView(TraceEntry entry) {
        this.tabs.setSelection(TAB_LOGICAL);
        this.application.getFolderMonitor().handleSelection(this.tabs.getItem(TAB_LOGICAL));
        this.logicalView.setSelectedEntry(entry);
    }
    
    public void goBackToLastTab() {
        if (this.application.getFolderMonitor().getCurrentSelection() == TAB_PROGRESS) {
            this.tabs.setSelection(this.returnTabIndex);
            this.application.getFolderMonitor().handleSelection(this.tabs.getItem(this.returnTabIndex));
        }
    }

    public ProgressComposite getProgressContainer() {
        return progressContainer;
    }

    public void addFolderItem(String title, Image img, AbstractTabComposite content) {
        CTabItem itm = new CTabItem(tabs, SWT.NONE);
        content.setTab(itm);
        if (img != null) {
            itm.setImage(img);
        }
        Application.setTabLabel(itm, title);
        itm.setControl(content);
        this.application.getFolderMonitor().registerTabItem(itm);
    }

    public void refresh(boolean refreshTree, boolean refreshLog) {
        application.getFolderMonitor().invalidate();
        application.resetCurrentDates();
        AppActionReferenceHolder.refresh();

        refreshProperties();

        if (refreshTree) {
            this.pnlTree.refresh();
            pnlTree.synchronizeHistory();
        }
    }
    
    public void refreshProperties() {
        pnlProperties.refresh();
    }

    public String getTitle() {
        return null;
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
    }

    protected void updateState(boolean rulesSatisfied) {
    }

    private void readShellPreferences(Shell shell) {
        try {
            boolean max = LocalPreferences.instance().getBoolean("mainframe.maximized");
    
            if (max || ! LocalPreferences.instance().contains("mainframe.x")) {
                shell.setMaximized(true);
            } else {
                int x = LocalPreferences.instance().getInt("mainframe.x");
                int y = LocalPreferences.instance().getInt("mainframe.y");
                int wi = LocalPreferences.instance().getInt("mainframe.width");
                int he = LocalPreferences.instance().getInt("mainframe.height");
                shell.setBounds(x, y, wi, he);
            }
        } catch (Throwable e) {
            Logger.defaultLogger().error("Error loading user preferences.", e);
        }
    }

    private void readPreferences() {
        try {
            int pos = LocalPreferences.instance().getInt("mainframe.mainsplitpos", 30);
            mainSash.setWeights(new int[] {pos, 100-pos});
    
            pos = LocalPreferences.instance().getInt("mainframe.leftsplitpos", 70);
            leftSash.setWeights(new int[] {pos, 100-pos});
        } catch (Throwable e) {
            Logger.defaultLogger().error("Error loading user preferences.", e);
        }
    }

    protected void savePreferences() {
        try {
            Shell shell = getShell();
            Rectangle bounds = shell.getBounds();
            LocalPreferences.instance().set("mainframe.x", bounds.x);
            LocalPreferences.instance().set("mainframe.y", bounds.y);
            LocalPreferences.instance().set("mainframe.width", bounds.width);
            LocalPreferences.instance().set("mainframe.height", bounds.height);
            LocalPreferences.instance().set("mainframe.maximized", shell.getMaximized());
    
    
            LocalPreferences.instance().set("mainframe.mainsplitpos", normalizeWeights(mainSash.getWeights()));
            LocalPreferences.instance().set("mainframe.leftsplitpos", normalizeWeights(leftSash.getWeights())); 
    
            LocalPreferences.instance().save();
        } catch (Throwable e) {
            Logger.defaultLogger().error("Error saving user preferences.", e);
        }
    }

    private static int normalizeWeights(int[] w) {
        return (int)(100 * w[0] / (w[0] + w[1]));
    }

    public void show() {
        setBlockOnOpen(true);
        open();
        Display.getCurrent().dispose();
    }

    public boolean close() {
        return close(false);
    }

    public boolean close(boolean force) {
        if (force) {
        	Logger.defaultLogger().info("Closing Areca's main window ...");
            return super.close();
        } else {
            application.processExit();
            return true;
        }
    }

    public void enforceSelectedTarget(AbstractTarget target) {
        this.pnlTree.setSelectedTarget(target);
    }
}
