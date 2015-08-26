package com.application.areca.version;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.myJava.system.OSTool;
import com.myJava.util.version.VersionData;

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
public class VersionInfos {
	protected static ArrayList VERSIONS = new ArrayList();  
	protected static Map VERSION_BY_ID = new HashMap();

	protected static long BUILD_ID = -1;

	public static String APP_NAME = "Areca Backup";
	public static String APP_SHORT_NAME = "Areca";
	public static int[] REQUIRED_JAVA_VERSION = new int[] {1, 4, 2};
	public static String VERSION_MSG = APP_NAME + 
	" requires Java version " 
	+ OSTool.formatJavaVersion(REQUIRED_JAVA_VERSION) 
	+ " to run properly.\nThe version which is currently installed on your system (v " 
	+ OSTool.formatJavaVersion(OSTool.getJavaVersion()) 
	+ ") does not meet this requirement.\n\nPlease go to http://java.sun.com/javase/downloads/ and get a newer version of Java.\n "; 
	
	public static String VENDOR_MSG = "CAUTION : The Java Runtime Environment you are currently using has been released by '" + OSTool.getJavaVendor() + "'.\nIt seems that some open source Java Virtual Machines have problems dealing with some specific filenames characters (German 'Umlaut' for instance).\nIt is so highly advised to use Sun Microsystem's Java Runtime Environment to run " + VersionInfos.APP_SHORT_NAME + ".\n(http://www.java.com/download/)";

