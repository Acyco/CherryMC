/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.server.command;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.Map;

public class CommandDimensions extends CommandBase
{
    @Override
    public String func_71517_b()
    {
        return "dimensions";
    }

    @Override
    public String func_71518_a(ICommandSender sender)
    {
        return "commands.forge.dimensions.usage";
    }

    @Override
    public int func_82362_a()
    {
        return 0;
    }

    @Override
    public boolean func_184882_a(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        sender.func_145747_a(TextComponentHelper.createComponentTranslation(sender, "commands.forge.dimensions.list"));
        for (Map.Entry<DimensionType, IntSortedSet> entry : DimensionManager.getRegisteredDimensions().entrySet())
        {
            sender.func_145747_a(new TextComponentString(entry.getKey().func_186065_b() + ": " + entry.getValue()));
        }
    }
}
