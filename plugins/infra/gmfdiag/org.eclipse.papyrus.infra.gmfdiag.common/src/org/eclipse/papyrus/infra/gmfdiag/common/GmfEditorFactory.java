/*****************************************************************************
 * Copyright (c) 2008, 2016 LIFL, CEA LIST, Christian W. Damus, and others.
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
 *  Christian W. Damus (CEA) - service hook for integrating tools into graphical editor (CDO)
 *  Christian W. Damus (CEA) - bug 392301
 *  Christian W. Damus - bug 474467
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common;

import java.lang.reflect.Constructor;

import org.eclipse.gef.ui.parts.GraphicalEditor;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.papyrus.infra.core.editor.BackboneException;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.AbstractPageModel;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IEditorModel;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IPageModel;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.messages.Messages;
import org.eclipse.papyrus.infra.gmfdiag.common.providers.NotationLabelProvider;
import org.eclipse.papyrus.infra.services.labelprovider.service.LabelProviderService;
import org.eclipse.papyrus.infra.ui.extension.diagrameditor.AbstractEditorFactory;
import org.eclipse.papyrus.infra.ui.multidiagram.actionbarcontributor.ActionBarContributorRegistry;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * Base class of GmfEditor factories. Editor should subclass this class and provide a 0 args
 * constructor initializing the super class.
 *
 * @author Cedric Dumoulin
 * @author Remi Schnekenburger
 * @author Patrick Tessier
 */
public class GmfEditorFactory extends AbstractEditorFactory {

	/**
	 * Creates a new GmfEditorFactory.
	 *
	 * @param diagramClass
	 *            expected Class of the diagram to create.
	 * @param expectedType
	 *            expected diagram type (@see {@link Diagram#getType()})
	 */
	protected GmfEditorFactory(Class<?> diagramClass, String expectedType) {
		super(diagramClass, expectedType);
	}

	/**
	 * Return true if this PageModelFactory can create a PageModel for the specified pageIdentifier.
	 * The pageIdentifier is an instance of Diagram.
	 *
	 * @see org.eclipse.papyrus.infra.ui.extension.diagrameditor.IPluggableEditorFactory#isPageModelFactoryFor(java.lang.Object)
	 * @param pageIdentifier
	 * @return
	 *
	 */
	@Override
	public boolean isPageModelFactoryFor(Object pageIdentifier) {

		if (pageIdentifier instanceof Diagram) {
			Diagram diagram = (Diagram) pageIdentifier;
			// disable it when diagram is a proxy (dedicated factory will handle it)
			if (!diagram.eIsProxy()) {
				final String type = diagram.getType();
				return getExpectedType().equals(type);
			}
		}
		// no
		return false;

	}

	@Override
	public IPageModel createIPageModel(Object pageIdentifier) {
		ServicesRegistry services = getServiceRegistry();
		ILabelProvider labels = ServiceUtils.getInstance().tryService(services, LabelProviderService.class)
				.map(lps -> lps.getLabelProvider(pageIdentifier))
				.orElseGet(NotationLabelProvider::new);

		return new GMFEditorModel((Diagram) pageIdentifier, services, labels);
	}

	/**
	 * IEditorModel handling creation of the requested Editor.
	 *
	 * @author dumoulin
	 *
	 */
	class GMFEditorModel extends AbstractPageModel implements IEditorModel {

		/**
		 * The Diagram object describing the diagram.
		 */
		private Diagram diagram;

		/**
		 * The servicesRegistry provided at creation.
		 */
		private ServicesRegistry servicesRegistry;

		/**
		 *
		 * Constructor.
		 */
		public GMFEditorModel(Diagram pageIdentifier, ServicesRegistry servicesRegistry, ILabelProvider labels) {
			super(labels);

			diagram = pageIdentifier;
			this.servicesRegistry = servicesRegistry;
		}

		/**
		 * Create the IEditor for the diagram.
		 *
		 * @see org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IEditorModel#createIEditorPart()
		 * @return
		 * @throws PartInitException
		 *
		 */
		@Override
		public IEditorPart createIEditorPart() throws PartInitException {
			GraphicalEditor editor;
			try {
				Constructor<?> c = getDiagramClass().getConstructor(ServicesRegistry.class, Diagram.class);
				editor = (GraphicalEditor) c.newInstance(servicesRegistry, diagram);

				IGraphicalEditorSupport editorSupport = servicesRegistry.getService(IGraphicalEditorSupport.class);
				editorSupport.initialize(editor);

				return editor;

			} catch (Exception e) {
				// Lets propagate. This is an implementation problem that should be solved by
				// programmer.
				throw new PartInitException(Messages.GmfEditorFactory_ErrorCreatingEditorPart + diagram, e);
			}

		}

		/**
		 * Get the action bar requested by the Editor.
		 *
		 * @see org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IEditorModel#getActionBarContributor()
		 * @return
		 *
		 */
		@Override
		public EditorActionBarContributor getActionBarContributor() {

			String actionBarId = editorDescriptor.getActionBarContributorId();

			// Do nothing if no EditorActionBarContributor is specify.
			if (actionBarId == null || actionBarId.length() == 0) {
				return null;
			}

			// Try to get it.

			// Get ServiceRegistry
			ActionBarContributorRegistry registry;
			try {
				registry = servicesRegistry.getService(ActionBarContributorRegistry.class);
			} catch (ServiceException e) {
				Activator.log.error(e);
				return null;
			}

			try {
				return registry.getActionBarContributor(actionBarId);
			} catch (BackboneException e) {
				Activator.log.error(e);
				return null;
			}
		}

		/**
		 * Get the underlying RawModel. Return the Diagram.
		 *
		 * @see org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IPageModel#getRawModel()
		 * @return
		 *
		 */
		@Override
		public Object getRawModel() {
			return diagram;
		}
	}

}
