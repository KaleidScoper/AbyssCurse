# AbyssCurse

[English](README_EN.md) | [中文](README.md)

![Welcome to the Abyss](abyss.png)

A PaperMC plugin for Minecraft Paper servers that adds an Abyss Curse mechanism, inspired by *Made in Abyss*. Players ascending in the abyss will trigger deadly curse effects, creating an intense survival experience.

## Table of Contents

- [Feature Overview](#feature-overview)
- [Core Mechanics](#core-mechanics)
  - [Curse System](#curse-system)
  - [Narehate System](#narehate-system)
  - [Achievement System](#achievement-system)
- [Installation & Configuration](#installation--configuration)
- [Usage Guide](#usage-guide)
- [Command Reference](#command-reference)
- [Permissions](#permissions)
- [Troubleshooting](#troubleshooting)
- [Contributing & License](#contributing--license)

## Feature Overview

- **Plugin Modes**: Supports OFF mode, region mode, and world-wide mode
- **Curse System**: Algorithmically recreates the original curse mechanism
- **Narehate System**: Players may transform into Narehate (exempted) under deep curse effects
- **Region Management**: Supports custom abyss regions and exemption zones
- **New Achievement Tree**: Complete abyss achievement tree, from Red Whistle to Naraku
- **Visual Effects**: Layer filters, visual effects, and immersive experiences

## Core Mechanics

### Curse System

#### Targets and Exemptions

- **Targets**: Curses only affect players within the abyss region as "targets"
- **Exemption Zones**: Administrators can define exemption zones using two corner points (must provide complete xyz coordinates). Targets within exemption zones are not affected by curses, even if the exemption zone is located inside the abyss
- **Exempted (Narehate)**: Administrators can designate a player as exempted (called Narehate). Exempted players are not affected by curses under any circumstances

#### Curse Modes

The plugin supports two curse modes:

- **abyss-curse mode** (default): Curses only affect targets within the effective range who have accumulated an ascent distance exceeding 2m (y difference > 2) within 20 minutes. See "Accumulated Ascent Mechanism" below for details
- **decompression-sickness mode**: In this mode, curses are only related to the player's height and persist permanently
  - **Shallow Layer** (y >= 0): Applies the curse content of the sixth layer in abyss-curse mode
  - **Deep Layer** (0 > y >= -64): No curse

#### Curse Layers (abyss-curse mode)

In this mode, the curse received is first related to the safe height recorded when triggered. When this safe height is in different intervals, the following curses lasting ten minutes are applied:

> The coordinates mentioned below are default values, calculated proportionally from the original layer dimensions after artificially defining MC's underground as 96 ~ -64. If you're creating a map and need to extend the abyss to a larger range, please customize it in the configuration file (i.e., the plugins/AbyssCurse/config.yml file in the server)

##### Zero: Above the Abyss
- **Height Range**: World height limit > y >= 96
- **Curse**: None
- **Effects**: None

##### One: Edge of the Abyss
- **Height Range**: 96 > y >= 85
- **Curse**: Nausea
- **Effects**: On entry, displays red Title "阿比斯之渊" (Edge of the Abyss), subtitle "Edge of the Abyss", and grants achievement "赤笛" (Red Whistle)

##### Two: Forest of Temptation
- **Height Range**: 85 > y >= 75
- **Curse**: All above + Hunger + Slowness
- **Effects**: On entry, displays green Title "诱惑之森" (Forest of Temptation), subtitle "Forest of Temptation", and grants achievement "苍笛" (Blue Whistle)

##### Three: Great Fault
- **Height Range**: 75 > y >= 40
- **Curse**: All above + Darkness + Random sound effects (sound library includes all monster sounds, all cave ambiance sounds, TNT ignition sound)
- **Effects**: On entry, displays gray Title "大断层" (Great Fault), subtitle "Great Fault", and grants achievement "月笛" (Moon Whistle)

##### Four: Goblets of Giants
- **Height Range**: 40 > y >= 0
- **Curse**: All above + Bad Luck + Mining Fatigue + Weakness
- **Effects**: On entry, displays cyan Title "巨人之杯" (Goblets of Giants), subtitle "Goblets of Giants", and grants achievement "黑笛" (Black Whistle)

##### Five: Sea of Corpses
- **Height Range**: 0 > y >= -8
- **Curse**: All above + Blindness + Disable right-click + Game muted + Poison + Chat invisible
- **Effects**: On entry, displays blue Title "亡骸之海" (Sea of Corpses), subtitle "Sea of Corpses", and grants achievement "白笛" (White Whistle) (Challenge advancement, sound event: `ui.toast.challenge_complete`)

##### Six: Capital of the Unreturned
- **Height Range**: -8 > y >= -28
- **Curse**: All above + Parasite + Withering. If the curse ends after ten minutes without death, transforms into exempted (Narehate)
- **Effects**:
  - On entry, displays golden Title "来无还之都" (Capital of the Unreturned), subtitle "Capital of the Unreturned"
  - Permanently active while in this layer: No longer allows opening chat (but can see content)
  - Grants achievement "绝界行" (Unreturned Journey) (Challenge advancement, sound event: `ui.toast.challenge_complete`)

##### Seven: Final Maelstrom
- **Height Range**: -28 > y >= -64
- **Curse**: All above + Instant Damage 2
- **Effects**:
  - On entry, displays white Title "最终极之涡" (Final Maelstrom), subtitle "Final Maelstrom"
  - No longer allows opening chat (but can see content)
  - Grants achievement "奈落之底" (Naraku) (Challenge advancement, sound event: `ui.toast.challenge_complete`)

#### Curse Duration

The curse is also related to the Chebyshev distance between the "chunk where the triggering player is located" and the "center chunk" (this value is called the curse arm, arm of curse). The duration of negative effects decreases as this value increases, specifically: **10 minutes × (abyss radius - curse arm) / abyss radius**

When a curse of any layer is triggered, it plays the sound effect of "Elder Guardian applying Mining Fatigue" to the target player.

#### Accumulated Ascent Mechanism (abyss-curse mode)

Curses only affect targets within the effective range who have accumulated an ascent distance exceeding 2m (y difference > 2) within 20 minutes.

> Please note that this value is only the default value, designed to accommodate MC's overly shallow underground space. In actual gameplay, a two-block limit is quite unreasonable. If you need to change this value, please customize it in the configuration file.

##### Safe Height

The player's y coordinate when entering the game is stored as a safe height record. The safe height is refreshed (setting the current height as the new safe height) each time the player descends or when the "accumulated ascent height is cleared".

##### Accumulated Ascent Record

The plugin checks each target's current y coordinate once per second (20 ticks):

- **When ascending**: Each block ascended is recorded in real-time (accumulated ascent height +1), and automatically expires after 20 minutes (-1). When it reaches zero, the target's y coordinate at that time is set as the new safe height
- **When descending**: Does not accumulate but eliminates "accumulated ascent height", and also refreshes the safe height

##### Curse Trigger

If a player's accumulated ascent height reaches 2, a "curse content" will be applied once, then the accumulated ascent height is cleared and the safe height is refreshed to await new recording.

##### Accumulated Ascent Height Indicator

Each block of "accumulated ascent height" will display red indicator text on the player's screen:
- 0: Not displayed
- 1: Displays "累计上升高度1" (Accumulated Ascent Height 1)
- 2: Displays "累计上升高度2" (Accumulated Ascent Height 2)

### Narehate System

#### Transformation Mechanism

In abyss-curse mode, players may transform into exempted (Narehate) when cursed in the sixth layer "Capital of the Unreturned".

#### Blessings

Narehate (exempted) have a series of permanent effects called blessings:

- **Curse Exemption**: Narehate are no longer affected by any curses
- **Achievement Reward**: Becoming Narehate immediately grants the achievement "来自深渊" (From the Abyss) (Challenge advancement, sound event: `ui.toast.challenge_complete`)
- **Identity Type**: When becoming Narehate, players randomly receive one of the following identities:

  **Lucky Narehate (LUCKY)**, permanently gains the following positive effects:
  - Speed 2
  - Haste 2
  - Strength 2
  - Jump Boost 2
  - Night Vision
  - Regeneration 2
  - Hero of the Village
  - Dolphin's Grace
  - Conduit Power
  - Luck

  **Sad Narehate (SAD)**, permanently gains the following positive effects:
  - Regeneration 2
  - Resistance 4
  - Absorption 4
  - Fire Resistance
  - Water Breathing
  - Slow Falling
  - Chat permanently cannot be opened (cannot input or send), but can see chat messages

### Achievement System

The achievement tree added by this plugin is called "Abyss" and is separate and independent from the vanilla achievements.

#### Achievement Tree Structure

```
Edge of the Abyss (Root node, default unlocked; Description: The story begins here)
└── Red Whistle (Description: Gaze into the abyss)
    └── Blue Whistle (Description: Leave the cradle)
        └── Moon Whistle (Description: Carve history)
            └── Black Whistle (Description: Bring back stories)
                └── White Whistle (Description: Become a legend, Challenge)
                    └── Unreturned Journey (Description: No return, Challenge)
                        ├── From the Abyss (Description: Return to the abyss, Challenge)
                        └── Naraku (Description: The story will not end here, Challenge)
```

#### Achievement Acquisition Methods

Achievements are automatically triggered through the in-game advancement system and do not require manual granting. Achievement acquisition methods:

- **Edge of the Abyss**: Root node, default unlocked
- **Red Whistle**: Obtained when entering the first layer "Edge of the Abyss"
- **Blue Whistle**: Obtained when entering the second layer "Forest of Temptation"
- **Moon Whistle**: Obtained when entering the third layer "Great Fault"
- **Black Whistle**: Obtained when entering the fourth layer "Goblets of Giants"
- **White Whistle**: Obtained when entering the fifth layer "Sea of Corpses" (Challenge advancement)
- **Unreturned Journey**: Obtained when entering the sixth layer "Capital of the Unreturned" (Challenge advancement)
- **From the Abyss**: Obtained when transforming into Narehate (Challenge advancement)
- **Naraku**: Obtained when entering the seventh layer "Final Maelstrom" (Challenge advancement)

## Installation & Configuration

### Requirements

- **Server**: PaperMC 1.21 or higher
- **Java**: Java 17 or higher
- **Permissions**: Server administrator permissions (for configuration)

### Installation Steps

1. Download the latest version of `AbyssCurse-1.1.0.jar`
2. Place the file in the server's `plugins` directory
3. Restart the server
4. Edit `plugins/AbyssCurse/config.yml` for configuration
5. Use `/abysscurse mode` command to set the plugin mode

### Configuration File

The main configuration file is located at `plugins/AbyssCurse/config.yml`, containing the following settings:

- **Plugin Mode** (OFF/ABYSS/WORLD)
- **Abyss Region Center Coordinates and Radius** (ABYSS mode)
  - Center coordinates: Any block coordinates (x, y, z) within the center chunk
  - Radius: Chebyshev distance, unit is chunks
- **Curse Mode** (abyss-curse/decompression-sickness)
- **Ascent Accumulation Threshold** (default 2.0 blocks)
- **Layer Range Configuration** (customizable height range for each layer)
  - Format: `min <= y < max`
  - Default ranges see "Curse Layers" section above
- **Debug Mode Toggle**
- **Exemption Zone List** (complete xyz coordinates of two corner points)
- **Exempted List** (Narehate player UUID list)

### Data Structure

- Player data is stored in the `plugins/AbyssCurse/players/` directory
- One YAML file per player (UUID.yml)
- Data includes: safe height, accumulated ascent height, curse status, Narehate status, etc.
- Data is automatically saved every 5 minutes

## Usage Guide

### Plugin Modes

The plugin supports three operation modes:

- **OFF (Disabled Mode)**: This is the default plugin mode. In disabled mode, the plugin does not apply curses to any players or entities

- **ABYSS (Region Mode)**: Allows administrators to define a region called abyss in the overworld. The abyss location refers to its center chunk. Administrators only need to provide any block coordinates within the center chunk, and the plugin will automatically determine which chunk it is. The abyss region includes all heights within its range, from the world height lower limit -64 to the world height upper limit 320. The abyss size refers to a "radius" value, which is a Chebyshev distance in chunks. All chunks with a Chebyshev distance less than or equal to this value from the center chunk will be included in the abyss

  > **Why use Chebyshev distance?** In the original work, the Abyss is of course circular, so Euclidean distance should theoretically be used. However, this plugin requires various frequent listeners, and Euclidean distance would cause performance issues. Additionally, a square abyss in Minecraft is very intuitive

- **WORLD (World-wide Mode)**: The entire overworld is considered to be the abyss

## Command Reference

All commands require the `abysscurse.admin` permission (OP has by default).

### Main Command

```
/abysscurse
/abysscurse help
```

Or use aliases:
```
/ac
/abyss
```

Displays command help information.

---

### Mode Switch

```
/abysscurse mode <off|abyss|world> [centerX] [centerY] [centerZ] [radius]
```

Switch plugin operation mode.

**Parameters:**
- `off`: Disable plugin
- `abyss`: Region mode, requires region parameters
- `world`: World-wide mode

**Region Mode Parameters:**
- `centerX`: Abyss center X coordinate (any block coordinate)
- `centerY`: Abyss center Y coordinate (any block coordinate)
- `centerZ`: Abyss center Z coordinate (any block coordinate)
- `radius`: Abyss radius (number of chunks, using Chebyshev distance)

**Examples:**
```
/abysscurse mode off
/abysscurse mode world
/abysscurse mode abyss 0 64 0 10
```

---

### Reload Configuration

```
/abysscurse reload
```

Reload plugin configuration file and apply new configuration settings.

---

### View Information

```
/abysscurse info
```

View current plugin status information, including:
- Current operation mode
- Abyss region configuration (if using region mode)
- Plugin version

---

### Debug Command

```
/abysscurse debug <on|off|toggle|info|global> [on|off]
```

Control debug mode for troubleshooting.

**Subcommands:**
- `on`: Enable player debug mode (player only)
- `off`: Disable player debug mode (player only)
- `toggle`: Toggle player debug mode (player only)
- `info`: Display player debug information (player only)
- `global <on|off>`: Control global debug mode (admin only)

**Examples:**
```
/abysscurse debug on
/abysscurse debug toggle
/abysscurse debug global on
```

---

### Narehate Management

```
/abysscurse narehate <set|remove|check> <player>
```

Manage player's Narehate status.

**Subcommands:**
- `set <player>`: Set the specified player as Narehate
  - Randomly assigns Narehate type (LUCKY or SAD)
  - Plays totem of undying effect
  - Automatically adds to exempted list
- `remove <player>`: Remove the specified player's Narehate status
  - Clears Narehate type
  - Removes from exempted list
- `check <player>`: Check the specified player's Narehate status
  - Shows whether they are Narehate
  - Shows Narehate type (if exists)

**Examples:**
```
/abysscurse narehate set Steve
/abysscurse narehate remove Steve
/abysscurse narehate check Steve
```

**Note:**
- Supports online and offline players
- Setting Narehate automatically plays totem of undying particle effects and sounds
- Narehate automatically gain curse exemption

---

## Permissions

### Permission Nodes

- `abysscurse.admin`: Administrator permission
  - Allows use of all administrator commands
  - Default: OP
- `abysscurse.*`: All permissions
  - Includes all sub-permissions
  - Default: OP

### Permission Configuration Example

Configure in a permission plugin (such as LuckPerms):

```
/lp group admin permission set abysscurse.admin true
```

## Troubleshooting

### Common Issues

**Q: Curses not triggering?**
- Check if plugin mode is `off`
- Confirm player is within the abyss region
- Check if player is in an exemption zone or is exempted
- Use `/abysscurse debug info` to view debug information

**Q: Narehate transformation not working?**
- Confirm player has withering effect (sixth or seventh layer curse)
- Confirm player is within abyss range
- Check if player is already Narehate

**Q: Configuration not taking effect?**
- Use `/abysscurse reload` to reload configuration
- Check if configuration file format is correct
- Check server logs for error messages

## Contributing & License

Issues and Pull Requests are welcome!

See the LICENSE file in the project root directory for license details.

