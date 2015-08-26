package com.application.areca.search;

import java.util.HashSet;
import java.util.Set;

import com.application.areca.AbstractTarget;
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
public class DefaultSearchCriteria implements SearchCriteria {

    private String pattern;
    private boolean restrictLatestArchive;
    private boolean matchCase;
    private boolean regularExpression;
    private Set targets = new HashSet();
    private TaskMonitor monitor;
    
    public DefaultSearchCriteria() {
    }

    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public boolean isRestrictLatestArchive() {
        return restrictLatestArchive;
    }
    
    public void setRestrictLatestArchive(boolean restrictLatestArchive) {
        this.restrictLatestArchive = restrictLatestArchive;
    }
    
    public Set getTargets() {
        return targets;
    }
    
    public void addTarget(AbstractTarget target) {
        this.targets.add(target);
    }

    public boolean isMatchCase() {
        return matchCase;
    }
    
    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }
    
    public boolean isRegularExpression() {
        return regularExpression;
    }
    
    public void setRegularExpression(boolean regularExpression) {
        this.regularExpression = regularExpression;
    }

	public TaskMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(TaskMonitor monitor) {
		this.monitor = monitor;
	}
}
