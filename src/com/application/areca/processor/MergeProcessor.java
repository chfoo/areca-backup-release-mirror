package com.application.areca.processor;

import com.application.areca.AbstractTarget;
import com.application.areca.ActionProxy;
import com.application.areca.ApplicationException;
import com.application.areca.CheckParameters;
import com.application.areca.MergeParameters;
import com.application.areca.context.ProcessContext;
import com.myJava.object.Duplicable;
import com.myJava.object.EqualsHelper;
import com.myJava.object.HashHelper;

/**
 * Merge archives
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
public class MergeProcessor extends AbstractProcessor {

	private int fromDelay = 0; // 0 = -infinity
	private int toDelay = 0; // 0 = now
	private MergeParameters params;
	private CheckParameters checkParams;

	/**
	 * @param target
	 */
	public MergeProcessor() {
		super();
	}

	public int getToDelay() {
		return toDelay;
	}

	public boolean requireStatistics() {
		return false;
	}

	public String getKey() {
		return "Merge Archives";
	}

	public void setToDelay(int toDelay) {
		this.toDelay = toDelay;
	}

	public int getFromDelay() {
		return fromDelay;
	}

	public MergeParameters getParams() {
		return params;
	}

	public void setParams(MergeParameters params) {
		this.params = params;
	}

	public CheckParameters getCheckParams() {
		return checkParams;
	}

	public void setCheckParams(CheckParameters checkParams) {
		this.checkParams = checkParams;
	}

	public void setFromDelay(int delay) {
		this.fromDelay = delay;
	}

	public void runImpl(ProcessContext context) throws ApplicationException {
		AbstractTarget target = context.getReport().getTarget();
		ProcessContext subContext = context.createSubContext();
		ActionProxy.processMergeOnTargetImpl(
				target, 
				fromDelay, 
				toDelay, 
				null, 
				params,
				checkParams,
				false, 
				subContext
				);
	}

	public boolean requiresFilteredEntriesListing() {
		return false;
	}

	public String getParametersSummary() {
		String ret = "";
		if (fromDelay > 0) {
			ret += "[-" + fromDelay;
		} else {
			ret += "]-infinity";
		}
		ret +=  "; " + (toDelay != 0 ? "-":"") + toDelay + "]";

		return ret;
	}

	public Duplicable duplicate() {
		MergeProcessor pro = new MergeProcessor();
		copyAttributes(pro);
		pro.fromDelay = this.fromDelay;
		pro.toDelay = this.toDelay;
		pro.params = (MergeParameters)this.params.duplicate();
		pro.checkParams = (CheckParameters)this.checkParams.duplicate();
		return pro;
	}

	public void validate() throws ProcessorValidationException {
		if (fromDelay < 0) {
			throw new ProcessorValidationException("The merge delay must be above or equal to 0");
		}

		if (toDelay < 0) {
			throw new ProcessorValidationException("The merge delay must be above or equal to 0");
		}

		if (fromDelay != 0 && toDelay > fromDelay) {
			throw new ProcessorValidationException("The 'From' delay must be greater than the 'To' delay.");
		}
	}

	public boolean equals(Object obj) {
		if (obj == null || (! (obj instanceof MergeProcessor)) ) {
			return false;
		} else {
			MergeProcessor other = (MergeProcessor)obj;
			return 
					super.equals(other)
					&& EqualsHelper.equals(this.fromDelay, other.fromDelay)
					&& EqualsHelper.equals(this.toDelay, other.toDelay)
					&& EqualsHelper.equals(this.params, other.params)
					&& EqualsHelper.equals(this.checkParams, other.checkParams);
		}
	}

	public int hashCode() {
		int h = HashHelper.initHash(this);
		h = HashHelper.hash(h, super.hashCode());
		h = HashHelper.hash(h, this.fromDelay);
		h = HashHelper.hash(h, this.toDelay);
		h = HashHelper.hash(h, this.params);
		h = HashHelper.hash(h, this.checkParams);
		return h;
	}
}
