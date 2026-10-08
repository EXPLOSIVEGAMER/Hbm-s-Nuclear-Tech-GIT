package api.hbm.hypertube;

import net.minecraftforge.common.util.ForgeDirection;

public interface IHyperTubeConnector {
	public default boolean canConnect(ForgeDirection dir) { return dir != ForgeDirection.UNKNOWN; }
}
