package com.variamos.perspsupport.translationsupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mxgraph.util.mxResources;
import com.variamos.hlcl.BooleanExpression;
import com.variamos.hlcl.Expression;
import com.variamos.hlcl.HlclFactory;
import com.variamos.hlcl.HlclProgram;
import com.variamos.hlcl.Identifier;
import com.variamos.hlcl.Labeling;
import com.variamos.perspsupport.expressionsupport.InstanceExpression;
import com.variamos.perspsupport.expressionsupport.OperationLabeling;
import com.variamos.perspsupport.expressionsupport.OperationSubActionExpType;
import com.variamos.perspsupport.expressionsupport.SemanticExpression;
import com.variamos.perspsupport.expressionsupport.SemanticOperationSubAction;
import com.variamos.perspsupport.instancesupport.InstAttribute;
import com.variamos.perspsupport.instancesupport.InstElement;
import com.variamos.perspsupport.perspmodel.ModelInstance;
import com.variamos.perspsupport.semanticinterface.IntSemanticElement;
import com.variamos.perspsupport.semanticinterface.IntSemanticExpression;
import com.variamos.perspsupport.semanticsupport.SemanticOverTwoRelation;
import com.variamos.perspsupport.semanticsupport.SemanticPairwiseRelation;
import com.variamos.perspsupport.syntaxsupport.AbstractAttribute;
import com.variamos.perspsupport.types.ExpressionVertexType;
import com.variamos.perspsupport.types.OperationSubActionExecType;
import com.variamos.semantic.expressionsupport.ElementExpressionSet;

/**
 * A class to represent the constraints. Part of PhD work at University of Paris
 * 1
 * 
 * @author Juan C. Mu�oz Fern�ndez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-12-13
 */
public class TranslationExpressionSet extends ElementExpressionSet {

	/**
	 * Identifier of the operation
	 */
	private String operation;
	/* 
	 */
	private Map<String, List<InstanceExpression>> instanceExpressions;

	/**
	 * must be obtained from UI
	 */
	private Map<String, Identifier> idMap;
	/**
	 * 
	 */
	private HlclFactory hlclFactory;

	static private HlclFactory f = new HlclFactory();
	/**
	 * 
	 */
	private boolean optional = false;
	private ModelInstance refas;

	/**
	 * Assign the parameters on the abstract class
	 * 
	 * @param operation
	 * @param column
	 */
	public TranslationExpressionSet(ModelInstance refas, String operation,
			Map<String, Identifier> idMap, HlclFactory hlclFactory) {
		super(operation, mxResources.get("defect-concepts") + " " + operation,
				idMap, hlclFactory);
		this.refas = refas;
		instanceExpressions = new HashMap<String, List<InstanceExpression>>();
		this.idMap = idMap;
		this.hlclFactory = hlclFactory;
		this.operation = operation;
	}

