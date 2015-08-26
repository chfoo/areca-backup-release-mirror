/**
 * Utility class ....
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

package com.myJava.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;

import com.myJava.file.FileList.FileListIterator;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;

public abstract class Util {
    
    private static Short[] POOLED_SHORTS;
    private static final Random rndGenerator = new Random(System.currentTimeMillis());

    static {
        POOLED_SHORTS = new Short[21];
        for (short i = -10; i<POOLED_SHORTS.length - 10; i++) {
            POOLED_SHORTS[i+10] = new Short(i);
        }
    }
    
    public static Short buildOptimizedShort(short s) {
        if (s < -10 || s > 10) {
            return new Short(s);
        } else {
            return POOLED_SHORTS[s+10];
        }
    }
    
    /**
     * Replace all occurences of "toReplace" in "initialString" by "replacement"
     */
    public static String replace(String initialString, String toReplace, String replacement) {

        if (initialString == null) {
            return null;
        }

        if (toReplace != null && !(toReplace.equals("")) && replacement != null) {
            StringBuffer stb = new StringBuffer();
            int index = initialString.indexOf(toReplace);
            int oldIndex = 0;

            while (index != -1) {
                stb.append(initialString.substring(oldIndex, index));
                stb.append(replacement);
                oldIndex = index + toReplace.length();
                index = initialString.indexOf(toReplace, index + toReplace.length());
            }

            stb.append(initialString.substring(oldIndex));
            return stb.toString();
        } else {
            return initialString;
        }
    }

    public static int count(String chaine, String str) {
        int index = chaine.indexOf(str);
        int nbOcc = 0;

        while (index != -1) {
            nbOcc ++;
            index = chaine.indexOf(str, index+str.length());
        }
        return nbOcc;
    }
    
    public static int count(String chaine, int startIndex, char c) {
        if (chaine == null) {
            return 0;
        }
        
        int l = chaine.length();
        int cpt = 0;
        for (int i=startIndex; i<l; i++) {
            if (chaine.charAt(i) == c) {
                cpt++;
            }
        }
        
        return cpt;
    }
    
    public static void logAllThreadInformations() {
    	int nb = Thread.activeCount();
    	Thread[] th = new Thread[nb+100];
    	Thread.enumerate(th);
    	boolean stop = false;
    	Logger.defaultLogger().info("Thread information : " + nb + " threads.");
    	for (int i=0; i<th.length && ! stop; i++) {
    		if (th[i] == null) {
    			stop = true;
    		} else {
    			String header = "Thread " + i + " (" + th[i].getName() + " / " + th[i].getId() + ")";
    			logThreadInformations(header, th[i]);
    		}
    	}
    }
    
    public static void logThreadInformations() {
    	logThreadInformations(null);	
    }
    
	public static void main(String[] args) {
		byte[] data = new byte[128];
		for (int i=0; i<data.length; i++) {
			data[i] = (byte)(Math.random()*128.);
		}
		String enc = base64Encode(data);
		System.out.println(enc);
		System.out.println(base64Encode(base64Decode(enc)));
	}
    
    public static void logThreadInformations(String header) {
    	logThreadInformations(header, Thread.currentThread());
    }
    
    public static void logThreadInformations(String header, Thread thread) {
    	try {
    		StackTraceElement[] elements = thread.getStackTrace();
    		String thd;
    		if (header != null) {
    			thd = header + "\n";
    		} else {
    			thd = "";
    		}
    		thd += "Thread dump :";
    		if (elements != null) {
    			for (int i=0; i<elements.length; i++) {
    				thd += "\n";
    				if (i != 0) {
    					thd += "at ";
    				}
    				thd += elements[i].getClassName() + "." + elements[i].getMethodName() + " (Line " + elements[i].getLineNumber() + ")";
    			}
    		}
    		Logger.defaultLogger().fine(thd);
    	} catch (Throwable e) {
    		Logger.defaultLogger().warn(e.getMessage());
    	}
    }

    /**
     * Compute a random double between -1 and 1
     */
    public static double getRnd() {
        return ((rndGenerator.nextDouble() - .5) * 2.);
    }
    
    /**
     * Compute a random double between 0 and 1
     */
    public static double getRnd01() {
        return rndGenerator.nextDouble();
    }
    
    public static int getRndInt(int lowerBoundIncluded, int upperBoundExcluded) {
    	return (int)(lowerBoundIncluded + (rndGenerator.nextDouble() * (upperBoundExcluded - lowerBoundIncluded)));
    }

    /**
     * Compute a random long
     */
    public static long getRndLong() {
        return (rndGenerator.nextLong());
    }
    
    public static String[] split(String data, int chunkSize) {
    	int size = (int)Math.ceil(((double)data.length()) / ((double)chunkSize));
    	String[] ret = new String[size];
    	
    	for (int i=0; i<size; i++) {
    		ret[i] = data.substring(chunkSize*i, Math.min(chunkSize*(i+1), data.length()));
    	}
    	
    	return ret;
    }
    
    /**
     * Return a base 64 String representation of the byte[] passed as argument
     */
    public static String base64Encode(byte[] data) {
    	if (data == null) {
    		return "<null>";
    	} else {
    		return Base64.encodeBase64String(data).trim().replace("\r", "").replace("\n", "");
    	}
    }
    
    /**
     * Decode the base 64 representation passed as argument and return the corresponding byte[]
     */
    public static byte[] base64Decode(String data) {
    	if (data == null || data.trim().equals("<null>")) {
    		return null;
    	} else {
    		return Base64.decodeBase64(data);		
    	}
    }
    
    /**
     * Return a base 16 String representation of the byte[] passed as argument
     */
    public static String base16Encode(byte[] data) {
    	if (data == null) {
    		return null;
    	}
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<data.length; i++) {
            int d = data[i];
            String s = Integer.toString(d+128, 16);
            if (s.length() < 2) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }
    
    /**
     * Decode the base 16 representation passed as argument and return the corresponding byte[]
     */
    public static byte[] base16Decode(String input) {
    	if (input == null) {
    		return null;
    	}
        if (input.length()%2 == 1) {
            throw new IllegalArgumentException("The string's length must be even. Current length = " + input.length() + " characters. (" + input + ")");
        }
        
        byte[] b = new byte[(int)(input.length() / 2)];
        for (int i=0; i<b.length; i++) {
            b[i] = (byte)(Integer.parseInt(input.substring(2*i, 2*i+2), 16) - 128);            
        }
        return b;
    }

    /**
     * Return the String between the second and fourth arguments
     */
    public static String subString(String orig, String balise1, int offset, String balise2) {
        if (orig != null) {
            boolean found = true;
            int i1 = 0;
            if (balise1 != null) {
                int tmp = orig.indexOf(balise1);
                if (tmp == -1) {
                    found = false;
                }
                i1 = tmp + balise1.length() + offset;
            }

            int i2 = 0;
            if (balise2 != null && found) {
                i2 = orig.indexOf(balise2, i1);
                if (i2 == -1) {
                    found = false;
                }
            }

            if (found) {
                return orig.substring(i1, i2);
            } else {
                Logger.defaultLogger().warn("Element [" + balise1 + "], [" + offset + "], [" + balise2 + "] non trouv� dans [" + orig + "]", "Utilitaire.subString()");
                return "";
            }
        } else {
            return null;
        }
    }

    /**
     * Split a String in an array of Strings
     */
    public static Vector split(String orig, String pattern) {

        if (orig == null || pattern == null) {
            return null;
        }

        int currentIndex = orig.indexOf(pattern);
        int lastIndex = 0;
        Vector elements = new Vector();

        while (currentIndex != -1) {
            elements.addElement(orig.substring(lastIndex, currentIndex));

            lastIndex = currentIndex+pattern.length();
            currentIndex = orig.indexOf(pattern, lastIndex);
        }

        elements.addElement(orig.substring(lastIndex, orig.length()));

        return elements;
    }

    /**
     * Generic left trim
     */
    public static String gLTrim(String str, char c) {
        if (str == null) {
            return null;
        } else {
            int l = str.length();
            int i;
            for (i=0; (i<l && str.charAt(i) == c); i++) {
            }

            if (i == l) {
                return "";
            } else {
                return str.substring(i);
            }
        }
    }

    /**
     * Extract the classe's short name
     */
    public static String getClassName(String path) {
        if (path == null) {
            return null;
        }
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return path;
        } else {
            return path.substring(index+1);
        }
    }

    /**
     * Write the whole content of a Stream
     */
    public static void logStreamContent(InputStream is) {
        String content = "";
        try {
            Logger.defaultLogger().info("----------------- DEBUT CONTENU -----------------");
            while(true) {
                int v = is.read();
                if (v == -1) {
                    break;
                } else {
                    content += (char)v;
                }
            }
        } catch (IOException e) {
            Logger.defaultLogger().error("Erreur durant la lecture du flux", e, "Utilitaire.logStreamContent");
        } finally {
            Logger.defaultLogger().info(content);
            Logger.defaultLogger().info("----------------- FIN CONTENU -----------------");
        }
    }

    /**
     * Return the keys of the HashTable as a single String.
     * <BR>The separator passed as argument is used.
     * <BR>For instance, [1=toto 2=tutu 6=titi 9=tata] will give "1,2,6,9" if the separator
     * is ",".
     */
    public static String getContentList(Hashtable values, String separator) {
        StringBuffer sb = new StringBuffer();
        Enumeration keys = values.keys();
        while(keys.hasMoreElements()) {
            sb.append(separator);
            sb.append(keys.nextElement().toString());
        }

        if (sb.length() == 0) {
            return "";
        } else {
            return sb.toString().substring(separator.length());
        }
    }
    
    /**
     * Return the keys of the HashTable as a single String.
     * <BR>Same as getContentList(values, ",")
     */
    public static String getContentList(Hashtable values) {
        return Util.getContentList(values, ",");
    } 
    
    /**
     * Adjust the String's size to the requested size.
     * <BR>- If the size of the first argument is inferior to the second argument, the third argument is
     * added until it reaches the requested size
     * <BR>- If the size of the first argument is superior to the second argument, it is truncated. 
     * <BR>
     * <BR>The fourth argument tells whether it is the left or right part of the first argument which is kept.
     */
    public static String adjustSize(String s, int size, char completion, boolean dockRight) {
        String ret;
        
        if (dockRight) {
            if (s.length() > size) {
                ret = s.substring(s.length() - size, size);
            } else {
                ret = s;
                for (int i=0; i < size - s.length(); i++) { 
                    ret = completion + ret;
                }
            }
        } else {      
            if (s.length() > size) {
                ret = s.substring(0, size);
            } else {
                ret = s;
                for (int i=0; i < size - s.length(); i++) { 
                    ret = ret + completion;
                }
            } 
        }
         
        return ret;
    }   
    
    /**
     * Duplicates the first argument
     */
    public static String duplicate(String s, int nb) {
        String ret = "";
        for (int i=0; i<nb; i++) {
            ret += s;
        }
        return ret;
    }

    /**
     * Normalize the String passed as argument.
     * <BR>All chars which are not in the second argument are replaced by the third argument.
     */
    public static String normalizeString(String s, String acceptedChars, char replacementChar) {
        String lower = s.toLowerCase();
        char[] copy = new char[s.length()];
        for (int i=0; i<s.length(); i++) {
            if (acceptedChars.indexOf(lower.charAt(i)) == -1) {
                copy[i] = replacementChar; 
            } else {
                copy[i] = s.charAt(i);
            }
        }
        return new String(copy);
    }
    
    /**
     * Remove all '/' in the beginning of the file
     */
    public static String trimSlashes(String orig) {
        if (orig == null || orig.length() == 0) {
            return orig;
        } else if (orig.length() == 1) {
            if (orig.charAt(0) == '/') {
                return "";
            } else {
                return orig;
            }
        } else {
            boolean t0 = orig.charAt(0) == '/';
            boolean tn = orig.charAt(orig.length() - 1) == '/';
            if (t0 && tn) {
                return orig.substring(1, orig.length() - 1);
            } else if (t0) {
                return orig.substring(1);
            } else if (tn) {
                return orig.substring(0, orig.length() - 1);
            } else {
                return orig;
            }
        }
    }
    
    /**
     * Return true if s equals one of the Strings contained in the "filter" array or
     * if they start with one of them plus "/"
     */
    public static boolean passFilter(String s, String[] filter) {
        for (int i=0; i<filter.length; i++) {
        	// Check that the filter is normalized
			if (FileNameUtil.endsWithSeparator(filter[i])) {
				filter[i] = filter[i].substring(0, filter[i].length() - 1);
			}
        	
            if (filter[i].length() == 0 || s.equals(filter[i]) || s.startsWith(filter[i] + "/")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean passFilter(String s, FileListIterator iter) throws IOException {
    	return iter == null || iter.fetch(s);
    }
    
    /**
     * Call "normalizeString" on each non existing part of the file's path.
     * <BR>Keep the existing parts unchenged.
     */
    public static File normalizeIfNotExists(File file, String acceptedChars, char replacementChar) {
        if (FileSystemManager.exists(file)) {
            return file;
        } else {
            return new File(FileSystemManager.getAbsoluteFile(normalizeIfNotExists(FileSystemManager.getParentFile(file), acceptedChars, replacementChar)), normalizeString(FileSystemManager.getName(file), acceptedChars, replacementChar));
        }
    }
    

    /**
     * Read the stream until a CRLF (13-10) is reached OR the buffer's limit is reached.
     * <BR>The actual number of bytes read (or -1 if the end of stream has been reached) is returned.
     */
    public static int readLine(InputStream is, byte[] buff) throws IOException {
        byte b=0, pb;
        int v;
        int offset = 0;
        while (true) {
            pb = b;
            v = is.read();
            if (v == -1) {
                if (offset == 0) {
                    // End of stream reached
                    return -1;
                } else {
                    // End of stream reached
                    return offset;
                }
            } else {
                b = (byte)v;
                buff[offset++] = b;

                // CRLF or buffer limit reached
                if ((pb == 13 && b == 10) || offset == buff.length) {
                    return offset;
                }
            }
        }
    }

    /**
     * Read the stream until a CRLF (13-10) is reached OR the buffer's limit is reached.
     * <BR>The actual number of bytes read (or -1 if the end of stream has been reached) is returned.
     */
    public static byte[] readLineBA(InputStream is, int size) throws IOException {
        byte[] tmp = new byte[size];
        int nb = readLine(is, tmp);
        if (nb == -1) {
            return null;
        } else {
            if (nb == size) {
                return tmp;
            } else {
                byte[] nArray = new byte[nb];
                for (int i=0; i<nb; i++) {
                    nArray[i] = tmp[i];
                }
                return nArray;
            }
        }
    }

    /**
     * Read the stream until a CRLF (13-10)
     */
    public static String readLineString(InputStream is) throws IOException {
        String ret = "";

        while (true) {
            byte[] tmp = readLineBA(is, 4*1024);
            if (tmp == null) {
                break;
            }
            ret += new String(tmp);

            if (tmp[tmp.length-1] == 10 && tmp[tmp.length-2] == 13) {
                break;
            }
        }

        return ret;
    }
}