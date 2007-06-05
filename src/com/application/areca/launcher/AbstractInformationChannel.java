package com.application.areca.launcher;

import java.util.HashSet;
import java.util.Set;

/**
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
public abstract class AbstractInformationChannel {

    private Set displayedMessages = new HashSet();

    public AbstractInformationChannel() {
    }
    
    protected void registerMessage(Object messageKey) {
        if (messageKey != null) {
            this.displayedMessages.add(messageKey);
        }
    }
    
    protected boolean hasMessageBeenDisplayed(Object messageKey) {
        return (
                messageKey != null
                && displayedMessages.contains(messageKey)
        );
    }
}
