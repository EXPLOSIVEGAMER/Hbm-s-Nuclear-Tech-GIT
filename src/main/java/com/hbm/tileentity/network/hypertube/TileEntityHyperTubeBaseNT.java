package com.hbm.tileentity.network.hypertube;

import api.hbm.hypertube.HyperTubeNode;
import api.hbm.hypertube.IHyperTube;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.HyperTubeNetworkProvider;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityHyperTubeBaseNT extends TileEntityLoadedBase implements IHyperTube {
	public HyperTubeNode node;

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			if (node == null || node.expired) {
				node = (HyperTubeNode) UniNodespace.getNode(worldObj, xCoord, yCoord, zCoord, HyperTubeNetworkProvider.PROVIDER);

				if (node == null || node.expired) {
					if (shouldCreateNode()) {
						node = this.createNode();
						UniNodespace.createNode(worldObj, node);
					}
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!worldObj.isRemote && node != null) {
			UniNodespace.destroyNode(worldObj, xCoord ,yCoord, zCoord, HyperTubeNetworkProvider.PROVIDER);
		}
	}

	public ForgeDirection getFacing() { return ForgeDirection.getOrientation(this.getBlockMetadata()); }

	public boolean shouldCreateNode() { return true; }

	public void rebuildNode() {
		if (!worldObj.isRemote && node != null) {
			UniNodespace.destroyNode(worldObj, xCoord, yCoord, zCoord, HyperTubeNetworkProvider.PROVIDER);
			node = null;
		}
	}
}
