/*****************************************************************************
 * Copyright (c) 2018 CEA
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
 *   Vincent Lorenzo (CEA LIST) - vincent.lorenzo@cea.fr - Bug 531520 - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.migration;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.commands.SetConnectionAnchorsCommand;
import org.eclipse.gmf.runtime.draw2d.ui.figures.BaseSlidableAnchor;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Shape;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.infra.emf.gmf.command.EMFtoGMFCommandWrapper;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramReconciler;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;

/**
 * This reconciler does this stuff:
 * <ul>
 * <li>1.3.0 to 1.4.0 diagram version</li>
 * <li>use Lifeline as source/target for messages attached to BehaviorExecutionSpecification or to ActivityExecutionSpecification</li>
 * <li>recalculate the anchors location of retargeted message</li>
 * <li>bendpoints are really ignored</li>
 * </ul>
 *
 * @author Vincent LORENZO
 * @since 5.0
 */
public class RetargetMessageEndFromExecutionSpecificationToLifelineDiagramReconciler_1_4_0 extends DiagramReconciler {

	/**
	 *
	 * Constructor.
	 *
	 */
	public RetargetMessageEndFromExecutionSpecificationToLifelineDiagramReconciler_1_4_0() {
		// nothing yo do
	}

	@Override
	public ICommand getReconcileCommand(final Diagram diagram) {
		final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(diagram);
		Assert.isNotNull(domain);
		final CompositeCommand cc = new CompositeCommand("Retarget Message from ExecutionSpecification to Lifeline"); //$NON-NLS-1$
		for (final Object current : diagram.getEdges()) {
			if (isAMessageConnector(current)) {
				final Connector connector = (Connector) current;
				if (migrateSource(connector)) {
					final ICommand tmp = getRetargetCommand(domain, connector, true);
					if (null != tmp && tmp.canExecute()) {
						cc.add(tmp);
					}
				}

				if (migrateTarget(connector)) {
					final ICommand tmp = getRetargetCommand(domain, connector, false);
					if (null != tmp && tmp.canExecute()) {
						cc.add(tmp);
					}
				}
			}
		}

		return cc.canExecute() ? cc : null;
	}


	private ICommand getRetargetCommand(final TransactionalEditingDomain domain, final Connector editedConnector, final boolean updateSource) {
		final CompositeCommand cc = new CompositeCommand(NLS.bind("Update Graphical {0} for Message {1}.", updateSource ? "source" : "target", ((NamedElement) editedConnector.getElement()).getName())); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		// 1. get the required values according to the boolean updateSource
		final Shape lifeline = updateSource ? getNewSource(editedConnector) : getNewTarget(editedConnector);
		final IdentityAnchor oldAnchor = updateSource ? (IdentityAnchor) editedConnector.getSourceAnchor() : (IdentityAnchor) editedConnector.getTargetAnchor();
		final Shape oldEndShape = updateSource ? (Shape) editedConnector.getSource() : (Shape) editedConnector.getTarget();

		// 2. we get the
		final Bounds oldEnd_Size = (Bounds) oldEndShape.getLayoutConstraint();
		final PrecisionPoint currentPercentageOnOldEndShape = BaseSlidableAnchor.parseTerminalString(oldAnchor.getId());


		// 3. the y of the anchor in the currentParent (an ExecutionSpecification)
		final double yOnOldEndShape = currentPercentageOnOldEndShape.preciseY() * oldEnd_Size.getHeight();

		// 4. we calculate the Y location of the ExecutionSpec
		final double yOnLifeline = yOnOldEndShape + oldEnd_Size.getY();
		final Bounds lifelineBounds = (Bounds) lifeline.getLayoutConstraint();
		final double yPercentage = yOnLifeline / new Double(lifelineBounds.getHeight());

		final String newTerminal = "(0.5," + yPercentage + ")"; //$NON-NLS-1$ //$NON-NLS-2$

		// 5. create the update anchor command
		final SetConnectionAnchorsCommand anchorCommand = new SetConnectionAnchorsCommand(domain, NLS.bind("Update anchors for {0}.", ((NamedElement) editedConnector.getElement()).getName())); //$NON-NLS-1$
		anchorCommand.setEdgeAdaptor(new EObjectAdapter(editedConnector));
		if (updateSource) {
			anchorCommand.setNewSourceTerminal(newTerminal);
		} else {
			anchorCommand.setNewTargetTerminal(newTerminal);
		}
		cc.add(anchorCommand);

		// 6. set the new object to reference as end
		final EReference editedFeature = updateSource ? NotationPackage.eINSTANCE.getEdge_Source() : NotationPackage.eINSTANCE.getEdge_Target();
		cc.add(new EMFtoGMFCommandWrapper(SetCommand.create(domain, editedConnector, editedFeature, lifeline)));

		return cc;
	}

	/**
	 *
	 * @param view
	 *            a view
	 * @return
	 * 		<code>true</code> if the current view is the expected Lifeline View to which the messages must be linked
	 */
	private boolean isLifelineShape(final View view) {
		return view instanceof Shape && view.getElement() instanceof Lifeline && LifelineEditPart.VISUAL_ID.equals(view.getType());
	}

	/**
	 *
	 * @param object
	 *            an object
	 * @return
	 * 		<code>true</code> if the object is a Notation Connector and represents a UML Message
	 */
	private boolean isAMessageConnector(final Object object) {
		return object instanceof Connector && ((Connector) object).getElement() instanceof Message;
	}

	/**
	 *
	 * @param messageConnector
	 *            a message connector
	 * @return
	 * 		<code>true</code> if the source must be migrated
	 */
	private boolean migrateSource(final Connector messageConnector) {
		return isAnExecutionSpecification(messageConnector.getSource());
	}

	/**
	 *
	 * @param messageConnector
	 *            a message connector
	 * @return
	 * 		<code>true</code> if the target must be migrated
	 */
	private boolean migrateTarget(final Connector messageConnector) {
		return isAnExecutionSpecification(messageConnector.getTarget());
	}

	/**
	 *
	 * @param messageConnector
	 *            a message connector
	 * @return
	 * 		the new source to use for the connector
	 */
	private Shape getNewSource(final Connector messageConnector) {
		final View source = messageConnector.getSource();
		if (isAnExecutionSpecification(source)) {
			final View parentSource = (View) source.eContainer();
			if (isLifelineShape(parentSource)) {
				return (Shape) parentSource;
			}
		}
		return null;
	}

	/**
	 *
	 * @param messageConnector
	 *            a message connector
	 * @return
	 * 		the new target to use for the connector
	 */
	private Shape getNewTarget(final Connector messageConnector) {
		final View target = messageConnector.getTarget();
		if (isAnExecutionSpecification(target)) {
			final View parentSource = (View) target.eContainer();
			if (isLifelineShape(parentSource)) {
				return (Shape) parentSource;
			}
		}
		return null;
	}

	/**
	 *
	 * @param view
	 *            a view
	 * @return
	 * 		<code>true</code> if the view represents an ActionExecutionSpecification or a BehaviorExecutionSpecification
	 */
	private boolean isAnExecutionSpecification(final View view) {
		return view instanceof Shape &&
				(ActionExecutionSpecificationEditPart.VISUAL_ID.equals(view.getType())
						|| BehaviorExecutionSpecificationEditPart.VISUAL_ID.equals(view.getType()));
	}
}
