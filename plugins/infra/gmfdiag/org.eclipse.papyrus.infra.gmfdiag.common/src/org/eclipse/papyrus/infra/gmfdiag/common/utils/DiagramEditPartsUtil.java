/*****************************************************************************
 * Copyright (c) 2012, 2016, 2017 CEA LIST, Christian W. Damus, and others.
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
 *  Christian W. Damus - bugs 433206, 473148, 485220
 *  Vincent Lorenzo - bug 492522
 *  Ansgar Radermacher - bug 527181
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.util.EditPartUtilities;
import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderItemEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.CanonicalStyle;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.papyrus.infra.core.resource.IReadOnlyHandler2;
import org.eclipse.papyrus.infra.core.resource.ReadOnlyAxis;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.appearance.helper.AppearanceHelper;
import org.eclipse.papyrus.infra.emf.readonly.ReadOnlyManager;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.ConnectionEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.editpart.PapyrusDiagramEditPart;
import org.eclipse.papyrus.infra.gmfdiag.common.helper.NotationHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.preferences.PreferencesConstantsHelper;
import org.eclipse.papyrus.infra.services.labelprovider.service.LabelProviderService;
import org.eclipse.papyrus.infra.ui.util.EditorHelper;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Different utility methods to manage and manipulate edit parts in diagrams.
 */
public class DiagramEditPartsUtil {

	protected DiagramEditPartsUtil() { // FIXME : protected instead of private for non regression purposes
										// should be removed as soon as org.eclipse.papyrus.uml.diagram.common.util.DiagramEditPartsUtil
										// is removed
		// to prevent instantiation
	}

	/**
	 * Returns the edit part that controls the given view.
	 *
	 * @param view
	 *            the view for which the edit part should be found. This should not be <code>null</code>
	 * @param anyEditPart
	 *            any edit part from which to get the edit part registry
	 *
	 * @return the edit part that controls the given view or <code>null</code> if none was found
	 */
	public static EditPart getEditPartFromView(View view, EditPart anyEditPart) {
		if (view != null && anyEditPart != null) {
			return (EditPart) anyEditPart.getViewer().getEditPartRegistry().get(view);
		}
		return null;
	}

	/**
	 * Gets the diagram edit part.
	 *
	 * @param editPart
	 *            the edit part
	 *
	 * @return the diagram edit part
	 */
	public static DiagramEditPart getDiagramEditPart(EditPart editPart) {
		if (editPart == null) {
			return null;
		}

		if (editPart instanceof IGraphicalEditPart) {
			IGraphicalEditPart graphicalEditPart = (IGraphicalEditPart) editPart;
			View view = graphicalEditPart.getNotationView();
			Diagram diagram = view.getDiagram();
			Object object = graphicalEditPart.getViewer().getEditPartRegistry().get(diagram);
			if (object instanceof DiagramEditPart) {
				return (DiagramEditPart) object;
			}
		}

		if (editPart instanceof DiagramEditPart) {
			return (DiagramEditPart) editPart;
		}

		EditPart actual = editPart;
		EditPart parent = null;
		while ((parent = actual.getParent()) != null) {
			if (parent instanceof DiagramEditPart) {
				return (DiagramEditPart) parent;
			} else {
				actual = parent;
			}
		}

		return null;
	}

	/**
	 *
	 * @param ep
	 *            an edit part
	 * @return
	 * 		all children edit part which are "top" semantic edit part
	 */
	public static Collection<EditPart> getAllTopSemanticEditPart(final EditPart ep) {
		final Collection<EditPart> editparts = new HashSet<EditPart>();
		for (final Object current : ep.getChildren()) {
			if (current instanceof EditPart) {
				editparts.addAll(getAllTopSemanticEditPart((EditPart) current));
				final EditPart topEP = getTopSemanticEditPart((EditPart) current);
				if (topEP != null) {
					editparts.add(topEP);
				}
			}
		}
		return editparts;
	}

