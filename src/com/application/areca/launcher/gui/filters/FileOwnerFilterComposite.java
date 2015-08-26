package com.application.areca.launcher.gui.filters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.FileOwnerArchiveFilter;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;


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
public class FileOwnerFilterComposite extends AbstractFilterComposite  {

	protected Text txtOwner;
	protected Text txtGroup;

	public FileOwnerFilterComposite(Composite composite, ArchiveFilter filter, FilterEditionWindow window) {
		super(composite, FilterRepository.getIndex(FileOwnerFilterComposite.class), filter, window);

		this.setLayout(new GridLayout(2, false));

		txtOwner = addTextBox("filteredition.ownerfld");
		txtGroup = addTextBox("filteredition.groupfld");

		FileOwnerArchiveFilter sff = (FileOwnerArchiveFilter)currentFilter;
		if (sff != null) {
			if (sff.getOwner() != null) {
				txtOwner.setText(sff.getOwner());
			}
			if (sff.getGroup() != null) {
				txtGroup.setText(sff.getGroup());
			}
		}
	}

	private Text addTextBox(String label) {
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(RM.getLabel(label + ".label"));
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Text txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		window.monitorControl(txt);
		
		return txt;
	}

	public void initFilter(ArchiveFilter filter) {
		FileOwnerArchiveFilter sff = (FileOwnerArchiveFilter)filter;
		if (txtOwner.getText() != null && txtOwner.getText().trim().length() == 0) {
			sff.setOwner(null);
		} else {
			sff.setOwner(txtOwner.getText());
		}
		
		if (txtGroup.getText() != null && txtGroup.getText().trim().length() == 0) {
			sff.setGroup(null);
		} else {
			sff.setGroup(txtGroup.getText());	
		}
	}

	public boolean validateParams() {
        window.resetErrorState(txtGroup);
        window.resetErrorState(txtOwner);  
		if (txtGroup.getText().trim().length() == 0 && txtOwner.getText().trim().length() == 0) {
			window.setInError(txtGroup, RM.getLabel("error.owner.or.group.expected"));
			window.setInError(txtOwner, RM.getLabel("error.owner.or.group.expected"));
			return false;
		}
		return true;
	}
}
