package io.github.kaleidscoper.abysscurse.effect;

import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.visual.VisualManager;
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
    private VisualManager visualManager;
    
    // 定期检查任务
    private BukkitTask checkTask;
    
    public LayerEffectManager(JavaPlugin plugin, PlayerDataManager playerDataManager, EffectManager effectManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.effectManager = effectManager;
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
     */
    public void updateLayerEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
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
        if (y >= 85 && y < 96) return 1;  // 第一层：阿比斯之渊
        if (y >= 75 && y < 85) return 2;   // 第二层：诱惑之森
        if (y >= 40 && y < 75) return 3;   // 第三层：大断层
        if (y >= 0 && y < 40) return 4;    // 第四层：巨人之杯
        if (y >= -8 && y < 0) return 5;    // 第五层：亡骸之海
        if (y >= -28 && y < -8) return 6;  // 第六层：来无还之都
        if (y >= -64 && y < -28) return 7; // 第七层：最终极之涡
        return 0;
    }
    
    /**
     * 施加层级效果
     */
    private void applyLayerEffects(Player player, int layer) {
        switch (layer) {
            case 1: // 第一层：阿比斯之渊 - 生命恢复
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.REGENERATION, 0, -1, EffectManager.EffectSource.LAYER);
                break;
            case 2: // 第二层：诱惑之森 - 伤害吸收1
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.ABSORPTION, 1, -1, EffectManager.EffectSource.LAYER);
                break;
            case 3: // 第三层：大断层 - 抗性提升
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.RESISTANCE, 0, -1, EffectManager.EffectSource.LAYER);
                break;
            case 4: // 第四层：巨人之杯 - 夜视 + 急迫 + 迅捷 + 力量
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.NIGHT_VISION, 0, -1, EffectManager.EffectSource.LAYER);
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.HASTE, 0, -1, EffectManager.EffectSource.LAYER);
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.SPEED, 0, -1, EffectManager.EffectSource.LAYER);
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.STRENGTH, 0, -1, EffectManager.EffectSource.LAYER);
                break;
            case 5: // 第五层：亡骸之海 - 无效果
                break;
            case 6: // 第六层：来无还之都 - 幸运
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.LUCK, 0, -1, EffectManager.EffectSource.LAYER);
                break;
            case 7: // 第七层：最终极之涡 - 缓降
                effectManager.addEffect(player, org.bukkit.potion.PotionEffectType.SLOW_FALLING, 0, -1, EffectManager.EffectSource.LAYER);
                break;
        }
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

