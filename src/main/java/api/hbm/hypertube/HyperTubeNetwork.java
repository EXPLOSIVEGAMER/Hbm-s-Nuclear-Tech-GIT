package api.hbm.hypertube;

import com.hbm.uninos.NodeNet;

public class HyperTubeNetwork extends NodeNet<IHyperTubeReceiver, IHyperTubeProvider, HyperTubeNode> {
	public int version = 0;

	@Override
	public void update() {
		long now = System.currentTimeMillis();
		receiverEntries.entrySet().removeIf(e -> now - e.getValue() > 3000 || isBadLink(e.getKey()));
		providerEntries.entrySet().removeIf(e -> now - e.getValue() > 3000 || isBadLink(e.getKey()));
	}

	@Override
	public NodeNet forceJoinLink(HyperTubeNode node) {
		version++;
		return super.forceJoinLink(node);
	}

	@Override
	public void joinNetworks(NodeNet network) {
		version++;
		super.joinNetworks(network);
	}
}
