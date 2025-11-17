package io.github.kaleidscoper.abysscurse.achievement;

import io.github.kaleidscoper.abysscurse.AbyssCursePlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * 成就管理器
 * 负责注册和管理"深渊"成就树
 */
@SuppressWarnings("deprecation")
public class AchievementManager {

    private final AbyssCursePlugin plugin;
    private final Set<NamespacedKey> registeredAdvancements = new HashSet<>();

    public AchievementManager(AbyssCursePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化成就系统
     * 通过 Bukkit 的动态注册接口载入 JSON 定义，避免玩家手动 reload
     */
    public void initialize() {
        plugin.getLogger().info("正在初始化成就系统（使用动态注册 API）...");

        for (Achievement achievement : Achievement.values()) {
            registerAdvancement(achievement);
        }

        plugin.getLogger().info("成就系统初始化完成，共动态注册 " + registeredAdvancements.size() + " 个成就");
    }

    /**
     * 插件卸载时撤销动态注册的成就，保持世界整洁
     */
    public void shutdown() {
        UnsafeValues unsafe = Bukkit.getUnsafe();
        int removed = 0;
        for (NamespacedKey key : registeredAdvancements) {
            try {
                unsafe.removeAdvancement(key);
                removed++;
            } catch (Exception e) {
                plugin.getLogger().warning("撤销成就失败: " + key + " -> " + e.getMessage());
            }
        }
        registeredAdvancements.clear();
        if (removed > 0) {
            plugin.getLogger().info("已撤销 " + removed + " 个动态注册的成就");
        }
    }

    /**
     * 注册单个成就
     *
     * @param achievement 成就枚举
     */
    private void registerAdvancement(Achievement achievement) {
        NamespacedKey key = achievement.getKey(plugin);

        Advancement existing = Bukkit.getAdvancement(key);
        if (existing != null) {
            plugin.getLogger().fine("成就已存在: " + key);
            return;
        }

        String json = loadAdvancementJson(achievement);
        if (json == null || json.isEmpty()) {
            plugin.getLogger().warning("无法读取成就定义: " + key);
            return;
        }

        try {
            Bukkit.getUnsafe().loadAdvancement(key, json);
            registeredAdvancements.add(key);
            plugin.getLogger().info("已动态注册成就: " + achievement.getName() + " (" + key + ")");
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("动态注册成就失败: " + key + " -> " + e.getMessage());
        }
    }

    private String loadAdvancementJson(Achievement achievement) {
        String resourcePath = "data/abysscurse/advancements/abyss/" + achievement.getId() + ".json";
        try (InputStream stream = plugin.getResource(resourcePath)) {
            if (stream == null) {
                plugin.getLogger().warning("找不到资源文件: " + resourcePath);
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            stream.transferTo(buffer);
            return buffer.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("读取资源失败: " + resourcePath + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * 授予玩家成就
     *
     * @param player      玩家
     * @param achievement 成就
     */
    public void grantAchievement(Player player, Achievement achievement) {
        NamespacedKey key = achievement.getKey(plugin);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            plugin.getLogger().warning("成就不存在: " + key + "，无法授予玩家 " + player.getName());
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) {
            for (String criteria : progress.getRemainingCriteria()) {
                progress.awardCriteria(criteria);
            }

            plugin.getLogger().info("已授予玩家 " + player.getName() + " 成就: " + achievement.getName());
        } else {
            plugin.getLogger().fine("玩家 " + player.getName() + " 已拥有成就: " + achievement.getName());
        }
    }

    /**
     * 检查玩家是否已获得成就
     *
     * @param player      玩家
     * @param achievement 成就
     * @return 是否已获得
     */
    public boolean hasAchievement(Player player, Achievement achievement) {
        NamespacedKey key = achievement.getKey(plugin);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            return false;
        }

        return player.getAdvancementProgress(advancement).isDone();
    }

    /**
     * 撤销玩家的成就
     *
     * @param player      玩家
     * @param achievement 成就
     */
    public void revokeAchievement(Player player, Achievement achievement) {
        NamespacedKey key = achievement.getKey(plugin);
        Advancement advancement = Bukkit.getAdvancement(key);

        if (advancement == null) {
            plugin.getLogger().warning("成就不存在: " + key + "，无法撤销玩家 " + player.getName() + " 的成就");
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) {
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }

            plugin.getLogger().fine("已撤销玩家 " + player.getName() + " 的成就: " + achievement.getName());
        }
    }
}

