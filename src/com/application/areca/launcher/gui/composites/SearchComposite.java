package com.application.areca.launcher.gui.composites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ResourceManager;
import com.application.areca.TargetGroup;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchResult;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class SearchComposite 
extends Composite 
implements MouseListener, Listener, Refreshable { 
    
    protected final ResourceManager RM = ResourceManager.instance();
    private final Application application = Application.getInstance();
    
    private Composite pnlTargets;
    private Composite pnlButtons;
    private Tree tree;
    
    protected Text txtPattern;
    protected Button chkLatest;
    protected Button chkCase;
    protected Button chkRegex;
    protected Button btnSearch;
    protected Button btnSelectAll;
    protected Button btnClear;    
    
    protected boolean select = true;
    
    protected ArrayList targets = new ArrayList();
    protected ArrayList checkBoxes = new ArrayList();

    public SearchComposite(Composite parent) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 1;
        setLayout(layout);

        Composite top = buildTopComposite(this);
        GridData dt1 = new GridData();
        dt1.grabExcessHorizontalSpace = true;
        dt1.grabExcessVerticalSpace = false;
        dt1.horizontalAlignment = SWT.FILL;
        dt1.verticalAlignment = SWT.FILL;
        top.setLayoutData(dt1);
        
        Composite bottom = buildBottomComposite(this);
        GridData dt2 = new GridData();
        dt2.grabExcessHorizontalSpace = true;
        dt2.grabExcessVerticalSpace = true;
        dt2.horizontalAlignment = SWT.FILL;
        dt2.verticalAlignment = SWT.FILL;
        bottom.setLayoutData(dt2);
    }
    
    private Composite buildTopComposite(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.horizontalSpacing = 30;
        panel.setLayout(layout);

        Label lblPattern = new Label(panel, SWT.NONE);
        lblPattern.setText(RM.getLabel("search.criteria.pattern"));
        
        GridData dtPattern = new GridData();
        dtPattern.grabExcessHorizontalSpace = true;
        dtPattern.horizontalAlignment = SWT.FILL;
        txtPattern = new Text(panel, SWT.BORDER);
        txtPattern.setLayoutData(dtPattern);
        
        chkLatest = new Button(panel, SWT.CHECK);
        chkLatest.setSelection(true);
        chkLatest.setText(RM.getLabel("search.criteria.latest"));

        GridData dtTargets = new GridData();
        dtTargets.verticalSpan = 3;
        dtTargets.verticalAlignment = SWT.TOP;
        Label lblTargets = new Label(panel, SWT.NONE);
        lblTargets.setText(RM.getLabel("search.targets.label"));
        lblTargets.setLayoutData(dtTargets);

        GridData dtTargetsContent = new GridData();
        dtTargetsContent.verticalSpan = 3;
        dtTargetsContent.grabExcessHorizontalSpace = true;
        dtTargetsContent.grabExcessVerticalSpace = false;
        dtTargetsContent.horizontalAlignment = SWT.FILL;
        dtTargetsContent.verticalAlignment = SWT.TOP;
        dtTargetsContent.heightHint = 100;
        
        ScrolledComposite scr = new ScrolledComposite(panel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scr.setLayout(new FillLayout());
        scr.setLayoutData(dtTargetsContent);
   
        pnlTargets = new Composite(scr, SWT.NONE);
        GridLayout tgLayout = new GridLayout();
        tgLayout.numColumns = 1;
        tgLayout.verticalSpacing = 0;
        pnlTargets.setLayout(tgLayout);
        scr.setContent(pnlTargets);
        
        chkCase = new Button(panel, SWT.CHECK);
        chkCase.setText(RM.getLabel("search.criteria.case"));
        
        chkRegex = new Button(panel, SWT.CHECK);
        chkRegex.setText(RM.getLabel("search.criteria.regex"));
        
        pnlButtons = new Composite(panel, SWT.NONE);
        GridLayout ly = new GridLayout(3, false);
        ly.marginHeight = 0;
        ly.marginWidth = 0;
        pnlButtons.setLayout(ly);
        pnlButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                
        GridData dtSelectAll = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true);
        btnSelectAll = new Button(pnlButtons, SWT.PUSH);
        btnSelectAll.setLayoutData(dtSelectAll);
        btnSelectAll.setText(RM.getLabel("search.selectall.label"));
        btnSelectAll.addListener(SWT.Selection, this);
        
        GridData dtClearAll = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
        btnClear = new Button(pnlButtons, SWT.PUSH);
        btnClear.setLayoutData(dtClearAll);
        btnClear.setText(RM.getLabel("search.clearall.label"));
        btnClear.addListener(SWT.Selection, this);
        
        GridData dtButton = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true);
        btnSearch = new Button(pnlButtons, SWT.PUSH);
        btnSearch.setLayoutData(dtButton);
        btnSearch.setText(RM.getLabel("search.search.label"));
        btnSearch.addListener(SWT.Selection, this);
        
        return panel;
    }
    
    private Composite buildBottomComposite(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        panel.setLayout(layout);

        GridData data = new GridData();
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = SWT.FILL;
        
        TreeViewer tv = new TreeViewer(panel, SWT.BORDER);
        tree = tv.getTree();
        tree.setLayoutData(data);
        tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
        tree.addMouseListener(this);
        
        return panel;
    }
    
    private boolean haveTargetsChanged() {
        Iterator existingTgs = this.targets.iterator();
        Map map = new HashMap(this.targets.size());
        while (existingTgs.hasNext()) {
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)existingTgs.next();
            map.put(target.getUid(), target);
        }
        
        Iterator pIter = this.application.getWorkspace().getGroupIterator();
        boolean hasChanged = false;
        while (pIter.hasNext() && ! hasChanged) {
            TargetGroup process = (TargetGroup)pIter.next();
            Iterator tIter = process.getSortedTargetIterator();
            while (tIter.hasNext() && ! hasChanged) {
                AbstractRecoveryTarget target = (AbstractRecoveryTarget)tIter.next();
                AbstractRecoveryTarget exist = (AbstractRecoveryTarget)map.remove(target.getUid());
                if (exist == null || ! (exist.getTargetName().equals(target.getTargetName()))) {
                    return true;
                }
            }
        }
        return (map.size() != 0);
    }
    
    private void selectAll() {
        if (select) {
            btnSelectAll.setText(RM.getLabel("search.deselectall.label"));            
        } else {
            btnSelectAll.setText(RM.getLabel("search.selectall.label"));    
        }
        
        Iterator iter = this.checkBoxes.iterator();
        while (iter.hasNext()) {
            Button btn = (Button)iter.next();
            btn.setSelection(select);
        }
        
        select = ! select;
        pnlButtons.layout();
    }
    
    private void clearAll() {
        refreshContent(null);
    }
    
    private void buildTargetList() {
        // REMOVE EXISTING
        Iterator chkIter = this.checkBoxes.iterator();
        while (chkIter.hasNext()) {
            Button btn = (Button)chkIter.next();
            btn.dispose();
        }
        this.checkBoxes.clear();
        this.targets.clear();
        pnlTargets.pack();
        
        // ADD TGS
        Iterator pIter = this.application.getWorkspace().getGroupIterator();
        while (pIter.hasNext()) {
            TargetGroup process = (TargetGroup)pIter.next();
            Iterator tIter = process.getSortedTargetIterator();
            while (tIter.hasNext()) {
                AbstractRecoveryTarget target = (AbstractRecoveryTarget)tIter.next();
                this.targets.add(target);
                Button chk = new Button(pnlTargets, SWT.CHECK);
                chk.setText(target.getTargetName());
                this.checkBoxes.add(chk);
            }
        }
        pnlTargets.setSize(pnlTargets.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }
    
    public void refresh() {
        // Refresh targets if needed
        if (haveTargetsChanged()) {
            buildTargetList();
        }
        
        // pre-check checkBoxes
        Iterator chkIter = this.checkBoxes.iterator();
        Iterator tgIter = this.targets.iterator();
        while (chkIter.hasNext()) {
            Button chk = (Button)chkIter.next();
            AbstractRecoveryTarget target = (AbstractRecoveryTarget)tgIter.next();
            chk.setSelection(
                    (this.application.isCurrentObjectTarget() && this.application.getCurrentTarget().getUid().equals(target.getUid()))
                    || (this.application.isCurrentObjectProcess() && this.application.getCurrentTargetGroup().getUid().equals(target.getGroup().getUid()))
            );
        }
    }

    public Object getRefreshableKey() {
        return this.getClass().getName();
    }

    public void handleEvent(Event event) {
        try {
            if (event.widget.equals(this.btnSearch)) {
                if (this.txtPattern.getText().trim().length() != 0) {              
                    application.enableWaitCursor();
                    SearchResult result = new SearchResult();
    
                    DefaultSearchCriteria criteria = new DefaultSearchCriteria();
                    criteria.setPattern(txtPattern.getText());
                    criteria.setRestrictLatestArchive(this.chkLatest.getSelection());
                    criteria.setMatchCase(chkCase.getSelection());
                    criteria.setRegularExpression(chkRegex.getSelection());
    
                    for (int i=0; i<this.targets.size(); i++) {
                        Button chk = (Button)this.checkBoxes.get(i);
                        if (chk.getSelection()) {
                            AbstractRecoveryTarget target = (AbstractRecoveryTarget)this.targets.get(i);
                            TargetSearchResult targetResult = target.search(criteria);
                            if (! targetResult.isEmpty()) {
                                result.setTargetSearchResult(target, targetResult);
                            }
                        }
                    }
    
                    refreshContent(result);
                } 
            } else if (event.widget.equals(this.btnClear)) {
                clearAll();                
            } else {
                selectAll();
            }
        } catch (Throwable e) {
            this.application.handleException(e);
        } finally {
            application.disableWaitCursor();            
        }
    }
    
    private void refreshContent(SearchResult result) {
        tree.removeAll();

        if (result != null) {
            Iterator iter = result.targetIterator();
            while (iter.hasNext()) {
                AbstractRecoveryTarget target = (AbstractRecoveryTarget)iter.next();
    
                TreeItem targetNode =new TreeItem(tree, SWT.NONE);
                targetNode.setText(target.getTargetName());
                targetNode.setData(target);
                targetNode.setImage(ArecaImages.ICO_REF_TARGET);
    
                TargetSearchResult tResult = result.getTargetSearchResult(target);
                Iterator items = tResult.getItems().iterator();
                while (items.hasNext()) {
                    SearchResultItem searchItem = (SearchResultItem)items.next();
                    TreeItem item = new TreeItem(targetNode, SWT.NONE);
                    item.setData(searchItem);
                    item.setText(searchItem.getEntry().getKey());
                    item.setImage(ArecaImages.ICO_FS_FILE);
                }
            }
    
            if (result.size() == 0) {
                TreeItem noresult = new TreeItem(tree, SWT.NONE);
                noresult.setText(RM.getLabel("search.noresult.label"));
            }
    
            if (result.size() == 1) {
                tree.getItem(0).setExpanded(true);
            }
        }
    }

    public void mouseDoubleClick(MouseEvent e) {}
    public void mouseUp(MouseEvent e) {}
    public void mouseDown(MouseEvent e) {
        TreeItem item = this.tree.getItem(new Point(e.x, e.y));
        if (item != null && item.getParentItem() != null) {
            showMenu(e, Application.getInstance().getSearchContextMenu());
        }
    }
    
    private void showMenu(MouseEvent e, Menu m) {
        if (e.button == 3) {
            m.setVisible(true);
        }
    }
    
    public SearchResultItem getSelectedItem() {
        TreeItem[] selection = tree.getSelection();
        if (selection != null && selection.length != 0 && selection[0].getData() instanceof SearchResultItem) {
            return (SearchResultItem)selection[0].getData();
        } else {
            return null;
        }
    }
}
