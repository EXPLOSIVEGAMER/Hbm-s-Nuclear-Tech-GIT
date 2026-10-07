package com.hbm.dim.hell;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

// @author iris-lgtm
// how the fuck do you do this
// helper methods for nether biomes
public class NetherBiomeHelper {

	public static int getFloorHeight(World world, int x, int z) {
		for(int y = 100; y > 0; y--) {
			Block b = world.getBlock(x, y, z);
			if(b == ModBlocks.bloatsprout_leaves) continue;
			if(b != Blocks.air)
				return y + 1;
		}
		return -1;
	}

	public static boolean isSolidFloor(World world, int x, int z) {
		int y = getFloorHeight(world, x, z);
		if(y <= 0) return false;
		Block b = world.getBlock(x, y - 1, z);
		return b == Blocks.netherrack || b == Blocks.soul_sand || b == ModBlocks.glyphid_base || b == ModBlocks.nether_glyphid || b == ModBlocks.certus_quartz_block || b == ModBlocks.frozen_netherrack || b == ModBlocks.certine_netherrack;
	}

	// used for things which do stack on caps
	public static boolean isSolidMass(World world, int x, int y, int z) {
		return !world.isAirBlock(x, y, z) && !world.isAirBlock(x, y + 1, z);
	}

	// used for things that dont stack on things like caps
	public static boolean isThickFloor(World world, int x, int z) {
		int y = getFloorHeight(world, x, z);
		if(y <= 2) return false;
		for(int dy = 1; dy <= 3; dy++) {
			Block b = world.getBlock(x, y - dy, z);
			if(b != Blocks.netherrack && b != Blocks.soul_sand && b != ModBlocks.glyphid_base && b != ModBlocks.nether_glyphid) return false;
		}
		return true;
	}

	public static boolean isLavaNearby(World world, int x, int y, int z) {
		for(int dy = 1; dy <= 5; dy++) {
			Block b = world.getBlock(x, y - dy, z);
			if(b == Blocks.lava) return true;
			if(b != Blocks.netherrack && b != ModBlocks.glyphid_base && b != ModBlocks.nether_glyphid && b != Blocks.soul_sand) return false;
		}
		return false;
	}

	public static boolean isFlatArea(World world, int x, int z, int r) {
		int h = getFloorHeight(world, x, z);
		for(int dx = -r; dx <= r; dx++)
			for(int dz = -r; dz <= r; dz++)
				if(Math.abs(getFloorHeight(world, x + dx, z + dz) - h) > 1)
					return false;
		return true;
	}

	private static double hash(int x, int y, int z, int seed) {
		int h = seed;
		h = h * 31 + x;
		h = h * 31 + y;
		h = h * 31 + z;
		h ^= h >> 13;
		h *= 0x5bd1e995;
		h ^= h >> 15;
		return (h & 0x7fffffff) / (double) 0x7fffffff;
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	private static double smooth(double t) {
		return t * t * (3D - 2D * t);
	}

	public static double noise3(double x, double y, double z, int seed) {
		int xi = (int) Math.floor(x), yi = (int) Math.floor(y), zi = (int) Math.floor(z);
		double fx = smooth(x - xi), fy = smooth(y - yi), fz = smooth(z - zi);

		double c000 = hash(xi, yi, zi, seed), c100 = hash(xi + 1, yi, zi, seed);
		double c010 = hash(xi, yi + 1, zi, seed), c110 = hash(xi + 1, yi + 1, zi, seed);
		double c001 = hash(xi, yi, zi + 1, seed), c101 = hash(xi + 1, yi, zi + 1, seed);
		double c011 = hash(xi, yi + 1, zi + 1, seed), c111 = hash(xi + 1, yi + 1, zi + 1, seed);

		double x00 = lerp(c000, c100, fx), x10 = lerp(c010, c110, fx);
		double x01 = lerp(c001, c101, fx), x11 = lerp(c011, c111, fx);
		return lerp(lerp(x00, x10, fy), lerp(x01, x11, fy), fz);
	}
}
