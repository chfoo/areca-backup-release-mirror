package com.application.areca.impl.policy;

import java.nio.charset.Charset;

import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.myJava.file.driver.CompressedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.object.PublicClonable;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 6892146605129115786
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
public class CompressedFileSystemPolicy implements FileSystemPolicy {
    protected FileSystemPolicy base;
    protected long volumeSize = -1;
    protected Charset charset = null;
    protected String comment = null;
    protected boolean useZip64 = false;

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isUseZip64() {
        return useZip64;
    }

    public void setUseZip64(boolean useZip64) {
        this.useZip64 = useZip64;
    }

    public long getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(long volumeSize) {
        this.volumeSize = volumeSize;
    }

    public FileSystemPolicy getBase() {
        return base;
    }

    public void setBase(FileSystemPolicy base) {
        this.base = base;
    }

    public PublicClonable duplicate() {
        CompressedFileSystemPolicy clone = new CompressedFileSystemPolicy();
        clone.setBase((FileSystemPolicy)base.duplicate());
        clone.setCharset(charset);
        clone.setComment(comment);
        clone.setUseZip64(useZip64);
        clone.setVolumeSize(volumeSize);
        return clone;
    }

    public String getBaseArchivePath() {
        return base.getBaseArchivePath();
    }

    public String getDisplayableParameters() {
        return base.getDisplayableParameters();
    }

    public String getId() {
        return base.getId();
    }

    public FileSystemDriver initFileSystemDriver() throws ApplicationException {
        FileSystemDriver root = base.initFileSystemDriver();
        CompressedFileSystemDriver driver = new CompressedFileSystemDriver(root);
        driver.setCharset(charset);
        driver.setComment(comment);
        driver.setUseZip64(useZip64);
        driver.setVolumeSize(volumeSize);
        return driver;
    }

    public void setMedium(ArchiveMedium medium) {
        base.setMedium(medium);
    }

    public void synchronizeConfiguration() {
        base.synchronizeConfiguration();
    }

    public void validate(boolean extendedTests) throws ApplicationException {
        base.validate(extendedTests);
    }
}
