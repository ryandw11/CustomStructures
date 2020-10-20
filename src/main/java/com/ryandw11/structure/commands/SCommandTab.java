package com.ryandw11.structure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SCommandTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length < 2) {
            completions = new ArrayList<>(Arrays.asList("reload", "test", "list", "additem", "checkkey", "getitem", "createschem", "create"));
            completions = getAppliableTabCompleters(args.length == 1 ? args[0] : "", completions);
        }
        Collections.sort(completions);
        return completions;
    }

    public List<String> getAppliableTabCompleters(String arg, List<String> completions) {
        if (arg == null || arg.equalsIgnoreCase("")) {
            return completions;
        }
        ArrayList<String> valid = new ArrayList<>();
        for (String posib : completions) {
            if (posib.startsWith(arg)) {
                valid.add(posib);
            }
        }
        return valid;
    }
}
