/*****************************************************************************
 * Copyright (c) 2009 Atos Origin.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Atos Origin - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence;

import org.eclipse.papyrus.infra.gmfdiag.common.GmfEditorFactory;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.SequenceDiagramEditPart;

/**
 * The editor factory to launch the sequence diagram.
 */
public class SequenceDiagramEditorFactory extends GmfEditorFactory {

	/**
	 * Instantiates a new sequence diagram editor factory.
	 */
	public SequenceDiagramEditorFactory() {
		super(UmlSequenceDiagramForMultiEditor.class, SequenceDiagramEditPart.MODEL_ID);
	}
}
