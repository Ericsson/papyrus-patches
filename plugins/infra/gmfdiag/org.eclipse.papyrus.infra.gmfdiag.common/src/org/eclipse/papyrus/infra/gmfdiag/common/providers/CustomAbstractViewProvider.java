/*****************************************************************************
 * Copyright (c) 2011-2012 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateEdgeViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateNodeViewOperation;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewForKindOperation;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.service.ProviderServiceUtil;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;

/**
 * This abstract view provider retrieve the view type from the graphical type
 * registry and use it in edge and node view creation.
 */
public abstract class CustomAbstractViewProvider extends AbstractViewProvider {

	/** Local graphical type registry */
	protected IGraphicalTypeRegistry registry;

	/** The provides only provides for this diagram type */
	protected String diagramType;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Edge createEdge(IAdaptable semanticAdapter, View containerView, String semanticHint, int index, boolean persisted, PreferencesHint preferencesHint) {
		String graphicalType = getEdgeGraphicalType(semanticAdapter, containerView, semanticHint);
		return super.createEdge(semanticAdapter, containerView, graphicalType, index, persisted, preferencesHint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node createNode(IAdaptable semanticAdapter, View containerView, String semanticHint, int index, boolean persisted, PreferencesHint preferencesHint) {
		String graphicalType = getNodeGraphicalType(semanticAdapter, containerView, semanticHint);
		return super.createNode(semanticAdapter, containerView, graphicalType, index, persisted, preferencesHint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean provides(CreateViewForKindOperation op) {
		if (!ProviderServiceUtil.isEnabled(this, op.getContainerView())) {
			return false;
		}
		if (!isRelevantDiagram(op.getContainerView().getDiagram())) {
			return false;
		}
		// if(op.getViewKind() == Node.class) {
		// String graphicalType = getNodeGraphicalType(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint());
		// return getNodeViewClass(op.getSemanticAdapter(), op.getContainerView(), graphicalType) != null;
		// }
		//
		// if(op.getViewKind() == Edge.class) {
		// String graphicalType = getEdgeGraphicalType(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint());
		// return getEdgeViewClass(op.getSemanticAdapter(), op.getContainerView(), graphicalType) != null;
		// }
		throw new UnsupportedOperationException("Should never be called by the " + diagramType + " diagram.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean provides(CreateEdgeViewOperation operation) {
		if (!ProviderServiceUtil.isEnabled(this, operation.getContainerView())) {
			return false;
		}
		if (!isRelevantDiagram(operation.getContainerView().getDiagram())) {
			return false;
		}
		String graphicalType = getEdgeGraphicalType(operation.getSemanticAdapter(), operation.getContainerView(), operation.getSemanticHint());
		return (getEdgeViewClass(operation.getSemanticAdapter(), operation.getContainerView(), graphicalType) != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean provides(CreateNodeViewOperation operation) {
		if (!ProviderServiceUtil.isEnabled(this, operation.getContainerView())) {
			return false;
		}
		if (!isRelevantDiagram(operation.getContainerView().getDiagram())) {
			return false;
		}
		String graphicalType = getNodeGraphicalType(operation.getSemanticAdapter(), operation.getContainerView(), operation.getSemanticHint());
		return (getNodeViewClass(operation.getSemanticAdapter(), operation.getContainerView(), graphicalType) != null);
	}

	protected String getNodeGraphicalType(IAdaptable semanticAdapter, View containerView, String semanticHint) {
		String graphicalType = null;
		// Some ViewDescriptor constructors initialize unspecified semanticHint with ""
		if ((semanticHint != null) && (!"".equals(semanticHint))) {
			graphicalType = registry.getNodeGraphicalType(semanticHint, containerView.getType());
		} else {
			EObject domainElement = semanticAdapter.getAdapter(EObject.class);
			if (domainElement != null) {
				graphicalType = registry.getNodeGraphicalType(domainElement, containerView.getType());
			}
			IElementType elementType = semanticAdapter.getAdapter(IElementType.class);
			if (elementType != null) {
				graphicalType = registry.getNodeGraphicalType(elementType, containerView.getType());
			}
		}
		return graphicalType;
	}

	protected String getEdgeGraphicalType(IAdaptable semanticAdapter, View containerView, String semanticHint) {
		String graphicalType = null;
		// Some ViewDescriptor constructors initialize unspecified semanticHint with ""
		if ((semanticHint != null) && (!"".equals(semanticHint))) {
			graphicalType = registry.getEdgeGraphicalType(semanticHint);
		} else {
			EObject domainElement = semanticAdapter.getAdapter(EObject.class);
			if (domainElement != null) {
				graphicalType = registry.getEdgeGraphicalType(domainElement);
			}
			IElementType elementType = semanticAdapter.getAdapter(IElementType.class);
			if (elementType != null) {
				graphicalType = registry.getEdgeGraphicalType(elementType);
			}
		}
		return graphicalType;
	}

	/**
	 * This method is used to know id the diagram is conform to type, it may be a prototype view, or a generatedDiagram
	 *
	 * @param diagram
	 * @return
	 */
	protected boolean isRelevantDiagram(Diagram diagram) {
		ViewPrototype viewPrototype = org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils.getPrototype(diagram);
		if (viewPrototype != null) {
			if ((diagramType != null) && diagramType.equals(viewPrototype.getLabel())) {
				return true;
			}
			return false;

		}
		if ((diagramType != null) && (diagramType.equals(diagram.getType()))) {
			return true;
		}
		return false;
	}
}
