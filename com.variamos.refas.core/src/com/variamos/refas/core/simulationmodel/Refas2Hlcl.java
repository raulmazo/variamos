package com.variamos.refas.core.simulationmodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.cfm.hlcl.Expression;
import com.cfm.hlcl.HlclFactory;
import com.cfm.hlcl.HlclProgram;
import com.cfm.hlcl.HlclUtil;
import com.cfm.hlcl.Identifier;
import com.cfm.productline.solver.Configuration;
import com.cfm.productline.solver.ConfigurationOptions;
import com.cfm.productline.solver.SWIPrologSolver;
import com.cfm.productline.solver.Solver;
import com.variamos.refas.core.sematicsmetamodel.SemanticGroupDependency;
import com.variamos.refas.core.transformations.AndBooleanTransformation;
import com.variamos.refas.core.transformations.AssignBooleanTransformation;
import com.variamos.refas.core.transformations.DiffNumericTransformation;
import com.variamos.refas.core.transformations.EqualsComparisonTransformation;
import com.variamos.refas.core.transformations.GreaterOrEqualsBooleanTransformation;
import com.variamos.refas.core.transformations.ImplicationBooleanTransformation;
import com.variamos.refas.core.transformations.NotBooleanTransformation;
import com.variamos.refas.core.transformations.NumberNumericTransformation;
import com.variamos.refas.core.transformations.OrBooleanTransformation;
import com.variamos.refas.core.transformations.ProdNumericTransformation;
import com.variamos.refas.core.transformations.SumNumericTransformation;
import com.variamos.refas.core.types.CardinalityType;
import com.variamos.refas.core.types.DirectEdgeType;
import com.variamos.syntaxsupport.metametamodel.MetaConcept;
import com.variamos.syntaxsupport.metametamodel.MetaDirectRelation;
import com.variamos.syntaxsupport.metametamodel.MetaEdge;
import com.variamos.syntaxsupport.metametamodel.MetaGroupDependency;
import com.variamos.syntaxsupport.metamodel.InstAttribute;
import com.variamos.syntaxsupport.metamodel.InstConcept;
import com.variamos.syntaxsupport.metamodel.InstEdge;
import com.variamos.syntaxsupport.metamodel.InstGroupDependency;
import com.variamos.syntaxsupport.metamodel.InstVertex;
import com.variamos.syntaxsupport.refas.Refas;
import com.variamos.syntaxsupport.semanticinterface.IntRefas2Hlcl;

/**
 * Class to create the Hlcl program. Part of PhD work at University of Paris 1
 * 
 * @author Juan C. Mu�oz Fern�ndez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-12-13
 */
public class Refas2Hlcl implements IntRefas2Hlcl {
	private HlclFactory f = new HlclFactory();
	private Map<String, AbstractConstraintGroup> constraintGroups;
	private String text;
	private HlclProgram hlclProgram = new HlclProgram();
	private Refas refas;
	private Map<String, Identifier> idMap = new HashMap<>();

