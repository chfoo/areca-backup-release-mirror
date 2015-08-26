package com.application.areca.launcher.gui.composites;

import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.application.areca.impl.FileSystemTarget;
import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.ProcessorEditionWindow;
import com.application.areca.launcher.gui.ProcessorRepository;
import com.application.areca.launcher.gui.TargetEditionWindow;
import com.application.areca.launcher.gui.common.AbstractWindow;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.application.areca.processor.Processor;
import com.application.areca.processor.ProcessorList;

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
public class ProcessorsTable {

    private static final ResourceManager RM = ResourceManager.instance();

    protected Table tblProc;  
    protected Button btnAddProc;
    protected Button btnRemoveProc;
    protected Button btnModifyProc;
    protected Button chkForwardErrors;
    
    protected Button btnUp;
    protected Button btnDown;

    protected boolean preprocess = true;
    protected final TargetEditionWindow main;

    public ProcessorsTable(Composite parent, TargetEditionWindow tge, boolean preprocess) {

        parent.setLayout(initLayout(5));
        this.main = tge;
        this.preprocess = preprocess;

        TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE);
        tblProc = viewer.getTable();
        GridData dt = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2);
        dt.heightHint = AbstractWindow.computeHeight(50);
        tblProc.setLayoutData(dt);
        
        TableColumn col1 = new TableColumn(tblProc, SWT.NONE);
        col1.setText(RM.getLabel("targetedition.proctable.type.label"));
        col1.setWidth(AbstractWindow.computeWidth(200));
        col1.setMoveable(true);

        TableColumn col2 = new TableColumn(tblProc, SWT.NONE);
        col2.setText(RM.getLabel("targetedition.proctable.parameters.label"));
        col2.setWidth(AbstractWindow.computeWidth(200));
        col2.setMoveable(true);

        tblProc.setHeaderVisible(true);
        tblProc.setLinesVisible(AbstractWindow.getTableLinesVisible());

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                editCurrentProcessor();
            }
        });
        
        
        btnUp = new Button(parent, SWT.PUSH);
        btnUp.setText(RM.getLabel("common.up.label"));
        btnUp.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
        btnUp.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                up();
            }
        });
        
        
        btnDown = new Button(parent, SWT.PUSH);
        btnDown.setText(RM.getLabel("common.down.label"));
        btnDown.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        btnDown.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                down();
            }
        });

        btnAddProc = new Button(parent, SWT.PUSH);
        btnAddProc.setText(RM.getLabel("targetedition.addprocaction.label"));
        btnAddProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                Processor newproc = showProcEditionFrame(null);
                if (newproc != null) {
                    addProcessor(newproc);
                    main.publicRegisterUpdate();                
                }
            }
        });

        btnModifyProc = new Button(parent, SWT.PUSH);
        btnModifyProc.setText(RM.getLabel("targetedition.editprocaction.label"));
        btnModifyProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
                editCurrentProcessor();
            }
        });

        btnRemoveProc = new Button(parent, SWT.PUSH);
        btnRemoveProc.setText(RM.getLabel("targetedition.removeprocaction.label"));
        btnRemoveProc.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event event) {
            	removeCurrentProcessor();
            }
        });

        tblProc.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                updateProcListState();
            }
        });
        
        tblProc.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				if (evt.character == SWT.DEL) {
					removeCurrentProcessor();
				}
			}

			public void keyReleased(KeyEvent evt) {
			}
        });
        
        if (preprocess) {
	        chkForwardErrors = new Button(parent, SWT.CHECK);
	        chkForwardErrors.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
	        chkForwardErrors.setText(RM.getLabel("targetedition.fwderrors.label"));
	        chkForwardErrors.setSelection(true);
        }
    }
    
    private void up() {
    	shift(-1);
    }

    private void down() {
    	shift(1);
    }
    
    private void shift(int shift) {
        int index = tblProc.getSelectionIndex();
    	int tgIndex = index + shift;
    	
    	if (index != -1 && tgIndex >= 0 && tgIndex < tblProc.getItemCount()) {
    		TableItem item = tblProc.getItem(index);
        	TableItem target = tblProc.getItem(tgIndex);
        	Processor srcProc = (Processor)item.getData();
        	Processor tgProc = (Processor)target.getData();
        	
            target.dispose();
            item.dispose();
        	
            TableItem item2 = new TableItem(tblProc, SWT.NONE, tgIndex + (index < tgIndex ? -1 : 0));
            TableItem target2 = new TableItem(tblProc, SWT.NONE, index);
            configure(item2, (Processor)srcProc);
        	configure(target2, (Processor)tgProc);
            
            tblProc.setSelection(tgIndex);
        }
    }
    
    private GridLayout initLayout(int nbCols) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.numColumns = nbCols;
        layout.marginHeight = 0;
        return layout;
    }

    public void updateProcListState() {
        int index =  this.tblProc.getSelectionIndex();
        this.btnRemoveProc.setEnabled(index != -1);
        this.btnModifyProc.setEnabled(index != -1);    
        this.btnDown.setEnabled(index != -1);
        this.btnUp.setEnabled(index != -1);    
    }
    
    private void removeCurrentProcessor() {
    	int idx = tblProc.getSelectionIndex();
        if (idx != -1) {
            int result = Application.getInstance().showConfirmDialog(
                    RM.getLabel("targetedition.removeprocaction.confirm.message"),
                    RM.getLabel("targetedition.confirmremoveproc.title"));

            if (result == SWT.YES) {
                tblProc.remove(idx);
                tblProc.setSelection(Math.max(0, Math.min(tblProc.getItemCount() - 1, idx)));
                main.publicRegisterUpdate();                 
            }
        }
    }


    private void editCurrentProcessor() {
        if (tblProc.getSelectionIndex() != -1) {
            TableItem item = tblProc.getItem(tblProc.getSelectionIndex());
            Processor proc = (Processor)item.getData();
            showProcEditionFrame(proc);
            updateProcessor(item, proc);
            main.publicRegisterUpdate();       
        }
    }

    private Processor showProcEditionFrame(Processor proc) {
        ProcessorEditionWindow frm = new ProcessorEditionWindow(proc, (FileSystemTarget)main.getTarget(), preprocess);
        main.showDialog(frm);
        Processor prc = frm.getCurrentProcessor();
        return prc;
    }


    private void addProcessor(Processor proc) {
        TableItem item = new TableItem(tblProc, SWT.NONE);
        updateProcessor(item, proc);
    }

    private void updateProcessor(TableItem item, Processor proc) {
        item.setText(0, ProcessorRepository.getName(proc));
        item.setText(1, proc.getParametersSummary());
        item.setData(proc);
    }

    public Button getBtnAddProc() {
        return btnAddProc;
    }

    public Button getBtnModifyProc() {
        return btnModifyProc;
    }

    public Button getBtnRemoveProc() {
        return btnRemoveProc;
    }

    public Table getTblProc() {
        return tblProc;
    }

    public void addProcessors(ProcessorList list) {
        for (int i=0; i<tblProc.getItemCount(); i++) {
            list.addProcessor((Processor)tblProc.getItem(i).getData());
        }
    }

    public void setProcessors(ProcessorList list) {
        Iterator processors = list.iterator();
        int index = tblProc.getSelectionIndex();
        while (processors.hasNext()) {
            TableItem item = new TableItem(tblProc, SWT.NONE);
            configure(item, (Processor)processors.next());
        } 
        if (index != -1) {
            tblProc.setSelection(index);
        }  
        
        if (preprocess) {
        	chkForwardErrors.setSelection(list.isForwardErrors());
        }
    }
    
    private void configure(TableItem item, Processor proc) {
        item.setText(0, ProcessorRepository.getName(proc));
        item.setText(1, proc.getParametersSummary());
        item.setData(proc);
    }
    
    public boolean isForwardErrors() {
    	return chkForwardErrors.getSelection();
    }
}
