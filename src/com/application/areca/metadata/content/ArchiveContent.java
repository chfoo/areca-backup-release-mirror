package com.application.areca.metadata.content;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.application.areca.RecoveryEntry;
import com.application.areca.impl.FileSystemRecoveryEntry;
import com.application.areca.metadata.MetadataConstants;
import com.application.areca.metadata.MetadataEncoder;
import com.application.areca.metadata.trace.ArchiveTrace;
import com.myJava.system.OSTool;

/**
 * Class defining the physical content of an archive.
 * <BR>It is implemented as a set of RecoveryEntries.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 8785459451506899793
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
public class ArchiveContent implements MetadataConstants {
    
    private Set content = new HashSet();;

    private String defaultRootDirectory;
    
    public ArchiveContent() {
        this.defaultRootDirectory = OSTool.getTempDirectory();
    }

    public Iterator getContent() {
        return this.content.iterator();
    }
    
    public void add(FileSystemRecoveryEntry entry) {
        this.content.add(entry);
    }
    
    public void parse(String[] serialized, long version) {
        boolean backWardCompatibility = (version <= 2);
        
        for (int i=0; i<serialized.length; i++) {
            if (serialized[i] != null) {
            	String str = backWardCompatibility ? ContentBackwardCompatibleReencoder.reencode(serialized[i]) : serialized[i];
                this.content.add(this.deserialize(str));
            }
        }
    }
    
    protected static String serialize(FileSystemRecoveryEntry entry) {
        return MetadataEncoder.encode(entry.getName()) + SEPARATOR + entry.getSize();
    }
    
    public boolean contains(FileSystemRecoveryEntry entry) {
        return content.contains(entry);
    }
    
    protected RecoveryEntry deserialize(String serialized) {
        if (serialized == null || serialized.length() == 0) {
            return null;
        }
        int i = serialized.indexOf(SEPARATOR);
        if (i == -1) {
            return null;
        }
        String name = MetadataEncoder.decode(serialized.substring(0, i));
        long length = Long.parseLong(serialized.substring(i + SEPARATOR.length()));
        
        return new FileSystemRecoveryEntry(defaultRootDirectory, new File(defaultRootDirectory, name), RecoveryEntry.STATUS_STORED, length, false, false); 
    }
    
    public void override(ArchiveContent previousContent) {
        this.content.removeAll(previousContent.content);
        this.content.addAll(previousContent.content);
    }
    
    /**
     * Removes the entries from the ArchiveContent which are not referenced by the archive trace.
     * <BR>This process is useful after archive merge to ensure that deleted files are also removed from the ArchiveContent object.
     */
    public void clean(ArchiveTrace trace) {
        Iterator iter = this.content.iterator();
        List entriesToRemove = new ArrayList();
        while (iter.hasNext()) {
            FileSystemRecoveryEntry entry = (FileSystemRecoveryEntry)iter.next();
            if (! trace.containsFile(entry)) {
                entriesToRemove.add(entry);
            }
        }
        
        this.content.removeAll(entriesToRemove);
    }
}
