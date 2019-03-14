package org.eclipse.papyrus.uml.diagram.sequence.runtime.test.debug;

import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.editpart.IEditPartProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.CreateGraphicEditPartOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.CreateRootEditPartOperation;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.sequence.edit.parts.PackageEditPart;

public class DebugEditPartProvider extends AbstractEditPartProvider {

	public DebugEditPartProvider() {
	}

	@Override
	public boolean provides(IOperation operation) {
		if (operation instanceof CreateRootEditPartOperation ) {
			CreateRootEditPartOperation epop = (CreateRootEditPartOperation)operation;
			if (epop.getView() instanceof Diagram && epop.getView().getType().equals(PackageEditPart.MODEL_ID))
				return true;
		}
		return false;
	}

	@Override
	public IGraphicalEditPart createGraphicEditPart(View view) {
		return super.createGraphicEditPart(view);
	}

	@Override
	public RootEditPart createRootEditPart(Diagram diagram) {
		return new DebugDiagramRootEditPart(diagram.getMeasurementUnit());
	}

}
