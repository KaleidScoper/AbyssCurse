package io.github.kaleidscoper.abysscurse.mode;

/**
 * 插件模式枚举
 */
public enum PluginMode {
    /**
     * 关闭模式 - 插件不对任何玩家施加诅咒
     */
    OFF,
    
    /**
     * 区域开启模式 - 仅在定义的 abyss 区域内施加诅咒
     */
    ABYSS,
    
    /**
     * 全部开启模式 - 整个主世界都被视为 abyss
     */
    WORLD
}

