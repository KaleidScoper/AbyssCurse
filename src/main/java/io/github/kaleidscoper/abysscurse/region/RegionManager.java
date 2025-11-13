package io.github.kaleidscoper.abysscurse.region;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.mode.PluginMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 区域管理器
 * 负责管理 Abyss 区域和豁免区，判断玩家是否在区域内
 */
public class RegionManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModeManager modeManager;
    
    // 豁免区列表（存储角点坐标对）
    private final List<ExemptionZone> exemptionZones;
    
    // 豁免者列表（玩家 UUID）
    private final List<UUID> exemptPlayers;

    public RegionManager(JavaPlugin plugin, ConfigManager configManager, ModeManager modeManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.modeManager = modeManager;
        this.exemptionZones = new ArrayList<>();
        this.exemptPlayers = new ArrayList<>();
        
        // 从配置文件加载豁免区和豁免者
        loadExemptionZones();
        loadExemptPlayers();
    }

    /**
     * 判断玩家是否在 Abyss 区域内
     * @param location 玩家位置
     * @return 是否在 Abyss 内
     */
    public boolean isInAbyss(Location location) {
        PluginMode mode = modeManager.getCurrentMode();
        
        // OFF 模式：不在 Abyss 内
        if (mode == PluginMode.OFF) {
            return false;
        }
        
        // WORLD 模式：整个主世界都是 Abyss
        if (mode == PluginMode.WORLD) {
            return location.getWorld() != null && 
                   location.getWorld().getEnvironment() == World.Environment.NORMAL;
        }
        
        // ABYSS 模式：检查是否在定义的区域内
        if (mode == PluginMode.ABYSS) {
            return isInAbyssRegion(location);
        }
        
        return false;
    }

    /**
     * 判断位置是否在 Abyss 区域内（ABYSS 模式）
     * 使用切比雪夫距离计算
     */
    private boolean isInAbyssRegion(Location location) {
        int centerX = configManager.getAbyssCenterX();
        int centerZ = configManager.getAbyssCenterZ();
        int radius = configManager.getAbyssRadius();
        
        // 计算区块坐标
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        
        // 计算切比雪夫距离
        int dx = Math.abs(chunkX - centerChunkX);
        int dz = Math.abs(chunkZ - centerChunkZ);
        
        return Math.max(dx, dz) <= radius;
    }

    /**
     * 计算诅咒臂（Arm of Curse）
     * 诅咒臂 = 玩家所在区块与中心区块的切比雪夫距离
     * @param location 玩家位置
     * @return 诅咒臂值，如果不在 Abyss 内则返回 Integer.MAX_VALUE
     */
    public int getArmOfCurse(Location location) {
        PluginMode mode = modeManager.getCurrentMode();
        
        // WORLD 模式：诅咒臂始终为 0
        if (mode == PluginMode.WORLD) {
            return 0;
        }
        
        // OFF 模式：不在 Abyss 内
        if (mode == PluginMode.OFF) {
            return Integer.MAX_VALUE;
        }
        
        // ABYSS 模式：计算切比雪夫距离
        if (mode == PluginMode.ABYSS) {
            if (!isInAbyssRegion(location)) {
                return Integer.MAX_VALUE;
            }
            
            int centerX = configManager.getAbyssCenterX();
            int centerZ = configManager.getAbyssCenterZ();
            
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            
            int centerChunkX = centerX >> 4;
            int centerChunkZ = centerZ >> 4;
            
            int dx = Math.abs(chunkX - centerChunkX);
            int dz = Math.abs(chunkZ - centerChunkZ);
            
            return Math.max(dx, dz);
        }
        
        return Integer.MAX_VALUE;
    }

    /**
     * 获取 Abyss 半径
     */
    public int getAbyssRadius() {
        return configManager.getAbyssRadius();
    }

    /**
     * 判断玩家是否在豁免区内
     * @param location 玩家位置
     * @return 是否在豁免区内
     */
    public boolean isInExemptionZone(Location location) {
        for (ExemptionZone zone : exemptionZones) {
            if (zone.contains(location)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断玩家是否为豁免者（生骸）
     * @param playerUuid 玩家 UUID
     * @return 是否为豁免者
     */
    public boolean isExemptPlayer(UUID playerUuid) {
        return exemptPlayers.contains(playerUuid);
    }

    /**
     * 判断玩家是否受诅咒影响
     * 玩家必须：1. 在 Abyss 内 2. 不在豁免区内 3. 不是豁免者
     * @param location 玩家位置
     * @param playerUuid 玩家 UUID
     * @return 是否受诅咒影响
     */
    public boolean isAffectedByCurse(Location location, UUID playerUuid) {
        // 不在 Abyss 内
        if (!isInAbyss(location)) {
            return false;
        }
        
        // 在豁免区内
        if (isInExemptionZone(location)) {
            return false;
        }
        
        // 是豁免者
        if (isExemptPlayer(playerUuid)) {
            return false;
        }
        
        return true;
    }

    /**
     * 添加豁免区
     * @param minX 最小 X 坐标
     * @param minY 最小 Y 坐标
     * @param minZ 最小 Z 坐标
     * @param maxX 最大 X 坐标
     * @param maxY 最大 Y 坐标
     * @param maxZ 最大 Z 坐标
     */
    public void addExemptionZone(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        ExemptionZone zone = new ExemptionZone(minX, minY, minZ, maxX, maxY, maxZ);
        exemptionZones.add(zone);
        saveExemptionZones();
    }

    /**
     * 移除豁免区
     * @param index 豁免区索引
     * @return 是否移除成功
     */
    public boolean removeExemptionZone(int index) {
        if (index >= 0 && index < exemptionZones.size()) {
            exemptionZones.remove(index);
            saveExemptionZones();
            return true;
        }
        return false;
    }

    /**
     * 获取所有豁免区
     */
    public List<ExemptionZone> getExemptionZones() {
        return new ArrayList<>(exemptionZones);
    }

    /**
     * 添加豁免者
     * @param playerUuid 玩家 UUID
     */
    public void addExemptPlayer(UUID playerUuid) {
        if (!exemptPlayers.contains(playerUuid)) {
            exemptPlayers.add(playerUuid);
            saveExemptPlayers();
        }
    }

    /**
     * 移除豁免者
     * @param playerUuid 玩家 UUID
     */
    public void removeExemptPlayer(UUID playerUuid) {
        if (exemptPlayers.remove(playerUuid)) {
            saveExemptPlayers();
        }
    }

    /**
     * 获取所有豁免者
     */
    public List<UUID> getExemptPlayers() {
        return new ArrayList<>(exemptPlayers);
    }

    /**
     * 从配置文件加载豁免区
     */
    private void loadExemptionZones() {
        exemptionZones.clear();
        ConfigurationSection exemptionSection = configManager.getConfig().getConfigurationSection("exemption.zones");
        if (exemptionSection != null) {
            for (String key : exemptionSection.getKeys(false)) {
                ConfigurationSection zoneSection = exemptionSection.getConfigurationSection(key);
                if (zoneSection != null) {
                    int minX = zoneSection.getInt("min.x");
                    int minY = zoneSection.getInt("min.y");
                    int minZ = zoneSection.getInt("min.z");
                    int maxX = zoneSection.getInt("max.x");
                    int maxY = zoneSection.getInt("max.y");
                    int maxZ = zoneSection.getInt("max.z");
                    exemptionZones.add(new ExemptionZone(minX, minY, minZ, maxX, maxY, maxZ));
                }
            }
        }
    }

    /**
     * 保存豁免区到配置文件
     */
    private void saveExemptionZones() {
        ConfigurationSection exemptionSection = configManager.getConfig().createSection("exemption.zones");
        for (int i = 0; i < exemptionZones.size(); i++) {
            ExemptionZone zone = exemptionZones.get(i);
            ConfigurationSection zoneSection = exemptionSection.createSection(String.valueOf(i));
            zoneSection.set("min.x", zone.getMinX());
            zoneSection.set("min.y", zone.getMinY());
            zoneSection.set("min.z", zone.getMinZ());
            zoneSection.set("max.x", zone.getMaxX());
            zoneSection.set("max.y", zone.getMaxY());
            zoneSection.set("max.z", zone.getMaxZ());
        }
        configManager.saveConfig();
    }

    /**
     * 从配置文件加载豁免者
     */
    private void loadExemptPlayers() {
        exemptPlayers.clear();
        List<String> exemptPlayerList = configManager.getConfig().getStringList("exemption.players");
        for (String uuidStr : exemptPlayerList) {
            try {
                exemptPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的豁免者 UUID: " + uuidStr);
            }
        }
    }

    /**
     * 保存豁免者到配置文件
     */
    private void saveExemptPlayers() {
        List<String> exemptPlayerList = new ArrayList<>();
        for (UUID uuid : exemptPlayers) {
            exemptPlayerList.add(uuid.toString());
        }
        configManager.getConfig().set("exemption.players", exemptPlayerList);
        configManager.saveConfig();
    }

    /**
     * 重载配置（从配置文件重新加载豁免区和豁免者）
     */
    public void reload() {
        loadExemptionZones();
        loadExemptPlayers();
    }
}

