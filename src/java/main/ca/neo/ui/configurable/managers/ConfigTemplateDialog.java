package ca.neo.ui.configurable.managers;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import ca.neo.ui.configurable.PropertyInputPanel;
import ca.shu.ui.lib.Style.Style;
import ca.shu.ui.lib.util.UserMessages;
import ca.shu.ui.lib.util.Util;

/**
 * A Configuration dialog which allows the user to manage templates
 * 
 * @author Shu
 */
public class ConfigTemplateDialog extends ConfigDialog {

	private static final long serialVersionUID = 5650002324576913316L;

	private JComboBox templateList;

	public ConfigTemplateDialog(UserTemplateConfigurer configManager,
			Frame owner) {
		super(configManager, owner);
	}

	public ConfigTemplateDialog(UserTemplateConfigurer configManager,
			Dialog owner) {
		super(configManager, owner);
	}

	@Override
	protected void createPropertiesDialog(JPanel panel) {
		super.createPropertiesDialog(panel);

		if (checkPropreties()) {
			/*
			 * Use existing properties
			 */
			templateList.setSelectedItem(null);
		} else {
			/*
			 * Selects the default template
			 */
			for (int i = 0; i < templateList.getItemCount(); i++) {
				if (templateList.getItemAt(i).toString().compareTo(
						UserTemplateConfigurer.DEFAULT_TEMPLATE_NAME) == 0) {
					templateList.setSelectedIndex(i);
					break;
				}
			}

			updateDialogFromFile();
		}
	}

	@Override
	protected void initPanelTop(JPanel panel) {
		/*
		 * Add existing templates
		 */
		String[] files = configurerParent.getPropertyFiles();

		templateList = new JComboBox(files);

		JPanel savedFilesPanel = new JCustomPanel();

		JPanel dropDownPanel = new JCustomPanel();

		templateList.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				updateDialogFromFile();

			}

		});

		savedFilesPanel.add(new JLabel("Templates"));
		dropDownPanel.add(templateList);
		dropDownPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		savedFilesPanel.add(dropDownPanel);

		JPanel buttonsPanel = new JCustomPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 5));

		JButton button;
		button = new JButton("New");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (applyProperties()) {
					String name = JOptionPane.showInputDialog("Name:");

					if (name != null && name.compareTo("") != 0) {
						configurerParent.savePropertiesFile(name);
						templateList.addItem(name);
						templateList.setSelectedIndex(templateList
								.getItemCount() - 1);
					}
				} else {
					UserMessages.showWarning("Properties not complete");
				}
			}
		});
		button.setFont(Style.FONT_SMALL);
		buttonsPanel.add(button);

		button = new JButton("Remove");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedFile = (String) templateList.getSelectedItem();

				templateList.removeItem(selectedFile);

				configurerParent.deletePropertiesFile(selectedFile);

				updateDialogFromFile();
			}
		});
		button.setFont(Style.FONT_SMALL);
		buttonsPanel.add(button);

		savedFilesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));

		savedFilesPanel.add(buttonsPanel);

		JPanel wrapperPanel = new JCustomPanel();
		wrapperPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));
		wrapperPanel.add(savedFilesPanel);

		JPanel seperator = new JCustomPanel();
		seperator.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		if (getConfigurerParent().isTemplateEditable()) {
			panel.add(wrapperPanel);
			panel.add(seperator);
		}
	}

	/**
	 * Loads the properties associated with the item selected in the file drop
	 * down list
	 */
	protected void updateDialogFromFile() {
		try {
			if (templateList.getSelectedItem() != null) {
				configurerParent.loadPropertiesFromFile((String) templateList
						.getSelectedItem());
				Iterator<PropertyInputPanel> it = propertyInputPanels
						.iterator();
				while (it.hasNext()) {
					PropertyInputPanel panel = it.next();

					Object currentValue = configurerParent.getProperty(panel
							.getName());
					if (currentValue != null && panel.isEnabled()) {
						panel.setValue(currentValue);
					}

				}
			}
		} catch (ClassCastException e) {
			Util
					.debugMsg("Saved template has incompatible data, it will be ignored");
		}
	}

	@Override
	public UserTemplateConfigurer getConfigurerParent() {
		return (UserTemplateConfigurer) super.getConfigurerParent();
	}

}

/**
 * A JPanel which has some commonly used settings
 * 
 * @author Shu
 */
class JCustomPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public JCustomPanel() {
		super();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentY(TOP_ALIGNMENT);
		setAlignmentX(LEFT_ALIGNMENT);
	}

}