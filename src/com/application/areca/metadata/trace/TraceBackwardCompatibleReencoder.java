package com.application.areca.metadata.trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.application.areca.metadata.MetadataConstants;
import com.myJava.file.FileTool;
import com.myJava.util.Util;

/**
 * Old : 
 * <BR>File : 		[NAME]#-#[SIZE]-[DATE]-[PERMS]
 * <BR>Directory : 	!D![NAME]#-#[DATE]-[PERMS]
 * <BR>SymLink : 	!S![NAME]#-#[d/f][PATH]
 * <BR>
 * <BR>New : 
 * <BR>File : 		f[NAME];[SIZE];[DATE];[PERMS]
 * <BR>Directory : 	d[NAME];[DATE];[PERMS]
 * <BR>SymLink : 	s[NAME];[d/f][PATH]
 * <BR>'@' are re-encoded as '@@'
 * <BR>';' are re-encoded as '@P'
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5323430991191230653
 */
 
 /*
 Copyright 2005-2007, Olivier PETRUCCI.
 
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
public class TraceBackwardCompatibleReencoder implements MetadataConstants {	
	public static String reencode(String in) {
		// '@' => '@@'
		String encoded = Util.replace(in, SPEC_CHAR, SC_AROBASE);
		
		// ';' => '@P'
		encoded = Util.replace(encoded, SEPARATOR, SC_SEMICOLON);
		
		boolean isSymLink = false;
		if (encoded.startsWith(DEPRECATED_DIRECTORY_MARKER)) {
			// '!D!' => 'd'
			encoded = T_DIR + encoded.substring(DEPRECATED_DIRECTORY_MARKER.length());
		} else if (encoded.startsWith(DEPRECATED_SYMLINK_MARKER)) {
			// '!S!' => 's'
			encoded = T_SYMLINK + encoded.substring(DEPRECATED_SYMLINK_MARKER.length());
			isSymLink = true;
		} else {
			// default => 'f'
			encoded = T_FILE + encoded;
		}
		
		int idx = encoded.lastIndexOf(DEPRECATED_SEP);
		if (idx == -1) {
			throw new IllegalStateException("Unable to parse trace : [" + in + "]");
		}
		
		String firstPart = encoded.substring(0, idx);
		String secondPart = encoded.substring(DEPRECATED_SEP.length() + idx);
		if (! isSymLink) {
			// '-' => ';'
			secondPart = secondPart.replace(DEPRECATED_INTERNAL_SEP, SEPARATOR_CHAR);
			secondPart = Util.replace(secondPart, ";;", ";-");
		}
		
		// '#-#' => ';'
		encoded = firstPart + SEPARATOR + secondPart;
				
		return encoded;
	}
	
	public static void main(String[] args) {
		try {
			File f = new File("/home/olivier/Bureau/trace");
			String[] rows = FileTool.getInstance().getInputStreamRows(new FileInputStream(f), "UTF-8", true);
			for (int i=2; i<rows.length; i++) {
				System.out.println(reencode(rows[i]));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
