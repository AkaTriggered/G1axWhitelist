package dev.akatriggered.g1axwhitelist.utils;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class EligibilityChecker {
    
    public static EligibilityResult checkEligibility(TierUtils.TierResult result, FileConfiguration config) {
        List<String> allowedTiers = config.getStringList("eligibility.allowed-tiers");
        
        // Check if no tier data available
        if (result.getMcTier() == TierUtils.Tier.UNRANKED && result.getPvpTier() == TierUtils.Tier.UNRANKED) {
            return new EligibilityResult(false, "NO_DATA");
        }
        
        // Check specific tier whitelist first
        if (!allowedTiers.isEmpty()) {
            boolean mcAllowed = allowedTiers.contains(result.getMcTier().getName());
            boolean pvpAllowed = allowedTiers.contains(result.getPvpTier().getName());
            
            if (!mcAllowed && !pvpAllowed) {
                return new EligibilityResult(false, "NOT_ALLOWED");
            }
            return new EligibilityResult(true, "ALLOWED");
        }
        
        // Check minimum requirements
        int mcMin = config.getInt("eligibility.mctiers-minimum", 0);
        int pvpMin = config.getInt("eligibility.pvptiers-minimum", 0);
        boolean requireBoth = config.getBoolean("eligibility.require-both", false);
        
        boolean mcEligible = mcMin == 0 || result.getMcTier().getValue() >= mcMin;
        boolean pvpEligible = pvpMin == 0 || result.getPvpTier().getValue() >= pvpMin;
        
        if (requireBoth) {
            if (mcEligible && pvpEligible) {
                return new EligibilityResult(true, "BOTH_MET");
            } else {
                return new EligibilityResult(false, "REQUIRE_BOTH");
            }
        } else {
            if (mcEligible || pvpEligible) {
                return new EligibilityResult(true, "MINIMUM_MET");
            } else {
                return new EligibilityResult(false, "INSUFFICIENT");
            }
        }
    }
    
    public static boolean isEligible(TierUtils.TierResult result, FileConfiguration config) {
        return checkEligibility(result, config).isEligible();
    }
    
    public static class EligibilityResult {
        private final boolean eligible;
        private final String reason;
        
        public EligibilityResult(boolean eligible, String reason) {
            this.eligible = eligible;
            this.reason = reason;
        }
        
        public boolean isEligible() { return eligible; }
        public String getReason() { return reason; }
    }
}
