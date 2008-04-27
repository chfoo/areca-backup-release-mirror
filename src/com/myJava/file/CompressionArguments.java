package com.myJava.file;

import java.io.File;
import java.nio.charset.Charset;

import com.myJava.object.PublicClonable;
import com.myJava.object.ToStringHelper;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 5323430991191230653
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
    public static final String ZIP_SUFFIX = ".zip";
	
    protected boolean isCompressed = false;
    protected long volumeSize = -1;
    protected int nbDigits;
    protected Charset charset = null;
    protected String comment = null;
    protected boolean useZip64 = false;
    protected boolean addExtension = true;

    public Charset getCharset() {
        return charset;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public boolean isAddExtension() {
		return addExtension;
	}

	public void setAddExtension(boolean addExtension) {
		this.addExtension = addExtension;
	}

	public void setCompressed(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public int getNbDigits() {
		return this.nbDigits;
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

    public void setMultiVolumes(long volumeSize, int nbDigits) {
        this.volumeSize = volumeSize;
        this.nbDigits = nbDigits;
    }
    
    public boolean isMultiVolumes() {
        return volumeSize != -1;
    }

    public PublicClonable duplicate() {
        CompressionArguments clone = new CompressionArguments();
        clone.setCharset(charset);
        clone.setComment(comment);
        clone.setAddExtension(addExtension);
        clone.setCompressed(isCompressed);
        clone.setUseZip64(useZip64);
        clone.setMultiVolumes(volumeSize, nbDigits);
        return clone;
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("IsCompressed", this.isCompressed, sb);
        if (isCompressed) {
            ToStringHelper.append("Zip64", this.useZip64, sb);
            ToStringHelper.append("Add extension", this.addExtension, sb);
            ToStringHelper.append("Charset", this.charset != null ? this.charset.displayName() : null, sb);
            ToStringHelper.append("Comment", this.comment, sb);
            ToStringHelper.append("VolumeSize", this.volumeSize, sb);
            ToStringHelper.append("NbDigits", this.nbDigits, sb);
        }
        return ToStringHelper.close(sb);
    }
}