	/**
	 *
	 * @param ep
	 *            an editpart
	 * @return
	 * 		the top edit part representing the same eobject or <code>null</code> if ep doesn't represent an editpart
	 */
	public static final EditPart getTopSemanticEditPart(final EditPart ep) {
		final EObject currentEObject = ep.getAdapter(EObject.class);
		if (currentEObject != null) {
			EditPart previousParent = ep;
			EditPart parent = ep;
			while (parent != null) {
				if (parent.getAdapter(EObject.class) != currentEObject || parent instanceof DiagramEditPart) {
					return previousParent;
				}
				previousParent = parent;
				parent = parent.getParent();
			}
			return previousParent;
		}
		return null;
	}


	/**
	 * A utility method to return the active <code>DiagramEditPart</code> if
	 * the current part implements <code>IDiagramWorkbenchPart</code>
	 *
	 * @return The current diagram if the parts implements <code>IDiagramWorkbenchPart</code>; <code>null</code> otherwise
	 */
	public static final IDiagramGraphicalViewer getActiveDiagramGraphicalViewer() {
		IDiagramWorkbenchPart part = getActiveDiagramWorkbenchPart();
		return part != null ? part.getDiagramGraphicalViewer() : null;
	}

	/**
	 * A utility method to return the active part if it implements
	 * or adapts to the <code>IDiagramWorkbenchPart</code> interface
	 *
	 * @return The current part if it implements or adapts to <code>IDiagramWorkbenchPart</code>; <code>null</code> otherwise
	 */
	public static final IDiagramWorkbenchPart getActiveDiagramWorkbenchPart() {
		IDiagramWorkbenchPart diagramPart = null;

		IWorkbenchPart part = EditorHelper.getActivePart();

		if (part instanceof IDiagramWorkbenchPart) {
			diagramPart = (IDiagramWorkbenchPart) part;

		} else if (part != null) {
			diagramPart = part.getAdapter(IDiagramWorkbenchPart.class);
		}

		return diagramPart;
	}

	/**
	 *
	 * @param anEditPart
	 *            an edit part
	 * @return
	 * 		the preference store for the diagram owning this edit part or <code>null</code> if not found
	 *
	 */
	public static final IPreferenceStore getDiagramWorkspacePreferenceStore(final EditPart anEditPart) {
		final EditPartViewer viewer = anEditPart.getViewer();
		if (viewer instanceof DiagramGraphicalViewer) {
			return ((DiagramGraphicalViewer) viewer).getWorkspaceViewerPreferenceStore();
		}
		return null;
	}

	/**
	 *
	 * @param anEditPart
	 *            an edit part
	 * @return
	 *         <code>true</code> if snap to grid is activated for the diagram owning the editpart
	 *
	 */
	public static final boolean isSnapToGridActive(final EditPart anEditPart) {
		boolean result = false;
		final IPreferenceStore store = getDiagramWorkspacePreferenceStore(anEditPart);
		if (store != null) {
			result = store.getBoolean(PreferencesConstantsHelper.SNAP_TO_GRID_CONSTANT);
		}
		return result;
	}

	/**
	 *
	 * @param anEditPart
	 *            an edit part
	 * @return
	 * 		the value of the grid spacing or -1 if not found
	 */
	public static final double getDiagramGridSpacing(final EditPart anEditPart) {
		final RootEditPart rootEP = anEditPart.getRoot();
		if (rootEP instanceof DiagramRootEditPart) {
			return ((DiagramRootEditPart) rootEP).getGridSpacing();
		}
		return -1.0;
	}

	/**
	 * This Method return the Graphical container of an EditPart.
	 * Depending on the type of EditPart, the container can be the Direct Parent or the grand parent.
	 * 
	 * @param currentEP
	 * @return
	 */
	public static final EditPart getContainerEditPart(GraphicalEditPart currentEP) {

		EditPart container;
		EditPart parent = currentEP.getParent();
		if (parent instanceof AbstractConnectionEditPart) {
			container = parent.getParent();
		} else if (parent instanceof AbstractBorderItemEditPart) {
			container = parent.getParent().getParent();
		} else if (currentEP instanceof AbstractBorderItemEditPart) {
			container = parent.getParent();
		} else {
			container = parent;
		}

		return container;
	}

