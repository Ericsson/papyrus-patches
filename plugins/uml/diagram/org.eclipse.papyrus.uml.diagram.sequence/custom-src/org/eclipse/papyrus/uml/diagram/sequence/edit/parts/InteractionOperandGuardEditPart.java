/*****************************************************************************
 * Copyright (c) 2013, 2018 CEA, Christian W. Damus, and others
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Soyatec - Initial API and implementation
 *   Nicolas FAUVERGUE (CEA LIST) nicolas.fauvergue@cea.fr - Bug 533689
 *   Christian W. Damus - bug 533699
 *
 *****************************************************************************/
package org.eclipse.papyrus.uml.diagram.sequence.edit.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserOptions;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ComponentEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.NonResizableEditPolicyEx;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.SemanticEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramColorRegistry;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.diagram.ui.tools.TextDirectEditManager;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.IMapMode;
import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ISemanticParser;
import org.eclipse.gmf.runtime.gef.ui.internal.parts.TextCellEditorEx;
import org.eclipse.gmf.runtime.notation.FontStyle;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.window.Window;
import org.eclipse.papyrus.commands.wrappers.EMFtoGEFCommandWrapper;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.configuration.IAdvancedEditorConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.configuration.ICustomDirectEditorConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.configuration.IDirectEditorConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.configuration.IPopupEditorConfiguration;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.ui.ExtendedDirectEditionDialog;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.ui.ILabelEditorDialog;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.ui.IPopupEditorHelper;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.utils.DirectEditorsUtil;
import org.eclipse.papyrus.infra.gmfdiag.extensionpoints.editors.utils.IDirectEditorsIds;
import org.eclipse.papyrus.infra.emf.commands.CreateEAnnotationCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.editpolicies.IMaskManagedLabelEditPolicy;
import org.eclipse.papyrus.infra.gmfdiag.common.figure.node.PapyrusWrappingLabel;
import org.eclipse.papyrus.uml.diagram.common.directedit.MultilineLabelDirectEditManager;
import org.eclipse.papyrus.uml.diagram.common.editpolicies.IDirectEdition;
import org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure;
import org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure;
import org.eclipse.papyrus.uml.diagram.sequence.edit.policies.UMLTextSelectionEditPolicy;
import org.eclipse.papyrus.uml.diagram.sequence.figures.InteractionOperandFigure;
import org.eclipse.papyrus.uml.diagram.sequence.locator.TextCellEditorLocator;
import org.eclipse.papyrus.uml.diagram.sequence.parsers.MessageFormatParser;
import org.eclipse.papyrus.uml.diagram.sequence.part.UMLDiagramEditorPlugin;
import org.eclipse.papyrus.uml.diagram.sequence.util.ElementIconUtil;
import org.eclipse.papyrus.uml.diagram.sequence.util.InteractionOperandModelElementFactory;
import org.eclipse.papyrus.uml.diagram.sequence.util.LoopOperatorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.InteractionConstraint;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;

/**
 * this class is used to edit a guard of an interaction operand
 */
public class InteractionOperandGuardEditPart extends ShapeEditPart implements ITextAwareEditPart {

	public static final String GUARD_TYPE = "InteractionOperand_Guard";

	private DirectEditManager manager;

	private IParser parser;

	private List<?> parserElements;

	private String defaultText;

	protected int directEditionMode = IDirectEdition.UNDEFINED_DIRECT_EDITOR;

	protected IDirectEditorConfiguration configuration;

	/**
	 * Constructor.
	 *
	 * @param view
	 */
	public InteractionOperandGuardEditPart(View view) {
		super(view);
	}



