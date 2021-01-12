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

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes the armor or hands inventory of an {@link EntityLivingBase} as an {@link IItemHandler} using {@link EntityLivingBase#getItemStackFromSlot} and
 * {@link EntityLivingBase#setItemStackToSlot}.
 */
public abstract class EntityEquipmentInvWrapper implements IItemHandlerModifiable
{
    /**
     * The entity.
     */
    protected final EntityLivingBase entity;

    /**
     * The slots exposed by this wrapper, with {@link EntityEquipmentSlot#index} as the index.
     */
    protected final List<EntityEquipmentSlot> slots;

    /**
     * @param entity   The entity.
     * @param slotType The slot type to expose.
     */
    public EntityEquipmentInvWrapper(final EntityLivingBase entity, final EntityEquipmentSlot.Type slotType)
    {
        this.entity = entity;

        final List<EntityEquipmentSlot> slots = new ArrayList<EntityEquipmentSlot>();

        for (final EntityEquipmentSlot slot : EntityEquipmentSlot.values())
        {
            if (slot.func_188453_a() == slotType)
            {
                slots.add(slot);
            }
        }

        this.slots = ImmutableList.copyOf(slots);
    }

    @Override
    public int getSlots()
    {
        return slots.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(final int slot)
    {
        return entity.func_184582_a(validateSlotIndex(slot));
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate)
    {
        if (stack.func_190926_b())
            return ItemStack.field_190927_a;

        final EntityEquipmentSlot equipmentSlot = validateSlotIndex(slot);

        final ItemStack existing = entity.func_184582_a(equipmentSlot);

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
                entity.func_184201_a(equipmentSlot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else
            {
                existing.func_190917_f(reachedLimit ? limit : stack.func_190916_E());
            }
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.func_190916_E() - limit) : ItemStack.field_190927_a;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate)
    {
        if (amount == 0)
            return ItemStack.field_190927_a;

        final EntityEquipmentSlot equipmentSlot = validateSlotIndex(slot);

        final ItemStack existing = entity.func_184582_a(equipmentSlot);

        if (existing.func_190926_b())
            return ItemStack.field_190927_a;

        final int toExtract = Math.min(amount, existing.func_77976_d());

        if (existing.func_190916_E() <= toExtract)
        {
            if (!simulate)
            {
                entity.func_184201_a(equipmentSlot, ItemStack.field_190927_a);
            }

            return existing;
        }
        else
        {
            if (!simulate)
            {
                entity.func_184201_a(equipmentSlot, ItemHandlerHelper.copyStackWithSize(existing, existing.func_190916_E() - toExtract));
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(final int slot)
    {
        final EntityEquipmentSlot equipmentSlot = validateSlotIndex(slot);
        return equipmentSlot.func_188453_a() == EntityEquipmentSlot.Type.ARMOR ? 1 : 64;
    }

    protected int getStackLimit(final int slot, @Nonnull final ItemStack stack)
    {
        return Math.min(getSlotLimit(slot), stack.func_77976_d());
    }

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack)
    {
        final EntityEquipmentSlot equipmentSlot = validateSlotIndex(slot);
        if (ItemStack.func_77989_b(entity.func_184582_a(equipmentSlot), stack))
            return;
        entity.func_184201_a(equipmentSlot, stack);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return IItemHandlerModifiable.super.isItemValid(slot, stack);
    }

    protected EntityEquipmentSlot validateSlotIndex(final int slot)
    {
        if (slot < 0 || slot >= slots.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");

        return slots.get(slot);
    }
}
