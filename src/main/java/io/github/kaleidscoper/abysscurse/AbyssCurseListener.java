package io.github.kaleidscoper.abysscurse;

import io.github.kaleidscoper.abysscurse.curse.CurseManager;
import io.github.kaleidscoper.abysscurse.data.PlayerCurseData;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.mode.PluginMode;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AbyssCurse 事件监听器
 * 负责监听玩家相关事件并更新玩家数据
 */
public class AbyssCurseListener implements Listener {
    private final AbyssCursePlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final RegionManager regionManager;
    private final ModeManager modeManager;
    private final CurseManager curseManager;
    
    // 存储每个玩家的定时检查任务（每20tick检查一次Y坐标变化）
    private final Map<UUID, BukkitTask> playerCheckTasks = new HashMap<>();

    public AbyssCurseListener(AbyssCursePlugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.regionManager = plugin.getRegionManager();
        this.modeManager = plugin.getModeManager();
        this.curseManager = plugin.getCurseManager();
    }

    /**
     * 玩家加入事件
     * 加载玩家数据并初始化安全高度
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 获取或创建玩家数据
        PlayerCurseData data = playerDataManager.getData(player);
        
        // 重要：将玩家进入游戏时的 Y 坐标存储为初始安全高度
        // 如果数据是新创建的，safeHeight 已经是当前 Y 坐标
        // 如果数据是从文件加载的，保持原有的 safeHeight
        // 但如果是第一次进入游戏，需要设置为当前 Y 坐标
        if (data.getSafeHeight() == 0.0 && player.getLocation().getY() != 0.0) {
            data.setSafeHeight(player.getLocation().getY());
        }
        
        // 初始化 lastY 为当前 Y 坐标
        data.setLastY(player.getLocation().getY());
        
        // 清空累计上升记录（玩家重新进入游戏时从新的安全高度开始）
        data.clearRiseRecords();
        
        // 发送欢迎消息
        player.sendMessage("§8[§5AbyssCurse§8] §7欢迎来到深渊，探窟家" + player.getName() + "！");
        
        // 启动定时检查任务（每20tick检查一次Y坐标变化）
        startPlayerCheckTask(player);
    }

    /**
     * 玩家退出事件
     * 保存玩家数据
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 停止定时检查任务
        BukkitTask task = playerCheckTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        
        // 清理滤镜管理器中的玩家数据
        if (plugin.getFilterManager() != null) {
            plugin.getFilterManager().cleanupPlayer(uuid);
        }
        
        // 保存玩家数据
        playerDataManager.savePlayerData(player);
        
        // 从缓存中移除（可选，为了节省内存）
        // playerDataManager.removePlayerData(uuid);
    }

    /**
     * 玩家死亡事件
     * 清空累计上升记录并重置安全高度
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null) {
            return;
        }
        
        // 清空累计上升记录
        data.clearRiseRecords();
        
        // 重置安全高度为死亡位置的高度
        Location deathLocation = player.getLocation();
        data.setSafeHeight(deathLocation.getY());
        data.setLastY(deathLocation.getY());
        
        // 清除诅咒效果
        if (plugin.getEffectManager() != null) {
            plugin.getEffectManager().removeCurseEffects(player);
        }
        
        // 清除第一层滤镜
        if (plugin.getFilterManager() != null) {
            plugin.getFilterManager().setCurseFilter(player, false);
        }
        
        // 停止诅咒检查任务（如果存在）
        curseManager.stopCurseCheck(player);
        
        plugin.getLogger().info("玩家 " + player.getName() + " 死亡，已清空累计上升记录并重置安全高度");
    }

    /**
     * 玩家复活事件
     * 初始化复活后的数据
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerCurseData data = playerDataManager.getData(player);
        if (data == null) {
            return;
        }
        
        // 获取复活位置
        Location respawnLocation = event.getRespawnLocation();
        double respawnY = respawnLocation.getY();
        
        // 重置安全高度和 lastY 为复活位置的高度
        data.setSafeHeight(respawnY);
        data.setLastY(respawnY);
        
        // 确保累计上升记录已清空（死亡时应该已清空，但这里再次确保）
        data.clearRiseRecords();
        
        plugin.getLogger().info("玩家 " + player.getName() + " 复活，已重置安全高度为: " + respawnY);
    }

    /**
     * 玩家移动事件
     * 注意：这个事件触发频率很高，主要用于检测玩家进入/离开 Abyss 区域
     * 实际的 Y 坐标检查在定时任务中进行（每20tick一次）
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 只检查位置是否改变（忽略旋转）
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) {
            return;
        }
        
        // 如果只是旋转，不处理
        if (from.getBlockX() == to.getBlockX() && 
            from.getBlockY() == to.getBlockY() && 
            from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // 检查玩家是否进入/离开 Abyss 区域（用于提示）
        // 注意：这里只是提示，实际的诅咒逻辑在定时任务中处理
        boolean wasInAbyss = regionManager.isInAbyss(from);
        boolean isInAbyss = regionManager.isInAbyss(to);
        
        if (!wasInAbyss && isInAbyss) {
            // 进入 Abyss 区域
            player.sendMessage("§8[§5AbyssCurse§8] §c你进入了深渊区域...");
        } else if (wasInAbyss && !isInAbyss) {
            // 离开 Abyss 区域
            player.sendMessage("§8[§5AbyssCurse§8] §a你离开了深渊区域");
        }
    }

    /**
     * 启动玩家的定时检查任务
     * 每20tick（1秒）检查一次玩家的Y坐标变化
     */
    private void startPlayerCheckTask(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 如果已有任务，先取消
        BukkitTask existingTask = playerCheckTasks.get(uuid);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
        
        // 创建新任务
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // 检查玩家是否在线
                if (!player.isOnline()) {
                    this.cancel();
                    playerCheckTasks.remove(uuid);
                    return;
                }
                
                // 如果插件处于 OFF 模式，不处理
                if (modeManager.getCurrentMode() == PluginMode.OFF) {
                    return;
                }
                
                // 获取玩家数据
                PlayerCurseData data = playerDataManager.getData(player);
                if (data == null) {
                    return;
                }
                
                Location location = player.getLocation();
                double currentY = location.getY();
                double lastY = data.getLastY();
                
                // 检查玩家是否受诅咒影响
                if (!regionManager.isAffectedByCurse(location, uuid)) {
                    // 不受诅咒影响，只更新 lastY，不处理上升/下降
                    data.setLastY(currentY);
                    return;
                }
                
                // 处理 Y 坐标变化
                if (currentY > lastY) {
                    // 上升：记录真实浮点增量，避免因四舍五入误判
                    double riseDelta = currentY - lastY;
                    
                    // 忽略极小抖动
                    if (riseDelta >= 0.01) {
                        data.addRiseDelta(riseDelta);
                        
                        // 获取当前累计上升高度（自动清理过期记录）
                        double totalRise = data.getTotalRise();
                        
                        // 检查是否达到触发诅咒的阈值（2m）
                        if (totalRise >= 2.0) {
                            // 触发诅咒
                            double safeHeight = data.getSafeHeight();
                            curseManager.triggerCurse(player, safeHeight);
                            
                            // 清空累计上升记录
                            data.clearRiseRecords();
                            // 累计上升高度清零时刷新安全高度（将当前高度设为新的安全高度）
                            data.setSafeHeight(currentY);
                        }
                    }
                } else if (currentY < lastY) {
                    // 下降：减少累计上升高度，刷新安全高度
                    double descendDelta = lastY - currentY;
                    if (descendDelta >= 0.01) {
                        data.consumeRiseDelta(descendDelta);
                    }
                    data.setSafeHeight(currentY);
                }
                
                // 累计上升高度为零时，将当前高度设为新的安全高度
                double totalRise = data.getTotalRise();
                if (totalRise <= 1e-6 && data.getSafeHeight() != currentY) {
                    data.setSafeHeight(currentY);
                }
                
                // 更新 lastY
                data.setLastY(currentY);
            }
        }.runTaskTimer(plugin, 0, 10); // 每10tick（0.5秒）执行一次
        
        playerCheckTasks.put(uuid, task);
    }
}
