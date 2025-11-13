package dev.akatriggered.g1axwhitelist.commands;

import dev.akatriggered.g1axwhitelist.G1axWhitelistPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final G1axWhitelistPlugin plugin;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public ReloadCommand(G1axWhitelistPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("g1axwhitelist.reload")) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<red>You don't have permission to use this command."));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(MINI_MESSAGE.deserialize("<green>G1axWhitelist configuration reloaded!"));
        return true;
    }
}
