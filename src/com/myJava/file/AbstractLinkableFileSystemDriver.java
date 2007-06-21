package com.myJava.file;

import java.io.IOException;

import com.myJava.util.ToStringHelper;


/**
 * Classe dérivée de l'AbstractFileSystemDriver.
 * <BR>Définit un driver pouvant être chaîné, c'est à dire se reposant sur un autre
 * FileSystemDriver (son "prédécesseur") pour les accès.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3274863990151426915
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
public abstract class AbstractLinkableFileSystemDriver
extends AbstractFileSystemDriver 
implements LinkableFileSystemDriver {

    /**
     * Le driver prédécesseur.
     * <BR>C'est sur ce driver que s'appuiera le driver pour les accès au FileSystem.
     */
    protected FileSystemDriver predecessor;
    
    public FileSystemDriver getPredecessor() {
        return predecessor;
    }
    
    public void setPredecessor(FileSystemDriver predecessor) {
        this.predecessor = predecessor;
    }

    public boolean supportsLongFileNames() {
        return predecessor.supportsLongFileNames();
    }

    public void flush() throws IOException {
        predecessor.flush();
    }

    public void unmount() throws IOException {
        predecessor.unmount();
    }
    
    public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("PREDECESSOR", this.predecessor, sb);
        return ToStringHelper.close(sb);
    }
    
    public short getAccessEfficiency() {
        return predecessor.getAccessEfficiency();
    }
}

