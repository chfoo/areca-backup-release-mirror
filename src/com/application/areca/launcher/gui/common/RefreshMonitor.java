package com.application.areca.launcher.gui.common;

import com.application.areca.launcher.gui.Application;

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
public class RefreshMonitor {

	private boolean validated = false;
	private boolean synchronous = false;
	private Refreshable refreshable = null;

	public RefreshMonitor(Refreshable refreshable) {
		this.refreshable = refreshable;
	}

	public void invalidate() {
		this.validated = false;

		if (synchronous) {
			refresh();
			this.validated = true;
		}
	}

	public void getFocus() {
		this.synchronous = true;
		this.refreshable.getFocus();

		if (! validated) {
			refresh();
			this.validated = true;
		}
	}

	private void refresh() {
		try {
			Application.getInstance().enableWaitCursor();
			refreshable.refresh();
		} finally {
			Application.getInstance().disableWaitCursor();
		}
	}

	public void lostFocus() {
		this.synchronous = false;
		this.refreshable.looseFocus();
	}
}
