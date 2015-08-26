package com.myJava.util.version;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;
import com.myJava.object.ToStringHelper;

/**
 * Class containing various informations about a sotfware's version
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
public class VersionData {

    private String versionId;
    private URL downloadUrl;
    private GregorianCalendar versionDate;
    private String description;
    private String additionalNotes;
    private String implementationNotes;
    
    public VersionData() {
    }
    
    public VersionData(String versionId, GregorianCalendar versionDate, String description) {
        this.versionId = versionId;
        this.versionDate = versionDate;
        this.description = description;
    }
    
    public VersionData(String versionId, GregorianCalendar versionDate, URL downloadUrl, String description) {
    	this(versionId, versionDate, description);
        this.downloadUrl = downloadUrl;
    }
    
    public VersionData(String versionId, GregorianCalendar versionDate, String description, String additionalNotes) {
    	this(versionId, versionDate, description);
        this.additionalNotes = additionalNotes;
    }
    
    public boolean isAfterOrEquals(VersionData other) {
    	return isAfterOrEquals(other.versionId);
    }
    
    public boolean isAfterOrEquals(String otherId) {
    	String[] tc = this.versionId.split(".");
    	String[] oc = otherId.split(".");
    	
    	int nb = Math.min(tc.length, oc.length);
    	for (int i=0; i<nb; i++) {
    		int ti = Integer.parseInt(tc[i]);
    		int oi = Integer.parseInt(oc[i]);
    		if (ti < oi) {
    			return false;
    		} else if (ti > oi) {
    			return true;
    		}
    	}
    	
    	return (tc.length >= oc.length);
    }
    
    public VersionData(String versionId, GregorianCalendar versionDate, String description, String additionalNotes, String implementationNotes) {
    	this(versionId, versionDate, description, additionalNotes);
        this.implementationNotes = implementationNotes;
    }
    
    public int getYear() {
    	return this.versionDate.get(Calendar.YEAR);
    }
    
    public int getMonth() {
    	return this.versionDate.get(Calendar.MONTH);
    }
    
    public int getDay() {
    	return this.versionDate.get(Calendar.DAY_OF_MONTH);
    }
    
    public URL getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public GregorianCalendar getVersionDate() {
        return versionDate;
    }
    
    public void setVersionDate(GregorianCalendar versionDate) {
        this.versionDate = versionDate;
    }
    
    public String getVersionId() {
        return versionId;
    }
    
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdditionalNotes() {
		return additionalNotes;
	}

	public void setAdditionalNotes(String additionalNotes) {
		this.additionalNotes = additionalNotes;
	}

	public String getImplementationNotes() {
		return implementationNotes;
	}

	public void setImplementationNodes(String implementationNodes) {
		this.implementationNotes = implementationNodes;
	}

	public String toString() {
        StringBuffer sb = ToStringHelper.init(this);
        ToStringHelper.append("Version", versionId, sb);
        ToStringHelper.append("Date", versionDate.get(Calendar.YEAR) + "-" + (versionDate.get(Calendar.MONTH) + 1) + "-" + versionDate.get(Calendar.DAY_OF_MONTH), sb);
        ToStringHelper.append("URL", downloadUrl, sb);
        ToStringHelper.append("Description", description, sb); 	
        ToStringHelper.append("Additional Notes", additionalNotes, sb); 	
        ToStringHelper.append("Implementation Notes", implementationNotes, sb); 	
        return ToStringHelper.close(sb);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (! (obj instanceof VersionData)) {
            return false;
        } else {
            VersionData other = (VersionData)obj;
            return EqualsHelper.equals(other.getVersionId(), this.getVersionId());
        }
    }
    
    public int hashCode() {
        int hash = HashHelper.initHash(this);
        hash = HashHelper.hash(hash, this.versionId);
        return hash;
    }
}
