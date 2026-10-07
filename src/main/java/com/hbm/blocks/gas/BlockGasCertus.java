package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGasCertus extends BlockGasBase {

	public BlockGasCertus() {
		super(0.5F, 0.8F, 0.9F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		super.randomDisplayTick(world, x, y, z, rand);
		if(rand.nextInt(4) == 0)
			world.spawnParticle("crit", x + rand.nextFloat(), y + rand.nextFloat(), z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		if(entity instanceof EntityLivingBase) {

			EntityLivingBase living = (EntityLivingBase) entity;

			if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.PARTICLE_ULTRA_FINE))
				ArmorUtil.damageGasMaskFilter(living, 1);
			else
				HbmLivingProps.incrementCertosis(living, 10);
		}
	}

	@Override
	public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

		if(world.rand.nextInt(5) == 0)
			return ForgeDirection.DOWN;

		return ForgeDirection.getOrientation(world.rand.nextInt(6));
	}

	@Override
	public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {

		if(!world.isRemote && rand.nextInt(40) == 0) {
			world.setBlockToAir(x, y, z);
			return;
		}

		super.updateTick(world, x, y, z, rand);
	}
}
