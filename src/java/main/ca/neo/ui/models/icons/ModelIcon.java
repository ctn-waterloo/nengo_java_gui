package ca.neo.ui.models.icons;

import ca.shu.ui.lib.objects.models.ModelObject;
import ca.shu.ui.lib.world.EventListener;
import ca.shu.ui.lib.world.IWorldObject;
import ca.shu.ui.lib.world.piccolo.WorldObjectImpl;
import ca.shu.ui.lib.world.piccolo.primitives.Text;

/**
 * An Icon which has a representation and an label. It is used to represent NEO
 * models.
 * 
 * @author Shu Wu
 */
public class ModelIcon extends WorldObjectImpl implements EventListener {

	private static final long serialVersionUID = 1L;

	/**
	 * The inner icon node which contains the actual icon representation
	 */
	private IWorldObject iconReal;

	/**
	 * Label of the icon
	 */
	private Text label;

	/**
	 * Parent of this icon
	 */
	private ModelObject parent;

	/**
	 * Whether to show the type of model in the label
	 */
	private boolean showTypeInLabel = true;

	/**
	 * @param parent
	 *            The Model the icon is representing
	 * @param icon
	 *            the UI representation
	 * @param scale
	 *            Scale of the Icon
	 */
	public ModelIcon(ModelObject parent, IWorldObject icon) {
		super();
		this.parent = parent;
		this.iconReal = icon;

		setSelectable(false);
		addChild(icon);
		// icon.setSelectable(false);
		// icon.setChildrenPickable(false);

		label = new Text();
		label.setConstrainWidthToTextWidth(true);
		updateLabel();
		addChild(label);

		// if (icon instanceof WorldObject) {
		// ((IWorldObject) icon).setSelectable(false);
		// }

		// parent.addPropertyChangeListener(PROPERTY_NAME, this);
		parent.addPropertyChangeListener(EventType.MODEL_CHANGED, this);
		setSelectable(false);

		/*
		 * The bounds of this object matches those of the real icon
		 */
		iconReal.addPropertyChangeListener(EventType.FULL_BOUNDS, this);
		updateBounds();
	}

	/**
	 * Updates the bounds of this node based on the inner icon
	 */
	private void updateBounds() {
		setBounds(iconReal.localToParent(iconReal.getBounds()));
	}

	protected IWorldObject getIconReal() {
		return iconReal;
	}

	protected ModelObject getModelParent() {
		return parent;
	}

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		/*
		 * Layout the icon and label
		 */
		double iconWidth = getWidth() * getScale();
		double labelWidth = label.getWidth();
		double offsetX = ((labelWidth - iconWidth) / 2.0) * -1;

		label.setOffset(offsetX, getHeight() * getScale());

	}

	/**
	 * Called when the NEO model has been updated
	 */
	protected void modelUpdated() {
		updateLabel();
	}

	/**
	 * Configures the label
	 * 
	 * @param showType
	 *            Whether to show the model type in the label
	 */
	public void configureLabel(boolean showType) {
		showTypeInLabel = showType;
		updateLabel();
	}

	@Override
	public void doubleClicked() {
		parent.doubleClicked();
	}

	// @Override
	// public void signalBoundsChanged() {
	//
	// super.signalBoundsChanged();
	//
	// /*
	// * Pass on the message to icon
	// */
	// iconReal.signalBoundsChanged();
	// }

	/**
	 * @return the name of the label
	 */
	@Override
	public String getName() {
		return label.getText();
	}

	/**
	 * @param isVisible
	 *            Whether the label is visible
	 */
	public void setLabelVisible(boolean isVisible) {
		if (isVisible) {
			addChild(label);
		} else {
			if (label.getParent() != null)
				label.removeFromParent();

		}
	}

	/**
	 * Updates the label text
	 */
	public void updateLabel() {
		if (showTypeInLabel) {
			if (parent.getName().compareTo("") == 0)
				label.setText("unnamed " + parent.getTypeName());
			else
				label.setText(parent.getName() + " (" + parent.getTypeName()
						+ ")");
		} else {
			if (parent.getName().compareTo("") == 0)
				label.setText("unnamed");
			else
				label.setText(parent.getName());
		}
	}

	public void propertyChanged(EventType event) {

		if (event == EventType.FULL_BOUNDS) {
			updateBounds();
		} else if (event == EventType.MODEL_CHANGED) {
			modelUpdated();
		}
	}

}