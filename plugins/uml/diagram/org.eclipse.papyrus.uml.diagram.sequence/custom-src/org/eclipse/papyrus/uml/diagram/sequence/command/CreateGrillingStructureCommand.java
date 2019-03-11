package org.eclipse.papyrus.uml.diagram.sequence.command;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.notation.BasicCompartment;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.referencialgrilling.GrillingEditpart;

/**
 * the goal of this command is to create a basic compartment in the notation that represent a compartment of stereotypes
 *
 */
public class CreateGrillingStructureCommand extends RecordingCommand {


	/**
	 *
	 */
	private static final int Y_SPACE = 30;

	private static final int X_SPACE = 30;

	protected View node;

	protected Node parent;

	/**
	 *
	 * Constructor.
	 *
	 * @param domain
	 * @param node
	 *            The EditPart view of the Compartment
	 */
	public CreateGrillingStructureCommand(TransactionalEditingDomain domain, View node) {
		super(domain, "create Grilling Structure");
		this.node = node;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void doExecute() {

		// Create the Graphical Compartment
		BasicCompartment compartment = NotationFactory.eINSTANCE.createBasicCompartment();
		// Complete the creation
		compartment.setType(GrillingEditpart.VISUAL_ID);
		compartment.setMutable(true);
		ViewUtil.insertChildView(node, compartment, ViewUtil.APPEND, false);
	}



}
