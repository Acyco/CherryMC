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

package net.minecraftforge.oredict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.BlockPrismarine;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;

public class OreDictionary
{
    private static final boolean DEBUG = false;
    private static boolean hasInit = false;
    private static List<String>          idToName = new ArrayList<String>();
    private static Map<String, Integer>  nameToId = new HashMap<String, Integer>(128);
    private static List<NonNullList<ItemStack>> idToStack = Lists.newArrayList();
    private static List<NonNullList<ItemStack>> idToStackUn = Lists.newArrayList();
    private static Map<Integer, List<Integer>> stackToId = Maps.newHashMapWithExpectedSize((int)(128 * 0.75));
    public static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.func_191196_a();

    /**
     * Minecraft changed from -1 to Short.MAX_VALUE in 1.5 release for the "block wildcard". Use this in case it
     * changes again.
     */
    public static final int WILDCARD_VALUE = Short.MAX_VALUE;

    static {
        initVanillaEntries();
    }

    private static void initVanillaEntries()
    {
        if (!hasInit)
        {
            // tree- and wood-related things
            registerOre("logWood",     new ItemStack(Blocks.field_150364_r, 1, WILDCARD_VALUE));
            registerOre("logWood",     new ItemStack(Blocks.field_150363_s, 1, WILDCARD_VALUE));
            registerOre("plankWood",   new ItemStack(Blocks.field_150344_f, 1, WILDCARD_VALUE));
            registerOre("slabWood",    new ItemStack(Blocks.field_150376_bx, 1, WILDCARD_VALUE));
            registerOre("stairWood",   Blocks.field_150476_ad);
            registerOre("stairWood",   Blocks.field_150485_bF);
            registerOre("stairWood",   Blocks.field_150487_bG);
            registerOre("stairWood",   Blocks.field_150481_bH);
            registerOre("stairWood",   Blocks.field_150400_ck);
            registerOre("stairWood",   Blocks.field_150401_cl);
            registerOre("fenceWood", Blocks.field_180407_aO);
            registerOre("fenceWood", Blocks.field_180408_aP);
            registerOre("fenceWood", Blocks.field_180404_aQ);
            registerOre("fenceWood", Blocks.field_180403_aR);
            registerOre("fenceWood", Blocks.field_180406_aS);
            registerOre("fenceWood", Blocks.field_180405_aT);
            registerOre("fenceGateWood", Blocks.field_180390_bo);
            registerOre("fenceGateWood", Blocks.field_180391_bp);
            registerOre("fenceGateWood", Blocks.field_180392_bq);
            registerOre("fenceGateWood", Blocks.field_180386_br);
            registerOre("fenceGateWood", Blocks.field_180385_bs);
            registerOre("fenceGateWood", Blocks.field_180387_bt);
            registerOre("doorWood", Items.field_179572_au);
            registerOre("doorWood", Items.field_179568_as);
            registerOre("doorWood", Items.field_179571_av);
            registerOre("doorWood", Items.field_179570_aq);
            registerOre("doorWood", Items.field_179567_at);
            registerOre("doorWood", Items.field_179569_ar);
            registerOre("stickWood",   Items.field_151055_y);
            registerOre("treeSapling", new ItemStack(Blocks.field_150345_g, 1, WILDCARD_VALUE));
            registerOre("treeLeaves",  new ItemStack(Blocks.field_150362_t, 1, WILDCARD_VALUE));
            registerOre("treeLeaves",  new ItemStack(Blocks.field_150361_u, 1, WILDCARD_VALUE));
            registerOre("vine",        Blocks.field_150395_bd);

            // Ores
            registerOre("oreGold",     Blocks.field_150352_o);
            registerOre("oreIron",     Blocks.field_150366_p);
            registerOre("oreLapis",    Blocks.field_150369_x);
            registerOre("oreDiamond",  Blocks.field_150482_ag);
            registerOre("oreRedstone", Blocks.field_150450_ax);
            registerOre("oreEmerald",  Blocks.field_150412_bA);
            registerOre("oreQuartz",   Blocks.field_150449_bY);
            registerOre("oreCoal",     Blocks.field_150365_q);

            // ingots/nuggets
            registerOre("ingotIron",     Items.field_151042_j);
            registerOre("ingotGold",     Items.field_151043_k);
            registerOre("ingotBrick",    Items.field_151118_aC);
            registerOre("ingotBrickNether", Items.field_151130_bT);
            registerOre("nuggetGold",  Items.field_151074_bl);
            registerOre("nuggetIron",  Items.field_191525_da);

            // gems and dusts
            registerOre("gemDiamond",  Items.field_151045_i);
            registerOre("gemEmerald",  Items.field_151166_bC);
            registerOre("gemQuartz",   Items.field_151128_bU);
            registerOre("gemPrismarine", Items.field_179562_cC);
            registerOre("dustPrismarine", Items.field_179563_cD);
            registerOre("dustRedstone",  Items.field_151137_ax);
            registerOre("dustGlowstone", Items.field_151114_aO);
            registerOre("gemLapis",    new ItemStack(Items.field_151100_aR, 1, 4));

            // storage blocks
            registerOre("blockGold",     Blocks.field_150340_R);
            registerOre("blockIron",     Blocks.field_150339_S);
            registerOre("blockLapis",    Blocks.field_150368_y);
            registerOre("blockDiamond",  Blocks.field_150484_ah);
            registerOre("blockRedstone", Blocks.field_150451_bX);
            registerOre("blockEmerald",  Blocks.field_150475_bE);
            registerOre("blockQuartz",   Blocks.field_150371_ca);
            registerOre("blockCoal",     Blocks.field_150402_ci);

            // crops
            registerOre("cropWheat",   Items.field_151015_O);
            registerOre("cropPotato",  Items.field_151174_bG);
            registerOre("cropCarrot",  Items.field_151172_bF);
            registerOre("cropNetherWart", Items.field_151075_bm);
            registerOre("sugarcane",   Items.field_151120_aE);
            registerOre("blockCactus", Blocks.field_150434_aF);

            // misc materials
            registerOre("dye",         new ItemStack(Items.field_151100_aR, 1, WILDCARD_VALUE));
            registerOre("paper",       new ItemStack(Items.field_151121_aF));

            // mob drops
            registerOre("slimeball",   Items.field_151123_aH);
            registerOre("enderpearl",  Items.field_151079_bi);
            registerOre("bone",        Items.field_151103_aS);
            registerOre("gunpowder",   Items.field_151016_H);
            registerOre("string",      Items.field_151007_F);
            registerOre("netherStar",  Items.field_151156_bN);
            registerOre("leather",     Items.field_151116_aA);
            registerOre("feather",     Items.field_151008_G);
            registerOre("egg",         Items.field_151110_aK);

            // records
            registerOre("record",      Items.field_151096_cd);
            registerOre("record",      Items.field_151093_ce);
            registerOre("record",      Items.field_151094_cf);
            registerOre("record",      Items.field_151091_cg);
            registerOre("record",      Items.field_151092_ch);
            registerOre("record",      Items.field_151089_ci);
            registerOre("record",      Items.field_151090_cj);
            registerOre("record",      Items.field_151087_ck);
            registerOre("record",      Items.field_151088_cl);
            registerOre("record",      Items.field_151085_cm);
            registerOre("record",      Items.field_151086_cn);
            registerOre("record",      Items.field_151084_co);

            // blocks
            registerOre("dirt",        Blocks.field_150346_d);
            registerOre("grass",       Blocks.field_150349_c);
            registerOre("stone",       Blocks.field_150348_b);
            registerOre("cobblestone", Blocks.field_150347_e);
            registerOre("gravel",      Blocks.field_150351_n);
            registerOre("sand",        new ItemStack(Blocks.field_150354_m, 1, WILDCARD_VALUE));
            registerOre("sandstone",   new ItemStack(Blocks.field_150322_A, 1, WILDCARD_VALUE));
            registerOre("sandstone",   new ItemStack(Blocks.field_180395_cM, 1, WILDCARD_VALUE));
            registerOre("netherrack",  Blocks.field_150424_aL);
            registerOre("obsidian",    Blocks.field_150343_Z);
            registerOre("glowstone",   Blocks.field_150426_aN);
            registerOre("endstone",    Blocks.field_150377_bs);
            registerOre("torch",       Blocks.field_150478_aa);
            registerOre("workbench",   Blocks.field_150462_ai);
            registerOre("blockSlime",    Blocks.field_180399_cE);
            registerOre("blockPrismarine", new ItemStack(Blocks.field_180397_cI, 1, BlockPrismarine.EnumType.ROUGH.func_176807_a()));
            registerOre("blockPrismarineBrick", new ItemStack(Blocks.field_180397_cI, 1, BlockPrismarine.EnumType.BRICKS.func_176807_a()));
            registerOre("blockPrismarineDark", new ItemStack(Blocks.field_180397_cI, 1, BlockPrismarine.EnumType.DARK.func_176807_a()));
            registerOre("stoneGranite",          new ItemStack(Blocks.field_150348_b, 1, 1));
            registerOre("stoneGranitePolished",  new ItemStack(Blocks.field_150348_b, 1, 2));
            registerOre("stoneDiorite",          new ItemStack(Blocks.field_150348_b, 1, 3));
            registerOre("stoneDioritePolished",  new ItemStack(Blocks.field_150348_b, 1, 4));
            registerOre("stoneAndesite",         new ItemStack(Blocks.field_150348_b, 1, 5));
            registerOre("stoneAndesitePolished", new ItemStack(Blocks.field_150348_b, 1, 6));
            registerOre("blockGlassColorless", Blocks.field_150359_w);
            registerOre("blockGlass",    Blocks.field_150359_w);
            registerOre("blockGlass",    new ItemStack(Blocks.field_150399_cn, 1, WILDCARD_VALUE));
            //blockGlass{Color} is added below with dyes
            registerOre("paneGlassColorless", Blocks.field_150410_aZ);
            registerOre("paneGlass",     Blocks.field_150410_aZ);
            registerOre("paneGlass",     new ItemStack(Blocks.field_150397_co, 1, WILDCARD_VALUE));
            //paneGlass{Color} is added below with dyes
            registerOre("wool",          new ItemStack(Blocks.field_150325_L, 1, WILDCARD_VALUE));
            //wool{Color} is added below with dyes

            // chests
            registerOre("chest",        Blocks.field_150486_ae);
            registerOre("chest",        Blocks.field_150477_bB);
            registerOre("chest",        Blocks.field_150447_bR);
            registerOre("chestWood",    Blocks.field_150486_ae);
            registerOre("chestEnder",   Blocks.field_150477_bB);
            registerOre("chestTrapped", Blocks.field_150447_bR);
        }

        // Build our list of items to replace with ore tags
        Map<ItemStack, String> replacements = new HashMap<ItemStack, String>();

        // wood-related things
        replacements.put(new ItemStack(Items.field_151055_y), "stickWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 0), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 1), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 2), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 3), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 4), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, 5), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150344_f, 1, WILDCARD_VALUE), "plankWood");
        replacements.put(new ItemStack(Blocks.field_150376_bx, 1, WILDCARD_VALUE), "slabWood");

        // ingots/nuggets
        replacements.put(new ItemStack(Items.field_151043_k), "ingotGold");
        replacements.put(new ItemStack(Items.field_151042_j), "ingotIron");

        // gems and dusts
        replacements.put(new ItemStack(Items.field_151045_i), "gemDiamond");
        replacements.put(new ItemStack(Items.field_151166_bC), "gemEmerald");
        replacements.put(new ItemStack(Items.field_179562_cC), "gemPrismarine");
        replacements.put(new ItemStack(Items.field_179563_cD), "dustPrismarine");
        replacements.put(new ItemStack(Items.field_151137_ax), "dustRedstone");
        replacements.put(new ItemStack(Items.field_151114_aO), "dustGlowstone");

        // crops
        replacements.put(new ItemStack(Items.field_151120_aE), "sugarcane");
        replacements.put(new ItemStack(Blocks.field_150434_aF), "blockCactus");

        // misc materials
        replacements.put(new ItemStack(Items.field_151121_aF), "paper");

        // mob drops
        replacements.put(new ItemStack(Items.field_151123_aH), "slimeball");
        replacements.put(new ItemStack(Items.field_151007_F), "string");
        replacements.put(new ItemStack(Items.field_151116_aA), "leather");
        replacements.put(new ItemStack(Items.field_151079_bi), "enderpearl");
        replacements.put(new ItemStack(Items.field_151016_H), "gunpowder");
        replacements.put(new ItemStack(Items.field_151156_bN), "netherStar");
        replacements.put(new ItemStack(Items.field_151008_G), "feather");
        replacements.put(new ItemStack(Items.field_151103_aS), "bone");
        replacements.put(new ItemStack(Items.field_151110_aK), "egg");

        // blocks
        replacements.put(new ItemStack(Blocks.field_150348_b), "stone");
        replacements.put(new ItemStack(Blocks.field_150347_e), "cobblestone");
        replacements.put(new ItemStack(Blocks.field_150347_e, 1, WILDCARD_VALUE), "cobblestone");
        replacements.put(new ItemStack(Blocks.field_150426_aN), "glowstone");
        replacements.put(new ItemStack(Blocks.field_150359_w), "blockGlassColorless");
        replacements.put(new ItemStack(Blocks.field_180397_cI), "prismarine");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 1), "stoneGranite");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 2), "stoneGranitePolished");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 3), "stoneDiorite");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 4), "stoneDioritePolished");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 5), "stoneAndesite");
        replacements.put(new ItemStack(Blocks.field_150348_b, 1, 6), "stoneAndesitePolished");

        // chests
        replacements.put(new ItemStack(Blocks.field_150486_ae), "chestWood");
        replacements.put(new ItemStack(Blocks.field_150477_bB), "chestEnder");
        replacements.put(new ItemStack(Blocks.field_150447_bR), "chestTrapped");

        // Register dyes
        String[] dyes =
        {
            "Black",
            "Red",
            "Green",
            "Brown",
            "Blue",
            "Purple",
            "Cyan",
            "LightGray",
            "Gray",
            "Pink",
            "Lime",
            "Yellow",
            "LightBlue",
            "Magenta",
            "Orange",
            "White"
        };

        for(int i = 0; i < 16; i++)
        {
            ItemStack dye = new ItemStack(Items.field_151100_aR, 1, i);
            ItemStack wool = new ItemStack(Blocks.field_150325_L, 1, 15 - i);
            ItemStack block = new ItemStack(Blocks.field_150399_cn, 1, 15 - i);
            ItemStack pane = new ItemStack(Blocks.field_150397_co, 1, 15 - i);
            if (!hasInit)
            {
                registerOre("dye" + dyes[i], dye);
                registerOre("wool" + dyes[i], wool);
                registerOre("blockGlass" + dyes[i], block);
                registerOre("paneGlass"  + dyes[i], pane);
            }
            replacements.put(dye,   "dye" + dyes[i]);
            replacements.put(wool,  "wool" + dyes[i]);
            replacements.put(block, "blockGlass" + dyes[i]);
            replacements.put(pane,  "paneGlass" + dyes[i]);
        }
        hasInit = true;

        ItemStack[] replaceStacks = replacements.keySet().toArray(new ItemStack[replacements.keySet().size()]);

        // Ignore recipes for the following items
        ItemStack[] exclusions = new ItemStack[]
        {
            new ItemStack(Blocks.field_150368_y),
            new ItemStack(Items.field_151106_aX),
            new ItemStack(Blocks.field_150417_aV),
            new ItemStack(Blocks.field_150333_U, 1, WILDCARD_VALUE),
            new ItemStack(Blocks.field_150446_ar),
            new ItemStack(Blocks.field_150463_bK),
            new ItemStack(Blocks.field_180407_aO),
            new ItemStack(Blocks.field_180390_bo),
            new ItemStack(Blocks.field_150476_ad),
            new ItemStack(Blocks.field_180408_aP),
            new ItemStack(Blocks.field_180391_bp),
            new ItemStack(Blocks.field_150485_bF),
            new ItemStack(Blocks.field_180392_bq),
            new ItemStack(Blocks.field_180404_aQ),
            new ItemStack(Blocks.field_150487_bG),
            new ItemStack(Blocks.field_180403_aR),
            new ItemStack(Blocks.field_180386_br),
            new ItemStack(Blocks.field_150481_bH),
            new ItemStack(Blocks.field_180405_aT),
            new ItemStack(Blocks.field_180387_bt),
            new ItemStack(Blocks.field_150400_ck),
            new ItemStack(Blocks.field_180406_aS),
            new ItemStack(Blocks.field_180385_bs),
            new ItemStack(Blocks.field_150401_cl),
            new ItemStack(Blocks.field_150376_bx, 1, WILDCARD_VALUE),
            new ItemStack(Blocks.field_150410_aZ),
            new ItemStack(Blocks.field_189880_di), // Bone Block, to prevent conversion of dyes into bone meal.
            new ItemStack(Items.field_151124_az),
            new ItemStack(Items.field_185150_aH),
            new ItemStack(Items.field_185151_aI),
            new ItemStack(Items.field_185152_aJ),
            new ItemStack(Items.field_185153_aK),
            new ItemStack(Items.field_185154_aL),
            new ItemStack(Items.field_179570_aq),
            new ItemStack(Items.field_179569_ar),
            new ItemStack(Items.field_179568_as),
            new ItemStack(Items.field_179567_at),
            new ItemStack(Items.field_179572_au),
            new ItemStack(Items.field_179571_av),
            ItemStack.field_190927_a //So the above can have a comma and we don't have to keep editing extra lines.
        };

        FMLLog.log.info("Starts to replace vanilla recipe ingredients with ore ingredients.");
        int replaced = 0;
        // Search vanilla recipes for recipes to replace
        for(IRecipe obj : CraftingManager.field_193380_a)
        {
            if(obj.getClass() == ShapedRecipes.class || obj.getClass() == ShapelessRecipes.class)
            {
                ItemStack output = obj.func_77571_b();
                if (!output.func_190926_b() && containsMatch(false, new ItemStack[]{ output }, exclusions))
                {
                    continue;
                }

                Set<Ingredient> replacedIngs = new HashSet<>();
                NonNullList<Ingredient> lst = obj.func_192400_c();
                for (int x = 0; x < lst.size(); x++)
                {
                    Ingredient ing = lst.get(x);
                    ItemStack[] ingredients = ing.func_193365_a();
                    String oreName = null;
                    boolean skip = false;

                    for (ItemStack stack : ingredients)
                    {
                        boolean matches = false;
                        for (Entry<ItemStack, String> ent : replacements.entrySet())
                        {
                            if (itemMatches(ent.getKey(), stack, true))
                            {
                                matches = true;
                                if (oreName != null && !oreName.equals(ent.getValue()))
                                {
                                    FMLLog.log.info("Invalid recipe found with multiple oredict ingredients in the same ingredient..."); //TODO: Write a dumper?
                                    skip = true;
                                    break;
                                }
                                else if (oreName == null)
                                {
                                    oreName = ent.getValue();
                                    break;
                                }
                            }
                        }
                        if (!matches && oreName != null)
                        {
                            //TODO: Properly fix this, Broken recipe example: Beds
                            //FMLLog.info("Invalid recipe found with ingredient that partially matches ore entries..."); //TODO: Write a dumper?
                            skip = true;
                        }
                        if (skip)
                            break;
                    }
                    if (!skip && oreName != null)
                    {
                        //Replace!
                        lst.set(x, new OreIngredient(oreName));
                        replaced++;
                        if(DEBUG && replacedIngs.add(ing))
                        {
                            String recipeName = obj.getRegistryName().func_110623_a();
                            FMLLog.log.debug("Replaced {} of the recipe \'{}\' with \"{}\".", ing.func_193365_a(), recipeName, oreName);
                        }
                    }
                }
            }
        }

        FMLLog.log.info("Replaced {} ore ingredients", replaced);
    }

    /**
     * Gets the integer ID for the specified ore name.
     * If the name does not have a ID it assigns it a new one.
     *
     * @param name The unique name for this ore 'oreIron', 'ingotIron', etc..
     * @return A number representing the ID for this ore type
     */
    public static int getOreID(String name)
    {
        Integer val = nameToId.get(name);
        if (val == null)
        {
            idToName.add(name);
            val = idToName.size() - 1; //0 indexed
            nameToId.put(name, val);
            NonNullList<ItemStack> back = NonNullList.func_191196_a();
            idToStack.add(back);
            idToStackUn.add(back);
        }
        return val;
    }

    /**
     * Reverse of getOreID, will not create new entries.
     *
     * @param id The ID to translate to a string
     * @return The String name, or "Unknown" if not found.
     */
    public static String getOreName(int id)
    {
        return (id >= 0 && id < idToName.size()) ? idToName.get(id) : "Unknown";
    }

    /**
     * Gets all the integer ID for the ores that the specified item stack is registered to.
     * If the item stack is not linked to any ore, this will return an empty array and no new entry will be created.
     *
     * @param stack The item stack of the ore.
     * @return An array of ids that this ore is registered as.
     */
    public static int[] getOreIDs(@Nonnull ItemStack stack)
    {
        if (stack.func_190926_b()) throw new IllegalArgumentException("Stack can not be invalid!");

        Set<Integer> set = new HashSet<Integer>();

        // HACK: use the registry name's ID. It is unique and it knows about substitutions. Fallback to a -1 value (what Item.getIDForItem would have returned) in the case where the registry is not aware of the item yet
        // IT should be noted that -1 will fail the gate further down, if an entry already exists with value -1 for this name. This is what is broken and being warned about.
        // APPARENTLY it's quite common to do this. OreDictionary should be considered alongside Recipes - you can't make them properly until you've registered with the game.
        ResourceLocation registryName = stack.func_77973_b().delegate.name();
        int id;
        if (registryName == null)
        {
            FMLLog.log.debug("Attempted to find the oreIDs for an unregistered object ({}). This won't work very well.", stack);
            return new int[0];
        }
        else
        {
            id = Item.field_150901_e.func_148757_b(stack.func_77973_b().delegate.get());
        }
        List<Integer> ids = stackToId.get(id);
        if (ids != null) set.addAll(ids);
        ids = stackToId.get(id | ((stack.func_77952_i() + 1) << 16));
        if (ids != null) set.addAll(ids);

        Integer[] tmp = set.toArray(new Integer[set.size()]);
        int[] ret = new int[tmp.length];
        for (int x = 0; x < tmp.length; x++)
            ret[x] = tmp[x];
        return ret;
    }

    /**
     * Retrieves the ArrayList of items that are registered to this ore type.
     * Creates the list as empty if it did not exist.
     *
     * The returned List is unmodifiable, but will be updated if a new ore
     * is registered using registerOre
     *
     * @param name The ore name, directly calls getOreID
     * @return An arrayList containing ItemStacks registered for this ore
     */
    public static NonNullList<ItemStack> getOres(String name)
    {
        return getOres(getOreID(name));
    }

    /**
     * Retrieves the List of items that are registered to this ore type at this instant.
     * If the flag is TRUE, then it will create the list as empty if it did not exist.
     *
     * This option should be used by modders who are doing blanket scans in postInit.
     * It greatly reduces clutter in the OreDictionary is the responsible and proper
     * way to use the dictionary in a large number of cases.
     *
     * The other function above is utilized in OreRecipe and is required for the
     * operation of that code.
     *
     * @param name The ore name, directly calls getOreID if the flag is TRUE
     * @param alwaysCreateEntry Flag - should a new entry be created if empty
     * @return An arraylist containing ItemStacks registered for this ore
     */
    public static NonNullList<ItemStack> getOres(String name, boolean alwaysCreateEntry)
    {
        if (alwaysCreateEntry) {
            return getOres(getOreID(name));
        }
        return nameToId.get(name) != null ? getOres(getOreID(name)) : EMPTY_LIST;
    }

    /**
     * Returns whether or not an oreName exists in the dictionary.
     * This function can be used to safely query the Ore Dictionary without
     * adding needless clutter to the underlying map structure.
     *
     * Please use this when possible and appropriate.
     *
     * @param name The ore name
     * @return Whether or not that name is in the Ore Dictionary.
     */
    public static boolean doesOreNameExist(String name)
    {
        return nameToId.get(name) != null;
    }

    /**
     * Retrieves a list of all unique ore names that are already registered.
     *
     * @return All unique ore names that are currently registered.
     */
    public static String[] getOreNames()
    {
        return idToName.toArray(new String[idToName.size()]);
    }

    /**
     * Retrieves the List of items that are registered to this ore type.
     * Creates the list as empty if it did not exist.
     *
     * @param id The ore ID, see getOreID
     * @return An List containing ItemStacks registered for this ore
     */
    private static NonNullList<ItemStack> getOres(int id)
    {
        return idToStackUn.size() > id ? idToStackUn.get(id) : EMPTY_LIST;
    }

    private static boolean containsMatch(boolean strict, ItemStack[] inputs, @Nonnull ItemStack... targets)
    {
        for (ItemStack input : inputs)
        {
            for (ItemStack target : targets)
            {
                if (itemMatches(target, input, strict))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsMatch(boolean strict, NonNullList<ItemStack> inputs, @Nonnull ItemStack... targets)
    {
        for (ItemStack input : inputs)
        {
            for (ItemStack target : targets)
            {
                if (itemMatches(target, input, strict))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean itemMatches(@Nonnull ItemStack target, @Nonnull ItemStack input, boolean strict)
    {
        if (input.func_190926_b() && !target.func_190926_b() || !input.func_190926_b() && target.func_190926_b())
        {
            return false;
        }
        return (target.func_77973_b() == input.func_77973_b() && ((target.func_77960_j() == WILDCARD_VALUE && !strict) || target.func_77960_j() == input.func_77960_j()));
    }

    //Convenience functions that make for cleaner code mod side. They all drill down to registerOre(String, int, ItemStack)
    public static void registerOre(String name, Item      ore){ registerOre(name, new ItemStack(ore));  }
    public static void registerOre(String name, Block     ore){ registerOre(name, new ItemStack(ore));  }
    public static void registerOre(String name, @Nonnull ItemStack ore){ registerOreImpl(name, ore);             }

    /**
     * Registers a ore item into the dictionary.
     * Raises the registerOre function in all registered handlers.
     *
     * @param name The name of the ore
     * @param ore The ore's ItemStack
     */
    private static void registerOreImpl(String name, @Nonnull ItemStack ore)
    {
        if ("Unknown".equals(name)) return; //prevent bad IDs.
        if (ore.func_190926_b())
        {
            FMLLog.bigWarning("Invalid registration attempt for an Ore Dictionary item with name {} has occurred. The registration has been denied to prevent crashes. The mod responsible for the registration needs to correct this.", name);
            return; //prevent bad ItemStacks.
        }

        int oreID = getOreID(name);
        // HACK: use the registry name's ID. It is unique and it knows about substitutions. Fallback to a -1 value (what Item.getIDForItem would have returned) in the case where the registry is not aware of the item yet
        // IT should be noted that -1 will fail the gate further down, if an entry already exists with value -1 for this name. This is what is broken and being warned about.
        // APPARENTLY it's quite common to do this. OreDictionary should be considered alongside Recipes - you can't make them properly until you've registered with the game.
        ResourceLocation registryName = ore.func_77973_b().delegate.name();
        int hash;
        if (registryName == null)
        {
            ModContainer modContainer = Loader.instance().activeModContainer();
            String modContainerName = modContainer == null ? null : modContainer.getName();
            FMLLog.bigWarning("A broken ore dictionary registration with name {} has occurred. It adds an item (type: {}) which is currently unknown to the game registry. This dictionary item can only support a single value when"
                    + " registered with ores like this, and NO I am not going to turn this spam off. Just register your ore dictionary entries after the GameRegistry.\n"
                    + "TO USERS: YES this is a BUG in the mod " + modContainerName + " report it to them!", name, ore.func_77973_b().getClass());
            hash = -1;
        }
        else
        {
            hash = Item.field_150901_e.func_148757_b(ore.func_77973_b().delegate.get());
        }
        if (ore.func_77952_i() != WILDCARD_VALUE)
        {
            hash |= ((ore.func_77952_i() + 1) << 16); // +1 so 0 is significant
        }

        //Add things to the baked version, and prevent duplicates
        List<Integer> ids = stackToId.get(hash);
        if (ids != null && ids.contains(oreID)) return;
        if (ids == null)
        {
            ids = Lists.newArrayList();
            stackToId.put(hash, ids);
        }
        ids.add(oreID);

        //Add to the unbaked version
        ore = ore.func_77946_l();
        idToStack.get(oreID).add(ore);
        MinecraftForge.EVENT_BUS.post(new OreRegisterEvent(name, ore));
    }

    public static class OreRegisterEvent extends Event
    {
        private final String Name;
        private final ItemStack Ore;

        public OreRegisterEvent(String name, @Nonnull ItemStack ore)
        {
            this.Name = name;
            this.Ore = ore;
        }

        public String getName()
        {
            return Name;
        }

        @Nonnull
        public ItemStack getOre()
        {
            return Ore;
        }
    }

    public static void rebakeMap()
    {
        //System.out.println("Baking OreDictionary:");
        stackToId.clear();
        for (int id = 0; id < idToStack.size(); id++)
        {
            NonNullList<ItemStack> ores = idToStack.get(id);
            if (ores == null) continue;
            for (ItemStack ore : ores)
            {
                // HACK: use the registry name's ID. It is unique and it knows about substitutions
                ResourceLocation name = ore.func_77973_b().delegate.name();
                int hash;
                if (name == null)
                {
                    FMLLog.log.debug("Defaulting unregistered ore dictionary entry for ore dictionary {}: type {} to -1", getOreName(id), ore.func_77973_b().getClass());
                    hash = -1;
                }
                else
                {
                    hash = Item.field_150901_e.func_148757_b(ore.func_77973_b().delegate.get());
                }
                if (ore.func_77952_i() != WILDCARD_VALUE)
                {
                    hash |= ((ore.func_77952_i() + 1) << 16); // +1 so meta 0 is significant
                }
                List<Integer> ids = stackToId.computeIfAbsent(hash, k -> Lists.newArrayList());
                ids.add(id);
                //System.out.println(id + " " + getOreName(id) + " " + Integer.toHexString(hash) + " " + ore);
            }
        }
    }
}
