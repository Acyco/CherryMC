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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class InvWrapper implements IItemHandlerModifiable
{
    private final IInventory inv;

    public InvWrapper(IInventory inv)
    {
        this.inv = inv;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InvWrapper that = (InvWrapper) o;

        return getInv().equals(that.getInv());

    }

    @Override
    public int hashCode()
    {
        return getInv().hashCode();
    }

    @Override
    public int getSlots()
    {
        return getInv().func_70302_i_();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        return getInv().func_70301_a(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.func_190926_b())
            return ItemStack.field_190927_a;

        ItemStack stackInSlot = getInv().func_70301_a(slot);

        int m;
        if (!stackInSlot.func_190926_b())
        {
            if (stackInSlot.func_190916_E() >= Math.min(stackInSlot.func_77976_d(), getSlotLimit(slot)))
                return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            if (!getInv().func_94041_b(slot, stack))
                return stack;

            m = Math.min(stack.func_77976_d(), getSlotLimit(slot)) - stackInSlot.func_190916_E();

            if (stack.func_190916_E() <= m)
            {
                if (!simulate)
                {
                    ItemStack copy = stack.func_77946_l();
                    copy.func_190917_f(stackInSlot.func_190916_E());
                    getInv().func_70299_a(slot, copy);
                    getInv().func_70296_d();
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
                    getInv().func_70299_a(slot, copy);
                    getInv().func_70296_d();
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
            if (!getInv().func_94041_b(slot, stack))
                return stack;

            m = Math.min(stack.func_77976_d(), getSlotLimit(slot));
            if (m < stack.func_190916_E())
            {
                // copy the stack to not modify the original one
                stack = stack.func_77946_l();
                if (!simulate)
                {
                    getInv().func_70299_a(slot, stack.func_77979_a(m));
                    getInv().func_70296_d();
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
                {
                    getInv().func_70299_a(slot, stack);
                    getInv().func_70296_d();
                }
                return ItemStack.field_190927_a;
            }
        }

    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.field_190927_a;

        ItemStack stackInSlot = getInv().func_70301_a(slot);

        if (stackInSlot.func_190926_b())
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

            ItemStack decrStackSize = getInv().func_70298_a(slot, m);
            getInv().func_70296_d();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        getInv().func_70299_a(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return getInv().func_70297_j_();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return getInv().func_94041_b(slot, stack);
    }

    public IInventory getInv()
    {
        return inv;
    }
}
