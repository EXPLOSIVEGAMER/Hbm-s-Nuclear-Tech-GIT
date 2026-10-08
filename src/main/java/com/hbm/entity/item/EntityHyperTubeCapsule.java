package com.hbm.entity.item;

import api.hbm.hypertube.IHyperTubeConnector;
import api.hbm.hypertube.IHyperTubeReceiver;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class EntityHyperTubeCapsule extends Entity {

	private Entity entity;
	private double speed = 0.1;
	private List<BlockPos> path;
	private BlockPos origin;
	private BlockPos target;
	private double progress = 0;

	public EntityHyperTubeCapsule(World world) {
		super(world);
		this.setSize(0.8F, 0.8F);
		this.noClip = true;
	}

	public EntityHyperTubeCapsule(World world, List<BlockPos> path, BlockPos origin, BlockPos target, double speed) {
		this(world);
		this.path = path;
		this.origin = origin;
		this.target = target;
		this.speed = speed;

		BlockPos p = path.get(0);
		this.setPosition(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
	}

	@Override
	protected void entityInit() { }

	@Override
	public void onUpdate() {
		if (worldObj.isRemote) return;
		if (riddenByEntity == null && entity != null) { abort(); return; }
		if (path == null || riddenByEntity == null) { setDead(); return; }

		progress += speed;
		int i = (int) progress;

		if (i >= path.size() - 1) { arrive(); return; }

		BlockPos a = path.get(i), b = path.get(i + 1);

		TileEntity te = worldObj.getTileEntity(b.getX(), b.getY(), b.getZ());
		if (!(te instanceof IHyperTubeConnector)) { abort(); return; }

		double f = progress - i;
		double x = a.getX() + (b.getX() - a.getX()) * f + 0.5;
		double y = a.getY() + (b.getY() - a.getY()) * f + 0.5;
		double z = a.getZ() + (b.getZ() - a.getZ()) * f + 0.5;

		this.motionX = (b.getX() - a.getX()) * speed;
		this.motionY = (b.getY() - a.getY()) * speed;
		this.motionZ = (b.getZ() - a.getZ()) * speed;
		this.setPosition(x, y, z);
	}

	private void arrive() {
		Entity entity = riddenByEntity;
		entity.mountEntity(null);
		TileEntity te = worldObj.getTileEntity(target.getX(), target.getY(), target.getZ());
		if (te instanceof IHyperTubeReceiver) ((IHyperTubeReceiver) te).onArrive(entity, ForgeDirection.UNKNOWN);
		setDead();
	}

	private void abort() {
		Entity entity = riddenByEntity;
		entity.mountEntity(null);
		TileEntity te = worldObj.getTileEntity(origin.getX(), origin.getY(), origin.getZ());
		if (te instanceof IHyperTubeReceiver) ((IHyperTubeReceiver) te).onArrive(entity, ForgeDirection.UNKNOWN);
		setDead();
	}

	@Override public double getMountedYOffset() { return -0.5; }
	@Override public boolean canBeCollidedWith() { return false; }
	@Override public boolean shouldRiderSit() { return false; }

	@Override protected void readEntityFromNBT(NBTTagCompound nbtTagCompound) {}
	@Override protected void writeEntityToNBT(NBTTagCompound nbtTagCompound) {}
}
