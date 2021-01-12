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

package net.minecraftforge.client.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition.MissingVariantException;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.animation.AnimationItemOverrideList;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.Models;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.IClip;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.registries.IRegistryDelegate;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ModelLoader extends ModelBakery
{
    private final Map<ModelResourceLocation, IModel> stateModels = Maps.newHashMap();
    private final Map<ModelResourceLocation, ModelBlockDefinition> multipartDefinitions = Maps.newHashMap();
    private final Map<ModelBlockDefinition, IModel> multipartModels = Maps.newHashMap();
    // TODO: nothing adds to missingVariants, remove it?
    private final Set<ModelResourceLocation> missingVariants = Sets.newHashSet();
    private final Map<ResourceLocation, Exception> loadingExceptions = Maps.newHashMap();
    private IModel missingModel = null;

    private boolean isLoading = false;
    public boolean isLoading()
    {
        return isLoading;
    }

    private final boolean enableVerboseMissingInfo = FMLLaunchHandler.isDeobfuscatedEnvironment() || Boolean.parseBoolean(System.getProperty("forge.verboseMissingModelLogging", "false"));
    private final int verboseMissingInfoCount = Integer.parseInt(System.getProperty("forge.verboseMissingModelLoggingCount", "5"));

    public ModelLoader(IResourceManager manager, TextureMap map, BlockModelShapes shapes)
    {
        super(manager, map, shapes);
        VanillaLoader.INSTANCE.setLoader(this);
        VariantLoader.INSTANCE.setLoader(this);
        ModelLoaderRegistry.clearModelCache(manager);
    }

    @Nonnull
    @Override
    public IRegistry<ModelResourceLocation, IBakedModel> func_177570_a()
    {
        if (FMLClientHandler.instance().hasError()) // skip loading models if we're just going to show a fatal error screen
            return field_177605_n;

        isLoading = true;
        func_188640_b();
        func_177577_b();
        missingModel = ModelLoaderRegistry.getMissingModel();
        stateModels.put(field_177604_a, missingModel);

        final Set<ResourceLocation> textures = Sets.newHashSet(ModelLoaderRegistry.getTextures());
        textures.remove(TextureMap.field_174945_f);
        textures.addAll(field_177602_b);

        field_177609_j.func_174943_a(field_177598_f, map -> textures.forEach(map::func_174942_a));

        IBakedModel missingBaked = missingModel.bake(missingModel.getDefaultState(), DefaultVertexFormats.field_176599_b, DefaultTextureGetter.INSTANCE);
        Map<IModel, IBakedModel> bakedModels = Maps.newHashMap();
        HashMultimap<IModel, ModelResourceLocation> models = HashMultimap.create();
        Multimaps.invertFrom(Multimaps.forMap(stateModels), models);

        ProgressBar bakeBar = ProgressManager.push("ModelLoader: baking", models.keySet().size());

        for(IModel model : models.keySet())
        {
            String modelLocations = "[" + Joiner.on(", ").join(models.get(model)) + "]";
            bakeBar.step(modelLocations);
            if(model == getMissingModel())
            {
                bakedModels.put(model, missingBaked);
            }
            else
            {
                try
                {
                    bakedModels.put(model, model.bake(model.getDefaultState(), DefaultVertexFormats.field_176599_b, DefaultTextureGetter.INSTANCE));
                }
                catch (Exception e)
                {
                    FMLLog.log.error("Exception baking model for location(s) {}:", modelLocations, e);
                    bakedModels.put(model, missingBaked);
                }
            }
        }

        ProgressManager.pop(bakeBar);

        for (Entry<ModelResourceLocation, IModel> e : stateModels.entrySet())
        {
            field_177605_n.func_82595_a(e.getKey(), bakedModels.get(e.getValue()));
        }
        return field_177605_n;
    }

    // NOOP, replaced by dependency resolution
    @Override
    protected void func_177595_c() {}

    // NOOP, replaced by dependency resolution
    @Override
    protected void func_188637_e() {}

    @Override
    protected void func_188640_b()
    {
        List<Block> blocks = StreamSupport.stream(Block.field_149771_c.spliterator(), false)
                .filter(block -> block.getRegistryName() != null)
                .sorted(Comparator.comparing(b -> b.getRegistryName().toString()))
                .collect(Collectors.toList());
        ProgressBar blockBar = ProgressManager.push("ModelLoader: blocks", blocks.size());

        BlockStateMapper mapper = this.field_177610_k.func_178120_a();

        for(Block block : blocks)
        {
            blockBar.step(block.getRegistryName().toString());
            for(ResourceLocation location : mapper.func_188182_a(block))
            {
                loadBlock(mapper, block, location);
            }
        }
        ProgressManager.pop(blockBar);
    }

    @Override
    protected void func_177569_a(@Nullable ModelBlockDefinition definition, ModelResourceLocation location)
    {
        IModel model;
        try
        {
            model = ModelLoaderRegistry.getModel(location);
        }
        catch(Exception e)
        {
            storeException(location, e);
            model = ModelLoaderRegistry.getMissingModel(location, e);
        }
        stateModels.put(location, model);
    }

    @Override
    protected void registerMultipartVariant(ModelBlockDefinition definition, Collection<ModelResourceLocation> locations)
    {
        for (ModelResourceLocation location : locations)
        {
            multipartDefinitions.put(location, definition);
            func_177569_a(null, location);
        }
    }

    private void storeException(ResourceLocation location, Exception exception)
    {
        loadingExceptions.put(location, exception);
    }

    @Override
    protected ModelBlockDefinition func_177586_a(ResourceLocation location)
    {
        try
        {
            return super.func_177586_a(location);
        }
        catch (Exception exception)
        {
            storeException(location, new Exception("Could not load model definition for variant " + location, exception));
        }
        return new ModelBlockDefinition(new ArrayList<>());
    }

    @Override
    protected void func_177590_d()
    {
        func_177592_e();

        List<Item> items = StreamSupport.stream(Item.field_150901_e.spliterator(), false)
                .filter(item -> item.getRegistryName() != null)
                .sorted(Comparator.comparing(i -> i.getRegistryName().toString()))
                .collect(Collectors.toList());

        ProgressBar itemBar = ProgressManager.push("ModelLoader: items", items.size());
        for(Item item : items)
        {
            itemBar.step(item.getRegistryName().toString());
            for(String s : func_177596_a(item))
            {
                ResourceLocation file = func_177583_a(s);
                ModelResourceLocation memory = getInventoryVariant(s);
                IModel model = ModelLoaderRegistry.getMissingModel();
                Exception exception = null;
                try
                {
                    model = ModelLoaderRegistry.getModel(memory);
                }
                catch (Exception blockstateException)
                {
                    try
                    {
                        model = ModelLoaderRegistry.getModel(file);
                        ModelLoaderRegistry.addAlias(memory, file);
                    }
                    catch (Exception normalException)
                    {
                        exception = new ItemLoadingException("Could not load item model either from the normal location " + file + " or from the blockstate", normalException, blockstateException);
                    }
                }
                if (exception != null)
                {
                    storeException(memory, exception);
                    model = ModelLoaderRegistry.getMissingModel(memory, exception);
                }
                stateModels.put(memory, model);
            }
        }
        ProgressManager.pop(itemBar);
    }

    /**
     * Hooked from ModelBakery, allows using MRLs that don't end with "inventory" for items.
     */
    public static ModelResourceLocation getInventoryVariant(String s)
    {
        if(s.contains("#"))
        {
            return new ModelResourceLocation(s);
        }
        return new ModelResourceLocation(s, "inventory");
    }

    @Override
    protected ResourceLocation func_177580_d(ResourceLocation model)
    {
        return new ResourceLocation(model.func_110624_b(), model.func_110623_a() + ".json");
    }

    private final class VanillaModelWrapper implements IModel
    {
        private final ResourceLocation location;
        private final ModelBlock model;
        private final boolean uvlock;
        private final ModelBlockAnimation animation;

        public VanillaModelWrapper(ResourceLocation location, ModelBlock model, boolean uvlock, ModelBlockAnimation animation)
        {
            this.location = location;
            this.model = model;
            this.uvlock = uvlock;
            this.animation = animation;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            Set<ResourceLocation> set = Sets.newHashSet();
            for(ResourceLocation dep : model.func_187965_e())
            {
                if(!location.equals(dep))
                {
                    set.add(dep);
                    // TODO: check if this can go somewhere else, random access to global things is bad
                    stateModels.put(getInventoryVariant(dep.toString()), ModelLoaderRegistry.getModelOrLogError(dep, "Could not load override model " + dep + " for model " + location));
                }
            }
            if(model.func_178305_e() != null && !model.func_178305_e().func_110623_a().startsWith("builtin/"))
            {
                set.add(model.func_178305_e());
            }
            return ImmutableSet.copyOf(set);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            // setting parent here to make textures resolve properly
            ResourceLocation parentLocation = model.func_178305_e();
            if(parentLocation != null && model.field_178315_d == null)
            {
                if(parentLocation.func_110623_a().equals("builtin/generated"))
                {
                    model.field_178315_d = field_177606_o;
                }
                else
                {
                    model.field_178315_d = ModelLoaderRegistry.getModelOrLogError(parentLocation, "Could not load vanilla model parent '" + parentLocation + "' for '" + model + "'")
                            .asVanillaModel().orElseThrow(() -> new IllegalStateException("vanilla model '" + model + "' can't have non-vanilla parent"));
                }
            }

            ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

            if(func_177581_b(model))
            {
                for(String s : ItemModelGenerator.field_178398_a)
                {
                    String r = model.func_178308_c(s);
                    ResourceLocation loc = new ResourceLocation(r);
                    if(!r.equals(s))
                    {
                        builder.add(loc);
                    }
                }
            }
            for(String s : model.field_178318_c.values())
            {
                if(!s.startsWith("#"))
                {
                    builder.add(new ResourceLocation(s));
                }
            }
            return builder.build();
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            return VanillaLoader.INSTANCE.modelCache.getUnchecked(new BakedModelCacheKey(this, state, format, bakedTextureGetter));
        }

        public IBakedModel bakeImpl(IModelState state, final VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            if(!Attributes.moreSpecific(format, Attributes.DEFAULT_BAKED_FORMAT))
            {
                throw new IllegalArgumentException("can't bake vanilla models to the format that doesn't fit into the default one: " + format);
            }
            ModelBlock model = this.model;
            if(model == null) return getMissingModel().bake(getMissingModel().getDefaultState(), format, bakedTextureGetter);

            List<TRSRTransformation> newTransforms = Lists.newArrayList();
            for(int i = 0; i < model.func_178298_a().size(); i++)
            {
                BlockPart part = model.func_178298_a().get(i);
                newTransforms.add(animation.getPartTransform(state, part, i));
            }

            ItemCameraTransforms transforms = model.func_181682_g();
            Map<TransformType, TRSRTransformation> tMap = Maps.newEnumMap(TransformType.class);
            tMap.putAll(PerspectiveMapWrapper.getTransforms(transforms));
            tMap.putAll(PerspectiveMapWrapper.getTransforms(state));
            IModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap), state.apply(Optional.empty()));

            if(func_177581_b(model))
            {
                return new ItemLayerModel(model).bake(perState, format, bakedTextureGetter);
            }
            if(func_177587_c(model)) return new BuiltInModel(transforms, model.func_187967_g());
            return bakeNormal(model, perState, state, newTransforms, format, bakedTextureGetter, uvlock);
        }

        private IBakedModel bakeNormal(ModelBlock model, IModelState perState, final IModelState modelState, List<TRSRTransformation> newTransforms, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, boolean uvLocked)
        {
            final TRSRTransformation baseState = modelState.apply(Optional.empty()).orElse(TRSRTransformation.identity());
            TextureAtlasSprite particle = bakedTextureGetter.apply(new ResourceLocation(model.func_178308_c("particle")));
            SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(model, model.func_187967_g())).func_177646_a(particle);
            for(int i = 0; i < model.func_178298_a().size(); i++)
            {
                if(modelState.apply(Optional.of(Models.getHiddenModelPart(ImmutableList.of(Integer.toString(i))))).isPresent())
                {
                    continue;
                }
                BlockPart part = model.func_178298_a().get(i);
                TRSRTransformation transformation = baseState;
                if(newTransforms.get(i) != null)
                {
                    transformation = transformation.compose(newTransforms.get(i));
                    BlockPartRotation rot = part.field_178237_d;
                    if(rot == null) rot = new BlockPartRotation(new org.lwjgl.util.vector.Vector3f(), EnumFacing.Axis.Y, 0, false);
                    part = new BlockPart(part.field_178241_a, part.field_178239_b, part.field_178240_c, rot, part.field_178238_e);
                }
                for(Entry<EnumFacing, BlockPartFace> e : part.field_178240_c.entrySet())
                {
                    TextureAtlasSprite textureatlassprite1 = bakedTextureGetter.apply(new ResourceLocation(model.func_178308_c(e.getValue().field_178242_d)));

                    if (e.getValue().field_178244_b == null || !TRSRTransformation.isInteger(transformation.getMatrix()))
                    {
                        builder.func_177648_a(makeBakedQuad(part, e.getValue(), textureatlassprite1, e.getKey(), transformation, uvLocked));
                    }
                    else
                    {
                        builder.func_177650_a(baseState.rotate(e.getValue().field_178244_b), makeBakedQuad(part, e.getValue(), textureatlassprite1, e.getKey(), transformation, uvLocked));
                    }
                }
            }

            return new PerspectiveMapWrapper(builder.func_177645_b(), perState)
            {
                private final ItemOverrideList overrides = new AnimationItemOverrideList(VanillaModelWrapper.this, modelState, format, bakedTextureGetter, super.func_188617_f());

                @Override
                public List<BakedQuad> func_188616_a(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
                {
                    if(state instanceof IExtendedBlockState)
                    {
                        IExtendedBlockState exState = (IExtendedBlockState)state;
                        if(exState.getUnlistedNames().contains(Properties.AnimationProperty))
                        {
                            IModelState newState = exState.getValue(Properties.AnimationProperty);
                            IExtendedBlockState newExState = exState.withProperty(Properties.AnimationProperty, null);
                            if(newState != null)
                            {
                                return VanillaModelWrapper.this.bake(new ModelStateComposition(modelState, newState), format, bakedTextureGetter).func_188616_a(newExState, side, rand);
                            }
                        }
                    }
                    return super.func_188616_a(state, side, rand);
                }

                @Override
                public ItemOverrideList func_188617_f()
                {
                    return overrides;
                }
            };
        }

        @Override
        public VanillaModelWrapper retexture(ImmutableMap<String, String> textures)
        {
            if (textures.isEmpty())
                return this;

            List<BlockPart> elements = Lists.newArrayList(); //We have to duplicate this so we can edit it below.
            for (BlockPart part : this.model.func_178298_a())
            {
                elements.add(new BlockPart(part.field_178241_a, part.field_178239_b, Maps.newHashMap(part.field_178240_c), part.field_178237_d, part.field_178238_e));
            }

            ModelBlock newModel = new ModelBlock(this.model.func_178305_e(), elements,
                Maps.newHashMap(this.model.field_178318_c), this.model.func_178309_b(), this.model.func_178311_c(), //New Textures man VERY IMPORTANT
                model.func_181682_g(), Lists.newArrayList(model.func_187966_f()));
            newModel.field_178317_b = this.model.field_178317_b;
            newModel.field_178315_d = this.model.field_178315_d;

            Set<String> removed = Sets.newHashSet();

            for (Entry<String, String> e : textures.entrySet())
            {
                if ("".equals(e.getValue()))
                {
                    removed.add(e.getKey());
                    newModel.field_178318_c.remove(e.getKey());
                }
                else
                    newModel.field_178318_c.put(e.getKey(), e.getValue());
            }

            // Map the model's texture references as if it was the parent of a model with the retexture map as its textures.
            Map<String, String> remapped = Maps.newHashMap();

            for (Entry<String, String> e : newModel.field_178318_c.entrySet())
            {
                if (e.getValue().startsWith("#"))
                {
                    String key = e.getValue().substring(1);
                    if (newModel.field_178318_c.containsKey(key))
                        remapped.put(e.getKey(), newModel.field_178318_c.get(key));
                }
            }

            newModel.field_178318_c.putAll(remapped);

            //Remove any faces that use a null texture, this is for performance reasons, also allows some cool layering stuff.
            for (BlockPart part : newModel.func_178298_a())
            {
                part.field_178240_c.entrySet().removeIf(entry -> removed.contains(entry.getValue().field_178242_d));
            }

            return new VanillaModelWrapper(location, newModel, uvlock, animation);
        }

        @Override
        public Optional<? extends IClip> getClip(String name)
        {
            if(animation.getClips().containsKey(name))
            {
                return Optional.ofNullable(animation.getClips().get(name));
            }
            return Optional.empty();
        }

        @Override
        public VanillaModelWrapper smoothLighting(boolean value)
        {
            if(model.field_178322_i == value)
            {
                return this;
            }
            ModelBlock newModel = new ModelBlock(model.func_178305_e(), model.func_178298_a(), model.field_178318_c, value, model.func_178311_c(), model.func_181682_g(), Lists.newArrayList(model.func_187966_f()));
            newModel.field_178315_d = model.field_178315_d;
            newModel.field_178317_b = model.field_178317_b;
            return new VanillaModelWrapper(location, newModel, uvlock, animation);
        }

        @Override
        public VanillaModelWrapper gui3d(boolean value)
        {
            if(model.func_178311_c() == value)
            {
                return this;
            }
            ModelBlock newModel = new ModelBlock(model.func_178305_e(), model.func_178298_a(), model.field_178318_c, model.field_178322_i, value, model.func_181682_g(), Lists.newArrayList(model.func_187966_f()));
            newModel.field_178315_d = model.field_178315_d;
            newModel.field_178317_b = model.field_178317_b;
            return new VanillaModelWrapper(location, newModel, uvlock, animation);
        }

        @Override
        public IModel uvlock(boolean value)
        {
            if(uvlock == value)
            {
                return this;
            }
            return new VanillaModelWrapper(location, model, value, animation);
        }

        @Override
        public Optional<ModelBlock> asVanillaModel()
        {
            return Optional.of(model);
        }
    }

    private static final class WeightedRandomModel implements IModel
    {
        private final List<Variant> variants;
        private final List<ResourceLocation> locations;
        private final Set<ResourceLocation> textures;
        private final List<IModel> models;
        private final IModelState defaultState;

        public WeightedRandomModel(ResourceLocation parent, VariantList variants) throws Exception
        {
            this.variants = variants.func_188114_a();
            this.locations = new ArrayList<>();
            this.textures = Sets.newHashSet();
            this.models = new ArrayList<>();
            ImmutableList.Builder<Pair<IModel, IModelState>> builder = ImmutableList.builder();
            for (Variant v : this.variants)
            {
                ResourceLocation loc = v.func_188046_a();
                locations.add(loc);

                /*
                 * Vanilla eats this, which makes it only show variants that have models.
                 * But that doesn't help debugging, so throw the exception
                 */
                IModel model;
                if(loc.equals(field_177604_a))
                {
                    // explicit missing location, happens if blockstate has "model"=null
                    model = ModelLoaderRegistry.getMissingModel();
                }
                else
                {
                    model = ModelLoaderRegistry.getModel(loc);
                }

                // FIXME: is this the place? messes up dependency and texture resolution
                model = v.process(model);
                for(ResourceLocation location : model.getDependencies())
                {
                    ModelLoaderRegistry.getModelOrMissing(location);
                }
                //FMLLog.getLogger().error("Exception resolving indirect dependencies for model" + loc, e);
                textures.addAll(model.getTextures()); // Kick this, just in case.

                models.add(model);

                IModelState modelDefaultState = model.getDefaultState();
                Preconditions.checkNotNull(modelDefaultState, "Model %s returned null as default state", loc);
                builder.add(Pair.of(model, new ModelStateComposition(v.getState(), modelDefaultState)));
            }

            if (models.size() == 0) //If all variants are missing, add one with the missing model and default rotation.
            {
                // FIXME: log this?
                IModel missing = ModelLoaderRegistry.getMissingModel();
                models.add(missing);
                builder.add(Pair.of(missing, TRSRTransformation.identity()));
            }

            defaultState = new MultiModelState(builder.build());
        }

        private WeightedRandomModel(List<Variant> variants, List<ResourceLocation> locations, Set<ResourceLocation> textures, List<IModel> models, IModelState defaultState)
        {
            this.variants = variants;
            this.locations = locations;
            this.textures = textures;
            this.models = models;
            this.defaultState = defaultState;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return ImmutableList.copyOf(locations);
        }

        @Override
        public Collection<ResourceLocation> getTextures()
        {
            return ImmutableSet.copyOf(textures);
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            if(!Attributes.moreSpecific(format, Attributes.DEFAULT_BAKED_FORMAT))
            {
                throw new IllegalArgumentException("can't bake vanilla weighted models to the format that doesn't fit into the default one: " + format);
            }
            if(variants.size() == 1)
            {
                IModel model = models.get(0);
                return model.bake(MultiModelState.getPartState(state, model, 0), format, bakedTextureGetter);
            }
            WeightedBakedModel.Builder builder = new WeightedBakedModel.Builder();
            for(int i = 0; i < variants.size(); i++)
            {
                IModel model = models.get(i);
                builder.func_177677_a(model.bake(MultiModelState.getPartState(state, model, i), format, bakedTextureGetter), variants.get(i).func_188047_d());
            }
            return builder.func_177676_a();
        }

        @Override
        public IModelState getDefaultState()
        {
            return defaultState;
        }

        @Override
        public WeightedRandomModel retexture(ImmutableMap<String, String> textures)
        {
            if (textures.isEmpty())
                return this;

            // rebuild the texture list taking into account new textures
            Set<ResourceLocation> modelTextures = Sets.newHashSet();
            // also recreate the MultiModelState so IModelState data is properly applied to the retextured model
            ImmutableList.Builder<Pair<IModel, IModelState>> builder = ImmutableList.builder();
            List<IModel> retexturedModels = Lists.newArrayList();
            for(int i = 0; i < this.variants.size(); i++)
            {
                IModel retextured = this.models.get(i).retexture(textures);
                modelTextures.addAll(retextured.getTextures());
                retexturedModels.add(retextured);
                builder.add(Pair.of(retextured, this.variants.get(i).getState()));
            }

            return new WeightedRandomModel(this.variants, this.locations, modelTextures, retexturedModels, new MultiModelState(builder.build()));
        }
    }

    protected IModel getMissingModel()
    {
        if (missingModel == null)
        {
            try
            {
                missingModel = VanillaLoader.INSTANCE.loadModel(new ResourceLocation(field_177604_a.func_110624_b(), field_177604_a.func_110623_a()));
            }
            catch(Exception e)
            {
                throw new RuntimeException("Missing the missing model, this should never happen");
            }
        }
        return missingModel;
    }

    protected final class BakedModelCacheKey
    {
        private final VanillaModelWrapper model;
        private final IModelState state;
        private final VertexFormat format;
        private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

        public BakedModelCacheKey(VanillaModelWrapper model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            this.model = model;
            this.state = state;
            this.format = format;
            this.bakedTextureGetter = bakedTextureGetter;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            BakedModelCacheKey that = (BakedModelCacheKey) o;
            return Objects.equal(model, that.model) && Objects.equal(state, that.state) && Objects.equal(format, that.format) && Objects.equal(bakedTextureGetter, that.bakedTextureGetter);
        }

        @Override
        public int hashCode()
        {
            return Objects.hashCode(model, state, format, bakedTextureGetter);
        }
    }

    protected static enum VanillaLoader implements ICustomModelLoader
    {
        INSTANCE;

        @Nullable
        private ModelLoader loader;
        private LoadingCache<BakedModelCacheKey, IBakedModel> modelCache = CacheBuilder.newBuilder().maximumSize(50).expireAfterWrite(100, TimeUnit.MILLISECONDS).build(new CacheLoader<BakedModelCacheKey, IBakedModel>() {
            @Override
            public IBakedModel load(BakedModelCacheKey key) throws Exception
            {
                return key.model.bakeImpl(key.state, key.format, key.bakedTextureGetter);
            }
        });

        void setLoader(ModelLoader loader)
        {
            this.loader = loader;
        }

        @Nullable
        ModelLoader getLoader()
        {
            return loader;
        }

        // NOOP, handled in loader
        @Override
        public void func_110549_a(IResourceManager resourceManager) {}

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return true;
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            if(modelLocation.equals(field_177604_a) && loader.missingModel != null)
            {
                return loader.getMissingModel();
            }
            String modelPath = modelLocation.func_110623_a();
            if(modelLocation.func_110623_a().startsWith("models/"))
            {
                modelPath = modelPath.substring("models/".length());
            }
            ResourceLocation armatureLocation = new ResourceLocation(modelLocation.func_110624_b(), "armatures/" + modelPath + ".json");
            ModelBlockAnimation animation = ModelBlockAnimation.loadVanillaAnimation(loader.field_177598_f, armatureLocation);
            ModelBlock model = loader.func_177594_c(modelLocation);
            IModel iModel = loader.new VanillaModelWrapper(modelLocation, model, false, animation);
            if(loader.missingModel == null && modelLocation.equals(field_177604_a))
            {
                loader.missingModel = iModel;
            }
            return iModel;
        }

        @Override
        public String toString()
        {
            return "VanillaLoader.INSTANCE";
        }
    }

    /**
     * 16x16 pure white sprite.
     */
    public static final class White extends TextureAtlasSprite
    {
        public static final ResourceLocation LOCATION = new ResourceLocation("white");
        public static final White INSTANCE = new White();

        private White()
        {
            super(LOCATION.toString());
            this.field_130223_c = this.field_130224_d = 16;
        }

        @Override
        public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
        {
            return true;
        }

        @Override
        public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
        {
            BufferedImage image = new BufferedImage(this.func_94211_a(), this.func_94216_b(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setBackground(Color.WHITE);
            graphics.clearRect(0, 0, this.func_94211_a(), this.func_94216_b());
            int[][] pixels = new int[Minecraft.func_71410_x().field_71474_y.field_151442_I + 1][];
            pixels[0] = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels[0], 0, image.getWidth());
            this.func_130103_l();
            this.field_110976_a.add(pixels);
            return false;
        }

        public void register(TextureMap map)
        {
            map.setTextureEntry(White.INSTANCE);
        }
    }

    private static class ItemLoadingException extends ModelLoaderRegistry.LoaderException
    {
        private final Exception normalException;
        private final Exception blockstateException;

        public ItemLoadingException(String message, Exception normalException, Exception blockstateException)
        {
            super(message);
            this.normalException = normalException;
            this.blockstateException = blockstateException;
        }
    }

    /**
     * Internal, do not use.
     */
    public void onPostBakeEvent(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry)
    {
        if (!isLoading) return;

        IBakedModel missingModel = modelRegistry.func_82594_a(field_177604_a);
        Map<String, Integer> modelErrors = Maps.newHashMap();
        Set<ResourceLocation> printedBlockStateErrors = Sets.newHashSet();
        Multimap<ModelResourceLocation, IBlockState> reverseBlockMap = null;
        Multimap<ModelResourceLocation, String> reverseItemMap = HashMultimap.create();
        if(enableVerboseMissingInfo)
        {
            reverseBlockMap = HashMultimap.create();
            for(Entry<IBlockState, ModelResourceLocation> entry : field_177610_k.func_178120_a().func_178446_a().entrySet())
            {
                reverseBlockMap.put(entry.getValue(), entry.getKey());
            }
            ForgeRegistries.ITEMS.forEach(item ->
            {
                for(String s : func_177596_a(item))
                {
                    ModelResourceLocation memory = getInventoryVariant(s);
                    reverseItemMap.put(memory, item.getRegistryName().toString());
                }
            });
        }

        for(Entry<ResourceLocation, Exception> entry : loadingExceptions.entrySet())
        {
            // ignoring pure ResourceLocation arguments, all things we care about pass ModelResourceLocation
            if(entry.getKey() instanceof ModelResourceLocation)
            {
                ModelResourceLocation location = (ModelResourceLocation)entry.getKey();
                IBakedModel model = modelRegistry.func_82594_a(location);
                if(model == null || model == missingModel || model instanceof FancyMissingModel.BakedModel)
                {
                    String domain = entry.getKey().func_110624_b();
                    Integer errorCountBox = modelErrors.get(domain);
                    int errorCount = errorCountBox == null ? 0 : errorCountBox;
                    errorCount++;
                    if(errorCount < verboseMissingInfoCount)
                    {
                        String errorMsg = "Exception loading model for variant " + entry.getKey();
                        if(enableVerboseMissingInfo)
                        {
                            Collection<IBlockState> blocks = reverseBlockMap.get(location);
                            if(!blocks.isEmpty())
                            {
                                if(blocks.size() == 1)
                                {
                                    errorMsg += " for blockstate \"" + blocks.iterator().next() + "\"";
                                }
                                else
                                {
                                    errorMsg += " for blockstates [\"" + Joiner.on("\", \"").join(blocks) + "\"]";
                                }
                            }
                            Collection<String> items = reverseItemMap.get(location);
                            if(!items.isEmpty())
                            {
                                if(!blocks.isEmpty()) errorMsg += " and";
                                if(items.size() == 1)
                                {
                                    errorMsg += " for item \"" + items.iterator().next() + "\"";
                                }
                                else
                                {
                                    errorMsg += " for items [\"" + Joiner.on("\", \"").join(items) + "\"]";
                                }
                            }
                        }
                        if(entry.getValue() instanceof ItemLoadingException)
                        {
                            ItemLoadingException ex = (ItemLoadingException)entry.getValue();
                            FMLLog.log.error("{}, normal location exception: ", errorMsg, ex.normalException);
                            FMLLog.log.error("{}, blockstate location exception: ", errorMsg, ex.blockstateException);
                        }
                        else
                        {
                            FMLLog.log.error(errorMsg, entry.getValue());
                        }
                        ResourceLocation blockstateLocation = new ResourceLocation(location.func_110624_b(), location.func_110623_a());
                        if(loadingExceptions.containsKey(blockstateLocation) && !printedBlockStateErrors.contains(blockstateLocation))
                        {
                            FMLLog.log.error("Exception loading blockstate for the variant {}: ", location, loadingExceptions.get(blockstateLocation));
                            printedBlockStateErrors.add(blockstateLocation);
                        }
                    }
                    modelErrors.put(domain, errorCount);
                }
                if(model == null)
                {
                    modelRegistry.func_82595_a(location, missingModel);
                }
            }
        }
        for(ModelResourceLocation missing : missingVariants)
        {
            IBakedModel model = modelRegistry.func_82594_a(missing);
            if(model == null || model == missingModel)
            {
                String domain = missing.func_110624_b();
                Integer errorCountBox = modelErrors.get(domain);
                int errorCount = errorCountBox == null ? 0 : errorCountBox;
                errorCount++;
                if(errorCount < verboseMissingInfoCount)
                {
                    FMLLog.log.fatal("Model definition for location {} not found", missing);
                }
                modelErrors.put(domain, errorCount);
            }
            if(model == null)
            {
                modelRegistry.func_82595_a(missing, missingModel);
            }
        }
        for(Entry<String, Integer> e : modelErrors.entrySet())
        {
            if(e.getValue() >= verboseMissingInfoCount)
            {
                FMLLog.log.fatal("Suppressed additional {} model loading errors for domain {}", e.getValue() - verboseMissingInfoCount, e.getKey());
            }
        }
        loadingExceptions.clear();
        missingVariants.clear();
        isLoading = false;
    }

    private static final Map<IRegistryDelegate<Block>, IStateMapper> customStateMappers = Maps.newHashMap();

    /**
     * Adds a custom IBlockState -> model variant logic.
     */
    public static void setCustomStateMapper(Block block, IStateMapper mapper)
    {
        customStateMappers.put(block.delegate, mapper);
    }

    /**
     * Internal, do not use.
     */
    public static void onRegisterAllBlocks(BlockModelShapes shapes)
    {
        for (Entry<IRegistryDelegate<Block>, IStateMapper> e : customStateMappers.entrySet())
        {
            shapes.func_178121_a(e.getKey().get(), e.getValue());
        }
    }

    private static final Map<IRegistryDelegate<Item>, ItemMeshDefinition> customMeshDefinitions = Maps.newHashMap();
    private static final Map<Pair<IRegistryDelegate<Item>, Integer>, ModelResourceLocation> customModels = Maps.newHashMap();

    /**
     * Adds a simple mapping from Item + metadata to the model variant.
     * Registers the variant with the ModelBakery too.
     */
    public static void setCustomModelResourceLocation(Item item, int metadata, ModelResourceLocation model)
    {
        customModels.put(Pair.of(item.delegate, metadata), model);
        ModelBakery.registerItemVariants(item, model);
    }

    /**
     * Adds generic ItemStack -> model variant logic.
     * You still need to manually call ModelBakery.registerItemVariants with all values that meshDefinition can return.
     */
    public static void setCustomMeshDefinition(Item item, ItemMeshDefinition meshDefinition)
    {
        customMeshDefinitions.put(item.delegate, meshDefinition);
    }

    /**
     * Helper method for registering all itemstacks for given item to map to universal bucket model.
     */
    public static void setBucketModelDefinition(Item item) {
        ModelLoader.setCustomMeshDefinition(item, stack -> ModelDynBucket.LOCATION);
        ModelBakery.registerItemVariants(item, ModelDynBucket.LOCATION);
    }

    /**
     * Internal, do not use.
     */
    public static void onRegisterItems(ItemModelMesher mesher)
    {
        for (Entry<IRegistryDelegate<Item>, ItemMeshDefinition> e : customMeshDefinitions.entrySet())
        {
            mesher.func_178080_a(e.getKey().get(), e.getValue());
        }
        for (Entry<Pair<IRegistryDelegate<Item>, Integer>, ModelResourceLocation> e : customModels.entrySet())
        {
            mesher.func_178086_a(e.getKey().getLeft().get(), e.getKey().getRight(), e.getValue());
        }
    }

    private static enum DefaultTextureGetter implements Function<ResourceLocation, TextureAtlasSprite>
    {
        INSTANCE;

        @Override
        public TextureAtlasSprite apply(ResourceLocation location)
        {
            return Minecraft.func_71410_x().func_147117_R().func_110572_b(location.toString());
        }
    }

    /**
     * Get the default texture getter the models will be baked with.
     */
    public static Function<ResourceLocation, TextureAtlasSprite> defaultTextureGetter()
    {
        return DefaultTextureGetter.INSTANCE;
    }

    protected static enum VariantLoader implements ICustomModelLoader
    {
        INSTANCE;

        private ModelLoader loader;

        void setLoader(ModelLoader loader)
        {
            this.loader = loader;
        }

        // NOOP, handled in loader
        @Override
        public void func_110549_a(IResourceManager resourceManager) {}

        @Override
        public boolean accepts(ResourceLocation modelLocation)
        {
            return modelLocation instanceof ModelResourceLocation;
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws Exception
        {
            ModelResourceLocation variant = (ModelResourceLocation) modelLocation;
            ModelBlockDefinition definition = loader.func_177586_a(variant);

            try
            {
                VariantList variants = definition.func_188004_c(variant.func_177518_c());
                return new WeightedRandomModel(variant, variants);
            }
            catch (MissingVariantException e)
            {
                if (definition.equals(loader.multipartDefinitions.get(variant)))
                {
                    IModel model = loader.multipartModels.get(definition);
                    if (model == null)
                    {
                        model = new MultipartModel(new ResourceLocation(variant.func_110624_b(), variant.func_110623_a()), definition.func_188001_c());
                        loader.multipartModels.put(definition, model);
                    }
                    return model;
                }
                throw e;
            }
        }

        @Override
        public String toString()
        {
            return "VariantLoader.INSTANCE";
        }
    }

    private static class MultipartModel implements IModel
    {
        private final ResourceLocation location;
        private final Multipart multipart;
        private final ImmutableMap<Selector, IModel> partModels;

        public MultipartModel(ResourceLocation location, Multipart multipart) throws Exception
        {
            this.location = location;
            this.multipart = multipart;
            ImmutableMap.Builder<Selector, IModel> builder = ImmutableMap.builder();
            for (Selector selector : multipart.func_188136_a())
            {
                builder.put(selector, new WeightedRandomModel(location, selector.func_188165_a()));
            }
            partModels = builder.build();
        }

        private MultipartModel(ResourceLocation location, Multipart multipart, ImmutableMap<Selector, IModel> partModels)
        {
            this.location = location;
            this.multipart = multipart;
            this.partModels = partModels;
        }

        // FIXME: represent selectors as dependencies?
        // FIXME
        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
        {
            MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();

            for (Selector selector : multipart.func_188136_a())
            {
                builder.func_188648_a(selector.func_188166_a(multipart.func_188135_c()), partModels.get(selector).bake(partModels.get(selector).getDefaultState(), format, bakedTextureGetter));
            }

            IBakedModel bakedModel = builder.func_188647_a();
            return bakedModel;
        }

        @Override
        public IModel retexture(ImmutableMap<String, String> textures)
        {
            if (textures.isEmpty())
                return this;

            ImmutableMap.Builder<Selector, IModel> builder = ImmutableMap.builder();
            for (Entry<Selector, IModel> partModel : this.partModels.entrySet())
            {
                builder.put(partModel.getKey(), partModel.getValue().retexture(textures));
            }

            return new MultipartModel(location, multipart, builder.build());
        }
    }
}
