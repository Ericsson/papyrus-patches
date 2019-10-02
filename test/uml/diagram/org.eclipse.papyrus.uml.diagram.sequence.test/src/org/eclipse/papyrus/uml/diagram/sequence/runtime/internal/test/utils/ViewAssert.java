package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.LayoutConstraint;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageReplyEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities.EdgeSide;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.NamedElement;
import org.junit.Assert;

public class ViewAssert {

	public static void assertView(Diagram dia, Element element, String type, View parent, Rectangle bounds) {
		assertView(dia,element, type, parent); 
		assertViewBounds(dia, element, bounds);
	}
	
	public static void assertView(Diagram dia, Element element, String type, View parent) {
		 View view = InteractionNotationHelper.findRootView(dia, element);
		 String name = getElementName(element);
		 Assert.assertEquals(String.format("View Type for '%s': ", name), type, view.getType());
		 Assert.assertEquals(String.format("Parent View for '%s': ", name), type, view.getType());		 
	}
	
	public static void assertViewsBounds(Diagram dia, List<SimpleEntry<? extends Element, Rectangle>> entries) {
		entries.forEach(d->assertViewBounds(dia, d.getKey(), d.getValue()));
	}

	public static void assertViewBounds(Diagram dia, Element element, Rectangle bounds) {
		 EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(dia);
		 View view = InteractionNotationHelper.findRootView(dia, element);
		 String name = getElementName(element);
		 Assert.assertTrue(String.format("View for '%s' is not a Node",name),  view instanceof Node);
		 Node vwNode = (Node)view;
		 
		 LayoutConstraint layoutConstraints = vwNode.getLayoutConstraint();
		 Assert.assertNotNull(String.format("View for '%s' has no layout constarints",name),  layoutConstraints);
		 Assert.assertTrue(String.format("View for '%s' layout constraint is not a Bounds instances",name),  layoutConstraints instanceof Bounds);
		 
		 Rectangle r = ViewUtilities.getBounds(viewer, view);
		 Assert.assertNotNull(String.format("View for '%s' can not calculate Bounds rect.",name),  r);
		 if (element instanceof Lifeline) {
			 if (Math.abs(r.x-bounds.x) >= 2 || Math.abs(r.y-bounds.y) >= 2 ||
				 Math.abs(r.bottom()-bounds.bottom()) >= 2 || Math.abs(r.right()-bounds.right()) >= 2) {
				 Assert.assertEquals(String.format("View for '%s' bounds:",name), bounds, r);			 				 
			 }
		 } else {
			 Assert.assertEquals(String.format("View for '%s' bounds:",name), bounds, r);			 
		 }
	}

	public static void assertEdge(Diagram dia, Message msg, String type, Element anchoringSource, Point srcAnchor, Element anchoringTarget, Point trgAnchor) {
		assertEdge(dia, msg, type, anchoringSource, anchoringTarget);
		assertEdgeAnchorLocation(dia, msg, msg.getSendEvent(), srcAnchor);
		assertEdgeAnchorLocation(dia, msg, msg.getReceiveEvent(), trgAnchor);
	}

	public static void assertEdge(Diagram dia, Message msg, String type, Element anchoringSource, Element anchoringTarget) {
		 EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(dia);
		 String name = getElementName(msg);
		 View view = InteractionNotationHelper.findRootView(dia, msg);
		 
		 Assert.assertEquals(String.format("View Type for '%s': ", name), type, view.getType());
		 Assert.assertEquals(String.format("Parent View for '%s': ", name), type, view.getType());		 
		 
		 Assert.assertTrue(String.format("View for '%s' is not a Edge",name),  view instanceof Edge);
		 Edge edge = (Edge)view;
		 
		 Assert.assertSame(String.format("Source View for '%s': ", name), 
				 InteractionNotationHelper.findRootView(dia, anchoringSource), 
				 edge.getSource());

		 Assert.assertSame(String.format("Target View for '%s': ", name), 
				 InteractionNotationHelper.findRootView(dia, anchoringTarget), 
				 edge.getTarget());
	}

	public static void assertEdgeAnchorsLocations(Diagram dia, List<SimpleEntry<Message, Point[]>> entries) {
		entries.forEach(d-> {
			assertEdgeAnchorLocation(dia, d.getKey(), d.getKey().getSendEvent(), d.getValue()[0]);
			assertEdgeAnchorLocation(dia, d.getKey(), d.getKey().getReceiveEvent(), d.getValue()[1]);
		});
	}

	public static void assertEdgeAnchorLocation(Diagram dia, Message msg, MessageEnd msgEnd, Point anchorLoc) {		
		 EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(dia);
		 String name = getElementName(msg);
		 View view = InteractionNotationHelper.findRootView(dia, msg);

		 Assert.assertTrue(String.format("View for '%s' is not a Edge",name),  view instanceof Edge);
		 Edge edge = (Edge)view;
		 
		 EdgeSide side = msg.getSendEvent() == msgEnd ? EdgeSide.Source : EdgeSide.Target;
		 Point p = ViewUtilities.getAnchorLocationForView(viewer, edge, side);
		 Assert.assertNotNull(String.format("Anchor (%s) for Edge '%s' can not be calculated",side.toString(), name),  p);
		 
		 Assert.assertEquals(String.format("Anchor(%s) location for Edge '%s':",side.toString(), name), anchorLoc, p);		 
		 
	}

	public static void assertMessageGroup(Diagram dia, 
			Message msg, String type, Element anchoringSource, Point srcAnchor, ExecutionSpecification execSpec, String execSpecType,
			Rectangle execSpec_Rectangle, Message msgReply) 
	{
		// Check Message
		
		assertEdge(dia, msg, type,anchoringSource, srcAnchor, execSpec, execSpec_Rectangle.getTop());

		// Check Reply Message
		assertEdge(dia, msgReply, MessageReplyEditPart.VISUAL_ID, 
				execSpec, execSpec_Rectangle.getTop().getTranslated(0, execSpec_Rectangle.height), 
				anchoringSource, srcAnchor.getTranslated(0, execSpec_Rectangle.height));
		
		assertView(dia, execSpec, execSpecType, ViewUtilities.getViewForElement(dia, execSpec.getCovereds().get(0)),
				execSpec_Rectangle);
		
		
/*
*/
		
	}
	
	private static final String getElementName(Element el) {
		if (el instanceof NamedElement)
			return ((NamedElement) el).getName();
		else
			return el.eClass().getName();
	}
}
