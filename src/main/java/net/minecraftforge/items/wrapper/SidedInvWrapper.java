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

package net.minecraftforge.items.wrapper;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SidedInvWrapper implements IItemHandlerModifiable
{
    protected final ISidedInventory inv;
    protected final EnumFacing side;

    public SidedInvWrapper(ISidedInventory inv, EnumFacing side)
    {
        this.inv = inv;
        this.side = side;
    }

    public static int getSlot(ISidedInventory inv, int slot, EnumFacing side)
    {
        int[] slots = inv.func_180463_a(side);
        if (slot < slots.length)
            return slots[slot];
        return -1;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SidedInvWrapper that = (SidedInvWrapper) o;

        return inv.equals(that.inv) && side == that.side;
    }

    @Override
    public int hashCode()
    {
        int result = inv.hashCode();
        result = 31 * result + Objects.hashCode(side);
        return result;
    }

    @Override
    public int getSlots()
    {
        return inv.func_180463_a(side).length;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        int i = getSlot(inv, slot, side);
        return i == -1 ? ItemStack.field_190927_a : inv.func_70301_a(i);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.func_190926_b())
            return ItemStack.field_190927_a;

        int slot1 = getSlot(inv, slot, side);

        if (slot1 == -1)
            return stack;

        ItemStack stackInSlot = inv.func_70301_a(slot1);

        int m;
        if (!stackInSlot.func_190926_b())
        {
            if (stackInSlot.func_190916_E() >= Math.min(stackInSlot.func_77976_d(), getSlotLimit(slot)))
                return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            if (!inv.func_180462_a(slot1, stack, side) || !inv.func_94041_b(slot1, stack))
                return stack;

            m = Math.min(stack.func_77976_d(), getSlotLimit(slot)) - stackInSlot.func_190916_E();

            if (stack.func_190916_E() <= m)
            {
                if (!simulate)
                {
                    ItemStack copy = stack.func_77946_l();
                    copy.func_190917_f(stackInSlot.func_190916_E());
                    setInventorySlotContents(slot1, copy);
                }

                return ItemStack.field_190927_a;
            }
            else
            {
                // copy the stack to not modify the original one
                stack = stack.func_77946_l();
                if (!simulate)
                {
                    ItemStack copy = stack.func_77979_a(m);
                    copy.func_190917_f(stackInSlot.func_190916_E());
                    setInventorySlotContents(slot1, copy);
                    return stack;
                }
                else
                {
                    stack.func_190918_g(m);
                    return stack;
                }
            }
        }
        else
        {
            if (!inv.func_180462_a(slot1, stack, side) || !inv.func_94041_b(slot1, stack))
                return stack;

            m = Math.min(stack.func_77976_d(), getSlotLimit(slot));
            if (m < stack.func_190916_E())
            {
                // copy the stack to not modify the original one
                stack = stack.func_77946_l();
                if (!simulate)
                {
                    setInventorySlotContents(slot1, stack.func_77979_a(m));
                    return stack;
                }
                else
                {
                    stack.func_190918_g(m);
                    return stack;
                }
            }
            else
            {
                if (!simulate)
                    setInventorySlotContents(slot1, stack);
                return ItemStack.field_190927_a;
            }
        }

    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        int slot1 = getSlot(inv, slot, side);

        if (slot1 != -1)
            setInventorySlotContents(slot1, stack);
    }

    private void setInventorySlotContents(int slot, ItemStack stack) {
      inv.func_70296_d(); //Notify vanilla of updates, We change the handler to be responsible for this instead of the caller. So mimic vanilla behavior
      inv.func_70299_a(slot, stack);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.field_190927_a;

        int slot1 = getSlot(inv, slot, side);

        if (slot1 == -1)
            return ItemStack.field_190927_a;

        ItemStack stackInSlot = inv.func_70301_a(slot1);

        if (stackInSlot.func_190926_b())
            return ItemStack.field_190927_a;

        if (!inv.func_180461_b(slot1, stackInSlot, side))
            return ItemStack.field_190927_a;

        if (simulate)
        {
            if (stackInSlot.func_190916_E() < amount)
            {
                return stackInSlot.func_77946_l();
            }
            else
            {
                ItemStack copy = stackInSlot.func_77946_l();
                copy.func_190920_e(amount);
                return copy;
            }
        }
        else
        {
            int m = Math.min(stackInSlot.func_190916_E(), amount);
            ItemStack ret = inv.func_70298_a(slot1, m);
            inv.func_70296_d();
            return ret;
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return inv.func_70297_j_();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        int slot1 = getSlot(inv, slot, side);
        return slot1 == -1 ? false : inv.func_94041_b(slot1, stack);
    }
}
