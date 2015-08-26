package com.application.areca.filter;

import java.io.File;
import java.io.IOException;

import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.util.log.Logger;

/**
 * 
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
public class SpecialFileFilter extends AbstractArchiveFilter {
	private static final long serialVersionUID = 1292446243312786589L;
	
	private boolean link;
	private boolean pipe;
	private boolean socket;
	private boolean charSpecFile;
	private boolean blockSpecFile;

	public boolean isLink() {
		return link;
	}

	public void setLink(boolean link) {
		this.link = link;
	}

	public boolean isPipe() {
		return pipe;
	}

	public void setPipe(boolean pipe) {
		this.pipe = pipe;
	}

	public boolean isSocket() {
		return socket;
	}

	public void setSocket(boolean socket) {
		this.socket = socket;
	}

	public boolean isCharSpecFile() {
		return charSpecFile;
	}

	public void setCharSpecFile(boolean charSpecFile) {
		this.charSpecFile = charSpecFile;
	}

	public boolean isBlockSpecFile() {
		return blockSpecFile;
	}

	public void setBlockSpecFile(boolean blockSpecFile) {
		this.blockSpecFile = blockSpecFile;
	}

	private boolean matchParameters(File entry) throws IOException {
		short type = FileSystemManager.getType(entry);
		return 
		(link && type == FileMetaDataAccessor.TYPE_LINK)
		|| (pipe && type == FileMetaDataAccessor.TYPE_PIPE)				
		|| (socket && type == FileMetaDataAccessor.TYPE_SOCKET)
		|| (blockSpecFile && type == FileMetaDataAccessor.TYPE_BLOCK_SPEC_FILE)
		|| (charSpecFile && type == FileMetaDataAccessor.TYPE_CHAR_SPEC_FILE);
	}

	public void acceptParameters(String parameters) {
		throw new UnsupportedOperationException("Parameters are not supported by this implementation.");
	}

    public short acceptIteration(File entry, File data) {
        return WILL_MATCH_PERHAPS;
    }

	public boolean acceptElement(File entry, File data) {   
		if (entry == null) {
			return false;
		} else {
			try {
				if (matchParameters(data)) {
					return ! logicalNot;
				} else {
					return logicalNot;
				}
			} catch (IOException e) {
				Logger.defaultLogger().error("Error during filtering of " + FileSystemManager.getDisplayPath(data), e);
				throw new IllegalArgumentException("Error during filtering of " + FileSystemManager.getDisplayPath(data));
			}
		}
	}

	public Duplicable duplicate() {
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.logicalNot = this.logicalNot;
		filter.blockSpecFile = this.blockSpecFile;
		filter.charSpecFile = this.charSpecFile;
		filter.link = this.link;
		filter.pipe = this.pipe;
		filter.socket = this.socket;
		return filter;
	}    
	
	public String getStringParameters() {
		String r = "";
		
		if (this.blockSpecFile) {
			r = append("filteredition.blockspecfile.label", r);
		}
		if (this.charSpecFile) {
			r = append("filteredition.charspecfile.label", r);
		}
		if (this.pipe) {
			r = append("filteredition.pipe.label", r);
		}
		if (this.link) {
			r = append("filteredition.link.label", r);
		}
		if (this.socket) {
			r = append("filteredition.socket.label", r);
		}
		
		return r;
	}
	
	private String append(String label, String ret) {
		if (ret.length() != 0) {
			ret += ", ";
		}
		return ret + ResourceManager.instance().getLabel(label);
	}

	public boolean requiresParameters() {
		return true;
	}

	public boolean equals(Object obj) {
		if (obj == null || (! (obj instanceof SpecialFileFilter)) ) {
			return false;
		} else {
			SpecialFileFilter other = (SpecialFileFilter)obj;
			return 
				EqualsHelper.equals(this.logicalNot, other.logicalNot)
				&& EqualsHelper.equals(this.pipe, other.pipe)
				&& EqualsHelper.equals(this.blockSpecFile, other.blockSpecFile)
				&& EqualsHelper.equals(this.charSpecFile, other.charSpecFile)
				&& EqualsHelper.equals(this.link, other.link)
				&& EqualsHelper.equals(this.socket, other.socket)				
			;
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, this.logicalNot);
		h = HashHelper.hash(h, this.blockSpecFile);
		h = HashHelper.hash(h, this.charSpecFile);
		h = HashHelper.hash(h, this.link);
		h = HashHelper.hash(h, this.pipe);
		h = HashHelper.hash(h, this.socket);
		return h;
	}
}
