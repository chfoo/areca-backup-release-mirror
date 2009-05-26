package com.myJava.file.driver.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import com.myJava.file.EventOutputStream;
import com.myJava.file.FileNameUtil;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.OutputStreamListener;
import com.myJava.file.driver.AbstractFileSystemDriver;
import com.myJava.file.driver.FileCacheableInformations;
import com.myJava.util.Util;
import com.myJava.util.log.Logger;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public abstract class AbstractRemoteFileSystemDriver 
extends AbstractFileSystemDriver {
    protected File localRootDirectory;
    protected String strLocalRootDirectory;
	
    protected static boolean USE_BUFFER = FrameworkConfiguration.getInstance().useFileSystemBuffer();
    protected static int BUFFER_SIZE = FrameworkConfiguration.getInstance().getFileSystemBufferSize();

    // Contains the local files, which will be sent on call to "flush"
    protected Map localInputFiles = new HashMap();
    protected AbstractProxy proxy;
    protected int maxProxies;
    protected ArrayList alternateProxies = new ArrayList();
    
    protected abstract String translateToLocal(String remoteFile);
    protected abstract String translateToRemote(File localFile);

    public AbstractRemoteFileSystemDriver(File localRootDirectory) {
		super();
		this.localRootDirectory = localRootDirectory;
        this.strLocalRootDirectory = FileNameUtil.normalizeSlashes(normalizeIfNeeded(this.localRootDirectory.getAbsolutePath()), true);
	}

    // Returns the normalized local root
    protected String getNormalizedLocalRoot() {
        return strLocalRootDirectory;
    }
    
	public boolean createNewFile(File file) throws IOException {
        String owner = this.buildNewOwnerId("createNewFile");
        AbstractProxy proxy = this.getAvailableProxy(owner);
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
    
    // Wraps the file into a FictiveLocalFile if necessary
    protected FictiveFile getFictiveLocalFile(File file) {
        if (file instanceof FictiveFile) {
            return (FictiveFile)file;
        } else {
            return new FictiveFile(normalizeIfNeeded(file.getAbsolutePath()), this.translateToRemote(file), this);
        }
    } 
    
    protected void initFictiveLocalFile(FictiveFile file) {
        String owner = this.buildNewOwnerId("initFictiveLocalFile");
        AbstractProxy proxy = this.getAvailableProxy(owner);
        try {
            proxy.initFictiveLocalFile(file);
        } finally {
            this.releaseProxy(proxy, owner);
        }
    }   
    
    protected synchronized AbstractProxy getAvailableProxy(String owner) {
        if (this.proxy.acquireLock(owner)) {
            return proxy;
        } else {
            Iterator iter = this.alternateProxies.iterator();
            while (iter.hasNext()) {
            	AbstractProxy alternateProxy = (AbstractProxy)iter.next();
                if (alternateProxy.acquireLock(owner)) {
                    return alternateProxy;
                }
            }

            if (this.alternateProxies.size() == maxProxies) {
                Logger.defaultLogger().error("Maximum number of proxies (" + maxProxies + ") reached - Unable to create another proxy", "getAvailableProxy");
                Logger.defaultLogger().info("Main Proxy : " + this.proxy.getOwnerId());
                Iterator piter = this.alternateProxies.iterator();
                while (piter.hasNext()) {
                	AbstractProxy px = (AbstractProxy)piter.next();
                    Logger.defaultLogger().info("Alternate Proxy : " + px.getOwnerId());
                }
                return null;
            } else {
                Logger.defaultLogger().info("Creating an alternate proxy on " + this.proxy.toString() + " : " + + (this.alternateProxies.size()+1) + " th.");
                AbstractProxy newProxy = this.proxy.cloneProxy();             
                newProxy.acquireLock(owner);
                this.alternateProxies.add(newProxy);
                return newProxy;   
            }
        }
    }

    protected void releaseProxy(AbstractProxy proxy, String owner) {
        if (proxy != null) {
            proxy.releaseLock(owner);
        }
    }
    
    public synchronized OutputStream getCachedFileOutputStream(File file) throws IOException {
        removeLocalInputFile(file);
        OutputStream raw = new LocalOutputStream(file, this);
        
        if (USE_BUFFER) {
            return new BufferedOutputStream(raw, BUFFER_SIZE);
        } else {
            return raw;
        }
    }
    
    public short getAccessEfficiency() {
        return ACCESS_EFFICIENCY_POOR;
    }

    public boolean canRead(File file) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }

    public boolean canWrite(File file) {
        throw new UnsupportedOperationException("This method is not supported by this implementation");
    }

    public boolean supportsLongFileNames() {
        return false;
    }
    
    public boolean isFile(File file) {
        return (! isDirectory(file));
    }

    public File getAbsoluteFile(File file) {
        return file.getAbsoluteFile();
    }
    
    public String getAbsolutePath(File file) {
        return normalizeIfNeeded(file.getAbsolutePath());
    }

    public File getCanonicalFile(File file) throws IOException {
        return file.getCanonicalFile();
    }

    public String getCanonicalPath(File file) throws IOException {
        return normalizeIfNeeded(file.getCanonicalPath());
    }

    public String getName(File file) {
        return file.getName();
    }

    public String getParent(File file) {
        return normalizeIfNeeded(file.getParent());
    }

    public File getParentFile(File file) {
        return file.getParentFile();
    }

    public String getPath(File file) {
        return normalizeIfNeeded(file.getPath());
    }

    public boolean isAbsolute(File file) {
        return file.isAbsolute();
    }
    
    public String[] list(File file, FilenameFilter filter) {
        File[] files = this.listFiles(file, filter);
        String[] ret = new String[files.length];

        for (int i=0; i<files.length; i++) {
            ret[i] = normalizeIfNeeded(files[i].getAbsolutePath());
        }

        return ret;
    }

    public String[] list(File file) {
        File[] files = this.listFiles(file);
        String[] ret = new String[files.length];

        for (int i=0; i<files.length; i++) {
            ret[i] = normalizeIfNeeded(files[i].getAbsolutePath());
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
    
    // No direct file access supported
    public boolean directFileAccessSupported() {
        return false;
    }

    protected void removeLocalInputFile(File file) {
    	synchronized (localInputFiles) {
        	File loc = (File)this.localInputFiles.remove(file);
        	if (loc != null && FileSystemManager.exists(loc)) {
        		FileSystemManager.delete(loc);
        	}
		}
    }
    
    private File getOrCreateLocalFile(File file) throws IOException {
    	synchronized (localInputFiles) {
        	File localFile = (File)localInputFiles.get(file);
        	if (localFile == null) {
        		localFile = FileTool.getInstance().generateNewWorkingFile("java", "ftpin", true);
                this.localInputFiles.put(file, localFile);
                FileTool.getInstance().copy(getFileInputStream(file), FileSystemManager.getFileOutputStream(localFile), true, true);
                FileSystemManager.deleteOnExit(localFile);
        	}
        	return localFile;
		}
    }

    public InputStream getCachedFileInputStream(File file) throws IOException {
        return FileSystemManager.getFileInputStream(getOrCreateLocalFile(file));
    }
    
    public void mount() throws IOException {
    }

    public void unmount() throws IOException {
        Logger.defaultLogger().info("Unmounting driver : " + this.getClass().getName());
        this.flush();
        disconnect();
    }


    protected void disconnect() {
        Logger.defaultLogger().info("Disconnecting all proxies ...");
        this.proxy.disconnect();
        Iterator iter = this.alternateProxies.iterator();
        while (iter.hasNext()) {
            AbstractProxy px = (AbstractProxy)iter.next();
            px.disconnect();
        }
    }
    
    public synchronized void flush() throws IOException {
        // Flush all proxies
        Iterator iter = this.alternateProxies.iterator();
        while (iter.hasNext()) {
            AbstractProxy proxy = (AbstractProxy)iter.next();
            proxy.flush();
        }
        this.proxy.flush();
        Logger.defaultLogger().info("Flush completed.");
        Logger.defaultLogger().info("" + this.alternateProxies.size() + " alternate proxies are currently active.");
    }

    protected synchronized String buildNewOwnerId(String role) {
        return role + "#" + Util.getRndLong();
    }
    
    
    public OutputStream getFileOutputStream(File file, boolean append, OutputStreamListener listener) throws IOException {
    	OutputStream out = getFileOutputStream(file, append);
    	return listener == null ? out : new EventOutputStream(out, listener);
	}

    public OutputStream getFileOutputStream(File file) throws IOException {
        return getFileOutputStream(file, false);
    }
    
    public FileCacheableInformations getInformations(File file) {
        FictiveFile data = this.getFictiveLocalFile(file);
        return new FileCacheableInformations(
                data.length(),
                data.lastModified(),
                data.exists(),
                false,
                false,
                data.isDirectory(),
                false
        );
    }

    public boolean isContentSensitive() {
        return false;
    }
    

    public boolean delete(File file) {
    	removeLocalInputFile(file);
    	
        String owner = this.buildNewOwnerId("delete");
        AbstractProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
        	FileCacheableInformations infos = this.getInformations(file);
        	if (infos.isExists()) {
        		if (infos.isDirectory()) {
                    res = proxy.deleteDir(this.translateToRemote(file));
        		} else {
                    res = proxy.deleteFile(this.translateToRemote(file));
        		}
        	} else {
        		res = true;
        	}
        } catch (RemoteConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }

        return res;
    }
    

    public boolean mkdir(File file) {       
        String owner = this.buildNewOwnerId("mkdir");
        AbstractProxy proxy = this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.mkdir(this.translateToRemote(file));
        } catch (RemoteConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }

        return res;
    }
    

    public boolean renameTo(File source, File dest) {
    	removeLocalInputFile(source);
    	removeLocalInputFile(dest);
    	
        String owner = this.buildNewOwnerId("renameTo");
        AbstractProxy proxy = (AbstractProxy)this.getAvailableProxy(owner);
        boolean res = false;
        try {
            res = proxy.renameTo(this.translateToRemote(source), this.translateToRemote(dest));
        } catch (RemoteConnectionException e) {
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }

        return res;
    }
    

    public File[] listFiles(File file) {
        String owner = this.buildNewOwnerId("listFiles");
        AbstractProxy proxy = (AbstractProxy)this.getAvailableProxy(owner);
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
        } catch (RemoteConnectionException e) {
            Logger.defaultLogger().error(e);
            throw new UnexpectedConnectionException(e);
        } finally {
            this.releaseProxy(proxy, owner);
        }

        return returned;
    }
    
    public OutputStream getFileOutputStream(File file, boolean append) throws IOException {
    	removeLocalInputFile(file);
        
        String owner = this.buildNewOwnerId("getFileOutputStream");
        AbstractProxy proxy = this.getAvailableProxy(owner);

        try {
            OutputStream raw = proxy.getFileOutputStream(this.translateToRemote(file), append);

            if (USE_BUFFER) {
                return new BufferedOutputStream(raw, BUFFER_SIZE);
            } else {
                return raw;
            }
        } catch (IOException e) {
            releaseProxy(proxy, owner);
            throw e;
        } catch (RuntimeException e) {
            releaseProxy(proxy, owner);
            throw e;
        }
        // Pas de "releaseLock" systematique ici car c'est le stream qui s'en charge a la fermeture
    }

    public InputStream getFileInputStream(File file) throws IOException {
        String owner = this.buildNewOwnerId("getFileInputStream");
        AbstractProxy proxy = this.getAvailableProxy(owner);

        try {
            InputStream raw = proxy.getFileInputStream(this.translateToRemote(file));

            if (USE_BUFFER) {
                return new BufferedInputStream(raw, BUFFER_SIZE);
            } else {
                return raw;
            }
        } catch (IOException e) {
            releaseProxy(proxy, owner);
            throw e;
        } catch (RuntimeException e) {
            releaseProxy(proxy, owner);
            throw e;
        }
        // Pas de "releaseLock" systematique ici car c'est le stream qui s'en charge a la fermeture
    }
}
