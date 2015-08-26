package com.application.areca.tests;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.myJava.file.FileTool;

/**
 * 
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
public class TestCopy {
	private static byte[] TO_WRITE = new byte[] {0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9};
	private static long ITERATE = (long)(1024*1024*0.5);
	private static long NR_FILE = 30;
	private static int BUFFER_SIZE = 100 * 1024;

	public static void main(String[] args) {
		String asource = args[0];
		String adest = args[1];

		File source = new File(asource, "areca_test_src");
		File dest = new File(adest, "areca_test_dest");
		
		try {
			FileTool.getInstance().createDir(source);
			FileTool.getInstance().createDir(dest);
			
			// STEP 0 : create source data
			long c0 = System.currentTimeMillis();
			for (int fi = 0; fi < NR_FILE; fi++) {
				System.out.println("Creating source file #" + fi);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(source, ""+fi)), BUFFER_SIZE);
				for (int i=0; i<ITERATE; i++) {
					bos.write(TO_WRITE);
				}
				bos.close();
			}
			long c1 = System.currentTimeMillis();
			System.out.println("Source data created in : " + (c1 - c0) + " ms");
			
			double rateCreate = 1000*NR_FILE*TO_WRITE.length*ITERATE/(c1-c0)/1024/1024;
			System.out.println("Create (Mb / sec) : " + rateCreate);
			
			// STEP 1 : copy source data to destination
			System.out.println("Copying files ...");
			FileTool.getInstance().copy(source, dest);
			long c2 = System.currentTimeMillis();
			System.out.println("Data copied in : " + (c2 - c1) + " ms");
			
			double rateCp = 1000*NR_FILE*TO_WRITE.length*ITERATE/(c2-c1)/1024/1024;
			System.out.println("Copy (Mb / sec) : " + rateCp);
			
			// STEP 2 : clean
			System.out.println("Cleaning files ...");
			FileTool.getInstance().delete(source);
			FileTool.getInstance().delete(dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
