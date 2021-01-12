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
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.StartupQuery;

public class GuiConfirmation extends GuiNotification
{
    public GuiConfirmation(StartupQuery query)
    {
        super(query);
    }

    @Override
    public void func_73866_w_()
    {
        this.field_146292_n.add(new GuiOptionButton(0, this.field_146294_l / 2 - 155, this.field_146295_m - 38, I18n.func_135052_a("gui.yes")));
        this.field_146292_n.add(new GuiOptionButton(1, this.field_146294_l / 2 - 155 + 160, this.field_146295_m - 38, I18n.func_135052_a("gui.no")));
    }

    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146124_l && (button.field_146127_k == 0 || button.field_146127_k == 1))
        {
            FMLClientHandler.instance().showGuiScreen(null);
            query.setResult(button.field_146127_k == 0);
            query.finish();
        }
    }
}
