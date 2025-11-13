package io.github.kaleidscoper.abysscurse.data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 玩家诅咒数据
 * 存储单个玩家的所有诅咒相关数据
 */
public class PlayerCurseData {
    // 安全高度（玩家下降或累计上升高度清零时记录的高度）
    private double safeHeight;
    
    // 上次检查时的 Y 坐标（用于计算上升/下降）
    private double lastY;
    
    // 累计上升记录时间（时间戳队列，每个时间戳代表1格上升）
    private final Queue<Long> riseTimestamps;
    
    // 当前诅咒层级（1-7，0表示无诅咒）
    private int currentLayer;
    
    // 诅咒生效时间（时间戳，触发诅咒时记录）
    private long curseStartTime;
    
    // 诅咒持续时间（tick数，根据诅咒臂动态计算）
    private long curseDuration;
    
    // 触发诅咒时的诅咒臂（用于计算持续时间）
    private int curseArm;
    
    // 是否为生骸（豁免者）
    private boolean isNarehate;
    
    // 生骸类型（LUCKY/SAD，枚举）
    private NarehateType narehateType;
    
    // 累计上升过期时间（20分钟 = 20 * 60 * 1000 毫秒）
    private static final long EXPIRE_TIME = 20 * 60 * 1000;

    public PlayerCurseData(double initialY) {
        this.safeHeight = initialY;
        this.lastY = initialY;
        this.riseTimestamps = new ConcurrentLinkedQueue<>();
        this.currentLayer = 0;
        this.curseStartTime = 0;
        this.curseDuration = 0;
        this.curseArm = 0;
        this.isNarehate = false;
        this.narehateType = null;
    }

    /**
     * 获取安全高度
     */
    public double getSafeHeight() {
        return safeHeight;
    }

    /**
     * 设置安全高度
     */
    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
    }

    /**
     * 获取上次检查时的 Y 坐标
     */
    public double getLastY() {
        return lastY;
    }

    /**
     * 设置上次检查时的 Y 坐标
     */
    public void setLastY(double lastY) {
        this.lastY = lastY;
    }

    /**
     * 添加上升记录
     * @param blocks 上升的方块数
     */
    public void addRise(int blocks) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < blocks; i++) {
            riseTimestamps.offer(now);
        }
    }

    /**
     * 获取当前累计上升高度
     * 自动清理过期记录
     */
    public int getTotalRise() {
        long expireTime = System.currentTimeMillis() - EXPIRE_TIME;
        // 清理队列头部的过期项
        while (!riseTimestamps.isEmpty() && riseTimestamps.peek() < expireTime) {
            riseTimestamps.poll();
        }
        return riseTimestamps.size();
    }

    /**
     * 清空累计上升记录
     */
    public void clearRiseRecords() {
        riseTimestamps.clear();
    }

    /**
     * 获取当前诅咒层级
     */
    public int getCurrentLayer() {
        return currentLayer;
    }

    /**
     * 设置当前诅咒层级
     */
    public void setCurrentLayer(int currentLayer) {
        this.currentLayer = currentLayer;
    }

    /**
     * 获取诅咒生效时间
     */
    public long getCurseStartTime() {
        return curseStartTime;
    }

    /**
     * 设置诅咒生效时间
     */
    public void setCurseStartTime(long curseStartTime) {
        this.curseStartTime = curseStartTime;
    }

    /**
     * 获取诅咒持续时间（tick数）
     */
    public long getCurseDuration() {
        return curseDuration;
    }

    /**
     * 设置诅咒持续时间（tick数）
     */
    public void setCurseDuration(long curseDuration) {
        this.curseDuration = curseDuration;
    }

    /**
     * 获取触发诅咒时的诅咒臂
     */
    public int getCurseArm() {
        return curseArm;
    }

    /**
     * 设置触发诅咒时的诅咒臂
     */
    public void setCurseArm(int curseArm) {
        this.curseArm = curseArm;
    }

    /**
     * 是否为生骸
     */
    public boolean isNarehate() {
        return isNarehate;
    }

    /**
     * 设置是否为生骸
     */
    public void setNarehate(boolean narehate) {
        this.isNarehate = narehate;
    }

    /**
     * 获取生骸类型
     */
    public NarehateType getNarehateType() {
        return narehateType;
    }

    /**
     * 设置生骸类型
     */
    public void setNarehateType(NarehateType narehateType) {
        this.narehateType = narehateType;
    }

    /**
     * 生骸类型枚举
     */
    public enum NarehateType {
        LUCKY,  // 幸运生骸
        SAD     // 悲惨生骸
    }
}

