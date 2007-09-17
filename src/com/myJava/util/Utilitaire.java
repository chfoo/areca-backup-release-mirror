/**
 * Classe hébergeant des fonctions utilitaires.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : 3732974506771028333
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

package com.myJava.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import com.myJava.file.FileSystemManager;
import com.myJava.util.log.Logger;

public abstract class Utilitaire {
    
    private static Short[] POOLED_SHORTS;
    private static final Random rndGenerator = new Random(System.currentTimeMillis());

    static {
        POOLED_SHORTS = new Short[21];
        for (short i = -10; i<POOLED_SHORTS.length - 10; i++) {
            POOLED_SHORTS[i+10] = new Short(i);
        }
    }
    
    public static Short buildOptimizedShort(short s) {
        if (s < -10 || s > 10) {
            return new Short(s);
        } else {
            return POOLED_SHORTS[s+10];
        }
    }
    
    /**
     * Remplacement des occurences d'une chaine par une autre chaine
     */
    public static String replace(String chaine, String orig, String newC) {

        if (chaine == null) {
            return null;
        }

        if (orig != null && !(orig.equals("")) && newC != null) {
            StringBuffer stb = new StringBuffer();
            int index = chaine.indexOf(orig);
            int oldIndex = 0;

            while (index != -1) {
                stb.append(chaine.substring(oldIndex, index));
                stb.append(newC);
                oldIndex = index + orig.length();
                index = chaine.indexOf(orig, index + orig.length());
            }

            stb.append(chaine.substring(oldIndex));
            return new String(stb);
        } else {
            return chaine;
        }
    }

    /**
     * comptage des occurences d'une chaine dans une autre chaine
     */
    public static int count(String chaine, String str) {
        int index = chaine.indexOf(str);
        int nbOcc = 0;

        while (index != -1) {
            nbOcc ++;
            index = chaine.indexOf(str, index+str.length());
        }
        return nbOcc;
    }
    
    /**
     * comptage des occurences d'une chaine dans une autre chaine
     */
    public static int count(String chaine, int startIndex, char c) {
        if (chaine == null) {
            return 0;
        }
        
        int l = chaine.length();
        int cpt = 0;
        for (int i=startIndex; i<l; i++) {
            if (chaine.charAt(i) == c) {
                cpt++;
            }
        }
        
        return cpt;
    }

    /**
     * Retourne un nombre aléatoire entre -1 et 1
     */
    public static double getRnd() {
        return ((rndGenerator.nextDouble() - .5) * 2.);
    }

    /**
     * Retourne un entier long aléatoire
     */
    public static long getRndLong() {
        return (rndGenerator.nextLong());
    }

    /**
     * Retourne la chaine comprise entre les deux balises (en tenant compte de l'offset)
     *
     */
    public static String subString(String orig, String balise1, int offset, String balise2) {
        if (orig != null) {
            boolean found = true;
            int i1 = 0;
            if (balise1 != null) {
                int tmp = orig.indexOf(balise1);
                if (tmp == -1) {
                    found = false;
                }
                i1 = tmp + balise1.length() + offset;
            }

            int i2 = 0;
            if (balise2 != null && found) {
                i2 = orig.indexOf(balise2, i1);
                if (i2 == -1) {
                    found = false;
                }
            }

            if (found) {
                return orig.substring(i1, i2);
            } else {
                Logger.defaultLogger().warn("Element [" + balise1 + "], [" + offset + "], [" + balise2 + "] non trouvé dans [" + orig + "]", "Utilitaire.subString()");
                return "";
            }
        } else {
            return null;
        }
    }

    /**
     * explose une chaine en sous-chaines et les stocke dans un tableau
     */
    public static Vector split(String orig, String pattern) {

        if (orig == null || pattern == null) {
            return null;
        }

        int currentIndex = orig.indexOf(pattern);
        int lastIndex = 0;
        Vector elements = new Vector();

        while (currentIndex != -1) {
            elements.addElement(orig.substring(lastIndex, currentIndex));

            lastIndex = currentIndex+pattern.length();
            currentIndex = orig.indexOf(pattern, lastIndex);
        }

        elements.addElement(orig.substring(lastIndex, orig.length()));

        return elements;
    }

    /**
     * Trim Left sur un caractère générique
     */
    public static String gLTrim(String str, char c) {
        if (str == null) {
            return null;
        } else {
            int l = str.length();
            int i;
            for (i=0; (i<l && str.charAt(i) == c); i++) {
            }

            if (i == l) {
                return "";
            } else {
                return str.substring(i);
            }
        }
    }

    /**
     * Extrait le nom d'une classe à partir de son chemin complet
     * (package.souspackage.nomClasse)
     */
    public static String getClassName(String path) {
        if (path == null) {
            return null;
        }
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return path;
        } else {
            return path.substring(index+1);
        }
    }

    /**
     * Transforme un vecteur d'objets en tableau de string
     */
    public static String[] vectorToStringArray(Vector v) {
        int nb = v.size();
        String[] array = new String[nb];
        for (int i=0; i<nb; i++) {
            array[i] = v.elementAt(i).toString();
        }
        return array;
    }


    /**
     * Méthode utilitaire permettant de logger l'intégralité de ce qui est
     * émis sur un flux d'entrée.
     * Utile pour le débuggage.
     */
    public static void logStreamContent(InputStream is) {
        String content = "";
        try {
            Logger.defaultLogger().info("----------------- DEBUT CONTENU -----------------");
            while(true) {
                int v = is.read();
                if (v == -1) {
                    break;
                } else {
                    content += (char)v;
                }
            }
        } catch (IOException e) {
            Logger.defaultLogger().error("Erreur durant la lecture du flux", e, "Utilitaire.logStreamContent");
        } finally {
            Logger.defaultLogger().info(content);
            Logger.defaultLogger().info("----------------- FIN CONTENU -----------------");
        }
    }

    /**
     * Retourne les clefs de la hashtable sous forme de liste
     * [1 2 6 9] donnera la chaîne "1,2,6,9" si le séparateur
     * fourni est ",".
     */
    public static String getContentList(Hashtable values, String separator) {
        StringBuffer sb = new StringBuffer();
        Enumeration keys = values.keys();
        while(keys.hasMoreElements()) {
            sb.append(separator);
            sb.append(keys.nextElement().toString());
        }

        if (sb.length() == 0) {
            return "";
        } else {
            return sb.toString().substring(separator.length());
        }
    }
    
    /**
     * Retourne le contenu sous forme de liste
     * Séparateur = ","
     */
    public static String getContentList(Hashtable values) {
        return Utilitaire.getContentList(values, ",");
    } 
    
    /**
     * Ajuste la taille du String s à la taille spécifiée (size)
     * - Si la taille de s est inférieure à la taille spécifiée, s est tronqué
     * - Sinon, s est complété à l'aide du nombre de caractères "completion" nécessaires pour atteindre s
     * 
     * "dockRight" indique si c'est la partie gauche ou droite de s qui est conservée.
     */
    public static String adjustSize(String s, int size, char completion, boolean dockRight) {
        String ret;
        
        if (dockRight) {
            // Cas 1 : on ajuste en conservant la partie droite
            if (s.length() > size) {
                ret = s.substring(s.length() - size, size);
            } else {
                ret = s;
                for (int i=0; i < size - s.length(); i++) { 
                    ret = completion + ret;
                }
            }
        } else {
            // Cas 2 : on ajuste en conservant la partie droite        
            if (s.length() > size) {
                ret = s.substring(0, size);
            } else {
                ret = s;
                for (int i=0; i < size - s.length(); i++) { 
                    ret = ret + completion;
                }
            } 
        }
         
        return ret;
    }   
    
    /**
     * Duplique nb fois le string s
     */
    public static String duplicate(String s, int nb) {
        String ret = "";
        for (int i=0; i<nb; i++) {
            ret += s;
        }
        return ret;
    }

    /**
     * Normalise le nom du fichier.
     * Tous les caractères ne faisant pas partie de acceptedChars sont
     * remplacés par replacementChar.
     * La comparaison ne tient pas compte de la casse.
     * 
     * @param fileName
     * @param acceptedChars
     * @param replacementChar
     * @return
     */
    public static String normalizeFileName(String fileName, String acceptedChars, char replacementChar) {
        String lower = fileName.toLowerCase();
        char[] copy = new char[fileName.length()];
        for (int i=0; i<fileName.length(); i++) {
            if (acceptedChars.indexOf(lower.charAt(i)) == -1) {
                copy[i] = replacementChar; 
            } else {
                copy[i] = fileName.charAt(i);
            }
        }
        return new String(copy);
    }
    
    /**
     * Supprime les "/" en début et fin de nom de fichier
     */
    public static String trimSlashes(String orig) {
        if (orig == null || orig.length() == 0) {
            return orig;
        } else if (orig.length() == 1) {
            if (orig.charAt(0) == '/') {
                return "";
            } else {
                return orig;
            }
        } else {
            boolean t0 = orig.charAt(0) == '/';
            boolean tn = orig.charAt(orig.length() - 1) == '/';
            if (t0 && tn) {
                return orig.substring(1, orig.length() - 1);
            } else if (t0) {
                return orig.substring(1);
            } else if (tn) {
                return orig.substring(0, orig.length() - 1);
            } else {
                return orig;
            }
        }
    }
    
    public static boolean passFilter(String entry, String[] filter) {
        for (int i=0; i<filter.length; i++) {
            if (filter[i].length() == 0 || entry.equals(filter[i]) || entry.startsWith(filter[i] + "/")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Identique à normalizeFileName à ceci près que les parties existantes du nom de fichier
     * (répertoires existant déjà sur le disque) ne sont pas impactés
     *  
     * @param fileName
     * @param acceptedChars
     * @param replacementChar
     * @return
     */
    public static File normalizeIfNotExists(File file, String acceptedChars, char replacementChar) {
        if (FileSystemManager.exists(file)) {
            return file;
        } else {
            return new File(FileSystemManager.getAbsoluteFile(normalizeIfNotExists(FileSystemManager.getParentFile(file), acceptedChars, replacementChar)), normalizeFileName(FileSystemManager.getName(file), acceptedChars, replacementChar));
        }
    }
    

    /**
     * Fonction utilitaire : lit le flux d'entrée jusqu'à tomber sur un CRLF (13-10)
     * OU jusqu'à atteindre la limite du buffer.
     *
     * Retourne -1 si la fin du flux est atteinte ou le nombre de bytes lus.
     */
    public static int readLine(InputStream is, byte[] buff) throws IOException {
        byte b=0, pb;
        int v;
        int offset = 0;
        while (true) {
            pb = b;
            v = is.read();
            if (v == -1) {
                if (offset == 0) {
                    // Fin de stream atteint dès le début de la lecture
                    return -1;
                } else {
                    // Fin de stream attteint en cours de lecture
                    return offset;
                }
            } else {
                b = (byte)v;
                buff[offset++] = b;


                // On est tombé sur un CRLF ou on atteint la fin du buffer
                if ((pb == 13 && b == 10) || offset == buff.length) {
                    return offset;
                }
            }
        }
    }

    /**
     * Lit une ligne du flux
     * Retourne :
     * - NULL si le flux est vide
     * - un tableau de bytes, de taille <= size, correspondant aux bytes lus
     */
    public static byte[] readLineBA(InputStream is, int size) throws IOException {
        byte[] tmp = new byte[size];
        int nb = readLine(is, tmp);
        if (nb == -1) {
            return null;
        } else {
            if (nb == size) {
                return tmp;
            } else {
                byte[] nArray = new byte[nb];
                for (int i=0; i<nb; i++) {
                    nArray[i] = tmp[i];
                }
                return nArray;
            }
        }
    }

    /**
     * Lit le flux d'entrée et retourne les données sous forme de string
     * la lecture s'arrête dès que la fin du flux est rencontrée ou qu'un CRLF est rencontré.
     */
    public static String readLineString(InputStream is) throws IOException {
        String ret = "";

        while (true) {
            byte[] tmp = readLineBA(is, 4*1024);
            if (tmp == null) {
                break;
            }
            ret += new String(tmp);

            if (tmp[tmp.length-1] == 10 && tmp[tmp.length-2] == 13) {
                break;
            }
        }

        return ret;
    }
    
}