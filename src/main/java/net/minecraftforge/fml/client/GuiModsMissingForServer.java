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

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.MissingModsException;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;

public class GuiModsMissingForServer extends GuiScreen
{
    private MissingModsException modsMissing;

    public GuiModsMissingForServer(MissingModsException modsMissing)
    {
        this.modsMissing = modsMissing;
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
            FMLClientHandler.instance().showGuiScreen(null);
        }
    }
    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        List<MissingModsException.MissingModInfo> missingModsVersions = modsMissing.getMissingModInfos();
        int offset = Math.max(85 - missingModsVersions.size() * 10, 10);
        this.func_73732_a(this.field_146289_q, "Forge Mod Loader could not connect to this server", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset += 10;
        this.func_73732_a(this.field_146289_q, "The mods and versions listed below could not be found", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset += 10;
        this.func_73732_a(this.field_146289_q, "They are required to play on this server", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset += 5;
        for (MissingModsException.MissingModInfo info : missingModsVersions)
        {
            ArtifactVersion v = info.getAcceptedVersion();
            offset += 10;
            this.func_73732_a(this.field_146289_q, String.format("%s : %s", v.getLabel(), v.getRangeString()), this.field_146294_l / 2, offset, 0xEEEEEE);
        }
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }
}
