package com.application.areca.postprocess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.application.areca.ApplicationException;
import com.application.areca.ArecaTechnicalConfiguration;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.myJava.util.CommonRules;
import com.myJava.util.EqualsHelper;
import com.myJava.util.HashHelper;
import com.myJava.util.PublicClonable;
import com.myJava.util.log.Logger;
import com.myJava.util.os.OSTool;

/**
 * Sends the report by email
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1628055869823963574
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
public class MailSendPostProcessor extends AbstractPostProcessor {

    private String smtpServer;
    private String recipients;
    private String user;
    private String password;
    private boolean onlyIfError;
    private boolean listFiltered = true;

    public MailSendPostProcessor() {
        super();
    }
    
    public String getRecipients() {
        return recipients;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }

    public boolean isOnlyIfError() {
        return onlyIfError;
    }

    public void setOnlyIfError(boolean onlyIfError) {
        this.onlyIfError = onlyIfError;
    }

    public boolean isListFiltered() {
        return listFiltered;
    }

    public void setListFiltered(boolean listFiltered) {
        this.listFiltered = listFiltered;
    }

    /**
     * Sets the recipients.
     * <BR>Multiple recipients are separated by one of the following characters : ' '    ','    ';' 
     */
    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }
    
    public String getSmtpServer() {
        return smtpServer;
    }
    
    /**
     * Sets the smtp server used for transport 
     */
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    
    private String getSmtpServerName() {
        if (smtpServer.indexOf(':') != -1) {
            StringTokenizer stt = new StringTokenizer(smtpServer, ":");
            return stt.nextToken().trim();
        } else {
            return smtpServer.trim();
        }
    }

    private int getSmtpServerPort() {
        if (smtpServer.indexOf(':') != -1) {
            StringTokenizer stt = new StringTokenizer(smtpServer, ":");
            stt.nextToken();
            return Integer.parseInt(stt.nextToken().trim());
        } else {
            return 25;
        }
    }
    
    private boolean isAuthenticated() {
        return 
        	this.password != null 
        	&& this.password.trim().length() != 0
        	&& this.user  != null
        	&& this.user.trim().length() != 0        	
        ;
    }
    
    public void run(ProcessContext context) throws ApplicationException {
        if ((! context.getReport().isCommited()) || (! this.onlyIfError)) {
            PrintStream str = null;
            ByteArrayOutputStream baos = null;
            try {
                String subject = "Areca : Backup report for target '" + context.getReport().getTarget().getTargetName() + "'";
                String content = getReportAsText(context.getReport());
                
                if (ArecaTechnicalConfiguration.get().isSMTPDebugMode()) {
                    baos = new ByteArrayOutputStream();
                	str = new PrintStream(baos);
                }
    
                sendMail(
                        subject,
                        content,
                        str
                );
            } catch (IOException e) {
                Logger.defaultLogger().error("Error during mail processing", e);
                throw new ApplicationException("Error during mail processing", e);
            } finally {
                if (baos != null) {
                    System.out.println(baos.toString());
                    Logger.defaultLogger().info(baos.toString(), "MailSendPostProcessor.run()");
                }
            }
        } else {
            Logger.defaultLogger().info("No mail report was send because the backup was successfull");
        }
    }
    
    public void sendMail(
            String subject,
            String content,
            PrintStream debugStream
    ) throws ApplicationException {
    	Properties props = System.getProperties();
    	props.put("mail.smtp.host", getSmtpServerName());
    	props.put("mail.smtp.port", "" + getSmtpServerPort());
    	
    	Session session = Session.getInstance(props, null);
    	if (debugStream != null) {
	    	session.setDebug(true);
	    	session.setDebugOut(debugStream);
    	}
    	
    	try {
    	    List recp = getAddressesAsList();;
    	    InternetAddress[] addresses = new InternetAddress[recp.size()];
    	    for (int i=0; i<addresses.length; i++) {
    	        addresses[i] = new InternetAddress((String)recp.get(i));
    	    }
    	    
    	    MimeMessage msg = new MimeMessage(session);
    	    msg.setFrom(addresses[0]);

    	    msg.setRecipients(Message.RecipientType.TO, addresses);
    	    msg.setSubject(subject);
    	    msg.setText(content, OSTool.getIANAFileEncoding());
    	    msg.setSentDate(new Date());
                	    
    	    if (isAuthenticated()) {
        	    props.put("mail.smtp.auth", "true");
                Transport tr = session.getTransport("smtp");
                tr.connect(getSmtpServerName(), getSmtpServerPort(), getUser(), getPassword());
                msg.saveChanges();
                tr.sendMessage(msg, msg.getAllRecipients());
                tr.close();
    	    } else {
    	       Transport.send(msg);
            }
    	} catch (MessagingException e) {
    	    Logger.defaultLogger().error("Error during mail processing", e);
    	    throw new ApplicationException("Error during mail processing", e);
    	}
    }
    
    public boolean requiresFilteredEntriesListing() {
        return listFiltered;
    }
    
    /**
     * Builds a string representation of the report using a report writer. 
     */
    private String getReportAsText(ProcessReport report) throws IOException {
        ProcessReportWriter writer = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writer = new ProcessReportWriter(new OutputStreamWriter(baos), this.listFiltered);
            writer.writeReport(report);
        } finally {
            writer.close();            
        }
        
        return baos.toString();
    }
    
    public String getParametersSummary() {
        return this.recipients + " - " + this.smtpServer;
    }
    
    public PublicClonable duplicate() {
        MailSendPostProcessor pro = new MailSendPostProcessor();
        pro.recipients = this.recipients;
        pro.smtpServer = this.smtpServer;   
        pro.user = this.user;
        pro.password = this.password;
        pro.onlyIfError = this.onlyIfError;
        pro.listFiltered = this.listFiltered;
        return pro;
    }
    
    public void validate() throws PostProcessorValidationException {
        if (smtpServer == null || smtpServer.trim().length() == 0) {
            throw new PostProcessorValidationException("A SMTP server must be provided");
        }
        if (recipients == null || recipients.trim().length() == 0) {
            throw new PostProcessorValidationException("At least one recipient must be provided");
        }
        
        List recp = getAddressesAsList();
        for (int i=0; i<recp.size(); i++) {
            if (! CommonRules.checkEmail((String)recp.get(i))) {
                throw new PostProcessorValidationException("Invalid Email : " + recp.get(i));
            }
        }
    }
    
    private List getAddressesAsList() {
	    ArrayList recp = new ArrayList();
	    StringTokenizer stt = new StringTokenizer(this.recipients, " ,;");
	    while (stt.hasMoreTokens()) {
	        String add = stt.nextToken();
	        if (add != null && add.trim().length() != 0) {
	            recp.add(add);
	        }
	    }
	    return recp;
    }
    
    public boolean equals(Object obj) {
        if (obj == null || (! (obj instanceof MailSendPostProcessor)) ) {
            return false;
        } else {
            MailSendPostProcessor other = (MailSendPostProcessor)obj;
            return 
            	EqualsHelper.equals(this.password, other.password)
            	&& EqualsHelper.equals(this.user, other.user)
            	&& EqualsHelper.equals(this.smtpServer, other.smtpServer)
            	&& EqualsHelper.equals(this.recipients, other.recipients)
                && EqualsHelper.equals(this.onlyIfError, other.onlyIfError)
                && EqualsHelper.equals(this.listFiltered, other.listFiltered)                
            	;
        }
    }
    
    public int hashCode() {
        int h = HashHelper.initHash(this);
        h = HashHelper.hash(h, this.password);
        h = HashHelper.hash(h, this.user);
        h = HashHelper.hash(h, this.smtpServer);
        h = HashHelper.hash(h, this.recipients);
        h = HashHelper.hash(h, this.onlyIfError);
        h = HashHelper.hash(h, this.listFiltered);        
        return h;
    }
}
