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

package net.minecraftforge.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

public class GuiIngameForge extends GuiIngame
{
    //private static final ResourceLocation VIGNETTE     = new ResourceLocation("textures/misc/vignette.png");
    //private static final ResourceLocation WIDGITS      = new ResourceLocation("textures/gui/widgets.png");
    //private static final ResourceLocation PUMPKIN_BLUR = new ResourceLocation("textures/misc/pumpkinblur.png");

    private static final int WHITE = 0xFFFFFF;

    //Flags to toggle the rendering of certain aspects of the HUD, valid conditions
    //must be met for them to render normally. If those conditions are met, but this flag
    //is false, they will not be rendered.
    public static boolean renderVignette = true;
    public static boolean renderHelmet = true;
    public static boolean renderPortal = true;
    public static boolean renderHotbar = true;
    public static boolean renderCrosshairs = true;
    public static boolean renderBossHealth = true;
    public static boolean renderHealth = true;
    public static boolean renderArmor = true;
    public static boolean renderFood = true;
    public static boolean renderHealthMount = true;
    public static boolean renderAir = true;
    public static boolean renderExperiance = true;
    public static boolean renderJumpBar = true;
    public static boolean renderObjective = true;

    public static int left_height = 39;
    public static int right_height = 39;

    private ScaledResolution res = null;
    private FontRenderer fontrenderer = null;
    private RenderGameOverlayEvent eventParent;
    //private static final String MC_VERSION = MinecraftForge.MC_VERSION;
    private GuiOverlayDebugForge debugOverlay;

    public GuiIngameForge(Minecraft mc)
    {
        super(mc);
        debugOverlay = new GuiOverlayDebugForge(mc);
    }

