package io.github.kaleidscoper.abysscurse.filter;

import io.github.kaleidscoper.abysscurse.data.PlayerCurseData;
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
    
    // 存储每个玩家上次显示的累计上升高度（用于减少不必要的更新）
    private final Map<UUID, Integer> lastDisplayedRise = new HashMap<>();
    
    // 定期更新任务
    private BukkitTask updateTask;
    
    public FilterManager(JavaPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        startUpdateTask();
    }
    
    /**
     * 更新滤镜（根据累计上升高度）
     */
    public void updateFilter(Player player, double totalRise) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        boolean isCurseFilter = curseFilterState.getOrDefault(uuid, false);
        int intensity = toIntensity(totalRise);
        
        // 第一层诅咒状态：强制显示"累计上升高度2"的文字
        if (isCurseFilter) {
            updateFilterWithColor(player, 2); // 使用累计上升高度2的滤镜和文字
            return;
        }
        
        // 正常状态：根据累计上升高度显示
        updateFilterWithColor(player, intensity);
    }
    
    /**
     * 设置第一层诅咒滤镜状态
     */
    public void setCurseFilter(Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();
        if (enabled) {
            curseFilterState.put(uuid, true);
            updateFilterWithColor(player, 2); // 显示重度滤镜
            lastDisplayedRise.put(uuid, 2); // 更新记录
        } else {
            curseFilterState.remove(uuid);
            // 恢复正常的累计上升高度滤镜
            double totalRise = playerDataManager.getData(player).getTotalRise();
            updateFilter(player, totalRise);
            lastDisplayedRise.put(uuid, toIntensity(totalRise)); // 更新记录
        }
    }
    
    /**
     * 内部方法：根据强度更新滤镜
     */
    private void updateFilterWithColor(Player player, int intensity) {
        TextColor filterColor = getFilterColor(intensity);
        
        // 第一层诅咒状态：显示"累计上升高度2"的文字
        boolean isCurseFilter = curseFilterState.getOrDefault(player.getUniqueId(), false);
        if (isCurseFilter) {
            // 显示文字：累计上升高度2
            Component filterText = Component.text()
                .content("累计上升高度2")
                .color(filterColor)
                .build();
            player.sendActionBar(filterText);
            return;
        }
        
        // 正常状态：根据累计上升高度显示文字
        if (intensity <= 0) {
            Component filterText = Component.text()
                .content("累计上升高度0")
                .color(filterColor)
                .build();
            player.sendActionBar(filterText);
        } else if (intensity == 1) {
            Component filterText = Component.text()
                .content("累计上升高度1")
                .color(filterColor)
                .build();
            player.sendActionBar(filterText);
        } else if (intensity >= 2) {
            Component filterText = Component.text()
                .content("累计上升高度2")
                .color(filterColor)
                .build();
            player.sendActionBar(filterText);
        } else {
            // 不显示滤镜
            player.sendActionBar(Component.empty());
        }
    }
    
    /**
     * 获取滤镜颜色
     */
    private TextColor getFilterColor(int totalRise) {
        if (totalRise <= 0) {
            // 灰色：表示已清空
            return TextColor.color(160, 160, 160);
        }
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
     * 降低更新频率，避免覆盖原生 ActionBar 文本
     */
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!player.isOnline()) {
                        continue;
                    }
                    
                    PlayerCurseData data = playerDataManager.getData(player);
                    if (data == null) {
                        continue;
                    }
                    
                    double totalRise = data.getTotalRise();
                    int intensity = toIntensity(totalRise);
                    UUID uuid = player.getUniqueId();
                    
                    // 只在累计上升高度变化或处于诅咒状态时才更新
                    // 这样可以减少对原生 ActionBar 文本的干扰
                    Integer lastRise = lastDisplayedRise.get(uuid);
                    boolean isCurseFilter = curseFilterState.getOrDefault(uuid, false);
                    
                    // 如果累计上升高度变化了，或者处于诅咒状态，才更新
                    if (lastRise == null || lastRise != intensity || isCurseFilter) {
                        updateFilter(player, totalRise);
                        lastDisplayedRise.put(uuid, intensity);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20); // 改为每20tick（1秒）更新一次，降低频率
    }
    
    /**
     * 清理玩家数据（玩家退出时调用）
     */
    public void cleanupPlayer(UUID uuid) {
        curseFilterState.remove(uuid);
        lastDisplayedRise.remove(uuid);
    }
    
    /**
     * 停止滤镜管理器
     */
    public void stop() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
    }

    /**
     * 将累计上升高度转换为滤镜显示强度（0/1/2）
     */
    private int toIntensity(double totalRise) {
        if (totalRise >= 2.0) {
            return 2;
        }
        if (totalRise >= 1.0) {
            return 1;
        }
        return 0;
    }
}

