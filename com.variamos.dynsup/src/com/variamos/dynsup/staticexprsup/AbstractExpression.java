package com.variamos.dynsup.staticexprsup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.variamos.dynsup.instance.InstAttribute;
import com.variamos.dynsup.instance.InstConcept;
import com.variamos.dynsup.instance.InstElement;
import com.variamos.dynsup.instance.InstPairwiseRel;
import com.variamos.dynsup.model.ElemAttribute;
import com.variamos.dynsup.model.ModelExpr;
import com.variamos.dynsup.model.SyntaxElement;
import com.variamos.dynsup.types.ExpressionVertexType;
import com.variamos.hlcl.core.DomainParser;
import com.variamos.hlcl.model.expressions.HlclFactory;
import com.variamos.hlcl.model.expressions.Identifier;
import com.variamos.hlcl.model.expressions.IntBooleanExpression;
import com.variamos.hlcl.model.expressions.IntExpression;
import com.variamos.hlcl.model.expressions.IntNumericExpression;

/**
 * Abstract root Class to group at the Transformation functionality. Part of PhD
 * work at University of Paris 1
 * 
 * @author Juan C. Munoz Fernandez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-12-13
 */
public abstract class AbstractExpression {
	private InstElement leftVertex;
	private InstElement rightVertex;
	private String leftAttributeName;
	private String rightAttributeName;
	private AbstractExpression leftSubExpression;
	private AbstractExpression rightSubExpression;
	private IntBooleanExpression leftBooleanExpression;
	private IntBooleanExpression rightBooleanExpression;
	private IntNumericExpression leftNumericExpression;
	private IntNumericExpression rightNumericExpression;
	protected String operation;

	protected List<ExpressionVertexType> expressionVertexTypes;
	protected List<String> expressionConnectors;

	public AbstractExpression(InstElement left, InstElement right,
			String leftAttributeName, String rightAttributeName) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		this.leftVertex = left;
		this.rightVertex = right;
		this.leftAttributeName = leftAttributeName;
		this.rightAttributeName = rightAttributeName;

