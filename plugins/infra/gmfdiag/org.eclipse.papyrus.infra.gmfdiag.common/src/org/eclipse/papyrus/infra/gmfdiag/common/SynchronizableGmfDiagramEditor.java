/*****************************************************************************
 * Copyright (c) 2010, 2017 CEA LIST, Christian W. Damus, and others.
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
 *  Christian W. Damus (CEA) - bug 437217
 *  Christian W. Damus - bug 451683
 *  Christian W. Damus - bug 465416
 *  Simon Delisle - Move ReconcilerHelper to a separate class
 *  Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Bug 491816
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IPrimaryEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.properties.WorkspaceViewerProperties;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.papyrus.commands.CheckedDiagramCommandStack;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.ReconcileHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.reconciler.DiagramVersioningUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.CommandIds;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.ServiceUtilsForEditPart;
import org.eclipse.papyrus.infra.sync.service.ISyncService;
import org.eclipse.papyrus.infra.ui.editor.reload.IReloadContextProvider;
import org.eclipse.papyrus.infra.ui.util.EclipseCommandUtils;
import org.eclipse.papyrus.infra.widgets.util.IRevealSemanticElement;
import org.eclipse.papyrus.infra.widgets.util.NavigationTarget;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 *
 * This GMF editor contains a methods in order to reveal visual element from a list of semantic element.
 * <p/>
 * It also provides subclasses with common capability to automatically migrate (reconcile) input diagrams between papurus versions.
 */

@SuppressWarnings("restriction")
// suppress the warning for WorkspaceViewerProperties
public class SynchronizableGmfDiagramEditor extends DiagramDocumentEditor implements IRevealSemanticElement, NavigationTarget {

	private Collection<PalettePageWrapper> palettePages;

	private Object palettePageState;

	public SynchronizableGmfDiagramEditor(boolean hasFlyoutPalette) {
		super(hasFlyoutPalette);
	}

