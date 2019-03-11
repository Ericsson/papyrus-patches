/*****************************************************************************
 * Copyright (c) 2008, 2014 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
 *  Camille Letavernier (CEA LIST) camille.letavernier@cea.fr - Style implementation
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Add condition to set SVG Path
 *  Christian W. Damus - bug 451230
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.editpart;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;
import org.eclipse.gmf.runtime.notation.BooleanValueStyle;
import org.eclipse.gmf.runtime.notation.FillStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.datatype.GradientData;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.linklf.editparts.LinkLFBorderedShapeEditPart;
import org.eclipse.papyrus.infra.emf.appearance.helper.AppearanceHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.BorderDisplayEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.FollowSVGSymbolEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusConnectionHandleEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusPopupBarEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.PapyrusResizableShapeEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.IPapyrusNodeFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.LinkLFSVGNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.SVGNodePlateFigure;
import org.eclipse.papyrus.infra.gmfdiag.common.service.shape.ShapeService;
import org.eclipse.papyrus.infra.gmfdiag.common.snap.PapyrusDragEditPartsTrackerEx;
import org.eclipse.swt.graphics.Color;
import org.w3c.dom.svg.SVGDocument;

/**
 * this edit part can refresh shadow and gradient.
 */
public abstract class NodeEditPart extends LinkLFBorderedShapeEditPart implements IPapyrusEditPart {

	protected SVGNodePlateFigure svgNodePlate;

	protected IFigure shape;

