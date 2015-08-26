package com.application.areca.launcher.gui.composites;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

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
public abstract class AbstractTabComposite 
extends Composite {
	private CTabItem tab;
	private boolean focus = false;
	
	public AbstractTabComposite(Composite parent, int style) {
		super(parent, style);
	}

	public CTabItem getTab() {
		return tab;
	}

	public void setTab(CTabItem item) {
		this.tab = item;
	}
	
	public void getFocus() {
		focus = true;
	}
	
	public void looseFocus() {
		focus = false;
	}
	
	public boolean hasFocus() {
		return focus;
	}
}
