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
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.ZipperUtil;

public class GuiOldSaveLoadConfirm extends GuiYesNo implements GuiYesNoCallback {

    private String dirName;
    private String saveName;
    private File zip;
    private GuiScreen parent;
    public GuiOldSaveLoadConfirm(String dirName, String saveName, GuiScreen parent)
    {
        super(null, "", "", 0);
        this.parent = parent;
        this.dirName = dirName;
        this.saveName = saveName;
        this.zip = new File(FMLClientHandler.instance().getClient().field_71412_D,String.format("%s-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.zip", dirName, System.currentTimeMillis()));
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        this.func_73732_a(this.field_146289_q, String.format("The world %s contains pre-update modding data", saveName), this.field_146294_l / 2, 50, 16777215);
        this.func_73732_a(this.field_146289_q, String.format("There may be problems updating it to this version"), this.field_146294_l / 2, 70, 16777215);
        this.func_73732_a(this.field_146289_q, String.format("FML will save a zip to %s", zip.getName()), this.field_146294_l / 2, 90, 16777215);
        this.func_73732_a(this.field_146289_q, String.format("Do you wish to continue loading?"), this.field_146294_l / 2, 110, 16777215);
        int k;

        for (k = 0; k < this.field_146292_n.size(); ++k)
        {
            this.field_146292_n.get(k).func_191745_a(this.field_146297_k, mouseX, mouseY, partialTicks);
        }

        for (k = 0; k < this.field_146293_o.size(); ++k)
        {
            this.field_146293_o.get(k).func_146159_a(this.field_146297_k, mouseX, mouseY);
        }
    }
    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146127_k == 1)
        {
            FMLClientHandler.instance().showGuiScreen(parent);
        }
        else
        {
            FMLLog.log.info("Capturing current state of world {} into file {}", saveName, zip.getAbsolutePath());
            try
            {
                String skip = System.getProperty("fml.doNotBackup");
                if (skip == null || !"true".equals(skip))
                {
                    ZipperUtil.zip(new File(FMLClientHandler.instance().getSavesDir(), dirName), zip);
                }
                else
                {
                    for (int x = 0; x < 10; x++)
                        FMLLog.log.fatal("!!!!!!!!!! UPDATING WORLD WITHOUT DOING BACKUP !!!!!!!!!!!!!!!!");
                }
            } catch (IOException e)
            {
                FMLLog.log.warn("There was a problem saving the backup {}. Please fix and try again", zip.getName(), e);
                FMLClientHandler.instance().showGuiScreen(new GuiBackupFailed(parent, zip));
                return;
            }
            FMLClientHandler.instance().showGuiScreen(null);

            try
            {
                field_146297_k.func_71371_a(dirName, saveName, null);
            }
            catch (StartupQuery.AbortedException e)
            {
                // ignore
            }
        }
    }
}
