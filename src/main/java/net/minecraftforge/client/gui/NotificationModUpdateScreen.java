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

package net.minecraftforge.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NotificationModUpdateScreen extends GuiScreen
{

    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation(ForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");

    private final GuiButton modButton;
    private Status showNotification = null;
    private boolean hasCheckedForUpdates = false;

    public NotificationModUpdateScreen(GuiButton modButton)
    {
        this.modButton = modButton;
    }

    @Override
    public void func_73866_w_()
    {
        if (!hasCheckedForUpdates)
        {
            if (modButton != null)
            {
                for (ModContainer mod : Loader.instance().getModList())
                {
                    Status status = ForgeVersion.getResult(mod).status;
                    if (status == Status.OUTDATED || status == Status.BETA_OUTDATED)
                    {
                        // TODO: Needs better visualization, maybe stacked icons
                        // drawn in a terrace-like pattern?
                        showNotification = Status.OUTDATED;
                    }
                }
            }
            hasCheckedForUpdates = true;
        }
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        if (showNotification == null || !showNotification.shouldDraw() || ForgeModContainer.disableVersionCheck)
        {
            return;
        }

        Minecraft.func_71410_x().func_110434_K().func_110577_a(VERSION_CHECK_ICONS);
        GlStateManager.func_179131_c(1, 1, 1, 1);
        GlStateManager.func_179094_E();

        int x = modButton.field_146128_h;
        int y = modButton.field_146129_i;
        int w = modButton.field_146120_f;
        int h = modButton.field_146121_g;

        func_146110_a(x + w - (h / 2 + 4), y + (h / 2 - 4), showNotification.getSheetOffset() * 8, (showNotification.isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0, 8, 8, 64, 16);
        GlStateManager.func_179121_F();
    }

    public static NotificationModUpdateScreen init(GuiMainMenu guiMainMenu, GuiButton modButton)
    {
        NotificationModUpdateScreen notificationModUpdateScreen = new NotificationModUpdateScreen(modButton);
        notificationModUpdateScreen.func_183500_a(guiMainMenu.field_146294_l, guiMainMenu.field_146295_m);
        notificationModUpdateScreen.func_73866_w_();
        return notificationModUpdateScreen;
    }

}