	/**
	 *
	 * Constructor.
	 *
	 * @param view
	 */
	public NodeEditPart(View view) {
		super(view);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected NodeFigure createMainFigure() {
		return createNodeFigure();
	}

	/**
	 * Adds the border item figure to the border item container with a locator.
	 * 
	 * @param borderItemContainer
	 *            the figure to which the border item figure is added
	 * @param borderItemEditPart
	 *            the border item editpart from which to retrieve the border
	 *            item figure and determine which locator to create
	 */
	protected void addBorderItem(IFigure borderItemContainer,
			IBorderItemEditPart borderItemEditPart) {
		if (borderItemEditPart instanceof IBorderItemWithLocator) {
			borderItemContainer.add(borderItemEditPart.getFigure(), ((IBorderItemWithLocator) borderItemEditPart).getNewBorderItemLocator(getMainFigure()));
		} else {
			super.addBorderItem(borderItemContainer, borderItemEditPart);
		}
	}

	/**
	 * Refresh the SVG Path for anchorable elements
	 */
	protected void refreshSVGPath() {
		View view = getNotationView();
		if (svgNodePlate != null) {

			if (ShapeService.getInstance().hasShapeToDisplay(getNotationView())) {
				List<SVGDocument> svgToDisplay = ShapeService.getInstance().getSVGDocumentToDisplay(getNotationView());
				int documentNumber = svgToDisplay.size();
				// If there is more than one element we don't follow the SVG path.
				if (documentNumber == 1) {
					// Set the SVG document of the SVGNodePlate to the document to display
					svgNodePlate.setSVGDocument(svgToDisplay.get(0));
				} else {
					svgNodePlate.setSVGDocument(null);
				}
			} else {
				svgNodePlate.setSVGDocument(null);
			}

			BooleanValueStyle followStyle = (BooleanValueStyle) view.getNamedStyle(NotationPackage.eINSTANCE.getBooleanValueStyle(), FollowSVGSymbolEditPolicy.FOLLOW_SVG_SYMBOL);
			// set follow SVG path
			if (followStyle != null) {
				svgNodePlate.setFollowSVGPapyrusPath(followStyle.isBooleanValue());
			}

		}
	}

	/**
	 * <p>
	 * Returns the primary shape being the View of this edit part.
	 * </p>
	 * <b>Warning</b> It should never return <code>null</code>
	 *
	 * @return the primary shape associated to this edit part.
	 */
	@Override
	public abstract IPapyrusNodeFigure getPrimaryShape();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsGradient() {
		return true;
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void handleNotificationEvent(Notification event) {
		super.handleNotificationEvent(event);

		// Update the figure when the line width changes
		Object feature = event.getFeature();
		if ((getModel() != null) && (getModel() == event.getNotifier())) {
			if (NotationPackage.eINSTANCE.getLineStyle_LineWidth().equals(feature)) {
				refreshLineWidth();
			} else if (NotationPackage.eINSTANCE.getLineTypeStyle_LineType().equals(feature)) {
				refreshLineType();
			}
		}

		// set the figure active when the feature of the of a class is true
		if (resolveSemanticElement() != null) {
			refreshShadow();
			// refresh root to avoid shadow artifact
			if (AppearanceHelper.showShadow((View) getModel()) && getRoot() != null) {
				getRoot().refresh();
			}
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshShadow();
		refreshLineType();
		refreshLineWidth();
		refreshTransparency();
		refreshSVGPath();
	}

	@Override
	protected void setLineWidth(int width) {
		if (width < 0) {
			width = 1;
		}
		getPrimaryShape().setLineWidth(width);
	}

	@Override
	protected void setLineType(int style) {
		getPrimaryShape().setLineStyle(style);
	}

	/**
	 * Override to set the transparency to the correct figure
	 */
	@Override
	protected void setTransparency(int transp) {
		getPrimaryShape().setTransparency(transp);
	}

	/**
	 * sets the back ground color of this edit part
	 *
	 * @param color
	 *            the new value of the back ground color
	 */
	@Override
	protected void setBackgroundColor(Color color) {
		getPrimaryShape().setBackgroundColor(color);
		getPrimaryShape().setIsUsingGradient(false);
		getPrimaryShape().setGradientData(-1, -1, 0);
	}

	/**
	 * Override to set the gradient data to the correct figure
	 */
	@Override
	protected void setGradient(GradientData gradient) {
		IPapyrusNodeFigure fig = getPrimaryShape();
		FillStyle style = (FillStyle) getPrimaryView().getStyle(NotationPackage.Literals.FILL_STYLE);
		if (gradient != null) {
			fig.setIsUsingGradient(true);
			fig.setGradientData(style.getFillColor(), gradient.getGradientColor1(), gradient.getGradientStyle());
		} else {
			fig.setIsUsingGradient(false);
		}
	}

	/**
	 * sets the font color
	 *
	 * @param color
	 *            the new value of the font color
	 */
	@Override
	protected void setFontColor(Color color) {
		// NULL implementation
	}

	/**
	 * sets the fore ground color of this edit part's figure
	 *
	 * @param color
	 *            the new value of the foregroundcolor
	 */
	@Override
	protected void setForegroundColor(Color color) {
		getPrimaryShape().setForegroundColor(color);
	}

	/**
	 * Refresh the shadow of the figure
	 */
	protected final void refreshShadow() {
		getPrimaryShape().setShadow(AppearanceHelper.showShadow((View) getModel()));
	}

	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(BorderDisplayEditPolicy.BORDER_DISPLAY_EDITPOLICY, new BorderDisplayEditPolicy());

		// Replace the GMF connection handle and popup bar policies with our own
		removeEditPolicy(EditPolicyRoles.CONNECTION_HANDLES_ROLE);
		installEditPolicy(EditPolicyRoles.CONNECTION_HANDLES_ROLE, new PapyrusConnectionHandleEditPolicy());
		removeEditPolicy(EditPolicyRoles.POPUPBAR_ROLE);
		installEditPolicy(EditPolicyRoles.POPUPBAR_ROLE, new PapyrusPopupBarEditPolicy());
	}

	/**
	 *
	 * @return the figure that represent the shape, this class is generated by the GMF tooling
	 */
	protected abstract IFigure createNodePlate();

	/**
	 *
	 * @return the figure that represent the shape, this class is generated by the GMF tooling
	 */
	protected abstract IFigure createNodeShape();

	/**
	 * this method installs the content pane in the node shape to add compartment for example
	 *
	 * @param nodeShape
	 * @return the figure that is the the node shape
	 */
	protected abstract IFigure setupContentPane(IFigure nodeShape);


	/**
	 * Now the method create node plate is not used,
	 * If you want to overlad it you must overload createSVGNodePlate
	 *
	 * @return the figure that allow following border of shape
	 */
	protected NodeFigure createSVGNodePlate() {
		svgNodePlate = new LinkLFSVGNodePlateFigure(this, -1, -1).withLinkLFEnabled();
		svgNodePlate.setDefaultNodePlate(createNodePlate());
		return svgNodePlate;
	}

	/**
	 * Creates figure for this edit part.
	 *
	 * Body of this method does not depend on settings in generation model
	 * so you may safely remove <i>generated</i> tag and modify it.
	 *
	 */
	protected NodeFigure createMainFigureWithSVG() {

		NodeFigure figure = createSVGNodePlate();

		figure.setLayoutManager(new StackLayout());
		shape = createNodeShape();
		figure.add(shape);
		setupContentPane(shape);
		return figure;
	}

	@Override
	public IFigure getContentPane() {
		if (shape != null) {
			IFigure contentPane = setupContentPane(shape);
			return contentPane;
		} else {
			return super.getContentPane();
		}
	}

	/**
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#getDragTracker(org.eclipse.gef.Request)
	 *
	 * @param request
	 * @return
	 */
	@Override
	public DragTracker getDragTracker(final Request request) {
		return new PapyrusDragEditPartsTrackerEx(this, true, false, false);
	}

	/**
	 * TODO : remove this override when the bug will be fixed
	 * See Bug 424943 ResizableEditPolicy#getResizeCommand duplicates request ignoring some request values
	 *
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart#getPrimaryDragEditPolicy()
	 *
	 * @return
	 */
	@Override
	public EditPolicy getPrimaryDragEditPolicy() {
		EditPolicy policy = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		return policy != null ? policy : new PapyrusResizableShapeEditPolicy();
	}
}
