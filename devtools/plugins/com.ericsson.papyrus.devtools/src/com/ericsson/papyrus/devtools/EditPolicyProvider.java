package com.ericsson.papyrus.devtools;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.core.service.IProviderChangeListener;
import org.eclipse.gmf.runtime.diagram.ui.services.editpolicy.IEditPolicyProvider;

import com.ericsson.papyrus.devtools.views.RequestsLogService;

public class EditPolicyProvider implements IEditPolicyProvider {

	@Override
	public void addProviderChangeListener(IProviderChangeListener listener) {
	}

	@Override
	public boolean provides(IOperation operation) {
		return true;
	}

	@Override
	public void removeProviderChangeListener(IProviderChangeListener listener) {
	}

	@Override
	public void createEditPolicies(EditPart editPart) {
		editPart.installEditPolicy("RequestLogger", new AbstractEditPolicy() {

			/* (non-Javadoc)
			 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#understandsRequest(org.eclipse.gef.Request)
			 */
			@Override
			public boolean understandsRequest(Request req) {
				return true;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#getCommand(org.eclipse.gef.Request)
			 */
			@Override
			public Command getCommand(Request request) {
				if (!RequestsLogService.getInstance().isEnabled())
					return null;
				
				if (requestingCommand == 0) {
					requestingCommand++;
					Command cmd = null;
					try {
						cmd = getHost().getCommand(request);
					} finally {
						requestingCommand --;
					}
					RequestsLogService.getInstance().log(getHost(), request, cmd);
					
				}
				return null;
			}
			
			private int requestingCommand;
		});
	}

}
