package io.github.kaleidscoper.abysscurse.effect;

import io.github.kaleidscoper.abysscurse.curse.CurseEffectHandler;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 效果管理器
 * 负责统一管理所有效果施加，避免效果覆盖
 */
public class EffectManager implements CurseEffectHandler {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    // 存储玩家当前所有效果及其来源
    private final Map<UUID, Map<PotionEffectType, EffectData>> playerEffects = new HashMap<>();
    
    // 定期刷新任务
    private BukkitTask refreshTask;
    
    public EffectManager(org.bukkit.plugin.java.JavaPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        startRefreshTask();
    }
    
    /**
     * 效果来源枚举
     */
    public enum EffectSource {
        CURSE,        // 诅咒效果（优先级：中）
        LAYER,        // 层级效果（优先级：高）
        NAREHATE,     // 生骸效果（优先级：最高）
        TEMPORARY     // 临时效果（优先级：低，但短时效果优先）
    }
    
    /**
     * 效果数据类
     */
    public static class EffectData {
        public PotionEffectType type;
        public int amplifier;
        public int duration; // -1 表示永久
        public EffectSource source;
        public long timestamp; // 添加时间
        
        public EffectData(PotionEffectType type, int amplifier, int duration, EffectSource source, long timestamp) {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
            this.source = source;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * 获取玩家效果映射（用于 LayerEffectManager 访问）
     */
    public Map<UUID, Map<PotionEffectType, EffectData>> getPlayerEffects() {
        return playerEffects;
    }
    
    /**
     * 移除指定来源的效果
     */
    public void removeEffectsBySource(Player player, EffectSource source) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        Map<PotionEffectType, EffectData> effects = playerEffects.get(uuid);
        if (effects == null) {
            return;
        }
        
        List<PotionEffectType> toRemove = new ArrayList<>();
        for (Map.Entry<PotionEffectType, EffectData> entry : effects.entrySet()) {
            if (entry.getValue().source == source) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (PotionEffectType type : toRemove) {
            effects.remove(type);
            player.removePotionEffect(type);
        }
    }
    
    /**
     * 施加诅咒效果
     */
    @Override
    public void applyCurseEffects(Player player, int layer, long duration) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // 根据层级施加对应效果
        switch (layer) {
            case 1: // 第一层：阿比斯之渊
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                break;
            case 2: // 第二层：诱惑之森
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                break;
            case 3: // 第三层：大断层
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.DARKNESS, 1, (int)duration, EffectSource.CURSE);
                break;
            case 4: // 第四层：巨人之杯
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.DARKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.UNLUCK, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.MINING_FATIGUE, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WEAKNESS, 1, (int)duration, EffectSource.CURSE);
                break;
            case 5: // 第五层：亡骸之海
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.DARKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.UNLUCK, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.MINING_FATIGUE, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WEAKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.BLINDNESS, 1, (int)duration, EffectSource.CURSE); // 失明
                addEffect(player, PotionEffectType.POISON, 1, (int)duration, EffectSource.CURSE);
                // 注意：禁止右键、游戏静音、聊天栏不可见等效果在事件监听器中处理
                break;
            case 6: // 第六层：来无还之都
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.DARKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.UNLUCK, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.MINING_FATIGUE, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WEAKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.BLINDNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.POISON, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WITHER, 1, (int)duration, EffectSource.CURSE);
                // 注意：寄生效果在第四阶段实现
                break;
            case 7: // 第七层：最终极之涡
                addEffect(player, PotionEffectType.NAUSEA, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.HUNGER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.SLOWNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.DARKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.UNLUCK, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.MINING_FATIGUE, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WEAKNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.BLINDNESS, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.POISON, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.WITHER, 1, (int)duration, EffectSource.CURSE);
                addEffect(player, PotionEffectType.INSTANT_DAMAGE, 1, (int)duration, EffectSource.CURSE);
                break;
        }
    }
    
    /**
     * 移除诅咒效果
     */
    @Override
    public void removeCurseEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        Map<PotionEffectType, EffectData> effects = playerEffects.get(uuid);
        if (effects == null) {
            return;
        }
        
        // 移除所有诅咒来源的效果
        List<PotionEffectType> toRemove = new ArrayList<>();
        for (Map.Entry<PotionEffectType, EffectData> entry : effects.entrySet()) {
            if (entry.getValue().source == EffectSource.CURSE) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (PotionEffectType type : toRemove) {
            effects.remove(type);
            player.removePotionEffect(type);
        }
    }
    
    /**
     * 添加效果
     */
    public void addEffect(Player player, PotionEffectType type, int amplifier, int duration, EffectSource source) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        Map<PotionEffectType, EffectData> effects = playerEffects.computeIfAbsent(uuid, k -> new HashMap<>());
        
        EffectData existing = effects.get(type);
        EffectData newEffect = new EffectData(type, amplifier, duration, source, System.currentTimeMillis());
        
        // 判断是否应该添加/覆盖
        boolean shouldApply = false;
        boolean playerCurrentlyHasEffect = player.hasPotionEffect(type);
        if (existing == null || !playerCurrentlyHasEffect) {
            // 玩家当前没有该效果（可能被牛奶等方式移除），应重新施加
            shouldApply = true;
        } else {
            if (shouldOverride(existing, newEffect)) {
                shouldApply = true;
            }
        }
        
        if (shouldApply) {
            effects.put(type, newEffect);
            applyEffectToPlayer(player, newEffect);
        }
    }
    
    /**
     * 判断是否应该覆盖
     */
    private boolean shouldOverride(EffectData existing, EffectData newEffect) {
        int existingPriority = getPriority(existing.source);
        int newPriority = getPriority(newEffect.source);
        
        // 优先级高的覆盖优先级低的
        if (newPriority > existingPriority) {
            return true;
        }
        
        // 相同优先级时，强度高的覆盖强度低的
        if (newPriority == existingPriority) {
            if (newEffect.amplifier > existing.amplifier) {
                return true;
            }
            // 强度相同时，短时效果优先（避免永久效果覆盖短时效果）
            if (newEffect.amplifier == existing.amplifier) {
                if (existing.duration == -1 && newEffect.duration > 0) {
                    return false; // 现有效果是永久的，新效果是短时的，不覆盖
                }
                if (newEffect.duration == -1 && existing.duration > 0) {
                    return true; // 新效果是永久的，现有效果是短时的，覆盖
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取优先级
     */
    private int getPriority(EffectSource source) {
        switch (source) {
            case NAREHATE: return 3;
            case LAYER: return 2;
            case CURSE: return 1;
            case TEMPORARY: return 0;
            default: return 0;
        }
    }
    
    /**
     * 应用效果到玩家
     */
    private void applyEffectToPlayer(Player player, EffectData effect) {
        PotionEffect potionEffect = new PotionEffect(
            effect.type,
            effect.duration == -1 ? Integer.MAX_VALUE : effect.duration,
            effect.amplifier,
            true,  // 环境粒子
            false   // 不显示图标（避免UI混乱）
        );
        
        // 先移除再添加，避免覆盖问题
        player.removePotionEffect(effect.type);
        player.addPotionEffect(potionEffect);
    }
    
    /**
     * 启动定期刷新任务
     */
    private void startRefreshTask() {
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    refreshEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // 每20tick（1秒）刷新一次
    }
    
    /**
     * 刷新玩家效果
     * 注意：诅咒效果（EffectSource.CURSE）不会被刷新，由 CurseManager 管理
     */
    public void refreshEffects(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        Map<PotionEffectType, EffectData> effects = playerEffects.get(uuid);
        if (effects == null) {
            return;
        }
        
        long now = System.currentTimeMillis();
        List<PotionEffectType> toRemove = new ArrayList<>();
        
        for (Map.Entry<PotionEffectType, EffectData> entry : effects.entrySet()) {
            EffectData effect = entry.getValue();
            
            // 诅咒效果不刷新，由 CurseManager 管理
            if (effect.source == EffectSource.CURSE) {
                // 只检查是否过期，不重新应用
                if (effect.duration > 0) {
                    long elapsed = now - effect.timestamp;
                    if (elapsed >= effect.duration * 50) { // duration 是 tick 数
                        toRemove.add(entry.getKey());
                    }
                }
                continue;
            }
            
            // 检查短时效果是否过期
            if (effect.duration > 0) {
                long elapsed = now - effect.timestamp;
                if (elapsed >= effect.duration * 50) { // duration 是 tick 数
                    toRemove.add(entry.getKey());
                } else {
                    // 重新应用效果（确保效果持续）
                    applyEffectToPlayer(player, effect);
                }
            } else {
                // 永久效果，直接重新应用
                applyEffectToPlayer(player, effect);
            }
        }
        
        // 移除过期效果
        for (PotionEffectType type : toRemove) {
            effects.remove(type);
            player.removePotionEffect(type);
        }
    }
    
    /**
     * 停止效果管理器
     */
    public void stop() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
        }
    }
}

