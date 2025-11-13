package dev.akatriggered.g1axwhitelist.listeners;

import dev.akatriggered.g1axwhitelist.G1axWhitelistPlugin;
import dev.akatriggered.g1axwhitelist.features.TabListManager;
import dev.akatriggered.g1axwhitelist.utils.EligibilityChecker;
import dev.akatriggered.g1axwhitelist.utils.TierUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
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
        if (!plugin.getConfig().getBoolean("features.auto-whitelist", true)) {
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
            var result = TierUtils.requestFromBothAPIs(playerName).get();
            var eligibility = EligibilityChecker.checkEligibility(result, plugin.getConfig());
            
            if (eligibility.isEligible()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    offlinePlayer.setWhitelisted(true);
                    
                    try {
                        plugin.getDatabaseManager().addPlayer(
                            event.getUniqueId().toString().replace("-", ""),
                            playerName,
                            result.getBestTier().getName()
                        );
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to store player in database: " + e.getMessage());
                    }
                    
                    plugin.getLogger().info("Auto-whitelisted " + playerName + " (" + result.getBestTier().getName() + ")");
                });
            } else {
                String kickMessage = getKickMessage(eligibility.getReason(), result, plugin.getConfig());
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
                plugin.getLogger().info("Denied " + playerName + " (MC:" + result.getMcTier().getName() + " PVP:" + result.getPvpTier().getName() + " - " + eligibility.getReason() + ")");
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
                String welcomeMessage = plugin.getConfig().getString("messages.whitelist", "");
                var component = miniMessage.deserialize(welcomeMessage);
                player.sendMessage(component);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check auto-whitelist status for " + player.getName() + ": " + e.getMessage());
        }
        
        // Update tab list name with tier
        if (plugin.getConfig().getBoolean("features.tab-tier-display", true)) {
            TabListManager.updatePlayerTabName(player);
        }
    }
    
    private String getKickMessage(String reason, TierUtils.TierResult result, FileConfiguration config) {
        String discord = config.getString("discord-invite", "");
        
        switch (reason) {
            case "REQUIRE_BOTH":
                return config.getString("messages.kick-require-both", "")
                    .replace("{discord}", discord)
                    .replace("{mctier}", result.getMcTier().getName())
                    .replace("{pvptier}", result.getPvpTier().getName());
            case "NO_DATA":
                return config.getString("messages.kick-no-data", "")
                    .replace("{discord}", discord);
            case "NOT_ALLOWED":
                return config.getString("messages.kick-not-allowed", "")
                    .replace("{discord}", discord)
                    .replace("{tier}", result.getBestTier().getName());
            default:
                return config.getString("messages.kick", "")
                    .replace("{discord}", discord);
        }
    }
}
