package com.myJava.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.myJava.file.iterator.FileSystemIterator;

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
public class FileDiff {
	private static OutputStream ostream = null;
	
	public static void main(String[] args) {
		try {
			if (args.length < 2) {
				log("Usage : filediff <file or directory> <file or directory> [output file]");
				return;
			}
			
			String outFile = System.getProperty("user.home") + "/diff.out";
			if (args.length >=3) {
				outFile = args[2];
			}
			int index = 0;
			File out = new File(outFile + index);
			while(FileSystemManager.exists(out)) {
				index++;
				out = new File(outFile + index);
			}
			ostream = FileSystemManager.getFileOutputStream(out);
			
			File source1 = new File(args[0]);
			File source2 = new File(args[1]);
			GregorianCalendar cal = new GregorianCalendar();
			log("" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ":" + cal.get(Calendar.MILLISECOND));
			log("Sources : " + FileSystemManager.getAbsolutePath(source1) + ", " + FileSystemManager.getAbsolutePath(source2));
			log("Output file : " + FileSystemManager.getAbsolutePath(out));
			
			FileSystemIterator iter = new FileSystemIterator(source1, false, true, true, true);
			boolean ok = true;
			while (iter.hasNext()) {
				File f1 = (File)iter.next();
				if (FileSystemManager.isFile(f1)) {
					String relativePath = FileSystemManager.getAbsolutePath(f1).substring(args[0].length());
					File f2 = new File(args[1], relativePath);
					ok &= processFiles(f1, f2);
				}
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
		} finally {
			if (ostream != null) {
				try {
					ostream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
 		}
	}
	
	public static boolean processFiles(File f1, File f2) throws Exception {
		write("Comparing " + FileSystemManager.getAbsolutePath(f1) + " and " + FileSystemManager.getAbsolutePath(f2));
		if (! FileSystemManager.exists(f1)) {
			write("   ERROR : " + FileSystemManager.getAbsolutePath(f1) + " does not exist.");
			return false;
		}
		
		if (! FileSystemManager.exists(f2)) {
			write("   ERROR : " + FileSystemManager.getAbsolutePath(f2) + " does not exist.");
			return false;
		}
		
		if (FileSystemManager.length(f1) != FileSystemManager.length(f2)) {
			write("   WARNING : Sizes are different.");
			return false;
		}
		
		InputStream in1 = null;
		InputStream in2 = null;
		boolean eq = true;
		try {
			in1 = FileSystemManager.getFileInputStream(f1);
			in2 = FileSystemManager.getFileInputStream(f2);
			
			int r1, r2;
			long position = 0;
			while ((r1 = in1.read()) != -1) {
				r2 = in2.read();
				
				if (r1 != r2) {
					eq = false;
					write("   WARNING : Difference detected at position : " + position + ".");
					break;
				}
				position++;
			}
		} finally {
			in1.close();
			in2.close();
		}
		if (eq) {
			write("   ok.");
		}
		return eq;
	}
	
	public static void write(String s) throws IOException {
		if (ostream != null) {
			ostream.write((s + "\n").getBytes());
		}
	}
	
	public static void log(String s) throws IOException {
		System.out.println(s);
		write(s);
	}
}
