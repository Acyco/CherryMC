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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandSetDimension extends CommandBase
{
    @Override
    public String func_71517_b()
    {
        return "setdimension";
    }

    @Override
    public List<String> func_71514_a()
    {
        return Collections.singletonList("setdim");
    }

    @Override
    public String func_71518_a(ICommandSender sender)
    {
        return "commands.forge.setdim.usage";
    }

    @Override
    public List<String> func_184883_a(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length > 2 && args.length <= 5)
        {
            return func_175771_a(args, 2, targetPos);
        }
        return Collections.emptyList();
    }

    @Override
    public int func_82362_a()
    {
        return 2;
    }

    @Override
    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // args: <entity> <dim> [<x> <y> <z>]
        if (args.length != 2 && args.length != 5)
        {
            throw new WrongUsageException("commands.forge.setdim.usage");
        }
        Entity entity = func_184885_b(server, sender, args[0]);
        if (!checkEntity(entity))
        {
            throw new CommandException("commands.forge.setdim.invalid.entity", entity.func_70005_c_());
        }
        int dimension = func_175755_a(args[1]);
        if (!DimensionManager.isDimensionRegistered(dimension))
        {
            throw new CommandException("commands.forge.setdim.invalid.dim", dimension);
        }
        if (dimension == entity.field_71093_bK)
        {
            throw new CommandException("commands.forge.setdim.invalid.nochange", entity.func_70005_c_(), dimension);
        }
        BlockPos pos = args.length == 5 ? func_175757_a(sender, args, 2, false) : sender.func_180425_c();
        entity.changeDimension(dimension, new CommandTeleporter(pos));
    }

    private static boolean checkEntity(Entity entity)
    {
        // use vanilla portal logic, try to avoid doing anything too silly
        return !entity.func_184218_aH() && !entity.func_184207_aI() && entity.func_184222_aU();
    }

    private static class CommandTeleporter implements ITeleporter
    {
        private final BlockPos targetPos;

        private CommandTeleporter(BlockPos targetPos)
        {
            this.targetPos = targetPos;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw)
        {
            entity.func_174828_a(targetPos, yaw, entity.field_70125_A);
        }
    }
}
