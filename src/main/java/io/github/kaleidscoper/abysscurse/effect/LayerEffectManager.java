package io.github.kaleidscoper.abysscurse.effect;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.mode.PluginMode;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import io.github.kaleidscoper.abysscurse.visual.VisualManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


/**
 * 层级效果管理器
 * 负责管理永久层级效果（根据玩家高度施加）
 */
public class LayerEffectManager {
    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final EffectManager effectManager;
    private final ConfigManager configManager;
    private final ModeManager modeManager;
    private final RegionManager regionManager;
    private VisualManager visualManager;
    
    // 定期检查任务
    private BukkitTask checkTask;
    
    public LayerEffectManager(JavaPlugin plugin, PlayerDataManager playerDataManager, EffectManager effectManager, ConfigManager configManager, ModeManager modeManager, RegionManager regionManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.effectManager = effectManager;
        this.configManager = configManager;
        this.modeManager = modeManager;
        this.regionManager = regionManager;
        startCheckTask();
    }
    
    /**
     * 设置视觉管理器
     */
    public void setVisualManager(VisualManager visualManager) {
        this.visualManager = visualManager;
    }
    
    /**
     * 更新玩家层级效果
     * 根据玩家当前高度判断层级并施加对应效果
     * 注意：只在主世界（NORMAL 环境）且在abyss区域内生效
     */
    public void updateLayerEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // 只在主世界显示层级标题
        World world = player.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) {
            // 如果不在主世界，清除视觉显示
            if (visualManager != null) {
                visualManager.clearLayerDisplay(player);
            }
            return;
        }
        
        // 检查插件模式
        PluginMode mode = modeManager.getCurrentMode();
        
        // OFF 模式：不显示层级播报
        if (mode == PluginMode.OFF) {
            if (visualManager != null) {
                visualManager.clearLayerDisplay(player);
            }
            return;
        }
        
        // ABYSS 模式：只在abyss区域内显示层级播报
        if (mode == PluginMode.ABYSS) {
            if (!regionManager.isInAbyss(player.getLocation())) {
                // 如果不在abyss区域内，清除视觉显示
                if (visualManager != null) {
                    visualManager.clearLayerDisplay(player);
                }
                return;
            }
        }
        
        // WORLD 模式：整个主世界都是abyss，继续处理
        
        double y = player.getLocation().getY();
        int layer = getLayerByHeight(y);
        
        // 移除之前的层级效果
        removeLayerEffects(player);
        
        // 施加新的层级效果
        if (layer > 0) {
            applyLayerEffects(player, layer);
            
            // 更新视觉显示（Title 和 BossBar）
            if (visualManager != null) {
                visualManager.updateLayerDisplay(player, layer);
            }
        } else {
            // 清除视觉显示
            if (visualManager != null) {
                visualManager.clearLayerDisplay(player);
            }
        }
    }
    
    /**
     * 根据高度判断层级
     */
    private int getLayerByHeight(double y) {
        return configManager.getLayerByHeight(y);
    }
    
    /**
     * 施加层级效果
     * 注意：根据最新需求，层级增益效果已移除，只保留 Title 和 BossBar 显示
     */
    private void applyLayerEffects(Player player, int layer) {
        // 层级增益效果已移除，不再施加任何药水效果
        // 只保留 Title 和 BossBar 显示（在 updateLayerEffects 方法中处理）
    }
    
    /**
     * 移除层级效果
     */
    private void removeLayerEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // 移除所有层级来源的效果
        effectManager.removeEffectsBySource(player, EffectManager.EffectSource.LAYER);
    }
    
    /**
     * 启动定期检查任务
     */
    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    updateLayerEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // 每20tick（1秒）检查一次
    }
    
    /**
     * 停止层级效果管理器
     */
    public void stop() {
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }
    }
}

