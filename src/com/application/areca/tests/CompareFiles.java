package com.application.areca.tests;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.myJava.file.FileSystemManager;

/**
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
public class CompareFiles {

	public static void main(String[] args) {
		try {
			if (args.length <= 2) {
				System.out.println("Usage : [filename1] [filename2] [outfile]");
				System.exit(-1);
			}
			
			File f1 = new File(args[0]);
			File f2 = new File(args[1]);
			File fout = new File(args[2]);
			
			InputStream i1 = FileSystemManager.getFileInputStream(f1);
			InputStream i2 = FileSystemManager.getFileInputStream(f2);
			OutputStreamWriter writer = new OutputStreamWriter(FileSystemManager.getFileOutputStream(fout));
			
			int b1 = -1, b2 = -1;
			boolean eof1 = false;
			boolean eof2 = false;
			
			while (!eof1 || ! eof2) {
				
				if (! eof1) {
					b1 = i1.read();
					
					if (b1 == -1) {
						eof1 = true;
					}
				}
				
				if (! eof2) {
					b2 = i2.read();
					
					if (b2 == -1) {
						eof2 = true;
					}
				}

				writer.write("\n" + (eof1 ? "EOF":""+b1) + " " + (eof2 ? "EOF":""+b2));
			}
			
			i1.close();
			i2.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
