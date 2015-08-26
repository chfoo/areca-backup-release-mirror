package com.application.areca.launcher.gui.filters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.application.areca.filter.ArchiveFilter;
import com.application.areca.filter.SpecialFileFilter;
import com.application.areca.launcher.gui.FilterEditionWindow;
import com.application.areca.launcher.gui.FilterRepository;
import com.myJava.file.metadata.FileMetaDataAccessor;
import com.myJava.file.metadata.FileMetaDataAccessorHelper;

/**
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
public class SpecFileFilterComposite extends AbstractFilterComposite {
    
    protected Button chkLink;
    protected Button chkPipe;
    protected Button chkSocket;
    protected Button chkCharSpecFile;
    protected Button chkBlockSpecFile;
    
    public SpecFileFilterComposite(Composite composite, ArchiveFilter filter, FilterEditionWindow window) {
        super(composite, FilterRepository.getIndex(SpecFileFilterComposite.class), filter, window);
        
        this.setLayout(new GridLayout(1, false));
        
        chkLink = addCheckBox("filteredition.link", FileMetaDataAccessor.TYPE_LINK);
        chkPipe = addCheckBox("filteredition.pipe", FileMetaDataAccessor.TYPE_PIPE);
        chkSocket = addCheckBox("filteredition.socket", FileMetaDataAccessor.TYPE_SOCKET);
        chkCharSpecFile = addCheckBox("filteredition.charspecfile", FileMetaDataAccessor.TYPE_CHAR_SPEC_FILE);
        chkBlockSpecFile = addCheckBox("filteredition.blockspecfile", FileMetaDataAccessor.TYPE_BLOCK_SPEC_FILE);

        SpecialFileFilter sff = (SpecialFileFilter)currentFilter;
        if (sff != null) {
        	chkBlockSpecFile.setSelection(sff.isBlockSpecFile());
        	chkCharSpecFile.setSelection(sff.isCharSpecFile());
        	chkLink.setSelection(sff.isLink());
        	chkPipe.setSelection(sff.isPipe());
        	chkSocket.setSelection(sff.isSocket());
        }
    }
    
    private Button addCheckBox(String label, short type) {
        Button chk = new Button(this, SWT.CHECK);
        chk.setText(RM.getLabel(label + ".label"));
        chk.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        window.monitorControl(chk);
        
        if (! FileMetaDataAccessorHelper.getFileSystemAccessor().typeSupported(type)) {
        	chk.setEnabled(false);
        	chk.setToolTipText(RM.getLabel("filteredition.not.available.tt"));
        }
        return chk;
    }
    
    public void initFilter(ArchiveFilter filter) {
    	SpecialFileFilter sff = (SpecialFileFilter)filter;
    	sff.setBlockSpecFile(chkBlockSpecFile.getSelection());
    	sff.setCharSpecFile(chkCharSpecFile.getSelection());
    	sff.setLink(chkLink.getSelection());
    	sff.setPipe(chkPipe.getSelection());
    	sff.setSocket(chkSocket.getSelection());
    }
    
    public boolean validateParams() {
        return true;
    }
}
