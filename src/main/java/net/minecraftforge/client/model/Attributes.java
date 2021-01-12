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

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;

public class Attributes
{
    /*
     * Default format of the data in IBakedModel
     */
    public static final VertexFormat DEFAULT_BAKED_FORMAT;

    static
    {
        DEFAULT_BAKED_FORMAT = new VertexFormat();
        DEFAULT_BAKED_FORMAT.func_181721_a(new VertexFormatElement(0, EnumType.FLOAT, EnumUsage.POSITION, 3));
        DEFAULT_BAKED_FORMAT.func_181721_a(new VertexFormatElement(0, EnumType.UBYTE, EnumUsage.COLOR,    4));
        DEFAULT_BAKED_FORMAT.func_181721_a(new VertexFormatElement(0, EnumType.FLOAT, EnumUsage.UV,       2));
        DEFAULT_BAKED_FORMAT.func_181721_a(new VertexFormatElement(0, EnumType.BYTE,  EnumUsage.PADDING,  4));
    }

    /*
     * Can first format be used where second is expected
     */
    public static boolean moreSpecific(VertexFormat first, VertexFormat second)
    {
        int size = first.func_177338_f();
        if(size != second.func_177338_f()) return false;

        int padding = 0;
        int j = 0;
        for(VertexFormatElement firstAttr : first.func_177343_g())
        {
            while(j < second.func_177345_h() && second.func_177348_c(j).func_177375_c() == EnumUsage.PADDING)
            {
                padding += second.func_177348_c(j++).func_177368_f();
            }
            if(j >= second.func_177345_h() && padding == 0)
            {
                // if no padding is left, but there are still elements in first (we're processing one) - it doesn't fit
                return false;
            }
            if(padding == 0)
            {
                // no padding - attributes have to match
                VertexFormatElement secondAttr = second.func_177348_c(j++);
                if(
                    firstAttr.func_177369_e() != secondAttr.func_177369_e() ||
                    firstAttr.func_177370_d() != secondAttr.func_177370_d() ||
                    firstAttr.func_177367_b() != secondAttr.func_177367_b() ||
                    firstAttr.func_177375_c() != secondAttr.func_177375_c())
                {
                    return false;
                }
            }
            else
            {
                // padding - attribute should fit in it
                padding -= firstAttr.func_177368_f();
                if(padding < 0) return false;
            }
        }

        if(padding != 0 || j != second.func_177345_h()) return false;
        return true;
    }
}
