package com.application.areca.context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <BR>
 * 
 * @author Olivier PETRUCCI <BR>
 *         
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
public class RecoveryResult {
	protected List processedArchives = new ArrayList();
	protected List recoveredArchives = new ArrayList();
	protected List ignoredArchives = new ArrayList();

	public List getIgnoredArchives() {
		return ignoredArchives;
	}

	public List getProcessedArchives() {
		return processedArchives;
	}

	public List getRecoveredArchives() {
		return recoveredArchives;
	}

	public File[] getIgnoredArchivesAsArray() {
		return (File[]) ignoredArchives
				.toArray(new File[ignoredArchives.size()]);
	}

	public File[] getRecoveredArchivesAsArray() {
		return (File[]) recoveredArchives.toArray(new File[recoveredArchives.size()]);
	}

	public File[] getProcessedArchivesAsArray() {
		return (File[]) processedArchives.toArray(new File[processedArchives.size()]);
	}

	public void addProcessedArchives(File[] archives) {
		for (int i = 0; i < archives.length; i++) {
			this.processedArchives.add(archives[i]);
		}
	}

	public void addRecoveredArchives(File[] archives) {
		for (int i = 0; i < archives.length; i++) {
			this.recoveredArchives.add(archives[i]);
		}
	}
}
