package com.application.areca.launcher.gui.donation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.common.ArecaImages;
import com.application.areca.launcher.gui.common.SavePanel;
import com.application.areca.launcher.gui.composites.DonationLink;

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
public class DonationWindow 
extends AbstractWindow {
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        
		Label icon = new Label(composite, SWT.NONE);
		icon.setImage(ArecaImages.ICO_BIG);
		icon.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 2));
        
        Label lbl = new Label(composite, SWT.NONE);
        lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        lbl.setText("You have used Areca-Backup more than " + ArecaUserPreferences.getLaunchCount() + " times since its installation on your computer.\nIf you find Areca useful, please consider making a donation to support the time that has been (and still is) spent on it.");
        
        Link lnk = DonationLink.build(composite);
        lnk.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        
        SavePanel pnlSave = new SavePanel(RM.getLabel("common.close.label"), this);
        pnlSave.setShowCancel(false);
        Composite pnl = pnlSave.buildComposite(composite);
        pnl.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
        
        composite.pack();
        return composite;
    }

    public String getTitle() {
        return RM.getLabel("about.support");
    }

    protected boolean checkBusinessRules() {
        return true;
    }

    protected void saveChanges() {
        this.hasBeenUpdated = false;
        this.close();
    }

    protected void updateState(boolean rulesSatisfied) {
    }
}
