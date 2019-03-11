/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tessier (CEA LIST) - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 521312, 526079, 526191
 *   Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Bug 531520
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.SemanticFromGMFElement;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.AbstractMessageEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.RedirectionCommandStackListener;
import org.eclipse.papyrus.uml.diagram.sequence.util.RedirectionContentAdapter;
import org.eclipse.papyrus.uml.diagram.sequence.util.RedirectionOperationListener;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.OccurrenceSpecification;

/**
 * this class is used to manage strong and weak references to editPart in the sequence : all execution specification and all messages
 * for example: a message connected to an executionSpecification means that a strong reference exists between editpart of the message
 * and the editpart of the execution specification.
 * Two consecutive execution specifications on the same life-line means that a weak reference exists from the top exec to the bottom lifeline.
 *
 * @since 4.0
 */
public class SequenceReferenceEditPolicy extends GraphicalEditPolicy implements NotificationListener {

	public static final String SEQUENCE_REFERENCE = "SEQUENCE_REFERENCE"; //$NON-NLS-1$
	public static final String NO_ROLE = "NO_ROLE"; //$NON-NLS-1$
	public static final String ROLE_START = "START"; //$NON-NLS-1$
	public static final String ROLE_FINISH = "FINISH"; //$NON-NLS-1$
	public static final String ROLE_SOURCE = "SOURCE"; //$NON-NLS-1$
	public static final String ROLE_TARGET = "TARGET"; //$NON-NLS-1$

	protected HashMap<EditPart, String> weakReferences = new HashMap<>();
	protected HashMap<EditPart, String> strongReferences = new HashMap<>();
	protected RedirectionContentAdapter redirectionContentAdapter;
	protected RedirectionCommandStackListener redirectionCommandStackListener;
	protected RedirectionOperationListener redirectionOperationListener;

	/**
	 * @return the weakReferences
	 */
	public HashMap<EditPart, String> getWeakReferences() {
		return weakReferences;
	}

	/**
	 * @return the strongReferences
	 */
	public HashMap<EditPart, String> getStrongReferences() {
		return strongReferences;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();
		// add a listener to update weak and string references
		redirectionOperationListener = new RedirectionOperationListener(this);
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(redirectionOperationListener);

		updateStrongAndWeakReferences();
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#deactivate()
	 *
	 */
	@Override
	public void deactivate() {
		if (null != redirectionOperationListener) {
			OperationHistoryFactory.getOperationHistory().removeOperationHistoryListener(redirectionOperationListener);
		}
		super.deactivate();
	}

	/**
	 * compute strong a weak reference
	 */
	public void updateStrongAndWeakReferences() {
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG, "+ Update Strong and weak ref " + getHost().getClass().getName());//$NON-NLS-1$

		strongReferences.clear();
		weakReferences.clear();

		// management of execution specification
		if (getHost() instanceof AbstractExecutionSpecificationEditPart && (((AbstractExecutionSpecificationEditPart) getHost()).resolveSemanticElement() instanceof ExecutionSpecification)) {

			ExecutionSpecification exec = (ExecutionSpecification) ((AbstractExecutionSpecificationEditPart) getHost()).resolveSemanticElement();
			// manage Strong references
			fillStrongReferencesOfExecutionSpecification(exec);

			// manage weak references
			// the weak reference is the next element associated to next event after the finish event.
			if (exec.getCovereds().size() == 1) {
				Lifeline currentLifeline = exec.getCovereds().get(0);
				fillWeakReferenceForExecSpec(exec.getFinish(), currentLifeline);
			}
		}
		// management of messages
		if (getHost() instanceof AbstractMessageEditPart && (((AbstractMessageEditPart) getHost()).resolveSemanticElement() instanceof Message)) {
			Message aMessage = (Message) ((AbstractMessageEditPart) getHost()).resolveSemanticElement();

			// manage Strong references
			// fill about source
			MessageEnd sourceEvent = aMessage.getSendEvent();
			if (sourceEvent instanceof OccurrenceSpecification) {
				addExecutionSpecIntoReferences((OccurrenceSpecification) sourceEvent, strongReferences, ROLE_SOURCE);
			}
			// fill about target
			MessageEnd targetEvent = aMessage.getReceiveEvent();
			if (targetEvent instanceof OccurrenceSpecification) {
				addExecutionSpecIntoReferences((OccurrenceSpecification) targetEvent, strongReferences, ROLE_TARGET);
			}

			// manage weakReferences
			// source
			if (sourceEvent instanceof OccurrenceSpecification && ((OccurrenceSpecification) sourceEvent).getCovereds().size() == 1) {
				Lifeline currentLifeline = ((OccurrenceSpecification) sourceEvent).getCovereds().get(0);
				fillWeakReference(((OccurrenceSpecification) sourceEvent), currentLifeline);
			}
			// target
			if (targetEvent instanceof OccurrenceSpecification && ((OccurrenceSpecification) targetEvent).getCovereds().size() == 1) {
				Lifeline currentLifeline = ((OccurrenceSpecification) targetEvent).getCovereds().get(0);
				fillWeakReference(((OccurrenceSpecification) targetEvent), currentLifeline);
			}
		}


	}

