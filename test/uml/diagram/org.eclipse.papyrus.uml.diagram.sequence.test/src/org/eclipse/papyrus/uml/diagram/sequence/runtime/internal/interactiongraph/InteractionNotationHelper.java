/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.gef.ui.figures.SlidableAnchor;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.Size;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.sasheditor.editor.ISashWindowsContainer;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResource;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.GateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionUseEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.providers.CustomViewProvider;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities.EdgeSide;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Gate;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionUse;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.OccurrenceSpecification;


public class InteractionNotationHelper {
	private static int STEP = 40; 
	private static int DEFAULT_LIFELINE_HEAD_HEIGHT = 30;
	private static int EXECUTION_INIT_WIDTH = 16;
	
	public static EditPartViewer getEditPartViewer(Diagram diagram) {
		try {
			ServicesRegistry servicesReg = ServiceUtilsForResource.getInstance().getServiceRegistry(diagram.eResource());
			ISashWindowsContainer container = servicesReg.getService(ISashWindowsContainer.class);	
			List<IEditorPart> visibleEditors = container .getVisibleIEditorParts();
			for (IEditorPart ep : visibleEditors) {			
				GraphicalViewer viewer = ep.getAdapter(GraphicalViewer.class);
				if (viewer.getRootEditPart().getContents().getModel() == diagram)
					return viewer;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void refreshViewer(EditPartViewer viewer) {
		viewer.flush();					
		viewer.getRootEditPart().getContents().refresh();
		((AbstractGraphicalEditPart)viewer.getRootEditPart().getContents()).getFigure().invalidateTree();
		((AbstractGraphicalEditPart)viewer.getRootEditPart().getContents()).getFigure().validate();				
		viewer.flush();					
	}
	
	public static Diagram getDiagram(ModelSet set) {
		Resource modelResource = set.getResource(URI.createURI("model.notation"), false);
		return (Diagram)modelResource.getContents().get(0);
	}

	public static Diagram createSequenceDiagram(Interaction interaction) {
		Diagram dia = viewProvider.createDiagram(new EObjectAdapter(interaction), null,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
		View interactionView = viewProvider.createNode(new EObjectAdapter(interaction), dia, InteractionEditPart.VISUAL_ID, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
		addDefaultBounds((Node)interactionView);
		return dia;
	}
	
	public static View createLifeline(EditPartViewer viewer, Diagram dia, Lifeline lf1) {
		return createLifeline(viewer, dia, lf1, null);
	}
	
	public static View createLifeline(EditPartViewer viewer, Diagram dia, Lifeline lf1, Rectangle bounds) {
		InteractionModelHelper.startTransaction(dia);
		try {
			View interactionViewContainer = findContainer(dia, (Element)dia.getElement(), LifelineEditPart.VISUAL_ID); 
			View lifelineView = viewProvider.createNode(new EObjectAdapter(lf1), interactionViewContainer, LifelineEditPart.VISUAL_ID, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
			if (bounds == null) {
				addDefaultBounds((Node)lifelineView);	
				List<View> lifelineViews = lf1.getInteraction().getLifelines().stream().filter(Predicate.isEqual(lf1).negate()).
						map(d->findRootView(dia,d)).collect(Collectors.toList());
				if (!lifelineViews.isEmpty()) {
	 				int nextX = ViewUtilities.getBounds(viewer, lifelineViews).right() + STEP;
					ViewUtilities.getLayoutConstraint(lifelineView).setX(ViewUtilities.toRelativeForLayoutConstraints(viewer, lifelineView, new Point(nextX,0)).x);
				}
			} else {
				setLayoutBounds((Node)lifelineView, bounds.x, bounds.y, bounds.width, bounds.height);
			}
			return lifelineView;
		} finally {
			InteractionModelHelper.endTransaction();
		}						
	}
	
	public static View createGate(EditPartViewer viewer, Diagram dia, Gate gate, int alignment) {
		InteractionModelHelper.startTransaction(dia);
		try {
			View gateView = viewProvider.createNode(new EObjectAdapter(gate), 
					ViewUtilities.getViewForElement(dia, gate.getOwner()), 
					GateEditPart.VISUAL_ID, -1, true,  
					UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
			addDefaultBounds((Node)gateView);			
			Rectangle r = ViewUtilities.getBounds(viewer, gateView);
			Rectangle rc = ViewUtilities.getBounds(viewer, (View)gateView.eContainer());
			int lastPos = getLastAnchorPosition(viewer, dia) + STEP;
			int x_offset = alignment == SWT.LEFT ? 0 : rc.width;
			
			if (gate.getOwner() instanceof InteractionUse) {
				InteractionUse use = (InteractionUse)gate.getOwner();
				List<Gate> gates = use.getActualGates();
				lastPos = 20 + (20 * gates.indexOf(gate)) + rc.y;
			}
			
			Point pt = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)gateView.eContainer(), new Point(r.x + x_offset,lastPos));			
			addBounds((Node)gateView, pt.x, pt.y, r.width, r.height);
			return gateView;
		} finally {
			InteractionModelHelper.endTransaction();
		}						
	}

	public static View createExecutionSpecification(EditPartViewer viewer, Diagram diagram, ExecutionSpecification execSpec) {
		InteractionModelHelper.startTransaction(diagram);
		try {
			Lifeline lifeline = execSpec.getCovereds().stream().findFirst().orElse(null);
			View lifelineView = findRootView(diagram, lifeline);
			View execSpecView = viewProvider.createNode(new EObjectAdapter(execSpec), lifelineView, 
					execSpec instanceof ActionExecutionSpecification ? 
							ActionExecutionSpecificationEditPart.VISUAL_ID :
							BehaviorExecutionSpecificationEditPart.VISUAL_ID,	
					-1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
			
			OccurrenceSpecification start = execSpec.getStart();
			int startPosition = getAnchorPosition(viewer, (Node)lifelineView, execSpec.getStart());
			int endPosition = execSpec.getFinish() != null ? getAnchorPosition(viewer, (Node)lifelineView, execSpec.getFinish()) : (startPosition + STEP);
			Rectangle r = ViewUtilities.getBounds(viewer, lifelineView);
			r.x = r.x + (r.width - EXECUTION_INIT_WIDTH) / 2;
			r.width = EXECUTION_INIT_WIDTH; 
			r.y = startPosition;
			r.height = endPosition-startPosition;
			// TODO: Shift along x for nested executions
			r = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)execSpecView.eContainer(), r);
			addBounds((Node)execSpecView, r);
			
			if (start instanceof MessageOccurrenceSpecification) {
				Edge msgEdge = (Edge)findRootView(diagram, ((MessageOccurrenceSpecification) start).getMessage());
				if (msgEdge != null) {
					moveMessageOcurrenceIntoExecSpec(viewer,diagram, msgEdge, (MessageOccurrenceSpecification)start, execSpecView); 
				}
			} 
			
			OccurrenceSpecification finish = execSpec.getFinish();
			if (finish instanceof MessageOccurrenceSpecification) {
				Edge msgEdge = (Edge)findRootView(diagram, ((MessageOccurrenceSpecification) finish).getMessage());
				if (msgEdge != null) {
					moveMessageOcurrenceIntoExecSpec(viewer,diagram, msgEdge, (MessageOccurrenceSpecification)finish, execSpecView); 
				}
			} 

			List<InteractionFragment> l = lifeline.getCoveredBys();
			int index = l.indexOf(start);
			if (index >= 0)
				l = l.subList(index+1, l.size());
			
			index = l.indexOf(finish);			
			if (index >= 0) 
				l = l.subList(0, index);
			
			List<MessageOccurrenceSpecification> moss = l.stream().filter(MessageOccurrenceSpecification.class::isInstance)
					.map(MessageOccurrenceSpecification.class::cast).collect(Collectors.toList());			

			List<ExecutionSpecification> execs = l.stream().filter(ExecutionSpecification.class::isInstance).
					map(ExecutionSpecification.class::cast).
					collect(Collectors.toList());
			List<View> execsViews = execs.stream().map(d->findRootView(diagram,d)).collect(Collectors.toList()); 
			int x_offset = 0;
			for (ExecutionSpecification exec : execs) {
				int index1 = l.indexOf(exec.getStart());
				int index2 = l.indexOf(exec.getFinish());
				if (index1 == -1 || index2 == -1)
					continue;				
				x_offset += 10;
				moss.removeAll(l.subList(index1, index2+1));
				// Move ExecSpec to the right.
				View execView = findRootView(diagram, exec);
				Location loc = ViewUtilities.getLayoutConstraint(execView);				
				loc.setX(loc.getX()+x_offset);
			}
			
			// Z-Order
			View container = (View)execSpecView.eContainer(); 
			container.getPersistedChildren().removeAll(execsViews);
			container.getPersistedChildren().addAll(execsViews);
						
			for (MessageOccurrenceSpecification mos : moss) {
				Edge msgEdge = (Edge)findRootView(diagram, ((MessageOccurrenceSpecification) mos).getMessage());
				if (msgEdge != null) {
					moveMessageOcurrenceIntoExecSpec(viewer,diagram, msgEdge, (MessageOccurrenceSpecification)mos, execSpecView); 
				}
			}
			return execSpecView;
		} finally {
			InteractionModelHelper.endTransaction();
		}
	}
	
	private static void moveMessageOcurrenceIntoExecSpec(EditPartViewer viewer, Diagram diagram, Edge msgEdge, MessageOccurrenceSpecification mos, View execSpecView) {
		// TODO: Update bendpoints
		boolean isTarget = ((MessageOccurrenceSpecification) mos).getMessage().getReceiveEvent() == mos;
		IdentityAnchor anchor = null;
		Point anchorLoc = null;
		if (isTarget) {
			anchor = (IdentityAnchor)msgEdge.getTargetAnchor();
			anchorLoc = ViewUtilities.getAnchorLocationForView(viewer, msgEdge, EdgeSide.Target); 
			msgEdge.setTarget(execSpecView);
			
		} else {
			anchor = (IdentityAnchor)msgEdge.getSourceAnchor();
			anchorLoc = ViewUtilities.getAnchorLocationForView(viewer, msgEdge, EdgeSide.Source); 
			msgEdge.setSource(execSpecView);
		}
		
		Bounds b = (Bounds)((Node)execSpecView).getLayoutConstraint();
		ExecutionSpecification execSpec = (ExecutionSpecification)execSpecView.getElement();
		if (execSpec.getStart() == mos) {
			// TODO: Check side of anchor ??
			anchor.setId("(0.5,0.0){8}");
			// Set start location
			anchorLoc = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)execSpecView, anchorLoc);
			//b.setY(anchorLoc.y);
		} else if (execSpec.getFinish() == mos) {
			anchor.setId("(0.5,1.0){32}");						
			// Set finish location
			anchorLoc = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)execSpecView, anchorLoc);
			//b.setHeight(Math.max(STEP, anchorLoc.y - b.getY()));			
		} else {
			anchorLoc = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)execSpecView, anchorLoc);
			double frac = ((double)anchorLoc.y) / (double)b.getHeight();
			anchor.setId("(0.5,"+Math.min(1.0, frac)+")");						
		}
		
		//makeMsgHorizontal(viewer, msgEdge);
	}

	// TODO: Check if the message ends are inside a ExecSpec???
	public static Edge createMessage(EditPartViewer viewer, Diagram dia, Message msg) {
		// TODO: Create the bendpoints
		InteractionModelHelper.startTransaction(dia);
		try {
			Edge edge = null;
			switch (msg.getMessageSort()) {
				case ASYNCH_CALL_LITERAL:
				case ASYNCH_SIGNAL_LITERAL:
					edge = viewProvider.createMessage_AsynchEdge(msg, dia, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
					break;
				case SYNCH_CALL_LITERAL:
					edge = viewProvider.createMessage_SynchEdge(msg, dia, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
					break;
				case CREATE_MESSAGE_LITERAL:
					edge = viewProvider.createMessage_CreateEdge(msg, dia, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);				
					break;
				case DELETE_MESSAGE_LITERAL:
					edge = viewProvider.createMessage_DeleteEdge(msg, dia, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);				
					break;
				case REPLY_LITERAL:
					edge = viewProvider.createMessage_ReplyEdge(msg, dia, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
					break;
				default:
					throw new IllegalArgumentException();
			}
			
			View gateView = null;
			int lastAnchor = getLastAnchorPosition(viewer, dia);
			MessageEnd sendEvent = msg.getSendEvent();
			MessageEnd recvEvent = msg.getReceiveEvent();

			if (sendEvent instanceof Gate) {
				Gate gate = (Gate)sendEvent;
				gateView = ViewUtilities.getViewForElement(dia, gate);
				edge.setSource(gateView);
				IdentityAnchor sourceAnchor = (IdentityAnchor)edge.createSourceAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);
				sourceAnchor.setId("(0.5,0.5)");
				lastAnchor = ViewUtilities.getBounds(viewer, gateView).getCenter().y;
			} 
			
			if (recvEvent instanceof Gate) {
				Gate gate = (Gate)recvEvent;
				gateView = ViewUtilities.getViewForElement(dia, gate);
				edge.setTarget(gateView);
				IdentityAnchor sourceAnchor = (IdentityAnchor)edge.createTargetAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);
				sourceAnchor.setId("(0.5,0.5)");
				lastAnchor = ViewUtilities.getBounds(viewer, gateView).getCenter().y;				
			}
			
			if (sendEvent instanceof MessageOccurrenceSpecification) {
				anchorMessageEnd(viewer, dia, edge, (MessageOccurrenceSpecification)sendEvent, true, lastAnchor);								
			} 
	
			if (recvEvent instanceof MessageOccurrenceSpecification) {
				MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification)recvEvent;
				if (edge.getSource() == findRootView(dia, mos.getCovered()))
					lastAnchor += 40; // SelfMessage
				anchorMessageEnd(viewer, dia, edge, mos, false, lastAnchor);								
			} 

				
			if (gateView != null) {
				Bounds bounds = (Bounds)((Node)gateView).getLayoutConstraint();
				if (bounds.getX() == 0 && bounds.getY() == 0) {
					bounds.setX(0);
					bounds.setY(STEP+(int)lastAnchor - (bounds.getHeight() / 2));
					((Node)gateView).setLayoutConstraint(bounds);
				}
			}
			
			if (msg.getMessageSort() == MessageSort.REPLY_LITERAL && sendEvent instanceof MessageOccurrenceSpecification) {
				Lifeline lf = ((MessageOccurrenceSpecification)sendEvent).getCovered();
				ExecutionSpecification exec = lf.getCoveredBys().stream().
						filter(ExecutionSpecification.class::isInstance).map(ExecutionSpecification.class::cast).
						filter(d-> d.getFinish() == sendEvent).findFirst().orElse(null);
				if (exec != null) {
					createExecutionSpecification(viewer, dia, exec);
				}
			}
			
			//makeMsgHorizontal(viewer, edge);
			return edge;
		} finally {
			InteractionModelHelper.endTransaction();
		}						
	}
	
	private static void makeMsgHorizontal_(EditPartViewer viewer, Edge edge) {
		Point ptSrc = ViewUtilities.getAnchorLocationForView(viewer, edge, EdgeSide.Source);
		Point ptDst = ViewUtilities.getAnchorLocationForView(viewer, edge, EdgeSide.Target);
		
		if (Math.abs(ptSrc.y - ptDst.y) > NodeUtilities.THRESHOLD_HORIZONTAL_DEPS) {
			Point pt = null;
			EdgeSide side = null;
			View anchorView = null; 
			if (ptSrc.y < ptDst.y) {
				pt = new Point(ptSrc.x, ptDst.y);
				anchorView = edge.getSource();
				side = EdgeSide.Source;
			} else {
				pt = new Point(ptDst.x, ptSrc.y);
				anchorView = edge.getTarget();					
				side = EdgeSide.Target;
			}
			
			if (anchorView.getElement() instanceof Gate) {
				Rectangle r = ViewUtilities.getBounds(viewer, anchorView);
				pt = pt.getCopy();
				pt.y = pt.y - (r.height / 2);
				pt.x = pt.x - (r.width / 2);
				pt = ViewUtilities.toRelativeForLayoutConstraints(viewer, (View)anchorView.eContainer(), pt);
				InteractionNotationHelper.addBounds((Node)anchorView, pt.x, pt.y, r.width, r.height );
			} else {
				ViewUtilities.setAnchorLocationForView(viewer, edge, side, pt);
			}
		}
	}
	
	private static int anchorMessageEnd(EditPartViewer viewer, Diagram dia, Edge edge, MessageOccurrenceSpecification mos, boolean isSource, int yPos) {
		int lastAnchor = yPos;
		Lifeline lifeline = mos.getCovered();
		View lifelineView = findRootView(dia, lifeline); 
		ExecutionSpecification execSpec = lifeline.getCoveredBys().stream().filter(ExecutionSpecification.class::isInstance)
			.map(ExecutionSpecification.class::cast)
			.filter(d -> d.getStart() == mos || d.getFinish() == mos).
			findFirst().orElse(null);
		if (execSpec == null) {
			List<InteractionFragment> l = lifeline.getCoveredBys();
			int index = l.indexOf(mos);
			l = l.subList(0, index);
			List<ExecutionSpecification> execs = l.stream().filter(ExecutionSpecification.class::isInstance)
					.map(ExecutionSpecification.class::cast).collect(Collectors.toList());
			for (ExecutionSpecification exec : execs) {				 
				if (l.contains(exec.getFinish()))
					continue;
				execSpec = exec;
			}			
		}
		View execSpecView = execSpec != null ? findRootView(dia, execSpec) : null;
		IdentityAnchor anchor = null; 
		if (execSpecView != null) {
			if (isSource) {
				edge.setSource(execSpecView);
				anchor = (IdentityAnchor)edge.createSourceAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);
			} else {
				edge.setTarget(execSpecView);				
				anchor = (IdentityAnchor)edge.createTargetAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);
			}

			Rectangle execSpecBounds = ViewUtilities.getBounds(viewer, execSpecView);
			if (execSpec.getStart() == mos) {
				anchor.setId("(0.5,0.0){8}");
				lastAnchor = execSpecBounds.y;
			} else if (execSpec.getFinish() == mos) {
				anchor.setId("(0.5,1.0){32}");
				lastAnchor = execSpecBounds.y;
			} else {
				// In the middle... => Won't work as we change the heigh of the exec spec... All existing messages will be moved
				double pos = ((double)execSpecBounds.height) / (double)(execSpecBounds.height + STEP);
				anchor.setId("(0.5,"+pos+")");				
				execSpecBounds.setHeight(execSpecBounds.height + STEP);
				lastAnchor = execSpecBounds.y;
				addBounds((Node)execSpecView,execSpecBounds);
			}
		} else {
			if (isSource) {
				edge.setSource(lifelineView);
				anchor = (IdentityAnchor)edge.createSourceAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);				
			} else {
				edge.setTarget(lifelineView);
				anchor = (IdentityAnchor)edge.createTargetAnchor(NotationPackage.Literals.IDENTITY_ANCHOR);
			}
				
			double height = (double)getLifelineContentPaneHeight(viewer, (Node)lifelineView);
			int y = ViewUtilities.toRelativeForLayoutConstraints(viewer,lifelineView, new Point(0,lastAnchor)).y;
			anchor.setId("(0.5,"+String.valueOf(Math.min(1.0, ((double)y) / height))+")");
		}		
		
		return lastAnchor;
	}
	
	public static Node createInteractionUse(EditPartViewer viewer, Diagram diagram, InteractionUse intUse) {
		InteractionModelHelper.startTransaction(diagram);
		try {
			List<Lifeline> lifelines = intUse.getCovereds();
			List<View> lifelineViews = lifelines.stream().map(d -> findRootView(diagram, d)).collect(Collectors.toList());
			Rectangle r = ViewUtilities.getBounds(viewer, lifelineViews);
			
			int maxY = Integer.MIN_VALUE;
			for (View lf : lifelineViews) {
				Node n = (Node)lf;
				maxY = Math.max(maxY, getLastAnchorPosition(viewer, n));
			}
			
			r.y = maxY; r.height = 40;
			View container = findContainer(diagram, (Interaction)diagram.getElement(), InteractionUseEditPart.VISUAL_ID); 
			Node node = (Node)viewProvider.createInteractionUse_Shape(intUse, container, -1, true,  UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
			r = ViewUtilities.toRelativeForLayoutConstraints(viewer, container , r); 
			addBounds(node, r);
			return node;
		} finally {
			InteractionModelHelper.endTransaction();
		}
 	}

	
	// TODO: Move them to ViewUtilities
	// <---------------------
	public static View findContainer(View view, Element parent, String visualId) {
		View rootView = findRootView(view, parent);		
		return findContainerImpl(rootView, parent, visualId);
	}
	
	private static List<View> getChildren(View container, String visualId) {
		return getChildren(null, container, visualId, false);
	}
	
	private static List<View> getAllChildren(View container, String visualId) {
		return getChildren(null, container, visualId, true);
	}

	private static List<View> getChildren(List<View> views, View container, String visualId, boolean rec) {
		views = views == null ? new ArrayList<View>() : views;
		for (Object obj : container.getChildren()) {
			if (!(obj instanceof View))
				continue;
			View v = (View)obj;
			if (v.getType().equals(visualId))
				views.add(v);
			
			if (rec) {
				getChildren(views, v, visualId, true);
			}
		}
		
		return views;
	}

	private static View findContainerImpl(View view, EObject parent, String visualId) {
		for (Object vw : view.getChildren()) {
			if (!(vw instanceof Node))
				continue;
			View child = (View)vw;			
			if (child.getElement() != parent)
				continue;
			if (!UMLVisualIDRegistry.canCreateNode(child, visualId))
				continue;
			return child;
		}
		
		// Try in-deep.
		for (Object vw : view.getChildren()) {
			if (!(vw instanceof Node))
				continue;
			View child = (View)vw;			
			if (child.getElement() != parent)
				continue;

			View c = findContainerImpl(child, parent, visualId);
			if (c != null)
				return c;
		}
		return null;
	}
		
	public static View findRootView(View view, Element el) {
		// Check Parent
		while (view.getElement() == el && view.eContainer() instanceof View && 
			   ((View)view.eContainer()).getElement() == el && view.eContainer() != view.getDiagram()) {
			view = (View)view.eContainer();
		}
		
		if (view.getElement() == el && view != view.getDiagram())
			return view;
		
		// Check children
		for (Object vw : view.getChildren()) {
			if (!(vw instanceof Node))
				continue;
			View child = (View)vw;
			if (child.getElement() == el)
				return child;
			
			View v = findRootView(child, el);
			if (v != null)
				return v;
		}		
		
		if (view instanceof Diagram) {
			// Check Edges also.
			for (Object vw : ((Diagram)view).getEdges()) {
				View child = (View)vw;
				if (child.getElement() == el)
					return child;
				
				View v = findRootView(child, el);
				if (v != null)
					return v;
			}					
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void addDefaultBounds(Node n) {		
		n.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
		n.getChildren().stream().forEach(d -> {if (d instanceof Node) addDefaultBounds((Node)d);}); 
	}
	
	private static Bounds addBounds(Node n, Rectangle r) {		
		return addBounds(n, r.x, r.y, r.width, r.height);
	}

	private static Bounds addBounds(Node n, int x, int y, int w, int h) {		
		Bounds b = NotationFactory.eINSTANCE.createBounds();
		b.setX(x);
		b.setY(y);
		b.setWidth(w);
		b.setHeight(h);
		n.setLayoutConstraint(b);
		return b;
	}

	private static Bounds setLayoutBounds(Node n, int x, int y, int w, int h) {
		return addBounds(n, x, y, w, h);
	}
	
	public static Location setLayoutPosition(Node n, int x, int y) {
		Location b = (Location)n.getLayoutConstraint();
		if (b == null) {
			b = NotationFactory.eINSTANCE.createBounds();
			n.setLayoutConstraint(b);
		}
		b.setX(x);
		b.setY(y);
		return b;
	}

	private static Size setLayoutSize(Node n, int w, int h) {
		Size b = (Size)n.getLayoutConstraint();
		if (b == null) {
			b = NotationFactory.eINSTANCE.createBounds();
			n.setLayoutConstraint(b);
		}
		b.setWidth(w);
		b.setHeight(h);
		return b;
	}

	private static int getLifelineContentPaneHeight(EditPartViewer viewer, Node lifelineView) {
		GraphicalEditPart ep = (GraphicalEditPart)viewer.getEditPartRegistry().get(lifelineView);
		if (ep == null) {
			// Use the layout contraint - default head height
			return ((Bounds)lifelineView.getLayoutConstraint()).getHeight() - DEFAULT_LIFELINE_HEAD_HEIGHT;
		}
		return ep.getContentPane().getBounds().height;
	}
	
	// Return relative to the interaction container 
	private static int getLastAnchorPosition(EditPartViewer viewer, Diagram dia) {
		View interactionViewContainer = findContainer(dia, (Element)dia.getElement(), LifelineEditPart.VISUAL_ID); 
		int maxY = ViewUtilities.getBounds(viewer, interactionViewContainer).y + DEFAULT_LIFELINE_HEAD_HEIGHT;
		Interaction inter = (Interaction)interactionViewContainer.getElement();
		for (Lifeline lf : inter.getLifelines()) {
			Node lfNode = (Node)findRootView(interactionViewContainer,lf);
			maxY = Math.max(maxY, getLastAnchorPosition(viewer, lfNode));			
		}

		// TODO: Consider Gates
		for (Gate g : inter.getFormalGates()) {
			View vw = findRootView(dia, g);
			if (vw != null) {
				Rectangle r = ViewUtilities.getBounds(viewer, vw);
				maxY = Math.max(maxY, r.y+(r.height / 2));
			}
		}
		return maxY;//ViewUtilities.toRelativeForLayoutConstraints(viewer, interactionViewContainer, new Point(0,maxY)).y;
	}

	@SuppressWarnings("unchecked")
	private static int getLastAnchorPosition(EditPartViewer viewer, Node lifelineView) {
		return getAnchorPosition(viewer, lifelineView, null);
	}
	
	private static int getAnchorPosition(EditPartViewer viewer, Node lifelineView, InteractionFragment frag) {
		NodeEditPart ep = (NodeEditPart)viewer.getEditPartRegistry().get(lifelineView);		
		if (ep != null) {
			int yPos =  ViewUtilities.getBounds(viewer, lifelineView).y + DEFAULT_LIFELINE_HEAD_HEIGHT; 
			yPos = (yPos + 19) / 40 * 40;
			Lifeline lf = (Lifeline)lifelineView.getElement();
			List<InteractionFragment> covered = lf.getCoveredBys();
			boolean found = false;
			for (InteractionFragment ifg : covered) {
				if (found)
					break;
				found = ifg == frag;
				if (ifg instanceof ExecutionSpecification) {
					continue;
				}
				
				if (ifg instanceof MessageOccurrenceSpecification) {
					MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification)ifg;
					Message msg = mos.getMessage();
					boolean isSource = msg.getSendEvent() == mos;
					
					ConnectionAnchor anchor = ((List<ConnectionEditPart>)(isSource ? ep.getSourceConnections() : ep.getTargetConnections())).stream().filter(
							d->((View)d.getModel()).getElement() == msg).map(d -> isSource ? ep.getSourceConnectionAnchor(d) : ep.getTargetConnectionAnchor(d)).
							findFirst().orElse(null);
					if (anchor == null) {
						yPos += STEP;						
					} else {					
						yPos = anchor.getLocation(anchor.getReferencePoint()).y;
					}
					continue;
				}

				View frgView = findRootView(lifelineView.getDiagram(), ifg);
				Rectangle r = ViewUtilities.getBounds(viewer, frgView);
				if (r == null) {
					yPos += STEP;
					continue;
				}

				int y = r.y + r.height;
				if (ifg instanceof OccurrenceSpecification) {
					y -= (r.height / 2);
				}
				yPos = y;
			}
			
			return yPos;
		} else {
			// This is not working.... REMOVE!!!
			double val = 0.0;
			for (Edge e : (List<Edge>)lifelineView.getSourceEdges()) {
				IdentityAnchor anchor = (IdentityAnchor)e.getSourceAnchor();
				PrecisionPoint point = SlidableAnchor.parseTerminalString(anchor.getId());
				val = Math.max(val, point.preciseY());				
			}
			for (Edge e : (List<Edge>)lifelineView.getTargetEdges()) {
				IdentityAnchor anchor = (IdentityAnchor)e.getTargetAnchor();
				PrecisionPoint point = SlidableAnchor.parseTerminalString(anchor.getId());
				val = Math.max(val, point.preciseX());				
			}
			
			return (int)((double)getLifelineContentPaneHeight(viewer, lifelineView) * val);
		}
	}
	// ------------------------------------>
	
	private static final CustomViewProvider viewProvider = new CustomViewProvider();
}
