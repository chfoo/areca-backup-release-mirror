package com.application.areca.metadata.transaction;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.myJava.util.CalendarUtils;
import com.myJava.util.log.Logger;
import com.myJava.util.xml.AdapterException;

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
public class ConditionalTransactionHandler implements TransactionHandler {
	private int threshold = -1;

	public ConditionalTransactionHandler(int threshold) {
		this.threshold = threshold;
	}

	public boolean shallSearchForPendingTransactions() {
		return true;
	}

	public boolean shallHandleTransactionPoint(TransactionPoint transactionPoint) {
		TransactionPointHeader header = null;
		try {
			header = transactionPoint.readHeader();
		} catch (AdapterException e) {
			Logger.defaultLogger().warn("Error reading transaction point attributes. It will be ignored.", e);
			return false;
		}
		
		if (header != null) {
			Calendar tpDate = CalendarUtils.removeTime(header.getDate());
			Calendar now = CalendarUtils.removeTime(new GregorianCalendar());
			
			tpDate.add(Calendar.DATE, threshold);
			return tpDate.equals(now) || tpDate.after(now);
		} else {
			return false;
		}
	}
}
