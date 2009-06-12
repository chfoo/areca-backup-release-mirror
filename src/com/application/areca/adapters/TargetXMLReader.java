package com.application.areca.adapters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.AbstractRecoveryTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.TargetGroup;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileDateArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileOwnerArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.filter.SpecialFileFilter;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.EncryptionConfiguration;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.handler.ArchiveHandler;
import com.application.areca.impl.handler.DefaultArchiveHandler;
import com.application.areca.impl.handler.DeltaArchiveHandler;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.plugins.FileSystemPolicyXMLHandler;
import com.application.areca.plugins.StoragePlugin;
import com.application.areca.plugins.StoragePluginRegistry;
import com.application.areca.processor.AbstractProcessor;
import com.application.areca.processor.DeleteProcessor;
import com.application.areca.processor.FileDumpProcessor;
import com.application.areca.processor.MailSendProcessor;
import com.application.areca.processor.MergeProcessor;
import com.application.areca.processor.Processor;
import com.application.areca.processor.ShellScriptProcessor;
import com.application.areca.version.VersionInfos;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.CompressionArguments;

/**
 * Adapter for target serialization / deserialization
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class TargetXMLReader implements XMLTags {
	private static final int DEFAULT_ZIP_MV_DIGITS = FrameworkConfiguration.getInstance().getZipMvDigits();

	protected int version;
	protected TargetGroup group;
	protected Node targetNode;
	protected MissingDataListener missingDataListener = null;
	protected boolean readIDInfosOnly = false;

	public TargetXMLReader(Node targetNode, TargetGroup group, int version) throws AdapterException {
		this.targetNode = targetNode;
		this.group = group;
		this.version = version;
	}

	public void setReadIDInfosOnly(boolean readIDInfosOnly) {
		this.readIDInfosOnly = readIDInfosOnly;
	}

	public void setMissingDataListener(MissingDataListener missingDataListener) {
		this.missingDataListener = missingDataListener;
	}

	public MissingDataListener getMissingDataListener() {
		return missingDataListener;
	}

	public FileSystemRecoveryTarget readTarget() throws IOException, AdapterException, ApplicationException {
		Node id = targetNode.getAttributes().getNamedItem(XML_TARGET_ID);
		Node uid = targetNode.getAttributes().getNamedItem(XML_TARGET_UID);        
		Node name = targetNode.getAttributes().getNamedItem(XML_TARGET_NAME);     

		if (id == null) {
			throw new AdapterException("Target ID not found : your target must have a '" + XML_TARGET_ID + "' attribute.");
		}   

		String strUid = null;
		if (uid != null) {
			strUid = uid.getNodeValue();
		}

		FileSystemRecoveryTarget target = new FileSystemRecoveryTarget();
		target.setId(Integer.parseInt(id.getNodeValue()));
		target.setUid(strUid);
		target.setGroup(group);

		if (name != null) {
			target.setTargetName(name.getNodeValue());
		}

		if (! readIDInfosOnly) {

			// BACKWARD COMPATIBILITY
			Node baseDir = targetNode.getAttributes().getNamedItem(XML_TARGET_BASEDIR);
			if (baseDir != null) {
				HashSet src = new HashSet();
				src.add(new File(baseDir.getNodeValue()));
				target.setSources(src);
			}      
			// EOF BACKWARD COMPATIBILITY

			Node commentsNode = targetNode.getAttributes().getNamedItem(XML_TARGET_DESCRIPTION);
			if (commentsNode != null) {
				target.setComments(commentsNode.getNodeValue());
			}  

			Node trackEmptyDirsNode = targetNode.getAttributes().getNamedItem(XML_TARGET_TRACK_EMPTY_DIRS);
			if (trackEmptyDirsNode != null) {
				boolean trackEmptyDirs = trackEmptyDirsNode.getNodeValue().equalsIgnoreCase("true");
				target.setTrackEmptyDirectories(trackEmptyDirs); 
			}

			Node followSymLinksNode = targetNode.getAttributes().getNamedItem(XML_TARGET_FOLLOW_SYMLINKS);  
			if (followSymLinksNode != null) {
				target.setTrackSymlinks(! Boolean.valueOf(followSymLinksNode.getNodeValue()).booleanValue());
			} else {
				target.setTrackSymlinks(false);
			}

			Node followSubDirectoriesNode = targetNode.getAttributes().getNamedItem(XML_TARGET_FOLLOW_SUBDIRECTORIES);  
			if (followSubDirectoriesNode != null) {
				target.setFollowSubdirectories(! Boolean.valueOf(followSubDirectoriesNode.getNodeValue()).booleanValue());
			} else {
				target.setFollowSubdirectories(true);
			}

			Node createSecurityCopyNode = targetNode.getAttributes().getNamedItem(XML_TARGET_CREATE_XML_SECURITY_COPY);  
			if (createSecurityCopyNode != null) {
				target.setCreateSecurityCopyOnBackup(Boolean.valueOf(createSecurityCopyNode.getNodeValue()).booleanValue());
			} else {
				target.setCreateSecurityCopyOnBackup(true);
			}

			HashSet sources = new HashSet();

			NodeList children = targetNode.getChildNodes();
			for (int i=0; i<children.getLength(); i++) {
				String child = children.item(i).getNodeName();

				// ===== BACKWARD COMPATIBILITY =====
					if (child.equalsIgnoreCase(XML_FILTER_DIRECTORY)) {
						target.addFilter(this.readDirectoryArchiveFilter( children.item(i)));
					} else if (child.equalsIgnoreCase(XML_FILTER_FILEEXTENSION)) {
						target.addFilter(this.readFileExtensionArchiveFilter( children.item(i)));   
					} else if (child.equalsIgnoreCase(XML_FILTER_REGEX)) {
						target.addFilter(this.readRegexArchiveFilter( children.item(i)));  
					} else if (child.equalsIgnoreCase(XML_FILTER_FILESIZE)) {
						target.addFilter(this.readFileSizeArchiveFilter( children.item(i)));  
					} else if (child.equalsIgnoreCase(XML_FILTER_OWNER)) {
						target.addFilter(this.readFileOwnerArchiveFilter( children.item(i)));  
					} else if (child.equalsIgnoreCase(XML_FILTER_LINK)) {
						target.addFilter(this.readLinkFilter( children.item(i)));  
					} else if (child.equalsIgnoreCase(XML_FILTER_TP)) {
						target.addFilter(this.readSpecFileFilter(children.item(i))); 
					} else if (child.equalsIgnoreCase(XML_FILTER_LOCKED)) {
						target.addFilter(this.readLockedFileFilter( children.item(i)));         
					} else if (child.equalsIgnoreCase(XML_FILTER_OWNER)) {
						target.addFilter(this.readFileOwnerArchiveFilter( children.item(i)));                        
					} else if (child.equalsIgnoreCase(XML_FILTER_FILEDATE)) {
						target.addFilter(this.readFileDateArchiveFilter( children.item(i)));  
						// ===== EOF BACKWARD COMPATIBILITY =====

					} else if (child.equalsIgnoreCase(XML_FILTER_GROUP)) {
						target.setFilterGroup(this.readFilterGroup(children.item(i)));  
					} else if (child.equalsIgnoreCase(XML_MEDIUM)) {
						target.setMedium(this.readMedium(children.item(i), target), false);      
						target.getMedium().install();
					} else if (child.equalsIgnoreCase(XML_PROCESSOR_DUMP)) {
						addProcessor(children.item(i), this.readDumpProcessor(children.item(i)), target);                
					} else if (child.equalsIgnoreCase(XML_PROCESSOR_EMAIL)) {
						addProcessor(children.item(i), this.readEmailProcessor(children.item(i)), target);                
					} else if (child.equalsIgnoreCase(XML_PROCESSOR_SHELL)) {
						addProcessor(children.item(i), this.readShellProcessor(children.item(i)), target);                
					} else if (child.equalsIgnoreCase(XML_PROCESSOR_MERGE)) {
						addProcessor(children.item(i), this.readMergeProcessor(children.item(i)), target);    
					} else if (child.equalsIgnoreCase(XML_PROCESSOR_DELETE)) {
						addProcessor(children.item(i), this.readDeleteProcessor(children.item(i)), target);                 
					} else if (child.equalsIgnoreCase(XML_SOURCE)) {
						readSource(sources, children.item(i));
					}
			}

			if (sources.size() > 0 && baseDir != null) {
				throw new AdapterException("The '" + XML_TARGET_BASEDIR + "' attribute is deprecated. It shall not be used anymore. Use '" + XML_SOURCE + "' elements instead.");
			} else if (baseDir == null) {
				target.setSources(sources);
			}
		}

		return target;
	}

	protected void addProcessor(Node node, Processor action, AbstractRecoveryTarget target) throws AdapterException {
		Node executeAfterNode = node.getAttributes().getNamedItem(XML_PP_AFTER);
		boolean executeAfter = true;
		if (executeAfterNode != null) {
			executeAfter = Boolean.valueOf(executeAfterNode.getNodeValue()).booleanValue();
		}

		if (executeAfter) {
			target.getPostProcessors().addProcessor(action);
		} else {
			target.getPreProcessors().addProcessor(action);            
		}
	}

	protected void readSource(Set sources, Node node) throws AdapterException {
		Node pathNode = node.getAttributes().getNamedItem(XML_SOURCE_PATH);
		if (pathNode == null) {
			throw new AdapterException("A '" + XML_SOURCE_PATH + "' attribute must be set.");
		} else {
			sources.add(new File(pathNode.getNodeValue()));
		}
	}

	protected void readProcessorAttributes(Node node, AbstractProcessor proc) throws AdapterException {
		Node runSchemeNode = node.getAttributes().getNamedItem(XML_PP_RUN_SCHEME);
		short runScheme = Processor.RUN_SCHEME_ALWAYS;
		if (runSchemeNode != null) {
			if (XML_PP_RUN_SCHEME_ALWAYS.equals(runSchemeNode.getNodeValue())) {
				runScheme = Processor.RUN_SCHEME_ALWAYS;
			} else if (XML_PP_RUN_SCHEME_FAILURE.equals(runSchemeNode.getNodeValue())) {
				runScheme = Processor.RUN_SCHEME_FAILURE;
			} else if (XML_PP_RUN_SCHEME_SUCCESS.equals(runSchemeNode.getNodeValue())) {
				runScheme = Processor.RUN_SCHEME_SUCCESS;
			} else {
				throw new AdapterException("Run rule not supported for processor " + proc.getName() + " : " + runSchemeNode.getNodeValue());
			}
		} else {
			// BACKWARD-COMPATIBILITY //
			Node failureOnlyNode = node.getAttributes().getNamedItem(XML_PP_ONLY_IF_ERROR);
			if (failureOnlyNode != null) {
				if (Boolean.valueOf(failureOnlyNode.getNodeValue()).booleanValue()) {
					runScheme = Processor.RUN_SCHEME_FAILURE;
				}
			} else {
				Node failureOnlyNode_old = node.getAttributes().getNamedItem("smtp_" + XML_PP_ONLY_IF_ERROR);
				if (failureOnlyNode_old != null) {
					if (Boolean.valueOf(failureOnlyNode_old.getNodeValue()).booleanValue()) {
						runScheme = Processor.RUN_SCHEME_FAILURE;
					}
				}
			}
			// EOF BACKWARD-COMPATIBILITY //
		}

		proc.setRunScheme(runScheme);
	}

	protected Processor readDumpProcessor(Node node) throws AdapterException {
		Node paramNode = node.getAttributes().getNamedItem(XML_PP_DUMP_DIRECTORY);
		if (paramNode == null) {
			throw new AdapterException("Dump directory not found for File Dump Processor. A '" + XML_PP_DUMP_DIRECTORY + "' attribute must be set.");
		}          
		FileDumpProcessor pp = new FileDumpProcessor();
		readProcessorAttributes(node, pp);
		pp.setDestinationFolder(new File(paramNode.getNodeValue()));

		Node nameNode = node.getAttributes().getNamedItem(XML_PP_DUMP_NAME);
		if (nameNode != null) {
			pp.setReportName(nameNode.getNodeValue());
		}

		return pp;
	}

	protected Processor readShellProcessor(Node node) throws AdapterException {
		Node scriptNode = node.getAttributes().getNamedItem(XML_PP_SHELL_SCRIPT);
		if (scriptNode == null) {
			throw new AdapterException("Shell script file not found for Shell Processor. A '" + XML_PP_SHELL_SCRIPT + "' attribute must be set.");
		}     

		ShellScriptProcessor pp = new ShellScriptProcessor();
		pp.setCommand(scriptNode.getNodeValue());
		readProcessorAttributes(node, pp);

		Node paramNode = node.getAttributes().getNamedItem(XML_PP_SHELL_PARAMS);
		if (paramNode != null) {
			pp.setCommandParameters(paramNode.getNodeValue());
		}

		return pp;
	}

	protected Processor readMergeProcessor(Node node) throws AdapterException {
		MergeProcessor pp = new MergeProcessor();

		// TO
		Node toNode = node.getAttributes().getNamedItem(XML_PP_MERGE_TO_DELAY);
		if (toNode == null) {
			toNode = node.getAttributes().getNamedItem(XML_PP_DELAY);  // Backward compatibility
			if (toNode == null) {
				throw new AdapterException("Merge delay not found for merge processor. A '" + XML_PP_DELAY + "' attribute must be set.");
			}
		}
		pp.setToDelay(Integer.parseInt(toNode.getNodeValue()));

		// FROM
		Node fromNode = node.getAttributes().getNamedItem(XML_PP_MERGE_FROM_DELAY);
		if (fromNode != null) {
			pp.setFromDelay(Integer.parseInt(fromNode.getNodeValue()));
		}  

		//KEEP DELETED ENTRIES
		Node keepNode = node.getAttributes().getNamedItem(XML_PP_MERGE_KEEP_DELETED);
		if (keepNode != null) {
			pp.setKeepDeletedEntries(Boolean.valueOf(keepNode.getNodeValue()).booleanValue());
		} else {
			pp.setKeepDeletedEntries(false);
		}
		readProcessorAttributes(node, pp);
		return pp;
	}

	protected Processor readDeleteProcessor(Node node) throws AdapterException {
		DeleteProcessor pp = new DeleteProcessor();
		Node delayNode = node.getAttributes().getNamedItem(XML_PP_DELAY);
		if (delayNode == null) {
			throw new AdapterException("Delay not found for delete processor. A '" + XML_PP_DELAY + "' attribute must be set.");
		}  
		readProcessorAttributes(node, pp);
		pp.setDelay(Integer.parseInt(delayNode.getNodeValue()));
		return pp;
	}

	protected Processor readEmailProcessor(Node node) throws AdapterException {
		MailSendProcessor pp = new MailSendProcessor();

		Node recipientsNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_RECIPIENTS);
		if (recipientsNode == null) {
			throw new AdapterException("Recipient list not found for Email Processor. A '" + XML_PP_EMAIL_RECIPIENTS + "' attribute must be set.");
		}         
		Node smtpNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_SMTP);
		if (smtpNode == null) {
			throw new AdapterException("Smtp host not found for Email Processor. A '" + XML_PP_EMAIL_SMTP + "' attribute must be set.");
		}       

		Node userNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_USER);
		if (userNode != null) {
			pp.setUser(userNode.getNodeValue());
		}
		Node passwordNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_PASSWORD);
		if (passwordNode != null) {
			pp.setPassword(passwordNode.getNodeValue());
		}

		readProcessorAttributes(node, pp);

		Node smtpsNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_SMTPS);
		if (smtpsNode != null) {
			pp.setSmtps(Boolean.valueOf(smtpsNode.getNodeValue()).booleanValue());
		}

		Node titleNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_TITLE);
		if (titleNode != null) {
			pp.setTitle(titleNode.getNodeValue());
		}

		Node introNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_INTRO);
		if (introNode != null) {
			pp.setIntro(introNode.getNodeValue());
		}

		Node fromNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_FROM);
		if (fromNode != null) {
			pp.setFrom(fromNode.getNodeValue());
		}

		pp.setRecipients(recipientsNode.getNodeValue());
		pp.setSmtpServer(smtpNode.getNodeValue());        
		return pp;
	}

	protected static boolean isOverwrite(Node mediumNode) {
		Node overwriteNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_OVERWRITE);
		return (overwriteNode != null && overwriteNode.getNodeValue().equalsIgnoreCase("true"));   
	}

	protected ArchiveMedium readMedium(Node mediumNode, AbstractRecoveryTarget target) throws IOException, AdapterException, ApplicationException {
		Node typeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TYPE);
		if (typeNode == null) {
			throw new AdapterException("Medium type not found : your medium must have a '" + XML_MEDIUM_TYPE + "' attribute.");
		}      

		// backward compatibility
		Node trackDirsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_DIRS);
		if (trackDirsNode != null) {
			boolean trackDirs = trackDirsNode.getNodeValue().equalsIgnoreCase("true");
			((FileSystemRecoveryTarget)target).setTrackEmptyDirectories(trackDirs); 
		}
		// EOF backward compatibility

		Node trackPermsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_PERMS);
		boolean trackPerms = (trackPermsNode != null && trackPermsNode.getNodeValue().equalsIgnoreCase("true"));   

		EncryptionPolicy encrArgs = readEncryptionPolicy(mediumNode, target);
		FileSystemPolicy storage = readFileSystemPolicy(mediumNode, target);

		CompressionArguments compression = new CompressionArguments();
		Node volumeSizeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_VOLUME_SIZE);
		if (volumeSizeNode != null) {
			long volumeSize = Long.parseLong(volumeSizeNode.getNodeValue());
			int volumeDigits = DEFAULT_ZIP_MV_DIGITS;

			Node volumeDigitsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_VOLUME_DIGITS);
			if (volumeDigitsNode != null) {
				volumeDigits = Integer.parseInt(volumeDigitsNode.getNodeValue());
			}

			compression.setMultiVolumes(volumeSize, volumeDigits);
		}

		Node commentNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_COMMENT);
		if (commentNode != null) {
			compression.setComment(commentNode.getNodeValue());
		}

		Node levelNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_LEVEL);
		if (levelNode != null) {
			compression.setLevel(Integer.parseInt(levelNode.getNodeValue()));
		}

		Node addExtensionNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_EXTENSION);
		if (addExtensionNode != null) {
			compression.setAddExtension(Boolean.valueOf(addExtensionNode.getNodeValue()).booleanValue());
		}

		Node charsetNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_CHARSET);
		if (charsetNode != null) {
			compression.setCharset(Charset.forName(charsetNode.getNodeValue()));
		}

		Node z64Node = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_Z64);
		if (z64Node != null) {
			compression.setUseZip64(Boolean.valueOf(z64Node.getNodeValue()).booleanValue());
		}

		AbstractIncrementalFileSystemMedium medium;

		if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP) || typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP64)) {
			medium = new IncrementalZipMedium();
			compression.setCompressed(true);

			// BACKWARD COMPATIBILITY //
			if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP64)) {
				compression.setUseZip64(true);
			}
			// EOF BACKWARD COMPATIBILITY //
		} else if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_DIR)) {
			medium = new IncrementalDirectoryMedium();   

			Node fileCompressionNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_FILECOMPRESSION);
			if (fileCompressionNode != null) {
				compression.setCompressed(Boolean.valueOf(fileCompressionNode.getNodeValue()).booleanValue());
			}
		}  else {
			throw new AdapterException("Unknown medium : " + typeNode.getNodeValue());
		}

		NodeList children = mediumNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			String child = children.item(i).getNodeName();

			if (child.equalsIgnoreCase(XML_HANDLER)) {
				if (medium.getHandler() != null) {
					throw new AdapterException("Handler already set. You can't define more than one handler per medium.");
				}
				medium.setHandler(readHandler(children.item(i)));
			}
		}

		// Default handler
		if (medium.getHandler() == null) {
			medium.setHandler(new DefaultArchiveHandler());
		}

		medium.setCompressionArguments(compression);
		medium.setFileSystemPolicy(storage);
		medium.setEncryptionPolicy(encrArgs);
		medium.setOverwrite(isOverwrite(mediumNode));
		medium.setTrackPermissions(trackPerms); 

		if (medium.isOverwrite() && medium.getHandler() instanceof DeltaArchiveHandler) {
			throw new AdapterException("Illegal state : 'Delta' archive mode is incompatible with 'image' targets.");
		}

		return medium;
	}

	protected ArchiveHandler readHandler(Node handlerNode) throws AdapterException {
		Node typeNode = handlerNode.getAttributes().getNamedItem(XML_HANDLER_TYPE);
		if (typeNode == null || XML_HANDLER_TYPE_STANDARD.equals(typeNode.getNodeValue())) {
			return new DefaultArchiveHandler();
		} else if (XML_HANDLER_TYPE_DELTA.equals(typeNode.getNodeValue())) {
			return new DeltaArchiveHandler();
		} else {
			throw new AdapterException("Unsupported archive handler : " + typeNode.getNodeValue());
		}
	}


	protected FileSystemPolicy readFileSystemPolicy(
			Node mediumNode, 
			AbstractRecoveryTarget target
	) throws IOException, AdapterException, ApplicationException {
		Node policyNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_POLICY);
		String policyId;
		if (policyNode != null) {
			policyId = policyNode.getNodeValue();
		} else {
			// Backward compatible read
			Node pathNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVEPATH_DEPRECATED);
			if (pathNode != null) {
				policyId = POLICY_HD;
			} else {
				policyId = POLICY_FTP;                
			}
		}

		StoragePlugin plugin = StoragePluginRegistry.getInstance().getById(policyId);
		FileSystemPolicyXMLHandler handler = plugin.buildFileSystemPolicyXMLHandler();
		handler.setVersion(version);
		return handler.read(mediumNode, target, this);
	}

	protected EncryptionPolicy readEncryptionPolicy(
			Node mediumNode, 
			AbstractRecoveryTarget target
	) throws IOException, AdapterException, ApplicationException {
		Node encryptedNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTED);
		boolean isEncrypted = (encryptedNode != null && encryptedNode.getNodeValue().equalsIgnoreCase("true"));   

		Node encryptionKeyNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONKEY);
		String encryptionKey = encryptionKeyNode != null ? encryptionKeyNode.getNodeValue() : null;   

		Node encryptionAlgoNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONALGO);
		String encryptionAlgo = encryptionAlgoNode != null ? encryptionAlgoNode.getNodeValue() : null;   

		Node encryptNamesNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTNAMES);
		boolean encryptNames = encryptNamesNode == null ? true : encryptNamesNode.getNodeValue().equalsIgnoreCase("true");  

		if (isEncrypted && encryptionKey == null) { // No check for the encryptionAlgorithm because we use a default one if none is specified.
			if (missingDataListener != null) {
				Object[] encrData = (Object[])missingDataListener.missingEncryptionDataDetected(target);
				if (encrData != null) {
					encryptionAlgo = (String)encrData[0];
					encryptionKey = (String)encrData[1];
					encryptNames = ((Boolean)encrData[2]).booleanValue();
				}
			}
		}

		if (isEncrypted && encryptionKey == null) { // Second check .... after missingDataListener invocation.
			throw new AdapterException("No encryption key found : your medium must have a '" + XML_MEDIUM_ENCRYPTIONKEY + "' attribute because it is encrypted (" + XML_MEDIUM_ENCRYPTED + " = true).");
		}

		EncryptionPolicy encrArgs = new EncryptionPolicy();
		encrArgs.setEncrypted(isEncrypted);
		encrArgs.setEncryptionAlgorithm(encryptionAlgo);
		encrArgs.setEncryptNames(encryptNames);
		encrArgs.setEncryptionKey(encryptionKey);

		// Encryption management for version 3 and higher id not compatible 
		// with previous versions. (except for the AES "RAW" key scheme)
		if (
				version <= 2 
				&& isEncrypted 
				&& (! encryptionAlgo.equals(EncryptionConfiguration.AES_RAW))
				&& (! encryptionAlgo.equals(EncryptionConfiguration.AES256_RAW))
		) {
			throw new AdapterException("\nError reading target \"" + target.getTargetName() + "\" in group \"" + target.getGroup().getName() + "\" (" + target.getGroup().getSourceFile().getAbsolutePath() + ") :\nEncryption management has been refactored in version 6.1 of Areca, and your configuration has been generated with a previous version of Areca. As a result, it is not compatible with your current version (" + VersionInfos.getLastVersion().getVersionId() + ").\nYou must either :\n- re-create your target/targetgroup and use one of the available encryption algorithms, or\n- re-install a previous version of Areca (6.0.7 or before).");
		}

		return encrArgs;
	}

	protected FilterGroup readFilterGroup(Node filterNode) throws AdapterException {  
		FilterGroup grp = new FilterGroup();
		initFilter(grp, filterNode, null);

		// Operator
		Node operatorNode = filterNode.getAttributes().getNamedItem(XML_FILTER_GROUP_OPERATOR);
		boolean isAnd = (operatorNode == null || operatorNode.getNodeValue().equalsIgnoreCase(XML_FILTER_GROUP_OPERATOR_AND));
		grp.setAnd(isAnd);

		// Components
		NodeList children = filterNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			String child = children.item(i).getNodeName();

			// BACKWARD COMPATIBILITY
			if (child.equalsIgnoreCase(XML_FILTER_DIRECTORY)) {
				grp.addFilter(this.readDirectoryArchiveFilter(children.item(i)));
			} else if (child.equalsIgnoreCase(XML_FILTER_FILEEXTENSION)) {
				grp.addFilter(this.readFileExtensionArchiveFilter(children.item(i)));   
			} else if (child.equalsIgnoreCase(XML_FILTER_REGEX)) {
				grp.addFilter(this.readRegexArchiveFilter(children.item(i)));  
			} else if (child.equalsIgnoreCase(XML_FILTER_FILESIZE)) {
				grp.addFilter(this.readFileSizeArchiveFilter(children.item(i)));  
			} else if (child.equalsIgnoreCase(XML_FILTER_LINK)) {
				grp.addFilter(this.readLinkFilter(children.item(i)));  
			} else if (child.equalsIgnoreCase(XML_FILTER_TP)) {
				grp.addFilter(this.readSpecFileFilter(children.item(i)));  
			} else if (child.equalsIgnoreCase(XML_FILTER_LOCKED)) {
				grp.addFilter(this.readLockedFileFilter(children.item(i)));                  
			} else if (child.equalsIgnoreCase(XML_FILTER_FILEDATE)) {
				grp.addFilter(this.readFileDateArchiveFilter(children.item(i)));  
			} else if (child.equalsIgnoreCase(XML_FILTER_OWNER)) {
				grp.addFilter(this.readFileOwnerArchiveFilter(children.item(i)));                 
			} else if (child.equalsIgnoreCase(XML_FILTER_GROUP)) {
				grp.addFilter(this.readFilterGroup(children.item(i)));  
			}
		}        

		return grp;
	}

	protected FileDateArchiveFilter readFileDateArchiveFilter(Node filterNode) throws AdapterException {
		Node paramNode = filterNode.getAttributes().getNamedItem(XML_FILTER_PARAM);
		if (paramNode == null) {
			throw new AdapterException("Filter date not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg '2006_07_17').");
		}          
		FileDateArchiveFilter filter = new FileDateArchiveFilter();
		initFilter(filter, filterNode, paramNode);
		return filter;
	}

	protected FileSizeArchiveFilter readFileSizeArchiveFilter(Node filterNode) throws AdapterException {
		Node paramNode = filterNode.getAttributes().getNamedItem(XML_FILTER_PARAM);
		if (paramNode == null) {
			throw new AdapterException("Maximum size not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg '> 1024').");
		}          
		FileSizeArchiveFilter filter = new FileSizeArchiveFilter();
		initFilter(filter, filterNode, paramNode);
		return filter;
	}

	protected FileOwnerArchiveFilter readFileOwnerArchiveFilter(Node filterNode) throws AdapterException {
		Node paramNode = filterNode.getAttributes().getNamedItem(XML_FILTER_PARAM);
		if (paramNode == null) {
			throw new AdapterException("Owner attributes not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg 'root:root').");
		}          
		FileOwnerArchiveFilter filter = new FileOwnerArchiveFilter();
		initFilter(filter, filterNode, paramNode);
		return filter;
	}

	/**
	 * Backward compatibility
	 */
	protected SpecialFileFilter readLinkFilter(Node filterNode) throws AdapterException {     
		SpecialFileFilter filter = new SpecialFileFilter();
		initFilter(filter, filterNode, null);
		filter.setLink(true);
		return filter;
	}

	protected SpecialFileFilter readSpecFileFilter(Node filterNode) throws AdapterException {     
		SpecialFileFilter filter = new SpecialFileFilter();
		initFilter(filter, filterNode, null);

		Node node;

		node = filterNode.getAttributes().getNamedItem(XML_FILTER_TP_LINK);
		filter.setLink(node != null && node.getNodeValue().equalsIgnoreCase("true"));

		node = filterNode.getAttributes().getNamedItem(XML_FILTER_TP_BLOCKSPECFILE);
		filter.setBlockSpecFile(node != null && node.getNodeValue().equalsIgnoreCase("true"));

		node = filterNode.getAttributes().getNamedItem(XML_FILTER_TP_CHARSPECFILE);
		filter.setCharSpecFile(node != null && node.getNodeValue().equalsIgnoreCase("true"));

		node = filterNode.getAttributes().getNamedItem(XML_FILTER_TP_PIPE);
		filter.setPipe(node != null && node.getNodeValue().equalsIgnoreCase("true"));

		node = filterNode.getAttributes().getNamedItem(XML_FILTER_TP_SOCKET);
		filter.setSocket(node != null && node.getNodeValue().equalsIgnoreCase("true"));

		return filter;
	}

	protected LockedFileFilter readLockedFileFilter(Node filterNode) throws AdapterException {        
		LockedFileFilter filter = new LockedFileFilter();
		initFilter(filter, filterNode, null);
		return filter;
	}

	protected RegexArchiveFilter readRegexArchiveFilter(Node filterNode) throws AdapterException {    
		Node regexNode = filterNode.getAttributes().getNamedItem(XML_FILTER_RG_PATTERN);
		if (regexNode == null) {
			throw new AdapterException("Regex not found : your filter must have a '" + XML_FILTER_RG_PATTERN + "' attribute.");
		}          
		RegexArchiveFilter filter = new RegexArchiveFilter();
		initFilter(filter, filterNode, null);
		filter.setRegex(regexNode.getNodeValue());

		Node modeNode = filterNode.getAttributes().getNamedItem(XML_FILTER_RG_MODE);
		if (modeNode != null) {
			filter.setScheme(modeNode.getNodeValue());
		}    

		Node matchAllNode = filterNode.getAttributes().getNamedItem(XML_FILTER_RG_FULL_MATCH);
		if (matchAllNode != null) {
			filter.setMatch(matchAllNode != null && matchAllNode.getNodeValue().equalsIgnoreCase("true"));
		}   

		return filter;
	}

	protected DirectoryArchiveFilter readDirectoryArchiveFilter(Node filterNode) throws AdapterException {
		Node directoryNode = filterNode.getAttributes().getNamedItem(XML_FILTER_DIR_PATH);
		if (directoryNode == null) {
			throw new AdapterException("Directory not found : your filter must have a '" + XML_FILTER_DIR_PATH + "' attribute.");
		}          
		DirectoryArchiveFilter filter = new DirectoryArchiveFilter();
		initFilter(filter, filterNode, directoryNode);
		return filter;
	}

	protected FileExtensionArchiveFilter readFileExtensionArchiveFilter(Node filterNode) throws AdapterException {
		FileExtensionArchiveFilter filter = new FileExtensionArchiveFilter();
		initFilter(filter, filterNode, null);

		NodeList children = filterNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			String nodeType = children.item(i).getNodeName();     
			if (nodeType.equalsIgnoreCase(XML_FILTER_EXTENSION)) {
				filter.addExtension(children.item(i).getChildNodes().item(0).getNodeValue());
			}
		}

		return filter;
	}    

	protected void initFilter(ArchiveFilter filter, Node filterNode, Node paramNode) {
		Node excludeNode = filterNode.getAttributes().getNamedItem(XML_FILTER_EXCLUDE);
		boolean isExclude = (excludeNode != null && excludeNode.getNodeValue().equalsIgnoreCase("true"));
		filter.setExclude(isExclude);

		if (paramNode != null) {
			filter.acceptParameters(paramNode.getNodeValue());
		}
	}
}