package com.application.areca;


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
public abstract class AbstractWorkspaceItem
implements WorkspaceItem {
    protected TargetGroup parent;
    protected ConfigurationSource loadedFrom;

	public TargetGroup getParent() {
		return parent;
	}

	public void setParent(TargetGroup parent) {
		this.parent = parent;
		if (parent.getItem(this.getUid()) != this) { // Yeah ... instance check again !
			parent.linkChild(this);
		}
	}

	public boolean isChildOf(WorkspaceItem ancestor) {
		if (this.getParent() == null) {
			return false;
		} else if (this.getParent().getUid().equals(ancestor.getUid())) {
			return true;
		} else {
			return this.getParent().isChildOf(ancestor);
		}
	}

	public ConfigurationSource getLoadedFrom() {
		return loadedFrom;
	}

	public void setLoadedFrom(ConfigurationSource loadedFrom) {
		this.loadedFrom = loadedFrom;
	}
}
