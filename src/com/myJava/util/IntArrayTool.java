package com.myJava.util;

/**
 * Classe utilitaire regroupant des fonctions dédiées aux tableaux d'entiers
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -4899974077672581254
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

public abstract class IntArrayTool {

    /**
     * Somme les valeurs d'un tableau
     */
    public static int sum(int[] values) {
        int total = 0;
        for (int i=0; i<values.length; i++) {
            total += values[i];
        }
        return total;
    }
    
    /**
     * Max des valeurs d'un tableau
     */
    public static int max(int[] values) {
    	if (values == null || values.length == 0) {
    		return 0;
    	}
        int max = values[0];
        for (int i=1; i<values.length; i++) {
        	if (max < values[i]){
        		max = values[i];
        	}
        }
        return max;
    }
    
    /**
     * Min des valeurs d'un tableau
     */
    public static int min(int[] values) {
    	if (values == null || values.length == 0) {
    		return 0;
    	}
        int min = values[0];
        for (int i=1; i<values.length; i++) {
        	if (min > values[i]){
        		min = values[i];
        	}
        }
        return min;
    }

    /**
     * Retourne le contenu sous forme de liste
     * [1 2 6 9] donnera "1,2,6,9" si le séparateur
     * fourni est ",".
     */
    public static String getContentList(int[] values, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<values.length; i++) {
            sb.append(separator);
            sb.append(values[i]);
        }

        return sb.toString().substring(separator.length());
    }

    /**
     * Retourne le contenu sous forme de liste
     * Séparateur = ","
     */
    public static String getContentList(int[] values) {
        return IntArrayTool.getContentList(values, ",");
    }

    /**
     * Indique si le tableau contient la valeur spécifiée
     */
    public static boolean contains(int[] values, int value) {
        for (int i=0; i<values.length; i++) {
            if (values[i] == value) {
                return true;
            }
        }
        return false;
    }
}