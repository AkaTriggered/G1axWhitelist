package dev.akatriggered.g1axwhitelist.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TierUtils {
    
    private static final Map<String, CachedTier> tierCache = new HashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private static final String MCTIERS_API_URL = "https://mctiers.com/api/search_profile/";
    private static final String PVPTIERS_API_URL = "https://pvptiers.com/api/rankings/";
    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    
    public enum Tier {
        HT1(10, "HT1", "#ff0000"),
        LT1(9, "LT1", "#ffb6c1"),
        HT2(8, "HT2", "#ffa500"),
        LT2(7, "LT2", "#ffe4b5"),
        HT3(6, "HT3", "#daa520"),
        LT3(5, "LT3", "#eee8aa"),
        HT4(4, "HT4", "#006400"),
        LT4(3, "LT4", "#90ee90"),
        HT5(2, "HT5", "#808080"),
        LT5(1, "LT5", "#d3d3d3"),
        UNRANKED(0, "UNRANKED", "#ffffff");
        
        private final int value;
        private final String name;
        private final String color;
        
        Tier(int value, String name, String color) {
            this.value = value;
            this.name = name;
            this.color = color;
        }
        
        public int getValue() { return value; }
        public String getName() { return name; }
        public String getColor() { return color; }
        
        public static Tier fromString(String tierName) {
            for (Tier tier : values()) {
                if (tier.name.equalsIgnoreCase(tierName)) {
                    return tier;
                }
            }
            return UNRANKED;
        }
    }
    
    private static class CachedTier {
        final Tier tier;
        final long timestamp;
        
        CachedTier(Tier tier) {
            this.tier = tier;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    public static CompletableFuture<Tier> requestFromAPI(String username) {
        return CompletableFuture.supplyAsync(() -> {
            CachedTier cached = tierCache.get(username.toLowerCase());
            if (cached != null && !cached.isExpired()) {
                return cached.tier;
            }
            
            Tier mcTier = getTierFromMCTiers(username);
            if (mcTier.getValue() >= 5) { // If LT3+ on MCTiers, use it
                tierCache.put(username.toLowerCase(), new CachedTier(mcTier));
                return mcTier;
            }
            
            Tier pvpTier = getTierFromPVPTiers(username);
            Tier bestTier = pvpTier.getValue() >= mcTier.getValue() ? pvpTier : mcTier;
            
            tierCache.put(username.toLowerCase(), new CachedTier(bestTier));
            return bestTier;
        });
    }
    
    public static CompletableFuture<TierResult> requestFromBothAPIs(String username) {
        return CompletableFuture.supplyAsync(() -> {
            Tier mcTier = getTierFromMCTiers(username);
            Tier pvpTier = getTierFromPVPTiers(username);
            return new TierResult(mcTier, pvpTier);
        });
    }
    
    private static Tier getTierFromMCTiers(String username) {
        try {
            URL url = new URL(MCTIERS_API_URL + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() != 200) {
                return Tier.UNRANKED;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            
            if (json.has("rankings") && !json.get("rankings").isJsonNull()) {
                JsonObject rankings = json.getAsJsonObject("rankings");
                
                if (rankings.has("vanilla")) {
                    JsonObject vanilla = rankings.getAsJsonObject("vanilla");
                    int tier = vanilla.get("tier").getAsInt();
                    int pos = vanilla.get("pos").getAsInt();
                    
                    return getTierFromValues(tier, pos);
                }
            }
            
            return Tier.UNRANKED;
        } catch (Exception e) {
            return Tier.UNRANKED;
        }
    }
    
    private static Tier getTierFromPVPTiers(String username) {
        try {
            UUID uuid = fetchUUIDFromUsername(username);
            if (uuid == null) {
                return Tier.UNRANKED;
            }
            
            URL url = new URL(PVPTIERS_API_URL + uuid.toString().replace("-", ""));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() != 200) {
                return Tier.UNRANKED;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject crystalObject = json.getAsJsonObject("crystal");
            
            if (crystalObject != null && !crystalObject.get("tier").isJsonNull()) {
                int tier = crystalObject.get("tier").getAsInt();
                int pos = crystalObject.get("pos").getAsInt();
                return getTierFromValues(tier, pos);
            }
            
            return Tier.UNRANKED;
        } catch (Exception e) {
            return Tier.UNRANKED;
        }
    }
    
    private static UUID fetchUUIDFromUsername(String username) {
        try {
            URL url = new URL(MOJANG_API_URL + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() != 200) {
                return null;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String uuidString = json.get("id").getAsString();
            
            return UUID.fromString(
                uuidString.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
                )
            );
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Tier getTierFromValues(int tier, int pos) {
        switch (tier) {
            case 1: return pos == 0 ? Tier.HT1 : Tier.LT1;
            case 2: return pos == 0 ? Tier.HT2 : Tier.LT2;
            case 3: return pos == 0 ? Tier.HT3 : Tier.LT3;
            case 4: return pos == 0 ? Tier.HT4 : Tier.LT4;
            case 5: return pos == 0 ? Tier.HT5 : Tier.LT5;
            default: return Tier.UNRANKED;
        }
    }
    
    private static Tier getTierFromPVPValue(int tierValue) {
        switch (tierValue) {
            case 10: return Tier.HT1;
            case 9: return Tier.LT1;
            case 8: return Tier.HT2;
            case 7: return Tier.LT2;
            case 6: return Tier.HT3;
            case 5: return Tier.LT3;
            case 4: return Tier.HT4;
            case 3: return Tier.LT4;
            case 2: return Tier.HT5;
            case 1: return Tier.LT5;
            default: return Tier.UNRANKED;
        }
    }
    
    public static class TierResult {
        private final Tier mcTier;
        private final Tier pvpTier;
        
        public TierResult(Tier mcTier, Tier pvpTier) {
            this.mcTier = mcTier;
            this.pvpTier = pvpTier;
        }
        
        public Tier getMcTier() { return mcTier; }
        public Tier getPvpTier() { return pvpTier; }
        public Tier getBestTier() { 
            return mcTier.getValue() >= pvpTier.getValue() ? mcTier : pvpTier; 
        }
    }
}
