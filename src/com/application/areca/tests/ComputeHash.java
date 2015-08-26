package com.application.areca.tests;

import java.io.File;

import com.myJava.file.FileTool;
import com.myJava.util.Util;

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
public class ComputeHash {
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Usage : [filename] [filename] [filename] ...");
				System.exit(-1);
			}
			
			System.out.println("Hashcodes (" + FileTool.HASH_ALGORITHM + ") :");
			
			for (int i=0; i<args.length; i++) {
				File target = new File(args[i]);
				byte[] data = FileTool.getInstance().hashFileContent(target, null);
				String encoded = Util.base16Encode(data);
				
				System.out.println("" + target + " : " + encoded + " - size :" + target.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
