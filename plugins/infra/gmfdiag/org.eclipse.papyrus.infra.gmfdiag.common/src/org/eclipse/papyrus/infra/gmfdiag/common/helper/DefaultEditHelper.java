/*****************************************************************************
 * Copyright (c) 2010, 2017 CEA LIST, EclipseSource and others.
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
 *  Yann Tanguy (CEA LIST) yann.tanguy@cea.fr - Initial API and implementation
 *  Christian W. Damus (CEA) - bug 440108
 *  Camille Letavernier (EclipseSource) - Bug 519446
 *
 *****************************************************************************/
package org.eclipse.papyrus.infra.gmfdiag.common.helper;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.core.command.IdentityCommand;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IClientContext;
import org.eclipse.gmf.runtime.emf.type.core.IContainerDescriptor;
import org.eclipse.gmf.runtime.emf.type.core.IEditHelperContext;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.ISpecializationType;
import org.eclipse.gmf.runtime.emf.type.core.edithelper.IEditHelperAdvice;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyDependentsRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.IEditCommandRequest;
import org.eclipse.papyrus.commands.DestroyElementPapyrusCommand;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.emf.commands.UnsetValueCommand;
import org.eclipse.papyrus.infra.emf.requests.UnsetRequest;
import org.eclipse.papyrus.infra.gmfdiag.common.Activator;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateEditBasedElementCommand;
import org.eclipse.papyrus.infra.gmfdiag.common.commands.CreateRelationshipCommandEx;
import org.eclipse.papyrus.infra.services.edit.context.TypeContext;
import org.eclipse.papyrus.infra.services.edit.utils.CacheRegistry;
import org.eclipse.papyrus.infra.services.edit.utils.IRequestCacheEntries;
import org.eclipse.papyrus.infra.types.core.notification.AbstractNotifierEditHelper;
import org.eclipse.papyrus.infra.types.core.utils.AdviceUtil;

/**
 * <pre>
 * The only reason to override getDestroyElementWithDependentsCommand and getDestroyElementCommand
 * method here is to propagate the shared IClientContext used by Papyrus during the request creation.
 * Without this changes, the command to destroy dependent element won't be correctly created,
 * in EditHelper(s) the getDestroyDependentsCommand will only be called with default element type
 * (null command) and in AdviceHelper the getBeforeDestroyDependentsCommand will work but will
 * not retrieve command to destroy elements that themselves depend on dependent element to destroy.
 * 
 * The changes are replacing:
 * ElementTypeRegistry.getInstance().getElementType(req.getElementToDestroy());
 * by
 * ElementTypeRegistry.getInstance().getElementType(req.getElementToDestroy(), req.getClientContext()); 
 * 
 * See:
 * - Bug328232 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=328232)
 * - Bug328506 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=328506)
 * 
 * </pre>
 */
public class DefaultEditHelper extends AbstractNotifierEditHelper {

	/** Defined in org.eclipse.gmf.runtime.emf.type.core.internal.requests.RequestCacheEntries (internal) */
	public static final String Cache_Maps = IRequestCacheEntries.Cache_Maps;

	/** Defined in org.eclipse.gmf.runtime.emf.type.core.internal.requests.RequestCacheEntries (internal) */
	public static final String Element_Type = IRequestCacheEntries.Element_Type;

	/** Defined in org.eclipse.gmf.runtime.emf.type.core.internal.requests.RequestCacheEntries (internal) */
	public static final String Checked_Elements = IRequestCacheEntries.Checked_Elements;

	/** Defined in org.eclipse.gmf.runtime.emf.type.core.internal.requests.RequestCacheEntries (internal) */
	public static final String EditHelper_Advice = IRequestCacheEntries.EditHelper_Advice;

