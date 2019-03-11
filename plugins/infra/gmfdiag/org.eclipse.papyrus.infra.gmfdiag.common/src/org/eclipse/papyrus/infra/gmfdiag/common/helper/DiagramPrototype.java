/*****************************************************************************
 * Copyright (c) 2013 CEA LIST.
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
 *  Laurent Wouters laurent.wouters@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.commands.Activator;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.gmfdiag.common.AbstractPapyrusGmfCreateDiagramCommandHandler;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;
import org.eclipse.papyrus.infra.gmfdiag.representation.PapyrusDiagram;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;

/**
 * Represents a prototype of GMF diagram for the viewpoints infrastructure
 *
 * @author Laurent Wouters
 */
public class DiagramPrototype extends ViewPrototype {
	protected final String category;
	protected final AbstractPapyrusGmfCreateDiagramCommandHandler command;

	/**
	 * @since 3.0
	 */
	public DiagramPrototype(PapyrusDiagram configuration, String category, AbstractPapyrusGmfCreateDiagramCommandHandler command) {
		super(configuration);
		this.category = category;
		this.command = command;
	}

	@Override
	public boolean instantiateOn(EObject owner) {
		return instantiateOn(owner, null);
	}

	@Override
	public boolean instantiateOn(EObject owner, String name) {
		ServicesRegistry registry;
		try {
			registry = ServiceUtilsForEObject.getInstance().getServiceRegistry(owner);
		} catch (ServiceException ex) {
			Activator.log.error(ex);
			return false;
		}
		ModelSet modelSet;
		try {
			modelSet = registry.getService(ModelSet.class);
		} catch (ServiceException ex) {
			Activator.log.error(ex);
			return false;
		}
		Object result = command.createDiagram(modelSet, owner, owner, this, name);
		return (result != null);
	}

	@Override
	public boolean isOwnerReassignable() {
		// Users can always move diagrams that are part of their viewpoint
		return true;
	}

	@Override
	public Command getCommandChangeOwner(EObject view, final EObject target) {
		final Diagram diagram = (Diagram) view;
		final EObject previous = DiagramUtils.getOwner(diagram);
		return new AbstractCommand("Change diagram owner") {
			@Override
			public void execute() {
				DiagramUtils.setOwner(diagram, target);
			}

			@Override
			public void undo() {
				DiagramUtils.setOwner(diagram, previous);
			}

			@Override
			public void redo() {
				DiagramUtils.setOwner(diagram, target);
			}

			@Override
			protected boolean prepare() {
				return true;
			}
		};
	}

	@Override
	public Command getCommandChangeRoot(EObject view, final EObject target) {
		final Diagram diagram = (Diagram) view;
		final EObject previous = diagram.getElement();
		return new AbstractCommand("Change diagram root element") {
			@Override
			public void execute() {
				diagram.setElement(target);
			}

			@Override
			public void undo() {
				diagram.setElement(previous);
			}

			@Override
			public void redo() {
				diagram.setElement(target);
			}

			@Override
			protected boolean prepare() {
				return true;
			}
		};
	}

	@Override
	public EObject getOwnerOf(EObject view) {
		return DiagramUtils.getOwner((Diagram) view);
	}

	@Override
	public EObject getRootOf(EObject view) {
		return ((Diagram) view).getElement();
	}
	
	/**
	 * @since 3.0
	 */
	@Override
	public PapyrusDiagram getRepresentationKind() {
		return (PapyrusDiagram) representationKind;
	}

}
