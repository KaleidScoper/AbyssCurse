package io.github.kaleidscoper.abysscurse.achievement;

import org.bukkit.NamespacedKey;

/**
 * 成就枚举
 * 定义"深渊"成就树中的所有成就节点
 */
public enum Achievement {
    /**
     * 根节点：阿比斯之渊
     * 默认达成，描述：故事从这里开始
     */
    ABYSS_EDGE("abyss_edge", "阿比斯之渊", "故事从这里开始", null, true),
    
    /**
     * 赤笛
     * 描述：望向深渊
     */
    RED_WHISTLE("red_whistle", "赤笛", "望向深渊", ABYSS_EDGE, false),
    
    /**
     * 苍笛
     * 描述：离开摇篮
     */
    BLUE_WHISTLE("blue_whistle", "苍笛", "离开摇篮", RED_WHISTLE, false),
    
    /**
     * 月笛
     * 描述：刻写历史
     */
    MOON_WHISTLE("moon_whistle", "月笛", "刻写历史", BLUE_WHISTLE, false),
    
    /**
     * 黑笛
     * 描述：带回故事
     */
    BLACK_WHISTLE("black_whistle", "黑笛", "带回故事", MOON_WHISTLE, false),
    
    /**
     * 白笛
     * 描述：成为传说
     * 挑战类进度，音效：ui.toast.challenge_complete
     */
    WHITE_WHISTLE("white_whistle", "白笛", "成为传说", BLACK_WHISTLE, false, true),
    
    /**
     * 绝界行
     * 描述：有去无回
     * 挑战类进度，音效：ui.toast.challenge_complete
     */
    UNRETURNED("unreturned", "绝界行", "有去无回", WHITE_WHISTLE, false, true),
    
    /**
     * 来自深渊
     * 描述：回归深渊
     * 挑战类进度，音效：ui.toast.challenge_complete
     */
    FROM_ABYSS("from_abyss", "来自深渊", "回归深渊", UNRETURNED, false, true),
    
    /**
     * 奈落之底
     * 描述：故事不会从这里结束
     * 挑战类进度，音效：ui.toast.challenge_complete
     */
    NARAKU("naraku", "奈落之底", "故事不会从这里结束", UNRETURNED, false, true);
    
    private final String id;
    private final String name;
    private final String description;
    private final Achievement parent;
    private final boolean defaultUnlocked;
    private final boolean isChallenge;
    
    /**
     * 构造函数
     * 
     * @param id 成就ID（用于NamespacedKey）
     * @param name 成就显示名称
     * @param description 成就描述
     * @param parent 父成就（null表示根节点）
     * @param defaultUnlocked 是否默认解锁
     */
    Achievement(String id, String name, String description, Achievement parent, boolean defaultUnlocked) {
        this(id, name, description, parent, defaultUnlocked, false);
    }
    
    /**
     * 构造函数
     * 
     * @param id 成就ID（用于NamespacedKey）
     * @param name 成就显示名称
     * @param description 成就描述
     * @param parent 父成就（null表示根节点）
     * @param defaultUnlocked 是否默认解锁
     * @param isChallenge 是否为挑战类进度
     */
    Achievement(String id, String name, String description, Achievement parent, boolean defaultUnlocked, boolean isChallenge) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.defaultUnlocked = defaultUnlocked;
        this.isChallenge = isChallenge;
    }
    
    /**
     * 获取成就ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取成就显示名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取成就描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取父成就
     */
    public Achievement getParent() {
        return parent;
    }
    
    /**
     * 是否默认解锁
     */
    public boolean isDefaultUnlocked() {
        return defaultUnlocked;
    }
    
    /**
     * 是否为挑战类进度
     */
    public boolean isChallenge() {
        return isChallenge;
    }
    
    /**
     * 获取NamespacedKey（需要插件实例来创建）
     */
    public NamespacedKey getKey(org.bukkit.plugin.Plugin plugin) {
        return new NamespacedKey(plugin, "abyss/" + id);
    }
}

