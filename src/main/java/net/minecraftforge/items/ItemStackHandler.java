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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public class ItemStackHandler implements IItemHandler, IItemHandlerModifiable, INBTSerializable<NBTTagCompound>
{
    protected NonNullList<ItemStack> stacks;

    public ItemStackHandler()
    {
        this(1);
    }

    public ItemStackHandler(int size)
    {
        stacks = NonNullList.func_191197_a(size, ItemStack.field_190927_a);
    }

    public ItemStackHandler(NonNullList<ItemStack> stacks)
    {
        this.stacks = stacks;
    }

    public void setSize(int size)
    {
        stacks = NonNullList.func_191197_a(size, ItemStack.field_190927_a);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots()
    {
        return stacks.size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (stack.func_190926_b())
            return ItemStack.field_190927_a;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.func_190926_b())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.func_190916_E();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.func_190916_E() > limit;

        if (!simulate)
        {
            if (existing.func_190926_b())
            {
                this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else
            {
                existing.func_190917_f(reachedLimit ? limit : stack.func_190916_E());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.func_190916_E()- limit) : ItemStack.field_190927_a;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.field_190927_a;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.func_190926_b())
            return ItemStack.field_190927_a;

        int toExtract = Math.min(amount, existing.func_77976_d());

        if (existing.func_190916_E() <= toExtract)
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemStack.field_190927_a);
                onContentsChanged(slot);
            }
            return existing;
        }
        else
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.func_190916_E() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    protected int getStackLimit(int slot, @Nonnull ItemStack stack)
    {
        return Math.min(getSlotLimit(slot), stack.func_77976_d());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).func_190926_b())
            {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.func_74768_a("Slot", i);
                stacks.get(i).func_77955_b(itemTag);
                nbtTagList.func_74742_a(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.func_74782_a("Items", nbtTagList);
        nbt.func_74768_a("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        setSize(nbt.func_150297_b("Size", Constants.NBT.TAG_INT) ? nbt.func_74762_e("Size") : stacks.size());
        NBTTagList tagList = nbt.func_150295_c("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.func_74745_c(); i++)
        {
            NBTTagCompound itemTags = tagList.func_150305_b(i);
            int slot = itemTags.func_74762_e("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, new ItemStack(itemTags));
            }
        }
        onLoad();
    }

    protected void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad()
    {

    }

    protected void onContentsChanged(int slot)
    {

    }
}
