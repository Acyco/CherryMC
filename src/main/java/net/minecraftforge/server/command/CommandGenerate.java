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

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.WorldWorkerManager;

class CommandGenerate extends CommandBase
{
    @Override
    public String func_71517_b()
    {
        return "generate";
    }

    @Override
    public List<String> func_71514_a()
    {
        return Collections.singletonList("gen");
    }

    @Override
    public String func_71518_a(ICommandSender sender)
    {
        return "commands.forge.gen.usage";
    }

    @Override
    public int func_82362_a()
    {
        return 4;
    }

    @Override
    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // x y z chunkCount [dim] [interval]
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.forge.gen.usage");
        }

        BlockPos blockpos = func_175757_a(sender, args, 0, false);
        int count = func_180528_a(args[3], 10);
        int dim = args.length >= 5 ? func_175755_a(args[4]) : sender.func_130014_f_().field_73011_w.getDimension();
        int interval = args.length >= 6 ? func_175755_a(args[5]) : -1;
        BlockPos chunkpos = new BlockPos(blockpos.func_177958_n() >> 4, 0, blockpos.func_177952_p() >> 4);

        ChunkGenWorker worker = new ChunkGenWorker(sender, chunkpos, count, dim, interval);
        sender.func_145747_a(worker.getStartMessage(sender));
        WorldWorkerManager.addWorker(worker);
    }

    @Override
    public List<String> func_184883_a(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length < 4)
        {
            return func_175771_a(args, 0, targetPos);
        }
        // Chunk Count? No completion
        // Dimension, Add support for names? Get list of ids? Meh
        return Collections.emptyList();
    }
}
