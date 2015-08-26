package com.application.areca.adapters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ConfigurationSource;
import com.application.areca.TargetGroup;
import com.application.areca.Workspace;
import com.application.areca.WorkspaceItem;
import com.application.areca.adapters.read.DeprecatedTargetGroupXMLReader;
import com.application.areca.adapters.read.TargetXMLReader;
import com.application.areca.adapters.write.AbstractXMLWriter;
import com.application.areca.adapters.write.DefaultTargetGroupXMLWriter;
import com.application.areca.adapters.write.TargetXMLWriter;
import com.application.areca.impl.FileSystemTarget;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

/**
 * Singleton class used to handle serialization/deserialization operations.
 * <BR>It uses xxxXMLReader/Writer to perform these operations.
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
public class ConfigurationHandler {
	private static ConfigurationHandler INSTANCE = new ConfigurationHandler();

	public static ConfigurationHandler getInstance() {
		return INSTANCE;
	}

	private ConfigurationHandler() {
	}

	public TargetGroup readTargetGroup(
			File file, 
			com.application.areca.adapters.MissingDataListener listener,
			boolean installMedium) 
	throws AdapterException {
		return (TargetGroup)readObject(file, listener, null, installMedium, true);
	}

	public AbstractTarget readTarget(
			File file, 
			com.application.areca.adapters.MissingDataListener listener, 
			boolean readIDInfosOnly,
			boolean installMedium) 
	throws AdapterException {
		ConfigurationSource source = new ConfigurationSource(false, file);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlConfig = builder.parse(file);
			Element root = xmlConfig.getDocumentElement();

			// Read Target
			TargetXMLReader targetAdapter = new TargetXMLReader(root, installMedium);
			targetAdapter.setReadIDInfosOnly(readIDInfosOnly);
			targetAdapter.setMissingDataListener(listener);
			targetAdapter.setSource(source);
			AbstractTarget target = targetAdapter.readTarget();

			return target;
		} catch (Exception e) {
			AdapterException ex = new AdapterException(e);
			ex.setSource(FileSystemManager.getAbsolutePath(file));
			throw ex;
		}
	}

	public boolean serialize(Workspace workspace, File path, boolean removeEncryptionData, boolean isBackupCopy) 
	throws ApplicationException {
		return serialize(workspace.getContent(), path, removeEncryptionData, isBackupCopy);
	}

	public boolean serialize(TargetGroup group, File targetFile, boolean removeEncrytionData, boolean isBackupCopy) 
	throws ApplicationException {
		DefaultTargetGroupXMLWriter writer = new DefaultTargetGroupXMLWriter(removeEncrytionData, isBackupCopy);
		return writer.serialize(group, targetFile);
	}

	public boolean serialize(
			FileSystemTarget target, 
			File configDirectory, 
			boolean removeEncrytionData,
			boolean isBackupCopy) 
	throws ApplicationException {
		try {
			// Serialize Target
			StringBuffer sb = new StringBuffer();
			TargetXMLWriter writer = new TargetXMLWriter(sb, isBackupCopy);
			writer.setRemoveSensitiveData(removeEncrytionData);
			writer.setWriteXMLHeader(true);
			writer.serializeTarget(target);
			String XML = sb.toString();

			// Check that the existing file is not protected (backup copies)
			File targetFile = target.computeConfigurationFile(configDirectory, false);
			if ((!isBackupCopy) && FileSystemManager.exists(targetFile)) {
				try {
					AbstractTarget existing = readTarget(targetFile, null, false, false);
					if (existing.getLoadedFrom().isBackupCopy()) {
						throw new ApplicationException("Modifications of configuration backup copies are not allowed. If you want to use and/or modify a copy of your configuration, please use the \"import configuration\" feature and import it into a new workspace.");
					}
				} catch (AdapterException e) {
					Logger.defaultLogger().warn("Error trying to read existing configuration file. Your changes will be applied without checking your previous data.", e);
				}
			}

			// Write the XML
			FileTool.getInstance().createDir(FileSystemManager.getParentFile(targetFile));
			OutputStream fos = FileSystemManager.getFileOutputStream(targetFile);
			OutputStreamWriter fw = null;
			try {
				fw = new OutputStreamWriter(fos, AbstractXMLWriter.getEncoding());
				fw.write(XML);
			} finally {
				if (fw != null) {
					fw.close();
				}
			}

			// Check written data
			String read = ("" + FileTool.getInstance().getFileContent(targetFile, AbstractXMLWriter.getEncoding())).trim();
			if (! read.equals(XML)) {
				// The written file is not OK
				Logger.defaultLogger().warn("An error occured while writing the XML configuration on " + FileSystemManager.getDisplayPath(targetFile) + " : Original content = [" + XML + "], written content = [" + read + "]");
				return false;
			}

			return true;
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	private boolean isDeprecatedGroupConfiguration(File file) {
		try {
			DeprecatedTargetGroupXMLReader reader = new DeprecatedTargetGroupXMLReader(file, false);
			return reader.readable();
		} catch (AdapterException e) {
			Logger.defaultLogger().error(e);
			return false;
		}
	}

	public WorkspaceItem readObject(
			File file, 
			com.application.areca.adapters.MissingDataListener listener, 
			TargetGroup parent,
			boolean installMedium,
			boolean forceRead
		) throws AdapterException {
		try {
			if (FileSystemManager.isFile(file)) {
				if (FileSystemManager.getName(file).toLowerCase().endsWith(FileSystemTarget.CONFIG_FILE_EXT_DEPRECATED) && isDeprecatedGroupConfiguration(file)) {
					Logger.defaultLogger().info("Reading configuration file (deprecated group format) : " + FileSystemManager.getDisplayPath(file));
					DeprecatedTargetGroupXMLReader reader = new DeprecatedTargetGroupXMLReader(file, installMedium);
					TargetGroup group = reader.load();
					if (parent != null) {
						parent.linkChild(group);
					}
					return group;
				} else if (FileSystemManager.getName(file).toLowerCase().endsWith(FileSystemTarget.CONFIG_FILE_EXT)) {
					Logger.defaultLogger().info("Reading target configuration file : " + FileSystemManager.getDisplayPath(file));
					AbstractTarget target = readTarget(file, listener, false, installMedium);
					if (parent != null) {
						parent.linkChild(target);
					}
					return target;
				} else {
					// Logger.defaultLogger().info("Ignoring " + FileSystemManager.getDisplayPath(file));
					return null;
				}
			} else if (forceRead || ! FileSystemManager.getName(file).startsWith(".")) {
				Logger.defaultLogger().info("Reading content of " + FileSystemManager.getDisplayPath(file));
				TargetGroup group = new TargetGroup(FileSystemManager.getName(file));
				group.setLoadedFrom(new ConfigurationSource(false, file));
				String[] childrenNames = FileSystemManager.list(file);
				if (childrenNames != null) {
					for (int i=0; i<childrenNames.length; i++) {
						File child = new File(file, childrenNames[i]);
						try {
							WorkspaceItem childItem = readObject(child, listener, group, installMedium, false);
							if (childItem != null) {
								if (childItem.getLoadedFrom().isBackupCopy()) {
									group.getLoadedFrom().setBackupCopy(true);
								}
							}
						} catch (Exception e) {
							Logger.defaultLogger().error("Error while reading " + child, e);
						}
					}
				}
				if (parent != null) {
					parent.linkChild(group);
				}
				return group;
			} else {
				// Logger.defaultLogger().info("Ignoring " + FileSystemManager.getDisplayPath(file));
				return null;
			}
		} catch (AdapterException e) {
			Logger.defaultLogger().error("Error detected in " + e.getSource(), e);
			throw e;
			/*
			Application.getInstance().handleException(
					ResourceManager.instance().getLabel("error.loadworkspace.message", new Object[] {e.getMessage(), e.getSource()}),
					e
			);
			return null;
			*/
		}
	}
}
