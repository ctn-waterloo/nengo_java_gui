package ca.neo.ui.configurable;

import java.io.Serializable;

/**
 * Describes a configuration parameter of a IConfigurable object
 */
/**
 * @author Shu
 */
public abstract class PropertyDescriptor implements Serializable {

	private Object defaultValue = null;

	private String name;

	private boolean isEnabled = true;

	/**
	 * @param name
	 *            Name to be given to the parameter
	 */
	public PropertyDescriptor(String name) {
		super();
		this.name = name;

	}

	/**
	 * @param name
	 *            Name to be given to the parameter
	 * @param defaultValue
	 *            Default value of this parameter
	 */
	public PropertyDescriptor(String name, Object defaultValue) {
		super();
		this.defaultValue = defaultValue;
		this.name = name;

	}

	/**
	 * Sets whether this property can be changed from its default value
	 * 
	 * @param bool
	 */
	public void setEditable(boolean bool) {
		isEnabled = bool;
	}

	/**
	 * Gets the input panel.
	 */
	public PropertyInputPanel getInputPanel() {
		// Instantiate a new input panel for each call, this is ok.
		PropertyInputPanel inputPanel = createInputPanel();
		inputPanel.setEnabled(isEnabled);
		return inputPanel;
	}

	/**
	 * @return UI Input panel which can be used for User Configuration of this
	 *         property, null if none exists
	 */
	protected abstract PropertyInputPanel createInputPanel();

	/**
	 * @return Default value of this parameter
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return Name of this parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Class type that this parameter's value must be
	 */
	@SuppressWarnings("unchecked")
	public abstract Class getTypeClass();

	/**
	 * @return A name given to the Class type of this parameter's value
	 */
	public abstract String getTypeName();

	@Override
	public String toString() {
		return getTypeName();
	}

}
