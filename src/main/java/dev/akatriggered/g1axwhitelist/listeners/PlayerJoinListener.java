package dev.akatriggered.g1axwhitelist.listeners;

import dev.akatriggered.g1axwhitelist.G1axWhitelistPlugin;
import dev.akatriggered.g1axwhitelist.utils.TierUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final G1axWhitelistPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    public PlayerJoinListener(G1axWhitelistPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getConfig().getBoolean("enable-auto-whitelist", true)) {
            return;
        }
        
        if (!plugin.getServer().hasWhitelist()) {
            return;
        }
        
        String playerName = event.getName();
        
        var offlinePlayer = plugin.getServer().getOfflinePlayer(event.getUniqueId());
        if (offlinePlayer.isWhitelisted() || offlinePlayer.isOp()) {
            return;
        }
        
        try {
            var tier = TierUtils.requestFromAPI(playerName).get();
            int minimumTierValue = plugin.getConfig().getInt("minimum-tier-value", 5);
            
            if (tier.getValue() >= minimumTierValue) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    offlinePlayer.setWhitelisted(true);
                    
                    try {
                        plugin.getDatabaseManager().addPlayer(
                            event.getUniqueId().toString().replace("-", ""),
                            playerName,
                            tier.getName()
                        );
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to store player in database: " + e.getMessage());
                    }
                    
                    plugin.getLogger().info("Auto-whitelisted " + playerName + " (" + tier.getName() + ")");
                });
            } else {
                String kickMessage = plugin.getConfig().getString("kick-message", 
                    "§cYou need to be LT3+ on MCTiers or PVPTiers to join this server!\n§eCreate a ticket on our Discord: {discord}");
                String discordInvite = plugin.getConfig().getString("discord-invite", "https://discord.gg/yourserver");
                kickMessage = kickMessage.replace("{discord}", discordInvite);
                
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
                plugin.getLogger().info("Denied " + playerName + " (" + tier.getName() + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check tier for " + playerName + ": " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        
        try {
            if (plugin.getDatabaseManager().isAutoWhitelisted(player.getUniqueId().toString().replace("-", ""))) {
                String welcomeMessage = plugin.getConfig().getString("whitelist-message", 
                    "<gradient:#00ff00:#00aa00>[G1ax]</gradient> <green>You've been automatically whitelisted!</green>");
                
                var component = miniMessage.deserialize(welcomeMessage);
                player.sendMessage(component);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check auto-whitelist status for " + player.getName() + ": " + e.getMessage());
        }
    }
}
