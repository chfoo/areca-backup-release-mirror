package com.application.areca.tests;

import java.io.File;

import com.myJava.file.FileTool;
import com.myJava.util.Util;

/**
 * 
 * @author Olivier
 *
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
