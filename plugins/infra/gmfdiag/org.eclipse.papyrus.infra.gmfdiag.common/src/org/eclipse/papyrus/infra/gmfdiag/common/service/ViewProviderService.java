/*****************************************************************************
 * Copyright (c) 2018 EclipseSource and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - Initial API and implementation (Bug 533701)
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service;

import org.eclipse.gmf.runtime.diagram.core.providers.IViewProvider;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.core.services.IServiceFactory;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * <p>
 * A service used to increase the control diagrams have over the {@link IViewProvider}
 * that should be enabled in that diagram. All generic Papyrus {@link IViewProvider} are expected to use this
 * service before deciding if they should indeed be enabled.
 * </p>
 * <p>
 * Note that this service is here to provide an additional pre-check to determine if a
 * provider is allowed to work in a specific diagram. However, this service knows nothing
 * about the internal requirements/dependencies of a specific provider, and should not
 * <strong>replace</strong> the {@link IViewProvider#provides(org.eclipse.gmf.runtime.common.core.service.IOperation)}
 * method (i.e. the actual provider is still expected to run its own checks, in addition
 * to calling this service).
 * </p>
 *
 * @since 3.100
 */
public interface ViewProviderService {
	/**
	 * Tests whether the given provider is allowed in the context of that view
	 *
	 * @param provider
	 * @param view
	 * @return
	 */
	boolean isEnabled(IViewProvider provider, View view);



	public static class Factory implements IServiceFactory {

		@Override
		public void init(ServicesRegistry servicesRegistry) throws ServiceException {
			// Nothing
		}

		@Override
		public void startService() throws ServiceException {
			// Nothing
		}

		@Override
		public void disposeService() throws ServiceException {
			// Nothing
		}

		@Override
		public ViewProviderService createServiceInstance() throws ServiceException {
			BundleContext bundleContext = FrameworkUtil.getBundle(Factory.class).getBundleContext();
			ServiceReference<ViewProviderService> serviceReference = bundleContext.getServiceReference(ViewProviderService.class);
			return bundleContext.getService(serviceReference);
		}

	}
}
