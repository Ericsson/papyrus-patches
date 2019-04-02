/*****************************************************************************
 * Copyright (c) 2012, 2014, 2017 Atos, CEA, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mathieu Velten (Atos) mathieu.velten@atos.net - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 323802
 *  Christian W. Damus (CEA) - bug 448139
 *  Pierre GAUTIER (CEA LIST) - bug 521857
 *  Fanch BONNABESSE (ALL4TEC) fanch.bonnabesse@all4tec.net - Bug 521902, Bug 526304
 *  Vincent LORENZO (CEA LIST) - bug 526900
 *****************************************************************************/
package org.eclipse.papyrus.uml.properties.widgets;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.GMFObservableList;
import org.eclipse.papyrus.infra.gmfdiag.common.databinding.GMFObservableValue;
import org.eclipse.papyrus.infra.ui.emf.providers.EMFEnumeratorContentProvider;
import org.eclipse.papyrus.infra.widgets.creation.ReferenceValueFactory;
import org.eclipse.papyrus.infra.widgets.editors.AbstractValueEditor;
import org.eclipse.papyrus.infra.widgets.editors.BooleanCombo;
import org.eclipse.papyrus.infra.widgets.editors.DoubleEditor;
import org.eclipse.papyrus.infra.widgets.editors.EnumCombo;
import org.eclipse.papyrus.infra.widgets.editors.FloatEditor;
import org.eclipse.papyrus.infra.widgets.editors.IntegerEditor;
import org.eclipse.papyrus.infra.widgets.editors.LongEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleBooleanEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleDoubleEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleIntegerEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleReferenceEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleStringEditor;
import org.eclipse.papyrus.infra.widgets.editors.MultipleValueEditor;
import org.eclipse.papyrus.infra.widgets.editors.ReferenceDialog;
import org.eclipse.papyrus.infra.widgets.editors.StringEditor;
import org.eclipse.papyrus.infra.widgets.editors.TypedMultipleStringEditor;
import org.eclipse.papyrus.infra.widgets.providers.IStaticContentProvider;
import org.eclipse.papyrus.uml.properties.Activator;
import org.eclipse.papyrus.uml.tools.utils.DataTypeUtil;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.PageBook;

/**
 * A structural feature editor
 *
 */
public class EStructuralFeatureEditor implements IValueChangeListener<Object>, IListChangeListener<Object> {

	protected PageBook pageBook;

	protected Composite currentPage;

	protected int style = 0;

	protected IStaticContentProvider contentProvider;

	protected ILabelProvider labelProvider;

	protected ReferenceValueFactory valueFactory;

	protected IChangeListener changeListener;

	private static final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

