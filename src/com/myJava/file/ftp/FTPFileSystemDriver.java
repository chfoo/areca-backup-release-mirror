package com.myJava.file.ftp;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.AbstractFileSystemDriver;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.ToStringHelper;
import com.myJava.util.Utilitaire;
import com.myJava.util.log.Logger;

/**
 * File System driver which is connected to a FTP Server.
 * <BR>
 * <BR>The Following methods are not supported :
 * <BR>- public boolean canRead(File file) 			--> Not supported
 * <BR>- public boolean canWrite(File file) 			--> Not supported
 * <BR>- public long lastModified(File file); 			--> Not supported
 * <BR>- public boolean setLastModified(File file, long time); 	--> Not supported
 * <BR>- public boolean setReadOnly(File file); 		--> Not supported        
 * <BR>- public boolean mkdirs(File file);			--> Not supported  
 * <BR>- public boolean isHidden(File file); 			--> Not supported
 * <BR>- public Permission getPermissions(File file); 			--> Not supported   
 * <BR>- public void applyPermissions(Permissions p, File f);	--> Not supported
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
public class FTPFileSystemDriver extends AbstractFileSystemDriver {
    
    public static int MAX_PROXIES = FrameworkConfiguration.getInstance().getMaxFTPProxies();
    
    private FTPProxy ftpProxy;
    private ArrayList alternateProxies = new ArrayList();
    private String remoteRootDirectory;
    private File localRootDirectory;
    
    // Contains the local files, which will be sent on call to "flush"
    private Map localFiles = new HashMap();
    
    public FTPFileSystemDriver(FTPProxy ftpProxy, File localRoot, String remoteRoot) {
        this.ftpProxy = ftpProxy;
        this.remoteRootDirectory = FileNameUtil.heavyNormalizePath(remoteRoot, true);
        this.localRootDirectory = localRoot;
        this.ftpProxy.setFileInfoCache(new FTPFileInfoCache());
    }

    public boolean supportsLongFileNames() {
        return false;
    }
    
    public File getAbsoluteFile(File file) {
        return file.getAbsoluteFile();
    }
    
    public String getAbsolutePath(File file) {
        return normalize(file.getAbsolutePath());
    }
    
    public File getCanonicalFile(File file) throws IOException {
        return file.getCanonicalFile();
    }
    
    public String getCanonicalPath(File file) throws IOException {
        return file.getCanonicalPath();
    }
    
    public String getName(File file) {
        return file.getName();
    }
    
    public String getParent(File file) {
        return file.getParent();
    }
    
    public File getParentFile(File file) {
        return file.getParentFile();
    }
    
    public String getPath(File file) {
        return file.getPath();
    }
    
    public boolean isAbsolute(File file) {
        return file.isAbsolute();
    }
    
    public boolean isFile(File file) {
        return (! isDirectory(file));
    }
    
    public String[] list(File file, FilenameFilter filter) {
        File[] files = this.listFiles(file, filter);
        String[] ret = new String[files.length];
        
        for (int i=0; i<files.length; i++) {
            ret[i] = normalize(files[i].getAbsolutePath());
        }
        
        return ret;
    }
    
    public String[] list(File file) {
        File[] files = this.listFiles(file);
        String[] ret = new String[files.length];
        
        for (int i=0; i<files.length; i++) {
            ret[i] = normalize(files[i].getAbsolutePath());
        }
        
        return ret;
    }    
    
    public File[] listFiles(File file, FileFilter filter) {
        File[] unfiltered = this.listFiles(file);
        ArrayList retList = new ArrayList();
        for (int i = 0; i<unfiltered.length; i++) {
            if (filter.accept(unfiltered[i])) {
                retList.add(unfiltered[i]);
            }
        }
        
        return (File[])retList.toArray(new File[0]);
    }
    
    public File[] listFiles(File file, FilenameFilter filter) {
        File[] unfiltered = this.listFiles(file);
        ArrayList retList = new ArrayList();
        for (int i = 0; i<unfiltered.length; i++) {
            if (filter.accept(unfiltered[i].getParentFile(), unfiltered[i].getName())) {
                retList.add(unfiltered[i]);
            }
        }
        
        return (File[])retList.toArray(new File[0]);
    }
    
    public boolean createNewFile(File file) throws IOException {
        String owner = this.buildNewOwnerId("createNewFile");
        FTPProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.createNewFile(this.translateToRemote(file));
        } finally {
            this.releaseProxy(proxy, owner);
        }
        return res;
    }
    
    public long length(File file) {
        return getFictiveLocalFile(file).length();
    }    

    public long lastModified(File file) {
        return getFictiveLocalFile(file).lastModified();
    }
    
    public boolean isDirectory(File file) {
        return getFictiveLocalFile(file).isDirectory();
    }
    
    public boolean exists(File file) {
        return getFictiveLocalFile(file).exists();
    }  
    
    public boolean delete(File file) {
        String owner = this.buildNewOwnerId("delete");
        FTPProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.delete(this.translateToRemote(file));
        } catch (FTPConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }
        
        return res;
    }
    
    public boolean mkdir(File file) {
        String owner = this.buildNewOwnerId("mkdir");
        FTPProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.mkdir(this.translateToRemote(file));
        } catch (FTPConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }
        
        return res;
    }
    
    public boolean renameTo(File source, File dest) {
        String owner = this.buildNewOwnerId("renameTo");
        FTPProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.renameTo(this.translateToRemote(source), this.translateToRemote(dest));
        } catch (FTPConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }
        
        return res;
    }
    
    public InputStream getFileInputStream(File file) throws IOException {
        String owner = this.buildNewOwnerId("getFileInputStream");
        FTPProxy proxy = this.getAvailableProxy(owner);
        return proxy.getFileInputStream(this.translateToRemote(file));
    }
    
    public synchronized OutputStream getCachedFileOutputStream(File file) throws IOException {
        long rnd = Utilitaire.getRndLong();
        File localFile = new File(System.getProperty("user.home"), "java_ftp_driver_local_file" + rnd + ".tmp");
        FileSystemManager.deleteOnExit(localFile);
        this.localFiles.put(translateToRemote(file), localFile);
        return FileSystemManager.getFileOutputStream(localFile);
    }
    
    public OutputStream getFileOutputStream(File file) throws IOException {
        String owner = this.buildNewOwnerId("getFileOutputStream");
        FTPProxy proxy = this.getAvailableProxy(owner);
        return proxy.getFileOutputStream(this.translateToRemote(file));
    }
    
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
        String owner = this.buildNewOwnerId("getFileOutputStream");
        FTPProxy proxy = this.getAvailableProxy(owner);
        return proxy.getFileOutputStream(this.translateToRemote(file), append);
    }
    
    public File[] listFiles(File file) {
        String owner = this.buildNewOwnerId("listFiles");
        FTPProxy proxy = this.getAvailableProxy(owner);
        FictiveFile[] files = null;
        File[] returned = null;
        try {
            files = proxy.listFiles(this.translateToRemote(file));
            returned = new File[files.length];
            for (int i=0; i<files.length; i++) {
                returned[i] = new FictiveFile(
                        translateToLocal(files[i].getRemotePath()),
                        files[i].getRemotePath(),
                        files[i].length(),
                        files[i].isDirectory(),
                        files[i].exists(),
                        files[i].lastModified()
                );
            }
        } catch (FTPConnectionException e) {
            Logger.defaultLogger().error(e);
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }

        return returned;
    }

    protected void initFictiveLocalFile(FictiveFile file) {
        String owner = this.buildNewOwnerId("initFictiveLocalFile");
        FTPProxy proxy = this.getAvailableProxy(owner);
        try {
            proxy.initFictiveLocalFile(file);
        } finally {
            this.releaseProxy(proxy, owner);
        }
    }   
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, remoteRootDirectory);
        h = HashHelper.hash(h, localRootDirectory);
        h = HashHelper.hash(h, ftpProxy);                
        return h;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            if (o instanceof FTPFileSystemDriver) {
                FTPFileSystemDriver other = (FTPFileSystemDriver)o;
                
                return (
                        EqualsHelper.equals(this.remoteRootDirectory, other.remoteRootDirectory)
                        && EqualsHelper.equals(this.localRootDirectory, other.localRootDirectory)
                        && EqualsHelper.equals(this.ftpProxy, other.ftpProxy)			
                );
            } else {
                return false;
            }
        }
    }
    
    // No direct file access supported
    public boolean directFileAccessSupported() {
        return false;
    }
    
    
    // Returns the normalized local root
    private String getNormalizedLocalRoot() {
        return FileNameUtil.heavyNormalizePath(this.localRootDirectory.getAbsolutePath(), true);
    }
    
    // Returns the normalized remote root
    private String getNormalizedRemoteRoot() {
        return FileNameUtil.heavyNormalizePath(remoteRootDirectory, true);
    }
    
    // Converts the local file name to a remote file name
    private String translateToRemote(File localFile) {
        int l = this.getNormalizedLocalRoot().length();
        
        if (l < localFile.getAbsolutePath().length()) {
            String suffix = localFile.getAbsolutePath().substring(l);
            return FileNameUtil.heavyNormalizePath(this.getNormalizedRemoteRoot() + suffix, false);
        } else {
            return this.getNormalizedRemoteRoot();
        }
    }
    
    // Converts the remote file name to a local file name    
    private String translateToLocal(String remoteFile) {
        int l = getNormalizedRemoteRoot().length();
        
        if (l < remoteFile.length()) {
            String suffix = remoteFile.substring(l);
            return FileNameUtil.heavyNormalizePath(this.getNormalizedLocalRoot() + suffix, false);
        } else {
            return this.getNormalizedLocalRoot();
        }
    }
    
    // Wraps the file into a FictiveLocalFile if necessary
    protected FictiveFile getFictiveLocalFile(File file) {
        if (file instanceof FictiveFile) {
            return (FictiveFile)file;
        } else {
            return new FictiveFile(normalize(file.getAbsolutePath()), this.translateToRemote(file), this);
        }
    } 

    public void unmount() throws IOException {
        this.flush();
        disconnect();
    }
    
    private void disconnect() {
        this.ftpProxy.disconnect();
        Iterator iter = this.alternateProxies.iterator();
        while (iter.hasNext()) {
            FTPProxy px = (FTPProxy)iter.next();
            px.disconnect();
        }
    }
    
    public static void main(String[] args) {
        String localRoot = "/fictive/testftp/";
        String remoteRoot = "/test1/test2/";
        
        FTPFileSystemDriver driver = null;
        try {
            FTPProxy proxy = new FTPProxy();
            proxy.setRemoteServer(args[0]);
            proxy.setLogin(args[1]);
            proxy.setPassword(args[2]);
            
            proxy.setRemotePort(21);
            proxy.setPassivMode(true);
            
            driver = new FTPFileSystemDriver(proxy, new File(localRoot), remoteRoot);
            FileSystemManager.getInstance().registerDriver(new File(localRoot), driver);
            
            test2(localRoot);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.disconnect();
        }
    }

    public synchronized void flush() throws IOException {
        this.logProxies();
        Logger.defaultLogger().info("Flushing cached data : " + this.localFiles.size() + " files ...");
        Iterator iter = this.localFiles.entrySet().iterator();
        FileTool ft = new FileTool();
        try {
	        while (iter.hasNext()) {
	            Map.Entry entry = (Map.Entry)iter.next();
	            String remoteFile = (String)entry.getKey();
	            File localFile = (File)entry.getValue();
	
	            FTPProxy proxy = this.getAvailableProxy(this.buildNewOwnerId("flush"));
	            ft.copy(
	                    FileSystemManager.getFileInputStream(localFile),
	                    proxy.getFileOutputStream(remoteFile),
	                    true,
	                    true
	            );
	        }
	        
	        // Flush all proxies
	        iter = this.alternateProxies.iterator();
	        while (iter.hasNext()) {
	            FTPProxy proxy = (FTPProxy)iter.next();
	            proxy.flush();
	        }
	        this.ftpProxy.flush();
        } catch (IOException e) {
            Logger.defaultLogger().error("Got Exception during flush : ", e);
            throw e;
        } finally {
            try {
	            iter = this.localFiles.entrySet().iterator();
		        while (iter.hasNext()) {
		            Map.Entry entry = (Map.Entry)iter.next();
		            File localFile = (File)entry.getValue();
		            ft.delete(localFile, true);   
		        }
            } finally {
                this.localFiles.clear();
            }
        }
        Logger.defaultLogger().info("Flush complete.");
        Logger.defaultLogger().info("" + this.alternateProxies.size() + " alternate proxies are currently active.");
    }
    
    private void logProxies() {
        Logger.defaultLogger().info("Proxy informations : ");
        Logger.defaultLogger().info("Main proxy :" + this.ftpProxy.getOwnerId());
        Iterator iter = this.alternateProxies.iterator();
        while (iter.hasNext()) {
            FTPProxy proxy = (FTPProxy)iter.next();
            Logger.defaultLogger().info("Alternate proxy : " + proxy.getOwnerId());            
        }
        Logger.defaultLogger().info("End of proxy informations.");
    }
    
    private synchronized FTPProxy getAvailableProxy(String owner) {
        if (this.ftpProxy.acquireLock(owner)) {
            return ftpProxy;
        } else {
            Iterator iter = this.alternateProxies.iterator();
            while (iter.hasNext()) {
                FTPProxy alternateProxy = (FTPProxy)iter.next();
                if (alternateProxy.acquireLock(owner)) {
                    return alternateProxy;
                }
            }
            
            if (this.alternateProxies.size() == MAX_PROXIES) {
                Logger.defaultLogger().error("Maximum number of proxies (" + MAX_PROXIES + ") reached - Unable to create another proxy", "getAvailableProxy");
                return null;
            } else {
                Logger.defaultLogger().info("Creating an alternate proxy on " + this.ftpProxy.toString() + " : " + + (this.alternateProxies.size()+1) + " th.");
                FTPProxy newProxy = this.ftpProxy.cloneProxy();             
                newProxy.acquireLock(owner);
                this.alternateProxies.add(newProxy);
                return newProxy;   
            }
        }
    }
    
    private void releaseProxy(FTPProxy proxy, String owner) {
        proxy.releaseLock(owner);
    }
    
    private synchronized String buildNewOwnerId(String role) {
        return role + "#" + Utilitaire.getRndLong();
    }
    
    public static void test2(String localRoot) throws Exception {
        String remoteDir = "test3";
        String testFile = "/home/olivier/Desktop/test.txt";
                
        // Tests
        FileTool tool = new FileTool();
        
        // 1 : list 
        System.out.println("START : ");
        ls(localRoot);
        
        long time = System.currentTimeMillis();
        
        // 2 : copy
        System.out.println("COPY FILE");
        tool.copy(new File(testFile), new File(localRoot));
        ls(localRoot);
        
        System.out.println("COPY FILE 2");
        File current = new File(localRoot, new File(testFile).getName());
        tool.copy(current, new File(testFile).getParentFile().getParentFile());
        
        // 3 : rename remote
        System.out.println("RENAME FILE TO REMOTE");
        File renamed = new File(localRoot, new File(testFile).getName() + "2");
        System.out.println(FileSystemManager.renameTo(current, renamed));
        ls(localRoot);
        
        // 4 : delete renamed remote
        System.out.println("DELETE RENAMED");
        System.out.println(FileSystemManager.delete(renamed));
        ls(localRoot);
        
        // 5 : rename directory, and back
        System.out.println("RENAME REMOTE DIRECTORY");
        File currentD = new File(localRoot, remoteDir);
        File renamedD = new File(localRoot, remoteDir + "_2");
        System.out.println(FileSystemManager.renameTo(currentD, renamedD));
        ls(localRoot);
        
        System.out.println("RE-RENAME REMOTE DIRECTORY (BACK)");
        System.out.println(FileSystemManager.renameTo(renamedD, currentD));
        ls(localRoot);
        
        // 6 : delete directory
        System.out.println("DELETE REMOTE DIRECTORY");
        System.out.println(FileSystemManager.delete(currentD));
        ls(localRoot);
        
        // 7 : create directory
        System.out.println("CREATE REMOTE DIRECTORY");
        System.out.println(FileSystemManager.mkdir(currentD));
        ls(localRoot);
        
        System.out.println("Time elapsed : " + (System.currentTimeMillis() - time));
    }
    
    private static void ls(String localRoot) {
        System.out.println("      .... ls started ...");
        File[] result = FileSystemManager.listFiles(new File(localRoot));
        for (int i=0; i<result.length; i++) {
            System.out.println(result[i].toString());
            System.out.println("--------------------------------------------");
        }
        System.out.println("      .... ls ended ...");
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("LOCAL DIR", this.localRootDirectory, sb);
        ToStringHelper.append("REMOTE DIR", this.remoteRootDirectory, sb);
        ToStringHelper.append("HOST", this.ftpProxy.getRemoteServer(), sb);
        ToStringHelper.append("PORT", this.ftpProxy.getRemotePort(), sb);
        ToStringHelper.append("PASSIV", this.ftpProxy.isPassivMode(), sb);
        ToStringHelper.append("LOGIN", this.ftpProxy.getLogin(), sb);
        ToStringHelper.append("PASSWORD", this.ftpProxy.getPassword(), sb);
        return ToStringHelper.close(sb);
    }
    
    public short getAccessEfficiency() {
        return ACCESS_EFFICIENCY_POOR;
    }
    
    public boolean isContentSensitive() {
        return false;
    }
}