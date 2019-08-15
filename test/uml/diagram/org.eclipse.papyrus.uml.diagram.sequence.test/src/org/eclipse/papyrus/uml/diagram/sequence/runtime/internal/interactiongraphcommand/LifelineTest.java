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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.InteractionInteractionCompartmentEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.LifelineEditPart;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewAssert;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.ViewConstants;
import org.eclipse.uml2.uml.Lifeline;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Order;


public class LifelineTest extends BaseTest {
	/**
	 *   +---+  
	 *   | A |  
	 *   +---+  
	 *     |    
	 *     |    
	 *     |    
	 *     |    
	 *     |    
	 */
	@Test
	@Order(1)
	public void lifeline003_CreateLifeline() throws ExecutionException {
		test(this::createLifeline1);
	}
	
	private Lifeline createLifeline1(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle lifeline1_rect = new Rectangle(40, 10, 100, -1);
		Lifeline lifeline1 = helper.addLifeline("Lf_1", lifeline1_rect);
		Assert.assertNotEquals(null, lifeline1);		
		editor.flushDisplayEvents();
				
		
		lifeline1_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline1, lifeline1_rect);
		ViewAssert.assertView(diagram, lifeline1, 
				LifelineEditPart.VISUAL_ID, 
				ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
				lifeline1_rect);
		
		return lifeline1;
	}

	/**
	 *   +---+            +---+
	 *   | A |            | B |
	 *   +---+            +---+
	 *     |                |
	 *     |                |
	 *     |                |
	 *     |                |
	 *     |                |
	 */

	@Test
	@Order(2)
	public void lifeline003_CreateLifeline2() throws ExecutionException {
		test(this::createLifeline1, this::createLifeline2);
	}
	
	public Lifeline createLifeline2(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Rectangle lifeline2_rect = new Rectangle(160, 10, 100, -1);
		Lifeline lifeline2 = helper.addLifeline("Lf_2", lifeline2_rect);
		Assert.assertNotEquals(null, lifeline2);		
		editor.flushDisplayEvents();
		
		lifeline2_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline2, lifeline2_rect);
		ViewAssert.assertView(diagram, lifeline2, 
				LifelineEditPart.VISUAL_ID, 
				ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
				lifeline2_rect);
		return lifeline2;
	}

	/**
	 *   +---+  +---+
	 *   | B |  | A |
	 *   +---+  +---+
	 *     |      |
	 *     |      |
	 *     |      |
	 *     |      |
	 *     |      |
	 *     |      |
	 *     |      |
	 */
	@Test
	@Order(3)
	public void lifeline003_CreateLifeline_Nudge() throws ExecutionException {
		test(this::createLifeline2_Nudge);
	}
	
	public Lifeline createLifeline2_Nudge(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Lifeline lifeline1 = createLifeline1(diagram, helper);
		Rectangle lifeline1_rect = ViewUtilities.getBounds(helper.getViewer(), ViewUtilities.getViewForElement(diagram, lifeline1));
	
		Rectangle lifeline2_rect = new Rectangle(20, 10, 100, -1);
		Lifeline lifeline2 = helper.addLifeline("Lf_2", lifeline2_rect);
		Assert.assertNotEquals(null, lifeline2);		
		editor.flushDisplayEvents();
		
		lifeline1_rect.x += 100; // Nudging  
		lifeline1_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline1, lifeline1_rect);
		ViewAssert.assertView(diagram, lifeline1, 
				LifelineEditPart.VISUAL_ID, 
				ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
				lifeline1_rect);
					
		lifeline2_rect = ViewConstants.DEFAULT_LIFELINE_YPOS(helper.getViewer(), lifeline2, lifeline2_rect);
		ViewAssert.assertView(diagram, lifeline2, 
				LifelineEditPart.VISUAL_ID, 
				ViewUtilities.getViewWithType(diagram, InteractionInteractionCompartmentEditPart.VISUAL_ID),
				lifeline2_rect);
		return lifeline2;
	}
	
	
	@Test
	@Order(4)	
	public void lifeline004_NudgeLifeline1() throws ExecutionException {
		test(this::nudgeLifeline1);
	}

	public Void nudgeLifeline1(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Lifeline lifeline1 = createLifeline1(diagram, helper);
		Rectangle lifeline1_rect = ViewUtilities.getBounds(helper.getViewer(), ViewUtilities.getViewForElement(diagram, lifeline1));

		Lifeline lifeline2 = createLifeline2(diagram, helper);
		Rectangle lifeline2_rect = ViewUtilities.getBounds(helper.getViewer(), ViewUtilities.getViewForElement(diagram, lifeline2));

		helper.nudgeLifeline(lifeline1, 120);
		lifeline1_rect.x += 120; // Nudging  
		lifeline2_rect.x += 120; // Nudging  
		
		ViewAssert.assertViewsBounds(diagram, Arrays.asList(
				new SimpleEntry<>(lifeline1, lifeline1_rect), 
				new SimpleEntry<>(lifeline2, lifeline2_rect)));
		
		return null;
	}

	@Test
	@Order(5)	
	public void lifeline004_NudgeLifeline2() throws ExecutionException {
		test(this::nudgeLifeline2);
	}

	public Void nudgeLifeline2(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException {
		Lifeline lifeline1 = createLifeline1(diagram, helper);
		Rectangle lifeline1_rect = ViewUtilities.getBounds(helper.getViewer(), ViewUtilities.getViewForElement(diagram, lifeline1));

		Lifeline lifeline2 = createLifeline2(diagram, helper);
		Rectangle lifeline2_rect = ViewUtilities.getBounds(helper.getViewer(), ViewUtilities.getViewForElement(diagram, lifeline2));

		helper.nudgeLifeline(lifeline2, 120);
		lifeline2_rect.x += 120; // Nudging  
		
		ViewAssert.assertViewsBounds(diagram, Arrays.asList(
				new SimpleEntry<>(lifeline1, lifeline1_rect), 
				new SimpleEntry<>(lifeline2, lifeline2_rect)));
		
		return null;
	}
}
