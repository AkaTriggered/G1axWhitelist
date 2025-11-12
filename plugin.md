# G1axWhitelist Plugin Documentation

## 🎯 Overview

**G1axWhitelist** is an advanced Minecraft server plugin that provides intelligent, tier-based player management through automatic whitelisting. The plugin integrates with both **MCTiers** and **PVPTiers** APIs to evaluate player skill levels and automatically grant server access to qualified players.

## 👨‍💻 Creator

**Created by:** [AkaTriggered](https://github.com/AkaTriggered) / G1ax  
**GitHub Repository:** [G1axWhitelist](https://github.com/AkaTriggered/G1axWhitelist)  
**License:** Open Source (MIT)

## ✨ Key Features

### 🔄 Dual API Integration
- **Primary:** MCTiers API integration for tier checking
- **Fallback:** PVPTiers API as secondary tier source
- **Smart Logic:** Uses the best tier from either platform

### 🚀 Automatic Whitelisting
- Automatically whitelists players who meet tier requirements (LT3+ by default)
- Real-time tier verification during player login
- Seamless integration with server whitelist system

### 💾 Database Management
- SQLite database for tracking auto-whitelisted players
- Persistent storage of player tier information
- Efficient caching system for improved performance

### 🎨 Rich User Experience
- Beautiful ASCII art console display
- MiniMessage support for colorful in-game messages
- Customizable kick messages with Discord integration
- Clean, informative console logging

### ⚡ Performance Optimized
- Asynchronous API calls to prevent server lag
- Built-in caching system (5-minute cache duration)
- Non-blocking player authentication process

## 🛠️ Technical Specifications

### Requirements
- **Minecraft Version:** 1.20.4+
- **Server Software:** Paper/Spigot/Bukkit
- **Java Version:** 17+
- **Dependencies:** None (all libraries shaded)

### API Endpoints Used
- **MCTiers:** `https://mctiers.com/api/search_profile/{username}`
- **PVPTiers:** `https://pvptiers.com/api/rankings/{uuid}`
- **Mojang:** `https://api.mojang.com/users/profiles/minecraft/{username}`

## 📋 Configuration

### Default Config (`config.yml`)
```yaml
# G1axWhitelist Configuration
minimum-tier-value: 5  # LT3 = 5
kick-message: "§cYou need to be LT3+ on MCTiers or PVPTiers to join this server!\n§eCreate a ticket on our Discord: {discord}"
whitelist-message: "<gradient:#00ff00:#00aa00>[G1ax]</gradient> <green>You've been automatically whitelisted! (Tier: {tier})</green>"
discord-invite: "https://discord.gg/yourserver"
enable-auto-whitelist: true
enable-pvptiers-fallback: true
debug-mode: false
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `minimum-tier-value` | Integer | 5 | Minimum tier value required (5 = LT3) |
| `kick-message` | String | See above | Message shown to denied players |
| `whitelist-message` | String | See above | Welcome message for auto-whitelisted players |
| `discord-invite` | String | `https://discord.gg/yourserver` | Discord server invite link |
| `enable-auto-whitelist` | Boolean | true | Enable/disable automatic whitelisting |
| `enable-pvptiers-fallback` | Boolean | true | Enable PVPTiers as fallback |
| `debug-mode` | Boolean | false | Enable debug logging |

## 🎮 Commands & Permissions

### Commands
- `/checktier <username>` - Check a player's tier on both platforms
  - **Permission:** `g1axwhitelist.check`
  - **Default:** OP only

### Permissions
- `g1axwhitelist.check` - Allows checking player tiers
- `g1axwhitelist.bypass` - Bypasses tier requirements

## 🏆 Tier System

### Tier Values & Rankings
| Tier | Value | Color | Description |
|------|-------|-------|-------------|
| HT1 | 10 | Red | High Tier 1 (Best) |
| LT1 | 9 | Pink | Low Tier 1 |
| HT2 | 8 | Orange | High Tier 2 |
| LT2 | 7 | Light Orange | Low Tier 2 |
| HT3 | 6 | Gold | High Tier 3 |
| **LT3** | **5** | **Yellow** | **Low Tier 3 (Minimum)** |
| HT4 | 4 | Dark Green | High Tier 4 |
| LT4 | 3 | Light Green | Low Tier 4 |
| HT5 | 2 | Gray | High Tier 5 |
| LT5 | 1 | Light Gray | Low Tier 5 |
| UNRANKED | 0 | White | No ranking |

## 📊 Database Schema

### `autowhitelisted` Table
```sql
CREATE TABLE autowhitelisted (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    tier TEXT NOT NULL,
    whitelisted_at INTEGER NOT NULL
);
```

## 🔧 Installation & Setup

1. **Download** the latest release from GitHub
2. **Place** the JAR file in your server's `plugins` folder
3. **Start** your server to generate the configuration
4. **Edit** `config.yml` to customize settings
5. **Set** your Discord invite link
6. **Restart** the server to apply changes

## 🚀 How It Works

### Player Join Process
1. Player attempts to join the server
2. Plugin intercepts the login event (AsyncPlayerPreLoginEvent)
3. Checks MCTiers API for player's tier
4. If not LT3+ on MCTiers, checks PVPTiers as fallback
5. Uses the best tier from either platform
6. If tier meets requirements (≥ LT3):
   - Adds player to server whitelist
   - Stores information in database
   - Allows login and sends welcome message
7. If tier insufficient:
   - Denies login with informative message
   - Provides Discord invite for manual whitelist requests

### Caching System
- API responses cached for 5 minutes per player
- Reduces API calls and improves performance
- Automatic cache expiration and cleanup

## 🎨 Console Output

The plugin features beautiful ASCII art on startup:
```
  ██████╗  ██╗ █████╗ ██╗  ██╗
 ██╔════╝ ███║██╔══██╗╚██╗██╔╝
 ██║  ███╗╚██║███████║ ╚███╔╝ 
 ██║   ██║ ██║██╔══██║ ██╔██╗ 
 ╚██████╔╝ ██║██║  ██║██╔╝ ██╗
  ╚═════╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝
    G1ax Auto-Whitelist v1.0.0 - MCTiers + PVPTiers
    Plugin loaded successfully!
```

## 🤝 Contributing

This plugin is open source and welcomes contributions! Visit the [GitHub repository](https://github.com/AkaTriggered/G1axWhitelist) to:
- Report bugs
- Suggest features
- Submit pull requests
- View source code

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **GitHub Issues:** [Report bugs or request features](https://github.com/AkaTriggered/G1axWhitelist/issues)
- **Discord:** Join our community server for support
- **Documentation:** This file and inline code comments

---

*Made with ❤️ by AkaTriggered for the Minecraft PvP community*
