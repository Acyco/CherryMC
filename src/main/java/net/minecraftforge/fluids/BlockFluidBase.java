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

import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.common.property.PropertyFloat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is a base implementation for Fluid blocks.
 *
 * It is highly recommended that you extend this class or one of the Forge-provided child classes.
 *
 */
public abstract class BlockFluidBase extends Block implements IFluidBlock
{
    protected final static Map<Block, Boolean> defaultDisplacements = Maps.newHashMap();

    static
    {
        defaultDisplacements.put(Blocks.field_180413_ao,                       false);
        defaultDisplacements.put(Blocks.field_180414_ap,                    false);
        defaultDisplacements.put(Blocks.field_180412_aq,                     false);
        defaultDisplacements.put(Blocks.field_180411_ar,                    false);
        defaultDisplacements.put(Blocks.field_180410_as,                    false);
        defaultDisplacements.put(Blocks.field_180409_at,                  false);
        defaultDisplacements.put(Blocks.field_150415_aT,                       false);
        defaultDisplacements.put(Blocks.field_180400_cw,                  false);
        defaultDisplacements.put(Blocks.field_180407_aO,                      false);
        defaultDisplacements.put(Blocks.field_180408_aP,                   false);
        defaultDisplacements.put(Blocks.field_180404_aQ,                    false);
        defaultDisplacements.put(Blocks.field_180403_aR,                   false);
        defaultDisplacements.put(Blocks.field_180406_aS,                 false);
        defaultDisplacements.put(Blocks.field_180405_aT,                   false);
        defaultDisplacements.put(Blocks.field_150386_bk,             false);
        defaultDisplacements.put(Blocks.field_180390_bo,                 false);
        defaultDisplacements.put(Blocks.field_180391_bp,              false);
        defaultDisplacements.put(Blocks.field_180392_bq,               false);
        defaultDisplacements.put(Blocks.field_180386_br,              false);
        defaultDisplacements.put(Blocks.field_180385_bs,            false);
        defaultDisplacements.put(Blocks.field_180387_bt,              false);
        defaultDisplacements.put(Blocks.field_150452_aw,          false);
        defaultDisplacements.put(Blocks.field_150456_au,           false);
        defaultDisplacements.put(Blocks.field_150445_bS,  false);
        defaultDisplacements.put(Blocks.field_150443_bT,  false);
        defaultDisplacements.put(Blocks.field_150468_ap,                         false);
        defaultDisplacements.put(Blocks.field_150411_aY,                      false);
        defaultDisplacements.put(Blocks.field_150410_aZ,                     false);
        defaultDisplacements.put(Blocks.field_150397_co,             false);
        defaultDisplacements.put(Blocks.field_150427_aO,                         false);
        defaultDisplacements.put(Blocks.field_150384_bq,                     false);
        defaultDisplacements.put(Blocks.field_150463_bK,               false);
        defaultDisplacements.put(Blocks.field_180401_cv,                        false);
        defaultDisplacements.put(Blocks.field_180393_cK,                false);
        defaultDisplacements.put(Blocks.field_180394_cL,                    false);
        defaultDisplacements.put(Blocks.field_150414_aQ,                           false);

        defaultDisplacements.put(Blocks.field_150454_av,     false);
        defaultDisplacements.put(Blocks.field_150472_an, false);
        defaultDisplacements.put(Blocks.field_150444_as,     false);
        defaultDisplacements.put(Blocks.field_150436_aH,         false);
    }
    protected Map<Block, Boolean> displacements = Maps.newHashMap();

    private static final class UnlistedPropertyBool extends Properties.PropertyAdapter<Boolean>
    {
        public UnlistedPropertyBool(String name)
        {
            super(PropertyBool.func_177716_a(name));
        }
    }

