/*****************************************************************************
 * Copyright (c) 2016, 2018 CEA LIST, ALL4TEC, Christian W. Damus, and others.
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
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Bug 519756
 *   Vincent Lorenzo (CEA LIST) vincent.lorenzo@cea.fr - Bug 531936
 *   Christian W. Damus - bugs 533679, 530201
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.diagram.core.listener.DiagramEventBroker;
import org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.gef.ui.internal.editpolicies.GraphicalEditPolicyEx;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.DecorationNode;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.utils.OneShotExecutor;
import org.eclipse.papyrus.infra.core.utils.TransactionHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.AutomaticNotationEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramEditPartsUtil;
import org.eclipse.papyrus.uml.diagram.sequence.command.CreateCoordinateCommand;
import org.eclipse.papyrus.uml.diagram.sequence.command.CreateGrillingStructureCommand;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.LogOptions;
import org.eclipse.papyrus.uml.diagram.sequence.util.RedirectionOperationListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * This edit policy is used to manage the referential grid of the sequence diagram.
 */
public class GridManagementEditPolicy extends GraphicalEditPolicyEx implements AutomaticNotationEditPolicy, NotificationListener, IGrillingEditpolicy {
	public static final String GRID_CONNECTION = "Grid Connection"; //$NON-NLS-1$
	protected GrillingEditpart gridCompartment = null;


	public static String GRID_MANAGEMENT = "GRID_MANAGEMENT"; //$NON-NLS-1$
	public static String COLUMN = "COLUMN_"; //$NON-NLS-1$
	public static String ROW = "ROW_"; //$NON-NLS-1$

	public static int threshold = 5;

	private Executor transactionExecutor;
	private Executor coveredUpdateExecutor;

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(int threshold) {
		GridManagementEditPolicy.threshold = threshold;
	}

	public int margin = 50;
	public boolean respectMargin = true;
	public boolean moveAllLinesAtSamePosition = false;

	public ArrayList<DecorationNode> rows = new ArrayList<>();
	public ArrayList<DecorationNode> columns = new ArrayList<>();

	// ok if the creation a X is free
	public boolean CREATION_X_FREE = true;
	/** if the CREATION_X_FREE == false COLUMN are created at fixed position **/
	public int X_SPACING = 100;
	// private ContentDiagramListener contentDiagramListener;
	// private CommandStackListener commandStackListener;

	/**
	 * @return the moveAllLinesAtSamePosition
	 */
	public boolean isMoveAllLinesAtSamePosition() {
		return moveAllLinesAtSamePosition;
	}

