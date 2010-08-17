/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NodeContainerIcon.java". Description: 
"Icon for a Node Container"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

package ca.nengo.ui.models.icons;

import ca.nengo.ui.lib.Style.Style;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.primitives.Text;
import ca.nengo.ui.models.nodes.UINodeViewable;

/**
 * Icon for a Node Container. The size of this icon scales depending on the
 * number of nodes contained by the model.
 * 
 * @author Shu
 */
public abstract class NodeContainerIcon extends ModelIcon {

	private static final long serialVersionUID = 1L;

	public static final float MAX_SCALE = 1.5f;

	public static final float MIN_SCALE = 0.5f;

	private int myNumOfNodes = -1;

	private final Text sizeLabel;

	public NodeContainerIcon(UINodeViewable parent, WorldObject icon) {
		super(parent, icon);
		sizeLabel = new Text("");
		sizeLabel.setFont(Style.FONT_SMALL);
		sizeLabel.setConstrainWidthToTextWidth(true);
		addChild(sizeLabel);
		layoutChildren();
		modelUpdated();
	}

	/**
	 * Scales the icon display size depending on how many nodes are contained
	 * within it
	 */
	private void updateIconScale() {

		int numOfNodes = getModelParent().getNodesCount();

		if (myNumOfNodes == numOfNodes) {
			return;
		}
		myNumOfNodes = numOfNodes;

		sizeLabel.setText(myNumOfNodes + " nodes");

		float numOfNodesNormalized;
		if (numOfNodes >= getNodeCountNormalization())
			numOfNodesNormalized = 1;
		else {
			numOfNodesNormalized = (float) Math.sqrt((float) numOfNodes
					/ (float) getNodeCountNormalization());
		}

		float scale = MIN_SCALE
				+ (numOfNodesNormalized * (MAX_SCALE - MIN_SCALE));

		getIconReal().setScale(scale);

	}

	protected abstract int getNodeCountNormalization();

	@Override
	public void layoutChildren() {
		super.layoutChildren();

		sizeLabel.setOffset(0, -(sizeLabel.getHeight() + 1));

		sizeLabel.moveToFront();
	}

	@Override
	protected void modelUpdated() {
		super.modelUpdated();
		updateIconScale();
	}

	@Override
	public UINodeViewable getModelParent() {

		return (UINodeViewable) super.getModelParent();
	}

}
