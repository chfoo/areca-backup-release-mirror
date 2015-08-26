package com.myJava.util.xml;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.w3c.dom.Node;

import com.myJava.encryption.EncryptionUtil;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
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
public class XMLTool {
	private static String PASSWORD_ENCODING_SUFFIX = "_e";
	private static Cipher ENCRYPTION_CIPHER;
	private static Cipher DECRYPTION_CIPHER;

	static {
		ENCRYPTION_CIPHER = buildNewCipher(Cipher.ENCRYPT_MODE);
		DECRYPTION_CIPHER = buildNewCipher(Cipher.DECRYPT_MODE);
	}

	private static Cipher buildNewCipher(int mode) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(mode, EncryptionUtil.buildKeyFromPassphrase("-- ! Gl0ub1boulg4 ! --", 16, "AES"));                

			return cipher;
		} catch (InvalidKeyException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		} catch (NoSuchAlgorithmException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		} catch (NoSuchPaddingException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		}
	}

	public static String encode(long orig) {
		return encode("" + orig);
	}

	public static String encode(int orig) {
		return encode("" + orig);
	}

	public static String encode(boolean orig) {
		return encode("" + orig);
	}

	public static String encode(String orig) {
		String ret = orig;

		ret = Util.replace(ret, "&", "&amp;");
		ret = Util.replace(ret, "\n", "&#xA;");
		ret = Util.replace(ret, "<", "&lt;");
		ret = Util.replace(ret, ">", "&gt;");  
		ret = Util.replace(ret, "\"", "&quot;");
		ret = Util.replace(ret, "'", "&apos;");            

		return "\"" + ret + "\"";
	}

	public static String getHeader(String encoding) {
		return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
	}
	
	public static String encodeProperty(String property, double value) {
		return encodeProperty(property, "" + value);
	}

	public static String encodeProperty(String property, int value) {
		return encodeProperty(property, "" + value);
	}

	public static String encodeProperty(String property, boolean value) {
		return encodeProperty(property, "" + value);
	}

	public static String encodeProperty(String property, long value) {
		return encodeProperty(property, "" + value);
	}

	public static String encodeProperty(String property, String value) {
		StringBuffer sb = new StringBuffer();
		if (value != null) {
			sb.append(" ").append(property).append("=").append(encode(value));
		}
		return sb.toString();
	}

	public static String readNonNullableNode(Node data, String tag) throws AdapterException {
		Node node = data.getAttributes().getNamedItem(tag);
		if (node == null) {
			throw new AdapterException("Invalid XML content : missing '" + tag + "' tag.");
		} else {
			return node.getNodeValue();
		}
	}
	
	public static long readNonNullableLong(Node data, String tag) throws AdapterException {
		String str = readNonNullableNode(data, tag);
		return Long.parseLong(str);
	}
	
	public static int readNonNullableInt(Node data, String tag) throws AdapterException {
		String str = readNonNullableNode(data, tag);
		return Integer.parseInt(str);
	}

	public static boolean readNonNullableBoolean(Node data, String tag) throws AdapterException {
		String str = readNonNullableNode(data, tag);
		return Boolean.parseBoolean(str);
	}
	
	public static String readNullableNode(Node data, String tag) {
		Node node = data.getAttributes().getNamedItem(tag);
		if (node == null) {
			return null;
		} else {
			return node.getNodeValue();
		}
	}
	
	public static String readNullableNode(Node data, String tag, String defaultValue) {
		String val = readNullableNode(data, tag);
		if (val == null) {
			return defaultValue;
		} else {
			return val;
		}
	}
	
	public static int readNullableNode(Node data, String tag, int defaultValue) {
		String val = readNullableNode(data, tag);
		if (val == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(val);
		}
	}
	
	public static boolean readNullableNode(Node data, String tag, boolean defaultValue) {
		String val = readNullableNode(data, tag);
		if (val == null) {
			return defaultValue;
		} else {
			return val.trim().equalsIgnoreCase("true");
		}
	}
	
	public static long readNullableNode(Node data, String tag, long defaultValue) {
		String val = readNullableNode(data, tag);
		if (val == null) {
			return defaultValue;
		} else {
			return Long.parseLong(val);
		}
	}

	private static String encrypt(String value) {
		if (value == null) {
			return "";
		}

		try {
			byte[] bytes = ENCRYPTION_CIPHER.doFinal(value.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<bytes.length; i++) {
				String str = Integer.toHexString((int)bytes[i] + 128);
				if (str.length() == 1) {
					sb.append("0");
				}
				sb.append(str);
			}

			return sb.toString();
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException("Error while encrypting [" + value + "]", e);
		}
	}

	private static String decrypt(String value) {
		if (value == null || value.trim().length() == 0) {
			return null;
		}

		try {
			int nb = value.length() / 2;
			byte[] values = new byte[nb];

			for (int i=0; i<nb; i++) {
				String str = value.substring(2*i, 2*i+2);

				int iValue = Integer.parseInt(str, 16);
				values[i] = (byte)(iValue - 128);
			}

			byte[] decrypted = DECRYPTION_CIPHER.doFinal(values);
			return new String(decrypted, "UTF-8");
		} catch (Exception e) {
			Logger.defaultLogger().error(e);
			throw new IllegalArgumentException("Error while decrypting [" + value + "]", e);
		}
	}

	public static String encodePassword(String basePropertyName, String value) {
		String encodedValue = encrypt(value);
		return encodeProperty(basePropertyName + PASSWORD_ENCODING_SUFFIX, encodedValue);
	}
	
	public static void writeOpeningTag(String tag, StringBuffer sb) {
		sb.append("\n<").append(tag).append(">");
	}
	
	public static void writeClosingTag(String tag, StringBuffer sb) {
		sb.append("\n</").append(tag).append(">");
	}

	public static String extractPassword(String basePropertyName, Node node) {
		Node encodedNode = node.getAttributes().getNamedItem(basePropertyName + PASSWORD_ENCODING_SUFFIX);
		if (encodedNode == null) {
			Node decodedNode = node.getAttributes().getNamedItem(basePropertyName);	
			if (decodedNode == null) {
				return null;
			} else {
				return decodedNode.getNodeValue();
			}
		} else {
			return decrypt(encodedNode.getNodeValue());
		}
	}
}
