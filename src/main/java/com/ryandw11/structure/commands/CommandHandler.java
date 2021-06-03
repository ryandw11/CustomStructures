package com.ryandw11.structure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extremely primitive command handler.
 * <p>
 * This will be replaced in the future with a proper one.
 */
public class CommandHandler {
    private final Map<List<String>, SubCommand> commandMap;

    public CommandHandler() {
        commandMap = new HashMap<>();
    }

    /**
     * Register a command.
     *
     * @param s          The command to register.
     * @param subCommand The sub command.
     */
    public void registerCommand(String s, SubCommand subCommand) {
        if (commandMap.containsKey(Collections.singletonList(s.toLowerCase())))
            throw new IllegalArgumentException("Command already exists!");

        commandMap.put(Collections.singletonList(s.toLowerCase()), subCommand);
    }

    /**
     * Register a command.
     *
     * @param subCommand The sub command.
     * @param args The aliases to register.
     */
    public void registerCommand(SubCommand subCommand, String... args) {
        List<String> list = new ArrayList<>(Arrays.asList(args));
        list = list.stream().map(String::toLowerCase).collect(Collectors.toList());
        if (commandMap.containsKey(list))
            throw new IllegalArgumentException("Command already exists!");

        commandMap.put(list, subCommand);
    }

    /**
     * Handle a command.
     *
     * <p>Throws IllegalArgumentException if sub command does not exist.</p>
     *
     * @param sender The command sender.
     * @param cmd    The command.
     * @param s      The string.
     * @param args   The arguments.
     * @return If the command is valid.
     */
    public boolean handleCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length == 0)
            return false;
        for (Map.Entry<List<String>, SubCommand> entry : commandMap.entrySet()) {
            if (entry.getKey().contains(args[0].toLowerCase())) {
                String[] newArgs = new String[args.length - 1];
                if (newArgs.length > 0) {
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                }
                return entry.getValue().subCommand(sender, cmd, s, newArgs);
            }
        }
        throw new IllegalArgumentException("Invalid command");
    }


}
