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

package net.minecraftforge.client.model;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;

public abstract class BakedModelWrapper<T extends IBakedModel> implements IBakedModel
{
    protected final T originalModel;

    public BakedModelWrapper(T originalModel)
    {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> func_188616_a(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        return originalModel.func_188616_a(state, side, rand);
    }

    @Override
    public boolean func_177555_b()
    {
        return originalModel.func_177555_b();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state)
    {
        return originalModel.isAmbientOcclusion(state);
    }

    @Override
    public boolean func_177556_c()
    {
        return originalModel.func_177556_c();
    }

    @Override
    public boolean func_188618_c()
    {
        return originalModel.func_188618_c();
    }

    @Override
    public TextureAtlasSprite func_177554_e()
    {
        return originalModel.func_177554_e();
    }

    @Override
    public ItemCameraTransforms func_177552_f()
    {
        return originalModel.func_177552_f();
    }

    @Override
    public ItemOverrideList func_188617_f()
    {
        return originalModel.func_188617_f();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType)
    {
        return originalModel.handlePerspective(cameraTransformType);
    }
}
