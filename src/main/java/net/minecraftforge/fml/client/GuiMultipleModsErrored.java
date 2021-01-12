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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.MissingModsException;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.MultipleModsErrored;
import net.minecraftforge.fml.common.WrongMinecraftVersionException;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiMultipleModsErrored extends GuiErrorBase
{
    private final List<WrongMinecraftVersionException> wrongMinecraftExceptions;
    private final List<MissingModsException> missingModsExceptions;
    private GuiList list;

    public GuiMultipleModsErrored(MultipleModsErrored exception)
    {
        wrongMinecraftExceptions = exception.wrongMinecraftExceptions;
        missingModsExceptions = exception.missingModsExceptions;
    }

    @Override
    public void func_73866_w_()
    {
        super.func_73866_w_();
        int additionalSize = missingModsExceptions.isEmpty() || wrongMinecraftExceptions.isEmpty() ? 20 : 55;
        for (MissingModsException exception : missingModsExceptions)
        {
            additionalSize += exception.getMissingModInfos().size() * 10;
        }
        list = new GuiList(wrongMinecraftExceptions.size() * 10 + missingModsExceptions.size() * 15 + additionalSize);
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        this.func_73732_a(this.field_146289_q, I18n.func_135052_a("fml.messages.mod.missing.multiple", missingModsExceptions.size() + wrongMinecraftExceptions.size()), this.field_146294_l/2, 10, 0xFFFFFF);
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }

    @Override
    public void func_146284_a(GuiButton button)
    {
        this.list.actionPerformed(button);
        super.func_146284_a(button);
    }

    @Override
    public void func_146274_d() throws IOException
    {
        super.func_146274_d();
        int mouseX = Mouse.getEventX() * this.field_146294_l / this.field_146297_k.field_71443_c;
        int mouseY = this.field_146295_m - Mouse.getEventY() * this.field_146295_m / this.field_146297_k.field_71440_d - 1;
        this.list.handleMouseInput(mouseX, mouseY);
    }

    private class GuiList extends GuiScrollingList
    {
        public GuiList(int entryHeight)
        {
            super(GuiMultipleModsErrored.this.field_146297_k,
                  GuiMultipleModsErrored.this.field_146294_l-20,
                  GuiMultipleModsErrored.this.field_146295_m -30,
                  30, GuiMultipleModsErrored.this.field_146295_m-50,
                    10,
                  entryHeight,
                  GuiMultipleModsErrored.this.field_146294_l,
                  GuiMultipleModsErrored.this.field_146295_m);
        }

        @Override
        protected int getSize()
        {
            return 1;
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }

        @Override
        protected void drawBackground()
        {
            func_146276_q_();
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess)
        {
            int offset = slotTop;
            FontRenderer renderer = GuiMultipleModsErrored.this.field_146289_q;
            if (!wrongMinecraftExceptions.isEmpty())
            {
                renderer.func_78276_b(TextFormatting.UNDERLINE + I18n.func_135052_a("fml.messages.mod.wrongminecraft", Loader.instance().getMinecraftModContainer().getVersion()), this.left, offset, 0xFFFFFF);
                offset+=15;
                for(WrongMinecraftVersionException exception : wrongMinecraftExceptions)
                {
                    renderer.func_78276_b(I18n.func_135052_a("fml.messages.mod.wrongminecraft.requirement", TextFormatting.BOLD + exception.mod.getName() + TextFormatting.RESET, exception.mod.getModId(), exception.mod.acceptableMinecraftVersionRange().toStringFriendly()), this.left, offset, 0xFFFFFF);
                    offset += 10;
                }
                offset+=5;
                renderer.func_78276_b(I18n.func_135052_a("fml.messages.mod.wrongminecraft.fix.multiple"), this.left, offset, 0xFFFFFF);
                offset+=20;
            }
            if (!missingModsExceptions.isEmpty())
            {
                renderer.func_78276_b(I18n.func_135052_a("fml.messages.mod.missing.dependencies.multiple.issues"), this.left, offset, 0xFFFFFF);
                offset+=15;
                for (MissingModsException exception : missingModsExceptions)
                {
                    renderer.func_78276_b(exception.getModName() + ":", this.left, offset, 0xFFFFFF);
                    for (MissingModsException.MissingModInfo versionInfo : exception.getMissingModInfos())
                    {
                        ArtifactVersion acceptedVersion = versionInfo.getAcceptedVersion();
                        String acceptedModId = acceptedVersion.getLabel();
                        ArtifactVersion currentVersion = versionInfo.getCurrentVersion();
                        String missingReason;
                        if (currentVersion == null)
                        {
                            missingReason = I18n.func_135052_a("fml.messages.mod.missing.dependencies.missing");
                        }
                        else
                        {
                            missingReason = I18n.func_135052_a("fml.messages.mod.missing.dependencies.you.have", currentVersion.getVersionString());
                        }
                        String acceptedModVersionString = acceptedVersion.getRangeString();
                        if (acceptedVersion instanceof DefaultArtifactVersion)
                        {
                            DefaultArtifactVersion dav = (DefaultArtifactVersion)acceptedVersion;
                            if (dav.getRange() != null)
                            {
                                acceptedModVersionString = dav.getRange().toStringFriendly();
                            }
                        }
                        ModContainer acceptedMod = Loader.instance().getIndexedModList().get(acceptedModId);
                        String acceptedModName = acceptedMod != null ? acceptedMod.getName() : acceptedModId;
                        String versionInfoText = String.format(TextFormatting.BOLD + "%s " + TextFormatting.RESET + "%s (%s)", acceptedModName, acceptedModVersionString, missingReason);
                        String message;
                        if (versionInfo.isRequired())
                        {
                            message = I18n.func_135052_a("fml.messages.mod.missing.dependencies.requires", versionInfoText);
                        }
                        else
                        {
                            message = I18n.func_135052_a("fml.messages.mod.missing.dependencies.compatible.with", versionInfoText);
                        }
                        offset += 10;
                        renderer.func_78276_b(message, this.left, offset, 0xEEEEEE);
                    }

                    offset += 15;
                }
            }
        }
    }
}
