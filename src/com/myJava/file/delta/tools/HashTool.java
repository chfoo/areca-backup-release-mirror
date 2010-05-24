package com.myJava.file.delta.tools;

import com.myJava.configuration.FrameworkConfiguration;

/**
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class HashTool {
    private static final int MODULUS = FrameworkConfiguration.getInstance().getDeltaQuickHashModulus();
    private static final int MULTIPLIER = FrameworkConfiguration.getInstance().getDeltaQuickHashMultiplier();
    
    public static int hash(int currentHash, byte value) {
        return (currentHash + MULTIPLIER * Math.abs(value)) % MODULUS;
    }
    
    public static int update(int currentHash, byte newValue, byte oldValue) {
        return (currentHash + MULTIPLIER * (Math.abs(newValue) - Math.abs(oldValue))) % MODULUS;
    }
}
