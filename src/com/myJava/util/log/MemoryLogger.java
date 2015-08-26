package com.myJava.util.log;

import com.myJava.util.schedule.ScheduledTask;

/**
 * T�che de logging de m�moire
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

public class MemoryLogger extends ScheduledTask {

	// Logging par d�faut toutes les 4 minutes
    public MemoryLogger() {
        this(4 * 60);
    }
	
    public MemoryLogger(long nbSec) {
        super(nbSec);
    }

    public void execute() {
        super.execute();
        
        long freeMem = Runtime.getRuntime().freeMemory();
        long totalMem = Runtime.getRuntime().totalMemory();

        Logger.defaultLogger().info("Max available memory : " + Runtime.getRuntime().maxMemory());
        Logger.defaultLogger().info("Global reserved memory : " + totalMem);
        Logger.defaultLogger().info("Free memory : " + freeMem);
        Logger.defaultLogger().info("Used memory : " + (totalMem - freeMem));
    }
}