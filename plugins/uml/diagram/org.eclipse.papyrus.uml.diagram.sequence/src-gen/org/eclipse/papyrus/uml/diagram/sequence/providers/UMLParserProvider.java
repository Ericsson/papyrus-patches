/*****************************************************************************
 * Copyright (c) 2009, 2018 Atos Origin, Christian W. Damus, CEA LIST, and others.
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
 *   Atos Origin - Initial API and implementation
 *   Christian W. Damus - bug 536486
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserService;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ParserHintAdapter;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.common.parser.CommentParser;
import org.eclipse.papyrus.uml.diagram.common.parser.ConstraintParser;
import org.eclipse.papyrus.uml.diagram.common.parser.ObservationParser;
import org.eclipse.papyrus.uml.diagram.common.parser.stereotype.AppliedStereotypeParser;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.CommentBodyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.Constraint2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ConstraintNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ContinuationNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationConstraintLinkNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.DurationObservationLinkNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GeneralOrderingAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseName2EditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageFoundNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageLostNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantLabelEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.StateInvariantNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeConstraintNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationAppliedStereotypeEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.TimeObservationNameEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.parser.custom.InteractionUseCustomParsers;
import org.eclipse.papyrus.uml.diagram.sequence.parser.custom.LifelineCustomParsers;
import org.eclipse.papyrus.uml.diagram.sequence.parser.custom.MessageCustomParser;
import org.eclipse.papyrus.uml.diagram.sequence.parsers.MessageFormatParser;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @generated
 */
