package com.application.areca.tests;

import com.myJava.util.Util;

/**
 * 
 * @author Olivier
 *
 */
public class DecodeBase64 {
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Usage : [base64]");
			} else {
				System.out.println("Argument : " + args[0]);
				
				String reencoded = Util.base16Encode(Util.base64Decode(args[0]));
				
				System.out.println("Base 16 : " + reencoded);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