    public static final PropertyInteger LEVEL = PropertyInteger.func_177719_a("level", 0, 15);
    public static final PropertyFloat[] LEVEL_CORNERS = new PropertyFloat[4];
    public static final PropertyFloat FLOW_DIRECTION = new PropertyFloat("flow_direction", -1000f, 1000f);
    public static final UnlistedPropertyBool[] SIDE_OVERLAYS = new UnlistedPropertyBool[4];
    public static final ImmutableList<IUnlistedProperty<?>> FLUID_RENDER_PROPS;

    static
    {
        ImmutableList.Builder<IUnlistedProperty<?>> builder = ImmutableList.builder();
        builder.add(FLOW_DIRECTION);
        for(int i = 0; i < 4; i++)
        {
            LEVEL_CORNERS[i] = new PropertyFloat("level_corner_" + i, 0f, 1f);
            builder.add(LEVEL_CORNERS[i]);

            SIDE_OVERLAYS[i] = new UnlistedPropertyBool("side_overlay_" + i);
            builder.add(SIDE_OVERLAYS[i]);
        }
        FLUID_RENDER_PROPS = builder.build();
    }

    protected int quantaPerBlock = 8;
    protected float quantaPerBlockFloat = 8F;
    protected float quantaFraction = 8f / 9f;
    protected int density = 1;
    protected int densityDir = -1;
    protected int temperature = 295;

    protected int tickRate = 20;
    protected BlockRenderLayer renderLayer = BlockRenderLayer.TRANSLUCENT;
    protected int maxScaledLight = 0;

    protected final String fluidName;

    /**
     * This is the fluid used in the constructor. Use this reference to configure things
     * like icons for your block. It might not be active in the registry, so do
     * NOT expose it.
     */
    protected final Fluid definedFluid;

    public BlockFluidBase(Fluid fluid, Material material, MapColor mapColor)
    {
        super(material, mapColor);
        this.func_149675_a(true);
        this.func_149649_H();

        this.fluidName = fluid.getName();
        this.density = fluid.density;
        this.temperature = fluid.temperature;
        this.maxScaledLight = fluid.luminosity;
        this.tickRate = fluid.viscosity / 200;
        this.densityDir = fluid.density > 0 ? -1 : 1;
        fluid.setBlock(this);

        this.definedFluid = fluid;
        displacements.putAll(defaultDisplacements);
        this.func_180632_j(field_176227_L.func_177621_b().func_177226_a(LEVEL, getMaxRenderHeightMeta()));
    }

    public BlockFluidBase(Fluid fluid, Material material)
    {
        this(fluid, material, material.func_151565_r());
    }

    @Override
    @Nonnull
    protected BlockStateContainer func_180661_e()
    {
        return new BlockStateContainer.Builder(this)
                .add(LEVEL)
                .add(FLUID_RENDER_PROPS.toArray(new IUnlistedProperty<?>[0]))
                .build();
    }

    @Override
    public int func_176201_c(@Nonnull IBlockState state)
    {
        return state.func_177229_b(LEVEL);
    }
    @Override
    @Deprecated
    @Nonnull
    public IBlockState func_176203_a(int meta)
    {
        return this.func_176223_P().func_177226_a(LEVEL, meta);
    }

    public BlockFluidBase setQuantaPerBlock(int quantaPerBlock)
    {
        if (quantaPerBlock > 16 || quantaPerBlock < 1) quantaPerBlock = 8;
        this.quantaPerBlock = quantaPerBlock;
        this.quantaPerBlockFloat = quantaPerBlock;
        this.quantaFraction = quantaPerBlock / (quantaPerBlock + 1f);
        return this;
    }

    public BlockFluidBase setDensity(int density)
    {
        if (density == 0) density = 1;
        this.density = density;
        this.densityDir = density > 0 ? -1 : 1;
        return this;
    }

    public BlockFluidBase setTemperature(int temperature)
    {
        this.temperature = temperature;
        return this;
    }

