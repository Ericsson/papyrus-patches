/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.BadStateException;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.resource.sasheditor.DiModel;
import org.eclipse.papyrus.infra.core.resource.sasheditor.SashModel;
import org.eclipse.papyrus.infra.core.sashwindows.di.service.IPageManager;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.papyrus.infra.gmfdiag.css.resource.CSSNotationModel;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class WorkspaceAndPapyrusEditor implements TestRule {
	public static boolean DEBUG = false; 
	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				startWorkbench(description);
				try {
					base.evaluate();
				} catch(Throwable th) {
					throw new RuntimeException("Failed to start test: " + th.getMessage(), th);			
				} finally {
					finished(description);
				}
			}
		};
	}

	private void startWorkbench(Description description) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProject project = getProject(description);

		try {
			// Close the Welcome view
			IViewReference welcome = page.findViewReference("org.eclipse.ui.internal.introview");
			if (welcome != null) {
				page.hideView(welcome);
			}

			// Open the editor
			editor = IDE.openEditor(page, diFile);
			page.setPartState(page.getReference(editor), IWorkbenchPage.STATE_MAXIMIZED);
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to open Papyrus editor: " + e.getMessage(), e);
			//fail("Failed to open Papyrus editor: " + e.getMessage());
		} finally {
			flushDisplayEvents();
		}

		EditingDomain editingDomain = editor.getAdapter(EditingDomain.class);
		resourceSet = editingDomain.getResourceSet();
	}

	public void waitForClose() {
		if (!DEBUG)
			return;
		System.out.println("GUI Test finished! Test Editor waiting for close to continue...");
		Display display = Display.getCurrent();
		while (!display.isDisposed() && editor != null && isPapyrusEditorOpen()) {
			if (!display.readAndDispatch())
				synchronized (this) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
		editor = null;
	}
	
	public void waitForClose(Diagram dia) {
		if (!DEBUG)
			return;
		System.out.println("GUI Test finished! Test Editor waiting for close to continue...");
		Display display = Display.getCurrent();
		while (!display.isDisposed() && editor != null && isPapyrusEditorOpen()) {
			IPageManager pageManager = editor.getAdapter(IPageManager.class);
			if (pageManager == null)
				return;
			if (!pageManager.isOpen(dia))
				return;
			
			if (!display.readAndDispatch())
				synchronized (this) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}
		editor = null;
	}

	public boolean isPapyrusEditorOpen() {
		if (PlatformUI.getWorkbench() == null || PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
			return false;
		}
		
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null)
			return false;
		
		IEditorReference[] refs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		if (refs.length == 0)
			return false;
		
		for (IEditorReference ref : refs) {
			if (ref.getEditor(false) == editor)
				return true;
		}
		
		return false;
	}
	
	public final void flushDisplayEvents() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
			// Pass
		}
	}

	protected void finished(Description description) {
		interaction = null;
		model = null;

		resourceSet.getResources().forEach(Resource::unload);
		resourceSet.getResources().clear();
		resourceSet.eAdapters().clear();
		resourceSet = null;
		
		if (editor != null) {
			editor.getSite().getPage().closeEditor(editor, false);
		}
		if (project != null) {
			try {
				project.delete(true, true, null);
			} catch (CoreException e) {
				e.printStackTrace();
				// Best effort
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected IProject getProject(Description description) {
		if (project == null) {
			String projectName = description.getMethodName();
			if (projectName == null) {
				// It's a class rule, then
				projectName = description.getDisplayName();
				// Note that if there's no dot, this gets the substring from zero
				projectName = projectName.substring(projectName.lastIndexOf('.') + 1);
				// Strip out all non-letters
				projectName = projectName.replaceAll("\\P{L}", "");
			}
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

			try {
				if (!project.exists()) {
					project.create(null);
				}
				if (!project.isAccessible()) {
					project.open(null);
				}
				
				diFile = project.getFile(projectName+".di");
				URI uri = URI.createPlatformResourceURI(diFile.getFullPath().toString(), true);
				final ModelSet modelSet = new ModelSet();
				modelSet.registerModel(new SashModel());
				modelSet.registerModel(new DiModel());
				UmlModel umlModel = new UmlModel();
				modelSet.registerModel(umlModel);	
				CSSNotationModel notationModel = new CSSNotationModel(); 
				modelSet.registerModel(notationModel);
				modelSet.getInternal().setPrimaryModelResourceURI(uri);
				modelSet.createsModels(diFile);	
				umlModel.initializeEmptyModel();
				modelSet.save(new NullProgressMonitor());

				ServicesRegistry registry = new ExtensionServicesRegistry(org.eclipse.papyrus.infra.core.Activator.PLUGIN_ID);
				try {
					registry.add(ModelSet.class, Integer.MAX_VALUE, modelSet);
					registry.startRegistry();
				} catch (ServiceException ex) {
					//Ignore
				}
				

				// create the UML model


			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Failed to create/open project: " + e.getMessage(),e);
			}
		}

		return project;
	}

	public IProject getProject() {
		return project;
	}

	public String getPath() {
		return path;
	}

	public IEditorPart getEditor() {
		return editor;
	}

	public ModelSet getResourceSet() {
		return (ModelSet)resourceSet;
	}

	@SuppressWarnings("unchecked")
	public void initDiagramAndModel(Element element, Diagram diagram) {
		ModelSet modelSet = getResourceSet();
		UmlModel uml = (UmlModel)modelSet.getModel(UmlModel.MODEL_ID);
		NotationModel notation = (NotationModel)modelSet.getModel(NotationModel.MODEL_ID);
		try {
			modelSet.loadModel(uml.getIdentifier());
			modelSet.loadModel(notation.getIdentifier());
		} catch (BadStateException e) {
			e.printStackTrace();
		}
		List<EObject> contents = uml.getResource().getContents();
		if (contents.size() > 0) {
			org.eclipse.uml2.uml.Package p = (org.eclipse.uml2.uml.Package)uml.getResource().getContents().get(0);
			contents = (List)p.getPackagedElements();
		}
		
		contents.add(element);
		notation.getResource().getContents().add(diagram);		
	}
	
	public EditPartViewer getEditPartViewer() {
		Object viewer = editor.getAdapter(IDiagramGraphicalViewer.class);
		if (viewer instanceof EditPartViewer) {
			return (EditPartViewer) viewer;
		}	
		return null;
	}
	
	public IPageManager getPapyrusEditorPageManager() {
		return editor.getAdapter(IPageManager.class);
	}

	public void openDiagram(Diagram diagram) {
		try {
			// Open the diagram
			IPageManager pageManager = editor.getAdapter(IPageManager.class);
	
			if (pageManager.isOpen(diagram)) {
				pageManager.selectPage(diagram);
			} else {
				pageManager.openPage(diagram);
			}
		} finally {
			flushDisplayEvents();
		}
	}
	
	public void closeDiagram(Diagram diagram) {
		try {
			// Open the diagram
			IPageManager pageManager = editor.getAdapter(IPageManager.class);
	
			if (!pageManager.isOpen(diagram)) {
				return;
			} else 
			pageManager.closePage(diagram);
			
		} finally {
			flushDisplayEvents();
		}
	}
	
	public void closeDiagrams() {
		try {
			// Open the diagram
			if (editor != null) {
				IPageManager pageManager = editor.getAdapter(IPageManager.class);
				pageManager.closeAllOpenedPages();
			}
			
		} finally {
			flushDisplayEvents();
		}
		
	}
	
	private IProject project;
	private IFile diFile;
	private String path;
	private IEditorPart editor;	
	private ResourceSet resourceSet;
	private Package model;
	private Interaction interaction;
}
