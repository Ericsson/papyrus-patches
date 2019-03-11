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
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Initial API and implementation
 *  Celine Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 455311 : Refactor Stereotypes Display
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - bug 461489: add supports of AcceptEventAction
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 517679
 *  Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 431940
 *  Alain Le Guennec (Esterel Technologies SAS) - Bug 521575
 *  Asma Smaoui (CEA LIST) - Bug 533382
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.css.dom;

import static org.eclipse.papyrus.uml.diagram.common.stereotype.IStereotypePropertyReferenceEdgeAdvice.FEATURE_TO_SET_ANNOTATION_KEY;
import static org.eclipse.papyrus.uml.diagram.common.stereotype.IStereotypePropertyReferenceEdgeAdvice.STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT;
import static org.eclipse.papyrus.uml.diagram.common.stereotype.IStereotypePropertyReferenceEdgeAdvice.STEREOTYPE_QUALIFIED_NAME_ANNOTATION_KEY;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.css.dom.GMFElementAdapter;
import org.eclipse.papyrus.infra.gmfdiag.css.engine.ExtendedCSSEngine;
import org.eclipse.papyrus.infra.tools.util.ListHelper;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayConstant;
import org.eclipse.papyrus.uml.diagram.common.stereotype.display.helper.StereotypeDisplayUtil;
import org.eclipse.papyrus.uml.diagram.css.helper.CSSDOMUMLSemanticElementHelper;
import org.eclipse.papyrus.uml.service.types.utils.ConnectorUtils;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;

/**
 * DOM Element Adapter for UML Elements
 *
 * Supports applied stereotypes and stereotype properties
 *
 * @author Camille Letavernier
 *
 */
public class GMFUMLElementAdapter extends GMFElementAdapter {


	/**
	 * The Constant STEREOTYPE_REFERENCE_EDGE_SOURCE_APPLIED_STEREOTYPES_PROPERTY.
	 */
	private static final String STEREOTYPE_REFERENCE_EDGE_SOURCE_APPLIED_STEREOTYPES_PROPERTY = "sourceAppliedStereotypes";//$NON-NLS-1$

	/**
	 * CSS property to access the connected elements type of a connector
	 */
	private static final String CONNECTOR_END_TYPE_PROPERTY = "connectorEndType"; //$NON-NLS-1$
	
	/**
	 * Name of the CSS Simple Selector to match on the Stereotype Compartment Shape
	 */
	private static final String STEREOTYPE_COMMENT = "StereotypeComment"; //$NON-NLS-1$

	/** The stereotype helper. */
	public final StereotypeDisplayUtil stereotypeHelper = StereotypeDisplayUtil.getInstance();

	/** The Constant IS_TIME_EVENT_ACTION. */
	private static final String IS_TIME_EVENT_ACTION_PROPERTY = "isTimeEventAction"; //$NON-NLS-1$

	/** The Constant APPLIED_STEREOTYPES_PROPERTY. */
	public static final String APPLIED_STEREOTYPES_PROPERTY = "appliedStereotypes"; //$NON-NLS-1$

	/** The Constant IS_FRAMEZABLE. */
	public static final String IS_FRAME = "isFrame"; //$NON-NLS-1$

	/**
	 * CSS property to verify if a stereotype is applied on the type of a TypedElement
	 */
	private static final String TYPE_APPLIED_STEREOTYPES_PROPERTY = "typeAppliedStereotypes"; //$NON-NLS-1$

	/** Notation type of comment link. */
	private static final String COMMENT_ANNOTATED_ELEMENT_EDGE_TYPE = "Comment_AnnotatedElementEdge";//$NON-NLS-1$

	/** Selector name for comment link. */
	private static final String COMMENT_LINK = "CommentLink";//$NON-NLS-1$

	/** Notation type of constraint link. */
	private static final String CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_TYPE = "Constraint_ConstrainedElementEdge";//$NON-NLS-1$

	/** Selector name for constraint link. */
	private static final String CONSTRAINT_LINK = "ConstraintLink";//$NON-NLS-1$

	/** Notation type of context link. */
	private static final String CONSTRAINT_CONTEXT_EDGE_TYPE = "Constraint_ContextEdge";//$NON-NLS-1$

