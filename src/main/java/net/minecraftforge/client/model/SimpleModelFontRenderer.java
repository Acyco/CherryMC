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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public abstract class SimpleModelFontRenderer extends FontRenderer {

    private float r, g, b, a;
    private final TRSRTransformation transform;
    private ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
    private final VertexFormat format;
    private final Vector3f normal = new Vector3f(0, 0, 1);
    private final EnumFacing orientation;
    private boolean fillBlanks = false;

    private TextureAtlasSprite sprite;

    public SimpleModelFontRenderer(GameSettings settings, ResourceLocation font, TextureManager manager, boolean isUnicode, Matrix4f matrix, VertexFormat format)
    {
        super(settings, font, manager, isUnicode);
        this.transform = new TRSRTransformation(matrix);
        this.format = format;
        transform.transformNormal(normal);
        orientation = EnumFacing.func_176737_a(normal.x, normal.y, normal.z);
    }

    public void setSprite(TextureAtlasSprite sprite)
    {
        this.sprite = sprite;
        super.func_110549_a(null);
    }

    public void setFillBlanks(boolean fillBlanks)
    {
        this.fillBlanks = fillBlanks;
    }

    @Override
    protected float func_78266_a(int pos, boolean italic)
    {
        float x = (pos % 16) / 16f;
        float y = (pos / 16) / 16f;
        float sh = italic ? 1f : 0f;
        float w = field_78286_d[pos] - 1.01f;
        float h = field_78288_b - 1.01f;
        float wt = w  / 128f;
        float ht = h  / 128f;

        UnpackedBakedQuad.Builder quadBuilder = new UnpackedBakedQuad.Builder(format);
        quadBuilder.setTexture(sprite);
        quadBuilder.setQuadOrientation(orientation);

        addVertex(quadBuilder, field_78295_j + sh,     field_78296_k,     x,      y);
        addVertex(quadBuilder, field_78295_j - sh,     field_78296_k + h, x,      y + ht);
        addVertex(quadBuilder, field_78295_j + w + sh, field_78296_k + h, x + wt, y + ht);
        addVertex(quadBuilder, field_78295_j + w - sh, field_78296_k,     x + wt, y);
        builder.add(quadBuilder.build());

        if(fillBlanks)
        {
            float cuv = 15f / 16f;

            quadBuilder = new UnpackedBakedQuad.Builder(format);
            quadBuilder.setTexture(sprite);
            quadBuilder.setQuadOrientation(orientation);

            addVertex(quadBuilder, field_78295_j + w + sh,              field_78296_k,     cuv, cuv);
            addVertex(quadBuilder, field_78295_j + w - sh,              field_78296_k + h, cuv, cuv);
            addVertex(quadBuilder, field_78295_j + field_78286_d[pos] + sh, field_78296_k + h, cuv, cuv);
            addVertex(quadBuilder, field_78295_j + field_78286_d[pos] - sh, field_78296_k,     cuv, cuv);
            builder.add(quadBuilder.build());

            quadBuilder = new UnpackedBakedQuad.Builder(format);
            quadBuilder.setTexture(sprite);
            quadBuilder.setQuadOrientation(orientation);

            addVertex(quadBuilder, field_78295_j + sh,                  field_78296_k + h,           cuv, cuv);
            addVertex(quadBuilder, field_78295_j - sh,                  field_78296_k + field_78288_b, cuv, cuv);
            addVertex(quadBuilder, field_78295_j + field_78286_d[pos] + sh, field_78296_k + field_78288_b, cuv, cuv);
            addVertex(quadBuilder, field_78295_j + field_78286_d[pos] - sh, field_78296_k + h,           cuv, cuv);
            builder.add(quadBuilder.build());
        }
        return field_78286_d[pos];
    }

    private final Vector4f vec = new Vector4f();

    private void addVertex(UnpackedBakedQuad.Builder quadBuilder, float x, float y, float u, float v)
    {
        for(int e = 0; e < format.func_177345_h(); e++)
        {
            switch(format.func_177348_c(e).func_177375_c())
            {
                case POSITION:
                    vec.set(x, y, 0f, 1f);
                    transform.transformPosition(vec);
                    quadBuilder.put(e, vec.x, vec.y, vec.z, vec.w);
                    break;
                case COLOR:
                    quadBuilder.put(e, r, g, b, a);
                    break;
                case NORMAL:
                    //quadBuilder.put(e, normal.x, normal.y, normal.z, 1);
                    quadBuilder.put(e, 0, 0, 1, 1);
                    break;
                case UV:
                    if(format.func_177348_c(e).func_177369_e() == 0)
                    {
                        quadBuilder.put(e, sprite.func_94214_a(u * 16), sprite.func_94207_b(v * 16), 0, 1);
                        break;
                    }
                    // else fallthrough to default
                default:
                    quadBuilder.put(e);
                    break;
            }
        }
    }

    @Override
    public void func_110549_a(IResourceManager resourceManager)
    {
        super.func_110549_a(resourceManager);
        String p = field_111273_g.func_110623_a();
        if(p.startsWith("textures/")) p = p.substring("textures/".length(), p.length());
        if(p.endsWith(".png")) p = p.substring(0, p.length() - ".png".length());
        String f = field_111273_g.func_110624_b() + ":" + p;
        sprite = Minecraft.func_71410_x().func_147117_R().func_110572_b(f);
    }

    @Override
    protected abstract float func_78277_a(char c, boolean italic);

    @Override
    protected void doDraw(float shift)
    {
        field_78295_j += (int)shift;
    }

    @Override
    protected void setColor(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override public void enableAlpha()
    {
    }

    @Override
    protected void bindTexture(ResourceLocation location)
    {
    }

    public ImmutableList<BakedQuad> build()
    {
        ImmutableList<BakedQuad> ret = builder.build();
        builder = ImmutableList.builder();
        return ret;
    }
}
