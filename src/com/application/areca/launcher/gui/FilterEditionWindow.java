package com.application.areca.launcher.gui;

import java.io.File;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.filters.AbstractFilterComposite;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;

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
public class FilterEditionWindow 
extends AbstractWindow {
	private static final ResourceManager RM = ResourceManager.instance();
	private static final String TITLE = RM.getLabel("filteredition.dialog.title");

	protected Combo cboFilterType;
	protected Button chkExclude;
	protected Button btnSave;
	protected Button btnTest;
	protected AbstractFilterComposite pnlParams;
	protected Group pnlParamsContainer;
	protected String usualPath = null;

	protected ArchiveFilter currentFilter;  
	protected FileSystemTarget currentTarget;

	public FilterEditionWindow(ArchiveFilter currentFilter, FileSystemTarget currentTarget) {
		super();
		this.currentFilter = currentFilter;
		this.currentTarget = currentTarget;
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// TYPE
		Label lblFilterType = new Label(composite, SWT.NONE);
		lblFilterType.setText(RM.getLabel("filteredition.filtertypefield.label"));
		cboFilterType = new Combo(composite, SWT.READ_ONLY);
		Iterator iter = FilterRepository.getFilters().iterator();
		while (iter.hasNext()) {
			cboFilterType.add((String)iter.next());
		}
		GridData dt = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		dt.widthHint = AbstractWindow.computeWidth(400);
		cboFilterType.setLayoutData(dt);

		cboFilterType.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				refreshParamPnl();
				registerUpdate();
			}
		});

		// EXCLUDE
		new Label(composite, SWT.NONE);
		chkExclude = new Button(composite, SWT.CHECK);
		chkExclude.setText(RM.getLabel("filteredition.exclusionfilterfield.label"));
		chkExclude.setToolTipText(RM.getLabel("filteredition.exclusionfilterfield.tooltip"));
		monitorControl(chkExclude);

		// CONTAINER
		pnlParamsContainer = new Group(composite, SWT.NONE);
		pnlParamsContainer.setText(RM.getLabel("filteredition.parametersfield.label"));
		GridLayout lt = new GridLayout();
		pnlParamsContainer.setLayout(lt);
		pnlParamsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		// INIT
		cboFilterType.select(FilterRepository.getIndex(currentFilter));  
		if (this.currentFilter != null) {
			this.cboFilterType.setEnabled(false);
			chkExclude.setSelection(currentFilter.isLogicalNot());
		} else {
			chkExclude.setSelection(true);
		}

		buildParamPnl();

		// SAVE
		SavePanel sv = new SavePanel(this);
		if (pnlParams == null || pnlParams.allowTest()) {
			sv.addOptionButton("filteredition.test.label");
		}

		Composite pnlSave = sv.buildComposite(composite);
		btnTest = sv.getOptionButton();  
		if (btnTest != null) {
			btnTest.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					testFilter();
				}
			});
		}
		btnSave = sv.getBtnSave();
		pnlSave.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 3, 1));

		composite.pack();
		return composite;
	}

	public String getTitle() {
		return TITLE;
	}

	public ArchiveFilter getCurrentFilter() {
		return currentFilter;
	}

	public FileSystemTarget getCurrentTarget() {
		return currentTarget;
	}

	protected boolean checkBusinessRules() {
		if (pnlParams == null) {
			return true;
		} else {
			return pnlParams.validateParams();
		}
	}

	protected void testFilter() {
		String f = Application.getInstance().showFileDialog(
				usualPath, 
				this,
				null,
				null,
				SWT.OPEN
		);

		if (f != null) {
			File tg = new File(f);
			usualPath = FileSystemManager.getParent(tg);

			ArchiveFilter filter = FilterRepository.buildFilter(this.cboFilterType.getSelectionIndex());
			initFilter(filter);

			boolean accepted = filter.acceptElement(tg, tg);
			String message;
			if (accepted) {
				message = RM.getLabel("filteredition.test.ok.label"); 
			} else {
				message = RM.getLabel("filteredition.test.nok.label"); 
			}
			Application.getInstance().showInformationDialog(
					message, 
					RM.getLabel("filteredition.test.title"), 
					false);
		}
	}

	protected void saveChanges() {
		if (this.currentFilter == null) {
			this.currentFilter = FilterRepository.buildFilter(this.cboFilterType.getSelectionIndex());
		}
		initFilter(this.currentFilter);

		this.hasBeenUpdated = false;
		this.close();
	}

	protected void initFilter(ArchiveFilter filter) {
		filter.setLogicalNot(this.chkExclude.getSelection());
		if (pnlParams != null) {
			pnlParams.initFilter(filter);
		}
	}

	protected void updateState(boolean rulesSatisfied) {
		btnSave.setEnabled(rulesSatisfied);
		if (btnTest != null) {
			btnTest.setEnabled(rulesSatisfied);
		}
	}

	private void buildParamPnl() {
		this.pnlParams = FilterRepository.buildFilterComposite(
				this.cboFilterType.getSelectionIndex(), 
				this.pnlParamsContainer, 
				currentFilter, 
				this);

		GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true);
		if (this.pnlParams != null) {
			this.pnlParams.setLayoutData(dt);
			this.pnlParamsContainer.setVisible(true);
		} else {
			this.pnlParamsContainer.setVisible(false);
		}
	}

	private void refreshParamPnl() {      
		if (pnlParams != null) {
			this.pnlParams.dispose();
			this.pnlParams = null;
			this.getShell().pack(true);
		}

		buildParamPnl();
		this.getShell().pack(true);
	}
}
