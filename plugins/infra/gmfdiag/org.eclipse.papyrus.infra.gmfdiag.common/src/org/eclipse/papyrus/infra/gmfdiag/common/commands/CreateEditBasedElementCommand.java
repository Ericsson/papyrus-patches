/*****************************************************************************
 * Copyright (c) 2013, 2016 CEA LIST, Christian W. Damus, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Remi Schnekenburger (CEA LIST) - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 431109
 *  Christian W. Damus - bug 507618
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.CompositeEMFOperation;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForResource;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.services.labelprovider.service.LabelProviderService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Creation command based on the metamodel.edit framework instead of relying on
 * the defaut GMF creation mechanism.
 * <P>
 * So the creation of elements is similar to the creation customized in the metamodel itself, which can be interesting for metamodels like UML. This can avoid specific code for example for Activity.createNode() list of commands.
 * </P>
 * <P>
 * The implementation relies on a composite command that delegates first the basic creation of the element to the metamodel.edit service.<BR/>
 * A second operation computes during execution the list of additional commands to configure the newly created element. For example, the name of the new element can be set in configure commands. Note: the configure command needs the newly created command, so
 * the list of operations to perform is unknown until the composite operation is executed. The executability of the 2nd set of commands can be hard to test.
 * </P>
 */
public class CreateEditBasedElementCommand extends CreateElementCommand {

	protected final class CreateEditBasedElementTransactionalCommand extends AbstractTransactionalCommand {
		private Collection<Command> possibleCommands;
		private Command commandDone;

		protected CreateEditBasedElementTransactionalCommand(TransactionalEditingDomain domain, String label, List<?> affectedFiles) {
			super(domain, label, affectedFiles);
		}