    public BlockFluidBase setTickRate(int tickRate)
    {
        if (tickRate <= 0) tickRate = 20;
        this.tickRate = tickRate;
        return this;
    }

    public BlockFluidBase setRenderLayer(BlockRenderLayer renderLayer)
    {
        this.renderLayer = renderLayer;
        return this;
    }

    public BlockFluidBase setMaxScaledLight(int maxScaledLight)
    {
        this.maxScaledLight = maxScaledLight;
        return this;
    }

    public final int getDensity()
    {
        return density;
    }

    public final int getTemperature()
    {
        return temperature;
    }

    /**
     * Returns true if the block at (pos) is displaceable. Does not displace the block.
     */
    public boolean canDisplace(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        Block block = state.func_177230_c();

        if (block.isAir(state, world, pos))
        {
            return true;
        }

        if (block == this)
        {
            return false;
        }

        if (displacements.containsKey(block))
        {
            return displacements.get(block);
        }

        Material material = state.func_185904_a();
        if (material.func_76230_c() || material == Material.field_151567_E || material == Material.field_189963_J)
        {
            return false;
        }

        int density = getDensity(world, pos);
        if (density == Integer.MAX_VALUE)
        {
            return true;
        }

        return this.density > density;
    }

    /**
     * Attempt to displace the block at (pos), return true if it was displaced.
     */
    public boolean displaceIfPossible(World world, BlockPos pos)
    {
        boolean canDisplace = canDisplace(world, pos);
        if (canDisplace)
        {
            IBlockState state = world.func_180495_p(pos);
            Block block = state.func_177230_c();

            if (!block.isAir(state, world, pos) && !isFluid(state))
            {
                // Forge: Vanilla has a 'bug' where snowballs don't drop like every other block. So special case because ewww...
                if (block != Blocks.field_150431_aC) block.func_176226_b(world, pos, state, 0);
            }
        }
        return canDisplace;
    }

    public abstract int getQuantaValue(IBlockAccess world, BlockPos pos);

    @Override
    public abstract boolean func_176209_a(@Nonnull IBlockState state, boolean fullHit);

    public abstract int getMaxRenderHeightMeta();

    /* BLOCK FUNCTIONS */
    @Override
    public void func_176213_c(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state)
    {
        world.func_175684_a(pos, this, tickRate);
    }

    @Override
    public void func_189540_a(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighbourPos)
    {
        world.func_175684_a(pos, this, tickRate);
    }

    // Used to prevent updates on chunk generation
    @Override
    public boolean func_149698_L()
    {
        return false;
    }

    @Override
    public boolean func_176205_b(@Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return true;
    }

    @Override
    @Nonnull
    public Item func_180660_a(@Nonnull IBlockState state, @Nonnull Random rand, int fortune)
    {
        return Items.field_190931_a;
    }

    @Override
    public int func_149745_a(@Nonnull Random par1Random)
    {
        return 0;
    }

    @Override
    public int func_149738_a(@Nonnull World world)
    {
        return tickRate;
    }

    @Override
    @Nonnull
    public Vec3d func_176197_a(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Entity entity, @Nonnull Vec3d vec)
    {
        return densityDir > 0 ? vec : vec.func_178787_e(getFlowVector(world, pos));
    }

