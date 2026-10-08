package api.hbm.hypertube;

import net.minecraftforge.common.util.ForgeDirection;

public class HyperTubeShape {
	public static final ForgeDirection[][] SHAPES = new ForgeDirection[15][];

	static {
		int i = 0;
		for (int a = 0; a < 6; a++)
			for (int b = a + 1; b < 6; b++)
				SHAPES[i++] = new ForgeDirection[] { ForgeDirection.getOrientation(a), ForgeDirection.getOrientation(b) };
	}

	public static ForgeDirection[] getEnds(int meta) {
		return SHAPES[meta < 15 ? meta : 0];
	}

	public static int getMeta(ForgeDirection a, ForgeDirection b) {
		for (int i = 0; i < 15; i++) {
			ForgeDirection[] s = SHAPES[i];
			if ((s[0]) == a && s[1] == b || s[0] == b && s[1] == a) return i;
		}
		return 0;
	}

	public static boolean isOpen(int meta, ForgeDirection dir) {
		ForgeDirection[] e = getEnds(meta);
		return e[0] == dir || e[1] == dir;
	}
}
