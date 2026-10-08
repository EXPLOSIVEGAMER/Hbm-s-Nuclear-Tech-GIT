package api.hbm.hypertube;

import api.hbm.tile.ILoadedTile;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.HyperTubeNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IHyperTubeReceiver extends IHyperTubeUser {
	String getStationName();
	boolean canAccept(Entity entity);
	void onArrive(Entity entity, ForgeDirection from);
	DirPos getTubeConnection();

	public default void trySubscribe(World world, DirPos pos) { trySubscribe(world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir()); }

	public default void trySubscribe(World world, int x, int y, int z, ForgeDirection dir) {
		TileEntity te = ILoadedTile.TileAccessCache.getTileOrCache(world, x, y, z);

		boolean red = false;
		if (te instanceof IHyperTubeConnector) {
			IHyperTubeConnector con = (IHyperTubeConnector) te;
			if (!con.canConnect(dir.getOpposite())) return;

			GenNode node = UniNodespace.getNode(world, x, y, z, HyperTubeNetworkProvider.PROVIDER);
			if (node != null && node.net != null) {
				node.net.addReceiver(this);
				red = true;
			}
		}

		if (particleDebug) {
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "network");
			data.setString("mode", "hypertube");
			double posX = x + 0.5 + dir.offsetX * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
			double posY = y + 0.5 + dir.offsetY * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
			double posZ = z + 0.5 + dir.offsetZ * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
			data.setDouble("mX", -dir.offsetX * (red ? 0.025 : 0.1));
			data.setDouble("mY", -dir.offsetY * (red ? 0.025 : 0.1));
			data.setDouble("mZ", -dir.offsetZ * (red ? 0.025 : 0.1));
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, posX, posY, posZ), new NetworkRegistry.TargetPoint(world.provider.dimensionId, posX, posY, posZ, 25));
		}
	}

	public default void tryUnsubscribe(World world, BlockPos pos) { tryUnsubscribe(world, pos.getX(), pos.getY(), pos.getZ()); }

	public default void tryUnsubscribe(World world, int x, int y, int z) {
		GenNode node = UniNodespace.getNode(world, x, y, z, HyperTubeNetworkProvider.PROVIDER);
		if (node != null && node.net != null) {
			node.net.removeReceiver(this);
		}
	}
}
