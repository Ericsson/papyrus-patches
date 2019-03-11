/*****************************************************************************
 * Copyright (c) 2010 CEA
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
 *   Soyatec - Initial API and implementation
 *   Céline Janssens (ALL4TEC) celine.janssens@all4tec.net - Bug 440230 : Label Margin
 *   Nicolas FAUVERGUE (ALL4TEC) nicolas.fauvergue@all4tec.net - Bug 496905
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ISemanticParser;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.emf.gmf.command.EMFtoGMFCommandWrapper;
import org.eclipse.papyrus.infra.internationalization.common.utils.InternationalizationPreferencesUtils;
import org.eclipse.papyrus.uml.diagram.sequence.parsers.MessageFormatParser;
import org.eclipse.papyrus.uml.internationalization.utils.utils.UMLLabelInternationalization;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Add implementing IPapyrusEditPart to displaying Stereotypes.
 *
 * @author Jin Liu (jin.liu@soyatec.com)
 *
 *         XXX Check remaining usages
 */
@Deprecated
public class OLDCustomCombinedFragmentEditPart {


	/**
	 * Default Margin when not present in CSS
	 */
	public static final int DEFAULT_MARGIN = 0;

	/**
	 * CSS Integer property to define the horizontal Label Margin
	 */
	public static final String TOP_MARGIN_PROPERTY = "TopMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the vertical Label Margin
	 */
	public static final String LEFT_MARGIN_PROPERTY = "LeftMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the horizontal Label Margin
	 */
	public static final String BOTTOM_MARGIN_PROPERTY = "BottomMarginLabel"; // $NON-NLS$

	/**
	 * CSS Integer property to define the vertical Label Margin
	 */
	public static final String RIGHT_MARGIN_PROPERTY = "RightMarginLabel"; // $NON-NLS$
	/**
	 * Title for dialog of bloc operator modification error
	 */
	private static final String FORBIDDEN_ACTION = "Forbidden action"; //$NON-NLS-1$

	/**
	 * Message for dialog of block operator modification
	 */
	private static final String BLOCK_OPERATOR_MODIFICATION_MSG = "It's impossible to change the operator kind of the combined fragment\nbecause the combined fragment has more than one operand"; //$NON-NLS-1$

	/**
	 * Message for dialog of block forbidden operator modification
	 */
	private static final String FORBIDEN_OPERATOR_MODIFICATION_MSG = "It's impossible to configure combined fragment as consider or ignore.\nUse ConsiderIgnoreFragment instead"; //$NON-NLS-1$

	/**
	 * Message for dialog of block forbidden operand addition
	 */
	private static final String BLOCK_OPERAND_ADDITION_MSG = "It's impossible to add more than one operand on opt, loop, break, neg combined fragment"; //$NON-NLS-1$


	@Deprecated
	private OLDCustomCombinedFragmentEditPart() {
		// Deprecated
	}

	static class CombinedFragmentTitleParser extends MessageFormatParser implements ISemanticParser {

		public CombinedFragmentTitleParser() {
			super(new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() });
		}

		@Override
		public List getSemanticElementsBeingParsed(EObject element) {
			List<Element> semanticElementsBeingParsed = new ArrayList<>();
			if (element instanceof CombinedFragment) {
				CombinedFragment cf = (CombinedFragment) element;
				semanticElementsBeingParsed.add(cf);
			}
			return semanticElementsBeingParsed;
		}

		@Override
		public boolean areSemanticElementsAffected(EObject listener, Object notification) {
			EStructuralFeature feature = getEStructuralFeature(notification);
			return isValidFeature(feature);
		}

		@Override
		public boolean isAffectingEvent(Object event, int flags) {
			EStructuralFeature feature = getEStructuralFeature(event);
			return isValidFeature(feature);
		}

		@Override
		public String getPrintString(IAdaptable element, int flags) {
			Object adapter = element.getAdapter(EObject.class);
			if (adapter instanceof CombinedFragment) {
				CombinedFragment cf = (CombinedFragment) adapter;
				return UMLLabelInternationalization.getInstance().getLabel(cf);
			}
			return "";
		}

