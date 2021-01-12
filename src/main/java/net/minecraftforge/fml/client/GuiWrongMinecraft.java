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

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.WrongMinecraftVersionException;

public class GuiWrongMinecraft extends GuiErrorBase
{
    private WrongMinecraftVersionException wrongMC;

    public GuiWrongMinecraft(WrongMinecraftVersionException wrongMC)
    {
        this.wrongMC = wrongMC;
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        int offset = 75;
        this.func_73732_a(this.field_146289_q, I18n.func_135052_a("fml.messages.mod.wrongminecraft", Loader.instance().getMinecraftModContainer().getVersion()), this.field_146294_l / 2, offset, 0xFFFFFF);
        offset+=15;
        this.func_73732_a(this.field_146289_q, I18n.func_135052_a("fml.messages.mod.wrongminecraft.requirement", TextFormatting.BOLD + wrongMC.mod.getName() + TextFormatting.RESET, wrongMC.mod.getModId(), wrongMC.mod.acceptableMinecraftVersionRange().toStringFriendly()), this.field_146294_l / 2, offset, 0xEEEEEE);
        offset+=15;
        this.func_73732_a(this.field_146289_q, I18n.func_135052_a("fml.messages.mod.wrongminecraft.fix", wrongMC.mod.getName()),this.field_146294_l/2, offset, 0xFFFFFF);
        offset+=20;
        this.func_73732_a(this.field_146289_q, I18n.func_135052_a("fml.messages.mod.missing.dependencies.see.log", GuiErrorBase.logFile.getName()), this.field_146294_l / 2, offset, 0xFFFFFF);
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }
}
