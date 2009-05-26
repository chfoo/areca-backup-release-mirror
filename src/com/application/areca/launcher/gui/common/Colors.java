package com.application.areca.launcher.gui.common;

import org.eclipse.swt.SWT;

import com.application.areca.launcher.gui.Application;

/**
 * Default colors.
 * 
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
public interface Colors {
    public static final org.eclipse.swt.graphics.Color C_ERROR = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 180, 0, 0); 
    public static final org.eclipse.swt.graphics.Color C_WARNING = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 0,0,150);   
    public static final org.eclipse.swt.graphics.Color C_INFO = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 0,0,150);    
    
    public static final org.eclipse.swt.graphics.Color C_PROGRESS = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 220, 85, 65);
    public static final org.eclipse.swt.graphics.Color C_FINISHED = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 170, 208, 253);
    
    public static final org.eclipse.swt.graphics.Color C_FLD_ERROR = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 170, 208, 253);
    //public static final org.eclipse.swt.graphics.Color C_FLD_ERROR = new org.eclipse.swt.graphics.Color(Application.getInstance().getDisplay(), 255, 50, 50);
    
    public static final org.eclipse.swt.graphics.Color C_BLACK = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_BLACK);
    public static final org.eclipse.swt.graphics.Color C_GRAY = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
    public static final org.eclipse.swt.graphics.Color C_LIGHT_GRAY = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_GRAY);
}
