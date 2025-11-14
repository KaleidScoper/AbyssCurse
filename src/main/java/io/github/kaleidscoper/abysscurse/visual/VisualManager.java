package io.github.kaleidscoper.abysscurse.visual;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 视觉管理器
 * 负责管理 Title/Subtitle 层级提示和 BossBar
 */
public class VisualManager {
    private final JavaPlugin plugin;
    
    // 存储玩家当前层级（用于检测层级变化）
    private final Map<UUID, Integer> playerLayers = new HashMap<>();
    
    // 存储玩家的 BossBar（用于显示当前层级）
    private final Map<UUID, org.bukkit.boss.BossBar> playerBossBars = new HashMap<>();
    
    public VisualManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 更新玩家层级显示
     * 当玩家进入新层级时显示 Title，并更新 BossBar
     * 注意：只在主世界（NORMAL 环境）显示
     */
    public void updateLayerDisplay(Player player, int layer) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // 只在主世界显示层级标题
        World world = player.getWorld();
        if (world == null || world.getEnvironment() != World.Environment.NORMAL) {
            // 如果不在主世界，清除显示
            clearLayerDisplay(player);
            return;
        }
        
        UUID uuid = player.getUniqueId();
        Integer previousLayer = playerLayers.get(uuid);
        
        // 如果层级变化，显示 Title
        if (previousLayer == null || previousLayer != layer) {
            showLayerTitle(player, layer);
            playerLayers.put(uuid, layer);
        }
        
        // 更新 BossBar
        updateBossBar(player, layer);
    }
    
    /**
     * 显示层级 Title
     */
    private void showLayerTitle(Player player, int layer) {
        Component title;
        Component subtitle;
        NamedTextColor titleColor;
        
        switch (layer) {
            case 1: // 第一层：阿比斯之渊
                title = Component.text("阿比斯之渊");
                subtitle = Component.text("Edge of the Abyss");
                titleColor = NamedTextColor.RED;
                break;
            case 2: // 第二层：诱惑之森
                title = Component.text("诱惑之森");
                subtitle = Component.text("Forest of Temptation");
                titleColor = NamedTextColor.GREEN;
                break;
            case 3: // 第三层：大断层
                title = Component.text("大断层");
                subtitle = Component.text("Great Fault");
                titleColor = NamedTextColor.GRAY;
                break;
            case 4: // 第四层：巨人之杯
                title = Component.text("巨人之杯");
                subtitle = Component.text("Goblets of Giants");
                titleColor = NamedTextColor.AQUA; // 天青色（使用 AQUA 作为近似）
                break;
            case 5: // 第五层：亡骸之海
                title = Component.text("亡骸之海");
                subtitle = Component.text("Sea of Corpses");
                titleColor = NamedTextColor.BLUE;
                break;
            case 6: // 第六层：来无还之都
                title = Component.text("来无还之都");
                subtitle = Component.text("Capital of the Unreturned");
                titleColor = NamedTextColor.GOLD;
                break;
            case 7: // 第七层：最终极之涡
                title = Component.text("最终极之涡");
                subtitle = Component.text("Final Maelstrom");
                titleColor = NamedTextColor.WHITE;
                break;
            default:
                return; // 不在任何层级
        }
        
        Title titleObj = Title.title(
            title.color(titleColor),
            subtitle,
            Title.Times.times(
                Duration.ofMillis(500),  // 淡入时间
                Duration.ofSeconds(3),   // 显示时间
                Duration.ofMillis(500)   // 淡出时间
            )
        );
        
        player.showTitle(titleObj);
    }
    
    /**
     * 更新 BossBar
     */
    private void updateBossBar(Player player, int layer) {
        if (layer == 0) {
            // 移除 BossBar
            removeBossBar(player);
            return;
        }
        
        UUID uuid = player.getUniqueId();
        org.bukkit.boss.BossBar bossBar = playerBossBars.get(uuid);
        
        if (bossBar == null) {
            // 创建新的 BossBar
            bossBar = plugin.getServer().createBossBar(
                getLayerName(layer),
                getBossBarColor(layer),
                org.bukkit.boss.BarStyle.SOLID
            );
            bossBar.addPlayer(player);
            playerBossBars.put(uuid, bossBar);
        } else {
            // 更新现有 BossBar
            bossBar.setTitle(getLayerName(layer));
            bossBar.setColor(getBossBarColor(layer));
        }
        
        // 设置进度（根据层级，1-7层对应0.14-1.0）
        bossBar.setProgress((double)layer / 7.0);
    }
    
    /**
     * 获取层级名称
     */
    private String getLayerName(int layer) {
        switch (layer) {
            case 1: return "第一层：阿比斯之渊";
            case 2: return "第二层：诱惑之森";
            case 3: return "第三层：大断层";
            case 4: return "第四层：巨人之杯";
            case 5: return "第五层：亡骸之海";
            case 6: return "第六层：来无还之都";
            case 7: return "第七层：最终极之涡";
            default: return "";
        }
    }
    
    /**
     * 获取 BossBar 颜色
     */
    private org.bukkit.boss.BarColor getBossBarColor(int layer) {
        switch (layer) {
            case 1: return org.bukkit.boss.BarColor.RED;
            case 2: return org.bukkit.boss.BarColor.GREEN;
            case 3: return org.bukkit.boss.BarColor.WHITE;
            case 4: return org.bukkit.boss.BarColor.BLUE;
            case 5: return org.bukkit.boss.BarColor.BLUE;
            case 6: return org.bukkit.boss.BarColor.YELLOW;
            case 7: return org.bukkit.boss.BarColor.WHITE;
            default: return org.bukkit.boss.BarColor.PURPLE;
        }
    }
    
    /**
     * 移除 BossBar
     */
    private void removeBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        org.bukkit.boss.BossBar bossBar = playerBossBars.remove(uuid);
        if (bossBar != null) {
            bossBar.removePlayer(player);
            bossBar.removeAll();
        }
    }
    
    /**
     * 清除玩家层级显示
     */
    public void clearLayerDisplay(Player player) {
        if (player == null) {
            return;
        }
        
        UUID uuid = player.getUniqueId();
        playerLayers.remove(uuid);
        removeBossBar(player);
    }
    
    /**
     * 停止视觉管理器
     */
    public void stop() {
        // 清除所有 BossBar
        for (org.bukkit.boss.BossBar bossBar : playerBossBars.values()) {
            bossBar.removeAll();
        }
        playerBossBars.clear();
        playerLayers.clear();
    }
}

