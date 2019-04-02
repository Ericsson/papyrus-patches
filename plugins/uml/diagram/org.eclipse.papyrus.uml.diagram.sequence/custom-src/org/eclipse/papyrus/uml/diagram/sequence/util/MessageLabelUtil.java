/*****************************************************************************
 * (c) Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) - Initial API and implementation
 *
 *****************************************************************************/

package org.eclipse.papyrus.uml.diagram.sequence.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.uml.tools.utils.ValueSpecificationUtil;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Expression;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralReal;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageEnd;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.util.UMLUtil;

/**
 * @author ETXACAM
 *
 */
public class MessageLabelUtil {
	public static String getMessageLabel(Message msg, Collection<String> mask) {
		String name = getNameValue(msg);
		if (name == null || name.equals("")) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		boolean isReply = msg.getMessageSort() == MessageSort.REPLY_LITERAL;

		Map<String, ConnectableElement> msgParameters = getMessageParameters(msg);
		boolean usingNames = areParametersUsingNames(msg, new ArrayList<>(msgParameters.values()));
		List<ArgumentPair> msg_args = getMessageArguments(msg, msgParameters, usingNames);


		ArgumentPair ret_arg = null;

		if (isReply) {
			// Add <assignment-target>
			if (mask.contains(IMessageAppearance.DISP_ASSIGNMENT_TARGET) && msg_args != null) {
				ret_arg = findReturnParameter(msg_args);
				if (ret_arg != null && ret_arg.value instanceof Expression) {
					String symbol = ((Expression) ret_arg.value).getSymbol();
					if (symbol != null) {
						buf.append(symbol + "=");
					}
				}
			}
		}

		buf.append(name);

		if (!mask.contains(IMessageAppearance.DISP_ARGUMENTS) || msg_args.isEmpty()) {
			return getMessageSequenceNumber(msg) + ". " + buf.toString();
		}

		boolean first = true;
		boolean closed = false;
		boolean addSep = true;
		for (ArgumentPair arg : msg_args) {

			if (isReply && arg.parameter instanceof Parameter && ((Parameter) arg.parameter).getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
				if (closed) {
					if (addSep) {
						buf.append(", ");
					}
				}
				if (!first && !closed) {
					buf.append(") : ");
					closed = true;
				}

				String argLabel = getMessageArgumentLabel(arg.value, arg.parameter, isReply, usingNames, mask);
				if (argLabel == null || argLabel.isEmpty()) {
					addSep = false;
					continue;
				}
				buf.append(argLabel);
				addSep = true;
				continue;
			}

			if (arg.value == null && !mask.contains(IMessageAppearance.DISP_DEFAULT_ARGUMENTS)) {
				continue;
			}

			if (first) {
				buf.append("(");
			}

			if (!first) {
				if (addSep) {
					buf.append(", ");
				}
			}

			first = false;

			String argLabel = getMessageArgumentLabel(arg.value, arg.parameter, isReply, usingNames, mask);
			if (argLabel == null || argLabel.isEmpty()) {
				addSep = false;
				continue;
			}
			buf.append(argLabel);
			addSep = true;
		}

		if (!first && !closed) {
			buf.append(")");
		}
		return getMessageSequenceNumber(msg) + ". " + buf.toString();
	}