	public Refas2Hlcl(Refas refas) {
		this.refas = refas;
		text = "";
		constraintGroups = new HashMap<String, AbstractConstraintGroup>();
			
	}
	public void execute()
	{
		hlclProgram = new HlclProgram();
		createVertexExpressions(null);
		createEdgeExpressions(null);
		// Previous call to createEdgeExpressions is required to fill the
		// attribute names for createGroupExpressions
		createGroupExpressions(null);

		List<AbstractTransformation> transformations = new ArrayList<AbstractTransformation>();
		for (AbstractConstraintGroup constraintGroup : constraintGroups
				.values())
			transformations.addAll(constraintGroup.getTransformations());

		for (AbstractTransformation transformation : transformations) {
			idMap.putAll(transformation.getIndentifiers(f));
			if (transformation instanceof AbstractBooleanTransformation) {
				hlclProgram
						.add(((AbstractBooleanTransformation) transformation)
								.transform(f, idMap));
				// For negation testing
				// prog.add(((AbstractBooleanTransformation) transformation)
				// .transformNegation(f, idMap, true, false));
			} else if (transformation instanceof AbstractComparisonTransformation) {
				hlclProgram
						.add(((AbstractComparisonTransformation) transformation)
								.transform(f, idMap));
				// For negation testing
				// prog.add(((AbstractComparisonTransformation) transformation)
				// .transformNegation(f, idMap));
			} else {
				hlclProgram
						.add(((AbstractComparisonTransformation) transformation)
								.transform(f, idMap));
			}

		}

		Set<Identifier> identifiers = new TreeSet<Identifier>();
		for (Expression exp : hlclProgram) {
			identifiers.addAll(HlclUtil.getUsedIdentifiers(exp));
			text += exp + "\n";
		}
		Solver swiSolver = new SWIPrologSolver(hlclProgram);
		swiSolver.solve(new Configuration(), new ConfigurationOptions());
		Configuration configuration = swiSolver.getSolution();
		System.out.println("configuration: " + configuration.toString());

		// Call the SWIProlog and obtain the result
		Map<String, Integer> prologOut = configuration.getConfiguration();

		int i = 0;
		for (String identifier : prologOut.keySet()) {
			String[] split = identifier.split("_");
			String conceptId = split[0];
			String attribute = split[1];
			InstVertex vertex = refas.getVertex(conceptId);
			if (vertex.getInstAttribute(attribute).getModelingAttributeType()
					.equals("Boolean"))

				if (prologOut.get(identifier).intValue() == 1)
					vertex.getInstAttribute(attribute).setValue(true);
				else if (prologOut.get(identifier).intValue() == 0)
					vertex.getInstAttribute(attribute).setValue(false);

				else
					vertex.getInstAttribute(attribute).setValue(
							prologOut.get(i));
			System.out.print(conceptId
					+ " "
					+ attribute
					+ " "
					+ vertex.getInstAttribute(attribute)
							.getModelingAttributeType() + "; ");
		}
		/*
		 * int i = 0; for (Identifier identifier : identifiers) { String id =
		 * identifier.getId(); String[] split = id.split("_"); String conceptId
		 * = split[0]; String attribute = split[1]; InstVertex vertex =
		 * refas.getVertex(conceptId); if
		 * (vertex.getInstAttribute(attribute).getModelingAttributeType()
		 * .equals("Boolean")) /* if (prologOut.get(i).equals(1))
		 * vertex.getInstAttribute(attribute).setValue(true); else
		 * vertex.getInstAttribute(attribute).setValue(false);
		 * 
		 * vertex.getInstAttribute(attribute).setValue(true); // TODO // delete
		 * else //
		 * vertex.getInstAttribute(attribute).setValue(prologOut.get(i));
		 * System.out.print(conceptId + " " + attribute + " " +
		 * vertex.getInstAttribute(attribute) .getModelingAttributeType() +
		 * "; "); }
		 */
	}

	public AbstractConstraintGroup getElementConstraintGroup (String identifier, String type)
	{
		
		if (type.equals("vertex"))
			createVertexExpressions(identifier);
		else if (type.equals("edge"))
			createEdgeExpressions(identifier);
		else if (type.equals("groupdep"))
			createGroupExpressions(identifier);
		return constraintGroups.get(identifier);
	}

	
	public String getText() {
		return text;
	}

	private void createVertexExpressions(String identifier) {
		if (identifier == null)
			for (InstVertex elm : refas.getVariabilityVertexCollection()) {
				constraintGroups.put(elm.getIdentifier(),
						new RestrictionConstraint(elm.getIdentifier(), idMap,
								f, elm));
			}
		else
			constraintGroups.put(identifier,
					new RestrictionConstraint(identifier, idMap, f, refas
							.getVariabilityVertex().get(identifier)));
	}

	private void createEdgeExpressions( String identifier) {
		if (identifier == null)
			for (InstEdge elm : refas.getConstraintInstEdgesCollection()) {
				constraintGroups.put(elm.getIdentifier(),
						new DirectEdgeConstraintGroup(elm.getIdentifier(),
								idMap, f, elm));
			}
		else
			if (refas.getConstraintInstEdges().get(identifier)!= null)
			constraintGroups.put(identifier,
					new DirectEdgeConstraintGroup(identifier, idMap, f, refas
							.getConstraintInstEdges().get(identifier)));

	}

	private void createGroupExpressions(String identifier) {
 		createEdgeExpressions(null); //TODO define a better solution
		if (identifier == null)
			for (InstGroupDependency elm : refas
					.getInstGroupDependenciesCollection()) {
				constraintGroups.put(elm.getIdentifier(),
						new GroupDependencyConstraintGroup(elm.getIdentifier(),
								idMap, f, elm));
			}
		else
			constraintGroups.put(identifier,
					new GroupDependencyConstraintGroup(identifier, idMap, f,
							refas.getInstGroupDependencies().get(identifier)));

	}
	public String getElementTextConstraints(String identifier, String string) {
		String out ="";
		for (Expression expression : getElementConstraintGroup(identifier, string).getExpressions())
			out += expression.toString()+"\n";
		return out;
	}
}