package org.lolicode.nekomusic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.lolicode.nekomusic.NekoMusic;
import org.lolicode.nekomusic.config.ModConfig;
import org.lolicode.nekomusic.helper.LoginHelper;
import org.lolicode.nekomusic.manager.MusicManager;
import org.lolicode.nekomusic.music.SongList;

public class MusicCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootNode = Commands.literal("music")
                .requires(Permissions.require("nekomusic", 0))
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .requires(Permissions.require("nekomusic.add", 0))
                        .executes(context -> {
                            String url = StringArgumentType.getString(context, "url");
                            MusicManager.order(context.getSource().getServer(), context.getSource(), url, false, false);
                            return 0;
                        }))
                .then(Commands.literal("--now")
                        .requires(Permissions.require("nekomusic.add.now", 0))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String url = StringArgumentType.getString(context, "url");
                                    MusicManager.order(context.getSource().getServer(), context.getSource(), url, true, false);
                                    return 0;
                                })))
                .then(Commands.literal("--replace")
                        .requires(Permissions.require("nekomusic.add.replace", 1))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String url = StringArgumentType.getString(context, "url");
                                    MusicManager.order(context.getSource().getServer(), context.getSource(), url, true, true);
                                    return 0;
                                })))
                .build();
        LiteralCommandNode<CommandSourceStack> addNode = Commands.literal("add")
                .requires(Permissions.require("nekomusic.add", 0))
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(context -> {
                            String url = StringArgumentType.getString(context, "url");
                            MusicManager.order(context.getSource().getServer(), context.getSource(), url, false, false);
                            return 0;
                        }))
                .then(Commands.literal("--now")
                        .requires(Permissions.require("nekomusic.add.now", 0))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String url = StringArgumentType.getString(context, "url");
                                    MusicManager.order(context.getSource().getServer(), context.getSource(), url, true, false);
                                    return 0;
                                })))
                .then(Commands.literal("--replace")
                        .requires(Permissions.require("nekomusic.add.replace", 1))
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String url = StringArgumentType.getString(context, "url");
                                    MusicManager.order(context.getSource().getServer(), context.getSource(), url, true, true);
                                    return 0;
                                })))
                .build();
        LiteralCommandNode<CommandSourceStack> delNode = Commands.literal("del")
                .requires(Permissions.require("nekomusic.del", 0))
                .then(Commands.argument("index", IntegerArgumentType.integer())
                        .executes(context -> {
                            int index = IntegerArgumentType.getInteger(context, "index");
                            MusicManager.del(context.getSource().getServer(), context.getSource(), index);
                            return 0;
                        }))
                .then(Commands.literal("id")
                        .then(Commands.argument("id", LongArgumentType.longArg())
                                .executes(context -> {
                                    long id = LongArgumentType.getLong(context, "id");
                                    MusicManager.del(context.getSource().getServer(), context.getSource(), id);
                                    return 0;
                                })))
                .build();
        LiteralCommandNode<CommandSourceStack> banNode = Commands.literal("ban")
                .requires(Permissions.require("nekomusic.ban", 1))
                    .then(Commands.argument("id", LongArgumentType.longArg())
                            .executes(context -> {
                                long id = LongArgumentType.getLong(context, "id");
                                MusicManager.ban(context.getSource().getServer(), context.getSource(), id);
                                return 0;
                            }))
                .build();
        LiteralCommandNode<CommandSourceStack> unbanNode = Commands.literal("unban")
                .requires(Permissions.require("nekomusic.unban", 1))
                .then(Commands.argument("id", LongArgumentType.longArg())
                        .executes(context -> {
                            long id = LongArgumentType.getLong(context, "id");
                            MusicManager.unban(context.getSource().getServer(), context.getSource(), id);
                            return 0;
                        }))
                .build();
        LiteralCommandNode<CommandSourceStack> listNode = Commands.literal("list")
                .requires(Permissions.require("nekomusic.list", 0))
                .executes(context -> {
                    MusicManager.list(context.getSource().getServer(), context.getSource());
                    return 0;
                }).build();
        LiteralCommandNode<CommandSourceStack> nextNode = Commands.literal("next")
                .requires(Permissions.require("nekomusic.next", 1)
                        .or(Permissions.require("nekomusic.vote", 0)))
                .executes(context -> {
                    if (Permissions.check(context.getSource(), "nekomusic.next", 1))
                        MusicManager.next(context.getSource().getServer(), context.getSource());
                    else
                        MusicManager.vote(context.getSource().getServer(), context.getSource());
                    return 0;
                }).build();
        LiteralCommandNode<CommandSourceStack> searchNode = Commands.literal("search")
                .requires(Permissions.require("nekomusic.search", 0))
                .then(Commands.literal("page")
                        .then(Commands.argument("page", IntegerArgumentType.integer())
                                .then(Commands.argument("keyword", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String keyword = StringArgumentType.getString(context, "keyword");
                                            int page = IntegerArgumentType.getInteger(context, "page");
                                            MusicManager.search(context.getSource().getServer(), context.getSource(), keyword, page);
                                            return 0;
                                        }))))
                .then(Commands.argument("keyword", StringArgumentType.greedyString())
                        .executes(context -> {
                            String keyword = StringArgumentType.getString(context, "keyword");
                            MusicManager.search(context.getSource().getServer(), context.getSource(), keyword, 1);
                            return 0;
                        })).build();
        LiteralCommandNode<CommandSourceStack> reloadNode = Commands.literal("reload")
                .requires(Permissions.require("nekomusic.reload", 1))
                .then(Commands.literal("all")
                        .requires(Permissions.require("nekomusic.reload.all", 2))
                        .executes(context -> {
                            ModConfig.reload(context.getSource().getServer(), context.getSource());
                            return 0;
                        }))
                .then(Commands.literal("list")
                        .requires(Permissions.require("nekomusic.reload.list", 1))
                        .executes(context -> {
                            if (NekoMusic.CONFIG.idleList > 0) {
                                SongList.loadIdleList();
                            }
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("§aNekoMusic: Refreshing playlist"), true);
                            return 0;
                        }))
                .build();
        LiteralCommandNode<CommandSourceStack> loginNode = Commands.literal("login")
                .requires(Permissions.require("nekomusic.login", 4))
                .then(Commands.literal("start")
                        .requires(Permissions.require("nekomusic.login.start", 4))
                        .executes(context -> {
                            LoginHelper.genQr(context.getSource());
                            return 0;
                        }))
                .then(Commands.literal("check")
                        .requires(Permissions.require("nekomusic.login.check", 4))
                        .executes(context -> {
                            LoginHelper.check(context.getSource());
                            return 0;
                        }))
                .then(Commands.literal("status")
                        .requires(Permissions.require("nekomusic.login.status", 4))
                        .executes(context -> {
                            LoginHelper.status(context.getSource());
                            return 0;
                        }))
                .build();

        rootNode.addChild(addNode);
        rootNode.addChild(delNode);
        rootNode.addChild(banNode);
        rootNode.addChild(unbanNode);
        rootNode.addChild(listNode);
        rootNode.addChild(nextNode);
        rootNode.addChild(searchNode);
        rootNode.addChild(reloadNode);
        rootNode.addChild(loginNode);
        dispatcher.getRoot().addChild(rootNode);
    }
}
