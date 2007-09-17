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
import com.application.areca.RecoveryProcess;
import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.DirectoryArchiveFilter;
import com.application.areca.filter.FileDateArchiveFilter;
import com.application.areca.filter.FileExtensionArchiveFilter;
import com.application.areca.filter.FileSizeArchiveFilter;
import com.application.areca.filter.FilterGroup;
import com.application.areca.filter.LinkFilter;
import com.application.areca.filter.LockedFileFilter;
import com.application.areca.filter.RegexArchiveFilter;
import com.application.areca.impl.AbstractIncrementalFileSystemMedium;
import com.application.areca.impl.FileSystemRecoveryTarget;
import com.application.areca.impl.IncrementalDirectoryMedium;
import com.application.areca.impl.IncrementalZipMedium;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.impl.policy.FileSystemPolicy;
import com.application.areca.plugins.StoragePlugin;
import com.application.areca.plugins.StoragePluginRegistry;
import com.application.areca.postprocess.FileDumpPostProcessor;
import com.application.areca.postprocess.MailSendPostProcessor;
import com.application.areca.postprocess.MergePostProcessor;
import com.application.areca.postprocess.PostProcessor;
import com.application.areca.postprocess.ShellScriptPostProcessor;

/**
 * Adaptateur pour la sérialisation / désérialisation XML.
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
public class TargetXMLReader implements XMLTags {
    protected RecoveryProcess process;
    protected Node targetNode;
    protected MissingDataListener missingDataListener = null;
    
    public TargetXMLReader(Node targetNode, RecoveryProcess process) throws AdapterException {
        this.targetNode = targetNode;
        this.process = process;
    }
    
    public void setMissingDataListener(MissingDataListener missingDataListener) {
        this.missingDataListener = missingDataListener;
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
        target.setProcess(process);
        
        // BACKWARD COMPATIBILITY
        Node baseDir = targetNode.getAttributes().getNamedItem(XML_TARGET_BASEDIR);
        if (baseDir != null) {
            HashSet src = new HashSet();
            src.add(new File(baseDir.getNodeValue()));
            target.setSources(src);
        }      
        // EOF BACKWARD COMPATIBILITY
        
        if (name != null) {
            target.setTargetName(name.getNodeValue());
        }
        
        Node commentsNode = targetNode.getAttributes().getNamedItem(XML_TARGET_DESCRIPTION);
        if (commentsNode != null) {
            target.setComments(commentsNode.getNodeValue());
        }  

        Node followSymLinksNode = targetNode.getAttributes().getNamedItem(XML_TARGET_FOLLOW_SYMLINKS);  
        if (followSymLinksNode != null) {
            target.setTrackSymlinks(! Boolean.valueOf(followSymLinksNode.getNodeValue()).booleanValue());
        } else {
            target.setTrackSymlinks(false);
        }
        
        HashSet sources = new HashSet();
        
        NodeList children = targetNode.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            String child = children.item(i).getNodeName();
            
            // ===== BACKWARD COMPATIBILITY =====
            if (child.equalsIgnoreCase(XML_FILTER_DIRECTORY)) {
                target.addFilter(this.readDirectoryArchiveFilter(children.item(i)));
            } else if (child.equalsIgnoreCase(XML_FILTER_FILEEXTENSION)) {
                target.addFilter(this.readFileExtensionArchiveFilter(children.item(i)));   
            } else if (child.equalsIgnoreCase(XML_FILTER_REGEX)) {
                target.addFilter(this.readRegexArchiveFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_FILESIZE)) {
                target.addFilter(this.readFileSizeArchiveFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_LINK)) {
                target.addFilter(this.readLinkFilter(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_FILTER_LOCKED)) {
                target.addFilter(this.readLockedFileFilter(children.item(i)));                  
            } else if (child.equalsIgnoreCase(XML_FILTER_FILEDATE)) {
                target.addFilter(this.readFileDateArchiveFilter(children.item(i)));  
            // ===== EOF BACKWARD COMPATIBILITY =====

            } else if (child.equalsIgnoreCase(XML_FILTER_GROUP)) {
                target.setFilterGroup(this.readFilterGroup(children.item(i)));  
            } else if (child.equalsIgnoreCase(XML_MEDIUM)) {
                target.setMedium(this.readMedium(children.item(i), target), false);      
                target.getMedium().install();
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_DUMP)) {
                target.getPostProcessors().addPostProcessor(this.readDumpProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_EMAIL)) {
                target.getPostProcessors().addPostProcessor(this.readEmailProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_SHELL)) {
                target.getPostProcessors().addPostProcessor(this.readShellProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_POSTPROCESSOR_MERGE)) {
                target.getPostProcessors().addPostProcessor(this.readMergeProcessor(children.item(i), target));                
            } else if (child.equalsIgnoreCase(XML_SOURCE)) {
                readSource(sources, children.item(i));
            }
        }
        
        if (sources.size() > 0 && baseDir != null) {
            throw new AdapterException("The '" + XML_TARGET_BASEDIR + "' attribute is deprecated. It shall not be used anymore. Use '" + XML_SOURCE + "' elements instead.");
        } else if (baseDir == null) {
            target.setSources(sources);
        }
        
        return target;
    }
    
    protected void readSource(Set sources, Node node) throws AdapterException {
        Node pathNode = node.getAttributes().getNamedItem(XML_SOURCE_PATH);
        if (pathNode == null) {
            throw new AdapterException("A '" + XML_SOURCE_PATH + "' attribute must be set.");
        } else {
            sources.add(new File(pathNode.getNodeValue()));
        }
    }
   
    protected PostProcessor readDumpProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_DUMP_DIRECTORY);
        if (paramNode == null) {
            throw new AdapterException("Dump directory not found for File Dump Processor. A '" + XML_PP_DUMP_DIRECTORY + "' attribute must be set.");
        }          
        FileDumpPostProcessor pp = new FileDumpPostProcessor();
        pp.setDestinationFolder(new File(paramNode.getNodeValue()));
        
        Node failureOnlyNode = node.getAttributes().getNamedItem(XML_PP_ONLY_IF_ERROR);
        if (failureOnlyNode != null) {
            pp.setOnlyIfError(Boolean.valueOf(failureOnlyNode.getNodeValue()).booleanValue());
        }
        
        Node listFilteredNode = node.getAttributes().getNamedItem(XML_PP_LIST_FILTERED);
        if (listFilteredNode != null) {
            pp.setListFiltered(Boolean.valueOf(listFilteredNode.getNodeValue()).booleanValue());
        }
        
        Node nameNode = node.getAttributes().getNamedItem(XML_PP_DUMP_NAME);
        if (nameNode != null) {
            pp.setReportName(nameNode.getNodeValue());
        }
        
        return pp;
    }
    
    protected PostProcessor readShellProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node scriptNode = node.getAttributes().getNamedItem(XML_PP_SHELL_SCRIPT);
        if (scriptNode == null) {
            throw new AdapterException("Shell script file not found for Shell Processor. A '" + XML_PP_SHELL_SCRIPT + "' attribute must be set.");
        }     
        
        ShellScriptPostProcessor pp = new ShellScriptPostProcessor();
        pp.setCommand(scriptNode.getNodeValue());
        
        
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_SHELL_PARAMS);
        if (paramNode != null) {
            pp.setCommandParameters(paramNode.getNodeValue());
        }
        
        return pp;
    }
    
    protected PostProcessor readMergeProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        Node paramNode = node.getAttributes().getNamedItem(XML_PP_MERGE_DELAY);
        if (paramNode == null) {
            throw new AdapterException("Merge delay not found for merge processor. A '" + XML_PP_MERGE_DELAY + "' attribute must be set.");
        }          
        Node keepNode = node.getAttributes().getNamedItem(XML_PP_MERGE_KEEP_DELETED);
        
        MergePostProcessor pp = new MergePostProcessor();
        pp.setDelay(Integer.parseInt(paramNode.getNodeValue()));
        
        if (keepNode != null) {
            pp.setKeepDeletedEntries(Boolean.valueOf(keepNode.getNodeValue()).booleanValue());
        } else {
            pp.setKeepDeletedEntries(false);
        }
        
        return pp;
    }
    
    protected PostProcessor readEmailProcessor(Node node, AbstractRecoveryTarget target) throws AdapterException {
        MailSendPostProcessor pp = new MailSendPostProcessor();
        
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
        
        Node failureOnlyNode = node.getAttributes().getNamedItem(XML_PP_ONLY_IF_ERROR);
        if (failureOnlyNode != null) {
            pp.setOnlyIfError(Boolean.valueOf(failureOnlyNode.getNodeValue()).booleanValue());
        }
        
        Node listFilteredNode = node.getAttributes().getNamedItem(XML_PP_LIST_FILTERED);
        if (listFilteredNode != null) {
            pp.setListFiltered(Boolean.valueOf(listFilteredNode.getNodeValue()).booleanValue());
        }
        
        // BACKWARD-COMPATIBILITY //
        Node failureOnlyNode_old = node.getAttributes().getNamedItem("smtp_" + XML_PP_ONLY_IF_ERROR);
        if (failureOnlyNode_old != null) {
            pp.setOnlyIfError(Boolean.valueOf(failureOnlyNode_old.getNodeValue()).booleanValue());
        }
        Node listFilteredNode_old = node.getAttributes().getNamedItem("smtp_" + XML_PP_LIST_FILTERED);
        if (listFilteredNode_old != null) {
            pp.setListFiltered(Boolean.valueOf(listFilteredNode_old.getNodeValue()).booleanValue());
        }
        // EOF BACKWARD-COMPATIBILITY //
        
        Node titleNode = node.getAttributes().getNamedItem(XML_PP_EMAIL_TITLE);
        if (titleNode != null) {
            pp.setTitle(titleNode.getNodeValue());
        }
        
        pp.setRecipients(recipientsNode.getNodeValue());
        pp.setSmtpServer(smtpNode.getNodeValue());        
        return pp;
    }
    
    protected ArchiveMedium readMedium(Node mediumNode, AbstractRecoveryTarget target) throws IOException, AdapterException, ApplicationException {
        Node typeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TYPE);
        if (typeNode == null) {
            throw new AdapterException("Medium type not found : your medium must have a '" + XML_MEDIUM_TYPE + "' attribute.");
        }           
        
        Node overwriteNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_OVERWRITE);
        boolean isOverwrite = (overwriteNode != null && overwriteNode.getNodeValue().equalsIgnoreCase("true"));    
        
        Node trackDirsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_DIRS);
        boolean trackDirs = (trackDirsNode != null && trackDirsNode.getNodeValue().equalsIgnoreCase("true"));   
        
        Node trackPermsNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_TRACK_PERMS);
        boolean trackPerms = (trackPermsNode != null && trackPermsNode.getNodeValue().equalsIgnoreCase("true"));   

        AbstractIncrementalFileSystemMedium medium;
        
        if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP) || typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP64)) {
            medium = new IncrementalZipMedium();       
            ((IncrementalZipMedium)medium).setUseZip64(typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_ZIP64));
            
            Node volumeSizeNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_VOLUME_SIZE);
            if (volumeSizeNode != null) {
                long volumeSize = Long.parseLong(volumeSizeNode.getNodeValue());
                ((IncrementalZipMedium)medium).setVolumeSize(volumeSize);
                ((IncrementalZipMedium)medium).setMultiVolumes(true);
            }
            
            Node commentNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_COMMENT);
            if (commentNode != null) {
                ((IncrementalZipMedium)medium).setComment(commentNode.getNodeValue());
            }
            
            Node charsetNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ZIP_CHARSET);
            if (charsetNode != null) {
                ((IncrementalZipMedium)medium).setCharset(Charset.forName(charsetNode.getNodeValue()));
            }
        } else if (typeNode.getNodeValue().equalsIgnoreCase(XML_MEDIUM_TYPE_DIR)) {
            medium = new IncrementalDirectoryMedium();                
        }  else {
            throw new AdapterException("Unknown medium : " + typeNode.getNodeValue());
        }
        
        EncryptionPolicy encrArgs = readEncryptionPolicy(mediumNode, target);
        FileSystemPolicy storage = readFileSystemPolicy(mediumNode);
        medium.setFileSystemPolicy(storage);
        medium.setEncryptionPolicy(encrArgs);
        medium.setOverwrite(isOverwrite);
        medium.setTrackDirectories(trackDirs);
        medium.setTrackPermissions(trackPerms);        
        
        return medium;
    }
    
    protected FileSystemPolicy readFileSystemPolicy(Node mediumNode) throws IOException, AdapterException, ApplicationException {
        Node policyNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_POLICY);
        String policyId;
        if (policyNode != null) {
            policyId = policyNode.getNodeValue();
        } else {
            // Backward compatible read
            Node pathNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ARCHIVEPATH);
            if (pathNode != null) {
                policyId = POLICY_HD;
            } else {
                policyId = POLICY_FTP;                
            }
        }
        
        StoragePlugin plugin = StoragePluginRegistry.getInstance().getById(policyId);
        return plugin.getFileSystemPolicyXMLHandler().read(mediumNode);
    }
    
    protected EncryptionPolicy readEncryptionPolicy(Node mediumNode, AbstractRecoveryTarget target) throws IOException, AdapterException, ApplicationException {
        Node encryptedNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTED);
        boolean isEncrypted = (encryptedNode != null && encryptedNode.getNodeValue().equalsIgnoreCase("true"));   
        
        Node encryptionKeyNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONKEY);
        String encryptionKey = encryptionKeyNode != null ? encryptionKeyNode.getNodeValue() : null;   
        
        Node encryptionAlgoNode = mediumNode.getAttributes().getNamedItem(XML_MEDIUM_ENCRYPTIONALGO);
        String encryptionAlgo = encryptionAlgoNode != null ? encryptionAlgoNode.getNodeValue() : null;          
        
        if (isEncrypted && encryptionKey == null) { // No check for the encryptionAlgorithm because we use a default one if none is specified.
            if (this.missingDataListener != null) {
                Object[] encrData = (Object[])missingDataListener.missingEncryptionDataDetected(target);
                if (encrData != null) {
	                encryptionAlgo = (String)encrData[0];
	                encryptionKey = (String)encrData[1];
                }
            }
        }    
        
        if (isEncrypted && encryptionKey == null) { // Second check .... after missingDataListener invocation.
            throw new AdapterException("No encryption key found : your medium must have a '" + XML_MEDIUM_ENCRYPTIONKEY + "' attribute because it is encrypted (" + XML_MEDIUM_ENCRYPTED + " = true).");
        }
        
        EncryptionPolicy encrArgs = new EncryptionPolicy();
        encrArgs.setEncrypted(isEncrypted);
        encrArgs.setEncryptionAlgorithm(encryptionAlgo);
        encrArgs.setEncryptionKey(encryptionKey);
        
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
            } else if (child.equalsIgnoreCase(XML_FILTER_LOCKED)) {
                grp.addFilter(this.readLockedFileFilter(children.item(i)));                  
            } else if (child.equalsIgnoreCase(XML_FILTER_FILEDATE)) {
                grp.addFilter(this.readFileDateArchiveFilter(children.item(i)));  
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
            throw new AdapterException("Maximum size not found. Your filter must have a '" + XML_FILTER_PARAM + "' attribute (eg '1024').");
        }          
        FileSizeArchiveFilter filter = new FileSizeArchiveFilter();
        initFilter(filter, filterNode, paramNode);
        return filter;
    }
    
    protected LinkFilter readLinkFilter(Node filterNode) throws AdapterException {     
        LinkFilter filter = new LinkFilter();
        initFilter(filter, filterNode, null);
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
        initFilter(filter, filterNode, regexNode);
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