package com.myJava.file.attributes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * Helper for file permissions.
 * <BR>Reads/Writes file permissions from the file system
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1700699344456460829
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
public class AttributesHelper {

    /**
     * Reads the permission from the FileSystem
     */
    public static Attributes readFileAttributes(File f) throws IOException {
        if (OSTool.isSystemWindows()) {
            return readWindowsAttributes(f);
        } else {
            return readUnixAttributes(f);            
        }
    }
    
    /**
     * Applies the permission to the file 
     */
    public static void applyFileAttributes(File f, Attributes p) throws IOException {
        if (OSTool.isSystemWindows()) {
            applyWindowsAttributes(f, p);
        } else {
            applyUnixAttributes(f, p);            
        }
    }
    
    /**
     * Serializes the permission 
     */
    public static String serialize(Attributes p) {
        if (OSTool.isSystemWindows()) {
            return encodeWindows(p);
        } else {
            return encodeUnix(p);            
        }
    }
    
    /**
     * Deserializes the permission 
     */
    public static Attributes deserialize(String s) {
        if (s.charAt(0) == 'w') {
            return decodeWindows(s);
        } else {
            return decodeUnix(s);            
        }
    }
    
    private static Attributes readWindowsAttributes(File f) throws IOException {
        WindowsAttributes p = new WindowsAttributes();
        p.setCanRead(f.canRead());
        p.setCanWrite(f.canWrite());
        return p;
    }
    
    private static Attributes readUnixAttributes(File f) throws IOException {
        UnixAttributes p = new UnixAttributes();
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        Process process = null;
        
        try {
            process = Runtime.getRuntime().exec(new String[] {"ls", "-ald1", FileSystemManager.getAbsolutePath(f)});
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = reader.readLine();

            if (str == null) {
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String err = errorReader.readLine();
                throw new IOException("Error during file permission reading for file [" + FileSystemManager.getAbsolutePath(f) + "] : " + err); 
            }
            
            // Permissions
            int perm = 
                100 * readrwx(str.charAt(1), str.charAt(2), str.charAt(3))
                + 10 * readrwx(str.charAt(4), str.charAt(5), str.charAt(6))
                + readrwx(str.charAt(7), str.charAt(8), str.charAt(9));              
            p.setPermissions(perm);
            
            // Size
            int index = 10;
            while (str.charAt(index) == ' ') {
                index++;
            }
            while (str.charAt(index) != ' ') {
                index++;
            }
            while (str.charAt(index) == ' ') {
                index++;
            }
            
            // owner
            int index2 = str.indexOf(" ", index);
            p.setOwner(str.substring(index, index2));
            
            // owner group
            int index3 = str.indexOf(" ", index2 + 1);
            p.setOwnerGroup(str.substring(index2 + 1, index3).trim());
        } catch (InterruptedException e) {
            Logger.defaultLogger().error(e);
            throw new IOException("Unable to read attributes for file : " + FileSystemManager.getAbsolutePath(f));
        } finally {
            // Explicitly close all streams
            
            // IN
            if (reader != null) {
                reader.close();
            } else if (process != null) {
                process.getInputStream().close();
            }
            
            // ERROR
            if (errorReader != null) {
                errorReader.close();
            } else if (process != null) {
                process.getErrorStream().close();
            }
            
            // OUT
            if (process != null) {
                process.getOutputStream().close();
            }
        }

        return p;
    }
    
    private static int readrwx(char r, char w, char x) {
        int ret = 0;
        if (r != '-') {
            ret += 4;
        }
        if (w != '-') {
            ret += 2;
        }
        if (x != '-') {
            ret += 1;
        }
        return ret;
    }
    
    private static void applyWindowsAttributes(File f, Attributes p) throws IOException {
        WindowsAttributes perm = (WindowsAttributes)p;
        if (! perm.isCanWrite()) {
            f.setReadOnly();
        }
    }
    
