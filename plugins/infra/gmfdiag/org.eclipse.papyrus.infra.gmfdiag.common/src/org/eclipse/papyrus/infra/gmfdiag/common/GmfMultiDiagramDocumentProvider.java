/*****************************************************************************
 * Copyright (c) 2008 CEA LIST.
 *
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
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.DiagramEditingDomainFactory;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.AbstractDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.DiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocumentProvider;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.EditorStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.internal.util.DiagramIOUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.resources.GMFResourceFactory;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class GmfMultiDiagramDocumentProvider extends AbstractDocumentProvider implements IDiagramDocumentProvider,
		IEditingDomainProvider {

	public static final String EditingDomainID = "com.cea.papyrus.core.PapyrusEditingDomainID"; //$NON-NLS-1$

	/**
	 * The shared ResourceSet.
	 */
	private TransactionalEditingDomain sharedEditingDomain = null;

	/**
	 * ResourceSet to use when creation EditingDomain. Can be null.
	 */
	private ResourceSet resourceSet;

	/**
	 * @param resourceSet
	 */
	public GmfMultiDiagramDocumentProvider(ResourceSet resourceSet) {
		super();
		this.resourceSet = resourceSet;
	}

	/**
	 * @param resourceSet
	 */
	public GmfMultiDiagramDocumentProvider(TransactionalEditingDomain editingDomain) {
		super();
		this.sharedEditingDomain = editingDomain;
		configureEditingDomain(sharedEditingDomain);
	}

	/**
	 * Constructor.
	 */
	public GmfMultiDiagramDocumentProvider() {
	}

	@Override
	protected ElementInfo createElementInfo(Object element) throws CoreException {
		if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, NLS.bind(
					Messages.GmfMultiDiagramDocumentProvider_IncorrectInputError, new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
		IEditorInput editorInput = (IEditorInput) element;
		IDiagramDocument document = (IDiagramDocument) createDocument(editorInput);

		ResourceSetInfo info = new ResourceSetInfo(document, editorInput);
		info.setModificationStamp(computeModificationStamp(info));
		info.fStatus = null;
		return info;
	}

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		if (false == element instanceof FileEditorInput && false == element instanceof URIEditorInput) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, NLS.bind(
					Messages.GmfMultiDiagramDocumentProvider_IncorrectInputError, new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
		IDocument document = createEmptyDocument();
		setDocumentContent(document, (IEditorInput) element);
		setupDocument(element, document);
		return document;
	}

	/**
	 * Sets up the given document as it would be provided for the given element. The content of the
	 * document is not changed. This default implementation is empty. Subclasses may reimplement.
	 *
	 * @param element
	 *            the blue-print element
	 * @param document
	 *            the document to set up
	 */
	protected void setupDocument(Object element, IDocument document) {
		// for subclasses
	}

	private long computeModificationStamp(ResourceSetInfo info) {
		int result = 0;
		for (Iterator<Resource> it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
			Resource nextResource = it.next();

			// bug 347300: skip faulty URIs
			if (isBadURI(nextResource)) {
				continue;
			}
			IFile file = WorkspaceSynchronizer.getFile(nextResource);
			if (file != null) {
				if (file.getLocation() != null) {
					result += file.getLocation().toFile().lastModified();
				} else {
					result += file.getModificationStamp();
				}
			}
		}
		return result;
	}

	/**
	 * Return true if the URI is a faulty URI, false otherwise.
	 * Faulty URI: scheme = pathmap, platform
	 *
	 * @param resource
	 * @return
	 */
	private boolean isBadURI(Resource resource) {
		try {
			java.net.URI uri = new java.net.URI(resource.getURI().toString());
			String scheme = uri.getScheme();
			if ("pathmap".equals(scheme) || "platform".equals(scheme)) {
				return true;
			}

			// not a bad uri
			return false;
		} catch (URISyntaxException e) {
			return true;
		}
	}

	@Override
	protected IDocument createEmptyDocument() {
		DiagramDocument document = new DiagramDocument();
		document.setEditingDomain(createEditingDomain());
		return document;
	}

	/**
	 * Create the editing Domain. All Editing Domain will be created with the same ResourceSet. The
	 * first creation will record the ResourceSet, other creation will use it.
	 */
	private TransactionalEditingDomain createEditingDomain() {
		TransactionalEditingDomain editingDomain = null;
		// Check if edit domain exist
		if (sharedEditingDomain != null) { // Already initialized
			editingDomain = sharedEditingDomain;
			// System.out.println(this.getClass().getSimpleName()
			// + ".createEditingDomain() - got EditingDomain from previous call (" + editingDomain + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			return editingDomain;
		}

		// No editingDomain. Create and initialize a new one.

		// Try to get a registered one if any
		editingDomain = TransactionalEditingDomain.Registry.INSTANCE.getEditingDomain(EditingDomainID);
		if (editingDomain != null) {
			// got it
			sharedEditingDomain = editingDomain;
			Activator.log.debug((this.getClass().getSimpleName()
					+ ".createEditingDomain() - got EditingDomain from REGISTRY (" + editingDomain + ")")); //$NON-NLS-1$ //$NON-NLS-2$
			return editingDomain;
		}

		// Create EditingDomain using provided ResourceSet if any.
		if (resourceSet == null) {
			editingDomain = DiagramEditingDomainFactory.getInstance().createEditingDomain();
		} else {
			editingDomain = DiagramEditingDomainFactory.getInstance().createEditingDomain(resourceSet);
		}
		sharedEditingDomain = editingDomain;
		editingDomain.setID(EditingDomainID);
		Activator.log.debug(this.getClass().getSimpleName()
				+ ".createEditingDomain() - create a new EditingDomain (" + editingDomain + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		configureEditingDomain(editingDomain);

		return editingDomain;
	}

	/**
	 * Configure an EditingDomain suitable for GMF. This method should be called only once for an
	 * EditingDomain.
	 *
	 * @param editingDomain
	 */
	private void configureEditingDomain(TransactionalEditingDomain editingDomain) {
		// Add listener on resource change
		final NotificationFilter diagramResourceModifiedFilter = NotificationFilter.createNotifierFilter(
				editingDomain.getResourceSet()).and(NotificationFilter.createEventTypeFilter(Notification.ADD)).and(
						NotificationFilter.createFeatureFilter(ResourceSet.class, ResourceSet.RESOURCE_SET__RESOURCES));
		editingDomain.getResourceSet().eAdapters().add(new Adapter() {

			private Notifier myTarger;

			@Override
			public Notifier getTarget() {
				return myTarger;
			}

			@Override
			public boolean isAdapterForType(Object type) {
				return false;
			}

			@Override
			public void notifyChanged(Notification notification) {
				if (diagramResourceModifiedFilter.matches(notification)) {
					Object value = notification.getNewValue();
					if (value instanceof Resource) {
						((Resource) value).setTrackingModification(true);
					}
				}
			}

			@Override
			public void setTarget(Notifier newTarget) {
				myTarger = newTarget;
			}

		});
	}


	protected void setDocumentContent(IDocument document, IEditorInput element) throws CoreException {
		IDiagramDocument diagramDocument = (IDiagramDocument) document;
		TransactionalEditingDomain domain = diagramDocument.getEditingDomain();
		if (element instanceof FileEditorInput) {
			IStorage storage = ((FileEditorInput) element).getStorage();
			Diagram diagram = DiagramIOUtil.load(domain, storage, true, getProgressMonitor());
			document.setContent(diagram);
		} else if (element instanceof URIEditorInput) {
			URI uri = ((URIEditorInput) element).getURI();
			Resource resource = null;
			try {
				resource = domain.getResourceSet().getResource(uri.trimFragment(), false);
				if (resource == null) {
					resource = domain.getResourceSet().createResource(uri.trimFragment());
				}
				if (!resource.isLoaded()) {
					try {
						Map options = new HashMap(GMFResourceFactory.getDefaultLoadOptions());
						// @see 171060
						// options.put(org.eclipse.emf.ecore.xmi.XMLResource.OPTION_RECORD_UNKNOWN_FEATURE,
						// Boolean.TRUE);
						resource.load(options);
					} catch (IOException e) {
						resource.unload();
						throw e;
					}
				}
				if (uri.fragment() != null) {
					EObject rootElement = resource.getEObject(uri.fragment());
					if (rootElement instanceof Diagram) {
						document.setContent(rootElement);
						return;
					}
				} else {
					for (Iterator it = resource.getContents().iterator(); it.hasNext();) {
						Object rootElement = it.next();
						if (rootElement instanceof Diagram) {
							document.setContent(rootElement);
							return;
						}
					}
				}
				throw new RuntimeException(Messages.GmfMultiDiagramDocumentProvider_NoDiagramInResourceError);
			} catch (Exception e) {
				CoreException thrownExcp = null;
				if (e instanceof CoreException) {
					thrownExcp = (CoreException) e;
				} else {
					String msg = e.getLocalizedMessage();
					thrownExcp = new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, msg != null ? msg
							: Messages.GmfMultiDiagramDocumentProvider_DiagramLoadingError, e));
				}
				throw thrownExcp;
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, NLS.bind(
					Messages.GmfMultiDiagramDocumentProvider_IncorrectInputError, new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
					null));
		}
	}

	@Override
	public long getModificationStamp(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			return computeModificationStamp(info);
		}
		return super.getModificationStamp(element);
	}

	@Override
	public boolean isDeleted(Object element) {
		IDiagramDocument document = getDiagramDocument(element);
		if (document != null) {
			Resource diagramResource = document.getDiagram().eResource();
			if (diagramResource != null) {
				IFile file = WorkspaceSynchronizer.getFile(diagramResource);
				return file == null || file.getLocation() == null || !file.getLocation().toFile().exists();
			}
		}
		return super.isDeleted(element);
	}

	public ResourceSetInfo getResourceSetInfo(Object editorInput) {
		return (ResourceSetInfo) super.getElementInfo(editorInput);
	}

	@Override
	protected void disposeElementInfo(Object element, ElementInfo info) {
		if (info instanceof ResourceSetInfo) {
			ResourceSetInfo resourceSetInfo = (ResourceSetInfo) info;
			resourceSetInfo.dispose();
		}
		super.disposeElementInfo(element, info);
	}


	@Override
	protected void doValidateState(Object element, Object computationContext) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection files2Validate = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null && file.isReadOnly()) {
					files2Validate.add(file);
				}
			}
			ResourcesPlugin.getWorkspace().validateEdit(
					(IFile[]) files2Validate.toArray(new IFile[files2Validate.size()]), computationContext);
		}

		super.doValidateState(element, computationContext);
	}


	@Override
	public boolean isReadOnly(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			if (info.isUpdateCache()) {
				try {
					updateCache(element);
				} catch (CoreException ex) {
					Activator.getInstance().logError(Messages.GmfMultiDiagramDocumentProvider_isModifiable, ex);
					// Error message to log was initially taken from
					// org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.internal.l10n.EditorMessages.StorageDocumentProvider_isModifiable
				}
			}
			return info.isReadOnly();
		}
		return super.isReadOnly(element);
	}


	@Override
	public boolean isModifiable(Object element) {
		if (!isStateValidated(element)) {
			if (element instanceof FileEditorInput || element instanceof URIEditorInput) {
				return true;
			}
		}
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			if (info.isUpdateCache()) {
				try {
					updateCache(element);
				} catch (CoreException ex) {
					Activator.getInstance().logError(Messages.GmfMultiDiagramDocumentProvider_isModifiable, ex);
					// Error message to log was initially taken from
					// org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.internal.l10n.EditorMessages.StorageDocumentProvider_isModifiable
				}
			}
			return info.isModifiable();
		}
		return super.isModifiable(element);
	}

	protected void updateCache(Object element) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null && file.isReadOnly()) {
					info.setReadOnly(true);
					info.setModifiable(false);
					return;
				}
			}
			info.setReadOnly(false);
			info.setModifiable(true);
			return;
		}
	}


	@Override
	protected void doUpdateStateCache(Object element) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			info.setUpdateCache(true);
		}
		super.doUpdateStateCache(element);
	}


	@Override
	public boolean isSynchronized(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			return info.isSynchronized();
		}
		return super.isSynchronized(element);
	}


	@Override
	protected ISchedulingRule getResetRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}


	@Override
	protected ISchedulingRule getSaveRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(computeSchedulingRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}


	@Override
	protected ISchedulingRule getSynchronizeRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection rules = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					rules.add(ResourcesPlugin.getWorkspace().getRuleFactory().refreshRule(file));
				}
			}
			return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
		}
		return null;
	}


	@Override
	protected ISchedulingRule getValidateStateRule(Object element) {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			Collection files = new ArrayList();
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				IFile file = WorkspaceSynchronizer.getFile(nextResource);
				if (file != null) {
					files.add(file);
				}
			}
			return ResourcesPlugin.getWorkspace().getRuleFactory().validateEditRule(
					(IFile[]) files.toArray(new IFile[files.size()]));
		}
		return null;
	}


	private ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		if (toCreateOrModify.exists()) {
			return ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(toCreateOrModify);
		}

		IResource parent = toCreateOrModify;
		do {
			/*
			 * XXX This is a workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=67601
			 * IResourceRuleFactory.createRule should iterate the hierarchy itself.
			 */
			toCreateOrModify = parent;
			parent = toCreateOrModify.getParent();
		} while (parent != null && !parent.exists());

		return ResourcesPlugin.getWorkspace().getRuleFactory().createRule(toCreateOrModify);
	}


	@Override
	protected void doSynchronize(Object element, IProgressMonitor monitor) throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			for (Iterator it = info.getResourceSet().getResources().iterator(); it.hasNext();) {
				Resource nextResource = (Resource) it.next();
				handleElementChanged(info, nextResource, monitor);
			}
			return;
		}
		super.doSynchronize(element, monitor);
	}


	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		ResourceSetInfo info = getResourceSetInfo(element);
		if (info != null) {
			if (!overwrite && !info.isSynchronized()) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.ID, IResourceStatus.OUT_OF_SYNC_LOCAL,
						Messages.GmfMultiDiagramDocumentProvider_UnsynchronizedFileSaveError, null));
			}
			info.stopResourceListening();
			fireElementStateChanging(element);
			List resources = info.getResourceSet().getResources();
			try {
				monitor.beginTask(Messages.GmfMultiDiagramDocumentProvider_SaveDiagramTask, resources.size() + 1); // "Saving diagram"
				for (Iterator it = resources.iterator(); it.hasNext();) {
					Resource nextResource = (Resource) it.next();
					monitor.setTaskName(NLS.bind(Messages.GmfMultiDiagramDocumentProvider_SaveNextResourceTask,
							nextResource.getURI()));
					if (nextResource.isLoaded() && !info.getEditingDomain().isReadOnly(nextResource)) {
						try {
							nextResource.save(UMLDiagramEditorUtil.getSaveOptions());
						} catch (IOException e) {
							fireElementStateChangeFailed(element);
							throw new CoreException(new Status(IStatus.ERROR, Activator.ID,
									EditorStatusCodes.RESOURCE_FAILURE, e.getLocalizedMessage(), null));
						}
					}
					monitor.worked(1);
				}
				monitor.done();
				info.setModificationStamp(computeModificationStamp(info));
			} catch (RuntimeException x) {
				fireElementStateChangeFailed(element);
				throw x;
			} finally {
				info.startResourceListening();
			}
		} else {
			URI newResoruceURI;
			List affectedFiles = null;
			if (element instanceof FileEditorInput) {
				IFile newFile = ((FileEditorInput) element).getFile();
				affectedFiles = Collections.singletonList(newFile);
				newResoruceURI = URI.createPlatformResourceURI(newFile.getFullPath().toString(), true);
			} else if (element instanceof URIEditorInput) {
				newResoruceURI = ((URIEditorInput) element).getURI();
			} else {
				fireElementStateChangeFailed(element);
				throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, NLS.bind(
						Messages.GmfMultiDiagramDocumentProvider_IncorrectInputError, new Object[] { element, "org.eclipse.ui.part.FileEditorInput", "org.eclipse.emf.common.ui.URIEditorInput" }), //$NON-NLS-1$ //$NON-NLS-2$
						null));
			}
			if (false == document instanceof IDiagramDocument) {
				fireElementStateChangeFailed(element);
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								Activator.ID,
								0,
								"Incorrect document used: " + document + " instead of org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			IDiagramDocument diagramDocument = (IDiagramDocument) document;
			final Resource newResource = diagramDocument.getEditingDomain().getResourceSet().createResource(
					newResoruceURI);
			final Diagram diagramCopy = EcoreUtil.copy(diagramDocument.getDiagram());
			try {
				new AbstractTransactionalCommand(diagramDocument.getEditingDomain(), NLS.bind(
						Messages.GmfMultiDiagramDocumentProvider_SaveAsOperation, diagramCopy.getName()), affectedFiles) {

					@Override
					protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info)
							throws ExecutionException {
						newResource.getContents().add(diagramCopy);
						return CommandResult.newOKCommandResult();
					}
				}.execute(monitor, null);
				newResource.save(UMLDiagramEditorUtil.getSaveOptions());
			} catch (ExecutionException e) {
				fireElementStateChangeFailed(element);
				throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, e.getLocalizedMessage(), null));
			} catch (IOException e) {
				fireElementStateChangeFailed(element);
				throw new CoreException(new Status(IStatus.ERROR, Activator.ID, 0, e.getLocalizedMessage(), null));
			}
			newResource.unload();
		}
	}


	protected void handleElementChanged(ResourceSetInfo info, Resource changedResource, IProgressMonitor monitor) {
		IFile file = WorkspaceSynchronizer.getFile(changedResource);
		if (file != null) {
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException ex) {
				Activator.getInstance().logError(Messages.GmfMultiDiagramDocumentProvider_handleElementContentChanged,
						ex);
				// Error message to log was initially taken from
				// org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide.internal.l10n.EditorMessages.FileDocumentProvider_handleElementContentChanged
			}
		}
		changedResource.unload();

		fireElementContentAboutToBeReplaced(info.getEditorInput());
		removeUnchangedElementListeners(info.getEditorInput(), info);
		info.fStatus = null;
		try {
			setDocumentContent(info.fDocument, info.getEditorInput());
		} catch (CoreException e) {
			info.fStatus = e.getStatus();
		}
		if (!info.fCanBeSaved) {
			info.setModificationStamp(computeModificationStamp(info));
		}
		addUnchangedElementListeners(info.getEditorInput(), info);
		fireElementContentReplaced(info.getEditorInput());
	}

	protected void handleElementMoved(IEditorInput input, URI uri) {
		if (input instanceof FileEditorInput) {
			IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(
					new Path(URI.decode(uri.path())).removeFirstSegments(1));
			fireElementMoved(input, newFile == null ? null : new FileEditorInput(newFile));
			return;
		}
		// TODO: append suffix to the URI! (use diagram as a parameter)
		fireElementMoved(input, new URIEditorInput(uri));
	}


	@Override
	public IEditorInput createInputWithEditingDomain(IEditorInput editorInput, TransactionalEditingDomain domain) {
		return editorInput;
	}


	@Override
	public IDiagramDocument getDiagramDocument(Object element) {
		IDocument doc = getDocument(element);
		if (doc instanceof IDiagramDocument) {
			return (IDiagramDocument) doc;
		}
		return null;
	}


	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		return null;
	}

	
	protected class ResourceSetInfo extends ElementInfo {


		private long myModificationStamp = IResource.NULL_STAMP;


		private WorkspaceSynchronizer mySynchronizer;


		private Collection myUnSynchronizedResources = new ArrayList();


		private IDiagramDocument myDocument;


		private IEditorInput myEditorInput;


		private boolean myUpdateCache = true;


		private boolean myModifiable = false;


		private boolean myReadOnly = true;


		private ResourceSetModificationListener myResourceSetListener;

		public ResourceSetInfo(IDiagramDocument document, IEditorInput editorInput) {
			super(document);
			myDocument = document;
			myEditorInput = editorInput;
			startResourceListening();
			for (Adapter a : getResourceSet().eAdapters()) {
				if (a instanceof ResourceSetModificationListener) {
					myResourceSetListener = (ResourceSetModificationListener) a;
					break;
				}
			}
			if (myResourceSetListener == null) {
				myResourceSetListener = new ResourceSetModificationListener();
				getResourceSet().eAdapters().add(myResourceSetListener);
			}
			myResourceSetListener.addResourceSet(this);
		}

		public long getModificationStamp() {
			return myModificationStamp;
		}

		public void setModificationStamp(long modificationStamp) {
			myModificationStamp = modificationStamp;
		}

		public TransactionalEditingDomain getEditingDomain() {
			return myDocument.getEditingDomain();
		}

		public ResourceSet getResourceSet() {
			return getEditingDomain().getResourceSet();
		}

		public IEditorInput getEditorInput() {
			return myEditorInput;
		}

		public void dispose() {
			stopResourceListening();
			myResourceSetListener.removeResourceSet(this);
			if (myResourceSetListener.isEmpty()) {
				getResourceSet().eAdapters().remove(myResourceSetListener);
			}
			// for(Iterator it = getResourceSet().getResources().iterator(); it.hasNext();) {
			// Resource resource = (Resource)it.next();
			// Do not unload the resource because the DocumentProvider can be disposed while its
			// Diagram node is
			// kept for future re-openeing.
			// resource.unload();
			// }
		}


		public boolean isSynchronized() {
			return myUnSynchronizedResources.isEmpty();
		}


		public void setUnSynchronized(Resource resource) {
			myUnSynchronizedResources.add(resource);
		}


		public void setSynchronized(Resource resource) {
			myUnSynchronizedResources.remove(resource);
		}


		public final void stopResourceListening() {
			// mySynchronizer.dispose();
			mySynchronizer = null;
		}


		public final void startResourceListening() {
			// Do not listen because all document use the same EditingDomain
			// mySynchronizer = new WorkspaceSynchronizer(getEditingDomain(), new
			// SynchronizerDelegate());
		}

		public boolean isUpdateCache() {
			return myUpdateCache;
		}


		public void setUpdateCache(boolean update) {
			myUpdateCache = update;
		}


		public boolean isModifiable() {
			return myModifiable;
		}


		public void setModifiable(boolean modifiable) {
			myModifiable = modifiable;
		}


		public boolean isReadOnly() {
			return myReadOnly;
		}


		public void setReadOnly(boolean readOnly) {
			myReadOnly = readOnly;
		}


		private class SynchronizerDelegate implements WorkspaceSynchronizer.Delegate {


			@Override
			public void dispose() {
			}


			@Override
			public boolean handleResourceChanged(final Resource resource) {
				synchronized (ResourceSetInfo.this) {
					if (ResourceSetInfo.this.fCanBeSaved) {
						ResourceSetInfo.this.setUnSynchronized(resource);
						return true;
					}
				}
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						handleElementChanged(ResourceSetInfo.this, resource, null);
					}
				});
				return true;
			}


			@Override
			public boolean handleResourceDeleted(Resource resource) {
				synchronized (ResourceSetInfo.this) {
					if (ResourceSetInfo.this.fCanBeSaved) {
						ResourceSetInfo.this.setUnSynchronized(resource);
						return true;
					}
				}
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						fireElementDeleted(ResourceSetInfo.this.getEditorInput());
					}
				});
				return true;
			}

			@Override
			public boolean handleResourceMoved(Resource resource, final URI newURI) {
				synchronized (ResourceSetInfo.this) {
					if (ResourceSetInfo.this.fCanBeSaved) {
						ResourceSetInfo.this.setUnSynchronized(resource);
						return true;
					}
				}
				if (myDocument.getDiagram().eResource() == resource) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							handleElementMoved(ResourceSetInfo.this.getEditorInput(), newURI);
						}
					});
				} else {
					handleResourceDeleted(resource);
				}
				return true;
			}

		}

	}


	private class ResourceSetModificationListener extends EContentAdapter {

		private NotificationFilter myModifiedFilter;

		private List<ResourceSetInfo> myInfos;

		public ResourceSetModificationListener() {
			myInfos = new ArrayList<GmfMultiDiagramDocumentProvider.ResourceSetInfo>();
			myModifiedFilter = NotificationFilter.createEventTypeFilter(Notification.SET).or(
					NotificationFilter.createEventTypeFilter(Notification.UNSET)).and(
							NotificationFilter.createFeatureFilter(Resource.class, Resource.RESOURCE__IS_MODIFIED));
		}

		public void addResourceSet(ResourceSetInfo info) {
			myInfos.add(info);
		}

		public boolean removeResourceSet(ResourceSetInfo info) {
			return myInfos.remove(info);
		}

		public boolean isEmpty() {
			return myInfos.isEmpty();
		}

	
		@Override
		public void notifyChanged(Notification notification) {
			if (notification.getNotifier() instanceof ResourceSet) {
				super.notifyChanged(notification);
			}
			if (!notification.isTouch() && myModifiedFilter.matches(notification)) {
				if (notification.getNotifier() instanceof Resource) {
					Resource resource = (Resource) notification.getNotifier();
					if (resource.isLoaded()) {
						boolean modified = false;
						for (Iterator it = resource.getResourceSet().getResources().iterator(); it.hasNext() && !modified;) {
							Resource nextResource = (Resource) it.next();
							if (nextResource.isLoaded()) {
								modified = nextResource.isModified();
							}
						}
						for (ResourceSetInfo myInfo : myInfos) {
							boolean dirtyStateChanged = false;
							synchronized (myInfo) {
								if (modified != myInfo.fCanBeSaved) {
									myInfo.fCanBeSaved = modified;
									dirtyStateChanged = true;
								}
								if (!resource.isModified()) {
									myInfo.setSynchronized(resource);
								}
							}
							if (dirtyStateChanged) {
								fireElementDirtyStateChanged(myInfo.getEditorInput(), modified);
								if (!modified) {
									myInfo.setModificationStamp(computeModificationStamp(myInfo));
								}
							}
						}
					}
				}
			}
		}

	}

	/**
	 * Return the editingDomain
	 */
	@Override
	public EditingDomain getEditingDomain() {
		if (sharedEditingDomain == null) {
			createEditingDomain();
		}
		return sharedEditingDomain;
	}

}
