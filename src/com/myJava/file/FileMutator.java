package com.myJava.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.myJava.file.iterator.FileSystemIterator;
import com.myJava.util.Util;

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
public class FileMutator {
	private static OutputStream ostream = null;
	
	public static void main(String[] args) {
		try {
			if (args.length < 2) {
				log("Usage : filediff <source file or directory> <destination file or directory> [output file]");
				return;
			}
			
			String outFile = System.getProperty("user.home") + "/mutation.out";
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
			
			File source = new File(args[0]);
			File dest = new File(args[1]);
			GregorianCalendar cal = new GregorianCalendar();
			log("" + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + ":" + cal.get(Calendar.MILLISECOND));
			log("Source : " + FileSystemManager.getAbsolutePath(source));
			log("Destination : " + FileSystemManager.getAbsolutePath(dest));
			log("Output file : " + FileSystemManager.getAbsolutePath(out));
			
			FileSystemIterator iter = new FileSystemIterator(source, false, true,true, true);
			while (iter.hasNext()) {
				File f1 = (File)iter.next();
				if (FileSystemManager.isFile(f1)) {
					String relativePath = FileSystemManager.getAbsolutePath(f1).substring(args[0].length());
					File f2 = new File(args[1], relativePath);
					processFiles(f1, f2);
				}
			}
			GregorianCalendar cal2 = new GregorianCalendar();
			log("" + cal2.get(Calendar.DAY_OF_MONTH) + " " + cal2.get(Calendar.HOUR_OF_DAY) + ":" + cal2.get(Calendar.MINUTE) + ":" + cal2.get(Calendar.SECOND) + ":" + cal2.get(Calendar.MILLISECOND));
			log("Mutation completed.");
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
	
	public static void processFiles(File source, File dest) throws Exception {
		write("Mutating " + FileSystemManager.getAbsolutePath(source) + " to " + FileSystemManager.getAbsolutePath(dest));
		if (! FileSystemManager.exists(source)) {
			write("   ERROR : " + FileSystemManager.getAbsolutePath(source) + " does not exist.");
			return;
		}
		
		FileTool.getInstance().createDir(FileSystemManager.getParentFile(dest));
		long length = FileSystemManager.length(source);
		byte[] buff = new byte[1024];
		double proba = 4.0 / length * buff.length;
		
		InputStream in = null;
		OutputStream out = null;
		boolean eq = true;
		try {
			in = FileSystemManager.getFileInputStream(source);
			out = FileSystemManager.getFileOutputStream(dest);
			
			int l;
			long position = 0;
			while ((l = in.read(buff)) != -1) {
				out.write(mutate(buff, l, proba, position++));
			}
		} finally {
			in.close();
			out.close();
		}
		if (eq) {
			write("   Mutation completed.");
		}
	}
	
	private static byte[] mutate(byte[] in, int len, double probability, long position) throws IOException {
		byte[] ret = in;
		if (Math.abs(Util.getRnd()) < probability) {
			if (len > 2) {
				write("   Mutating byte at position " + position);
				double rnd = Math.abs(Util.getRnd());
				int l = (int)(Math.abs(Util.getRnd()) * len);
				if (rnd < 0.3333333) {
					write("   " + l + " bytes will be removed");
					len -= l;
				} else if (rnd > 0.66666666) {
					write("   " + l + " byte will be added");
					len += l;
				}
				ret = mutate(in, len);
			}
		}
		return ret;	
	}
	
	private static byte[] mutate(byte[] data, int length) {
		byte[] ret;
		if (length == data.length) {
			ret = data;
		} else if (length > data.length) {
			ret = new byte[length];
			byte last = data[data.length - 1];
			for (int i=0; i<data.length; i++) {
				ret[i] = data[i];
			}
			for (int i=data.length; i<length; i++) {
				ret[i] = last;
			}
		} else {
			ret = new byte[length];
			for (int i=0; i<ret.length; i++) {
				ret[i] = data[i];
			}

		}
		
		if (length >= 2) {
			ret[1] = (byte)(ret[1] > 0 ? ret[1] - 1 : ret[1] + 1);
		} else if (length == 1) {
			ret[0] = (byte)(ret[0] > 0 ? ret[0] - 1 : ret[0] + 1);
		}
		
		return ret;
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