	/** Selector name for context link. */
	private static final String CONTEXT_LINK = "ContextLink";//$NON-NLS-1$

	/** Selector name for stereotype property reference link. */
	private static final String STEREOTYPE_REFERENCE_LINK = "StereotypePropertyReferenceLink";//$NON-NLS-1$

	/** Selector name for stereotype stereotype comment link. */
	private static final String STEREOTYPE_COMMENT_LINK = "StereotypeCommentLink";//$NON-NLS-1$

	/**
	 * The CSS Separator for qualifiers, when we must use CSS ID
	 * When we can use CSS String, we use the standard UML "::" qualifier separator
	 *
	 * NOTE: Separator "__" does not work
	 */
	public static final String QUALIFIER_SEPARATOR = "--"; //$NON-NLS-1$

	public GMFUMLElementAdapter(View view, ExtendedCSSEngine engine) {
		super(view, engine);
		helper = CSSDOMUMLSemanticElementHelper.getInstance();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Applied Stereotypes are manipulated as DOM Attributes
	 */
	@Override
	protected String doGetAttribute(String attr) {

		String parentValue = super.doGetAttribute(attr);

		if (parentValue != null) {
			return parentValue;
		}


		// get stereotype Label attribute
		if (stereotypeHelper.isStereotypeLabel(semanticElement)) {
			String value = getStereotypeLabelAttribute(attr);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		// get stereotype Compartment attribute
		if (stereotypeHelper.isStereotypeCompartment(semanticElement) || stereotypeHelper.isStereotypeBrace(semanticElement)) {
			String value = getStereotypeCompartmentAttribute(attr);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}


		// get stereotype Property attribute
		if (stereotypeHelper.isStereotypeProperty(semanticElement) || stereotypeHelper.isStereotypeBraceProperty(semanticElement)) {

			String value = getStereotypePropertyAttribute(attr);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}

		if (semanticElement instanceof Element) {

			// Applied stereotypes
			Element currentElement = (Element) semanticElement;

			// Get applied STereotypes Attributes
			if (APPLIED_STEREOTYPES_PROPERTY.equals(attr)) {
				List<Stereotype> appliedStereotypes = currentElement.getAppliedStereotypes();
				if (!appliedStereotypes.isEmpty()) {
					List<String> appliedStereotypeNames = new ArrayList<String>(appliedStereotypes.size() * 2);
					for (Stereotype stereotype : appliedStereotypes) {
						appliedStereotypeNames.add(stereotype.getName());
						appliedStereotypeNames.add(stereotype.getQualifiedName());
					}
					return ListHelper.deepToString(appliedStereotypeNames, CSS_VALUES_SEPARATOR);
				}
				return ""; //$NON-NLS-1$
			}

			if (TYPE_APPLIED_STEREOTYPES_PROPERTY.equals(attr) && semanticElement instanceof TypedElement) {
				Type type = ((TypedElement) semanticElement).getType();
				if (type != null) {
					List<Stereotype> appliedStereotypes = type.getAppliedStereotypes();
					if (!appliedStereotypes.isEmpty()) { 
						List<String> appliedStereotypeNames = new ArrayList<String>(appliedStereotypes.size() * 2);
						for (Stereotype stereotype : appliedStereotypes) {
							appliedStereotypeNames.add(stereotype.getName());
							appliedStereotypeNames.add(stereotype.getQualifiedName());
						}
						return ListHelper.deepToString(appliedStereotypeNames, CSS_VALUES_SEPARATOR);
					}
				}
				return ""; //$NON-NLS-1$
			}

			for (EObject stereotypeApplication : currentElement.getStereotypeApplications()) {
				EStructuralFeature feature = stereotypeApplication.eClass().getEStructuralFeature(attr);
				if (feature != null) {
					if (feature.isMany()) {
						List<?> values = (List<?>) stereotypeApplication.eGet(feature);
						if (!values.isEmpty()) {
							List<String> cssValues = new ArrayList<String>(values.size());
							for (Object value : values) {
								cssValues.add(getCSSValue(feature, value));
							}
							return ListHelper.deepToString(cssValues, CSS_VALUES_SEPARATOR);
						} else {
							return ""; //$NON-NLS-1$
						}
					} else {
						Object value = stereotypeApplication.eGet(feature);
						String cssValue = getCSSValue(feature, value);
						return cssValue;
					}
				}
			}

			if (attr.contains(QUALIFIER_SEPARATOR)) {
				List<String> qualifiers = ListHelper.asList(attr.split(QUALIFIER_SEPARATOR)); // Writable list
				String propertyName = qualifiers.remove(qualifiers.size() - 1); // Last element is the property name
				// Remaining strings can be used to build the Stereotype's qualified name
				String stereotypeName = ListHelper.deepToString(qualifiers, "::"); //$NON-NLS-1$
				Stereotype appliedStereotype = currentElement.getAppliedStereotype(stereotypeName);
				if (appliedStereotype != null) {
					EObject stereotypeApplication = currentElement.getStereotypeApplication(appliedStereotype);
					EStructuralFeature feature = stereotypeApplication.eClass().getEStructuralFeature(propertyName);
					if (feature != null) {
						Object value = stereotypeApplication.eGet(feature);
						return getCSSValue(feature, value);
					}
				}
			}
			// manage of isTimeEventAction=true attribute for AcceptEventAction
			if (IS_TIME_EVENT_ACTION_PROPERTY.equals(attr)) {
				if (semanticElement instanceof AcceptEventAction) {
					return String.valueOf(isAcceptTimeEventAction((AcceptEventAction) semanticElement));
				}
			}

			// manage of isFraezable=true attribute for dislaying header/frame
			if (IS_FRAME.equals(attr)) {
				if (notationElement.eContainer() == notationElement.getDiagram()) {
					return String.valueOf(true);
				} else {
					return String.valueOf(false);
				}
			}
			if (CONNECTOR_END_TYPE_PROPERTY.equals(attr) && semanticElement instanceof Connector) {
				EList<ConnectorEnd> ends = ((Connector) semanticElement).getEnds();
				if (ends != null && !ends.isEmpty()) {
					// check that both connectable elements have the same type before returning the type 
					ConnectableElement source = new ConnectorUtils().getSourceConnectorEnd((Connector) semanticElement).getRole();
					ConnectableElement target = new ConnectorUtils().getTargetConnectorEnd((Connector) semanticElement).getRole();
					if (source.getType() != null && target.getType() != null && source.getType().equals(target.getType())) {
						return source.getType().getName();
					}
				}
			}
		}

		// manage of stereotype reference link
		if (STEREOTYPE_REFERENCE_EDGE_SOURCE_APPLIED_STEREOTYPES_PROPERTY.equals(attr)) {
			if (null != notationElement && STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT.equals(notationElement.getType())) {
				EAnnotation eAnnotation = notationElement.getEAnnotation(STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT);
				if (null != eAnnotation) {
					return eAnnotation.getDetails().get(STEREOTYPE_QUALIFIED_NAME_ANNOTATION_KEY);
				}
			}
		}
		if (FEATURE_TO_SET_ANNOTATION_KEY.equals(attr)) {
			if (null != notationElement && STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT.equals(notationElement.getType())) {
				EAnnotation eAnnotation = notationElement.getEAnnotation(STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT);
				if (null != eAnnotation) {
					return eAnnotation.getDetails().get(FEATURE_TO_SET_ANNOTATION_KEY);
				}
			}
		}

		return null;
	}

	/**
	 * Checks if is accept time event action.
	 *
	 * @param action
	 *            the action
	 * @return true, if is accept time event action
	 */
	public static boolean isAcceptTimeEventAction(AcceptEventAction action) {
		boolean hasTimeEvent = false;
		boolean hasOthersTriggers = false;
		// Get triggers
		if (action.getTriggers() != null) {
			for (Trigger trigger : action.getTriggers()) {
				if (trigger != null) {
					Event event = trigger.getEvent();
					if (event instanceof TimeEvent) {
						hasTimeEvent = true;
					} else {
						hasOthersTriggers = true;
					}
				}
			}
		}
		// only time events have been encountered.
		return hasTimeEvent && !hasOthersTriggers;
	}

	/**
	 * Retrieve the Matching String Value for the StereotypeCompartment Element
	 *
	 * @param attr
	 *            Attribute of the String to match with
	 * @return The matching value for this Attribute
	 */
	protected String getStereotypeCompartmentAttribute(String attr) {
		if (StereotypeDisplayConstant.STEREOTYPE_COMPARTMENT_NAME.equals(attr)) {

			BasicCompartment propertyCompartment = (BasicCompartment) semanticElement;
			return stereotypeHelper.getName(propertyCompartment);

		}
		return "";//$NON-NLS-1$
	}

	/**
	 * Retrieve the Matching String Value for the StereotypeCompartment Element
	 *
	 * @param attr
	 *            Attribute of the String to match with
	 * @return The matching value for this Attribute
	 */
	protected String getStereotypePropertyAttribute(String attr) {
		// CSS can match property level
		if (StereotypeDisplayConstant.STEREOTYPE_PROPERTY_NAME.equals(attr)) {

			DecorationNode propertyLabel = (DecorationNode) semanticElement;
			if (propertyLabel.getElement() instanceof Property) {
				Property prop = (Property) propertyLabel.getElement();
				String propLabel = prop.getName();
				return propLabel;
			}
			// CSS can match Container Name
		} else if (StereotypeDisplayConstant.STEREOTYPE_COMPARTMENT_NAME.equals(attr)) {

			EObject propertyCompartment = ((DecorationNode) semanticElement).eContainer();
			if (stereotypeHelper.isStereotypeCompartment(propertyCompartment)) {

				return stereotypeHelper.getName((DecorationNode) propertyCompartment);
			}

		}
		return "";//$NON-NLS-1$

	}

	/**
	 * Get the matching Value of the Attribute
	 *
	 * @param attr
	 *            Attribute of the String to match with
	 * @return The matching value for this Attribute
	 */
	protected String getStereotypeLabelAttribute(String attr) {

		if (StereotypeDisplayConstant.STEREOTYPE_LABEL_NAME.equals(attr)) {
			DecorationNode label = (DecorationNode) semanticElement;

			String stereoName = stereotypeHelper.getName(label);
			return stereoName;

		}

		if (KIND.equals(attr)) {
			if (stereotypeHelper.isStereotypeLabel(semanticElement)) {
				return StereotypeDisplayConstant.STEREOTYPE_LABEL_TYPE;
			}

		}
		return null;
	}

	@Override
	protected String getCSSValue(EStructuralFeature feature, Object value) {
		if (feature instanceof EReference && value instanceof NamedElement) {
			String name = ((NamedElement) value).getName();
			return name == null || name.isEmpty() ? EMPTY_VALUE : name; // Bug 467716: Never return null or empty string if the value is not null
		}
		return super.getCSSValue(feature, value);
	}

	/**
	 * @see org.eclipse.papyrus.infra.gmfdiag.css.dom.GMFElementAdapter#getLocalName()
	 *
	 * @return The Local name for the CSS matching.
	 */
	@Override
	public String getLocalName() {
		if (localName == null) {
			// In case of StereotypeComment type, the selector should match on the Stereotype Comment.
			if (stereotypeHelper.isStereotypeComment(getNotationElement())) {
				localName = STEREOTYPE_COMMENT;
			} else if (getSemanticElement() instanceof Edge) {
				switch (((Edge) getSemanticElement()).getType()) {
				case COMMENT_ANNOTATED_ELEMENT_EDGE_TYPE:
					localName = COMMENT_LINK;
					break;
				case CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_TYPE:
					localName = CONSTRAINT_LINK;
					break;
				case CONSTRAINT_CONTEXT_EDGE_TYPE:
					localName = CONTEXT_LINK;
					break;
				case STEREOTYPE_PROPERTY_REFERENCE_EDGE_HINT:
					localName = STEREOTYPE_REFERENCE_LINK;
					break;
				case StereotypeDisplayConstant.STEREOTYPE_COMMENT_LINK_TYPE:
					localName = STEREOTYPE_COMMENT_LINK;
					break;
				}
			}
		}

		return localName == null ? super.getLocalName() : localName;
	}
}
