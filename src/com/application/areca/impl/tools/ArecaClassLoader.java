package com.application.areca.impl.tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.myJava.util.log.Logger;

/**
 * A classloader implementation that points to the jars and zip files contained in the "libext" subdirectory of Areca
 * <BR>(ie at the same level of the "lib" directory)
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
public class ArecaClassLoader extends URLClassLoader {
	private static URL[] URLS = new URL[0];
	private static String PREFIX = "areca-";
	
	static {

		try {
			File jarDir = getArecaJarPath();
			if (jarDir == null) {
				Logger.defaultLogger().warn("No additional jar files will be loaded");
			} else {	
				//Logger.defaultLogger().fine("Looking for additional jar files in " + jarDir + " ...");
				String loadedList = "";

				// List jars that have already been loaded by Areca
				String prefix = jarDir.getAbsolutePath();

				Set alreadyLoaded = new HashSet();
				StringTokenizer stt = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
				while (stt.hasMoreTokens()) {
					String item = stt.nextToken();
					if (item.startsWith(prefix)) {
						String name = new File(item).getName();
						alreadyLoaded.add(name);
						loadedList += ", " + name;
					}
				}
				//Logger.defaultLogger().fine("Already loaded : " + loadedList.substring(2));


				// List jars to be dynamically loaded, and build the URL list
				File[] jars = jarDir.listFiles();
				if (jars != null) {
					ArrayList list = new ArrayList();
					String dynamicList = "";
					for (int i=0; i<jars.length; i++) {
						String jarName = jars[i].getName();
						if (
								(jarName.toLowerCase().endsWith(".jar") || jarName.toLowerCase().endsWith(".zip"))
								&& jarName.startsWith(PREFIX)
								&& (! alreadyLoaded.contains(jars[i].getName()))
						) {
							list.add(jars[i].toURI().toURL());
							dynamicList += ", " + jars[i].getName();
						}
					}

					URLS = (URL[])list.toArray(new URL[list.size()]);
					/*
					if (dynamicList.length() < 2) {
						Logger.defaultLogger().fine("No additional jar files detected in " + jarDir);
					} else {
						Logger.defaultLogger().fine("Additional jar files : " + dynamicList.substring(2));
					}
					*/
				}
			}
		} catch (Throwable e) {
			Logger.defaultLogger().error("Unable to instanciate dynamic classloader", e);
		}

	}

	private static File getArecaJarPath() {
		StringTokenizer stt = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
		String arecajar = null;
		while (stt.hasMoreTokens()) {
			String item = stt.nextToken();
			if (item.endsWith("\\areca.jar") || item.endsWith("/areca.jar")) {
				arecajar = item;
				break;
			}
		}

		if (arecajar == null) {
			Logger.defaultLogger().warn("Unable to locate areca jar file");
			return null;
		} else {
			File jarDir = new File(arecajar).getParentFile();
			return jarDir;
		}
	}
	
	public ArecaClassLoader() {
		super(URLS);
	}
	
	public ArecaClassLoader(ClassLoader parent) {
		super(URLS, parent);
	}
}
