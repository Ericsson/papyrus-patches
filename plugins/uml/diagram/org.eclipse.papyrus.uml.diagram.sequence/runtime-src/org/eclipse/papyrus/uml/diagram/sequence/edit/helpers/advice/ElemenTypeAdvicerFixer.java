/*****************************************************************************
 * Copyright (c) 2018 CEA LIST and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   CEA LIST - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.edit.helpers.advice;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gmf.runtime.emf.type.core.AdviceBindingAddedEvent;
import org.eclipse.gmf.runtime.emf.type.core.AdviceBindingInheritance;
import org.eclipse.gmf.runtime.emf.type.core.AdviceBindingRemovedEvent;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeAddedEvent;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRemovedEvent;
import org.eclipse.gmf.runtime.emf.type.core.IAdviceBindingDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IContainerDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IElementMatcher;
import org.eclipse.gmf.runtime.emf.type.core.IElementTypeRegistryListener2;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.IEditHelperAdvice;

/**
 * @author ETXACAM
 *
 */
public class ElemenTypeAdvicerFixer implements IElementTypeRegistryListener2 {
	public static final ElemenTypeAdvicerFixer INSTANCE = new ElemenTypeAdvicerFixer();

	private Map<String, IAdviceBindingDescriptor> adviceToDeregister = new HashMap<>();

	public void start() {
		org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry.getInstance().addElementTypeRegistryListener(this);
		adviceToDeregister.put("org.eclipse.papyrus.uml.advice.Message",
				new AdviceBindingDescriptor("org.eclipse.papyrus.uml.advice.Message", "org.eclipse.papyrus.uml.Message"));
	}


	@Override
	public void elementTypeAdded(ElementTypeAddedEvent elementTypeAddedEvent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void elementTypeRemoved(ElementTypeRemovedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adviceBindingRemoved(AdviceBindingRemovedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void adviceBindingAdded(AdviceBindingAddedEvent event) {
		if (adviceToDeregister.containsKey(event.getAdviceBindingId())) {
			ElementTypeRegistry.getInstance().deregisterAdvice(adviceToDeregister.get(event.getAdviceBindingId()));
		}
	}

	public static boolean deregisterAdvice(String adviseId, String elementTypeId) {
		return ElementTypeRegistry.getInstance().deregisterAdvice(new AdviceBindingDescriptor(adviseId, elementTypeId));
	}

	private static class AdviceBindingDescriptor implements IAdviceBindingDescriptor {
		public AdviceBindingDescriptor(String id, String typeId) {
			super();
			this.id = id;
			this.typeId = typeId;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getTypeId() {
			return typeId;
		}

		@Override
		public IElementMatcher getMatcher() {
			return null;
		}

		@Override
		public IContainerDescriptor getContainerDescriptor() {
			return null;
		}

		@Override
		public IEditHelperAdvice getEditHelperAdvice() {
			return null;
		}

		@Override
		public AdviceBindingInheritance getInheritance() {
			return null;
		}

		private String id;
		private String typeId;
	}
}
