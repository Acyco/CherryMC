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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotItemHandler extends Slot
{
    private static IInventory emptyInventory = new InventoryBasic("[Null]", true, 0);
    private final IItemHandler itemHandler;
    private final int index;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean func_75214_a(@Nonnull ItemStack stack)
    {
        if (stack.func_190926_b() || !itemHandler.isItemValid(index, stack))
            return false;

        IItemHandler handler = this.getItemHandler();
        ItemStack remainder;
        if (handler instanceof IItemHandlerModifiable)
        {
            IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;
            ItemStack currentStack = handlerModifiable.getStackInSlot(index);

            handlerModifiable.setStackInSlot(index, ItemStack.field_190927_a);

            remainder = handlerModifiable.insertItem(index, stack, true);

            handlerModifiable.setStackInSlot(index, currentStack);
        }
        else
        {
            remainder = handler.insertItem(index, stack, true);
        }
        return remainder.func_190916_E() < stack.func_190916_E();
    }

    @Override
    @Nonnull
    public ItemStack func_75211_c()
    {
        return this.getItemHandler().getStackInSlot(index);
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    @Override
    public void func_75215_d(@Nonnull ItemStack stack)
    {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, stack);
        this.func_75218_e();
    }

    @Override
    public void func_75220_a(@Nonnull ItemStack p_75220_1_, @Nonnull ItemStack p_75220_2_)
    {

    }

    @Override
    public int func_75219_a()
    {
        return this.itemHandler.getSlotLimit(this.index);
    }

    @Override
    public int func_178170_b(@Nonnull ItemStack stack)
    {
        ItemStack maxAdd = stack.func_77946_l();
        int maxInput = stack.func_77976_d();
        maxAdd.func_190920_e(maxInput);

        IItemHandler handler = this.getItemHandler();
        ItemStack currentStack = handler.getStackInSlot(index);
        if (handler instanceof IItemHandlerModifiable) {
            IItemHandlerModifiable handlerModifiable = (IItemHandlerModifiable) handler;

            handlerModifiable.setStackInSlot(index, ItemStack.field_190927_a);

            ItemStack remainder = handlerModifiable.insertItem(index, maxAdd, true);

            handlerModifiable.setStackInSlot(index, currentStack);

            return maxInput - remainder.func_190916_E();
        }
        else
        {
            ItemStack remainder = handler.insertItem(index, maxAdd, true);

            int current = currentStack.func_190916_E();
            int added = maxInput - remainder.func_190916_E();
            return current + added;
        }
    }

    @Override
    public boolean func_82869_a(EntityPlayer playerIn)
    {
        return !this.getItemHandler().extractItem(index, 1, true).func_190926_b();
    }

    @Override
    @Nonnull
    public ItemStack func_75209_a(int amount)
    {
        return this.getItemHandler().extractItem(index, amount, false);
    }

    public IItemHandler getItemHandler()
    {
        return itemHandler;
    }

    @Override
    public boolean isSameInventory(Slot other)
    {
        return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
    }
}
