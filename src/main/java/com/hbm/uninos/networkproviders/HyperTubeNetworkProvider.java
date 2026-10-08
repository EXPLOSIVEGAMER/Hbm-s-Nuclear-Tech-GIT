package com.hbm.uninos.networkproviders;

import api.hbm.hypertube.HyperTubeNetwork;
import com.hbm.uninos.INetworkProvider;

public class HyperTubeNetworkProvider implements INetworkProvider<HyperTubeNetwork> {
	public static HyperTubeNetworkProvider PROVIDER = new HyperTubeNetworkProvider();

	@Override
	public HyperTubeNetwork provideNetwork() {
		return new HyperTubeNetwork();
	}
}
