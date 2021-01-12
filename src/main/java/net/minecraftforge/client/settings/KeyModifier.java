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

package net.minecraftforge.client.settings;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public enum KeyModifier {
    CONTROL {
        @Override
        public boolean matches(int keyCode)
        {
            if (Minecraft.field_142025_a)
            {
                return keyCode == Keyboard.KEY_LMETA || keyCode == Keyboard.KEY_RMETA;
            }
            else
            {
                return keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL;
            }
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.func_146271_m();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.func_146271_m();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.func_74298_c(keyCode);
            String localizationFormatKey = Minecraft.field_142025_a ? "forge.controlsgui.control.mac" : "forge.controlsgui.control";
            return I18n.func_135052_a(localizationFormatKey, keyName);
        }
    },
    SHIFT {
        @Override
        public boolean matches(int keyCode)
        {
            return keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT;
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.func_146272_n();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.func_146272_n();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.func_74298_c(keyCode);
            return I18n.func_135052_a("forge.controlsgui.shift", keyName);
        }
    },
    ALT {
        @Override
        public boolean matches(int keyCode)
        {
            return keyCode == Keyboard.KEY_LMENU || keyCode == Keyboard.KEY_RMENU;
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.func_175283_s();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.func_175283_s();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.func_74298_c(keyCode);
            return I18n.func_135052_a("forge.controlsgui.alt", keyName);
        }
    },
    NONE {
        @Override
        public boolean matches(int keyCode)
        {
            return false;
        }

        @Override
        public boolean isActive()
        {
            return true;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            if (conflictContext != null && !conflictContext.conflicts(KeyConflictContext.IN_GAME))
            {
                for (KeyModifier keyModifier : MODIFIER_VALUES)
                {
                    if (keyModifier.isActive(conflictContext))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            return GameSettings.func_74298_c(keyCode);
        }
    };

    public static final KeyModifier[] MODIFIER_VALUES = {SHIFT, CONTROL, ALT};

    public static KeyModifier getActiveModifier()
    {
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.isActive(null))
            {
                return keyModifier;
            }
        }
        return NONE;
    }

    public static boolean isKeyCodeModifier(int keyCode)
    {
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.matches(keyCode))
            {
                return true;
            }
        }
        return false;
    }

    public static KeyModifier valueFromString(String stringValue)
    {
        try
        {
            return valueOf(stringValue);
        }
        catch (NullPointerException | IllegalArgumentException ignored)
        {
            return NONE;
        }
    }

    public abstract boolean matches(int keyCode);

    /**
     * @deprecated use {@link #isActive(IKeyConflictContext)}
     */
    @Deprecated
    public abstract boolean isActive();

    public abstract boolean isActive(@Nullable IKeyConflictContext conflictContext);

    public abstract String getLocalizedComboName(int keyCode);
}
