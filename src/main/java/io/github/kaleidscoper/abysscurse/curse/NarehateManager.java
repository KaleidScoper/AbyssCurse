package io.github.kaleidscoper.abysscurse.curse;

import io.github.kaleidscoper.abysscurse.AbyssCursePlugin;
import io.github.kaleidscoper.abysscurse.data.PlayerCurseData;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

/**
 * 生骸管理器
 * 负责管理玩家转换为生骸的逻辑
 */
public class NarehateManager {
    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final RegionManager regionManager;
    private final Random random;
    
    // 定期检查任务
    private BukkitTask checkTask;
    
    // 最大概率（50%）
    private static final double MAX_PROBABILITY = 0.5;
    
    // 达到最大概率的时间（10分钟 = 600秒 = 600000毫秒）
    private static final long MAX_PROBABILITY_TIME = 10 * 60 * 1000;
    
    // 检查间隔（1分钟 = 60秒 = 1200 tick）
    private static final long CHECK_INTERVAL = 60 * 20;
    
    public NarehateManager(JavaPlugin plugin, PlayerDataManager playerDataManager, RegionManager regionManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.regionManager = regionManager;
        this.random = new Random();
        startCheckTask();
    }
    
    /**
     * 启动定期检查任务
     * 每分钟检查一次玩家是否应该转换为生骸
     */
    private void startCheckTask() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.isOnline() || player.isDead()) {
                        continue;
                    }
                    
                    checkNarehateTransformation(player);
                }
            }
        }.runTaskTimer(plugin, CHECK_INTERVAL, CHECK_INTERVAL); // 每分钟执行一次
    }
    
    /**
     * 检查玩家是否应该转换为生骸
     */
    private void checkNarehateTransformation(Player player) {
        // 如果玩家已经是生骸，跳过
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null || data.isNarehate()) {
            return;
        }
        
        // 检查玩家是否有凋零效果（说明是第六层及以下的诅咒）
        if (!player.hasPotionEffect(PotionEffectType.WITHER)) {
            // 没有凋零效果，重置暴露时间
            if (data.getDeepCurseExposureStartTime() > 0) {
                data.setDeepCurseExposureStartTime(0);
            }
            return;
        }
        
        // 检查玩家是否在abyss范围内
        if (!regionManager.isInAbyss(player.getLocation())) {
            // 不在abyss范围内，重置暴露时间
            if (data.getDeepCurseExposureStartTime() > 0) {
                data.setDeepCurseExposureStartTime(0);
            }
            return;
        }
        
        // 玩家有凋零效果且在abyss范围内，开始或继续计时
        long now = System.currentTimeMillis();
        long exposureStartTime = data.getDeepCurseExposureStartTime();
        
        if (exposureStartTime == 0) {
            // 第一次检测到暴露，记录开始时间
            data.setDeepCurseExposureStartTime(now);
            return;
        }
        
        // 计算暴露时间（毫秒）
        long exposureTime = now - exposureStartTime;
        
        // 计算转换概率
        double probability = calculateTransformationProbability(exposureTime);
        
        // 根据概率决定是否转换
        if (random.nextDouble() < probability) {
            convertToNarehate(player);
        }
    }
    
    /**
     * 计算转换为生骸的概率
     * 使用指数型增长，10分钟时达到50%，之后保持50%
     * 
     * 公式：P(t) = 0.5 * (1 - e^(-k * t / T))
     * 其中 k = 6，T = MAX_PROBABILITY_TIME (10分钟)
     * 在 t = T 时，P ≈ 0.4987，非常接近0.5
     * 
     * @param exposureTime 暴露时间（毫秒）
     * @return 转换概率（0.0 - 0.5）
     */
    private double calculateTransformationProbability(long exposureTime) {
        if (exposureTime >= MAX_PROBABILITY_TIME) {
            // 超过10分钟，保持最大概率50%
            return MAX_PROBABILITY;
        }
        
        // 指数型增长函数
        // 使用 k = 6 使得在10分钟时概率非常接近0.5
        double normalizedTime = (double) exposureTime / MAX_PROBABILITY_TIME;
        double probability = MAX_PROBABILITY * (1 - Math.exp(-6 * normalizedTime));
        
        return probability;
    }
    
    /**
     * 将玩家转换为生骸
     */
    private void convertToNarehate(Player player) {
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null) {
            return;
        }
        
        // 清除所有诅咒效果和诅咒数据
        clearAllCurses(player, data);
        
        // 随机选择生骸类型（50%概率为LUCKY，50%概率为SAD）
        PlayerCurseData.NarehateType type = random.nextBoolean() 
            ? PlayerCurseData.NarehateType.LUCKY 
            : PlayerCurseData.NarehateType.SAD;
        
        // 设置为生骸
        data.setNarehate(true);
        data.setNarehateType(type);
        
        // 重置暴露时间
        data.setDeepCurseExposureStartTime(0);
        
        // 将玩家添加到豁免者列表
        regionManager.addExemptPlayer(player.getUniqueId());
        
        // 播放不死图腾粒子效果
        playTotemEffect(player);
        
        // 发送消息给玩家
        String typeName = type == PlayerCurseData.NarehateType.LUCKY ? "幸运生骸" : "悲惨生骸";
        player.sendMessage("§8[§5AbyssCurse§8] §c你已转变为" + typeName + "！");
        
        plugin.getLogger().info("玩家 " + player.getName() + " 已转变为" + typeName);
    }
    
    /**
     * 清除玩家的所有诅咒效果和诅咒数据
     */
    private void clearAllCurses(Player player, PlayerCurseData data) {
        // 通过插件获取各个管理器
        if (plugin instanceof AbyssCursePlugin) {
            AbyssCursePlugin abyssPlugin = (AbyssCursePlugin) plugin;
            
            // 停止诅咒检查任务
            if (abyssPlugin.getCurseManager() != null) {
                abyssPlugin.getCurseManager().stopCurseCheck(player);
            }
            
            // 清除诅咒效果
            if (abyssPlugin.getEffectManager() != null) {
                abyssPlugin.getEffectManager().removeCurseEffects(player);
            }
            
            // 停止随机音效（第三层及以下都有随机音效，直接停止即可，如果不存在任务会安全处理）
            if (abyssPlugin.getSoundManager() != null) {
                abyssPlugin.getSoundManager().stopRandomSounds(player);
            }
            
            // 清除第一层诅咒滤镜（如果存在）
            if (abyssPlugin.getFilterManager() != null) {
                abyssPlugin.getFilterManager().setCurseFilter(player, false);
            }
        }
        
        // 清除诅咒数据
        data.setCurrentLayer(0);
        data.setCurseStartTime(0);
        data.setCurseDuration(0);
        data.setCurseArm(0);
    }
    
    /**
     * 获取不死图腾粒子类型
     * 尝试多种可能的粒子名称以兼容不同版本
     */
    private Particle getTotemParticle() {
        // 尝试常见的粒子名称
        String[] particleNames = {"TOTEM", "TOTEM_OF_UNDYING"};
        
        for (String name : particleNames) {
            try {
                return Particle.valueOf(name);
            } catch (IllegalArgumentException e) {
                // 继续尝试下一个
            }
        }
        
        // 如果都不存在，返回 null（调用者需要处理）
        return null;
    }
    
    /**
     * 播放不死图腾粒子效果
     * 模拟不死图腾触发时的视觉效果（仅粒子，不显示手持模型）
     * 可以在玩家转变为生骸时调用，也可以在其他需要的地方调用
     */
    public void playTotemEffect(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        Location location = player.getLocation();
        
        // 播放不死图腾音效
        player.getWorld().playSound(
            location,
            Sound.ITEM_TOTEM_USE,
            1.0f,
            1.0f
        );
        
        // 获取不死图腾粒子类型
        Particle totemParticle = getTotemParticle();
        if (totemParticle == null) {
            // 如果无法获取 TOTEM 粒子，记录警告但不影响其他功能
            plugin.getLogger().warning("无法找到 TOTEM 粒子类型，跳过粒子效果");
            return;
        }
        
        // 生成不死图腾粒子效果
        // 在玩家位置周围生成多个粒子，模拟不死图腾触发效果
        for (int i = 0; i < 20; i++) {
            // 在玩家周围随机位置生成粒子
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            
            Location particleLocation = location.clone().add(offsetX, offsetY, offsetZ);
            
            player.getWorld().spawnParticle(
                totemParticle,
                particleLocation,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            );
        }
        
        // 在玩家头顶生成额外的粒子效果
        Location headLocation = location.clone().add(0, player.getHeight(), 0);
        player.getWorld().spawnParticle(
            totemParticle,
            headLocation,
            30,
            0.3,
            0.3,
            0.3,
            0.1
        );
    }
    
    /**
     * 停止生骸管理器
     */
    public void stop() {
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
        }
    }
}

