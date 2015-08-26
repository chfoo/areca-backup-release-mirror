package com.application.areca.launcher.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileDateArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileOwnerArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.filter.SpecialFileFilter;
import com.application.areca.launcher.gui.filters.AbstractFilterComposite;
import com.application.areca.launcher.gui.filters.DirectoryFilterComposite;
import com.application.areca.launcher.gui.filters.FileDateFilterComposite;
import com.application.areca.launcher.gui.filters.FileExtensionFilterComposite;
import com.application.areca.launcher.gui.filters.FileOwnerFilterComposite;
import com.application.areca.launcher.gui.filters.FileSizeFilterComposite;
import com.application.areca.launcher.gui.filters.FilterGroupComposite;
import com.application.areca.launcher.gui.filters.RegexFilterComposite;
import com.application.areca.launcher.gui.filters.SpecFileFilterComposite;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.system.OSTool;
import com.myJava.util.log.Logger;

/**
 * <BR>0 > group
 * <BR>1 > file extension
 * <BR>2 > regex
 * <BR>3 > directory
 * <BR>4 > size
 * <BR>5 > date
 * <BR>6 > locked files
 * <BR>7 > special files
 * <BR>8 > file owner
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
public class FilterRepository {
	private static final ResourceManager RM = ResourceManager.instance();
	private static final int DEFAULT_INDEX = 1;

	private static String[] LABELS = new String[] {
		RM.getLabel("filteredition.filtergroup.label"),
		RM.getLabel("filteredition.fileextensionfilter.label"),
		RM.getLabel("filteredition.regexfilter.label"),
		RM.getLabel("filteredition.directoryfilter.label"),
		RM.getLabel("filteredition.filesizefilter.label"),
		RM.getLabel("filteredition.filedatefilter.label"),  
		RM.getLabel("filteredition.lockedfilefilter.label"),   
		RM.getLabel("filteredition.specfilter.label"),
		RM.getLabel("filteredition.fileownerfilter.label")            
	};
	
	private static Class[] CLASSES = new Class[] {
		FilterGroup.class,
		FileExtensionArchiveFilter.class,
		RegexArchiveFilter.class,
		DirectoryArchiveFilter.class,
		FileSizeArchiveFilter.class, 
		FileDateArchiveFilter.class,
		LockedFileFilter.class,
		SpecialFileFilter.class,             
		FileOwnerArchiveFilter.class
	};

	public static List getFilters() {
		ArrayList list = new ArrayList();
		int nb = OSTool.isSystemWindows() ? 7 : 9;
		for (int i=0; i<nb; i++) {
			list.add(LABELS[i]);
		}
		return list;
	}

	public static boolean checkParameters(String params, int filterIndex) {
		// Check that the parameters are correct
		ArchiveFilter filter = buildFilter(filterIndex);

		// Parameters are mandatory
		if (
				filter == null 
				|| ((params== null || params.length() == 0) && filter.requiresParameters())
		) {
			return false;
		}

		if (filterIndex == 3 && ! FileSystemManager.exists(new File(params))) {
			return false; // todo  : refactor
		} 

		try {
			filter.acceptParameters(params);
		} catch (Throwable e) {
			return false;
		}

		return true;        
	}

	public static AbstractFilterComposite buildFilterComposite(
			int index, 
			Composite composite,
			ArchiveFilter filter, 
			FilterEditionWindow frm
	) {
		AbstractFilterComposite pnl = null;
		if (index == 0) {
			pnl = new FilterGroupComposite(composite, filter, frm);
		} else if (index == 1) {
			pnl = new FileExtensionFilterComposite(composite, filter, frm);
		} else if (index == 2){
			pnl = new RegexFilterComposite(composite, filter, frm);
		} else if (index == 3){
			pnl = new DirectoryFilterComposite(composite, filter, frm);
		} else if (index == 4){
			pnl = new FileSizeFilterComposite(composite, filter, frm);
		} else if (index == 5){
			pnl = new FileDateFilterComposite(composite, filter, frm);
		} else if (index == 7){
			pnl = new SpecFileFilterComposite(composite, filter, frm);            
		} else if (index == 8){
			pnl = new FileOwnerFilterComposite(composite, filter, frm);            
		}

		return pnl;
	}

	public static ArchiveFilter buildFilter(int filterIndex) {
		ArchiveFilter filter = null;
		Class cls = CLASSES[filterIndex];
		try {
			filter = (ArchiveFilter)cls.newInstance();
		} catch (InstantiationException e) {
			Logger.defaultLogger().error("Error instanciating index " + filterIndex, e);
		} catch (IllegalAccessException e) {
			Logger.defaultLogger().error("Error instanciating index " + filterIndex, e);
		}

		return filter;
	}

	public static int getIndex(Class currentFilter) {
		int index = DEFAULT_INDEX;
		for (int i=0; i<CLASSES.length; i++) {
			if (CLASSES[i].isAssignableFrom(currentFilter)) {
				index = i;
				break;
			}
		}
	
		return index;
	}

	public static int getIndex(ArchiveFilter currentFilter) {
		if (currentFilter == null) {
			return DEFAULT_INDEX;
		}
		return getIndex(currentFilter.getClass());
	}

	public static String getName(Class filter) {
		if (DirectoryArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.directory.label");
		} else if (RegexArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.regex.label");
		} else if (FileSizeArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.filesize.label");
		} else if (FileDateArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.filedate.label");    
		} else if (SpecialFileFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.specfile.label");      
		} else if (LockedFileFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.lockedfile.label");                      
		} else if (FileExtensionArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.fileext.label");       
		} else if (FileOwnerArchiveFilter.class.isAssignableFrom(filter)) {
			return RM.getLabel("filteredition.owner.label");                 
		} else {
			return "...";
		}
	}
}
