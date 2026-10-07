package com.hbm.entity.projectile;

import com.hbm.items.weapon.sedna.factory.XFactoryEnergy;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityCerticFlare extends EntityThrowable {

	public static final int CAP = 12;

	private int fireCooldown = 40;

	public EntityCerticFlare(World world) {
		super(world);
		this.setSize(0.6F, 0.6F);
	}

	public EntityCerticFlare(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(0.6F, 0.6F);
	}

	public static boolean isOverCap(World world) {
		int count = 0;
		for(Object entity : world.loadedEntityList) {
			if(entity instanceof EntityCerticFlare && ++count >= CAP) return true;
		}
		return false;
	}

	@Override
	protected float getGravityVelocity() {
		return 0F;
	}

	@Override
	public void onUpdate() {
		motionX *= 0.9D;
		motionZ *= 0.9D;
		if(this.ticksExisted > 24) motionY *= 0.8D;

		super.onUpdate();

		if(worldObj.isRemote) {
			return;
		}

		if(--fireCooldown <= 0) {
			EntityPlayer target = worldObj.getClosestPlayerToEntity(this, 24D);

			if(target != null) {
				fire(target);
				fireCooldown = 60 + rand.nextInt(60);
			} else {
				fireCooldown = 20;
			}
		}

		if(ticksExisted > 600) setDead();
	}

	private void fire(EntityLivingBase target) {
		double dx = target.posX - posX;
		double dy = (target.posY + target.getEyeHeight() * 0.5D) - posY;
		double dz = target.posZ - posZ;
		double len = Math.max(0.001D, Math.sqrt(dx * dx + dy * dy + dz * dz));
		dx /= len;
		dy /= len;
		dz /= len;

		double offset = 1.5D;
		EntityBulletBeamBase beam = new EntityBulletBeamBase(worldObj, XFactoryEnergy.energy_las_certic, 4F);
		beam.setLocationAndAngles(posX + dx * offset, posY + dy * offset, posZ + dz * offset, 0F, 0F);
		beam.setRotationsFromVector(Vec3.createVectorHelper(dx, dy, dz));
		beam.beamLength = len;
		beam.performHitscanExternal(250D);
		worldObj.spawnEntityInWorld(beam);
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) { }

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}
}
