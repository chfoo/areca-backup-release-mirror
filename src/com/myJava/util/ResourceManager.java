package com.myJava.util;

import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Classe chargée de gérer des fichiers de ressources.
 * Utile pour les applications localisées.
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

public class ResourceManager {

    /**
     * La Hashtable contenant les dictionnaires.
     */
    protected Hashtable resources;

    /**
     * Constructeur par défaut
     */
    public ResourceManager() {
        this.resources = new Hashtable();
    }

    /**
     * Retourne le dictionnaire demandé, ou tente de l'initialiser s'il
     * ne l'a pas encore été.
     */
    protected ResourceBundle getResource(String name, String language) throws IllegalArgumentException {
        // Vérification des paramètres
        if (language == null || language.length() != 2) {
            throw new IllegalArgumentException("Le code langue est obligatoire et correspond au code ISO-639 de la langue. (2 caractères)");
        }

        // On tente de récupérer le dictionnaire depuis le cache
        ResourceBundle res = (ResourceBundle)(resources.get(name + "." + language.toLowerCase()));

        // S'il n'est pas dans le cache, on l'instancie et on le stocke
        if (res == null) {
            Locale loc = new Locale(language.toLowerCase(), "");
            res = ResourceBundle.getBundle(name, loc);

            // Stockage en cache
            this.store(res, name, language);
        }

        return res;
    }

    /**
     * Stocke le dictionnaire proposé en cache
     */
    protected void store(ResourceBundle resource, String name, String language) {
        this.resources.put(name + "." + language.toLowerCase(), resource);
    }

    /**
     * Retourne la traduction demandée
     */
    public String getString(String key, String resourceName, String language) throws IllegalArgumentException {
        if (key == null || key.trim().length() == 0) {
            return "";
        } else {
            return this.getResource(resourceName, language).getString(key);
        }
    }
}