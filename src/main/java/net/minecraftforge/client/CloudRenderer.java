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

package net.minecraftforge.client;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.function.Predicate;

import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;

public class CloudRenderer implements ISelectiveResourceReloadListener
{
    // Shared constants.
    private static final float PX_SIZE = 1 / 256F;

    // Building constants.
    private static final VertexFormat FORMAT = DefaultVertexFormats.field_181709_i;
    private static final int TOP_SECTIONS = 12;	// Number of slices a top face will span.
    private static final int HEIGHT = 4;
    private static final float INSET = 0.001F;
    private static final float ALPHA = 0.8F;

    // Debug
    private static final boolean WIREFRAME = false;

    // Instance fields
    private final Minecraft mc = Minecraft.func_71410_x();
    private final ResourceLocation texture = new ResourceLocation("textures/environment/clouds.png");

    private int displayList = -1;
    private VertexBuffer vbo;
    private int cloudMode = -1;
    private int renderDistance = -1;

    private DynamicTexture COLOR_TEX = null;

    private int texW;
    private int texH;

    public CloudRenderer()
    {
        // Resource manager should always be reloadable.
        ((IReloadableResourceManager) mc.func_110442_L()).func_110542_a(this);
    }

    private int getScale()
    {
        return cloudMode == 2 ? 12 : 8;
    }

    private float ceilToScale(float value)
    {
        float scale = getScale();
        return MathHelper.func_76123_f(value / scale) * scale;
    }

