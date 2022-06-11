package com.ryandw11.structure.commands;

import com.ryandw11.structure.CustomStructures;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SCommandTab implements TabCompleter {
    private final CustomStructures plugin;

    public SCommandTab(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2 && (args[0].equalsIgnoreCase("test") || args[0].equalsIgnoreCase("testspawn"))) {
            completions = new ArrayList<>(plugin.getStructureHandler().getStructureNames());
            completions = getApplicableTabCompleter(args[1], completions);
        } else if(args.length == 2 && (
                args[0].equalsIgnoreCase("setLootTable") ||
                        args[0].equalsIgnoreCase("setLoot") ||
                        args[0].equalsIgnoreCase("setlt")))  {
            completions = plugin.getLootTableHandler().getLootTablesNames();
            completions = getApplicableTabCompleter(args[1], completions);
        } else if (args.length <= 1) {
            completions = new ArrayList<>(Arrays.asList("reload", "test", "list", "addItem", "checkKey", "getItem",
                    "createSchem", "create", "nearby", "testspawn", "addons", "setLootTable"));
            completions = getApplicableTabCompleter(args.length == 1 ? args[0] : "", completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private List<String> getApplicableTabCompleter(String arg, List<String> completions) {
        if (arg == null || arg.equalsIgnoreCase("")) {
            return completions;
        }
        List<String> valid = new ArrayList<>();
        for (String posib : completions) {
            if (posib.startsWith(arg)) {
                valid.add(posib);
            }
        }
        return valid;
    }
}
