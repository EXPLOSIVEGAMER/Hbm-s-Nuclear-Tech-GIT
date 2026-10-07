package com.hbm.items.armor;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ArmorUtil;
import com.hbm.util.AstronomyUtil;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Jetpack extends JetpackFueledBase {

	public static final int MODE_BOOST = 0;
	public static final int MODE_BUILDER = 1;
	public static final int MODE_VECTOR = 2;
	public static final int MODE_COUNT = 3;

	public Jetpack(FluidType fuel, int maxFuel) {
		super(fuel, maxFuel);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		int mode = entity instanceof EntityPlayer ? HbmPlayerProps.getData((EntityPlayer) entity).jetpackMode : MODE_BOOST;

		switch(mode) {
		case MODE_BUILDER: return "hbm:textures/models/JetPackBlue.png";
		case MODE_VECTOR: return "hbm:textures/models/JetPackGreen.png";
		default: return "hbm:textures/models/JetPackRed.png";
		}
	}

	public static ItemStack getWorn(EntityPlayer player) {

		ItemStack armor = player.getCurrentArmor(2);
		if(armor == null) return null;
		if(armor.getItem() instanceof Jetpack) return armor;

		if(ArmorModHandler.hasMods(armor)) {
			ItemStack mod = ArmorModHandler.pryMod(armor, ArmorModHandler.plate_only);
			if(mod != null && mod.getItem() instanceof Jetpack) return mod;
		}

		return null;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		boolean active = props.isJetpackActive();
		float gravity = CelestialBody.getGravity(player);
		int mode = props.jetpackMode;

		if(!world.isRemote) {

			boolean thrusting = mode == MODE_BUILDER
					? getFuel(stack) > 0 && (active || (!player.onGround && !player.isSneaking() && props.enableBackpack && gravity > 0))
					: getFuel(stack) > 0 && active;

			if(thrusting) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "jetpack");
				data.setInteger("player", player.getEntityId());
				if(mode == MODE_VECTOR) data.setInteger("mode", 1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.dimensionId, player.posX, player.posY, player.posZ, 100));
			}
		}

		if(getFuel(stack) <= 0)
			return;

		if(mode == MODE_BOOST) {

			if(active) {
				player.fallDistance = 0;

				if(gravity == 0) {
					Vec3 look = player.getLookVec();

					player.motionX += look.xCoord * 0.05;
					player.motionY += look.yCoord * 0.05;
					player.motionZ += look.zCoord * 0.05;
				} else if(player.motionY < 0.4D) {
					player.motionY += 0.1D * Math.max(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);
				}

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 5);
				ArmorUtil.resetFlightTime(player);
			}

		} else if(mode == MODE_BUILDER) {

			boolean playerTriesToHover = player.isSneaking() && active;
			boolean playerShouldHover = playerTriesToHover || !player.isSneaking();

			if(active && !playerTriesToHover) {
				player.fallDistance = 0;

				if(gravity == 0) {
					Vec3 look = player.getLookVec();

					player.motionX += look.xCoord * 0.05;
					player.motionY += look.yCoord * 0.05;
					player.motionZ += look.zCoord * 0.05;
				} else if(player.motionY < 0.4D) {
					player.motionY += 0.1D * Math.max(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);
				}

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 5);
				ArmorUtil.resetFlightTime(player);

			} else if(playerShouldHover && !player.onGround && props.enableBackpack && gravity > 0) {
				player.fallDistance = 0;

				float thrustMultiplier = Math.max(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);

				if(player.motionY < -1 * thrustMultiplier)
					player.motionY += 0.2D * thrustMultiplier;
				else if(player.motionY < -0.1 * thrustMultiplier)
					player.motionY += 0.1D * thrustMultiplier;
				else if(player.motionY < 0)
					player.motionY = 0;

				player.motionX *= 1.025D;
				player.motionZ *= 1.025D;

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 10);
				ArmorUtil.resetFlightTime(player);
			}

		} else if(mode == MODE_VECTOR) {

			if(active) {
				if(player.motionY < 0.4D)
					player.motionY += 0.1D * Math.min(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);

				Vec3 look = player.getLookVec();

				if(Vec3.createVectorHelper(player.motionX, player.motionY, player.motionZ).lengthVector() < 2) {
					player.motionX += look.xCoord * 0.1;
					player.motionY += look.yCoord * 0.1;
					player.motionZ += look.zCoord * 0.1;

					if(look.yCoord > 0)
						player.fallDistance = 0;
				}

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 3);
				ArmorUtil.resetFlightTime(player);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		int mode = player != null ? HbmPlayerProps.getData(player).jetpackMode : MODE_BOOST;

		String[] modes = { "Boost Mode", "Builder Mode", "Vector Mode" };
		list.add(EnumChatFormatting.GOLD + "Mode: " + EnumChatFormatting.YELLOW + modes[mode]);

		if(mode == MODE_BOOST) {
			list.add("Regular jetpack for simple upwards momentum.");
		} else if(mode == MODE_BUILDER) {
			list.add("Regular jetpack that will automatically hover mid-air.");
			list.add("Sneaking will stop hover mode.");
			list.add("Hover mode will consume less fuel and increase air-mobility.");
		} else {
			list.add("High-mobility jetpack.");
			list.add("Higher fuel consumption.");
		}

		super.addInformation(stack, player, list, ext);
	}
}