	/*
	 * @param anEditPart
	 * an edit part
	 * 
	 * @return
	 * the zoom level in the diagram or 1.0 when {@link ZoomManager} has not been found
	 */

	public static final double getDiagramZoomLevel(final EditPart anEditPart) {

		final RootEditPart rootEP = anEditPart.getRoot();
		if (rootEP instanceof DiagramRootEditPart) {
			final ZoomManager zoomManager = ((DiagramRootEditPart) rootEP).getZoomManager();
			if (zoomManager != null) {
				return zoomManager.getZoom();
			}
		}
		return 1.0;
	}

	/** The Constant BelongToDiagramSource. */
	// @unused
	public static final String BelongToDiagramSource = "es.cv.gvcase.mdt.uml2.diagram.common.Belongs_To_This_Diagram";

	/** EAnnotation Source for diagrams that grow from this a view. */
	// @unused
	public static final String DiagramsRelatedToElement = "es.cv.gvcase.mdt.uml2.diagram.common.DiagramsRelatedToElement";

	/**
	 * Gets a list of all GraphicalEditParts in the diagram of the given
	 * EditPart.
	 *
	 * @param editPart
	 *            Any <code>EditPart</code> in the diagram. The TopGraphicalNode
	 *            will be found from this.
	 *
	 * @return a list containing all <code>GraphicalEditPart</code> in the
	 *         diagram.
	 */
	// @unused
	public static List<IGraphicalEditPart> getAllEditParts(EditPart editPart) {

		if (editPart == null) {
			return null;
		}

		EditPart topEditPart = getTopMostEditPart(editPart);

		List<IGraphicalEditPart> editParts = new ArrayList<IGraphicalEditPart>();

		if (editPart instanceof IGraphicalEditPart) {
			editParts.add((IGraphicalEditPart) editPart);
		}
		addEditPartGraphicalChildren(editPart, editParts);

		return editParts;
	}

	/**
	 * Returns the upper most EditPart in the hierarchy of the given EditPart.
	 *
	 * @param editPart
	 *            A non-null EditPart
	 *
	 * @return The upper most EditPart in the hierarchy; may be the same
	 *         EditPart
	 */
	public static EditPart getTopMostEditPart(EditPart editPart) {

		if (editPart == null) {
			return null;
		}

		EditPart actual, parent;

		actual = editPart;

		while ((parent = actual.getParent()) != null) {
			actual = parent;
		}

		return actual;
	}

	/**
	 * Handle notification for diagram.
	 *
	 * @param editPart
	 *            the edit part
	 * @param notification
	 *            the notification
	 * @param features
	 *            the features
	 */
	// @unused
	public static void handleNotificationForDiagram(IGraphicalEditPart editPart, Notification notification, List<EStructuralFeature> features) {
		EObject element = editPart.resolveSemanticElement();
		Object notifier = notification.getNotifier();
		Object feature = notification.getFeature();
		Object oldValue = notification.getOldValue();
		Object newValue = notification.getNewValue();
		if (notifier != null && notifier == element) {
			if (feature != null && oldValue != null && oldValue != newValue && features.contains(feature)) {
				DiagramEditPartsUtil.updateDiagram(editPart);
			}

		}
	}

	/**
	 * Handle notification for view.
	 *
	 * @param editPart
	 *            the edit part
	 * @param notification
	 *            the notification
	 * @param features
	 *            the features
	 */
	// @unused
	public static void handleNotificationForView(IGraphicalEditPart editPart, Notification notification, List<EStructuralFeature> features) {
		EObject element = editPart.resolveSemanticElement();
		Object notifier = notification.getNotifier();
		Object feature = notification.getFeature();
		Object oldValue = notification.getOldValue();
		Object newValue = notification.getNewValue();
		if (notifier != null && notifier == element) {
			if (feature != null && oldValue != null && oldValue != newValue && features.contains(feature)) {
				DiagramEditPartsUtil.updateEditPart(editPart);
			}

		}
	}

