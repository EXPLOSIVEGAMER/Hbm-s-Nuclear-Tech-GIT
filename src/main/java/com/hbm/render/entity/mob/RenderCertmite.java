package com.hbm.render.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderCertmite extends RenderMaggot {

	public static final ResourceLocation certmiteTexture = new ResourceLocation(RefStrings.MODID, "textures/entity/certmite.png");

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return certmiteTexture;
	}
}
