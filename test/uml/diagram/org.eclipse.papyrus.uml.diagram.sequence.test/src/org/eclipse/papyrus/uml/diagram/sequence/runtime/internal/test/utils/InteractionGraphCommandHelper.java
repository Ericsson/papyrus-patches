package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraph;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.InteractionGraphFactory;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Link;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.interactiongraph.Node;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.NodeUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.commands.InteractionGraphCommand;
import org.eclipse.papyrus.uml.service.types.element.UMLElementTypes;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLFactory;

public class InteractionGraphCommandHelper {
	public InteractionGraphCommandHelper(ModelSet modelSet) {
		
	}
	
	public Diagram getDiagram() {
		return diagram;
	}
	
	public Interaction getInteraction() {
		return interaction;
	}

	public EditPartViewer getViewer() {
		return viewer;
	}

	public Diagram createSequenceDiagram(String name) {
		interaction = UMLFactory.eINSTANCE.createInteraction();
		interaction.setName(name);		
		
		diagram = InteractionNotationHelper.createSequenceDiagram(interaction);
		diagram.setName(name);				
		return diagram;
	}

	private void initInteractionGraph() {
		if (editingDomain == null) {
			editingDomain = TransactionUtil.getEditingDomain(diagram);			
		}
		
		if (viewer == null) {
			viewer = InteractionNotationHelper.getEditPartViewer(diagram);			
		}
		
		if (graph == null) {
			graph = InteractionGraphFactory.getInstance().createInteractionGraph(interaction, diagram, viewer);
		}
	}
	
