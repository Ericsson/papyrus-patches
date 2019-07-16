/*****************************************************************************
 * Copyright (c) 2008, 2016, 2019 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Cedric Dumoulin  Cedric.dumoulin@lifl.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - only calculate affected files for workspace resources (CDO)
 *  Laurent Wouters (CEA) - laurent.wouters@cea.fr - Refactoring for viewpoints
 *  Christian W. Damus - bug 485220
 *  Ansgar Radermacher - Bug 482587 diagram creation does not mark model as dirty
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IClientContext;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.papyrus.commands.ICreationCommand;
import org.eclipse.papyrus.commands.OpenDiagramCommand;
import org.eclipse.papyrus.infra.architecture.representation.ModelAutoCreate;
import org.eclipse.papyrus.infra.architecture.representation.OwningRule;
import org.eclipse.papyrus.infra.architecture.representation.RootAutoSelect;
import org.eclipse.papyrus.infra.core.language.ILanguageService;
import org.eclipse.papyrus.infra.core.resource.IEMFModel;
import org.eclipse.papyrus.infra.core.resource.IReadOnlyHandler2;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.ReadOnlyAxis;
import org.eclipse.papyrus.infra.core.resource.sasheditor.SashModelUtils;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.gmf.command.GMFtoEMFCommandWrapper;
import org.eclipse.papyrus.infra.emf.readonly.ReadOnlyManager;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.DiagramPrototype;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;
import org.eclipse.papyrus.infra.services.edit.context.TypeContext;
import org.eclipse.papyrus.infra.services.edit.service.ElementEditServiceUtils;
import org.eclipse.papyrus.infra.services.edit.service.IElementEditService;
import org.eclipse.papyrus.infra.viewpoints.policy.PolicyChecker;
import org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Command creating a new GMF diagram in Papyrus. This command is intended to be used in eclipse
 * extensions.
 *
 * Commands to create a GMF Diagram can subclass this class. There is two kinds of commands: -
 * Eclipse handlers issuing commands (toolbar, menu, ...). This commands can find the active editor
 * by using the Worbench.getActivePArt(). The entry point is {@link #execute(ExecutionEvent)}. -
 * Commands called during editor initializing (like wizard). This commands require the diResourceSet
 * to work. The entry point is {@link #createDiagram(Resource, EObject, String)}
 *
 * @author cedric dumoulin
 * @author <a href="mailto:jerome.benois@obeo.fr">Jerome Benois</a>
 */
public abstract class AbstractPapyrusGmfCreateDiagramCommandHandler extends AbstractHandler implements IHandler, ICreationCommand {

	/**
	 * Inner class for the creation of diagrams
	 *
	 * @author Laurent Wouters
	 *
	 */
	private class Creator {

		private ModelSet modelSet;

		private EObject owner;

		private EObject element;

		private DiagramPrototype prototype;

		private OwningRule rule;

		private String name;

		private IElementEditService service;

		private IClientContext clientContext;

		public Creator(ModelSet modelSet, EObject owner, EObject element, DiagramPrototype prototype, String name) {
			this.modelSet = modelSet;
			this.owner = owner;
			this.element = element;
			this.prototype = prototype;
			this.name = name;
		}

