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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.primitives.Ints;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a fluid block implementation which emulates vanilla Minecraft fluid behavior.
 *
 * It is highly recommended that you use/extend this class for "classic" fluid blocks.
 *
 */
public class BlockFluidClassic extends BlockFluidBase
{
    protected static final List<EnumFacing> SIDES = Collections.unmodifiableList(Arrays.asList(
            EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH));

    protected boolean[] isOptimalFlowDirection = new boolean[4];
    protected int[] flowCost = new int[4];

    protected boolean canCreateSources = false;

    protected FluidStack stack;

    public BlockFluidClassic(Fluid fluid, Material material, MapColor mapColor)
    {
        super(fluid, material, mapColor);
        stack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
    }

    public BlockFluidClassic(Fluid fluid, Material material)
    {
        this(fluid, material, material.func_151565_r());
    }

    public BlockFluidClassic setFluidStack(FluidStack stack)
    {
        this.stack = stack;
        return this;
    }

    public BlockFluidClassic setFluidStackAmount(int amount)
    {
        this.stack.amount = amount;
        return this;
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

        return quantaPerBlock - state.func_177229_b(LEVEL);
    }

    @Override
    public boolean func_176209_a(@Nonnull IBlockState state, boolean fullHit)
    {
        return fullHit && state.func_177229_b(LEVEL) == 0;
    }

    @Override
    public int getMaxRenderHeightMeta()
    {
        return 0;
    }

    @Override
    public void func_180650_b(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {
        int quantaRemaining = quantaPerBlock - state.func_177229_b(LEVEL);
        int expQuanta = -101;

        // check adjacent block levels if non-source
        if (quantaRemaining < quantaPerBlock)
        {
            int adjacentSourceBlocks = 0;

            if (ForgeEventFactory.canCreateFluidSource(world, pos, state, canCreateSources))
            {
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                {
                    if (isSourceBlock(world, pos.func_177972_a(side))) adjacentSourceBlocks++;
                }
            }

            // new source block
            if (adjacentSourceBlocks >= 2 && (world.func_180495_p(pos.func_177981_b(densityDir)).func_185904_a().func_76220_a() || isSourceBlock(world, pos.func_177981_b(densityDir))))
            {
                expQuanta = quantaPerBlock;
            }
            // vertical flow into block
            else if (hasVerticalFlow(world, pos))
            {
                expQuanta = quantaPerBlock - 1;
            }
            else
            {
                int maxQuanta = -100;
                for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
                {
                    maxQuanta = getLargerQuanta(world, pos.func_177972_a(side), maxQuanta);
                }
                expQuanta = maxQuanta - 1;
            }

            // decay calculation
            if (expQuanta != quantaRemaining)
            {
                quantaRemaining = expQuanta;

                if (expQuanta <= 0)
                {
                    world.func_175698_g(pos);
                }
                else
                {
                    world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaPerBlock - expQuanta), Constants.BlockFlags.SEND_TO_CLIENTS);
                    world.func_175684_a(pos, this, tickRate);
                    world.func_175685_c(pos, this, false);
                }
            }
        }

        // Flow vertically if possible
        if (canDisplace(world, pos.func_177981_b(densityDir)))
        {
            flowIntoBlock(world, pos.func_177981_b(densityDir), 1);
            return;
        }

        // Flow outward if possible
        int flowMeta = quantaPerBlock - quantaRemaining + 1;
        if (flowMeta >= quantaPerBlock)
        {
            return;
        }

