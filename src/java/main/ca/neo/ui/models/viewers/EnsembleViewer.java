package ca.neo.ui.models.viewers;

import ca.neo.model.Ensemble;
import ca.neo.model.Node;
import ca.neo.model.Probeable;
import ca.neo.model.neuron.Neuron;
import ca.neo.ui.models.PNeoNode;
import ca.neo.ui.models.nodes.PEnsemble;
import ca.neo.ui.models.nodes.PNeuron;
import ca.neo.util.Probe;
import ca.shu.ui.lib.util.Util;

public class EnsembleViewer extends NodeViewer {

	private static final long serialVersionUID = 1L;

	public EnsembleViewer(PEnsemble ensembleUI) {
		super(ensembleUI);
	}

	@Override
	public void applyDefaultLayout() {
		applySquareLayout();

	}

	@Override
	public Ensemble getModel() {

		return (Ensemble) super.getModel();
	}

	@Override
	public PEnsemble getViewerParent() {
		return (PEnsemble) super.getViewerParent();
	}

	@Override
	public void updateViewFromModel() {

		Node[] nodes = getModel().getNodes();

		/*
		 * Construct Neurons
		 */
		for (Node node : nodes) {
			if (node instanceof Neuron) {
				Neuron neuron = (Neuron) node;

				PNeuron neuronUI = new PNeuron(neuron);

				addNeoNode(neuronUI, false, false, false);
			} else {
				Util.UserError("Unsupported node type "
						+ node.getClass().getSimpleName()
						+ " in EnsembleViewer");
			}

		}

		if (getViewerParent().getParentNetwork() != null) {
			/*
			 * Construct probes
			 */
			Probe[] probes = getViewerParent().getParentNetwork()
					.getSimulator().getProbes();

			for (Probe probe : probes) {
				Probeable target = probe.getTarget();

				if (!(target instanceof Node)) {
					Util.UserError("Unsupported target type for probe");
				} else {

					if (probe.isInEnsemble()
							&& probe.getEnsembleName() == getModel().getName()) {
						Node node = (Node) target;

						PNeoNode nodeUI = getNode(node.getName());
						nodeUI.showProbe(probe);
					}
				}

			}
		}

	}

}
