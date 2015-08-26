package com.myJava.system;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
public class VMLauncher {
    private String mainClass;
    private String[] args;
    private List transferedOptions = new ArrayList();
    private Map overridenOptions = new HashMap();
    private String javaHome = System.getProperty("java.home");
    private String classPath = System.getProperty("java.class.path");
    private int initialHeap = (int)(Runtime.getRuntime().totalMemory() / (1024*1024));
    private int maxHeap = (int)(Runtime.getRuntime().maxMemory() / (1024*1024));
    private boolean waitForProcess = false;
    
    public VMLauncher() {
        transferedOptions.add("java.library.path");
    }

    public int getInitialHeap() {
        return initialHeap;
    }

    public void setInitialHeap(int initialHeap) {
        this.initialHeap = initialHeap;
    }

    public int getMaxHeap() {
        return maxHeap;
    }

    public boolean isWaitForProcess() {
        return waitForProcess;
    }

    public void setWaitForProcess(boolean waitForProcess) {
        this.waitForProcess = waitForProcess;
    }

    public void setMaxHeap(int maxHeap) {
        this.maxHeap = maxHeap;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public List getTransferedOptions() {
        return transferedOptions;
    }

    public void addTransferedOption(String option) {
        this.transferedOptions.add(option);
    }

    public Map getOverridenOptions() {
        return overridenOptions;
    }

    public void addOverridenOption(String option, String value) {
        overridenOptions.put(option, value);
    }
    
    public void addOverridenOptions(Map values) {
        overridenOptions.putAll(values);
    }

    private String[] getCommandForm() {
        List command = new ArrayList();
        command.add(javaHome + "/bin/java");
        if (maxHeap != -1) {
            command.add("-Xmx" + maxHeap + "m");
        }
        if (initialHeap != -1) {
            command.add("-Xms" + initialHeap + "m");
        }
        command.add("-cp");
        command.add(classPath);

        Iterator transfered = transferedOptions.iterator();
        while (transfered.hasNext()) {
            String option = (String)transfered.next();
            command.add("-D" + option + "=" + System.getProperty(option));
        }

        Iterator overriden = overridenOptions.keySet().iterator();
        while (overriden.hasNext()) {
            String option = (String)overriden.next();
            String value = (String)overridenOptions.get(option);
            command.add("-D" + option + "=" + value);
        }

        command.add(mainClass);

        for (int i=0; i<args.length; i++) {
            command.add(args[i]);
        }

        return (String[])command.toArray(new String[command.size()]);
    }

    public void run() {
        String[] command = getCommandForm();
        System.out.println("Launching " + this.toString());
        try {
            Process process = Runtime.getRuntime().exec(command);
            StreamPipe err = new StreamPipe(process.getErrorStream(), System.err);
            err.listen();

            StreamPipe in = new StreamPipe(process.getInputStream(), System.out);
            in.listen();

            if (waitForProcess) {
                process.waitFor();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        String[] cmd=  getCommandForm();
        String str = "";
        String delim = "";
        for (int i=0; i<cmd.length; i++) {
            if (cmd[i].indexOf(' ') != -1) {
                delim = "\"";
            } else {
                delim = "";
            }
            str += delim + cmd[i] + delim + " ";
        }

        return str.trim();
    }

    private static class StreamPipe {
        private InputStream in;
        private OutputStream out;

        public StreamPipe(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void listen() {
            Runnable rn = new Runnable() {
                public void run() {
                    try {
                        BufferedInputStream bin = new BufferedInputStream(in);
                        byte[] data = new byte[1024];
                        int nb = 0;
                        while ((nb = bin.read(data)) != -1) {
                            out.write(data, 0, nb);
                        }
                    } catch (IOException e) {
                        e.printStackTrace(new PrintWriter(out));
                    }
                }
            };

            Thread th = new Thread(rn);
            th.setDaemon(true);
            th.start();
        }
    }

    public static void main(String[] args) {
        VMLauncher lch = new VMLauncher();
        lch.addOverridenOption("file.encoding", "UTF-8");
        lch.setMainClass("com.application.areca.launcher.gui.Launcher");
        lch.setArgs(args);
        lch.run();
    }
}
