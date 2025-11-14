package io.github.kaleidscoper.abysscurse.curse;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.data.PlayerCurseData;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.filter.FilterManager;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import io.github.kaleidscoper.abysscurse.sound.SoundManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 诅咒管理器
 * 负责管理诅咒触发逻辑和持续时间检查
 */
public class CurseManager {
    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final RegionManager regionManager;
    private final ConfigManager configManager;
    
    // 存储诅咒检查任务
    private final Map<UUID, BukkitTask> curseCheckTasks = new HashMap<>();
    
    // 诅咒效果管理器（将在初始化时注入）
    private CurseEffectHandler effectHandler;
    
    // 滤镜管理器和音效管理器（将在初始化时注入）
    private FilterManager filterManager;
    private SoundManager soundManager;
    
    public CurseManager(JavaPlugin plugin, PlayerDataManager playerDataManager, RegionManager regionManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.regionManager = regionManager;
        this.configManager = configManager;
    }
    
    /**
     * 设置效果处理器
     */
    public void setEffectHandler(CurseEffectHandler effectHandler) {
        this.effectHandler = effectHandler;
    }
    
    /**
     * 设置滤镜管理器
     */
    public void setFilterManager(FilterManager filterManager) {
        this.filterManager = filterManager;
    }
    
    /**
     * 设置音效管理器
     */
    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * 触发诅咒
     * 当玩家累计上升高度达到2m时调用
     * @param player 玩家
     * @param safeHeight 触发诅咒时的安全高度
     */
    public void triggerCurse(Player player, double safeHeight) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null) {
            return;
        }
        
        // 检查玩家是否受诅咒影响
        Location location = player.getLocation();
        if (!regionManager.isAffectedByCurse(location, player.getUniqueId())) {
            return;
        }
        
        // 计算诅咒臂
        int armOfCurse = regionManager.getArmOfCurse(location);
        int abyssRadius = regionManager.getAbyssRadius();
        
        // 计算动态诅咒持续时间
        long duration = calculateCurseDuration(armOfCurse, abyssRadius);
        
        // 判断诅咒层级（根据安全高度）
        int layer = getCurseLayer(safeHeight);
        
        if (layer == 0) {
            // 不在任何层级范围内，不触发诅咒
            return;
        }
        
        // 更新玩家数据
        data.setCurrentLayer(layer);
        data.setCurseStartTime(System.currentTimeMillis());
        data.setCurseDuration(duration);
        data.setCurseArm(armOfCurse);
        
        // 施加诅咒效果
        if (effectHandler != null) {
            effectHandler.applyCurseEffects(player, layer, duration);
        }
        
        // 第三层特殊处理：开始播放随机音效
        if (layer == 3 && soundManager != null) {
            soundManager.startRandomSounds(player);
        }
        
        // 播放诅咒触发音效
        if (soundManager != null) {
            soundManager.playCurseSound(player);
        }
        
        // 启动诅咒检查任务（检查诅咒是否过期）
        startCurseCheckTask(player);
        
        plugin.getLogger().info("玩家 " + player.getName() + " 触发了第 " + layer + " 层诅咒（安全高度: " + safeHeight + ", 持续时间: " + (duration / 20) + "秒）");
    }
    
    /**
     * 判断诅咒层级（根据安全高度）
     * @param safeHeight 安全高度
     * @return 诅咒层级（1-7），0表示不在任何层级
     */
    public int getCurseLayer(double safeHeight) {
        return configManager.getLayerByHeight(safeHeight);
    }
    
    /**
     * 计算动态诅咒持续时间
     * @param armOfCurse 诅咒臂
     * @param abyssRadius Abyss 半径
     * @return 持续时间（tick数）
     */
    public long calculateCurseDuration(int armOfCurse, int abyssRadius) {
        if (abyssRadius <= 0) {
            // 防止除零错误，返回默认值
            return 600 * 20; // 10分钟
        }
        
        // 基础持续时间：10分钟 = 600秒 = 12000 tick
        long baseDuration = 600 * 20;
        
        // 实际持续时间 = 10分钟 * (abyss半径 - 诅咒臂) / abyss半径
        double ratio = (double)(abyssRadius - armOfCurse) / abyssRadius;
        long actualDuration = (long)(baseDuration * ratio);
        
        // 确保持续时间不为负
        return Math.max(0, actualDuration);
    }
    
    /**
     * 启动诅咒检查任务
     * 定期检查诅咒是否过期
     */
    private void startCurseCheckTask(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 如果已有任务，先取消
        BukkitTask existingTask = curseCheckTasks.get(uuid);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
        
        // 创建新任务（每20tick检查一次）
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    curseCheckTasks.remove(uuid);
                    return;
                }
                
                checkCurseExpiry(player);
            }
        }.runTaskTimer(plugin, 0, 20); // 每20tick（1秒）执行一次
        
        curseCheckTasks.put(uuid, task);
    }
    
    /**
     * 检查诅咒是否过期
     */
    public void checkCurseExpiry(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null) {
            return;
        }
        
        int currentLayer = data.getCurrentLayer();
        if (currentLayer == 0) {
            // 无诅咒，停止检查任务
            BukkitTask task = curseCheckTasks.remove(player.getUniqueId());
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
            return;
        }
        
        long now = System.currentTimeMillis();
        long elapsed = (now - data.getCurseStartTime()) / 50; // 转换为 tick
        
        if (elapsed >= data.getCurseDuration()) {
            // 诅咒过期
            handleCurseExpiry(player, currentLayer);
        }
    }
    
    /**
     * 处理诅咒过期
     */
    private void handleCurseExpiry(Player player, int layer) {
        PlayerCurseData data = playerDataManager.getData(player);
        
        // 第六层：诅咒过期时的处理（生骸转换由 NarehateManager 在暴露期间每分钟检查）
        if (layer == 6) {
            // 生骸转换逻辑已由 NarehateManager 处理
            // 玩家在暴露于深层诅咒期间每分钟都有概率转换为生骸
        } else if (layer == 7) {
            // 第七层：诅咒结束后未死亡则强制击杀
            if (player.isOnline() && !player.isDead()) {
                player.setHealth(0);
            }
        }
        
        // 清除诅咒效果
        if (effectHandler != null) {
            effectHandler.removeCurseEffects(player);
        }
        
        // 停止第三层随机音效
        if (layer == 3 && soundManager != null) {
            soundManager.stopRandomSounds(player);
        }
        
        // 清除诅咒数据
        data.setCurrentLayer(0);
        data.setCurseStartTime(0);
        data.setCurseDuration(0);
        data.setCurseArm(0);
        
        // 停止检查任务
        BukkitTask task = curseCheckTasks.remove(player.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        
        plugin.getLogger().info("玩家 " + player.getName() + " 的第 " + layer + " 层诅咒已过期");
    }
    
    /**
     * 停止玩家的诅咒检查任务
     */
    public void stopCurseCheck(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = curseCheckTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}

