package com.myJava.file;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.file.metadata.posix.PosixMetaDataImpl;
import com.myJava.file.metadata.posix.jni.wrapper.FileAccessWrapper;

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
public class MetaDataDiff {
	public static void main(String[] args) {
		try {
			if (args.length < 2) {
				log("Usage : MetaDataDiff <file or directory> <file or directory>");
				return;
			}
			
			File source1 = new File(args[0]);
			File source2 = new File(args[1]);
			GregorianCalendar cal = new GregorianCalendar();
			log("" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ":" + cal.get(Calendar.MILLISECOND));
			log("Sources : " + FileSystemManager.getAbsolutePath(source1) + ", " + FileSystemManager.getAbsolutePath(source2));
			
			FileSystemIterator iter = new FileSystemIterator(source1, false, true, true, true);
			boolean ok = true;
			while (iter.hasNext()) {
				File f1 = (File)iter.next();
				String relativePath = FileSystemManager.getAbsolutePath(f1).substring(args[0].length());
				File f2 = new File(args[1], relativePath);
				ok &= processFiles(f1, f2);
			}
			GregorianCalendar cal2 = new GregorianCalendar();
			log("" + cal2.get(Calendar.DAY_OF_MONTH) + " " + cal2.get(Calendar.HOUR_OF_DAY) + ":" + cal2.get(Calendar.MINUTE) + ":" + cal2.get(Calendar.SECOND) + ":" + cal2.get(Calendar.MILLISECOND));
			log("Check completed.");
			if (ok) {
				log("No error detected");
			} else {
				log("Some errors were detected");				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean processFiles(File f1, File f2) throws Exception {
		log("");
		log("");		
		log("");
		log("Comparing " + FileSystemManager.getAbsolutePath(f1) + " and " + FileSystemManager.getAbsolutePath(f2));
		PosixMetaDataImpl d1 = (PosixMetaDataImpl)FileSystemManager.getMetaData(f1, false);
		if (FileAccessWrapper.isA(d1.getMode(), FileAccessWrapper.TYPE_LINK)) {
			log("   LINK");
		} else if (FileAccessWrapper.isA(d1.getMode(), FileAccessWrapper.TYPE_FILE)) {
			log("   FILE");
		} else if (FileAccessWrapper.isA(d1.getMode(), FileAccessWrapper.TYPE_DIRECTORY)) {
			log("   DIRECTORY");
		} else {
			log("   !! UNKNOWN !!");
		}
		log("   " + d1.toString());
		
		if (! FileSystemManager.exists(f1)) {
			log("   ERROR : " + FileSystemManager.getAbsolutePath(f1) + " does not exist.");
			return false;
		}
		
		if (! FileSystemManager.exists(f2)) {
			log("   ERROR : " + FileSystemManager.getAbsolutePath(f2) + " does not exist.");
			return false;
		}
		
		if (FileSystemManager.length(f1) != FileSystemManager.length(f2)) {
			log("   WARNING : Sizes are different.");
			return false;
		}
		
		if (FileSystemManager.lastModified(f1) != FileSystemManager.lastModified(f2)) {
			log("   WARNING : Dates are different.");
			return false;
		}
		

		PosixMetaDataImpl d2 = (PosixMetaDataImpl)FileSystemManager.getMetaData(f2, false);
		
		if (! d1.equals(d2)) {
			log("   " + d2.toString());
			log("   WARNING : MetaData are different.");
			return false;
		}
		
		log("   They are identical.");
		
		return true;
	}
	
	public static void log(String s) throws IOException {
		System.out.println(s);
	}
}