		private CommandResult createDiagram() throws ServiceException {
			Resource notationResource = getNotationResource(modelSet, owner, element);
			if (notationResource == null) {
				return CommandResult.newErrorCommandResult("Cannot create a diagram on the selected element (ReadOnly?)"); //$NON-NLS-1$
			}

			if (owner == null) {
				Resource modelResource = ILanguageService.getLanguageModels(modelSet).stream()
						.filter(IEMFModel.class::isInstance)
						.map(IEMFModel.class::cast)
						.findAny()
						.map(IEMFModel::getResource)
						.orElse(null);
				owner = getRootElement(modelResource);
				attachModelToResource(owner, modelResource);
			}

			try {
				clientContext = TypeContext.getContext(modelSet);
			} catch (ServiceException e) {
				Activator.log.error(e);
			}
			if (clientContext == null) {
				// Something isn't right ...
				return null;
			}

			service = ElementEditServiceUtils.getCommandProvider(owner, clientContext);
			if (service == null) {
				// Something isn't right ...
				return null;
			}

			rule = PolicyChecker.getFor(modelSet).getOwningRuleFor(prototype, owner);
			if (rule == null) {
				// Something isn't right ...
				return null;
			}

			element = owner;
			if (rule.getNewModelPath() != null) {
				// We have a path for the root auto-creation
				for (ModelAutoCreate auto : rule.getNewModelPath()) {
					EReference ref = auto.getFeature();
					String type = auto.getCreationType();
					if (ref.isMany()) {
						element = create(element, ref, type);
					} else {
						EObject temp = (EObject) element.eGet(ref);
						if (temp != null) {
							element = temp;
						} else {
							element = create(element, ref, type);
						}
					}
				}
			}

			if (rule.getSelectDiagramRoot() != null) {
				// We have a path for the root auto-selection
				for (RootAutoSelect auto : rule.getSelectDiagramRoot()) {
					EReference ref = auto.getFeature();
					element = (EObject) element.eGet(ref);
				}
			}

			CommandResult result = doEditDiagramName(prototype, name);
			if (!result.getStatus().isOK()) {
				return result;
			} else {
				name = (result.getReturnValue() != null) ? result.getReturnValue().toString() : null;
			}

			Diagram diagram = doCreateDiagram(notationResource, owner, element, prototype, name);

			if (diagram != null) {
				return CommandResult.newOKCommandResult(diagram);
			}

			return CommandResult.newCancelledCommandResult();
		}

		private EObject create(EObject origin, EReference reference, String typeID) {
			IElementType itype = ElementTypeRegistry.getInstance().getType(typeID);
			CreateElementRequest request = new CreateElementRequest(origin, itype, reference);
			ICommand command = service.getEditCommand(request);
			IStatus status = null;
			try {
				status = command.execute(null, null);
			} catch (ExecutionException e) {
				return null;
			}
			if (!status.isOK()) {
				return null;
			}
			CommandResult result = command.getCommandResult();
			if (result == null) {
				return null;
			}
			return (EObject) result.getReturnValue();
		}
	}

	protected Resource getNotationResource(ModelSet modelSet, EObject owner, EObject element) {
		if (element == null) { // If the element is null, the root element of the main model will be used. Return the main notation resource
			return NotationUtils.getNotationResource(modelSet);
		}
		URI uriWithoutExtension = element.eResource().getURI().trimFileExtension();

		URI notationURI = uriWithoutExtension.appendFileExtension(NotationModel.NOTATION_FILE_EXTENSION);
		Resource notationResource = modelSet.getResource(notationURI, false);

		// The resource doesn't exist. Maybe we're trying to create a
		// diagram on a pure-UML library. Try to create a new resource
		if (notationResource == null) {
			notationResource = modelSet.createResource(notationURI);
			if (notationResource == null) {
				modelSet.getResources().remove(notationResource);
				return null;
			}

			EditingDomain editingDomain = EMFHelper.resolveEditingDomain(element);

			if (EMFHelper.isReadOnly(notationResource, editingDomain)) {
				// Check whether the resource can be made writable
				IReadOnlyHandler2 roHandler = ReadOnlyManager.getReadOnlyHandler(editingDomain);
				if (roHandler.canMakeWritable(ReadOnlyAxis.anyAxis(), new URI[] { notationResource.getURI() }).or(false)) {
					return notationResource; // The read-only manager will eventually ask for a user confirmation
				} else {
					modelSet.getResources().remove(notationResource);
					return null; // The resource can't be made writable; don't go further
				}
			}
		}

		return notationResource;
	}


	/**
	 * @return
	 */
	protected CommandResult doEditDiagramName(ViewPrototype prototype, String name) {

		if (name == null) {
			name = openDiagramNameDialog("New" + prototype.getLabel().replace(" ", ""));
		}
		// canceled
		if (name == null) {
			return CommandResult.newCancelledCommandResult();
		}
		return CommandResult.newOKCommandResult(name);
	}


	/**
	 * Get the root element associated with canvas.
	 */
	private EObject getRootElement(Resource modelResource) {
		EObject rootElement = null;
		if (modelResource != null && modelResource.getContents() != null && modelResource.getContents().size() > 0) {
			Object root = modelResource.getContents().get(0);
			if (root instanceof EObject) {
				rootElement = (EObject) root;
			}
		}

		return rootElement;
	}

