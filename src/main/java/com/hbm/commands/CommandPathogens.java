package com.hbm.commands;

import java.util.List;
import java.util.Locale;

import com.hbm.extprop.HbmBloodstreamProps;
import com.hbm.handler.contagion.DiseaseDefinition;
import com.hbm.handler.contagion.DiseaseInstance;
import com.hbm.handler.contagion.DiseaseRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class CommandPathogens extends CommandBase {

	@Override
	public String getCommandName() {
		return "ntmpathogens";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ntmpathogens <player> <list/clear>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		if(args.length != 2) {
			sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
			return;
		}

		EntityPlayerMP player = getPlayer(sender, args[0]);

		if(player == null) {
			sender.addChatMessage(new ChatComponentText("Player not found: " + args[0]));
			return;
		}

		String action = args[1].toLowerCase(Locale.US);
		HbmBloodstreamProps props = HbmBloodstreamProps.getData(player);

		if("list".equals(action)) {
			List<DiseaseInstance> instances = props.getPathogenInstances();

			sender.addChatMessage(new ChatComponentText(player.getCommandSenderName() + " has " + instances.size() + " pathogen(s):"));

			for(DiseaseInstance instance : instances) {
				sender.addChatMessage(new ChatComponentText(" * " + describe(instance)));
			}

			return;
		}

		if("clear".equals(action)) {
			int cleared = props.clearPathogens();

			sender.addChatMessage(new ChatComponentText("Cleared " + cleared + " pathogen(s) from " + player.getCommandSenderName() + "."));

			if(player != sender) {
				player.addChatMessage(new ChatComponentText("All pathogens have been cleared from your bloodstream."));
			}

			return;
		}

		sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
	}

	private static String describe(DiseaseInstance instance) {
		DiseaseDefinition def = DiseaseRegistry.resolve(instance.frameId, instance.frameDef);

		String name = def != null && def.displayName != null && !def.displayName.isEmpty() ? def.displayName : instance.frameId;
		String type = def != null && def.type != null ? def.type.name() : "UNKNOWN";

		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" (").append(type).append(")");
		sb.append(", severity ").append((int) (instance.currentSeverity * 100F)).append("%");
		if(instance.ticksRemaining > 0) sb.append(", ").append(formatTicks(instance.ticksRemaining)).append(" left");
		if(instance.genome != null && !instance.genome.isEmpty()) sb.append(", genome ").append(instance.genome);

		return sb.toString();
	}

	private static String formatTicks(long ticks) {
		long seconds = ticks / 20;
		long minutes = seconds / 60;
		seconds %= 60;
		return minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1) return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		if(args.length == 2) return getListOfStringsMatchingLastWord(args, "list", "clear");
		return null;
	}

}
