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

package net.minecraftforge.client.model.obj;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.FMLLog;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;

/*
 * Loader for OBJ models.
 * To enable your mod call instance.addDomain(modid).
 * If you need more control over accepted resources - extend the class, and register a new instance with ModelLoaderRegistry.
 */
public enum OBJLoader implements ICustomModelLoader {
    INSTANCE;

    private IResourceManager manager;
    private final Set<String> enabledDomains = new HashSet<>();

    public void addDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
        FMLLog.log.info("OBJLoader: Domain {} has been added.", domain.toLowerCase());
    }

    @Override
    public void func_110549_a(IResourceManager resourceManager)
    {
        this.manager = resourceManager;
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        return enabledDomains.contains(modelLocation.func_110624_b()) && modelLocation.func_110623_a().endsWith(".obj");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        ResourceLocation file = new ResourceLocation(modelLocation.func_110624_b(), modelLocation.func_110623_a());
        IResource resource = null;
        try
        {
            try
            {
                resource = manager.func_110536_a(file);
            }
            catch (FileNotFoundException e)
            {
                if (modelLocation.func_110623_a().startsWith("models/block/"))
                    resource = manager.func_110536_a(new ResourceLocation(file.func_110624_b(), "models/item/" + file.func_110623_a().substring("models/block/".length())));
                else if (modelLocation.func_110623_a().startsWith("models/item/"))
                    resource = manager.func_110536_a(new ResourceLocation(file.func_110624_b(), "models/block/" + file.func_110623_a().substring("models/item/".length())));
                else throw e;
            }
            OBJModel.Parser parser = new OBJModel.Parser(resource, manager);
            OBJModel model;
            try
            {
                model = parser.parse();
            }
            catch (Exception e)
            {
                throw new ModelLoaderRegistry.LoaderException("Error loading model previously: " + file, e);
            }
            return model;
        }
        finally
        {
            IOUtils.closeQuietly(resource);
        }
    }
}