    @Override
    public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        if (maxScaledLight == 0)
        {
            return super.getLightValue(state, world, pos);
        }
        return (int) (getQuantaPercentage(world, pos) * maxScaledLight);
    }

    @Override
    public boolean func_149662_c(@Nonnull IBlockState state)
    {
        return false;
    }

    @Override
    public boolean func_149686_d(@Nonnull IBlockState state)
    {
        return false;
    }

    @Override
    public int func_185484_c(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        int lightThis     = world.func_175626_b(pos, 0);
        int lightUp       = world.func_175626_b(pos.func_177979_c(densityDir), 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase   = lightUp & 255;
        int lightThisExt  = lightThis >> 16 & 255;
        int lightUpExt    = lightUp >> 16 & 255;
        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public BlockRenderLayer func_180664_k()
    {
        return this.renderLayer;
    }

    @Override
    @Nonnull
    public BlockFaceShape func_193383_a(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean func_176225_a(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side)
    {
        IBlockState neighbor = world.func_180495_p(pos.func_177972_a(side));
        if (neighbor.func_185904_a() == state.func_185904_a())
        {
            return false;
        }
        if (side == (densityDir < 0 ? EnumFacing.UP : EnumFacing.DOWN))
        {
            return true;
        }
        return super.func_176225_a(state, world, pos, side);
    }

    private static boolean isFluid(@Nonnull IBlockState blockstate)
    {
        return blockstate.func_185904_a().func_76224_d() || blockstate.func_177230_c() instanceof IFluidBlock;
    }

    @Override
    @Nonnull
    public IBlockState getExtendedState(@Nonnull IBlockState oldState, @Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        IExtendedBlockState state = (IExtendedBlockState)oldState;
        state = state.withProperty(FLOW_DIRECTION, (float)getFlowDirection(world, pos));
        IBlockState[][] upBlockState = new IBlockState[3][3];
        float[][] height = new float[3][3];
        float[][] corner = new float[2][2];
        upBlockState[1][1] = world.func_180495_p(pos.func_177979_c(densityDir));
        height[1][1] = getFluidHeightForRender(world, pos, upBlockState[1][1]);
        if (height[1][1] == 1)
        {
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = 1;
                }
            }
        }
        else
        {
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    if (i != 1 || j != 1)
                    {
                        upBlockState[i][j] = world.func_180495_p(pos.func_177982_a(i - 1, 0, j - 1).func_177979_c(densityDir));
                        height[i][j] = getFluidHeightForRender(world, pos.func_177982_a(i - 1, 0, j - 1), upBlockState[i][j]);
                    }
                }
            }
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = getFluidHeightAverage(height[i][j], height[i][j + 1], height[i + 1][j], height[i + 1][j + 1]);
                }
            }
            //check for downflow above corners
            boolean n =  isFluid(upBlockState[0][1]);
            boolean s =  isFluid(upBlockState[2][1]);
            boolean w =  isFluid(upBlockState[1][0]);
            boolean e =  isFluid(upBlockState[1][2]);
            boolean nw = isFluid(upBlockState[0][0]);
            boolean ne = isFluid(upBlockState[0][2]);
            boolean sw = isFluid(upBlockState[2][0]);
            boolean se = isFluid(upBlockState[2][2]);
            if (nw || n || w)
            {
                corner[0][0] = 1;
            }
            if (ne || n || e)
            {
                corner[0][1] = 1;
            }
            if (sw || s || w)
            {
                corner[1][0] = 1;
            }
            if (se || s || e)
            {
                corner[1][1] = 1;
            }
        }

        for (int i = 0; i < 4; i++)
        {
            EnumFacing side = EnumFacing.func_176731_b(i);
            BlockPos offset = pos.func_177972_a(side);
            boolean useOverlay = world.func_180495_p(offset).func_193401_d(world, offset, side.func_176734_d()) == BlockFaceShape.SOLID;
            state = state.withProperty(SIDE_OVERLAYS[i], useOverlay);
        }

        state = state.withProperty(LEVEL_CORNERS[0], corner[0][0]);
        state = state.withProperty(LEVEL_CORNERS[1], corner[0][1]);
        state = state.withProperty(LEVEL_CORNERS[2], corner[1][1]);
        state = state.withProperty(LEVEL_CORNERS[3], corner[1][0]);
        return state;
    }

    /* FLUID FUNCTIONS */
    public static int getDensity(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        Block block = state.func_177230_c();

        if (block instanceof BlockFluidBase)
        {
            return ((BlockFluidBase)block).getDensity();
        }

        Fluid fluid = getFluid(state);
        if (fluid != null)
        {
            return fluid.getDensity();
        }
        return Integer.MAX_VALUE;
    }

    public static int getTemperature(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        Block block = state.func_177230_c();

        if (block instanceof BlockFluidBase)
        {
            return ((BlockFluidBase)block).getTemperature();
        }

        Fluid fluid = getFluid(state);
        if (fluid != null)
        {
            return fluid.getTemperature();
        }
        return Integer.MAX_VALUE;
    }

    @Nullable
    private static Fluid getFluid(IBlockState state)
    {
        Block block = state.func_177230_c();

        if (block instanceof IFluidBlock)
        {
            return ((IFluidBlock)block).getFluid();
        }
        if (block instanceof BlockLiquid)
        {
            if (state.func_185904_a() == Material.field_151586_h)
            {
                return FluidRegistry.WATER;
            }
            if (state.func_185904_a() == Material.field_151587_i)
            {
                return FluidRegistry.LAVA;
            }
        }
        return null;
    }

    public static double getFlowDirection(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        if (!state.func_185904_a().func_76224_d())
        {
            return -1000.0;
        }
        Vec3d vec = ((BlockFluidBase)state.func_177230_c()).getFlowVector(world, pos);
        return vec.field_72450_a == 0.0D && vec.field_72449_c == 0.0D ? -1000.0D : MathHelper.func_181159_b(vec.field_72449_c, vec.field_72450_a) - Math.PI / 2D;
    }

    public final int getQuantaValueBelow(IBlockAccess world, BlockPos pos, int belowThis)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        if (quantaRemaining >= belowThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final int getQuantaValueAbove(IBlockAccess world, BlockPos pos, int aboveThis)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        if (quantaRemaining <= aboveThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final float getQuantaPercentage(IBlockAccess world, BlockPos pos)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        return quantaRemaining / quantaPerBlockFloat;
    }

    public float getFluidHeightAverage(float... flow)
    {
        float total = 0;
        int count = 0;

        for (int i = 0; i < flow.length; i++)
        {
            if (flow[i] >= quantaFraction)
            {
                total += flow[i] * 10;
                count += 10;
            }

            if (flow[i] >= 0)
            {
                total += flow[i];
                count++;
            }
        }

        return total / count;
    }

    public float getFluidHeightForRender(IBlockAccess world, BlockPos pos, @Nonnull IBlockState up)
    {
        IBlockState here = world.func_180495_p(pos);
        if (here.func_177230_c() == this)
        {
            if (isFluid(up))
            {
                return 1;
            }

            if (func_176201_c(here) == getMaxRenderHeightMeta())
            {
                return quantaFraction;
            }
        }
        if (here.func_177230_c() instanceof BlockLiquid)
        {
            return Math.min(1 - BlockLiquid.func_149801_b(here.func_177229_b(BlockLiquid.field_176367_b)), quantaFraction);
        }
        return !here.func_185904_a().func_76220_a() && up.func_177230_c() == this ? 1 : this.getQuantaPercentage(world, pos) * quantaFraction;
    }

    public Vec3d getFlowVector(IBlockAccess world, BlockPos pos)
    {
        Vec3d vec = new Vec3d(0.0D, 0.0D, 0.0D);
        int decay = getFlowDecay(world, pos);

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos offset = pos.func_177972_a(side);
            int otherDecay = getFlowDecay(world, offset);
            if (otherDecay >= quantaPerBlock)
            {
                if (!world.func_180495_p(offset).func_185904_a().func_76230_c())
                {
                    otherDecay = getFlowDecay(world, offset.func_177981_b(densityDir));
                    if (otherDecay < quantaPerBlock)
                    {
                        int power = otherDecay - (decay - quantaPerBlock);
                        vec = vec.func_72441_c(side.func_82601_c() * power, 0, side.func_82599_e() * power);
                    }
                }
            }
            else
            {
                int power = otherDecay - decay;
                vec = vec.func_72441_c(side.func_82601_c() * power, 0, side.func_82599_e() * power);
            }
        }

        if (hasVerticalFlow(world, pos))
        {
            for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
            {
                BlockPos offset = pos.func_177972_a(side);
                if (causesDownwardCurrent(world, offset, side) || causesDownwardCurrent(world, offset.func_177979_c(densityDir), side))
                {
                    vec = vec.func_72432_b().func_72441_c(0.0, 6.0 * densityDir, 0.0);
                    break;
                }
            }
        }

        return vec.func_72432_b();
    }

    private int getFlowDecay(IBlockAccess world, BlockPos pos)
    {
        return quantaPerBlock - getEffectiveQuanta(world, pos);
    }

    final int getEffectiveQuanta(IBlockAccess world, BlockPos pos)
    {
        int quantaValue = getQuantaValue(world, pos);
        return quantaValue > 0 && quantaValue < quantaPerBlock && hasVerticalFlow(world, pos) ? quantaPerBlock : quantaValue;
    }

    final boolean hasVerticalFlow(IBlockAccess world, BlockPos pos)
    {
        return world.func_180495_p(pos.func_177979_c(densityDir)).func_177230_c() == this;
    }

    protected boolean causesDownwardCurrent(IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        IBlockState state = world.func_180495_p(pos);
        Block block = state.func_177230_c();

        if (block == this) return false;
        if (face == (densityDir < 0 ? EnumFacing.UP : EnumFacing.DOWN)) return true;
        if (state.func_185904_a() == Material.field_151588_w) return false;

        boolean flag = func_193382_c(block) || block instanceof BlockStairs;
        return !flag && state.func_193401_d(world, pos, face) == BlockFaceShape.SOLID;
    }

    /* IFluidBlock */
    @Override
    public Fluid getFluid()
    {
        return FluidRegistry.getFluid(fluidName);
    }

    @Override
    public float getFilledPercentage(World world, BlockPos pos)
    {
        return getFilledPercentage((IBlockAccess) world, pos);
    }

    public float getFilledPercentage(IBlockAccess world, BlockPos pos)
    {
        int quantaRemaining = getEffectiveQuanta(world, pos);
        float remaining = (quantaRemaining + 1f) / (quantaPerBlockFloat + 1f);
        return remaining * (density > 0 ? 1 : -1);
    }

    @Override
    public AxisAlignedBB func_180646_a(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos)
    {
        return field_185506_k;
    }
    
    @Override
    @SideOnly (Side.CLIENT)
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks)
    {
        if (!isWithinFluid(world, pos, ActiveRenderInfo.func_178806_a(entity, partialTicks)))
        {
            BlockPos otherPos = pos.func_177979_c(densityDir);
            IBlockState otherState = world.func_180495_p(otherPos);
            return otherState.func_177230_c().getFogColor(world, otherPos, otherState, entity, originalColor, partialTicks);
        }

        if (getFluid() != null)
        {
            int color = getFluid().getColor();
            float red = (color >> 16 & 0xFF) / 255.0F;
            float green = (color >> 8 & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;
            return new Vec3d(red, green, blue);
        }

        return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    @Override
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint)
    {
        if (!isWithinFluid(world, pos, viewpoint))
        {
            return world.func_180495_p(pos.func_177979_c(densityDir));
        }

        return super.getStateAtViewpoint(state, world, pos, viewpoint);
    }

    private boolean isWithinFluid(IBlockAccess world, BlockPos pos, Vec3d vec)
    {
        float filled = getFilledPercentage(world, pos);
        return filled < 0 ? vec.field_72448_b > pos.func_177956_o() + filled + 1
                          : vec.field_72448_b < pos.func_177956_o() + filled;
    }
    
    @Override
    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material)
    {
        float filled = getFilledPercentage(world, pos);
        return Math.max(filled, 0);
    }
}