	public void addExpressions(ModelInstance refas, InstElement instElement,
			String subAction, OperationSubActionExecType expressionType) {

		List<InstanceExpression> out = new ArrayList<InstanceExpression>();

		// List<InstElement> semModel = refas.getVariabilityVertex("OMMModel");
		// for (InstElement oper : semModel) {
		// InstElement oper2 = refas.getElement("REFAS1");
		// IntSemanticElement semModelElement =
		// oper2.getEditableSemanticElement();

		// out.addAll(createElementInstanceExpressions(oper2));
		// TODO create expressions for model concepts

		// if (instElement == null)
		// out.addAll(createElementInstanceExpressions(operAction));
		// }

		List<InstElement> operActions = refas.getOperationalModel()
				.getVariabilityVertex("OMMOperation");
		InstElement operAction = null;
		SemanticOperationSubAction operSubAction = null;
		for (InstElement oper : operActions) {
			if (oper.getIdentifier().equals(operation)) {
				operAction = oper;
				break;
			}
		}
		for (InstElement rel : operAction.getTargetRelations()) {
			InstElement subOper = rel.getTargetRelations().get(0);
			if (subOper.getIdentifier().equals(subAction))
				operSubAction = (SemanticOperationSubAction) subOper
						.getEditableSemanticElement();
		}

		/*
		 * SemanticOperationAction operAction = null; for (InstElement oper :
		 * operActions) { if (oper.getIdentifier().equals(operation)) operAction
		 * = (SemanticOperationAction) oper .getEditableSemanticElement();
		 * 
		 * } SemanticOperationSubAction operSubAction = operAction
		 * .getExpressionSubAction(subAction);
		 */
		if (operSubAction != null) {
			OperationSubActionExpType operExpType = operSubAction
					.getOperationSubActionExpType(expressionType);
			if (operExpType != null) {
				List<IntSemanticExpression> semExp = operExpType
						.getSemanticExpressions();

				if (instElement == null)
					for (InstElement instE : refas.getElements()) {
						out.addAll(createElementInstanceExpressions(instE,
								semExp));
						for (InstAttribute att : instE.getInstAttributes()
								.values()) {
							// create instance expressions for conditional
							// expressions
							if (att.getType()
									.equals(InstanceExpression.class
											.getCanonicalName())) {
								if (att.getValue() != null) {
									InstanceExpression instanceExpression = new InstanceExpression(
											true, "cond", true);
									instanceExpression
											.setSemanticExpressionType(refas
													.getSemanticExpressionTypes()
													.get("DoubleImplies"));
									instanceExpression.setLeftElement(instE);
									instanceExpression.setLeftAttributeName(att
											.getIdentifier());
									instanceExpression
											.setRightInstanceExpression((InstanceExpression) att
													.getValue());
									instanceExpression
											.setRightExpressionType(ExpressionVertexType.RIGHTSUBEXPRESSION);
									out.add(instanceExpression);
								} else {
									InstanceExpression instanceExpression = new InstanceExpression(
											true, "cond", true);
									instanceExpression
											.setSemanticExpressionType(refas
													.getSemanticExpressionTypes()
													.get("Equals"));
									instanceExpression.setLeftElement(instE);
									instanceExpression.setLeftAttributeName(att
											.getIdentifier());
									instanceExpression
											.setRightExpressionType(ExpressionVertexType.RIGHTNUMERICVALUE);
									instanceExpression.setRightNumber(1);
									out.add(instanceExpression);
								}
							}
						}
						for (AbstractAttribute var : operSubAction
								.getInVariables()) {
							int attributeValue = 0;
							InstAttribute instAttribute = instE
									.getInstAttribute(var.getName());
							// FIXME: compare attribute name and element name
							if (instAttribute != null
									&& instAttribute.getAttribute() == var) {
								String type = (String) instAttribute.getType();
								if (type.equals("Integer")
										|| type.equals("Boolean")) {
									if (instAttribute.getValue() instanceof Boolean)
										attributeValue = ((boolean) instAttribute
												.getValue()) ? 1 : 0;
									else if (instAttribute.getValue() instanceof String)
										attributeValue = Integer
												.valueOf((String) instAttribute
														.getValue());
									else
										attributeValue = (Integer) instAttribute
												.getValue();
								}
								if (type.equals("String")) {
									attributeValue = ((String) instAttribute
											.getValue()).hashCode();
								}
								InstanceExpression instanceExpression = new InstanceExpression(
										true, "t", true);
								instanceExpression
										.setSemanticExpressionType(refas
												.getSemanticExpressionTypes()
												.get("Equals"));
								instanceExpression.setLeftElement(instE);
								instanceExpression
										.setLeftAttributeName(instAttribute
												.getIdentifier());
								instanceExpression
										.setRightNumber(attributeValue);
								instanceExpression
										.setRightExpressionType(ExpressionVertexType.RIGHTNUMERICVALUE);
								out.add(instanceExpression);
							}
						}
					}
				else
					out.addAll(createElementInstanceExpressions(instElement,
							semExp));

			}
		}
		instanceExpressions.put(subAction + "-" + expressionType, out);

	}

