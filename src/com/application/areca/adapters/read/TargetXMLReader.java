package com.application.areca.adapters.read;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.application.areca.AbstractTarget;
import com.application.areca.ApplicationException;
import com.application.areca.ArchiveMedium;
import com.application.areca.CheckParameters;
import com.application.areca.ConfigurationSource;
import com.application.areca.MergeParameters;
import com.application.areca.Utils;
import com.application.areca.adapters.MissingDataListener;
import com.application.areca.adapters.XMLTags;
import com.application.areca.adapters.write.XMLVersions;
import com.application.areca.context.ProcessReportWriter;
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
import com.application.areca.impl.FileSystemTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.handler.ArchiveHandler;
import com.application.areca.impl.handler.DefaultArchiveHandler;
import com.application.areca.impl.handler.DeltaArchiveHandler;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.plugins.ConfigurationPlugin;
import com.application.areca.plugins.ConfigurationPluginXMLHandler;
import com.application.areca.plugins.FileSystemPolicyXMLHandler;
import com.application.areca.plugins.PluginRegistry;
import com.application.areca.plugins.StoragePlugin;
import com.application.areca.processor.AbstractMailSendProcessor;
import com.application.areca.processor.AbstractProcessor;
import com.application.areca.processor.DeleteProcessor;
import com.application.areca.processor.FileDumpProcessor;
import com.application.areca.processor.MergeProcessor;
import com.application.areca.processor.Processor;
import com.application.areca.processor.SendMailProcessor;
import com.application.areca.processor.SendReportByMailProcessor;
import com.application.areca.processor.ShellScriptProcessor;
import com.application.areca.version.VersionInfos;
import com.myJava.configuration.FrameworkConfiguration;
import com.myJava.file.CompressionArguments;
import com.myJava.file.driver.EncryptedFileSystemDriver;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;
import com.myJava.util.xml.XMLTool;