	public static String getEditLabel(Message msg) {
		StringBuffer buf = new StringBuffer();
		Map<String, ConnectableElement> msgParameters = getMessageParameters(msg);
		boolean usingNames = areParametersUsingNames(msg, new ArrayList<>(msgParameters.values()));
		List<ArgumentPair> values = getMessageArguments(msg, msgParameters, usingNames);
		ArgumentPair returnPair = null;
		boolean isReply = msg.getMessageSort() == MessageSort.REPLY_LITERAL;
		if (isReply) {
			returnPair = findReturnParameter(values);
			if (returnPair != null && returnPair.value instanceof Expression) {
				String symbol = ((Expression) returnPair.value).getSymbol();
				if (symbol != null && !symbol.isEmpty()) {
					buf.append(symbol).append("=");
				}
			}
		}

		String sigName = getNameValue(msg);
		buf.append(sigName);
		buf.append("(");
		boolean first = true;
		boolean mustName = false;
		boolean hasValues = false;
		for (ArgumentPair argPair : values) {
			if (argPair.value != null) {
				hasValues = true;
				break;
			}
		}

		if (hasValues) {
			for (ArgumentPair argPair : values) {
				Parameter param = null;
				if (argPair.parameter instanceof Parameter) {
					param = (Parameter) argPair.parameter;
					if ((!isReply && param.getDirection() != ParameterDirectionKind.IN_LITERAL &&
							param.getDirection() != ParameterDirectionKind.INOUT_LITERAL) ||
							(isReply && param.getDirection() != ParameterDirectionKind.INOUT_LITERAL &&
									param.getDirection() != ParameterDirectionKind.OUT_LITERAL)) {
						continue;
					}
				}

				String name = "";
				String value = "-";
				if (argPair.value != null) {
					value = MessageLabelUtil.getMessageArgumentValue(argPair.value);
					name = argPair.value.getName();
					if (first) {
						mustName = name != null && !name.isEmpty();
					}
				} else if (argPair.parameter != null) {
					name = argPair.parameter.getName();
				}


				if (mustName) {
					if (argPair.value != null) {
						if (!first) {
							buf.append(",");
						}
						buf.append(name).append(isReply ? ":" : "=").append(value);
					}
				} else {
					if (!first) {
						buf.append(",");
					}
					buf.append(value);
				}

				first = false;
			}
		}

		buf.append(")");
		if (isReply) {
			if (returnPair != null && returnPair.value != null) {
				if (returnPair.value instanceof Expression) {
					Expression exp = (Expression) returnPair.value;
					if (exp.getOperands().size() > 0) {
						buf.append(":").append(MessageLabelUtil.getMessageArgumentValue(returnPair.value));
					}
				} else {
					buf.append(":").append(MessageLabelUtil.getMessageArgumentValue(returnPair.value));
				}
			}
		}

		return buf.toString();
	}

	private static String getMessageSequenceNumber(Message msg) {
		List<Message> messages = getOrderedMessages(msg.getInteraction());
		int index = messages.indexOf(msg);
		if (index < 0) {
			return "";
		}
		return String.valueOf(index + 1);
	}

	private static List<Message> getOrderedMessages(Interaction interaction) {
		LinkedHashSet<Message> messages = new LinkedHashSet<>();
		return new ArrayList<>(getOrderedMessages(interaction, messages));
	}

	private static LinkedHashSet<Message> getOrderedMessages(InteractionFragment fragment, LinkedHashSet<Message> messages) {
		List<? extends InteractionFragment> fragments = null;
		if (fragment instanceof Interaction) {
			fragments = ((Interaction) fragment).getFragments();
		} else if (fragment instanceof CombinedFragment) {
			// TODO: Handle operator
			fragments = ((CombinedFragment) fragment).getOperands();
		} else if (fragment instanceof InteractionOperand) {
			fragments = ((InteractionOperand) fragment).getFragments();
		} else {
			return messages;
		}

		// TODO: Handle Grouping for execution specs
		for (InteractionFragment frg : fragments) {
			if (frg instanceof MessageEnd) {
				MessageOccurrenceSpecification mos = (MessageOccurrenceSpecification) frg;
				if (!messages.contains(mos.getMessage())) {
					messages.add(mos.getMessage());
				}
			} else {
				getOrderedMessages(frg, messages);
			}
		}
		return messages;
	}

	private static String getNameValue(Message message) {
		NamedElement signature = message.getSignature();
		List<ValueSpecification> values = message.getArguments();
		if (signature instanceof Operation) {
			Operation operation = (Operation) signature;
			return operation.getName();
		} else if (signature instanceof Signal) {
			Signal signal = (Signal) signature;
			return signal.getName();
		} else {
			return message.getName();
		}
	}


	private static class ArgumentPair {
		public ArgumentPair(ConnectableElement parameter, ValueSpecification value) {
			super();
			this.parameter = parameter;
			this.value = value;
		}

		public ConnectableElement parameter;
		public ValueSpecification value;
	}

	private static boolean areParametersUsingNames(Message message, List<ConnectableElement> sigParamsByIndex) {
		int retParams = 0;
		for (int i = sigParamsByIndex.size() - 1; i >= 0; i--) {
			ConnectableElement ce = sigParamsByIndex.get(i);
			if (!(ce instanceof Parameter)) {
				break;
			}
			Parameter p = (Parameter) ce;
			if (p.getDirection() != ParameterDirectionKind.RETURN_LITERAL) {
				break;
			}

			retParams++;
		}
		List<ValueSpecification> args = message.getArguments();
		args = args.subList(0, args.size() - retParams);
		return args.stream().allMatch(d -> d.getName() != null && !d.getName().isEmpty());
	}