	/**
	 * Store model element in the resource.
	 */
	private void attachModelToResource(EObject root, Resource resource) {
		resource.getContents().add(root);
	}

	/**
	 * Open popup to enter the new diagram name
	 *
	 * @param defaultValue
	 *            the default value
	 * @return the entered diagram name
	 */
	private String openDiagramNameDialog(String defaultValue) {
		if (defaultValue == null) {
			defaultValue = "";
		}

		InputDialog inputDialog = new InputDialog(Display.getCurrent().getActiveShell(), Messages.AbstractPapyrusGmfCreateDiagramCommandHandler_SelectNewDiagramName, Messages.AbstractPapyrusGmfCreateDiagramCommandHandler_NewDiagramName, defaultValue, null);
		int result = inputDialog.open();

		if (result == Window.OK) {
			String name = inputDialog.getValue();
			if (name == null || name.length() == 0) {
				name = defaultValue;
			}
			return name;
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#createDiagram(org.eclipse.emf.ecore.resource.Resource, org.eclipse.emf.ecore.EObject,
	 * java.lang.String)
	 */
	@Override
	public final Diagram createDiagram(ModelSet modelSet, EObject owner, String name) {
		Diagram diagram = null;
		ViewPrototype proto = ViewPrototype.get(PolicyChecker.getFor(modelSet), getCreatedDiagramType(), owner, owner);
		if (proto != null) {
			diagram = createDiagram(modelSet, owner, owner, proto, name);
		}
		return diagram;
	}


	public final Diagram createDiagram(ModelSet modelSet, EObject owner, EObject element, ViewPrototype prototype, String name, boolean openDiagram) {
		ICommand createCmd = getCreateDiagramCommand(modelSet, owner, element, prototype, name);
		TransactionalEditingDomain dom = modelSet.getTransactionalEditingDomain();
		CompositeCommand cmd = new CompositeCommand("Create diagram");
		cmd.add(createCmd);
		if (openDiagram) {
			cmd.add(new OpenDiagramCommand(dom, createCmd));
		}

		CommandResult result = cmd.getCommandResult();
		dom.getCommandStack().execute(GMFtoEMFCommandWrapper.wrap(cmd));
		IStatus status = result.getStatus();

		if (status.isOK()) {
			Object returnValue = result.getReturnValue();

			// CompositeCommands should always return a collection
			if (returnValue instanceof Collection<?>) {
				for (Object returnElement : (Collection<?>) returnValue) {
					if (returnElement instanceof Diagram) {
						return (Diagram) returnElement;
					}
				}
			}
		} else if (status.getSeverity() != IStatus.CANCEL) {
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		}

		return null;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#createDiagram(org.eclipse.emf.ecore.resource.Resource, org.eclipse.emf.ecore.EObject,
	 * org.eclipse.emf.ecore.EObject, org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype, java.lang.String)
	 */
	@Override
	public final Diagram createDiagram(ModelSet modelSet, EObject owner, EObject element, ViewPrototype prototype, String name) {
		return createDiagram(modelSet, owner, element, prototype, name, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#getCreateDiagramCommand(org.eclipse.papyrus.infra.core.resource.ModelSet,
	 * org.eclipse.emf.ecore.EObject, java.lang.String)
	 */
	@Override
	public final ICommand getCreateDiagramCommand(ModelSet modelSet, EObject owner, String name) {
		ViewPrototype proto = ViewPrototype.get(PolicyChecker.getFor(modelSet), getCreatedDiagramType(), owner, owner);
		if (proto == null) {
			return null;
		}
		return getCreateDiagramCommand(modelSet, owner, owner, proto, name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#getCreateDiagramCommand(org.eclipse.papyrus.infra.core.resource.ModelSet,
	 * org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.papyrus.infra.viewpoints.policy.ViewPrototype,
	 * java.lang.String)
	 */
	@Override
	public final ICommand getCreateDiagramCommand(final ModelSet modelSet, final EObject owner, final EObject element, final ViewPrototype prototype, final String name) {
		// Diagram creation should not change the semantic resource
		final Resource notationResource = NotationUtils.getNotationResource(modelSet);
		// the SashModel's resource (either the shared .di or the local .sash) contains the sash window mgr that can be impacted with diagram creation
		final Resource sashResource = SashModelUtils.getSashModel(modelSet).getResource();

		ArrayList<IFile> modifiedFiles = new ArrayList<IFile>();
		if (notationResource.getURI().isPlatformResource()) {
			modifiedFiles.add(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(notationResource.getURI().toPlatformString(true))));
		}
		if (sashResource.getURI().isPlatformResource()) {
			modifiedFiles.add(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(sashResource.getURI().toPlatformString(true))));
		}

		return new AbstractTransactionalCommand(modelSet.getTransactionalEditingDomain(), Messages.AbstractPapyrusGmfCreateDiagramCommandHandler_CreateDiagramCommandLabel, modifiedFiles) {

			private Diagram diagram = null;

			private EObject diagramElement = null;

			private EObject diagramOwner = null;

			@Override
			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				Creator creator = new Creator(modelSet, owner, element, (DiagramPrototype) prototype, name);
				try {
					CommandResult commandResult = creator.createDiagram();
					if (commandResult != null) {
						if (!commandResult.getStatus().isOK()) {
							return commandResult;
						}

						diagram = (Diagram) commandResult.getReturnValue();
						diagramElement = diagram.getElement();
						diagramOwner = DiagramUtils.getOwner(diagram);
						return commandResult;
					}
				} catch (ServiceException e) {
					Activator.log.error(e);
				}
				return CommandResult.newErrorCommandResult("Error during diagram creation"); //$NON-NLS-1$
			}

			@Override
			protected IStatus doUndo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				// the undo corresponds to a destroy diagram command
				// during diagram creation no adapters are set to the diagram so the setElement is not registered
				// to remove the cross reference using the element reference it is better to use the destroy element command
				// DestroyElementPapyrusCommand depc = (diagram != null) ? new DestroyElementPapyrusCommand(new DestroyElementRequest(diagram, false)) : null;
				IStatus status = super.doUndo(monitor, info);
				diagram.setElement(null);
				// reset owner, otherwise diagram remains in model explorer
				DiagramUtils.setOwner(diagram, null);
				return status;
			}

			@Override
			protected IStatus doRedo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				diagram.setElement(diagramElement);
				DiagramUtils.setOwner(diagram, diagramOwner);
				IStatus status = super.doRedo(monitor, info);
				return status;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#getCreatedDiagramType()
	 */
	@Override
	public String getCreatedDiagramType() {
		return getDiagramNotationID();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.commands.ICreationCommand#isParentReassignable()
	 */
	@Override
	public boolean isParentReassignable() {
		// yes by default
		return true;
	}



	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// This method should not be called, use the execute(ExecutionEvent, ViewPrototype, String) method.
		throw new UnsupportedOperationException();
	}


	protected abstract String getDiagramNotationID();

	protected abstract PreferencesHint getPreferenceHint();

	protected abstract String getDefaultDiagramName();

	/**
	 * Overridable method that effectively create the diagram with the given validated parameters
	 *
	 * @param diagramResource
	 *            the diagram resource
	 * @param owner
	 *            the diagram's owner
	 * @param element
	 *            the diagram's model element
	 * @param prototype
	 *            the diagram's prototype
	 * @param name
	 *            the diagram's name
	 * @return the created diagram, or <code>null</code> if the creation failed
	 */
	protected Diagram doCreateDiagram(Resource diagramResource, EObject owner, EObject element, DiagramPrototype prototype, String name) {
		// create diagram
		Diagram diagram = ViewService.createDiagram(element, getDiagramNotationID(), getPreferenceHint());
		if (diagram != null) {
			diagram.setName(name);
			diagram.setElement(element);
			DiagramUtils.setOwner(diagram, owner);
			DiagramUtils.setPrototype(diagram, prototype);
			diagramResource.getContents().add(diagram);
			initializeDiagram(diagram);

		}
		return diagram;
	}

	/**
	 * Overridable method for the initialization of create diagrams
	 *
	 * @param diagram
	 *            the created diagram
	 */
	protected void initializeDiagram(EObject diagram) {
		// Subclasses may override
	}
}
