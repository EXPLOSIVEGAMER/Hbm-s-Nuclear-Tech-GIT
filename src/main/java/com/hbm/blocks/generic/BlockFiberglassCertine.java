package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockFiberglassCertine extends Block {

	public BlockFiberglassCertine() {
		super(Material.cloth);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if(entity instanceof EntityLivingBase) {
			entity.attackEntityFrom(DamageSource.cactus, 1.0F);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		super.breakBlock(world, x, y, z, block, meta);

		if(world.isRemote) return;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			int nx = x + dir.offsetX;
			int ny = y + dir.offsetY;
			int nz = z + dir.offsetZ;
			if(world.getBlock(nx, ny, nz) == Blocks.air && world.rand.nextInt(3) != 0) {
				world.setBlock(nx, ny, nz, ModBlocks.gas_certus);
			}
		}
	}
}
