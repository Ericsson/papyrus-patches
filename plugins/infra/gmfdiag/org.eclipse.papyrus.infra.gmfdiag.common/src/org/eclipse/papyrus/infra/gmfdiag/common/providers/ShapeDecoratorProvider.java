/*****************************************************************************
 * Copyright (c) 2009-2010 CEA LIST.
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
 *  Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;

/**
 * Provides the decorator for the shape, based on the shape service
 */
public class ShapeDecoratorProvider extends AbstractProvider implements IDecoratorProvider {

	/** The key used for the mood decoration */
	public static final String SHAPE_DECORATOR = "ShapeDecorator"; //$NON-NLS-1$

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createDecorators(IDecoratorTarget decoratorTarget) {
		View node = ShapeDecorator.getDecoratorTargetNode(decoratorTarget);
		if (node != null) {
			decoratorTarget.installDecorator(SHAPE_DECORATOR, new ShapeDecorator(decoratorTarget));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean provides(IOperation operation) {
		Assert.isNotNull(operation);

		if (!(operation instanceof CreateDecoratorsOperation)) {
			return false;
		}

		IDecoratorTarget decoratorTarget = ((CreateDecoratorsOperation) operation).getDecoratorTarget();

		View notationElement = NotationHelper.findView(decoratorTarget);
		if (notationElement == null) {
			return false;
		}

		try {
			ServicesRegistry papyrusRegistry = ServiceUtilsForEObject.getInstance().getServiceRegistry(notationElement);
			if (papyrusRegistry == null) {
				return false;
			}
		} catch (Exception ex) {
			return false; // Not a Papyrus model
		}

		return ShapeDecorator.getDecoratorTargetNode(decoratorTarget) != null;
	}

}
