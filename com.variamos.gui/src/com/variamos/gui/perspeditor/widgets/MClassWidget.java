package com.variamos.gui.perspeditor.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;
import com.variamos.dynsup.instance.InstAttribute;
import com.variamos.dynsup.instance.InstCell;
import com.variamos.dynsup.instance.InstConcept;
import com.variamos.dynsup.instance.InstElement;
import com.variamos.dynsup.interfaces.IntInstAttribute;
import com.variamos.dynsup.model.InstanceModel;
import com.variamos.dynsup.model.OpersElement;
import com.variamos.dynsup.model.SyntaxElement;
import com.variamos.dynsup.types.ClassMultiSelectionType;
import com.variamos.gui.core.io.ConsoleTextArea;

/**
 * A class to support class widgets on the interface with multi-selection.
 * Inspired on other widgets from ProductLine. Part of PhD work at University of
 * Paris 1
 * 
 * @author Juan C. Munoz Fernandez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-12-04
 * @see com.variamos.gui.pl.editor.widgets
 */
@SuppressWarnings("serial")
public class MClassWidget extends WidgetR {

	private JList<String> txtValue;
	private Map<String, InstConcept> concepts;

	public MClassWidget() {
		super();
		setLayout(new BorderLayout());

	}

	@Override
	public void configure(IntInstAttribute v, mxGraph graph,
			InstanceModel semanticModel,
			boolean showSimulationCustomizationBox, int perspective) {
		super.configure(v, graph, semanticModel,
				showSimulationCustomizationBox, perspective);
		ClassLoader classLoader = ClassMultiSelectionType.class
				.getClassLoader();
		@SuppressWarnings("rawtypes")
		Class aClass = null;
		InstAttribute instAttribute = (InstAttribute) v;
		try {
			aClass = classLoader.loadClass(instAttribute.getAttribute()
					.getClassCanonicalName());
		} catch (ClassNotFoundException e) {
			ConsoleTextArea.addText(instAttribute.getAttribute()
					.getClassCanonicalName());
			ConsoleTextArea.addText(e.getStackTrace());
		}
		String[] out = null;
		if (aClass.getSuperclass() != null
				&& aClass.getSuperclass().equals(OpersElement.class)) {
			/*
			 * Collection<IntSemanticElement> list = semanticSyntaxObject
			 * .getSemanticConcepts().values();
			 * 
			 * System.out.println("MClassW old semanticSyntax");
			 * List<IntSemanticElement> list2 = new
			 * ArrayList<IntSemanticElement>();
			 * 
			 * for (IntSemanticElement concept : list) { if
			 * (aClass.isInstance(concept)) list2.add(concept); } out = new
			 * String[list2.size()]; int i = 0; for (IntSemanticElement concept
			 * : list2) { if (semanticConcepts.put(concept.getIdentifier(),
			 * concept) == null) { String str = concept.getIdentifier();
			 * out[i++] = str.toString(); } }
			 */
		}
		if (aClass.equals(InstConcept.class)) {
			concepts = new HashMap<String, InstConcept>();
			List<InstConcept> list = getInstConcepts(instAttribute
					.getAttribute().getMetaConceptInstanceType(), graph);

			Set<InstConcept> set = new HashSet<InstConcept>();
			set.addAll(list);
			out = new String[set.size()];
			int i = 0;
			for (InstConcept concept : set) {
				concepts.put(concept.getIdentifier(), concept);
				String str = concept.getInstAttribute("name").toString();
				out[i++] = str.toString();
			}

		}

		txtValue = new JList<String>(out);
		JScrollPane panel = new JScrollPane(txtValue);
		panel.setPreferredSize(new Dimension(200, 50));
		panel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(panel, BorderLayout.CENTER);
		revalidate();
	}

	public List<InstConcept> getInstConcepts(String object, mxGraph graph) {
		List<InstConcept> out = new ArrayList<InstConcept>();
		mxIGraphModel refasGraph = graph.getModel();
		Object o = refasGraph.getRoot(); // Main Root
		mxCell o1 = (mxCell) refasGraph.getChildAt(o, 0); // Null Root
		for (int i = 0; i < o1.getChildCount(); i++) {
			mxCell mv = (mxCell) refasGraph.getChildAt(o1, i);
			for (int j = 0; j < mv.getChildCount(); j++) {
				mxCell concept = (mxCell) refasGraph.getChildAt(mv, j);
				InstElement value = ((InstCell) concept.getValue())
						.getInstElement();
				if (value instanceof InstConcept) {
					InstConcept ic = (InstConcept) value;
					SyntaxElement mc = ic.getTransSupportMetaElement();
					if (mc.getAutoIdentifier().equals(object))
						out.add(ic);
				}

			}
		}
		return out;
	}

	@Override
	protected boolean pushValue(IntInstAttribute v) {
		if (v.getValue() instanceof int[]) {
			@SuppressWarnings("unchecked")
			List<Integer> values = (List<Integer>) v.getValue();
			int[] valuesArray = new int[values.size()];
			int i = 0;
			for (Integer value : values)
				valuesArray[i++] = value;
			txtValue.setSelectedIndices(valuesArray);
		}
		group.setText((String) v.getGroup());
		revalidate();
		repaint();
		return false;
	}

	@Override
	protected void pullValue(IntInstAttribute v) {
		List<Integer> values = new ArrayList<Integer>();
		int[] valuesArray = txtValue.getSelectedIndices();
		for (int i = 0; i < valuesArray.length; i++)
			values.add(valuesArray[i]);
		v.setValue(values);
		String out = "";
		List<String> tmp = txtValue.getSelectedValuesList();
		v.setGroup(group.getText());
		for (String str : tmp)
			out += str + ";";
		InstAttribute instAttribute = (InstAttribute) v;
		instAttribute.displayValue(out);
	}

	@Override
	public JComponent getEditor() {
		return txtValue;
	}

	@Override
	public JComponent getGroup() {
		return group;
	}
}
