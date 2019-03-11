/*****************************************************************************
 * Copyright (c) 2015 Christian W. Damus and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian W. Damus - Initial API and implementation
 *   
 *****************************************************************************/

package org.eclipse.papyrus.infra.gmfdiag.common.sync;

import static org.eclipse.gmf.runtime.notation.NotationPackage.Literals.STRING_LIST_VALUE_STYLE;
import static org.eclipse.gmf.runtime.notation.NotationPackage.Literals.STRING_LIST_VALUE_STYLE__STRING_LIST_VALUE;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.notation.EObjectValueStyle;
import org.eclipse.gmf.runtime.notation.NamedStyle;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.StringListValueStyle;
import org.eclipse.gmf.runtime.notation.StringValueStyle;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.infra.tools.util.TypeUtils;

/**
 * Utilities for working with the synchronization-related {@link NamedStyle}s.
 */
public class SyncStyles {
	// A style name that is compatible with the conventions for CSS attributes
	private static final String SYNC_STYLE_NAME = "papyrus-diagram-sync"; //$NON-NLS-1$

	// A style name that is compatible with the conventions for CSS attributes
	private static final String SYNC_EXCLUDED_FEATURES_NAME = "papyrus-sync-exclude"; //$NON-NLS-1$

	/**
	 * Not instantiable by clients.
	 */
	private SyncStyles() {
		super();
	}

	static NamedStyle getSyncStyle(View view) {
		return view.getNamedStyle(NotationPackage.Literals.STRING_VALUE_STYLE, SYNC_STYLE_NAME);
	}

	public static SyncKind getSyncKind(View view) {
		SyncKind result = SyncKind.NONE;

		NamedStyle style = view.getNamedStyle(NotationPackage.Literals.STRING_VALUE_STYLE, SYNC_STYLE_NAME);
		if (style != null) {
			result = SyncKind.forStyle(style);
		}

		return result;
	}

	public static View getMaster(NamedStyle slaveStyle) {
		View result = null;

		if (slaveStyle instanceof EObjectValueStyle) {
			result = TypeUtils.as(((EObjectValueStyle) slaveStyle).getEObjectValue(), View.class);
		}

		return result;
	}

	public static void clearSync(View view) {
		NamedStyle syncStyle = getSyncStyle(view);
		if (syncStyle != null) {
			EcoreUtil.remove(syncStyle);
		}
	}

	public static NamedStyle setSync(View view) {
		return setSync(view, SyncKind.PEER);
	}

	private static NamedStyle setSync(View view, SyncKind kind) {
		StringValueStyle result = null;

		clearSync(view);

		if (kind != SyncKind.NONE) {
			result = (StringValueStyle) view.createStyle(NotationPackage.Literals.STRING_VALUE_STYLE);
			result.setName(SYNC_STYLE_NAME);
			result.setStringValue(kind.styleKey());
		}

		return result;
	}

	public static NamedStyle setSyncMaster(View view) {
		return setSync(view, SyncKind.MASTER);
	}

	public static NamedStyle setSyncSlave(View view) {
		return setSync(view, SyncKind.SLAVE);
	}

