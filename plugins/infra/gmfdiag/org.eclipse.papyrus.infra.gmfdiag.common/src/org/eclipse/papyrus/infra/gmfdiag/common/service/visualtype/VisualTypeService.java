/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.visualtype;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.ExecutionStrategy;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener;
import org.eclipse.gmf.runtime.common.core.service.ProviderPriority;
import org.eclipse.gmf.runtime.common.core.service.Service;
import org.eclipse.gmf.runtime.common.ui.services.util.ActivityFilterProviderDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * A GMF service for visual element-type information specific to a diagram.
 */
public class VisualTypeService extends Service implements IVisualTypeProvider {
	private static final VisualTypeService INSTANCE;

	static {
		INSTANCE = new VisualTypeService();
		INSTANCE.configureProviders(Activator.ID, "visualTypeProviders"); //$NON-NLS-1$
	}

	private VisualTypeService() {
		super(true, true);
	}

	public static VisualTypeService getInstance() {
		return INSTANCE;
	}

	@Override
	protected Object getCachingKey(IOperation operation) {
		return ((IVisualTypeOperation) operation).getDiagramType();
	}

	@Override
	public String getPriority(IConfigurationElement element) {
		String result = ProviderPriority.LOWEST.getName();

		IConfigurationElement[] priorities = element.getChildren("Priority");
		if (priorities.length > 0) {
			String priority = priorities[0].getAttribute("name");
			if (!Strings.isNullOrEmpty(priority)) {
				result = priority;
			}
		}

		return result;
	}

	@Override
	protected ProviderDescriptor newProviderDescriptor(IConfigurationElement element) {
		class VTPDesc extends ActivityFilterProviderDescriptor {
			private final String diagramType;

			public VTPDesc(IConfigurationElement element) {
				super(element);

				diagramType = element.getAttribute("diagramType");
			}

			@Override
			public boolean provides(IOperation operation) {
				return (operation instanceof IVisualTypeOperation)
						&& Objects.equal(((IVisualTypeOperation) operation).getDiagramType(), diagramType)
						&& super.provides(operation);
			}

			@Override
			public IProvider getProvider() {
				IProvider result = provider;

				if (result == null) {
					// We are initializing the provider
					result = super.getProvider();
					if (result == null) {
						Activator.log.warn(String.format("Provider initialization failed for <%s> in %s.", getElement().getName(), getElement().getContributor().getName()));
						provider = new NullProvider();
						result = provider;
					} else {
						((AbstractVisualTypeProvider) result).setConfiguration(getElement());
					}
				}

				return result;
			}
		}

		return new VTPDesc(element);
	}

	//
	// Provider API
	//

	@Override
	public IElementType getElementType(Diagram diagram, String viewType) {
		IOperation operation = new GetElementTypeOperation(diagram, viewType);
		List<?> result = ExecutionStrategy.FIRST.execute(this, operation);

		return result.isEmpty() ? null : (IElementType) result.get(0);
	}

	@Override
	public String getNodeType(View parentView, EObject element) {
		IOperation operation = new GetNodeTypeOperation(parentView, element);
		List<?> result = ExecutionStrategy.FIRST.execute(this, operation);

		return result.isEmpty() ? null : (String) result.get(0);
	}

	@Override
	public String getLinkType(Diagram diagram, EObject element) {
		IOperation operation = new GetLinkTypeOperation(diagram, element);
		List<?> result = ExecutionStrategy.FIRST.execute(this, operation);

		return result.isEmpty() ? null : (String) result.get(0);
	}

	//
	// Nested types
	//

	private static final class NullProvider implements IVisualTypeProvider {
		@Override
		public boolean provides(IOperation operation) {
			return false;
		}

		@Override
		public void addProviderChangeListener(IProviderChangeListener listener) {
			// No point
		}

		@Override
		public void removeProviderChangeListener(IProviderChangeListener listener) {
			// Never added any
		}

		@Override
		public IElementType getElementType(Diagram diagram, String viewType) {
			return null;
		}

		@Override
		public String getNodeType(View parentView, EObject element) {
			return null;
		}

		@Override
		public String getLinkType(Diagram diagram, EObject element) {
			return null;
		}

	}
}
