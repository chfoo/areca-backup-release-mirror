package com.myJava.util.history;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;


/**
 * Interface définisant un historique d'événements.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public interface History {

    public void addEntry(HistoryEntry entry) throws IOException;
    
    /**
     * Retourne une map clef/valeur où :
     * <BR>- Clef = GregorianCalendar
     * <BR>- Valeur = HistoryEntry (entry)
     */
    public HashMap getContent();
    
    public GregorianCalendar[] getOrderedKeys();
    
    public HistoryEntry getEntry(GregorianCalendar date);
    
    /**
     * Force l'écriture de l'historique sur le disque 
     */
    public void flush() throws IOException;
    
    /**
     * Removes all entries from the history
     */
    public void clear();
    
    /**
     * Ne modifie pas le contenu de l'historique; se contente de supprimer les données éventuellement écrites sur le support (fichier, base de données).
     * <BR>Ces données peuvent donc être réécrites par appel à la méthode "flush()" 
     */
    public void clearData();
    
    /**
     * Charge l'historique à partir du support (par ex disque ou base de données) 
     */
    public void load() throws IOException;
    
    public void updateLocation(Object newLocation) throws IOException;
    
    /**
     * Retourne la date de la dernière entrée enregistrée dans l'historique 
     */
    public GregorianCalendar getLastEntryDate();
    
    /**
     * Imports the content of the source history
     */
    public void importHistory(History source) throws IOException;
}