package com.hbm.entity.mob;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityCertcloud extends EntityFlying implements IMob {

	public static final int CAP = 20;

	public EntityCertcloud(World world) {
		super(world);
		this.setSize(1.8F, 1.4F);
		this.experienceValue = 5;
	}

	public static boolean isOverCap(World world) {
		int count = 0;
		for(Object entity : world.loadedEntityList) {
			if(entity instanceof EntityCertcloud && ++count >= CAP) return true;
		}
		return false;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(worldObj.isRemote) {
			for(int i = 0; i < 8; i++) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "certcloud");
				data.setDouble("scale", width * 7.0D);
				data.setDouble("mX", (rand.nextDouble() - 0.5D) * 0.01D);
				data.setDouble("mY", 0.005D + rand.nextDouble() * 0.01D);
				data.setDouble("mZ", (rand.nextDouble() - 0.5D) * 0.01D);
				data.setDouble("posX", posX + (rand.nextDouble() - 0.5D) * width);
				data.setDouble("posY", posY + rand.nextDouble() * height - height * 0.5D);
				data.setDouble("posZ", posZ + (rand.nextDouble() - 0.5D) * width);
				MainRegistry.proxy.effectNT(data);
			}
			return;
		}

		EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 32D);

		if(player != null) {
			double dx = player.posX - posX;
			double dy = (player.posY + 1D) - posY;
			double dz = player.posZ - posZ;
			double distance = Math.max(0.1D, Math.sqrt(dx * dx + dy * dy + dz * dz));

			if(distance > 2.0D) {
				motionX += (dx / distance) * 0.03D;
				motionY += (dy / distance) * 0.03D;
				motionZ += (dz / distance) * 0.03D;
			} else {
				afflict(player);
			}
		} else if(worldObj.getClosestPlayerToEntity(this, 64D) == null) {
			setDead();
			return;
		}

		motionX = clamp(motionX);
		motionY = clamp(motionY);
		motionZ = clamp(motionZ);
	}

	private static double clamp(double d) {
		return d < -0.35D ? -0.35D : (d > 0.35D ? 0.35D : d);
	}

	private void afflict(EntityPlayer player) {
		if(!ArmorRegistry.hasAllProtection(player, 3, HazardClass.PARTICLE_ULTRA_FINE)) {
			HbmLivingProps.incrementCertosis(player, 20);
		}
		if(!ArmorUtil.checkForHazmat(player)) {
			player.addPotionEffect(new PotionEffect(HbmPotion.damagedSkin.id, 200, 0));
		}
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL
				&& this.worldObj.checkNoEntityCollision(this.boundingBox)
				&& this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()
				&& !this.worldObj.isAnyLiquid(this.boundingBox)
				&& !isOverCap(this.worldObj);
	}

	@Override
	protected void dropFewItems(boolean byPlayer, int looting) {
		this.entityDropItem(new ItemStack(ModItems.quartz_crystal, 1 + rand.nextInt(2)), 0F);
	}

	@Override
	protected String getHurtSound() {
		return "mob.ghast.moan";
	}

	@Override
	protected String getDeathSound() {
		return "mob.ghast.death";
	}
}
