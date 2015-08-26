package com.myJava.file.driver.remote.ftp;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ftp.FTPReply;

import com.myJava.util.log.Logger;

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
public class FTPClient extends org.apache.commons.net.ftp.FTPClient {

	private boolean ignorePasvErrors = false;

	public FTPClient(boolean ignorePasvErrors) {
		super();
		this.ignorePasvErrors = ignorePasvErrors;
	}

	public int pasv() throws IOException {
		int passiveReturnCode = super.pasv();

		if (passiveReturnCode == FTPReply.ENTERING_PASSIVE_MODE) {

			// Parse and display reply host and port in passive mode
			// It's quite ugly but there is no entry point in apache's ftp library to perform this check.
			String reply = getReplyStrings()[0];
			int i, index, lastIndex;
			String octet1, octet2;
			StringBuffer host;

			reply = reply.substring(reply.indexOf('(') + 1, reply.indexOf(')')).trim();

			host = new StringBuffer(24);
			lastIndex = 0;
			index = reply.indexOf(',');
			host.append(reply.substring(lastIndex, index));

			for (i = 0; i < 3; i++) {
				host.append('.');
				lastIndex = index + 1;
				index = reply.indexOf(',', lastIndex);
				host.append(reply.substring(lastIndex, index));
			}

			lastIndex = index + 1;
			index = reply.indexOf(',', lastIndex);

			octet1 = reply.substring(lastIndex, index);
			octet2 = reply.substring(index + 1);

			// index and lastIndex now used as temporaries
			try {
				index = Integer.parseInt(octet1);
				lastIndex = Integer.parseInt(octet2);
			} catch (NumberFormatException e) {
				throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
			}

			index <<= 8;
			index |= lastIndex;

			String passvHost = host.toString();

			//int passvPort = index;
			//Logger.defaultLogger().info("Passive host received from server : " + passvHost + ":" + passvPort);

			InetAddress refAddress = InetAddress.getByName(passvHost);
			if (! refAddress.equals(this._socket_.getInetAddress())) {
				String msg = "Passive address (" + refAddress + ") differs from host address (" + this._socket_.getInetAddress() + "). This is probably because your server is behind a router. Please check your FTP server's configuration. (for instance, use a masquerade address)";
				Logger.defaultLogger().warn(msg);
				if (! ignorePasvErrors) {
					throw new IOException(msg);
				}
			}
		}

		return passiveReturnCode;
	}
}
