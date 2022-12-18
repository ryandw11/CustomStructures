package com.ryandw11.structure.schematic.structuresigns;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.utils.CSUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Command Structure Sign.
 *
 * <p>This structure sign is registered in the {@link com.ryandw11.structure.schematic.StructureSignHandler} constructor.</p>
 */
public class CommandSign extends StructureSign {

    @Override
    public boolean onStructureSpawn(@NotNull Location location, @NotNull Structure structure) {
        CustomStructures plugin = CustomStructures.getInstance();

        if (!hasArgument(0)) {
            plugin.getLogger().warning(String.format("Invalid command configuration on a structure sign! (%s)", structure.getName()));
            return true;
        }

        List<String> commands = plugin.getSignCommandsHandler().getCommands(getStringArgument(0));
        if (commands != null) {
            for (String command : commands) {
                command = CSUtils.replacePlaceHolders(command, location, getStructureMinimumLocation(), getStructureMaximumLocation());
                command = CustomStructures.replacePAPIPlaceholders(command);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                if (plugin.isDebug()) {
                    plugin.getLogger().info("Executing console command: '" + command + "'");
                }
            }
        } else {
            plugin.getLogger().warning(String.format("Unable to execute command group '%s', no configuration found!", getStringArgument(0)));
        }

        return true;
    }

}
