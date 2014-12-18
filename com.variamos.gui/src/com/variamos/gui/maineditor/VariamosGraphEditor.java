package com.variamos.gui.maineditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cfm.common.AbstractModel;
import com.cfm.productline.AbstractElement;
import com.cfm.productline.Editable;
import com.cfm.productline.ProductLine;
import com.cfm.productline.VariabilityElement;
import com.cfm.productline.Variable;
import com.cfm.productline.io.SXFMReader;
import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.model.mxCell;
import com.mxgraph.shape.mxStencilShape;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import com.variamos.gui.pl.editor.ConfiguratorPanel;
import com.variamos.gui.pl.editor.PLEditorToolBar;
import com.variamos.gui.pl.editor.PLGraphEditorFunctions;
import com.variamos.gui.pl.editor.ProductLineGraph;
import com.variamos.gui.pl.editor.SpringUtilities;
import com.variamos.gui.pl.editor.VariabilityAttributeList;
import com.variamos.gui.pl.editor.widgets.WidgetFactory;
import com.variamos.gui.pl.editor.widgets.WidgetPL;
import com.variamos.gui.refas.editor.ModelButtonAction;
import com.variamos.gui.refas.editor.PropertyAttributeList;
import com.variamos.gui.refas.editor.RefasGraph;
import com.variamos.gui.refas.editor.RefasGraphEditorFunctions;
import com.variamos.gui.refas.editor.SemanticPlusSyntax;
import com.variamos.gui.refas.editor.widgets.MClassWidget;
import com.variamos.gui.refas.editor.widgets.MEnumerationWidget;
import com.variamos.gui.refas.editor.widgets.RefasWidgetFactory;
import com.variamos.gui.refas.editor.widgets.WidgetR;
import com.variamos.refas.core.simulationmodel.Refas2Hlcl;
import com.variamos.syntaxsupport.metametamodel.ConfigurationAttribute;
import com.variamos.syntaxsupport.metametamodel.MetaDirectRelation;
import com.variamos.syntaxsupport.metametamodel.MetaEdge;
import com.variamos.syntaxsupport.metametamodel.MetaElement;
import com.variamos.syntaxsupport.metametamodel.MetaGroupDependency;
import com.variamos.syntaxsupport.metametamodel.MetaView;
import com.variamos.syntaxsupport.metametamodel.SimulationAttribute;
import com.variamos.syntaxsupport.metamodel.EditableElement;
import com.variamos.syntaxsupport.metamodel.InstAttribute;
import com.variamos.syntaxsupport.metamodel.InstConcept;
import com.variamos.syntaxsupport.metamodel.InstEdge;
import com.variamos.syntaxsupport.metamodel.InstEnumeration;
import com.variamos.syntaxsupport.metamodel.InstGroupDependency;
import com.variamos.syntaxsupport.refas.Refas;
import com.variamos.syntaxsupport.semanticinterface.IntDirectSemanticEdge;
import com.variamos.syntaxsupport.semanticinterface.IntSemanticGroupDependency;
import com.variamos.syntaxsupport.type.DomainRegister;

import fm.FeatureModelException;

/**
 * @author jcmunoz
 *
 */
/**
 * @author jcmunoz
 *
 */
@SuppressWarnings("serial")
public class VariamosGraphEditor extends BasicGraphEditor {

	static {
		try {
			mxResources.add("com/variamos/gui/maineditor/resources/editor");
		} catch (Exception e) {
			// ignore
		}
	}
	private int modelViewIndex = 0;
	private int modelSubViewIndex = 0;
	private List<String> validElements = null;

	private static List<MetaView> metaViews = null;

	protected DomainRegister domainRegister = new DomainRegister();
	protected GraphTree productLineIndex;
	protected ConfiguratorPanel configurator;
	protected JTextArea messagesArea;
	protected JTextArea expressionsArea;
	protected JPanel elementDesPropPanel;
	protected JPanel elementConfigPropPanel;
	protected JPanel elementExpressionPanel;
	protected JPanel elementSimPropPanel;
	protected PerspectiveToolBar perspectiveToolBar;
	// Bottom tabs
	protected JTabbedPane extensionTabs;
	protected static SemanticPlusSyntax sematicSyntaxObject;

	protected int mode = 0;
	private int tabIndex = 0, lastTabIndex = 0;
	private Refas2Hlcl refas2hlcl;

	public VariamosGraphEditor getEditor() {
		return this;
	}

