package dev.akatriggered.g1axwhitelist.features;

import dev.akatriggered.g1axwhitelist.utils.TierUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class TabListManager {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    public static void updatePlayerTabName(Player player) {
        TierUtils.requestFromBothAPIs(player.getName()).thenAccept(result -> {
            String tierColor = result.getBestTier().getColor();
            String displayName = player.getName() + " <color:" + tierColor + ">[" + result.getBestTier().getName() + "]</color>";
            
            Component tabName = MINI_MESSAGE.deserialize(displayName);
            player.playerListName(tabName);
        });
    }
}
