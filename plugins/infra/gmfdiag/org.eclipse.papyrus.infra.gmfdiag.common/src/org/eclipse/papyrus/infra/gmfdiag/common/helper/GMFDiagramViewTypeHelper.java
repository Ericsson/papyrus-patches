/*****************************************************************************
 * Copyright (c) 2013, 2017 CEA LIST and others.
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
 *  Christian W. Damus - bug 527580
 *  Ansgar Radermacher - bug 539754
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.gmfdiag.common.AbstractPapyrusGmfCreateDiagramCommandHandler;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;
import org.eclipse.papyrus.infra.gmfdiag.representation.PapyrusDiagram;
import org.eclipse.papyrus.infra.tools.util.ClassLoaderHelper;
import org.eclipse.papyrus.infra.viewpoints.policy.AbstractViewTypeHelper;
import org.eclipse.papyrus.infra.viewpoints.policy.PolicyChecker;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;

/**
 * Represents the dynamic contribution of a policy to menus
 *
 * @author Laurent Wouters
 */
public class GMFDiagramViewTypeHelper extends AbstractViewTypeHelper<PapyrusDiagram> {

	/**
	 * Initializes me.
	 */
	public GMFDiagramViewTypeHelper() {
		super(PapyrusDiagram.class);
	}

	@Override
	public boolean isSupported(EObject view) {
		return (view instanceof Diagram);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.100
	 */
	@Override
	protected ViewPrototype doGetPrototypeFor(PapyrusDiagram diagramKind) {
		String commandClassName = diagramKind.getCreationCommandClass();
		if (commandClassName != null) {
			Class<?> creationCommandClass = ClassLoaderHelper.loadClass(commandClassName);

			if (creationCommandClass != null) {
				AbstractPapyrusGmfCreateDiagramCommandHandler command;
				try {
					command = (AbstractPapyrusGmfCreateDiagramCommandHandler) creationCommandClass.newInstance();
				} catch (Exception e) {
					Activator.log.error(e);
					return null;
				}

				String language = diagramKind.getLanguage().getId();
				return new DiagramPrototype(diagramKind, language, command);
			} else {
				// ClassLoaderHelper should have already logged an exception, but without stating diagram kind
				Activator.log.error(new ClassNotFoundException(
						String.format("Can not load creation command class %s for diagramKind %s.", //$NON-NLS-1$
								commandClassName, diagramKind.getName())));
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.2
	 */
	@Override
	protected ViewPrototype doGetPrototypeOf(EObject view) {
		Diagram diagram = (Diagram) view;

		PolicyChecker checker = getPolicyChecker(diagram);
		return DiagramUtils.getPrototype(diagram, checker);
	}
}
