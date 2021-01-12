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
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;

import java.awt.*;
import java.io.File;

public class GuiErrorBase extends GuiErrorScreen
{
    static final File minecraftDir = new File(Loader.instance().getConfigDir().getParent());
    static final File logFile = new File(minecraftDir, "logs/latest.log");
    public GuiErrorBase()
    {
        super(null, null);
    }

    private String translateOrDefault(String translateKey, String alternative, Object... format)
    {
        return I18n.func_188566_a(translateKey) ? I18n.func_135052_a(translateKey, format) : String.format(alternative, format); //When throwing a DuplicateModsException, the translation system does not work...
    }

    @Override
    public void func_73866_w_()
    {
        super.func_73866_w_();
        this.field_146292_n.clear();
        this.field_146292_n.add(new GuiButton(10, 50, this.field_146295_m - 38, this.field_146294_l / 2 - 55, 20, translateOrDefault("fml.button.open.mods.folder", "Open Mods Folder")));
        String openFileText = translateOrDefault("fml.button.open.file", "Open %s", logFile.getName());
        this.field_146292_n.add(new GuiButton(11, this.field_146294_l / 2 + 5, this.field_146295_m - 38, this.field_146294_l / 2 - 55, 20, openFileText));
    }

    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146127_k == 10)
        {
            try
            {
                File modsDir = new File(minecraftDir, "mods");
                Desktop.getDesktop().open(modsDir);
            }
            catch (Exception e)
            {
                FMLLog.log.error("Problem opening mods folder", e);
            }
        }
        else if (button.field_146127_k == 11)
        {
            try
            {
                Desktop.getDesktop().open(logFile);
            }
            catch (Exception e)
            {
                FMLLog.log.error("Problem opening log file {}", logFile, e);
            }
        }
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        for(GuiButton button : field_146292_n)
        {
            button.func_191745_a(this.field_146297_k, mouseX, mouseY, partialTicks);
        }
    }
}
