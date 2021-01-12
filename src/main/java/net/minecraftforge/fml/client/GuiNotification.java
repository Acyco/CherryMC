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

package net.minecraftforge.fml.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.StartupQuery;

public class GuiNotification extends GuiScreen
{
    public GuiNotification(StartupQuery query)
    {
        this.query = query;
    }

    @Override
    public void func_73866_w_()
    {
        this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m - 38, I18n.func_135052_a("gui.done")));
    }

    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146124_l && button.field_146127_k == 0)
        {
            FMLClientHandler.instance().showGuiScreen(null);
            query.finish();
        }
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();

        String[] lines = query.getText().split("\n");

        int spaceAvailable = this.field_146295_m - 38 - 20;
        int spaceRequired = Math.min(spaceAvailable, 10 + 10 * lines.length);

        int offset = 10 + (spaceAvailable - spaceRequired) / 2; // vertically centered

        for (String line : lines)
        {
            if (offset >= spaceAvailable)
            {
                this.func_73732_a(this.field_146289_q, "...", this.field_146294_l / 2, offset, 0xFFFFFF);
                break;
            }
            else
            {
                if (!line.isEmpty()) this.func_73732_a(this.field_146289_q, line, this.field_146294_l / 2, offset, 0xFFFFFF);
                offset += 10;
            }
        }

        super.func_73863_a(mouseX, mouseY, partialTicks);
    }

    protected final StartupQuery query;
}
