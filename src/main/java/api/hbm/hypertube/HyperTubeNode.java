package api.hbm.hypertube;

import com.hbm.uninos.GenNode;
import com.hbm.uninos.networkproviders.HyperTubeNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;

public class HyperTubeNode extends GenNode<HyperTubeNetwork> {

	public HyperTubeNode(BlockPos... pos) {
		super(HyperTubeNetworkProvider.PROVIDER, pos);
	}
}