	/**
	 * Update a <View>.
	 *
	 * @param view
	 *            the view
	 */
	// @unused
	public static void updateDiagram(View view) {
		if (view == null) {
			return;
		}
		view = view.getDiagram();
		if (view == null) {
			return;
		}
		EObject element = view.getElement();
		if (element == null) {
			return;
		}

		List editPolicies = CanonicalEditPolicy.getRegisteredEditPolicies(element);
		for (Iterator it = editPolicies.iterator(); it.hasNext();) {
			CanonicalEditPolicy nextEditPolicy = (CanonicalEditPolicy) it.next();
			nextEditPolicy.refresh();
		}
	}

	/**
	 * Update diagram.
	 *
	 * @param editPart
	 *            any edit part in the <Diagram>
	 */
	public static void updateDiagram(IGraphicalEditPart editPart) {
		if (editPart == null) {
			return;
		}
		View view = editPart.getNotationView();
		if (view == null) {
			return;
		}
		view = view.getDiagram();
		if (view == null) {
			return;
		}
		EObject element = view.getElement();
		if (element == null) {
			return;
		}

		List editPolicies = CanonicalEditPolicy.getRegisteredEditPolicies(element);
		for (Iterator it = editPolicies.iterator(); it.hasNext();) {
			CanonicalEditPolicy nextEditPolicy = (CanonicalEditPolicy) it.next();
			nextEditPolicy.refresh();
		}
	}

	/**
	 * Update EditPart.
	 *
	 * @param editPart
	 *            the edit part
	 */
	public static void updateEditPart(IGraphicalEditPart editPart) {
		if (editPart == null) {
			return;
		}
		View view = editPart.getNotationView();
		if (view == null) {
			return;
		}
		EObject element = view.getElement();
		if (element == null) {
			return;
		}

		List editPolicies = CanonicalEditPolicy.getRegisteredEditPolicies(element);
		for (Iterator it = editPolicies.iterator(); it.hasNext();) {
			CanonicalEditPolicy nextEditPolicy = (CanonicalEditPolicy) it.next();
			nextEditPolicy.refresh();
		}
	}

	/**
	 * Update EditPart and all contained EditPart that share the same type of
	 * model element.
	 *
	 * @param editPart
	 *            the edit part
	 * @param eClass
	 *            the e class
	 */
	public static void updateEditPartAndChildren(IGraphicalEditPart editPart, EClass eClass) {
		if (editPart == null) {
			return;
		}
		View view = editPart.getNotationView();
		if (view == null) {
			return;
		}

		for (Object child : editPart.getChildren()) {
			if (child instanceof IGraphicalEditPart) {
				updateEditPartAndChildren(((IGraphicalEditPart) child), eClass);
			}
		}

		EObject element = view.getElement();
		if (eClass != null && eClass.isInstance(element)) {
			List editPolicies = CanonicalEditPolicy.getRegisteredEditPolicies(element);
			for (Iterator it = editPolicies.iterator(); it.hasNext();) {
				CanonicalEditPolicy nextEditPolicy = (CanonicalEditPolicy) it.next();
				nextEditPolicy.refresh();
			}
		}
	}

	/**
	 * Adds the edit part graphical children.
	 *
	 * @param editPart
	 *            the edit part
	 * @param list
	 *            the list
	 */
	private static void addEditPartGraphicalChildren(EditPart editPart, List<IGraphicalEditPart> list) {

		if (editPart == null) {
			return;
		}

		List<EditPart> children = editPart.getChildren();

		for (EditPart ep : children) {
			if (ep instanceof IGraphicalEditPart) {
				list.add((IGraphicalEditPart) ep);
			}
			addEditPartGraphicalChildren(ep, list);
		}
	}