	static {
		// INIT BUILD ID - DO NOT MODIFY THE FOLLOWING COMMENT :
		BUILD_ID = 5872222636083894532L;

		// INIT VERSION DATA
		VERSIONS.add(new VersionData("7.5", new GregorianCalendar(2015, 7, 26), "Fixed the issue that prevented archives from being created when there were only deleted files (no new files);Fixed a minor issue when reconnecting to a FTP server."));
		VERSIONS.add(new VersionData("7.4.9", new GregorianCalendar(2014, 10, 26), "Fixed compression issue that could occur with filenames containing line breaks."));
		VERSIONS.add(new VersionData("7.4.8", new GregorianCalendar(2014, 10, 02), "Fixed encryption issued which could occur with recent versions of JREs"));
		VERSIONS.add(new VersionData("7.4.7", new GregorianCalendar(2014, 5, 16), "Fixed a performance regression introduced in v7.4"));
		VERSIONS.add(new VersionData("7.4.6", new GregorianCalendar(2014, 4, 15), "Fixes a concurrency issue that could occur when creating temporary files; Minor improvements."));
		VERSIONS.add(new VersionData("7.4.5", new GregorianCalendar(2014, 4, 1), "Fixes the SFTP kerberos dialog box issue with Java7; Fixes SWT issues on 64 bit JREs."));
		VERSIONS.add(new VersionData("7.4.4", new GregorianCalendar(2014, 3, 06), "Fixes a compatibility issue with Java-8; Chineese translation update."));
		VERSIONS.add(new VersionData("7.4.3", new GregorianCalendar(2014, 2, 19), "Fixes a bug that could occur on Posix systems when the file's owner group was empty; New '%BACKUP_TYPE%' dynamic tag for post-processors (full/incremental/differential); Ability to limit Areca's throughput when writing files; If 32 and 64 bits Java environments are installed on the system, Areca will use the 64 bit by default (windows only); Areca can now use up to 1GB RAM."));
		VERSIONS.add(new VersionData("7.4.2", new GregorianCalendar(2014, 0, 19), "Fixes a bug in Delta storage that could generate 'out of memory' errors when reading large files."));
		VERSIONS.add(new VersionData("7.4.1", new GregorianCalendar(2014, 0, 17), "Minor memory optimizations"));
		VERSIONS.add(new VersionData("7.4", new GregorianCalendar(2013, 11, 05), "Fixed issues that could occur on temporary files in multi-users environment; Areca now attempts to locate your archives when importing backup configurations; Minor memory optimizations when handling large directories; Max memory that can be used by Areca has been increased from 256MB to 512MB."));
		VERSIONS.add(new VersionData("7.3.9", new GregorianCalendar(2013, 10, 29), "Better error management when reading metadata; Cleaner configuration import window; Fixed a bug that occurred when using the '-date' switch in the command line tool"));
		VERSIONS.add(new VersionData("7.3.8", new GregorianCalendar(2013, 10, 03), "Additional integrity checks after archive merges; Fixed memory management issue when recovering; Prevent from modifying backup scheme (full, differential, incremental) when resuming an interrupted backup."));
		VERSIONS.add(new VersionData("7.3.7", new GregorianCalendar(2013, 7, 31), "CS, ES and HU translation updates."));
		VERSIONS.add(new VersionData("7.3.6", new GregorianCalendar(2013, 7, 21), "Added certificate authentication for SFTP connectivity; Passwords are now encrypted in configuration files."));
		VERSIONS.add(new VersionData("7.3.5", new GregorianCalendar(2013, 6, 14), "Fixed a but that prevented from creating backup shortcuts in some configurations."));
		VERSIONS.add(new VersionData("7.3.4", new GregorianCalendar(2013, 5, 24), "Improved error messages when dangling symbolic links are encountered."));
		VERSIONS.add(new VersionData("7.3.3", new GregorianCalendar(2013, 4, 12), "Improved error messages in case of invalid encryption configuration; Delta storage bugfix; Fixed memory issues that could occur when too many errors were encountered while checking archives."));
		VERSIONS.add(new VersionData("7.3.2", new GregorianCalendar(2013, 4, 10), "Fixed a bug that could prevent backups to be resumed in case of error."));
		VERSIONS.add(new VersionData("7.3.1", new GregorianCalendar(2013, 3, 14), "Fixed a bug that could prevent the workspace from loading when drives are disconnected."));
		VERSIONS.add(new VersionData("7.3", new GregorianCalendar(2013, 2, 30), "Performance improvement on delta storage mode."));
		VERSIONS.add(new VersionData("7.2.18", new GregorianCalendar(2013, 2, 22), "Fixed freeze issues when trying to connect to unresponsive FTP/SFTP servers; Up-to-date Chinese and Japanese translations."));
		VERSIONS.add(new VersionData("7.2.17", new GregorianCalendar(2012, 10, 17), "Minor GUI bugfixes."));
		VERSIONS.add(new VersionData("7.2.16", new GregorianCalendar(2012, 10, 13), "Fixed SMTPs compatibility issue : Areca can now send the 'STARTTLS' command when establishing the connection; Fixed a bug on transaction points; Fixed a bug that could occur when handling files starting with a whitespace; Improved file modification detection : Areca can now inspect the files' content instead of relying on their attributes; Minor wizard improvements."));
		VERSIONS.add(new VersionData("7.2.15", new GregorianCalendar(2012, 9, 30), "Minor technical bugfixes."));
		VERSIONS.add(new VersionData("7.2.14", new GregorianCalendar(2012, 9, 23), "Fixed a compatibility issue with IBM JVMs (Base64 encoding); Better error management when handling compressed archives; Areca now uses by default the local temporary directory as working directory when checking archives; Better plugins integration"));
		VERSIONS.add(new VersionData("7.2.13", new GregorianCalendar(2012, 9, 14), "Fixed a bug that could occur when handling target sources spread across multiple drives; better integration of plugins."));
		VERSIONS.add(new VersionData("7.2.12", new GregorianCalendar(2012, 7, 26), "Fixed a bug introduced in v7.2.11 that could occur with image backups when no file needs to be stored.", "Pre processors are now run whether a backup is required or not (ie even if no file has to be stored)."));
		VERSIONS.add(new VersionData("7.2.11", new GregorianCalendar(2012, 7, 25), "Backup is now launched without simulation; Improved texteditor options; Minor GUI bugfixes.", "Pre processors are now run whether a backup is required or not (ie even if no file has to be stored)."));
		VERSIONS.add(new VersionData("7.2.10", new GregorianCalendar(2012, 4, 24), "Pre/Postprocessors can now be run when archives are merged or checked; Minor bugfixes and improvements."));
		VERSIONS.add(new VersionData("7.2.9", new GregorianCalendar(2012, 4, 17), "Added filename encoding verifications when recovering or merging archives; Minor GUI improvements."));
		VERSIONS.add(new VersionData("7.2.8", new GregorianCalendar(2012, 4, 5), "Added a 'check archive' option after merge; CBC mode added to encryption parameters; Stored file list can be included in backup reports."));
		VERSIONS.add(new VersionData("7.2.7", new GregorianCalendar(2012, 3, 29), "Minor backup shortcut codepage bug fix; Classpath bug fix."));
		VERSIONS.add(new VersionData("7.2.6", new GregorianCalendar(2012, 3, 9), "New email preprocessor; Translation fix on German language pack; File filtering bugfix; Errorcode management bugfix (Windows); Minor GUI enhancements", "This version changes the way memory settings can be overriden. Please refer to the online help for more informations."));
		VERSIONS.add(new VersionData("7.2.5", new GregorianCalendar(2012, 0, 26), "Minor improvements & bugfixes."));
		VERSIONS.add(new VersionData("7.2.4", new GregorianCalendar(2012, 0, 18), "Recovery optimization when handling large volume of data; Improved recovery options; Two merge bug fixes", "CAUTION : This new version of Areca fixes two bugs in the 'merge' function. It is recommended to check archives created by merging with previous (7.2.x) versions of Areca."));
		VERSIONS.add(new VersionData("7.2.3", new GregorianCalendar(2011, 7, 20), "Minor bugfixes"));
		VERSIONS.add(new VersionData("7.2.2", new GregorianCalendar(2011, 7, 28), "Statistics can now be added to backup reports; Recovery performance improvements when handling large number of files; Minor GUI enhancements and bugfixes."));
		VERSIONS.add(new VersionData("7.2.1", new GregorianCalendar(2011, 1, 19), "Transaction parameters have been added to the target configuration window."));
		VERSIONS.add(new VersionData("7.2", new GregorianCalendar(2011, 0, 23), "Added support for intermediate transaction points; Added SFTP storage; Added control on encrypted filenames wrapping; Enhanced control on FTP passive mode."));
		VERSIONS.add(new VersionData("7.1.10", new GregorianCalendar(2010, 10, 01), "Fixed a bug on search window."));
		VERSIONS.add(new VersionData("7.1.9", new GregorianCalendar(2010, 9, 17), "Fixed a bug that could occur when checking recovered files; Default zip compression level set to 4."));
		VERSIONS.add(new VersionData("7.1.8", new GregorianCalendar(2010, 8, 29), "Added control encoding option to FTP window; Fixed a problem that prevented last modification time to be recovered on read-only files (windows specific); Faster and storage efficient 'archive check' feature; Faster delta storage mode; Added control on zip compression level; Minor other bugfixes & enhancements."));
		VERSIONS.add(new VersionData("7.1.7", new GregorianCalendar(2010, 4, 19), "Minor GUI enhancements & bugfixes."));
		VERSIONS.add(new VersionData("7.1.6", new GregorianCalendar(2009, 11, 07), "Big files (over 2 GB) metadata are now properly handled; More control has been added to file path naming conventions on Windows; A bug that could occur when recovering single files in 'delta' mode has been fixed; Configuration storage has been refactored and simplified; 'Check' and 'Merge' features have been improved; Better 'Configuration import' window; 'Search' feature enhancements; Minor user interface enhancements.", "CAUTION : This version includes major modifications of Areca's backup configuration format."));
		VERSIONS.add(new VersionData("7.1.5", new GregorianCalendar(2009, 7, 24), "More secured configuration backup (Areca makes sure that the XML backup file is correctly written on the backup location); Command-line output in console has been reactivated; Bugfix when forcing 'full backup' mode on delta targets; Areca can now handle <null> extended attribute values; Progress bars enhancements; Post-processors enhancements."));
		VERSIONS.add(new VersionData("7.1.4", new GregorianCalendar(2009, 7, 2), "Drag and Drop in sources configuration window; Bugfix when recovering a specific version of a file (the latest version was always recovered); Minor enhancements."));
		VERSIONS.add(new VersionData("7.1.3", new GregorianCalendar(2009, 6, 12), "Regular expression filter bug fix; Minor XML configuration bug fix; Recovery bug fix."));
		VERSIONS.add(new VersionData("7.1.2", new GregorianCalendar(2009, 5, 12), "New regular expression filters options; Backup report enhancements; Post processors enhancements; Minor bugfixes."));
		VERSIONS.add(new VersionData("7.1.1", new GregorianCalendar(2009, 4, 26), "Ability to view files contained in your archives using the default applications configured on your system; Post-backup actions enhancements; Minor other enhancements."));
		VERSIONS.add(new VersionData("7.1", new GregorianCalendar(2009, 3, 21), "Minor error messages enhancements; Named pipes are now properly handled by Areca; Better \"Check Archive\" feature."));
		VERSIONS.add(new VersionData("7.0.9", new GregorianCalendar(2009, 3, 3), "Synchronous backup mode available from command-line interface; The 256 characters limit on file path is disabled if the Java version is over 1.6; Enhanced FTP connexions management."));
		VERSIONS.add(new VersionData("7.0.8", new GregorianCalendar(2009, 2, 21), "Logical view bug fix; Image backups bug fix."));
		VERSIONS.add(new VersionData("7.0.7", new GregorianCalendar(2009, 2, 17), "Regex file filter bug fix; Added 'encrypt file names' to the 'missing encryption data' window; File handle cleanup."));
		VERSIONS.add(new VersionData("7.0.6", new GregorianCalendar(2009, 2, 7), "Delta backup bug fix."));
		VERSIONS.add(new VersionData("7.0.5", new GregorianCalendar(2009, 2, 5), "ACL serialization bug fix in Java 1.5; Minor recovery bug fix."));
		VERSIONS.add(new VersionData("7.0", new GregorianCalendar(2009, 1, 15), "ACL and extended attributes support for Linux; New 'archive check' feature; Heavy memory management refactoring.", "CAUTION : this version is NOT backward compatible with previous versions of Areca."));
		VERSIONS.add(new VersionData("6.1", new GregorianCalendar(2008, 11, 2), "Backup pause implementation; Plugin API enhancement; Configurable compression level; Encryption refactoring", "CAUTION : This refactoring is NOT backward-compatible. This means that this new version will NOT be able to read archives encrypted with previous versions of Areca."));
		VERSIONS.add(new VersionData("6.0.7", new GregorianCalendar(2008, 3, 28), "log bug fix."));
		VERSIONS.add(new VersionData("6.0.6", new GregorianCalendar(2008, 3, 27), "Various bug fixes."));
		VERSIONS.add(new VersionData("6.0.5", new GregorianCalendar(2008, 3, 10), "Log bug fix."));
		VERSIONS.add(new VersionData("6.0.4", new GregorianCalendar(2008, 3, 9), "Archive trace backward-compatibility bug fix."));
		VERSIONS.add(new VersionData("6.0.3", new GregorianCalendar(2008, 3, 8), "Zip Bugfix."));
		VERSIONS.add(new VersionData("6.0.2", new GregorianCalendar(2008, 3, 7), "Bugfixes; Archive trace storage refactoring; translation updates."));
		VERSIONS.add(new VersionData("6.0.1", new GregorianCalendar(2008, 2, 30), "Minor bugfixes; Minor encryption and log enhancements."));
		VERSIONS.add(new VersionData("6.0", new GregorianCalendar(2008, 2, 26), "Delta storage implementation; Chinese (traditional), Swedish and Danish translations; Minor bugfixes and enhancements."));
		VERSIONS.add(new VersionData("5.5.7", new GregorianCalendar(2008, 0, 25), "Minor bugfixes; Japanese, Chinese and Spanish translations."));
		VERSIONS.add(new VersionData("5.5.6", new GregorianCalendar(2008, 0, 15), "Filename size checks were added; Minor enhancements."));
		VERSIONS.add(new VersionData("5.5.5", new GregorianCalendar(2008, 0, 13), "Encryption bug fix; Zip64 bug fix; Minor enhancements."));		
		VERSIONS.add(new VersionData("5.5.4", new GregorianCalendar(2007, 11, 20), "Recovery bug fix."));
		VERSIONS.add(new VersionData("5.5.3", new GregorianCalendar(2007, 11, 2), "Full and differential backups support."));		
		VERSIONS.add(new VersionData("5.5.2", new GregorianCalendar(2007, 10, 9), "Compression enhancements; Hungarian translation; Minor bugfixes."));		
		VERSIONS.add(new VersionData("5.5.1", new GregorianCalendar(2007, 9, 27), "Recovery optimization; Backup strategy wizard."));
		VERSIONS.add(new VersionData("5.5", new GregorianCalendar(2007, 9, 14), "Merge processor enhancements; New delete processor; Encryption enhancements; Bug Fixes."));      
		VERSIONS.add(new VersionData("5.4", new GregorianCalendar(2007, 9, 8), "Pre-processors; Filter enhancements; Minor bug fixes."));
		VERSIONS.add(new VersionData("5.3.5", new GregorianCalendar(2007, 8, 21), "Mail send processor enhancement; Minor bug fixes."));
		VERSIONS.add(new VersionData("5.3.4", new GregorianCalendar(2007, 8, 14), "External decompression tool; GUI enhancements; Java properties override feature."));        
		VERSIONS.add(new VersionData("5.3.3", new GregorianCalendar(2007, 8, 1), "FTP data cache; Some bug fixes."));        
		VERSIONS.add(new VersionData("5.3.2", new GregorianCalendar(2007, 7, 24), "External decryption tool; New archive merge option."));        
		VERSIONS.add(new VersionData("5.3.1", new GregorianCalendar(2007, 7, 19), "Faster file copy; Zip options enhancements; Filters enhancements; Multiple source directories support."));        
		VERSIONS.add(new VersionData("5.3", new GregorianCalendar(2007, 7, 11), "Dutch and Italian translation; Zip split support; Symbolic links support."));        
		VERSIONS.add(new VersionData("5.2.1", new GregorianCalendar(2007, 6, 22), "Permission management bug fix; Post-processors enhancements; Locked file filter enhancements; FTP enhancements."));        
		VERSIONS.add(new VersionData("5.2", new GregorianCalendar(2007, 5, 27), "Zip64 bug fix; Russian translation; Mail reports enhancements; Shell script dynamic parameters."));        
		VERSIONS.add(new VersionData("5.1", new GregorianCalendar(2007, 5, 17), "Better multithreading management; User interface enhancements (Archive's files edition feature & improved target deletion)."));        
		VERSIONS.add(new VersionData("5.0.2", new GregorianCalendar(2007, 5, 5), "Bug fix : directories starting with '#' were not processed properly."));        
		VERSIONS.add(new VersionData("5.0.1", new GregorianCalendar(2007, 4, 30), "UTF-8 is used for all metadata files; Backup shortcut implementation; Search-Window enhancements."));        
		VERSIONS.add(new VersionData("5.0", new GregorianCalendar(2007, 4, 12), "Graphical user interface refactoring (SWT is used instead of Swing)."));
		VERSIONS.add(new VersionData("4.5.2", new GregorianCalendar(2007, 4, 1), "Some bug fixes; Target duplication feature."));        
		VERSIONS.add(new VersionData("4.5.1", new GregorianCalendar(2007, 3, 29), "FTPs (implicit/explicit SSL/TLS) support."));
		VERSIONS.add(new VersionData("4.5", new GregorianCalendar(2007, 3, 11), "Zip64 compression; FTP support; Improved archives controls; XML configuration backup; Log window."));
		VERSIONS.add(new VersionData("4.2.3", new GregorianCalendar(2007, 2, 7), "Some more bug fixes."));
		VERSIONS.add(new VersionData("4.2.2", new GregorianCalendar(2007, 2, 6), "Various bug fixes."));
		VERSIONS.add(new VersionData("4.2.1", new GregorianCalendar(2007, 2, 3), "Memory optimizations and improved controls on zip32 archive size."));
		VERSIONS.add(new VersionData("4.2", new GregorianCalendar(2007, 1, 7), "File attributes recovery (date & permissions); Authenticated SMTP support; Encrypted targets bug fix."));
		VERSIONS.add(new VersionData("4.1.7", new GregorianCalendar(2007, 1, 4), "Merge bug fix."));
		VERSIONS.add(new VersionData("4.1.6", new GregorianCalendar(2007, 0, 30), "Memory optimizations; Recovery enhancement (File last modification date is now preserved)."));
		VERSIONS.add(new VersionData("4.1.5", new GregorianCalendar(2006, 11, 1), "Stronger encryption; Some bug fixes (shell post-processors)."));
		VERSIONS.add(new VersionData("4.1", new GregorianCalendar(2006, 10, 22), "Search feature; Toolbar; Minor enhancements & Bug fixes."));
		VERSIONS.add(new VersionData("4.0.5", new GregorianCalendar(2006, 10, 10), "Mail encoding bug fix; Workspace backup feature."));
		VERSIONS.add(new VersionData("4.0", new GregorianCalendar(2006, 10, 4), "Post-processors and user preferences; Various optimizations."));        
		VERSIONS.add(new VersionData("3.5.1", new GregorianCalendar(2006, 9, 21), "Minor ZIP bug fix (This bug occurred on empty archives)."));        
		VERSIONS.add(new VersionData("3.5", new GregorianCalendar(2006, 9, 10), "New file filters; Major archive medium refactoring."));        
		VERSIONS.add(new VersionData("3.4", new GregorianCalendar(2006, 8, 12), "Empty directory tracking; Archive mediums refactoring; File size & date filters."));        
		VERSIONS.add(new VersionData("3.3.1", new GregorianCalendar(2006, 8, 9), "Internationalization bug fix."));        
		VERSIONS.add(new VersionData("3.3", new GregorianCalendar(2006, 8, 7), "Internationalization (by Stephane Brunel), UTF8 support and various improvements."));        
		VERSIONS.add(new VersionData("3.2.7", new GregorianCalendar(2006, 7, 30), "Backup simulation bug fix."));        
		VERSIONS.add(new VersionData("3.2.6", new GregorianCalendar(2006, 7, 28), "Command line interface improvements - Backup pre-check improvements."));        
		VERSIONS.add(new VersionData("3.2.5", new GregorianCalendar(2006, 7, 24), "Command line interface bug fix."));        
		VERSIONS.add(new VersionData("3.2.4", new GregorianCalendar(2006, 7, 4), "Added AES encryption algorithm; Minor GUI enhancements."));        
		VERSIONS.add(new VersionData("3.2.3", new GregorianCalendar(2006, 7, 2), "Minor bug fix."));        
		VERSIONS.add(new VersionData("3.2.2", new GregorianCalendar(2006, 6, 27), "Added a check module for new releases."));    	
		VERSIONS.add(new VersionData("3.2.1", new GregorianCalendar(2006, 6, 20), "Target history enhancement."));    	
		VERSIONS.add(new VersionData("3.2", new GregorianCalendar(2006, 6, 18), "Implemented process cancellation; added target Indicators."));    	
		VERSIONS.add(new VersionData("3.1.5", new GregorianCalendar(2006, 6, 12), "Added Java version check on startup and minor GUI enhancements."));    	
		VERSIONS.add(new VersionData("3.1.4", new GregorianCalendar(2006, 6, 8), "Support for deep directories encryption on Windows (Windows limits paths length to 256 characters)."));    	
		VERSIONS.add(new VersionData("3.1.3", new GregorianCalendar(2006, 6, 6), "Added references to the project's home page."));
		VERSIONS.add(new VersionData("3.1.2", new GregorianCalendar(2006, 6, 2), "Minor GUI correction (the 'Help' window didn't open correctly under Windows 2000.)"));    	
		VERSIONS.add(new VersionData("3.1.1", new GregorianCalendar(2006, 5, 12), "Incremental storage mediums now check that the data have really changed before backup."));    	
		VERSIONS.add(new VersionData("3.1", new GregorianCalendar(2006, 4, 21), "Archive encryption implementation."));    	
		VERSIONS.add(new VersionData("3.0", new GregorianCalendar(2006, 4, 7), "Backup simulation and single file recovery implementation."));    	
		VERSIONS.add(new VersionData("2.7", new GregorianCalendar(2006, 3, 15), "Archive merge enhancement; Archive deletion implementation."));    	
		VERSIONS.add(new VersionData("2.6", new GregorianCalendar(2006, 2, 22), "Archive detail window."));    	
		VERSIONS.add(new VersionData("2.5", new GregorianCalendar(2006, 2, 9), "Commit / Rollback implementation for backups and merges."));    	
		VERSIONS.add(new VersionData("2.4", new GregorianCalendar(2006, 1, 6), "Regex filters implementation."));    	
		VERSIONS.add(new VersionData("2.3.1", new GregorianCalendar(2006, 0, 2), "Backup monitoring enhancement."));    	
		VERSIONS.add(new VersionData("2.3", new GregorianCalendar(2005, 11, 18), "Backup / Merge / Recover pre-check improvements."));    	
		VERSIONS.add(new VersionData("2.2.1", new GregorianCalendar(2005, 11, 8), "Log history management."));    	
		VERSIONS.add(new VersionData("2.2", new GregorianCalendar(2005, 9, 11), "Manifest implementation."));    	
		VERSIONS.add(new VersionData("2.1.2", new GregorianCalendar(2005, 6, 23), "Context menus implementation."));
		VERSIONS.add(new VersionData("2.1.1", new GregorianCalendar(2005, 6, 13), "History improvement."));
		VERSIONS.add(new VersionData("2.1", new GregorianCalendar(2005, 6, 11), "Minor GUI enhancements. ('about' and 'help' dialogs)"));
		VERSIONS.add(new VersionData("2.0", new GregorianCalendar(2005, 6, 3), "Group / Target edition windows."));
		VERSIONS.add(new VersionData("1.2.2", new GregorianCalendar(2005, 5, 21), "Minor GUI enhancements."));
		VERSIONS.add(new VersionData("1.2.1", new GregorianCalendar(2005, 5, 6), "History implementation."));
		VERSIONS.add(new VersionData("1.2", new GregorianCalendar(2005, 5, 1), "New storage mediums; XML processing improvement."));
		VERSIONS.add(new VersionData("1.1", new GregorianCalendar(2005, 4,15), "GUI implementation"));
		VERSIONS.add(new VersionData("1.0", new GregorianCalendar(2005, 4, 1), "Backup engine implementation."));

		Iterator iter = VERSIONS.iterator();
		while (iter.hasNext()) {
			VersionData infos = (VersionData)iter.next();
			VERSION_BY_ID.put(infos.getVersionId(), infos);
		}
	}

	public static VersionData getLastVersion() {
		return (VersionData)VERSIONS.get(0);
	}

	public static List getVersions() {
		return VERSIONS;
	}

	/**
	 * Some open source VM implementations have problems dealing with some filenames characters
	 * (like german "Umlaut") ... so it is highly advised to use Sun Microsystem's VM implementation.
	 */
	public static boolean checkJavaVendor() {
		String vendor = OSTool.getJavaVendor();
		return (vendor != null && vendor.toLowerCase().indexOf("free software foundation") == -1);
	}

	public static String formatVersionDate(GregorianCalendar date) {
		DateFormat FORMAT = DateFormat.getDateInstance(DateFormat.LONG);
		return FORMAT.format(date.getTime());
	}

	public static VersionData getVersion(String id) {
		return (VersionData)VERSION_BY_ID.get(id);
	}

	public static long getBuildId() {
		return BUILD_ID;
	}
}
