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

package net.minecraftforge.common.brewing;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;

import javax.annotation.Nonnull;

/**
 * Used in BrewingRecipeRegistry to maintain the vanilla behaviour.
 *
 * Most of the code was simply adapted from net.minecraft.tileentity.TileEntityBrewingStand
 */
public class VanillaBrewingRecipe implements IBrewingRecipe {

    /**
     * Code adapted from TileEntityBrewingStand.isItemValidForSlot(int index, ItemStack stack)
     */
    @Override
    public boolean isInput(@Nonnull ItemStack stack)
    {
        Item item = stack.func_77973_b();
        return item == Items.field_151068_bn || item == Items.field_185155_bH || item == Items.field_185156_bI || item == Items.field_151069_bo;
    }

    /**
     * Code adapted from TileEntityBrewingStand.isItemValidForSlot(int index, ItemStack stack)
     */
    @Override
    public boolean isIngredient(@Nonnull ItemStack stack)
    {
        return PotionHelper.func_185205_a(stack);
    }

    /**
     * Code copied from TileEntityBrewingStand.brewPotions()
     * It brews the potion by doing the bit-shifting magic and then checking if the new PotionEffect list is different to the old one,
     * or if the new potion is a splash potion when the old one wasn't.
     */
    @Override
    @Nonnull
    public ItemStack getOutput(@Nonnull ItemStack input, @Nonnull ItemStack ingredient)
    {
        if (!input.func_190926_b() && !ingredient.func_190926_b() && isIngredient(ingredient))
        {
            ItemStack result = PotionHelper.func_185212_d(ingredient, input);
            if (result != input)
            {
                return result;
            }
            return ItemStack.field_190927_a;
        }

        return ItemStack.field_190927_a;
    }
}