        if (isSourceBlock(world, pos) || !isFlowingVertically(world, pos))
        {
            if (hasVerticalFlow(world, pos))
            {
                flowMeta = 1;
            }
            boolean flowTo[] = getOptimalFlowDirections(world, pos);
            for (int i = 0; i < 4; i++)
            {
                if (flowTo[i]) flowIntoBlock(world, pos.func_177972_a(SIDES.get(i)), flowMeta);
            }
        }
    }

    protected final boolean hasDownhillFlow(IBlockAccess world, BlockPos pos, EnumFacing direction)
    {
        return world.func_180495_p(pos.func_177972_a(direction).func_177979_c(densityDir)).func_177230_c() == this
                && (canFlowInto(world, pos.func_177972_a(direction))
                ||  canFlowInto(world, pos.func_177979_c(densityDir)));
    }

    public boolean isFlowingVertically(IBlockAccess world, BlockPos pos)
    {
        return world.func_180495_p(pos.func_177981_b(densityDir)).func_177230_c() == this ||
            (world.func_180495_p(pos).func_177230_c() == this && canFlowInto(world, pos.func_177981_b(densityDir)));
    }

    public boolean isSourceBlock(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        return state.func_177230_c() == this && state.func_177229_b(LEVEL) == 0;
    }

    protected boolean[] getOptimalFlowDirections(World world, BlockPos pos)
    {
        for (int side = 0; side < 4; side++)
        {
            flowCost[side] = 1000;

            BlockPos pos2 = pos.func_177972_a(SIDES.get(side));

            if (!canFlowInto(world, pos2) || isSourceBlock(world, pos2))
            {
                continue;
            }

            if (canFlowInto(world, pos2.func_177981_b(densityDir)))
            {
                flowCost[side] = 0;
            }
            else
            {
                flowCost[side] = calculateFlowCost(world, pos2, 1, side);
            }
        }

        int min = Ints.min(flowCost);
        for (int side = 0; side < 4; side++)
        {
            isOptimalFlowDirection[side] = flowCost[side] == min;
        }
        return isOptimalFlowDirection;
    }

    protected int calculateFlowCost(World world, BlockPos pos, int recurseDepth, int side)
    {
        int cost = 1000;
        for (int adjSide = 0; adjSide < 4; adjSide++)
        {
            if (SIDES.get(adjSide) == SIDES.get(side).func_176734_d())
            {
                continue;
            }

            BlockPos pos2 = pos.func_177972_a(SIDES.get(adjSide));

            if (!canFlowInto(world, pos2) || isSourceBlock(world, pos2))
            {
                continue;
            }

            if (canFlowInto(world, pos2.func_177981_b(densityDir)))
            {
                return recurseDepth;
            }

            if (recurseDepth >= quantaPerBlock / 2)
            {
                continue;
            }

            cost = Math.min(cost, calculateFlowCost(world, pos2, recurseDepth + 1, adjSide));
        }
        return cost;
    }

    protected void flowIntoBlock(World world, BlockPos pos, int meta)
    {
        if (meta < 0) return;
        if (displaceIfPossible(world, pos))
        {
            world.func_175656_a(pos, this.func_176223_P().func_177226_a(LEVEL, meta));
        }
    }

    protected boolean canFlowInto(IBlockAccess world, BlockPos pos)
    {
        return world.func_180495_p(pos).func_177230_c() == this || canDisplace(world, pos);
    }

    protected int getLargerQuanta(IBlockAccess world, BlockPos pos, int compare)
    {
        int quantaRemaining = getEffectiveQuanta(world, pos);
        if (quantaRemaining <= 0)
        {
            return compare;
        }
        return quantaRemaining >= compare ? quantaRemaining : compare;
    }

    /* IFluidBlock */
    @Override
    public int place(World world, BlockPos pos, @Nonnull FluidStack fluidStack, boolean doPlace)
    {
        if (fluidStack.amount < Fluid.BUCKET_VOLUME)
        {
            return 0;
        }
        if (doPlace)
        {
            FluidUtil.destroyBlockOnFluidPlacement(world, pos);
            world.func_180501_a(pos, this.func_176223_P(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        }
        return Fluid.BUCKET_VOLUME;
    }

    @Override
    @Nullable
    public FluidStack drain(World world, BlockPos pos, boolean doDrain)
    {
        if (!isSourceBlock(world, pos))
        {
            return null;
        }

        if (doDrain)
        {
            world.func_175698_g(pos);
        }

        return stack.copy();
    }

    @Override
    public boolean canDrain(World world, BlockPos pos)
    {
        return isSourceBlock(world, pos);
    }
}
