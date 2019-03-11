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
 *  Vincent Lorenzo (CEA LIST)
 *  Mathieu Velten (Atos) mathieu.velten@atos.net - use commands instead of running code in post commit
 *  Philippe ROLAND (Atos) philippe.roland@atos.net - Implemented PreActions
 *  Micka�l ADAM (ALL4TEC) mickael.adam@all4tec.net - Move from oep.uml.diagram.com and remove aspect actions framework, see bug 512343.
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.core.edithelpers.CreateElementRequestAdapter;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredCreateConnectionViewCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest.ConnectionViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedAdapter;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateUnspecifiedTypeConnectionRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewAndElementRequest.ViewAndElementDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest.ViewDescriptor;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeConnectionTool;
import org.eclipse.gmf.runtime.diagram.ui.util.INotationType;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.NodeEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.ConnectionToolPreferences;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.LayoutUtils;
import org.w3c.dom.Node;

/**
 * Connection tool that adds stereotype application after creation actions.
 * 
 * @since 3.0
 */
public class AspectUnspecifiedTypeConnectionTool extends UnspecifiedTypeConnectionTool {

	private static final int STATE_CONNECTION_WAITING_END = STATE_CONNECTION_STARTED + 1;

	/**
	 * List of element types of which one will be created (of type <code>IElementType</code>).
	 */

	/** List of elements to create */
	private final List<IElementType> elementTypes;

	/**
	 * Creates an AspectUnspecifiedTypeCreationTool
	 *
	 * @param elementTypes
	 *            List of element types of which one will be created (of type <code>IElementType</code>).
	 */
	public AspectUnspecifiedTypeConnectionTool(List<IElementType> elementTypes) {
		super(elementTypes);
		this.elementTypes = elementTypes;
	}


	/**
	 * @return the elementDescriptors
	 */
	public List<IElementType> getElementTypes() {
		return elementTypes;
	}

	protected boolean handleButtonUpOneClick(int button) {
		return super.handleButtonUp(button);
	}

	/**
	 * fix source EditPart part, i.e. already create a connection to this edit part
	 *
	 * @param editPart
	 */
	public void setSourceEditPart(final EditPart editPart) {
		// only activate in single click mode
		if (!ConnectionToolPreferences.instance.isInSingleClickMode()) {
			// lock current edit part, otherwise updateTargetUnderMouse (called by handleButtonDown) would update the edit part
			lockTargetEditPart(editPart);
			setViewer(editPart.getViewer());
			handleButtonDown(1);
			unlockTargetEditPart();
			handleButtonUp(1);
		}
	}

	protected boolean handleButtonUpTwoClicks(int button) {
		setCtrlKeyDown(getCurrentInput().isControlKeyDown());

		if (isInState(STATE_CONNECTION_STARTED)) {
			setState(STATE_CONNECTION_WAITING_END);
			return false;
		} else if (isInState(STATE_CONNECTION_WAITING_END)) {
			handleCreateConnection();
		}

		setState(STATE_TERMINAL);

		if (isInState(STATE_TERMINAL | STATE_INVALID)) {
			handleFinished();
		}

		return true;
	}

