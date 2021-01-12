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

package net.minecraftforge.fluids;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A universal bucket that can hold any liquid
 */
public class UniversalBucket extends Item
{

    private final int capacity; // how much the bucket holds
    @Nonnull
    private final ItemStack empty; // empty item to return and recognize when filling
    private final boolean nbtSensitive;

    public UniversalBucket()
    {
        this(Fluid.BUCKET_VOLUME, new ItemStack(Items.field_151133_ar), false);
    }

    /**
     * @param capacity        Capacity of the container
     * @param empty           Item used for filling with the bucket event and returned when emptied
     * @param nbtSensitive    Whether the empty item is NBT sensitive (usually true if empty and full are the same items)
     */
    public UniversalBucket(int capacity, @Nonnull ItemStack empty, boolean nbtSensitive)
    {
        this.capacity = capacity;
        this.empty = empty;
        this.nbtSensitive = nbtSensitive;

        this.func_77625_d(1);

        this.func_77637_a(CreativeTabs.field_78026_f);

        BlockDispenser.field_149943_a.func_82595_a(this, DispenseFluidContainer.getInstance());
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack)
    {
        return !getEmpty().func_190926_b();
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack)
    {
        if (!getEmpty().func_190926_b())
        {
            // Create a copy such that the game can't mess with it
            return getEmpty().func_77946_l();
        }
        return super.getContainerItem(itemStack);
    }

    @Override
    public void func_150895_a(@Nullable CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems)
    {
        if (!this.func_194125_a(tab))
            return;
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values())
        {
            if (fluid != FluidRegistry.WATER && fluid != FluidRegistry.LAVA && !fluid.getName().equals("milk"))
            {
                // add all fluids that the bucket can be filled  with
                FluidStack fs = new FluidStack(fluid, getCapacity());
                ItemStack stack = new ItemStack(this);
                IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
                if (fluidHandler.fill(fs, true) == fs.amount)
                {
                    ItemStack filled = fluidHandler.getContainer();
                    subItems.add(filled);
                }
            }
        }
    }

    @Override
    @Nonnull
    public String func_77653_i(@Nonnull ItemStack stack)
    {
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null)
        {
            if(!getEmpty().func_190926_b())
            {
                return getEmpty().func_82833_r();
            }
            return super.func_77653_i(stack);
        }

        String unloc = this.func_77657_g(stack);

        if (I18n.func_94522_b(unloc + "." + fluidStack.getFluid().getName()))
        {
            return I18n.func_74838_a(unloc + "." + fluidStack.getFluid().getName());
        }

        return I18n.func_74837_a(unloc + ".name", fluidStack.getLocalizedName());
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> func_77659_a(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand)
    {
        ItemStack itemstack = player.func_184586_b(hand);
        FluidStack fluidStack = getFluid(itemstack);
        // empty bucket shouldn't exist, do nothing since it should be handled by the bucket event
        if (fluidStack == null)
        {
            return ActionResult.newResult(EnumActionResult.PASS, itemstack);
        }

        // clicked on a block?
        RayTraceResult mop = this.func_77621_a(world, player, false);

        ActionResult<ItemStack> ret = ForgeEventFactory.onBucketUse(player, world, itemstack, mop);
        if (ret != null) return ret;

        if(mop == null || mop.field_72313_a != RayTraceResult.Type.BLOCK)
        {
            return ActionResult.newResult(EnumActionResult.PASS, itemstack);
        }

        BlockPos clickPos = mop.func_178782_a();
        // can we place liquid there?
        if (world.func_175660_a(player, clickPos))
        {
            // the block adjacent to the side we clicked on
            BlockPos targetPos = clickPos.func_177972_a(mop.field_178784_b);

            // can the player place there?
            if (player.func_175151_a(targetPos, mop.field_178784_b, itemstack))
            {
                // try placing liquid
                FluidActionResult result = FluidUtil.tryPlaceFluid(player, world, targetPos, itemstack, fluidStack);
                if (result.isSuccess() && !player.field_71075_bZ.field_75098_d)
                {
                    // success!
                    player.func_71029_a(StatList.func_188057_b(this));

                    itemstack.func_190918_g(1);
                    ItemStack drained = result.getResult();
                    ItemStack emptyStack = !drained.func_190926_b() ? drained.func_77946_l() : new ItemStack(this);

                    // check whether we replace the item or add the empty one to the inventory
                    if (itemstack.func_190926_b())
                    {
                        return ActionResult.newResult(EnumActionResult.SUCCESS, emptyStack);
                    }
                    else
                    {
                        // add empty bucket to player inventory
                        ItemHandlerHelper.giveItemToPlayer(player, emptyStack);
                        return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
                    }
                }
            }
        }

        // couldn't place liquid there2
        return ActionResult.newResult(EnumActionResult.FAIL, itemstack);
    }

    @SubscribeEvent(priority = EventPriority.LOW) // low priority so other mods can handle their stuff first
    public void onFillBucket(FillBucketEvent event)
    {
        if (event.getResult() != Event.Result.DEFAULT)
        {
            // event was already handled
            return;
        }

        // not for us to handle
        ItemStack emptyBucket = event.getEmptyBucket();
        if (emptyBucket.func_190926_b() ||
                !emptyBucket.func_77969_a(getEmpty()) ||
                (isNbtSensitive() && ItemStack.func_77970_a(emptyBucket, getEmpty())))
        {
            return;
        }

        // needs to target a block
        RayTraceResult target = event.getTarget();
        if (target == null || target.field_72313_a != RayTraceResult.Type.BLOCK)
        {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = target.func_178782_a();

        ItemStack singleBucket = emptyBucket.func_77946_l();
        singleBucket.func_190920_e(1);

        FluidActionResult filledResult = FluidUtil.tryPickUpFluid(singleBucket, event.getEntityPlayer(), world, pos, target.field_178784_b);
        if (filledResult.isSuccess())
        {
            event.setResult(Event.Result.ALLOW);
            event.setFilledBucket(filledResult.getResult());
        }
        else
        {
            // cancel event, otherwise the vanilla minecraft ItemBucket would
            // convert it into a water/lava bucket depending on the blocks material
            event.setCanceled(true);
        }
    }

    /**
     * @deprecated use the NBT-sensitive version {@link FluidUtil#getFilledBucket(FluidStack)}
     */
    @Deprecated
    @Nonnull
    public static ItemStack getFilledBucket(@Nonnull UniversalBucket item, Fluid fluid)
    {
        return FluidUtil.getFilledBucket(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
    }

    @Nullable
    public FluidStack getFluid(@Nonnull ItemStack container)
    {
        return FluidStack.loadFluidStackFromNBT(container.func_77978_p());
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Nonnull
    public ItemStack getEmpty()
    {
        return empty;
    }

    public boolean isNbtSensitive()
    {
        return nbtSensitive;
    }

    @Nullable
    @Override
    public String getCreatorModId(@Nonnull ItemStack itemStack)
    {
        FluidStack fluidStack = getFluid(itemStack);
        String modId = FluidRegistry.getModId(fluidStack);
        return modId != null ? modId : super.getCreatorModId(itemStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, NBTTagCompound nbt)
    {
        return new FluidBucketWrapper(stack);
    }
}
