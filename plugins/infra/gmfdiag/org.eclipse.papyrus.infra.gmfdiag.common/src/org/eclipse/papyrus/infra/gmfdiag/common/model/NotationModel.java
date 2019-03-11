/*****************************************************************************
 * Copyright (c) 2011, 2016 LIFL, CEA LIST, Christian W. Damus, and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   LIFL - Initial API and implementation
 *   Christian W. Damus - bugs 485220, 496299
 *   
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.model;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.infra.core.resource.BadArgumentExcetion;
import org.eclipse.papyrus.infra.core.resource.EMFLogicalModel;
import org.eclipse.papyrus.infra.core.resource.IEMFModel;
import org.eclipse.papyrus.infra.core.resource.IModel;
import org.eclipse.papyrus.infra.core.resource.NotFoundException;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;

/**
 * @author cedric dumoulin
 *
 */
public class NotationModel extends EMFLogicalModel implements IModel {

	/**
	 * File extension used for notation.
	 */
	public static final String NOTATION_FILE_EXTENSION = "notation"; //$NON-NLS-1$

	/**
	 * Model ID.
	 */
	public static final String MODEL_ID = "org.eclipse.papyrus.infra.core.resource.notation.NotationModel"; //$NON-NLS-1$

	/**
	 *
	 * Constructor.
	 *
	 */
	public NotationModel() {

	}

	/**
	 * Get the file extension used for this model.
	 *
	 * @see org.eclipse.papyrus.infra.core.resource.AbstractBaseModel#getModelFileExtension()
	 *
	 * @return
	 */
	@Override
	protected String getModelFileExtension() {
		return NOTATION_FILE_EXTENSION;
	}

	/**
	 * Get the identifier used to register this model.
	 *
	 * @see org.eclipse.papyrus.infra.core.resource.AbstractBaseModel#getIdentifier()
	 *
	 * @return
	 */
	@Override
	public String getIdentifier() {
		return MODEL_ID;
	}

	/**
	 * Add a new initialized {@link Diagram} to the model.
	 *
	 * @param newDiagram
	 *            The diagram to add.
	 */
	public void addDiagram(Diagram newDiagram) {
		getResource().getContents().add(newDiagram);
	}

	// Prevent infinite loop from 2 models delegating to each other.
	private boolean checkingControlState = false;

	/**
	 * Notation resources are controlled if their base element is controlled
	 * In case the notation resource is empty, we should look at the associated resources and see if one of them is controlled.
	 */
	@Override
	public boolean isControlled(Resource resource) {
		if (checkingControlState) {
			return false;
		}

		try {
			checkingControlState = true;

			for (Resource resourceInModelSet : modelSet.getResources()) {
				if (resource.getURI().trimFileExtension().equals(resourceInModelSet.getURI().trimFileExtension()) && !isRelatedResource(resourceInModelSet)) {
					if (!resourceInModelSet.getContents().isEmpty()) {
						EObject eObject = resourceInModelSet.getContents().get(0);
						IModel iModel = modelSet.getModelFor(eObject);
						if (iModel instanceof IEMFModel) {
							if (((IEMFModel) iModel).isControlled(resourceInModelSet)) {
								return true;
							}
						}
					}
				}
			}
		} finally {
			checkingControlState = false;
		}

		return false;
	}

	@Override
	public void handle(Resource resource) {
		super.handle(resource);
	}


	/**
	 * Get a diagram by its name.
	 *
	 * @param diagramName
	 *            Name of the diagram. This is the name set by the user.
	 * @return
	 * @throws NotFoundException
	 * @throws BadArgumentExcetion
	 */
	public Diagram getDiagram(String diagramName) throws NotFoundException, BadArgumentExcetion {

		if (diagramName == null || diagramName.length() == 0) {
			throw new BadArgumentExcetion("Diagram name should not be null and size should be >0."); //$NON-NLS-1$
		}

		for (Resource current : getResources()) {
			for (EObject element : current.getContents()) {
				if (element instanceof Diagram) {
					Diagram diagram = (Diagram) element;

					if (diagramName.equals(diagram.getName())) {
						// Found
						return diagram;

					}
				}
			}
		}
		// not found
		throw new NotFoundException(NLS.bind("No Diagram named '{0}' can be found in Model.", diagramName)); //$NON-NLS-1$
	}

	/**
	 * An object is additionally a root element only if it has a corresponding
	 * viewpoint prototope.
	 */
	@Override
	protected boolean isRootElement(EObject object) {
		return super.isRootElement(object) && ViewPrototype.isViewObject(object);
	}

	@Override
	protected boolean isSupportedRoot(EObject object) {
		return ViewPrototype.isViewObject(object);
	}
}
