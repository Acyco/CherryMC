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

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ShapelessOreRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    @Nonnull
    protected ItemStack output = ItemStack.field_190927_a;
    protected NonNullList<Ingredient> input = NonNullList.func_191196_a();
    protected ResourceLocation group;
    protected boolean isSimple = true;

    public ShapelessOreRecipe(ResourceLocation group, Block result, Object... recipe){ this(group, new ItemStack(result), recipe); }
    public ShapelessOreRecipe(ResourceLocation group, Item  result, Object... recipe){ this(group, new ItemStack(result), recipe); }
    public ShapelessOreRecipe(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result)
    {
        this.group = group;
        output = result.func_77946_l();
        this.input = input;
        for (Ingredient i : input)
            this.isSimple &= i.isSimple();
    }
    public ShapelessOreRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe)
    {
        this.group = group;
        output = result.func_77946_l();
        for (Object in : recipe)
        {
            Ingredient ing = CraftingHelper.getIngredient(in);
            if (ing != null)
            {
                input.add(ing);
                this.isSimple &= ing.isSimple();
            }
            else
            {
                String ret = "Invalid shapeless ore recipe: ";
                for (Object tmp :  recipe)
                {
                    ret += tmp + ", ";
                }
                ret += output;
                throw new RuntimeException(ret);
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack func_77571_b(){ return output; }

    @Override
    @Nonnull
    public ItemStack func_77572_b(@Nonnull InventoryCrafting var1){ return output.func_77946_l(); }

    @Override
    public boolean func_77569_a(@Nonnull InventoryCrafting inv, @Nonnull World world)
    {
        int ingredientCount = 0;
        RecipeItemHelper recipeItemHelper = new RecipeItemHelper();
        List<ItemStack> items = Lists.newArrayList();

        for (int i = 0; i < inv.func_70302_i_(); ++i)
        {
            ItemStack itemstack = inv.func_70301_a(i);
            if (!itemstack.func_190926_b())
            {
                ++ingredientCount;
                if (this.isSimple)
                    recipeItemHelper.accountStack(itemstack, 1);
                else
                    items.add(itemstack);
            }
        }

        if (ingredientCount != this.input.size())
            return false;

        if (this.isSimple)
            return recipeItemHelper.func_194116_a(this, null);

        return RecipeMatcher.findMatches(items, this.input) != null;
    }

    @Override
    @Nonnull
    public NonNullList<Ingredient> func_192400_c()
    {
        return this.input;
    }

    @Override
    @Nonnull
    public String func_193358_e()
    {
        return this.group == null ? "" : this.group.toString();
    }

    @Override
    public boolean func_194133_a(int p_194133_1_, int p_194133_2_)
    {
        return p_194133_1_ * p_194133_2_ >= this.input.size();
    }

    public static ShapelessOreRecipe factory(JsonContext context, JsonObject json)
    {
        String group = JsonUtils.func_151219_a(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.func_191196_a();
        for (JsonElement ele : JsonUtils.func_151214_t(json, "ingredients"))
            ings.add(CraftingHelper.getIngredient(ele, context));

        if (ings.isEmpty())
            throw new JsonParseException("No ingredients for shapeless recipe");

        ItemStack itemstack = CraftingHelper.getItemStack(JsonUtils.func_152754_s(json, "result"), context);
        return new ShapelessOreRecipe(group.isEmpty() ? null : new ResourceLocation(group), ings, itemstack);
    }
}
