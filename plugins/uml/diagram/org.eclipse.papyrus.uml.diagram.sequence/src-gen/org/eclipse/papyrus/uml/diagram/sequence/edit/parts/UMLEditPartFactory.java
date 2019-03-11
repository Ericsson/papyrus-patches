package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.directedit.locator.CellEditorLocatorAccess;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IMultilineEditableFigure;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;

/**
 * @generated
 */
public class UMLEditPartFactory implements EditPartFactory {

	/**
	 * @generated
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof View) {
			View view = (View) model;
			switch (UMLVisualIDRegistry.getVisualID(view)) {

			case SequenceDiagramEditPart.VISUAL_ID:
				return new SequenceDiagramEditPart(view);

			case InteractionEditPart.VISUAL_ID:
				return new InteractionEditPart(view);

			case InteractionNameEditPart.VISUAL_ID:
				return new InteractionNameEditPart(view);

			case ConsiderIgnoreFragmentEditPart.VISUAL_ID:
				return new ConsiderIgnoreFragmentEditPart(view);

			case CombinedFragmentEditPart.VISUAL_ID:
				return new CombinedFragmentEditPart(view);

			case InteractionOperandEditPart.VISUAL_ID:
				return new InteractionOperandEditPart(view);

			case InteractionUseEditPart.VISUAL_ID:
				return new InteractionUseEditPart(view);

			case InteractionUseNameEditPart.VISUAL_ID:
				return new InteractionUseNameEditPart(view);

			case InteractionUseName2EditPart.VISUAL_ID:
				return new InteractionUseName2EditPart(view);

			case ContinuationEditPart.VISUAL_ID:
				return new ContinuationEditPart(view);

			case ContinuationNameEditPart.VISUAL_ID:
				return new ContinuationNameEditPart(view);

			case LifelineEditPart.VISUAL_ID:
				return new LifelineEditPart(view);

			case LifelineNameEditPart.VISUAL_ID:
				return new LifelineNameEditPart(view);

			case ActionExecutionSpecificationEditPart.VISUAL_ID:
				return new ActionExecutionSpecificationEditPart(view);

			case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
				return new BehaviorExecutionSpecificationEditPart(view);

			case StateInvariantEditPart.VISUAL_ID:
				return new StateInvariantEditPart(view);

			case StateInvariantNameEditPart.VISUAL_ID:
				return new StateInvariantNameEditPart(view);

			case StateInvariantLabelEditPart.VISUAL_ID:
				return new StateInvariantLabelEditPart(view);

			case TimeConstraintEditPart.VISUAL_ID:
				return new TimeConstraintEditPart(view);

			case TimeConstraintLabelEditPart.VISUAL_ID:
				return new TimeConstraintLabelEditPart(view);

			case TimeConstraintAppliedStereotypeEditPart.VISUAL_ID:
				return new TimeConstraintAppliedStereotypeEditPart(view);

			case TimeObservationEditPart.VISUAL_ID:
				return new TimeObservationEditPart(view);

			case TimeObservationLabelEditPart.VISUAL_ID:
				return new TimeObservationLabelEditPart(view);

			case TimeObservationAppliedStereotypeEditPart.VISUAL_ID:
				return new TimeObservationAppliedStereotypeEditPart(view);

			case DurationConstraintEditPart.VISUAL_ID:
				return new DurationConstraintEditPart(view);

			case DurationConstraintLabelEditPart.VISUAL_ID:
				return new DurationConstraintLabelEditPart(view);

			case DurationConstraintAppliedStereotypeEditPart.VISUAL_ID:
				return new DurationConstraintAppliedStereotypeEditPart(view);

			case DestructionOccurrenceSpecificationEditPart.VISUAL_ID:
				return new DestructionOccurrenceSpecificationEditPart(view);

			case ConstraintEditPart.VISUAL_ID:
				return new ConstraintEditPart(view);

			case ConstraintNameEditPart.VISUAL_ID:
				return new ConstraintNameEditPart(view);

			case Constraint2EditPart.VISUAL_ID:
				return new Constraint2EditPart(view);

			case CommentEditPart.VISUAL_ID:
				return new CommentEditPart(view);

			case CommentBodyEditPart.VISUAL_ID:
				return new CommentBodyEditPart(view);

			case DurationConstraintInMessageEditPart.VISUAL_ID:
				return new DurationConstraintInMessageEditPart(view);

			case DurationConstraintInMessageLabelEditPart.VISUAL_ID:
				return new DurationConstraintInMessageLabelEditPart(view);

			case DurationConstraintInMessageAppliedStereotypeEditPart.VISUAL_ID:
				return new DurationConstraintInMessageAppliedStereotypeEditPart(view);

			case DurationObservationEditPart.VISUAL_ID:
				return new DurationObservationEditPart(view);

			case DurationObservationLabelEditPart.VISUAL_ID:
				return new DurationObservationLabelEditPart(view);

			case DurationObservationAppliedStereotypeEditPart.VISUAL_ID:
				return new DurationObservationAppliedStereotypeEditPart(view);

			case GateEditPart.VISUAL_ID:
				return new GateEditPart(view);

			case GateNameEditPart.VISUAL_ID:
				return new GateNameEditPart(view);

			case InteractionInteractionCompartmentEditPart.VISUAL_ID:
				return new InteractionInteractionCompartmentEditPart(view);

			case CombinedFragmentCombinedFragmentCompartmentEditPart.VISUAL_ID:
				return new CombinedFragmentCombinedFragmentCompartmentEditPart(view);

			case MessageSyncEditPart.VISUAL_ID:
				return new MessageSyncEditPart(view);

			case MessageSyncNameEditPart.VISUAL_ID:
				return new MessageSyncNameEditPart(view);

			case MessageSyncAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageSyncAppliedStereotypeEditPart(view);

			case MessageAsyncEditPart.VISUAL_ID:
				return new MessageAsyncEditPart(view);

			case MessageAsyncNameEditPart.VISUAL_ID:
				return new MessageAsyncNameEditPart(view);

			case MessageAsyncAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageAsyncAppliedStereotypeEditPart(view);

			case MessageReplyEditPart.VISUAL_ID:
				return new MessageReplyEditPart(view);

			case MessageReplyNameEditPart.VISUAL_ID:
				return new MessageReplyNameEditPart(view);

			case MessageReplyAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageReplyAppliedStereotypeEditPart(view);

			case MessageCreateEditPart.VISUAL_ID:
				return new MessageCreateEditPart(view);

			case MessageCreateNameEditPart.VISUAL_ID:
				return new MessageCreateNameEditPart(view);

			case MessageCreateAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageCreateAppliedStereotypeEditPart(view);

			case MessageDeleteEditPart.VISUAL_ID:
				return new MessageDeleteEditPart(view);

			case MessageDeleteNameEditPart.VISUAL_ID:
				return new MessageDeleteNameEditPart(view);

			case MessageDeleteAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageDeleteAppliedStereotypeEditPart(view);

			case MessageLostEditPart.VISUAL_ID:
				return new MessageLostEditPart(view);

			case MessageLostNameEditPart.VISUAL_ID:
				return new MessageLostNameEditPart(view);

			case MessageLostAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageLostAppliedStereotypeEditPart(view);

			case MessageFoundEditPart.VISUAL_ID:
				return new MessageFoundEditPart(view);

			case MessageFoundNameEditPart.VISUAL_ID:
				return new MessageFoundNameEditPart(view);

			case MessageFoundAppliedStereotypeEditPart.VISUAL_ID:
				return new MessageFoundAppliedStereotypeEditPart(view);

			case CommentAnnotatedElementEditPart.VISUAL_ID:
				return new CommentAnnotatedElementEditPart(view);

			case ConstraintConstrainedElementEditPart.VISUAL_ID:
				return new ConstraintConstrainedElementEditPart(view);

			case GeneralOrderingEditPart.VISUAL_ID:
				return new GeneralOrderingEditPart(view);

			case GeneralOrderingAppliedStereotypeEditPart.VISUAL_ID:
				return new GeneralOrderingAppliedStereotypeEditPart(view);

			case ContextLinkEditPart.VISUAL_ID:
				return new ContextLinkEditPart(view);

			case ConstraintContextAppliedStereotypeEditPart.VISUAL_ID:
				return new ConstraintContextAppliedStereotypeEditPart(view);

			}
		}
		return createUnrecognizedEditPart(context, model);
	}

	/**
	 * @generated
	 */
	private EditPart createUnrecognizedEditPart(EditPart context, Object model) {
		// Handle creation of unrecognized child node EditParts here
		return null;
	}

