/*****************************************************************************
 * Copyright (c) 2017 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Remi Schnekenburger (CEA LIST) remi.schnekenburger@cea.fr - Initial API and implementation
 *  Mathieu Velten (Atos) mathieu.velten@atos.net - use commands instead of running code in post commit
 *  Philippe ROLAND (Atos) philippe.roland@atos.net - Implemented PreActions
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Move from oep.uml.diagram.com and remove aspect actions framework, see bug 512343.
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedAdapter;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeCreationTool;
import org.eclipse.gmf.runtime.diagram.ui.util.INotationType;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.swt.SWT;

/**
 * Creation tool for papyrus.
 * 
 * @since 3.0
 */
public class AspectUnspecifiedTypeCreationTool extends UnspecifiedTypeCreationTool {

	/**
	 * List of element types of which one will be created (of type <code>IElementType</code>).
	 */
	protected List<IElementType> elementTypes;

	/**
	 * Creates an AspectUnspecifiedTypeCreationTool
	 *
	 * @param elementTypes
	 *            List of element types of which one will be created (of type <code>IElementType</code>).
	 */
	public AspectUnspecifiedTypeCreationTool(List<IElementType> elementTypes) {
		super(elementTypes);
		this.elementTypes = elementTypes;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performCreation(int button) {
		antiScroll = true;
		final EditPartViewer currentViewer = getCurrentViewer();

		Command command = getCurrentCommand();

		if (command != null) {
			Command completeCommand = getCompleteCommand(command);
			setCurrentCommand(completeCommand);
			executeCurrentCommand();
		}

		Collection<?> selectableEditParts = new LinkedList<Object>(DiagramCommandStack.getReturnValues(command));
		Iterator<?> editPartsIterator = selectableEditParts.iterator();

		while (editPartsIterator.hasNext()) {
			Object editPartObject = editPartsIterator.next();
			if (editPartObject instanceof IAdaptable) {
				EditPart editPart = (EditPart) currentViewer.getEditPartRegistry().get(((IAdaptable) editPartObject).getAdapter(View.class));
				if (editPart == null || !editPart.isSelectable()) {
					editPartsIterator.remove();
				}
			}
		}

		selectAddedObject(currentViewer, selectableEditParts);

		antiScroll = false;
	}

	protected Command getCompleteCommand(Command createCommand) {
		CompoundCommand compositeCmd = new CompoundCommand("Create Element"); //$NON-NLS-1$

		compositeCmd.add(createCommand);

		CreateUnspecifiedAdapter viewAdapter = new CreateUnspecifiedAdapter();
		viewAdapter.add(getCreateRequest());

		return compositeCmd;
	}


	/**
	 * Returns the list of element types
	 *
	 * @return the list of element types
	 */
	public List<IElementType> getElementTypes() {
		return elementTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Request createTargetRequest() {
		CreateAspectUnspecifiedTypeRequest request = new CreateAspectUnspecifiedTypeRequest(getElementTypes(), getPreferencesHint());
		request.setLocation(getLocation());
		return request;
	}

	public class CreateAspectUnspecifiedTypeRequest extends CreateUnspecifiedTypeRequest {

		/**
		 * Constructor.
		 *
		 * @param elementTypes
		 * @param preferencesHint
		 */
		public CreateAspectUnspecifiedTypeRequest(List<IElementType> elementTypes, PreferencesHint preferencesHint) {
			super(elementTypes, preferencesHint);
		}

		/**
		 * Creates a <code>CreateViewRequest</code> or <code>CreateViewAndElementRequest</code> for each creation hint and
		 * adds it to the map of requests.
		 */
		@Override
		protected void createRequests() {
			for (IElementType elementType : (List<IElementType>) CreateAspectUnspecifiedTypeRequest.this.getElementTypes()) {
				Request request = null;
				if (elementType instanceof INotationType) {
					ViewDescriptor viewDescriptor = new ViewDescriptor(null, Node.class, ((INotationType) elementType).getSemanticHint(), getPreferencesHint());
					request = new CreateViewRequest(viewDescriptor);
				} else if (elementType instanceof IHintedType) {
					ViewAndElementDescriptor viewDescriptor = new ViewAndElementDescriptor(new CreateElementRequestAdapter(new CreateElementRequest(elementType)), Node.class, getGraphicalHint((IHintedType) elementType), getPreferencesHint());
					request = new CreateViewAndElementRequest(viewDescriptor);
					request.setExtendedData(getExtendedData());
				} else {
					ViewAndElementDescriptor viewDescriptor = new ViewAndElementDescriptor(new CreateElementRequestAdapter(new CreateElementRequest(elementType)), Node.class, getPreferencesHint());
					request = new CreateViewAndElementRequest(viewDescriptor);
					request.setExtendedData(getExtendedData());
				}
				if (request instanceof CreateRequest) {
					// see bug 427129: Figures newly created via the palette should be snapped to grid if "snap to grid" is activated
					((CreateRequest) request).setSnapToEnabled(!getCurrentInput().isModKeyDown(MODIFIER_NO_SNAPPING));
				}
				request.setType(getType());
				requests.put(elementType, request);
			}
		}

		/**
		 * Returns the semantic hint for the given type. In case of extended
		 * type, it returns the hint of the super type
		 *
		 * @param elementType
		 *            the hinted element type from which hint is retrieved
		 * @return
		 */
		protected String getGraphicalHint(IHintedType elementType) {
			String hint = elementType.getSemanticHint();
			return hint;
		}

	}

	@Override
	protected void createShapeAt(Point point) {
		setTargetEditPart(getCurrentViewer().getRootEditPart().getContents());
		getCreateRequest().setLocation(point);
		setCurrentCommand(getCommand());
		performCreation(0);
	}

	/**
	 * Key modifier for ignoring snap while dragging. It's CTRL on Mac, and ALT
	 * on all other platforms.
	 */
	static final int MODIFIER_NO_SNAPPING;

	static {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			MODIFIER_NO_SNAPPING = SWT.CTRL;
		} else {
			MODIFIER_NO_SNAPPING = SWT.ALT;
		}
	}

	/** a string used as key to register the initial position for creation in the parameters map of the CreateRequest */
	public static final String INITIAL_MOUSE_LOCATION_FOR_CREATION = "initialMouseLocationForCreation";

	/**
	 *
	 * @see org.eclipse.gef.tools.CreationTool#updateTargetRequest()
	 *
	 */
	@Override
	protected void updateTargetRequest() {
		super.updateTargetRequest();
		CreateRequest createRequest = getCreateRequest();

		// in some case with snap to grid, we need to know the real location (for affixed child node for example)
		@SuppressWarnings("unchecked")
		Map<Object, Object> params = createRequest.getExtendedData();
		params.put(INITIAL_MOUSE_LOCATION_FOR_CREATION, getLocation());

		// see bug 427129: Figures newly created via the palette should be snapped to grid if "snap to grid" is activated
		if (!isInState(STATE_DRAG_IN_PROGRESS) && !getCurrentInput().isModKeyDown(MODIFIER_NO_SNAPPING)) {
			// allow to do a snap to grid for creation with one click
			if (getTargetEditPart() != null) {
				SnapToHelper helper = getTargetEditPart().getAdapter(SnapToHelper.class);
				Point loq = getLocation();
				Rectangle bounds = new Rectangle(loq, loq);
				createRequest.setSnapToEnabled(!getCurrentInput().isModKeyDown(MODIFIER_NO_SNAPPING));
				createRequest.setLocation(bounds.getLocation());
				if (helper != null && createRequest.isSnapToEnabled()) {

					PrecisionRectangle baseRect = new PrecisionRectangle(bounds);
					PrecisionRectangle result = baseRect.getPreciseCopy();
					helper.snapRectangle(createRequest, PositionConstants.NORTH_WEST, baseRect, result);
					createRequest.setLocation(result.getLocation());
				}
				enforceConstraintsForSizeOnDropCreate(createRequest);
			}
		}
	}

}
