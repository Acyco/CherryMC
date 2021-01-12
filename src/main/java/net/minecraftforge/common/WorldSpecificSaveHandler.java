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

package net.minecraftforge.common;

import java.io.File;
import java.io.IOException;

import net.minecraft.world.gen.structure.template.TemplateManager;
import org.apache.logging.log4j.Level;

import com.google.common.io.Files;

import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.MinecraftException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLLog;

//Class used internally to provide the world specific data directories.

public class WorldSpecificSaveHandler implements ISaveHandler
{
    private WorldServer world;
    private ISaveHandler parent;
    private File dataDir;

    public WorldSpecificSaveHandler(WorldServer world, ISaveHandler parent)
    {
        this.world = world;
        this.parent = parent;
    }

    @Override public WorldInfo func_75757_d() { return parent.func_75757_d(); }
    @Override public void func_75762_c() throws MinecraftException { parent.func_75762_c(); }
    @Override public IChunkLoader func_75763_a(WorldProvider var1) { return parent.func_75763_a(var1); }
    @Override public void func_75755_a(WorldInfo var1, NBTTagCompound var2) { parent.func_75755_a(var1, var2); }
    @Override public void func_75761_a(WorldInfo var1){ parent.func_75761_a(var1); }
    @Override public IPlayerFileData func_75756_e() { return parent.func_75756_e(); }
    @Override public void func_75759_a() { parent.func_75759_a(); }
    @Override public File func_75765_b() { return parent.func_75765_b(); }

    @Override
    public File func_75758_b(String name)
    {
        if (dataDir == null) //Delayed down here do that world has time to be initialized first.
        {
            dataDir = new File(world.getChunkSaveLocation(), "data");
            dataDir.mkdirs();
        }
        File file = new File(dataDir, name + ".dat");
        if (!file.exists())
        {
            switch (world.field_73011_w.getDimension())
            {
                case -1:
                    if (name.equalsIgnoreCase("FORTRESS")) copyFile(name, file);
                    break;
                case 1:
                    if (name.equalsIgnoreCase("VILLAGES_END")) copyFile(name, file);
                    break;
            }
        }
        return file;
    }

    private void copyFile(String name, File to)
    {
        File parentFile = parent.func_75758_b(name);
        if (parentFile.exists())
        {
            try
            {
                Files.copy(parentFile, to);
            }
            catch (IOException e)
            {
                FMLLog.log.error("A critical error occurred copying {} to world specific dat folder - new file will be created.", parentFile.getName(), e);
            }
        }
    }

    @Override
    public TemplateManager func_186340_h()
    {
        return parent.func_186340_h();
    }

}
