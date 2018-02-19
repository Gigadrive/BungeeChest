package eu.thechest.bungeechest.cmd;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class BlockCommands extends Command {
    public BlockCommands(){
        super("me",null, "bukkit:me","minecraft:me","op","minecraft:op","bukkit:op","deop","bukkit:deop","minecraft:deop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "Unknown command!"));
    }
}
