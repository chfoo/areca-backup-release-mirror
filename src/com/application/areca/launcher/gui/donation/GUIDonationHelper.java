package com.application.areca.launcher.gui.donation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.application.areca.launcher.ArecaUserPreferences;
import com.application.areca.launcher.gui.Application;

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
public class GUIDonationHelper {
	private static final int DONATION_INTERVAL = ArecaUserPreferences.getDonationThreshold();
	private static final int LAUNCH_REQUIREMENT = (int)(0.25 * DONATION_INTERVAL);
	
	public static void handleDonationMessage() {
		GregorianCalendar cal = new GregorianCalendar();
		int dayNumber = (cal.get(Calendar.YEAR)-2000)*365 + cal.get(Calendar.DAY_OF_YEAR);
		
		int lastMsgDayNumber = ArecaUserPreferences.getDonationMsgDay();
		int interval = dayNumber - lastMsgDayNumber;
		
		if (lastMsgDayNumber == 0) {
			ArecaUserPreferences.setDonationMsgDay(dayNumber - (int)(0.75 * DONATION_INTERVAL));
		} else if (
				DONATION_INTERVAL != -1 
				&& interval > DONATION_INTERVAL
				&& ArecaUserPreferences.getLaunchCount() > LAUNCH_REQUIREMENT
		) {
			ArecaUserPreferences.setDonationMsgDay(dayNumber);
			Application.getInstance().showDialog(new DonationWindow(), false);
		}
	}
}
