package io.github.kaleidscoper.abysscurse.mode;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 模式管理器
 * 负责管理插件的三种模式：OFF、ABYSS、WORLD
 */
public class ModeManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private PluginMode currentMode;

    public ModeManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.currentMode = configManager.getMode();
    }

    /**
     * 获取当前模式
     */
    public PluginMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 切换模式
     * @param mode 目标模式
     * @return 是否切换成功
     * 
     * TODO: 占位实现 - 当前仅保存配置，无实际功能
     * 待实现：
     * 1. 模式切换时通知相关模块（RegionManager、CurseManager）
     * 2. 根据模式状态实际控制诅咒系统
     * 3. 模式切换时的效果清理和重新应用
     */
    public boolean setMode(PluginMode mode) {
        if (mode == null) {
            return false;
        }

        // 验证模式切换条件
        if (mode == PluginMode.ABYSS) {
            // ABYSS 模式需要有效的区域配置
            int radius = configManager.getAbyssRadius();
            if (radius <= 0) {
                plugin.getLogger().warning("无法切换到 ABYSS 模式：半径必须大于 0");
                return false;
            }
        }

        PluginMode oldMode = this.currentMode;
        this.currentMode = mode;
        configManager.setMode(mode);

        plugin.getLogger().info("模式已从 " + oldMode.name() + " 切换到 " + mode.name());
        return true;
    }

    /**
     * 检查是否为关闭模式
     */
    public boolean isOff() {
        return currentMode == PluginMode.OFF;
    }

    /**
     * 检查是否为区域开启模式
     */
    public boolean isAbyss() {
        return currentMode == PluginMode.ABYSS;
    }

    /**
     * 检查是否为全部开启模式
     */
    public boolean isWorld() {
        return currentMode == PluginMode.WORLD;
    }

    /**
     * 重载模式（从配置文件读取）
     */
    public void reload() {
        this.currentMode = configManager.getMode();
        plugin.getLogger().info("模式已重载：当前模式为 " + currentMode.name());
    }
}

