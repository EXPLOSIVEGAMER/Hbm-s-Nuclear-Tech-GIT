package api.hbm.hypertube;

import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IHyperTube extends IHyperTubeConnector {
	public default HyperTubeNode createNode() {
		TileEntity te = (TileEntity) this;
		ForgeDirection[] ends = HyperTubeShape.getEnds(te.getBlockMetadata());
		return (HyperTubeNode) new HyperTubeNode(new BlockPos(te)).setConnections(
			new DirPos(te.xCoord + ends[0].offsetX, te.yCoord + ends[0].offsetY, te.zCoord + ends[0].offsetZ, ends[0]),
			new DirPos(te.xCoord + ends[1].offsetX, te.yCoord + ends[1].offsetY, te.zCoord + ends[1].offsetZ, ends[1])
		);
	}

	@Override
	default boolean canConnect(ForgeDirection dir) {
		return HyperTubeShape.isOpen(((TileEntity) this).getBlockMetadata(), dir);
	}
}