    private static void applyUnixAttributes(File f, Attributes p) throws IOException {   
        UnixAttributes current = (UnixAttributes)readFileAttributes(f);
        UnixAttributes target = (UnixAttributes)p;
        
        // Owner / Group
        if (
                (! current.getOwner().equals(p.getOwner()))
                || (! current.getOwnerGroup().equals(p.getOwnerGroup()))
        ) {
            execute(new String[] {"chown", p.getOwner() + ":" + p.getOwnerGroup(), FileSystemManager.getAbsolutePath(f)});
        }
        
        if (current.getPermissions() != target.getPermissions()) {
            execute(new String[] {"chmod", "" + target.getPermissions(), FileSystemManager.getAbsolutePath(f)});
        }
    }
    
    private static String encodeWindows(Attributes p) {
        WindowsAttributes perm = (WindowsAttributes)p;
        int nb = 
            (perm.isCanRead() ? 1 : 0)
            + 2*(perm.isCanWrite() ? 1 : 0);
        
        return "w" + nb;
    }

    private static String encodeUnix(Attributes p) {
        UnixAttributes perm = (UnixAttributes)p;
        StringBuffer sb = new StringBuffer("u");
        
        return 
        	sb
        	.append(perm.getPermissions())
        	.append(perm.getOwner())
        	.append(" ")
        	.append(perm.getOwnerGroup())
        	.toString();
    }
    
    private static Attributes decodeWindows(String s) {
        int nb = Integer.parseInt("" + s.charAt(1));
        WindowsAttributes p = new WindowsAttributes();
        p.setCanRead(nb % 2 == 1);
        p.setCanWrite(nb >= 2);
        
        return p;
    }
    
    private static Attributes decodeUnix(String s) {
        String perms = s.substring(1, 4);
        int iPerms = Integer.parseInt(perms);
        
        int index = s.indexOf(' ');
        String owner = s.substring(4, index);
        String group = s.substring(index + 1).trim();
        
        UnixAttributes p = new UnixAttributes();
        p.setOwner(owner);
        p.setOwnerGroup(group);
        p.setPermissions(iPerms);

        return p;
    }

    private static void execute(String[] cmd) throws IOException {
        Runtime.getRuntime().exec(cmd);
    }
    
    public static void main(String[] args) {
        /*
    }
        try {
            */
            //File f1 = new File("/home/olivier/Desktop/test");
            //File f1 = new File("/home/olivier/WineCVS.sh");
            //File f2 = new File("/home/olivier/Incoming");
            
            //Attributes p1 = AttributesHelper.readFileAttributes(f1);
            //System.out.println(p1);
            
            File f = new File("/");
            
            System.out.println(f.lastModified());
            
            /*
            Attributes p2 = AttributesHelper.readFileAttributes(f2);
            
            System.out.println("\nAVANT :");
            System.out.println(f1.getAbsolutePath() + "              " + p1.toString());
            System.out.println(f2.getAbsolutePath() + "              " + p2.toString());

            applyFileAttributes(f1, p2);
            
            Attributes p1bis = AttributesHelper.readFileAttributes(f1);
            
            System.out.println("\nAPRES :");
            System.out.println(f1.getAbsolutePath() + "              " + p1bis.toString());
            System.out.println(f2.getAbsolutePath() + "              " + p2.toString());
            
            applyFileAttributes(f1, p1);
            
            Attributes p1ter = AttributesHelper.readFileAttributes(f1);
            
            System.out.println("\nRAZ :");
            System.out.println(f1.getAbsolutePath() + "              " + p1ter.toString());
            System.out.println(f2.getAbsolutePath() + "              " + p2.toString());
            
            String s1 = serialize(p1);
            String s2 = serialize(p2);
            
            System.out.println("\nENCODAGE :");
            System.out.println(f1.getAbsolutePath() + "              " + s1);
            System.out.println(f2.getAbsolutePath() + "              " + s2);
            
            System.out.println("\nDECODAGE :");
            System.out.println(f1.getAbsolutePath() + "              " + deserialize(s1).toString());
            System.out.println(f2.getAbsolutePath() + "              " + deserialize(s2).toString());
            */
            /*
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
