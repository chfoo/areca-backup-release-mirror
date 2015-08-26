package com.application.areca.launcher.tui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
public class UserCommand {
    private String name;
    private Map mandatoryArguments = new HashMap();
    private Map optionalArguments = new HashMap();

    public UserCommand(String name) {
        super();
        this.name = name;
    }
    
    public void addMandatoryArgument(UserOption option) {
        this.mandatoryArguments.put(option.getName(), option);
    }
    
    public void addOptionalArgument(UserOption option) {
        this.optionalArguments.put(option.getName(), option);
    }

    public String getName() {
        return name;
    }
    
    public UserOption getArgument(String optionName) {
        UserOption opt = (UserOption)this.mandatoryArguments.get(optionName);
        if (opt == null) {
            opt = (UserOption)this.optionalArguments.get(optionName);
        }
        
        return opt;
    }
    
    public boolean isMandatory(String optionName) {
        return mandatoryArguments.containsKey(optionName);
    }
    
    public void validateArguments(Map args) throws InvalidCommandException {
        Iterator iter = this.mandatoryArguments.values().iterator();
        while (iter.hasNext()) {
            UserOption option = (UserOption)iter.next();

            String value = (String)args.get(option.getName());
            if (value == null) {
                throw new InvalidCommandException("the following parameter is mandatory : " + option.getName());
            }
        }
    }
}
