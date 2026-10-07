package com.hbm.dim.hell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.hbm.blocks.BlockEnums;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.entity.mob.EntityCertcloud;
import com.hbm.entity.mob.EntityCertmite;
import com.hbm.entity.projectile.EntityCerticFlare;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.ForgeDirection;

public class BiomeGenCertineCaverns extends NetherBiomeBase {

	public static BiomeGenCertineCaverns certineCaverns;

	public static void init() {
		certineCaverns = (BiomeGenCertineCaverns) new BiomeGenCertineCaverns(SpaceConfig.certineCavernsBiome).setBiomeName("Certine Caverns");
		BiomeDictionary.registerBiomeType(certineCaverns, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.SPOOKY);
	}

	public BiomeGenCertineCaverns(int id) {
		super(id);
		this.topBlock = ModBlocks.certus_quartz_block;
		this.fillerBlock = ModBlocks.certus_quartz_block;
		this.setColor(0x86C9E8);
		this.setTemperatureRainfall(2.0F, 0.0F);
		this.rootHeight = 0.0F;
		this.heightVariation = 0.05F;

		this.spawnableMonsterList.clear();
		this.spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityCertmite.class, 12, 3, 6));
		this.spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityCertcloud.class, 10, 1, 3));
	}

	@Override
	public void decorate(World world, Random rand, int chunkX, int chunkZ) {
		int ox = chunkX, oz = chunkZ;

		int[] floor = new int[16 * 16];
		boolean[] ok = new boolean[16 * 16];
		for(int x = 0; x < 16; x++)
			for(int z = 0; z < 16; z++) {
				int wx = ox + x, wz = oz + z;
				int y = NetherBiomeHelper.getFloorHeight(world, wx, wz);
				int idx = x + z * 16;
				floor[idx] = y;
				ok[idx] = y > 0 && y < 126 && world.isAirBlock(wx, y, wz) && world.isAirBlock(wx, y + 1, wz)
						&& isColumnClear(world, wx, y, wz);
			}

		List<int[]> patches = generatePockets(world, rand, ox, oz);

		for(int i = 0; i < 3; i++) {
			int[] a = anchor(world, floor, ok, ox, oz, rand, 4);
			if(a == null) continue;
			generateMonolith(world, rand, a[0], a[1], a[2], 32 + rand.nextInt(40), 5 + rand.nextInt(6));
		}

		for(int i = 0; i < 8; i++) {
			int[] a = anchor(world, floor, ok, ox, oz, rand, 2);
			if(a == null) continue;
			if(rand.nextBoolean()) generateSpike(world, rand, a[0], a[1], a[2], 16 + rand.nextInt(26), 3 + rand.nextInt(3));
			else generateSlantedSpike(world, rand, a[0], a[1], a[2], 16 + rand.nextInt(26));
		}

		for(int i = 0; i < 3; i++) {
			int[] a = anchor(world, floor, ok, ox, oz, rand, 2);
			if(a == null) continue;
			generateSpire(world, rand, a[0], a[1], a[2], 26 + rand.nextInt(30), 4 + rand.nextInt(4));
		}

		for(int i = 0; i < 3; i++) {
			int[] a = anchor(world, floor, ok, ox, oz, rand, 2);
			if(a == null) continue;
			int x = a[0], y = a[1], z = a[2];
			int height = 10 + rand.nextInt(16);
			for(int dy = 0; dy < height; dy++)
				if(world.isAirBlock(x, y + dy, z))
					world.setBlock(x, y + dy, z, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
			if(world.isAirBlock(x, y + height, z))
				world.setBlock(x, y + height, z, ModBlocks.certus_quartz_cluster, ForgeDirection.UP.ordinal(), 2);
		}

		for(int i = 0; i < 40; i++) {
			int x = ox + rand.nextInt(16);
			int z = oz + rand.nextInt(16);
			int y = floor[(x - ox) + (z - oz) * 16];
			if(y <= 30 || y >= 100 || world.getBlock(x, y - 1, z) != ModBlocks.certus_quartz_block || !world.isAirBlock(x, y, z)) continue;
			world.setBlock(x, y, z, randomBud(rand), ForgeDirection.UP.ordinal(), 2);
		}

		if(rand.nextInt(4) == 0) {
			int cx = ox + rand.nextInt(16);
			int cz = oz + rand.nextInt(16);
			for(int i = 0; i < 3 + rand.nextInt(3); i++) {
				int x = cx + rand.nextInt(5) - 2;
				int z = cz + rand.nextInt(5) - 2;
				if(x < ox || x >= ox + 16 || z < oz || z >= oz + 16) continue;
				int y = floor[(x - ox) + (z - oz) * 16] - 1;
				if(y <= 30 || y >= 100) continue;
				world.setBlock(x, y, z, ModBlocks.budding_certus_quartz, 0, 2);
			}
		}

		for(int i = 0; i < 6; i++) {
			int x = ox + rand.nextInt(16);
			int z = oz + rand.nextInt(16);
			int r = 2 + rand.nextInt(3);
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					int vx = x + dx, vz = z + dz;
					if(vx < ox || vx >= ox + 16 || vz < oz || vz >= oz + 16) continue;
					if(dx * dx + dz * dz > r * r) continue;
					int py = floor[(vx - ox) + (vz - oz) * 16] - 1;
					if(py > 0 && world.getBlock(vx, py, vz) == ModBlocks.certus_quartz_block)
						world.setBlock(vx, py, vz, ModBlocks.certine_netherrack, 0, 2);
				}
		}

		for(int i = 0; i < 4; i++) {
			int x = ox + rand.nextInt(16);
			int z = oz + rand.nextInt(16);
			Block cap = world.getBlock(x, 31, z);
			if(cap == Blocks.packed_ice || cap == ModBlocks.uranus_tears_block)
				generateGeyser(world, rand, x, z);
		}

		for(int[] p : patches) {
			if(rand.nextInt(5) != 0) continue;
			int x = p[0], y = p[1], z = p[2], type = p[3];
			if(!world.isAirBlock(x, y, z)) continue;
			Block below = world.getBlock(x, y - 1, z);
			if(below != Blocks.packed_ice && below != Blocks.snow) continue;
			if(world.getBlock(x, y - 2, z) == Blocks.air || world.getBlock(x, y - 3, z) == Blocks.air) continue;
			world.setBlock(x, y, z, ModBlocks.stalagmite, type, 2);
		}

		for(int[] p : patches) {
			if(rand.nextInt(10) != 0) continue;
			int x = p[0], z = p[2], type = p[3];
			int max = Math.min(126, p[1] + 24);
			for(int y = p[1] + 3; y < max; y++) {
				if(!world.isAirBlock(x, y - 1, z)) continue;
				if(!world.isAirBlock(x, y, z) && !world.isAirBlock(x, y + 1, z) && !world.isAirBlock(x, y + 2, z)) {
					world.setBlock(x, y - 1, z, ModBlocks.stalactite, type, 2);
					break;
				}
			}
		}
	}

	private static void fillSupport(World world, int x, int y, int z) {
		for(int by = y - 1; by >= 2; by--) {
			if(!world.isAirBlock(x, by, z)) return;
			world.setBlock(x, by, z, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
		}
	}

	private static int[] anchor(World world, int[] floor, boolean[] ok, int ox, int oz, Random rand, int flatR) {
		int x = ox + rand.nextInt(16);
		int z = oz + rand.nextInt(16);
		int idx = (x - ox) + (z - oz) * 16;
		if(!ok[idx]) return null;
		int y = floor[idx];
		if(y <= 30 || y >= 100) return null;
		if(!world.isAirBlock(x, y, z) || !world.isAirBlock(x, y + 1, z)) return null;
		if(!NetherBiomeHelper.isFlatArea(world, x, z, flatR)) return null;
		return new int[] { x, y, z };
	}

	private static boolean isColumnClear(World world, int x, int y, int z) {
		for(int by = y - 1; by >= 2; by--) {
			Block b = world.getBlock(x, by, z);
			if(b == Blocks.air) return false;
			if(isGround(b)) return true;
		}
		return false;
	}

	private static boolean isGround(Block b) {
		return b == ModBlocks.certus_quartz_block || b == ModBlocks.frozen_netherrack
				|| b == ModBlocks.certine_netherrack || b == Blocks.packed_ice || b == Blocks.snow
				|| b == ModBlocks.uranus_tears_block;
	}

	private void generateGeyser(World world, Random rand, int x, int z) {
		int surface = -1;
		for(int y = 127; y > 1; y--) {
			if(world.getBlock(x, y, z) == ModBlocks.uranus_tears_block) { surface = y; break; }
		}
		if(surface < 6) return;

		int bed = -1;
		for(int y = surface; y > 0; y--) {
			Block b = world.getBlock(x, y, z);
			if(b != ModBlocks.uranus_tears_block && b != Blocks.packed_ice && !world.isAirBlock(x, y, z)) { bed = y; break; }
		}
		if(bed < 0) return;

		int top = surface + 4 + rand.nextInt(5);
		for(int y = bed; y <= top; y++) {
			int r = y > surface ? 0 : (bed + 2 >= y ? 2 : 1);
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(Math.abs(dx) + Math.abs(dz) > r) continue;
					world.setBlock(x + dx, y, z + dz, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
				}
		}

		world.setBlock(x, top, z, ModBlocks.certic_geyser, 0, 2);

		if(!EntityCerticFlare.isOverCap(world)) {
			EntityCerticFlare flare = new EntityCerticFlare(world, x + 0.5D, top + 2, z + 0.5D);
			flare.motionY = 0.25D;
			world.spawnEntityInWorld(flare);
		}
	}

	private List<int[]> generatePockets(World world, Random rand, int ox, int oz) {
		List<int[]> patches = new ArrayList<int[]>();
		Set<Long> patched = new HashSet<Long>();

		for(int i = 0; i < 14; i++) {
			int cx = ox + rand.nextInt(16);
			int cz = oz + rand.nextInt(16);
			int r = 3 + rand.nextInt(4);
			int type = rand.nextInt(3) == 0 ? BlockEnums.EnumStalagmiteType.ICE.ordinal() : BlockEnums.EnumStalagmiteType.SNOW.ordinal();
			Block surface = type == BlockEnums.EnumStalagmiteType.ICE.ordinal() ? Blocks.packed_ice : Blocks.snow;

			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(dx * dx + dz * dz > r * r) continue;
					int x = cx + dx, z = cz + dz;
					if(x < ox || x >= ox + 16 || z < oz || z >= oz + 16) continue;
					long key = (((long) (x - ox)) << 8) | (z - oz);
					if(!patched.add(key)) continue;

					int y = NetherBiomeHelper.getFloorHeight(world, x, z);
					if(y <= 31 || y >= 100 || y - 3 < 0) continue;
					if(world.getBlock(x, y - 2, z) == Blocks.air || world.getBlock(x, y - 3, z) == Blocks.air) continue;

					int depth = 1 + rand.nextInt(3);
					for(int dy = 1; dy <= depth; dy++) {
						Block cur = world.getBlock(x, y - dy, z);
						if(cur != ModBlocks.certus_quartz_block && cur != Blocks.packed_ice && cur != Blocks.snow) break;
						world.setBlock(x, y - dy, z, surface, 0, 2);
					}
					patches.add(new int[] { x, y, z, type });
				}
		}
		return patches;
	}

	private void generateMonolith(World world, Random rand, int x, int y, int z, int height, int maxR) {
		for(int dy = 0; dy < height; dy++) {
			double t = (double) dy / height;
			double wob = NetherBiomeHelper.noise3(x * 0.18D, (y + dy) * 0.18D, z * 0.18D, 71) * 2D - 1D;
			int r = (int) Math.max(0, Math.round(maxR * (1D - t * 0.85D) + wob * 1.6D));

			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(dx * dx + dz * dz > r * r) continue;
					int bx = x + dx, by = y + dy, bz = z + dz;
					if(!world.isAirBlock(bx, by, bz)) continue;
					world.setBlock(bx, by, bz, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
					fillSupport(world, bx, by, bz);
					if(rand.nextInt(6) == 0) placeBud(world, rand, bx, by, bz);
				}
		}
		if(world.isAirBlock(x, y + height, z))
			world.setBlock(x, y + height, z, ModBlocks.certus_quartz_cluster, ForgeDirection.UP.ordinal(), 2);
	}

	private void generateSlantedSpike(World world, Random rand, int x, int y, int z, int height) {
		double dirX = rand.nextGaussian(), dirZ = rand.nextGaussian();
		double len = Math.max(0.0001D, Math.sqrt(dirX * dirX + dirZ * dirZ));
		dirX /= len;
		dirZ /= len;

		double slant = 0.18D + rand.nextDouble() * 0.35D;
		double cx = x + 0.5D, cz = z + 0.5D;
		int baseR = 1 + rand.nextInt(2);

		for(int dy = 0; dy < height; dy++) {
			double t = (double) dy / height;
			int r = (int) Math.max(0, Math.round(baseR * (1D - t * t)));
			int bx = (int) Math.floor(cx), bz = (int) Math.floor(cz);

			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(dx * dx + dz * dz > r * r + 1) continue;
					int px = bx + dx, py = y + dy, pz = bz + dz;
					if(!world.isAirBlock(px, py, pz)) continue;
					world.setBlock(px, py, pz, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
					fillSupport(world, px, py, pz);
					if(rand.nextInt(6) == 0) placeBud(world, rand, px, py, pz);
				}

			double wob = (NetherBiomeHelper.noise3(cx * 0.3D, (y + dy) * 0.3D, cz * 0.3D, 55) * 2D - 1D) * 0.1D;
			cx += dirX * slant + wob;
			cz += dirZ * slant + wob;
		}
	}

	private void generateSpike(World world, Random rand, int x, int y, int z, int height, int baseR) {
		for(int dy = 0; dy < height; dy++) {
			double t = (double) dy / height;
			int r = (int) Math.max(0, Math.round(baseR * (1D - t * t)));
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++) {
					if(dx * dx + dz * dz > r * r + 1) continue;
					int bx = x + dx, by = y + dy, bz = z + dz;
					if(!world.isAirBlock(bx, by, bz)) continue;
					world.setBlock(bx, by, bz, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
					fillSupport(world, bx, by, bz);
					if(rand.nextInt(6) == 0) placeBud(world, rand, bx, by, bz);
				}
		}
		if(world.isAirBlock(x, y + height, z))
			world.setBlock(x, y + height, z, ModBlocks.certus_quartz_cluster, ForgeDirection.UP.ordinal(), 2);
	}

	private void generateSpire(World world, Random rand, int x, int y, int z, int height, int maxR) {
		for(int dy = 0; dy < height; dy++) {
			double t = (double) dy / height;
			int r = (int) Math.max(0, Math.round(maxR * (1D - t)));
			for(int dx = -r; dx <= r; dx++)
				for(int dz = -r; dz <= r; dz++)
					if(dx * dx + dz * dz <= r * r + 1 && world.isAirBlock(x + dx, y + dy, z + dz)) {
						world.setBlock(x + dx, y + dy, z + dz, ModBlocks.certus_quartz_block, ModBlocks.CERTUS_META, 2);
						fillSupport(world, x + dx, y + dy, z + dz);
						if(rand.nextInt(6) == 0) placeBud(world, rand, x + dx, y + dy, z + dz);
					}
		}
		if(world.isAirBlock(x, y + height, z))
			world.setBlock(x, y + height, z, ModBlocks.certus_quartz_cluster, ForgeDirection.UP.ordinal(), 2);
	}

	private void placeBud(World world, Random rand, int x, int y, int z) {
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[rand.nextInt(6)];
		int bx = x + dir.offsetX, by = y + dir.offsetY, bz = z + dir.offsetZ;
		if(!world.isAirBlock(bx, by, bz)) return;
		world.setBlock(bx, by, bz, randomBud(rand), dir.ordinal(), 2);
	}

	private static Block randomBud(Random rand) {
		switch(rand.nextInt(4)) {
		case 0: return ModBlocks.certus_quartz_bud_small;
		case 1: return ModBlocks.certus_quartz_bud_medium;
		case 2: return ModBlocks.certus_quartz_bud_large;
		default: return ModBlocks.certus_quartz_cluster;
		}
	}
}