	@Override
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new LabelDirectEditPolicy());
		// installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new UMLTextSelectionEditPolicy());
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, createPrimaryDragEditPolicy());
		// Do NOT allow delete guard, it also disable the Delete menu.
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new SemanticEditPolicy() {

			@Override
			protected Command getSemanticCommand(IEditCommandRequest request) {
				if (request instanceof DestroyElementRequest) {
					return org.eclipse.gef.commands.UnexecutableCommand.INSTANCE;
				}
				return super.getSemanticCommand(request);
			}
		});
		// Just hide the guard for a operand, it also overrite the Hide menu action.
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {

			@Override
			protected boolean shouldDeleteSemantic() {
				return false;
			}

			@Override
			protected Command createDeleteViewCommand(GroupRequest deleteRequest) {
				View source = ((InteractionOperandEditPart) getParent()).getNotationView();
				CreateEAnnotationCommand command = new CreateEAnnotationCommand(getEditingDomain(), source, InteractionOperandModelElementFactory.GUARD_VISIBILITY_KEY) {

					@Override
					protected void doExecute() {
						EAnnotation annotation = createEAnnotation();
						replaceEannotation(annotation, getObject());
						replaceEntry(annotation, InteractionOperandModelElementFactory.GUARD_VISIBILITY_KEY, Boolean.FALSE.toString());
					}
				};
				return new EMFtoGEFCommandWrapper(command);
			}
		});
	}

	protected EditPolicy createPrimaryDragEditPolicy() {
		return new NonResizableEditPolicyEx() {

			@Override
			protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
				IFigure feedback = getDragSourceFeedbackFigure();
				PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
				getHostFigure().translateToAbsolute(rect);
				Rectangle initialAbsBounds = rect.getCopy();
				rect.translate(request.getMoveDelta());
				rect.resize(request.getSizeDelta());
				IFigure f = getHostFigure();
				Dimension min = f.getMinimumSize().getCopy();
				Dimension max = f.getMaximumSize().getCopy();
				IMapMode mmode = MapModeUtil.getMapMode(f);
				min.height = mmode.LPtoDP(min.height);
				min.width = mmode.LPtoDP(min.width);
				max.height = mmode.LPtoDP(max.height);
				max.width = mmode.LPtoDP(max.width);
				if (min.width > rect.width) {
					rect.width = min.width;
				} else if (max.width < rect.width) {
					rect.width = max.width;
				}
				if (min.height > rect.height) {
					rect.height = min.height;
				} else if (max.height < rect.height) {
					rect.height = max.height;
				}
				// parent bounds
				IFigure parentFigure = ((GraphicalEditPart) getParent()).getFigure();
				Rectangle parentBounds = parentFigure.getBounds().getCopy();
				parentFigure.translateToAbsolute(parentBounds);
				if (rect.x < parentBounds.x) {
					rect.x = parentBounds.x;
				}
				if (rect.right() > parentBounds.right()) {
					rect.x = (parentBounds.right() - rect.width);
				}
				if (rect.y < parentBounds.y) {
					rect.y = parentBounds.y;
				}
				if (rect.bottom() > parentBounds.bottom()) {
					rect.y = parentBounds.bottom() - rect.height;
				}
				feedback.translateToRelative(rect);
				feedback.setBounds(rect.getCopy());
				// Fixed bug about moving guard.
				feedback.translateToAbsolute(rect);
				request.setMoveDelta(new Point(rect.x - initialAbsBounds.x, rect.y - initialAbsBounds.y));
			}

			/**
			 * Disable moving of the interaction operand guard, per
			 * <a href="http://eclip.se/533699">bug 533699</a>.
			 *
			 * @return {@code false}, always
			 * 
			 * @see <a href="http://eclip.se/533699">bug 533699</a>
			 */
			@Override
			public boolean isDragAllowed() {
				return false;
			}
		};
	}

	protected String getLabelTextHelper(IFigure figure) {
		if (figure instanceof WrappingLabel) {
			return ((WrappingLabel) figure).getText();
		} else if (figure instanceof ILabelFigure) {
			return ((ILabelFigure) figure).getText();
		} else {
			return ((Label) figure).getText();
		}
	}

	protected void setLabelTextHelper(IFigure figure, String text) {
		if (figure instanceof WrappingLabel) {
			((WrappingLabel) figure).setText(text);
		} else if (figure instanceof ILabelFigure) {
			((ILabelFigure) figure).setText(text);
		} else {
			((Label) figure).setText(text);
		}
	}

	protected Image getLabelIconHelper(IFigure figure) {
		if (figure instanceof WrappingLabel) {
			return ((WrappingLabel) figure).getIcon();
		} else if (figure instanceof ILabelFigure) {
			return ((ILabelFigure) figure).getIcon();
		} else {
			return ((Label) figure).getIcon();
		}
	}

	protected void setLabelIconHelper(IFigure figure, Image icon) {
		if (figure instanceof WrappingLabel) {
			((WrappingLabel) figure).setIcon(icon);
		} else if (figure instanceof ILabelFigure) {
			((ILabelFigure) figure).setIcon(icon);
		} else {
			((Label) figure).setIcon(icon);
		}
	}

	public void setLabel(WrappingLabel figure) {
		unregisterVisuals();
		setFigure(figure);
		defaultText = getLabelTextHelper(figure);
		registerVisuals();
		refreshVisuals();
		figure.setBackgroundColor(ColorConstants.red);
	}

	@Override
	protected List getModelChildren() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public IGraphicalEditPart getChildBySemanticHint(String semanticHint) {
		return null;
	}

	/**
	 * return the interactionoperand
	 *
	 * @return
	 */
	protected EObject getParserElement() {
		if (this.getParent() instanceof InteractionOperandEditPart) {
			InteractionOperandEditPart interactionOperandEditPart = (InteractionOperandEditPart) this.getParent();
			return interactionOperandEditPart.resolveSemanticElement();
		}
		return null;
		// return getInteractionOperand(resolveSemanticElement());
	}

	protected Image getLabelIcon() {
		return ElementIconUtil.getLabelIcon((org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) getParent());
	}

	protected String getLabelText() {
		String text = null;
		EObject parserElement = getParserElement();
		if (parserElement != null && getParser() != null) {
			text = getParser().getPrintString(new EObjectAdapter(parserElement), getParserOptions().intValue());
		}
		if (text == null || text.length() == 0) {
			text = defaultText;
		}
		return text;
	}

	@Override
	public void setLabelText(String text) {
		setLabelTextHelper(getFigure(), text);
		Object pdEditPolicy = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pdEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) pdEditPolicy).refreshFeedback();
		}
		Object sfEditPolicy = getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		if (sfEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) sfEditPolicy).refreshFeedback();
		}
	}

	@Override
	public String getEditText() {
		if (getParserElement() == null || getParser() == null) {
			return ""; //$NON-NLS-1$
		}
		return getParser().getEditString(new EObjectAdapter(getParserElement()), getParserOptions().intValue());
	}

	protected boolean isEditable() {
		return getParser() != null;
	}

	@Override
	public ICellEditorValidator getEditTextValidator() {
		return new ICellEditorValidator() {

			@Override
			public String isValid(final Object value) {
				if (value instanceof String) {
					final EObject element = getParserElement();
					final IParser parser = getParser();
					try {
						IParserEditStatus valid = (IParserEditStatus) getEditingDomain().runExclusive(new RunnableWithResult.Impl() {

							@Override
							public void run() {
								setResult(parser.isValidEditString(new EObjectAdapter(element), (String) value));
							}
						});
						return valid.getCode() == IParserEditStatus.EDITABLE ? null : valid.getMessage();
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				// shouldn't get here
				return null;
			}
		};
	}

	@Override
	public IContentAssistProcessor getCompletionProcessor() {
		if (getParserElement() == null || getParser() == null) {
			return null;
		}
		return getParser().getCompletionProcessor(new EObjectAdapter(getParserElement()));
	}

	@Override
	public ParserOptions getParserOptions() {
		return ParserOptions.NONE;
	}

	@Override
	public IParser getParser() {
		if (parser == null) {
			parser = new GuardConditionParser();
		}
		return parser;
	}

	protected DirectEditManager getManager() {
		if (manager == null) {
			WrappingLabel primaryLabel = getPrimaryFigure();
			CellEditorLocator locator = new TextCellEditorLocator(primaryLabel);
			setManager(new MultilineLabelDirectEditManager(this, TextCellEditorEx.class, locator));
		}
		return manager;
	}

	public WrappingLabel getPrimaryFigure() {
		return (WrappingLabel) getFigure();
	}

	protected void setManager(DirectEditManager manager) {
		this.manager = manager;
	}

	protected void performDirectEdit() {
		getManager().show();
	}

	protected void performDirectEdit(Point eventLocation) {
		if (getManager() instanceof TextDirectEditManager) {
			((TextDirectEditManager) getManager()).show(eventLocation.getSWTPoint());
		}
	}

	protected void performDirectEdit(char initialCharacter) {
		if (getManager() instanceof TextDirectEditManager) {
			((TextDirectEditManager) getManager()).show(initialCharacter);
		} else {
			performDirectEdit();
		}
	}

	@Override
	protected void performDirectEditRequest(Request request) {
		final Request theRequest = request;
		if (IDirectEdition.UNDEFINED_DIRECT_EDITOR == directEditionMode) {
			directEditionMode = getDirectEditionType();
		}
		switch (directEditionMode) {
		case IDirectEdition.NO_DIRECT_EDITION:
			// no direct edition mode => does nothing
			return;
		case IDirectEdition.EXTENDED_DIRECT_EDITOR:
			updateExtendedEditorConfiguration();
			if (configuration == null || configuration.getLanguage() == null) {
				// Create default edit manager
				setManager(new MultilineLabelDirectEditManager(this,
						MultilineLabelDirectEditManager.getTextCellEditorClass(this),
						UMLEditPartFactory.getTextCellEditorLocator(this)));
				performDefaultDirectEditorEdit(theRequest);
			} else {
				configuration.preEditAction(resolveSemanticElement());
				Dialog dialog = null;
				if (configuration instanceof ICustomDirectEditorConfiguration) {
					setManager(((ICustomDirectEditorConfiguration) configuration).createDirectEditManager(this));
					initializeDirectEditManager(theRequest);
					return;
				} else if (configuration instanceof IPopupEditorConfiguration) {
					IPopupEditorHelper helper = ((IPopupEditorConfiguration) configuration)
							.createPopupEditorHelper(this);
					helper.showEditor();
					return;
				} else if (configuration instanceof IAdvancedEditorConfiguration) {
					dialog = ((IAdvancedEditorConfiguration) configuration).createDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), resolveSemanticElement(),
							configuration.getTextToEdit(resolveSemanticElement()));
				} else if (configuration instanceof IDirectEditorConfiguration) {
					dialog = new ExtendedDirectEditionDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), resolveSemanticElement(),
							configuration.getTextToEdit(resolveSemanticElement()), configuration);
				} else {
					return;
				}
				final Dialog finalDialog = dialog;

				if (Window.OK == dialog.open()) {
					TransactionalEditingDomain domain = getEditingDomain();
					RecordingCommand command = new RecordingCommand(domain, "Edit Label") {

						@Override
						protected void doExecute() {
							configuration.postEditAction(resolveSemanticElement(),
									((ILabelEditorDialog) finalDialog).getValue());

						}
					};
					domain.getCommandStack().execute(command);
				}
			}
			break;
		case IDirectEdition.DEFAULT_DIRECT_EDITOR:
			// initialize the direct edit manager
			try {
				getEditingDomain().runExclusive(new Runnable() {

					@Override
					public void run() {
						if (isActive() && isEditable()) {
							if (theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR) instanceof Character) {
								Character initialChar = (Character) theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR);
								performDirectEdit(initialChar.charValue());
							} else if ((theRequest instanceof DirectEditRequest) && (getEditText().equals(getLabelText()))) {
								DirectEditRequest editRequest = (DirectEditRequest) theRequest;
								performDirectEdit(editRequest.getLocation());
							} else {
								performDirectEdit();
							}
						}
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	/**
	 * This allows to initialize the direct edit manager for the edit.
	 * 
	 * @param request
	 *            The called request.
	 * @since 5.0
	 */
	protected void initializeDirectEditManager(final Request request) {
		// initialize the direct edit manager
		try {
			getEditingDomain().runExclusive(new Runnable() {
				@Override
				public void run() {
					if (isActive() && isEditable()) {
						if (request.getExtendedData()
								.get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR) instanceof Character) {
							Character initialChar = (Character) request.getExtendedData()
									.get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR);
							performDirectEdit(initialChar.charValue());
						} else {
							performDirectEdit();
						}
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshLabel();
		refreshFont();
		refreshFontColor();
		refreshUnderline();
		refreshStrikeThrough();



	}

	/**
	 * @see org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 *
	 * @param notification
	 */
	@Override
	public void notifyChanged(Notification notification) {

		super.notifyChanged(notification);
	}

	@Override
	public void refreshBounds() {
		int width = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Width())).intValue();
		int height = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getSize_Height())).intValue();
		Dimension size = new Dimension(width, height);
		int x = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_X())).intValue();
		int y = ((Integer) getStructuralFeatureValue(NotationPackage.eINSTANCE.getLocation_Y())).intValue();
		Point loc = new Point(x, y);
		// IFigure figure = getFigure();
		// Dimension size = figure.getPreferredSize(-1, -1);
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure, new Rectangle(loc, size));
	}

	protected void refreshLabel() {
		EditPolicy maskLabelPolicy = getEditPolicy(IMaskManagedLabelEditPolicy.MASK_MANAGED_LABEL_EDIT_POLICY);
		if (maskLabelPolicy == null) {
			setLabelTextHelper(getFigure(), getLabelText());
			setLabelIconHelper(getFigure(), getLabelIcon());
		}
		Object pdEditPolicy = getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
		if (pdEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) pdEditPolicy).refreshFeedback();
		}
		Object sfEditPolicy = getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
		if (sfEditPolicy instanceof UMLTextSelectionEditPolicy) {
			((UMLTextSelectionEditPolicy) sfEditPolicy).refreshFeedback();
		}

	}

	protected void refreshUnderline() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null && getFigure() instanceof WrappingLabel) {
			((WrappingLabel) getFigure()).setTextUnderline(style.isUnderline());
		}
		if (resolveSemanticElement() instanceof Feature) {
			if (((Feature) resolveSemanticElement()).isStatic()) {
				((WrappingLabel) getFigure()).setTextUnderline(true);
			} else {
				((WrappingLabel) getFigure()).setTextUnderline(false);
			}
		}
	}

	protected void refreshStrikeThrough() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null && getFigure() instanceof WrappingLabel) {
			((WrappingLabel) getFigure()).setTextStrikeThrough(style.isStrikeThrough());
		}
	}

	@Override
	protected void refreshFont() {
		FontStyle style = (FontStyle) getFontStyleOwnerView().getStyle(NotationPackage.eINSTANCE.getFontStyle());
		if (style != null) {
			FontData fontData = new FontData(style.getFontName(), style.getFontHeight(), (style.isBold() ? SWT.BOLD : SWT.NORMAL) | (style.isItalic() ? SWT.ITALIC : SWT.NORMAL));
			setFont(fontData);
		}
	}

	@Override
	protected void setFontColor(Color color) {
		getFigure().setForegroundColor(color);
	}

	@Override
	protected void addSemanticListeners() {

		super.addSemanticListeners();
		if (getParser() instanceof ISemanticParser) {
			EObject element = resolveSemanticElement();
			parserElements = ((ISemanticParser) getParser()).getSemanticElementsBeingParsed(element);
			for (int i = 0; i < parserElements.size(); i++) {
				addListenerFilter("SemanticModel" + i, this, (EObject) parserElements.get(i)); //$NON-NLS-1$
			}
		} else {
			super.addSemanticListeners();
		}
	}

	@Override
	protected void removeSemanticListeners() {
		super.resolveSemanticElement();
		if (parserElements != null) {
			for (int i = 0; i < parserElements.size(); i++) {
				removeListenerFilter("SemanticModel" + i); //$NON-NLS-1$
			}
		} else {
			super.removeSemanticListeners();
		}
	}

	@Override
	protected AccessibleEditPart getAccessibleEditPart() {
		if (accessibleEP == null) {
			accessibleEP = new AccessibleGraphicalEditPart() {

				@Override
				public void getName(AccessibleEvent e) {
					e.result = getLabelTextHelper(getFigure());
				}
			};
		}
		return accessibleEP;
	}

	private View getFontStyleOwnerView() {
		return (View) getModel();
	}

	public int getDirectEditionType() {
		if (checkExtendedEditor()) {
			initExtendedEditorConfiguration();
			return IDirectEdition.EXTENDED_DIRECT_EDITOR;
		}
		if (checkDefaultEdition()) {
			return IDirectEdition.DEFAULT_DIRECT_EDITOR;
		}
		// not a named element. no specific editor => do nothing
		return IDirectEdition.NO_DIRECT_EDITION;
	}

	protected boolean checkExtendedEditor() {
		if (resolveSemanticElement() != null) {
			return DirectEditorsUtil.hasSpecificEditorConfiguration(resolveSemanticElement(), this);
		}
		return false;
	}

	protected boolean checkDefaultEdition() {
		return (getParser() != null);
	}

	protected void initExtendedEditorConfiguration() {
		if (configuration == null) {
			final String languagePreferred = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getString(IDirectEditorsIds.EDITOR_FOR_ELEMENT + resolveSemanticElement().eClass().getInstanceClassName());
			if (languagePreferred != null && !languagePreferred.equals("")) {
				configuration = DirectEditorsUtil.findEditorConfiguration(languagePreferred, resolveSemanticElement(), this);
			} else {
				configuration = DirectEditorsUtil.findEditorConfiguration(IDirectEditorsIds.UML_LANGUAGE, resolveSemanticElement(), this);
			}
		}
	}

	protected void updateExtendedEditorConfiguration() {
		String languagePreferred = UMLDiagramEditorPlugin.getInstance().getPreferenceStore().getString(IDirectEditorsIds.EDITOR_FOR_ELEMENT + resolveSemanticElement().eClass().getInstanceClassName());
		if (languagePreferred != null && !languagePreferred.equals("") && languagePreferred != configuration.getLanguage()) {
			configuration = DirectEditorsUtil.findEditorConfiguration(languagePreferred, resolveSemanticElement(), this);
		} else if (IDirectEditorsIds.SIMPLE_DIRECT_EDITOR.equals(languagePreferred)) {
			configuration = null;
		}
	}

	protected void performDefaultDirectEditorEdit(final Request theRequest) {
		// initialize the direct edit manager
		try {
			getEditingDomain().runExclusive(new Runnable() {

				@Override
				public void run() {
					if (isActive() && isEditable()) {
						if (theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR) instanceof Character) {
							Character initialChar = (Character) theRequest.getExtendedData().get(RequestConstants.REQ_DIRECTEDIT_EXTENDEDDATA_INITIAL_CHAR);
							performDirectEdit(initialChar.charValue());
						} else if ((theRequest instanceof DirectEditRequest) && (getEditText().equals(getLabelText()))) {
							DirectEditRequest editRequest = (DirectEditRequest) theRequest;
							performDirectEdit(editRequest.getLocation());
						} else {
							performDirectEdit();
						}
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void handleNotificationEvent(Notification event) {
		Object feature = event.getFeature();
		if (NotationPackage.eINSTANCE.getFontStyle_FontColor().equals(feature)) {
			Integer c = (Integer) event.getNewValue();
			setFontColor(DiagramColorRegistry.getInstance().getColor(c));
		} else if (NotationPackage.eINSTANCE.getFontStyle_Underline().equals(feature)) {
			refreshUnderline();
		} else if (NotationPackage.eINSTANCE.getFontStyle_StrikeThrough().equals(feature)) {
			refreshStrikeThrough();
		} else if (NotationPackage.eINSTANCE.getFontStyle_FontHeight().equals(feature) || NotationPackage.eINSTANCE.getFontStyle_FontName().equals(feature) || NotationPackage.eINSTANCE.getFontStyle_Bold().equals(feature)
				|| NotationPackage.eINSTANCE.getFontStyle_Italic().equals(feature)) {
			refreshFont();
		} else if (ElementIconUtil.isIconNotification(event)) {
			refreshLabel();
		} else {
			refreshLabel();
		}
		refresh();
		super.handleNotificationEvent(event);
	}

	@Override
	protected IFigure createFigure() {
		WrappingLabel label = null;
		if (getParent() instanceof InteractionOperandEditPart) {
			InteractionOperandFigure fig = ((InteractionOperandEditPart) getParent()).getPrimaryShape();
			label = fig.getInteractionConstraintLabel();
		}
		// GuardFigure label = new GuardFigure();
		// WrappingLabel label = new WrappingLabel();
		defaultText = getLabelTextHelper(label);
		return label;
	}

	private static String getGuardLabelText(InteractionOperand interactionOperand, boolean edit) {
		if (interactionOperand == null) {
			return "";
		}
		Element owner = interactionOperand.getOwner();
		while (owner != null && !(owner instanceof CombinedFragment)) {
			owner = owner.getOwner();
		}
		CombinedFragment enclosingCF = (CombinedFragment) owner;
		if (enclosingCF == null) {
			return "";
		}
		InteractionOperatorKind cfOperator = enclosingCF.getInteractionOperator();
		InteractionConstraint guard = interactionOperand.getGuard();
		String specValue = null;
		if (guard != null) {
			ValueSpecification specification = guard.getSpecification();
			if (specification != null) {
				try {
					specValue = specification.stringValue();
				} catch (Exception e) {
				}
			}
		}
		StringBuilder sb = new StringBuilder("");
		if (InteractionOperatorKind.LOOP_LITERAL.equals(cfOperator)) {
			String condition = LoopOperatorUtil.getLoopCondition(guard);
			if (condition != null) {
				sb.append(condition);
			}
		}
		if (specValue == null) {
			EList<InteractionOperand> operands = enclosingCF.getOperands();
			if (InteractionOperatorKind.ALT_LITERAL.equals(cfOperator) && interactionOperand.equals(operands.get(operands.size() - 1))) {
				specValue = "else";
			}
		}
		if (specValue != null) {
			if (!edit) {
				sb.append('[');
			}
			sb.append(specValue);
			if (!edit) {
				sb.append(']');
			}
		}
		String text = sb.toString();
		return text;
	}

	@Deprecated
	protected static InteractionOperand getInteractionOperand(Object element) {
		InteractionOperand operand = null;
		if (element instanceof InteractionConstraint) {
			EObject owner = ((InteractionConstraint) element).eContainer();
			if (owner instanceof InteractionOperand) {
				operand = (InteractionOperand) owner;
			}
		} else if (element instanceof InteractionOperand) {
			operand = (InteractionOperand) element;
		}
		return operand;
	}

	private static class GuardConditionParser extends MessageFormatParser implements ISemanticParser {

		public GuardConditionParser() {
			super(new EAttribute[] { UMLPackage.eINSTANCE.getLiteralInteger_Value(), UMLPackage.eINSTANCE.getLiteralString_Value() });
		}

		@Override
		public List getSemanticElementsBeingParsed(EObject element) {
			List<Element> semanticElementsBeingParsed = new ArrayList<>();
			if (element instanceof InteractionConstraint) {
				InteractionConstraint op = (InteractionConstraint) element;
				semanticElementsBeingParsed.add(op);
			}
			return semanticElementsBeingParsed;
		}

		@Override
		public boolean areSemanticElementsAffected(EObject listener, Object notification) {
			EStructuralFeature feature = getEStructuralFeature(notification);
			return isValidFeature(feature);
		}

		@Override
		public boolean isAffectingEvent(Object event, int flags) {
			EStructuralFeature feature = getEStructuralFeature(event);
			return isValidFeature(feature);
		}

		@Override
		public String getPrintString(IAdaptable element, int flags) {
			EObject operand = element.getAdapter(EObject.class);
			// InteractionOperand operand = getInteractionOperand(adapter);
			if (operand instanceof InteractionOperand) {
				return getGuardLabelText((InteractionOperand) operand, false);
			}
			return "";
		}

		@Override
		public IParserEditStatus isValidEditString(IAdaptable adapter, String editString) {
			return ParserEditStatus.EDITABLE_STATUS;
		}

		@Override
		public ICommand getParseCommand(IAdaptable adapter, String newString, int flags) {
			EObject element = getInteractionOperand(adapter.getAdapter(EObject.class));
			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(element);
			if (editingDomain == null || !(element instanceof InteractionOperand)) {
				return UnexecutableCommand.INSTANCE;
			}
			InteractionOperand interactionOperand = (InteractionOperand) element;
			InteractionConstraint guard = interactionOperand.getGuard();
			CompositeTransactionalCommand command = new CompositeTransactionalCommand(editingDomain, "Set Values"); //$NON-NLS-1$
			command.compose(new UpdateGuardConditionCommand(editingDomain, interactionOperand, guard, newString));
			return command;
		}

		@Override
		public String getEditString(IAdaptable element, int flags) {
			InteractionOperand operand = getInteractionOperand(element.getAdapter(Element.class));
			return getGuardLabelText(operand, true);
		}

		protected EStructuralFeature getEStructuralFeature(Object notification) {
			EStructuralFeature featureImpl = null;
			if (notification instanceof Notification) {
				Object feature = ((Notification) notification).getFeature();
				if (feature instanceof EStructuralFeature) {
					featureImpl = (EStructuralFeature) feature;
				}
			}
			return featureImpl;
		}

		private boolean isValidFeature(EStructuralFeature feature) {
			return UMLPackage.eINSTANCE.getInteractionConstraint_Maxint().equals(feature) || UMLPackage.eINSTANCE.getInteractionConstraint_Minint().equals(feature) || UMLPackage.eINSTANCE.getConstraint_Specification().equals(feature);
		}
	}

	private static class UpdateGuardConditionCommand extends AbstractTransactionalCommand {

		private InteractionConstraint guard;

		private String text;

		private InteractionOperand interactionOperand;

		public UpdateGuardConditionCommand(TransactionalEditingDomain domain, InteractionOperand interactionOperand, InteractionConstraint guard, String text) {
			super(domain, null, null);
			this.interactionOperand = interactionOperand;
			this.guard = guard;
			this.text = text;
		}

		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
			CombinedFragment enclosingCF = (CombinedFragment) interactionOperand.getOwner();
			interactionOperand.setName(interactionOperand.getName());
			InteractionOperatorKind cfOperator = enclosingCF.getInteractionOperator();
			if (InteractionOperatorKind.LOOP_LITERAL.equals(cfOperator)) {
				if (text.contains("]") && text.contains("[")) {
					String[] parts = text.split("]");
					String[] nums = parts[0].replaceAll("\\[", "").split(",");
					int min = 0, max = -1;
					min = parseInt(nums[0], 0);
					max = nums.length > 1 ? parseInt(nums[1], -1) : min;
					if (guard.getMinint() != null) {
						setIntValue(guard.getMinint(), min);
					} else {
						guard.setMinint(createLiteralInteger(min));
					}
					if (guard.getMaxint() != null) {
						setIntValue(guard.getMaxint(), max);
					} else {
						guard.setMaxint(createLiteralInteger(max));
					}
					if (parts.length > 1) {
						text = parts[1] == null ? "" : parts[1].trim();
					} else {
						text = "";
					}
				} else {
					guard.setMinint(null);
					guard.setMaxint(null);
				}
			}
			if (guard.getSpecification() != null || text.length() > 0) {
				LiteralString literalString = UMLFactory.eINSTANCE.createLiteralString();
				literalString.setValue(text);
				guard.setSpecification(literalString);
			}
			return CommandResult.newOKCommandResult();
		}

		private LiteralInteger createLiteralInteger(int val) {
			LiteralInteger li = UMLFactory.eINSTANCE.createLiteralInteger();
			li.setValue(val);
			return li;
		}

		private void setIntValue(ValueSpecification spec, int val) {
			if (spec instanceof LiteralInteger) {
				((LiteralInteger) spec).setValue(val);
			}
		}

		private int parseInt(String string, int defaultInt) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException e) {
			}
			return defaultInt;
		}
	}

	public class GuardFigure extends Figure implements ILabelFigure, IPapyrusNodeUMLElementFigure {

		private WrappingLabel primaryLabel;

		private PapyrusWrappingLabel stereotypeLabel;

		private WrappingLabel stereotypePropertiesInBraceContent;

		/**
		 * Constructor.
		 *
		 */
		public GuardFigure() {
			ToolbarLayout layout = new ToolbarLayout(false);
			layout.setStretchMinorAxis(true);
			setLayoutManager(layout);
			primaryLabel = new WrappingLabel();
			primaryLabel.setTextWrap(true);
			primaryLabel.setAlignment(PositionConstants.CENTER);
			this.add(primaryLabel);
		}

		public WrappingLabel getPrimaryLabel() {
			return primaryLabel;
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusUMLElementFigure#setStereotypeDisplay(java.lang.String, org.eclipse.swt.graphics.Image)
		 *
		 * @param stereotypes
		 * @param image
		 */

		@Override
		public void setStereotypeDisplay(String stereotypes, Image image) {
			if ((stereotypes == null || stereotypes.trim().equals("")) && image == null) {
				if (stereotypeLabel != null) {
					remove(stereotypeLabel);
				}
				stereotypeLabel = null;
			} else {
				if (stereotypeLabel == null) {
					stereotypeLabel = new PapyrusWrappingLabel();
					add(stereotypeLabel, 0);
				}
				stereotypeLabel.setText(stereotypes);
				stereotypeLabel.setIcon(image);
			}
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#setStereotypePropertiesInBrace(java.lang.String)
		 *
		 * @param stereotypeProperties
		 */

		@Override
		public void setStereotypePropertiesInBrace(String stereotypeProperties) {
			if (stereotypeProperties == null || stereotypeProperties.trim().equals("")) {
				if (stereotypePropertiesInBraceContent != null) {
					remove(stereotypePropertiesInBraceContent);
				}
				stereotypePropertiesInBraceContent = null;
			} else {
				if (stereotypePropertiesInBraceContent == null) {
					stereotypePropertiesInBraceContent = new WrappingLabel();
					stereotypePropertiesInBraceContent.setOpaque(false);
					int index = getChildren().indexOf(stereotypeLabel);
					this.add(stereotypePropertiesInBraceContent, index + 1);
				}
				stereotypePropertiesInBraceContent.setText("{" + stereotypeProperties + "}");
			}

		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#setStereotypePropertiesInCompartment(java.lang.String)
		 *
		 * @param stereotypeProperties
		 */

		@Override
		public void setStereotypePropertiesInCompartment(String stereotypeProperties) {

		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.IPapyrusNodeUMLElementFigure#getStereotypesLabel()
		 *
		 * @return
		 */

		@Override
		public PapyrusWrappingLabel getStereotypesLabel() {
			return stereotypeLabel;
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure#setText(java.lang.String)
		 *
		 * @param text
		 */

		@Override
		public void setText(String text) {
			primaryLabel.setText(text);
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure#getText()
		 *
		 * @return
		 */

		@Override
		public String getText() {
			return primaryLabel.getText();
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure#setIcon(org.eclipse.swt.graphics.Image)
		 *
		 * @param icon
		 */

		@Override
		public void setIcon(Image icon) {
			primaryLabel.setIcon(icon);
		}

		/**
		 * @see org.eclipse.papyrus.uml.diagram.common.figure.node.ILabelFigure#getIcon()
		 *
		 * @return
		 */

		@Override
		public Image getIcon() {
			return primaryLabel.getIcon();
		}

	}
}
