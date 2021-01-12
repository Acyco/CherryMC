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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage.OpenGui;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OpenGuiHandler extends SimpleChannelInboundHandler<OpenGui> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final OpenGui msg) throws Exception
    {
        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
        if (thread.func_152345_ab())
        {
            process(msg);
        }
        else
        {
            thread.func_152344_a(() -> OpenGuiHandler.this.process(msg));
        }
    }

    private void process(OpenGui msg)
    {
        EntityPlayer player = FMLClientHandler.instance().getClient().field_71439_g;
        player.openGui(msg.modId, msg.modGuiId, player.field_70170_p, msg.x, msg.y, msg.z);
        player.field_71070_bA.field_75152_c = msg.windowId;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        FMLLog.log.error("OpenGuiHandler exception", cause);
        super.exceptionCaught(ctx, cause);
    }

}
