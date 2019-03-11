package org.eclipse.papyrus.uml.diagram.sequence.sheet;

import org.eclipse.papyrus.infra.gmfdiag.tooling.runtime.sheet.DefaultPropertySection;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

/**
 * @generated
 */
public class UMLPropertySection extends DefaultPropertySection implements IPropertySourceProvider {

	/**
	 * Modify/unwrap selection.
	 *
	 * @generated
	 */
	@Override
	protected Object transformSelection(Object selected) {
		selected = /* super. */transformSelectionToDomain(selected);
		return selected;
	}

}
