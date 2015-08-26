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

import com.application.areca.AbstractTarget;
import com.application.areca.TargetGroup;
import com.application.areca.WorkspaceItem;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.Refreshable;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.search.DefaultSearchCriteria;
import com.application.areca.search.SearchResult;
import com.application.areca.search.SearchResultItem;
import com.application.areca.search.TargetSearchResult;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

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
public class SearchComposite 
extends AbstractTabComposite 
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
	protected Button btnCancel;
	protected Button btnSelectAll;
	protected Button btnClear;    
	protected Label lblLog;

	protected boolean select = true;

	protected ArrayList targets = new ArrayList();
	protected ArrayList checkBoxes = new ArrayList();
	
	protected TaskMonitor mon;

	public SearchComposite(Composite parent) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		Composite top = buildTopComposite(this);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Composite bottom = buildBottomComposite(this);
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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

		GridData dtTargetsContent = new GridData(SWT.FILL, SWT.TOP, true, false);
		dtTargetsContent.verticalSpan = 3;
		dtTargetsContent.heightHint = 100;

		ScrolledComposite scr = new ScrolledComposite(panel, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scr.setLayout(new FillLayout());
		scr.setLayoutData(dtTargetsContent);

		pnlTargets = new Composite(scr, SWT.NONE);
		GridLayout tgLayout = new GridLayout(1, false);
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

		btnSelectAll = new Button(pnlButtons, SWT.PUSH);
		btnSelectAll.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));
		btnSelectAll.setText(RM.getLabel("search.selectall.label"));
		btnSelectAll.addListener(SWT.Selection, this);

		btnSearch = new Button(pnlButtons, SWT.PUSH);
		btnSearch.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true));
		btnSearch.setText(RM.getLabel("search.search.label"));
		btnSearch.addListener(SWT.Selection, this);
		
		btnCancel = new Button(pnlButtons, SWT.PUSH);
		btnCancel.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true));
		btnCancel.setText(RM.getLabel("search.cancel.label"));
		btnCancel.addListener(SWT.Selection, this);
		btnCancel.setEnabled(false);

		return panel;
	}

	private Composite buildBottomComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout(2, false));

		TreeViewer tv = new TreeViewer(panel, SWT.BORDER);
		tree = tv.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tree.setLinesVisible(AbstractWindow.getTableLinesVisible());
		tree.addMouseListener(this);

		lblLog = new Label(panel, SWT.NONE);
		lblLog.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		btnClear = new Button(panel, SWT.PUSH);
		btnClear.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		btnClear.setText(RM.getLabel("search.clearall.label"));
		btnClear.addListener(SWT.Selection, this);

		return panel;
	}

	private void log(final String s) {
		Logger.defaultLogger().info(s);
		SecuredRunner.execute(new Runnable() {
			public void run() {
				lblLog.setText(s);
			}
		});
	}

	private String format(String s, int nb) {
		String ret = "" + nb + s;
		if (nb != 1) {
			ret += "s";
		}
		return ret;
	}

	private boolean haveTargetsChanged() {
		Iterator existingTgs = this.targets.iterator();
		Map map = new HashMap(this.targets.size());
		while (existingTgs.hasNext()) {
			AbstractTarget target = (AbstractTarget)existingTgs.next();
			map.put(target.getUid(), target);
		}

		if (checkChanged(application.getWorkspace().getContent(), map)) {
			return true;
		}
		return (map.size() != 0);
	}

	private boolean checkChanged(TargetGroup group, Map ref) {
		Iterator iter = group.getIterator();
		while (iter.hasNext()) {
			WorkspaceItem item = (WorkspaceItem)iter.next();
			if (item instanceof AbstractTarget) {
				AbstractTarget target = (AbstractTarget)item;
				AbstractTarget exist = (AbstractTarget)ref.remove(target.getUid());
				if (exist == null || ! (exist.getName().equals(target.getName()))) {
					return true;
				}
			} else {
				if (checkChanged((TargetGroup)item, ref)) {
					return true;
				}
			}
		}
		return false;
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
		addItems(application.getWorkspace().getContent());
		pnlTargets.setSize(pnlTargets.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addItems(TargetGroup group) {
		Iterator iter = group.getSortedIterator(true);
		while (iter.hasNext()) {
			WorkspaceItem item = (WorkspaceItem)iter.next();
			if (item instanceof AbstractTarget) {
				AbstractTarget target = (AbstractTarget)item;
				this.targets.add(target);
				Button chk = new Button(pnlTargets, SWT.CHECK);
				chk.setText(target.getParent().getFullPath(" / ") + " / " + target.getName());
				this.checkBoxes.add(chk);
			} else {
				addItems((TargetGroup)item);
			}
		}
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
			AbstractTarget target = (AbstractTarget)tgIter.next();
			chk.setSelection(
					(this.application.isCurrentObjectTarget() && this.application.getCurrentTarget().getUid().equals(target.getUid()))
					|| (this.application.isCurrentObjectTargetGroup() && target.isChildOf(this.application.getCurrentTargetGroup()))
			);
		}
	}

	public Object getRefreshableKey() {
		return this.getClass().getName();
	}

	public void handleEvent(Event event) {

		if (event.widget.equals(this.btnSearch)) {
			if (this.txtPattern.getText().trim().length() != 0) {  
				clearAll();
				
				final SearchResult result = new SearchResult();
				mon = new TaskMonitor("Search");

				final DefaultSearchCriteria criteria = new DefaultSearchCriteria();
				criteria.setPattern(txtPattern.getText());
				criteria.setRestrictLatestArchive(this.chkLatest.getSelection());
				criteria.setMatchCase(chkCase.getSelection());
				criteria.setRegularExpression(chkRegex.getSelection());
				criteria.setMonitor(mon);

				final ArrayList selected = new ArrayList();
				for (int i=0; i<targets.size(); i++) {
					Button chk = (Button)checkBoxes.get(i);
					if (chk.getSelection()) {
						selected.add(targets.get(i));
					}
				}
				
				Runnable rn = new Runnable() {
					public void run() {
						SecuredRunner.execute(new Runnable() {
							public void run() {
								application.enableWaitCursor();
								btnClear.setEnabled(false);
								btnSearch.setEnabled(false);
								btnSelectAll.setEnabled(false);
								btnCancel.setEnabled(true);
							}
						});

						try {
							for (int i=0; i<selected.size(); i++) {
								mon.checkTaskState();
								AbstractTarget target = (AbstractTarget)selected.get(i);
								log("Searching \"" + criteria.getPattern() + "\" in \"" + target.getName() + "\" ...");
								TargetSearchResult targetResult = target.search(criteria);
								if (! targetResult.isEmpty()) {
									result.setTargetSearchResult(target, targetResult);
								}
							}
							
							log("Search completed - Initializing view ...");

							SecuredRunner.execute(new Runnable() {
								public void run() {
									refreshContent(result);
								}
							});

							int nb = result.resultCount();
							if (nb == 0) {
								log("Search completed - No result found");
							} else {
								log("Search completed - " + format(" result", nb) + " found in " + format(" target", result.size()) + ".");
							}
						} catch (final TaskCancelledException e) {	
							log("Search cancelled.");
						} catch (final Throwable e) {
							SecuredRunner.execute(new Runnable() {
								public void run() {
									application.handleException(e);
								}
							}); 
						} finally {
							SecuredRunner.execute(new Runnable() {
								public void run() {
									application.disableWaitCursor();
									btnClear.setEnabled(true);
									btnSearch.setEnabled(true);
									btnSelectAll.setEnabled(true);
									btnCancel.setEnabled(false);
								}
							});           
						}
					}
				};
				
				Thread searchThread = new Thread(rn);
				searchThread.setName("Search \"" + criteria.getPattern() + "\"");
				searchThread.start();
			} 
		} else if (event.widget.equals(this.btnClear)) {
			log("");
			clearAll();                
		} else if (event.widget.equals(this.btnCancel)) {
			if (mon != null) {
				mon.setCancelRequested();
			}
		} else {
			selectAll();
		}
	}

	private void refreshContent(SearchResult result) {
		tree.removeAll();

		if (result != null) {
			Iterator iter = result.targetIterator();
			while (iter.hasNext()) {
				AbstractTarget target = (AbstractTarget)iter.next();
				TargetSearchResult tResult = result.getTargetSearchResult(target);
				
				TreeItem targetNode =new TreeItem(tree, SWT.NONE);
				targetNode.setText(target.getName() + " (" + tResult.getItems().size() + ")");
				targetNode.setData(target);
				targetNode.setImage(ArecaImages.ICO_REF_TARGET);

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
