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
        
        // 设置默认值
        setDefaults();
        
        // 保存配置（确保新添加的默认值被写入）
        saveConfig();
        
        plugin.getLogger().info("配置文件已加载");
    }

    /**
     * 设置默认配置值
     */
    private void setDefaults() {
        // 模式配置
        if (!config.contains("mode")) {
            config.set("mode", PluginMode.OFF.name());
        }
        
        // Abyss 模式配置
        if (!config.contains("abyss.center.x")) {
            config.set("abyss.center.x", 0);
        }
        if (!config.contains("abyss.center.y")) {
            config.set("abyss.center.y", 64);
        }
        if (!config.contains("abyss.center.z")) {
            config.set("abyss.center.z", 0);
        }
        if (!config.contains("abyss.radius")) {
            config.set("abyss.radius", 5);
        }
        
        // 诅咒模式配置
        if (!config.contains("curse-mode")) {
            config.set("curse-mode", "abyss-curse");
        }
        
        // 调试配置
        if (!config.contains("debug.enabled")) {
            config.set("debug.enabled", false);
        }
        
        // 上升积累阈值配置
        if (!config.contains("rise-threshold")) {
            config.set("rise-threshold", 2.0);
        }
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
}

