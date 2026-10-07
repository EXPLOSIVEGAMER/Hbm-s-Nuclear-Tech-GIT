package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.sedna.factory.LegoClient;
import com.hbm.lib.RefStrings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderCerticFlare extends Render {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/certmite.png");

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		LegoClient.renderFlare(entity, partialTicks, 0.4F, 0.8F, 1.0F);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}
}
