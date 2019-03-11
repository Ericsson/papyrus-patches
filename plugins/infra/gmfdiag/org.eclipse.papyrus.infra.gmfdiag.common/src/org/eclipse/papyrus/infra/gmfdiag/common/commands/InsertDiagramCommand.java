/*****************************************************************************
 * Copyright (c) 2014 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;


/**
 * Insert a diagram with the specified owner and element
 */
public class InsertDiagramCommand extends RecordingCommand {

	/** FIXME the domain of Recording command should be accessible */
	protected TransactionalEditingDomain transactionalEditingDomain;

	/** diagram to be inserted */
	protected Diagram diagram;

	/** owner of the diagram */
	protected EObject owner;

	/** element of the diagram */
	protected EObject element;


	/**
	 * Insert a diagram with the specified owner and element
	 *
	 * @param transactionalEditingDomain
	 * @param label
	 *            of the command
	 * @param diagram
	 *            to be inserted
	 * @param element
	 *            of the diagram // FIXME
	 * @param owner
	 *            of the diagram
	 */
	public InsertDiagramCommand(TransactionalEditingDomain transactionalEditingDomain, String label, Diagram diagram, EObject owner) {
		this(transactionalEditingDomain, label, diagram, owner, null);
	}

	/**
	 * Insert a diagram with the specified owner and element
	 *
	 * @param transactionalEditingDomain
	 * @param label
	 *            of the command
	 * @param diagram
	 *            to be inserted
	 * @param element
	 *            of the diagram
	 * @param owner
	 *            of the diagram
	 */
	public InsertDiagramCommand(TransactionalEditingDomain transactionalEditingDomain, String label, Diagram diagram, EObject owner, EObject element) {
		super(transactionalEditingDomain, label);
		this.transactionalEditingDomain = transactionalEditingDomain; // FIXME the domain of Recording command should be accessible
		this.diagram = diagram;
		this.element = element;
		this.owner = owner;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.emf.transaction.RecordingCommand#doExecute()
	 */
	@Override
	protected void doExecute() {
		Resource targetResource = NotationUtils.getNotationResourceForDiagram(owner, transactionalEditingDomain);
		DiagramUtils.setOwner(diagram, owner);
		if (element != null) {
			diagram.setElement(element);
		}

		if (targetResource != null) {
			targetResource.getContents().add(diagram);
		}
	}

}
