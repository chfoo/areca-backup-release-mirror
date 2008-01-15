package com.application.areca.indicator;

/**
 * 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 1926729655347670856
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
public interface IndicatorTypes {

    public static Integer T_NOF = new Integer(1);
    public static Integer T_SFS = new Integer(2);
    public static Integer T_NOA = new Integer(3);
    public static Integer T_APS = new Integer(4);
    public static Integer T_ALS = new Integer(5);
    public static Integer T_PSR = new Integer(6);
    public static Integer T_SWH = new Integer(7);
    public static Integer T_SOH = new Integer(8);
    public static Integer T_SRR = new Integer(9);

    
    public static String N_NOF = "Number of source files (NOF)";
    public static String N_SFS = "Source files size (SFS)";
    public static String N_NOA = "Number of archives (NOA)";
    public static String N_APS = "Archives physical size (APS)";
    public static String N_ALS = "Archives logical size (ALS)";
    public static String N_PSR = "Physical size ratio (PSR)";
    public static String N_SWH = "Size without history (SWH)";
    public static String N_SOH = "Size of history (SOH)";
    public static String N_SRR = "Storage redundancy rate (SRR)";
    
    
    public static String D_NOF = "Total number of source files, as of last archive date.";
    public static String D_SFS = "Total size of the source files, as of last archive date.";
    public static String D_NOA = "Total number of archives.";
    public static String D_APS = "Size of the archives on disk. (this indicator includes compression ratio or size overcost due to encryption)";
    public static String D_ALS = "Size of the files contained in archives. (without compression ratio or size overcost due to encryption)";
    public static String D_PSR = "Compression ratio. (or size overcost due to compression)\nPSR = APS / ALS\nPSR < 1 for compressed archives.\nPSR >= 1 for encrypted archives.";
    public static String D_SWH = "Estimated physical size of the source files as of last archive date, without history.\nIt is the minimum possible size of the archive, which can be obtained if all archives are merged into one unique archive.\nSWH = PSR * SFS";
    public static String D_SOH = "Physical size on disk used by the history. (overcost of history data, in kb)\nSOH = APS - SWH\nSOH = 0 if there is no history.\nSOH > 0 if an history is maintained.";
    public static String D_SRR = "Ratio of history size compared to the source files size.\nThis indicator represents the history overcost, in % of the source data size.\nSRR = (ALS / SFS) - 1\nSRR = 0 if there is no history.\nSRR > 0 if an history is maintained.";
}
