package com.myJava.file;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.myJava.encryption.EncryptionUtil;
import com.myJava.file.attributes.Attributes;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.ToStringHelper;

/**
 * Driver "chainable" apportant des fonctionnalités de cryptage.
 * <BR>Les fichiers (noms, répertoires et contenus) sont cryptés avant traitement
 * par le Driver prédécesseur. 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -6307890396762748969
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
public class EncryptedFileSystemDriver 
extends AbstractLinkableFileSystemDriver {

    /**
     * La racine du répertoire qui sera crypté.
     */
    protected File directoryRoot;
    
    /**
     * Clef d'encryptage
     */
    protected Key key;
    
    protected String transformation;
    
    protected AlgorithmParameterSpec iv;
    
    protected Cipher fileNameEncryptionCipher;
    
    protected Cipher fileNameDecryptionCipher;

    /**
     * @param directoryRoot
     * @param key
     */
    public EncryptedFileSystemDriver(File directoryRoot, String transformation, AlgorithmParameterSpec iv, Key key) {

        // Init attributs
        try {
            this.directoryRoot = directoryRoot.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        this.key = key;
        this.transformation = transformation;
        this.iv = iv;
        
        // Init ciphers
        fileNameEncryptionCipher = buildNewCipher(Cipher.ENCRYPT_MODE);
        fileNameDecryptionCipher = buildNewCipher(Cipher.DECRYPT_MODE);
    }

    /**
     * Sets the predecessor.
     * <BR>On Windows systems, this predecessor is wrapped by a HashFileSystemDriver to ensure that paths sizes 
     * won't exceed 256 characters. 
     */
    public void setPredecessor(FileSystemDriver predecessor) {
        if (shallWrappPredecessor(predecessor)) {
	        HashFileSystemDriver hashPredecessor = new HashFileSystemDriver(this.directoryRoot);
	        hashPredecessor.setPredecessor(predecessor);
	        super.setPredecessor(hashPredecessor);
        } else {
            super.setPredecessor(predecessor);
        }
    }

    /**
     * Gets the predecessor.
     * <BR>On Windows systems, this predecessor is wrapped by a HashFileSystemDriver to ensure that paths sizes 
     * won't exceed 256 characters. 
     * <BR>The Driver which is returned by this method is thus this HashFileSystemDriver's predecessor.
     */
    public FileSystemDriver getPredecessor() {
        if (shallWrappPredecessor(this.predecessor)) {
            HashFileSystemDriver hashPredecessor = (HashFileSystemDriver)this.predecessor;
            return hashPredecessor.getPredecessor();
        } else {
            return super.getPredecessor();
        }
    }
    
    protected boolean shallWrappPredecessor(FileSystemDriver predecessor) {
        return ! predecessor.supportsLongFileNames();
    }
    
    public File getDirectoryRoot() {
        return directoryRoot;
    }
    
    public boolean canRead(File file) {
        return this.predecessor.canRead(this.encryptFileName(file));
    }
    
    public boolean canWrite(File file) {
        return this.predecessor.canWrite(this.encryptFileName(file));
    }
    
    public Attributes getAttributes(File f) throws IOException {
        return this.predecessor.getAttributes(this.encryptFileName(f));
    }
    
    public boolean createNewFile(File file) throws IOException {
        return this.predecessor.createNewFile(this.encryptFileName(file));
    }
    
    public boolean delete(File file) {
        return this.predecessor.delete(this.encryptFileName(file));
    }
    
    public boolean exists(File file) {
        return this.predecessor.exists(this.encryptFileName(file));
    }
    
    public File getAbsoluteFile(File file) {
        return this.predecessor.getAbsoluteFile(file);
    }
    
    public String getAbsolutePath(File file) {
        return this.predecessor.getAbsolutePath(file);
    }
    
    public File getCanonicalFile(File file) throws IOException {
        return this.predecessor.getCanonicalFile(file);
    }
    
    public String getCanonicalPath(File file) throws IOException {
        return this.predecessor.getCanonicalPath(file);
    }
    
    public String getName(File file) {
        return this.predecessor.getName(file);
    }
    
    public String getParent(File file) {
        return this.predecessor.getParent(file);
    }
    
    public File getParentFile(File file) {
        return this.predecessor.getParentFile(file);
    }
    
    public String getPath(File file) {
        return this.predecessor.getPath(file);
    }

    public void deleteOnExit(File f) {
        predecessor.deleteOnExit(this.encryptFileName(f));
    }
    
    public boolean isAbsolute(File file) {
        return this.predecessor.isAbsolute(this.encryptFileName(file));
    }
    
    public boolean isDirectory(File file) {
        return this.predecessor.isDirectory(this.encryptFileName(file));
    }
    
    public boolean isFile(File file) {
        return this.predecessor.isFile(this.encryptFileName(file));
    }
    
    public boolean isHidden(File file) {
        return this.predecessor.isHidden(this.encryptFileName(file));
    }
    
    public long lastModified(File file) {
        return this.predecessor.lastModified(this.encryptFileName(file));
    }
    
    public long length(File file) {
        return this.predecessor.length(this.encryptFileName(file));
    }
    
    public String[] list(File file, FilenameFilter filter) {
        File[] files = this.listFiles(file, filter);
        if (files != null) {
            String[] ret = new String[files.length];
            for (int i=0; i<files.length; i++) {
                ret[i] = normalize(files[i].getAbsolutePath());
            }
            
            return ret;
        } else {
            return null;
        }
    }
    
    public String[] list(File file) {
        File[] files = this.listFiles(file);
        if (files != null) {
            String[] ret = new String[files.length];
            for (int i=0; i<files.length; i++) {
                ret[i] = normalize(files[i].getAbsolutePath());
            }
            
            return ret;
        } else {
            return null;
        }
    }
    
    public File[] listFiles(File file, FileFilter filter) {
        File[] files = this.predecessor.listFiles(this.encryptFileName(file), new FileFilterAdapter(filter, this));
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                files[i] = this.decryptFileName(files[i]);
            }
        }
        
        return files;
    }
    
    public File[] listFiles(File file, FilenameFilter filter) {
        File[] files = this.predecessor.listFiles(this.encryptFileName(file), new FilenameFilterAdapter(filter, this));
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                files[i] = this.decryptFileName(files[i]);
            }
        }
        
        return files;
    }
    
    public File[] listFiles(File file) {
        File[] files = this.predecessor.listFiles(this.encryptFileName(file));
        if (files != null) {
            for (int i=0; i<files.length; i++) {
                files[i] = this.decryptFileName(files[i]);
            }
        }
        
        return files;
    }
    
    public boolean mkdir(File file) {
        return this.predecessor.mkdir(this.encryptFileName(file));
    }
    
    public boolean mkdirs(File file) {
        return this.predecessor.mkdirs(this.encryptFileName(file));
    }
    
    public boolean renameTo(File source, File dest) {
        return this.predecessor.renameTo(this.encryptFileName(source), this.encryptFileName(dest));
    }
    
    public boolean setLastModified(File file, long time) {
        return this.predecessor.setLastModified(this.encryptFileName(file), time);
    }
    
    public boolean setReadOnly(File file) {
        return this.predecessor.setReadOnly(this.encryptFileName(file));
    }

    public void applyAttributes(Attributes p, File f) throws IOException {
        this.predecessor.applyAttributes(p, this.encryptFileName(f));
    }
    
    public InputStream getFileInputStream(File file) throws IOException {
        File target = this.encryptFileName(file);
        return new CipherInputStream(predecessor.getFileInputStream(target), buildNewCipher(Cipher.DECRYPT_MODE));
    }
    
    public OutputStream getCachedFileOutputStream(File file) throws IOException {
        File target = this.encryptFileName(file);
        return new CipherOutputStream(predecessor.getCachedFileOutputStream(target), buildNewCipher(Cipher.ENCRYPT_MODE));
    }    
    
    public OutputStream getFileOutputStream(File file) throws IOException {
        File target = this.encryptFileName(file);
        return new CipherOutputStream(predecessor.getFileOutputStream(target), buildNewCipher(Cipher.ENCRYPT_MODE));
    }    
    
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        if (append) {
            throw new IllegalArgumentException("Cannot open an OutputStream in 'append' mode on an encrypted FileSystem");
        }
        
        File target = this.encryptFileName(file);
        return new CipherOutputStream(predecessor.getFileOutputStream(target, append), buildNewCipher(Cipher.ENCRYPT_MODE)); 
    }    
    
    private Cipher buildNewCipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance(this.transformation);
            if (this.iv == null) {
                cipher.init(mode, this.key);                
            } else {
                cipher.init(mode, this.key, this.iv);
            }
            
            return cipher;
        } catch (InvalidKeyException e1) {
            throw new IllegalArgumentException(e1.getMessage());
        } catch (NoSuchAlgorithmException e1) {
            throw new IllegalArgumentException(e1.getMessage());
        } catch (NoSuchPaddingException e1) {
            throw new IllegalArgumentException(e1.getMessage());
        } catch (InvalidAlgorithmParameterException e1) {
            throw new IllegalArgumentException(e1.getMessage());
        }
    }
    
    protected File encryptFileName(File file) {
        try {
            File orig = file.getCanonicalFile();
            if (orig.equals(this.directoryRoot)) {
                return orig;
            } else {
                return new File(this.encryptFileName(orig.getParentFile()), this.encryptFileName(orig.getName()));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    protected File decryptFileName(File file) {
        try {
            File orig = file.getCanonicalFile();
            if (orig.equals(this.directoryRoot)) {
                return orig;
            } else {
                return new File(this.decryptFileName(orig.getParentFile()), this.decryptFileName(orig.getName()));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    /**
     * Crypte le nom du fichier/répertoire (nom court, sans répertoires parents) 
     */
    protected String encryptFileName(String shortName) {       
        try {
            if (shortName == null) {
                return null;
            }
            
            if (shortName.length() == 0) {
                return "";
            }
            
            byte[] bytes = this.fileNameEncryptionCipher.doFinal(shortName.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i=0; i<bytes.length; i++) {
                String str = Integer.toHexString((int)bytes[i] + 128);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            
            return sb.toString();
            
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("IllegalStateException : [" + shortName + "] - " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("IllegalBlockSizeException : [" + shortName + "] - " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("BadPaddingException : [" + shortName + "] - " + e.getMessage());
        }
    }
    
    /**
     * Décrypte le nom du fichier/répertoire (nom court, sans répertoires parents) 
     */
    protected String decryptFileName(String shortName) {
        try {
            int nb = shortName.length() / 2;
            byte[] values = new byte[nb];

            for (int i=0; i<nb; i++) {
                String str = shortName.substring(2*i, 2*i+2);
                
                int iValue = Integer.parseInt(str, 16);
                values[i] = (byte)(iValue - 128);
            }

            byte[] decrypted = this.fileNameDecryptionCipher.doFinal(values);
            return new String(decrypted);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("IllegalStateException : [" + shortName + "] - " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("IllegalBlockSizeException : [" + shortName + "] - " + e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalArgumentException("BadPaddingException : [" + shortName + "] - " + e.getMessage());
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, EncryptionUtil.hash(this.fileNameDecryptionCipher));
        h = HashHelper.hash(h, EncryptionUtil.hash(this.fileNameEncryptionCipher));
        h = HashHelper.hash(h, this.directoryRoot);
        h = HashHelper.hash(h, this.predecessor);
        
        return h;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof EncryptedFileSystemDriver) {
            EncryptedFileSystemDriver other = (EncryptedFileSystemDriver)o;
            
            return (
                    EncryptionUtil.equals(other.fileNameDecryptionCipher, this.fileNameDecryptionCipher)
                    && EncryptionUtil.equals(other.fileNameEncryptionCipher, this.fileNameEncryptionCipher) 
                    && EqualsHelper.equals(other.directoryRoot, this.directoryRoot) 
                    && EqualsHelper.equals(other.predecessor, this.predecessor) 
            );
        } else {
            return false;
        }
    }
    
    protected static class FilenameFilterAdapter implements FilenameFilter {
        protected FilenameFilter filter;
        protected EncryptedFileSystemDriver driver;
        
        public FilenameFilterAdapter(
                FilenameFilter wrappedFilter,
                EncryptedFileSystemDriver driver) {
            
            this.filter = wrappedFilter;
            this.driver = driver;
        }

        public boolean accept(File dir, String name) {
            File targetDirectory = driver.decryptFileName(dir);
            String targetName = driver.decryptFileName(name);
            
            return filter.accept(targetDirectory, targetName);
        }
    }
    
    protected static class FileFilterAdapter implements FileFilter {
        protected FileFilter filter;
        protected EncryptedFileSystemDriver driver;
        
        public FileFilterAdapter(
                FileFilter wrappedFilter,
                EncryptedFileSystemDriver driver) {
            
            this.filter = wrappedFilter;
            this.driver = driver;
        }

        public boolean accept(File filename) {
            File target = driver.decryptFileName(filename);
            return filter.accept(target);
        }
    }

    public boolean directFileAccessSupported() {
        return false;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("ROOT", this.directoryRoot, sb);
        ToStringHelper.append("PREDECESSOR", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }
    
    public boolean isContentSensitive() {
        return true;
    }
}