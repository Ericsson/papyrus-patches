package org.eclipse.papyrus.infra.gmfdiag.common.reconciler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.StringValueStyle;

/**
 * Instances of this base class are provided via "org.eclipse.papyrus.infra.gmfdiag.common.diagramReconciler" extension point and provide migration of
 * the diagram instances from "previous" version to the "current" Papyrus version. The version is stored as a {@link StringValueStyle} with name {@link DiagramVersioningUtils#COMPATIBILITY_VERSION}.
 * <p/>
 * Every {@link DiagramReconciler} subclass provides statical descriptor of its capabilities (that is, the supported diagram type, outdated "previous" version and the expected "current" version). For some possible advanced cases it also gets a chance to
 * decline the reconciliation for some particular instance based on runtime data, via {@link DiagramReconciler#canReconcileFrom(Diagram, String)}.
 * <p/>
 * If the suitable reconcilers are found, all of them are asked to update diagram instance before the first diagram opening. Planned modifications are provided as an {@link ICommand} instance that allows user to roll-back them all at once.
 * <p/>
 */
public abstract class DiagramReconciler implements IExecutableExtension {

	/**
	 * Extension point attribute passed to {@link IConfigurationElement#createExecutableExtension(String)}, so value should be FQN of reconciler to
	 * instantiate.
	 * <p/>
	 * Note that the value of this class will be used to search the instance to un-register, so it should be unique among all the reconcilers for given diagram type. It is however safe to define same reconciler class for different diagram types.
	 * <p/>
	 * Value of this constant is "reconcilerClass"
	 */
	public static final String ATTR_RECONCILER_CLASS = "reconcilerClass"; //$NON-NLS-1$

	/**
	 * Mandatory extension attribute to define applicable diagram type for reconciler
	 * <p/>
	 * Value of this constant is "diagramType"
	 */
	public static final String ATTR_DIAGRAM_TYPE = "diagramType"; //$NON-NLS-1$

	/**
	 * Mandatory extension attribute to define applicable outdated diagram version this reconciler can handle.
	 * Reconciler's that wants to update diagrams created before the Papyrus 1.0.0 release may be registered to "undefined" source version.
	 * <p/>
	 * Value of this constant is "source"
	 */
	public static final String ATTR_SOURCE_VERSION = "source"; //$NON-NLS-1$

	/**
	 * Mandatory extension attribute to define applicable "current" diagram version this reconciler will update the instance to.
	 * <p/>
	 * This value should pass the {@link DiagramVersioningUtils#isCurrentPapyrusVersion(String)} check for reconciler to be considered.
	 * <p/>
	 * Value of this constant is "target"
	 */
	public static final String ATTR_TARGET_VERSION = "target"; //$NON-NLS-1$

	private String myDiagramType;

	private String mySourceVersion;

	private String myTargetVersion;

	/**
	 * The value of this to be used when searching
	 */
	private String myClassFqn;

	/**
	 * Instance of reconciler gets a chance to deny reconciliation based on some custom run-time properties.
	 * The <code>false</code> value returned from this method means that the diagram is unsuitable for this reconciler, and does not assume any error.
	 * <p>
	 * Subclass can override tgis method, default implementation always return true, because all the dfeault filtering is done based on static extension declarations.
	 *
	 * @param diagram
	 *            the runtime instance to reconcile
	 * @param currentDiagramVersion
	 * @return <code>false</code> if this reconciler wants to be ignored for some particular diagram instance
	 */
	public boolean canReconcileFrom(Diagram diagram, String currentDiagramVersion) {
		return true;
	}

	/**
	 * Creates command that will fix the given diagram and update it to the statically known "current" version.
	 * It is considered an error to return the command with {@link ICommand#canExecute()} of <code>false</code>, and editor implementation will log an
	 * erroneous reconcile attempt.
	 * <p/>
	 * If the reconciler wants to ignore the instance it can either return <code>false</code> from {@link DiagramReconciler#canReconcileFrom(Diagram, String)} or return <code>null</code> from this method
	 *
	 * @param diagram
	 * @return the command or <code>null</code> if nothing to do with this instance
	 */
	public abstract ICommand getReconcileCommand(Diagram diagram);

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		myDiagramType = config.getAttribute(ATTR_DIAGRAM_TYPE);
		mySourceVersion = config.getAttribute(ATTR_SOURCE_VERSION);
		myTargetVersion = config.getAttribute(ATTR_TARGET_VERSION);
		myClassFqn = config.getAttribute(propertyName);
	}

	public String getDiagramVisualID() {
		return myDiagramType;
	}

	public String getSourceVersion() {
		return mySourceVersion;
	}

	public String getTargetVersion() {
		return myTargetVersion;
	}

	public String getClassFqn() {
		return myClassFqn;
	}

	protected static boolean safeEquals(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		return s1 != null && s1.equals(s2);
	}

	/**
	 * For debug purpose only
	 */
	@Override
	public String toString() {
		return "DiagramReconciler:" + myClassFqn + "[" + myDiagramType + ": " + mySourceVersion + " -> " + myTargetVersion + "]";
	}
}
