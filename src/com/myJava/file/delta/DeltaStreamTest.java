package com.myJava.file.delta;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.delta.sequence.FileSequencer;
import com.myJava.file.delta.sequence.HashSequence;
import com.myJava.object.ToStringHelper;
import com.myJava.util.Util;

/**
 * Test Class for diff input stream
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 2736893395693886205
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
public class DeltaStreamTest {
    private static final double mutationProbability = 0.0005;
    private static final File sourceDirectory = new File("/home/olivier/Sources/Sources Java");
    private static final File workDirectory = new File("/home/olivier/tmp/arecatest");
    private static final FileTool tool = FileTool.getInstance();
    private static List filePairs = new ArrayList();
    private static int blocksize = 10240;
    private static long modulus = 1000000;
    private static final int compareSize = 10240;
    private static File logfile = new File("/home/olivier/Desktop/log.txt");
    
    public static void main(String[] args) {
        try {
            boolean update = args.length != 0;
            
            tool.delete(logfile, true);
            
            if (update) {
                tool.delete(workDirectory, true);
                tool.createDir(workDirectory);
            }
            
            mutateDirectory(sourceDirectory, update);
            comparePairs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void mutateDirectory(File dir, boolean update) throws Exception {
        File[] files = FileSystemManager.listFiles(dir);
        for (int i=0; i<files.length; i++) {
            if (FileSystemManager.isDirectory(files[i])) {
                mutateDirectory(files[i], update);
            } else {
                mutateFile(files[i], update);
            }
        }
    }
    
    private static File computeMutatedName(File in) {
        return new File(FileSystemManager.getAbsolutePath(workDirectory) + "/" + FileSystemManager.getAbsolutePath(in).substring(FileSystemManager.getAbsolutePath(sourceDirectory).length()));        
    }
    
    private static void mutateFile(File source, boolean update) throws Exception {
        try {
            println("Mutating " + source + " ...");
            File mutated = computeMutatedName(source);
            int mutCount = 0;
            
            if (update) {
                InputStream in = new BufferedInputStream(new FileInputStream(source), 200000);
                if (! FileSystemManager.exists(mutated.getParentFile())) {
                    tool.createDir(mutated.getParentFile());
                }
                OutputStream out = new BufferedOutputStream(new FileOutputStream(mutated), 200000);
                
                int read = 0;
                while ((read = in.read()) != -1) {
                    double rnd = Util.getRnd();
                    if (Math.abs(rnd) < mutationProbability) {
                        if (read > 0) {
                            read -= 1;
                        } else {
                            read += 1;
                        }
                        mutCount++;
                    }
                    out.write(read);
                }
                
                out.close();
                in.close();
                println("Mutated bytes : " + mutCount);
            }
            
            FilePair pair = new FilePair();
            pair.source = source;
            pair.mutated = mutated;
            pair.mutatedBytes = mutCount;
            filePairs.add(pair);
        } catch (FileNotFoundException ignored) {
            ignored.printStackTrace();
        }
    }
    
    private static void comparePairs() throws Exception {
        
        Iterator iter = filePairs.iterator();
        while (iter.hasNext()) {
            try {
                FilePair pair = (FilePair)iter.next();
                File source = pair.source;
                File mutated = pair.mutated;
                
                println("Creating sequence from " + source + " ...");

                // Write diff file
                InputStream f1 = new BufferedInputStream(new FileInputStream(source), 200000);
                InputStream f2 = new BufferedInputStream(new FileInputStream(mutated), 200000);

                FileSequencer s =  new FileSequencer(f1, blocksize);
                HashSequence seq = s.getHash();
                
                println("Sequence created - length = " + seq.getSize());
                
                println("Creating diff file from " + source + " and " + mutated + " ...");
                DeltaProcessor proc = new LayerWriterDeltaProcessor(new BufferedOutputStream(new FileOutputStream(FileSystemManager.getAbsolutePath(mutated) + ".diff"), 200000));            
                
                DeltaReader reader = new DeltaReader(seq, f2, new DeltaProcessor[] {proc}, null);
                //reader.read();
                
                f1.close();
                f2.close();
                
                // Compare diff file and mutated file
                println("Comparing " + source + " and " + mutated + "(" + pair.mutatedBytes + " mutated bytes) ...");
                InputStream orig = new BufferedInputStream(new FileInputStream(mutated), 200000);
                DeltaInputStream diffz = new DeltaInputStream();
                diffz.setMainInputStream(new FileInputStream(source));
                diffz.addInputStream(new BufferedInputStream(new FileInputStream(FileSystemManager.getAbsolutePath(mutated) + ".diff"), 200000), "");
                InputStream diff = new BufferedInputStream(diffz, 200000);
                
                byte[] buff1 = new byte[compareSize];
                byte[] buff2 = new byte[compareSize];
                MessageDigest dg1 = MessageDigest.getInstance("SHA");
                MessageDigest dg2 = MessageDigest.getInstance("SHA");
                int len1 = 0;
                int len2 = 0;
                long position = 0;
                while ((len1 = orig.read(buff1)) != -1) {
                    len2 = diff.read(buff2);
                    
                    if (len1 != len2) {
                        println("ERROR : INCOHERENT LENGTHS : " + len1 + " VS " + len2);
                    }
                    
                    for (int i=0; i<len1; i++) {
                        if (buff1[i] != buff2[i]) {
                            println("ERROR AT " + (position+i) + " : EXPECTED " + buff1[i] + " - GOT " + buff2[i]);
                        }
                    }
                    dg1.update(buff1, 0, len1);
                    dg2.update(buff2, 0, len2);
                    position += len1;
                }
                
                println("The files are identical : " + position + " bytes compared. Checksums : " + ToStringHelper.serialize(dg1.digest()) + " / " + ToStringHelper.serialize(dg2.digest()));
                
                diff.close();
                orig.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void println(String s) {
        System.out.println(s);
        try {
            FileWriter writer = new FileWriter(logfile, true);
            writer.write("\n" + s);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static final class FilePair {
        public File source;
        public File mutated;
        public long mutatedBytes;
    }
}
