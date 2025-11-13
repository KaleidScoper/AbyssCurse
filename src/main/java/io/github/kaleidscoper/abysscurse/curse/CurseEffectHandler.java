package io.github.kaleidscoper.abysscurse.curse;

import org.bukkit.entity.Player;

/**
 * 诅咒效果处理器接口
 * 用于 CurseManager 与 EffectManager 之间的解耦
 */
public interface CurseEffectHandler {
    /**
     * 施加诅咒效果
     * @param player 玩家
     * @param layer 诅咒层级（1-7）
     * @param duration 持续时间（tick数）
     */
    void applyCurseEffects(Player player, int layer, long duration);
    
    /**
     * 移除诅咒效果
     * @param player 玩家
     */
    void removeCurseEffects(Player player);
}