	public VariamosGraphEditor(String appTitle,
			VariamosGraphComponent component, int perspective,
			AbstractModel abstractModel) {
		super(appTitle, component, perspective);
		refas2hlcl = new Refas2Hlcl((Refas) abstractModel);
		registerEvents();
		((AbstractGraph) graphComponent.getGraph()).setModel(abstractModel);
		if (perspective == 0) {
			setPerspective(0);
			graphEditorFunctions = new PLGraphEditorFunctions(this);
			graphEditorFunctions.updateEditor(validElements,
					getGraphComponent(), modelViewIndex);
		}
		mxCell root = new mxCell();
		root.insert(new mxCell());
		RefasGraph refasGraph = (RefasGraph) component.getGraph();
		refasGraph.getModel().setRoot(root);
		for (int i = 0; i < metaViews.size(); i++) {
			mxCell parent = new mxCell("mv" + i);
			refasGraph.addCell(parent);
			MetaView metaView = metaViews.get(i);
			JPanel tabPane = new JPanel();
			if (metaView.getChildViews().size() > 0) {
				modelsTabPane.add(metaView.getName(), tabPane);
				refasGraph.addCell(new mxCell("mv" + i), parent); // Add the
																	// parent as
																	// first
																	// child
				for (int j = 0; j < metaView.getChildViews().size(); j++) {
					refasGraph.addCell(new mxCell("mv" + i + "-" + j), parent);
					MetaView metaChildView = metaView.getChildViews().get(j);
					JButton a = new JButton(metaChildView.getName());
					tabPane.add(a);
					a.addActionListener(new ModelButtonAction());
				}
				// TODO include recursive calls if more view levels are required
			} else {
				modelsTabPane.add(metaView.getName(), tabPane);
			}
			final EditorPalette palette = new EditorPalette();
			palette.setName("ee");
			final JScrollPane scrollPane = new JScrollPane(palette);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			modelsTabPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					// System.out.println("Tab: "
					// + modelsTabPane.getTitleAt(modelsTabPane
					// .getSelectedIndex()));
					List<MetaView> metaViews = sematicSyntaxObject
							.getMetaViews();
					VariamosGraphEditor editor = getEditor();
					int modelInd = getModelViewIndex();
					for (int i = 0; i < metaViews.size(); i++) {
						if (modelInd != i
								&& modelsTabPane.getTitleAt(
										modelsTabPane.getSelectedIndex())
										.equals(metaViews.get(i).getName())) {

							if (metaViews.get(i).getChildViews().size() > 0) {
								// if (false) //TODO validate the name of the
								// button with the tab, if true, identify the
								// subview
								// editor.setVisibleModel(i ,0);
								// else
								editor.setVisibleModel(i, 0);
								editor.updateView();
								center.setDividerLocation(60);
								center.setMaximumSize(center.getSize());
								center.setMinimumSize(center.getSize());
								center.setResizeWeight(0);
							} else {
								editor.setVisibleModel(i, -1);
								editor.updateView();
								center.setDividerLocation(25);
								center.setMaximumSize(center.getSize());
								center.setMinimumSize(center.getSize());
								center.setPreferredSize(center.getSize());
							}
						}
					}

					// Prints the string 3 times if there are 3 tabs etc
				}
			});
		}
	}

	public SemanticPlusSyntax getSematicSintaxObject() {
		return sematicSyntaxObject;
	}

	public AbstractGraphEditorFunctions getGraphEditorFunctions() {
		return graphEditorFunctions;
	}

	public void setGraphEditorFunctions(AbstractGraphEditorFunctions gef) {
		graphEditorFunctions = gef;
	}

	public int getModelViewIndex() {
		return modelViewIndex;
	}

	public int getModelSubViewIndex() {
		return modelSubViewIndex;
	}

	public List<MetaView> getMetaViews() {
		return metaViews;
	}

	public void setVisibleModel(int modelIndex, int modelSubIndex) {
		modelViewIndex = modelIndex;
		modelSubViewIndex = modelSubIndex;
		RefasGraph mode = ((RefasGraph) getGraphComponent().getGraph());
		validElements = mode
				.getValidElements(modelViewIndex, modelSubViewIndex);
		mode.setModelViewIndex(modelIndex);
		mode.setModelViewSubIndex(modelSubIndex);
		mode.showElements();

		elementDesPropPanel.repaint();
		elementConfigPropPanel.repaint();
		elementExpressionPanel.repaint();
		elementSimPropPanel.repaint();
	}

	public void updateEditor() {
		graphEditorFunctions.updateEditor(this.validElements,
				getGraphComponent(), modelViewIndex);
		perspectiveToolBar.updateButtons();
	}

	public void updateView() {
		graphEditorFunctions.updateView(this.validElements,
				getGraphComponent(), modelViewIndex);
		// perspectiveToolBar.updateButtons();
	}

	/**
	 * @param appTitle
	 * @param component
	 *            New constructor to load directly files and perspectives
	 * @throws FeatureModelException
	 */
	public static VariamosGraphEditor loader(String appTitle, String file,
			String perspective) throws FeatureModelException {
		AbstractModel abstractModel = null;
		sematicSyntaxObject = new SemanticPlusSyntax();
		metaViews = sematicSyntaxObject.getMetaViews();

		int persp = 0;
		if (perspective.equals("ProductLine")) {
			persp = 0;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readFile(file);
			} else

				abstractModel = new ProductLine();
			ProductLineGraph plGraph = new ProductLineGraph();
			// plGraph.add
			VariamosGraphEditor vge = new VariamosGraphEditor(
					"Configurator - VariaMos", new VariamosGraphComponent(
							plGraph), persp, abstractModel);
			return vge;
		} else if (perspective.equals("modeling")) {
			persp = 2;
			RefasGraph refasGraph = null;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readRefasFile(file);
				refasGraph = new RefasGraph(sematicSyntaxObject);
			} else {
				{
					abstractModel = new Refas();
					refasGraph = new RefasGraph(sematicSyntaxObject);

				}

				// ProductLineGraph plGraph2 = new ProductLineGraph();
				VariamosGraphEditor vge2 = new VariamosGraphEditor(
						"Configurator - VariaMos", new VariamosGraphComponent(
								refasGraph), persp, abstractModel);
				vge2.createFrame().setVisible(true);
				vge2.setVisibleModel(0, -1);
				vge2.setDefaultButton();
				vge2.setPerspective(2);
				vge2.setGraphEditorFunctions(new RefasGraphEditorFunctions(vge2));
				vge2.updateEditor();

				return vge2;
			}
		} else if (perspective.equals("metamodeling")) {
			// todo: change for metamodeling
			persp = 3;
			RefasGraph refasGraph = null;
			if (file != null) {
				SXFMReader reader = new SXFMReader();
				abstractModel = reader.readRefasFile(file);
				refasGraph = new RefasGraph(sematicSyntaxObject);
			} else {
				{
					abstractModel = new Refas();
					refasGraph = new RefasGraph(sematicSyntaxObject);

				}

				// ProductLineGraph plGraph2 = new ProductLineGraph();
				VariamosGraphEditor vge2 = new VariamosGraphEditor(
						"Configurator - VariaMos", new VariamosGraphComponent(
								refasGraph), persp, abstractModel);
				vge2.createFrame().setVisible(true);
				vge2.setVisibleModel(0, -1);
				vge2.setPerspective(3);
				vge2.setGraphEditorFunctions(new RefasGraphEditorFunctions(vge2));
				vge2.updateEditor();
				mxCell root = new mxCell();
				root.insert(new mxCell());
				refasGraph.getModel().setRoot(root);
				return vge2;
			}
		}
		return null;
	}

	public void editModel(AbstractModel pl) {
		// productLineIndex.reset();
		AbstractGraph abstractGraph = null;

		// todo: review other perspectives
		if (perspective == 0 || perspective == 1)
			abstractGraph = new ProductLineGraph();
		if (perspective == 2 || perspective == 3 || perspective == 4)
			abstractGraph = new RefasGraph(sematicSyntaxObject);
		// abstractGraph = (AbstractGraph) getGraphComponent()
		// .getGraph();
		((VariamosGraphComponent) graphComponent).updateGraph(abstractGraph);
		registerEvents();

		abstractGraph.setModel(pl);

		// productLineIndex.populate(pl);

	}

	public void resetView() {
		updateEditor();
		mxGraph graph = getGraphComponent().getGraph();
		// Check modified flag and display save dialog
		mxCell root = new mxCell();
		root.insert(new mxCell());
		graph.getModel().setRoot(root);
		if (perspective == 2) {
			setGraphEditorFunctions(new RefasGraphEditorFunctions(this));

			((RefasGraph) graph).defineInitialGraph();
		}

		setModified(false);
		setCurrentFile(null);
		getGraphComponent().zoomAndCenter();
	}

	private void registerEvents() {
		mxGraphSelectionModel selModel = getGraphComponent().getGraph()
				.getSelectionModel();
		selModel.addListener(mxEvent.CHANGE, new mxIEventListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				// Collection<mxCell> added = (Collection<mxCell>)
				// evt.getProperty("added");
				// System.out.println("Added: " + added);

				Collection<mxCell> removed = (Collection<mxCell>) evt
						.getProperty("removed");
				// System.out.println("Removed: " + removed);

				// editProperties(null);
				editPropertiesRefas(null);

				if (removed == null)
					return;

				mxCell cell = null;
				if (removed.size() == 1)
					cell = removed.iterator().next();

				// Multiselection case
				if (cell == null) {

					return;
				}
				if (cell.getValue() instanceof Editable) {
					Editable elm = (Editable) cell.getValue();
					editProperties(elm);
					getGraphComponent().scrollCellToVisible(cell, true);
				}

				if (cell.getValue() instanceof EditableElement) {
					EditableElement elm = (EditableElement) cell.getValue();
					editPropertiesRefas(elm);
					getGraphComponent().scrollCellToVisible(cell, true);
				}
			}
		});
	}

	public static String loadShape(EditorPalette palette, File f)
			throws IOException {
		String nodeXml = mxUtils.readFile(f.getAbsolutePath());
		addStencilShape(palette, nodeXml, f.getParent() + File.separator);
		return nodeXml;
	}

	/**
	 * Loads and registers the shape as a new shape in mxGraphics2DCanvas and
	 * adds a new entry to use that shape in the specified palette
	 * 
	 * @param palette
	 *            The palette to add the shape to.
	 * @param nodeXml
	 *            The raw XML of the shape
	 * @param path
	 *            The path to the directory the shape exists in
	 * @return the string name of the shape
	 */
	public static String addStencilShape(EditorPalette palette, String nodeXml,
			String path) {

		// Some editors place a 3 byte BOM at the start of files
		// Ensure the first char is a "<"
		int lessthanIndex = nodeXml.indexOf("<");
		nodeXml = nodeXml.substring(lessthanIndex);
		mxStencilShape newShape = new mxStencilShape(nodeXml);
		String name = newShape.getName();
		ImageIcon icon = null;

		if (path != null) {
			String iconPath = path + newShape.getIconPath();
			icon = new ImageIcon(iconPath);
		}

		// Registers the shape in the canvas shape registry
		mxGraphics2DCanvas.putShape(name, newShape);

		if (palette != null && icon != null) {
			palette.addTemplate(name, icon, "shape=" + name, 80, 80, "");
		}

		return name;
	}

	@Override
	protected Component getLeftComponent() {
		productLineIndex = new GraphTree();
		productLineIndex.bind((AbstractGraph) getGraphComponent().getGraph());

		JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				productLineIndex, null);
		inner.setDividerLocation(250);
		inner.setResizeWeight(1);
		inner.setDividerSize(6);
		inner.setBorder(null);

		return inner;
	}

	@Override
	public Component getExtensionsTab(final EditableElement elm) {
		if (extensionTabs != null)
			return extensionTabs;

		messagesArea = new JTextArea("Output");
		messagesArea.setEditable(false);

		elementDesPropPanel = new JPanel();
		elementDesPropPanel.setLayout(new SpringLayout());

		elementConfigPropPanel = new JPanel();
		elementConfigPropPanel.setLayout(new SpringLayout());

		elementExpressionPanel = new JPanel();
		// elementExpressionPanel.setLayout(new SpringLayout());

		expressionsArea = new JTextArea("Element Expressions");
		expressionsArea.setEditable(false);
		// elementExpressionPanel.add(expressionsArea);

		elementSimPropPanel = new JPanel();
		elementSimPropPanel.setLayout(new SpringLayout());

		configurator = new ConfiguratorPanel();

		// if (getPerspective() == 2) {
		
		// }

		// Bottom panel : Properties, Messages and Configuration
		extensionTabs = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		extensionTabs.addTab(mxResources.get("elementExpressionTab"),
				new JScrollPane(expressionsArea));
		extensionTabs.addTab(mxResources.get("messagesTab"), new JScrollPane(
				messagesArea));
		extensionTabs.addTab(mxResources.get("configurationTab"),
				new JScrollPane(configurator));
		extensionTabs.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (((JTabbedPane) e.getSource()).getTabCount() > 3
						&& ((JTabbedPane) e.getSource()).getSelectedIndex() >= 0) {
					tabIndex = ((JTabbedPane) e.getSource()).getSelectedIndex();
					lastTabIndex = tabIndex;
					Component c = ((JTabbedPane) e.getSource())
							.getComponent(tabIndex);
					if (c != null) {
						c.revalidate();
						c.repaint();
					}
				}
				System.out.println(tabIndex);

			}

		});
		return extensionTabs;
	}

	private void updateVisibleProperties(final EditableElement elm) {
		extensionTabs.removeAll();
		if (elm != null) {
			extensionTabs.addTab(mxResources.get("elementDisPropTab"),
					new JScrollPane(elementDesPropPanel));
			extensionTabs.addTab(mxResources.get("elementConfPropTab"),
					new JScrollPane(elementConfigPropPanel));
			// extensionTabs.addTab(mxResources.get("elementExpressionTab"),
			// new JScrollPane(elementExpressionPanel));
			extensionTabs.addTab(mxResources.get("elementSimPropTab"),
					new JScrollPane(elementSimPropPanel));
		}
		extensionTabs.addTab(mxResources.get("elementExpressionTab"),
				new JScrollPane(expressionsArea));
		extensionTabs.addTab(mxResources.get("messagesTab"), new JScrollPane(
				messagesArea));
		extensionTabs.addTab(mxResources.get("configurationTab"),
				new JScrollPane(configurator));
	}

	public void bringUpExtension(String name) {
		for (int i = 0; i < extensionTabs.getTabCount(); i++) {
			if (extensionTabs.getTitleAt(i).equals(name)) {
				extensionTabs.setSelectedIndex(i);
				return;
			}
		}
	}

	public void bringUpTab(String name) {	
		for (int i = 0; i < extensionTabs.getTabCount(); i++) {
			if (extensionTabs.getTitleAt(i).equals(name)) {
				extensionTabs.setSelectedIndex(i);
				Component c = extensionTabs.getComponent(i);
				if (c != null) {
					c.revalidate();
					c.repaint();
					return;
				}
			}
		}
	}

	public JTextArea getMessagesArea() {
		return messagesArea;
	}

	public ConfiguratorPanel getConfigurator() {
		return configurator;
	}

	public void editModelReset() {
		productLineIndex.reset();
		if (perspective == 0)
			editModel(new ProductLine());
		else {
			Refas refas = new Refas();
			refas2hlcl = new Refas2Hlcl(refas);
			editModel(refas);
		}

	}

	public void populateIndex(ProductLine pl) {

		// productLineIndex.populate(pl);
		AbstractGraph plGraph = (AbstractGraph) getGraphComponent().getGraph();
		plGraph.buildFromProductLine2(pl, productLineIndex);
		// ((mxGraphModel) plGraph.getModel()).clear();
		// plGraph.setProductLine(pl);

	}

	public AbstractModel getEditedModel() {
		if (perspective == 0)
			return ((AbstractGraph) getGraphComponent().getGraph())
					.getProductLine();
		else
			return ((AbstractGraph) getGraphComponent().getGraph()).getRefas();

	}

	public void editProperties(final Editable elm) {
		elementDesPropPanel.removeAll();
		elementConfigPropPanel.removeAll();
		elementExpressionPanel.removeAll();
		elementSimPropPanel.removeAll();

		if (elm == null) {
			bringUpTab("Properties");
			elementDesPropPanel.repaint();
			elementConfigPropPanel.repaint();
			elementExpressionPanel.repaint();
			elementSimPropPanel.repaint();
			return;
		}

		JPanel variablesPanel = new JPanel(new SpringLayout());

		Variable[] editables = elm.getEditableVariables();

		WidgetFactory factory = new WidgetFactory(this);
		for (Variable v : editables) {
			final WidgetPL w = factory.getWidgetFor(v);
			if (w == null)
				// Check the problem and/or raise an exception
				return;

			// TODO: Add listeners to w.
			w.getEditor().addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent arg0) {
					// Makes it pull the values.
					Variable v = w.getVariable();
					if (v.getType().equals("String"))
						v.setValue(AbstractElement.multiLine(v.toString(), 15));
					System.out.println("Focus Lost: " + v.hashCode() + " val: "
							+ v.getValue());
					onVariableEdited(elm);
				}

				@Override
				public void focusGained(FocusEvent arg0) {

				}
			});

			w.getEditor().addPropertyChangeListener(
					new PropertyChangeListener() {

						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (WidgetPL.PROPERTY_VALUE.equals(evt
									.getPropertyName())) {
								w.getVariable();
								onVariableEdited(elm);
							}
						}
					});
			w.getEditor().setMinimumSize(new Dimension(50, 30));
			w.getEditor().setMaximumSize(new Dimension(200, 30));
			w.getEditor().setPreferredSize(new Dimension(200, 30));
			w.editVariable(v);

			// GARA
			// variablesPanel.add(new JLabel(v.getName() + ":: "));
			variablesPanel.add(new JLabel(v.getName() + ": "));
			variablesPanel.add(w);
		}
		// variablesPanel.setPreferredSize(new Dimension(250, 25 *
		// editables.length));
		SpringUtilities.makeCompactGrid(variablesPanel, editables.length, 2, 4,
				4, 4, 4);

		elementDesPropPanel.add(variablesPanel);

		JPanel attPanel = new JPanel(new SpringLayout());
		// Fill Attributes Panel (Only for VariabilityElements ) in Properties
		// Panel
		if (elm instanceof VariabilityElement) {
			attPanel.setPreferredSize(new Dimension(150, 150));
			attPanel.add(new JLabel(mxResources.get("attributesPanel")));

			VariabilityAttributeList attList = new VariabilityAttributeList(
					this, (VariabilityElement) elm);
			attPanel.add(new JScrollPane(attList));

			SpringUtilities.makeCompactGrid(attPanel, 2, 1, 4, 4, 4, 4);

			elementDesPropPanel.add(attPanel);

			SpringUtilities.makeCompactGrid(elementDesPropPanel, 1, 2, 4, 4, 4,
					4);
		}

		elementDesPropPanel.revalidate();
	}

	// jcmunoz: new method for REFAS

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void editPropertiesRefas(final EditableElement elm) {
		updateVisibleProperties(elm);
		elementDesPropPanel.removeAll();
		elementConfigPropPanel.removeAll();
		elementExpressionPanel.removeAll();
		elementSimPropPanel.removeAll();

		if (elm == null) {
			if (lastTabIndex != 0)
				lastTabIndex = 0;
			else {
				tabIndex = 0;
				extensionTabs.setSelectedIndex(0);

			}
			return;
		} else {
			JButton test = new JButton("Execute Simulation");
			configurator.removeAll();
			configurator.add(test);
			test.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refas2hlcl.execute();
					messagesArea.setText(refas2hlcl.getText());
					bringUpTab(mxResources.get("elementSimPropTab"));
					editPropertiesRefas(elm);
				}
			});
			if (extensionTabs.getTabCount() > tabIndex && tabIndex >= 0) {
				extensionTabs.setSelectedIndex(tabIndex);
				extensionTabs.getSelectedComponent().repaint();
			}
			JPanel elementDesPropSubPanel = new JPanel(new SpringLayout());
			JPanel elementConfPropSubPanel = new JPanel(new SpringLayout());
			JPanel elementSimPropSubPanel = new JPanel(new SpringLayout());
			elm.getInstAttributes();

			List<InstAttribute> editables = elm.getEditableVariables();

			List<InstAttribute> visible = elm.getVisibleVariables();

			RefasWidgetFactory factory = new RefasWidgetFactory(this);
			int designPanelElements = 0, configurationPanelElements = 0, simulationPanelElements = 0;
			String description = null;

			if (elm instanceof InstConcept) {
				expressionsArea.setText(refas2hlcl.getElementTextConstraints(
						elm.getIdentifier(), "vertex"));
			}
			if (elm instanceof InstEdge) {
				expressionsArea.setText(refas2hlcl.getElementTextConstraints(
						elm.getIdentifier(), "edge"));
			}
			if (elm instanceof InstGroupDependency) {
				expressionsArea.setText(refas2hlcl.getElementTextConstraints(
						elm.getIdentifier(), "groupdep"));
			}
			for (InstAttribute v : visible) {
				if (elm instanceof InstGroupDependency) {

					if (v.getEnumType() != null
							&& v.getEnumType()
									.equals(MetaGroupDependency.VAR_SEMANTICGROUPDEPENDENCYCLASS)) {
						InstGroupDependency groupdep = (InstGroupDependency) elm;
						List<IntSemanticGroupDependency> metaGD = groupdep
								.getMetaGroupDependency()
								.getSemanticRelations();
						v.setValidationGDList(metaGD);
					}
				}
				if (elm instanceof InstEdge) {

					if (v.getEnumType() != null
							&& v.getEnumType()
									.equals(MetaDirectRelation.VAR_DIRECTSEMANTICEDGECLASS)) {
						MetaEdge metaEdge = ((InstEdge) elm).getMetaEdge();
						if (metaEdge instanceof MetaDirectRelation) {
							List<IntDirectSemanticEdge> directRel = ((MetaDirectRelation) metaEdge)
									.getSemanticRelations();
							v.setValidationDRList(directRel);
						}
					}
					if (v.getEnumType() != null
							&& v.getEnumType().equals(
									InstEdge.VAR_METAEDGECLASS)) {
						Map<String, MetaElement> mapElements = VariamosGraphEditor.sematicSyntaxObject
								.getSyntaxElements();
						Iterator<String> elementNames = mapElements.keySet()
								.iterator();
						List<MetaEdge> metaGD = new ArrayList<MetaEdge>();
						while (elementNames.hasNext()) {
							String elementName = elementNames.next();
							if (mapElements.get(elementName) instanceof MetaEdge) // TODO
																					// also
																					// validate
																					// origin
																					// and
																					// destination
																					// relation
								metaGD.add((MetaEdge) mapElements
										.get(elementName));
						}
						v.setValidationMEList(metaGD);
					}
				}

				final WidgetR w = factory.getWidgetFor(v);

				if (w == null) {
					System.err.print("No Widget found for " + v);
					return;
				}
				// TODO: Add listeners to w.

				w.getEditor().addFocusListener(new FocusListener() {
					@Override
					public void focusLost(FocusEvent arg0) {
						// Makes it pull the values.
						InstAttribute v = w.getInstAttribute();
						if (v.getModelingAttributeType().equals("String"))
							v.setValue(AbstractElement.multiLine(v.toString(),
									15)); // Divide
											// lines
											// every
											// 15
											// characters
											// (aprox.)
						System.out.println("Focus Lost: " + v.hashCode()
								+ " val: " + v.getDisplayValue());
						onVariableEdited(elm);
					}

					@Override
					public void focusGained(FocusEvent arg0) {
					}
				});

				w.getEditor().addPropertyChangeListener(
						new PropertyChangeListener() {

							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if (WidgetPL.PROPERTY_VALUE.equals(evt
										.getPropertyName())) {
									w.getInstAttribute();
									onVariableEdited(elm);
								}
							}
						});
				if (w instanceof MClassWidget
						|| w instanceof MEnumerationWidget) {
					w.getEditor().setPreferredSize(new Dimension(200, 100));
				} else {
					w.getEditor().setPreferredSize(new Dimension(200, 20));
					w.getEditor().setMaximumSize(new Dimension(200, 20));
				}
				w.editVariable(v);
				if (!editables.contains(v))
					w.getEditor().setEnabled(false);
				// GARA
				// variablesPanel.add(new JLabel(v.getName() + ":: "));
				if (v.getAttribute() instanceof SimulationAttribute) {
					elementSimPropSubPanel.add(new JLabel(v.getDisplayName()
							+ ": "));
					elementSimPropSubPanel.add(w);

					if (v.isAffectProperties()) {
						JButton button = new JButton("Validate");
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								editPropertiesRefas(elm);
							}
						});
						elementSimPropSubPanel.add(button);
					} else
						elementSimPropSubPanel.add(new JPanel());

					simulationPanelElements++;
				} else if (v.getAttribute() instanceof ConfigurationAttribute) {
					elementConfPropSubPanel.add(new JLabel(v.getDisplayName()
							+ ": "));
					elementConfPropSubPanel.add(w);

					if (v.isAffectProperties()) {
						JButton button = new JButton("Validate");
						button.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								editPropertiesRefas(elm);
							}
						});
						elementConfPropSubPanel.add(button);
					} else
						elementConfPropSubPanel.add(new JPanel());

					configurationPanelElements++;
				} else {

					if (v.getDisplayName().equals(MetaElement.VAR_DESCRIPTION))
						description = (String) v.getValue();
					else {
						elementDesPropSubPanel.add(new JLabel(v
								.getDisplayName() + ": "));
						elementDesPropSubPanel.add(w);
						if (v.isAffectProperties()) {
							JButton button = new JButton("Validate");
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									editPropertiesRefas(elm);
								}
							});
							elementDesPropSubPanel.add(button);
						} else
							elementDesPropSubPanel.add(new JPanel());

						designPanelElements++;
					}
				}

			}
			// variablesPanel.setPreferredSize(new Dimension(250, 25 *
			// editables.length));
			JPanel dummy = new JPanel();
			dummy.setMinimumSize(new Dimension(100, 0));
			dummy.setPreferredSize(new Dimension(100, 120));
			dummy.setMaximumSize(new Dimension(100, 200));
			elementDesPropSubPanel.add(dummy);
			dummy = new JPanel();
			dummy.setMinimumSize(new Dimension(100, 0));
			dummy.setPreferredSize(new Dimension(100, 120));
			dummy.setMaximumSize(new Dimension(100, 200));
			elementDesPropSubPanel.add(dummy);
			dummy = new JPanel();
			dummy.setMinimumSize(new Dimension(100, 0));
			dummy.setPreferredSize(new Dimension(100, 120));
			dummy.setMaximumSize(new Dimension(100, 200));
			elementDesPropSubPanel.add(dummy);
			SpringUtilities.makeCompactGrid(elementDesPropSubPanel,
					designPanelElements + 1, 3, 4, 4, 4, 4);

			SpringUtilities.makeCompactGrid(elementSimPropSubPanel,
					simulationPanelElements / 2, 6, 4, 4, 4, 4);

			SpringUtilities.makeCompactGrid(elementConfPropSubPanel,
					configurationPanelElements, 3, 4, 4, 4, 4);

			elementDesPropPanel.add(elementDesPropSubPanel);
			elementConfigPropPanel.add(elementConfPropSubPanel);
			// elementExpressionPanel.add(expressionsPanel);
			elementSimPropPanel.add(elementSimPropSubPanel);

			JPanel attPanel = new JPanel(new SpringLayout());
			// Fill Attributes Panel (Only for VariabilityElements ) in
			// Properties
			// Panel
			JPanel dummy2 = new JPanel();
			if (description != null) {
				JTextArea ta = new JTextArea();
				ta.setText(description);
				ta.setEditable(false);
				ta.setLineWrap(true);
				dummy2.add(new JLabel("Element semantics"));// TODO add constant
				dummy2.add(ta);
				ta.setPreferredSize(new Dimension(500, 100));
				ta.setMaximumSize(new Dimension(300, 100));

			}
			dummy2.setPreferredSize(new Dimension(500, 100));
			dummy2.setMaximumSize(new Dimension(300, 100));

			if (elm instanceof InstEnumeration) {
				attPanel.addFocusListener(new FocusListener() {
					@Override
					public void focusLost(FocusEvent arg0) {
						editPropertiesRefas(elm);
					}

					@Override
					public void focusGained(FocusEvent arg0) {
						editPropertiesRefas(elm);
					}
				});
				attPanel.setPreferredSize(new Dimension(150, 80));
				attPanel.setMaximumSize(new Dimension(150, 80));
				attPanel.add(new JLabel(mxResources.get("attributesPanel")));

				PropertyAttributeList attList = new PropertyAttributeList(this,
						(InstEnumeration) elm);
				attPanel.add(new JScrollPane(attList));

				SpringUtilities.makeCompactGrid(attPanel, 2, 1, 4, 4, 4, 4);

				elementDesPropPanel.add(attPanel);
				elementDesPropPanel.add(dummy2);

				SpringUtilities.makeCompactGrid(elementDesPropPanel, 1, 3, 4,
						4, 4, 4);
			} else {
				elementDesPropPanel.add(dummy2);

				SpringUtilities.makeCompactGrid(elementDesPropPanel, 1, 2, 4,
						4, 4, 4);
			}

			extensionTabs.getSelectedComponent().repaint();
			elementDesPropPanel.revalidate();
			elementConfigPropPanel.revalidate();
			elementExpressionPanel.revalidate();
			elementSimPropPanel.revalidate();
		}

	}

	protected void onVariableEdited(Editable e) {
		((AbstractGraph) getGraphComponent().getGraph()).refreshVariable(e);
	}

	protected void onVariableEdited(EditableElement e) {
		((RefasGraph) getGraphComponent().getGraph()).refreshVariable(e);
	}

	// public DomainRegister getDomainRegister(){
	// return domainRegister;
	// }

	// Not used method
	/*
	 * public void loadPalettes(){ //Load first palette PaletteDefinition pl =
	 * new PaletteDefinition(); pl.name = "Product Lines";
	 * 
	 * PaletteNode node = new PaletteNode(); ScriptedVariabilityElement elm =
	 * new ScriptedVariabilityElement(); List<Variable> atts = new
	 * ArrayList<>(); atts.add(new Variable("height", 0,
	 * IntegerType.IDENTIFIER)); elm.setVarAttributes(atts); node.prototype =
	 * elm; node.width = 80; node.height = 40; node.icon =
	 * "/com/variamos/pl/editor/images/plnode.png"; node.name =
	 * "Variability Element"; node.styleName = "plnode"; pl.nodes.add(node);
	 * 
	 * PaletteEdge edge = new PaletteEdge(); edge.name = "Optional"; edge.icon =
	 * "/com/variamos/pl/editor/images/ploptional.png"; edge.styleName =
	 * "ploptional"; edge.width = 80; edge.height = 40; edge.value =
	 * ConstraintMode.Optional;
	 * 
	 * pl.edges.add(edge);
	 * 
	 * PaletteDatabase db = new PaletteDatabase(); // db.palettes.add(pl);
	 * 
	 * loadPaletteDatabase(db);
	 * 
	 * //Load second palette try { FileWriter writer; writer = new
	 * FileWriter(new File("palettes.pal")); Gson gson = new
	 * GsonBuilder().setPrettyPrinting().serializeNulls().create();
	 * gson.toJson(db, writer); writer.close(); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * }
	 */
	/*
	 * private void loadPaletteDatabase(PaletteDatabase db) { for
	 * (PaletteDefinition pal : db.palettes) { EditorPalette palette =
	 * insertPalette(pal.name); for (PaletteNode node : pal.nodes) {
	 * palette.addTemplate( node.name, new
	 * ImageIcon(GraphEditor.class.getResource(node.icon)), node.styleName,
	 * node.width, node.height, node.prototype); }
	 * 
	 * for (PaletteEdge edge : pal.edges) { palette.addEdgeTemplate(edge.name,
	 * new ImageIcon( GraphEditor.class.getResource(edge.icon)), edge.styleName,
	 * edge.width, edge.height, edge.value); } } }
	 */
	// moved to functions classes
	// protected void showGraphPopupMenus(MouseEvent e)
	// {
	// Point pt = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(),
	// graphComponent);
	// PLEditorPopupMenu menu = new PLEditorPopupMenu(VariamosGraphEditor.this);
	// menu.show(graphComponent, pt.x, pt.y);
	//
	// e.consume();
	// }

	protected void installToolBar() {

		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		jp.add(new PLEditorToolBar(this, JToolBar.HORIZONTAL),
				BorderLayout.WEST);
		jp.add(new JLabel(), BorderLayout.CENTER);
		perspectiveToolBar = new PerspectiveToolBar(this, JToolBar.HORIZONTAL,
				perspective);
		jp.add(perspectiveToolBar, BorderLayout.EAST);
		add(jp, BorderLayout.NORTH);
	}

}