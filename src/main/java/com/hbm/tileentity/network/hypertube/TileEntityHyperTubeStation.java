package com.hbm.tileentity.network.hypertube;

import api.hbm.hypertube.HyperTubeNode;
import api.hbm.hypertube.IHyperTubeConnector;
import api.hbm.hypertube.IHyperTubeReceiver;
import api.hbm.hypertube.IHyperTubeTransceiver;
import com.hbm.entity.item.EntityHyperTubeCapsule;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.HyperTubeNetworkProvider;
import com.hbm.uninos.NetworkPathFinder;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//todo: add Power consumption + Compressed Air input
public class TileEntityHyperTubeStation extends TileEntityLoadedBase implements IHyperTubeTransceiver, IControlReceiver, IGUIProvider {
	private String name = "";
	public List<Destination> destinations = new ArrayList<>();

	@Override
	public String getStationName() {
		return name;
	}

	private void setStationName(String name) {
		this.name = name;
		this.markDirty();
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			if (name.isEmpty()) name = "Station: " + xCoord + "," + yCoord + "," + zCoord;

			if (worldObj.getTotalWorldTime() % 20 == 0) {
				DirPos pos = getTubeConnection();
				trySubscribe(worldObj, pos);
				tryProvide(worldObj, pos);
				updateDestinations();
			}

			this.networkPackNT(25);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!worldObj.isRemote) {
			DirPos pos = getTubeConnection();
			tryUnsubscribe(worldObj, pos);
			tryUnprovide(worldObj, pos);
		}
	}

	public boolean startTravel(Entity entity, IHyperTubeReceiver target) {
		if(entity.ridingEntity != null || target == this || !target.canAccept(entity)) return false;

		HyperTubeNode node = getTubeNode();
		if(node == null || !node.hasValidNet()) return false;
		if(!node.net.receiverEntries.containsKey(target)) return false; // target must be on the same network

		List<BlockPos> tubes = NetworkPathFinder.BFS.findPath(worldObj, getTubeConnection(), target.getTubeConnection(), HyperTubeNetworkProvider.PROVIDER);
		if(tubes == null) return false;

		BlockPos self = new BlockPos(this), dest = new BlockPos((TileEntity) target);
		List<BlockPos> path = new ArrayList<>();
		path.add(self);
		path.addAll(tubes);
		path.add(dest);

		EntityHyperTubeCapsule capsule = new EntityHyperTubeCapsule(worldObj, path, self, dest, 0.1); //todo: Based on Air Pressure
		worldObj.spawnEntityInWorld(capsule);
		entity.mountEntity(capsule);
		return true;
	}

	@Override
	public DirPos getTubeConnection() {
		ForgeDirection dir = getFacing();
		return new DirPos(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, dir);
	}

	public HyperTubeNode getTubeNode() {
		DirPos tube = getTubeConnection();
		return (HyperTubeNode) UniNodespace.getNode(worldObj, tube.getX(), tube.getY(), tube.getZ(), HyperTubeNetworkProvider.PROVIDER);
	}

	private void updateDestinations() {
		destinations.clear();
		HyperTubeNode node = getTubeNode();
		if (node == null || !node.hasValidNet()) return;

		for (IHyperTubeReceiver r : node.net.receiverEntries.keySet()) {
			if (r == this) continue;
			TileEntity te = (TileEntity) r;
			destinations.add(new Destination(r.getStationName(), te.xCoord, te.yCoord, te.zCoord));
		}

		destinations.sort(Comparator.comparingDouble(d -> {
			double dx = d.x - xCoord, dy = d.y - yCoord, dz = d.z - zCoord;
			return dx * dx + dy * dy + dz * dz;
		}));
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir == getFacing();
	}

	public ForgeDirection getFacing() {
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public boolean canAccept(Entity entity) {
		return entity instanceof EntityLivingBase;
	}

	@Override
	public void onArrive(Entity entity, ForgeDirection from) {
		double x = xCoord + 0.5, y = yCoord + 1, z = zCoord + 0.5;
		if (entity instanceof EntityPlayerMP) ((EntityPlayerMP) entity).setPositionAndUpdate(x, y, z);
		else entity.setPosition(x, y, z);
		entity.fallDistance = 0;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if (data.hasKey("name")) {
			String n = data.getString("name").trim();
			if (!n.isEmpty() && n.length() <= 32) setStationName(n);
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null; //todo: add gui for destination selection
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		ByteBufUtils.writeUTF8String(buf, name);
		buf.writeInt(destinations.size());
		for (Destination d : destinations) {
			ByteBufUtils.writeUTF8String(buf, d.name);
			buf.writeInt(d.x); buf.writeInt(d.y); buf.writeInt(d.z);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.name = ByteBufUtils.readUTF8String(buf);
		int size = buf.readInt();
		List<Destination> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			list.add(new Destination(ByteBufUtils.readUTF8String(buf), buf.readInt(), buf.readInt(), buf.readInt()));
		}
		this.destinations = list;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("name", this.name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.name = nbt.getString("name");
	}

	public static class Destination {
		public final String name;
		public final int x, y, z;

		public Destination(String name, int x, int y, int z) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
