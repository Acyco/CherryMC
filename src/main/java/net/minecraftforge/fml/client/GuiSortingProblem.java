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

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.toposort.ModSortingException;
import net.minecraftforge.fml.common.toposort.ModSortingException.SortingExceptionData;

public class GuiSortingProblem extends GuiScreen {
    private SortingExceptionData<ModContainer> failedList;

    public GuiSortingProblem(ModSortingException modSorting)
    {
        this.failedList = modSorting.getExceptionData();
    }

    @Override
    public void func_73866_w_()
    {
        super.func_73866_w_();
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks)
    {
        this.func_146276_q_();
        int offset = Math.max(85 - (failedList.getVisitedNodes().size() + 3) * 10, 10);
        this.func_73732_a(this.field_146289_q, "Forge Mod Loader has found a problem with your minecraft installation", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset+=10;
        this.func_73732_a(this.field_146289_q, "A mod sorting cycle was detected and loading cannot continue", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset+=10;
        this.func_73732_a(this.field_146289_q, String.format("The first mod in the cycle is %s", failedList.getFirstBadNode()), this.field_146294_l / 2, offset, 0xFFFFFF);
        offset+=10;
        this.func_73732_a(this.field_146289_q, "The remainder of the cycle involves these mods", this.field_146294_l / 2, offset, 0xFFFFFF);
        offset+=5;
        for (ModContainer mc : failedList.getVisitedNodes())
        {
            offset+=10;
            this.func_73732_a(this.field_146289_q, String.format("%s : before: %s, after: %s", mc.toString(), mc.getDependants(), mc.getDependencies()), this.field_146294_l / 2, offset, 0xEEEEEE);
        }
        offset+=20;
        this.func_73732_a(this.field_146289_q, "The file 'ForgeModLoader-client-0.log' contains more information", this.field_146294_l / 2, offset, 0xFFFFFF);
    }

}
