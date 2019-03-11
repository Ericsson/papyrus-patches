package org.eclipse.papyrus.infra.gmfdiag.common.reconciler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.plugin.RegistryReader;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

public class DiagramReconcilersReader extends RegistryReader {

	private static volatile DiagramReconcilersReader ourInstance = null;

	private static final String EXT_PT = "diagramReconciler"; //$NON-NLS-1$

	private static final String TAG_DIAGRAM_RECONCILER = "diagramReconciler"; //$NON-NLS-1$

	private Map<String, Collection<DiagramReconciler>> myReconcilers;

	/**
	 * Read-only view on the myReconcilers bag. Only available when myReconcilers != null
	 */
	private Map<String, Collection<DiagramReconciler>> myReconcilersRO;

	public static final DiagramReconcilersReader getInstance() {
		if (ourInstance == null) {
			synchronized (DiagramReconcilersReader.class) {
				if (ourInstance == null) {
					ourInstance = new DiagramReconcilersReader();
				}
			}
		}
		return ourInstance;
	}

	DiagramReconcilersReader() {
		super(Platform.getExtensionRegistry(), Activator.ID, EXT_PT);
	}

	/**
	 * @return unmodifiable map of loaded reconcilers
	 */
	public synchronized Map<String, Collection<DiagramReconciler>> load() {
		if (myReconcilers == null) {
			myReconcilers = new HashMap<String, Collection<DiagramReconciler>>();
			myReconcilersRO = Collections.unmodifiableMap(myReconcilers);
			readRegistry();
		}
		return myReconcilersRO;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.emf.ecore.plugin.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement, boolean)
	 */
	@Override
	protected boolean readElement(IConfigurationElement element, boolean add) {
		if (!TAG_DIAGRAM_RECONCILER.equals(element.getName())) {
			return false;
		}

		String className = element.getAttribute(DiagramReconciler.ATTR_RECONCILER_CLASS);
		String diagramTypeString = element.getAttribute(DiagramReconciler.ATTR_DIAGRAM_TYPE);
		String sourceVersion = element.getAttribute(DiagramReconciler.ATTR_SOURCE_VERSION);
		String targetVersion = element.getAttribute(DiagramReconciler.ATTR_TARGET_VERSION);

		if (!checkNotEmpty(className)) {
			logMissingAttribute(element, DiagramReconciler.ATTR_RECONCILER_CLASS);
			return false;
		}
		if (!checkNotEmpty(diagramTypeString)) {
			logMissingAttribute(element, DiagramReconciler.ATTR_DIAGRAM_TYPE);
			return false;
		}
		if (!checkNotEmpty(sourceVersion)) {
			logMissingAttribute(element, DiagramReconciler.ATTR_SOURCE_VERSION);
			return false;
		}

		if (!checkNotEmpty(targetVersion)) {
			logMissingAttribute(element, DiagramReconciler.ATTR_TARGET_VERSION);
			return false;
		}
		if (!DiagramVersioningUtils.isCurrentPapyrusVersion(targetVersion)) {
			Activator.log.debug("Reconciler for outdated version is still registered but will never be executed: " + className); //$NON-NLS-1$
			return false;
		}


		if (add) {
			addDiagramReconciler(element);
		} else {
			removeDiagramReconciler(element);
		}

		return true;
	}

	private static boolean checkNotEmpty(String attr) {
		return (attr != null) && (attr.length() != 0);
	}

	protected void addDiagramReconciler(IConfigurationElement element) {
		DiagramReconciler reconciler = createReconciler(element);
		if (reconciler != null) {
			synchronized (myReconcilers) {
				String diagramType = reconciler.getDiagramVisualID();
				Collection<DiagramReconciler> listForType = myReconcilers.get(diagramType);
				if (listForType == null) {
					listForType = new LinkedList<DiagramReconciler>();
					myReconcilers.put(diagramType, listForType);
				}
				listForType.add(reconciler);
			}
		}
	}

	/**
	 * Unregisters reconciler defined by given extension element.
	 * <p/>
	 * Only diagram type and fully qualified class name will be used to find the one instance to remove.
	 */
	protected void removeDiagramReconciler(IConfigurationElement element) {
		String diagramTypeString = element.getAttribute(DiagramReconciler.ATTR_DIAGRAM_TYPE);
		String fqn = element.getAttribute(DiagramReconciler.ATTR_RECONCILER_CLASS);
		if (diagramTypeString == null || fqn == null) {
			// we already have skipped this config at the time of addition
			return;
		}

		synchronized (myReconcilers) {
			Collection<DiagramReconciler> reconcilers = myReconcilers.get(diagramTypeString);
			if (reconcilers != null) {
				for (Iterator<DiagramReconciler> it = reconcilers.iterator(); it.hasNext();) {
					DiagramReconciler next = it.next();
					if (fqn.equals(next.getClassFqn())) {
						it.remove();
						break;
					}
				}
				if (reconcilers.size() == 0) {
					myReconcilers.remove(diagramTypeString);
				}
			}
		}
	}

	/**
	 * Instantiates the reconciler defined by given extension
	 *
	 * @param element
	 * @return configured reconciler instance or <code>null</code> if something bad happens (error is logged in this case)
	 */
	private DiagramReconciler createReconciler(IConfigurationElement element) {
		try {
			Object diagramReconcilerObject = element.createExecutableExtension(DiagramReconciler.ATTR_RECONCILER_CLASS);
			if (diagramReconcilerObject instanceof DiagramReconciler) {
				return (DiagramReconciler) diagramReconcilerObject;
			} else {
				Activator.log.error("Diagram reconciler extension does not extend mandatory DiagramReconciler base class: " + element.getAttribute(DiagramReconciler.ATTR_RECONCILER_CLASS), null); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			Activator.getInstance().getLog().log(e.getStatus());
		}
		return null;
	}

}
