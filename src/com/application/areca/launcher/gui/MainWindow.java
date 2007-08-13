package com.application.areca.launcher.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
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

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Colors;
import com.application.areca.launcher.gui.common.LocalPreferences;
import com.application.areca.launcher.gui.composites.PhysicalViewComposite;
import com.application.areca.launcher.gui.composites.HistoryComposite;
import com.application.areca.launcher.gui.composites.IndicatorsComposite;
import com.application.areca.launcher.gui.composites.LogComposite;
import com.application.areca.launcher.gui.composites.LogicalViewComposite;
import com.application.areca.launcher.gui.composites.ProgressComposite;
import com.application.areca.launcher.gui.composites.PropertiesComposite;
import com.application.areca.launcher.gui.composites.SearchComposite;
import com.application.areca.launcher.gui.composites.TreeComposite;
import com.application.areca.launcher.gui.menus.AppActionReferenceHolder;
import com.application.areca.launcher.gui.menus.MenuBuilder;
import com.application.areca.launcher.gui.menus.ToolBarBuilder;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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
public class MainWindow extends AbstractWindow {

    private TreeComposite pnlTree;
    private PropertiesComposite pnlProperties;
    private CTabFolder tabs;
    private SashForm leftSash;
    private SashForm mainSash;
    private Composite progressContainer;

    /**
     * @param display
     */
    public MainWindow() {
        super();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        shell.setMenuBar(MenuBuilder.buildMainMenu(shell));
        Application.getInstance().initMenus(shell);

        readShellPreferences(shell);
        
        shell.addListener(SWT.Show, new Listener() {
            public void handleEvent(Event event) {
                application.checkSystem();
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

        ToolBarBuilder.buildMainToolBar(composite);

        // MAIN SASH
        mainSash = new SashForm(composite, SWT.HORIZONTAL);
        mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        //mainSash.setLayout(new FillLayout());

        // LEFT SASH
        leftSash = new SashForm(mainSash, SWT.VERTICAL);
        //leftSash.setLayout(new FillLayout());

        // TREE
        pnlTree = new TreeComposite(leftSash);
        
        // PROPERTIES
        pnlProperties = new PropertiesComposite(leftSash);

        leftSash.setWeights(new int[] {70, 30});

        // TABS
        tabs = new CTabFolder(mainSash, SWT.BORDER);
        tabs.setSimple(Application.SIMPLE_MAINTABS);
        tabs.setUnselectedCloseVisible(true);

        progressContainer = new ProgressComposite(tabs);
        
        addFolderItem(RM.getLabel("mainpanel.physical.label"), ArecaImages.ICO_REF_TARGET, new PhysicalViewComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.logical.label"), ArecaImages.ICO_REF_TARGET, new LogicalViewComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.history.label"), ArecaImages.ICO_HISTORY, new HistoryComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.indicators.label"), ArecaImages.ICO_TARGET_NEW, new IndicatorsComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.search.label"), ArecaImages.ICO_FIND, new SearchComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.log.label"), ArecaImages.ICO_TARGET_NEW, new LogComposite(tabs));
        addFolderItem(RM.getLabel("mainpanel.progress.label"), ArecaImages.ICO_TARGET_NEW, progressContainer);

        tabs.setSelection(0);
        application.getFolderMonitor().handleSelection(tabs.getItem(0));

        mainSash.setWeights(new int[] {30, 70});

        // Force colors loading
        Color c = Colors.C_BLACK;
        c.getBlue();

        readPreferences();
        
        return composite;
    }
    
    public void focusOnProgress() {
        this.tabs.setSelection(6);
        this.application.getFolderMonitor().handleSelection(this.tabs.getItem(6));
    }

    public Composite getProgressContainer() {
        return progressContainer;
    }

    public void addFolderItem(String title, Image img, Composite content) {
        CTabItem itm = new CTabItem(tabs, SWT.NONE);
        Application.setTabLabel(itm, title, img != null);
        if (img != null) {
            itm.setImage(img);
        }
        itm.setControl(content);

        this.application.getFolderMonitor().registerTabItem(itm);
    }

    public void refresh(boolean refreshTree, boolean refreshLog) {
        application.getFolderMonitor().invalidate();
        application.resetCurrentDates();
        AppActionReferenceHolder.refresh();

        pnlProperties.refresh();

        if (refreshTree) {
            this.pnlTree.refresh();
        }
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
            return super.close();
        } else {
            application.processExit();
            return true;
        }
    }

    public void enforceSelectedTarget(AbstractRecoveryTarget target) {
        this.pnlTree.setSelectedTarget(target);
    }
}
