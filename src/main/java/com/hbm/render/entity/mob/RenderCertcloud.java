package com.hbm.render.entity.mob;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderCertcloud extends Render {

	@Override
	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) { }

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}
