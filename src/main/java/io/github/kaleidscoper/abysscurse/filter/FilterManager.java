package io.github.kaleidscoper.abysscurse.filter;

import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 滤镜管理器
 * 负责管理红色滤镜效果（使用 ActionBar 显示）
 */
public class FilterManager {
    private final JavaPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    // 存储玩家是否处于第一层诅咒状态
    private final Map<UUID, Boolean> curseFilterState = new HashMap<>();
    
    // 定期更新任务
    private BukkitTask updateTask;
    
    private static final String FULL_WIDTH_CHAR = "█"; // 全角方块字符
    private static final int ACTIONBAR_WIDTH = 50; // ActionBar 宽度（大约50个字符）
    
    public FilterManager(JavaPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        startUpdateTask();
    }
    
    /**
     * 更新滤镜（根据累计上升高度）
     */
    public void updateFilter(Player player, int totalRise) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        boolean isCurseFilter = curseFilterState.getOrDefault(uuid, false);
        
        // 第一层诅咒状态：强制显示重度滤镜
        if (isCurseFilter) {
            updateFilterWithColor(player, 2); // 使用累计上升高度2的滤镜
            return;
        }
        
        // 正常状态：根据累计上升高度显示
        if (totalRise == 0) {
            // 不显示滤镜
            player.sendActionBar(Component.empty());
            return;
        }
        
        updateFilterWithColor(player, totalRise);
    }
    
    /**
     * 设置第一层诅咒滤镜状态
     */
    public void setCurseFilter(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            curseFilterState.put(uuid, true);
            updateFilterWithColor(player, 2); // 显示重度滤镜
        } else {
            curseFilterState.remove(uuid);
            // 恢复正常的累计上升高度滤镜
            int totalRise = playerDataManager.getData(player).getTotalRise();
            updateFilter(player, totalRise);
        }
    }
    
    /**
     * 内部方法：根据强度更新滤镜
     */
    private void updateFilterWithColor(Player player, int intensity) {
        TextColor filterColor = getFilterColor(intensity);
        
        // 创建全屏宽度的红色文本
        Component filterText = Component.text()
            .content(FULL_WIDTH_CHAR.repeat(ACTIONBAR_WIDTH))
            .color(filterColor)
            .build();
        
        player.sendActionBar(filterText);
    }
    
    /**
     * 获取滤镜颜色
     */
    private TextColor getFilterColor(int totalRise) {
        if (totalRise == 1) {
            // 轻微红色：RGB(255, 100, 100)
            return TextColor.color(255, 100, 100);
        } else if (totalRise >= 2) {
            // 重度红色：RGB(200, 0, 0)
            return TextColor.color(200, 0, 0);
        }
        return TextColor.color(0, 0, 0); // 黑色（不显示）
    }
    
    /**
     * 启动定期更新任务
     */
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    int totalRise = playerDataManager.getData(player).getTotalRise();
                    updateFilter(player, totalRise);
                }
            }
        }.runTaskTimer(plugin, 0, 5); // 每5tick（0.25秒）更新一次
    }
    
    /**
     * 停止滤镜管理器
     */
    public void stop() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
    }
}