	/**
	 * This method is used to add a weak reference from the next event after the given event
	 *
	 * @param sourceEvent
	 *            the given event the next is maybe an element to add in the weak reference.
	 * @param currentLifeline,
	 *            the life line where we look for the next event.
	 */
	protected void fillWeakReference(OccurrenceSpecification sourceEvent, Lifeline currentLifeline) {
		Element nextEvent = getNextEventFromLifeline(currentLifeline, sourceEvent);
		if (!isCoveredByStronReference(nextEvent)) {
			if (nextEvent instanceof MessageOccurrenceSpecification && isOnlyMessageEnd((MessageOccurrenceSpecification) nextEvent)) {
				addMessageIntoReferences((MessageEnd) nextEvent, weakReferences, NO_ROLE);
			} else if (nextEvent instanceof OccurrenceSpecification) {
				addExecutionSpecIntoReferences((OccurrenceSpecification) nextEvent, weakReferences, NO_ROLE);
			}
		}
	}

	/**
	 * This method is used to add a weak reference from the next event after the given event
	 *
	 * @param sourceEvent
	 *            the given event the next is maybe an element to add in the weak reference.
	 * @param currentLifeline,
	 *            the life line where we look for the next event.
	 */
	protected void fillWeakReferenceForExecSpec(OccurrenceSpecification sourceEvent, Lifeline currentLifeline) {
		Element nextEvent = getNextEventFromLifeline(currentLifeline, sourceEvent);
		if (!isCoveredByStronReference(nextEvent)) {
			if (nextEvent instanceof MessageOccurrenceSpecification && isOnlyMessageEnd((MessageOccurrenceSpecification) nextEvent)) {
				addMessageIntoReferences((MessageEnd) nextEvent, weakReferences, NO_ROLE);
			} else if (nextEvent instanceof OccurrenceSpecification) {
				ExecutionSpecification subExecutionSpec = getExecutionSpecificationAssociatedToEvent((OccurrenceSpecification) nextEvent);
				if (null != subExecutionSpec && !subExecutionSpec.getFinish().equals(nextEvent)) {
					addExecutionSpecIntoReferences((OccurrenceSpecification) nextEvent, weakReferences, NO_ROLE);
				}
			}
		}
	}

	/**
	 * this method is used to get the execution specification associated to the message End
	 *
	 * @param event
	 *            the given event where we look for a executionSpecification, must be never null
	 * @return the associated execution specification or null if there is no association
	 */
	protected ExecutionSpecification getExecutionSpecificationAssociatedToEvent(OccurrenceSpecification event) {
		ExecutionSpecification exec = null;
		if (null != event && !event.getCovereds().isEmpty()) {
			Lifeline currentLifeline = event.getCovereds().get(0);
			int index = 0;
			while (exec == null && index < currentLifeline.getCoveredBys().size()) {
				if (currentLifeline.getCoveredBys().get(index) instanceof ExecutionSpecification) {
					ExecutionSpecification currentExec = (ExecutionSpecification) currentLifeline.getCoveredBys().get(index);
					if (event.equals(currentExec.getStart())) {
						exec = currentExec;
					}
					if (event.equals(currentExec.getFinish())) {
						exec = currentExec;
					}
				}
				index++;
			}
		}
		return exec;
	}