/**
 * Adapter for target serialization / deserialization
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
public class TargetXMLReader implements XMLTags {
	private static final int DEFAULT_ZIP_MV_DIGITS = FrameworkConfiguration.getInstance().getZipMvDigits();

	protected int version = -1;
	protected Node targetNode;
	protected MissingDataListener missingDataListener;
	protected boolean readIDInfosOnly = false;
	protected boolean installMedium = true;
	protected ConfigurationSource source;

	public TargetXMLReader(Node targetNode, boolean installMedium) throws AdapterException {
		this.targetNode = targetNode;
		this.installMedium = installMedium;
	}
	
	public ConfigurationSource getSource() {
		return source;
	}

	public void setVersion(int version) {
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

	public void setSource(ConfigurationSource source) {
		this.source = source;
	}

	public FileSystemTarget readTarget() throws IOException, AdapterException, ApplicationException {
		if (version < 0) {
			// Read version
			Node versionNode = targetNode.getAttributes().getNamedItem(XMLTags.XML_VERSION);
			version = 1;
			if (versionNode != null) {
				version = Integer.parseInt(versionNode.getNodeValue());
			}  
		}

		if (version > XMLVersions.CURRENT_VERSION) {
			throw new AdapterException("Invalid XML version : This version of " + VersionInfos.APP_SHORT_NAME + " can't handle XML versions above " + XMLVersions.CURRENT_VERSION + ". You are trying to read a version " + version);
		}

		FileSystemTarget target = new FileSystemTarget();
		target.setLoadedFrom(source);

		Node id = targetNode.getAttributes().getNamedItem(XML_TARGET_ID);
		Node uid = targetNode.getAttributes().getNamedItem(XML_TARGET_UID);        
		Node name = targetNode.getAttributes().getNamedItem(XML_TARGET_NAME); 
		Node isBackupCopy = targetNode.getAttributes().getNamedItem(XML_BACKUP_COPY); 

		if (id == null && uid == null) {
			throw new AdapterException("Target UID not found : your target must have a '" + XML_TARGET_UID + "' attribute.");
		}   

		if (uid != null) {
			target.setUid(uid.getNodeValue());
		}

		if (id != null) {
			target.setId(Integer.parseInt(id.getNodeValue()));
		}

		if (name != null) {
			target.setTargetName(name.getNodeValue());
		}

		if (isBackupCopy != null) {
			target.getLoadedFrom().setBackupCopy(Boolean.valueOf(isBackupCopy.getNodeValue()).booleanValue());
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
				target.setFollowSubdirectories(Boolean.valueOf(followSubDirectoriesNode.getNodeValue()).booleanValue());
			} else {
				// Backward compatibility : fix of bug 2817721 - inverted boolean in the xml config
				followSubDirectoriesNode = targetNode.getAttributes().getNamedItem(XML_TARGET_FOLLOW_SUBDIRECTORIES_DEPREC);  
				if (followSubDirectoriesNode != null) {
					target.setFollowSubdirectories(! Boolean.valueOf(followSubDirectoriesNode.getNodeValue()).booleanValue());
				} else {
					target.setFollowSubdirectories(true);
				}
			}

			Node createSecurityCopyNode = targetNode.getAttributes().getNamedItem(XML_TARGET_CREATE_XML_SECURITY_COPY);  
			if (createSecurityCopyNode != null) {
				target.setCreateSecurityCopyOnBackup(Boolean.valueOf(createSecurityCopyNode.getNodeValue()).booleanValue());
			} else {
				target.setCreateSecurityCopyOnBackup(true);
			}

			Node fwdErrorsNode = targetNode.getAttributes().getNamedItem(XML_TARGET_FWD_PREPROC_ERRORS);  
			if (fwdErrorsNode != null) {
				target.getPreProcessors().setForwardErrors(Boolean.valueOf(fwdErrorsNode.getNodeValue()).booleanValue());
			} else {
				target.getPreProcessors().setForwardErrors(true);
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
					if (installMedium) {
						target.getMedium().install();
					}
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_DUMP)) {
					addProcessor(children.item(i), this.readDumpProcessor(children.item(i)), target);                
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_EMAIL_REPORT)) {
					addProcessor(children.item(i), this.readEmailProcessor(children.item(i), true), target);    
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_EMAIL)) {
					addProcessor(children.item(i), this.readEmailProcessor(children.item(i), false), target); 
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_SHELL)) {
					addProcessor(children.item(i), this.readShellProcessor(children.item(i)), target);                
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_MERGE)) {
					addProcessor(children.item(i), this.readMergeProcessor(children.item(i)), target);    
				} else if (child.equalsIgnoreCase(XML_PROCESSOR_DELETE)) {
					addProcessor(children.item(i), this.readDeleteProcessor(children.item(i)), target);                 
				} else if (child.equalsIgnoreCase(XML_SOURCE)) {
					readSource(sources, children.item(i));
				} else if (child.equalsIgnoreCase(XML_ADDONS)) {
					readAddons(children.item(i), target);
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

	protected void addProcessor(Node node, Processor action, AbstractTarget target) throws AdapterException {
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
			String path = pathNode.getNodeValue();
			String normalized = Utils.normalizePath(path);
			if (! path.equals(normalized)) {
				// We simply raise a warning because of backward compatibility ... 
				Logger.defaultLogger().warn("Caution : The following source path : " + path + " does not comply with Window's default naming conventions (this can lead to unexpected behaviors). The following path should be used instead : " +  normalized + ". It is highly advisable to update your target's configuration.");
			}
			sources.add(new File(path));
		}
	}

	protected void readProcessorAttributes(Node node, AbstractProcessor proc) throws AdapterException {
		Node runSchemeNode = node.getAttributes().getNamedItem(XML_PP_RUN_SCHEME);

		Node runIfOKNode = node.getAttributes().getNamedItem(XML_PP_RUN_SUCCESS);
		Node runIfWarningNode = node.getAttributes().getNamedItem(XML_PP_RUN_WARNING);
		Node runIfErrorNode = node.getAttributes().getNamedItem(XML_PP_RUN_ERROR);

		Node runBackupNode = node.getAttributes().getNamedItem(XML_PP_RUN_BACKUP);
		Node runMergeNode = node.getAttributes().getNamedItem(XML_PP_RUN_MERGE);
		Node runCheckNode = node.getAttributes().getNamedItem(XML_PP_RUN_CHECK);

		if (runIfErrorNode != null || runIfWarningNode != null || runIfOKNode != null) {
			proc.setRunIfError(runIfErrorNode != null && Boolean.valueOf(runIfErrorNode.getNodeValue()).booleanValue());
			proc.setRunIfWarning(runIfWarningNode != null && Boolean.valueOf(runIfWarningNode.getNodeValue()).booleanValue());
			proc.setRunIfOK(runIfOKNode != null && Boolean.valueOf(runIfOKNode.getNodeValue()).booleanValue());

			proc.setRunBackup(runBackupNode == null || Boolean.valueOf(runBackupNode.getNodeValue()).booleanValue());
			proc.setRunMerge(runMergeNode != null && Boolean.valueOf(runMergeNode.getNodeValue()).booleanValue());
			proc.setRunCheck(runCheckNode != null && Boolean.valueOf(runCheckNode.getNodeValue()).booleanValue());
		} else if (runSchemeNode != null) {
			// BACKWARD-COMPATIBILITY : version between 7.1.1 and 7.1.4 //
			if (XML_PP_RUN_SCHEME_ALWAYS.equals(runSchemeNode.getNodeValue())) {
				proc.setRunAlways();
			} else if (XML_PP_RUN_SCHEME_FAILURE.equals(runSchemeNode.getNodeValue())) {
				proc.setRunIfError(true);
				proc.setRunIfWarning(false);
				proc.setRunIfOK(false);
			} else if (XML_PP_RUN_SCHEME_SUCCESS.equals(runSchemeNode.getNodeValue())) {
				proc.setRunIfError(false);
				proc.setRunIfWarning(true);
				proc.setRunIfOK(true);
			} else {
				throw new AdapterException("Run rule not supported for processor " + proc.getName() + " : " + runSchemeNode.getNodeValue());
			}
			// EOF BACKWARD-COMPATIBILITY : version between 7.1.1 and 7.1.4 //
		} else {
			// BACKWARD-COMPATIBILITY : version older than 7.1.1 //
			Node failureOnlyNode = node.getAttributes().getNamedItem(XML_PP_ONLY_IF_ERROR);
			if (failureOnlyNode != null) {
				if (Boolean.valueOf(failureOnlyNode.getNodeValue()).booleanValue()) {
					proc.setRunIfError(true);
					proc.setRunIfWarning(false);
					proc.setRunIfOK(false);
				} else {
					proc.setRunAlways();
				}
			} else {
				Node failureOnlyNode_old = node.getAttributes().getNamedItem("smtp_" + XML_PP_ONLY_IF_ERROR);
				if (failureOnlyNode_old != null) {
					if (Boolean.valueOf(failureOnlyNode_old.getNodeValue()).booleanValue()) {
						proc.setRunIfError(true);
						proc.setRunIfWarning(false);
						proc.setRunIfOK(false);
					} else {
						proc.setRunAlways();
					}
				}
			}
			// EOF BACKWARD-COMPATIBILITY : version older than 7.1.1 //
		}
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

		Node statsNode = node.getAttributes().getNamedItem(XML_PP_ADD_STATS);
		if (statsNode != null) {
			pp.setAppendStatistics(Boolean.valueOf(statsNode.getNodeValue()).booleanValue());
		}

		Node listStoredFilesNode = node.getAttributes().getNamedItem(XML_PP_LIST_STORED_FILES);
		if (listStoredFilesNode != null) {
			pp.setAppendStoredFiles(Boolean.valueOf(listStoredFilesNode.getNodeValue()).booleanValue());
		}

		Node maxListedFilesNode = node.getAttributes().getNamedItem(XML_PP_MAX_LISTED_FILES);
		if (maxListedFilesNode != null) {
			pp.setMaxStoredFiles(Long.parseLong(maxListedFilesNode.getNodeValue()));
		} else {
			pp.setMaxStoredFiles(ProcessReportWriter.MAX_LISTED_FILES);
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
		boolean keepDeletedEntries = false;
		if (keepNode != null) {
			keepDeletedEntries = Boolean.valueOf(keepNode.getNodeValue()).booleanValue();
		}
		pp.setParams(new MergeParameters(keepDeletedEntries, false, null));

		//CHECK ARCHIVE
		Node checkNode = node.getAttributes().getNamedItem(XML_PP_MERGE_CHECK);
		boolean check = true;
		if (checkNode != null) {
			check = Boolean.valueOf(checkNode.getNodeValue()).booleanValue();
		}
		pp.setCheckParams(new CheckParameters(check, true, true, false, null));

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

	protected Processor readEmailProcessor(Node node, boolean appendReport) throws AdapterException {
		AbstractMailSendProcessor pp = appendReport ? (AbstractMailSendProcessor)new SendReportByMailProcessor() : (AbstractMailSendProcessor)new SendMailProcessor();

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
		
		String password = XMLTool.extractPassword(XML_PP_EMAIL_PASSWORD, node);
		if (password != null) {
			pp.setPassword(password);
		}

		readProcessorAttributes(node, pp);

		Node smtpsNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_SMTPS);
		if (smtpsNode != null) {
			pp.setSmtps(Boolean.valueOf(smtpsNode.getNodeValue()).booleanValue());
		}
		
		Node disableStarttlsNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_DISABLE_STARTTLS);
		if (disableStarttlsNode != null) {
			pp.setDisableSTARTTLS(Boolean.valueOf(disableStarttlsNode.getNodeValue()).booleanValue());
		} else {
			pp.setDisableSTARTTLS(true); // backward compatibility
		}

		Node titleNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_TITLE);
		if (titleNode != null) {
			pp.setTitle(titleNode.getNodeValue());
		}

		Node introNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_INTRO);
		if (introNode != null) {
			pp.setMessage(introNode.getNodeValue());
		}

		Node fromNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_FROM);
		if (fromNode != null) {
			pp.setFrom(fromNode.getNodeValue());
		}

		if (appendReport) {
			SendReportByMailProcessor smpp = (SendReportByMailProcessor)pp;

			Node statsNode = node.getAttributes().getNamedItem(XML_PP_ADD_STATS);
			if (statsNode != null) {
				smpp.setAppendStatistics(Boolean.valueOf(statsNode.getNodeValue()).booleanValue());
			}

			Node listStoredFilesNode = node.getAttributes().getNamedItem(XML_PP_LIST_STORED_FILES);
			if (listStoredFilesNode != null) {
				smpp.setAppendStoredFiles(Boolean.valueOf(listStoredFilesNode.getNodeValue()).booleanValue());
			}

			Node maxListedFilesNode = node.getAttributes().getNamedItem(XML_PP_MAX_LISTED_FILES);
			if (maxListedFilesNode != null) {
				smpp.setMaxStoredFiles(Long.parseLong(maxListedFilesNode.getNodeValue()));
			} else {
				smpp.setMaxStoredFiles(ProcessReportWriter.MAX_LISTED_FILES);
			}
		}


		pp.setRecipients(recipientsNode.getNodeValue());
		pp.setSmtpServer(smtpNode.getNodeValue());        
		return pp;
	}

	public static boolean isOverwrite(Node mediumNode) {
		Node overwriteNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_OVERWRITE);
		return (overwriteNode != null && overwriteNode.getNodeValue().equalsIgnoreCase("true"));   
	}

	protected void readAddons(Node node, AbstractTarget target) throws AdapterException {
		NodeList children = node.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);

			if (child.getNodeName().equalsIgnoreCase(XML_ADDON)) {
				Node idNode = child.getAttributes().getNamedItem(XML_ADDON_ID);
				if (idNode == null) {
					throw new AdapterException(XML_ADDON_ID + " not found.");
				} else {
					String id = idNode.getNodeValue();
					ConfigurationPlugin plugin = (ConfigurationPlugin)PluginRegistry.getInstance().getById(id);
					if (plugin == null) {
						Logger.defaultLogger().warn("Unable to load the following plugin : " + id + ". It will be ignored. (file : " + this.source.getSource() + ")");
					} else {
						ConfigurationPluginXMLHandler handler = plugin.buildConfigurationPluginXMLHandler();
						target.registerAddon(handler.read(child, this, target));
					}
				}
			}
		}
	}

	protected ArchiveMedium readMedium(Node mediumNode, AbstractTarget target) throws IOException, AdapterException, ApplicationException {
		Node typeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TYPE);
		if (typeNode == null) {
			throw new AdapterException("Medium type not found : your medium must have a '" + XML_MEDIUM_TYPE + "' attribute.");
		}  

		// backward compatibility
		Node trackDirsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_DIRS);
		if (trackDirsNode != null) {
			boolean trackDirs = trackDirsNode.getNodeValue().equalsIgnoreCase("true");
			((FileSystemTarget)target).setTrackEmptyDirectories(trackDirs); 
		}
		// EOF backward compatibility

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

		Node maxThroughputNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_MAX_THROUGHPUT);
		if (maxThroughputNode != null) {
			double mt = Double.parseDouble(maxThroughputNode.getNodeValue());
			medium.setMaxThroughput(mt);
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

			if (child.equalsIgnoreCase(XML_TRANSACTION_CONFIG)) {
				Node transactionNode = children.item(i);

				boolean useTransactions = true;
				long transactionSize = -1;

				Node enabledNode = transactionNode.getAttributes().getNamedItem(XML_USE_TRANSACTIONS);
				if (enabledNode != null) {
					useTransactions = Boolean.valueOf(enabledNode.getNodeValue()).booleanValue();
				}

				if (useTransactions) {
					Node sizeNode = transactionNode.getAttributes().getNamedItem(XML_TRANSACTION_SIZE);
					if (sizeNode != null) {
						transactionSize = Long.parseLong(sizeNode.getNodeValue());
					}
				}

				medium.setUseTransactions(useTransactions);
				if (useTransactions) {
					medium.setTransactionSize(transactionSize);
				}
			}
		}

		// Default handler
		if (medium.getHandler() == null) {
			medium.setHandler(new DefaultArchiveHandler());
		}

		medium.setCompressionArguments(compression);
		medium.setFileSystemPolicy(storage);
		medium.setEncryptionPolicy(encrArgs);
		medium.setImage(isOverwrite(mediumNode));

		Node inspectContentNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_INSPECT_FILE_CONTENT);
		if (inspectContentNode != null) {
			medium.setInspectFileContent(Boolean.valueOf(inspectContentNode.getNodeValue()).booleanValue());
		}

		if (medium.isImage() && medium.getHandler() instanceof DeltaArchiveHandler) {
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
			AbstractTarget target
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

		StoragePlugin plugin = (StoragePlugin)PluginRegistry.getInstance().getById(policyId);
		FileSystemPolicyXMLHandler handler = plugin.buildFileSystemPolicyXMLHandler();
		handler.setVersion(version);
		return handler.read(mediumNode, target, this);
	}

	protected EncryptionPolicy readEncryptionPolicy(
			Node mediumNode, 
			AbstractTarget target
			) throws IOException, AdapterException, ApplicationException {
		Node encryptedNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTED);
		boolean isEncrypted = (encryptedNode != null && encryptedNode.getNodeValue().equalsIgnoreCase("true"));   

		Node encryptionAlgoNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONALGO);
		Node encryptNamesNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTNAMES);
		Node wrapNamesNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_WRAPNAMES);

		String encryptionKey = XMLTool.extractPassword(XML_MEDIUM_ENCRYPTIONKEY, mediumNode);
		String encryptionAlgo = encryptionAlgoNode != null ? encryptionAlgoNode.getNodeValue() : null;   
		Boolean encryptNames = encryptNamesNode != null ? Boolean.valueOf(encryptNamesNode.getNodeValue()) : null;  
		String nameWrappingMode = EncryptedFileSystemDriver.WRAP_DEFAULT;
		if (wrapNamesNode != null) {
			nameWrappingMode = wrapNamesNode.getNodeValue();
		}

		if (isEncrypted && encryptionKey == null) {
			if (missingDataListener != null) {
				EncryptionPolicy encrData = missingDataListener.missingEncryptionDataDetected(target, encryptionAlgo, encryptNames, nameWrappingMode, this.source);
				if (encrData != null) {
					encryptionAlgo = encrData.getEncryptionAlgorithm();
					encryptionKey = encrData.getEncryptionKey();
					encryptNames = new Boolean(encrData.isEncryptNames());
				}
			}
		}

		if (isEncrypted && encryptionKey == null) { // Second check .... after missingDataListener invocation.
			throw new AdapterException("No encryption key found : your medium must have a '" + XML_MEDIUM_ENCRYPTIONKEY + "' attribute because it is encrypted (" + XML_MEDIUM_ENCRYPTED + " = true).");
		}

		EncryptionPolicy encrArgs = new EncryptionPolicy();
		encrArgs.setEncrypted(isEncrypted);
		encrArgs.setEncryptionAlgorithm(encryptionAlgo);
		encrArgs.setEncryptNames(encryptNames == null ? true : encryptNames.booleanValue());
		encrArgs.setEncryptionKey(encryptionKey);
		encrArgs.setNameWrappingMode(nameWrappingMode);


		// Encryption management for version 3 and higher id not compatible 
		// with previous versions. (except for the AES "RAW" key scheme)
		if (
				version <= 2 
				&& isEncrypted 
				&& (! encryptionAlgo.equals(EncryptionConfiguration.AES_RAW))
				&& (! encryptionAlgo.equals(EncryptionConfiguration.AES256_RAW))
				) {
			throw new AdapterException("\nError reading target \"" + target.getName() + "\" in group \"" + target.getParent().getName() + "\" (" + target.getParent().getFullPath() + ") :\nEncryption management has been refactored in version 6.1 of " + VersionInfos.APP_SHORT_NAME + ", and your configuration has been generated with a previous version of " + VersionInfos.APP_SHORT_NAME + ". As a result, it is not compatible with your current version (" + VersionInfos.getLastVersion().getVersionId() + ").\nYou must either :\n- re-create your target/targetgroup and use one of the available encryption algorithms, or\n- re-install a previous version of " + VersionInfos.APP_SHORT_NAME + " (6.0.7 or before).");
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


		String normalized = Utils.normalizePath(filter.getStringParameters());
		if (! filter.getStringParameters().equals(normalized)) {
			// We simply raise a warning because of backward compatibility ... 
			Logger.defaultLogger().warn("Caution : The following directory filter path : " + filter.getStringParameters() + " does not comply with Window's default naming conventions (this can lead to unexpected behaviors). The following path should be used instead : " +  normalized + ". It is highly advisable to update your target's configuration.");
		}

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
		Node logicalNotNode = filterNode.getAttributes().getNamedItem(XML_FILTER_LOGICAL_NOT);
		if (logicalNotNode == null) {
			logicalNotNode = filterNode.getAttributes().getNamedItem(XML_FILTER_LOGICAL_NOT_DEPRECATED);
		}
		boolean isLogicalNot = (logicalNotNode != null && logicalNotNode.getNodeValue().equalsIgnoreCase("true"));
		filter.setLogicalNot(isLogicalNot);

		if (paramNode != null) {
			filter.acceptParameters(paramNode.getNodeValue());
		}
	}
}