		this.expressionVertexTypes.add(ExpressionVertexType.LEFTVARIABLE);
		this.expressionVertexTypes.add(ExpressionVertexType.RIGHTVARIABLE);
	}

	public AbstractExpression(InstElement vertex, String attributeName,
			boolean replaceTarget, AbstractExpression subExpression) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();
		if (replaceTarget) {
			if (vertex != null) {
				this.leftVertex = vertex;
				this.leftAttributeName = attributeName;
				this.expressionVertexTypes
						.add(ExpressionVertexType.LEFTVARIABLE);
			}
			this.expressionVertexTypes
					.add(ExpressionVertexType.RIGHTSUBEXPRESSION);
			this.rightSubExpression = subExpression;
		} else {
			this.expressionVertexTypes
					.add(ExpressionVertexType.LEFTSUBEXPRESSION);
			this.leftSubExpression = subExpression;
			if (vertex != null) {
				this.rightVertex = vertex;
				this.rightAttributeName = attributeName;
				this.expressionVertexTypes
						.add(ExpressionVertexType.RIGHTVARIABLE);
			}
		}
	}

	public AbstractExpression(InstElement vertex, String attributeName,
			boolean replaceTarget, IntBooleanExpression booleanExpression) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		if (replaceTarget) {
			this.leftVertex = vertex;
			this.leftAttributeName = attributeName;
			this.expressionVertexTypes.add(ExpressionVertexType.LEFTVARIABLE);
			this.expressionVertexTypes
					.add(ExpressionVertexType.RIGHTBOOLEANEXPRESSION);
			this.rightBooleanExpression = booleanExpression;
		} else {
			this.rightVertex = vertex;
			this.rightAttributeName = attributeName;
			this.expressionVertexTypes
					.add(ExpressionVertexType.LEFTBOOLEANEXPRESSION);
			this.expressionVertexTypes.add(ExpressionVertexType.RIGHTVARIABLE);
			this.leftBooleanExpression = booleanExpression;
		}
	}

	public AbstractExpression(InstElement vertex, String attributeName,
			boolean replaceTarget, IntNumericExpression numericExpression) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		if (replaceTarget) {
			this.leftVertex = vertex;
			this.leftAttributeName = attributeName;
			this.expressionVertexTypes.add(ExpressionVertexType.LEFTVARIABLE);
			this.expressionVertexTypes
					.add(ExpressionVertexType.RIGHTNUMERICVALUE);
			this.rightNumericExpression = numericExpression;
		} else {
			this.rightVertex = vertex;
			this.rightAttributeName = attributeName;
			this.expressionVertexTypes
					.add(ExpressionVertexType.LEFTNUMERICVALUE);
			this.expressionVertexTypes.add(ExpressionVertexType.RIGHTVARIABLE);
			this.leftNumericExpression = numericExpression;
		}
	}

	public AbstractExpression(IntNumericExpression numericExpression1,
			boolean replaceTarget, IntNumericExpression numericExpression2) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		if (replaceTarget) {
			this.leftNumericExpression = numericExpression1;
			this.expressionVertexTypes
					.add(ExpressionVertexType.LEFTNUMERICVALUE);
			this.expressionVertexTypes
					.add(ExpressionVertexType.RIGHTNUMERICVALUE);
			this.rightNumericExpression = numericExpression2;
		} else {
			this.rightNumericExpression = numericExpression1;
			this.expressionVertexTypes
					.add(ExpressionVertexType.LEFTNUMERICVALUE);
			this.expressionVertexTypes
					.add(ExpressionVertexType.RIGHTNUMERICVALUE);
			this.leftNumericExpression = numericExpression2;
		}
	}

	public AbstractExpression(AbstractExpression leftSubExpression,
			AbstractExpression rightSubExpression) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		this.leftSubExpression = leftSubExpression;
		this.rightSubExpression = rightSubExpression;
		this.expressionVertexTypes.add(ExpressionVertexType.LEFTSUBEXPRESSION);
		this.expressionVertexTypes.add(ExpressionVertexType.RIGHTSUBEXPRESSION);
	}

	public AbstractExpression() {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();
	}

	public AbstractExpression(InstElement vertex, String attributeName) {
		expressionVertexTypes = new ArrayList<ExpressionVertexType>();
		expressionConnectors = new ArrayList<String>();

		this.leftVertex = vertex;
		this.leftAttributeName = attributeName;
		this.expressionVertexTypes.add(ExpressionVertexType.LEFTVARIABLE);
	}

	// Static call
	public Map<String, Identifier> getIdentifiers(HlclFactory f) {
		Map<String, Identifier> out = new HashMap<String, Identifier>();
		if (leftVertex != null) {
			// System.out.println(leftVertex.getIdentifier() + " "
			// + leftAttributeName);
			Identifier identifier = f.newIdentifier(leftVertex
					.getInstAttributeFullIdentifier(leftAttributeName, -1),
					leftAttributeName);
			out.put(leftVertex.getInstAttributeFullIdentifier(
					leftAttributeName, -1), identifier);
			ElemAttribute attribute = leftVertex.getInstAttribute(
					leftAttributeName).getAttribute();

			updateDomain(attribute, leftVertex, identifier);
		}
		if (rightVertex != null) {
			// System.out.println("GET ID" + rightVertex.getIdentifier()
			// + rightAttributeName);
			Identifier identifier = f.newIdentifier(rightVertex
					.getInstAttributeFullIdentifier(rightAttributeName, -1),
					rightAttributeName);

			out.put(rightVertex.getInstAttributeFullIdentifier(
					rightAttributeName, -1), identifier);
			ElemAttribute attribute = rightVertex.getInstAttribute(
					rightAttributeName).getAttribute();
			updateDomain(attribute, rightVertex, identifier);

		}
		if (leftSubExpression != null) {
			out.putAll(leftSubExpression.getIdentifiers(f));
		}
		if (rightSubExpression != null) {
			out.putAll(rightSubExpression.getIdentifiers(f));
		}
		return out;
	}

	private void updateDomain(ElemAttribute attribute, InstElement instVertex,
			Identifier identifier) {
		if (attribute.getName().equals("SDReqLevel")
				|| attribute.getName().equals("ClaimExpLevel")) {
			String configdomain = "";
			Set<Integer> values = new HashSet<Integer>();
			if (((InstConcept) instVertex).getInstAttribute("Required") != null
					&& ((InstConcept) instVertex)
							.getInstAttribute("ConfigReqLevel") != null
					&& ((InstConcept) instVertex).getInstAttribute("Required")
							.getAsBoolean()
					&& ((InstConcept) instVertex).getInstAttribute(
							"ConfigReqLevel").getAsInteger() != 5)
				values.add(((InstConcept) instVertex).getInstAttribute(
						"ConfigReqLevel").getAsInteger());
			for (InstElement relation : instVertex.getSourceRelations()) {
				// FIXME implement a dynamic definition for this validation
				if (((InstPairwiseRel) relation)
						.getInstAttribute("sourceLevel") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"sourceLevel").getAsInteger());
				if (((InstPairwiseRel) relation)
						.getInstAttribute("targetLevel") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"targetLevel").getAsInteger());
				if (((InstPairwiseRel) relation).getInstAttribute("level") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"level").getAsInteger());
				if (((InstPairwiseRel) relation).getInstAttribute("CLSGLevel") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"CLSGLevel").getAsInteger());
			}
			for (InstElement relation : instVertex.getTargetRelations()) {
				// FIXME implement a dynamic definition for this validation
				if (((InstPairwiseRel) relation)
						.getInstAttribute("sourceLevel") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"sourceLevel").getAsInteger());
				if (((InstPairwiseRel) relation)
						.getInstAttribute("targetLevel") != null)
					values.add(((InstPairwiseRel) relation).getInstAttribute(
							"targetLevel").getAsInteger());
			}
			if (values.size() == 0) {
				values.add(new Integer(0)); // TODO use value according to
											// MAX/MIN/As close as possible type
											// of SG.
			}
			for (Integer value : values) {
				configdomain += value.toString() + ",";
			}
			configdomain = configdomain.substring(0, configdomain.length() - 1);
			identifier.setDomain(DomainParser.parseDomain(configdomain, 0));
		} else if (attribute.getName().equals("varConfValue")) {
			Boolean isConfigdomain = (Boolean) instVertex.getInstAttribute(
					"isConfDom").getValue();
			String configdomain = (String) instVertex.getInstAttribute(
					"varConfDom").getValue();
			if (configdomain != null && isConfigdomain && isConfigdomain
					&& !configdomain.equals(""))
				identifier.setDomain(DomainParser.parseDomain(configdomain, 0));
		} else if (attribute.getName().equals("value")) {
			String type = (String) instVertex.getInstAttribute("variableType")
					.getValue();

			if (type.equals("Integer")) {
				String domain = (String) instVertex.getInstAttribute("varDom")
						.getValue();
				identifier.setDomain(DomainParser.parseDomain(domain, 0));
			} else if (type.equals("Float")) {
				String domain = ModelExpr.transformDomain((String) instVertex
						.getInstAttribute("floatDom").getValue(),
						(Integer) instVertex.getInstAttribute("floatPrec")
								.getValue());
				identifier.setDomain(DomainParser.parseDomain(domain,
						(int) instVertex.getInstAttribute("floatPrec")
								.getValue()));
			} else if (type.equals("Enumeration")) {
				Object object = instVertex.getInstAttribute("enumType")
						.getValueObject();
				String domain = "";
				if (object != null) {
					@SuppressWarnings("unchecked")
					Collection<InstAttribute> values = (Collection<InstAttribute>) ((InstElement) object)
							.getInstAttribute(SyntaxElement.VAR_METAENUMVALUE)
							.getValue();
					for (InstAttribute value : values) {
						String[] split = ((String) value.getValue()).split("#");
						domain += split[0] + ",";
					}
				}
				domain = domain.substring(0, domain.length() - 1);
				identifier.setDomain(DomainParser.parseDomain(domain, 0));
			}
		} else if (attribute.getType().equals("Integer")
				|| attribute.getType().equals("Enumeration")
				|| attribute.getType().equals("String")
				|| attribute.getType().equals("Float")) {
			if (attribute.getDomain() != null)
				identifier.setDomain(attribute.getDomain());
		}
	}

	public InstElement getLeft() {
		return leftVertex;
	}

	public InstElement getRight() {
		return rightVertex;
	}

	public InstElement getLeftVertex() {
		return leftVertex;
	}

	public InstElement getRightVertex() {
		return rightVertex;
	}

	public String getLeftAttributeName() {
		return leftAttributeName;
	}

	public String getRightAttributeName() {
		return rightAttributeName;
	}

	public AbstractExpression getLeftSubExpression() {
		return leftSubExpression;
	}

	public AbstractExpression getRightSubExpression() {
		return rightSubExpression;
	}

	public IntExpression getLeftComparativeExpression() {
		return leftBooleanExpression;
	}

	public IntExpression getRightComparativeExpression() {
		return rightBooleanExpression;
	}

	public List<ExpressionVertexType> getExpressionVertexTypes() {
		return expressionVertexTypes;
	}

	public List<String> getExpressionConnectors() {
		return expressionConnectors;
	}

	public IntBooleanExpression getLeftBooleanExpression() {
		return leftBooleanExpression;
	}

	public IntBooleanExpression getRightBooleanExpression() {
		return rightBooleanExpression;
	}

	public IntNumericExpression getLeftNumericExpression() {
		return leftNumericExpression;
	}

	public IntNumericExpression getRightNumericExpression() {
		return rightNumericExpression;
	}

	public String getOperation() {
		return operation;
	}

	public String expressionStructure() {
		String out = "";
		int i = 0;
		for (ExpressionVertexType expressionVertex : expressionVertexTypes) {
			if (expressionConnectors.size() > i)
				out += " " + expressionConnectors.get(i) + " ";
			switch (expressionVertex) {
			case LEFTBOOLEANEXPRESSION:
				out += leftBooleanExpression;
				break;
			case RIGHTBOOLEANEXPRESSION:
				out += rightBooleanExpression;
				break;
			case LEFTVARIABLE:
				out += leftAttributeName;
				break;
			case RIGHTVARIABLE:
				out += rightAttributeName;

				break;
			case LEFTSUBEXPRESSION:
				out += "(" + leftSubExpression.expressionStructure() + ")";
				break;
			case RIGHTSUBEXPRESSION:
				out += "(" + rightSubExpression.expressionStructure() + ")";
				break;
			default:
				break;

			}
			i++;
		}
		return out;
	}

	protected List<IntExpression> expressionTerms(HlclFactory f,
			Map<String, Identifier> idMap) {
		List<IntExpression> out = new ArrayList<IntExpression>();

		for (ExpressionVertexType expressionType : expressionVertexTypes) {
			switch (expressionType) {
			case LEFTBOOLEANEXPRESSION:
				out.add(leftBooleanExpression);
				break;
			case RIGHTBOOLEANEXPRESSION:
				out.add(rightBooleanExpression);
				break;
			case LEFTNUMERICVALUE:
				out.add(leftNumericExpression);
				break;
			case RIGHTNUMERICVALUE:
				out.add(rightNumericExpression);
				break;
			case LEFTVARIABLE:
				out.add(idMap.get(getLeft().getInstAttributeFullIdentifier(
						leftAttributeName, -1)));
				break;
			case RIGHTVARIABLE:
				out.add(idMap.get(getRight().getInstAttributeFullIdentifier(
						rightAttributeName, -1)));

				break;
			case LEFTSUBEXPRESSION:
				if (leftSubExpression instanceof AbstractBooleanExpression)
					out.add((leftSubExpression).transform(f, idMap));
				else if (leftSubExpression instanceof AbstractNumericExpression)
					out.add(((AbstractNumericExpression) leftSubExpression)
							.transform(f, idMap));
				else
					out.add(((AbstractComparisonExpression) leftSubExpression)
							.transform(f, idMap));
				break;
			case RIGHTSUBEXPRESSION:
				if (rightSubExpression instanceof AbstractBooleanExpression)
					out.add(((AbstractBooleanExpression) rightSubExpression)
							.transform(f, idMap));
				else if (rightSubExpression instanceof AbstractNumericExpression)
					out.add(((AbstractNumericExpression) rightSubExpression)
							.transform(f, idMap));
				else
					out.add(((AbstractComparisonExpression) rightSubExpression)
							.transform(f, idMap));
				break;
			default:
				break;

			}
		}

		return out;
	}

	protected List<IntExpression> expressionTermsNegation(HlclFactory f,
			Map<String, Identifier> idMap, boolean negateLeft,
			boolean negateRight) {
		List<IntExpression> out = new ArrayList<IntExpression>();

		for (ExpressionVertexType expressionType : expressionVertexTypes) {
			switch (expressionType) {
			case LEFTBOOLEANEXPRESSION:
				if (negateLeft)
					out.add(f.not(leftBooleanExpression));
				else
					out.add(leftBooleanExpression);
				break;
			case RIGHTBOOLEANEXPRESSION:
				out.add(rightBooleanExpression);
				break;
			case LEFTNUMERICVALUE:
				out.add(leftNumericExpression);
				break;
			case RIGHTNUMERICVALUE:
				out.add(rightNumericExpression);
				break;
			case LEFTVARIABLE:
				if (negateLeft)
					out.add(f.not(idMap.get(getLeft()
							.getInstAttributeFullIdentifier(leftAttributeName,
									-1))));
				else
					out.add(idMap.get(getLeft().getInstAttributeFullIdentifier(
							leftAttributeName, -1)));
				break;
			case RIGHTVARIABLE:
				if (negateRight)
					out.add(f.not(idMap.get(getRight()
							.getInstAttributeFullIdentifier(rightAttributeName,
									-1))));
				else
					out.add(idMap.get(getRight()
							.getInstAttributeFullIdentifier(rightAttributeName,
									-1)));
				break;
			case LEFTSUBEXPRESSION:
				if (negateLeft
						&& leftSubExpression instanceof AbstractBooleanExpression)
					// TODO special case for assign
					out.add(((AbstractBooleanExpression) leftSubExpression)
							.transformNegation(f, idMap, false, false));
				else
					out.add(leftSubExpression.transform(f, idMap));
				break;
			case RIGHTSUBEXPRESSION:
				if (negateRight
						&& rightSubExpression instanceof AbstractBooleanExpression)
					// TODO special case for assign
					out.add(((AbstractBooleanExpression) rightSubExpression)
							.transformNegation(f, idMap, false, false));
				else

					out.add(rightSubExpression.transform(f, idMap));
				break;
			default:
				break;

			}
		}

		return out;
	}

	public abstract IntExpression transform(HlclFactory hlclFactory,
			Map<String, Identifier> idMap);
}
