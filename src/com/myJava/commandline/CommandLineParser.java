package com.myJava.commandline;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


/**
 * <BR>
 * @author Ludovic QUESNELLE
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
public class CommandLineParser {

    private HashMap argMap = new HashMap(10);
    private String description;

    public void addParameter(CmdLineOption opt) {
        argMap.put(opt.getName(), opt);
    }

    public CmdLineOption getParameter(String name)
    throws CmdLineParserException {
        CmdLineOption opt = (CmdLineOption) argMap.get(name);
        if (opt == null)
            throw new CmdLineParserException(
                    CmdLineParserException.UNKNOWN_OPTION, name);
        return opt;
    }

    protected void manageOptionWithValue(String name, String value)
    throws CmdLineParserException {

        CmdLineOption option = getParameter(name);
        if (option != null) {
            if (option.getType() == CmdLineOption.STRING) {
                StringCmdLineOption stringOption = (StringCmdLineOption) option;
                stringOption.setValue(value);
            } else if (option.getType() == CmdLineOption.BOOLEAN) {
                boolean bvalue = Boolean.valueOf(value).booleanValue();
                BooleanCmdLineOption boolOption = (BooleanCmdLineOption) option;
                boolOption.setValue(bvalue);
            } else
                throw new CmdLineParserException(
                        CmdLineParserException.TYPE_MISMATCH, name);
        } else
            throw new CmdLineParserException(
                    CmdLineParserException.UNKNOWN_OPTION, name);
    }

    protected void manageBooleanOption(String name, boolean isSet)
    throws CmdLineParserException {

        CmdLineOption option = getParameter(name);
        if (option != null) {
            if (option.getType() == CmdLineOption.BOOLEAN) {
                BooleanCmdLineOption boolOption = (BooleanCmdLineOption) option;
                boolOption.setValue(isSet);
            } else
                throw new CmdLineParserException(
                        CmdLineParserException.TYPE_MISMATCH, name);
        } else
            throw new CmdLineParserException(
                    CmdLineParserException.UNKNOWN_OPTION, name);
    }

    public final void parse(String[] argv, Locale locale)
    throws CmdLineParserException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        int position = 0;
        boolean isArray = false;
        ArrayCmdLineOption arrayOption = null;

        while (position < argv.length) {
            String current = argv[position];
            String argName = null;
            String argValue = null;

            if (current.startsWith("-")) { // handle -arg=value
                isArray = false;
                int separator = current.indexOf("=");
                if (separator != -1) {
                    argValue = current.substring(separator + 1);
                    argName = current.substring(1, separator);
                    manageOptionWithValue(argName, argValue);
                } else
                    argName = current.substring(1);
                CmdLineOption opt = getParameter(argName);
                if (opt.getType() == CmdLineOption.BOOLEAN) {
                    manageBooleanOption(argName, true);
                } else if (opt.getType() == CmdLineOption.ARRAY) {
                    isArray = true;
                    arrayOption = (ArrayCmdLineOption) opt;
                }
            } else { // Does not start with - so a sub command line
                if (current.length() != 0) {
                    if (isArray == false) {
                        throw new CmdLineParserException(
                                CmdLineParserException.UNKNOWN_OPTION, current);
                    } else {
                        arrayOption.addParamOption(current);
                    }
                }
            }
            position++;
        }

        // Check mandatory arguments
        Iterator it = argMap.values().iterator();
        while (it.hasNext()) {
            CmdLineOption option = (CmdLineOption) it.next();
            if (option.isMandatory() && (! option.hasBeenSet()))
                throw new CmdLineParserException(CmdLineParserException.MISSING_MANDATORY_OPTION, option.getName());
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String usage() {
        StringBuffer buf=new StringBuffer();
        if (description != null) {
            buf.append("\n").append(description).append("\n\n");
        }
        buf.append("Arguments:\n");

        Iterator it = argMap.values().iterator();
        while (it.hasNext()) {
            CmdLineOption option = (CmdLineOption) it.next();
            buf.append("\t");
            buf.append("-").append(option.getName());
            buf.append(" (").append(option.isMandatory() ? "mandatory" : "optional").append(") : ");
            buf.append(option.getComment()).append("\n");
        }
        
        buf.append("\nExample : ");
        it = argMap.values().iterator();
        while (it.hasNext()) {
            CmdLineOption option = (CmdLineOption) it.next();
            if (option.isMandatory()) {
                buf.append("-").append(option.getName()).append("=").append("my_").append(option.getName()).append(" ");
            }
        }
        buf.append("\n");
        
        return buf.toString();
    }
}