	public static Command getSyncCommand(final View view, final SyncKind kind) {
		return new AbstractCommand("Configure Synchronization") {
			private SyncKind previousKind;

			@Override
			protected boolean prepare() {
				previousKind = getSyncKind(view);
				return true;
			}

			@Override
			public void execute() {
				setSync(view, kind);
			}

			@Override
			public void undo() {
				setSync(view, previousKind);
			}

			@Override
			public void redo() {
				setSync(view, kind);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static List<String> getExcludedFeatures(View view) {
		StringListValueStyle style = getExcludedFeaturesStyle(view);
		return (style == null) ? Collections.emptyList() : Collections.unmodifiableList(style.getStringListValue());
	}

	/**
	 * Queries whether the specified {@code feature} of a {@code view} is synchronized. A feature that is not excluded by
	 * the <tt>papyrus-sync-exclude</tt> style is synchronized.
	 * 
	 * @param view
	 *            a diagram view
	 * @param feature
	 *            the name/key of the synchronizable feature. This does not necessarily correspond to the name of any feature in any Ecore model but is more abstract than that
	 * 
	 * @return whether the {@code feature} is synchronized for this {@code view}
	 */
	public static boolean isSynchronized(View view, String feature) {
		StringListValueStyle style = getExcludedFeaturesStyle(view);
		return (style == null) || !style.getStringListValue().contains(feature);
	}

	/**
	 * Sets whether the specified {@code feature} of a {@code view} is excluded from synchronization. A feature that is not excluded by
	 * the <tt>papyrus-sync-exclude</tt> style is synchronized.
	 * 
	 * @param view
	 *            a diagram view
	 * @param feature
	 *            the name/key of the synchronizable feature. This does not necessarily correspond to the name of any feature in any Ecore model but is more abstract than that
	 * @param excluded
	 *            whether the {@code feature} is excluded from synchronization
	 * 
	 * @see #createSetExcludedCommand(View, String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public static void setExcluded(View view, String feature, boolean excluded) {
		StringListValueStyle style = getExcludedFeaturesStyle(view);

		if (!excluded) {
			// We're enabling synchronization
			if (style != null) {
				style.getStringListValue().remove(feature);
				if (style.getStringListValue().isEmpty()) {
					// No longer need the style
					EcoreUtil.remove(style);
				}
			} // Otherwise, it's already included implicitly
		} else {
			// We're disabling synchronization
			if (style == null) {
				style = createExcludedFeaturesStyle(view);
			}
			if (!style.getStringListValue().contains(feature)) {
				style.getStringListValue().add(feature);
			}
		}
	}

	static StringListValueStyle getExcludedFeaturesStyle(View view) {
		return (StringListValueStyle) view.getNamedStyle(STRING_LIST_VALUE_STYLE, SYNC_EXCLUDED_FEATURES_NAME);
	}

	static StringListValueStyle createExcludedFeaturesStyle(View view) {
		StringListValueStyle result = (StringListValueStyle) view.createStyle(STRING_LIST_VALUE_STYLE);
		result.setName(SYNC_EXCLUDED_FEATURES_NAME);
		return result;
	}

	/**
	 * Creates a command that sets whether the specified {@code feature} of a {@code view} is excluded from synchronization. A feature
	 * that is not excluded by the <tt>papyrus-sync-exclude</tt> style is synchronized.
	 * 
	 * @param view
	 *            a diagram view
	 * @param feature
	 *            the name/key of the synchronizable feature. This does not necessarily correspond to the name of any feature in any Ecore model but is more abstract than that
	 * @param excluded
	 *            whether the {@code feature} is excluded from synchronization
	 * @return a command that excludes (or not, as requested) the synchronization of the {@code feature} of the {@code view}. This command
	 *         will not be {@code null} but it may not be {@linkplain Command#canExecute() executable} if the view is already in the synchronization
	 *         override state requested
	 * @see #setExcluded(View, String, boolean)
	 */
	public static Command createSetExcludedCommand(final View view, final String feature, final boolean excluded) {
		return new CompoundCommand("Set view synchronization") {
			private final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(view);

			private StringListValueStyle style;

			@Override
			protected boolean prepare() {
				prepareCommand();
				return super.prepare();
			}

			@SuppressWarnings("unchecked")
			void prepareCommand() {
				style = getExcludedFeaturesStyle(view);
				if (style != null) {
					if (style.getStringListValue().contains(feature)) {
						if (!excluded) {
							// Need to remove it
							append(RemoveCommand.create(domain, style, STRING_LIST_VALUE_STYLE__STRING_LIST_VALUE, feature));
						}
					} else {
						if (excluded) {
							// Need to add it
							append(AddCommand.create(domain, style, STRING_LIST_VALUE_STYLE__STRING_LIST_VALUE, feature));
						}
					}
				} else {
					if (excluded) {
						// Need to create the entire style
						style = NotationFactory.eINSTANCE.createStringListValueStyle();
						style.setName(SYNC_EXCLUDED_FEATURES_NAME);
						style.getStringListValue().add(feature);
						append(AddCommand.create(domain, view, NotationPackage.Literals.VIEW__STYLES, style));
					}
				}
			}

			@Override
			public void execute() {
				super.execute();

				if (style.eContainer() != view) {
					// The style was a CSS-inferred style that needs to be attached
					// to the view explicitly now because we changed it
					appendAndExecute(AddCommand.create(domain, view, NotationPackage.Literals.VIEW__STYLES, style));
				} else if (style.getStringListValue().isEmpty()) {
					// We've emptied out the explicit overrides. Don't need the style object any more
					appendAndExecute(RemoveCommand.create(domain, style));
				}
			}
		};
	}
}