	static {
		// String
		EStructuralFeatureEditor.TYPE_ALIASES.put(String.class.getCanonicalName(), String.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("string", String.class); //$NON-NLS-1$
		// Boolean
		EStructuralFeatureEditor.TYPE_ALIASES.put(Boolean.class.getCanonicalName(), Boolean.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("boolean", Boolean.class); //$NON-NLS-1$
		EStructuralFeatureEditor.TYPE_ALIASES.put("bool", Boolean.class); //$NON-NLS-1$
		// Integer
		EStructuralFeatureEditor.TYPE_ALIASES.put(Integer.class.getCanonicalName(), Integer.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("int", Integer.class); //$NON-NLS-1$
		EStructuralFeatureEditor.TYPE_ALIASES.put("integer", Integer.class); //$NON-NLS-1$
		// Long
		EStructuralFeatureEditor.TYPE_ALIASES.put(Long.class.getCanonicalName(), Long.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("long", Long.class); //$NON-NLS-1$
		// Float
		EStructuralFeatureEditor.TYPE_ALIASES.put(Float.class.getCanonicalName(), Float.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("float", Float.class); //$NON-NLS-1$
		// Double
		EStructuralFeatureEditor.TYPE_ALIASES.put(Double.class.getCanonicalName(), Double.class);
		EStructuralFeatureEditor.TYPE_ALIASES.put("double", Double.class); //$NON-NLS-1$
	}

	private static final Map<Class<?>, Class<? extends MultipleStringEditor<?>>> TYPE_TO_MULTI_EDITOR_CLASS = new HashMap<>();
	private static final Map<Class<?>, Class<? extends AbstractValueEditor>> TYPE_TO_SINGLE_EDITOR_CLASS = new HashMap<>();

	static {
		// String
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(String.class, StringEditor.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(String.class, TypedMultipleStringEditor.class);
		// Boolean
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(Boolean.class, BooleanCombo.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(Boolean.class, MultipleBooleanEditor.class);
		// Integer
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(Integer.class, IntegerEditor.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(Integer.class, MultipleIntegerEditor.class);
		// Long
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(Long.class, LongEditor.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(Long.class, MultipleIntegerEditor.class);
		// Float
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(Float.class, FloatEditor.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(Float.class, MultipleDoubleEditor.class);
		// Double
		EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.put(Double.class, DoubleEditor.class);
		EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.put(Double.class, MultipleDoubleEditor.class);
	}

	public EStructuralFeatureEditor(final Composite parent, final int style) {
		this.style = style;
		pageBook = new PageBook(parent, style);
		currentPage = createEmptyPage();
		pageBook.showPage(currentPage);
	}

	public void setProviders(final IStaticContentProvider contentProvider, final ILabelProvider labelProvider) {
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
	}

	public void setValueFactory(final ReferenceValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public void setChangeListener(final IChangeListener changeListener) {
		this.changeListener = changeListener;
	}

	protected Composite createEmptyPage() {
		return new Composite(pageBook, style);
	}

	public void setFeatureToEdit(final String title, final EStructuralFeature feature, final EObject element) {
		Composite previousPage = this.currentPage;
		if (currentPage != null) {
			currentPage = null;
		}

		if (feature instanceof EReference) {
			if (feature.isMany()) {
				final MultipleReferenceEditor editor = new MultipleReferenceEditor(pageBook, style);
				setMultipleValueEditorProperties(editor, (List<?>) element.eGet(feature), element, title, feature);
				editor.setProviders(contentProvider, labelProvider);
				editor.setFactory(valueFactory);
				currentPage = editor;
			} else {
				final EClassifier featureType = feature.getEType();
				if (featureType instanceof EClass && DataTypeUtil.isDataTypeDefinition((EClass) featureType, element)) {
					final EObjectContentsEditor editor = new EObjectContentsEditor(pageBook, style, (EReference) feature);
					editor.setValue(new GMFObservableValue(element, feature, EMFHelper.resolveEditingDomain(element)));
					currentPage = editor;
				} else {
					final ReferenceDialog editor = new ReferenceDialog(pageBook, style);
					setValueEditorProperties(editor, element, title, feature);
					editor.setContentProvider(contentProvider);
					editor.setLabelProvider(labelProvider);
					editor.setValueFactory(valueFactory);
					editor.setDirectCreation(((EReference) feature).isContainment());
					currentPage = editor;
				}
			}
		}

		if (feature instanceof EAttribute) {
			final EClassifier featureType = feature.getEType();
			if (featureType instanceof EEnum) {
				if (feature.isMany()) {
					final MultipleReferenceEditor editor = new MultipleReferenceEditor(pageBook, style);
					setMultipleValueEditorProperties(editor, (List<?>) element.eGet(feature), element, title, feature);
					editor.setProviders(contentProvider, labelProvider);
					editor.setFactory(valueFactory);
					currentPage = editor;
				} else {
					final EnumCombo editor = new EnumCombo(pageBook, style);
					setValueEditorProperties(editor, element, title, feature);
					editor.setContentProvider(new EMFEnumeratorContentProvider(feature));
					currentPage = editor;
				}
			} else if (featureType instanceof EDataType) {
				final String aliasedInstanceClassName = ((EDataType) featureType).getInstanceClassName();
				final Class<?> clazz = EStructuralFeatureEditor.TYPE_ALIASES.get(aliasedInstanceClassName);
				if (clazz == null) {
					throw new IllegalArgumentException("No clazz has been found for aliasedInstanceClassName '" + aliasedInstanceClassName + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (feature.isMany()) {
					createMultipleEditor(clazz, element, title, feature);
				} else {
					createSingleEditor(clazz, element, title, feature);
				}
			}
		}
		if (currentPage == null) {
			currentPage = createEmptyPage();
		}
		pageBook.showPage(currentPage);
		// we dispose it after the showPage because until the previous line, we continue to display it, so we can't call the dispose!
		previousPage.dispose();
		previousPage = null;
	}

	protected void setValueEditorProperties(final AbstractValueEditor editor, final EObject stereotypeApplication, final String title, final EStructuralFeature feature) {
		final GMFObservableValue observable = new GMFObservableValue(stereotypeApplication, feature, EMFHelper.resolveEditingDomain(stereotypeApplication));
		observable.addValueChangeListener(this);
		editor.setLabel(title);
		editor.setReadOnly(!isEditable(stereotypeApplication, feature));
		editor.setModelObservable(observable);
	}

	protected void setMultipleValueEditorProperties(final MultipleValueEditor<?> editor, final List<?> initialList, final EObject stereotypeApplication, final String title, final EStructuralFeature feature) {
		final GMFObservableList observable = new GMFObservableList(initialList, EMFHelper.resolveEditingDomain(stereotypeApplication), stereotypeApplication, feature);
		observable.addListChangeListener(this);
		editor.setLabel(title);
		editor.setUnique(feature.isUnique());
		editor.setOrdered(feature.isOrdered());
		editor.setUpperBound(feature.getUpperBound());
		editor.setModelObservable(observable);
		editor.setReadOnly(!isEditable(stereotypeApplication, feature));
		if (feature instanceof EReference) {
			editor.setDirectCreation(((EReference) feature).isContainment());
		}
		editor.addCommitListener(observable);
	}

	private void createMultipleEditor(final Class<?> typeClass, final EObject element, final String title, final EStructuralFeature feature) {
		final Class<? extends MultipleStringEditor<?>> editorClazz = EStructuralFeatureEditor.TYPE_TO_MULTI_EDITOR_CLASS.get(typeClass);
		if (editorClazz == null) {
			throw new IllegalArgumentException("No multiple editor has been found for class '" + typeClass + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			final Constructor<? extends MultipleStringEditor<?>> constructor = editorClazz.getConstructor(Composite.class, boolean.class, boolean.class, int.class);
			final MultipleStringEditor<?> editor = constructor.newInstance(pageBook, true, true, style);
			setMultipleValueEditorProperties(editor, (List<?>) element.eGet(feature), element, title, feature);
			currentPage = editor;
		} catch (final Exception e) {
			Activator.log.error(e);
		}
	}

	private void createSingleEditor(final Class<?> typeClass, final EObject element, final String title, final EStructuralFeature feature) {
		final Class<? extends AbstractValueEditor> clazz = EStructuralFeatureEditor.TYPE_TO_SINGLE_EDITOR_CLASS.get(typeClass);
		if (clazz == null) {
			throw new IllegalArgumentException("No single editor has been found for class '" + typeClass + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			final Constructor<? extends AbstractValueEditor> constructor = clazz.getConstructor(Composite.class, int.class);
			final AbstractValueEditor editor = constructor.newInstance(pageBook, style);
			setValueEditorProperties(editor, element, title, feature);
			currentPage = editor;
		} catch (final Exception e) {
			Activator.log.error(e);
		}
	}

	protected boolean isEditable(final EObject object, final EStructuralFeature feature) {
		return !feature.isDerived() && feature.isChangeable() && !EMFHelper.isReadOnly(object);
	}

	public void setLayoutData(final GridData data) {
		pageBook.setLayoutData(data);
	}

	@Override
	public void handleValueChange(final ValueChangeEvent<?> event) {
		if (changeListener != null) {
			changeListener.handleChange(new ChangeEvent(event.getObservable()));
		}
	}

	@Override
	public void handleListChange(final ListChangeEvent<?> event) {
		if (changeListener != null) {
			changeListener.handleChange(new ChangeEvent(event.getObservable()));
		}
	}
}
