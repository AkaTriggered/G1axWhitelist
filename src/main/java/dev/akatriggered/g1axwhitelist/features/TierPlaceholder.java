package dev.akatriggered.g1axwhitelist.features;

import dev.akatriggered.g1axwhitelist.utils.TierUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TierPlaceholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "g1ax";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AkaTriggered";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        switch (params.toLowerCase()) {
            case "tier":
                return TierUtils.requestFromAPI(player.getName())
                    .thenApply(tier -> tier.getName())
                    .join();
            case "tier_value":
                return String.valueOf(TierUtils.requestFromAPI(player.getName())
                    .thenApply(tier -> tier.getValue())
                    .join());
            case "tier_color":
                return TierUtils.requestFromAPI(player.getName())
                    .thenApply(tier -> tier.getColor())
                    .join();
            case "mctier":
                return TierUtils.requestFromBothAPIs(player.getName())
                    .thenApply(result -> result.getMcTier().getName())
                    .join();
            case "pvptier":
                return TierUtils.requestFromBothAPIs(player.getName())
                    .thenApply(result -> result.getPvpTier().getName())
                    .join();
        }
        return null;
    }
}
