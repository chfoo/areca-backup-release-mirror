package com.application.areca.launcher.gui.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

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
public interface Colors {
    public static final Color C_RED = new Color(Application.getInstance().getDisplay(), 250, 0, 0);
    public static final Color C_ORANGE = new Color(Application.getInstance().getDisplay(), 250, 120, 0);
    public static final Color C_BLUE = new Color(Application.getInstance().getDisplay(), 0, 0, 250);
     
    public static final Color C_INFO = new Color(Application.getInstance().getDisplay(), 0,0,150);    
    public static final Color C_GREY = new Color(Application.getInstance().getDisplay(), 150, 150, 150);
    
    public static final Color C_FLD_ERROR = new Color(Application.getInstance().getDisplay(), 170, 208, 253);
    
    public static final Color C_BLACK = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_BLACK);
    public static final Color C_LIGHT_GRAY = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_GRAY);
    public static final Color C_WHITE = Application.getInstance().getDisplay().getSystemColor(SWT.COLOR_WHITE);
}
