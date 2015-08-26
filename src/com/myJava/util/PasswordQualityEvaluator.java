package com.myJava.util;


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
public class PasswordQualityEvaluator {

	public static double evaluate(String password) {
		double base = evaluateLengthCriteria(password);
		double bonus = computeBonusRatio(password);
		
		if (weakPattern(password)) {
			base = base * 0.85;
		}
		
		return 1.0 - 1.0/(1.0 + 0.05 * base * bonus);
	}
	
	/**
	 * Long passwords get a bonus
	 */
	private static double evaluateLengthCriteria(String password) {
		return password.length() * 0.25 + countCharacterChanges(password) * 0.75;
	}
	
	/**
	 * Identical characters get a penalty
	 */
	private static double countCharacterChanges(String password) {
		char pc = 0;
		char c = 0;
		double count = 0;
		for (int i=0; i< password.length(); i++) {
			pc = c;
			c = password.charAt(i);
			
			if (pc != c) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Mixed case, numeric and special characters get a bonus 
	 */
	private static double computeBonusRatio(String password) {
		double nbUpper = 0;
		double nbLower = 0;
		double nbNumeric = 0;
		double nbOther = 0;
		double l = password.length();

		if (l < 4) {
			return 0.5;
		}
		
		for (int i=0; i<l; i++) {
			char c = password.charAt(i);
			if (isUpper(c)){
				nbUpper++;
			} else if (isLower(c)) {
				nbLower++;
			} else if (isNumeric(c)) {
				nbNumeric++;
			} else {
				nbOther++;
			}
		}
		
		double tgLength = 2;
		return
		   -0.5
			+ 1.5 * Math.min(1, nbUpper / tgLength)
			+ 1.5 * Math.min(1, nbLower / tgLength)
			+ 1.5 * Math.min(1, nbNumeric / tgLength)
			+ 1.5 * Math.min(1, nbOther / tgLength);			
	}
	
	/**
	 * "classic" patterns get a penalty
	 */
	private static boolean weakPattern(String password) {
		// Length check
		boolean weak = password.length() <= 6;
		
		// Upper case letter at first position only
		if (! weak) {
			if (isUpper(password.charAt(0))) {
				weak = true;
				for (int i=1; i<password.length(); i++) {
					if (isUpper(password.charAt(i))) {
						weak = false;
						break;
					}
				}
			}
		}
		
		// Numbers at last position only
		if (! weak) {
			boolean numbersAtEnd = false;
			int i;
			for (i=password.length()-1; i>=0 && isNumeric(password.charAt(i)); i--) {
				numbersAtEnd = true;
			}
			if (numbersAtEnd) {
				boolean numbersInMiddle = false;
				for (int j=i; j>=0; j--) {
					if (isNumeric(password.charAt(j))) {
						numbersInMiddle = true;
					}
				}
				weak = ! numbersInMiddle;
			}
		}
		return weak;
	}

	private static boolean isUpper(char c) {
		return c >= 65 && c <= 90;
	}

	private static boolean isLower(char c) {
		return c >= 97 && c <= 122;
	}

	private static boolean isNumeric(char c) {
		return c >= 48 && c <= 57;
	}
}
