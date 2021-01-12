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

/**
 * This class provides a checkbox style control.
 *
 * @author bspkrs
 */
public class GuiCheckBox extends GuiButton
{
    private boolean isChecked;
    private int     boxWidth;

    public GuiCheckBox(int id, int xPos, int yPos, String displayString, boolean isChecked)
    {
        super(id, xPos, yPos, displayString);
        this.isChecked = isChecked;
        this.boxWidth = 11;
        this.field_146121_g = 11;
        this.field_146120_f = this.boxWidth + 2 + Minecraft.func_71410_x().field_71466_p.func_78256_a(displayString);
    }

    @Override
    public void func_191745_a(Minecraft mc, int mouseX, int mouseY, float partial)
    {
        if (this.field_146125_m)
        {
            this.field_146123_n = mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.boxWidth && mouseY < this.field_146129_i + this.field_146121_g;
            GuiUtils.drawContinuousTexturedBox(field_146122_a, this.field_146128_h, this.field_146129_i, 0, 46, this.boxWidth, this.field_146121_g, 200, 20, 2, 3, 2, 2, this.field_73735_i);
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

            if (this.isChecked)
                this.func_73732_a(mc.field_71466_p, "x", this.field_146128_h + this.boxWidth / 2 + 1, this.field_146129_i + 1, 14737632);

            this.func_73731_b(mc.field_71466_p, field_146126_j, this.field_146128_h + this.boxWidth + 2, this.field_146129_i + 2, color);
        }
    }

    @Override
    public boolean func_146116_c(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.field_146124_l && this.field_146125_m && mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g)
        {
            this.isChecked = !this.isChecked;
            return true;
        }

        return false;
    }

    public boolean isChecked()
    {
        return this.isChecked;
    }

    public void setIsChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
    }
}
