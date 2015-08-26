package com.myJava.file.driver;

import com.myJava.util.log.Logger;
import com.myJava.util.taskmonitor.TaskMonitor;

/**
 * <BR>
 * CAUTION: This class is *NOT* thread safe
 * 
 * @author Olivier PETRUCCI <BR>
 *         
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
public class ThrottleHandler {
	// Max batch size : 1 MB
	private static final int MAX_BATCH_SIZE_BYTES = 1024 * 1024;
	private static final int MAX_WAIT_TIME_MS = 5 * 1000;
	
	private long timer = 0;
	private long written = 0;
	private int maxBatchSize;
	private TaskMonitor monitor;
	
	/**
	 * Expected throughput - in bytes per millisecond
	 */
	private double throughput;

	public ThrottleHandler(double throughputKb) {
		this.throughput = throughputKb * 1.024;
		this.timer = System.currentTimeMillis();

		this.maxBatchSize = (int)Math.min(MAX_BATCH_SIZE_BYTES, MAX_WAIT_TIME_MS * throughput);
	}
	
	public int getMaxBatchSizeBytes() {
		return maxBatchSize;
	}
	
	public void initializeTimer(TaskMonitor monitor) {
		Logger.defaultLogger().info("Resetting throttling: " + (throughput / 1.024) + " kb/second.");
		timer = System.currentTimeMillis();
		written = 0;
		this.monitor = monitor;
	}
	
	public void checkTimer(int nrOfBytes) {
		if (monitor != null && (! monitor.isCancelRequested())) {
			written += nrOfBytes;
			
			long sleepTimeMs = (long)(timer + (written/throughput) - System.currentTimeMillis());
	        if (sleepTimeMs > 0) {
	            try {
	                Thread.sleep(sleepTimeMs);
	            } catch (InterruptedException ignored) {
	            }
	        }
		}
	}
}
