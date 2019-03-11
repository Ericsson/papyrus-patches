/*****************************************************************************
 * Copyright (c) 2015 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gef.Request;
import org.eclipse.papyrus.infra.services.edit.utils.ElementTypeUtils;
import org.eclipse.papyrus.infra.services.edit.utils.RequestParameterConstants;

/**
 * Extension of ElementTypeUtils for GEF-specific requests (Which don't implement IEditCommandRequest)
 *
 * @author Camille Letavernier
 */
public class RequestUtils extends ElementTypeUtils {

	/**
	 * Constructor.
	 *
	 */
	protected RequestUtils() {
		super();
	}


	/**
	 * return a boolean about the usage of a GUI for edition of an Element
	 *
	 * @param request
	 *            an edition request
	 * @return true if the request do not contain information about usage of GUI
	 */
	public static boolean useGUI(Request request) {
		Map<String, Object> parameters = getParameters(request);
		Object value = parameters.get(RequestParameterConstants.USE_GUI);

		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			Boolean booleanObject = Boolean.valueOf((String) value);
			return booleanObject.booleanValue();
		}

		return true; // Default
	}

	/**
	 * Configure a request to specify whether the GUI should be used or not.
	 *
	 * If set to false, dialogs shouldn't be opened during the execution of the associated command(s)
	 *
	 * @param request
	 * @param useGUI
	 */
	public static void setUseGUI(Request request, boolean useGUI) {
		Map<String, Object> parameters = getParameters(request);
		parameters.put(RequestParameterConstants.USE_GUI, useGUI);
	}

	/**
	 * Returns the Parameters Map associated to this request. An empty map
	 * is created and attached to the request if it doesn't exist
	 *
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getParameters(Request request) {
		Map<String, Object> parameters = request.getExtendedData();
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
			request.setExtendedData(parameters);
		}

		return parameters;
	}

}
