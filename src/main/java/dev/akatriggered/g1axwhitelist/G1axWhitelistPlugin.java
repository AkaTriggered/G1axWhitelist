package dev.akatriggered.g1axwhitelist;

import dev.akatriggered.g1axwhitelist.commands.CheckTierCommand;
import dev.akatriggered.g1axwhitelist.database.DatabaseManager;
import dev.akatriggered.g1axwhitelist.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class G1axWhitelistPlugin extends JavaPlugin {
    
    private DatabaseManager databaseManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        databaseManager = new DatabaseManager(getDataFolder());
        try {
            databaseManager.connect();
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getCommand("checktier").setExecutor(new CheckTierCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        getLogger().info("§a  ██████╗  ██╗ █████╗ ██╗  ██╗");
        getLogger().info("§a ██╔════╝ ███║██╔══██╗╚██╗██╔╝");
        getLogger().info("§a ██║  ███╗╚██║███████║ ╚███╔╝ ");
        getLogger().info("§a ██║   ██║ ██║██╔══██║ ██╔██╗ ");
        getLogger().info("§a ╚██████╔╝ ██║██║  ██║██╔╝ ██╗");
        getLogger().info("§a  ╚═════╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝");
        getLogger().info("§b    G1ax Auto-Whitelist v1.0.0 - MCTiers + PVPTiers");
        getLogger().info("§2    Plugin loaded successfully!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            try {
                databaseManager.disconnect();
            } catch (Exception e) {
                getLogger().warning("Error disconnecting from database: " + e.getMessage());
            }
        }
        getLogger().info("G1axWhitelist has been disabled!");
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
