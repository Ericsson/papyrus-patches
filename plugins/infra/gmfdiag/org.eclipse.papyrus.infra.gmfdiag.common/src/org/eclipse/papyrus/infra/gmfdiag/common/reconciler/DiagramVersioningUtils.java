/*****************************************************************************
 * Copyright (c) 2011, 2018 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *		CEA LIST - Initial API and implementation
 *   Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Bug 531520 (update to 1.4)
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.reconciler;

import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;

/**
 * This class provide several convenience methods to tag a diagram with a version number
 * in a notation style, or retrieve this version number.
 */
public class DiagramVersioningUtils {

	/**
	 * Returns the "current" diagram version. Diagrams with this version don't require the reconciliation until the Papyrus version updates in such a
	 * way that some diagram needs reconciliation.
	 * <p/>
	 * The current value returned by this method is "1.4.0".
	 * <p/>
	 * The value itself, howewer, should NOT be used outside of this package to avoid weird dependency issues. Instead, external code should use {@link DiagramVersioningUtils#stampCurrentVersion(Diagram)} and
	 * {@link DiagramVersioningUtils#createStampCurrentVersionCommand(Diagram)}.
	 * <p/>
	 * This method is intentinally NOT a constant but indeed the method. This method is intentionally private and should NOT be made public.
	 */
	private static String CURRENT_DIAGRAM_VERSION() {
		return "1.4.0"; //$NON-NLS-1$
	}

	/**
	 * Directly marks the given diagram as either created with "current" Papyrus version or already reconciled to the "current" Papyrus version.
	 * <p/>
	 * It is guaranteed that {@link DiagramVersioningUtils#isOfCurrentPapyrusVersion(Diagram)} returns true immediately after the call to this method.
	 *
	 * @param diagram
	 *            diagram to stamp as "current"
	 */
	public static void stampCurrentVersion(Diagram diagram) {
		setCompatibilityVersion(diagram, CURRENT_DIAGRAM_VERSION());
	}

	/**
	 * Returns the command that will mark the given diagram as either created with "current" Papyrus version or already reconciled to the "current"
	 * Papyrus version.
	 * <p/>
	 * It is guaranteed that {@link DiagramVersioningUtils#isOfCurrentPapyrusVersion(Diagram)} will returns true immediately after the execution of the command.
	 *
	 * @param diagram
	 * @return the command that is guaranteed to be not null and executable
	 */
	public static ICommand createStampCurrentVersionCommand(Diagram diagram) {
		StringValueStyle style = findOrCreateCompatibilityStyle(diagram);
		if (style.eContainer() == null) {
			style.setStringValue(CURRENT_DIAGRAM_VERSION());
			return new SetValueCommand(new SetRequest(diagram, NotationPackage.eINSTANCE.getView_Styles(), style));
		} else {
			return new SetValueCommand(new SetRequest(style, NotationPackage.eINSTANCE.getStringValueStyle_StringValue(), CURRENT_DIAGRAM_VERSION()));
		}
	}

	/**
	 * The name of the {@link StringValueStyle} that defines actual diagram version.
	 * <p/>
	 * The value for this constant is "diagram_compatibility_version", it is intentionally the same as been used for SysML diagrams versioning.
	 */
	public static final String COMPATIBILITY_VERSION = "diagram_compatibility_version";//$NON-NLS-1$

	/**
	 * The version constant for the diagrams that does not have a {@link DiagramVersioningUtils#COMPATIBILITY_VERSION} style.
	 * It may be assumed that these diagrams had been created before Papyrus 1.0.
	 */
	public static final String UNDEFINED_VERSION = "undefined";//$NON-NLS-1$

	private static final String DELIM_VERSION = ".";//$NON-NLS-1$