	public Lifeline addLifeline(String name, Rectangle rect) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Add Lifeline");
		CreateViewAndElementRequest req = new CreateViewAndElementRequest(UMLElementTypes.LIFELINE, 
				graph.getInteraction(), 
				PreferencesHint.USE_DEFAULTS);
		command.addLifeline((CreateElementRequestAdapter)req.getViewAndElementDescriptor().getElementAdapter(),
				req.getViewAndElementDescriptor(),
				rect);
		command.addAction().apply(d->last(d.getLifelineClusters())).handleResult((Node n)->setName(n, name));
		IStatus status = executeCommand(command);
		if (status.isOK()) {
			Lifeline lf = (Lifeline)req.getViewAndElementDescriptor().getElementAdapter().getAdapter(Lifeline.class);
			return lf;
		}
		return null;
	}
		
	public boolean nudgeLifeline(Lifeline lifeline, int dx) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Nudge Lifeline");
		command.nudgeLifeline(lifeline, new Point(dx,0));
		IStatus status = executeCommand(command);
		return status.isOK(); 
	}

	public boolean moveLifeline(Lifeline lifeline, int dx) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Move Lifeline");
		command.moveLifeline(lifeline, new Point(dx,0));
		IStatus status = executeCommand(command);
		return status.isOK(); 
	}

	public boolean deleteLifeline(Lifeline lifeline) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Delete Lifeline");
		command.deleteLifeline(lifeline);
		IStatus status = executeCommand(command);
		return status.isOK(); 
	}

	public InteractionUse addInteractionUse(String name, Rectangle rect) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Add Interaction Use");
		CreateViewAndElementRequest req = new CreateViewAndElementRequest(UMLElementTypes.INTERACTION_USE, 
				graph.getInteraction(), 
				PreferencesHint.USE_DEFAULTS);
		command.addInteractionUse((CreateElementRequestAdapter)req.getViewAndElementDescriptor().getElementAdapter(),
				req.getViewAndElementDescriptor(),
				rect);
		command.addAction().apply(d->last(d.getFragmentClusters())).handleResult((Node n)->setName(n, name));
		IStatus status = executeCommand(command);
		if (status.isOK()) {
			InteractionUse use = (InteractionUse)req.getViewAndElementDescriptor().getElementAdapter().getAdapter(InteractionUse.class);
			return use;
		}
		return null;
	}


	public Message addMessage(String name, MessageSort msgSort, Element source, Point srcPoint, Element target, Point trgPoint) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Add Message");

		IElementType elemType = null;
		String hint = null;
		switch (msgSort.getValue()) {
			case MessageSort.ASYNCH_CALL:
				elemType = UMLElementTypes.COMPLETE_ASYNCH_CALL;
				hint = MessageAsyncEditPart.VISUAL_ID; break;
			case MessageSort.ASYNCH_SIGNAL:
				elemType = UMLElementTypes.COMPLETE_ASYNCH_SIGNAL; 
				hint = MessageAsyncEditPart.VISUAL_ID; break;
			case MessageSort.CREATE_MESSAGE:
				elemType = UMLElementTypes.COMPLETE_CREATE_MESSAGE;
				hint = MessageCreateEditPart.VISUAL_ID; break;
			case MessageSort.DELETE_MESSAGE:
				elemType = UMLElementTypes.COMPLETE_DELETE_MESSAGE; 
				hint = MessageDeleteEditPart.VISUAL_ID; break;
			case MessageSort.REPLY:
				elemType = UMLElementTypes.COMPLETE_REPLY;
				hint = MessageReplyEditPart.VISUAL_ID; break;
			case MessageSort.SYNCH_CALL:
				elemType = UMLElementTypes.COMPLETE_SYNCH_CALL; 
				hint = MessageSyncEditPart.VISUAL_ID; break;
		}
	

		CreateConnectionViewAndElementRequest req = new CreateConnectionViewAndElementRequest(elemType, hint, PreferencesHint.USE_DEFAULTS);
		
		command.addMessage(name, msgSort, req.getConnectionViewAndElementDescriptor().getCreateElementRequestAdapter(), 
				req.getConnectionViewAndElementDescriptor(), 
				source, srcPoint,
				target,	trgPoint);

		IStatus status = executeCommand(command);
		if (status.isOK()) {			
			Message msg = (Message)req.getConnectionViewAndElementDescriptor().getElementAdapter().getAdapter(Message.class);
			return msg;
		}
		return null;
	}

	public boolean deleteMessage(Message msg) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Delete Message");
		command.deleteMessage(msg);
		IStatus status = executeCommand(command);
		return status.isOK(); 
	}

	public boolean nudgeMessage(Message msg, int offsetY) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Nudge Message");
		command.nudgeMessage(msg, new Point(0,offsetY));
		IStatus status = executeCommand(command);
		return status.isOK(); 		
	}

	
	public boolean moveMessage(Message msg, int offsetY) {
		initInteractionGraph();
		InteractionGraphCommand command = newCommand("Nudge Message");
		command.moveMessage(msg, new Point(0,offsetY));
		IStatus status = executeCommand(command);
		return status.isOK(); 		
	}

	public IStatus executeCommand(InteractionGraphCommand command) {
		InteractionGraph graph = command.getInteractionGraph(); 
		EditPartViewer viewer = graph.getEditPartViewer();
		
		InteractionModelHelper.startTransaction(graph.getInteraction());
		IStatus status;
		try {
			editingDomain.getCommandStack().execute(new GMFtoEMFCommandWrapper(command));
			status = command.getCommandResult().getStatus();
			//status = command.execute(new NullProgressMonitor(), null);
		} catch (Exception e) {
			status = new Status(IStatus.ERROR, 
					org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin.ID, 
					e.getMessage(), e);
		}
		InteractionModelHelper.endTransaction();
		InteractionNotationHelper.refreshViewer(viewer);
		graph = null;
		return status;
	}

	private CommandStack getCommandStack() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if ((activeWorkbenchWindow != null) && (activeWorkbenchWindow.getActivePage() != null)) {
			return activeWorkbenchWindow.getActivePage().getActivePart().getAdapter(CommandStack.class);
		}

		return null;
		
	}
	
	private <T> T last(List<T> list) {
		return last(list,0);
	}

	private <T> T last(List<T> list, int index) {
		if (list.isEmpty())
			return null;
		return list.get(list.size()-1-index); 
	}

	private void setName(Node node, String name) {
		NamedElement el = (NamedElement)node.getElement();
		el.setName(name);
	}
	
	private void setName(Link link, String name) {
		NamedElement el = (NamedElement)link.getElement();
		el.setName(name);
	}

	private InteractionGraphCommand newCommand(String label){
		return new InteractionGraphCommand(editingDomain, label, graph, null);
	}
	
	private Interaction interaction;
	private Diagram diagram;  
	private EditPartViewer viewer;
	private InteractionGraph graph;
	private TransactionalEditingDomain editingDomain;
}