    @Override
    public void func_175180_a(float partialTicks)
    {
        res = new ScaledResolution(field_73839_d);
        eventParent = new RenderGameOverlayEvent(partialTicks, res);
        int width = res.func_78326_a();
        int height = res.func_78328_b();
        renderHealthMount = field_73839_d.field_71439_g.func_184187_bx() instanceof EntityLivingBase;
        renderFood = field_73839_d.field_71439_g.func_184187_bx() == null;
        renderJumpBar = field_73839_d.field_71439_g.func_110317_t();

        right_height = 39;
        left_height = 39;

        if (pre(ALL)) return;

        fontrenderer = field_73839_d.field_71466_p;
        field_73839_d.field_71460_t.func_78478_c();
        GlStateManager.func_179147_l();

        if (renderVignette && Minecraft.func_71375_t())
        {
            func_180480_a(field_73839_d.field_71439_g.func_70013_c(), res);
        }
        else
        {
            GlStateManager.func_179126_j();
            GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        if (renderHelmet) renderHelmet(res, partialTicks);

        if (renderPortal && !field_73839_d.field_71439_g.func_70644_a(MobEffects.field_76431_k))
        {
            renderPortal(res, partialTicks);
        }

        if (renderHotbar) func_180479_a(res, partialTicks);

        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
        field_73735_i = -90.0F;
        field_73842_c.setSeed((long)(field_73837_f * 312871));

        if (renderCrosshairs) renderCrosshairs(partialTicks);
        if (renderBossHealth) renderBossHealth();

        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.field_73839_d.field_71442_b.func_78755_b() && this.field_73839_d.func_175606_aa() instanceof EntityPlayer)
        {
            if (renderHealth) renderHealth(width, height);
            if (renderArmor)  renderArmor(width, height);
            if (renderFood)   renderFood(width, height);
            if (renderHealthMount) renderHealthMount(width, height);
            if (renderAir)    renderAir(width, height);
        }

        renderSleepFade(width, height);

        if (renderJumpBar)
        {
            renderJumpBar(width, height);
        }
        else if (renderExperiance)
        {
            renderExperience(width, height);
        }

        renderToolHighlight(res);
        renderHUDText(width, height);
        renderFPSGraph();
        renderPotionIcons(res);
        renderRecordOverlay(width, height, partialTicks);
        renderSubtitles(res);
        renderTitle(width, height, partialTicks);


        Scoreboard scoreboard = this.field_73839_d.field_71441_e.func_96441_U();
        ScoreObjective objective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.func_96509_i(field_73839_d.field_71439_g.func_70005_c_());
        if (scoreplayerteam != null)
        {
            int slot = scoreplayerteam.func_178775_l().func_175746_b();
            if (slot >= 0) objective = scoreboard.func_96539_a(3 + slot);
        }
        ScoreObjective scoreobjective1 = objective != null ? objective : scoreboard.func_96539_a(1);
        if (renderObjective && scoreobjective1 != null)
        {
            this.func_180475_a(scoreobjective1, res);
        }

        GlStateManager.func_179147_l();
        GlStateManager.func_179120_a(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.func_179118_c();

        renderChat(width, height);

        renderPlayerList(width, height);

        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.func_179140_f();
        GlStateManager.func_179141_d();

        post(ALL);
    }

    public ScaledResolution getResolution()
    {
        return res;
    }

    protected void renderCrosshairs(float partialTicks)
    {
        if (pre(CROSSHAIRS)) return;
        bind(Gui.field_110324_m);
        GlStateManager.func_179147_l();
        super.func_184045_a(partialTicks, res);
        post(CROSSHAIRS);
    }

    protected void renderPotionIcons(ScaledResolution resolution)
    {
        if (pre(POTION_ICONS)) return;
        super.func_184048_a(resolution);
        post(POTION_ICONS);
    }

    protected void renderSubtitles(ScaledResolution resolution)
    {
        if (pre(SUBTITLES)) return;
        this.field_184049_t.func_184068_a(res);
        post(SUBTITLES);
    }

    //@Override
    protected void renderBossHealth()
    {
        if (pre(BOSSHEALTH)) return;
        bind(Gui.field_110324_m);
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        field_73839_d.field_71424_I.func_76320_a("bossHealth");
        GlStateManager.func_179147_l();
        this.field_184050_w.func_184051_a();
        GlStateManager.func_179084_k();
        field_73839_d.field_71424_I.func_76319_b();
        post(BOSSHEALTH);
    }

    @Override
    protected void func_180480_a(float lightLevel, ScaledResolution scaledRes)
    {
        if (pre(VIGNETTE))
        {
            // Need to put this here, since Vanilla assumes this state after the vignette was rendered.
            GlStateManager.func_179126_j();
            GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            return;
        }
        super.func_180480_a(lightLevel, scaledRes);
        post(VIGNETTE);
    }

    private void renderHelmet(ScaledResolution res, float partialTicks)
    {
        if (pre(HELMET)) return;

        ItemStack itemstack = this.field_73839_d.field_71439_g.field_71071_by.func_70440_f(3);

        if (this.field_73839_d.field_71474_y.field_74320_O == 0 && !itemstack.func_190926_b())
        {
            Item item = itemstack.func_77973_b();
            if (item == Item.func_150898_a(Blocks.field_150423_aK))
            {
                func_180476_e(res);
            }
            else
            {
                item.renderHelmetOverlay(itemstack, field_73839_d.field_71439_g, res, partialTicks);
            }
        }

        post(HELMET);
    }

    protected void renderArmor(int width, int height)
    {
        if (pre(ARMOR)) return;
        field_73839_d.field_71424_I.func_76320_a("armor");

        GlStateManager.func_179147_l();
        int left = width / 2 - 91;
        int top = height - left_height;

        int level = ForgeHooks.getTotalArmorValue(field_73839_d.field_71439_g);
        for (int i = 1; level > 0 && i < 20; i += 2)
        {
            if (i < level)
            {
                func_73729_b(left, top, 34, 9, 9, 9);
            }
            else if (i == level)
            {
                func_73729_b(left, top, 25, 9, 9, 9);
            }
            else if (i > level)
            {
                func_73729_b(left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        left_height += 10;

        GlStateManager.func_179084_k();
        field_73839_d.field_71424_I.func_76319_b();
        post(ARMOR);
    }

    protected void renderPortal(ScaledResolution res, float partialTicks)
    {
        if (pre(PORTAL)) return;

        float f1 = field_73839_d.field_71439_g.field_71080_cy + (field_73839_d.field_71439_g.field_71086_bY - field_73839_d.field_71439_g.field_71080_cy) * partialTicks;

        if (f1 > 0.0F)
        {
            func_180474_b(f1, res);
        }

        post(PORTAL);
    }

    @Override
    protected void func_180479_a(ScaledResolution res, float partialTicks)
    {
        if (pre(HOTBAR)) return;

        if (field_73839_d.field_71442_b.func_78747_a())
        {
            this.field_175197_u.func_175264_a(res, partialTicks);
        }
        else
        {
            super.func_180479_a(res, partialTicks);
        }

        post(HOTBAR);
    }

    @Override
    public void func_175188_a(ITextComponent component, boolean animateColor)
    {
        this.func_110326_a(component.func_150254_d(), animateColor);
    }

    protected void renderAir(int width, int height)
    {
        if (pre(AIR)) return;
        field_73839_d.field_71424_I.func_76320_a("air");
        EntityPlayer player = (EntityPlayer)this.field_73839_d.func_175606_aa();
        GlStateManager.func_179147_l();
        int left = width / 2 + 91;
        int top = height - right_height;

        if (player.func_70055_a(Material.field_151586_h))
        {
            int air = player.func_70086_ai();
            int full = MathHelper.func_76143_f((double)(air - 2) * 10.0D / 300.0D);
            int partial = MathHelper.func_76143_f((double)air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i)
            {
                func_73729_b(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            right_height += 10;
        }

        GlStateManager.func_179084_k();
        field_73839_d.field_71424_I.func_76319_b();
        post(AIR);
    }

    public void renderHealth(int width, int height)
    {
        bind(field_110324_m);
        if (pre(HEALTH)) return;
        field_73839_d.field_71424_I.func_76320_a("health");
        GlStateManager.func_179147_l();

        EntityPlayer player = (EntityPlayer)this.field_73839_d.func_175606_aa();
        int health = MathHelper.func_76123_f(player.func_110143_aJ());
        boolean highlight = field_175191_F > (long)field_73837_f && (field_175191_F - (long)field_73837_f) / 3L %2L == 1L;

        if (health < this.field_175194_C && player.field_70172_ad > 0)
        {
            this.field_175190_E = Minecraft.func_71386_F();
            this.field_175191_F = (long)(this.field_73837_f + 20);
        }
        else if (health > this.field_175194_C && player.field_70172_ad > 0)
        {
            this.field_175190_E = Minecraft.func_71386_F();
            this.field_175191_F = (long)(this.field_73837_f + 10);
        }

        if (Minecraft.func_71386_F() - this.field_175190_E > 1000L)
        {
            this.field_175194_C = health;
            this.field_175189_D = health;
            this.field_175190_E = Minecraft.func_71386_F();
        }

        this.field_175194_C = health;
        int healthLast = this.field_175189_D;

        IAttributeInstance attrMaxHealth = player.func_110148_a(SharedMonsterAttributes.field_111267_a);
        float healthMax = (float)attrMaxHealth.func_111126_e();
        float absorb = MathHelper.func_76123_f(player.func_110139_bj());

        int healthRows = MathHelper.func_76123_f((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.field_73842_c.setSeed((long)(field_73837_f * 312871));

        int left = width / 2 - 91;
        int top = height - left_height;
        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;

        int regen = -1;
        if (player.func_70644_a(MobEffects.field_76428_l))
        {
            regen = field_73837_f % 25;
        }

        final int TOP =  9 * (field_73839_d.field_71441_e.func_72912_H().func_76093_s() ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.func_70644_a(MobEffects.field_76436_u))      MARGIN += 36;
        else if (player.func_70644_a(MobEffects.field_82731_v)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = MathHelper.func_76123_f((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
        {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.func_76123_f((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += field_73842_c.nextInt(2);
            if (i == regen) y -= 2;

            func_73729_b(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (i * 2 + 1 < healthLast)
                    func_73729_b(x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    func_73729_b(x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    func_73729_b(x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    func_73729_b(x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (i * 2 + 1 < health)
                    func_73729_b(x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    func_73729_b(x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        GlStateManager.func_179084_k();
        field_73839_d.field_71424_I.func_76319_b();
        post(HEALTH);
    }

    public void renderFood(int width, int height)
    {
        if (pre(FOOD)) return;
        field_73839_d.field_71424_I.func_76320_a("food");

        EntityPlayer player = (EntityPlayer)this.field_73839_d.func_175606_aa();
        GlStateManager.func_179147_l();
        int left = width / 2 + 91;
        int top = height - right_height;
        right_height += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodStats stats = field_73839_d.field_71439_g.func_71024_bL();
        int level = stats.func_75116_a();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte background = 0;

            if (field_73839_d.field_71439_g.func_70644_a(MobEffects.field_76438_s))
            {
                icon += 36;
                background = 13;
            }
            if (unused) background = 1; //Probably should be a += 1 but vanilla never uses this

            if (player.func_71024_bL().func_75115_e() <= 0.0F && field_73837_f % (level * 3 + 1) == 0)
            {
                y = top + (field_73842_c.nextInt(3) - 1);
            }

            func_73729_b(x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                func_73729_b(x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                func_73729_b(x, y, icon + 45, 27, 9, 9);
        }
        GlStateManager.func_179084_k();
        field_73839_d.field_71424_I.func_76319_b();
        post(FOOD);
    }

    protected void renderSleepFade(int width, int height)
    {
        if (field_73839_d.field_71439_g.func_71060_bI() > 0)
        {
            field_73839_d.field_71424_I.func_76320_a("sleep");
            GlStateManager.func_179097_i();
            GlStateManager.func_179118_c();
            int sleepTime = field_73839_d.field_71439_g.func_71060_bI();
            float opacity = (float)sleepTime / 100.0F;

            if (opacity > 1.0F)
            {
                opacity = 1.0F - (float)(sleepTime - 100) / 10.0F;
            }

            int color = (int)(220.0F * opacity) << 24 | 1052704;
            func_73734_a(0, 0, width, height, color);
            GlStateManager.func_179141_d();
            GlStateManager.func_179126_j();
            field_73839_d.field_71424_I.func_76319_b();
        }
    }

    protected void renderExperience(int width, int height)
    {
        bind(field_110324_m);
        if (pre(EXPERIENCE)) return;
        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.func_179084_k();

        if (field_73839_d.field_71442_b.func_78763_f())
        {
            field_73839_d.field_71424_I.func_76320_a("expBar");
            int cap = this.field_73839_d.field_71439_g.func_71050_bK();
            int left = width / 2 - 91;

            if (cap > 0)
            {
                short barWidth = 182;
                int filled = (int)(field_73839_d.field_71439_g.field_71106_cc * (float)(barWidth + 1));
                int top = height - 32 + 3;
                func_73729_b(left, top, 0, 64, barWidth, 5);

                if (filled > 0)
                {
                    func_73729_b(left, top, 0, 69, filled, 5);
                }
            }

            this.field_73839_d.field_71424_I.func_76319_b();


            if (field_73839_d.field_71442_b.func_78763_f() && field_73839_d.field_71439_g.field_71068_ca > 0)
            {
                field_73839_d.field_71424_I.func_76320_a("expLevel");
                boolean flag1 = false;
                int color = flag1 ? 16777215 : 8453920;
                String text = "" + field_73839_d.field_71439_g.field_71068_ca;
                int x = (width - fontrenderer.func_78256_a(text)) / 2;
                int y = height - 31 - 4;
                fontrenderer.func_78276_b(text, x + 1, y, 0);
                fontrenderer.func_78276_b(text, x - 1, y, 0);
                fontrenderer.func_78276_b(text, x, y + 1, 0);
                fontrenderer.func_78276_b(text, x, y - 1, 0);
                fontrenderer.func_78276_b(text, x, y, color);
                field_73839_d.field_71424_I.func_76319_b();
            }
        }
        GlStateManager.func_179147_l();
        GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);

        post(EXPERIENCE);
    }

    protected void renderJumpBar(int width, int height)
    {
        bind(field_110324_m);
        if (pre(JUMPBAR)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.func_179084_k();

        field_73839_d.field_71424_I.func_76320_a("jumpBar");
        float charge = field_73839_d.field_71439_g.func_110319_bJ();
        final int barWidth = 182;
        int x = (width / 2) - (barWidth / 2);
        int filled = (int)(charge * (float)(barWidth + 1));
        int top = height - 32 + 3;

        func_73729_b(x, top, 0, 84, barWidth, 5);

        if (filled > 0)
        {
            this.func_73729_b(x, top, 0, 89, filled, 5);
        }

        GlStateManager.func_179147_l();
        field_73839_d.field_71424_I.func_76319_b();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(JUMPBAR);
    }

    protected void renderToolHighlight(ScaledResolution res)
    {
        if (this.field_73839_d.field_71474_y.field_92117_D && !this.field_73839_d.field_71442_b.func_78747_a())
        {
            field_73839_d.field_71424_I.func_76320_a("toolHighlight");

            if (this.field_92017_k > 0 && !this.field_92016_l.func_190926_b())
            {
                String name = this.field_92016_l.func_82833_r();
                if (this.field_92016_l.func_82837_s())
                    name = TextFormatting.ITALIC + name;

                name = this.field_92016_l.func_77973_b().getHighlightTip(this.field_92016_l, name);

                int opacity = (int)((float)this.field_92017_k * 256.0F / 10.0F);
                if (opacity > 255) opacity = 255;

                if (opacity > 0)
                {
                    int y = res.func_78328_b() - 59;
                    if (!field_73839_d.field_71442_b.func_78755_b()) y += 14;

                    GlStateManager.func_179094_E();
                    GlStateManager.func_179147_l();
                    GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    FontRenderer font = field_92016_l.func_77973_b().getFontRenderer(field_92016_l);
                    if (font != null)
                    {
                        int x = (res.func_78326_a() - font.func_78256_a(name)) / 2;
                        font.func_175063_a(name, x, y, WHITE | (opacity << 24));
                    }
                    else
                    {
                        int x = (res.func_78326_a() - fontrenderer.func_78256_a(name)) / 2;
                        fontrenderer.func_175063_a(name, x, y, WHITE | (opacity << 24));
                    }
                    GlStateManager.func_179084_k();
                    GlStateManager.func_179121_F();
                }
            }

            field_73839_d.field_71424_I.func_76319_b();
        }
        else if (this.field_73839_d.field_71439_g.func_175149_v())
        {
            this.field_175197_u.func_175263_a(res);
        }
    }

    protected void renderHUDText(int width, int height)
    {
        field_73839_d.field_71424_I.func_76320_a("forgeHudText");
        OpenGlHelper.func_148821_a(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        ArrayList<String> listL = new ArrayList<String>();
        ArrayList<String> listR = new ArrayList<String>();

        if (field_73839_d.func_71355_q())
        {
            long time = field_73839_d.field_71441_e.func_82737_E();
            if (time >= 120500L)
            {
                listR.add(I18n.func_135052_a("demo.demoExpired"));
            }
            else
            {
                listR.add(I18n.func_135052_a("demo.remainingTime", StringUtils.func_76337_a((int)(120500L - time))));
            }
        }

        if (this.field_73839_d.field_71474_y.field_74330_P && !pre(DEBUG))
        {
            listL.addAll(debugOverlay.getLeft());
            listR.addAll(debugOverlay.getRight());
            post(DEBUG);
        }

        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(eventParent, listL, listR);
        if (!MinecraftForge.EVENT_BUS.post(event))
        {
            int top = 2;
            for (String msg : listL)
            {
                if (msg == null) continue;
                func_73734_a(1, top - 1, 2 + fontrenderer.func_78256_a(msg) + 1, top + fontrenderer.field_78288_b - 1, -1873784752);
                fontrenderer.func_78276_b(msg, 2, top, 14737632);
                top += fontrenderer.field_78288_b;
            }

            top = 2;
            for (String msg : listR)
            {
                if (msg == null) continue;
                int w = fontrenderer.func_78256_a(msg);
                int left = width - 2 - w;
                func_73734_a(left - 1, top - 1, left + w + 1, top + fontrenderer.field_78288_b - 1, -1873784752);
                fontrenderer.func_78276_b(msg, left, top, 14737632);
                top += fontrenderer.field_78288_b;
            }
        }

        field_73839_d.field_71424_I.func_76319_b();
        post(TEXT);
    }

    protected void renderFPSGraph()
    {
        if (this.field_73839_d.field_71474_y.field_74330_P && this.field_73839_d.field_71474_y.field_181657_aC && !pre(FPS_GRAPH))
        {
            this.debugOverlay.func_181554_e();
            post(FPS_GRAPH);
        }
    }

    protected void renderRecordOverlay(int width, int height, float partialTicks)
    {
        if (field_73845_h > 0)
        {
            field_73839_d.field_71424_I.func_76320_a("overlayMessage");
            float hue = (float)field_73845_h - partialTicks;
            int opacity = (int)(hue * 256.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 0)
            {
                GlStateManager.func_179094_E();
                GlStateManager.func_179109_b((float)(width / 2), (float)(height - 68), 0.0F);
                GlStateManager.func_179147_l();
                GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int color = (field_73844_j ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                fontrenderer.func_78276_b(field_73838_g, -fontrenderer.func_78256_a(field_73838_g) / 2, -4, color | (opacity << 24));
                GlStateManager.func_179084_k();
                GlStateManager.func_179121_F();
            }

            field_73839_d.field_71424_I.func_76319_b();
        }
    }

    protected void renderTitle(int width, int height, float partialTicks)
    {
        if (field_175195_w > 0)
        {
            field_73839_d.field_71424_I.func_76320_a("titleAndSubtitle");
            float age = (float)this.field_175195_w - partialTicks;
            int opacity = 255;

            if (field_175195_w > field_175193_B + field_175192_A)
            {
                float f3 = (float)(field_175199_z + field_175192_A + field_175193_B) - age;
                opacity = (int)(f3 * 255.0F / (float)field_175199_z);
            }
            if (field_175195_w <= field_175193_B) opacity = (int)(age * 255.0F / (float)this.field_175193_B);

            opacity = MathHelper.func_76125_a(opacity, 0, 255);

            if (opacity > 8)
            {
                GlStateManager.func_179094_E();
                GlStateManager.func_179109_b((float)(width / 2), (float)(height / 2), 0.0F);
                GlStateManager.func_179147_l();
                GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.func_179094_E();
                GlStateManager.func_179152_a(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & -16777216;
                this.func_175179_f().func_175065_a(this.field_175201_x, (float)(-this.func_175179_f().func_78256_a(this.field_175201_x) / 2), -10.0F, 16777215 | l, true);
                GlStateManager.func_179121_F();
                GlStateManager.func_179094_E();
                GlStateManager.func_179152_a(2.0F, 2.0F, 2.0F);
                this.func_175179_f().func_175065_a(this.field_175200_y, (float)(-this.func_175179_f().func_78256_a(this.field_175200_y) / 2), 5.0F, 16777215 | l, true);
                GlStateManager.func_179121_F();
                GlStateManager.func_179084_k();
                GlStateManager.func_179121_F();
            }

            this.field_73839_d.field_71424_I.func_76319_b();
        }
    }

    protected void renderChat(int width, int height)
    {
        field_73839_d.field_71424_I.func_76320_a("chat");

        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(eventParent, 0, height - 48);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float) event.getPosX(), (float) event.getPosY(), 0.0F);
        field_73840_e.func_146230_a(field_73837_f);
        GlStateManager.func_179121_F();

        post(CHAT);

        field_73839_d.field_71424_I.func_76319_b();
    }

    protected void renderPlayerList(int width, int height)
    {
        ScoreObjective scoreobjective = this.field_73839_d.field_71441_e.func_96441_U().func_96539_a(0);
        NetHandlerPlayClient handler = field_73839_d.field_71439_g.field_71174_a;

        if (field_73839_d.field_71474_y.field_74321_H.func_151470_d() && (!field_73839_d.func_71387_A() || handler.func_175106_d().size() > 1 || scoreobjective != null))
        {
            this.field_175196_v.func_175246_a(true);
            if (pre(PLAYER_LIST)) return;
            this.field_175196_v.func_175249_a(width, this.field_73839_d.field_71441_e.func_96441_U(), scoreobjective);
            post(PLAYER_LIST);
        }
        else
        {
            this.field_175196_v.func_175246_a(false);
        }
    }

    protected void renderHealthMount(int width, int height)
    {
        EntityPlayer player = (EntityPlayer)field_73839_d.func_175606_aa();
        Entity tmp = player.func_184187_bx();
        if (!(tmp instanceof EntityLivingBase)) return;

        bind(field_110324_m);

        if (pre(HEALTHMOUNT)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        field_73839_d.field_71424_I.func_76318_c("mountHealth");
        GlStateManager.func_179147_l();
        EntityLivingBase mount = (EntityLivingBase)tmp;
        int health = (int)Math.ceil((double)mount.func_110143_aJ());
        float healthMax = mount.func_110138_aP();
        int hearts = (int)(healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20)
        {
            int top = height - right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i)
            {
                int x = left_align - i * 8 - 9;
                func_73729_b(x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    func_73729_b(x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    func_73729_b(x, top, HALF, 9, 9, 9);
            }

            right_height += 10;
        }
        GlStateManager.func_179084_k();
        post(HEALTHMOUNT);
    }

    //Helper macros
    private boolean pre(ElementType type)
    {
        return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
    }
    private void post(ElementType type)
    {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
    }
    private void bind(ResourceLocation res)
    {
        field_73839_d.func_110434_K().func_110577_a(res);
    }

    private class GuiOverlayDebugForge extends GuiOverlayDebug
    {
        private Minecraft mc;
        private GuiOverlayDebugForge(Minecraft mc)
        {
            super(mc);
            this.mc = mc;
        }
        @Override protected void func_180798_a(){}
        @Override protected void func_175239_b(ScaledResolution res){}
        private List<String> getLeft()
        {
            List<String> ret = this.call();
            ret.add("");
            ret.add("Debug: Pie [shift]: " + (this.mc.field_71474_y.field_74329_Q ? "visible" : "hidden") + " FPS [alt]: " + (this.mc.field_71474_y.field_181657_aC ? "visible" : "hidden"));
            ret.add("For help: press F3 + Q");
            return ret;
        }
        private List<String> getRight(){ return this.func_175238_c(); }
    }
}
