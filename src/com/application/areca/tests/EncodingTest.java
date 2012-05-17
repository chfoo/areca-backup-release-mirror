package com.application.areca.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class EncodingTest {
	public static void main(String[] args) {
		try {
			System.out.println("file.encoding="+System.getProperty("file.encoding"));
			System.out.println("sun.jnu.encoding="+System.getProperty("sun.jnu.encoding"));
			String filename = "c:\\users\\olivier\\Desktop\\FilenameWithéoràorü.txt";
			FileWriter writer = new FileWriter(new File(filename));
			writer.write(new char[] {'a', 'b', 'c', 'd', 'e'});
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
