package com.application.areca.launcher.gui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.RecoveryProcess;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.ResourceManager;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchResult;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
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
public class SearchComposite 
extends Composite 
implements Listener, Refreshable, IDoubleClickListener { 
    
    protected final ResourceManager RM = ResourceManager.instance();
    private final Application application = Application.getInstance();
    
    private SearchResultItem currentItem = null;
    private Tree tree;
    
    protected Text txtPattern;
    protected Button chkLatest;
    protected Button chkCase;
    protected Button chkRegex;
    protected Button btnSearch;
    
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
   
        Composite pnlTargets = new Composite(scr, SWT.NONE);
        GridLayout tgLayout = new GridLayout();
        tgLayout.numColumns = 1;
        tgLayout.verticalSpacing = 0;
        pnlTargets.setLayout(tgLayout);
        
        Iterator pIter = this.application.getWorkspace().getProcessIterator();
        while (pIter.hasNext()) {
            RecoveryProcess process = (RecoveryProcess)pIter.next();
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
        scr.setContent(pnlTargets);
        
        chkCase = new Button(panel, SWT.CHECK);
        chkCase.setText(RM.getLabel("search.criteria.case"));
        
        chkRegex = new Button(panel, SWT.CHECK);
        chkRegex.setText(RM.getLabel("search.criteria.regex"));
        
        GridData dtButton = new GridData();
        dtButton.verticalAlignment = SWT.BOTTOM;
        dtButton.horizontalAlignment = SWT.RIGHT;
        btnSearch = new Button(panel, SWT.PUSH);
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
        tv.addDoubleClickListener(this);
        
        return panel;
    }
    
    public void refresh() {
        // does nothing
    }

    public Object getRefreshableKey() {
        return this.getClass().getName();
    }

    public void handleEvent(Event event) {
        try {
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
        } catch (Throwable e) {
            this.application.handleException(e);
        } finally {
            application.disableWaitCursor();            
        }
    }
    
    private void refreshContent(SearchResult result) {
        this.currentItem = null;
        tree.removeAll();

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
                item.setText(searchItem.getEntry().getName());
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

    public void doubleClick(DoubleClickEvent event) {
        TreeItem[] selection = tree.getSelection();
        if (selection != null && selection.length != 0 && selection[0].getData() instanceof SearchResultItem) {
            this.currentItem = (SearchResultItem)selection[0].getData();
            
            this.application.enforceSelectedTarget(this.currentItem.getTarget());
            this.application.setCurrentDates(this.currentItem.getCalendar(), this.currentItem.getCalendar());
            
            this.application.showArchiveDetail(this.currentItem.getEntry());
        }
    }
}