	/**
	 * Get the diagram compatibility version.
	 *
	 * @param view
	 *            the diagram
	 * @return the compatibility version or {@link DiagramVersioningUtils#UNDEFINED_VERSION} if none stored. Never returns <code>null</code>.
	 */
	public static String getCompatibilityVersion(View view) {
		StringValueStyle semanticStyle = (StringValueStyle) view.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), COMPATIBILITY_VERSION);
		return semanticStyle == null ? UNDEFINED_VERSION : semanticStyle.getStringValue();
	}

	/**
	 * Set the diagram compatibility version.
	 *
	 * @param diagram
	 *            the diagram
	 * @param version
	 *            the compatibility version
	 */
	@SuppressWarnings("unchecked")
	public static void setCompatibilityVersion(View view, String version) {
		StringValueStyle style = findOrCreateCompatibilityStyle(view);
		style.setStringValue(version);
		if (style.eContainer() == null) {
			view.getStyles().add(style);
		}
	}

	/**
	 * Finds the existing style with {@link DiagramVersioningUtils#COMPATIBILITY_VERSION} name or creates a new one if none existing found.
	 * Does NOT attach the new style to the instance, it is left as a caller responsibility.
	 *
	 * @param view
	 * @return the existing or a new not-attached style with {@link DiagramVersioningUtils#COMPATIBILITY_VERSION} name. Called can determine the case
	 *         by checking {@link EObject#eContainer()} which is guaranteed to be null in case if new object had been created
	 */
	private static StringValueStyle findOrCreateCompatibilityStyle(View view) {
		StringValueStyle style = (StringValueStyle) view.getNamedStyle(NotationPackage.eINSTANCE.getStringValueStyle(), COMPATIBILITY_VERSION);
		if (style == null) {
			style = NotationFactory.eINSTANCE.createStringValueStyle();
			style.setName(COMPATIBILITY_VERSION);
		}
		return style;
	}

	/**
	 * Checks whether the diagram is of "current", last released type.
	 */
	public static boolean isOfCurrentPapyrusVersion(Diagram diagram) {
		return isCurrentPapyrusVersion(getCompatibilityVersion(diagram));
	}

	/**
	 * Checks whether the given string represent the current papyrus version without telling explicitly what the current version is.
	 *
	 * @param version
	 *            version to check
	 * @return
	 */
	public static boolean isCurrentPapyrusVersion(String version) {
		return CURRENT_DIAGRAM_VERSION().equals(version);
	}

	/**
	 * Compare to version number.
	 * The test is done only on the first 2 segments of a version.
	 * The two String should have the same number of segments (i.e: 0.9.2 and 1.1.0).
	 * 
	 * @param referenceVersion
	 *            Version that is the reference for the test
	 * @param testedVersion
	 *            the version that is compare to the reference.
	 * @return true if the tested Version is before the reference Version .
	 *         false by default.
	 */
	public static boolean isBeforeVersion(String referenceVersion, String testedVersion) {
		boolean before = false;

		StringTokenizer targetVersionTokenizer = new StringTokenizer(referenceVersion, DELIM_VERSION);
		StringTokenizer sourceVersionTokenizer = new StringTokenizer(testedVersion, DELIM_VERSION);
		try {
			if (targetVersionTokenizer.countTokens() == sourceVersionTokenizer.countTokens()) {// Check if the format is the same for the 2 Strings
				int targetMainVersion = Integer.parseInt(targetVersionTokenizer.nextToken());// get the first number
				int sourceMainVersion = Integer.parseInt(sourceVersionTokenizer.nextToken());
				if (targetMainVersion == sourceMainVersion) {// if main versions are the same check the intermediate version
					int targetIntermediateVersion = Integer.parseInt(targetVersionTokenizer.nextToken());// get the second number
					int sourceIntermediateVersion = Integer.parseInt(sourceVersionTokenizer.nextToken());
					before = (targetIntermediateVersion > sourceIntermediateVersion);


				} else {
					before = (targetMainVersion > sourceMainVersion);
				}
			}

		} catch (NumberFormatException e) {
			Activator.log.error(e);
		}

		return before;
	}


}
