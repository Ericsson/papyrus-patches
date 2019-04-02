
/**
 * Copyright (c) 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
  *  CEA LIST - Initial API and implementation
 */
package org.eclipse.papyrus.uml.diagram.sequence.edit.policies;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.requests.ReconnectRequest;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.ICompositeCommand;
import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.CommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.commands.MoveElementsCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyReferenceRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.GetEditContextRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.MoveRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientReferenceRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.edit.helpers.GeneratedEditHelperBase;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.uml.diagram.sequence.expressions.UMLOCLFactory;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLVisualIDRegistry;
import org.eclipse.papyrus.uml.diagram.sequence.providers.UMLElementTypes;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.DurationConstraint;
import org.eclipse.uml2.uml.DurationObservation;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.GeneralOrdering;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @generated
 */
public class UMLBaseItemSemanticEditPolicy extends SemanticEditPolicy {

	/**
	 * Extended request data key to hold editpart visual id.
	 *
	 * @generated
	 */
	public static final String VISUAL_ID_KEY = "visual_id"; //$NON-NLS-1$
	/**
	 * Extended request data key to hold the edge view during a reconnect request.
	 *
	 * @generated
	 */
	public static final String GRAPHICAL_RECONNECTED_EDGE = "graphical_edge"; //$NON-NLS-1$
	/**
	 * @generated
	 */
	private final IElementType myElementType;

	/**
	 * @generated
	 */
	protected UMLBaseItemSemanticEditPolicy(IElementType elementType) {
		myElementType = elementType;
	}

	/**
	 * Extended request data key to hold editpart visual id.
	 * Add visual id of edited editpart to extended data of the request
	 * so command switch can decide what kind of diagram element is being edited.
	 * It is done in those cases when it's not possible to deduce diagram
	 * element kind from domain element.
	 * Add the reoriented view to the request extended data so that the view
	 * currently edited can be distinguished from other views of the same element
	 * and these latter possibly removed if they become inconsistent after reconnect
	 *
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Command getCommand(Request request) {
		if (request instanceof ReconnectRequest) {
			Object view = ((ReconnectRequest) request).getConnectionEditPart().getModel();
			if (view instanceof View) {
				String id = UMLVisualIDRegistry.getVisualID((View) view);
				request.getExtendedData().put(VISUAL_ID_KEY, id);
				request.getExtendedData().put(GRAPHICAL_RECONNECTED_EDGE, view);
			}
		}
		return super.getCommand(request);
	}

	/**
	 * Returns visual id from request parameters.
	 *
	 * @generated
	 */
	protected String getVisualID(IEditCommandRequest request) {
		return (String) request.getParameter(VISUAL_ID_KEY);
	}

	/**
	 * @generated
	 */
	@Override
	protected Command getSemanticCommand(IEditCommandRequest request) {
		IEditCommandRequest completedRequest = completeRequest(request);
		Command semanticCommand = getSemanticCommandSwitch(completedRequest);
		semanticCommand = getEditHelperCommand(completedRequest, semanticCommand);
		if (completedRequest instanceof DestroyRequest) {
			DestroyRequest destroyRequest = (DestroyRequest) completedRequest;
			return shouldProceed(destroyRequest) ? addDeleteViewCommand(semanticCommand, destroyRequest) : null;
		}
		return semanticCommand;
	}

	/**
	 * @generated
	 */
	protected Command addDeleteViewCommand(Command mainCommand, DestroyRequest completedRequest) {
		Command deleteViewCommand = getGEFWrapper(new DeleteCommand(getEditingDomain(), (View) getHost().getModel()));
		return mainCommand == null ? deleteViewCommand : mainCommand.chain(deleteViewCommand);
	}

	/**
	 * @generated
	 */
	private Command getEditHelperCommand(IEditCommandRequest request, Command editPolicyCommand) {
		if (editPolicyCommand != null) {
			ICommand command = editPolicyCommand instanceof ICommandProxy
					? ((ICommandProxy) editPolicyCommand).getICommand()
					: new CommandProxy(editPolicyCommand);
			request.setParameter(GeneratedEditHelperBase.EDIT_POLICY_COMMAND, command);
		}
		IElementType requestContextElementType = getContextElementType(request);
		request.setParameter(GeneratedEditHelperBase.CONTEXT_ELEMENT_TYPE, requestContextElementType);
		ICommand command = requestContextElementType.getEditCommand(request);
		request.setParameter(GeneratedEditHelperBase.EDIT_POLICY_COMMAND, null);
		request.setParameter(GeneratedEditHelperBase.CONTEXT_ELEMENT_TYPE, null);
		if (command != null) {
			if (!(command instanceof CompositeTransactionalCommand)) {
				command = new CompositeTransactionalCommand(getEditingDomain(), command.getLabel()).compose(command);
			}
			return new ICommandProxy(command);
		}
		return editPolicyCommand;
	}

