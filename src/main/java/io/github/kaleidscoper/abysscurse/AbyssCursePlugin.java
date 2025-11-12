package io.github.kaleidscoper.abysscurse;

import org.bukkit.plugin.java.JavaPlugin;

public final class AbyssCursePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("AbyssCurse 已加载！");
        getServer().getPluginManager().registerEvents(new AbyssCurseListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("AbyssCurse 已卸载！");
    }
}
