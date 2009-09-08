package com.application.areca.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;

import com.application.areca.TargetGroup;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.myJava.file.FileTool;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class TestEnvironment {

	public static String TEST_ROOT = "/home/olivier/tmp/arecatest/";
	public static String CONFIG_ROOT = TEST_ROOT + "config/";
	public static String SRC_ROOT = TEST_ROOT + "src/";
	public static String SRC1 = SRC_ROOT + "dir/";
	public static String SRC2 = SRC_ROOT + "file_a";
	public static String SRC3 = SRC_ROOT + "file_b";
	
	public static void createTestEnvironment() throws Exception {
		FileTool.getInstance().createDir(new File(SRC_ROOT));
		createFile(new File(SRC2), 56);
		createFile(new File(SRC2), 0);
		createFile(new File(SRC1, "f0"), 3621);
		File subdir1 = new File(SRC1, "subdir1");
		createFile(new File(subdir1, "f1"), 521);
		createFile(new File(subdir1, "f2"), 0);
		createFile(new File(subdir1, "f3"), 4562);
		File subdir2 = new File(SRC1, "subdir2");
		createFile(new File(subdir2, "f4"), 1452);
	}
	
	public static void clean() throws Exception {
		FileTool.getInstance().delete(new File(TEST_ROOT), true);
	}
	
	private static void createFile(File f, long size) throws Exception {
		FileTool.getInstance().createDir(f.getParentFile());
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		String c = "";
		for (int i=0; i<size; i++) {
			c += i%10;
		}
		writer.write(c);
		writer.close();
	}
	
	public static TargetGroup createTargetGroup() {
		TargetGroup group = new TargetGroup(new File(CONFIG_ROOT));
		FileSystemTarget target = new FileSystemTarget();
		target.setGroup(group);
		target.setComments("");
		target.setCreateSecurityCopyOnBackup(true);
		FilterGroup filters = new FilterGroup();
		filters.setAnd(true);
		filters.setLogicalNot(false);
		LockedFileFilter filter1 = new LockedFileFilter();
		filter1.setLogicalNot(true);
		filters.addFilter(filter1);
		FileExtensionArchiveFilter filter2 = new FileExtensionArchiveFilter();
		filter2.addExtension("tmp");
		filter2.addExtension("toto");
		filter2.setLogicalNot(true);
		filters.addFilter(filter2);
		target.setFilterGroup(filters);
		target.setFollowSubdirectories(true);
		target.setId(1);
		HashSet sources = new HashSet();
		sources.add(new File(SRC1));
		sources.add(new File(SRC2));
		sources.add(new File(SRC3));
		target.setSources(sources);
		
		IncrementalDirectoryMedium medium = new IncrementalDirectoryMedium();
		target.setMedium(medium, true);
		return group;
	}
}