		@Override
		public boolean canExecute() {
			// Optimistically enable if the request would have to create
			// the container, because
			// we can't do that in a read-only context (when not
			// actually executing)
			return canCreateChild() && (!hasElementToEdit() || !(getPossibleCommands().isEmpty()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			Command command = prepareCommand();
			if (command.canExecute()) {
				// create an IUndoableOperation => undo is relying on
				// the execution of this command
				command.execute();
				return CommandResult.newOKCommandResult(newElement);
			}
			return CommandResult.newErrorCommandResult("Impossible to create Element");
		}

		protected Collection<Command> getPossibleCommands() {
			if (possibleCommands == null) {
				possibleCommands = new HashSet<Command>();
				EClass eClass = getElementType().getEClass();
				List<EClass> eClassesToCreateCommandsFor = new ArrayList<EClass>();
				if (eClass.isAbstract()) {
					// If we didn't receive a hint on the eClass to create
					if (eClassHint == null) {
						eClassesToCreateCommandsFor.addAll(EMFHelper.getSubclassesOf(eClass, true));
					} else {
						eClassesToCreateCommandsFor.add(eClassHint);
					}
				} else {
					eClassesToCreateCommandsFor.add(eClass);
				}
				if (eClassesToCreateCommandsFor.isEmpty()) {
					possibleCommands.add(UnexecutableCommand.INSTANCE);
				}
				EObject element = getElementToEdit();
				if (element != null) {
					// Do the default element creation
					EReference containment = getContainmentFeature();
					if (containment != null) {
						IEditingDomainItemProvider editingDomainItemProvider = AdapterFactoryEditingDomain.getEditingDomainItemProviderFor(element);
						ResourceSet resourceSet = EMFHelper.getResourceSet(element);
						for (EClass eClassToCreateCommandsFor : eClassesToCreateCommandsFor) {
							// Use the resource set's registered factory, if possible (UML-RT)
							EObject newElement = getFactory(eClassToCreateCommandsFor, resourceSet).create(eClassToCreateCommandsFor);
							Command possibleCommand = editingDomainItemProvider.createCommand(element, getEditingDomain(), CreateChildCommand.class, new CommandParameter(element, containment, new CommandParameter(null, containment, newElement)));
							if (possibleCommand == null) {
								possibleCommands.add(UnexecutableCommand.INSTANCE);
							} else {
								if (possibleCommand.canExecute()) {
									possibleCommands.add(possibleCommand);
								}
							}
						}
					}
				}
			}
			return possibleCommands;
		}

		/**
		 * Obtain the best factory for instantiation of the given {@code eClass}.
		 * This is either
		 * <ul>
		 * <li>the factory registered locally in the {@code resourceSet} for the
		 * {@code eClass}'s package, or</li>
		 * <li>in the very unusual case that the package is not registered, or
		 * if the {@code resourceSet} is {@code null}, the static factory
		 * of the {@code eClass}'s static package</li>
		 * </ul>
		 * 
		 * @param eClass
		 *            an Ecore class to instantiate
		 * @param resourceSet
		 *            the resource set in which context the model is being edited.
		 *            May be {@code null}
		 * 
		 * @return the factory to use for instantiation of the {@code eClass}
		 */
		EFactory getFactory(EClass eClass, ResourceSet resourceSet) {
			EFactory result = null;

			if (resourceSet != null) {
				result = resourceSet.getPackageRegistry().getEFactory(eClass.getEPackage().getNsURI());
			}

			if (result == null) {
				result = eClass.getEPackage().getEFactoryInstance();
			}

			return result;
		}

		/**
		 * Prepares the EMF command to which we delegate execution, if
		 * it has not already been created. This is not safe to do in a
		 * read-only context if our request does not {@link CreateEditBasedElementCommand#hasElementToEdit() have
		 * a container}, because this would attempt to create that
		 * container.
		 *
		 * @see CreateEditBasedElementCommand#hasElementToEdit()
		 */
		protected Command prepareCommand() {
			if (commandDone == null) {
				// Do the default element creation
				EReference containment = getContainmentFeature();
				EClass eClass = getElementType().getEClass();
				if (eClass.isAbstract()) {
					if (eClassHint == null) {
						// If we didn't receive a hint on the eClass to create
						// Propose to select appropriate concrete sub-metaclass
						try {
							ServicesRegistry registry = ServiceUtilsForResource.getInstance().getServiceRegistry(getElementToEdit().eResource());
							if (registry != null) {
								LabelProviderService labelProviderService = registry.getService(LabelProviderService.class);
								if (labelProviderService != null) {
									ElementListSelectionDialog dialog = new ElementListSelectionDialog(Display.getCurrent().getActiveShell(), labelProviderService.getLabelProvider());
									dialog.setTitle("Metaclass selection");
									dialog.setMessage("Select the a concrete sub-metaclass of " + eClass.getName());
									dialog.setElements(EMFHelper.getSubclassesOf(eClass, true).toArray());
									dialog.setMultipleSelection(false);
									dialog.open();
									Object[] results = dialog.getResult();
									if (results != null) {
										if (results.length > 0) {
											if (results[0] instanceof EClass) {
												eClass = (EClass) results[0];
											}
										}
									}
								} else {
									Activator.log.error(new NullPointerException());
									commandDone = UnexecutableCommand.INSTANCE;
								}
							} else {
								Activator.log.error(new NullPointerException());
								commandDone = UnexecutableCommand.INSTANCE;
							}
						} catch (ServiceException e) {
							Activator.log.error(e);
							commandDone = UnexecutableCommand.INSTANCE;
						}
					} else {
						eClass = eClassHint;
					}
				}
				if (containment != null) {
					EObject element = getElementToEdit();
					if (element != null) {
						ResourceSet resourceSet = EMFHelper.getResourceSet(element);
						// Use the resource set's registered factory, if possible (UML-RT)
						newElement = getFactory(eClass, resourceSet).create(eClass);
						IEditingDomainItemProvider editingDomainItemProvider = AdapterFactoryEditingDomain.getEditingDomainItemProviderFor(element);
						commandDone = editingDomainItemProvider.createCommand(element, getEditingDomain(), CreateChildCommand.class, new CommandParameter(element, containment, new CommandParameter(null, containment, newElement)));
					}
				}
				if (commandDone == null) {
					// Couldn't create a useful command
					commandDone = UnexecutableCommand.INSTANCE;
				}
			}
			return commandDone;
		}
	}

	/** Shadow the base class's element-to-edit because we need direct access. */
	private EObject elementToEdit;

	/** newly created element */
	protected EObject newElement;

	/**
	 * composite operation in charge of listing and executing all performed
	 * operations
	 */
	protected CompositeEMFOperation compositeEMFOperation;

	public static final String ECLASS_HINT = "ECLASS_HINT";

	protected EClass eClassHint = null;

	/**
	 * Constructor.
	 *
	 * @param request
	 *            create element request creating this command
	 */
	public CreateEditBasedElementCommand(CreateElementRequest request) {
		super(request);
		Object eClassHintParamValue = request.getParameter(ECLASS_HINT);
		if (eClassHintParamValue instanceof EClass) {
			eClassHint = (EClass) eClassHintParamValue;
		}
		EObject container = request.getContainer();
		if ((container != null) && container.eClass().getEAllContainments().contains(request.getContainmentFeature())) {
			setElementToEdit(container);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canExecute() {
		// The superclass implementation can short-circuit the effort of
		// preparing the operation
		return super.canExecute() && prepareOperation().canExecute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IUndoableOperation operation = prepareOperation();
		IStatus compositeStatus = operation.execute(monitor, info);
		return (compositeStatus == null) ? CommandResult.newOKCommandResult(newElement) : new CommandResult(compositeStatus, newElement);
	}

	protected IUndoableOperation prepareOperation() {
		if (compositeEMFOperation == null) {
			compositeEMFOperation = new CompositeEMFOperation(getEditingDomain(), "Create Element");
			// creates the basic element
			AbstractTransactionalCommand createTransactionalCommand = new CreateEditBasedElementTransactionalCommand(getEditingDomain(), "Create basic element", getAffectedFiles());
			compositeEMFOperation.add(createTransactionalCommand);
			AbstractTransactionalCommand configureTransactionalCommand = new AbstractTransactionalCommand(getEditingDomain(), "Configure element", getAffectedFiles()) {

				/**
				 * {@inheritDoc}ondre
				 */
				@Override
				protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					// Configure the new element
					ConfigureRequest configureRequest = createConfigureRequest();
					ICommand configureCommand = getElementType().getEditCommand(configureRequest);
					IStatus configureStatus = null;
					if (configureCommand != null && configureCommand.canExecute()) {
						configureStatus = configureCommand.execute(monitor, info);
					}
					// Put the newly created element in the request so that the
					// 'after' commands have access to it.
					getCreateRequest().setNewElement(newElement);
					return (configureStatus == null) ? CommandResult.newOKCommandResult(newElement) : new CommandResult(configureStatus, newElement);
				}
			};
			compositeEMFOperation.add(configureTransactionalCommand);
		}
		return compositeEMFOperation;
	}

	protected boolean isPrepared() {
		return compositeEMFOperation != null;
	}

	/**
	 * Creates the request to configure the new element.
	 *
	 * @return the request
	 */
	@Override
	protected ConfigureRequest createConfigureRequest() {
		ConfigureRequest configureRequest = new ConfigureRequest(getEditingDomain(), newElement, getElementType());
		// pass along the client context
		configureRequest.setClientContext(getCreateRequest().getClientContext());
		configureRequest.addParameters(getRequest().getParameters());
		return configureRequest;
	}

	/**
	 * Queries whether I know my element to edit, yet.
	 *
	 * @return whether I have an element to edit
	 */
	protected boolean hasElementToEdit() {
		return elementToEdit != null;
	}

	@Override
	protected void setElementToEdit(EObject element) {
		this.elementToEdit = element;
		super.setElementToEdit(element);
	}

	/**
	 * An enablement filter heuristically determining whether we think we will
	 * be able to create the child element.
	 *
	 * @return whether we can create the new child element
	 */
	protected boolean canCreateChild() {
		// Assume we can unless we think we can't
		boolean result = true;
		// This is an additional constraint in the EMF CreateChildCommand that
		// we use, that the GMF CreateElementCommand doesn't apply
		Object context = getRequest().getEditHelperContext();
		if (context instanceof EObject) {
			EObject owner = (EObject) context;
			EReference reference = getContainmentFeature();
			// The context may not have this reference if some intermediate
			// container is to be created by the edit-helper.
			// But, then, we can only optimistically report that we can create
			// the child
			if ((reference != null) && !reference.isMany() && (owner.eClass().getEAllReferences().contains(reference))) {
				// Don't replace an existing value
				result = ((EObject) context).eGet(reference) == null;
			}
		}
		return result;
	}

	@Override
	public void dispose() {
		if (isPrepared()) {
			compositeEMFOperation.dispose();
			compositeEMFOperation = null;
		}
		super.dispose();
	}
}