	/**
	 * @generated
	 */
	public static CellEditorLocator getTextCellEditorLocator(ITextAwareEditPart source) {
		if (source.getFigure() instanceof IMultilineEditableFigure) {
			return new MultilineCellEditorLocator((IMultilineEditableFigure) source.getFigure());
		} else {
			return CellEditorLocatorAccess.INSTANCE.getTextCellEditorLocator(source);

		}
	}

	/**
	 * @generated
	 */
	static private class MultilineCellEditorLocator implements CellEditorLocator {

		/**
		 * @generated
		 */
		private IMultilineEditableFigure multilineEditableFigure;

		/**
		 * @generated
		 */
		public MultilineCellEditorLocator(IMultilineEditableFigure figure) {
			this.multilineEditableFigure = figure;
		}

		/**
		 * @generated
		 */
		public IMultilineEditableFigure getMultilineEditableFigure() {
			return multilineEditableFigure;
		}

		/**
		 * @generated
		 */
		@Override
		public void relocate(CellEditor celleditor) {
			Text text = (Text) celleditor.getControl();
			Rectangle rect = getMultilineEditableFigure().getBounds().getCopy();
			rect.x = getMultilineEditableFigure().getEditionLocation().x;
			rect.y = getMultilineEditableFigure().getEditionLocation().y;
			getMultilineEditableFigure().translateToAbsolute(rect);
			if (getMultilineEditableFigure().getText().length() > 0) {
				rect.setSize(new Dimension(text.computeSize(rect.width, SWT.DEFAULT)));
			}
			if (!rect.equals(new Rectangle(text.getBounds()))) {
				text.setBounds(rect.x, rect.y, rect.width, rect.height);
			}
		}
	}

}
