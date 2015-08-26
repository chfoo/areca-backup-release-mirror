package com.application.areca.external;

import java.io.File;
import java.io.IOException;

import com.application.areca.ApplicationException;
import com.application.areca.impl.policy.EncryptionPolicy;
import com.application.areca.launcher.AbstractArecaLauncher;
import com.application.areca.version.VersionInfos;
import com.myJava.commandline.BooleanCmdLineOption;
import com.myJava.commandline.CmdLineParserException;
import com.myJava.commandline.CommandLineParser;
import com.myJava.commandline.StringCmdLineOption;
import com.myJava.file.FileSystemManager;
import com.myJava.file.FileTool;
import com.myJava.file.driver.DefaultFileSystemDriver;
import com.myJava.file.driver.DriverAlreadySetException;
import com.myJava.file.driver.EncryptedFileSystemDriver;
import com.myJava.file.driver.FileSystemDriver;
import com.myJava.file.iterator.FileSystemIterator;

/**
 * <BR>
 * @author Ludovic QUESNELLE
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
public class CmdLineDeCipher 
extends AbstractArecaLauncher {
    
    private static final String ARG_ALG = "algorithm";
    private static final String ARG_PASSWD = "password";
    private static final String ARG_SOURCE = "source";
    private static final String ARG_FILE = "file";
    private static final String ARG_DESTINATION = "destination";
    private static final String ARG_SHOW = "l";
    private static final String ARG_DONT_ENCRYPT_NAMES = "r";
    private static final String ARG_WRAP_NAMES_ENABLE = "ew";
    private static final String ARG_WRAP_NAMES_DISABLE = "dw";
    
    private static final String DESCRIPTION = 
        "" + VersionInfos.APP_SHORT_NAME + "'s external decryption tool.\ndecrypt -" 
        + ARG_SOURCE + "=<" + ARG_SOURCE + "> -"
        + ARG_ALG + "=<" + ARG_ALG + "> -"
        + ARG_PASSWD + "=<" + ARG_PASSWD + "> -"
        + ARG_DESTINATION + "=<" + ARG_DESTINATION + "> [-" + ARG_SHOW + "] [-" + ARG_DONT_ENCRYPT_NAMES + "] [-" + ARG_WRAP_NAMES_ENABLE + "] [-" + ARG_WRAP_NAMES_DISABLE + "]"
        ;
    
	private String algorithm="";
	private String encryption="";
	private String mountPoint="";
	private String source="";
	private String targetDir="";
	private boolean disableNameDecryption=false;
	private boolean justShow=false;
	private String nameWrappingMode=EncryptedFileSystemDriver.WRAP_DEFAULT;
	
    public static void main(String[] args) {
        CmdLineDeCipher launcher = new CmdLineDeCipher();
        launcher.launch(args);
        launcher.exit();
    }
    
	public CmdLineDeCipher() {
	}

	protected boolean returnErrorCode() {
		return true;
	}

	public boolean init(String args[]) {
		CommandLineParser parser=new CommandLineParser();
        parser.setDescription(DESCRIPTION);
        
		parser.addParameter(new StringCmdLineOption(true,ARG_ALG,"Encryption algorithm [DESede_HASH, AES_HASH]"));
		parser.addParameter(new StringCmdLineOption(true,ARG_PASSWD,"Key phrase to use for decoding"));
		parser.addParameter(new StringCmdLineOption(true,ARG_SOURCE,"Source directory"));
		parser.addParameter(new StringCmdLineOption(false,ARG_FILE,"Specific file to extract"));
		parser.addParameter(new StringCmdLineOption(true,ARG_DESTINATION,"Destination Directory "));
		parser.addParameter(new BooleanCmdLineOption(false,ARG_SHOW,"Display only mode"));
		parser.addParameter(new BooleanCmdLineOption(false,ARG_DONT_ENCRYPT_NAMES,"Disable filenames decryption"));
		parser.addParameter(new BooleanCmdLineOption(false,ARG_WRAP_NAMES_DISABLE,"Disable wrapping of encrypted filenames"));
		parser.addParameter(new BooleanCmdLineOption(false,ARG_WRAP_NAMES_ENABLE,"Force wrapping of encrypted filenames"));
		
		try {
			parser.parse(args, null);
			
			algorithm =  (String)parser.getParameter(ARG_ALG).getValue();
			encryption=  (String)parser.getParameter(ARG_PASSWD).getValue();
			mountPoint=  (String)parser.getParameter(ARG_SOURCE).getValue();
			source    =  (String)parser.getParameter(ARG_FILE).getValue();
			targetDir =  (String)parser.getParameter(ARG_DESTINATION).getValue();
			justShow  =  ((Boolean)parser.getParameter(ARG_SHOW).getValue()).booleanValue();
			disableNameDecryption  =  ((Boolean)parser.getParameter(ARG_DONT_ENCRYPT_NAMES).getValue()).booleanValue();
			
			boolean enableWrapping = ((Boolean)parser.getParameter(ARG_WRAP_NAMES_ENABLE).getValue()).booleanValue();
			boolean disableWrapping = ((Boolean)parser.getParameter(ARG_WRAP_NAMES_DISABLE).getValue()).booleanValue();
			
			if (enableWrapping) {
				nameWrappingMode = EncryptedFileSystemDriver.WRAP_ENABLED;
			} else if (disableWrapping) {
				nameWrappingMode = EncryptedFileSystemDriver.WRAP_DISABLED;
			}
		} catch (CmdLineParserException e) {
			System.out.println("Syntax error : " + e.getMessage());
			System.out.println(parser.usage());
			return false;
		}	
		return true;
	}
	
	protected void initializeFileSystemManager() {
		EncryptionPolicy policy=new EncryptionPolicy();
		policy.setEncrypted(true);
		policy.setEncryptionAlgorithm(algorithm);
		policy.setEncryptionKey(encryption);
		policy.setEncryptNames(! disableNameDecryption);
		policy.setNameWrappingMode(nameWrappingMode);
		
		try {
		    File mnt = new File(mountPoint);
			FileSystemDriver driver=policy.initFileSystemDriver(mnt, new DefaultFileSystemDriver());
			FileSystemManager.getInstance().registerDriver(mnt, driver);
		} catch (ApplicationException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (DriverAlreadySetException e) {
			System.out.println(e);
		}
	}
		
	protected void process() throws IOException{
		if(justShow) {
			display();
		} else {
			FileTool fileMgr=FileTool.getInstance();
			String fileToProcess=(source!=null) ? source  : mountPoint;
			fileMgr.copy(new File(fileToProcess), new File(targetDir));
		}
	}
	
	protected void display() {
		String fileToProcess=(source!=null) ?source  : mountPoint;
		File tg = new File(fileToProcess);
		FileSystemIterator iterator = new FileSystemIterator(tg, false, true, true, true);
        showLine();
		while(iterator.hasNext()){
			File currentFile=(File)iterator.next();
			System.out.println(currentFile.getAbsoluteFile());
		}
        showLine();
	}

    protected void launchImpl(String[] args) {
		try {
            // Here Parse the commandLine
            // -Alg=Algo -Sen=Sentence -Source=source -Dest=destination
            CmdLineDeCipher deCipher = new CmdLineDeCipher();

            if (deCipher.init(args)) {
            	System.out.println("Source : " + deCipher.source);
            	System.out.println("Destination : " + deCipher.targetDir);
            	System.out.println("Algorithm : " + deCipher.algorithm);
            	System.out.println("Passphrase : " + deCipher.encryption);
            	System.out.println("Disable Name Decryption : " + deCipher.disableNameDecryption);
            	System.out.println("File Name Wrapping : " + deCipher.nameWrappingMode);
            	deCipher.initializeFileSystemManager();
            	deCipher.process();
            }
        } catch (Throwable e) {
        	setErrorCode(ERR_UNEXPECTED);
            System.out.println("\nWARNING : An error occurred during decryption. You should check that all your arguments are valid (encryption algorithm or password, source directory, ...)");
            showLine();
            e.printStackTrace();
            showLine();
        }
	}
}
