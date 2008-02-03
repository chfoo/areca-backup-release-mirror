package com.myJava.file;

import java.nio.charset.Charset;

import com.myJava.object.PublicClonable;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8290826359148479344
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
public class CompressionArguments implements PublicClonable {
    public static final String ZIP_EXTENSION = ".zip";
    
    protected boolean isCompressed = false;
    protected long volumeSize = -1;
    protected Charset charset = null;
    protected String comment = null;
    protected boolean useZip64 = false;
    protected boolean extensionEnabled;
    protected String extension;

    public CompressionArguments() {
        setExtensionEnabled(true);
    }
    
    public void setExtensionEnabled(boolean b) {
        extensionEnabled = b;
        extension = extensionEnabled ? ZIP_EXTENSION : "";
        checkMV();
    }
    
    private void checkMV() {
        if (isMultiVolumes() && (! extensionEnabled)) {
            throw new IllegalArgumentException("Zip extension MUST be enabled if multivolume archives are used");
        }
    }

    public String getExtension() {
        return extension;
    }

    public boolean isExtensionEnabled() {
        return extensionEnabled;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public void setCompressed(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment != null && comment.trim().length() == 0) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
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
        checkMV();
    }
    
    public boolean isMultiVolumes() {
        return volumeSize != -1;
    }

    public PublicClonable duplicate() {
        CompressionArguments clone = new CompressionArguments();
        clone.setCharset(charset);
        clone.setComment(comment);
        clone.setCompressed(isCompressed);
        clone.setUseZip64(useZip64);
        clone.setVolumeSize(volumeSize);
        clone.extension = extension;
        clone.extensionEnabled = extensionEnabled;
        return clone;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("IsCompressed", this.isCompressed, sb);
        if (isCompressed) {
            ToStringHelper.append("Zip64", this.useZip64, sb);
            ToStringHelper.append("Charset", this.charset != null ? this.charset.displayName() : null, sb);
            ToStringHelper.append("Comment", this.comment, sb);
            ToStringHelper.append("VolumeSize", this.volumeSize, sb);
            ToStringHelper.append("Extension", this.extension, sb);            
        }
        return ToStringHelper.close(sb);
    }
}
