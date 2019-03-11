/*****************************************************************************
 * Copyright (c) 2017 CEA LIST, ALL4TEC and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mickaël ADAM (ALL4TEC) mickael.adam@all4tec.net - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.service.palette;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProvider;
import org.eclipse.gmf.runtime.common.core.service.ProviderPriority;
import org.eclipse.gmf.runtime.common.ui.util.ActivityUtil;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.palette.ContributeToPaletteOperation;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditorWithFlyOutPalette;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.architecture.ArchitectureDescriptionUtils;
import org.eclipse.papyrus.infra.architecture.ArchitectureDomainManager;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ExtendedProviderDescriptor;
import org.eclipse.papyrus.infra.gmfdiag.paletteconfiguration.PaletteConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.representation.PapyrusDiagram;
import org.eclipse.papyrus.infra.gmfdiag.style.PapyrusDiagramStyle;
import org.eclipse.ui.IEditorPart;
import org.osgi.framework.Bundle;

/**
 * ProviderDescriptor for palette model defined in Architecture models.
 */
public class ArchitectureExtendedProviderDescriptor extends ExtendedProviderDescriptor {

	/**
	 * Suffix for name for palette defined in Architecture model.
	 */
	public static final String ARCHITECTURE_PALETTE_NAME_SUFFIX = " Palette";//$NON-NLS-1$

	/**
	 * Suffix for id for palette defined in Architecture model.
	 */
	public static final String ARCHITECTURE_PALETTE_ID_SUFFIX = "_Palette";//$NON-NLS-1$

	/** The PapyrusDiagram target */
	private PapyrusDiagram diagram;

