package com.myJava.util.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

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
public class DeprecatedVersionDataAdapter implements VersionDataAdapter {

	public VersionData read(InputStream in) throws VersionDataAdapterException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			VersionData data = new VersionData();
			String line;

			// Version
			line = reader.readLine();
			if (line == null) {
				throw new VersionDataAdapterException("Version ID not found");
			} else {
				data.setVersionId(line);
			}

			// Date AAAA-MM-DD
			line = reader.readLine();
			if (line == null) {
				throw new VersionDataAdapterException("Date not found");
			} else {
				StringTokenizer stt = new StringTokenizer(line, "-");
				int y = Integer.parseInt(stt.nextToken());
				int m = Integer.parseInt(stt.nextToken());
				int d = Integer.parseInt(stt.nextToken());

				data.setVersionDate(new GregorianCalendar(y, m, d));
			}

			// Download URL
			line = reader.readLine();
			if (line == null) {
				throw new VersionDataAdapterException("Download URL not found");
			} else {
				data.setDownloadUrl(new URL(line));
			}

			// Description (all remaining lines)
			String description = "";
			while ((line = reader.readLine()) != null) {
				description += line + "\n";
			}
			data.setDescription(description);

			return data;
		} catch (NumberFormatException e) {
			throw new VersionDataAdapterException(e);
		} catch (MalformedURLException e) {
			throw new VersionDataAdapterException(e);
		} catch (IOException e) {
			throw new VersionDataAdapterException(e);
		}
	}
}
