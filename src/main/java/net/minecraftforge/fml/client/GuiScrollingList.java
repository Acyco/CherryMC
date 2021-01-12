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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public abstract class GuiScrollingList
{
    private final Minecraft client;
    protected final int listWidth;
    protected final int listHeight;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected final int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    private float scrollDistance;
    protected int selectedIndex = -1;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;
    protected boolean captureMouse = true;

    @Deprecated // We need to know screen size.
    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight)
    {
       this(client, width, height, top, bottom, left, entryHeight, width, height);
    }
    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight)
    {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Deprecated // Unused, remove in 1.9.3?
    public void func_27258_a(boolean p_27258_1_)
    {
        this.highlightSelected = p_27258_1_;
    }

    @Deprecated protected void func_27259_a(boolean hasFooter, int footerHeight){ setHeaderInfo(hasFooter, footerHeight); }
    protected void setHeaderInfo(boolean hasHeader, int headerHeight)
    {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader) this.headerHeight = 0;
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.headerHeight;
    }

    protected abstract void drawBackground();

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);

    @Deprecated protected void func_27260_a(int entryRight, int relativeY, Tessellator tess) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) { func_27260_a(entryRight, relativeY, tess); }

    @Deprecated protected void func_27255_a(int x, int y) {}
    protected void clickHeader(int x, int y) { func_27255_a(x, y); }

    @Deprecated protected void func_27257_b(int mouseX, int mouseY) {}
    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawScreen(int mouseX, int mouseY) { func_27257_b(mouseX, mouseY); }

    @Deprecated // Unused, Remove in 1.9.3?
    public int func_27256_c(int x, int y)
    {
        int left = this.left + 1;
        int right = this.left + this.listWidth - 7;
        int relativeY = y - this.top - this.headerHeight + (int)this.scrollDistance - 4;
        int entryIndex = relativeY / this.slotHeight;
        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex : -1;
    }

    // FIXME: is this correct/still needed?
    public void registerScrollButtons(List<GuiButton> buttons, int upActionID, int downActionID)
    {
        this.scrollUpActionId = upActionID;
        this.scrollDownActionId = downActionID;
    }

    private void applyScrollLimits()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);

        if (listHeight < 0)
        {
            listHeight /= 2;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float)listHeight)
        {
            this.scrollDistance = (float)listHeight;
        }
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.field_146124_l)
        {
            if (button.field_146127_k == this.scrollUpActionId)
            {
                this.scrollDistance -= (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
            else if (button.field_146127_k == this.scrollDownActionId)
            {
                this.scrollDistance += (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }
    }


    public void handleMouseInput(int mouseX, int mouseY) throws IOException
    {
        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                             mouseY >= this.top && mouseY <= this.bottom;
        if (!isHovering)
            return;

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0)
        {
            this.scrollDistance += (float)((-1 * scroll / 120.0F) * this.slotHeight / 2);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();

        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth &&
                             mouseY >= this.top && mouseY <= this.bottom;
        int listLength     = this.getSize();
        int scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft  = scrollBarRight - scrollBarWidth;
        int entryLeft      = this.left;
        int entryRight     = scrollBarLeft - 1;
        int viewHeight     = this.bottom - this.top;
        int border         = 4;

        if (Mouse.isButtonDown(0))
        {
            if (this.initialMouseClickY == -1.0F)
            {
                if (isHovering)
                {
                    int mouseListY = mouseY - this.top - this.headerHeight + (int)this.scrollDistance - border;
                    int slotIndex = mouseListY / this.slotHeight;

                    if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength)
                    {
                        this.elementClicked(slotIndex, slotIndex == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
                        this.selectedIndex = slotIndex;
                        this.lastClickTime = System.currentTimeMillis();
                    }
                    else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0)
                    {
                        this.clickHeader(mouseX - entryLeft, mouseY - this.top + (int)this.scrollDistance - border);
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
                    {
                        this.scrollFactor = -1.0F;
                        int scrollHeight = this.getContentHeight() - viewHeight - border;
                        if (scrollHeight < 1) scrollHeight = 1;

                        int var13 = (int)((float)(viewHeight * viewHeight) / (float)this.getContentHeight());

                        if (var13 < 32) var13 = 32;
                        if (var13 > viewHeight - border*2)
                            var13 = viewHeight - border*2;

                        this.scrollFactor /= (float)(viewHeight - var13) / (float)scrollHeight;
                    }
                    else
                    {
                        this.scrollFactor = 1.0F;
                    }

                    this.initialMouseClickY = mouseY;
                }
                else
                {
                    this.initialMouseClickY = -2.0F;
                }
            }
            else if (this.initialMouseClickY >= 0.0F)
            {
                this.scrollDistance -= ((float)mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float)mouseY;
            }
        }
        else
        {
            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();

        Tessellator tess = Tessellator.func_178181_a();
        BufferBuilder worldr = tess.func_178180_c();

        ScaledResolution res = new ScaledResolution(client);
        double scaleW = client.field_71443_c / res.func_78327_c();
        double scaleH = client.field_71440_d / res.func_78324_d();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)(left      * scaleW), (int)(client.field_71440_d - (bottom * scaleH)),
                       (int)(listWidth * scaleW), (int)(viewHeight * scaleH));

        if (this.client.field_71441_e != null)
        {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
        }
        else // Draw dark dirt background
        {
            GlStateManager.func_179140_f();
            GlStateManager.func_179106_n();
            this.client.field_71446_o.func_110577_a(Gui.field_110325_k);
            GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
            final float scale = 32.0F;
            worldr.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_181709_i);
            worldr.func_181662_b(this.left,  this.bottom, 0.0D).func_187315_a(this.left  / scale, (this.bottom + (int)this.scrollDistance) / scale).func_181669_b(0x20, 0x20, 0x20, 0xFF).func_181675_d();
            worldr.func_181662_b(this.right, this.bottom, 0.0D).func_187315_a(this.right / scale, (this.bottom + (int)this.scrollDistance) / scale).func_181669_b(0x20, 0x20, 0x20, 0xFF).func_181675_d();
            worldr.func_181662_b(this.right, this.top,    0.0D).func_187315_a(this.right / scale, (this.top    + (int)this.scrollDistance) / scale).func_181669_b(0x20, 0x20, 0x20, 0xFF).func_181675_d();
            worldr.func_181662_b(this.left,  this.top,    0.0D).func_187315_a(this.left  / scale, (this.top    + (int)this.scrollDistance) / scale).func_181669_b(0x20, 0x20, 0x20, 0xFF).func_181675_d();
            tess.func_78381_a();
        }

        int baseY = this.top + border - (int)this.scrollDistance;

        if (this.hasHeader) {
            this.drawHeader(entryRight, baseY, tess);
        }

        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
        {
            int slotTop = baseY + slotIdx * this.slotHeight + this.headerHeight;
            int slotBuffer = this.slotHeight - border;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
            {
                if (this.highlightSelected && this.isSelected(slotIdx))
                {
                    int min = this.left;
                    int max = entryRight;
                    GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.func_179090_x();
                    worldr.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_181709_i);
                    worldr.func_181662_b(min,     slotTop + slotBuffer + 2, 0).func_187315_a(0, 1).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
                    worldr.func_181662_b(max,     slotTop + slotBuffer + 2, 0).func_187315_a(1, 1).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
                    worldr.func_181662_b(max,     slotTop              - 2, 0).func_187315_a(1, 0).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
                    worldr.func_181662_b(min,     slotTop              - 2, 0).func_187315_a(0, 0).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
                    worldr.func_181662_b(min + 1, slotTop + slotBuffer + 1, 0).func_187315_a(0, 1).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
                    worldr.func_181662_b(max - 1, slotTop + slotBuffer + 1, 0).func_187315_a(1, 1).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
                    worldr.func_181662_b(max - 1, slotTop              - 1, 0).func_187315_a(1, 0).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
                    worldr.func_181662_b(min + 1, slotTop              - 1, 0).func_187315_a(0, 0).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
                    tess.func_78381_a();
                    GlStateManager.func_179098_w();
                }

                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
            }
        }

        GlStateManager.func_179097_i();

        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        if (extraHeight > 0)
        {
            int height = (viewHeight * viewHeight) / this.getContentHeight();

            if (height < 32) height = 32;

            if (height > viewHeight - border*2)
                height = viewHeight - border*2;

            int barTop = (int)this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            GlStateManager.func_179090_x();
            worldr.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_181709_i);
            worldr.func_181662_b(scrollBarLeft,  this.bottom, 0.0D).func_187315_a(0.0D, 1.0D).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight, this.bottom, 0.0D).func_187315_a(1.0D, 1.0D).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight, this.top,    0.0D).func_187315_a(1.0D, 0.0D).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarLeft,  this.top,    0.0D).func_187315_a(0.0D, 0.0D).func_181669_b(0x00, 0x00, 0x00, 0xFF).func_181675_d();
            tess.func_78381_a();
            worldr.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_181709_i);
            worldr.func_181662_b(scrollBarLeft,  barTop + height, 0.0D).func_187315_a(0.0D, 1.0D).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight, barTop + height, 0.0D).func_187315_a(1.0D, 1.0D).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight, barTop,          0.0D).func_187315_a(1.0D, 0.0D).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarLeft,  barTop,          0.0D).func_187315_a(0.0D, 0.0D).func_181669_b(0x80, 0x80, 0x80, 0xFF).func_181675_d();
            tess.func_78381_a();
            worldr.func_181668_a(GL11.GL_QUADS, DefaultVertexFormats.field_181709_i);
            worldr.func_181662_b(scrollBarLeft,      barTop + height - 1, 0.0D).func_187315_a(0.0D, 1.0D).func_181669_b(0xC0, 0xC0, 0xC0, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight - 1, barTop + height - 1, 0.0D).func_187315_a(1.0D, 1.0D).func_181669_b(0xC0, 0xC0, 0xC0, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarRight - 1, barTop,              0.0D).func_187315_a(1.0D, 0.0D).func_181669_b(0xC0, 0xC0, 0xC0, 0xFF).func_181675_d();
            worldr.func_181662_b(scrollBarLeft,      barTop,              0.0D).func_187315_a(0.0D, 0.0D).func_181669_b(0xC0, 0xC0, 0xC0, 0xFF).func_181675_d();
            tess.func_78381_a();
        }

        this.drawScreen(mouseX, mouseY);
        GlStateManager.func_179098_w();
        GlStateManager.func_179103_j(GL11.GL_FLAT);
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2)
    {
        GuiUtils.drawGradientRect(0, left, top, right, bottom, color1, color2);
    }
}
