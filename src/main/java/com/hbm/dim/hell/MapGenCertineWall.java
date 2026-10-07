package com.hbm.dim.hell;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.gen.MapGenBaseMeta;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class MapGenCertineWall extends MapGenBaseMeta {

	private static final int RADIUS = 2;
	private static final int GRID = 16 + RADIUS * 2;

	@Override
	public void func_151539_a(IChunkProvider provider, World world, int chunkX, int chunkZ, Block[] blocks) {
		if(world.provider.dimensionId != -1) return;

		int ox = chunkX << 4;
		int oz = chunkZ << 4;

		boolean[] certine = new boolean[GRID * GRID];
		for(int gx = -RADIUS; gx < 16 + RADIUS; gx++)
			for(int gz = -RADIUS; gz < 16 + RADIUS; gz++)
				certine[(gx + RADIUS) + (gz + RADIUS) * GRID] =
						world.getWorldChunkManager().getBiomeGenAt(ox + gx, oz + gz) == BiomeGenCertineCaverns.certineCaverns;

		Random rand = new Random((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L ^ world.getSeed());

		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				boolean here = certine[(x + RADIUS) + (z + RADIUS) * GRID];
				boolean edge = false;

				for(int dx = -RADIUS; dx <= RADIUS && !edge; dx++)
					for(int dz = -RADIUS; dz <= RADIUS && !edge; dz++)
						if(certine[(x + RADIUS + dx) + (z + RADIUS + dz) * GRID] != here)
							edge = true;

				if(!edge) continue;

				for(int y = 0; y < 128; y++) {
					int idx = (x * 16 + z) * 128 + y;
					int roll = rand.nextInt(4);
					if(roll == 0) {
						blocks[idx] = Blocks.packed_ice;
					} else if(roll == 1) {
						blocks[idx] = Blocks.snow;
					} else {
						blocks[idx] = ModBlocks.certus_quartz_block;
						if(metas != null) metas[idx] = (byte) ModBlocks.CERTUS_META;
					}
				}
			}
		}
	}
}
