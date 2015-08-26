package com.myJava.file.driver;

/**
 * Exception jetée lorsque l'on tente d'initialiser un driver sur un point de montage 
 * alors qu'un autre driver a déjà été initialisé et utilisé.
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
public class DriverAlreadySetException extends Exception {

    public DriverAlreadySetException() {
        super();
    }

    public DriverAlreadySetException(String message) {
        super(message);
    }

    public DriverAlreadySetException(Throwable cause) {
        super(cause);
    }

    public DriverAlreadySetException(String message, Throwable cause) {
        super(message, cause);
    }
}
