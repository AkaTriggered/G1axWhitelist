package dev.akatriggered.g1axwhitelist.commands;

import dev.akatriggered.g1axwhitelist.G1axWhitelistPlugin;
import dev.akatriggered.g1axwhitelist.utils.TierUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CheckTierCommand implements CommandExecutor {
    
    private final G1axWhitelistPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    public CheckTierCommand(G1axWhitelistPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("g1axwhitelist.check")) {
            Component message = miniMessage.deserialize("<red>You don't have permission to use this command!");
            sender.sendMessage(message);
            return true;
        }
        
        if (args.length != 1) {
            Component message = miniMessage.deserialize("<red>Usage: /checktier <username>");
            sender.sendMessage(message);
            return true;
        }
        
        String username = args[0];
        
        Component loadingMessage = miniMessage.deserialize("<yellow>Checking tier for " + username + "...");
        sender.sendMessage(loadingMessage);
        
        TierUtils.requestFromBothAPIs(username).thenAccept(result -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int minimumTierValue = plugin.getConfig().getInt("minimum-tier-value", 5);
                boolean wouldBeWhitelisted = result.getBestTier().getValue() >= minimumTierValue;
                
                String statusColor = wouldBeWhitelisted ? "#00ff00" : "#ff0000";
                String statusText = wouldBeWhitelisted ? "WOULD BE WHITELISTED" : "WOULD BE KICKED";
                
                Component mcTierMsg = miniMessage.deserialize(
                    "<gradient:#00ff00:#00aa00>[G1ax]</gradient> <yellow>MCTiers:</yellow> <color:" + result.getMcTier().getColor() + ">" + result.getMcTier().getName() + "</color>"
                );
                Component pvpTierMsg = miniMessage.deserialize(
                    "<gradient:#00ff00:#00aa00>[G1ax]</gradient> <yellow>PVPTiers:</yellow> <color:" + result.getPvpTier().getColor() + ">" + result.getPvpTier().getName() + "</color>"
                );
                Component statusMsg = miniMessage.deserialize(
                    "<gradient:#00ff00:#00aa00>[G1ax]</gradient> <white>Best Tier: <color:" + result.getBestTier().getColor() + ">" + result.getBestTier().getName() + "</color> | Status: <color:" + statusColor + ">" + statusText + "</color>"
                );
                
                sender.sendMessage(mcTierMsg);
                sender.sendMessage(pvpTierMsg);
                sender.sendMessage(statusMsg);
            });
        }).exceptionally(throwable -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Component errorMessage = miniMessage.deserialize("<red>Failed to check tier for " + username + ": " + throwable.getMessage());
                sender.sendMessage(errorMessage);
            });
            return null;
        });
        
        return true;
    }
}
