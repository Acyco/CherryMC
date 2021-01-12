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

package net.minecraftforge.client.model.animation;

import java.util.List;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.common.animation.Event;
import net.minecraftforge.common.animation.IEventHandler;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;

import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

/**
 * ModelBase that works with the Forge model system and animations.
 * Some quirks are still left, deprecated for the moment.
 */
@Deprecated
public class AnimationModelBase<T extends Entity> extends ModelBase implements IEventHandler<T>
{
    private final VertexLighterFlat lighter;
    private final ResourceLocation modelLocation;

    public AnimationModelBase(ResourceLocation modelLocation, VertexLighterFlat lighter)
    {
        this.modelLocation = modelLocation;
        this.lighter = lighter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void func_78088_a(Entity entity, float limbSwing, float limbSwingSpeed, float timeAlive, float yawHead, float rotationPitch, float scale)
    {
        IAnimationStateMachine capability = entity.getCapability(CapabilityAnimation.ANIMATION_CAPABILITY, null);
        if (capability == null)
        {
            return;
        }
        Pair<IModelState, Iterable<Event>> pair = capability.apply(timeAlive / 20);
        handleEvents((T)entity, timeAlive / 20, pair.getRight());
        IModel model = ModelLoaderRegistry.getModelOrMissing(modelLocation);
        IBakedModel bakedModel = model.bake(pair.getLeft(), DefaultVertexFormats.field_176599_b, ModelLoader.defaultTextureGetter());

        BlockPos pos = new BlockPos(entity.field_70165_t, entity.field_70163_u + entity.field_70131_O, entity.field_70161_v);

        RenderHelper.func_74518_a();
        GlStateManager.func_179094_E();
        GlStateManager.func_179114_b(180, 0, 0, 1);
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder builder = tessellator.func_178180_c();
        builder.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_176600_a);
        builder.func_178969_c(-0.5, -1.5, -0.5);

        lighter.setParent(new VertexBufferConsumer(builder));
        lighter.setWorld(entity.field_70170_p);
        lighter.setState(Blocks.field_150350_a.func_176223_P());
        lighter.setBlockPos(pos);
        boolean empty = true;
        List<BakedQuad> quads = bakedModel.func_188616_a(null, null, 0);
        if(!quads.isEmpty())
        {
            lighter.updateBlockInfo();
            empty = false;
            for(BakedQuad quad : quads)
            {
                quad.pipe(lighter);
            }
        }
        for(EnumFacing side : EnumFacing.values())
        {
            quads = bakedModel.func_188616_a(null, side, 0);
            if(!quads.isEmpty())
            {
                if(empty) lighter.updateBlockInfo();
                empty = false;
                for(BakedQuad quad : quads)
                {
                    quad.pipe(lighter);
                }
            }
        }

        // debug quad
        /*VertexBuffer.pos(0, 1, 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex(0, 0).lightmap(240, 0).endVertex();
        VertexBuffer.pos(0, 1, 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex(0, 1).lightmap(240, 0).endVertex();
        VertexBuffer.pos(1, 1, 1).color(0xFF, 0xFF, 0xFF, 0xFF).tex(1, 1).lightmap(240, 0).endVertex();
        VertexBuffer.pos(1, 1, 0).color(0xFF, 0xFF, 0xFF, 0xFF).tex(1, 0).lightmap(240, 0).endVertex();*/

        builder.func_178969_c(0, 0, 0);

        tessellator.func_78381_a();
        GlStateManager.func_179121_F();
        RenderHelper.func_74519_b();
    }

    @Override
    public void handleEvents(T instance, float time, Iterable<Event> pastEvents) {}
}
