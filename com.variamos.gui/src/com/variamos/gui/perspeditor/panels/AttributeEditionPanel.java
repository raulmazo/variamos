package com.variamos.gui.perspeditor.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import com.variamos.gui.maineditor.VariamosGraphEditor;
import com.variamos.gui.perspeditor.SpringUtilities;
import com.variamos.gui.perspeditor.widgets.RefasWidgetFactory;
import com.variamos.gui.perspeditor.widgets.WidgetR;
import com.variamos.perspsupport.syntaxsupport.EditableElementAttribute;

public class AttributeEditionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1478873242908064197L;
	private HashMap<String, WidgetR> widgets;
	private DialogButtonAction onAccept, onCancel;
	private PropertyAttributeList propertyAttributeList;
	JPanel panel = null;

	static interface DialogButtonAction {
		public boolean onAction();
	}

	public AttributeEditionPanel() {
		super();
		setLayout(new BorderLayout());

		panel = new JPanel();
		panel.setLayout(new SpringLayout());

		JPanel dummy = new JPanel();
		dummy.setMinimumSize(new Dimension(100, 0));
		dummy.setPreferredSize(new Dimension(100, 40));
		dummy.setMaximumSize(new Dimension(100, 40));
		panel.add(dummy);
		dummy = new JPanel();
		dummy.setMinimumSize(new Dimension(100, 0));
		dummy.setPreferredSize(new Dimension(100, 40));
		dummy.setMaximumSize(new Dimension(100, 40));
		panel.add(dummy);
		this.setEnabled(false);
		SpringUtilities.makeCompactGrid(panel, 1, 2, 4, 4, 4, 4);
		add(panel, BorderLayout.CENTER);
	}

	public Map<String, EditableElementAttribute> getParameters() {
		Map<String, EditableElementAttribute> map = new HashMap<>();

		for (String s : widgets.keySet()) {
			EditableElementAttribute v = widgets.get(s).getInstAttribute();
			map.put(v.getIdentifier(), v);
		}

		return map;
	}

	public void setPropertyAttributeList(
			PropertyAttributeList propertyAttributeList) {
		this.propertyAttributeList = propertyAttributeList;

	}

	public void loadElementAttributes(VariamosGraphEditor editor,
			EditableElementAttribute... editableElementAttributes) {
		RefasWidgetFactory factory = new RefasWidgetFactory(editor);

		widgets = new HashMap<String, WidgetR>();
		panel.removeAll();
		int contEd = 0;
		for (EditableElementAttribute p : editableElementAttributes) {
			if (p != null) {
				contEd++;
				WidgetR w = factory.getWidgetFor(p);
				w.editVariable(p);

				w.addPropertyChangeListener("value",
						new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								revalidate();
								doLayout();
								// pack();
							}
						});
			
			widgets.put(p.getName(), w);

			panel.add(new JLabel(p.getDisplayName() + ": "));
			panel.add(w);
			}
		}
		this.setEnabled(false);
		SpringUtilities.makeCompactGrid(panel,
				contEd, 2, 4, 4, 4, 4);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new SpringLayout());

		final JButton btnCancel = new JButton();
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onCancel == null) {
					// dispose();

					propertyAttributeList.setEnabled(true);
					panel.removeAll();
					repaint();
					return;
				}
				// if( onCancel.onAction() )
				// dispose();
			}
		});

		buttonsPanel.add(btnCancel);

		final JButton btnAccept = new JButton();
		btnAccept.setText("Accept");
		btnAccept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if( onAccept == null )
				if (onAccept.onAction())
					// dispose();
					propertyAttributeList.setEnabled(true);
				panel.removeAll();
				panel.revalidate();
				repaint();
			}
		});

		buttonsPanel.add(btnAccept);

		SpringUtilities.makeCompactGrid(buttonsPanel, 1, 2, 4, 4, 4, 4);

		add(buttonsPanel, BorderLayout.SOUTH);

	}

	public void setOnAccept(DialogButtonAction onAccept) {
		this.onAccept = onAccept;
	}

	public void setOnCancel(DialogButtonAction onCancel) {
		this.onCancel = onCancel;
	}
}