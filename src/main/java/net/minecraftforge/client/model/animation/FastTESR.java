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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;

public abstract class FastTESR<T extends TileEntity> extends TileEntitySpecialRenderer<T>
{
    @Override
    public final void func_192841_a(T te, double x, double y, double z, float partialTicks, int destroyStage, float partial)
    {
        Tessellator tessellator = Tessellator.func_178181_a();
        BufferBuilder buffer = tessellator.func_178180_c();
        this.func_147499_a(TextureMap.field_110575_b);
        RenderHelper.func_74518_a();
        GlStateManager.func_179112_b(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.func_179147_l();
        GlStateManager.func_179129_p();

        if (Minecraft.func_71379_u())
        {
            GlStateManager.func_179103_j(GL11.GL_SMOOTH);
        }
        else
        {
            GlStateManager.func_179103_j(GL11.GL_FLAT);
        }

        buffer.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_176600_a);

        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);
        buffer.func_178969_c(0, 0, 0);

        tessellator.func_78381_a();

        RenderHelper.func_74519_b();
    }

    @Override
    public abstract void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer);
}
