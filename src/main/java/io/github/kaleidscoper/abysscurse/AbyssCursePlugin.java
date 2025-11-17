package io.github.kaleidscoper.abysscurse;

import io.github.kaleidscoper.abysscurse.achievement.AchievementManager;
import io.github.kaleidscoper.abysscurse.command.CommandHandler;
import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.curse.CurseManager;
import io.github.kaleidscoper.abysscurse.curse.NarehateManager;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.debug.DebugManager;
import io.github.kaleidscoper.abysscurse.effect.EffectManager;
import io.github.kaleidscoper.abysscurse.effect.LayerEffectManager;
import io.github.kaleidscoper.abysscurse.filter.FilterManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import io.github.kaleidscoper.abysscurse.sound.SoundManager;
import io.github.kaleidscoper.abysscurse.visual.VisualManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * AbyssCurse 插件主类
 * 一个为 Minecraft 添加深渊诅咒机制的 PaperMC 插件
 */
public final class AbyssCursePlugin extends JavaPlugin {

    private ConfigManager configManager;
    private ModeManager modeManager;
    private RegionManager regionManager;
    private PlayerDataManager playerDataManager;
    private EffectManager effectManager;
    private LayerEffectManager layerEffectManager;
    private CurseManager curseManager;
    private NarehateManager narehateManager;
    private FilterManager filterManager;
    private SoundManager soundManager;
    private VisualManager visualManager;
    private CommandHandler commandHandler;
    private DebugManager debugManager;
    private AchievementManager achievementManager;
    private Objective layerObjective;
    
    // 定期自动保存任务
    private BukkitTask autoSaveTask;

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

            // 初始化区域管理器
            regionManager = new RegionManager(this, configManager, modeManager);
            getLogger().info("区域管理器已初始化");

            // 初始化玩家数据管理器
            playerDataManager = new PlayerDataManager(this);
            getLogger().info("玩家数据管理器已初始化");

            // 初始化效果管理器
            effectManager = new EffectManager(this, playerDataManager);
            getLogger().info("效果管理器已初始化");
            
            // 初始化滤镜管理器（需要在 CurseManager 之前创建）
            filterManager = new FilterManager(this, playerDataManager, configManager);
            getLogger().info("滤镜管理器已初始化");
            
            // 初始化音效管理器（需要在 CurseManager 之前创建）
            soundManager = new SoundManager(this);
            getLogger().info("音效管理器已初始化");
            
            // 初始化视觉管理器（需要在 LayerEffectManager 之前创建）
            visualManager = new VisualManager(this);
            getLogger().info("视觉管理器已初始化");
            
            // 初始化层级效果管理器
            layerEffectManager = new LayerEffectManager(this, playerDataManager, effectManager, configManager);
            layerEffectManager.setVisualManager(visualManager);
            getLogger().info("层级效果管理器已初始化");
            
            // 初始化诅咒管理器（需要在依赖管理器创建之后）
            curseManager = new CurseManager(this, playerDataManager, regionManager, configManager);
            curseManager.setEffectHandler(effectManager);
            curseManager.setFilterManager(filterManager);
            curseManager.setSoundManager(soundManager);
            getLogger().info("诅咒管理器已初始化");
            
            // 初始化生骸管理器
            narehateManager = new NarehateManager(this, playerDataManager, regionManager);
            getLogger().info("生骸管理器已初始化");

            // 初始化成就管理器
            achievementManager = new AchievementManager(this);
            achievementManager.initialize();
            getLogger().info("成就管理器已初始化");

            // 初始化调试管理器
            debugManager = new DebugManager(this, configManager, modeManager, regionManager, playerDataManager);
            getLogger().info("调试管理器已初始化");

            // 初始化层级成就所需的 scoreboard
            setupLayerObjective();

            // 初始化命令处理器
            commandHandler = new CommandHandler(this, modeManager, configManager, debugManager, playerDataManager, regionManager);
            getCommand("abysscurse").setExecutor(commandHandler);
            getCommand("abysscurse").setTabCompleter(commandHandler);
            getLogger().info("命令处理器已注册");

            // 注册事件监听器
            getServer().getPluginManager().registerEvents(new AbyssCurseListener(this), this);
            getLogger().info("事件监听器已注册");
            
            // 启动定期自动保存任务（每5分钟保存一次）
            autoSaveTask = getServer().getScheduler().runTaskTimer(this, () -> {
                playerDataManager.autoSave();
            }, 6000, 6000); // 6000 tick = 5分钟
            getLogger().info("自动保存任务已启动");

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

        // 停止自动保存任务
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
            autoSaveTask.cancel();
        }

        // 保存所有玩家数据
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }

        // 停止所有管理器
        if (effectManager != null) {
            effectManager.stop();
        }
        if (layerEffectManager != null) {
            layerEffectManager.stop();
        }
        if (filterManager != null) {
            filterManager.stop();
        }
        if (visualManager != null) {
            visualManager.stop();
        }
        if (narehateManager != null) {
            narehateManager.stop();
        }
        if (debugManager != null) {
            debugManager.stop();
        }
        if (achievementManager != null) {
            achievementManager.shutdown();
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
     * 获取区域管理器
     */
    public RegionManager getRegionManager() {
        return regionManager;
    }

    /**
     * 获取玩家数据管理器
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * 获取调试管理器
     */
    public DebugManager getDebugManager() {
        return debugManager;
    }
    
    /**
     * 获取诅咒管理器
     */
    public CurseManager getCurseManager() {
        return curseManager;
    }
    
    /**
     * 获取效果管理器
     */
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    /**
     * 获取滤镜管理器
     */
    public FilterManager getFilterManager() {
        return filterManager;
    }
    
    /**
     * 获取音效管理器
     */
    public SoundManager getSoundManager() {
        return soundManager;
    }
    
    /**
     * 获取视觉管理器
     */
    public VisualManager getVisualManager() {
        return visualManager;
    }
    
    /**
     * 获取生骸管理器
     */
    public NarehateManager getNarehateManager() {
        return narehateManager;
    }
    
    /**
     * 获取成就管理器
     */
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    /**
     * 获取层级记分板目标
     */
    public Objective getLayerObjective() {
        return layerObjective;
    }

    private void setupLayerObjective() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            getLogger().warning("无法初始化层级记分板，部分成就可能无法触发");
            return;
        }

        Scoreboard scoreboard = manager.getMainScoreboard();
        Objective objective = scoreboard.getObjective("abyss_layer");
        if (objective == null) {
            try {
                objective = scoreboard.registerNewObjective("abyss_layer", Criteria.DUMMY, Component.text("Abyss Layer"));
            } catch (IllegalArgumentException e) {
                objective = scoreboard.getObjective("abyss_layer");
            }
        }
        this.layerObjective = objective;
    }
}
