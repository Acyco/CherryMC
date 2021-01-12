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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketUtil
{
    private PacketUtil() {}

    /**
     * Most ItemStack serialization is Server to Client, and must go through PacketBuffer.writeItemStack which uses Item.getNBTShareTag.
     * One exception is items from the creative menu, which must be sent from Client to Server with their full NBT.
     * <br/>
     * This method matches PacketBuffer.writeItemStack but without the Item.getNBTShareTag patch.
     * It is compatible with PacketBuffer.readItemStack.
     */
    public static void writeItemStackFromClientToServer(PacketBuffer buffer, ItemStack stack)
    {
        if (stack.func_190926_b())
        {
            buffer.writeShort(-1);
        }
        else
        {
            buffer.writeShort(Item.func_150891_b(stack.func_77973_b()));
            buffer.writeByte(stack.func_190916_E());
            buffer.writeShort(stack.func_77960_j());
            NBTTagCompound nbttagcompound = null;

            if (stack.func_77973_b().func_77645_m() || stack.func_77973_b().func_77651_p())
            {
                nbttagcompound = stack.func_77978_p();
            }

            buffer.func_150786_a(nbttagcompound);
        }
    }
}
