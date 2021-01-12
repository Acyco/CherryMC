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
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockHopper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VanillaInventoryCodeHooks
{
    /**
     * Copied from TileEntityHopper#captureDroppedItems and added capability support
     * @return Null if we did nothing {no IItemHandler}, True if we moved an item, False if we moved no items
     */
    @Nullable
    public static Boolean extractHook(IHopper dest)
    {
        Pair<IItemHandler, Object> itemHandlerResult = getItemHandler(dest, EnumFacing.UP);
        if (itemHandlerResult == null)
            return null;

        IItemHandler handler = itemHandlerResult.getKey();

        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack extractItem = handler.extractItem(i, 1, true);
            if (!extractItem.func_190926_b())
            {
                for (int j = 0; j < dest.func_70302_i_(); j++)
                {
                    ItemStack destStack = dest.func_70301_a(j);
                    if (dest.func_94041_b(j, extractItem) && (destStack.func_190926_b() || destStack.func_190916_E() < destStack.func_77976_d() && destStack.func_190916_E() < dest.func_70297_j_() && ItemHandlerHelper.canItemStacksStack(extractItem, destStack)))
                    {
                        extractItem = handler.extractItem(i, 1, false);
                        if (destStack.func_190926_b())
                            dest.func_70299_a(j, extractItem);
                        else
                        {
                            destStack.func_190917_f(1);
                            dest.func_70299_a(j, destStack);
                        }
                        dest.func_70296_d();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Copied from BlockDropper#dispense and added capability support
     */
    public static boolean dropperInsertHook(World world, BlockPos pos, TileEntityDispenser dropper, int slot, @Nonnull ItemStack stack)
    {
        EnumFacing enumfacing = world.func_180495_p(pos).func_177229_b(BlockDropper.field_176441_a);
        BlockPos blockpos = pos.func_177972_a(enumfacing);
        Pair<IItemHandler, Object> destinationResult = getItemHandler(world, (double) blockpos.func_177958_n(), (double) blockpos.func_177956_o(), (double) blockpos.func_177952_p(), enumfacing.func_176734_d());
        if (destinationResult == null)
        {
            return true;
        }
        else
        {
            IItemHandler itemHandler = destinationResult.getKey();
            Object destination = destinationResult.getValue();
            ItemStack dispensedStack = stack.func_77946_l().func_77979_a(1);
            ItemStack remainder = putStackInInventoryAllSlots(dropper, destination, itemHandler, dispensedStack);

            if (remainder.func_190926_b())
            {
                remainder = stack.func_77946_l();
                remainder.func_190918_g(1);
            }
            else
            {
                remainder = stack.func_77946_l();
            }

            dropper.func_70299_a(slot, remainder);
            return false;
        }
    }

    /**
     * Copied from TileEntityHopper#transferItemsOut and added capability support
     */
    public static boolean insertHook(TileEntityHopper hopper)
    {
        EnumFacing hopperFacing = BlockHopper.func_176428_b(hopper.func_145832_p());
        Pair<IItemHandler, Object> destinationResult = getItemHandler(hopper, hopperFacing);
        if (destinationResult == null)
        {
            return false;
        }
        else
        {
            IItemHandler itemHandler = destinationResult.getKey();
            Object destination = destinationResult.getValue();
            if (isFull(itemHandler))
            {
                return false;
            }
            else
            {
                for (int i = 0; i < hopper.func_70302_i_(); ++i)
                {
                    if (!hopper.func_70301_a(i).func_190926_b())
                    {
                        ItemStack originalSlotContents = hopper.func_70301_a(i).func_77946_l();
                        ItemStack insertStack = hopper.func_70298_a(i, 1);
                        ItemStack remainder = putStackInInventoryAllSlots(hopper, destination, itemHandler, insertStack);

                        if (remainder.func_190926_b())
                        {
                            return true;
                        }

                        hopper.func_70299_a(i, originalSlotContents);
                    }
                }

                return false;
            }
        }
    }

    private static ItemStack putStackInInventoryAllSlots(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack)
    {
        for (int slot = 0; slot < destInventory.getSlots() && !stack.func_190926_b(); slot++)
        {
            stack = insertStack(source, destination, destInventory, stack, slot);
        }
        return stack;
    }

    /**
     * Copied from TileEntityHopper#insertStack and added capability support
     */
    private static ItemStack insertStack(TileEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot)
    {
        ItemStack itemstack = destInventory.getStackInSlot(slot);

        if (destInventory.insertItem(slot, stack, true).func_190926_b())
        {
            boolean insertedItem = false;
            boolean inventoryWasEmpty = isEmpty(destInventory);

            if (itemstack.func_190926_b())
            {
                destInventory.insertItem(slot, stack, false);
                stack = ItemStack.field_190927_a;
                insertedItem = true;
            }
            else if (ItemHandlerHelper.canItemStacksStack(itemstack, stack))
            {
                int originalSize = stack.func_190916_E();
                stack = destInventory.insertItem(slot, stack, false);
                insertedItem = originalSize < stack.func_190916_E();
            }

            if (insertedItem)
            {
                if (inventoryWasEmpty && destination instanceof TileEntityHopper)
                {
                    TileEntityHopper destinationHopper = (TileEntityHopper)destination;

                    if (!destinationHopper.func_174914_o())
                    {
                        int k = 0;

                        if (source instanceof TileEntityHopper)
                        {
                            if (destinationHopper.getLastUpdateTime() >= ((TileEntityHopper) source).getLastUpdateTime())
                            {
                                k = 1;
                            }
                        }

                        destinationHopper.func_145896_c(8 - k);
                    }
                }
            }
        }

        return stack;
    }

    @Nullable
    private static Pair<IItemHandler, Object> getItemHandler(IHopper hopper, EnumFacing hopperFacing)
    {
        double x = hopper.func_96107_aA() + (double) hopperFacing.func_82601_c();
        double y = hopper.func_96109_aB() + (double) hopperFacing.func_96559_d();
        double z = hopper.func_96108_aC() + (double) hopperFacing.func_82599_e();
        return getItemHandler(hopper.func_145831_w(), x, y, z, hopperFacing.func_176734_d());
    }

    private static boolean isFull(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.func_190926_b() || stackInSlot.func_190916_E() != stackInSlot.func_77976_d())
            {
                return false;
            }
        }
        return true;
    }

    private static boolean isEmpty(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.func_190916_E() > 0)
            {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static Pair<IItemHandler, Object> getItemHandler(World worldIn, double x, double y, double z, final EnumFacing side)
    {
        Pair<IItemHandler, Object> destination = null;
        int i = MathHelper.func_76128_c(x);
        int j = MathHelper.func_76128_c(y);
        int k = MathHelper.func_76128_c(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        net.minecraft.block.state.IBlockState state = worldIn.func_180495_p(blockpos);
        Block block = state.func_177230_c();

        if (block.hasTileEntity(state))
        {
            TileEntity tileentity = worldIn.func_175625_s(blockpos);
            if (tileentity != null)
            {
                if (tileentity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
                {
                    IItemHandler capability = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                    destination = ImmutablePair.<IItemHandler, Object>of(capability, tileentity);
                }
            }
        }

        return destination;
    }
}
