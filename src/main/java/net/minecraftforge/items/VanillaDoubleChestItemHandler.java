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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VanillaDoubleChestItemHandler extends WeakReference<TileEntityChest> implements IItemHandlerModifiable
{
    // Dummy cache value to signify that we have checked and definitely found no adjacent chests
    public static final VanillaDoubleChestItemHandler NO_ADJACENT_CHESTS_INSTANCE = new VanillaDoubleChestItemHandler(null, null, false);
    private final boolean mainChestIsUpper;
    private final TileEntityChest mainChest;
    private final int hashCode;

    public VanillaDoubleChestItemHandler(@Nullable TileEntityChest mainChest, @Nullable TileEntityChest other, boolean mainChestIsUpper)
    {
        super(other);
        this.mainChest = mainChest;
        this.mainChestIsUpper = mainChestIsUpper;
        hashCode = Objects.hashCode(mainChestIsUpper ? mainChest : other) * 31 + Objects.hashCode(!mainChestIsUpper ? mainChest : other);
    }

    @Nullable
    public static VanillaDoubleChestItemHandler get(TileEntityChest chest)
    {
        World world = chest.func_145831_w();
        BlockPos pos = chest.func_174877_v();
        if (world == null || pos == null || !world.func_175667_e(pos))
            return null; // Still loading

        Block blockType = chest.func_145838_q();

        EnumFacing[] horizontals = EnumFacing.field_176754_o;
        for (int i = horizontals.length - 1; i >= 0; i--)   // Use reverse order so we can return early
        {
            EnumFacing enumfacing = horizontals[i];
            BlockPos blockpos = pos.func_177972_a(enumfacing);
            Block block = world.func_180495_p(blockpos).func_177230_c();

            if (block == blockType)
            {
                TileEntity otherTE = world.func_175625_s(blockpos);

                if (otherTE instanceof TileEntityChest)
                {
                    TileEntityChest otherChest = (TileEntityChest) otherTE;
                    return new VanillaDoubleChestItemHandler(chest, otherChest,
                            enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH);

                }
            }
        }
        return NO_ADJACENT_CHESTS_INSTANCE; //All alone
    }

    @Nullable
    public TileEntityChest getChest(boolean accessingUpper)
    {
        if (accessingUpper == mainChestIsUpper)
            return mainChest;
        else
        {
            return getOtherChest();
        }
    }

    @Nullable
    private TileEntityChest getOtherChest()
    {
        TileEntityChest tileEntityChest = get();
        return tileEntityChest != null && !tileEntityChest.func_145837_r() ? tileEntityChest : null;
    }

    @Override
    public int getSlots()
    {
        return 27 * 2;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot)
    {
        boolean accessingUpperChest = slot < 27;
        int targetSlot = accessingUpperChest ? slot : slot - 27;
        TileEntityChest chest = getChest(accessingUpperChest);
        return chest != null ? chest.func_70301_a(targetSlot) : ItemStack.field_190927_a;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack)
    {
        boolean accessingUpperChest = slot < 27;
        int targetSlot = accessingUpperChest ? slot : slot - 27;
        TileEntityChest chest = getChest(accessingUpperChest);
        if (chest != null)
        {
            IItemHandler singleHandler = chest.getSingleChestHandler();
            if (singleHandler instanceof IItemHandlerModifiable)
            {
                ((IItemHandlerModifiable) singleHandler).setStackInSlot(targetSlot, stack);
            }
        }

        chest = getChest(!accessingUpperChest);
        if (chest != null)
            chest.func_70296_d();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        boolean accessingUpperChest = slot < 27;
        int targetSlot = accessingUpperChest ? slot : slot - 27;
        TileEntityChest chest = getChest(accessingUpperChest);
        if (chest == null)
            return stack;

        int starting = stack.func_190916_E();
        ItemStack ret = chest.getSingleChestHandler().insertItem(targetSlot, stack, simulate);
        if (ret.func_190916_E() != starting && !simulate)
        {
            chest = getChest(!accessingUpperChest);
            if (chest != null)
                chest.func_70296_d();
        }

        return ret;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        boolean accessingUpperChest = slot < 27;
        int targetSlot = accessingUpperChest ? slot : slot - 27;
        TileEntityChest chest = getChest(accessingUpperChest);
        if (chest == null)
            return ItemStack.field_190927_a;

        ItemStack ret = chest.getSingleChestHandler().extractItem(targetSlot, amount, simulate);
        if (!ret.func_190926_b() && !simulate)
        {
            chest = getChest(!accessingUpperChest);
            if (chest != null)
                chest.func_70296_d();
        }

        return ret;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        boolean accessingUpperChest = slot < 27;
        return getChest(accessingUpperChest).func_70297_j_();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        boolean accessingUpperChest = slot < 27;
        int targetSlot = accessingUpperChest ? slot : slot - 27;
        TileEntityChest chest = getChest(accessingUpperChest);
        if (chest != null)
        {
            return chest.getSingleChestHandler().isItemValid(targetSlot, stack);
        }
        return true;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VanillaDoubleChestItemHandler that = (VanillaDoubleChestItemHandler) o;

        if (hashCode != that.hashCode)
            return false;

        final TileEntityChest otherChest = getOtherChest();
        if (mainChestIsUpper == that.mainChestIsUpper)
            return Objects.equal(mainChest, that.mainChest) && Objects.equal(otherChest, that.getOtherChest());
        else
            return Objects.equal(mainChest, that.getOtherChest()) && Objects.equal(otherChest, that.mainChest);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    public boolean needsRefresh()
    {
        if (this == NO_ADJACENT_CHESTS_INSTANCE)
            return false;
        TileEntityChest tileEntityChest = get();
        return tileEntityChest == null || tileEntityChest.func_145837_r();
    }
}