	/**
	 * reveal all editpart that represent an element in the given list.
	 *
	 * @see org.eclipse.papyrus.infra.core.ui.IRevealSemanticElement#revealSemanticElement(java.util.List)
	 *
	 */
	@Override
	public void revealSemanticElement(List<?> elementList) {
		revealElement(elementList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean revealElement(Object element) {
		return revealElement(Collections.singleton(element));
	}


	/**
	 * {@inheritDoc}
	 *
	 * reveal all editpart that represent an element in the given list.
	 *
	 * @see org.eclipse.papyrus.infra.core.ui.IRevealSemanticElement#revealSemanticElement(java.util.List)
	 *
	 */
	@Override
	public boolean revealElement(Collection<?> elementList) {
		// create an instance that can get semantic element from gmf
		SemanticFromGMFElement semanticFromGMFElement = new SemanticFromGMFElement();

		// get the graphical viewer
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		if (graphicalViewer != null) {

			// look amidst all edit part if the semantic is contained in the list
			Iterator<?> iter = graphicalViewer.getEditPartRegistry().values().iterator();
			IGraphicalEditPart researchedEditPart = null;
			List<?> clonedList = new ArrayList<>(elementList);
			List<IGraphicalEditPart> partSelection = new ArrayList<>();

			while (iter.hasNext() && !clonedList.isEmpty()) {
				Object currentEditPart = iter.next();
				// look only amidst IPrimary editpart to avoid compartment and labels of links
				if (currentEditPart instanceof IPrimaryEditPart) {
					Object currentElement = semanticFromGMFElement.getSemanticElement(currentEditPart);
					if (clonedList.contains(currentElement)) {
						clonedList.remove(currentElement);
						researchedEditPart = ((IGraphicalEditPart) currentEditPart);
						partSelection.add(researchedEditPart);

					}
				}
			}

			// We may also search for a GMF View (Instead of a semantic model Element)
			if (!clonedList.isEmpty()) {
				for (Iterator<?> iterator = clonedList.iterator(); iterator.hasNext();) {
					Object element = iterator.next();
					if (graphicalViewer.getEditPartRegistry().containsKey(element) && !clonedList.isEmpty()) {
						iterator.remove();
						researchedEditPart = (IGraphicalEditPart) graphicalViewer.getEditPartRegistry().get(element);
						partSelection.add(researchedEditPart);
					}
				}
			}

			// the second test, as the model element is not a PrimaryEditPart, is to allow the selection even if the user selected it with other elements
			// and reset the selection if only the model is selected
			if (clonedList.isEmpty() || (clonedList.size() == 1 && clonedList.get(0) == getDiagram().getElement())) {
				// all parts have been found
				IStructuredSelection sSelection = new StructuredSelection(partSelection);
				// this is used instead of graphicalViewer.select(IGraphicalEditPart) as the later only allows the selection of a single element
				graphicalViewer.setSelection(sSelection);
				if (!partSelection.isEmpty()) {
					graphicalViewer.reveal(partSelection.get(0));
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
		if (type == DiagramEditPart.class) {
			return getDiagramEditPart();
		}
		if (type == Diagram.class) {
			return getDiagram();
		}
		if (type == IReloadContextProvider.class) {
			return new DiagramReloadContextProvider(this);
		}
		if (type == PalettePage.class) {
			if (palettePages == null) {
				palettePages = Lists.newArrayListWithExpectedSize(1);
			} else {
				cleanUpPalettePages();
				if (!palettePages.isEmpty()) {
					// Make the new page look just like the last one (for continuity of the UI when the
					// PapyrusPaletteSynchronizer causes a new page to be created)
					Iterables.getLast(palettePages, null).saveState();
				}
			}
			PalettePageWrapper result = new PalettePageWrapper((CustomPalettePage) super.getAdapter(type));
			palettePages.add(result);
			return result;
		}
		if (type == IContentOutlinePage.class) {
			Object result = super.getAdapter(type);
			if (result instanceof ContentOutlinePage) {
				result = new OutlinePageWrapper((ContentOutlinePage) result);
			}
			return result;
		}
		return super.getAdapter(type);
	}

	Collection<? extends PalettePage> getPalettePages() {
		if (palettePages == null) {
			return Collections.emptyList();
		} else {
			cleanUpPalettePages();
			return Collections.unmodifiableCollection(palettePages);
		}
	}

	void setDeferredPalettePageReloadContext(Object reloadContext) {
		palettePageState = reloadContext;
	}

	private void cleanUpPalettePages() {
		for (Iterator<PalettePageWrapper> iter = palettePages.iterator(); iter.hasNext();) {
			if (iter.next().isDisposed()) {
				iter.remove();
			}
		}
	}

	/**
	 * Configures my diagram edit domain with its command stack.
	 * This method has been completely overridden in order to use a proxy stack.
	 */
	@Override
	protected void configureDiagramEditDomain() {

		DefaultEditDomain editDomain = getEditDomain();

		if (editDomain != null) {
			CommandStack stack = editDomain.getCommandStack();
			if (stack != null) {
				// dispose the old stack
				stack.dispose();
			}

			// create and assign the new stack
			CheckedDiagramCommandStack diagramStack = new CheckedDiagramCommandStack(getDiagramEditDomain());

			editDomain.setCommandStack(diagramStack);
		}

		DiagramEditDomain diagEditDomain = (DiagramEditDomain) getDiagramEditDomain();
		diagEditDomain.setActionManager(createActionManager());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		super.setFocus();
		updateToggleActionState();
	}


	/**
	 * this command update the status of the toggle actions
	 */
	protected void updateToggleActionState() {
		final ICommandService commandService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		if (commandService != null) {
			final IPreferenceStore wsPreferenceStore = ((DiagramGraphicalViewer) getDiagramGraphicalViewer()).getWorkspaceViewerPreferenceStore();
			org.eclipse.core.commands.Command command = commandService.getCommand(CommandIds.VIEW_GRID_COMMAND);
			EclipseCommandUtils.updateToggleCommandState(command, wsPreferenceStore.getBoolean(WorkspaceViewerProperties.VIEWGRID));

			command = commandService.getCommand(CommandIds.VIEW_RULER_COMMAND);
			EclipseCommandUtils.updateToggleCommandState(command, wsPreferenceStore.getBoolean(WorkspaceViewerProperties.VIEWRULERS));

			command = commandService.getCommand(CommandIds.VIEW_PAGE_BREAK_COMMAND);
			EclipseCommandUtils.updateToggleCommandState(command, wsPreferenceStore.getBoolean(WorkspaceViewerProperties.VIEWPAGEBREAKS));

			command = commandService.getCommand(CommandIds.SNAP_TO_GRID_COMMAND);
			EclipseCommandUtils.updateToggleCommandState(command, wsPreferenceStore.getBoolean(WorkspaceViewerProperties.SNAPTOGRID));

		} else {
			throw new RuntimeException(String.format("The Eclipse service %s has not been found", ICommandService.class)); //$NON-NLS-1$
		}
	}


	@Override
	protected void addDefaultPreferences() {
		super.addDefaultPreferences();
		final PreferencesHint preferencesHint = getPreferencesHint();
		final IPreferenceStore globalPreferenceStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
		final Diagram diagram = getDiagram();
		final String diagramType = diagram.getType();
		// get the preferences
		final boolean viewGrid = globalPreferenceStore.getBoolean(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.VIEW_GRID));
		final boolean viewRuler = globalPreferenceStore.getBoolean(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.VIEW_RULER));
		final int rulerUnit = globalPreferenceStore.getInt(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.RULER_UNITS));
		final boolean snapToGrid = globalPreferenceStore.getBoolean(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.SNAP_TO_GRID));
		final boolean snapToGeometry = globalPreferenceStore.getBoolean(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.SNAP_TO_GEOMETRY));
		final RGB rgb = PreferenceConverter.getColor(globalPreferenceStore, PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.GRID_LINE_COLOR));
		final int gridLineColor = FigureUtilities.RGBToInteger(rgb);
		final double gridSpacing = globalPreferenceStore.getDouble(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.GRID_SPACING));
		final boolean gridOrder = globalPreferenceStore.getBoolean(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.GRID_ORDER));
		final int gridLineStyle = globalPreferenceStore.getInt(PreferencesConstantsHelper.getDiagramConstant(diagramType, PreferencesConstantsHelper.GRID_LINE_STYLE));

		// set the preferences
		final IPreferenceStore localStore = getWorkspaceViewerPreferenceStore();
		localStore.setValue(PreferencesConstantsHelper.VIEW_GRID_CONSTANT, NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.VIEW_GRID_CONSTANT, viewGrid));
		localStore.setValue(PreferencesConstantsHelper.VIEW_RULERS_CONSTANT, NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.VIEW_RULERS_CONSTANT, viewRuler));
		localStore.setValue(PreferencesConstantsHelper.RULER_UNITS_CONSTANT, NotationUtils.getIntValue(diagram, PreferencesConstantsHelper.RULER_UNITS_CONSTANT, rulerUnit));
		localStore.setValue(PreferencesConstantsHelper.SNAP_TO_GRID_CONSTANT, NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.SNAP_TO_GRID_CONSTANT, snapToGrid));
		localStore.setValue(PreferencesConstantsHelper.SNAP_TO_GEOMETRY_CONSTANT, NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.SNAP_TO_GEOMETRY_CONSTANT, snapToGeometry));
		localStore.setValue(PreferencesConstantsHelper.GRID_LINE_COLOR_CONSTANT, NotationUtils.getIntValue(diagram, PreferencesConstantsHelper.GRID_LINE_COLOR_CONSTANT, gridLineColor));
		localStore.setValue(PreferencesConstantsHelper.GRID_SPACING_CONSTANT, NotationUtils.getDoubleValue(diagram, PreferencesConstantsHelper.GRID_SPACING_CONSTANT, gridSpacing));

		// to force refresh
		localStore.setValue(PreferencesConstantsHelper.GRID_ORDER_CONSTANT, !NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.GRID_ORDER_CONSTANT, gridOrder));
		localStore.setValue(PreferencesConstantsHelper.GRID_ORDER_CONSTANT, NotationUtils.getBooleanValue(diagram, PreferencesConstantsHelper.GRID_ORDER_CONSTANT, gridOrder));

		localStore.setValue(PreferencesConstantsHelper.GRID_LINE_STYLE_CONSTANT, NotationUtils.getIntValue(diagram, PreferencesConstantsHelper.GRID_LINE_STYLE_CONSTANT, gridLineStyle));

	}

	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();

		// Engage synchronization (if required)
		EditPart diagram = getDiagramEditPart();
		if (diagram != null) {
			try {
				ISyncService syncService = ServiceUtilsForEditPart.getInstance().getService(ISyncService.class, diagram);
				syncService.evaluateTriggers(diagram.getViewer());
			} catch (ServiceException e) {
				Activator.log.error(e);
			}
		}
	}

	@Override
	public void doSetInput(IEditorInput input, boolean releaseEditorContents) throws CoreException {
		super.doSetInput(input, releaseEditorContents);
		if (getDiagram() != null && !DiagramVersioningUtils.isOfCurrentPapyrusVersion(getDiagram())) {
			new ReconcileHelper(getEditingDomain()).reconcileDiagram(getDiagram());
		}
	}
	
	protected class PalettePageWrapper implements PalettePage, IAdaptable {

		private final CustomPalettePage delegate;

		private boolean disposed;

		protected PalettePageWrapper(CustomPalettePage delegate) {
			this.delegate = delegate;
		}

		@Override
		public void createControl(Composite parent) {
			Control existing = getControl();
			if ((existing != null) && !existing.isDisposed()) {
				// Attempting to creating the page controls again? Bail
				return;
			}

			delegate.createControl(parent);

			delegate.getControl().addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent e) {
					disposed = true;

					SynchronizableGmfDiagramEditor.this.palettePages.remove(PalettePageWrapper.this);
				}
			});

			if (palettePageState != null) {
				// We're re-creating the palette page after having closed it, either for editor re-load
				// or the PapyrusPaletteSynchronizer forcing a palette refresh. Reinitialize from the
				// last saved state
				PaletteViewerReloadContextProvider.getInstance(getPaletteViewer()).restore(palettePageState);
				palettePageState = null;
			}
		}

		@Override
		public void dispose() {
			// Save current state for potential re-opening later
			saveState();
			delegate.dispose();
		}

		public boolean isDisposed() {
			return disposed;
		}

		void saveState() {
			PaletteViewer palette = getPaletteViewer();
			if (palette != null) {
				palettePageState = PaletteViewerReloadContextProvider.getInstance(palette).createReloadContext();
			}
		}

		@Override
		public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
			if (adapter == IReloadContextProvider.class) {
				return new IReloadContextProvider() {

					@Override
					public Object createReloadContext() {
						return (getPaletteViewer() != null) ? PaletteViewerReloadContextProvider.getInstance(getPaletteViewer()).createReloadContext() : null;
					}

					@Override
					public void restore(Object reloadContext) {
						if (getPaletteViewer() != null) {
							PaletteViewerReloadContextProvider.getInstance(getPaletteViewer()).restore(reloadContext);
						} else {
							// We'll defer this until the page control is created
							palettePageState = reloadContext;
						}
					}
				};
			}
			return delegate.getAdapter(adapter);
		}

		@Override
		public Control getControl() {
			// CustomPalettePage will NPE if asked for the control before the PaletteViewer is created
			return (delegate.getPaletteViewer() == null) ? null : delegate.getControl();
		}

		@Override
		public void setFocus() {
			delegate.setFocus();
		}

		@Override
		public void setActionBars(IActionBars actionBars) {
			delegate.setActionBars(actionBars);
		}

		@Override
		public void init(IPageSite pageSite) {
			delegate.init(pageSite);
		}

		@Override
		public IPageSite getSite() {
			return delegate.getSite();
		}

		public PaletteViewer getPaletteViewer() {
			return delegate.getPaletteViewer();
		}

	}

	/**
	 * A wrapper for the GMF-provided outline page that lets us clean up references to the model content leaked via the Outline View tool bar.
	 */
	private class OutlinePageWrapper implements IPageBookViewPage, IContentOutlinePage {
		private final ContentOutlinePage delegate;

		OutlinePageWrapper(ContentOutlinePage delegate) {
			this.delegate = delegate;
		}

		@Override
		public void createControl(Composite parent) {
			delegate.createControl(parent);
		}

		@Override
		public void dispose() {
			try {
				// Remove the toolbar items that reference me and, through me, the diagram and its associated semantic model
				IActionBars bars = getSite().getActionBars();
				bars.getToolBarManager().removeAll();
				bars.updateActionBars();
			} finally {
				delegate.dispose();
			}
		}

		@Override
		public Control getControl() {
			return delegate.getControl();
		}

		@Override
		public void setActionBars(IActionBars actionBars) {
			delegate.setActionBars(actionBars);
		}

		@Override
		public void setFocus() {
			delegate.setFocus();
		}

		@Override
		public void init(IPageSite site) throws PartInitException {
			delegate.init(site);
		}

		@Override
		public IPageSite getSite() {
			return delegate.getSite();
		}

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			delegate.addSelectionChangedListener(listener);
		}

		@Override
		public ISelection getSelection() {
			return delegate.getSelection();
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			delegate.removeSelectionChangedListener(listener);
		}

		@Override
		public void setSelection(ISelection selection) {
			delegate.setSelection(selection);
		}
	}
}
