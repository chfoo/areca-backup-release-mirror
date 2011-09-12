package com.application.areca.processor;

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
import com.application.areca.ArecaConfiguration;
import com.application.areca.context.ProcessContext;
import com.application.areca.context.ProcessReport;
import com.application.areca.context.ProcessReportWriter;
import com.application.areca.impl.tools.TagHelper;
import com.application.areca.version.VersionInfos;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.system.OSTool;
import com.myJava.util.CommonRules;
import com.myJava.util.log.Logger;

/**
 * Sends the report by email
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2011, Olivier PETRUCCI.

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
public class MailSendProcessor extends AbstractProcessor {
	private String smtpServer;
	private boolean smtps;
	private String from;
	private String recipients;
	private String user;
	private String password;
	private String title = "" + VersionInfos.APP_SHORT_NAME + " : backup report for target %TARGET_NAME%.";
	private String intro = "Backup report :";
	private boolean appendStatistics;

	public MailSendProcessor() {
		super();
	}
	
	public boolean requireStatictics() {
		return appendStatistics;
	}

	public boolean isAppendStatistics() {
		return appendStatistics;
	}

	public void setAppendStatistics(boolean appendStatistics) {
		this.appendStatistics = appendStatistics;
	}
	
	public String getKey() {
		return "Send report by email";
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

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public boolean isSmtps() {
		return smtps;
	}

	public void setSmtps(boolean smtps) {
		this.smtps = smtps;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	 public void runImpl(ProcessContext context) throws ApplicationException {
		 PrintStream str = null;
		 ByteArrayOutputStream baos = null;
		 Logger.defaultLogger().info("Sending a mail report to : " + this.recipients + " using SMTP server : " + this.smtpServer);
		 try {
			 String subject = TagHelper.replaceParamValues(this.title, context);
			 String content = 
				 TagHelper.replaceParamValues(this.intro, context)
				 + "\n"
				 + getReportAsText(context.getReport());

			 if (ArecaConfiguration.get().isSMTPDebugMode()) {
				 baos = new ByteArrayOutputStream();
				 str = new PrintStream(baos);
			 }

			 sendMail(
					 subject,
					 content,
					 str,
					 context
			 );
		 } catch (Exception e) {
			 Logger.defaultLogger().error("Error during mail processing", e);
			 throw new ApplicationException("Error during mail processing", e);
		 } finally {
			 if (baos != null) {
				 Logger.defaultLogger().info(baos.toString(), "MailSendPostProcessor.run()");
			 }
		 }
	 }

	 public void sendMail(
			 String subject,
			 String content,
			 PrintStream debugStream,
			 ProcessContext context
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
			 List recp = getAddressesAsList();
			 InternetAddress[] addresses = new InternetAddress[recp.size()];
			 for (int i=0; i<addresses.length; i++) {
				 addresses[i] = new InternetAddress((String)recp.get(i));
			 }

			 MimeMessage msg = new MimeMessage(session);

			 InternetAddress fromAddress = (this.from == null || this.from.trim().length() == 0) ? addresses[0] : new InternetAddress(this.from);

			 msg.setFrom(fromAddress);
			 msg.setRecipients(Message.RecipientType.TO, addresses);
			 msg.setReplyTo(new InternetAddress[] {fromAddress});
			 msg.setSubject(subject);
			 msg.setText(content, OSTool.getIANAFileEncoding());
			 msg.setSentDate(new Date());
			 msg.setSender(fromAddress);
			 msg.setSentDate(new Date());
			 if (context != null && context.getReport() != null) {
				 msg.setHeader("X-" + VersionInfos.APP_SHORT_NAME + "-Target", context.getReport().getTarget().getUid());
			 }
			 msg.setHeader("X-" + VersionInfos.APP_SHORT_NAME + "-Version", VersionInfos.getLastVersion().getVersionId());
			 if (isAuthenticated()) {
				 Transport tr;
				 if (isSmtps()) {
					 props.put("mail.smtps.auth", "true");
					 tr = session.getTransport("smtps");
				 } else {
					 props.put("mail.smtp.auth", "true");
					 tr = session.getTransport("smtp");
				 }

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

	 /**
	  * Builds a string representation of the report using a report writer. 
	  */
	 private String getReportAsText(ProcessReport report) throws IOException {
		 ProcessReportWriter writer = null;
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 try {
			 writer = new ProcessReportWriter(new OutputStreamWriter(baos), appendStatistics);
			 writer.writeReport(report);
		 } finally {
			 writer.close();            
		 }

		 return baos.toString();
	 }

	 public String getParametersSummary() {
		 return this.recipients + " - " + this.smtpServer;
	 }

	 public Duplicable duplicate() {
		 MailSendProcessor pro = new MailSendProcessor();
		 copyAttributes(pro);
		 pro.recipients = this.recipients;
		 pro.smtpServer = this.smtpServer;   
		 pro.user = this.user;
		 pro.password = this.password;
		 pro.title = this.title;
		 pro.smtps = this.smtps;
		 pro.intro = this.intro;
		 pro.from = this.from;
		 return pro;
	 }

	 public void validate() throws ProcessorValidationException {
		 if (smtpServer == null || smtpServer.trim().length() == 0) {
			 throw new ProcessorValidationException("A SMTP server must be provided");
		 }
		 if (recipients == null || recipients.trim().length() == 0) {
			 throw new ProcessorValidationException("At least one recipient must be provided");
		 }

		 List recp = getAddressesAsList();
		 for (int i=0; i<recp.size(); i++) {
			 if (! CommonRules.checkEmail((String)recp.get(i))) {
				 throw new ProcessorValidationException("Invalid Email : " + recp.get(i));
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
		 if (obj == null || (! (obj instanceof MailSendProcessor)) ) {
			 return false;
		 } else {
			 MailSendProcessor other = (MailSendProcessor)obj;
			 return 
			 super.equals(other)
			 && EqualsHelper.equals(this.password, other.password)
			 && EqualsHelper.equals(this.user, other.user)
			 && EqualsHelper.equals(this.smtpServer, other.smtpServer)
			 && EqualsHelper.equals(this.recipients, other.recipients)
			 && EqualsHelper.equals(this.smtps, other.smtps)
			 && EqualsHelper.equals(this.title, other.title)      
			 && EqualsHelper.equals(this.intro, other.intro)      
			 && EqualsHelper.equals(this.from, other.from)                     
			 ;
		 }
	 }

	 public int hashCode() {
		 int h = HashHelper.initHash(this);
		 h = HashHelper.hash(h, super.hashCode());
		 h = HashHelper.hash(h, this.password);
		 h = HashHelper.hash(h, this.user);
		 h = HashHelper.hash(h, this.smtpServer);
		 h = HashHelper.hash(h, this.recipients);  
		 h = HashHelper.hash(h, this.smtps);             
		 h = HashHelper.hash(h, this.title);
		 h = HashHelper.hash(h, this.intro);    
		 h = HashHelper.hash(h, this.from);            
		 return h;
	 }
}
