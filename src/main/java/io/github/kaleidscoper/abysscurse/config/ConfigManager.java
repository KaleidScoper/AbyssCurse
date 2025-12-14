package io.github.kaleidscoper.abysscurse.config;

import io.github.kaleidscoper.abysscurse.mode.PluginMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * 配置管理器
 * 负责加载和保存配置文件
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 确保数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 如果配置文件不存在，创建默认配置
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        // 加载配置
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // 设置默认值（只在缺失时设置）
        boolean hasNewDefaults = setDefaults();
        
        // 只有在添加了新默认值时才保存配置，避免覆盖用户修改
        if (hasNewDefaults) {
            saveConfig();
            plugin.getLogger().info("已添加缺失的默认配置项");
        }
        
        plugin.getLogger().info("配置文件已加载");
    }

    /**
     * 设置默认配置值
     * @return 是否添加了新的默认值
     */
    private boolean setDefaults() {
        boolean hasNewDefaults = false;
        
        // 模式配置
        if (!config.contains("mode")) {
            config.set("mode", PluginMode.OFF.name());
            hasNewDefaults = true;
        }
        
        // Abyss 模式配置
        if (!config.contains("abyss.center.x")) {
            config.set("abyss.center.x", 0);
            hasNewDefaults = true;
        }
        if (!config.contains("abyss.center.y")) {
            config.set("abyss.center.y", 64);
            hasNewDefaults = true;
        }
        if (!config.contains("abyss.center.z")) {
            config.set("abyss.center.z", 0);
            hasNewDefaults = true;
        }
        if (!config.contains("abyss.radius")) {
            config.set("abyss.radius", 5);
            hasNewDefaults = true;
        }
        
        // 诅咒模式配置
        if (!config.contains("curse-mode")) {
            config.set("curse-mode", "abyss-curse");
            hasNewDefaults = true;
        }
        
        // 调试配置
        if (!config.contains("debug.enabled")) {
            config.set("debug.enabled", false);
            hasNewDefaults = true;
        }
        
        // 上升积累阈值配置
        if (!config.contains("rise-threshold")) {
            config.set("rise-threshold", 2.0);
            hasNewDefaults = true;
        }
        
        // 层级范围配置
        for (int layer = 1; layer <= 7; layer++) {
            String minKey = "layers." + layer + ".min";
            String maxKey = "layers." + layer + ".max";
            if (!config.contains(minKey)) {
                config.set(minKey, getDefaultLayerMin(layer));
                hasNewDefaults = true;
            }
            if (!config.contains(maxKey)) {
                config.set(maxKey, getDefaultLayerMax(layer));
                hasNewDefaults = true;
            }
        }
        
        return hasNewDefaults;
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存配置文件", e);
        }
    }

    /**
     * 重载配置
     */
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("配置文件已重载");
    }

    /**
     * 获取配置对象
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * 获取当前模式
     */
    public PluginMode getMode() {
        String modeStr = config.getString("mode", PluginMode.OFF.name());
        try {
            return PluginMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的模式配置: " + modeStr + "，使用默认模式 OFF");
            return PluginMode.OFF;
        }
    }

    /**
     * 设置模式
     */
    public void setMode(PluginMode mode) {
        config.set("mode", mode.name());
        saveConfig();
    }

    /**
     * 获取 Abyss 中心 X 坐标
     */
    public int getAbyssCenterX() {
        return config.getInt("abyss.center.x", 0);
    }

    /**
     * 获取 Abyss 中心 Y 坐标
     */
    public int getAbyssCenterY() {
        return config.getInt("abyss.center.y", 64);
    }

    /**
     * 获取 Abyss 中心 Z 坐标
     */
    public int getAbyssCenterZ() {
        return config.getInt("abyss.center.z", 0);
    }

    /**
     * 获取 Abyss 半径
     */
    public int getAbyssRadius() {
        return config.getInt("abyss.radius", 5);
    }

    /**
     * 设置 Abyss 区域
     */
    public void setAbyssRegion(int x, int y, int z, int radius) {
        config.set("abyss.center.x", x);
        config.set("abyss.center.y", y);
        config.set("abyss.center.z", z);
        config.set("abyss.radius", radius);
        saveConfig();
    }

    /**
     * 获取诅咒模式
     */
    public String getCurseMode() {
        return config.getString("curse-mode", "abyss-curse");
    }

    /**
     * 设置诅咒模式
     */
    public void setCurseMode(String curseMode) {
        config.set("curse-mode", curseMode);
        saveConfig();
    }

    /**
     * 是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    /**
     * 设置调试模式
     */
    public void setDebugEnabled(boolean enabled) {
        config.set("debug.enabled", enabled);
        saveConfig();
    }
    
    /**
     * 获取上升积累阈值（单位：格）
     * 当玩家累计上升高度达到此阈值时，将触发诅咒
     */
    public double getRiseThreshold() {
        return config.getDouble("rise-threshold", 2.0);
    }
    
    /**
     * 设置上升积累阈值（单位：格）
     */
    public void setRiseThreshold(double threshold) {
        config.set("rise-threshold", threshold);
        saveConfig();
    }
    
    /**
     * 获取指定层级的最小高度
     * @param layer 层级（1-7）
     * @return 最小高度
     */
    public double getLayerMinHeight(int layer) {
        if (layer < 1 || layer > 7) {
            plugin.getLogger().warning("无效的层级: " + layer + "，使用默认值");
            return 0.0;
        }
        return config.getDouble("layers." + layer + ".min", getDefaultLayerMin(layer));
    }
    
    /**
     * 获取指定层级的最大高度
     * @param layer 层级（1-7）
     * @return 最大高度
     */
    public double getLayerMaxHeight(int layer) {
        if (layer < 1 || layer > 7) {
            plugin.getLogger().warning("无效的层级: " + layer + "，使用默认值");
            return 0.0;
        }
        return config.getDouble("layers." + layer + ".max", getDefaultLayerMax(layer));
    }
    
    /**
     * 根据高度判断层级
     * @param y 高度
     * @return 层级（1-7），0表示不在任何层级
     */
    public int getLayerByHeight(double y) {
        for (int layer = 1; layer <= 7; layer++) {
            double min = getLayerMinHeight(layer);
            double max = getLayerMaxHeight(layer);
            if (y >= min && y < max) {
                return layer;
            }
        }
        return 0;
    }
    
    /**
     * 获取默认层级最小高度（用于配置缺失时的回退）
     */
    private double getDefaultLayerMin(int layer) {
        switch (layer) {
            case 1: return 85.0;
            case 2: return 75.0;
            case 3: return 40.0;
            case 4: return 0.0;
            case 5: return -8.0;
            case 6: return -28.0;
            case 7: return -64.0;
            default: return 0.0;
        }
    }
    
    /**
     * 获取默认层级最大高度（用于配置缺失时的回退）
     */
    private double getDefaultLayerMax(int layer) {
        switch (layer) {
            case 1: return 96.0;
            case 2: return 85.0;
            case 3: return 75.0;
            case 4: return 40.0;
            case 5: return 0.0;
            case 6: return -8.0;
            case 7: return -28.0;
            default: return 0.0;
        }
    }
}