	/** Defined in org.eclipse.gmf.runtime.emf.type.core.internal.requests.RequestCacheEntries (internal) */
	public static final String Client_Context = IRequestCacheEntries.Client_Context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean approveRequest(IEditCommandRequest request) {
		if (request instanceof CreateRelationshipRequest) {
			return defaultApproveCreateRelationshipRequest((CreateRelationshipRequest) request);
		} else if (request instanceof CreateElementRequest) {
			// check the containment feature.
			Object context = request.getEditHelperContext();
			if (context instanceof EObject) {
				EObject owner = (EObject) context;
				EReference reference = getContainmentFeature((CreateElementRequest) request);
				// The context may not have this reference if some intermediate
				// container is to be created by the edit-helper.
				// But, then, we can only optimistically report that we can create
				// the child
				if ((reference != null) && (owner.eClass().getEAllContainments().contains(reference))) {
					return true;
				} else {
					return false;
				}
			}
		}
		return super.approveRequest(request);
	}

	protected boolean defaultApproveCreateRelationshipRequest(CreateRelationshipRequest request) {
		return true;
	}

	/**
	 * @return
	 */
	protected EReference getContainmentFeature(CreateElementRequest request) {
		EReference containmentFeature = request.getContainmentFeature();
		if (containmentFeature != null) {
			return containmentFeature;
		}

		containmentFeature = computeContainmentFeature(request);
		request.initializeContainmentFeature(containmentFeature);
		return containmentFeature;
	}