	/**
	 * @see org.eclipse.gef.tools.AbstractTool#handleButtonUp(int)
	 */
	@Override
	protected boolean handleButtonUp(int button) {
		if (ConnectionToolPreferences.instance.isInSingleClickMode()) {
			return handleButtonUpOneClick(button);
		} else {
			return handleButtonUpTwoClicks(button);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createConnection() {
		List<?> selectedEditParts = getCurrentViewer().getSelectedEditParts();
		final EditPartViewer viewer = getCurrentViewer();
		// only attempt to create connection if there are two shapes selected
		if (!selectedEditParts.isEmpty()) {
			IGraphicalEditPart targetEditPart = (IGraphicalEditPart) selectedEditParts.get(selectedEditParts.size() - 1);

			for (int i = 0; i < selectedEditParts.size(); i++) {
				IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) selectedEditParts.get(i);
				if (i != 0) {
					if (sourceEditPart == targetEditPart) {
						break;
					}
				}

				// do nothing if you have at least one edge element, in fact the following lines have been written to take in account only nodes.
				if (!(sourceEditPart instanceof NodeEditPart) || !(targetEditPart instanceof NodeEditPart)) {
					break;
				}

				CreateConnectionRequest connectionRequest = createTargetRequest();
				// get the anchors locations
				Point[] newLocation = LayoutUtils.getLinkAnchor(sourceEditPart, targetEditPart);
				connectionRequest.setTargetEditPart(sourceEditPart);
				connectionRequest.setType(org.eclipse.gef.RequestConstants.REQ_CONNECTION_START);
				connectionRequest.setLocation(newLocation[0]);

				// only if the connection is supported will we get a non null
				// command from the sourceEditPart

				if (sourceEditPart.getCommand(connectionRequest) != null) {

					connectionRequest.setSourceEditPart(sourceEditPart);
					connectionRequest.setTargetEditPart(targetEditPart);
					connectionRequest.setType(org.eclipse.gef.RequestConstants.REQ_CONNECTION_END);

					connectionRequest.setLocation(newLocation[1]);

					setTargetRequest(connectionRequest);

					Command command = targetEditPart.getCommand(connectionRequest);

					if (command != null) {
						Command completeCommand = getCompleteCommand(command);
						setCurrentCommand(completeCommand);
						executeCurrentCommand();
					}

					selectAddedObject(viewer, DiagramCommandStack.getReturnValues(command));

				}
			}

			setAvoidDeactivation(false);
			eraseSourceFeedback();
			deactivate();

		}
	}

	protected Command getCompleteCommand(Command createConnectionCommand) {
		CompoundCommand compositeCmd = new CompoundCommand("Create Link");

		compositeCmd.add(createConnectionCommand);

		if (getTargetRequest() instanceof CreateRequest) {
			CreateUnspecifiedAdapter viewAdapter = new CreateUnspecifiedAdapter();
			viewAdapter.add((CreateRequest) getTargetRequest());
		}
		return compositeCmd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean handleCreateConnection() {
		// When a connection is to be created, a dialog box may appear which
		// will cause this tool to be deactivated and the feedback to be
		// erased. This behavior is overridden by setting the avoid
		// deactivation flag.
		setAvoidDeactivation(true);

		if (getTargetEditPart() == null) {
			return false;
		}

		final EditPartViewer currentViewer = getCurrentViewer();

		Command endCommand = getCommand();

		if (endCommand != null) {
			Command completeCommand = getCompleteCommand(endCommand);

			setCurrentCommand(completeCommand);

			executeCurrentCommand();
		}

		selectAddedObject(currentViewer, DiagramCommandStack.getReturnValues(endCommand));

		setAvoidDeactivation(false);
		eraseSourceFeedback();
		deactivate();

		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CreateConnectionRequest createTargetRequest() {
		return new CreateAspectUnspecifiedTypeConnectionRequest(getElementTypes(), false, getPreferencesHint());
	}

	/**
	 * Copy of the class {@link CreateUnspecifiedTypeConnectionRequest} to use
	 * the Papyrus request factory instead of the gmf one.
	 */
	public class CreateAspectUnspecifiedTypeConnectionRequest extends CreateUnspecifiedTypeConnectionRequest {

		/**
		 * List of relationship types of which one will be created (of type <code>IElementType</code>).
		 */
		// private List relationshipTypes;

		/**
		 * A map containing the <code>CreateConnectionRequest</code> for each
		 * element type.
		 */
		private Map<IElementType, CreateConnectionRequest> requests = new HashMap<IElementType, CreateConnectionRequest>();

		/** The result to be returned from which the new views can be retrieved. */
		private List newObjectList = new ArrayList();

		/**
		 * A flag to indicate if the Modeling Assistant Service should be used
		 * to find the types when the other end of the connection is known.
		 */
		private boolean useModelingAssistantService;

		/**
		 * A flag to indicate if this request is to create a connection from
		 * target to source.
		 */
		private boolean directionReversed = false;

		/**
		 * The hint used to find the appropriate preference store from which
		 * general diagramming preference values for properties of shapes,
		 * connections, and diagrams can be retrieved. This hint is mapped to a
		 * preference store in the {@link DiagramPreferencesRegistry}.
		 */
		private PreferencesHint preferencesHint;

		/**
		 * Creates a new <code>CreateUnspecifiedTypeConnectionRequest</code>.
		 *
		 * @param relationshipTypes
		 *            List of relationship types of which one will be created
		 *            (of type <code>IElementType</code>).
		 * @param useModelingAssistantService
		 *            True if the Modeling Assistant Service should be used to
		 *            find the types when the other end of the connection is
		 *            known.
		 * @param preferencesHint
		 *            The preference hint that is to be used to find the
		 *            appropriate preference store from which to retrieve
		 *            diagram preference values. The preference hint is mapped
		 *            to a preference store in the preference registry <@link
		 *            DiagramPreferencesRegistry>.
		 */
		public CreateAspectUnspecifiedTypeConnectionRequest(List<IElementType> relationshipTypes, boolean useModelingAssistantService, PreferencesHint preferencesHint) {
			super(relationshipTypes, useModelingAssistantService, preferencesHint);
			this.useModelingAssistantService = useModelingAssistantService;
			this.preferencesHint = preferencesHint;
			createRequests();
		}


		/**
		 * See bug 288695
		 *
		 * if there is only one {@link CreateRequest}, returns this {@link CreateRequest#getNewObject()},
		 * else, returns a list containing a single {@link IAdaptable} that delegates to the {@link CreateRequest#getNewObject()} after the command is
		 * executed.
		 * It is safe to use the return {@link IAdaptable} in a {@link DeferredCreateConnectionViewCommand}.
		 */
		@Override
		public Object getNewObject() {
			if (newObjectList.isEmpty()) {
				if (requests.size() == 1) {
					Object createRequest = requests.values().iterator().next();
					if (createRequest instanceof CreateRequest) {
						newObjectList.add(((CreateRequest) createRequest).getNewObject());
					}
				} else if (requests.size() > 1) {
					/* See bug 288695 */
					CreateUnspecifiedAdapter adapter = new CreateUnspecifiedAdapter();
					for (Object request : requests.values()) {
						if (request instanceof CreateRequest) {
							adapter.add((CreateRequest) request);
						}
					}
					List<IAdaptable> newObjects = new ArrayList<IAdaptable>();
					newObjects.add(adapter);
					return newObjects;
				}
			}
			return newObjectList;
		}

		/**
		 * Creates a <code>CreateConnectionRequest</code> for each relationship
		 * type and adds it to the map of requests.
		 */
		protected void createRequests() {
			for (IElementType elementType : (List<IElementType>) CreateAspectUnspecifiedTypeConnectionRequest.this.getElementTypes()) {
				CreateConnectionRequest request = PapyrusCreateViewRequestFactory.getCreateConnectionRequest(elementType, getPreferencesHint());
				request.setType(getType());
				requests.put(elementType, request);
			}
		}

		/**
		 * Returns the <code>CreateRequest</code> for the relationship type
		 * passed in.
		 *
		 * @param relationshipType
		 * @return the <code>CreateRequest</code>
		 */
		@Override
		public CreateRequest getRequestForType(IElementType relationshipType) {
			if (requests != null) {
				return requests.get(relationshipType);
			}
			return null;
		}

		@Override
		public void addRequest(IElementType relationshipType, Request request) {
			if (requests != null && request instanceof CreateConnectionRequest) {
				requests.put(relationshipType, (CreateConnectionRequest) request);
			}
		}

		/**
		 * Returns a list of all the requests.
		 *
		 * @return the requests
		 */
		@Override
		public List getAllRequests() {
			if (requests != null) {
				return new ArrayList(requests.values());
			}
			return Collections.EMPTY_LIST;
		}

		// /**
		// * Returns the list of element types.
		// *
		// * @return Returns the list of element types.
		// */
		// public List getElementTypes() {
		// return relationshipTypes;
		// }

		/**
		 * @see org.eclipse.gef.requests.CreateConnectionRequest#setSourceEditPart(org.eclipse.gef.EditPart)
		 */
		@Override
		public void setSourceEditPart(EditPart part) {
			if (requests != null) {
				for (CreateConnectionRequest request : requests.values()) {
					request.setSourceEditPart(part);
				}
			}
			super.setSourceEditPart(part);
		}

		/**
		 * @see org.eclipse.gef.requests.TargetRequest#setTargetEditPart(org.eclipse.gef.EditPart)
		 */
		@Override
		public void setTargetEditPart(EditPart part) {
			if (requests != null) {
				for (CreateConnectionRequest request : requests.values()) {
					request.setTargetEditPart(part);
				}
			}
			super.setTargetEditPart(part);
		}

		/**
		 * @see org.eclipse.gef.requests.CreateRequest#setLocation(org.eclipse.draw2d.geometry.Point)
		 */
		@Override
		public void setLocation(Point location) {
			if (requests != null) {
				for (CreateConnectionRequest request : requests.values()) {
					request.setLocation(location);
				}
			}
			super.setLocation(location);
		}

		/**
		 * @see org.eclipse.gef.Request#setType(java.lang.Object)
		 */
		@Override
		public void setType(Object type) {
			if (REQ_CONNECTION_END.equals(getType()) && REQ_CONNECTION_START.equals(type)) {

				// Bug 386011: [Class Diagrams] Incomplete associations get left on diagram
				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=386011

				// This case is specific to the two-clicks association mode
				// When we draw an association to nothing, and discard the popup menu proposing to
				// create a new Element as the target of the Association, the target request is changed
				// from type "connection_end" to "connection_start", which prevents cleaning the feedback

				// We should clean the feedback before changing the type (Because eraseSourceFeedback
				// tests the type of the request, and expects "connection_end")
				AspectUnspecifiedTypeConnectionTool.this.setAvoidDeactivation(false);
				AspectUnspecifiedTypeConnectionTool.this.eraseSourceFeedback();
				AspectUnspecifiedTypeConnectionTool.this.deactivate();
				AspectUnspecifiedTypeConnectionTool.this.handleFinished();
			}

			if (requests != null) {
				for (CreateConnectionRequest request : requests.values()) {
					request.setType(type);
				}
			}
			super.setType(type);
		}

		/**
		 * Returns true if this request is to create a connection from target to
		 * source.
		 *
		 * @return Returns the directionReversed.
		 */
		@Override
		public boolean isDirectionReversed() {
			return directionReversed;
		}

		/**
		 * Sets the directionReversed flag.
		 *
		 * @param directionReversed
		 *            The directionReversed to set.
		 */
		@Override
		public void setDirectionReversed(boolean directionReversed) {
			this.directionReversed = directionReversed;
		}

		/**
		 * Should the Modeling Assistant Service be used?
		 *
		 * @return Returns true if the Modeling Assistant Service should be used
		 *         to find the types when the other end of the connection is
		 *         known.
		 */
		@Override
		public boolean useModelingAssistantService() {
			return useModelingAssistantService;
		}

		/**
		 * Gets the preferences hint that is to be used to find the appropriate
		 * preference store from which to retrieve diagram preference values.
		 * The preference hint is mapped to a preference store in the preference
		 * registry <@link DiagramPreferencesRegistry>.
		 *
		 * @return the preferences hint
		 */
		@Override
		protected PreferencesHint getPreferencesHint() {
			return preferencesHint;
		}
	}

	public static class PapyrusCreateViewRequestFactory {

		/**
		 * Creates a new <code>CreateViewRequest</code> or <code>CreateViewAndElementRequest</code> based on the <code>IElementType</code> passed in.
		 *
		 * @param type
		 *            the <code>IElementType</code>
		 * @param preferencesHint
		 *            The preference hint that is to be used to find the
		 *            appropriate preference store from which to retrieve
		 *            diagram preference values. The preference hint is mapped
		 *            to a preference store in the preference registry <@link
		 *            DiagramPreferencesRegistry>.
		 * @return the new request
		 */
		public static CreateViewRequest getCreateShapeRequest(IElementType type, PreferencesHint preferencesHint) {
			if (type instanceof INotationType) {
				ViewDescriptor viewDescriptor = new ViewDescriptor(null, Node.class, ((INotationType) type).getSemanticHint(), preferencesHint);
				return new CreateViewRequest(viewDescriptor);
			} else if (type instanceof IHintedType) {
				ViewAndElementDescriptor viewDescriptor = new ViewAndElementDescriptor(new CreateElementRequestAdapter(new CreateElementRequest(type)), Node.class, ((IHintedType) type).getSemanticHint(), preferencesHint);
				return new CreateViewAndElementRequest(viewDescriptor);
			} else {
				return new CreateViewAndElementRequest(type, preferencesHint);
			}
		}

		/**
		 * Creates a new <code>CreateConnectionViewRequest</code> or <code>CreateConnectionViewAndElementRequest</code> based on the <code>IElementType</code> passed in.
		 *
		 * @param type
		 *            the <code>IElementType</code>
		 * @param preferencesHint
		 *            The preference hint that is to be used to find the
		 *            appropriate preference store from which to retrieve
		 *            diagram preference values. The preference hint is mapped
		 *            to a preference store in the preference registry <@link
		 *            DiagramPreferencesRegistry>.
		 * @return the new request
		 */
		public static CreateConnectionViewRequest getCreateConnectionRequest(IElementType type, PreferencesHint preferencesHint) {
			if (type instanceof INotationType) {
				// Pass in the type as the element adapter so that it can be
				// retrieved in the cases where a popup menu is to appear with a
				// list of types.
				ConnectionViewDescriptor viewDescriptor = new ConnectionViewDescriptor(type, ((IHintedType) type).getSemanticHint(), preferencesHint);
				return new CreateConnectionViewRequest(viewDescriptor);
			} else if (type instanceof IHintedType) {
				return new CreateConnectionViewAndElementRequest(type, ((IHintedType) type).getSemanticHint(), preferencesHint);
			} else {
				return new CreateConnectionViewAndElementRequest(type, preferencesHint);
			}
		}

		/**
		 * Creates a new <code>CreateConnectionViewRequest</code> or <code>CreateConnectionViewAndElementRequest</code> based on the <code>IElementType</code> passed in.
		 *
		 * @param type
		 *            the <code>IElementType</code>
		 * @param graphicalHint
		 *            graphical hint for the view to create
		 * @param preferencesHint
		 *            The preference hint that is to be used to find the
		 *            appropriate preference store from which to retrieve
		 *            diagram preference values. The preference hint is mapped
		 *            to a preference store in the preference registry <@link
		 *            DiagramPreferencesRegistry>.
		 * @return the new request
		 */
		public static CreateConnectionViewRequest getCreateConnectionRequest(IElementType type, String graphicalHint, PreferencesHint preferencesHint) {
			if (type instanceof INotationType) {
				// Pass in the type as the element adapter so that it can be
				// retrieved in the cases where a popup menu is to appear with a
				// list of types.
				ConnectionViewDescriptor viewDescriptor = new ConnectionViewDescriptor(type, graphicalHint, preferencesHint);
				return new CreateConnectionViewRequest(viewDescriptor);
			} else if (type instanceof IHintedType) {
				// force the graphical hint instead of the hint of the hinted
				// element
				return new CreateConnectionViewAndElementRequest(type, graphicalHint, preferencesHint);
			} else {
				return new CreateConnectionViewAndElementRequest(type, preferencesHint);
			}
		}
	}

}
