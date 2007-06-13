package ca.neo.ui.views.objects.properties;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.text.SimpleAttributeSet;

import ca.neo.math.Function;
import ca.neo.math.impl.ConstantFunction;
import ca.neo.math.impl.FourierFunction;
import ca.neo.math.impl.GaussianPDF;
import ca.sw.util.Util;

public class PTFunction extends PropertySchema {

	public PTFunction(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PropertyInputPanel createInputPanel() {
		// TODO Auto-generated method stub
		return new FunctionInputPanel(this);
	}

	@Override
	public Class<Function> getTypeClass() {
		// TODO Auto-generated method stub
		return Function.class;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "Function";
	}

}

class FunctionInputPanel extends PropertyInputPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Function function = null;

	/*
	 * TODO: make meta properties static to save memory
	 * 
	 */
	static final FnSchema[] functions = new FnSchema[] {

			new FnSchema(ConstantFunction.class, new PropertySchema[] {
					new PTInt("Dimension"), new PTFloat("Value") }),
			new FnSchema(FourierFunction.class, new PropertySchema[] {
					new PTFloat("Fundamental"), new PTFloat("Cutoff"),
					new PTFloat("RMS") }),

			new FnSchema(GaussianPDF.class, new PropertySchema[] {
					new PTFloat("Mean"), new PTFloat("Variance"),
					new PTFloat("Peak") }), };

	JComboBox comboBox;

	public FunctionInputPanel(PropertySchema property) {
		super(property);
		// TODO Auto-generated constructor stub
		setValue(null);
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return function;
	}

	@Override
	public void init(JPanel panel) {
		comboBox = new JComboBox(functions);
		JButton configureFunction = new JButton(new SetParametersAction());

		panel.add(comboBox);
		panel.add(configureFunction);
	}

	@Override
	public boolean isValueSet() {
		if (function != null)
			return true;
		else
			return false;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Function) {
			function = (Function) value;
			setStatusMsg("");

		} else {
			setStatusMsg("function parameters not set");
		}

	}

	protected void setParameters() {
		FnSchema fnDescriptor = (FnSchema) comboBox.getSelectedItem();

		if (fnDescriptor == null)
			return;

		FnProxy functionProxy = new FnProxy(this, fnDescriptor
				.getFunctionClass(), fnDescriptor.getMetaProperties());

		//		
		// Constructor<?>[] constructors =
		// functionWr.getClass().getConstructors();
		// // constructors[0].

		Container parent = getParent();
		while (parent != null) {
			if (parent instanceof JDialog) {
				new PropertiesDialog((JDialog) parent, functionProxy);
				return;
			}
			parent = parent.getParent();
		}
		Util.Error("Could not attach properties dialog");
	}

	class SetParametersAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SetParametersAction() {
			super("Set Parameters");
			// TODO Auto-generated constructor stub
		}

		public void actionPerformed(ActionEvent e) {
			setParameters();

		}

	}

}

/*
 * Schema for describing a function
 */
class FnSchema {
	Class functionClass;

	PropertySchema[] metaProperties;

	public FnSchema(Class functionClass, PropertySchema[] metaProperties) {
		super();
		this.functionClass = functionClass;
		this.metaProperties = metaProperties;
	}

	public Class getFunctionClass() {
		return functionClass;
	}

	public PropertySchema[] getMetaProperties() {
		return metaProperties;
	}

	@Override
	public String toString() {
		return functionClass.getSimpleName();
	}
	
	

}

class FnProxy implements IPropertiesConfigurable {
	Class functionType;

	PropertySchema[] metaProperties;

	String name;

	SimpleAttributeSet properties;

	Function function;

	FunctionInputPanel inputPanel;

	public FnProxy(FunctionInputPanel inputPanel, Class functionType,
			PropertySchema[] propertyTypes) {
		super();
		this.metaProperties = propertyTypes;
		properties = new SimpleAttributeSet();

		this.inputPanel = inputPanel;
		this.functionType = functionType;
		this.name = functionType.getSimpleName();
		

	}

	public void configurationCancelled() {
		// TODO Auto-generated method stub

	}

	public void configurationComplete() {

		/*
		 * Create function using Java reflection
		 */
		Class partypes[] = new Class[metaProperties.length];
		for (int i = 0; i < metaProperties.length; i++) {

			partypes[i] = metaProperties[i].getTypeClass();

		}
		Constructor ct = null;
		try {
			ct = functionType.getConstructor(partypes);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (ct == null) {
			return;
		}

		Object arglist[] = new Object[metaProperties.length];
		for (int i = 0; i < metaProperties.length; i++) {
			arglist[i] = getProperty(metaProperties[i].getName());
		}
		Object retobj = null;
		try {
			retobj = ct.newInstance(arglist);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (retobj != null) {
			inputPanel.setValue(retobj);
		}
	}

	public PropertySchema[] getMetaProperties() {
		return metaProperties;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public Object getProperty(String name) {
		return properties.getAttribute(name);
	}

	public Class getType() {
		return functionType;
	}

	public void setProperty(String name, Object value) {

		properties.addAttribute(name, value);

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}

	public Function getFunction() {
		return function;
	}

}
