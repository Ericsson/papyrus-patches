/*****************************************************************************
 * (c) Copyright 2018 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraphcommand;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageAsyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageCreateEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageDeleteEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.MessageSyncEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities.EdgeSide;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.UmlAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewConstants;
import org.eclipse.uml2.uml.DestructionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageSort;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageTest extends BaseTest {

	private Lifeline[] createLifelines(Diagram diagram, InteractionGraphCommandHelper helper, int nLifelines, Rectangle[] lifelineRects) throws ExecutionException {
		Lifeline[] lfs = new Lifeline[nLifelines];
		
		for (int i=0; i<nLifelines; i++) {
			lifelineRects[i] = new Rectangle(40+ (i*200), 10, 100, -1);
			lfs[i] = helper.addLifeline("Lf_" + i, lifelineRects[i]);
			Assert.assertNotEquals(null, lfs[i]);		
			editor.flushDisplayEvents();
		}
				
		for (int i=0; i<nLifelines; i++) {
		lifelineRects[i] = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lfs[i], lifelineRects[i]);
		ViewAssert.assertView(diagram, lfs[i], 
				LifelineEditPart.VISUAL_ID, 
				ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
				lifelineRects[i]);
		}
		
		Rectangle[] newLifelineRects = Arrays.asList(lfs).stream().
				map(d->ViewUtilities.getViewForElement(diagram, d)).				
				map(d->ViewUtilities.getBounds(helper.getViewer(), d)).
				toArray(d->new Rectangle[d]);
		System.arraycopy(newLifelineRects,0,lifelineRects,0,lifelineRects.length);

		return lfs;
	}

	@Test
	public void message007_CreateAsyncMessage() throws ExecutionException {
		test(this::createAsyncMessageAndLifelines);
	}
	
	private Message[] createAsyncMessageAndLifelines(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle[] lifelines_rect = new Rectangle[2];
		Lifeline[] lifelines = createLifelines(diagram, helper, 2, lifelines_rect);
		
		return createAsyncMessage(diagram, helper, lifelines, lifelines_rect);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Message[] createAsyncMessage(Diagram diagram, InteractionGraphCommandHelper helper, Lifeline[] lifelines, Rectangle[] lifelines_rect) throws ExecutionException {
		List<InteractionFragment> fragments = new ArrayList<>(helper.getInteraction().getFragments());
		List<InteractionFragment>[] fragmentsLifelines = new List[2];
		fragmentsLifelines[0] = new ArrayList<InteractionFragment>(lifelines[0].getCoveredBys());
		fragmentsLifelines[1] = new ArrayList<InteractionFragment>(lifelines[1].getCoveredBys());
		
		int y = offsetY + 80;
		Point msg1_send = new Point(lifelines_rect[0].getCenter().x, y);
		Point msg1_recv = new Point(lifelines_rect[1].getCenter().x, y);
		Message msg1 = helper.addMessage("Message1", 
				MessageSort.ASYNCH_SIGNAL_LITERAL, 
				lifelines[0], msg1_send, 
				lifelines[1], msg1_recv);
		Assert.assertNotEquals(null, msg1);		
		editor.flushDisplayEvents();
	
		y += 40;
		Point msg2_send = new Point(lifelines_rect[1].getCenter().x, y);
		Point msg2_recv = new Point(lifelines_rect[0].getCenter().x, y);
		Message msg2 = helper.addMessage("Message1", 
				MessageSort.ASYNCH_SIGNAL_LITERAL, 
				lifelines[1], msg2_send, 
				lifelines[0], msg2_recv);
		Assert.assertNotEquals(null, msg2);		
		editor.flushDisplayEvents();

		ViewAssert.assertEdge(diagram, msg1, MessageAsyncEditPart.VISUAL_ID, 
				lifelines[0], msg1_send, lifelines[1], msg1_recv);
		
		ViewAssert.assertEdge(diagram, msg2, MessageAsyncEditPart.VISUAL_ID, 
				lifelines[1], msg2_send, lifelines[0], msg2_recv);
		
		fragmentsLifelines[0].addAll(Arrays.asList((InteractionFragment)msg1.getSendEvent(), (InteractionFragment)msg2.getReceiveEvent()));
		fragmentsLifelines[1].addAll(Arrays.asList((InteractionFragment)msg1.getReceiveEvent(), (InteractionFragment)msg2.getSendEvent()));
		InteractionFragment[][] fragment_by_lifeline = {
				fragmentsLifelines[0].toArray(new InteractionFragment[0]),
				fragmentsLifelines[1].toArray(new InteractionFragment[1]),				
		};
		
		fragments.addAll((List)Arrays.asList(
				msg1.getSendEvent(), msg1.getReceiveEvent(),
				msg2.getSendEvent(), msg2.getReceiveEvent()));
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				fragments.toArray(new InteractionFragment[0]), 
				lifelines, fragment_by_lifeline);
		
		return new Message[]{msg1,msg2};
	}

	@Test
	public void message021_DeleteAsyncMessage() throws ExecutionException {
		test(this::deleteAsyncMessage);
	}

	private boolean deleteAsyncMessage(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Message[] msgs = createAsyncMessageAndLifelines(diagram,helper);
		Edge msgView1 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[0]);
		Edge msgView2 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[1]);
		
		Point msgSrc1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Source);
		Point msgTrg1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Target);
		
		Assert.assertTrue("Delete msg " + msgs[0].getName(), helper.deleteMessage(msgs[0]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[0].getName() + " View", msgView1.eContainer());
		Assert.assertNull("Delete msg " + msgs[0].getName() + " Element", msgs[0].getOwner());
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getSendEvent(), msgTrg1);
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getReceiveEvent(), msgSrc1);
				
		Assert.assertTrue("Delete msg " + msgs[1].getName(), helper.deleteMessage(msgs[1]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[1].getName() + " View", msgView2.eContainer());
		Assert.assertNull("Delete msg " + msgs[1].getName() + " Element", msgs[1].getOwner());

		InteractionFragment[][] fragment_by_lifeline = {
				{}, {}
			};
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				new InteractionFragment[0], 
				helper.getInteraction().getLifelines().toArray(new Lifeline[0]), 
				fragment_by_lifeline);
			
		return true;
	}
	
	@Test
	public void message008_CreateSyncMessage() throws ExecutionException {
		test(this::createSyncMessageAndLifelines);
	}
	
	private Message[] createSyncMessageAndLifelines(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle[] lifelines_rect = new Rectangle[2];
		Lifeline[] lifelines = createLifelines(diagram, helper, 2, lifelines_rect);
		
		return createSyncMessage(diagram, helper, lifelines, lifelines_rect);
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Message[] createSyncMessage(Diagram diagram, InteractionGraphCommandHelper helper, Lifeline[] lifelines, Rectangle[] lifelines_rect) throws ExecutionException {
		List<InteractionFragment> fragments = new ArrayList<>(helper.getInteraction().getFragments());
		List<InteractionFragment>[] fragmentsLifelines = new List[2];
		fragmentsLifelines[0] = new ArrayList<InteractionFragment>(lifelines[0].getCoveredBys());
		fragmentsLifelines[1] = new ArrayList<InteractionFragment>(lifelines[1].getCoveredBys());
		
		int y = offsetY + 80;

		Point msg1_send = new Point(lifelines_rect[0].getCenter().x, y);
		Point msg1_recv = new Point(lifelines_rect[1].getCenter().x, y);
		Message msg1 = helper.addMessage("Message1", 
				MessageSort.SYNCH_CALL_LITERAL, 
				lifelines[0], msg1_send, 
				lifelines[1], msg1_recv);
		Assert.assertNotEquals(null, msg1);		
		editor.flushDisplayEvents();
	
		Point msg2_send = new Point(lifelines_rect[1].getCenter().x, y+80);
		Point msg2_recv = new Point(lifelines_rect[0].getCenter().x, y+80);
		Message msg2 = helper.addMessage("Message2", 
				MessageSort.SYNCH_CALL_LITERAL, 
				lifelines[1], msg2_send, 
				lifelines[0], msg2_recv);
		Assert.assertNotEquals(null, msg2);		
		editor.flushDisplayEvents();

		// Message Group 1
		
		ExecutionSpecification execSpec1 = lifelines[1].getCoveredBys().stream().
				filter(ExecutionSpecification.class::isInstance).map(ExecutionSpecification.class::cast).
				findFirst().orElse(null);

		Message replyMessage1 = lifelines[0].getCoveredBys().stream().
				filter(MessageEnd.class::isInstance).map(MessageEnd.class::cast).
				map(d->d.getMessage()).filter(d->d.getMessageSort() == MessageSort.REPLY_LITERAL).
				findFirst().orElse(null);

		ViewAssert.assertMessageGroup(diagram, msg1, MessageSyncEditPart.VISUAL_ID, 
				lifelines[0], msg1_send, execSpec1, BehaviorExecutionSpecificationEditPart.VISUAL_ID, 
				new Rectangle(msg1_recv.getTranslated(-10, 0),new Dimension(20,40)), replyMessage1);

		UmlAssert.assertMessageGroup(msg1, MessageSort.ASYNCH_CALL_LITERAL, MessageKind.COMPLETE_LITERAL, 
				lifelines[0], lifelines[1], execSpec1, replyMessage1);

		// Message Group 2

		ExecutionSpecification execSpec2 = lifelines[0].getCoveredBys().stream().
				filter(ExecutionSpecification.class::isInstance).map(ExecutionSpecification.class::cast).
				findFirst().orElse(null);

		Message replyMessage2 = lifelines[0].getCoveredBys().stream().
				filter(MessageEnd.class::isInstance).map(MessageEnd.class::cast).
				map(d->d.getMessage()).filter(d->d.getMessageSort() == MessageSort.REPLY_LITERAL).
				toArray(d->new Message[d])[1];

		ViewAssert.assertMessageGroup(diagram, msg2, MessageSyncEditPart.VISUAL_ID, 
				lifelines[1], msg2_send, execSpec2, BehaviorExecutionSpecificationEditPart.VISUAL_ID, 
				new Rectangle(msg2_recv.getTranslated(-10, 0),new Dimension(20,40)), replyMessage2);

		UmlAssert.assertMessageGroup(msg2, MessageSort.ASYNCH_CALL_LITERAL, MessageKind.COMPLETE_LITERAL, 
				lifelines[1], lifelines[0], execSpec2, replyMessage2);
					
		fragmentsLifelines[0].addAll(Arrays.asList((InteractionFragment)msg1.getSendEvent(), (InteractionFragment)replyMessage1.getReceiveEvent(), 
				(InteractionFragment)msg2.getReceiveEvent(), execSpec2, (InteractionFragment)replyMessage2.getSendEvent()));
		fragmentsLifelines[1].addAll(Arrays.asList((InteractionFragment)msg1.getReceiveEvent(),  execSpec1, (InteractionFragment)replyMessage1.getSendEvent(),
				(InteractionFragment)msg2.getSendEvent(), (InteractionFragment)replyMessage2.getReceiveEvent()));
		
		InteractionFragment[][] fragment_by_lifeline = {
				fragmentsLifelines[0].toArray(new InteractionFragment[0]),
				fragmentsLifelines[1].toArray(new InteractionFragment[1]),				
		};
		
		fragments.addAll((List)Arrays.asList(
				msg1.getSendEvent(), msg1.getReceiveEvent(),
				execSpec1,
				replyMessage1.getSendEvent(), replyMessage1.getReceiveEvent(),
				msg2.getSendEvent(), msg2.getReceiveEvent(),
				execSpec2,
				replyMessage2.getSendEvent(), replyMessage2.getReceiveEvent()));
		
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				fragments.toArray(new InteractionFragment[0]), 
				lifelines, fragment_by_lifeline);
		
		return new Message[]{msg1,replyMessage1, msg2, replyMessage2};

	}
	
	@Test
	@Ignore("Delete empty space after block not working!")
	public void message022_DeleteSyncMessage() throws ExecutionException {
		test(this::deleteSyncMessage);
	}

	private boolean deleteSyncMessage(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Message[] msgs = createSyncMessageAndLifelines(diagram,helper);
		ExecutionSpecification[] execSpecs = helper.getInteraction().getFragments().stream().
				filter(ExecutionSpecification.class::isInstance).
				map(ExecutionSpecification.class::cast).
				toArray(d->new ExecutionSpecification[d]);
		
		Edge msgView1 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[0]);
		Edge replyMsgView1 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[1]);
		Edge msgView2 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[2]);
		Edge replyMsgView2 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[3]);
		
		Point msgSrc1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Source);
		Point msgTrg1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Target);
		Point replyMsgSrc1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), replyMsgView1, EdgeSide.Source);
		Point replyMsgTrg1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), replyMsgView1, EdgeSide.Target);
		
		Assert.assertTrue("Delete msg " + msgs[0].getName(), helper.deleteMessage(msgs[0]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[0].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[0]));
		Assert.assertNull("Delete msg " + msgs[0].getName() + " Element", msgs[0].getOwner());
		Assert.assertNull("Delete msg " + msgs[1].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[1]));
		Assert.assertNull("Delete msg " + msgs[1].getName() + " Element", msgs[1].getOwner());
		Assert.assertNull("Delete Execution Spec " + execSpecs[0].getName(), ViewUtilities.getViewForElement(diagram, execSpecs[0]));

		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[2], 	msgs[2].getSendEvent(), msgTrg1);
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[2], 	msgs[2].getReceiveEvent(), msgSrc1);
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[3], 	msgs[3].getSendEvent(), replyMsgTrg1);
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[3], 	msgs[3].getReceiveEvent(), replyMsgSrc1);
		
		
		Assert.assertTrue("Delete msg " + msgs[2].getName(), helper.deleteMessage(msgs[2]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[2].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[2]));
		Assert.assertNull("Delete msg " + msgs[2].getName() + " Element", msgs[2].getOwner());
		Assert.assertNull("Delete msg " + msgs[2].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[3]));
		Assert.assertNull("Delete msg " + msgs[2].getName() + " Element", msgs[3].getOwner());
		Assert.assertNull("Delete Execution Spec " + execSpecs[1].getName(), ViewUtilities.getViewForElement(diagram, execSpecs[1]));

		InteractionFragment[][] fragment_by_lifeline = {
				{}, {}
			};
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				new InteractionFragment[0], 
				helper.getInteraction().getLifelines().toArray(new Lifeline[0]), 
				fragment_by_lifeline);
		return true;
	}
	
	@Test
	public void message014_CreateCreateMessage() throws ExecutionException {
		test(this::createCreateMessageAndLifelines);
	}
	
	private Message[] createCreateMessageAndLifelines(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle[] lifelines_rect = new Rectangle[3];
		Lifeline[] lifelines = createLifelines(diagram, helper, 3, lifelines_rect);
		return createCreateMessage(diagram, helper, lifelines, lifelines_rect);
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Message[] createCreateMessage(Diagram diagram, InteractionGraphCommandHelper helper, Lifeline[] lifelines, Rectangle[] lifelines_rect) throws ExecutionException {
		List<InteractionFragment> fragments = new ArrayList<>(helper.getInteraction().getFragments());
		List<InteractionFragment>[] fragmentsLifelines = new List[3];
		fragmentsLifelines[0] = new ArrayList<InteractionFragment>(lifelines[0].getCoveredBys());
		fragmentsLifelines[1] = new ArrayList<InteractionFragment>(lifelines[1].getCoveredBys());
		fragmentsLifelines[2] = new ArrayList<InteractionFragment>(lifelines[2].getCoveredBys());
		
		int y = offsetY + 80;

		Point msg1_send = new Point(lifelines_rect[1].getCenter().x, y);
		Point msg1_recv = new Point(lifelines_rect[2].getCenter().x, y);
		Message msg1 = helper.addMessage("Message1", 
				MessageSort.CREATE_MESSAGE_LITERAL, 
				lifelines[1], msg1_send, 
				lifelines[2], msg1_recv);
		Assert.assertNotEquals(null, msg1);		
		editor.flushDisplayEvents();
	
		Point msg2_send = new Point(lifelines_rect[1].getCenter().x, y+60);
		Point msg2_recv = new Point(lifelines_rect[0].getCenter().x, y+60);
		Message msg2 = helper.addMessage("Message1", 
				MessageSort.CREATE_MESSAGE_LITERAL, 
				lifelines[1], msg2_send, 
				lifelines[0], msg2_recv);
		Assert.assertNotEquals(null, msg2);		
		editor.flushDisplayEvents();

		ViewAssert.assertEdge(diagram, msg1, MessageCreateEditPart.VISUAL_ID, 
				lifelines[1], msg1_send, lifelines[2], msg1_recv);
		
		ViewAssert.assertEdge(diagram, msg2, MessageCreateEditPart.VISUAL_ID, 
				lifelines[1], msg2_send, lifelines[0], msg2_recv);
		
		int off = msg1_recv.y - lifelines_rect[2].y - 
				ViewConstants.getLifelineHeaderHeight(helper.getViewer(), lifelines[2]) / 2;
		Rectangle lifeRectangle_after = lifelines_rect[2].getCopy();
		lifeRectangle_after.resize(0,-off);
		lifeRectangle_after.translate(0, off);
		ViewAssert.assertViewBounds(diagram, lifelines[2], lifeRectangle_after);

		off = msg2_recv.y - lifelines_rect[0].y - 
				ViewConstants.getLifelineHeaderHeight(helper.getViewer(), lifelines[0]) / 2;
		lifeRectangle_after = lifelines_rect[0].getCopy();
		lifeRectangle_after.resize(0,-off);
		lifeRectangle_after.translate(0, off);
		ViewAssert.assertViewBounds(diagram, lifelines[0], lifeRectangle_after);
		
		fragmentsLifelines[0].addAll(Arrays.asList((InteractionFragment)msg2.getReceiveEvent()));
		fragmentsLifelines[1].addAll(Arrays.asList((InteractionFragment)msg1.getSendEvent(), (InteractionFragment)msg2.getSendEvent()));
		fragmentsLifelines[2].addAll(Arrays.asList((InteractionFragment)msg1.getReceiveEvent()));
		
		InteractionFragment[][] fragment_by_lifeline = {
				fragmentsLifelines[0].toArray(new InteractionFragment[0]),
				fragmentsLifelines[1].toArray(new InteractionFragment[1]),				
				fragmentsLifelines[2].toArray(new InteractionFragment[1]),				
		};
		
		fragments.addAll((List)Arrays.asList(
				msg1.getSendEvent(), msg1.getReceiveEvent(),
				msg2.getSendEvent(), msg2.getReceiveEvent()));
		
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				fragments.toArray(new InteractionFragment[0]), 
				lifelines, fragment_by_lifeline);
		
		Rectangle[] newLifelineRects = Arrays.asList(lifelines).stream().
				map(d->ViewUtilities.getViewForElement(diagram, d)).				
				map(d->ViewUtilities.getBounds(helper.getViewer(), d)).
				toArray(d->new Rectangle[d]);
		System.arraycopy(newLifelineRects,0,lifelines_rect,0,lifelines_rect.length);

		return new Message[]{msg1,msg2};		

	}	

	@Test
	@Ignore("Delete empty space after block not working!")
	public void message024_DeleteCreateMessage() throws ExecutionException {
		test(this::deleteCreateMessage);
	}

	private boolean deleteCreateMessage(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Message[] msgs = createCreateMessageAndLifelines(diagram,helper);
		Lifeline[] lifelines = helper.getInteraction().getLifelines().toArray(new Lifeline[0]);
		Rectangle[] lifelineRects = Arrays.asList(lifelines).stream().
				map(d->ViewUtilities.getViewForElement(diagram, d)).				
				map(d->ViewUtilities.getBounds(helper.getViewer(), d)).
				toArray(d->new Rectangle[d]);
		
		Edge msgView1 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[0]);
		Edge msgView2 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[1]);
		
		Point msgSrc1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Source);
		Point msgTrg1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Target);
		Point msgSrc2 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView2, EdgeSide.Source);
		Point msgTrg2 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView2, EdgeSide.Target);
		
		Assert.assertTrue("Delete msg " + msgs[0].getName(), helper.deleteMessage(msgs[0]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[0].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[0]));
		Assert.assertNull("Delete msg " + msgs[0].getName() + " Element", msgs[0].getOwner());

		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getSendEvent(), msgSrc1);
		Point p = msgTrg2.getCopy();
		p.y = msgSrc1.y; 
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getReceiveEvent(), p);		
		ViewAssert.assertViewBounds(diagram, lifelines[2], new Rectangle(
				lifelineRects[2].x, lifelineRects[1].y,
				lifelineRects[2].width, lifelineRects[1].height));
		int delta = msgTrg2.y - msgSrc1.y;
		ViewAssert.assertViewBounds(diagram, lifelines[0], new Rectangle(
				lifelineRects[0].x, lifelineRects[0].y - delta,
				lifelineRects[0].width, lifelineRects[0].height + delta));		
		
		Assert.assertTrue("Delete msg " + msgs[1].getName(), helper.deleteMessage(msgs[1]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[1].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[1]));
		Assert.assertNull("Delete msg " + msgs[1].getName() + " Element", msgs[1].getOwner());
		ViewAssert.assertViewBounds(diagram, lifelines[0], new Rectangle(
				lifelineRects[0].x, lifelineRects[1].y,
				lifelineRects[0].width, lifelineRects[1].height));		

		InteractionFragment[][] fragment_by_lifeline = {
				{}, {}, {}
			};
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				new InteractionFragment[0], 
				lifelines, 
				fragment_by_lifeline);
		return true;
	}

	@Test
	public void message012_CreateDeleteMessage() throws ExecutionException {
		test(this::createDeleteMessageAndLifelines);
	}
	
	private Message[] createDeleteMessageAndLifelines(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle[] lifelines_rect = new Rectangle[3];
		Lifeline[] lifelines = createLifelines(diagram, helper, 3, lifelines_rect);
		return createDeleteMessage(diagram, helper, lifelines, lifelines_rect);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Message[] createDeleteMessage(Diagram diagram, InteractionGraphCommandHelper helper, Lifeline[] lifelines, Rectangle[] lifelines_rect) throws ExecutionException {
		List<InteractionFragment> fragments = new ArrayList<>(helper.getInteraction().getFragments());
		List<InteractionFragment>[] fragmentsLifelines = new List[3];
		fragmentsLifelines[0] = new ArrayList<InteractionFragment>(lifelines[0].getCoveredBys());
		fragmentsLifelines[1] = new ArrayList<InteractionFragment>(lifelines[1].getCoveredBys());
		fragmentsLifelines[2] = new ArrayList<InteractionFragment>(lifelines[2].getCoveredBys());
		
		int y = offsetY + 80;
		Point msg1_send = new Point(lifelines_rect[1].getCenter().x, y);
		Point msg1_recv = new Point(lifelines_rect[2].getCenter().x, y);
		Message msg1 = helper.addMessage("Message1", 
				MessageSort.DELETE_MESSAGE_LITERAL, 
				lifelines[1], msg1_send, 
				lifelines[2], msg1_recv);
		Assert.assertNotEquals(null, msg1);		
		editor.flushDisplayEvents();
	
		Point msg2_send = new Point(lifelines_rect[1].getCenter().x, y+60);
		Point msg2_recv = new Point(lifelines_rect[0].getCenter().x, y+60);
		Message msg2 = helper.addMessage("Message1", 
				MessageSort.DELETE_MESSAGE_LITERAL, 
				lifelines[1], msg2_send, 
				lifelines[0], msg2_recv);
		Assert.assertNotEquals(null, msg2);		
		editor.flushDisplayEvents();

		MessageEnd dos1 = msg1.getReceiveEvent();		
		assertThat("Destruction Occurrence Specification:", dos1, (Matcher)isA(DestructionOccurrenceSpecification.class));
		
		ViewAssert.assertEdge(diagram, msg1, MessageDeleteEditPart.VISUAL_ID, 
				lifelines[1], msg1_send, dos1, msg1_recv);
		
		MessageEnd dos2 = msg2.getReceiveEvent();		
		assertThat("Destruction Occurrence Specification:", dos2, (Matcher)isA(DestructionOccurrenceSpecification.class));
 
		ViewAssert.assertEdge(diagram, msg2, MessageDeleteEditPart.VISUAL_ID, 
				lifelines[1], msg2_send, dos2, msg2_recv);
		
		Rectangle lifeRectangle_after = lifelines_rect[2].getCopy();
		lifeRectangle_after = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifelines[2], lifeRectangle_after);
		lifeRectangle_after.height = msg1_recv.y - lifeRectangle_after.y;
		ViewAssert.assertViewBounds(diagram, lifelines[2], lifeRectangle_after);

		lifeRectangle_after = lifelines_rect[0].getCopy();
		lifeRectangle_after = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifelines[0], lifeRectangle_after);
		lifeRectangle_after.height = msg2_recv.y - lifeRectangle_after.y;
		ViewAssert.assertViewBounds(diagram, lifelines[0], lifeRectangle_after);
		

		fragmentsLifelines[0].addAll(Arrays.asList((InteractionFragment)dos2));
		fragmentsLifelines[1].addAll(Arrays.asList((InteractionFragment)msg1.getSendEvent(), (InteractionFragment)msg2.getSendEvent()));
		fragmentsLifelines[2].addAll(Arrays.asList((InteractionFragment)dos1));
		
		InteractionFragment[][] fragment_by_lifeline = {
				fragmentsLifelines[0].toArray(new InteractionFragment[0]),
				fragmentsLifelines[1].toArray(new InteractionFragment[1]),				
				fragmentsLifelines[2].toArray(new InteractionFragment[1]),				
		};
		
		fragments.addAll((List)Arrays.asList(
				msg1.getSendEvent(), dos1,
				msg2.getSendEvent(), dos2));
		
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				fragments.toArray(new InteractionFragment[0]), 
				lifelines, fragment_by_lifeline);
		
		Rectangle[] lifelineRects = Arrays.asList(lifelines).stream().
				map(d->ViewUtilities.getViewForElement(diagram, d)).				
				map(d->ViewUtilities.getBounds(helper.getViewer(), d)).
				toArray(d->new Rectangle[d]);
		System.arraycopy(lifelineRects,0,lifelines_rect,0,lifelines_rect.length);

		return new Message[]{msg1,msg2};		
	}	

	@Test
	public void message025_DeleteDeleteMessage() throws ExecutionException {
		test(this::deleteDeleteMessage);
	}
	
	private boolean deleteDeleteMessage(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Message[] msgs = createDeleteMessageAndLifelines(diagram,helper);
		Lifeline[] lifelines = helper.getInteraction().getLifelines().toArray(new Lifeline[0]);
		Rectangle[] lifelineRects = Arrays.asList(lifelines).stream().
				map(d->ViewUtilities.getViewForElement(diagram, d)).				
				map(d->ViewUtilities.getBounds(helper.getViewer(), d)).
				toArray(d->new Rectangle[d]);
		
		Edge msgView1 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[0]);
		Edge msgView2 = (Edge)ViewUtilities.getViewForElement(diagram, msgs[1]);
		
		Point msgSrc1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Source);
		Point msgTrg1 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView1, EdgeSide.Target);
		Point msgSrc2 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView2, EdgeSide.Source);
		Point msgTrg2 = ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgView2, EdgeSide.Target);
		
		Assert.assertTrue("Delete msg " + msgs[0].getName(), helper.deleteMessage(msgs[0]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[0].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[0]));
		Assert.assertNull("Delete msg " + msgs[0].getName() + " Element", msgs[0].getOwner());
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getSendEvent(), msgSrc1);
		Point p = msgTrg2.getCopy();
		p.y = msgSrc1.y; 
		ViewAssert.assertEdgeAnchorLocation(diagram, msgs[1], 	msgs[1].getReceiveEvent(), p);		
		
		ViewAssert.assertViewBounds(diagram, lifelines[2], new Rectangle(
				lifelineRects[2].x, lifelineRects[2].y,
				lifelineRects[2].width, lifelineRects[1].height));		
		int delta = msgTrg2.y - msgSrc1.y;
		ViewAssert.assertViewBounds(diagram, lifelines[0], new Rectangle(
				lifelineRects[0].x, lifelineRects[0].y,
				lifelineRects[0].width, lifelineRects[0].height - delta));		
		
		Assert.assertTrue("Delete msg " + msgs[1].getName(), helper.deleteMessage(msgs[1]));
		editor.flushDisplayEvents();
		Assert.assertNull("Delete msg " + msgs[1].getName() + " View", ViewUtilities.getViewForElement(diagram, msgs[1]));
		Assert.assertNull("Delete msg " + msgs[1].getName() + " Element", msgs[1].getOwner());
		ViewAssert.assertViewBounds(diagram, lifelines[0], new Rectangle(
				lifelineRects[0].x, lifelineRects[1].y,
				lifelineRects[0].width, lifelineRects[1].height));		

		InteractionFragment[][] fragment_by_lifeline = {
				{}, {}, {}
			};
		UmlAssert.assertFragmentOrder(helper.getInteraction(), 
				new InteractionFragment[0], 
				lifelines, 
				fragment_by_lifeline);
		return true;		
	}
	
	@Test
	public void message016_NudgeAsynchMessage() throws ExecutionException {
		test(this::nudgeAsynchMessage);
	}

	private boolean nudgeAsynchMessage(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle[] lf_rects = new Rectangle[3];
		Lifeline[] lfs = createLifelines(diagram, helper, 3, lf_rects);
		
		Rectangle[][] lifelines_rects = {
				{lf_rects[0], lf_rects[1]},
				{lf_rects[1], lf_rects[2]},
				{lf_rects[0], lf_rects[2]}
		};
		
		Lifeline[][] lifelines = {
				{lfs[0], lfs[1]},
				{lfs[1], lfs[2]},
				{lfs[0], lfs[2]}				
		};

		List<Message> msgs = new ArrayList<>();
		List<Edge> msgViews = new ArrayList<>();
		List<Point> msgSrcAnchors = new ArrayList<>();
		List<Point> msgTrgAnchors = new ArrayList<>();

		// Adding Asynch Message
		offsetY += 20;
		msgs.addAll(Arrays.asList(createCreateMessage(diagram,helper, lfs, lf_rects)));		
		offsetY += 120;
		msgs.addAll(Arrays.asList(createAsyncMessage(diagram,helper, lifelines[0], lifelines_rects[0])));
		offsetY += 80;
		msgs.addAll(Arrays.asList(createSyncMessage(diagram,helper, lifelines[1], lifelines_rects[1])));
		offsetY += 160;
		msgs.addAll(Arrays.asList(createDeleteMessage(diagram,helper, lfs, lf_rects)));		
		
		for (int i=0; i<msgs.size(); i+=2) {
			msgViews.add((Edge)ViewUtilities.getViewForElement(diagram, msgs.get(i)));
			msgViews.add((Edge)ViewUtilities.getViewForElement(diagram, msgs.get(i+1)));
			msgSrcAnchors.add(ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgViews.get(i), EdgeSide.Source));
			msgTrgAnchors.add(ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgViews.get(i), EdgeSide.Target));
			msgSrcAnchors.add(ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgViews.get(i+1), EdgeSide.Source));
			msgTrgAnchors.add(ViewUtilities.getAnchorLocationForView(helper.getViewer(), msgViews.get(i+1), EdgeSide.Target));
		}
		
		int nMessages[] = {2,4,0,7,6};
		int nudges[] = {40, 40, -20, 40, -20};
		
		for (int n=0; n<nMessages.length; n++) {
			helper.nudgeMessage(msgs.get(nMessages[n]), nudges[n]);
			Point p = new Point();
			for (int i=nMessages[n]; i<msgs.size(); i++) {
				Message msg = msgs.get(i);
				p.setLocation(msgSrcAnchors.get(i));
				p.translate(0, nudges[n]);
				ViewAssert.assertEdgeAnchorLocation(diagram, msg, msg.getSendEvent(), p);
				msgSrcAnchors.get(i).setLocation(p);
				
				p.setLocation(msgTrgAnchors.get(i));
				p.translate(0, nudges[n]);
				ViewAssert.assertEdgeAnchorLocation(diagram, msg, msg.getReceiveEvent(), p);
				msgTrgAnchors.get(i).setLocation(p);			
			}
		}
		
		return true;
	}
			

	@Before
	public void reset() {
		offsetY = 0;
	}
	
	private int offsetY = 0;
	
}