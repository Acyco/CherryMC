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

import java.util.Random;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/**
 * This is a cellular-automata based finite fluid block implementation.
 *
 * It is highly recommended that you use/extend this class for finite fluid blocks.
 *
 */
public class BlockFluidFinite extends BlockFluidBase
{
    public BlockFluidFinite(Fluid fluid, Material material, MapColor mapColor)
    {
        super(fluid, material, mapColor);
    }

    public BlockFluidFinite(Fluid fluid, Material material)
    {
        this(fluid, material, material.func_151565_r());
    }

    @Override
    public int getQuantaValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        if (state.func_177230_c().isAir(state, world, pos))
        {
            return 0;
        }

        if (state.func_177230_c() != this)
        {
            return -1;
        }
        return state.func_177229_b(LEVEL) + 1;
    }

    @Override
    public boolean func_176209_a(@Nonnull IBlockState state, boolean fullHit)
    {
        return fullHit;
    }

    @Override
    public int getMaxRenderHeightMeta()
    {
        return quantaPerBlock - 1;
    }

    @Override
    public void func_180650_b(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {
        boolean changed = false;
        int quantaRemaining = state.func_177229_b(LEVEL) + 1;

        // Flow vertically if possible
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(world, pos, quantaRemaining);

        if (quantaRemaining < 1)
        {
            return;
        }
        else if (quantaRemaining != prevRemaining)
        {
            changed = true;
            if (quantaRemaining == 1)
            {
                world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaRemaining - 1), Constants.BlockFlags.SEND_TO_CLIENTS);
                return;
            }
        }
        else if (quantaRemaining == 1)
        {
            return;
        }

        // Flow out if possible
        int lowerThan = quantaRemaining - 1;
        int total = quantaRemaining;
        int count = 1;

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos off = pos.func_177972_a(side);
            if (displaceIfPossible(world, off))
                world.func_175698_g(off);

            int quanta = getQuantaValueBelow(world, off, lowerThan);
            if (quanta >= 0)
            {
                count++;
                total += quanta;
            }
        }

        if (count == 1)
        {
            if (changed)
            {
                world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaRemaining - 1), Constants.BlockFlags.SEND_TO_CLIENTS);
            }
            return;
        }

        int each = total / count;
        int rem = total % count;

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos off = pos.func_177972_a(side);
            int quanta = getQuantaValueBelow(world, off, lowerThan);
            if (quanta >= 0)
            {
                int newQuanta = each;
                if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0)
                {
                    ++newQuanta;
                    --rem;
                }

                if (newQuanta != quanta)
                {
                    if (newQuanta == 0)
                    {
                        world.func_175698_g(off);
                    }
                    else
                    {
                        world.func_180501_a(off, func_176223_P().func_177226_a(LEVEL, newQuanta - 1), Constants.BlockFlags.SEND_TO_CLIENTS);
                    }
                    world.func_175684_a(off, this, tickRate);
                }
                --count;
            }
        }

        if (rem > 0)
        {
            ++each;
        }
        world.func_180501_a(pos, state.func_177226_a(LEVEL, each - 1), Constants.BlockFlags.SEND_TO_CLIENTS);
    }

    public int tryToFlowVerticallyInto(World world, BlockPos pos, int amtToInput)
    {
        IBlockState myState = world.func_180495_p(pos);
        BlockPos other = pos.func_177982_a(0, densityDir, 0);
        if (other.func_177956_o() < 0 || other.func_177956_o() >= world.func_72800_K())
        {
            world.func_175698_g(pos);
            return 0;
        }

        int amt = getQuantaValueBelow(world, other, quantaPerBlock);
        if (amt >= 0)
        {
            amt += amtToInput;
            if (amt > quantaPerBlock)
            {
                world.func_175656_a(other, myState.func_177226_a(LEVEL, quantaPerBlock - 1));
                world.func_175684_a(other, this, tickRate);
                return amt - quantaPerBlock;
            }
            else if (amt > 0)
            {
                world.func_175656_a(other, myState.func_177226_a(LEVEL, amt - 1));
                world.func_175684_a(other, this, tickRate);
                world.func_175698_g(pos);
                return 0;
            }
            return amtToInput;
        }
        else
        {
            int density_other = getDensity(world, other);
            if (density_other == Integer.MAX_VALUE)
            {
                if (displaceIfPossible(world, other))
                {
                    world.func_175656_a(other, myState.func_177226_a(LEVEL, amtToInput - 1));
                    world.func_175684_a(other, this, tickRate);
                    world.func_175698_g(pos);
                    return 0;
                }
                else
                {
                    return amtToInput;
                }
            }

            if (densityDir < 0)
            {
                if (density_other < density) // then swap
                {
                    IBlockState state = world.func_180495_p(other);
                    world.func_175656_a(other, myState.func_177226_a(LEVEL, amtToInput - 1));
                    world.func_175656_a(pos,   state);
                    world.func_175684_a(other, this, tickRate);
                    world.func_175684_a(pos,   state.func_177230_c(), state.func_177230_c().func_149738_a(world));
                    return 0;
                }
            }
            else
            {
                if (density_other > density)
                {
                    IBlockState state = world.func_180495_p(other);
                    world.func_175656_a(other, myState.func_177226_a(LEVEL, amtToInput - 1));
                    world.func_175656_a(pos, state);
                    world.func_175684_a(other, this,  tickRate);
                    world.func_175684_a(pos, state.func_177230_c(), state.func_177230_c().func_149738_a(world));
                    return 0;
                }
            }
            return amtToInput;
        }
    }

    /* IFluidBlock */
    @Override
    public int place(World world, BlockPos pos, @Nonnull FluidStack fluidStack, boolean doPlace)
    {
        IBlockState existing = world.func_180495_p(pos);
        float quantaAmount = Fluid.BUCKET_VOLUME / quantaPerBlockFloat;
        // If the stack contains more available fluid than the full source block,
        // set a source block
        int closest = Fluid.BUCKET_VOLUME;
        int quanta = quantaPerBlock;
        if (fluidStack.amount < closest)
        {
            // Figure out maximum level to match stack amount
            closest = MathHelper.func_76141_d(quantaAmount * MathHelper.func_76141_d(fluidStack.amount / quantaAmount));
            quanta = MathHelper.func_76141_d(closest / quantaAmount);
        }
        if (existing.func_177230_c() == this)
        {
            int existingQuanta = existing.func_177229_b(LEVEL) + 1;
            int missingQuanta = quantaPerBlock - existingQuanta;
            closest = Math.min(closest, MathHelper.func_76141_d(missingQuanta * quantaAmount));
            quanta = Math.min(quanta + existingQuanta, quantaPerBlock);
        }

        // If too little (or too much, technically impossible) fluid is to be placed, abort
        if (quanta < 1 || quanta > 16)
            return 0;

        if (doPlace)
        {
            FluidUtil.destroyBlockOnFluidPlacement(world, pos);
            world.func_180501_a(pos, func_176223_P().func_177226_a(LEVEL, quanta - 1), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        }

        return closest;
    }

    @Override
    public FluidStack drain(World world, BlockPos pos, boolean doDrain)
    {
        final FluidStack fluidStack = new FluidStack(getFluid(), MathHelper.func_76141_d(getQuantaPercentage(world, pos) * Fluid.BUCKET_VOLUME));

        if (doDrain)
        {
            world.func_175698_g(pos);
        }

        return fluidStack;
    }

    @Override
    public boolean canDrain(World world, BlockPos pos)
    {
        return true;
    }
}