		@Override
		public IParserEditStatus isValidEditString(IAdaptable adapter, String editString) {
			return ParserEditStatus.EDITABLE_STATUS;
		}

		@Override
		public ICommand getParseCommand(IAdaptable adapter, String newString, int flags) {
			EObject element = adapter.getAdapter(EObject.class);
			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(element);
			if (editingDomain == null || !(element instanceof CombinedFragment)) {
				return UnexecutableCommand.INSTANCE;
			}

			ICommand command = null;
			if (InternationalizationPreferencesUtils.getInternationalizationPreference(element) && null != UMLLabelInternationalization.getInstance().getLabelWithoutUML((NamedElement) element)) {
				final ModelSet modelSet = (ModelSet) element.eResource().getResourceSet();
				command = new EMFtoGMFCommandWrapper(UMLLabelInternationalization.getInstance().getSetLabelCommand(modelSet.getTransactionalEditingDomain(), (NamedElement) element, newString, null));
			} else {
				SetRequest request = new SetRequest(element, UMLPackage.eINSTANCE.getNamedElement_Name(), newString);
				command = new SetValueCommand(request);
			}
			return command;
		}

		@Override
		public String getEditString(IAdaptable element, int flags) {
			Object adapter = element.getAdapter(EObject.class);
			if (adapter instanceof CombinedFragment) {
				CombinedFragment cf = (CombinedFragment) adapter;
				return UMLLabelInternationalization.getInstance().getLabel(cf);
			}
			return "";
		}

		protected EStructuralFeature getEStructuralFeature(Object notification) {
			EStructuralFeature featureImpl = null;
			if (notification instanceof Notification) {
				Object feature = ((Notification) notification).getFeature();
				if (feature instanceof EStructuralFeature) {
					featureImpl = (EStructuralFeature) feature;
				}
			}
			return featureImpl;
		}

		private boolean isValidFeature(EStructuralFeature feature) {
			return UMLPackage.eINSTANCE.getNamedElement_Name().equals(feature);
		}
	}

	static class EObjectAdapterEx extends EObjectAdapter {

		private View view = null;

		/**
		 * constructor
		 *
		 * @param element
		 *                    element to be wrapped
		 * @param view
		 *                    view to be wrapped
		 */
		public EObjectAdapterEx(EObject element, View view) {
			super(element);
			this.view = view;
		}

		@Override
		public Object getAdapter(Class adapter) {
			Object o = super.getAdapter(adapter);
			if (o != null) {
				return o;
			}
			if (adapter.equals(View.class)) {
				return view;
			}
			return null;
		}
	}

	static class ResizableShapeEditPolicyEx extends ResizableShapeEditPolicy {

		@Override
		protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
			IFigure feedback = getDragSourceFeedbackFigure();
			PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
			getHostFigure().translateToAbsolute(rect);
			Rectangle old = rect.getCopy();
			rect.translate(request.getMoveDelta());
			rect.resize(request.getSizeDelta());
			IFigure f = getHostFigure();
			Dimension min = f.getMinimumSize().getCopy();
			Dimension max = f.getMaximumSize().getCopy();
			IMapMode mmode = MapModeUtil.getMapMode(f);
			min.height = mmode.LPtoDP(min.height);
			min.width = mmode.LPtoDP(min.width);
			max.height = mmode.LPtoDP(max.height);
			max.width = mmode.LPtoDP(max.width);
			if (min.width > rect.width) {
				rect.width = min.width;
				if (request.getMoveDelta().x > 0 && request.getSizeDelta().width < 0) { // shrinking from left
					rect.x = old.getRight().x - min.width;
					request.getMoveDelta().x = rect.x - old.getLeft().x;
				}
			} else if (max.width < rect.width) {
				rect.width = max.width;
			}
			if (min.height > rect.height) {
				rect.height = min.height;
				if (request.getMoveDelta().y > 0 && request.getSizeDelta().height < 0) { // shrinking from upper
					rect.y = old.getBottom().y - min.height;
					request.getMoveDelta().y = rect.y - old.getTop().y;
				}
			} else if (max.height < rect.height) {
				rect.height = max.height;
			}
			feedback.translateToRelative(rect);
			feedback.setBounds(rect);
		}
	}
}