public class UMLParserProvider extends AbstractProvider implements IParserProvider {
	/**
	 * @generated
	 *
	 */
	private IParser interaction_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getInteraction_NameLabel_Parser() {
		if (interaction_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			parser.setViewPattern("sd: {0}"); //$NON-NLS-1$
			parser.setEditorPattern("{0}"); //$NON-NLS-1$
			parser.setEditPattern("{0}"); //$NON-NLS-1$
			interaction_NameLabel_Parser = parser;
		}
		return interaction_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private IParser interactionUse_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getInteractionUse_NameLabel_Parser() {
		if (interactionUse_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			parser.setViewPattern("Ref"); //$NON-NLS-1$
			parser.setEditorPattern("Ref"); //$NON-NLS-1$
			parser.setEditPattern("Ref"); //$NON-NLS-1$
			interactionUse_NameLabel_Parser = parser;
		}
		return interactionUse_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private InteractionUseCustomParsers interactionUse_TypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getInteractionUse_TypeLabel_Parser() {
		if (interactionUse_TypeLabel_Parser == null) {
			interactionUse_TypeLabel_Parser = new InteractionUseCustomParsers();
		}
		return interactionUse_TypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private IParser continuation_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getContinuation_NameLabel_Parser() {
		if (continuation_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			continuation_NameLabel_Parser = parser;
		}
		return continuation_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private LifelineCustomParsers lifeline_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getLifeline_NameLabel_Parser() {
		if (lifeline_NameLabel_Parser == null) {
			lifeline_NameLabel_Parser = new LifelineCustomParsers();
		}
		return lifeline_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private IParser stateInvariant_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getStateInvariant_NameLabel_Parser() {
		if (stateInvariant_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			stateInvariant_NameLabel_Parser = parser;
		}
		return stateInvariant_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ConstraintParser stateInvariant_ConstraintLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getStateInvariant_ConstraintLabel_Parser() {
		if (stateInvariant_ConstraintLabel_Parser == null) {
			stateInvariant_ConstraintLabel_Parser = new ConstraintParser();
		}
		return stateInvariant_ConstraintLabel_Parser;
	}

	/**
	 * @generated
	 */
	private IParser constraint_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getConstraint_NameLabel_Parser() {
		if (constraint_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			constraint_NameLabel_Parser = parser;
		}
		return constraint_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ConstraintParser constraint_BodyLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getConstraint_BodyLabel_Parser() {
		if (constraint_BodyLabel_Parser == null) {
			constraint_BodyLabel_Parser = new ConstraintParser();
		}
		return constraint_BodyLabel_Parser;
	}

	/**
	 * @generated
	 */
	private CommentParser comment_BodyLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getComment_BodyLabel_Parser() {
		if (comment_BodyLabel_Parser == null) {
			comment_BodyLabel_Parser = new CommentParser();
		}
		return comment_BodyLabel_Parser;
	}

	/**
	 * @generated
	 */
	private IParser gate_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getGate_NameLabel_Parser() {
		if (gate_NameLabel_Parser == null) {
			EAttribute[] features = new EAttribute[] { UMLPackage.eINSTANCE.getNamedElement_Name() };
			MessageFormatParser parser = new MessageFormatParser(features);
			gate_NameLabel_Parser = parser;
		}
		return gate_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ConstraintParser timeConstraint_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getTimeConstraint_NameLabel_Parser() {
		if (timeConstraint_NameLabel_Parser == null) {
			timeConstraint_NameLabel_Parser = new ConstraintParser();
		}
		return timeConstraint_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser timeConstraint_StereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getTimeConstraint_StereotypeLabel_Parser() {
		if (timeConstraint_StereotypeLabel_Parser == null) {
			timeConstraint_StereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return timeConstraint_StereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ObservationParser timeObservation_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getTimeObservation_NameLabel_Parser() {
		if (timeObservation_NameLabel_Parser == null) {
			timeObservation_NameLabel_Parser = new ObservationParser();
		}
		return timeObservation_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser timeObservation_StereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getTimeObservation_StereotypeLabel_Parser() {
		if (timeObservation_StereotypeLabel_Parser == null) {
			timeObservation_StereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return timeObservation_StereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_SynchNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_SynchNameLabel_Parser() {
		if (message_SynchNameLabel_Parser == null) {
			message_SynchNameLabel_Parser = new MessageCustomParser();
		}
		return message_SynchNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_SynchStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_SynchStereotypeLabel_Parser() {
		if (message_SynchStereotypeLabel_Parser == null) {
			message_SynchStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_SynchStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_AsynchNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_AsynchNameLabel_Parser() {
		if (message_AsynchNameLabel_Parser == null) {
			message_AsynchNameLabel_Parser = new MessageCustomParser();
		}
		return message_AsynchNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_AsynchStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_AsynchStereotypeLabel_Parser() {
		if (message_AsynchStereotypeLabel_Parser == null) {
			message_AsynchStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_AsynchStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_ReplyNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_ReplyNameLabel_Parser() {
		if (message_ReplyNameLabel_Parser == null) {
			message_ReplyNameLabel_Parser = new MessageCustomParser();
		}
		return message_ReplyNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_ReplyStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_ReplyStereotypeLabel_Parser() {
		if (message_ReplyStereotypeLabel_Parser == null) {
			message_ReplyStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_ReplyStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_CreateNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_CreateNameLabel_Parser() {
		if (message_CreateNameLabel_Parser == null) {
			message_CreateNameLabel_Parser = new MessageCustomParser();
		}
		return message_CreateNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_CreateStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_CreateStereotypeLabel_Parser() {
		if (message_CreateStereotypeLabel_Parser == null) {
			message_CreateStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_CreateStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_DeleteNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_DeleteNameLabel_Parser() {
		if (message_DeleteNameLabel_Parser == null) {
			message_DeleteNameLabel_Parser = new MessageCustomParser();
		}
		return message_DeleteNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_DeleteStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_DeleteStereotypeLabel_Parser() {
		if (message_DeleteStereotypeLabel_Parser == null) {
			message_DeleteStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_DeleteStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_LostNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_LostNameLabel_Parser() {
		if (message_LostNameLabel_Parser == null) {
			message_LostNameLabel_Parser = new MessageCustomParser();
		}
		return message_LostNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_LostStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_LostStereotypeLabel_Parser() {
		if (message_LostStereotypeLabel_Parser == null) {
			message_LostStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_LostStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private MessageCustomParser message_FoundNameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_FoundNameLabel_Parser() {
		if (message_FoundNameLabel_Parser == null) {
			message_FoundNameLabel_Parser = new MessageCustomParser();
		}
		return message_FoundNameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser message_FoundStereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getMessage_FoundStereotypeLabel_Parser() {
		if (message_FoundStereotypeLabel_Parser == null) {
			message_FoundStereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return message_FoundStereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser generalOrdering_StereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getGeneralOrdering_StereotypeLabel_Parser() {
		if (generalOrdering_StereotypeLabel_Parser == null) {
			generalOrdering_StereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return generalOrdering_StereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ConstraintParser durationConstraint_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getDurationConstraint_NameLabel_Parser() {
		if (durationConstraint_NameLabel_Parser == null) {
			durationConstraint_NameLabel_Parser = new ConstraintParser();
		}
		return durationConstraint_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser durationConstraint_StereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getDurationConstraint_StereotypeLabel_Parser() {
		if (durationConstraint_StereotypeLabel_Parser == null) {
			durationConstraint_StereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return durationConstraint_StereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	private ObservationParser durationObservation_NameLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getDurationObservation_NameLabel_Parser() {
		if (durationObservation_NameLabel_Parser == null) {
			durationObservation_NameLabel_Parser = new ObservationParser();
		}
		return durationObservation_NameLabel_Parser;
	}

	/**
	 * @generated
	 */
	private AppliedStereotypeParser durationObservation_StereotypeLabel_Parser;

	/**
	 * @generated
	 */
	private IParser getDurationObservation_StereotypeLabel_Parser() {
		if (durationObservation_StereotypeLabel_Parser == null) {
			durationObservation_StereotypeLabel_Parser = new AppliedStereotypeParser();
		}
		return durationObservation_StereotypeLabel_Parser;
	}

	/**
	 * @generated
	 */
	protected IParser getParser(String visualID) {
		if (visualID != null) {
			switch (visualID) {
			case InteractionNameEditPart.VISUAL_ID:
				return getInteraction_NameLabel_Parser();

			case InteractionUseNameEditPart.VISUAL_ID:
				return getInteractionUse_NameLabel_Parser();
			case InteractionUseName2EditPart.VISUAL_ID:
				return getInteractionUse_TypeLabel_Parser();

			case ContinuationNameEditPart.VISUAL_ID:
				return getContinuation_NameLabel_Parser();

			case LifelineNameEditPart.VISUAL_ID:
				return getLifeline_NameLabel_Parser();

			case StateInvariantNameEditPart.VISUAL_ID:
				return getStateInvariant_NameLabel_Parser();
			case StateInvariantLabelEditPart.VISUAL_ID:
				return getStateInvariant_ConstraintLabel_Parser();

			case ConstraintNameEditPart.VISUAL_ID:
				return getConstraint_NameLabel_Parser();
			case Constraint2EditPart.VISUAL_ID:
				return getConstraint_BodyLabel_Parser();

			case CommentBodyEditPart.VISUAL_ID:
				return getComment_BodyLabel_Parser();

			case GateNameEditPart.VISUAL_ID:
				return getGate_NameLabel_Parser();

			case TimeConstraintNameEditPart.VISUAL_ID:
				return getTimeConstraint_NameLabel_Parser();
			case TimeConstraintAppliedStereotypeEditPart.VISUAL_ID:
				return getTimeConstraint_StereotypeLabel_Parser();

			case TimeObservationNameEditPart.VISUAL_ID:
				return getTimeObservation_NameLabel_Parser();
			case TimeObservationAppliedStereotypeEditPart.VISUAL_ID:
				return getTimeObservation_StereotypeLabel_Parser();

			case MessageSyncNameEditPart.VISUAL_ID:
				return getMessage_SynchNameLabel_Parser();
			case MessageSyncAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_SynchStereotypeLabel_Parser();

			case MessageAsyncNameEditPart.VISUAL_ID:
				return getMessage_AsynchNameLabel_Parser();
			case MessageAsyncAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_AsynchStereotypeLabel_Parser();

			case MessageReplyNameEditPart.VISUAL_ID:
				return getMessage_ReplyNameLabel_Parser();
			case MessageReplyAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_ReplyStereotypeLabel_Parser();

			case MessageCreateNameEditPart.VISUAL_ID:
				return getMessage_CreateNameLabel_Parser();
			case MessageCreateAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_CreateStereotypeLabel_Parser();

			case MessageDeleteNameEditPart.VISUAL_ID:
				return getMessage_DeleteNameLabel_Parser();
			case MessageDeleteAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_DeleteStereotypeLabel_Parser();

			case MessageLostNameEditPart.VISUAL_ID:
				return getMessage_LostNameLabel_Parser();
			case MessageLostAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_LostStereotypeLabel_Parser();

			case MessageFoundNameEditPart.VISUAL_ID:
				return getMessage_FoundNameLabel_Parser();
			case MessageFoundAppliedStereotypeEditPart.VISUAL_ID:
				return getMessage_FoundStereotypeLabel_Parser();

			case GeneralOrderingAppliedStereotypeEditPart.VISUAL_ID:
				return getGeneralOrdering_StereotypeLabel_Parser();

			case DurationConstraintLinkNameEditPart.VISUAL_ID:
				return getDurationConstraint_NameLabel_Parser();
			case DurationConstraintLinkAppliedStereotypeEditPart.VISUAL_ID:
				return getDurationConstraint_StereotypeLabel_Parser();

			case DurationObservationLinkNameEditPart.VISUAL_ID:
				return getDurationObservation_NameLabel_Parser();
			case DurationObservationLinkAppliedStereotypeEditPart.VISUAL_ID:
				return getDurationObservation_StereotypeLabel_Parser();

			}
		}
		return null;
	}

	/**
	 * Utility method that consults ParserService
	 *
	 * @generated
	 */
	public static IParser getParser(IElementType type, EObject object, String parserHint) {
		return ParserService.getInstance().getParser(new HintAdapter(type, object, parserHint));
	}

	/**
	 * @generated
	 */
	@Override
	public IParser getParser(IAdaptable hint) {
		String vid = hint.getAdapter(String.class);
		if (vid != null) {
			return getParser(UMLVisualIDRegistry.getVisualID(vid));
		}
		View view = hint.getAdapter(View.class);
		if (view != null) {
			return getParser(UMLVisualIDRegistry.getVisualID(view));
		}
		return null;
	}

	/**
	 * @generated
	 */
	@Override
	public boolean provides(IOperation operation) {
		if (operation instanceof GetParserOperation) {
			IAdaptable hint = ((GetParserOperation) operation).getHint();
			if (UMLElementTypes.getElement(hint) == null) {
				return false;
			}
			return getParser(hint) != null;
		}
		return false;
	}

	/**
	 * @generated
	 */
	private static class HintAdapter extends ParserHintAdapter {

		/**
		 * @generated
		 */
		private final IElementType elementType;

		/**
		 * @generated
		 */
		public HintAdapter(IElementType type, EObject object, String parserHint) {
			super(object, parserHint);
			assert type != null;
			elementType = type;
		}

		/**
		 * @generated
		 */
		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			if (IElementType.class.equals(adapter)) {
				return elementType;
			}
			return super.getAdapter(adapter);
		}
	}
}
