package com.application.areca;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Target group
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2010, Olivier PETRUCCI.

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
public class TargetGroup 
extends AbstractWorkspaceItem {
    private String name;
    private HashMap content = new HashMap();
    
    public TargetGroup(String name) {
        this.name = name;
    }

	public void doBeforeDelete() {
    	Iterator iter = this.getIterator();
    	while (iter.hasNext()) {
    		WorkspaceItem item = (WorkspaceItem)iter.next();
    		item.doBeforeDelete();
    	}
    }

    public void doAfterDelete() {
    	Iterator iter = this.getIterator();
    	while (iter.hasNext()) {
    		WorkspaceItem item = (WorkspaceItem)iter.next();
    		item.doAfterDelete();
    	}
    }
    
	public Iterator getSortedIterator() {
		Object[] data = new Object[content.size()];
		Iterator iter = getIterator();
		int i = 0;
		while (iter.hasNext()) {
			data[i++] = iter.next();
		}
		Arrays.sort(data, new WorkspaceItemComparator());

		return Arrays.asList(data).iterator();
	}
	
	private static class WorkspaceItemComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			String id0 = ((WorkspaceItem)arg0).getName();
			String id1 = ((WorkspaceItem)arg1).getName();
			
			return id0.compareTo(id1);
		}
	}
	
	public boolean contains(WorkspaceItem item) {
		return content.containsKey(item.getUid());
	}
	
	public void destroyRepository() throws ApplicationException {
		Iterator iter = this.getIterator();
		while (iter.hasNext()) {
			((WorkspaceItem)iter.next()).destroyRepository();
		}
	}
    
    public String getName() {
    	return name;
    }

    public SupportedBackupTypes getSupportedBackupSchemes() {
    	Iterator iter = getIterator();
    	SupportedBackupTypes ret = new SupportedBackupTypes();
    	
    	while (iter.hasNext()) {
        	WorkspaceItem item = (WorkspaceItem)iter.next();
        	SupportedBackupTypes types = item.getSupportedBackupSchemes();
        	
            if (types.isSupported(AbstractTarget.BACKUP_SCHEME_INCREMENTAL)) {
            	ret.setSupported(AbstractTarget.BACKUP_SCHEME_INCREMENTAL);
            }
            if (types.isSupported(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL)) {
            	ret.setSupported(AbstractTarget.BACKUP_SCHEME_DIFFERENTIAL);
            }
            if (types.isSupported(AbstractTarget.BACKUP_SCHEME_FULL)) {
            	ret.setSupported(AbstractTarget.BACKUP_SCHEME_FULL);
            }
    	}
    	
    	return ret;
	}

	public void linkChild(WorkspaceItem item) {
		if (this != item.getParent()) { // Yeah .... instance check !
			item.setParent(this);
		}
        this.content.put(item.getUid(), item);
    }
    
    // Backward compatibility
    public AbstractTarget getTarget(int id) {
    	Iterator iter = this.getIterator();
    	while (iter.hasNext()) {
    		Object o = iter.next();
    		if (o instanceof TargetGroup) {
    			AbstractTarget ret = ((TargetGroup)o).getTarget(id);
    			if (ret != null) {
    				return ret;
    			}
    		} else {
    			AbstractTarget target = (AbstractTarget)o;
    			if (target.getId() == id) {
    				return target;
    			}
    		}
    	}
        return null;
    }
    
    public WorkspaceItem getItem(String uid) {
    	return (WorkspaceItem)content.get(uid);
    }
    
	public void remove(String id) {
		WorkspaceItem itm = (WorkspaceItem)content.get(id);
		if (itm != null) {
			itm.doBeforeDelete();
			this.content.remove(id);
			itm.doAfterDelete();
		}
	}
    
    public boolean isRunning() {
    	Iterator iter = this.getIterator();
    	while (iter.hasNext()) {
    		WorkspaceItem item = (WorkspaceItem)iter.next();
			if (item.isRunning()) {
				return true;
			}
    	}
        
        return false;
    }
    
    public int size() {
        return this.content.size();
    }
    
    public Iterator getIterator() {
        return this.content.values().iterator();
    }

    public String getUid() {
        return name;
    }
    
    
    public String toString() {
        return "Group : " + this.name;
    }
    
    public String getDescription() {
        StringBuffer buf = new StringBuffer();        
        Iterator iter = this.getIterator();
        boolean first = true;
        while (iter.hasNext()) {
            WorkspaceItem item = (WorkspaceItem)iter.next();  
            if (! first) {
            	buf.append("\n\n");
            }
            first = false;
            buf.append(item.getDescription());
        }        
        
        return new String(buf);
    }
	
	public File computeConfigurationFile(File root) {
		return new File(root, getAncestorPath());
	}
	
	public File computeConfigurationFile(File root, boolean appendAncestors) {
		File f = computeConfigurationFile(root);
		if (appendAncestors) {
			return f;
		} else {
			return new File(root, f.getName());
		}
	}
	
	public String getAncestorPath() {
		String ret = "";
		
		if (parent != null) {
			String prefix = parent.getAncestorPath();
			if (prefix.length() == 0) {
				ret = this.name;
			} else {
				ret = prefix + "/" + this.name;
			}
		}
		
		return ret;
	}
}

