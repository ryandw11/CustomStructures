package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

public class AddOneTimeStructureCommand implements SubCommand {

    private final CustomStructures plugin;

    public AddOneTimeStructureCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission("customstructures.create")) {
            // Registers the MySQL driver
            try {
                Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
            } catch (Exception e) {
                ((Player) sender).sendMessage(ChatColor.RED + "MySQL driver could not be found!");
                e.printStackTrace();
            }

            // Gets a JDBC connection using the JDBC_URL specified in config.yml
            try (Connection conn = DriverManager.getConnection(plugin.getConfig().getString("JDBC_URL"))) {
                try (PreparedStatement statement = conn.prepareStatement("INSERT INTO one_time_structures (structure_name) VALUES (?)")) {
                    statement.setString(1, args[0]);
                    if (statement.executeUpdate() == 1) {
                        ((Player) sender).sendMessage(ChatColor.GREEN + args[0] + " has been added to the one-time structures list!");
                        plugin.getLogger().info(args[0] + " has been added to the one-time structures list!");
                    } else {
                        ((Player) sender).sendMessage(ChatColor.RED + args[0] + " has not been added to the one-time structures list!");
                    }
                }
            } catch (SQLException throwables) {
                ((Player) sender).sendMessage(ChatColor.RED + "Error occurred!");
                throwables.printStackTrace();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
        }

        return false;
    }
}
