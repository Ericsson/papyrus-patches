/*****************************************************************************
 * Copyright (c) 2014, 2019 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Benoit Maggi (CEA LIST) benoit.maggi@cea.fr - Initial API and implementation
 *  Ansgar Radermacher (CEA LIST) ansgar.radermacher@cea.fr - bug 541686 (duplicated replationships)
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.clipboard.IClipboardAdditionalData;
import org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.InsertDiagramCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.utils.DiagramUtils;


/**
 * Offer a copy/paste strategy for diagram in model explorer.
 */
public class DiagramPasteStrategy extends AbstractPasteStrategy implements IPasteStrategy {

	/** key to store diagrams with no owner */
	protected static final String DIAGRAM_WITH_NO_OWNER = "DIAGRAM_WITH_NO_OWNER"; //$NON-NLS-1$

	/** The instance. */
	private static IPasteStrategy instance = new DiagramPasteStrategy();

	/**
	 * Gets the single instance of DiagramPasteStrategy.
	 *
	 * @return single instance of DiagramPasteStrategy
	 */
	public static IPasteStrategy getInstance() {
		return instance;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Diagram Strategy"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getID()
	 */
	@Override
	public String getID() {
		return Activator.ID + ".DiagramStrategy"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Copy Diagrams in model explorer"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#getSemanticCommand(org.eclipse.emf.edit.domain.EditingDomain,
	 * org.eclipse.emf.ecore.EObject, org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public org.eclipse.emf.common.command.Command getSemanticCommand(final EditingDomain domain, final EObject targetOwner, PapyrusClipboard<Object> papyrusClipboard) {
		CompoundCommand compoundCommand = new CompoundCommand("Copy all Diagrams"); //$NON-NLS-1$

		Map internalClipboardToTargetCopy = papyrusClipboard.getInternalClipboardToTargetCopy();
		Map<Object, ?> additionalDataMap = papyrusClipboard.getAdditionalDataForStrategy(getID());
		if (additionalDataMap != null) {
			Object additionalData = additionalDataMap.get(DIAGRAM_WITH_NO_OWNER);
			if (additionalData instanceof DiagramClipboardAdditionalData) {
				DiagramClipboardAdditionalData diagramClipboardAdditionnalData = (DiagramClipboardAdditionalData) additionalData;
				Collection<Diagram> duplicateDiagrams = diagramClipboardAdditionnalData.getDuplicatedDiagrams(internalClipboardToTargetCopy);
				for (final Diagram diagram : duplicateDiagrams) {
					org.eclipse.emf.common.command.Command command = new InsertDiagramCommand((TransactionalEditingDomain) domain, "Insert a diagram with no source owner", diagram, targetOwner); //$NON-NLS-1$
					compoundCommand.append(command);
				}
			}

			for (Iterator<Object> iterator = papyrusClipboard.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				// get target Element
				EObject target = papyrusClipboard.getTragetCopyFromInternalClipboardCopy(object);
				if (target != null && target instanceof EObject) {
					// get affiliate additional data
					additionalData = additionalDataMap.get(object);
					if (additionalData instanceof DiagramClipboardAdditionalData) {
						DiagramClipboardAdditionalData diagramClipboardAdditionalData = (DiagramClipboardAdditionalData) additionalData;
						Collection<Diagram> diagrams = diagramClipboardAdditionalData.getDuplicatedDiagrams(internalClipboardToTargetCopy);
						// duplicate diagrams
						for (final Diagram diagram : diagrams) {
							org.eclipse.emf.common.command.Command command = new InsertDiagramCommand((TransactionalEditingDomain) domain, "InsertDiagramCommand", diagram, target); //$NON-NLS-1$
							compoundCommand.append(command);
						}
					}
				}
			}
		}

		// An empty compound Command can't be executed
		if (compoundCommand.getCommandList().isEmpty()) {
			return null;
		}
		return compoundCommand;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#dependsOn()
	 */
	@Override
	public IPasteStrategy dependsOn() {
		return DefaultPasteStrategy.getInstance();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.papyrus.infra.gmfdiag.common.strategy.paste.IPasteStrategy#prepare(org.eclipse.papyrus.infra.core.clipboard.PapyrusClipboard)
	 */
	@Override
	public void prepare(PapyrusClipboard<Object> papyrusClipboard, Collection<EObject> selection) {
		Map<Object, IClipboardAdditionalData> mapCopyToClipboardAdditionalData = new HashMap<>();
		Map sourceToInternalClipboard = papyrusClipboard.getSourceToInternalClipboard();
		List<Diagram> extractSelectedWithoutOwner = extractDiagramWithoutOwner(selection);
		if (extractSelectedWithoutOwner != null && !extractSelectedWithoutOwner.isEmpty()) {
			DiagramClipboardAdditionalData diagramAdditionnalData = new DiagramClipboardAdditionalData(extractSelectedWithoutOwner, sourceToInternalClipboard);
			mapCopyToClipboardAdditionalData.put(DIAGRAM_WITH_NO_OWNER, diagramAdditionnalData);
		}

		for (Iterator<EObject> iterator = papyrusClipboard.iterateOnSource(); iterator.hasNext();) {
			EObject eObjectSource = iterator.next();
			ResourceSet resourceSet = eObjectSource.eResource().getResourceSet();
			List<Diagram> associatedDiagrams = DiagramUtils.getAssociatedDiagrams(eObjectSource, resourceSet);
			if (associatedDiagrams != null) {
				DiagramClipboardAdditionalData diagramAdditionnalData = new DiagramClipboardAdditionalData(associatedDiagrams, sourceToInternalClipboard);
				Object copy = papyrusClipboard.getCopyFromSource(eObjectSource);
				mapCopyToClipboardAdditionalData.put(copy, diagramAdditionnalData);
			}

		}
		papyrusClipboard.pushAdditionalData(getID(), mapCopyToClipboardAdditionalData);
	}


	/**
	 * Extract Diagram which owner is not in the selection
	 *
	 * @param selection
	 * @return
	 */
	protected List<Diagram> extractDiagramWithoutOwner(Collection<EObject> selection) {
		List<Diagram> diagramWithoutOwnerInSelection = new ArrayList<>();
		if (selection != null) {
			for (EObject eObject : selection) {
				if (eObject instanceof Diagram) {
					Diagram diagram = (Diagram) eObject;
					EObject element = diagram.getElement();
					if (!selection.contains(element)) {
						diagramWithoutOwnerInSelection.add(diagram);
					}
				}
			}
		}
		return diagramWithoutOwnerInSelection;
	}


	protected class DiagramClipboardAdditionalData implements IClipboardAdditionalData {

		/** The diagrams. */
		protected Collection<Diagram> diagrams;

		/**
		 * @param diagramCopier
		 * @param diagrams
		 */
		public DiagramClipboardAdditionalData(Collection<Diagram> diagrams, Map<? extends EObject, ? extends EObject> alreadyCopied) {
			this.diagrams = duplicateDiagrams(diagrams, alreadyCopied);
		}

		/**
		 * @return
		 */
		public Collection<Diagram> getDuplicatedDiagrams(Map<? extends EObject, ? extends EObject> alreadyCopied) {
			return duplicateDiagrams(this.diagrams, alreadyCopied);
		}

		/**
		 * @param diagrams
		 *            to duplicate
		 * @param alreadyCopied
		 * @return duplicated diagrams
		 */
		protected Collection<Diagram> duplicateDiagrams(Collection<Diagram> diagrams, Map<? extends EObject, ? extends EObject> alreadyCopied) {
			Collection<Diagram> duplicatedDiagrams = new ArrayList<>();
			EcoreUtil.Copier copier = new EcoreUtil.Copier();
			copier.putAll(alreadyCopied);
			for (Diagram diagram : diagrams) {
				copier.copy(diagram);
				// do not copy references (already done in default strategy), avoid duplicates
				EObject copy = copier.get(diagram);
				if (copy instanceof Diagram) {
					duplicatedDiagrams.add((Diagram) copy);
				}
			}
			return duplicatedDiagrams;
		}
	}

}
