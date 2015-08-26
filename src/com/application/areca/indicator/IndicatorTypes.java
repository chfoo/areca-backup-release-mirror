package com.application.areca.indicator;

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
public interface IndicatorTypes {

    public static Integer T_NOF = new Integer(1);
    public static Integer T_SFS = new Integer(2);
    public static Integer T_NOA = new Integer(3);
    public static Integer T_APS = new Integer(4);
    public static Integer T_PSR = new Integer(6);
    public static Integer T_SWH = new Integer(7);
    public static Integer T_SOH = new Integer(8);
    public static Integer T_SRR = new Integer(9);

    public static String N_NOF = "Number of source files (NOF)";
    public static String N_SFS = "Source files size (SFS)";
    public static String N_NOA = "Number of archives (NOA)";
    public static String N_APS = "Archives physical size (APS)";
    public static String N_PSR = "Physical size ratio (PSR)";
    public static String N_SWH = "Size without history (SWH)";
    public static String N_SOH = "Size of history (SOH)";
    public static String N_SRR = "Storage redundancy rate (SRR)";
}