	/**
	 * @param moveAllLinesAtSamePosition
	 *            the moveAllLinesAtSamePosition to set
	 */
	public void setMoveAllLinesAtSamePosition(boolean moveAllLinesAtSamePosition) {

		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, ">> set moveAllLinesAtSamePosition=" + moveAllLinesAtSamePosition);//$NON-NLS-1$
		this.moveAllLinesAtSamePosition = moveAllLinesAtSamePosition;
	}

	/**
	 * @return the respectMargin
	 */
	public boolean isRespectMargin() {
		return respectMargin;
	}

	/**
	 * @param respectMargin
	 *            the respectMargin to set
	 */
	public void setRespectMargin(boolean respectMargin) {
		this.respectMargin = respectMargin;
	}



	public boolean strictRespectMargin = true;




	public Comparator<DecorationNode> RowComparator = new Comparator<DecorationNode>() {

		@Override
		public int compare(DecorationNode o1, DecorationNode o2) {
			LayoutConstraint layoutConstrainto1 = ((Node) o1).getLayoutConstraint();
			LayoutConstraint layoutConstrainto2 = ((Node) o2).getLayoutConstraint();
			if (layoutConstrainto1 != null && layoutConstrainto2 != null) {
				return ((Integer) ((Location) layoutConstrainto1).getY()).compareTo((((Location) layoutConstrainto2).getY()));
			}
			return 0;
		}
	};

	public Comparator<DecorationNode> ColumnComparator = new Comparator<DecorationNode>() {

		@Override
		public int compare(DecorationNode o1, DecorationNode o2) {
			LayoutConstraint layoutConstrainto1 = ((Node) o1).getLayoutConstraint();
			LayoutConstraint layoutConstrainto2 = ((Node) o2).getLayoutConstraint();
			if (layoutConstrainto1 != null && layoutConstrainto2 != null) {
				return ((Integer) ((Location) layoutConstrainto1).getX()).compareTo((((Location) layoutConstrainto2).getX()));
			}
			return 0;
		}
	};
	protected RedirectionOperationListener operationHistoryListener;




	/**
	 * Constructor.
	 *
	 */
	public GridManagementEditPolicy() {
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 *
	 */
	@Override
	public void activate() {
		super.activate();

		transactionExecutor = TransactionHelper.createTransactionExecutor(
				((IGraphicalEditPart) getHost()).getEditingDomain(),
				Display.getCurrent()::asyncExec);
		coveredUpdateExecutor = new OneShotExecutor(transactionExecutor);

		getDiagramEventBroker().addNotificationListener(((EObject) getHost().getModel()), this);

		// contentDiagramListener = new ContentDiagramListener(this);
		// commandStackListener = new GridCommandStackListener(this);

		this.operationHistoryListener = new RedirectionOperationListener(this);
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this.operationHistoryListener);
		// ((EObject) getHost().getModel()).eResource().eAdapters().add(contentDiagramListener);

		// getDiagramEditPart(getHost()).getEditingDomain().getCommandStack().addCommandStackListener(commandStackListener);
		// PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyDown, new KeyToSetMoveLinesListener(this, SWT.SHIFT, false));
		// PlatformUI.getWorkbench().getDisplay().addFilter(SWT.KeyUp, new KeyToSetMoveLinesListener(this, SWT.SHIFT, true));
		refreshGrillingStructure();
	}

	/**
	 *
	 */
	private void refreshGrillingStructure() {
		EditPart host = getHost();
		int i = 0;
		while (gridCompartment == null && i < host.getChildren().size()) {
			if (host.getChildren().get(i) instanceof GrillingEditpart) {
				gridCompartment = (GrillingEditpart) (host.getChildren().get(i));
			}
			i++;
		}
		if (gridCompartment == null) {
			CreateGrillingStructureCommand createGrillingStructureCommand = new CreateGrillingStructureCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), (View) getHost().getModel());
			// Record for undo if possible, otherwise unprotected
			execute(createGrillingStructureCommand);


		}
		while (gridCompartment == null && i < host.getChildren().size()) {
			if (host.getChildren().get(i) instanceof GrillingEditpart) {
				gridCompartment = (GrillingEditpart) (host.getChildren().get(i));
			}
			i++;
		}
		// cleanUnusedRowAndColumn();
		postRowColumnCoverageUpdate();
	}

	/**
	 * update the list of romw and colmumn
	 */
	public void updateRowsAndColumns() {
		rows.clear();
		columns.clear();
		if (gridCompartment != null) {
			for (int j = 0; j < gridCompartment.getNotationView().getChildren().size(); j++) {
				if (gridCompartment.getNotationView().getChildren().get(j) instanceof DecorationNode) {
					DecorationNode decorationNode = (DecorationNode) gridCompartment.getNotationView().getChildren().get(j);
					if (decorationNode.getType().startsWith(ROW)) {
						rows.add(decorationNode);

					}
					if (decorationNode.getType().startsWith(COLUMN)) {
						columns.add(decorationNode);
					}
				}
			}
		}
		Collections.sort(rows, RowComparator);
		Collections.sort(columns, ColumnComparator);
	}

	/**
	 * this class is very specific the the sequence diagram
	 * this purpose of this method is to ensure the consistency of event in the the represented diagram
	 **/
	public void updateCoveredAndOwnerAfterUpdate() {
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "____UPDATE COVERED_____");//$NON-NLS-1$

		updateCoveredBy();
		IComputeOwnerHelper computeOwner = new ComputeOwnerHelper();
		computeOwner.updateOwnedByInteractionOperand(((IGraphicalEditPart) getHost()).getEditingDomain(), rows, columns, (Interaction) ((IGraphicalEditPart) getHost()).resolveSemanticElement(), this);
	}

	protected void updateCoveredBy() {
		HashSet<Lifeline> lifelineList = new HashSet<>();
		for (DecorationNode column : columns) {
			if ((column.getElement()) instanceof Lifeline) {
				lifelineList.add((Lifeline) (column.getElement()));
			}

		}
		UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "--> there is " + lifelineList.size() + " lifelines");//$NON-NLS-1$

		// for each lifeline recreat the list of covered element
		for (Lifeline lifeline : lifelineList) {
			ArrayList<InteractionFragment> covered = new ArrayList<>();
			for (DecorationNode row : rows) {
				if (row.getElement() instanceof InteractionFragment) {
					InteractionFragment interactionFragment = (InteractionFragment) (row.getElement());
					if (lifeline.getCoveredBys().contains(interactionFragment)) {
						if (!covered.contains(interactionFragment)) {
							covered.add(interactionFragment);
							if (interactionFragment instanceof ExecutionOccurrenceSpecification) {
								if (!covered.contains(((ExecutionOccurrenceSpecification) interactionFragment).getExecution())) {
									covered.add(((ExecutionOccurrenceSpecification) interactionFragment).getExecution());
								}
							}
						}
					}
				}
			}

			// update the list of covered by taking account InteractionFragment
			if (covered.size() == lifeline.getCoveredBys().size()) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "the list is equals" + covered.size() + ", we reorder");//$NON-NLS-1$
				execute(new SetCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), lifeline, UMLPackage.eINSTANCE.getLifeline_CoveredBy(), covered));
			} else if (covered.size() < lifeline.getCoveredBys().size()) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "More event detected! +" + (covered.size() - lifeline.getCoveredBys().size()) + "--> modify covered");//$NON-NLS-1$
				covered.addAll(lifeline.getCoveredBys());
				execute(new SetCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), lifeline, UMLPackage.eINSTANCE.getLifeline_CoveredBy(), covered));
			} else if (covered.size() > lifeline.getCoveredBys().size()) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "problem! normaly event must be added by element types -" + (covered.size() - lifeline.getCoveredBys().size()));//$NON-NLS-1$
			}
			// 3. management of InteractionOperand
			// There are columns.

			ArrayList<InteractionOperand> coveredbyInteractionOperand = new ArrayList<>();
			// covered = new ArrayList<>();
			for (DecorationNode column : columns) {
				if (column.getElement() instanceof InteractionOperand) {
					// Except for the initial operand creation, coverage is managed
					// explicitly by the user
					continue;
				}
				if (column.getElement().equals(lifeline)) {
					covered.addAll(coveredbyInteractionOperand);
				}

			}

			// 4. put the combined fragment before the owner of the first interactionOperand
			// remove all CombiendFragment from this list
			{
				int index = 0;
				while (index < covered.size()) {
					if (covered.get(index) instanceof CombinedFragment) {
						covered.remove(index);
					} else {
						index++;
					}
				}
			}
			// remove CF owner before the first interactionOperand
			int index = 0;
			while (index < covered.size()) {
				if (covered.get(index) instanceof InteractionOperand) {
					if (covered.get(index - 1) instanceof CombinedFragment) {
						index++;

					} else {
						CombinedFragment cf = (CombinedFragment) ((InteractionOperand) covered.get(index)).eContainer();
						covered.add(index, cf);
						index++;
					}
				}
				index++;
			}

			if (covered.size() > 0) {
				UMLDiagramEditorPlugin.log.trace(LogOptions.SEQUENCE_DEBUG_REFERENCEGRID, "Add Interraction operand");//$NON-NLS-1$
				covered.addAll(lifeline.getCoveredBys());
				execute(new SetCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), lifeline, UMLPackage.eINSTANCE.getLifeline_CoveredBy(), covered));

			}

		}
	}


	/**
	 * Gets the diagram event broker from the editing domain.
	 *
	 * @return the diagram event broker
	 */
	protected DiagramEventBroker getDiagramEventBroker() {
		TransactionalEditingDomain theEditingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
		if (null != theEditingDomain) {
			return DiagramEventBroker.getInstance(theEditingDomain);
		}
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#deactivate()
	 *
	 */
	@Override
	public void deactivate() {
		getDiagramEventBroker().removeNotificationListener(((EObject) getHost().getModel()), this);
		TransactionHelper.disposeTransactionExecutor(transactionExecutor);
		transactionExecutor = null;
		coveredUpdateExecutor = null;

		if (null != this.operationHistoryListener) {
			OperationHistoryFactory.getOperationHistory().removeOperationHistoryListener(this.operationHistoryListener);
		}
		super.deactivate();
	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.core.listener.NotificationListener#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notifications
	 */
	@Override
	public void notifyChanged(Notification notification) {
		postRowColumnCoverageUpdate();
	}

	private void postRowColumnCoverageUpdate() {
		Runnable update = () -> {
			updateRowsAndColumns();
			updateCoveredAndOwnerAfterUpdate();
		};

		if (coveredUpdateExecutor == null) {
			update.run();
		} else {
			coveredUpdateExecutor.execute(update);
		}
	}

	/**
	 * get the decoration node that represents a column from a position (absolute)
	 *
	 * @param x
	 *            the position x for the column
	 * @return the decoration node
	 */
	public DecorationNode createColumnTolisten(int x, Element semantic) throws NoGrillElementFound {
		execute(new CreateCoordinateCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), ((BasicCompartment) gridCompartment.getNotationView()), COLUMN + columns.size(), semantic, x));
		refreshGrillingStructure();
		return getLastCreatedAxis();
	}

	/**
	 * get the decoration node that represents a line from a position (absolute)
	 *
	 * @param y
	 *            the position y for the line
	 * @return the decoration node
	 */
	public DecorationNode createRowTolisten(int y, Element semantic) throws NoGrillElementFound {
		execute(new CreateCoordinateCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), ((BasicCompartment) gridCompartment.getNotationView()), ROW + rows.size(), semantic, y));
		DecorationNode row = getLastCreatedAxis();
		refreshGrillingStructure();
		return row;

	}

	/**
	 * @return get the last created Axis
	 **/
	public DecorationNode getLastCreatedAxis() throws NoGrillElementFound {
		BasicCompartment grid = ((BasicCompartment) gridCompartment.getNotationView());
		if (grid.getChildren().size() == 0) {
			throw new NoGrillElementFound();
		} else {
			return (DecorationNode) grid.getChildren().get(grid.getChildren().size() - 1);
		}

	}


	/**
	 *
	 * @param y
	 *            the position y where we look for a row
	 * @return the rows that exists at the position [y- threshold, y+threshold]s
	 */
	public ArrayList<DecorationNode> getRowAtPosition(int y) {
		ArrayList<DecorationNode> sameLines = new ArrayList<>();
		for (Iterator<DecorationNode> iterator = rows.iterator(); iterator.hasNext();) {
			DecorationNode currentRow = iterator.next();
			int Yposition = getPositionY(currentRow);
			if (Yposition - threshold <= y && y <= Yposition + threshold) {
				sameLines.add(currentRow);
			}

		}
		return sameLines;
	}

	/**
	 * @param decorationNode
	 * @return the Position Y for a decoration node
	 */
	public int getPositionY(DecorationNode decorationNode) {
		LayoutConstraint constraint = decorationNode.getLayoutConstraint();
		if (constraint instanceof Location) {
			return ((Location) constraint).getY();
		}
		return 0;
	}


	protected void updateYpositionForRow(DecorationNode movedRow, int oldPosition) {
		LayoutConstraint newconstraint = movedRow.getLayoutConstraint();
		DecorationNode nextRow = getDistanceWithNextRowBeforeMoving(movedRow, oldPosition);
		if (nextRow == null) {
			return;
		}
		LayoutConstraint nextConstraint = nextRow.getLayoutConstraint();
		int nextDistance = ((Location) nextConstraint).getY() - ((Location) newconstraint).getY();
		int margin = getGridSpacing();
		if (nextDistance < margin) {
			ArrayList<DecorationNode> rowsCopy = new ArrayList<>();
			rowsCopy.addAll(rows);
			for (int i = rowsCopy.indexOf(nextRow); i < rowsCopy.size(); i++) {
				if (!(rowsCopy.get(i).equals(movedRow))) {
					LayoutConstraint aConstraint = rowsCopy.get(i).getLayoutConstraint();
					if (aConstraint instanceof Location) {
						// do not move row connected to interaction operand
						if ((!(rowsCopy.get(i).getElement() instanceof InteractionOperand))) {

							execute(new SetCommand(((IGraphicalEditPart) getHost()).getEditingDomain(), aConstraint, NotationPackage.eINSTANCE.getLocation_Y(), ((Location) aConstraint).getY() + margin));
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * @param currentRow
	 * @param currentRowPosition
	 * @return get the next row that has not the same position
	 */
	protected DecorationNode getDistanceWithNextRowBeforeMoving(DecorationNode currentRow, int oldPosition) {
		Object[] arrayRow = rows.toArray();
		List<Object> orderedRows = Arrays.asList(arrayRow);
		DecorationNode nextRow = null;
		// look for the next row
		for (Iterator<DecorationNode> iterator = rows.iterator(); iterator.hasNext();) {
			DecorationNode aRow = iterator.next();
			int Yposition = getPositionY(aRow);
			if (oldPosition < Yposition && (!aRow.equals(currentRow))) {
				nextRow = aRow;
				return nextRow;
			}

		}
		return nextRow;

	}

	protected int getGridSpacing() {
		if (DiagramEditPartsUtil.isSnapToGridActive(getHost())) {

			RootEditPart drep = getHost().getRoot();
			if (drep instanceof DiagramRootEditPart) {
				return (int) ((DiagramRootEditPart) drep).getGridSpacing();
			}
		}
		return margin;
	}

	public static Point getLocation(DecorationNode current) throws NoGrillElementFound {
		LayoutConstraint currentConstraint = current.getLayoutConstraint();
		if (currentConstraint instanceof Location) {
			return new Point(((Location) currentConstraint).getX(), ((Location) currentConstraint).getY());
		} else {
			throw new NoGrillElementFound();
		}
	}

}
