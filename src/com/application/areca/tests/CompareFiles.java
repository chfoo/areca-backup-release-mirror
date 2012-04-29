package com.application.areca.tests;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.myJava.file.FileSystemManager;

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
