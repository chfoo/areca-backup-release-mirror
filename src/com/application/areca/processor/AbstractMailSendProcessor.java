package com.application.areca.processor;

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
import com.application.areca.context.ProcessContext;
import com.application.areca.version.VersionInfos;
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
public abstract class AbstractMailSendProcessor extends AbstractProcessor {
	private String smtpServer;
	private boolean smtps;
	private String from;
	private String recipients;
	private String user;
	private String password;
	private String title = VersionInfos.APP_SHORT_NAME;
	private String message = "";
	
	/**
	 * 	In case of SMTPs connection, this boolean tells whether we must issue a "STARTTLS" or try to connect directly to a port which expects encrypted data.
	 * - If set to "true", Areca will connect directly to the server by using an encrypted connection. This mode is deprecated.
	 * - If set to "false", Areca will first establish a plain text connection, then issue a "STARTTLS" command and then switch to encrypted mode. This mode is recommended.
	 */
	private boolean disableSTARTTLS = false;

	public AbstractMailSendProcessor() {
		super();
	}

	public String getRecipients() {
		return recipients;
	}

	public boolean isDisableSTARTTLS() {
		return disableSTARTTLS;
	}

	public void setDisableSTARTTLS(boolean disableSTARTTLS) {
		this.disableSTARTTLS = disableSTARTTLS;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getSmtpServerName() {
		if (smtpServer.indexOf(':') != -1) {
			StringTokenizer stt = new StringTokenizer(smtpServer, ":");
			return stt.nextToken().trim();
		} else {
			return smtpServer.trim();
		}
	}

	public int getSmtpServerPort() {
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

	public String getParametersSummary() {
		return this.getRecipients() + " - " + this.getSmtpServer();
	}

	public void sendMail(
			String subject,
			String content,
			PrintStream debugStream,
			ProcessContext context
	) throws ApplicationException {  
		Properties props = System.getProperties();
		String protocol = isSmtps() && disableSTARTTLS ? "smtps":"smtp";

		props.put("mail." + protocol + ".host", getSmtpServerName());
		props.put("mail." + protocol + ".port", "" + getSmtpServerPort());

		if (isAuthenticated()) {
			props.put("mail." + protocol + ".auth", "true");
		}
		
		if (isSmtps()) {
			props.put("mail." + protocol + ".starttls.enable", "true");
		}

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
				if (isSmtps() && disableSTARTTLS) {
					tr = session.getTransport("smtps");
				} else {
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

	protected void copyAttributes(AbstractMailSendProcessor pro) {
		super.copyAttributes(pro);
		pro.recipients = this.recipients;
		pro.smtpServer = this.smtpServer;   
		pro.user = this.user;
		pro.password = this.password;
		pro.smtps = this.smtps;
		pro.from = this.from;
		pro.title = this.title;
		pro.disableSTARTTLS = this.disableSTARTTLS;
		pro.message = this.message;
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
		if (obj == null || (! (obj instanceof AbstractMailSendProcessor)) ) {
			return false;
		} else {
			AbstractMailSendProcessor other = (AbstractMailSendProcessor)obj;
			return super.equals(other)
			&& EqualsHelper.equals(this.password, other.password)
			&& EqualsHelper.equals(this.user, other.user)
			&& EqualsHelper.equals(this.smtpServer, other.smtpServer)
			&& EqualsHelper.equals(this.recipients, other.recipients)
			&& EqualsHelper.equals(this.smtps, other.smtps)   
			&& EqualsHelper.equals(this.from, other.from)    
			&& EqualsHelper.equals(this.title, other.title)   
			&& EqualsHelper.equals(this.disableSTARTTLS, other.disableSTARTTLS)  
			&& EqualsHelper.equals(this.message, other.message)  
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
		h = HashHelper.hash(h, this.disableSTARTTLS);   
		h = HashHelper.hash(h, this.from);   
		h = HashHelper.hash(h, this.title);
		h = HashHelper.hash(h, this.message);         
		return h;
	}
}
