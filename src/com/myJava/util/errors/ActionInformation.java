package com.myJava.util.errors;

/**
 * Classe représentant une information retournée par une action (par exemple requête SQL).
 * Ca peut être un id d'enregistrement créé, un nombre d'enregistrements supprimés, etc.
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

public class ActionInformation {

    /**
     * Type d'information
     */
    private String type;

    /**
     * Valeur de l'information
     */
    private Object value;

    /**
     * Constructeur
     */
    public ActionInformation(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Constructeur
     */
    public ActionInformation(Object value) {
        this(null, value);
    }

    public String getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }
}