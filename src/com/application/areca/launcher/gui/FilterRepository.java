package com.application.areca.launcher.gui;

import java.io.File;

import com.application.areca.ArchiveFilter;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileDateArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.LinkFilter;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.launcher.gui.common.ResourceManager;
import com.myJava.file.FileSystemManager;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 4945525256658487980
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
        
        if (filterIndex == 2 && ! FileSystemManager.exists(new File(params))) {
            return false;
        } 
      
        try {
            filter.acceptParameters(params);
        } catch (Throwable e) {
            return false;
        }
        
        return true;        
    }
    
    public static ArchiveFilter buildFilter(int filterIndex) {
        ArchiveFilter filter = null;
        if (filterIndex == 0) {
            filter = new FileExtensionArchiveFilter();
        } else if (filterIndex == 1){
            filter = new RegexArchiveFilter();
        } else if (filterIndex == 2){
            filter = new DirectoryArchiveFilter();
        } else if (filterIndex == 3){
            filter = new FileSizeArchiveFilter();
        } else if (filterIndex== 4){
            filter = new FileDateArchiveFilter();
        } else if (filterIndex== 5){
            filter = new LinkFilter();
        } else if (filterIndex== 6){
            filter = new LockedFileFilter();
        }
        
        return filter;
    }
    
    public static int getIndex(ArchiveFilter currentFilter) {
        if (currentFilter == null) {
            return 0;
        }
        
    	if (DirectoryArchiveFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 2;
    	} else if (RegexArchiveFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 1;
    	} else if (FileExtensionArchiveFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 0;         
    	} else if (FileSizeArchiveFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 3;   
    	} else if (FileDateArchiveFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 4;          		
    	} else if (LinkFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 5;          		
    	} else if (LockedFileFilter.class.isAssignableFrom(currentFilter.getClass())) {
    		return 6;          		
    	}    
    	return 0;
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
        } else if (LinkFilter.class.isAssignableFrom(filter)) {
            return RM.getLabel("filteredition.link.label");      
        } else if (LockedFileFilter.class.isAssignableFrom(filter)) {
            return RM.getLabel("filteredition.lockedfile.label");                      
        } else {
            return RM.getLabel("filteredition.fileext.label");          
        }        
    }
}
