package ca.shu.ui.lib.world;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;

import ca.shu.ui.lib.world.impl.WorldGround;
import ca.shu.ui.lib.world.impl.WorldObjectImpl;
import ca.shu.ui.lib.world.impl.WorldSky;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PTransformActivity;

public interface World {
	public boolean containsNode(PNode node);

	public PTransformActivity zoomToFit();

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener);

	public PTransformActivity zoomToNode(WorldObject node);

	public void setCameraCenterPosition(double x, double y);

	public void showTooltip(WorldObjectImpl pControls,
			WorldObjectImpl nodeAttacedTo);

	public void hideControls();

	public Point2D getPositionInGround(WorldObject wo);

	public Point2D getPositionInSky(WorldObjectImpl wo);

	public WorldGround getGround();

	public WorldSky getSky();

	public double getGroundScale();

	public boolean addActivity(PActivity activity);

	public Point2D skyToGround(Point2D position);

	public double getScreenWidth();

	public double getScreenHeight();

	public void setWorldScale(float scale);

	public String getName();

	// public void createGrid();
}