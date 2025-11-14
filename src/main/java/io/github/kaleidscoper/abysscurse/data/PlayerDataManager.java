package io.github.kaleidscoper.abysscurse.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 玩家数据管理器
 * 负责管理所有玩家的诅咒相关数据，包括数据持久化
 */
public class PlayerDataManager {
    private final JavaPlugin plugin;
    
    // 在线玩家数据缓存（线程安全）
    private final ConcurrentHashMap<UUID, PlayerCurseData> playerDataCache;
    
    // 玩家数据文件夹
    private final File playersFolder;

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerDataCache = new ConcurrentHashMap<>();
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        
        // 确保玩家数据文件夹存在
        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }
    }

    /**
     * 获取玩家数据（如果不存在则创建）
     * @param player 玩家
     * @return 玩家数据
     */
    public PlayerCurseData getData(Player player) {
        UUID uuid = player.getUniqueId();
        
        // 从缓存获取
        PlayerCurseData data = playerDataCache.get(uuid);
        if (data != null) {
            return data;
        }
        
        // 缓存中没有，尝试从文件加载
        data = loadPlayerData(uuid);
        if (data == null) {
            // 文件也没有，创建新数据（使用玩家当前 Y 坐标作为初始安全高度）
            data = new PlayerCurseData(player.getLocation().getY());
        }
        
        // 存入缓存
        playerDataCache.put(uuid, data);
        return data;
    }

    /**
     * 获取玩家数据（如果不存在则返回 null）
     * @param uuid 玩家 UUID
     * @return 玩家数据，如果不存在则返回 null
     */
    public PlayerCurseData getData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * 加载玩家数据（从文件）
     * @param uuid 玩家 UUID
     * @return 玩家数据，如果文件不存在或加载失败则返回 null
     */
    private PlayerCurseData loadPlayerData(UUID uuid) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        if (!playerFile.exists()) {
            return null;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            
            // 读取数据
            double safeHeight = config.getDouble("safeHeight", 64.0);
            double lastY = config.getDouble("lastY", safeHeight);
            int currentLayer = config.getInt("currentLayer", 0);
            long curseStartTime = config.getLong("curseStartTime", 0);
            long curseDuration = config.getLong("curseDuration", 0);
            int curseArm = config.getInt("curseArm", 0);
            boolean isNarehate = config.getBoolean("isNarehate", false);
            String narehateTypeStr = config.getString("narehateType", null);
            
            // 创建数据对象
            PlayerCurseData data = new PlayerCurseData(safeHeight);
            data.setLastY(lastY);
            data.setCurrentLayer(currentLayer);
            data.setCurseStartTime(curseStartTime);
            data.setCurseDuration(curseDuration);
            data.setCurseArm(curseArm);
            data.setNarehate(isNarehate);
            
            if (narehateTypeStr != null) {
                try {
                    data.setNarehateType(PlayerCurseData.NarehateType.valueOf(narehateTypeStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的生骸类型: " + narehateTypeStr);
                }
            }
            
            return data;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "加载玩家数据失败: " + uuid, e);
            return null;
        }
    }

    /**
     * 保存玩家数据（异步）
     * @param player 玩家
     */
    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerCurseData data = playerDataCache.get(uuid);
        if (data == null) {
            return;
        }
        
        // 异步保存
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerDataSync(uuid, data);
        });
    }

    /**
     * 保存玩家数据（同步，在异步任务中调用）
     * @param uuid 玩家 UUID
     * @param data 玩家数据
     */
    private void savePlayerDataSync(UUID uuid, PlayerCurseData data) {
        File playerFile = new File(playersFolder, uuid.toString() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            
            // 保存数据（注意：累计上升记录时间不持久化，因为玩家退出时会清零）
            config.set("safeHeight", data.getSafeHeight());
            config.set("lastY", data.getLastY());
            config.set("currentLayer", data.getCurrentLayer());
            config.set("curseStartTime", data.getCurseStartTime());
            config.set("curseDuration", data.getCurseDuration());
            config.set("curseArm", data.getCurseArm());
            config.set("isNarehate", data.isNarehate());
            
            if (data.getNarehateType() != null) {
                config.set("narehateType", data.getNarehateType().name());
            } else {
                config.set("narehateType", null);
            }
            
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据失败: " + uuid, e);
        }
    }

    /**
     * 保存所有在线玩家数据（异步）
     */
    public void saveAllPlayerData() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (UUID uuid : playerDataCache.keySet()) {
                PlayerCurseData data = playerDataCache.get(uuid);
                if (data != null) {
                    savePlayerDataSync(uuid, data);
                }
            }
        });
    }

    /**
     * 移除玩家数据（从缓存中移除，不删除文件）
     * @param uuid 玩家 UUID
     */
    public void removePlayerData(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    /**
     * 定期自动保存（在主线程中调用，然后异步保存）
     */
    public void autoSave() {
        // 在主线程中获取所有需要保存的数据，然后异步保存
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player);
        }
    }

    /**
     * 异步加载玩家数据（用于离线玩家）
     * @param uuid 玩家 UUID
     * @param callback 回调函数，在主线程中执行
     */
    public void loadPlayerDataAsync(UUID uuid, java.util.function.Consumer<PlayerCurseData> callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerCurseData data = loadPlayerData(uuid);
            // 在主线程中执行回调
            plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(data));
        });
    }

    /**
     * 异步保存玩家数据（通过 UUID）
     * @param uuid 玩家 UUID
     * @param data 玩家数据
     */
    public void savePlayerDataAsync(UUID uuid, PlayerCurseData data) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerDataSync(uuid, data);
        });
    }

    /**
     * 异步保存玩家数据（通过 Player 对象，别名方法）
     * @param player 玩家
     */
    public void savePlayerDataAsync(Player player) {
        savePlayerData(player);
    }
}