	// Code extracted from getViewReferers in CanonicalEditPolicy
	/**
	 * Gets the e object views.
	 *
	 * @param element
	 *            the element
	 *
	 * @return the e object views
	 */
	public static List<View> getEObjectViews(EObject element) {
		List<View> views = new ArrayList<View>();
		if (element != null) {
			EReference[] features = { NotationPackage.eINSTANCE.getView_Element() };

			// These can only be views according to the eReference
			@SuppressWarnings("unchecked")
			Collection<? extends View> referencingViews = EMFCoreUtil.getReferencers(element, features);
			views.addAll(referencingViews);
		}
		return views;
	}

	/**
	 * Find the views associated with the given eObject in the viewer
	 *
	 * @param parserElement
	 *            the
	 * @param viewer
	 *            the viewer
	 * @return views found if any
	 */
	public static List<View> findViews(EObject parserElement, EditPartViewer viewer) {
		List<View> modelElements = new ArrayList<View>();
		if (parserElement != null) {
			for (Object ep : viewer.getEditPartRegistry().keySet()) {
				if (ep instanceof View) {
					View view = (View) ep;
					if (parserElement.equals(view.getElement())) {
						modelElements.add(view);
					}
				}
			}
		}
		return modelElements;
	}

	/**
	 * Finds all of the {@link EditPart}s in currently open editors that present a given notation {@code view}.
	 * 
	 * @param view
	 *            a view that may be presented by zero or more open diagram editors
	 * @return all edit parts in all diagrams that currently present the {@code view} (so could be empty)
	 */
	public static Iterable<EditPart> findEditParts(View view) {
		List<EditPart> result;

		Diagram diagram = view.getDiagram();
		if (diagram == null) {
			result = Collections.emptyList();
		} else {
			result = Lists.newArrayListWithExpectedSize(1);
			for (Iterator<EditPart> iter = getAllContents(PapyrusDiagramEditPart.getDiagramEditPartsFor(diagram)); iter.hasNext();) {
				EditPart next = iter.next();
				if (next.getModel() == view) {
					result.add(next);
				}
			}
		}

		return result;
	}

