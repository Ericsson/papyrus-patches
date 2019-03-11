/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
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

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;

/**
 * this class is used to add senders of request in the request
 *
 * @since 4.0
 *
 */
public class SenderRequestUtils {
	public static String REQUEST_SENDER = "RequestSender";

	/**
	 * from a request add the editpart as sender of the request
	 *
	 * @param request
	 *            a given request, never null
	 * @param editPart
	 *            add it as sender of the request; never null
	 */
	@SuppressWarnings("unchecked")
	public static void addRequestSender(Request request, EditPart editPart) {
		Map<String, Object> extendedData = request.getExtendedData();
		ArrayList<EditPart> senderList = null;
		if (extendedData.get(REQUEST_SENDER) != null) {
			senderList = ((ArrayList<EditPart>) extendedData.get(REQUEST_SENDER));

		} else {
			senderList = new ArrayList<>();
		}
		senderList.add(editPart);
		extendedData.put(REQUEST_SENDER, senderList);
		request.setExtendedData(extendedData);
	}

	/**
	 * test if the given editpart is a sender of the request
	 *
	 * @param request
	 *            a given request, never null
	 * @param editPart
	 *            a given editpart , never null
	 * @return true if the given editpart is a sender
	 */
	@SuppressWarnings("unchecked")
	public static boolean isASender(Request request, EditPart editPart) {
		Map<String, Object> extendedData = request.getExtendedData();
		ArrayList<EditPart> senderList = null;
		if (extendedData.get(REQUEST_SENDER) == null) {
			return false;
		}
		senderList = ((ArrayList<EditPart>) extendedData.get(REQUEST_SENDER));

		return senderList.contains(editPart);
	}

	/**
	 * get all senders from a given request
	 *
	 * @param request
	 *            a a given request, never null
	 * @return always a list maybe empty
	 */
	public static ArrayList<EditPart> getSenders(Request request) {
		Map<String, Object> extendedData = request.getExtendedData();
		ArrayList<EditPart> senderList = null;
		if (extendedData.get(REQUEST_SENDER) != null) {
			senderList = ((ArrayList<EditPart>) extendedData.get(REQUEST_SENDER));

		} else {
			senderList = new ArrayList<>();
		}
		return senderList;
	}

	/**
	 * add a list of editpart as sender of the request
	 *
	 * @param request
	 *            a given request, never null
	 * @param senderList
	 *            a list of the editpart that have sent the request
	 */
	@SuppressWarnings("unchecked")
	public static void addRequestSenders(Request request, ArrayList<EditPart> senderList) {
		Map<String, Object> extendedData = request.getExtendedData();
		extendedData.put(REQUEST_SENDER, senderList);
		request.setExtendedData(extendedData);
	}

}
