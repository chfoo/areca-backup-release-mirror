package com.myJava.util.errors;

import java.util.Hashtable;

import com.myJava.util.collections.SimpleEnumeration;

/**
 * Classe représentant une erreur de données.
 * Utile pour les ActionReports
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

public class ActionError implements SimpleEnumeration {

    /**
     * Code
     */
    private int code;
    
    private String message;

    /**
     * Liste de champs associés à l'erreur
     */
    private Hashtable fields;

    /**
     * Constructeur
     */
    public ActionError(int code) {
        this.code = code;
        this.fields = new Hashtable();
    }
    
    /**
     * Constructeur
     */
    public ActionError(int code, String message) {
        this.code = code;
        this.message = message;
        this.fields = new Hashtable();
    }    
    
    public String getMessage() {
        return message;
    }

    /**
     * Ajout d'un champ
     */
    public void addField(String key, Object value) {
        this.fields.put(key, value);
    }

    /**
     * Retourne la valeur du champ
     */
    public Object getValue(String key) {
        return this.fields.get(key);
    }

    /**
     * Retourne la valeur du champ sous forme de chaîne de caractères
     */
    public String getString(String key) {
        return this.getValue(key).toString();
    }

    /**
     * Retourne le code d'erreur
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Redéfinition toString
     */
    public String toString() {
        return "" + this.code;
    }
}