	private static List<ArgumentPair> getMessageArguments(Message message, Map<String, ConnectableElement> msgParams, boolean usingNames) {
		List<ValueSpecification> values = message.getArguments();
		List<ConnectableElement> sigParamsByIndex = new ArrayList<>(msgParams.values());
		return usingNames ? getMessageArgumentsByName(values, msgParams, sigParamsByIndex) : getMessageArgumentsByIndex(values, sigParamsByIndex);
	}

	private static List<ArgumentPair> getMessageArgumentsByIndex(List<ValueSpecification> values, List<ConnectableElement> sigParamsByIndex) {
		List<ArgumentPair> params = new ArrayList<>();
		int index = 0;
		for (ConnectableElement param : sigParamsByIndex) {
			ValueSpecification spec = null;
			if (index < values.size()) {
				spec = values.get(index);
			}
			params.add(new ArgumentPair(param, spec));
			index++;
		}

		for (int i = sigParamsByIndex.size(); i < values.size(); i++) {
			params.add(new ArgumentPair(null, values.get(i)));
		}

		return params;

	}

	private static List<ArgumentPair> getMessageArgumentsByName(List<ValueSpecification> values, Map<String, ConnectableElement> sigParams, List<ConnectableElement> sigParamsByIndex) {
		List<ArgumentPair> params = new ArrayList<>();
		int index = 0;
		for (ValueSpecification spec : values) {
			String name = spec.getName();
			ConnectableElement param = null;
			if (name != null && !name.isEmpty()) {
				param = sigParams.remove(name);
			} else {
				if (index < sigParams.size()) {
					param = sigParamsByIndex.get(index);
					sigParams.remove(param.getName());
				}
			}
			params.add(new ArgumentPair(param, spec));
			index++;
		}

		for (ConnectableElement param : sigParams.values()) {
			params.add(new ArgumentPair(param, null));
		}

		return params;
	}

