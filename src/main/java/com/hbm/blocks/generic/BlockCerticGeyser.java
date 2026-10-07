package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.entity.projectile.EntityCerticFlare;
import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockCerticGeyser extends BlockBase {

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BlockCerticGeyser() {
		super(Material.rock);
		this.setTickRandomly(true);
		this.setStepSound(ModSoundTypes.crystalBlock);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(RefStrings.MODID + ":crystal_block.certus");
		this.iconTop = reg.registerIcon(RefStrings.MODID + ":certus_flare");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 1 ? this.iconTop : this.blockIcon;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(!world.isRemote) spawnFlare(world, x, y, z);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		if(!world.isRemote && rand.nextInt(3) == 0) spawnFlare(world, x, y, z);
	}

	private void spawnFlare(World world, int x, int y, int z) {
		if(EntityCerticFlare.isOverCap(world)) return;

		EntityCerticFlare flare = new EntityCerticFlare(world, x + 0.5D, y + 1.2D, z + 0.5D);
		flare.motionY = 0.3D;
		world.spawnEntityInWorld(flare);
	}
}
