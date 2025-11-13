package io.github.kaleidscoper.abysscurse;

import io.github.kaleidscoper.abysscurse.command.CommandHandler;
import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.debug.DebugManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AbyssCurse 插件主类
 * 一个为 Minecraft 添加深渊诅咒机制的 PaperMC 插件
 */
public final class AbyssCursePlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ModeManager modeManager;
    private CommandHandler commandHandler;
    private DebugManager debugManager;

    @Override
    public void onEnable() {
        getLogger().info("正在加载 AbyssCurse 插件...");

        try {
            // 初始化配置管理器
            configManager = new ConfigManager(this);
            getLogger().info("配置管理器已初始化");

            // 初始化模式管理器
            modeManager = new ModeManager(this, configManager);
            getLogger().info("模式管理器已初始化，当前模式: " + modeManager.getCurrentMode().name());

            // 初始化调试管理器
            debugManager = new DebugManager(this, configManager, modeManager);
            getLogger().info("调试管理器已初始化");

            // 初始化命令处理器
            commandHandler = new CommandHandler(this, modeManager, configManager, debugManager);
            getCommand("abysscurse").setExecutor(commandHandler);
            getCommand("abysscurse").setTabCompleter(commandHandler);
            getLogger().info("命令处理器已注册");

            // 注册事件监听器
            getServer().getPluginManager().registerEvents(new AbyssCurseListener(), this);
            getLogger().info("事件监听器已注册");

            // 启动调试管理器（如果全局调试开启）
            if (configManager.isDebugEnabled()) {
                debugManager.start();
            }

            getLogger().info("§aAbyssCurse 插件已成功加载！");
            getLogger().info("当前模式: " + modeManager.getCurrentMode().name());
        } catch (Exception e) {
            getLogger().severe("插件加载失败！");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("正在卸载 AbyssCurse 插件...");

        // 停止调试管理器
        if (debugManager != null) {
            debugManager.stop();
        }

        // 清理资源
        if (configManager != null) {
            configManager.saveConfig();
        }

        getLogger().info("AbyssCurse 插件已卸载！");
    }

    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取模式管理器
     */
    public ModeManager getModeManager() {
        return modeManager;
    }

    /**
     * 获取命令处理器
     */
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    /**
     * 获取调试管理器
     */
    public DebugManager getDebugManager() {
        return debugManager;
    }
}
