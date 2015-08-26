package com.application.areca.metadata.transaction;

import org.eclipse.swt.SWT;

import com.application.areca.launcher.gui.Application;
import com.application.areca.launcher.gui.common.SecuredRunner;
import com.application.areca.launcher.gui.resources.ResourceManager;
import com.myJava.file.FileSystemManager;

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
public class GUITransactionHandler implements TransactionHandler {
    private static final ResourceManager RM = ResourceManager.instance();
    
	public boolean shallSearchForPendingTransactions() {
		return true;
	}
	
	public boolean shallHandleTransactionPoint(TransactionPoint transactionPoint) {
		MySecuredRunnable rn = new MySecuredRunnable(transactionPoint);
		SecuredRunner.execute(rn);

		return (rn.getAnswer() == SWT.YES);
	}
	
	private static class MySecuredRunnable implements Runnable {
		private TransactionPoint transactionPoint;
		private int answer;
		
		public MySecuredRunnable(TransactionPoint transactionPoint) {
			this.transactionPoint = transactionPoint;
		}

		public int getAnswer() {
			return answer;
		}

		public void run() {
			answer = Application.getInstance().showConfirmDialog(RM.getLabel("common.transaction.message", new Object[] {FileSystemManager.getDisplayPath(transactionPoint.getPath())}), RM.getLabel("common.transaction.title"));
		}
	}
}
