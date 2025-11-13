package io.github.kaleidscoper.abysscurse.sound;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 音效管理器
 * 负责管理音效播放
 */
public class SoundManager {
    private final JavaPlugin plugin;
    
    // 第三层随机音效库
    private final List<Sound> randomSoundLibrary;
    
    // 存储玩家的随机音效任务（第三层）
    private final Map<UUID, BukkitTask> randomSoundTasks = new HashMap<>();
    
    public SoundManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.randomSoundLibrary = buildRandomSoundLibrary();
    }
    
    /**
     * 构建随机音效库
     * 包括：所有怪物音效、所有洞穴氛围音效、TNT点燃音效
     */
    private List<Sound> buildRandomSoundLibrary() {
        List<Sound> sounds = new ArrayList<>();
        
        // 怪物音效
        sounds.add(Sound.ENTITY_ZOMBIE_AMBIENT);
        sounds.add(Sound.ENTITY_SKELETON_AMBIENT);
        sounds.add(Sound.ENTITY_CREEPER_PRIMED);
        sounds.add(Sound.ENTITY_SPIDER_AMBIENT);
        sounds.add(Sound.ENTITY_ENDERMAN_STARE);
        sounds.add(Sound.ENTITY_WITCH_AMBIENT);
        sounds.add(Sound.ENTITY_GHAST_AMBIENT);
        sounds.add(Sound.ENTITY_BLAZE_AMBIENT);
        sounds.add(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
        sounds.add(Sound.ENTITY_WITHER_AMBIENT);
        sounds.add(Sound.ENTITY_ELDER_GUARDIAN_AMBIENT);
        sounds.add(Sound.ENTITY_GUARDIAN_AMBIENT);
        sounds.add(Sound.ENTITY_SHULKER_AMBIENT);
        sounds.add(Sound.ENTITY_ILLUSIONER_AMBIENT);
        sounds.add(Sound.ENTITY_EVOKER_AMBIENT);
        sounds.add(Sound.ENTITY_VINDICATOR_AMBIENT);
        sounds.add(Sound.ENTITY_PILLAGER_AMBIENT);
        sounds.add(Sound.ENTITY_RAVAGER_AMBIENT);
        sounds.add(Sound.ENTITY_HOGLIN_AMBIENT);
        sounds.add(Sound.ENTITY_PIGLIN_AMBIENT);
        sounds.add(Sound.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT);
        sounds.add(Sound.ENTITY_PIGLIN_BRUTE_AMBIENT);
        sounds.add(Sound.ENTITY_ZOGLIN_AMBIENT);
        sounds.add(Sound.ENTITY_WARDEN_AMBIENT);
        sounds.add(Sound.ENTITY_WARDEN_ANGRY);
        sounds.add(Sound.ENTITY_WARDEN_HEARTBEAT);
        
        // 洞穴氛围音效
        sounds.add(Sound.AMBIENT_CAVE);
        sounds.add(Sound.AMBIENT_UNDERWATER_ENTER);
        sounds.add(Sound.AMBIENT_UNDERWATER_EXIT);
        sounds.add(Sound.AMBIENT_UNDERWATER_LOOP);
        sounds.add(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS);
        sounds.add(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE);
        sounds.add(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE);
        // AMBIENT_UNDERWATER_LOOP_ADDITIONS_URBAN 可能不存在，使用其他音效替代
        // sounds.add(Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_URBAN);
        
        // TNT 点燃音效
        sounds.add(Sound.ENTITY_TNT_PRIMED);
        
        return sounds;
    }
    
    /**
     * 播放诅咒触发音效
     * 远古守卫者施加挖掘疲劳时的音效
     */
    public void playCurseSound(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 1.0f);
    }
    
    /**
     * 开始播放第三层随机音效
     */
    public void startRandomSounds(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 如果已有任务，先取消
        BukkitTask existingTask = randomSoundTasks.get(uuid);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
        
        // 创建随机音效任务（每3-5秒随机播放一次）
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    randomSoundTasks.remove(uuid);
                    return;
                }
                
                // 随机选择音效
                Sound randomSound = randomSoundLibrary.get(new Random().nextInt(randomSoundLibrary.size()));
                player.playSound(player.getLocation(), randomSound, 0.5f, 1.0f);
            }
        }.runTaskTimer(plugin, 0, 60 + new Random().nextInt(40)); // 每3-5秒（60-100tick）播放一次
        
        randomSoundTasks.put(uuid, task);
    }
    
    /**
     * 停止播放第三层随机音效
     */
    public void stopRandomSounds(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = randomSoundTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}

