package com.hbm.render.tileentity.hypertube;

import api.hbm.hypertube.HyperTubeShape;
import com.hbm.blocks.network.hypertube.BlockHyperTube;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class RenderHyperTube implements ISimpleBlockRenderingHandler {
	private static final double MIN = 0.25D;
	private static final double MAX = 0.75D;

	@Override
	public void renderInventoryBlock(Block block, int meta, int modelID, RenderBlocks renderer) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		tessellator.setColorOpaque_F(1, 1, 1);
		renderer.setRenderBounds(MIN, 0D, MIN, MAX, 1D, MAX);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0F, -1F, 0F);
		renderer.renderFaceYNeg(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 0, meta));
		tessellator.setNormal(0F, 1F, 0F);
		renderer.renderFaceYPos(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 1, meta));
		tessellator.setNormal(0F, 0F, -1F);
		renderer.renderFaceZNeg(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 2, meta));
		tessellator.setNormal(0F, 0F, 1F);
		renderer.renderFaceZPos(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 3, meta));
		tessellator.setNormal(-1F, 0F, 0F);
		renderer.renderFaceXNeg(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 4, meta));
		tessellator.setNormal(1F, 0F, 0F);
		renderer.renderFaceXPos(block, 0D, 0D, 0D, renderer.getBlockIconFromSideAndMetadata(block, 5, meta));
		tessellator.draw();

		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int i3, RenderBlocks renderer) {
		int meta = world.getBlockMetadata(x, y, z);

		renderer.setRenderBounds(MIN, MIN, MIN, MAX, MAX, MAX);
		renderer.renderStandardBlock(block, x, y, z);

		for (ForgeDirection end : HyperTubeShape.getEnds(meta)) {
			renderer.setRenderBounds(
				end == ForgeDirection.WEST ? 0D : MIN,
				end == ForgeDirection.DOWN ? 0D : MIN,
				end == ForgeDirection.NORTH ? 0D : MIN,
				end == ForgeDirection.EAST ? 1D : MAX,
				end == ForgeDirection.UP ? 1D : MAX,
				end == ForgeDirection.SOUTH ? 1D : MAX);
			renderer.renderStandardBlock(block, x, y, z);
		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int i) {
		return true;
	}

	@Override
	public int getRenderId() {
		return BlockHyperTube.renderID;
	}
}
