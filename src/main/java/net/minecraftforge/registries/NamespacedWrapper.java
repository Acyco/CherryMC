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

package net.minecraftforge.registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.FMLLog;

class NamespacedWrapper<V extends IForgeRegistryEntry<V>> extends RegistryNamespaced<ResourceLocation, V> implements ILockableRegistry
{
    private boolean locked = false;
    private ForgeRegistry<V> delegate;

    public NamespacedWrapper(ForgeRegistry<V> owner)
    {
        this.delegate = owner;
    }

    @Override
    public void func_177775_a(int id, ResourceLocation key, V value)
    {
        if (locked)
            throw new IllegalStateException("Can not register to a locked registry. Modder should use Forge Register methods.");

        Validate.notNull(value);

        if (value.getRegistryName() == null)
            value.setRegistryName(key);

        int realId = this.delegate.add(id, value);
        if (realId != id && id != -1)
            FMLLog.log.warn("Registered object did not get ID it asked for. Name: {} Type: {} Expected: {} Got: {}", key, value.getRegistryType().getName(), id, realId);
    }

    @Override
    public void func_82595_a(ResourceLocation key, V value)
    {
        func_177775_a(-1, key, value);
    }


    // Reading Functions
    @Override
    @Nullable
    public V func_82594_a(@Nullable ResourceLocation name)
    {
        return this.delegate.getValue(name);
    }

    @Override
    @Nullable
    public ResourceLocation func_177774_c(V value)
    {
        return this.delegate.getKey(value);
    }

    @Override
    public boolean func_148741_d(ResourceLocation key)
    {
        return this.delegate.containsKey(key);
    }

    @Override
    public int func_148757_b(@Nullable V value)
    {
        return this.delegate.getID(value);
    }

    @Override
    @Nullable
    public V func_148754_a(int id)
    {
        return this.delegate.getValue(id);
    }

    @Override
    public Iterator<V> iterator()
    {
        return this.delegate.iterator();
    }

    @Override
    public Set<ResourceLocation> func_148742_b()
    {
        return this.delegate.getKeys();
    }

    @Override
    @Nullable
    public V func_186801_a(Random random)
    {
        Collection<V> values = this.delegate.getValuesCollection();
        return values.stream().skip(random.nextInt(values.size())).findFirst().orElse(null);
    }

    //internal
    @Override
    public void lock(){ this.locked = true; }

    public static class Factory<V extends IForgeRegistryEntry<V>> implements IForgeRegistry.CreateCallback<V>
    {
        public static final ResourceLocation ID = new ResourceLocation("forge", "registry_defaulted_wrapper");
        @Override
        public void onCreate(IForgeRegistryInternal<V> owner, RegistryManager stage)
        {
            owner.setSlaveMap(ID, new NamespacedWrapper<V>((ForgeRegistry<V>)owner));
        }
    }
}