    private void vertices(BufferBuilder buffer)
    {
        boolean fancy = cloudMode == 2;    // Defines whether to hide all but the bottom.

        float scale = getScale();
        float CULL_DIST = 2 * scale;

        float bCol = fancy ? 0.7F : 1F;

        float sectEnd = ceilToScale((renderDistance * 2) * 16);
        float sectStart = -sectEnd;

        float sectStep = ceilToScale(sectEnd * 2 / TOP_SECTIONS);
        float sectPx = PX_SIZE / scale;

        buffer.func_181668_a(GL11.GL_QUADS, FORMAT);

        float sectX0 = sectStart;
        float sectX1 = sectX0;

        while (sectX1 < sectEnd)
        {
            sectX1 += sectStep;

            if (sectX1 > sectEnd)
                sectX1 = sectEnd;

            float sectZ0 = sectStart;
            float sectZ1 = sectZ0;

            while (sectZ1 < sectEnd)
            {
                sectZ1 += sectStep;

                if (sectZ1 > sectEnd)
                    sectZ1 = sectEnd;

                float u0 = sectX0 * sectPx;
                float u1 = sectX1 * sectPx;
                float v0 = sectZ0 * sectPx;
                float v1 = sectZ1 * sectPx;

                // Bottom
                buffer.func_181662_b(sectX0, 0, sectZ0).func_187315_a(u0, v0).func_181666_a(bCol, bCol, bCol, ALPHA).func_181675_d();
                buffer.func_181662_b(sectX1, 0, sectZ0).func_187315_a(u1, v0).func_181666_a(bCol, bCol, bCol, ALPHA).func_181675_d();
                buffer.func_181662_b(sectX1, 0, sectZ1).func_187315_a(u1, v1).func_181666_a(bCol, bCol, bCol, ALPHA).func_181675_d();
                buffer.func_181662_b(sectX0, 0, sectZ1).func_187315_a(u0, v1).func_181666_a(bCol, bCol, bCol, ALPHA).func_181675_d();

                if (fancy)
                {
                    // Top
                    buffer.func_181662_b(sectX0, HEIGHT, sectZ0).func_187315_a(u0, v0).func_181666_a(1, 1, 1, ALPHA).func_181675_d();
                    buffer.func_181662_b(sectX0, HEIGHT, sectZ1).func_187315_a(u0, v1).func_181666_a(1, 1, 1, ALPHA).func_181675_d();
                    buffer.func_181662_b(sectX1, HEIGHT, sectZ1).func_187315_a(u1, v1).func_181666_a(1, 1, 1, ALPHA).func_181675_d();
                    buffer.func_181662_b(sectX1, HEIGHT, sectZ0).func_187315_a(u1, v0).func_181666_a(1, 1, 1, ALPHA).func_181675_d();

                    float slice;
                    float sliceCoord0;
                    float sliceCoord1;

                    for (slice = sectX0; slice < sectX1;)
                    {
                        sliceCoord0 = slice * sectPx;
                        sliceCoord1 = sliceCoord0 + PX_SIZE;

                        // X sides
                        if (slice > -CULL_DIST)
                        {
                            slice += INSET;
                            buffer.func_181662_b(slice, 0,      sectZ1).func_187315_a(sliceCoord0, v1).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, HEIGHT, sectZ1).func_187315_a(sliceCoord1, v1).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, HEIGHT, sectZ0).func_187315_a(sliceCoord1, v0).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, 0,      sectZ0).func_187315_a(sliceCoord0, v0).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            slice -= INSET;
                        }

                        slice += scale;

                        if (slice <= CULL_DIST)
                        {
                            slice -= INSET;
                            buffer.func_181662_b(slice, 0,      sectZ0).func_187315_a(sliceCoord0, v0).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, HEIGHT, sectZ0).func_187315_a(sliceCoord1, v0).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, HEIGHT, sectZ1).func_187315_a(sliceCoord1, v1).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            buffer.func_181662_b(slice, 0,      sectZ1).func_187315_a(sliceCoord0, v1).func_181666_a(0.9F, 0.9F, 0.9F, ALPHA).func_181675_d();
                            slice += INSET;
                        }
                    }

                    for (slice = sectZ0; slice < sectZ1;)
                    {
                        sliceCoord0 = slice * sectPx;
                        sliceCoord1 = sliceCoord0 + PX_SIZE;

                        // Z sides
                        if (slice > -CULL_DIST)
                        {
                            slice += INSET;
                            buffer.func_181662_b(sectX0, 0,      slice).func_187315_a(u0, sliceCoord0).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX0, HEIGHT, slice).func_187315_a(u0, sliceCoord1).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX1, HEIGHT, slice).func_187315_a(u1, sliceCoord1).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX1, 0,      slice).func_187315_a(u1, sliceCoord0).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            slice -= INSET;
                        }

                        slice += scale;

                        if (slice <= CULL_DIST)
                        {
                            slice -= INSET;
                            buffer.func_181662_b(sectX1, 0,      slice).func_187315_a(u1, sliceCoord0).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX1, HEIGHT, slice).func_187315_a(u1, sliceCoord1).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX0, HEIGHT, slice).func_187315_a(u0, sliceCoord1).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            buffer.func_181662_b(sectX0, 0,      slice).func_187315_a(u0, sliceCoord0).func_181666_a(0.8F, 0.8F, 0.8F, ALPHA).func_181675_d();
                            slice += INSET;
                        }
                    }
                }

                sectZ0 = sectZ1;
            }

            sectX0 = sectX1;
        }
    }

    private void dispose()
    {
        if (vbo != null)
        {
            vbo.func_177362_c();
            vbo = null;
        }
        if (displayList >= 0)
        {
            GLAllocation.func_74523_b(displayList);
            displayList = -1;
        }
    }

    private void build()
    {
        Tessellator tess = Tessellator.func_178181_a();
        BufferBuilder buffer = tess.func_178180_c();

        if (OpenGlHelper.func_176075_f())
            vbo = new VertexBuffer(FORMAT);
        else
            GlStateManager.func_187423_f(displayList = GLAllocation.func_74526_a(1), GL11.GL_COMPILE);

        vertices(buffer);

        if (OpenGlHelper.func_176075_f())
        {
            buffer.func_178977_d();
            buffer.func_178965_a();
            vbo.func_181722_a(buffer.func_178966_f());
        }
        else
        {
            tess.func_78381_a();
            GlStateManager.func_187415_K();
        }
    }

    private int fullCoord(double coord, int scale)
    {   // Corrects misalignment of UV offset when on negative coords.
        return ((int) coord / scale) - (coord < 0 ? 1 : 0);
    }

    private boolean isBuilt()
    {
        return OpenGlHelper.func_176075_f() ? vbo != null : displayList >= 0;
    }

    public void checkSettings()
    {
        boolean newEnabled = ForgeModContainer.forgeCloudsEnabled
                && mc.field_71474_y.func_181147_e() != 0
                && mc.field_71441_e != null
                && mc.field_71441_e.field_73011_w.func_76569_d();

        if (isBuilt()
                    && (!newEnabled
                    || mc.field_71474_y.func_181147_e() != cloudMode
                    || mc.field_71474_y.field_151451_c != renderDistance))
        {
            dispose();
        }

        cloudMode = mc.field_71474_y.func_181147_e();
        renderDistance = mc.field_71474_y.field_151451_c;

        if (newEnabled && !isBuilt())
        {
            build();
        }
    }

    public boolean render(int cloudTicks, float partialTicks)
    {
        if (!isBuilt())
            return false;

        Entity entity = mc.func_175606_aa();

        double totalOffset = cloudTicks + partialTicks;

        double x = entity.field_70169_q + (entity.field_70165_t - entity.field_70169_q) * partialTicks
                + totalOffset * 0.03;
        double y = mc.field_71441_e.field_73011_w.func_76571_f()
                - (entity.field_70137_T + (entity.field_70163_u - entity.field_70137_T) * partialTicks)
                + 0.33;
        double z = entity.field_70166_s + (entity.field_70161_v - entity.field_70166_s) * partialTicks;

        int scale = getScale();

        if (cloudMode == 2)
            z += 0.33 * scale;

        // Integer UVs to translate the texture matrix by.
        int offU = fullCoord(x, scale);
        int offV = fullCoord(z, scale);

        GlStateManager.func_179094_E();

        // Translate by the remainder after the UV offset.
        GlStateManager.func_179137_b((offU * scale) - x, y, (offV * scale) - z);

        // Modulo to prevent texture samples becoming inaccurate at extreme offsets.
        offU = offU % texW;
        offV = offV % texH;

        // Translate the texture.
        GlStateManager.func_179128_n(GL11.GL_TEXTURE);
        GlStateManager.func_179109_b(offU * PX_SIZE, offV * PX_SIZE, 0);
        GlStateManager.func_179128_n(GL11.GL_MODELVIEW);

        GlStateManager.func_179129_p();

        GlStateManager.func_179147_l();
        GlStateManager.func_187428_a(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        // Color multiplier.
        Vec3d color = mc.field_71441_e.func_72824_f(partialTicks);
        float r = (float) color.field_72450_a;
        float g = (float) color.field_72448_b;
        float b = (float) color.field_72449_c;

        if (mc.field_71474_y.field_74337_g)
        {
            float tempR = r * 0.3F + g * 0.59F + b * 0.11F;
            float tempG = r * 0.3F + g * 0.7F;
            float tempB = r * 0.3F + b * 0.7F;
            r = tempR;
            g = tempG;
            b = tempB;
        }

        if (COLOR_TEX == null)
            COLOR_TEX = new DynamicTexture(1, 1);

        // Apply a color multiplier through a texture upload if shaders aren't supported.
        COLOR_TEX.func_110565_c()[0] = 255 << 24
                | ((int) (r * 255)) << 16
                | ((int) (g * 255)) << 8
                | (int) (b * 255);
        COLOR_TEX.func_110564_a();

        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179144_i(COLOR_TEX.func_110552_b());
        GlStateManager.func_179098_w();

        // Bind the clouds texture last so the shader's sampler2D is correct.
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);
        mc.field_71446_o.func_110577_a(texture);

        ByteBuffer buffer = Tessellator.func_178181_a().func_178180_c().func_178966_f();

        // Set up pointers for the display list/VBO.
        if (OpenGlHelper.func_176075_f())
        {
            vbo.func_177359_a();

            int stride = FORMAT.func_177338_f();
            GlStateManager.func_187420_d(3, GL11.GL_FLOAT, stride, 0);
            GlStateManager.func_187410_q(GL11.GL_VERTEX_ARRAY);
            GlStateManager.func_187405_c(2, GL11.GL_FLOAT, stride, 12);
            GlStateManager.func_187410_q(GL11.GL_TEXTURE_COORD_ARRAY);
            GlStateManager.func_187406_e(4, GL11.GL_UNSIGNED_BYTE, stride, 20);
            GlStateManager.func_187410_q(GL11.GL_COLOR_ARRAY);
        }
        else
        {
            buffer.limit(FORMAT.func_177338_f());
            for (int i = 0; i < FORMAT.func_177345_h(); i++)
                FORMAT.func_177343_g().get(i).func_177375_c().preDraw(FORMAT, i, FORMAT.func_177338_f(), buffer);
            buffer.position(0);
        }

        // Depth pass to prevent insides rendering from the outside.
        GlStateManager.func_179135_a(false, false, false, false);
        if (OpenGlHelper.func_176075_f())
            vbo.func_177358_a(GL11.GL_QUADS);
        else
            GlStateManager.func_179148_o(displayList);

        // Full render.
        if (!mc.field_71474_y.field_74337_g)
        {
            GlStateManager.func_179135_a(true, true, true, true);
        }
        else
        {
            switch (EntityRenderer.field_78515_b)
            {
            case 0:
                GlStateManager.func_179135_a(false, true, true, true);
                break;
            case 1:
                GlStateManager.func_179135_a(true, false, false, true);
                break;
            }
        }

        // Wireframe for debug.
        if (WIREFRAME)
        {
            GlStateManager.func_187409_d(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GlStateManager.func_187441_d(2.0F);
            GlStateManager.func_179090_x();
            GlStateManager.func_179132_a(false);
            GlStateManager.func_179106_n();
            if (OpenGlHelper.func_176075_f())
                vbo.func_177358_a(GL11.GL_QUADS);
            else
                GlStateManager.func_179148_o(displayList);
            GlStateManager.func_187409_d(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GlStateManager.func_179132_a(true);
            GlStateManager.func_179098_w();
            GlStateManager.func_179127_m();
        }

        if (OpenGlHelper.func_176075_f())
        {
            vbo.func_177358_a(GL11.GL_QUADS);
            vbo.func_177361_b(); // Unbind buffer and disable pointers.
        }
        else
        {
            GlStateManager.func_179148_o(displayList);
        }

        buffer.limit(0);
        for (int i = 0; i < FORMAT.func_177345_h(); i++)
            FORMAT.func_177343_g().get(i).func_177375_c().postDraw(FORMAT, i, FORMAT.func_177338_f(), buffer);
        buffer.position(0);

        // Disable our coloring.
        GlStateManager.func_179138_g(OpenGlHelper.field_77476_b);
        GlStateManager.func_179090_x();
        GlStateManager.func_179138_g(OpenGlHelper.field_77478_a);

        // Reset texture matrix.
        GlStateManager.func_179128_n(GL11.GL_TEXTURE);
        GlStateManager.func_179096_D();
        GlStateManager.func_179128_n(GL11.GL_MODELVIEW);

        GlStateManager.func_179084_k();
        GlStateManager.func_179089_o();

        GlStateManager.func_179121_F();

        return true;
    }

    private void reloadTextures()
    {
        if (mc.field_71446_o != null)
        {
            mc.field_71446_o.func_110577_a(texture);
            texW = GlStateManager.func_187411_c(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            texH = GlStateManager.func_187411_c(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        }
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, @Nonnull Predicate<IResourceType> resourcePredicate)
    {
        if (resourcePredicate.test(VanillaResourceType.TEXTURES))
        {
            reloadTextures();
        }
    }
}
