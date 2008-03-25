package ca.nengo.ui.actions;

import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.nodes.widgets.UIProbe;
import ca.shu.ui.lib.actions.ActionException;
import ca.shu.ui.lib.actions.ReversableAction;
import ca.shu.ui.lib.objects.models.ModelObject;
import ca.shu.ui.lib.util.UIEnvironment;
import ca.shu.ui.lib.util.UserMessages;

/**
 * Action for adding probes to a collection of nodes
 * 
 * @author Shu Wu
 */
public class AddProbesAction extends ReversableAction {

	private static final long serialVersionUID = 1;

	private HashMap<UINeoNode, UIProbe> myCreatedProbesMap;

	private Collection<ModelObject> myNodes;

	public AddProbesAction(Collection<ModelObject> nodes) {
		super("Add probes");

		this.myNodes = nodes;

	}

	@Override
	protected void action() throws ActionException {
		myCreatedProbesMap = new HashMap<UINeoNode, UIProbe>(myNodes
				.size());

		String stateName = JOptionPane.showInputDialog(UIEnvironment
				.getInstance(), "State name to probe (Case Sensitive): ",
				"Adding probes", JOptionPane.QUESTION_MESSAGE);

		if (stateName != null && !stateName.equals("")) {
			int successCount = 0;
			int failed = 0;

			for (ModelObject model : myNodes) {
				if (model instanceof UINeoNode) {
					UINeoNode node = (UINeoNode) model;
					UIProbe probeCreated;

					try {
						probeCreated = node.addProbe(stateName);
						myCreatedProbesMap.put(node, probeCreated);
						successCount++;
					} catch (SimulationException e) {

						failed++;
					}

				}

			}
			if (failed > 0) {
				UserMessages
						.showWarning(successCount
								+ " probes were successfully added. <BR> However it was not added to "
								+ failed
								+ " nodes. The state name specified may not exist on those nodes.");
			}
		}

	}

	@Override
	protected void undo() throws ActionException {
		for (ModelObject model : myNodes) {
			if (model instanceof UINeoNode) {
				UINeoNode node = (UINeoNode) model;
				UIProbe probeCreated = myCreatedProbesMap.get(node);
				node.removeProbe(probeCreated);
			}

		}

	}

}