	private static ArgumentPair findReturnParameter(List<ArgumentPair> values) {
		for (int i = values.size() - 1; i >= 0; i--) {
			ArgumentPair pair = values.get(i);
			if (pair.parameter instanceof Parameter) {
				Parameter p = (Parameter) pair.parameter;
				if (p.getDirection() == ParameterDirectionKind.RETURN_LITERAL) {
					return pair;
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, ConnectableElement> getMessageParameters(Message msg) {
		if (msg.getMessageSort() == MessageSort.ASYNCH_SIGNAL_LITERAL) {
			if (msg.getSignature() instanceof Signal) {
				return (Map) getSignalAttributes((Signal) msg.getSignature());
			}
		} else if (msg.getMessageSort() == MessageSort.REPLY_LITERAL) {
			if (msg.getSignature() instanceof Operation) {
				Map<String, Parameter> params = getOperationParameters(
						(Operation) msg.getSignature(), ParameterDirectionKind.OUT);
				params.putAll(getOperationParameters((Operation) msg.getSignature(), ParameterDirectionKind.RETURN));
				return (Map) params;
			}
		} else {
			if (msg.getSignature() instanceof Operation) {
				return (Map) getOperationParameters((Operation) msg.getSignature(), ParameterDirectionKind.IN);
			}
		}
		return Collections.EMPTY_MAP;
	}

	private static Map<String, Parameter> getOperationParameters(Operation op, int dir) {
		LinkedHashMap<String, Parameter> params = new LinkedHashMap<>();
		for (Parameter p : op.getOwnedParameters()) {
			switch (p.getDirection()) {
			case IN_LITERAL:
				if (dir == ParameterDirectionKind.IN) {
					params.put(p.getName(), p);
				}
				break;
			case INOUT_LITERAL:
				if (dir != ParameterDirectionKind.RETURN) {
					params.put(p.getName(), p);
				}
				break;
			case OUT_LITERAL:
				if (dir == ParameterDirectionKind.OUT) {
					params.put(p.getName(), p);
				}
			case RETURN_LITERAL:
				if (dir == ParameterDirectionKind.RETURN) {
					params.put(p.getName(), p);
				}
			default:
				break;
			}
		}
		return params;
	}

	private static Map<String, Property> getSignalAttributes(Signal sig) {
		LinkedHashMap<String, Property> params = new LinkedHashMap<>();
		for (Property prop : sig.allAttributes()) {
			params.put(prop.getName(), prop);
		}
		return params;
	}

	private static Lifeline getLifeline(MessageEnd messageEnd) {
		if (messageEnd instanceof MessageOccurrenceSpecification) {
			List<Lifeline> covered = ((MessageOccurrenceSpecification) messageEnd).getCovereds();
			if (!covered.isEmpty()) {
				return covered.get(0);
			}
		}

		return null;
	}


	private static Classifier getLifelineRepresentClassifier(MessageEnd messageEnd) {
		ConnectableElement ce = getLifelineRepresentPart(messageEnd);
		if (ce != null && ce.getType() instanceof Classifier) {
			return (Classifier) ce.getType();
		}
		return null;
	}

	private static ConnectableElement getLifelineRepresentPart(MessageEnd messageEnd) {
		Lifeline lf = getLifeline(messageEnd);
		if (lf != null) {
			return lf.getRepresents();
		}

		return null;
	}

	private static List<Signal> getReceivableSignals(MessageEnd sendEvent, MessageEnd receiveEvent) {
		return getReceivableSignals(sendEvent, receiveEvent, null);
	}

	private static List<Signal> getReceivableSignals(MessageEnd sendEvent, MessageEnd receiveEvent, String name) {
		List<Classifier> classifiers = getMessageReceivableSources(
				getLifelineRepresentPart(receiveEvent), getLifelineRepresentPart(sendEvent), false);

		if (classifiers.isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		Set<Signal> signals = new HashSet<>();
		for (Classifier classifier : classifiers) {
			for (Feature f : classifier.allFeatures()) {
				if (!(f instanceof Reception)) {
					continue;
				}
				Reception rec = (Reception) f;
				Signal s = rec.getSignal();
				if (s == null) {
					continue;
				}
				if (name == null || s.getName().equals(name)) {
					signals.add(s);
				}

				@SuppressWarnings("rawtypes")
				List<Generalization> specializations = (List) s.getTargetDirectedRelationships(UMLPackage.Literals.GENERALIZATION);
				for (Generalization g : specializations) {
					if (!(g.getSpecific() instanceof Signal)) {
						continue;
					}
					Signal specific = (Signal) g.getSpecific();
					if (name == null || specific.getName().equals(name)) {
						signals.add(specific);
					}
				}
			}
		}

		return new ArrayList<>(signals);
	}

	@SuppressWarnings("unchecked")
	private static Map<Reception, List<Signal>> getReceivableSignalsMap(MessageEnd sendEvent, MessageEnd receiveEvent, String name) {
		List<Classifier> classifiers = getMessageReceivableSources(
				getLifelineRepresentPart(receiveEvent), getLifelineRepresentPart(sendEvent), false);

		if (classifiers.isEmpty()) {
			return Collections.EMPTY_MAP;
		}

		Map<Reception, List<Signal>> receptionMap = new HashMap<>();
		for (Classifier classifier : classifiers) {
			for (Feature f : classifier.allFeatures()) {
				if (!(f instanceof Reception)) {
					continue;
				}
				List<Signal> signals = new ArrayList<>();

				Reception rec = (Reception) f;
				Signal s = rec.getSignal();
				if (s == null) {
					continue;
				}
				if (name == null || s.getName().equals(name)) {
					signals.add(s);
				}

				@SuppressWarnings("rawtypes")
				List<Generalization> specializations = (List) s.getTargetDirectedRelationships(UMLPackage.Literals.GENERALIZATION);
				for (Generalization g : specializations) {
					if (!(g.getSpecific() instanceof Signal)) {
						continue;
					}
					Signal specific = (Signal) g.getSpecific();
					if (name == null || specific.getName().equals(name)) {
						signals.add(specific);
					}
				}

				if (!signals.isEmpty()) {
					receptionMap.put(rec, signals);
				}

			}
		}

		return receptionMap;
	}

	@SuppressWarnings("unchecked")
	private static List<Operation> getCallableOperations(MessageEnd sendEvent, MessageEnd receiveEvent, String name) {
		List<Classifier> classifiers = getMessageReceivableSources(
				getLifelineRepresentPart(receiveEvent), getLifelineRepresentPart(sendEvent), false);

		if (classifiers.isEmpty()) {
			return Collections.EMPTY_LIST;
		}

		List<Operation> ops = new ArrayList<>();
		for (Classifier classifier : classifiers) {
			for (Operation op : classifier.getAllOperations()) {
				if (name != null && !op.getName().equals(name)) {
					continue;
				}

				ops.add(op);
			}
		}
		return ops;
	}

	@SuppressWarnings("unchecked")
	private static List<Classifier> getMessageReceivableSources(ConnectableElement targetPart, ConnectableElement sourcePart, boolean filtered) {
		if (targetPart == null || targetPart.getType() == null) {
			return Collections.EMPTY_LIST;
		}

		Type targetType = targetPart.getType();
		Type sourceType = sourcePart != null ? sourcePart.getType() : null;

		List<Classifier> sources = null;
		List<Interface> provideds = null;
		List<Interface> required = null;
		if (targetPart instanceof Port) {
			Port port = (Port) targetPart;
			provideds = port.getProvideds();
		} else if (targetType instanceof Component) {
			Component component = (Component) targetPart.getType();
			provideds = component.getProvideds();
		}

		if (sourcePart instanceof Port) {
			Port port = (Port) targetPart;
			required = port.getRequireds();
		} else if (sourceType instanceof Component) {
			Component component = (Component) targetPart.getType();
			required = component.getRequireds();
		} else if (targetType instanceof Classifier) {
			return Collections.singletonList((Classifier) targetType);
		}

		if (provideds != null) {
			if (filtered && required != null) {
				provideds = new ArrayList<>(provideds);
				provideds.retainAll(required);
			}

			if (!provideds.isEmpty()) {
				sources = new ArrayList<>(provideds);
			}
		}

		if (sources == null) {
			sources = Collections.EMPTY_LIST;
		}
		return sources;
	}

	private static String getMessageArgumentValue(ValueSpecification value) {
		if (value instanceof LiteralNull) {
			return "null";
		} else if (value instanceof LiteralString) {
			return "\"" + value.stringValue() + "\"";
		} else if (value instanceof LiteralInteger) {
			return "" + value.integerValue();
		} else if (value instanceof LiteralReal) {
			return "" + value.realValue();
		} else if (value instanceof OpaqueExpression) {
			OpaqueExpression opExp = (OpaqueExpression) value;
			if (opExp.getBodies().size() > 0) {
				return opExp.getBodies().get(0);
			}
			return "";
		} else if (value instanceof Expression) {
			Expression exp = (Expression) value;
			String symbol = exp.getSymbol();
			if (symbol == null) {
				symbol = "";
			}
			if (symbol.isEmpty() && exp.getOperands().isEmpty()) {
				return "-";
			}
			if (exp.getOperands().size() == 0 && !symbol.isEmpty()) {
				return symbol;
			}

			if (exp.getOperands().size() == 1) {
				return getMessageArgumentValue(exp.getOperands().get(0));
			}
		}
		// Fallback to default behavior.
		return ValueSpecificationUtil.getSpecificationValue(value);
	}

	private static String getMessageArgumentLabel(ValueSpecification specification, ConnectableElement param, boolean isReply, boolean usingNames, Collection<String> mask) {
		String assignSpecification = null;
		String name = null;
		if (mask.contains(IMessageAppearance.DISP_ARGUMENT_NAMES)) {
			if (specification != null) {
				name = specification.getName();
			} else if (param != null) {
				name = param.getName();
			}

		}

		String value = getMessageArgumentValue(specification);
		if (specification == null && mask.contains(IMessageAppearance.DISP_DEFAULT_ARGUMENTS)) {
			value = "-";
		}

		if (value.equals("-") && !mask.contains(IMessageAppearance.DISP_DEFAULT_ARGUMENTS)) {
			return "";
		}

		if (!mask.contains(IMessageAppearance.DISP_ARGUMENT_VALUES)) {
			value = null;
		}

		StringBuffer buf = new StringBuffer();
		if (isReply) {
			if (specification instanceof Expression) {
				Expression exp = (Expression) specification;
				assignSpecification = exp.getSymbol();
				if (exp.getOperands().size() == 1) {
					value = getMessageArgumentValue(exp.getOperands().get(0));
					if (value.equals("-") && !mask.contains(IMessageAppearance.DISP_DEFAULT_ARGUMENTS)) {
						return "";
					}
					if (!mask.contains(IMessageAppearance.DISP_ARGUMENT_VALUES)) {
						value = null;
					}
				} else if (exp.getOperands().size() == 0) {
					value = "";
				}
			}

			if (mask.contains(IMessageAppearance.DISP_ASSIGNMENT_TARGET)) {
				assignSpecification = null;
			}

			if (assignSpecification != null && !assignSpecification.isEmpty()) {
				buf.append(assignSpecification).append("=");
				if (name != null && !name.isEmpty()) {
					buf.append(name);
				}

				if (value != null && !value.isEmpty()) {
					buf.append(":").append(value);
				}
			} else {
				if (name != null && !name.isEmpty()) {
					buf.append(name);
				}
				if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
					buf.append(":");
				}
				if (value != null && !value.isEmpty()) {
					buf.append(value);
				}
			}
		} else {
			if (name != null && !name.isEmpty()) {

				buf.append(name);
			}
			if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
				buf.append("=");
			}
			if (value != null && !value.isEmpty()) {
				buf.append(value);
			}
		}
		return buf.toString();
	}

	private static final ExecutionSpecification getExecution(MessageEnd msgEnd, List<Message> messages) {
		if (!(msgEnd instanceof MessageOccurrenceSpecification)) {
			return null;
		}

		Message msg = msgEnd.getMessage();
		MessageOccurrenceSpecification moSpec = (MessageOccurrenceSpecification) msgEnd;
		Lifeline lifeLine = moSpec.getCovered();
		if (lifeLine == null) {
			return null;
		}

		ExecutionSpecification execution = null;
		Stack<ExecutionSpecification> current = new Stack<>();
		Stack<List<Message>> execMessages = new Stack<>();
		for (InteractionFragment intFrag : msg.getInteraction().getFragments()) {
			if (!intFrag.getCovereds().contains(lifeLine)) {
				continue;
			}
			if (intFrag instanceof ExecutionOccurrenceSpecification) {
				ExecutionOccurrenceSpecification eoc = (ExecutionOccurrenceSpecification) intFrag;
				if (intFrag == eoc.getExecution().getStart()) {
					current.push(eoc.getExecution());
					execMessages.push(new ArrayList<Message>());
				} else if (intFrag == eoc.getExecution().getFinish()) {
					ExecutionSpecification exec = current.pop();
					List<Message> msgs = execMessages.pop();
					if (exec == execution) {
						messages.addAll(msgs);
						return exec;
					}
				}
			} else if (intFrag instanceof MessageEnd) {
				if (intFrag == msgEnd) {
					execution = current.peek();
				}
				execMessages.peek().add(((MessageEnd) intFrag).getMessage());
			}

		}
		return null;
	}

	private static final Type getUmlIntegerFor(Element element) {
		return (PrimitiveType) findByQualifiedName(element.eResource(), "PrimitiveTypes::Integer", UMLPackage.Literals.PRIMITIVE_TYPE);
	}

	private static final Type getUmlUnlimitedNaturalFor(Element element) {
		return (PrimitiveType) findByQualifiedName(element.eResource(), "PrimitiveTypes::UnlimitedNatural", UMLPackage.Literals.PRIMITIVE_TYPE);
	}

	private static final Type getUmlRealFor(Element element) {
		return (PrimitiveType) findByQualifiedName(element.eResource(), "PrimitiveTypes::Real", UMLPackage.Literals.PRIMITIVE_TYPE);
	}

	private static final Type getUmlBooleanFor(Element element) {
		return (PrimitiveType) findByQualifiedName(element.eResource(), "PrimitiveTypes::Boolean", UMLPackage.Literals.PRIMITIVE_TYPE);
	}

	private static final PrimitiveType getUmlStringFor(Element element) {
		return (PrimitiveType) findByQualifiedName(element.eResource(), "PrimitiveTypes::String", UMLPackage.Literals.PRIMITIVE_TYPE);
	}

	private static final NamedElement findByQualifiedName(Resource res, String qn, EClass cls) {
		Collection<NamedElement> nes = UMLUtil.findNamedElements(res, qn);
		for (int i = 0; i < 2; i++) {
			Iterator<NamedElement> it = nes.iterator();
			while (it.hasNext()) {
				NamedElement el = it.next();
				if (cls.isSuperTypeOf(el.eClass())) {
					return el;
				}
			}

			nes = UMLUtil.findNamedElements(res.getResourceSet(), qn);
		}
		return null;
	}

}
