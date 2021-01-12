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

package net.minecraftforge.items;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemHandlerHelper
{
    @Nonnull
    public static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate)
    {
        if (dest == null || stack.func_190926_b())
            return stack;

        for (int i = 0; i < dest.getSlots(); i++)
        {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.func_190926_b())
            {
                return ItemStack.field_190927_a;
            }
        }

        return stack;
    }

    public static boolean canItemStacksStack(@Nonnull ItemStack a, @Nonnull ItemStack b)
    {
        if (a.func_190926_b() || !a.func_77969_a(b) || a.func_77942_o() != b.func_77942_o())
            return false;

        return (!a.func_77942_o() || a.func_77978_p().equals(b.func_77978_p())) && a.areCapsCompatible(b);
    }

    /**
     * A relaxed version of canItemStacksStack that stacks itemstacks with different metadata if they don't have subtypes.
     * This usually only applies when players pick up items.
     */
    public static boolean canItemStacksStackRelaxed(@Nonnull ItemStack a, @Nonnull ItemStack b)
    {
        if (a.func_190926_b() || b.func_190926_b() || a.func_77973_b() != b.func_77973_b())
            return false;

        if (!a.func_77985_e())
            return false;

        // Metadata value only matters when the item has subtypes
        // Vanilla stacks non-subtype items with different metadata together
        // e.g. a stick with metadata 0 and a stick with metadata 1 stack
        if (a.func_77981_g() && a.func_77960_j() != b.func_77960_j())
            return false;

        if (a.func_77942_o() != b.func_77942_o())
            return false;

        return (!a.func_77942_o() || a.func_77978_p().equals(b.func_77978_p())) && a.areCapsCompatible(b);
    }

    @Nonnull
    public static ItemStack copyStackWithSize(@Nonnull ItemStack itemStack, int size)
    {
        if (size == 0)
            return ItemStack.field_190927_a;
        ItemStack copy = itemStack.func_77946_l();
        copy.func_190920_e(size);
        return copy;
    }

    /**
     * Inserts the ItemStack into the inventory, filling up already present stacks first.
     * This is equivalent to the behaviour of a player picking up an item.
     * Note: This function stacks items without subtypes with different metadata together.
     */
    @Nonnull
    public static ItemStack insertItemStacked(IItemHandler inventory, @Nonnull ItemStack stack, boolean simulate)
    {
        if (inventory == null || stack.func_190926_b())
            return stack;

        // not stackable -> just insert into a new slot
        if (!stack.func_77985_e())
        {
            return insertItem(inventory, stack, simulate);
        }

        int sizeInventory = inventory.getSlots();

        // go through the inventory and try to fill up already existing items
        for (int i = 0; i < sizeInventory; i++)
        {
            ItemStack slot = inventory.getStackInSlot(i);
            if (canItemStacksStackRelaxed(slot, stack))
            {
                stack = inventory.insertItem(i, stack, simulate);

                if (stack.func_190926_b())
                {
                    break;
                }
            }
        }

        // insert remainder into empty slots
        if (!stack.func_190926_b())
        {
            // find empty slot
            for (int i = 0; i < sizeInventory; i++)
            {
                if (inventory.getStackInSlot(i).func_190926_b())
                {
                    stack = inventory.insertItem(i, stack, simulate);
                    if (stack.func_190926_b())
                    {
                        break;
                    }
                }
            }
        }

        return stack;
    }

    /** giveItemToPlayer without preferred slot */
    public static void giveItemToPlayer(EntityPlayer player, @Nonnull ItemStack stack) {
        giveItemToPlayer(player, stack, -1);
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     */
    public static void giveItemToPlayer(EntityPlayer player, @Nonnull ItemStack stack, int preferredSlot)
    {
        if (stack.func_190926_b()) return;

        IItemHandler inventory = new PlayerMainInvWrapper(player.field_71071_by);
        World world = player.field_70170_p;

        // try adding it into the inventory
        ItemStack remainder = stack;
        // insert into preferred slot first
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots())
        {
            remainder = inventory.insertItem(preferredSlot, stack, false);
        }
        // then into the inventory in general
        if (!remainder.func_190926_b())
        {
            remainder = insertItemStacked(inventory, remainder, false);
        }

        // play sound if something got picked up
        if (remainder.func_190926_b() || remainder.func_190916_E() != stack.func_190916_E())
        {
            world.func_184148_a(null, player.field_70165_t, player.field_70163_u + 0.5, player.field_70161_v,
                    SoundEvents.field_187638_cR, SoundCategory.PLAYERS, 0.2F, ((world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        // drop remaining itemstack into the world
        if (!remainder.func_190926_b() && !world.field_72995_K)
        {
            EntityItem entityitem = new EntityItem(world, player.field_70165_t, player.field_70163_u + 0.5, player.field_70161_v, remainder);
            entityitem.func_174867_a(40);
            entityitem.field_70159_w = 0;
            entityitem.field_70179_y = 0;

            world.func_72838_d(entityitem);
        }
    }

    /**
     * This method uses the standard vanilla algorithm to calculate a comparator output for how "full" the inventory is.
     * This method is an adaptation of Container#calcRedstoneFromInventory(IInventory).
     * @param inv The inventory handler to test.
     * @return A redstone value in the range [0,15] representing how "full" this inventory is.
     */
    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv)
    {
        if (inv == null)
        {
            return 0;
        }
        else
        {
            int itemsFound = 0;
            float proportion = 0.0F;

            for (int j = 0; j < inv.getSlots(); ++j)
            {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (!itemstack.func_190926_b())
                {
                    proportion += (float)itemstack.func_190916_E() / (float)Math.min(inv.getSlotLimit(j), itemstack.func_77976_d());
                    ++itemsFound;
                }
            }

            proportion = proportion / (float)inv.getSlots();
            return MathHelper.func_76141_d(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }
}
