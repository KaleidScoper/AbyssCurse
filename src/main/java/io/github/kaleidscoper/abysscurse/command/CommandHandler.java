package io.github.kaleidscoper.abysscurse.command;

import io.github.kaleidscoper.abysscurse.config.ConfigManager;
import io.github.kaleidscoper.abysscurse.debug.DebugManager;
import io.github.kaleidscoper.abysscurse.mode.ModeManager;
import io.github.kaleidscoper.abysscurse.mode.PluginMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 命令处理器
 * 处理所有插件相关命令
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final ModeManager modeManager;
    private final ConfigManager configManager;
    private final DebugManager debugManager;

    public CommandHandler(JavaPlugin plugin, ModeManager modeManager, ConfigManager configManager, DebugManager debugManager) {
        this.plugin = plugin;
        this.modeManager = modeManager;
        this.configManager = configManager;
        this.debugManager = debugManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "mode":
                return handleModeCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "info":
                return handleInfoCommand(sender);
            case "debug":
                return handleDebugCommand(sender, args);
            default:
                sender.sendMessage("§8[§5AbyssCurse§8] §c未知命令: " + subCommand);
                sendHelp(sender);
                return true;
        }
    }

    /**
     * 处理模式切换命令
     * /abysscurse mode <off|abyss|world> [centerX] [centerY] [centerZ] [radius]
     */
    private boolean handleModeCommand(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("abysscurse.admin")) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c你没有权限使用此命令！");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c用法: /abysscurse mode <off|abyss|world> [centerX] [centerY] [centerZ] [radius]");
            return true;
        }

        String modeStr = args[1].toUpperCase();
        PluginMode targetMode;

        try {
            targetMode = PluginMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c无效的模式: " + modeStr);
            sender.sendMessage("§8[§5AbyssCurse§8] §7可用模式: off, abyss, world");
            return true;
        }

        // 切换到 OFF 或 WORLD 模式
        if (targetMode == PluginMode.OFF || targetMode == PluginMode.WORLD) {
            if (modeManager.setMode(targetMode)) {
                sender.sendMessage("§8[§5AbyssCurse§8] §a模式已切换为: §e" + targetMode.name());
            } else {
                sender.sendMessage("§8[§5AbyssCurse§8] §c模式切换失败！");
            }
            return true;
        }

        // 切换到 ABYSS 模式
        if (targetMode == PluginMode.ABYSS) {
            // 如果提供了坐标和半径参数
            if (args.length >= 6) {
                try {
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);
                    int z = Integer.parseInt(args[4]);
                    int radius = Integer.parseInt(args[5]);

                    if (radius <= 0) {
                        sender.sendMessage("§8[§5AbyssCurse§8] §c半径必须大于 0！");
                        return true;
                    }

                    // 设置区域配置
                    configManager.setAbyssRegion(x, y, z, radius);

                    sender.sendMessage("§8[§5AbyssCurse§8] §a已设置 Abyss 区域:");
                    sender.sendMessage("§8[§5AbyssCurse§8] §7中心: §e(" + x + ", " + y + ", " + z + ")");
                    sender.sendMessage("§8[§5AbyssCurse§8] §7半径: §e" + radius + " 区块");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§8[§5AbyssCurse§8] §c无效的坐标或半径参数！");
                    return true;
                }
            }

            // 切换模式
            if (modeManager.setMode(targetMode)) {
                sender.sendMessage("§8[§5AbyssCurse§8] §a模式已切换为: §e" + targetMode.name());
                if (args.length < 6) {
                    sender.sendMessage("§8[§5AbyssCurse§8] §7提示: 使用 /abysscurse mode abyss <x> <y> <z> <radius> 设置区域");
                }
            } else {
                sender.sendMessage("§8[§5AbyssCurse§8] §c模式切换失败！请检查配置。");
            }
            return true;
        }

        return true;
    }

    /**
     * 处理重载命令
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("abysscurse.admin")) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c你没有权限使用此命令！");
            return true;
        }

        try {
            configManager.reloadConfig();
            modeManager.reload();
            sender.sendMessage("§8[§5AbyssCurse§8] §a配置已重载！");
        } catch (Exception e) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c重载失败: " + e.getMessage());
            plugin.getLogger().severe("重载配置时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 处理信息命令
     */
    private boolean handleInfoCommand(CommandSender sender) {
        sender.sendMessage("§8[§5AbyssCurse§8] §7========== 插件信息 ==========");
        sender.sendMessage("§8[§5AbyssCurse§8] §7当前模式: §e" + modeManager.getCurrentMode().name());
        
        if (modeManager.isAbyss()) {
            sender.sendMessage("§8[§5AbyssCurse§8] §7Abyss 中心: §e(" + 
                configManager.getAbyssCenterX() + ", " +
                configManager.getAbyssCenterY() + ", " +
                configManager.getAbyssCenterZ() + ")");
            sender.sendMessage("§8[§5AbyssCurse§8] §7Abyss 半径: §e" + 
                configManager.getAbyssRadius() + " 区块");
        }
        
        sender.sendMessage("§8[§5AbyssCurse§8] §7版本: §e" + plugin.getPluginMeta().getVersion());
        sender.sendMessage("§8[§5AbyssCurse§8] §7==============================");
        return true;
    }

    /**
     * 处理调试命令
     * /abysscurse debug <on|off|toggle|info> [player]
     */
    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("abysscurse.admin")) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c你没有权限使用此命令！");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c用法: /abysscurse debug <on|off|toggle|info|global> [on|off]");
            return true;
        }

        String action = args[1].toLowerCase();

        // 全局调试开关（仅控制台或管理员）
        if (action.equals("global")) {
            if (args.length < 3) {
                sender.sendMessage("§8[§5AbyssCurse§8] §c用法: /abysscurse debug global <on|off>");
                return true;
            }
            
            boolean enabled = args[2].equalsIgnoreCase("on");
            debugManager.setGlobalDebug(enabled);
            
            if (enabled) {
                debugManager.start();
                sender.sendMessage("§8[§5AbyssCurse§8] §a全局调试模式已开启");
            } else {
                debugManager.stop();
                sender.sendMessage("§8[§5AbyssCurse§8] §c全局调试模式已关闭");
            }
            return true;
        }

        // 玩家调试命令（需要玩家执行）
        if (!(sender instanceof Player)) {
            sender.sendMessage("§8[§5AbyssCurse§8] §c此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;

        switch (action) {
            case "on":
                debugManager.enableDebug(player);
                return true;
            case "off":
                debugManager.disableDebug(player);
                return true;
            case "toggle":
                debugManager.toggleDebug(player);
                return true;
            case "info":
                sender.sendMessage(debugManager.getDebugInfoText(player));
                return true;
            default:
                sender.sendMessage("§8[§5AbyssCurse§8] §c无效的操作: " + action);
                sender.sendMessage("§8[§5AbyssCurse§8] §7可用操作: on, off, toggle, info, global");
                return true;
        }
    }

    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8[§5AbyssCurse§8] §7========== 命令帮助 ==========");
        sender.sendMessage("§8[§5AbyssCurse§8] §e/abysscurse mode <off|abyss|world> [x] [y] [z] [radius]");
        sender.sendMessage("§8[§5AbyssCurse§8] §7  切换插件模式");
        sender.sendMessage("§8[§5AbyssCurse§8] §e/abysscurse reload");
        sender.sendMessage("§8[§5AbyssCurse§8] §7  重载配置文件");
        sender.sendMessage("§8[§5AbyssCurse§8] §e/abysscurse info");
        sender.sendMessage("§8[§5AbyssCurse§8] §7  查看插件信息");
        sender.sendMessage("§8[§5AbyssCurse§8] §e/abysscurse debug <on|off|toggle|info|global>");
        sender.sendMessage("§8[§5AbyssCurse§8] §7  调试模式控制");
        sender.sendMessage("§8[§5AbyssCurse§8] §7==============================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 主命令补全
            completions.addAll(Arrays.asList("mode", "reload", "info", "debug"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            // 调试命令补全
            completions.addAll(Arrays.asList("on", "off", "toggle", "info", "global"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("debug") && args[1].equalsIgnoreCase("global")) {
            // 全局调试开关补全
            completions.addAll(Arrays.asList("on", "off"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            // 模式补全
            completions.addAll(Arrays.asList("off", "abyss", "world"));
        } else if (args.length >= 3 && args.length <= 6 && args[0].equalsIgnoreCase("mode") 
                   && args[1].equalsIgnoreCase("abyss")) {
            // ABYSS 模式的坐标和半径补全
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 3) {
                    completions.add(String.valueOf(player.getLocation().getBlockX()));
                } else if (args.length == 4) {
                    completions.add(String.valueOf(player.getLocation().getBlockY()));
                } else if (args.length == 5) {
                    completions.add(String.valueOf(player.getLocation().getBlockZ()));
                } else if (args.length == 6) {
                    completions.add("5");
                    completions.add("10");
                    completions.add("15");
                }
            }
        }

        // 过滤匹配的补全项
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .collect(Collectors.toList());
    }
}

