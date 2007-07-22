package com.application.areca.metadata.data;

import java.util.Properties;

import com.application.areca.version.VersionInfos;
import com.myJava.util.version.VersionData;


/**
 * Implementation of an Archive's MetaData : Stores technical data about the system / backup engine that was used
 * to generate this archive ... useful to read this archive back.
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 * <BR>Areca Build ID : -1700699344456460829
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
public class MetaData implements MetaDataKeys {

    private Properties props = new Properties();
    
    public String getEngineVersionId() {
        return props.getProperty(META_VERSION);
    }
    
    public VersionData getEngineVersion() {
        return VersionInfos.getVersion(getEngineVersionId());
    }
    
    public String getFileEncoding() {
        return props.getProperty(META_ENCODING);
    }
    
    public void setFileEncoding(String enc) {
        this.props.put(META_ENCODING, enc);
    }
    
    public void setEngineVersionId(String id) {
        this.props.put(META_VERSION, id);
    }
    
    public String getOSName() {
        return props.getProperty(META_OS);
    }
    
    public void setOSName(String enc) {
        this.props.put(META_OS, enc);
    }
    
    public String getEngineBuildId() {
        return props.getProperty(META_BUILD_ID);
    }
    
    public void setEngineBuildId(String id) {
        this.props.put(META_BUILD_ID, id);
    }
    
    protected Properties getProperties() {
        return this.props;
    }
}
