package com.application.areca.adapters;

/**
 * List of XML tags used for configuration serialization
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
public interface XMLTags {

    public static final String POLICY_HD  = "hd";
    public static final String POLICY_FTP  = "ftp";
    
    public static final String XML_GROUP = "process";
    public static final String XML_VERSION = "version";
    public static final String XML_BACKUP_COPY = "backup_copy";
    public static final String XML_GROUP_DESCRIPTION = "description";
    
    public static final String XML_TARGET = "target";    
    public static final String XML_TARGET_BASEDIR = "base_directory"; // deprecated
    public static final String XML_TARGET_ID = "id"; // deprecated
    public static final String XML_TARGET_UID = "uid";    
    public static final String XML_TARGET_NAME = "name";    
    public static final String XML_TARGET_FOLLOW_SYMLINKS = "follow_symlinks";  
    public static final String XML_TARGET_TRACK_EMPTY_DIRS = "register_empty_directories";
    public static final String XML_TARGET_FOLLOW_SUBDIRECTORIES_DEPREC = "follow_subdirectories";
    public static final String XML_TARGET_FOLLOW_SUBDIRECTORIES = "follow_subdirs";    
    public static final String XML_TARGET_DESCRIPTION = "description";   
    public static final String XML_TARGET_FWD_PREPROC_ERRORS = "forward_preproc_errors";  
    public static final String XML_TARGET_CREATE_XML_SECURITY_COPY = "xml_security_copy";   
    
    public static final String XML_SOURCE = "source";
    public static final String XML_SOURCE_PATH = "path";
    
    public static final String XML_FILTER_GROUP = "filter_group";  
    public static final String XML_FILTER_GROUP_OPERATOR = "operator";
    public static final String XML_FILTER_GROUP_OPERATOR_AND = "and";
    public static final String XML_FILTER_GROUP_OPERATOR_OR = "or";
    public static final String XML_FILTER_DIRECTORY = "directory_filter";
    public static final String XML_FILTER_REGEX = "regex_filter";
    public static final String XML_FILTER_OWNER = "owner_filter";
    public static final String XML_FILTER_FILESIZE = "size_filter";
    public static final String XML_FILTER_FILEDATE = "date_filter";
    public static final String XML_FILTER_LINK = "link_filter";
    public static final String XML_FILTER_TP = "spec_file_filter";
    public static final String XML_FILTER_LOCKED = "locked_filter";    
    public static final String XML_FILTER_FILEEXTENSION = "extension_filter";   
    public static final String XML_FILTER_LOGICAL_NOT_DEPRECATED = "exclude";
    public static final String XML_FILTER_LOGICAL_NOT = "logical_not";
    public static final String XML_FILTER_DIR_PATH = "directory";
    public static final String XML_FILTER_RG_PATTERN = "rgpattern";
    public static final String XML_FILTER_RG_MODE = "mode";
    public static final String XML_FILTER_RG_FULL_MATCH = "full_match";
    public static final String XML_FILTER_PARAM = "param";    
    public static final String XML_FILTER_EXTENSION = "ext";  
    
    public static final String XML_FILTER_TP_PIPE = "pipe";
    public static final String XML_FILTER_TP_SOCKET = "socket";
    public static final String XML_FILTER_TP_CHARSPECFILE = "charspecfile";
    public static final String XML_FILTER_TP_BLOCKSPECFILE = "blockspecfile";
    public static final String XML_FILTER_TP_LINK = "symlink";
    
    public static final String XML_HANDLER = "handler";
    public static final String XML_HANDLER_TYPE = "type";
    public static final String XML_HANDLER_TYPE_STANDARD = "standard";
    public static final String XML_HANDLER_TYPE_DELTA = "delta";
    
    public static final String XML_TRANSACTION_CONFIG = "transaction_configuration";
    public static final String XML_USE_TRANSACTIONS = "use_transactions";
    public static final String XML_TRANSACTION_SIZE = "transaction_size";

    public static final String XML_ADDONS = "addons";
    public static final String XML_ADDON = "addon";
    public static final String XML_ADDON_ID = "id";
    
    public static final String XML_MEDIUM = "medium";    
    public static final String XML_MEDIUM_ARCHIVEPATH_DEPRECATED = "archive_path";   // Backward - compatibility
    public static final String XML_MEDIUM_PATH = "path";
    public static final String XML_MEDIUM_ARCHIVENAME = "archive_name";
    public static final String XML_MEDIUM_OVERWRITE = "overwrite";
    public static final String XML_MEDIUM_POLICY = "policy";
    public static final String XML_MEDIUM_FILECOMPRESSION = "file_compression";
    public static final String XML_MEDIUM_Z64 = "zip64";
    public static final String XML_MEDIUM_ZIP_LEVEL = "zip_level";
    public static final String XML_MEDIUM_INSPECT_FILE_CONTENT = "inspect_file_content";
    public static final String XML_MEDIUM_ENCRYPTED = "encrypted";
    public static final String XML_MEDIUM_ENCRYPTIONKEY = "encryption_key";
    public static final String XML_MEDIUM_ENCRYPTIONALGO = "encryption_algo";
    public static final String XML_MEDIUM_WRAPNAMES = "encrypted_names_policy";
    public static final String XML_MEDIUM_ENCRYPTNAMES = "encrypt_names";
    public static final String XML_MEDIUM_TRACK_DIRS = "track_directories";		// DEPRECATED
    public static final String XML_MEDIUM_TRACK_PERMS = "track_permissions";    
    public static final String XML_MEDIUM_VOLUME_SIZE = "volume_size";
    public static final String XML_MEDIUM_VOLUME_DIGITS = "volume_digits";
    public static final String XML_MEDIUM_ZIP_COMMENT = "zip_comment";    
    public static final String XML_MEDIUM_ZIP_CHARSET = "zip_charset";   
    public static final String XML_MEDIUM_ZIP_EXTENSION = "zip_ext";   
    public static final String XML_MEDIUM_TYPE = "type";
    public static final String XML_MEDIUM_MAX_THROUGHPUT = "max_throughput";
    public static final String XML_MEDIUM_TYPE_ZIP = "zip";
    public static final String XML_MEDIUM_TYPE_ZIP64 = "zip64"; // DEPRECATED 
    public static final String XML_MEDIUM_TYPE_DIR = "directory";        
    public static final String XML_MEDIUM_FTP_LOGIN = "ftp_login";
    public static final String XML_MEDIUM_FTP_PASSWORD = "ftp_password";
    public static final String XML_MEDIUM_FTP_HOST = "ftp_host";
    public static final String XML_MEDIUM_FTP_PORT = "ftp_port";
    public static final String XML_MEDIUM_FTP_IGNORE_PSV_ERR = "ftp_ignore_psv_errors";
    public static final String XML_MEDIUM_FTP_PASSIV = "ftp_passiv";
    public static final String XML_MEDIUM_FTP_PROTOCOL = "ftp_protocol";
    public static final String XML_MEDIUM_FTP_PROTECTION = "ftp_protection";
    public static final String XML_MEDIUM_FTP_CTRL_ENCODING = "ftp_ctrl_encoding";
    public static final String XML_MEDIUM_FTP_IMPLICIT = "ftp_implicit";
    public static final String XML_MEDIUM_FTP_REMOTEDIR= "ftp_remotedir";
    
    public static final String XML_PROCESSOR_SHELL = "shell_processor";
    public static final String XML_PROCESSOR_EMAIL_REPORT = "email_processor";
    public static final String XML_PROCESSOR_EMAIL = "email_pre_processor";
    public static final String XML_PROCESSOR_DUMP = "dump_processor";
    public static final String XML_PROCESSOR_MERGE = "merge_processor";
    public static final String XML_PROCESSOR_DELETE = "delete_processor";
    public static final String XML_PP_AFTER = "execute_after";   
    public static final String XML_PP_DUMP_DIRECTORY = "directory";
    public static final String XML_PP_DUMP_NAME = "filename";
    public static final String XML_PP_ADD_STATS = "statistics";
    public static final String XML_PP_LIST_STORED_FILES = "list_stored_files";
    public static final String XML_PP_MAX_LISTED_FILES = "max_listed_files";
    public static final String XML_PP_EMAIL_RECIPIENTS = "recipients";
    public static final String XML_PP_EMAIL_SMTP = "smtp_host";
    public static final String XML_PP_EMAIL_TITLE = "title";    
    public static final String XML_PP_EMAIL_FROM = "from";    
    public static final String XML_PP_EMAIL_INTRO = "introduction";    
    public static final String XML_PP_EMAIL_USER = "smtp_user";
    public static final String XML_PP_EMAIL_PASSWORD = "smtp_password";    
    public static final String XML_PP_EMAIL_SMTPS = "smtp_s";  
    public static final String XML_PP_EMAIL_DISABLE_STARTTLS = "smtp_disable_starttls"; 
    public static final String XML_PP_SHELL_SCRIPT = "script";
    public static final String XML_PP_SHELL_PARAMS = "parameters";    
    public static final String XML_PP_DELAY = "delay";   // deprecated 
    public static final String XML_PP_MERGE_FROM_DELAY = "from_delay";  
    public static final String XML_PP_MERGE_TO_DELAY = "to_delay";     
    public static final String XML_PP_MERGE_KEEP_DELETED = "keep_deleted";
    public static final String XML_PP_MERGE_CHECK = "check_archive";
    public static final String XML_PP_ONLY_IF_ERROR = "failure_only"; // deprecated since 7.1.1
    public static final String XML_PP_RUN_SCHEME = "run_scheme";		// deprecated since 7.1.5
    public static final String XML_PP_RUN_SCHEME_ALWAYS = "always";		// deprecated since 7.1.5
    public static final String XML_PP_RUN_SCHEME_FAILURE = "failure";	// deprecated since 7.1.5
    public static final String XML_PP_RUN_SCHEME_SUCCESS = "success";	// deprecated since 7.1.5
    
    public static final String XML_PP_RUN_SUCCESS = "run_if_success";
    public static final String XML_PP_RUN_ERROR = "run_if_error";
    public static final String XML_PP_RUN_WARNING = "run_if_warning";
    
    public static final String XML_PP_RUN_BACKUP = "run_if_backup";
    public static final String XML_PP_RUN_MERGE = "run_if_merge";
    public static final String XML_PP_RUN_CHECK = "run_if_check";
}
