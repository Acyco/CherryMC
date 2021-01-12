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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.WorldWorkerManager.IWorker;

public class ChunkGenWorker implements IWorker
{
    private final ICommandSender listener;
    protected final BlockPos start;
    protected final int total;
    private final int dim;
    private final Queue<BlockPos> queue;
    private final int notificationFrequency;
    private int lastNotification = 0;
    private long lastNotifcationTime = 0;
    private int genned = 0;
    private Boolean keepingLoaded;

    public ChunkGenWorker(ICommandSender listener, BlockPos start, int total, int dim, int interval)
    {
        this.listener = listener;
        this.start = start;
        this.total = total;
        this.dim  = dim;
        this.queue = buildQueue();
        this.notificationFrequency = interval != -1 ? interval : Math.max(total / 20, 100); //Every 5% or every 100, whichever is more.
        this.lastNotifcationTime = System.currentTimeMillis(); //We also notify at least once every 60 seconds, to show we haven't froze.
    }

    protected Queue<BlockPos> buildQueue()
    {
        Queue<BlockPos> ret = new ArrayDeque<BlockPos>();
        ret.add(start);

        //This *should* spiral outwards, starting on right side, down, left, up, right, but hey we'll see!
        int radius = 1;
        while (ret.size() < total)
        {
            for (int q = -radius + 1; q <= radius && ret.size() < total; q++)
                ret.add(start.func_177982_a(radius, 0, q));

            for (int q = radius - 1; q >= -radius && ret.size() < total; q--)
                ret.add(start.func_177982_a(q, 0, radius));

            for (int q = radius - 1; q >= -radius && ret.size() < total; q--)
                ret.add(start.func_177982_a(-radius, 0, q));

            for (int q = -radius + 1; q <= radius && ret.size() < total; q++)
                ret.add(start.func_177982_a(q, 0, -radius));

            radius++;
        }
        return ret;
    }

    @Deprecated // TODO remove in 1.13
    public TextComponentTranslation getStartMessage()
    {
        return new TextComponentTranslation("commands.forge.gen.start", total, start.func_177958_n(), start.func_177952_p(), dim);
    }

    public TextComponentBase getStartMessage(ICommandSender sender)
    {
        return TextComponentHelper.createComponentTranslation(sender, "commands.forge.gen.start", total, start.func_177958_n(), start.func_177952_p(), dim);
    }

    @Override
    public boolean hasWork()
    {
        return queue.size() > 0;
    }

    @Override
    public boolean doWork()
    {
        WorldServer world = DimensionManager.getWorld(dim);
        if (world == null)
        {
            DimensionManager.initDimension(dim);
            world = DimensionManager.getWorld(dim);
            if (world == null)
            {
                listener.func_145747_a(TextComponentHelper.createComponentTranslation(listener, "commands.forge.gen.dim_fail", dim));
                queue.clear();
                return false;
            }
        }

        AnvilChunkLoader loader = world.func_72863_F().field_73247_e instanceof AnvilChunkLoader ? (AnvilChunkLoader)world.func_72863_F().field_73247_e : null;
        if (loader != null && loader.getPendingSaveCount() > 100)
        {

            if (lastNotifcationTime < System.currentTimeMillis() - 10*1000)
            {
                listener.func_145747_a(TextComponentHelper.createComponentTranslation(listener, "commands.forge.gen.progress", total - queue.size(), total));
                lastNotifcationTime = System.currentTimeMillis();
            }
            return false;
        }

        BlockPos next = queue.poll();

        if (next != null)
        {
            // While we work we don't want to cause world load spam so pause unloading the world.
            if (keepingLoaded == null)
            {
                keepingLoaded = DimensionManager.keepDimensionLoaded(dim, true);
            }

            if (++lastNotification >= notificationFrequency || lastNotifcationTime < System.currentTimeMillis() - 60*1000)
            {
                listener.func_145747_a(TextComponentHelper.createComponentTranslation(listener, "commands.forge.gen.progress", total - queue.size(), total));
                lastNotification = 0;
                lastNotifcationTime = System.currentTimeMillis();
            }

            int x = next.func_177958_n();
            int z = next.func_177952_p();

            Chunk target = world.func_72964_e(x, z);
            Chunk[] chunks = { target };

            if (!target.func_177419_t())
            {
                // In order for a chunk to populate, The chunks around its bottom right corner need to be loaded.
                // So lets load those chunks, but this needs to be done in a certain order to make this trigger.
                // So this does load more chunks then it should, and is a hack, but lets go!.
                chunks = new Chunk[] {
                    target,
                    world.func_72964_e(x + 1, z),
                    world.func_72964_e(x + 1, z + 1),
                    world.func_72964_e(x,     z + 1),
                };
                try
                {
                    world.func_72863_F().field_73247_e.func_75816_a(world, target);
                }
                catch (IOException | MinecraftException e)
                {
                    listener.func_145747_a(TextComponentHelper.createComponentTranslation(listener, "commands.forge.gen.saveerror", e.getMessage()));
                }
                genned++;
            }

            for (Chunk chunk : chunks) //Now lets unload them. Note: Saving is done off thread so there may be cache hits, but this should still unload everything.
            {
                PlayerChunkMapEntry watchers = world.func_184164_w().func_187301_b(chunk.field_76635_g, chunk.field_76647_h);
                if (watchers == null) //If there are no players watching this, this will be null, so we can unload.
                    world.func_72863_F().func_189549_a(chunk);
            }
        }

        if (queue.size() == 0)
        {
            listener.func_145747_a(TextComponentHelper.createComponentTranslation(listener, "commands.forge.gen.complete", genned, total, dim));
            if (keepingLoaded != null && keepingLoaded)
            {
                DimensionManager.keepDimensionLoaded(dim, false);
            }
            return false;
        }
        return true;
    }
}
