/*****************************************************************************
 * Copyright (c) 2012 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.ui.editor.IMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;


public class GMFMultiDiagramAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IMultiDiagramEditor) {
			IEditorPart nestedEditor = ((IMultiDiagramEditor) adaptableObject).getActiveEditor();

			// The nestedEditor may or may not handle these cases.
			if (nestedEditor instanceof IAdaptable) {
				if (adapterType == IDiagramGraphicalViewer.class) {
					return nestedEditor.getAdapter(IDiagramGraphicalViewer.class);
				}

				if (adapterType == Diagram.class) {
					return nestedEditor.getAdapter(Diagram.class);
				}

				if (adapterType == DiagramEditPart.class) {
					return nestedEditor.getAdapter(DiagramEditPart.class);
				}

				if (adapterType == IDiagramWorkbenchPart.class) {
					return nestedEditor.getAdapter(IDiagramWorkbenchPart.class);
				}
			}
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { IMultiDiagramEditor.class };
	}

}
