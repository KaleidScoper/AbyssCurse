package io.github.kaleidscoper.abysscurse.data;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 玩家诅咒数据
 * 存储单个玩家的所有诅咒相关数据
 */
public class PlayerCurseData {
    // 安全高度（玩家下降或累计上升高度清零时记录的高度）
    private double safeHeight;
    
    // 上次检查时的 Y 坐标（用于计算上升/下降）
    private double lastY;
    
    // 累计上升记录（每条记录包含时间戳与对应的高度增量）
    private final ConcurrentLinkedDeque<RiseRecord> riseRecords;
    
    // 当前累计上升高度（含未满一格的小数）
    private double totalRise;
    
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
    
    // 暴露在深层诅咒（第六层及以下）中的开始时间（时间戳，0表示未暴露）
    private long deepCurseExposureStartTime;
    
    // 累计上升过期时间（20分钟 = 20 * 60 * 1000 毫秒）
    private static final long EXPIRE_TIME = 20 * 60 * 1000;

    public PlayerCurseData(double initialY) {
        this.safeHeight = initialY;
        this.lastY = initialY;
        this.riseRecords = new ConcurrentLinkedDeque<>();
        this.totalRise = 0.0;
        this.currentLayer = 0;
        this.curseStartTime = 0;
        this.curseDuration = 0;
        this.curseArm = 0;
        this.isNarehate = false;
        this.narehateType = null;
        this.deepCurseExposureStartTime = 0;
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
     * 按浮点增量记录上升
     * @param riseDelta 本次上升的高度（>0）
     */
    public void addRiseDelta(double riseDelta) {
        if (riseDelta <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        riseRecords.addLast(new RiseRecord(now, riseDelta));
        totalRise += riseDelta;
    }

    /**
     * 获取当前累计上升高度
     * 自动清理过期记录
     */
    public double getTotalRise() {
        cleanupExpiredRise();
        return Math.max(0.0, totalRise);
    }

    /**
     * 清空累计上升记录
     */
    public void clearRiseRecords() {
        riseRecords.clear();
        totalRise = 0.0;
    }

    /**
     * 下降时消耗累计上升高度（最多减至 0）
     * @param descendDelta 下降的高度（>0）
     */
    public void consumeRiseDelta(double descendDelta) {
        if (descendDelta <= 0) {
            return;
        }
        cleanupExpiredRise();
        if (descendDelta >= totalRise) {
            clearRiseRecords();
            return;
        }
        double remaining = descendDelta;
        while (remaining > 0 && !riseRecords.isEmpty()) {
            RiseRecord last = riseRecords.peekLast();
            if (last == null) {
                break;
            }
            if (last.amount <= remaining + 1e-9) {
                remaining -= last.amount;
                totalRise -= last.amount;
                riseRecords.pollLast();
            } else {
                last.amount -= remaining;
                totalRise -= remaining;
                remaining = 0;
            }
        }
        if (totalRise < 0) {
            totalRise = 0;
        }
    }

    /**
     * 清除过期的上升记录
     */
    private void cleanupExpiredRise() {
        long expireTime = System.currentTimeMillis() - EXPIRE_TIME;
        while (!riseRecords.isEmpty()) {
            RiseRecord first = riseRecords.peekFirst();
            if (first == null || first.timestamp >= expireTime) {
                break;
            }
            totalRise -= first.amount;
            riseRecords.pollFirst();
        }
        if (totalRise < 0) {
            totalRise = 0;
        }
    }

    /**
     * 累计上升记录
     */
    private static class RiseRecord {
        private final long timestamp;
        private double amount;

        private RiseRecord(long timestamp, double amount) {
            this.timestamp = timestamp;
            this.amount = amount;
        }
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
     * 获取暴露在深层诅咒中的开始时间
     */
    public long getDeepCurseExposureStartTime() {
        return deepCurseExposureStartTime;
    }

    /**
     * 设置暴露在深层诅咒中的开始时间
     */
    public void setDeepCurseExposureStartTime(long deepCurseExposureStartTime) {
        this.deepCurseExposureStartTime = deepCurseExposureStartTime;
    }

    /**
     * 生骸类型枚举
     */
    public enum NarehateType {
        LUCKY,  // 幸运生骸
        SAD     // 悲惨生骸
    }
}

