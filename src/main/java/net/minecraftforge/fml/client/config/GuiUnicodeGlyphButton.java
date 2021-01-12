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

package net.minecraftforge.fml.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

/**
 * This class provides a button that shows a string glyph at the beginning. The glyph can be scaled using the glyphScale parameter.
 *
 * @author bspkrs
 */
public class GuiUnicodeGlyphButton extends GuiButtonExt
{
    public String glyph;
    public float  glyphScale;

    public GuiUnicodeGlyphButton(int id, int xPos, int yPos, int width, int height, String displayString, String glyph, float glyphScale)
    {
        super(id, xPos, yPos, width, height, displayString);
        this.glyph = glyph;
        this.glyphScale = glyphScale;
    }

    @Override
    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        if (this.field_146125_m)
        {
            this.field_146123_n = mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g;
            int k = this.func_146114_a(this.field_146123_n);
            GuiUtils.drawContinuousTexturedBox(GuiButton.field_146122_a, this.field_146128_h, this.field_146129_i, 0, 46 + k * 20, this.field_146120_f, this.field_146121_g, 200, 20, 2, 3, 2, 2, this.field_73735_i);
            this.func_146119_b(mc, mouseX, mouseY);
            int color = 14737632;

            if (packedFGColour != 0)
            {
                color = packedFGColour;
            }
            else if (!this.field_146124_l)
            {
                color = 10526880;
            }
            else if (this.field_146123_n)
            {
                color = 16777120;
            }

            String buttonText = this.field_146126_j;
            int glyphWidth = (int) (mc.field_71466_p.func_78256_a(glyph) * glyphScale);
            int strWidth = mc.field_71466_p.func_78256_a(buttonText);
            int ellipsisWidth = mc.field_71466_p.func_78256_a("...");
            int totalWidth = strWidth + glyphWidth;

            if (totalWidth > field_146120_f - 6 && totalWidth > ellipsisWidth)
                buttonText = mc.field_71466_p.func_78269_a(buttonText, field_146120_f - 6 - ellipsisWidth).trim() + "...";

            strWidth = mc.field_71466_p.func_78256_a(buttonText);
            totalWidth = glyphWidth + strWidth;

            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a(glyphScale, glyphScale, 1.0F);
            this.func_73732_a(mc.field_71466_p, glyph,
                    (int) (((this.field_146128_h + (this.field_146120_f / 2) - (strWidth / 2)) / glyphScale) - (glyphWidth / (2 * glyphScale)) + 2),
                    (int) (((this.field_146129_i + ((this.field_146121_g - 8) / glyphScale) / 2) - 1) / glyphScale), color);
            GlStateManager.func_179121_F();

            this.func_73732_a(mc.field_71466_p, buttonText, (int) (this.field_146128_h + (this.field_146120_f / 2) + (glyphWidth / glyphScale)),
                    this.field_146129_i + (this.field_146121_g - 8) / 2, color);
        }
    }
}