	/**
	 * @generated
	 */
	protected IElementType getContextElementType(IEditCommandRequest request) {
		IElementType requestContextElementType = UMLElementTypes.getElementType(getVisualID(request));
		return requestContextElementType != null ? requestContextElementType : myElementType;
	}

	/**
	 * @generated
	 */
	protected Command getSemanticCommandSwitch(IEditCommandRequest req) {
		if (req instanceof CreateRelationshipRequest) {
			return getCreateRelationshipCommand((CreateRelationshipRequest) req);
		} else if (req instanceof CreateElementRequest) {
			return getCreateCommand((CreateElementRequest) req);
		} else if (req instanceof ConfigureRequest) {
			return getConfigureCommand((ConfigureRequest) req);
		} else if (req instanceof DestroyElementRequest) {
			return getDestroyElementCommand((DestroyElementRequest) req);
		} else if (req instanceof DestroyReferenceRequest) {
			return getDestroyReferenceCommand((DestroyReferenceRequest) req);
		} else if (req instanceof DuplicateElementsRequest) {
			return getDuplicateCommand((DuplicateElementsRequest) req);
		} else if (req instanceof GetEditContextRequest) {
			return getEditContextCommand((GetEditContextRequest) req);
		} else if (req instanceof MoveRequest) {
			return getMoveCommand((MoveRequest) req);
		} else if (req instanceof ReorientReferenceRelationshipRequest) {
			return getReorientReferenceRelationshipCommand((ReorientReferenceRelationshipRequest) req);
		} else if (req instanceof ReorientRelationshipRequest) {
			return getReorientRelationshipCommand((ReorientRelationshipRequest) req);
		} else if (req instanceof SetRequest) {
			return getSetCommand((SetRequest) req);
		}
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getConfigureCommand(ConfigureRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getCreateCommand(CreateElementRequest req) {
		IElementType requestElementType = req.getElementType();
		if (requestElementType instanceof IElementType) {
			IElementEditService commandProvider = ElementEditServiceUtils.getCommandProvider(req.getContainer());
			if (commandProvider != null) {
				ICommand command = commandProvider.getEditCommand(req);
				if (command != null && command.canExecute()) {
					return new ICommandProxy(command);
				}
			}
		}
		return null;
	}

	// RS: End of add code for extended types

	/**
	 * @generated
	 */
	protected Command getSetCommand(SetRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getEditContextCommand(GetEditContextRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getDestroyElementCommand(DestroyElementRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getDestroyReferenceCommand(DestroyReferenceRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getDuplicateCommand(DuplicateElementsRequest req) {
		return null;
	}

	/**
	 * @generated
	 */
	protected Command getMoveCommand(MoveRequest req) {
		EObject targetCEObject = req.getTargetContainer();
		if (targetCEObject != null) {
			IElementEditService provider = ElementEditServiceUtils.getCommandProvider(targetCEObject);
			if (provider != null) {
				ICommand moveCommand = provider.getEditCommand(req);
				if (moveCommand != null) {
					return new ICommandProxy(moveCommand);
				}
			}
			return UnexecutableCommand.INSTANCE;
		} else {
			return getGEFWrapper(new MoveElementsCommand(req));
		}

	}

	/**
	 * @generated
	 */
	protected Command getReorientReferenceRelationshipCommand(ReorientReferenceRelationshipRequest req) {
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * @generated
	 */
	protected Command getReorientRelationshipCommand(ReorientRelationshipRequest req) {
		return UnexecutableCommand.INSTANCE;
	}

	/**
	 * @generated
	 */
	protected final Command getGEFWrapper(ICommand cmd) {
		return (cmd == null) ? UnexecutableCommand.INSTANCE : new ICommandProxy(cmd);
	}

	/**
	 * Returns editing domain from the host edit part.
	 *
	 * @generated
	 */
	protected TransactionalEditingDomain getEditingDomain() {
		return ((IGraphicalEditPart) getHost()).getEditingDomain();
	}

	/**
	 * Clean all shortcuts to the host element from the same diagram
	 *
	 * @generated
	 */
	protected void addDestroyShortcutsCommand(ICompositeCommand cmd, View view) {
		assert view.getEAnnotation("Shortcut") == null; //$NON-NLS-1$
		for (Iterator<?> it = view.getDiagram().getChildren().iterator(); it.hasNext();) {
			View nextView = (View) it.next();
			if (nextView.getEAnnotation("Shortcut") == null || !nextView.isSetElement() //$NON-NLS-1$
					|| nextView.getElement() != view.getElement()) {
				continue;
			}
			cmd.add(new DeleteCommand(getEditingDomain(), nextView));
		}
	}

	/**
	 * @generated
	 */
	public static LinkConstraints getLinkConstraints() {
		LinkConstraints cached = UMLDiagramEditorPlugin.getInstance().getLinkConstraints();
		if (cached == null) {
			UMLDiagramEditorPlugin.getInstance().setLinkConstraints(cached = new LinkConstraints());
		}
		return cached;
	}

	/**
	 * @generated
	 */
	public static class LinkConstraints {

		/**
		 * @generated
		 */
		public LinkConstraints() { // use static method #getLinkConstraints() to access instance
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_SynchEdge(Interaction container, Element source, Element target) {
			return canExistMessage_SynchEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_AsynchEdge(Interaction container, Element source, Element target) {
			return canExistMessage_AsynchEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_ReplyEdge(Interaction container, Element source, Element target) {
			return canExistMessage_ReplyEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_CreateEdge(Interaction container, Element source, Element target) {
			return canExistMessage_CreateEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_DeleteEdge(Interaction container, Element source, Element target) {
			return canExistMessage_DeleteEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_LostEdge(Interaction container, Element source, Element target) {
			return canExistMessage_LostEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateMessage_FoundEdge(Interaction container, Element source, Element target) {
			return canExistMessage_FoundEdge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateComment_AnnotatedElementEdge(Comment source, Element target) {
			if (source != null) {
				if (source.getAnnotatedElements().contains(target)) {
					return false;
				}
			}

			return canExistComment_AnnotatedElementEdge(source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateConstraint_ConstrainedElementEdge(Constraint source, Element target) {
			if (source != null) {
				if (source.getConstrainedElements().contains(target)) {
					return false;
				}
			}

			return canExistConstraint_ConstrainedElementEdge(source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateGeneralOrdering_Edge(InteractionFragment container, OccurrenceSpecification source,
				OccurrenceSpecification target) {
			return canExistGeneralOrdering_Edge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateConstraint_ContextEdge(Constraint source, Namespace target) {
			if (source != null) {
				if (source.getContext() != null) {
					return false;
				}
			}
			if (target != null && (target.getOwnedRules().contains(target))) {
				return false;
			}

			return canExistConstraint_ContextEdge(source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateDurationConstraint_Edge(Namespace container, Element source, Element target) {
			return canExistDurationConstraint_Edge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canCreateDurationObservation_Edge(Package container, NamedElement source, NamedElement target) {
			return canExistDurationObservation_Edge(container, null, source, target);
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_SynchEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(2, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(3, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_AsynchEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(22, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(6, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_ReplyEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(8, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(22, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_CreateEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(11, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(12, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_DeleteEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(14, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(15, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_LostEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(22, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(18, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistMessage_FoundEdge(Interaction container, Message linkInstance, Element source,
				Element target) {
			try {
				if (source == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object sourceVal = UMLOCLFactory.getExpression(20, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(source, Collections.singletonMap("oppositeEnd", target)); //$NON-NLS-1$
					if (false == sourceVal instanceof Boolean || !((Boolean) sourceVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				if (target == null) {
					return true;
				} else {
					Map<String, EClassifier> env = Collections.<String, EClassifier> singletonMap("oppositeEnd", //$NON-NLS-1$
							UMLPackage.eINSTANCE.getElement());
					Object targetVal = UMLOCLFactory.getExpression(22, UMLPackage.eINSTANCE.getElement(), env)
							.evaluate(target, Collections.singletonMap("oppositeEnd", source)); //$NON-NLS-1$
					if (false == targetVal instanceof Boolean || !((Boolean) targetVal).booleanValue()) {
						return false;
					} // else fall-through
				}
				return true;
			} catch (Exception e) {
				UMLDiagramEditorPlugin.getInstance().logError("Link constraint evaluation error", e); //$NON-NLS-1$
				return false;
			}
		}

		/**
		 * @generated
		 */
		public boolean canExistComment_AnnotatedElementEdge(Comment source, Element target) {
			return true;
		}

		/**
		 * @generated
		 */
		public boolean canExistConstraint_ConstrainedElementEdge(Constraint source, Element target) {
			return true;
		}

		/**
		 * @generated
		 */
		public boolean canExistGeneralOrdering_Edge(InteractionFragment container, GeneralOrdering linkInstance,
				OccurrenceSpecification source, OccurrenceSpecification target) {
			return true;
		}

		/**
		 * @generated
		 */
		public boolean canExistConstraint_ContextEdge(Constraint source, Namespace target) {
			return true;
		}

		/**
		 * @generated
		 */
		public boolean canExistDurationConstraint_Edge(Namespace container, DurationConstraint linkInstance,
				Element source, Element target) {
			return true;
		}

		/**
		 * @generated
		 */
		public boolean canExistDurationObservation_Edge(Package container, DurationObservation linkInstance,
				NamedElement source, NamedElement target) {
			return true;
		}
	}

}
