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

package net.minecraftforge.common.util;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

//Preliminary, simple Fake Player class
public class FakePlayer extends EntityPlayerMP
{
    public FakePlayer(WorldServer world, GameProfile name)
    {
        super(FMLCommonHandler.instance().getMinecraftServerInstance(), world, name, new PlayerInteractionManager(world));
    }

    @Override public Vec3d func_174791_d(){ return new Vec3d(0, 0, 0); }
    @Override public boolean func_70003_b(int i, String s){ return false; }
    @Override public void func_146105_b(ITextComponent chatComponent, boolean actionBar){}
    @Override public void func_145747_a(ITextComponent component) {}
    @Override public void func_71064_a(StatBase par1StatBase, int par2){}
    @Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
    @Override public boolean func_180431_b(DamageSource source){ return true; }
    @Override public boolean func_96122_a(EntityPlayer player){ return false; }
    @Override public void func_70645_a(DamageSource source){ return; }
    @Override public void func_70071_h_(){ return; }
    @Override public Entity changeDimension(int dim, ITeleporter teleporter){ return this; }
    @Override public void func_147100_a(CPacketClientSettings pkt){ return; }
    @Override @Nullable public MinecraftServer func_184102_h() { return FMLCommonHandler.instance().getMinecraftServerInstance(); }
}
