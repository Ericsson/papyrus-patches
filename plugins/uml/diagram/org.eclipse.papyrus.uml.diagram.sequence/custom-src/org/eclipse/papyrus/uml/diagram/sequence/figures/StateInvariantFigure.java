/*****************************************************************************
 * Copyright (c) 2017 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Celine Jansens - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.OrderedLayout;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.text.TextFlowEx;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.uml.diagram.common.figure.node.CenteredWrappedLabel;
import org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IMultilineEditableFigure;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure;
import org.eclipse.swt.graphics.Image;

/**
 * Class extracted from the Custom State Invariant edit part
 *
 * @since 3.0
 *
 */
public class StateInvariantFigure extends ContinuationFigure implements ILabelFigure, IMultilineEditableFigure, IPapyrusNodeUMLElementFigure {

	protected static final String LEFT_BRACE = "{";

	protected static final String RIGHT_BRACE = "}";

	/**
	 * The transparency of this shape in percent.
	 * Must be in [0, 100] range.
	 */
	private int transparency = 0;

	private FlowPage constraintContentContainer;

	private TextFlowEx constraintContent;

	private CenteredWrappedLabel fFigureContinuationNameLabel;

	private PapyrusWrappingLabel stereotypesLabel;

	private Label stereotypePropertiesLabel;

	private IFigure labelContainer;

	private IMapMode mapMode;

	/**
	 * Constructor.
	 *
	 */
	public StateInvariantFigure(IMapMode map) {
		super();
		getChildren().clear();
		this.mapMode = map;
		setBorder(new MarginBorder(8));
		RoundedRectangle contents = new RoundedRectangle();
		contents.setCornerDimensions(new Dimension(mapMode.DPtoLP(50), mapMode.DPtoLP(50)));
		contents.setOutline(false);
		this.add(contents);
		// Name figure
		fFigureContinuationNameLabel = new CenteredWrappedLabel();
		contents.add(fFigureContinuationNameLabel);

		// Invariant figure
		constraintContentContainer = new FlowPage();
		constraintContentContainer.setOpaque(false);
		constraintContentContainer.setHorizontalAligment(PositionConstants.CENTER);
		constraintContent = new TextFlowEx("");
		constraintContentContainer.add(constraintContent);
		contents.add(constraintContentContainer);

		// Contents layout.
		ToolbarLayout layout = new ToolbarLayout(false);
		layout.setStretchMinorAxis(true);
		layout.setMinorAlignment(OrderedLayout.ALIGN_CENTER);
		contents.setLayoutManager(layout);
		labelContainer = contents;
	}

	public FlowPage getConstraintContentContainer() {
		return constraintContentContainer;
	}

	@Override
	public CenteredWrappedLabel getFigureContinuationNameLabel() {
		return fFigureContinuationNameLabel;
	}

	@Override
	public IFigure getInvariantFigure() {
		return this;
	}

	@Override
	protected void fillShape(Graphics graphics) {
		graphics.pushState();
		applyTransparency(graphics);
		graphics.fillRoundRectangle(getBounds(), corner.width, corner.height);
		graphics.popState();
	}

	/**
	 * Returns transparency value (belongs to [0, 100] interval)
	 *
	 * @return transparency
	 * @since 1.2
	 */
	public int getTransparency() {
		return transparency;
	}

	/**
	 * Sets the transparency if the given parameter is in [0, 100] range
	 *
	 * @param transparency
	 *            The transparency to set
	 * @since 1.2
	 */
	public void setTransparency(int transparency) {
		if (transparency != this.transparency && transparency >= 0 && transparency <= 100) {
			this.transparency = transparency;
			repaint();
		}
	}

	/**
	 * Converts transparency value from percent range [0, 100] to alpha range
	 * [0, 255] and applies converted value. 0% corresponds to alpha 255 and
	 * 100% corresponds to alpha 0.
	 *
	 * @param g
	 *            The Graphics used to paint
	 * @since 1.2
	 */
	protected void applyTransparency(Graphics g) {
		g.setAlpha(255 - transparency * 255 / 100);
	}

	@Override
	public void setText(String text) {
		if (constraintContent != null) {
			constraintContent.setText(LEFT_BRACE + text + RIGHT_BRACE);
		}
	}

	@Override
	public String getText() {
		if (constraintContent != null) {
			return constraintContent.getText();
		}
		return null;
	}

	@Override
	public void setIcon(Image icon) {

	}

	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public Point getEditionLocation() {
		if (constraintContentContainer != null) {
			return constraintContentContainer.getLocation();
		}
		return null;
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusUMLElementFigure#setStereotypeDisplay(java.lang.String, org.eclipse.swt.graphics.Image)
	 *
	 * @param stereotypes
	 * @param image
	 */

	@Override
	public void setStereotypeDisplay(String stereotypes, Image image) {
		if (stereotypes == null || stereotypes.trim().equals("")) {
			if (stereotypesLabel != null) {
				labelContainer.remove(stereotypesLabel);
			}
			stereotypesLabel = null;
		} else {
			if (stereotypesLabel == null) {
				stereotypesLabel = new PapyrusWrappingLabel(stereotypes, image);
				labelContainer.add(stereotypesLabel, 0);
			} else {
				stereotypesLabel.setText(stereotypes);
				stereotypesLabel.setIcon(image);
			}
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#setStereotypePropertiesInBrace(java.lang.String)
	 *
	 * @param stereotypeProperties
	 */

	@Override
	public void setStereotypePropertiesInBrace(String stereotypeProperties) {
		if (stereotypeProperties == null || stereotypeProperties.trim().equals("")) {
			if (stereotypePropertiesLabel != null) {
				labelContainer.remove(stereotypePropertiesLabel);
			}
			stereotypePropertiesLabel = null;
		} else {
			if (stereotypePropertiesLabel == null) {
				stereotypePropertiesLabel = new Label();
				int index = labelContainer.getChildren().indexOf(stereotypesLabel);
				labelContainer.add(stereotypePropertiesLabel, index + 1);
			}
			stereotypePropertiesLabel.setText(LEFT_BRACE + stereotypeProperties + RIGHT_BRACE);
		}
	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#setStereotypePropertiesInCompartment(java.lang.String)
	 *
	 * @param stereotypeProperties
	 */

	@Override
	public void setStereotypePropertiesInCompartment(String stereotypeProperties) {

	}

	/**
	 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#getStereotypesLabel()
	 *
	 * @return
	 */

	@Override
	public PapyrusWrappingLabel getStereotypesLabel() {
		return stereotypesLabel;
	}
}


