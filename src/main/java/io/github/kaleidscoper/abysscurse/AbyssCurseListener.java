package io.github.kaleidscoper.abysscurse;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class AbyssCurseListener implements Listener {

    // TODO: 占位实现 - 当前仅显示欢迎消息，无实际功能
    // 待实现：
    // 1. 玩家加入时加载玩家数据（PlayerDataManager）
    // 2. 初始化安全高度（玩家进入游戏时的 Y 坐标）
    // 3. 初始化累计上升高度为 0
    // 4. 其他事件监听（PlayerMoveEvent、PlayerQuitEvent 等）
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("§8[§5AbyssCurse§8] §7欢迎来到深渊，探窟家" + event.getPlayer().getName() + "！");
    }
}
