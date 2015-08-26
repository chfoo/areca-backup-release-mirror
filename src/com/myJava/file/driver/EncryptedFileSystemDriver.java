package com.myJava.file.driver;
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
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.myJava.encryption.EncryptionUtil;
import com.myJava.file.OutputStreamListener;
import com.myJava.file.driver.namehash.NameHashFileSystemDriver;
import com.myJava.file.metadata.FileMetaData;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskCancelledException;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * Linkable driver that adds encryption features
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
public class EncryptedFileSystemDriver 
extends AbstractLinkableFileSystemDriver {

	public static String WRAP_DEFAULT = "default";
	public static String WRAP_ENABLED = "enable";
	public static String WRAP_DISABLED = "disable";
	
	protected File directoryRoot;
	protected Key key;
	protected String transformation;
	protected AlgorithmParameterSpec iv;
	protected Cipher fileNameEncryptionCipher;
	protected Cipher fileNameDecryptionCipher;
	protected boolean encryptNames;
	protected String nameWrappingMode;

	public EncryptedFileSystemDriver(
			File directoryRoot, 
			String transformation, 
			AlgorithmParameterSpec iv, 
			Key key,
			boolean encryptNames,
			String nameWrappingMode
	) {
		// Init attributs
		this.directoryRoot = directoryRoot.getAbsoluteFile();

		this.key = key;
		this.transformation = transformation;
		this.iv = iv;
		this.encryptNames = encryptNames;
		this.nameWrappingMode = nameWrappingMode.toLowerCase();

		// Init ciphers
		fileNameEncryptionCipher = buildNewCipher(Cipher.ENCRYPT_MODE);
		fileNameDecryptionCipher = buildNewCipher(Cipher.DECRYPT_MODE);
		
		if (! checkNameEncryptionScheme()) {
			throw new IllegalStateException("Illegal name wrapping mode : [" + nameWrappingMode + "]");
		}
	}
	
	private boolean checkNameEncryptionScheme() {
		return nameWrappingMode != null
		&& (
				nameWrappingMode.equals(WRAP_DEFAULT)
				|| nameWrappingMode.equals(WRAP_DISABLED)
				|| nameWrappingMode.equals(WRAP_ENABLED)
		);
	}

	/**
	 * Sets the predecessor.
	 * <BR>On Windows systems, this predecessor is wrapped by a HashFileSystemDriver to ensure that paths sizes 
	 * won't exceed 256 characters. 
	 */
	public void setPredecessor(FileSystemDriver predecessor) {
		if (shallWrappPredecessor(predecessor)) {
			NameHashFileSystemDriver hashPredecessor = new NameHashFileSystemDriver(this.directoryRoot);
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
			NameHashFileSystemDriver hashPredecessor = (NameHashFileSystemDriver)this.predecessor;
			return hashPredecessor.getPredecessor();
		} else {
			return super.getPredecessor();
		}
	}

	protected boolean shallWrappPredecessor(FileSystemDriver predecessor) {
		if (this.nameWrappingMode.equals(WRAP_DISABLED) || (! encryptNames)) {
			return false;
		} else if (this.nameWrappingMode.equals(WRAP_ENABLED)) {
			return true;	
		} else {
			return (! predecessor.supportsLongFileNames());
		}
	}

	public File getDirectoryRoot() {
		return directoryRoot;
	}

	public boolean canRead(File file) {
		return this.predecessor.canRead(this.encryptFileName(file));
	}

	public short getType(File file) throws IOException {
		return this.predecessor.getType(this.encryptFileName(file));
	}

	public boolean canWrite(File file) {
		return this.predecessor.canWrite(this.encryptFileName(file));
	}

	public FileCacheableInformations getInformations(File file) {
		return this.predecessor.getInformations(this.encryptFileName(file));
	}

	public FileMetaData getMetaData(File f, boolean onlyBasicAttributes) throws IOException {
		return this.predecessor.getMetaData(this.encryptFileName(f), onlyBasicAttributes);
	}
	
    public String getPhysicalPath(File file) {
    	return predecessor.getPhysicalPath(this.encryptFileName(file));
	}

	public boolean createNewFile(File file) throws IOException {
		return this.predecessor.createNewFile(this.encryptFileName(file));
	}

	public boolean delete(File file) {
		return this.predecessor.delete(this.encryptFileName(file));
	}
	
    public void forceDelete(File file, TaskMonitor monitor)
    throws IOException, TaskCancelledException {
    	predecessor.forceDelete(this.encryptFileName(file), monitor);
	}

	public boolean exists(File file) {
		return this.predecessor.exists(this.encryptFileName(file));
	}

	public void deleteOnExit(File f) {
		predecessor.deleteOnExit(this.encryptFileName(f));
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

	public boolean isEncryptNames() {
		return this.encryptNames;
	}

	public String[] list(File file, FilenameFilter filter) {
		String[] files = this.predecessor.list(this.encryptFileName(file), new FilenameFilterAdapter(filter, this));
		return parseFiles(files);
	}

	public String[] list(File file) {
		String[] files = this.predecessor.list(this.encryptFileName(file));
		return parseFiles(files);
	}

	private String[] parseFiles(String[] files) {
		ArrayList ret = new ArrayList();

		if (files == null) {
			return null;
		} else {
			for (int i=0; i<files.length; i++) {
				try {
					ret.add(this.decryptFileName(files[i]));
				} catch (Throwable e) {
					Logger.defaultLogger().error("Invalid encryption key or encryption parameters : unable to read file name for " + files[i] + ". This file will be ignored. (" + e.getMessage() + ")");
				}
			}

			return (String[])ret.toArray(new String[ret.size()]);   
		}
	}
	
	private File[] parseFiles(File[] files) {
		ArrayList ret = new ArrayList();

		if (files == null) {
			return null;
		} else {
			for (int i=0; i<files.length; i++) {
				try {
					ret.add(this.decryptFileName(files[i]));
				} catch (Throwable e) {
					Logger.defaultLogger().error("Invalid encryption key or encryption parameters : unable to read file name for " + predecessor.getAbsolutePath(files[i]) + ". This file will be ignored. (" + e.getMessage() + ")");
				}
			}

			return (File[])ret.toArray(new File[ret.size()]);   
		}
	}

	public File[] listFiles(File file, FileFilter filter) {
		File[] files = this.predecessor.listFiles(this.encryptFileName(file), new FileFilterAdapter(filter, this));
		return parseFiles(files);
	}

	public File[] listFiles(File file, FilenameFilter filter) {
		File[] files = this.predecessor.listFiles(this.encryptFileName(file), new FilenameFilterAdapter(filter, this));
		return parseFiles(files);
	}

	public File[] listFiles(File file) {
		File[] files = this.predecessor.listFiles(this.encryptFileName(file));
		return parseFiles(files);
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

	public void applyMetaData(FileMetaData p, File f) throws IOException {
		this.predecessor.applyMetaData(p, this.encryptFileName(f));
	}
	
	public InputStream getCachedFileInputStream(File file) throws IOException {
		File target = this.encryptFileName(file);
		return buildInputStream(predecessor.getCachedFileInputStream(target));
	}

	public InputStream getFileInputStream(File file) throws IOException {
		File target = this.encryptFileName(file);
		return buildInputStream(predecessor.getFileInputStream(target));
	}

	private InputStream buildInputStream(InputStream sourceStream) {
		CipherInputStream cis = new CipherInputStream(sourceStream, buildNewCipher(Cipher.DECRYPT_MODE));
		return new CipherInputStreamWrapper(cis, sourceStream);
	}
	
	public OutputStream getCachedFileOutputStream(File file) throws IOException {
		File target = this.encryptFileName(file);
		return buildOutputStream(predecessor.getCachedFileOutputStream(target));
	} 

	public OutputStream getFileOutputStream(File file) throws IOException {
		File target = this.encryptFileName(file);
		return buildOutputStream(predecessor.getFileOutputStream(target));
	}    	

	public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
		return getFileOutputStream(file, append, null);
	}  

	public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
		if (append) {
			throw new IllegalArgumentException("Cannot open an OutputStream in 'append' mode on an encrypted FileSystem");
		}

		File target = this.encryptFileName(file);
		return buildOutputStream(predecessor.getFileOutputStream(target, append, listener)); 
	}
	
	private OutputStream buildOutputStream(OutputStream targetStream) {
		return new CipherOutputStream(targetStream, buildNewCipher(Cipher.ENCRYPT_MODE));
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
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		} catch (NoSuchAlgorithmException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		} catch (NoSuchPaddingException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		} catch (InvalidAlgorithmParameterException e1) {
			Logger.defaultLogger().error(e1);
			throw new IllegalArgumentException(e1.getMessage());
		}
	}

	protected File encryptFileName(File file) {
		if (encryptNames) {
			File orig = file.getAbsoluteFile();
			if (orig.equals(this.directoryRoot)) {
				return orig;
			} else {
				return new File(this.encryptFileName(orig.getParentFile()), this.encryptFileName(orig.getName()));
			}
		} else {
			return file;
		}
	}

	protected File decryptFileName(File file) {
		if (encryptNames) {
			File orig = file.getAbsoluteFile();
			if (orig.equals(this.directoryRoot)) {
				return orig;
			} else {
				return new File(this.decryptFileName(orig.getParentFile()), this.decryptFileName(orig.getName()));
			}
		} else {
			return file;
		}
	}

	/**
	 * Encrypts the file or directory name (without parent directory) 
	 */
	 private String encryptFileName(String shortName) { 
		 if (encryptNames) {
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
				 Logger.defaultLogger().error(e);
				 throw new IllegalArgumentException("IllegalStateException : [" + shortName + "] - " + e.getMessage());
			 } catch (IllegalBlockSizeException e) {
				 Logger.defaultLogger().error(e);
				 throw new IllegalArgumentException("IllegalBlockSizeException : [" + shortName + "] - " + e.getMessage());
			 } catch (BadPaddingException e) {
				 Logger.defaultLogger().error(e);
				 throw new IllegalArgumentException("BadPaddingException : [" + shortName + "] - " + e.getMessage());
			 }
		 } else {
			 return shortName;
		 }
	 }

	 /**
	  * Decrypts the file or directory name (without parent directory) 
	  */
	 protected String decryptFileName(String shortName) {
		 if (encryptNames) {
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
		 } else {
			 return shortName;
		 }
	 }

	 public int hashCode() {
		 int h = HashHelper.initHash(this);
		 h = HashHelper.hash(h, EncryptionUtil.hash(this.fileNameDecryptionCipher));
		 h = HashHelper.hash(h, EncryptionUtil.hash(this.fileNameEncryptionCipher));
		 h = HashHelper.hash(h, this.directoryRoot);
		 h = HashHelper.hash(h, this.predecessor);
		 h = HashHelper.hash(h, this.key);
		 h = HashHelper.hash(h, this.encryptNames);
		 h = HashHelper.hash(h, this.nameWrappingMode);
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
					 && EqualsHelper.equals(other.key, this.key) 
					 && EqualsHelper.equals(other.encryptNames, this.encryptNames) 			
					 && EqualsHelper.equals(other.nameWrappingMode, this.nameWrappingMode) 	
			 );
		 } else {
			 return false;
		 }
	 }

	 public String toString() {
		 StringBuffer sb = ToStringHelper.init(this);
		 ToStringHelper.append("Root", this.directoryRoot, sb);
		 ToStringHelper.append("Predecessor", this.predecessor, sb);
		 return ToStringHelper.close(sb);
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
			 try {
				 File targetDirectory = driver.decryptFileName(dir);
				 String targetName = driver.decryptFileName(name);

				 return filter.accept(targetDirectory, targetName);
			 } catch (Throwable e) {
				 Logger.defaultLogger().error("Invalid encryption key or encryption parameters : unable to read file name for " + driver.predecessor.getAbsolutePath(dir) + "/" + name + ". This file will be ignored. (" + e.getMessage() + ")");
				 return false;
			 }
		 }

		 public boolean equals(Object obj) {
			 if (obj == this) {
				 return true;
			 } else if (! (obj instanceof FilenameFilterAdapter)) {
				 return false;
			 } else {
				 FilenameFilterAdapter other = (FilenameFilterAdapter)obj;
				 return 
				 EqualsHelper.equals(this.filter, other.filter)
				 && EqualsHelper.equals(this.driver, other.driver);
			 }
		 }

		 public int hashCode() {
			 int h = HashHelper.initHash(this);
			 h = HashHelper.hash(h, filter);
			 h = HashHelper.hash(h, driver);
			 return h;
		 }

		 public String toString() {
			 StringBuffer sb = ToStringHelper.init(this);
			 ToStringHelper.append("Filter", this.filter, sb);
			 ToStringHelper.append("Driver", this.driver, sb);
			 return ToStringHelper.close(sb);
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
			 try {
				 File target = driver.decryptFileName(filename);
				 return filter.accept(target);
			 } catch (Throwable e) {
				 Logger.defaultLogger().error("Invalid encryption key or encryption parameters : unable to read file name for " + driver.predecessor.getAbsolutePath(filename) + ". This file will be ignored. (" + e.getMessage() + ")");
				 return false;
			 }
		 }

		 public boolean equals(Object obj) {
			 if (obj == this) {
				 return true;
			 } else if (! (obj instanceof FileFilterAdapter)) {
				 return false;
			 } else {
				 FileFilterAdapter other = (FileFilterAdapter)obj;
				 return 
				 EqualsHelper.equals(this.filter, other.filter)
				 && EqualsHelper.equals(this.driver, other.driver);
			 }
		 }

		 public int hashCode() {
			 int h = HashHelper.initHash(this);
			 h = HashHelper.hash(h, filter);
			 h = HashHelper.hash(h, driver);
			 return h;
		 }

		 public String toString() {
			 StringBuffer sb = ToStringHelper.init(this);
			 ToStringHelper.append("Filter", this.filter, sb);
			 ToStringHelper.append("Driver", this.driver, sb);
			 return ToStringHelper.close(sb);
		 }
	 }

	 public boolean directFileAccessSupported() {
		 return false;
	 }
}