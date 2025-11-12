package io.github.kaleidscoper.abysscurse;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class AbyssCurseListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("§8[§5AbyssCurse§8] §7欢迎来到深渊，探窟家" + event.getPlayer().getName() + "！");
    }
}