	public List<Labeling> getLabelings(ModelInstance refas, String subAction,
			OperationSubActionExecType expressionType) {

		List<InstElement> operActions = refas.getOperationalModel()
				.getVariabilityVertex("OMMOperation");
		InstElement operAction = null;
		SemanticOperationSubAction operSubAction = null;
		for (InstElement oper : operActions) {
			if (oper.getIdentifier().equals(operation)) {
				operAction = oper;
				break;
			}
		}
		for (InstElement rel : operAction.getTargetRelations()) {
			InstElement subOper = rel.getTargetRelations().get(0);
			if (subOper.getIdentifier().equals(subAction))
				operSubAction = (SemanticOperationSubAction) subOper
						.getEditableSemanticElement();
		}

		if (operSubAction != null) {
			OperationSubActionExpType operExpType = operSubAction
					.getOperationSubActionExpType(expressionType);
			if (operExpType != null) {
				List<Labeling> out = new ArrayList<Labeling>();
				for (InstElement instE : refas.getElements()) {
					for (OperationLabeling operLab : operSubAction
							.getOperLabels()) {
						List<Identifier> ident = new ArrayList<Identifier>();
						for (AbstractAttribute var : operLab.getVariables()) {
							InstAttribute instAttribute = instE
									.getInstAttribute(var.getName());
							if (instAttribute != null
									&& instAttribute.getAttribute() == var) {
								ident.add(f.newIdentifier(instE.getIdentifier()
										+ "_"
										+ instAttribute.getAttributeName()));
							}
						}
						Labeling lab = new Labeling((String) operLab.getName(),
								operLab.getPosition(), operLab.isOnce(),
								operLab.getLabelingOrderList(),
								operLab.getOrderExpressionList());
						lab.setVariables(ident);
						out.add(lab);
					}
				}

				return out;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected List<InstanceExpression> createElementInstanceExpressions(
			InstElement instElement,
			List<IntSemanticExpression> semanticExpressions) {
		IntSemanticElement semElement = instElement
				.getTransSupportMetaElement().getTransSemanticConcept();
		List<InstanceExpression> out = new ArrayList<InstanceExpression>();
		if (semElement != null
				&& semElement.getAllSemanticExpressions() != null)
			for (IntSemanticExpression semExpression : semElement
					.getAllSemanticExpressions()) {
				if (semanticExpressions.contains(semExpression)) {
					InstanceExpression instanceExpression = new InstanceExpression(
							refas, false, (SemanticExpression) semExpression);
					instanceExpression.createFromSemanticExpression(
							instElement, 0);
					out.add(instanceExpression);
				}
			}
		if (semElement != null
				&& (semElement instanceof SemanticOverTwoRelation || semElement instanceof SemanticPairwiseRelation)) {
			InstAttribute ia = instElement.getTransSupportMetaElement()
					.getTransInstSemanticElement()
					.getInstAttribute("operationsExpressions");
			List<InstAttribute> ias = (List<InstAttribute>) ia.getValue();
			for (InstAttribute attribute : ias) {
				String att = attribute.getIdentifier();
				String comp = (String) instElement.getInstAttribute(
						"relationType").getValue();
				if (att.equals(comp))
					for (IntSemanticExpression semExpression : (List<IntSemanticExpression>) attribute
							.getValue()) {
						if (semanticExpressions.contains(semExpression)) {
							InstanceExpression instanceExpression = new InstanceExpression(
									refas, false,
									(SemanticExpression) semExpression);
							instanceExpression.createFromSemanticExpression(
									instElement, 0);
							out.add(instanceExpression);
						}
					}
			}
		}
		return out;
	}

	protected List<InstanceExpression> createElementInstanceExpressions(
			InstElement instElement) {
		IntSemanticElement semElement = instElement
				.getTransSupportMetaElement().getTransSemanticConcept();
		List<InstanceExpression> out = new ArrayList<InstanceExpression>();
		if (semElement != null
				&& semElement.getAllSemanticExpressions() != null)
			for (IntSemanticExpression semExpression : semElement
					.getAllSemanticExpressions()) {
				InstanceExpression instanceExpression = new InstanceExpression(
						refas, false, (SemanticExpression) semExpression);
				instanceExpression.createFromSemanticExpression(instElement, 0);
				out.add(instanceExpression);
			}

		return out;
	}

	protected List<InstanceExpression> createElementInstanceExpressions(
			IntSemanticElement semElement) {
		List<InstanceExpression> out = new ArrayList<InstanceExpression>();
		if (semElement != null
				&& semElement.getAllSemanticExpressions() != null)
			for (IntSemanticExpression semExpression : semElement
					.getAllSemanticExpressions()) {
				InstanceExpression instanceExpression = new InstanceExpression(
						refas, false, (SemanticExpression) semExpression);
				instanceExpression.createFromSemanticExpression(null, 0);
				out.add(instanceExpression);
			}
		return out;
	}

	protected void setOptional(boolean optional) {
		this.optional = optional;
	}

	protected Map<String, Identifier> getIdMap() {
		return idMap;
	}

	protected HlclFactory getHlclFactory() {
		return hlclFactory;
	}

	public boolean isOptional() {
		return optional;
	}

	public List<InstanceExpression> getInstanceExpressions(String column) {
		return instanceExpressions.get(column);
	}

	/**
	 * Expression for textual representation
	 * 
	 * @return
	 */
	public List<Expression> getHLCLExpressions(String column) {
		List<Expression> out = new ArrayList<Expression>();
		for (InstanceExpression expression : instanceExpressions.get(column)) {
			// idMap.putAll(expression.(hlclFactory));
			Expression newExp = expression.createSGSExpression();
			if (newExp != null)
				out.add(newExp);
		}
		return out;
	}

	public HlclProgram getHlCLProgramExpressions(String column) {
		HlclProgram prog = new HlclProgram();
		for (InstanceExpression expression : instanceExpressions.get(column)) {
			// idMap.putAll(transformation.getIdentifiers(hlclFactory));
			BooleanExpression newExp = (BooleanExpression) expression
					.createSGSExpression();
			if (newExp != null)
				prog.add(newExp);
		}
		return prog;
	}

}
