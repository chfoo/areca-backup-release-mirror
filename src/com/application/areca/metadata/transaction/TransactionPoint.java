package com.application.areca.metadata.transaction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import com.application.areca.ArecaConfiguration;
import com.application.areca.ArecaFileConstants;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.common.FileComparator;
import com.application.areca.metadata.content.ArchiveContentAdapter;
import com.application.areca.metadata.trace.ArchiveTraceAdapter;
import com.application.areca.plugins.Plugin;
import com.application.areca.plugins.PluginRegistry;
import com.application.areca.version.VersionInfos;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.iterator.FileNameComparator;
import com.myJava.object.ToStringHelper;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

/**
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
public class TransactionPoint implements Serializable {
	private static final long serialVersionUID = 4126323377242827776L;
	private static final String PROCESS_CONTEXT_FILE = "context.bin";
	private static final int DIGITS = (int)(16 - Math.log10(ArecaConfiguration.get().getTransactionSize()));

	private File rootPath;
	private int index;
	
	private File hashFile;
	private File traceFile;
	private File contentFile;
	private File sequenceFile;
	
	/**
	 * The subdirectory in which data will be stored
	 */
	private File computedPath = null;

	private transient ArchiveContentAdapter hashAdapter;
	private transient ArchiveContentAdapter contentAdapter;
	private transient ArchiveContentAdapter sequenceAdapter;
	private transient ArchiveTraceAdapter traceAdapter;

	private transient TransactionPoint previousTP;
	private transient TransactionPointHeader header;

	public TransactionPoint(File rootPath, TransactionPoint previousTP) {
		this.previousTP = previousTP;

		if (previousTP != null) {
			initAttributes(rootPath, previousTP.index + 1);
		} else {
			initAttributes(rootPath, 0);
		}
	}

	private TransactionPoint(File rootPath, int index) {
		initAttributes(rootPath, index);
	}

	public File getPath() {
		return computedPath;
	}

	private void initAttributes(File rootPath, int index) {
		this.rootPath = rootPath;
		this.index = index;
		
		String nb = "" + index;
		while (nb.length() < DIGITS) {
			nb = "0" + nb;
		}
		this.computedPath = new File(new File(rootPath, ArecaFileConstants.TRANSACTION_FILE), nb);
		
		this.hashFile = new File(this.computedPath, ArecaFileConstants.HASH_FILE);
		this.traceFile = new File(this.computedPath, ArecaFileConstants.TRACE_FILE);
		this.contentFile = new File(this.computedPath, ArecaFileConstants.CONTENT_FILE);
		this.sequenceFile = new File(this.computedPath, ArecaFileConstants.SEQUENCE_FILE);

	}

	public int getIndex() {
		return index;
	}

	/**
	 * Find the last valid transaction point in the root directory
	 */
	public static TransactionPoint findLastTransactionPoint(File rootPath) {
		File dir = new File(rootPath, ArecaFileConstants.TRANSACTION_FILE);
		if (! FileSystemManager.exists(dir)) {
			return null;
		}

		String[] transactionPoints = FileSystemManager.list(dir);
		if (transactionPoints != null) {
			Arrays.sort(transactionPoints, new FileNameComparator());

			for (int i=transactionPoints.length-1; i>=0; i--) {
				try {
					TransactionPoint tp = new TransactionPoint(rootPath, Integer.parseInt(transactionPoints[i]));
					if (tp.isCommitted()) {
						return tp;
					}
				} catch (NumberFormatException e) {
					Logger.defaultLogger().info("Ignoring " + FileSystemManager.getDisplayPath(new File(dir, transactionPoints[i])));
				}
			}
		}

		return null;
	}
	
	public String displayedName() {
		return "" + index + " (" + computedPath + ")";
	}

	/**
	 * Destroy all files related to the transaction point
	 */
	public void destroyTransactionFiles() throws IOException {
		Logger.defaultLogger().info("Deleting transaction point " + displayedName() + " ...");

		FileTool.getInstance().delete(computedPath);
	}
	
	public void writeInit(ProcessContext context) throws IOException {
		Logger.defaultLogger().info("Opening transaction point " + displayedName() + " ...");
		
		// Clean existing data
		if (FileSystemManager.exists(computedPath)) {
			FileTool.getInstance().delete(computedPath);
		}

		// Build the adapters
		hashAdapter = new ArchiveContentAdapter(hashFile, context.getHashAdapter().getGlobalPrefix());
		context.getHashAdapter().setSecondaryAdapter(hashAdapter);

		contentAdapter = new ArchiveContentAdapter(contentFile, context.getContentAdapter().getGlobalPrefix());
		context.getContentAdapter().setSecondaryAdapter(contentAdapter);

		if (context.getSequenceAdapter() != null) {
			sequenceAdapter = new ArchiveContentAdapter(sequenceFile, context.getSequenceAdapter().getGlobalPrefix());
			context.getSequenceAdapter().setSecondaryAdapter(sequenceAdapter);
		}

		traceAdapter = new ArchiveTraceAdapter(traceFile, context.getTraceAdapter().getGlobalPrefix(), context.getTraceAdapter().isTrackSymlinks());
		context.getTraceAdapter().setSecondaryAdapter(traceAdapter);

		// Init the adapters' content
		if (previousTP != null) {
			hashAdapter.bulkInit(previousTP.hashFile);
			contentAdapter.bulkInit(previousTP.contentFile);
			if (context.getSequenceAdapter() != null) {
				sequenceAdapter.bulkInit(previousTP.sequenceFile);
			}
			traceAdapter.bulkInit(previousTP.traceFile);
		}
	}

	/**
	 * Close all the adapters and mark the transaction point as "committed"
	 */
	public void writeClose(boolean commit, ProcessContext context) throws IOException {
		if (commit) {
			Logger.defaultLogger().info("Closing transaction point " + displayedName() + " ...");
		}
		
		// Close the adapters
		if (hashAdapter != null) {
			hashAdapter.close();
		}
		if (contentAdapter != null) {
			contentAdapter.close();
		}
		if (sequenceAdapter != null) {
			sequenceAdapter.close();
		}
		if (traceAdapter != null) {
			traceAdapter.close();
		}

		// Mark committed
		if (commit) {
			// Serialize the process context
			serializeProcessContext(context);
			
			File marker = new File(computedPath, ArecaFileConstants.TRANSACTION_HEADER_FILE);
			TransactionHeaderAdapter adapter = new TransactionHeaderAdapter();
			TransactionPointHeader header = new TransactionPointHeader();
			header.setDate(context.getManifest().getDate());
			header.setArecaVersion(VersionInfos.getLastVersion().getVersionId());
			header.setBackupScheme(context.getBackupScheme());
			header.setSourcesRoot(((FileSystemTarget)context.getReport().getTarget()).getSourcesRoot());
			adapter.write(header, marker);
			
			// Once we are sure that all has been written, we can destroy the previous transaction point
			if (this.previousTP != null) {
				this.previousTP.destroyTransactionFiles();
				this.previousTP = null;
			}
		}
	}

	public boolean isCommitted() {
		File marker = new File(computedPath, ArecaFileConstants.TRANSACTION_HEADER_FILE);
		return FileSystemManager.exists(marker);
	}
	
	public TransactionPointHeader readHeader() throws AdapterException {
		if (header == null) {
			TransactionHeaderAdapter adapter = new TransactionHeaderAdapter();
			File marker = new File(computedPath, ArecaFileConstants.TRANSACTION_HEADER_FILE);
			if (FileSystemManager.exists(marker)) {
				header = adapter.read(marker);
			}
		}
		return header;
	}

	public File getHashFile() {
		return hashFile;
	}

	public File getTraceFile() {
		return traceFile;
	}

	public File getContentFile() {
		return contentFile;
	}

	public File getSequenceFile() {
		return sequenceFile;
	}

	public File getRootPath() {
		return rootPath;
	}

	private void serializeProcessContext(ProcessContext context) throws IOException {
		ObjectOutputStream out = null;
		try {
			FileTool.getInstance().createDir(computedPath);
			OutputStream fout = FileSystemManager.getFileOutputStream(new File(computedPath, PROCESS_CONTEXT_FILE));
			out = new ObjectOutputStream(fout);
			out.writeObject(context);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	private static class TPObjectInputStream extends ObjectInputStream {

		public TPObjectInputStream() throws IOException, SecurityException {
			super();
		}

		public TPObjectInputStream(InputStream arg0) throws IOException {
			super(arg0);
		}

		protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			String name = desc.getName();
			Class cls = null;
			String strPlugins = "";
			try {
				cls = Class.forName(name);
			} catch (ClassNotFoundException e) {
				Iterator plugins = PluginRegistry.getInstance().getAll(Plugin.class, false).iterator();
				while (plugins.hasNext()) {
					Plugin plugin = (Plugin)plugins.next();
					
					String pluginName = plugin.getDisplayName();
					if (pluginName == null || pluginName.length() == 0) {
						pluginName = plugin.getId();
					}
					
					if (strPlugins.length() != 0) {
						strPlugins += ", ";
					}
					strPlugins += pluginName;
					
					ClassLoader cl = plugin.getClassLoader();
					if (cl != null) {
						try {
							cls = cl.loadClass(name);
							break;
						} catch (ClassNotFoundException ignored) {
						}
					}
				}
			}
			
			if (cls == null) {
				throw new ClassNotFoundException("Class " + name + " not found. (attempted to load from main class loader and the following plugins : " + strPlugins + ")");
			}
			
			return cls;
		}
	}

	public ProcessContext deserializeProcessContext(ProcessContext target) throws IOException {
		TPObjectInputStream in = null;
		try {
			try {
				InputStream fin = FileSystemManager.getFileInputStream(new File(computedPath, PROCESS_CONTEXT_FILE));
				in = new TPObjectInputStream(fin);
				return (ProcessContext)in.readObject();
			} catch (Exception e) {
				Logger.defaultLogger().error("Error while trying to read the transaction point contained in " + computedPath + ". If the error persist, please delete this directory.", e);
				throw new IOException(e);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public String toString() {
		StringBuffer sb = ToStringHelper.init(this);
		ToStringHelper.append("RootPath", rootPath, sb);
		ToStringHelper.append("Index", index, sb);
		ToStringHelper.append("Previous TP", previousTP, sb);
		ToStringHelper.append("Path", computedPath, sb);
		return ToStringHelper.close(sb);
	}
}
