package com.application.areca.impl.copypolicy;

import java.io.File;

import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.AskBeforeOverwriteWindow;
import com.application.areca.launcher.gui.common.SecuredRunner;
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
public class AskBeforeOverwriteCopyPolicy extends OverwriteCopyPolicy {
	private boolean decisionRemembered = false;
	private boolean overwrite = false;

	protected boolean overrideExistingFile(File file) {
		if (decisionRemembered) {
			return overwrite;
		} else {
			final AskBeforeOverwriteWindow window = new AskBeforeOverwriteWindow(file);
			SecuredRunner.execute(new Runnable() {
				public void run() {
					Application.getInstance().showDialog(window);
					decisionRemembered = window.isRemembered();
					if (decisionRemembered) {
						overwrite = window.isOverwrite();
						Logger.defaultLogger().fine("Override decision remembered : Further files " + (overwrite ? "will":"won't") + " be overriden.");
					}
				}
			});
			
			return window.isOverwrite();
		}
	}

	public void rememberDecision(boolean overwrite) {
		this.overwrite = overwrite;
		this.decisionRemembered = true;
	}
}
