package com.myJava.file.archive.zip64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.myJava.util.log.Logger;

/**
 * Zip utility class for String encoding / decoding
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
public class ZipStringEncoder {

	public static byte[] encode(String s, Charset charset) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(baos, charset);
			writer.write(s);
			writer.flush();
		} catch (IOException e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				Logger.defaultLogger().error(e);
			}
		}
		byte[] ret = baos.toByteArray();
		checkEncoding(ret, s, charset);
		return ret;
	}
	
	private static void checkEncoding(byte[] encoded, String decoded, Charset charset) {
		String fromEncoded = decode(encoded, 0, encoded.length, charset);
		if (! fromEncoded.equals(decoded)) {
			Logger.defaultLogger().warn("Caution : unable to encode \"" + decoded + "\" properly using charset \"" + charset.name() + "\". It is advisable to use \"UTF-8\" as encoding for your zip archives.");
		}
	}
    
    public static String decode(byte[] b, int off, int len, Charset charset) {
        byte[] bytes = new byte[len];
        for (int i=0; i<len; i++) {
            bytes[i] = b[i+off];
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
       
        InputStreamReader reader = new InputStreamReader(bais, charset);
        StringBuffer ret = new StringBuffer();
        int read;
        char[] buffer = new char[100];
        try {
        	while((read = reader.read(buffer)) != -1) {
        		ret.append(buffer, 0, read);
        	}
        } catch (IOException e) {
            Logger.defaultLogger().error(e);
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Logger.defaultLogger().error(e);
            }
        }
        return ret.toString();
    }
}
