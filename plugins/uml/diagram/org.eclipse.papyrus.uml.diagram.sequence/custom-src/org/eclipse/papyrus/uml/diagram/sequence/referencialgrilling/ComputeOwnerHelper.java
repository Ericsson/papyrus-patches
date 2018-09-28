/*****************************************************************************
 * Copyright (c) 2017, 2018 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 525369
 *   Christian W. Damus - bugs 533679, 507479
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import static org.eclipse.papyrus.uml.diagram.sequence.util.ExecutionSpecificationUtil.getStartedExecution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.validation.AsyncValidateCommand;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This class is a basic class to compute owners.
 *
 * @since 3.0
 */
public class ComputeOwnerHelper implements IComputeOwnerHelper {

	protected static void fillHorizontalMatch(ArrayList<DecorationNode> columns, HashMap<Lifeline, ArrayList<InteractionOperand>> HorizontalLifeLinetoOperand) {
		ArrayList<InteractionOperand> interactionOperandStack = new ArrayList<>();
		for (DecorationNode column : columns) {
			if (column.getElement() instanceof InteractionOperand) {
				if (interactionOperandStack.contains(column.getElement())) {
					UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "update " + ((InteractionOperand) column.getElement()).getName());//$NON-NLS-1$
					interactionOperandStack.remove(column.getElement());
				} else {
					interactionOperandStack.add((InteractionOperand) column.getElement());
				}
			}
			if (column.getElement() instanceof Lifeline) {
				HorizontalLifeLinetoOperand.put((Lifeline) column.getElement(), new ArrayList<>(interactionOperandStack));
			}


		}
	}


	protected static void fillVerticalMatch(ArrayList<DecorationNode> rows, Map<Element, ArrayList<InteractionOperand>> verticalElementToOperand) {
		ArrayList<InteractionOperand> interactionOperandStack = new ArrayList<>();
		for (DecorationNode row : rows) {
			if (row.getElement() instanceof InteractionOperand) {
				InteractionOperand operand = (InteractionOperand) row.getElement();
				if (interactionOperandStack.remove(operand)) { // End of Operand
					UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "update " + ((InteractionOperand) row.getElement()).getName());//$NON-NLS-1$
					continue;
				} else { // Start of Operand
					CombinedFragment fragment = getOwningFragment(operand);
					if (fragment != null && !fragment.getOperands().isEmpty() && fragment.getOperands().get(0) == operand) {
						// Order the CombinedFragment at the location of its first operand
						verticalElementToOperand.put(fragment, new ArrayList<>(interactionOperandStack));
					}
					interactionOperandStack.add(operand);
				}
			} else if (row.getElement() instanceof Element) {
				verticalElementToOperand.put((Element) row.getElement(), new ArrayList<>(interactionOperandStack));
			}
		}
	}

	@Override
	public void updateOwnedByInteractionOperand(EditingDomain domain, ArrayList<DecorationNode> rows, ArrayList<DecorationNode> columns, Interaction interaction, GridManagementEditPolicy grid) {
		// update owner of interaction operand

		HashMap<Lifeline, ArrayList<InteractionOperand>> horizontalLifeLinetoOperand = new HashMap<>();
		HashMap<Element, ArrayList<InteractionOperand>> verticalElementToOperand = new HashMap<>();
		fillHorizontalMatch(columns, horizontalLifeLinetoOperand);
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "horizontal parsing done " + horizontalLifeLinetoOperand);//$NON-NLS-1$
		fillVerticalMatch(rows, verticalElementToOperand);
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "vertical parsing done " + verticalElementToOperand);//$NON-NLS-1$

		// list of element for the interaction

		ArrayList<InteractionFragment> elementForInteraction = new ArrayList<>();
		// list of element for the interactionOperand
		HashMap<InteractionOperand, ArrayList<InteractionFragment>> elementForInteractionOp = new HashMap<>();
		Iterator<EObject> elementInteraction = interaction.eAllContents();
		while (elementInteraction.hasNext()) {
			Element element = (Element) elementInteraction.next();
			if (element instanceof InteractionFragment) {
				InteractionFragment aFragment = (InteractionFragment) element;
				if (verticalElementToOperand.containsKey(aFragment)) {
					for (Lifeline currentLifeline : horizontalLifeLinetoOperand.keySet()) {
						if (currentLifeline.getCoveredBys().contains(aFragment)) {
							List<InteractionOperand> potentialoperand = intersection(verticalElementToOperand.get(aFragment), horizontalLifeLinetoOperand.get(currentLifeline));
							if (potentialoperand.size() >= 1) {
								simplifyOwnerInteractionOperand(potentialoperand);

								InteractionOperand lastOperand = potentialoperand.get(potentialoperand.size() - 1);

								if (aFragment instanceof InteractionOperand) {
									CombinedFragment owningFragment = getOwningFragment((InteractionOperand) aFragment);
									if (owningFragment != null) {
										aFragment = owningFragment;
									}
								}
								elementForInteractionOp.computeIfAbsent(lastOperand, f -> new ArrayList<>()).add(aFragment);
								if (aFragment instanceof OccurrenceSpecification) {
									Optional<ExecutionSpecification> exec = getStartedExecution((OccurrenceSpecification) aFragment);
									exec.ifPresent(elementForInteractionOp.get(lastOperand)::add);
								}
							} else {
								if (!(aFragment instanceof InteractionOperand)) {
									elementForInteraction.add(aFragment);
									if (aFragment instanceof ExecutionOccurrenceSpecification) {
										Optional<ExecutionSpecification> exec = getStartedExecution((OccurrenceSpecification) aFragment);
										exec.ifPresent(elementForInteraction::add);
									}
								}
							}
						}
					}
				} else {
					if (!(aFragment instanceof InteractionOperand)) {
						elementForInteraction.add(aFragment);
						if (aFragment instanceof ExecutionOccurrenceSpecification) {
							Optional<ExecutionSpecification> exec = getStartedExecution((OccurrenceSpecification) aFragment);
							exec.ifPresent(elementForInteraction::add);
						}
					}
				}
			}
		}

		// update fragments of interaction operrands
		Iterator<InteractionOperand> iterator = elementForInteractionOp.keySet().iterator();
		while (iterator.hasNext()) {
			InteractionOperand interactionOperand = iterator.next();
			ArrayList<InteractionFragment> elements = elementForInteractionOp.get(interactionOperand);
			if (elements.size() != 0) {
				// sort list bu taking
				ArrayList<InteractionFragment> existedFragments = new ArrayList<>();
				ArrayList<InteractionFragment> sorted = sortSemanticFromRows(elements, rows);
				existedFragments.addAll(sorted);
				existedFragments.addAll(interactionOperand.getFragments());
				grid.execute(new SetCommand(domain, interactionOperand, UMLPackage.eINSTANCE.getInteractionOperand_Fragment(), existedFragments));

				// Asynchronously re-validate the whole combined fragment in case of
				// dependencies between operands and the check for consistency between
				// lifeline coverage of the combined fragment as compared to the lifeline
				// coverage of the fragments of its operands
				Optional<CombinedFragment> cfrag = Optional.of(interactionOperand)
						.map(Element::getOwner).filter(CombinedFragment.class::isInstance)
						.map(CombinedFragment.class::cast);
				cfrag.flatMap(AsyncValidateCommand::get)
						.ifPresent(grid::execute);
			}
		}

		// Update fragments of the interaction
		if (elementForInteraction.size() != 0) {
			ArrayList<InteractionFragment> existedFragments = new ArrayList<>();
			ArrayList<InteractionFragment> sorted = sortSemanticFromRows(elementForInteraction, rows);
			existedFragments.addAll(sorted);
			// Add not sorted element existing into fragment
			for (InteractionFragment interactionFragment : interaction.getFragments()) {
				if (!existedFragments.contains(interactionFragment)) {
					if (interactionFragment instanceof ExecutionSpecification) {
						// if its an execution specification place it after the start
						int indexOfStartOS = existedFragments.indexOf(((ExecutionSpecification) interactionFragment).getStart());
						if (0 <= indexOfStartOS) {
							existedFragments.add(indexOfStartOS + 1, interactionFragment);
						} else {
							existedFragments.add(interactionFragment);
						}
					} else {
						// else add it to the end of the list
						existedFragments.add(interactionFragment);
					}
				}
			}
			grid.execute(new SetCommand(domain, interaction, UMLPackage.eINSTANCE.getInteraction_Fragment(), existedFragments));
		}
	}

	/**
	 * The goal is to create a new list of ordered fragment form a list of fragments by taking general order from rows
	 *
	 * @param fragments
	 *            a list of fragments
	 * @param rows
	 *            the general order of event
	 * @return an ordered list
	 */
	protected ArrayList<InteractionFragment> sortSemanticFromRows(ArrayList<InteractionFragment> fragments, ArrayList<DecorationNode> rows) {
		ArrayList<InteractionFragment> sortedList = new ArrayList<>();
		for (Iterator<DecorationNode> iteratorRow = rows.iterator(); iteratorRow.hasNext();) {
			DecorationNode row = iteratorRow.next();
			if (fragments.contains(row.getElement())) {
				if (!sortedList.contains(row.getElement())) {
					InteractionFragment fragment = (InteractionFragment) row.getElement();
					sortedList.add(fragment);

					if (fragment instanceof OccurrenceSpecification) {
						// These (often) aren't in the rows
						Optional<ExecutionSpecification> execSpec = getStartedExecution((OccurrenceSpecification) fragment);
						execSpec.ifPresent(exec -> {
							if (!sortedList.contains(exec)) {
								sortedList.add(exec);
							}
						});
					}
				}
			} else if (row.getElement() instanceof InteractionOperand) {
				// Sort the fragment owning the operand (The position of the CF is the position of its first operand)
				InteractionOperand operand = (InteractionOperand) row.getElement();
				CombinedFragment fragment = getOwningFragment(operand);
				if (fragment != null && fragments.contains(fragment) && !sortedList.contains(fragment)) {
					sortedList.add(fragment);
				}
			}
		}

		return sortedList;
	}

	private static CombinedFragment getOwningFragment(InteractionOperand operand) {
		if (operand.getOwner() instanceof CombinedFragment) {
			return (CombinedFragment) operand.getOwner();
		}
		return null;
	}

	/**
	 * simplify the list of interaction operand to find only one.
	 * all interaction operand in this list must have a relation owner-owned.
	 *
	 * @param operandList
	 */
	protected static void simplifyOwnerInteractionOperand(List<InteractionOperand> operandList) {
		/*
		 * while (operandList.size() > 1) {
		 *
		 * InteractionOperand last = operandList.get(operandList.size() - 1);
		 * EObject parent = last.eContainer();
		 * while (parent != null) {
		 * operandList.remove(parent);
		 * parent = parent.eContainer();
		 * }
		 * }
		 */
	}

	/**
	 * make the intersection of 2 lists
	 *
	 * @param list1
	 * @param list2
	 * @return
	 */
	protected static <T> ArrayList<T> intersection(List<T> list1, List<T> list2) {
		ArrayList<T> list = new ArrayList<>(list1);
		list.retainAll(list2);
		return list;
	}
}
