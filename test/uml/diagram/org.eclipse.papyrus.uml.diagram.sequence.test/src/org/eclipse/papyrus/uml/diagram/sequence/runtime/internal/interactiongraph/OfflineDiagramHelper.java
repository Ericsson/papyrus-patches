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
package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.ui.OffscreenEditPartFactory;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.papyrus.infra.core.editor.ModelSetServiceFactory;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class OfflineDiagramHelper {
	public static boolean init(ResourceSet set) {
		try {
			new Realm() {
							/* (non-Javadoc)
				 * @see org.eclipse.core.databinding.observable.Realm#isCurrent()
				 */
				@Override
				public boolean isCurrent() {
					return false;
				}
				
				public void setDefault() {
					Realm.setDefault(DisplayRealm.getRealm(Display.getDefault()));
				}
			}.setDefault();

			if (servicesRegistry == null)
				servicesRegistry = new ExtensionServicesRegistry(
					org.eclipse.papyrus.infra.core.Activator.PLUGIN_ID);
			ModelSetServiceFactory.setServiceRegistry(set, servicesRegistry);
			try {
				servicesRegistry.startRegistry();
			} catch (Exception e) {
				try {
					servicesRegistry.startRegistry();					
				} catch (Exception e1) {
					return false;
				}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public static EditPartViewer loadDiagram(Diagram diagram, Shell shell, PreferencesHint preferencesHint) {
		EditPartViewer[] viewer = new EditPartViewer[] {null};
		if (Realm.getDefault() == null) {
			DisplayRealm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {				
				@Override
				public void run() {
					viewer[0] = renderDiagram(diagram, shell, preferencesHint);
				}
			});			
		} else {
			viewer[0] = renderDiagram(diagram, shell, preferencesHint);
		}
		return viewer[0];
	}
	
	private static EditPartViewer renderDiagram(final Diagram diagram, final Shell shell, final PreferencesHint preferencesHint) {
		DiagramEditPart dep = OffscreenEditPartFactory.getInstance().createDiagramEditPart(diagram, shell, preferencesHint);
		return dep.getViewer();		
	}
	
	private static ExtensionServicesRegistry servicesRegistry;
}