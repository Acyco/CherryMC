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

package net.minecraftforge.fml.common.network.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTracker;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntityMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;

public class EntitySpawnHandler extends SimpleChannelInboundHandler<EntityMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final EntityMessage msg) throws Exception
    {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
        if (thread.func_152345_ab())
        {
            process(msg);
        }
        else
        {
            thread.func_152344_a(() -> EntitySpawnHandler.this.process(msg));
        }
    }

    private void process(EntityMessage msg)
    {
        if (msg.getClass().equals(FMLMessage.EntitySpawnMessage.class))
        {
            FMLMessage.EntitySpawnMessage spawnMsg = (FMLMessage.EntitySpawnMessage) msg;
            spawnEntity(spawnMsg);
            spawnMsg.dataStream.release();
        }
    }

    private void spawnEntity(FMLMessage.EntitySpawnMessage spawnMsg)
    {
        ModContainer mc = Loader.instance().getIndexedModList().get(spawnMsg.modId);
        EntityRegistration er = EntityRegistry.instance().lookupModSpawn(mc, spawnMsg.modEntityTypeId);
        if (er == null)
        {
            throw new RuntimeException( "Could not spawn mod entity ModID: " + spawnMsg.modId + " EntityID: " + spawnMsg.modEntityTypeId +
                    " at ( " + spawnMsg.rawX + "," + spawnMsg.rawY + ", " + spawnMsg.rawZ + ") Please contact mod author or server admin.");
        }
        WorldClient wc = FMLClientHandler.instance().getWorldClient();
        try
        {
            Entity entity;
            if (er.hasCustomSpawning())
            {
                entity = er.doCustomSpawning(spawnMsg);
            } else
            {
                entity = er.newInstance(wc);

                int offset = spawnMsg.entityId - entity.func_145782_y();
                entity.func_145769_d(spawnMsg.entityId);
                entity.func_184221_a(spawnMsg.entityUUID);
                entity.func_70012_b(spawnMsg.rawX, spawnMsg.rawY, spawnMsg.rawZ, spawnMsg.scaledYaw, spawnMsg.scaledPitch);
                if (entity instanceof EntityLiving)
                {
                    ((EntityLiving) entity).field_70759_as = spawnMsg.scaledHeadYaw;
                }

                Entity parts[] = entity.func_70021_al();
                if (parts != null)
                {
                    for (int j = 0; j < parts.length; j++)
                    {
                        parts[j].func_145769_d(parts[j].func_145782_y() + offset);
                    }
                }
            }

            EntityTracker.func_187254_a(entity, spawnMsg.rawX, spawnMsg.rawY, spawnMsg.rawZ);

            EntityPlayerSP clientPlayer = FMLClientHandler.instance().getClientPlayerEntity();
            if (entity instanceof IThrowableEntity)
            {
                Entity thrower = clientPlayer.func_145782_y() == spawnMsg.throwerId ? clientPlayer : wc.func_73045_a(spawnMsg.throwerId);
                ((IThrowableEntity) entity).setThrower(thrower);
            }

            if (spawnMsg.dataWatcherList != null)
            {
                entity.func_184212_Q().func_187218_a(spawnMsg.dataWatcherList);
            }

            if (spawnMsg.throwerId > 0)
            {
                entity.func_70016_h(spawnMsg.speedScaledX, spawnMsg.speedScaledY, spawnMsg.speedScaledZ);
            }

            if (entity instanceof IEntityAdditionalSpawnData)
            {
                ((IEntityAdditionalSpawnData) entity).readSpawnData(spawnMsg.dataStream);
            }
            wc.func_73027_a(spawnMsg.entityId, entity);
        }
        catch (Exception e)
        {
            throw new RuntimeException("A severe problem occurred during the spawning of an entity at (" + spawnMsg.rawX + ", " + spawnMsg.rawY + ", " + spawnMsg.rawZ + ")", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log.error("EntitySpawnHandler exception", cause);
        super.exceptionCaught(ctx, cause);
    }
}