	/**
	 * Finds the <EditPart>s for the <EObject>s in the selection.
	 *
	 * @param selection
	 *            the selection
	 * @param viewer
	 *            the viewer
	 *
	 * @return the edits the parts from selection
	 */
	// @unused
	public static List<EditPart> getEditPartsFromSelection(ISelection selection, IDiagramGraphicalViewer viewer) {
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			// look for Views of the EObjects in the selection
			List<View> views = new ArrayList<View>();
			for (Object o : structuredSelection.toList()) {
				if (o instanceof EObject) {
					List referencerViews = getEObjectViews((EObject) o);
					for (Object ro : referencerViews) {
						if (ro instanceof View) {
							views.add((View) ro);
						}
					}
				}
			}
			if (!views.isEmpty()) {
				List<EditPart> editParts = new ArrayList<EditPart>();
				for (View view : views) {
					Object ep = viewer.getEditPartRegistry().get(view);
					if (ep instanceof EditPart) {
						editParts.add((EditPart) ep);
					}
				}
				if (!editParts.isEmpty()) {
					return editParts;
				}
			}
		}
		return Collections.EMPTY_LIST;
	}

	// Code extracted from PackageCanonicalEditPolicy

	// *****************************************//

	// ********************************************//

	/**
	 * Find diagram from plugin.
	 *
	 * @param plugin
	 *            the plugin
	 *
	 * @return the diagram
	 */
	public static Diagram findDiagramFromPlugin(AbstractUIPlugin plugin) {
		IEditorPart editor = plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor instanceof DiagramEditor) {
			return ((DiagramEditor) editor).getDiagram();
		}

		return null;
	}

	/**
	 * Find diagram from edit part.
	 *
	 * @param editPart
	 *            the edit part
	 *
	 * @return the diagram
	 */
	public static Diagram findDiagramFromEditPart(EditPart editPart) {
		Object object = editPart.getModel();

		if (object instanceof View) {
			return ((View) object).getDiagram();
		}

		return null;
	}

	// **//

	/**
	 * Refresh i text aware edit parts.
	 *
	 * @param editPart
	 *            the edit part
	 */
	public static void refreshITextAwareEditParts(EditPart editPart) {

		for (Object obj : editPart.getChildren()) {
			if (obj instanceof EditPart) {
				refreshITextAwareEditParts((EditPart) obj);
			}
		}

		if (editPart instanceof ITextAwareEditPart) {
			editPart.refresh();
		}
	}

	/**
	 * Return the main edipart which correspond to the {@link EObject} passed in argument
	 *
	 * @param eObject
	 * @param rootEditPart
	 *            {@link IGraphicalEditPart} root from which the search will start
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static IGraphicalEditPart getChildByEObject(final EObject eObject, IGraphicalEditPart rootEditPart, boolean isEdge) {
		if (eObject != null && rootEditPart != null) {

			try {
				Predicate<EditPart> predicate = new Predicate<EditPart>() {

					@Override
					public boolean apply(EditPart input) {
						if (input instanceof IGraphicalEditPart) {
							IGraphicalEditPart current = (IGraphicalEditPart) input;
							// Same EObject
							if (eObject.equals(current.resolveSemanticElement())) {
								EditPart parent = current.getParent();
								if (parent instanceof IGraphicalEditPart) {
									// its parent do not have the same EObject
									if (!eObject.equals(((IGraphicalEditPart) parent).resolveSemanticElement())) {
										return true;
									}
								} else if (parent instanceof RootEditPart) {
									return true;
								}
							}
						}
						return false;
					}
				};

				EditPart find = (isEdge) ? Iterables.find((Iterable<EditPart>) EditPartUtilities.getAllNestedConnectionEditParts(rootEditPart), predicate) : Iterables.find((Iterable<EditPart>) EditPartUtilities.getAllChildren(rootEditPart), predicate);
				return (IGraphicalEditPart) find;
			} catch (NoSuchElementException e) {
				// Nothing to do
			}

		}
		return null;
	}

	/**
	 * Return the main edipart which correspond to the {@link EObject} passed in argument
	 *
	 * @param eObject
	 * @param rootEditPart
	 *            {@link IGraphicalEditPart} root from which the search will start
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Iterable<IGraphicalEditPart> getChildrenByEObject(final EObject eObject, IGraphicalEditPart rootEditPart, boolean isEdge) {
		if (eObject != null && rootEditPart != null) {

			try {
				Predicate<EditPart> predicate = new Predicate<EditPart>() {

					@Override
					public boolean apply(EditPart input) {
						if (input instanceof IGraphicalEditPart) {
							IGraphicalEditPart current = (IGraphicalEditPart) input;
							// Same EObject
							if (eObject.equals(current.resolveSemanticElement())) {
								EditPart parent = current.getParent();
								if (parent instanceof IGraphicalEditPart) {
									// its parent do not have the same EObject
									if (!eObject.equals(((IGraphicalEditPart) parent).resolveSemanticElement())) {
										return true;
									}
								} else if (parent instanceof RootEditPart) {
									return true;
								}
							}
						}
						return false;
					}
				};

				Iterable<EditPart> find = (isEdge) ? Iterables.filter((Iterable<EditPart>) EditPartUtilities.getAllNestedConnectionEditParts(rootEditPart), predicate) : Iterables.filter((Iterable<EditPart>) EditPartUtilities.getAllChildren(rootEditPart),
						predicate);
				return Iterables.transform(find, new Function<EditPart, IGraphicalEditPart>() {

					@Override
					public IGraphicalEditPart apply(EditPart from) {
						if (from instanceof IGraphicalEditPart) {
							return (IGraphicalEditPart) from;
						}
						return null;
					}
				});
			} catch (NoSuchElementException e) {
				return Collections.EMPTY_LIST;
			}

		}
		return Collections.EMPTY_LIST;
	}

	public static TreeIterator<EditPart> getAllContents(EditPart editPart, boolean includeRoot) {
		return internalGetAllContents(editPart, includeRoot);
	}

	public static TreeIterator<EditPart> getAllContents(Iterable<? extends EditPart> editParts) {
		return internalGetAllContents(editParts, false);
	}

	private static final TreeIterator<EditPart> internalGetAllContents(final Object root, boolean includeRoot) {
		return new AbstractTreeIterator<EditPart>(root, includeRoot) {
			private static final long serialVersionUID = 1L;

			// Let's do this instanceof check only once. And take a defensive copy, of course
			@SuppressWarnings("unchecked")
			private final Iterable<EditPart> rootCollection = root instanceof Iterable<?> ? (Iterable<EditPart>) root : null;

			@Override
			@SuppressWarnings("unchecked")
			protected Iterator<? extends EditPart> getChildren(Object object) {
				Iterator<? extends EditPart> result;

				if (object == rootCollection) {
					// Defensive copy
					result = ImmutableList.copyOf(rootCollection).iterator();
				} else {
					// Defensive copy
					ImmutableList.Builder<EditPart> copy = ImmutableList.builder();
					EditPart editPart = (EditPart) object;
					copy.addAll(editPart.getChildren());
					if (editPart instanceof DiagramEditPart) {
						// The view's edit-part registry is required to get connections
						if (editPart.getViewer() != null) {
							copy.addAll(((DiagramEditPart) editPart).getConnections());
						}
					}
					result = copy.build().iterator();
				}

				return result;
			}
		};
	}


	/**
	 * Queries whether an {@code editPart} has canonical synchronization enabled.
	 * 
	 * @param editPart
	 *            an edit part
	 * @return whether it has canonical synchronization enabled
	 */
	public static boolean isCanonical(EditPart editPart) {
		boolean result = false;

		if (editPart instanceof IGraphicalEditPart) {
			View view = ((IGraphicalEditPart) editPart).getNotationView();
			if (view != null) {
				CanonicalStyle style = (CanonicalStyle) view.getStyle(NotationPackage.Literals.CANONICAL_STYLE);
				if (style != null) {
					result = style.isCanonical();
				}
			}
		}

		return result;
	}

	/**
	 * Checks if is semantic deletion.
	 *
	 * @param editPart
	 *            the edit part
	 * @return true, if is semantic deletion
	 */
	public static boolean isSemanticDeletion(IGraphicalEditPart editPart) {
		boolean isSemanticDeletion = false;
		TransactionalEditingDomain editingDomain = null;

		// Get Editing Domain
		try {
			editingDomain = ServiceUtilsForEditPart.getInstance().getTransactionalEditingDomain(editPart);
		} catch (ServiceException e) {

		}

		if (editingDomain != null) {

			IReadOnlyHandler2 readOnly = ReadOnlyManager.getReadOnlyHandler(editingDomain);
			EObject semantic = EMFHelper.getEObject(editPart);
			View graphical = NotationHelper.findView(editPart);

			isSemanticDeletion = !(semantic == null || semantic == graphical || semantic.eContainer() == null);
			// add a test to fix for bug 492522
			if (!isSemanticDeletion) {
				if (editPart instanceof ConnectionEditPart) {
					isSemanticDeletion = ((ConnectionEditPart) editPart).isSemanticConnection();
				}
			}

			if (isSemanticDeletion && readOnly != null) {
				// Is the semantic element read-only?
				Optional<Boolean> result = readOnly.isReadOnly(ReadOnlyAxis.anyAxis(), semantic);
				if (!result.or(false) && (graphical != null)) {
					// Or, if not, is the graphical element read-only?
					result = readOnly.isReadOnly(ReadOnlyAxis.anyAxis(), graphical);
				}

				// Are both the semantic and graphical elements writable?
				isSemanticDeletion = !result.or(false);
			}
		}


		return isSemanticDeletion;
	}

	/**
	 * Checks if this is a read only element from the edit part.
	 *
	 * @param editPart
	 *            the edit part
	 * @return true, if this is a read only element.
	 */
	public static boolean isReadOnly(final IGraphicalEditPart editPart) {
		boolean isReadOnly = true;
		TransactionalEditingDomain editingDomain = null;

		// Get Editing Domain
		try {
			editingDomain = ServiceUtilsForEditPart.getInstance().getTransactionalEditingDomain(editPart);
		} catch (ServiceException e) {
			// Do nothing
		}

		if (null != editingDomain) {

			final IReadOnlyHandler2 readOnly = ReadOnlyManager.getReadOnlyHandler(editingDomain);
			final EObject semantic = EMFHelper.getEObject(editPart);
			final View graphical = NotationHelper.findView(editPart);

			if (null != readOnly && null != semantic) {
				// Is the semantic element read-only?
				Optional<Boolean> result = readOnly.isReadOnly(ReadOnlyAxis.anyAxis(), semantic);
				isReadOnly = result.get();

				if (!isReadOnly && (graphical != null)) {
					// Or, if not, is the graphical element read-only?
					result = readOnly.isReadOnly(ReadOnlyAxis.anyAxis(), graphical);
					isReadOnly = result.get();
				}
			}
		}

		return isReadOnly;
	}

	/**
	 * @since 3.2.0
	 * @deprecated has been replaced by labelProviderMap
	 */
	@Deprecated ILabelProvider labelProvider;

	/**
	 * @since 3.2.0
	 * Use hash map to cache label provider references. The "weak" assures that resources no longer
	 * in use are freed
	 */
	protected static Map<Resource, ILabelProvider> labelProviderMap = new WeakHashMap<Resource, ILabelProvider>();

	/**
	 * Return the icon of a label, taking element type definitions into account
	 *
	 * @param parserElement the parserElement, typically the (model) element of an edit part
	 * @param viewer the edit part viewer
	 * @return the icon element
	 * @since 3.0
	 */
	public static Image getIcon(EObject parserElement, EditPartViewer viewer) {
		if (parserElement == null) {
			return null;
		}
		List<View> views = DiagramEditPartsUtil.findViews(parserElement, viewer);
		for (View view : views) {
			if (AppearanceHelper.showElementIcon(view)) {
				Resource rs = parserElement.eResource();
				ILabelProvider labelProvider = labelProviderMap.get(rs); 
				if (labelProvider == null) {
					try {
						LabelProviderService provider = ServiceUtilsForEObject.getInstance().getService(LabelProviderService.class, parserElement);
						labelProvider = provider.getLabelProvider();
						labelProviderMap.put(rs, labelProvider);
					} catch (ServiceException ex) {
						Activator.log.error(ex);
					}
				}

				return labelProvider.getImage(parserElement);
			}
		}
		return null;
	}

	/**
	 * Gets the diagram on the given {@code context} matching a particular
	 * {@code filter} condition.
	 *
	 * @param context
	 *            a diagram context (its {@link View#getElement() element})
	 * @param filter
	 *            the diagram selection criterion, or {@code null} to get any diagram
	 *
	 * @return the matching diagram, or {@code null} if none
	 * @since 3.100
	 */
	public static Diagram getDiagram(EObject context, java.util.function.Predicate<? super Diagram> filter) {
		if (filter == null) {
			filter = __ -> true;
		}

		return EMFHelper.getUsages(context).stream()
				.filter(s -> s.getEObject() instanceof Diagram)
				.filter(s -> s.getEStructuralFeature() == NotationPackage.Literals.VIEW__ELEMENT)
				.map(s -> s.getEObject())
				// Not in an undone command's change description
				.filter(o -> o.eResource() != null)
				.map(Diagram.class::cast)
				.filter(filter)
				.findAny().orElse(null);
	}
}
