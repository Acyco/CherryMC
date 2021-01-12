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

import java.io.File;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiBackupFailed extends GuiScreen
{
    private GuiScreen parent;
    private File zipName;
    public GuiBackupFailed(GuiScreen parent, File zipName)
    {
        this.parent = parent;
        this.zipName = zipName;
    }

    @Override
    public void func_73866_w_()
    {
        this.field_146292_n.add(new GuiButton(1, this.field_146294_l / 2 - 75, this.field_146295_m - 38, I18n.func_135052_a("gui.done")));
    }

    @Override
    protected void func_146284_a(GuiButton p_73875_1_)
    {
        if (p_73875_1_.field_146124_l && p_73875_1_.field_146127_k == 1)
        {
            FMLClientHandler.instance().showGuiScreen(parent);
        }
    }
    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        int offset = Math.max(85 - 2 * 10, 10);
        this.func_73732_a(this.field_146289_q, String.format("There was an error saving the archive %s", zipName.getName()), this.field_146294_l / 2, offset, 0xFFFFFF);
        offset += 10;
        this.func_73732_a(this.field_146289_q, String.format("Please fix the problem and try again"), this.field_146294_l / 2, offset, 0xFFFFFF);
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }
}