	/**
	 * Gets the EClass of the element to be edited.
	 * 
	 * @return the EClass
	 */
	protected EClass getEClassToEdit(CreateElementRequest request) {

		Object context = request.getEditHelperContext();

		if (context instanceof EObject) {
			return ((EObject) context).eClass();

		} else {
			IElementType type = ElementTypeRegistry.getInstance()
					.getElementType(context);

			if (type != null) {
				return type.getEClass();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ICommand getCreateCommand(CreateElementRequest req) {
		return new CreateEditBasedElementCommand(req);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ICommand getCreateRelationshipCommand(CreateRelationshipRequest req) {
		EObject source = req.getSource();
        EObject target = req.getTarget();

        boolean noSourceOrTarget = (source == null || target == null);
        boolean noSourceAndTarget = (source == null && target == null);

        if (noSourceOrTarget && !noSourceAndTarget) {
            // The request isn't complete yet. Return the identity command so
            // that the create relationship gesture is enabled.
            return IdentityCommand.INSTANCE;
        }
        
		return new CreateRelationshipCommandEx(req);
	}
	
	/**
	 * Gets the command to destroy a single child of an element of my kind along
	 * with its dependents (not related by containment). By default, returns a
	 * composite that destroys the elements and zero or more dependents.
	 * 
	 * @param req
	 *            the destroy request
	 * @return a command that destroys the element specified as the request's {@linkplain DestroyElementRequest#getElementToDestroy() element to
	 *         destroy} and its non-containment dependents
	 */
	protected ICommand getDestroyElementWithDependentsCommand(DestroyElementRequest req) {
		ICommand result = getBasicDestroyElementCommand(req);

		EObject initial = (EObject) req.getParameter(DestroyElementRequest.INITIAL_ELEMENT_TO_DESTROY_PARAMETER);

		if (initial == null) {
			// set the parameter to keep track of the initial element to destroy
			req.setParameter(DestroyElementRequest.INITIAL_ELEMENT_TO_DESTROY_PARAMETER, req.getElementToDestroy());
		}

		// get elements dependent on the element we are destroying, that
		// must also be destroyed
		DestroyDependentsRequest ddr = (DestroyDependentsRequest) req.getParameter(DestroyElementRequest.DESTROY_DEPENDENTS_REQUEST_PARAMETER);
		if (ddr == null) {
			// create the destroy-dependents request that will be propagated to
			// destroy requests for all elements destroyed in this operation
			ddr = new DestroyDependentsRequest(req.getEditingDomain(), req.getElementToDestroy(), req.isConfirmationRequired());
			// propagate the parameters, including the initial element to
			// destroy parameter
			ddr.addParameters(req.getParameters());
			ddr.setClientContext(req.getClientContext());
			req.setParameter(DestroyElementRequest.DESTROY_DEPENDENTS_REQUEST_PARAMETER, ddr);
		} else {
			ddr.setElementToDestroy(req.getElementToDestroy());
		}

		IElementType typeToDestroy = null;
		Map cacheMaps = (Map) req.getParameter(Cache_Maps);
		if (cacheMaps != null) {
			Map map = (Map) cacheMaps.get(req.getElementToDestroy());
			if (map != null) {
				typeToDestroy = (IElementType) map.get(Element_Type);
			}
		}

		if (typeToDestroy == null) {
			typeToDestroy = ElementTypeRegistry.getInstance().getElementType(req.getElementToDestroy(), req.getClientContext());
		}

		if (typeToDestroy != null) {
			ICommand command = typeToDestroy.getEditCommand(ddr);

			if (command != null) {
				result = result.compose(command);
			}
		}

		return result;
	}


	/**
	 * Gets the command to destroy a child of an element of my kind. By
	 * default, returns a composite command that destroys the element specified
	 * by the request and all of its contents.
	 * 
	 * @param req
	 *            the destroy request
	 * @return a command that destroys the element specified as the request's {@link DestroyElementRequest#getElementToDestroy() element to destroy} along with its contents and other dependents
	 */
	protected ICommand getDestroyElementCommand(DestroyElementRequest req) {
		ICommand result = null;

		EObject parent = req.getElementToDestroy();

		if (req.getParameter(DestroyElementRequest.INITIAL_ELEMENT_TO_DESTROY_PARAMETER) == null) {
			req.setParameter(DestroyElementRequest.INITIAL_ELEMENT_TO_DESTROY_PARAMETER, parent);
		}

		IElementType parentType = null;

		Map cacheMaps = (Map) req.getParameter(Cache_Maps);
		Set checkedElement = null;
		if (cacheMaps != null) {
			checkedElement = (Set) cacheMaps.get(Checked_Elements);
			checkedElement.add(parent);
			Map parentMap = (Map) cacheMaps.get(parent);
			if (parentMap != null) {
				parentType = (IElementType) parentMap.get(Element_Type);
			} else {
				parentType = ElementTypeRegistry.getInstance().getElementType(parent, req.getClientContext());
			}
		} else {
			parentType = ElementTypeRegistry.getInstance().getElementType(parent, req.getClientContext());
		}

		if (parentType != null) {
			for (Iterator iter = parent.eContents().iterator(); iter.hasNext();) {
				EObject next = (EObject) iter.next();

				DestroyDependentsRequest ddr = (DestroyDependentsRequest) req.getParameter(DestroyElementRequest.DESTROY_DEPENDENTS_REQUEST_PARAMETER);

				// if another object is already destroying this one because it
				// is (transitively) a dependent, then don't destroy it again .
				if ((ddr == null) || ((checkedElement != null) && checkedElement.add(next)) || (!ddr.getDependentElementsToDestroy().contains(next))) {
					// set the element to be destroyed
					req.setElementToDestroy(next);

					ICommand command = parentType.getEditCommand(req);

					if (command != null) {
						if (result == null) {
							result = command;
						} else {
							result = result.compose(command);
						}

						// Under normal circumstances the command is executable.
						// Checking canExecute here slows down large scenarios and it is therefore
						// better to skip this check.
						// if (!command.canExecute()) {
						// // no point in continuing if we're abandoning the works
						// break;
						// }
					}
				}
			}
		}

		// restore the elementToDestroy in the original request
		req.setElementToDestroy(parent);

		ICommand destroyParent = getDestroyElementWithDependentsCommand(req);

		// bottom-up destruction: destroy children before parent
		if (result == null) {
			result = destroyParent;
		} else {
			result = result.compose(destroyParent);
		}

		return result;
	}

	/**
	 * Gets the array of edit helper advice for this request.
	 * 
	 * @param req
	 *            the edit request
	 * @return the edit helper advice, or <code>null</code> if there is none
	 */
	protected IEditHelperAdvice[] getEditHelperAdvice(IEditCommandRequest req) {
		IEditHelperAdvice[] advices = null;
		Object editHelperContext = req.getEditHelperContext();
		if (editHelperContext == null) {
			return null;
		}

		Map cacheMaps = (Map) req.getParameter(Cache_Maps);
		if (cacheMaps != null) {
			if (editHelperContext instanceof IEditHelperContext) {
				IElementType type = ((IEditHelperContext) editHelperContext).getElementType();
				if (type != null) {
					Map contextMap = (Map) cacheMaps.get(type);
					if (contextMap != null) {
						advices = (IEditHelperAdvice[]) contextMap.get(EditHelper_Advice);
					}
				} else {
					// get Type from the eobject
					EObject contextObject = ((IEditHelperContext) editHelperContext).getEObject();
					Map contextMap = (Map) cacheMaps.get(contextObject);
					if (contextMap != null) {
						advices = (IEditHelperAdvice[]) contextMap.get(EditHelper_Advice);
					}

				}

			} else {
				Map contextMap = (Map) cacheMaps.get(editHelperContext);
				if (contextMap != null) {
					advices = (IEditHelperAdvice[]) contextMap.get(EditHelper_Advice);
				}
			}

		}

		if (advices == null) {
			if (editHelperContext instanceof EObject) {
				IClientContext context = req.getClientContext();
				if(context == null) {
					try {
						context = TypeContext.getContext(((EObject)editHelperContext));
						req.setClientContext(context);
					} catch (ServiceException e) {
						Activator.log.error(e);
					}
				}
				advices = ElementTypeRegistry.getInstance().getEditHelperAdvice((EObject) editHelperContext, req.getClientContext());
				IElementType[] types = ElementTypeRegistry.getInstance().getAllTypesMatching((EObject) editHelperContext, req.getClientContext());
				AdviceUtil.sort(advices, types, req.getClientContext().getId());
			} else if (editHelperContext instanceof IElementType) {
				if(req.getClientContext() == null) {
					try {
						req.setClientContext(TypeContext.getContext(req.getEditingDomain()));
					} catch (ServiceException e) {
						Activator.log.error(e);
					}
				}
				advices = CacheRegistry.getInstance().getEditHelperAdvice(req.getClientContext(), ((IElementType) editHelperContext));
				AdviceUtil.sort(advices, (IElementType)editHelperContext, req.getClientContext().getId());

			} else if (editHelperContext instanceof IEditHelperContext) {
				IClientContext clientContext = ((IEditHelperContext) editHelperContext).getClientContext();
				IElementType elementType = ((IEditHelperContext) editHelperContext).getElementType();
				EObject eObject = ((IEditHelperContext) editHelperContext).getEObject();

				if (clientContext != null) {
					if (elementType != null) {
						advices = CacheRegistry.getInstance().getEditHelperAdvice(req.getClientContext(), elementType);
						AdviceUtil.sort(advices, elementType, req.getClientContext().getId());
					} else if (eObject != null) {
						IElementType[] types = ElementTypeRegistry.getInstance().getAllTypesMatching(eObject, req.getClientContext());
						advices = ElementTypeRegistry.getInstance().getEditHelperAdvice(editHelperContext);
						AdviceUtil.sort(advices, types, req.getClientContext().getId());

					}
				} else {
					if (elementType != null) {
						if(req.getClientContext() == null) {
							try {
								req.setClientContext(TypeContext.getContext(req.getEditingDomain()));
							} catch (ServiceException e) {
								Activator.log.error(e);
							}
						}
						advices = CacheRegistry.getInstance().getEditHelperAdvice(req.getClientContext(), elementType);
						AdviceUtil.sort(advices, elementType, req.getClientContext().getId());
					} else if (eObject != null) {
						IClientContext context = req.getClientContext();
						if(context == null) {
							try {
								context = TypeContext.getContext(((EObject)eObject));
								req.setClientContext(context);
							} catch (ServiceException e) {
								Activator.log.error(e);
							}
						}
						IElementType[] types = ElementTypeRegistry.getInstance().getAllTypesMatching(eObject, context);
						advices = ElementTypeRegistry.getInstance().getEditHelperAdvice(editHelperContext);
						AdviceUtil.sort(advices, types, req.getClientContext().getId());
					}
				}
			}
		}

		return advices;
	}

	@Override
	protected ICommand getBasicDestroyElementCommand(DestroyElementRequest req) {
		ICommand result = req.getBasicDestroyCommand();

		if (result == null) {
			result = new DestroyElementPapyrusCommand(req);
		} else {
			// ensure that re-use of this request will not accidentally
			// propagate this command, which would destroy the wrong object
			req.setBasicDestroyCommand(null);
		}

		return result;
	}

	@Override
	protected ICommand getInsteadCommand(IEditCommandRequest req) {
		ICommand result = null;

		if (req instanceof UnsetRequest) {
			result = new UnsetValueCommand((UnsetRequest) req);
		} else {
			result = super.getInsteadCommand(req);
		}

		return result;
	}

	protected EReference getContainmentFeatureFromSpecializationType(ISpecializationType specializationType, Object editHelperContext) {
		if (specializationType != null) {

			IContainerDescriptor containerDescriptor = specializationType.getEContainerDescriptor();

			if (containerDescriptor != null) {
				EReference[] features = containerDescriptor.getContainmentFeatures();

				if (features != null) {

					for (int i = 0; i < features.length; i++) {

						EClass eClass = null;

						if (editHelperContext instanceof EClass) {
							eClass = (EClass) editHelperContext;

						} else if (editHelperContext instanceof EObject) {
							eClass = ((EObject) editHelperContext).eClass();

						} else if (editHelperContext instanceof IElementType) {
							eClass = ((IElementType) editHelperContext).getEClass();
						}

						if (eClass != null && eClass.getEAllReferences().contains(features[i])) {
							// Use the first feature
							return features[i];
						}
					}
				}
			}
		}
		return null;
	}

	protected boolean initializeWithThisSpecializationType(ISpecializationType specializationType, CreateElementRequest req) {
		EReference containmentFeature = getContainmentFeatureFromSpecializationType(specializationType, req);
		if (containmentFeature != null) {
			req.initializeContainmentFeature(containmentFeature);
			return true;
		}
		return false;
	}

	protected boolean isKindOf(EClass a, EClassifier b) {
		if (a.getEAllSuperTypes().contains(b)) {
			return true;
		} else {
			return a.equals(b);
		}
	}

	protected EReference findDefaultContainmentFeature(EClass ownerType, EClass ownedType) {
		for (EReference eReference : ownerType.getEAllContainments()) {
			if (isKindOf(ownedType, eReference.getEReferenceType())) {
				return eReference;
			}
		}
		return null;
	}

	// Override to initialize containment also on the basis of superElementTypes
	@Override
	public void initializeDefaultFeature(CreateElementRequest req) {

		if (req.getContainmentFeature() == null) {
			EReference containmentFeature = computeContainmentFeature(req);
			if (containmentFeature != null) {
				req.initializeContainmentFeature(containmentFeature);
			}
		}
	}


	protected EReference computeContainmentFeature(CreateElementRequest request) {
		return computeContainmentFeature(request.getElementType(), request.getContainer(), request.getEditHelperContext());
	}

	protected EReference computeContainmentFeature(IElementType elementType, EObject container, Object editHelperContext) {
		// First, try to find the feature from the element type
		ISpecializationType specializationType = (ISpecializationType) elementType.getAdapter(ISpecializationType.class);

		if (specializationType != null) {

			EReference reference = getContainmentFeatureFromSpecializationType(specializationType, editHelperContext);
			if (reference != null) {
				return reference;
			}

			IElementType[] superTypes = specializationType.getAllSuperTypes();

			// Try to initialize with the superTypes if not already initialized
			for (int i = 0; i < superTypes.length; i++) {
				IElementType superElementType = superTypes[i];

				if (superElementType instanceof ISpecializationType) {
					reference = getContainmentFeatureFromSpecializationType((ISpecializationType) superElementType, editHelperContext);
					if (reference != null) {
						return reference;
					}
				}
			}
		}

		// reference has not been found from element types ==> return from default containment feature for EClass
		EClass eClass = elementType.getEClass();

		if (eClass != null) {
			// Next, try to get a default feature
			EReference defaultFeature = getDefaultContainmentFeature(eClass);
			if (defaultFeature != null) {
				return defaultFeature;
			}

			// Compute default container (the first feature of container that can contain the new element's type)
			EReference defaultEReference = findDefaultContainmentFeature(container.eClass(), eClass);
			if (defaultEReference != null) {
				return defaultEReference;
			}
		}
		// should never happen
		return null;
	}
}
