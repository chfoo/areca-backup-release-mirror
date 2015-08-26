package com.application.areca.processor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.application.areca.ApplicationException;
import com.application.areca.ArecaConfiguration;
import com.application.areca.context.ProcessContext;
import com.application.areca.impl.tools.TagHelper;
import com.myJava.object.Duplicable;
import com.myJava.object.HashHelper;
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
public class SendMailProcessor extends AbstractMailSendProcessor {

	public SendMailProcessor() {
		super();
	}
	
	public boolean requireStatistics() {
		return false;
	}

	public String getKey() {
		return "Send email";
	}

	public void runImpl(ProcessContext context) throws ApplicationException {
		 PrintStream str = null;
		 ByteArrayOutputStream baos = null;
		 Logger.defaultLogger().info("Sending email to : " + this.getRecipients() + " using SMTP server : " + this.getSmtpServer());
		 try {
			 String subject = TagHelper.replaceParamValues(this.getTitle(), context);
			 String content = TagHelper.replaceParamValues(this.getMessage(), context);

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
				 Logger.defaultLogger().info(baos.toString(), "SendMailProcessor.runImpl()");
			 }
		 }
	 }

	 public Duplicable duplicate() {
		 SendMailProcessor pro = new SendMailProcessor();
		 copyAttributes(pro);
		 return pro;
	 }

	 public boolean equals(Object obj) {
		 if (obj == null || (! (obj instanceof SendMailProcessor)) ) {
			 return false;
		 } else {
			 SendMailProcessor other = (SendMailProcessor)obj;
			 return super.equals(other);
		 }
	 }

	 public int hashCode() {
		 int h = HashHelper.initHash(this);
		 h = HashHelper.hash(h, super.hashCode());                       
		 return h;
	 }
}