	/**
	 * Constructor.
	 * 
	 * @param diagram
	 *            the PapyrusDiagram target
	 */
	public ArchitectureExtendedProviderDescriptor(final PapyrusDiagram diagram) {
		super(null);
		Assert.isNotNull(diagram);
		this.diagram = diagram;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor#getProvider()
	 */
	@Override
	public IProvider getProvider() {
		if (null == provider) {
			provider = new ArchitectureExtendedPaletteProvider();
			((ArchitectureExtendedPaletteProvider) provider).setContributions(this);
		}
		return provider;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor#hasOnlyEntriesDefinition()
	 */
	@Override
	public boolean hasOnlyEntriesDefinition() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor#getContributionName()
	 */
	@Override
	public String getContributionName() {
		return getDiagram().getName() + ARCHITECTURE_PALETTE_NAME_SUFFIX;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContributionID() {
		return getDiagram().getName() + ARCHITECTURE_PALETTE_ID_SUFFIX;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor#getPriority()
	 */
	@Override
	public ProviderPriority getPriority() {
		return ProviderPriority.MEDIUM;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.service.palette.PapyrusPaletteService.ProviderDescriptor#provides(org.eclipse.gmf.runtime.common.core.service.IOperation)
	 */
	@SuppressWarnings("restriction")
	@Override
	public boolean provides(IOperation operation) {
		Boolean provides = false;
		if (operation instanceof ContributeToPaletteOperation) {
			ContributeToPaletteOperation o = (ContributeToPaletteOperation) operation;
			IEditorPart part = o.getEditor();
			if (part instanceof DiagramEditorWithFlyOutPalette) {
				String diagramName = getDiagram().getName(); // name is the key of modelkind
				if (null != diagramName) {
					Diagram diagramPalette = ((DiagramEditorWithFlyOutPalette) part).getDiagram();
					String implementationID = getDiagram().getImplementationID();
					PapyrusDiagramStyle papyrusDiagramStyle = org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils.getPapyrusDiagramStyle(diagramPalette);

					boolean match = false;
					if (null != papyrusDiagramStyle) {
						ArchitectureDomainManager manager = ArchitectureDomainManager.getInstance();
						PapyrusDiagram repKind = (PapyrusDiagram) manager.getRepresentationKindById(papyrusDiagramStyle.getDiagramKindId());
						match = diagramName.equals(repKind.getName());
					} else if (null != implementationID) {
						// If there is no diagram style we match on the implementation ID. See bug 516878
						match = null != diagramPalette && implementationID.equals(diagramPalette.getType());
					}

					if (match && !isHidden(o)) {
						ModelSet modelSet = null;
						try {
							// Find out if a viewpoint refers the palettes diagram.
							modelSet = ServiceUtils.getInstance().getModelSet(part.getAdapter(ServicesRegistry.class));
							List<?> collect = new ArchitectureDescriptionUtils(modelSet).getArchitectureViewpoints().stream()// gets Viewpoints
									.flatMap(p -> p.getRepresentationKinds().stream())// get representation kinds from viewpoint
									.filter(PapyrusDiagram.class::isInstance).map(PapyrusDiagram.class::cast)// filter on diagram type
									.filter(p -> getDiagram().getQualifiedName().equals(p.getQualifiedName()))
									.flatMap(p -> p.getPalettes().stream()).distinct()// Get paletteConf
									.collect(Collectors.toList());// as list
							provides = !collect.isEmpty();

						} catch (ServiceException e) {
							Activator.log.error("Can't get model set", e);
						}
					}
				}
			}
		}

		return provides;
	}

	/**
	 * checks if this provider is providing elements, even if this should be
	 * hidden
	 *
	 * @param operation
	 *            the operation to contribute
	 * @return <code>true</code> if this provider contributes to the
	 *         operation
	 */
	public boolean providesWithVisibility(ContributeToPaletteOperation operation) {
		/**
		 * @see org.eclipse.gmf.runtime.common.core.service.IProvider#provides(org.eclipse.gmf.runtime.common.core.service.IOperation)
		 */
		boolean isEnable = ActivityUtil.isEnabled(getContributionID(), Activator.ID);

		if (!isEnable) {
			return false;
		}
		// => Remove the test
		if (operation instanceof ContributeToPaletteOperation) {
			ContributeToPaletteOperation o = operation;

			IEditorPart part = o.getEditor();
			if (!(part instanceof DiagramEditorWithFlyOutPalette)) {
				return false;
			}

			// will never work, ID of the site is the multi diagram
			// editor...
			if (getDiagram().getImplementationID() != null) {
				if (!getDiagram().getImplementationID().equals(((DiagramEditorWithFlyOutPalette) part).getDiagram().getType())) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Reads the configuration file in the bundle
	 *
	 * @param bundle
	 * @param filePath
	 * @return
	 */
	protected InputStream openConfigurationFile(Bundle bundle, String filePath) {
		try {
			URL urlFile = bundle.getEntry(filePath);
			urlFile = FileLocator.resolve(urlFile);
			urlFile = FileLocator.toFileURL(urlFile);
			if ("file".equals(urlFile.getProtocol())) { //$NON-NLS-1$
				return new FileInputStream(urlFile.getFile());
			} else if ("jar".equals(urlFile.getProtocol())) { //$NON-NLS-1$
				String path = urlFile.getPath();
				if (path.startsWith("file:")) {//$NON-NLS-1$
					// strip off the file: and the !/
					int jarPathEndIndex = path.indexOf("!/");
					if (jarPathEndIndex < 0) {
						Activator.log.error("Impossible to find the jar path end", null);
						return null;
					}
					String jarPath = path.substring("file:".length(), jarPathEndIndex);
					ZipFile zipFile = new ZipFile(jarPath);
					filePath = filePath.substring(jarPathEndIndex + 2, path.length());
					ZipEntry entry = zipFile.getEntry(path);
					return zipFile.getInputStream(entry);
					// return new File(filePath);
				}
			}
		} catch (IOException e) {
			Activator.log.error("Impossible to find initial file", e);
		}
		return null;
	}

	/**
	 * the bundle entry protocol prefix
	 */
	private static final String BUNDLEENTRY_PROTOCOL = "bundleentry://"; //$NON-NLS-1$

	/**
	 * the plateform protocol prefix
	 */
	private static final String PLUGIN_PROTOCOL = "platform:/plugin/"; //$NON-NLS-1$ ;

	private static String retrieveBundleId(final String initialValue) {
		String result = null;
		if (initialValue.startsWith(PLUGIN_PROTOCOL)) {
			String tmp = initialValue.substring(PLUGIN_PROTOCOL.length());
			int bundleIdEndIndex = tmp.indexOf("/");//$NON-NLS-1$
			result = tmp.substring(0, bundleIdEndIndex);
		} else if (initialValue.startsWith(BUNDLEENTRY_PROTOCOL)) {

			String absolutePath = null;
			try {
				URL url = new URL(initialValue);
				absolutePath = FileLocator.resolve(url).getPath();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}

			// Remove the local path (/icons/obj16/ContainmentConnection.gif)
			int bundleIdEndIndex = absolutePath.indexOf("/");//$NON-NLS-1$
			result = absolutePath.substring(0, bundleIdEndIndex);

			if (-1 != result.indexOf("/")) {//$NON-NLS-1$
				result = result.substring(result.lastIndexOf("/") + 1);//$NON-NLS-1$
			}

		}
		return result;
	}

	/**
	 * @return
	 */
	public String createLocalRedefinition() {
		PaletteConfiguration paletteConfiguration = getDiagram().getPalettes().get(0);
		URI uri = EcoreUtil.getURI(paletteConfiguration).trimFragment();

		String localRedifinitionPath = uri.toPlatformString(true);

		InputStream stream = null;
		String filePath = localRedifinitionPath.substring(1, localRedifinitionPath.length());
		int indexOf = filePath.indexOf(IPath.SEPARATOR);
		String bundleId = filePath.substring(0, indexOf);
		filePath = filePath.substring(indexOf, filePath.length());

		String realId = bundleId;
		Bundle bundle = Platform.getBundle(bundleId);
		//
		// getBundle
		if (Platform.isFragment(bundle)) {
			// retrieve the file in the fragment itself
			stream = openConfigurationFile(bundle, filePath);
		} else {
			// this is a plugin. Search in sub fragments, then in the plugin
			Bundle[] fragments = Platform.getFragments(bundle);
			// no fragment, so the file should be in the plugin itself
			if (fragments == null) {
				stream = openConfigurationFile(bundle, filePath);
			} else {
				for (Bundle fragment : fragments) {
					if (stream == null) {
						stream = openConfigurationFile(fragment, filePath);
						realId = fragment.getSymbolicName();
					}
				}

				if (stream == null) {
					// no file in fragments. open in the plugin itself
					stream = openConfigurationFile(bundle, filePath);
					realId = bundle.getSymbolicName();
				}
			}
		}

		// check the stream
		if (stream == null) {
			Activator.log.error("Impossible to read initial file", null);
			return null;
		}

		File stateLocationRootFile = Activator.getInstance().getStateLocation().toFile();
		File bundleFolder = new File(stateLocationRootFile, realId);
		bundleFolder.mkdir();

		// for all intermediate folders in filePath, create a folder in
		// plugin state location
		File root = bundleFolder;
		// Split folder with / or \ file separator
		String[] folders = filePath.split("\\Q\\\\E|\\Q/\\E");//$NON-NLS-1$
		for (int i = 0; i < folders.length - 1; i++) { // all intermediate
			// folders. Last one
			// is the file name
			// itself...
			String folderName = folders[i];
			if (null != folderName && folderName.length() != 0) {
				File newFolder = new File(root, folders[i]);
				newFolder.mkdir();
				root = newFolder;
			}
		}

		File newFile = new File(root, folders[folders.length - 1]);
		boolean fileCreated = false;

		// check if file already exists or not
		if (newFile.exists()) {
			fileCreated = true;
		} else {
			try {
				fileCreated = newFile.createNewFile();
			} catch (IOException e) {
				Activator.log.error("Impossible to create new file", e);
				return null;
			}
		}

		if (!fileCreated) {
			Activator.log.error("It was not possible to create the file", null);
			return null;
		}

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = stream.read(buf)) > 0) {
				fileOutputStream.write(buf, 0, len);
			}
			stream.close();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			Activator.log.error("It was not possible to write in the file", e);//$NON-NLS-1$
			return null;
		} catch (IOException e) {
			Activator.log.error("It was not possible to write in the file", e);//$NON-NLS-1$
			return null;
		}

		// Needs to add a / to have a correct path or the concatenation will be false.
		if (!filePath.startsWith("/") || !filePath.startsWith("\\")) {//$NON-NLS-1$ //$NON-NLS-2$
			filePath = IPath.SEPARATOR + filePath;
		}

		// return realId + filePath;
		return localRedifinitionPath;
	}

	/**
	 * @return the diagram
	 */
	public PapyrusDiagram getDiagram() {
		return diagram;
	}

}