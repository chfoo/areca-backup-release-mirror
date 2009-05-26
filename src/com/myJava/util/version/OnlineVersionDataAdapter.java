package com.myJava.util.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Reads a VersionData from an URL.
 * <BR>The content is expected to have the following format :
 * <BR>- VersionID (eg: 3.1.6)
 * <BR>- VersionDate : AAAA-M-D (eg: 2006-7-15)
 * <BR>- DownloadUrl (eg: http://download.myapp.net) 
 * <BR>- Description (A single line of text) 
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2009, Olivier PETRUCCI.

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
public class OnlineVersionDataAdapter implements VersionDataAdapter {

    private URL checkUrl;

    public URL getCheckUrl() {
        return checkUrl;
    }
    public void setCheckUrl(URL checkUrl) {
        this.checkUrl = checkUrl;
    }
    
    public VersionData readVersionData() throws VersionDataAdapterException {
        BufferedReader in = null;
        
        try {
            VersionData data = new VersionData();
            
            in = new BufferedReader(new InputStreamReader(checkUrl.openStream()));

            String line;
            
            // Version
            line = in.readLine();
            if (line == null) {
                throw new VersionDataAdapterException("Version ID not found");
            } else {
                data.setVersionId(line);
            }
            
            // Date AAAA-MM-DD
            line = in.readLine();
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
            line = in.readLine();
            if (line == null) {
                throw new VersionDataAdapterException("Download URL not found");
            } else {
                data.setDownloadUrl(new URL(line));
            }
            
            // Description (all remaining lines)
            String description = "";
            while ((line = in.readLine()) != null) {
                description += line + "\n";
            }
            data.setDescription(description);

            return data;
        } catch (IOException e) {
            throw new VersionDataAdapterException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // Ignored
                }
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            OnlineVersionDataAdapter adapter = new OnlineVersionDataAdapter();
            adapter.setCheckUrl(new URL("http://areca.sourceforge.net/version.php"));
            
            System.out.println(adapter.readVersionData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