	/**
	 * this method returns the next events after the given event
	 *
	 * @param event
	 *            we look for the next event after this one.
	 * @param currentLifeline
	 *            the current lifeline where we look for the covered element
	 * @return null if there is not event after the given event or the next.
	 */
	protected Element getNextEventFromLifeline(Lifeline currentLifeline, Object event) {
		int index = currentLifeline.getCoveredBys().indexOf(event);
		Element nextEvent = null;
		if (index != -1) {
			// we look for the next event
			index = index + 1;
			while (nextEvent == null && (index < currentLifeline.getCoveredBys().size())) {
				if (currentLifeline.getCoveredBys().get(index) instanceof MessageEnd ||
						currentLifeline.getCoveredBys().get(index) instanceof OccurrenceSpecification) {
					nextEvent = currentLifeline.getCoveredBys().get(index);
				}
				index++;
			}
		}
		return nextEvent;
	}


	public boolean isCoveredByStronReference(Element event) {
		for (Iterator<EditPart> iterator = getStrongReferences().keySet().iterator(); iterator.hasNext();) {
			EditPart editPart = iterator.next();
			if (editPart instanceof AbstractMessageEditPart) {
				Message message = (Message) (((AbstractMessageEditPart) editPart)).resolveSemanticElement();
				if (message.getSendEvent().equals(event)) {
					return true;
				}
				if (message.getReceiveEvent().equals(event)) {
					return true;
				}
			}
			if (editPart instanceof AbstractExecutionSpecificationEditPart) {
				ExecutionSpecification exec = (ExecutionSpecification) (((AbstractExecutionSpecificationEditPart) editPart)).resolveSemanticElement();
				if (exec.getStart().equals(event)) {
					return true;
				}
				if (exec.getFinish().equals(event)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * given a messageEnd, the corresponding editPart to a message is adding to the references list
	 *
	 * @param messageEnd
	 *            a messageEnd
	 * @param referenceList
	 *            the list of references
	 */
	protected void addMessageIntoReferences(MessageEnd messageEnd, HashMap<EditPart, String> referenceList, String role) {
		if (messageEnd.getMessage() != null) {
			IGraphicalEditPart resultedEditPart = getEditPartFromSemantic(messageEnd.getMessage());
			if (resultedEditPart != null) {
				if (referenceList == strongReferences) {
					referenceList.put(resultedEditPart, role);
				} else if (referenceList == weakReferences && !(strongReferences.containsKey(resultedEditPart))) {
					referenceList.put(resultedEditPart, role);
				}
			}
		}
	}


	/**
	 * test if this event is only use of a message not also for an executionSpecification
	 *
	 * @param messageEnd
	 * @return true if the message End is only used by a message
	 */
	protected boolean isOnlyMessageEnd(MessageOccurrenceSpecification messageEnd) {
		InteractionFragment owner = (InteractionFragment) messageEnd.getOwner();
		if (owner != null) {
			for (Element fragment : owner.getOwnedElements()) {
				if (fragment instanceof ExecutionSpecification) {
					if (messageEnd.equals(((ExecutionSpecification) (fragment)).getStart())) {
						return false;
					}
					if (messageEnd.equals(((ExecutionSpecification) (fragment)).getFinish())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * given a messageEnd, the corresponding editPart to a ExecutionSpec is added to the references list
	 *
	 * @param messageEnd
	 *            a messageEnd
	 * @param referenceList
	 *            the list of references
	 */
	protected void addExecutionSpecIntoReferences(OccurrenceSpecification sourceEvent, HashMap<EditPart, String> referenceList, String role) {
		ExecutionSpecification executionSpec = getExecutionSpecificationAssociatedToEvent(sourceEvent);
		if (executionSpec != null) {
			IGraphicalEditPart resultedEditPart = getEditPartFromSemantic(executionSpec);
			if (referenceList == strongReferences) {
				if (resultedEditPart != null) {
					if (executionSpec.getStart().equals(sourceEvent)) {
						referenceList.put(resultedEditPart, ROLE_START);
					} else {
						referenceList.put(resultedEditPart, ROLE_FINISH);
					}
				}
			} else if (referenceList == weakReferences && !(strongReferences.containsKey(resultedEditPart))) {
				if (resultedEditPart != null) {
					if (executionSpec.getStart().equals(sourceEvent)) {
						referenceList.put(resultedEditPart, ROLE_START);
					} else {
						referenceList.put(resultedEditPart, ROLE_FINISH);
					}
				}
			}
		}

	}

	/**
	 * this method return the controller attached to the semantic element
	 * the complexity of this algorithm is N (N the number of controller in the opened sequence diagram)
	 *
	 * @param semanticElement
	 *            must be different from null
	 * @return the reference to the controller or null.
	 */
	protected IGraphicalEditPart getEditPartFromSemantic(Object semanticElement) {
		IGraphicalEditPart researchedEditPart = null;
		SemanticFromGMFElement semanticFromGMFElement = new SemanticFromGMFElement();
		EditPartViewer editPartViewer = getHost().getViewer();
		if (editPartViewer != null) {
			// look for all edit part if the semantic is contained in the list
			Iterator<?> iter = editPartViewer.getEditPartRegistry().values().iterator();

			while (iter.hasNext() && researchedEditPart == null) {
				Object currentEditPart = iter.next();
				// look only amidst IPrimary editpart to avoid compartment and labels of links
				if (currentEditPart instanceof IPrimaryEditPart) {
					Object currentElement = semanticFromGMFElement.getSemanticElement(currentEditPart);
					if (semanticElement.equals(currentElement)) {
						researchedEditPart = ((IGraphicalEditPart) currentEditPart);
					}
				}
			}
		}
		return researchedEditPart;
	}



	/**
	 * @see org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateStrongAndWeakReferences();
	}


	/**
	 * get the current Interaction from the editpart
	 *
	 * @return null or the interaction
	 */
	protected Interaction getInteraction() {
		if (getHost() instanceof AbstractExecutionSpecificationEditPart && (((AbstractExecutionSpecificationEditPart) getHost()).resolveSemanticElement() instanceof ExecutionSpecification)) {

			ExecutionSpecification exec = (ExecutionSpecification) ((AbstractExecutionSpecificationEditPart) getHost()).resolveSemanticElement();
			return exec.getEnclosingInteraction();
		}
		if (getHost() instanceof AbstractMessageEditPart && (((AbstractMessageEditPart) getHost()).resolveSemanticElement() instanceof Message)) {
			Message aMessage = (Message) ((AbstractMessageEditPart) getHost()).resolveSemanticElement();
			return aMessage.getInteraction();
		}
		return null;
	}

	/**
	 * This allows to fill the string references of an execution specification with:
	 * - The message end of start and/or finish
	 * - Execution specifications defined between the start and the finish of the current execution specification
	 *
	 * @param exec
	 *            The current execution specification.
	 */
	protected void fillStrongReferencesOfExecutionSpecification(final ExecutionSpecification exec) {

		// The message end of start and/or finish of the execution specification are managed as strong references
		if (exec.getStart() instanceof MessageEnd) {
			MessageEnd messageEnd = (MessageEnd) exec.getStart();
			addMessageIntoReferences(messageEnd, strongReferences, ROLE_START);
		}
		if (exec.getFinish() instanceof MessageEnd) {
			MessageEnd messageEnd = (MessageEnd) exec.getFinish();
			addMessageIntoReferences(messageEnd, strongReferences, ROLE_FINISH);
		}

		// If an execution specification is defined between the start and the finish of the current execution specification, it will be defined as strong reference
		if (exec.getCovereds().size() >= 1) {
			final Lifeline currentLifeline = exec.getCovereds().get(0);
			int index = currentLifeline.getCoveredBys().indexOf(exec);
			Element nextEvent = null;
			OccurrenceSpecification foundSubExecSpec = null;
			if (index != -1) {
				// we look for the next event
				index = index + 1;
				while ((nextEvent != exec.getFinish() || foundSubExecSpec == null) && (index < currentLifeline.getCoveredBys().size())) {
					nextEvent = currentLifeline.getCoveredBys().get(index);
					if (nextEvent instanceof OccurrenceSpecification) {
						ExecutionSpecification subExecutionSpec = getExecutionSpecificationAssociatedToEvent((OccurrenceSpecification) nextEvent);
						if (null != subExecutionSpec && null != subExecutionSpec.getStart() && !subExecutionSpec.equals(exec) && subExecutionSpec.getStart().equals(nextEvent)) {
							foundSubExecSpec = (OccurrenceSpecification) nextEvent;
							addExecutionSpecIntoReferences(foundSubExecSpec, strongReferences, ROLE_START);
						}
					}
					index++;
				}
			}
		}
	}
}