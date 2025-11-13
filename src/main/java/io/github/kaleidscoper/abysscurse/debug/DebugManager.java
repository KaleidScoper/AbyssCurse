package io.github.kaleidscoper.abysscurse.debug;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.data.PlayerCurseData;
import io.github.kaleidscoper.abysscurse.data.PlayerDataManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.region.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 调试管理器
 * 负责管理调试模式并输出调试信息
 */
public class DebugManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ModeManager modeManager;
    private final RegionManager regionManager;
    private final PlayerDataManager playerDataManager;
    
    // 玩家级别的调试开关
    private final Set<UUID> debugPlayers = new HashSet<>();
    
    // 定时任务
    private BukkitTask debugTask;
    
    public DebugManager(JavaPlugin plugin, ConfigManager configManager, ModeManager modeManager,
                       RegionManager regionManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.modeManager = modeManager;
        this.regionManager = regionManager;
        this.playerDataManager = playerDataManager;
    }
    
    /**
     * 启动调试管理器
     */
    public void start() {
        // 如果已经有任务在运行，先取消
        if (debugTask != null && !debugTask.isCancelled()) {
            debugTask.cancel();
        }
        
        // 每 20 tick (1秒) 更新一次调试信息
        debugTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (shouldShowDebug(player)) {
                    updateDebugInfo(player);
                }
            }
        }, 0, 20);
        
        plugin.getLogger().info("调试管理器已启动");
    }
    
    /**
     * 停止调试管理器
     */
    public void stop() {
        if (debugTask != null && !debugTask.isCancelled()) {
            debugTask.cancel();
            debugTask = null;
        }
        
        // 清除所有玩家的调试信息显示
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendActionBar(Component.empty());
        }
        
        plugin.getLogger().info("调试管理器已停止");
    }
    
    /**
     * 判断是否应该显示调试信息
     */
    private boolean shouldShowDebug(Player player) {
        // 全局调试开关
        if (configManager.isDebugEnabled()) {
            return true;
        }
        
        // 玩家级别调试开关
        return debugPlayers.contains(player.getUniqueId());
    }
    
    /**
     * 更新玩家的调试信息显示
     */
    private void updateDebugInfo(Player player) {
        Component debugInfo = buildDebugInfo(player);
        player.sendActionBar(debugInfo);
    }
    
    /**
     * 构建调试信息组件
     */
    private Component buildDebugInfo(Player player) {
        TextComponent.Builder builder = Component.text();
        
        // 模式信息
        builder.append(Component.text("模式: ", NamedTextColor.GRAY));
        builder.append(Component.text(modeManager.getCurrentMode().name(), NamedTextColor.YELLOW));
        builder.append(Component.text(" | ", NamedTextColor.GRAY));
        
        // 位置信息
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        builder.append(Component.text("位置: ", NamedTextColor.GRAY));
        builder.append(Component.text(String.format("(%d, %d, %d)", x, y, z), NamedTextColor.AQUA));
        
        // 如果是在 ABYSS 模式，显示区域信息
        if (modeManager.isAbyss()) {
            builder.append(Component.text(" | ", NamedTextColor.GRAY));
            builder.append(Component.text("Abyss中心: ", NamedTextColor.GRAY));
            builder.append(Component.text(
                String.format("(%d, %d, %d)", 
                    configManager.getAbyssCenterX(),
                    configManager.getAbyssCenterY(),
                    configManager.getAbyssCenterZ()),
                NamedTextColor.GREEN));
            builder.append(Component.text(" 半径: ", NamedTextColor.GRAY));
            builder.append(Component.text(
                String.valueOf(configManager.getAbyssRadius()),
                NamedTextColor.GREEN));
        }
        
        // 获取玩家数据
        PlayerCurseData data = playerDataManager.getData(player);
        if (data != null) {
            builder.append(Component.text(" | ", NamedTextColor.GRAY));
            
            // 累计上升高度
            int totalRise = data.getTotalRise();
            builder.append(Component.text("上升: ", NamedTextColor.GRAY));
            builder.append(Component.text(String.valueOf(totalRise), 
                totalRise >= 2 ? NamedTextColor.RED : NamedTextColor.YELLOW));
            
            // 安全高度
            builder.append(Component.text(" | ", NamedTextColor.GRAY));
            builder.append(Component.text("安全高度: ", NamedTextColor.GRAY));
            builder.append(Component.text(String.format("%.1f", data.getSafeHeight()), NamedTextColor.AQUA));
            
            // 当前层级
            int layer = data.getCurrentLayer();
            if (layer > 0) {
                builder.append(Component.text(" | ", NamedTextColor.GRAY));
                builder.append(Component.text("层级: ", NamedTextColor.GRAY));
                builder.append(Component.text(String.valueOf(layer), NamedTextColor.RED));
            }
        }
        
        // 区域信息
        if (regionManager != null) {
            builder.append(Component.text(" | ", NamedTextColor.GRAY));
            boolean inAbyss = regionManager.isInAbyss(player.getLocation());
            builder.append(Component.text("Abyss: ", NamedTextColor.GRAY));
            builder.append(Component.text(inAbyss ? "是" : "否", 
                inAbyss ? NamedTextColor.RED : NamedTextColor.GREEN));
            
            boolean inExemption = regionManager.isInExemptionZone(player.getLocation());
            if (inExemption) {
                builder.append(Component.text(" | ", NamedTextColor.GRAY));
                builder.append(Component.text("豁免区: ", NamedTextColor.GRAY));
                builder.append(Component.text("是", NamedTextColor.GREEN));
            }
            
            boolean isExempt = regionManager.isExemptPlayer(player.getUniqueId());
            if (isExempt) {
                builder.append(Component.text(" | ", NamedTextColor.GRAY));
                builder.append(Component.text("豁免者: ", NamedTextColor.GRAY));
                builder.append(Component.text("是", NamedTextColor.GOLD));
            }
        }
        
        return builder.build();
    }
    
    /**
     * 为玩家开启调试模式
     */
    public void enableDebug(Player player) {
        debugPlayers.add(player.getUniqueId());
        player.sendMessage("§8[§5AbyssCurse§8] §a调试模式已开启");
    }
    
    /**
     * 为玩家关闭调试模式
     */
    public void disableDebug(Player player) {
        debugPlayers.remove(player.getUniqueId());
        player.sendActionBar(Component.empty());
        player.sendMessage("§8[§5AbyssCurse§8] §c调试模式已关闭");
    }
    
    /**
     * 切换玩家的调试模式
     */
    public void toggleDebug(Player player) {
        if (debugPlayers.contains(player.getUniqueId())) {
            disableDebug(player);
        } else {
            enableDebug(player);
        }
    }
    
    /**
     * 检查玩家是否开启了调试模式
     */
    public boolean isPlayerDebugEnabled(Player player) {
        return debugPlayers.contains(player.getUniqueId());
    }
    
    /**
     * 检查全局调试模式是否开启
     */
    public boolean isGlobalDebugEnabled() {
        return configManager.isDebugEnabled();
    }
    
    /**
     * 设置全局调试模式
     */
    public void setGlobalDebug(boolean enabled) {
        configManager.setDebugEnabled(enabled);
        if (enabled) {
            plugin.getLogger().info("全局调试模式已开启");
        } else {
            plugin.getLogger().info("全局调试模式已关闭");
            // 清除所有玩家的 ActionBar 显示
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!debugPlayers.contains(player.getUniqueId())) {
                    player.sendActionBar(Component.empty());
                }
            }
        }
    }
    
    /**
     * 获取调试信息文本（用于命令输出）
     */
    public String getDebugInfoText(Player player) {
        StringBuilder info = new StringBuilder();
        info.append("§8[§5AbyssCurse§8] §7========== 调试信息 ==========\n");
        info.append("§7模式: §e").append(modeManager.getCurrentMode().name()).append("\n");
        info.append("§7位置: §e(")
            .append(player.getLocation().getBlockX()).append(", ")
            .append(player.getLocation().getBlockY()).append(", ")
            .append(player.getLocation().getBlockZ()).append(")\n");
        
        if (modeManager.isAbyss()) {
            info.append("§7Abyss中心: §e(")
                .append(configManager.getAbyssCenterX()).append(", ")
                .append(configManager.getAbyssCenterY()).append(", ")
                .append(configManager.getAbyssCenterZ()).append(")\n");
            info.append("§7Abyss半径: §e").append(configManager.getAbyssRadius()).append(" 区块\n");
        }
        
        info.append("§7全局调试: §e").append(configManager.isDebugEnabled() ? "开启" : "关闭").append("\n");
        info.append("§7玩家调试: §e").append(isPlayerDebugEnabled(player) ? "开启" : "关闭").append("\n");
        
        // 玩家数据信息
        PlayerCurseData data = playerDataManager.getData(player);
        if (data != null) {
            info.append("§7当前层级: §e").append(data.getCurrentLayer() > 0 ? String.valueOf(data.getCurrentLayer()) : "无").append("\n");
            info.append("§7累计上升高度: §e").append(data.getTotalRise()).append(" 格\n");
            info.append("§7安全高度: §e").append(String.format("%.2f", data.getSafeHeight())).append("\n");
            info.append("§7是否为生骸: §e").append(data.isNarehate() ? "是" : "否").append("\n");
            if (data.isNarehate() && data.getNarehateType() != null) {
                info.append("§7生骸类型: §e").append(data.getNarehateType().name()).append("\n");
            }
        }
        
        // 区域信息
        if (regionManager != null) {
            info.append("§7是否在Abyss内: §e").append(regionManager.isInAbyss(player.getLocation()) ? "是" : "否").append("\n");
            info.append("§7是否在豁免区: §e").append(regionManager.isInExemptionZone(player.getLocation()) ? "是" : "否").append("\n");
            info.append("§7是否为豁免者: §e").append(regionManager.isExemptPlayer(player.getUniqueId()) ? "是" : "否").append("\n");
            
            int armOfCurse = regionManager.getArmOfCurse(player.getLocation());
            if (armOfCurse != Integer.MAX_VALUE) {
                info.append("§7诅咒臂: §e").append(armOfCurse).append(" 区块\n");
            }
        }
        
        info.append("§7==============================");
        return info.toString();
    }
}

