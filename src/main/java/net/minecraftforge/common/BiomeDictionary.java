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

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.*;
import static net.minecraftforge.common.BiomeDictionary.Type.*;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class BiomeDictionary
{
    private static final boolean DEBUG = false;

    public static final class Type
    {

        private static final Map<String, Type> byName = new HashMap<String, Type>();
        private static Collection<Type> allTypes = Collections.unmodifiableCollection(byName.values());

        /*Temperature-based tags. Specifying neither implies a biome is temperate*/
        public static final Type HOT = new Type("HOT");
        public static final Type COLD = new Type("COLD");

        /*Tags specifying the amount of vegetation a biome has. Specifying neither implies a biome to have moderate amounts*/
        public static final Type SPARSE = new Type("SPARSE");
        public static final Type DENSE = new Type("DENSE");

        /*Tags specifying how moist a biome is. Specifying neither implies the biome as having moderate humidity*/
        public static final Type WET = new Type("WET");
        public static final Type DRY = new Type("DRY");

        /*Tree-based tags, SAVANNA refers to dry, desert-like trees (Such as Acacia), CONIFEROUS refers to snowy trees (Such as Spruce) and JUNGLE refers to jungle trees.
         * Specifying no tag implies a biome has temperate trees (Such as Oak)*/
        public static final Type SAVANNA = new Type("SAVANNA");
        public static final Type CONIFEROUS = new Type("CONIFEROUS");
        public static final Type JUNGLE = new Type("JUNGLE");

        /*Tags specifying the nature of a biome*/
        public static final Type SPOOKY = new Type("SPOOKY");
        public static final Type DEAD = new Type("DEAD");
        public static final Type LUSH = new Type("LUSH");
        public static final Type NETHER = new Type("NETHER");
        public static final Type END = new Type("END");
        public static final Type MUSHROOM = new Type("MUSHROOM");
        public static final Type MAGICAL = new Type("MAGICAL");
        public static final Type RARE = new Type("RARE");

        public static final Type OCEAN = new Type("OCEAN");
        public static final Type RIVER = new Type("RIVER");
        /**
         * A general tag for all water-based biomes. Shown as present if OCEAN or RIVER are.
         **/
        public static final Type WATER = new Type("WATER", OCEAN, RIVER);

        /*Generic types which a biome can be*/
        public static final Type MESA = new Type("MESA");
        public static final Type FOREST = new Type("FOREST");
        public static final Type PLAINS = new Type("PLAINS");
        public static final Type MOUNTAIN = new Type("MOUNTAIN");
        public static final Type HILLS = new Type("HILLS");
        public static final Type SWAMP = new Type("SWAMP");
        public static final Type SANDY = new Type("SANDY");
        public static final Type SNOWY = new Type("SNOWY");
        public static final Type WASTELAND = new Type("WASTELAND");
        public static final Type BEACH = new Type("BEACH");
        public static final Type VOID = new Type("VOID");

        private final String name;
        private final List<Type> subTypes;
        private final Set<Biome> biomes = new HashSet<Biome>();
        private final Set<Biome> biomesUn = Collections.unmodifiableSet(biomes);

        private Type(String name, Type... subTypes)
        {
            this.name = name;
            this.subTypes = ImmutableList.copyOf(subTypes);

            byName.put(name, this);
        }

        private boolean hasSubTypes()
        {
            return !subTypes.isEmpty();
        }

        /**
         * Gets the name for this type.
         */
        public String getName()
        {
            return name;
        }

        public String toString()
        {
            return name;
        }

        /**
         * Retrieves a Type instance by name,
         * if one does not exist already it creates one.
         * This can be used as intermediate measure for modders to
         * add their own Biome types.
         * <p>
         * There are <i>no</i> naming conventions besides:
         * <ul><li><b>Must</b> be all upper case (enforced by name.toUpper())</li>
         * <li><b>No</b> Special characters. {Unenforced, just don't be a pain, if it becomes a issue I WILL
         * make this RTE with no worry about backwards compatibility}</li></ul>
         * <p>
         * Note: For performance sake, the return value of this function SHOULD be cached.
         * Two calls with the same name SHOULD return the same value.
         *
         * @param name The name of this Type
         * @return An instance of Type for this name.
         */
        public static Type getType(String name, Type... subTypes)
        {
            name = name.toUpperCase();
            Type t = byName.get(name);
            if (t == null)
            {
                t = new Type(name, subTypes);
            }
            return t;
        }
        
        /**
         * @return An unmodifiable collection of all current biome types.
         */
        public static Collection<Type> getAll()
        {
            return allTypes;
        }
    }

    private static final Map<ResourceLocation, BiomeInfo> biomeInfoMap = new HashMap<ResourceLocation, BiomeInfo>();

    private static class BiomeInfo
    {

        private final Set<Type> types = new HashSet<Type>();
        private final Set<Type> typesUn = Collections.unmodifiableSet(this.types);

    }

    static
    {
        registerVanillaBiomes();
    }

    /**
     * Adds the given types to the biome.
     *
     */
    public static void addTypes(Biome biome, Type... types)
    {
        Preconditions.checkArgument(ForgeRegistries.BIOMES.containsValue(biome), "Cannot add types to unregistered biome %s", biome);

        Collection<Type> supertypes = listSupertypes(types);
        Collections.addAll(supertypes, types);

        for (Type type : supertypes)
        {
            type.biomes.add(biome);
        }

        BiomeInfo biomeInfo = getBiomeInfo(biome);
        Collections.addAll(biomeInfo.types, types);
        biomeInfo.types.addAll(supertypes);
    }

    /**
     * Gets the set of biomes that have the given type.
     *
     */
    @Nonnull
    public static Set<Biome> getBiomes(Type type)
    {
        return type.biomesUn;
    }

    /**
     * Gets the set of types that have been added to the given biome.
     *
     */
    @Nonnull
    public static Set<Type> getTypes(Biome biome)
    {
        ensureHasTypes(biome);
        return getBiomeInfo(biome).typesUn;
    }

    /**
     * Checks if the two given biomes have types in common.
     *
     * @return returns true if a common type is found, false otherwise
     */
    public static boolean areSimilar(Biome biomeA, Biome biomeB)
    {
        for (Type type : getTypes(biomeA))
        {
            if (getTypes(biomeB).contains(type))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given type has been added to the given biome.
     *
     */
    public static boolean hasType(Biome biome, Type type)
    {
        return getTypes(biome).contains(type);
    }

    /**
     * Checks if any type has been added to the given biome.
     *
     */
    public static boolean hasAnyType(Biome biome)
    {
        return !getBiomeInfo(biome).types.isEmpty();
    }

    /**
     * Automatically adds appropriate types to a given biome based on certain heuristics.
     * If a biome's types are requested and no types have been added to the biome so far, the biome's types
     * will be determined and added using this method.
     *
     */
    public static void makeBestGuess(Biome biome)
    {
        if (biome.field_76760_I.field_76832_z >= 3)
        {
            if (biome.func_76736_e() && biome.func_185353_n() >= 0.9F)
            {
                BiomeDictionary.addTypes(biome, JUNGLE);
            }
            else if (!biome.func_76736_e())
            {
                BiomeDictionary.addTypes(biome, FOREST);

                if (biome.func_185353_n() <= 0.2f)
                {
                    BiomeDictionary.addTypes(biome, CONIFEROUS);
                }
            }
        }
        else if (biome.func_185360_m() <= 0.3F && biome.func_185360_m() >= 0.0F)
        {
            if (!biome.func_76736_e() || biome.func_185355_j() >= 0.0F)
            {
                BiomeDictionary.addTypes(biome, PLAINS);
            }
        }

        if (biome.func_76727_i() > 0.85f)
        {
            BiomeDictionary.addTypes(biome, WET);
        }

        if (biome.func_76727_i() < 0.15f)
        {
            BiomeDictionary.addTypes(biome, DRY);
        }

        if (biome.func_185353_n() > 0.85f)
        {
            BiomeDictionary.addTypes(biome, HOT);
        }

        if (biome.func_185353_n() < 0.15f)
        {
            BiomeDictionary.addTypes(biome, COLD);
        }

        if (biome.field_76760_I.field_76832_z > 0 && biome.field_76760_I.field_76832_z < 3)
        {
            BiomeDictionary.addTypes(biome, SPARSE);
        }
        else if (biome.field_76760_I.field_76832_z >= 10)
        {
            BiomeDictionary.addTypes(biome, DENSE);
        }

        if (biome.func_76736_e() && biome.func_185355_j() < 0.0F && (biome.func_185360_m() <= 0.3F && biome.func_185360_m() >= 0.0F))
        {
            BiomeDictionary.addTypes(biome, SWAMP);
        }

        if (biome.func_185355_j() <= -0.5F)
        {
            if (biome.func_185360_m() == 0.0F)
            {
                BiomeDictionary.addTypes(biome, RIVER);
            }
            else
            {
                BiomeDictionary.addTypes(biome, OCEAN);
            }
        }

        if (biome.func_185360_m() >= 0.4F && biome.func_185360_m() < 1.5F)
        {
            BiomeDictionary.addTypes(biome, HILLS);
        }

        if (biome.func_185360_m() >= 1.5F)
        {
            BiomeDictionary.addTypes(biome, MOUNTAIN);
        }

        if (biome.func_76746_c())
        {
            BiomeDictionary.addTypes(biome, SNOWY);
        }

        if (biome.field_76752_A.func_177230_c() != Blocks.field_150354_m && biome.func_185353_n() >= 1.0f && biome.func_76727_i() < 0.2f)
        {
            BiomeDictionary.addTypes(biome, SAVANNA);
        }

        if (biome.field_76752_A.func_177230_c() == Blocks.field_150354_m)
        {
            BiomeDictionary.addTypes(biome, SANDY);
        }
        else if (biome.field_76752_A.func_177230_c() == Blocks.field_150391_bh)
        {
            BiomeDictionary.addTypes(biome, MUSHROOM);
        }
        if (biome.field_76753_B.func_177230_c() == Blocks.field_150405_ch)
        {
            BiomeDictionary.addTypes(biome, MESA);
        }
    }

    //Internal implementation
    private static BiomeInfo getBiomeInfo(Biome biome)
    {
        return biomeInfoMap.computeIfAbsent(biome.getRegistryName(), k -> new BiomeInfo());
    }

    /**
     * Ensure that at least one type has been added to the given biome.
     */
    static void ensureHasTypes(Biome biome)
    {
        if (!hasAnyType(biome))
        {
            makeBestGuess(biome);
            FMLLog.log.warn("No types have been added to Biome {}, types have been assigned on a best-effort guess: {}", biome.getRegistryName(), getTypes(biome));
        }
    }

    private static Collection<Type> listSupertypes(Type... types)
    {
        Set<Type> supertypes = new HashSet<Type>();
        Deque<Type> next = new ArrayDeque<Type>();
        Collections.addAll(next, types);

        while (!next.isEmpty())
        {
            Type type = next.remove();

            for (Type sType : Type.byName.values())
            {
                if (sType.subTypes.contains(type) && supertypes.add(sType))
                    next.add(sType);
            }
        }

        return supertypes;
    }

    private static void registerVanillaBiomes()
    {
        addTypes(Biomes.field_76771_b,                            OCEAN                                                   );
        addTypes(Biomes.field_76772_c,                           PLAINS                                                  );
        addTypes(Biomes.field_76769_d,                           HOT,      DRY,        SANDY                             );
        addTypes(Biomes.field_76770_e,                    MOUNTAIN, HILLS                                         );
        addTypes(Biomes.field_76767_f,                           FOREST                                                  );
        addTypes(Biomes.field_76768_g,                            COLD,     CONIFEROUS, FOREST                            );
        addTypes(Biomes.field_76780_h,                        WET,      SWAMP                                         );
        addTypes(Biomes.field_76781_i,                            RIVER                                                   );
        addTypes(Biomes.field_76778_j,                             HOT,      DRY,        NETHER                            );
        addTypes(Biomes.field_76779_k,                              COLD,     DRY,        END                               );
        addTypes(Biomes.field_76776_l,                     COLD,     OCEAN,      SNOWY                             );
        addTypes(Biomes.field_76777_m,                     COLD,     RIVER,      SNOWY                             );
        addTypes(Biomes.field_76774_n,                       COLD,     SNOWY,      WASTELAND                         );
        addTypes(Biomes.field_76775_o,                    COLD,     SNOWY,      MOUNTAIN                          );
        addTypes(Biomes.field_76789_p,                  MUSHROOM, RARE                                          );
        addTypes(Biomes.field_76788_q,            MUSHROOM, BEACH,      RARE                              );
        addTypes(Biomes.field_76787_r,                            BEACH                                                   );
        addTypes(Biomes.field_76786_s,                     HOT,      DRY,        SANDY,    HILLS                   );
        addTypes(Biomes.field_76785_t,                     FOREST,   HILLS                                         );
        addTypes(Biomes.field_76784_u,                      COLD,     CONIFEROUS, FOREST,   HILLS                   );
        addTypes(Biomes.field_76783_v,               MOUNTAIN                                                );
        addTypes(Biomes.field_76782_w,                           HOT,      WET,        DENSE,    JUNGLE                  );
        addTypes(Biomes.field_76792_x,                     HOT,      WET,        DENSE,    JUNGLE,   HILLS         );
        addTypes(Biomes.field_150574_L,                      HOT,      WET,        JUNGLE,   FOREST,   RARE          );
        addTypes(Biomes.field_150575_M,                       OCEAN                                                   );
        addTypes(Biomes.field_150576_N,                      BEACH                                                   );
        addTypes(Biomes.field_150577_O,                       COLD,     BEACH,      SNOWY                             );
        addTypes(Biomes.field_150583_P,                     FOREST                                                  );
        addTypes(Biomes.field_150582_Q,               FOREST,   HILLS                                         );
        addTypes(Biomes.field_150585_R,                    SPOOKY,   DENSE,      FOREST                            );
        addTypes(Biomes.field_150584_S,                       COLD,     CONIFEROUS, FOREST,   SNOWY                   );
        addTypes(Biomes.field_150579_T,                 COLD,     CONIFEROUS, FOREST,   SNOWY,    HILLS         );
        addTypes(Biomes.field_150578_U,                    COLD,     CONIFEROUS, FOREST                            );
        addTypes(Biomes.field_150581_V,              COLD,     CONIFEROUS, FOREST,   HILLS                   );
        addTypes(Biomes.field_150580_W,         MOUNTAIN, FOREST,     SPARSE                            );
        addTypes(Biomes.field_150588_X,                          HOT,      SAVANNA,    PLAINS,   SPARSE                  );
        addTypes(Biomes.field_150587_Y,                  HOT,      SAVANNA,    PLAINS,   SPARSE,   RARE          );
        addTypes(Biomes.field_150589_Z,                             MESA,     SANDY,      DRY                               );
        addTypes(Biomes.field_150607_aa,                        MESA,     SANDY,      DRY,      SPARSE                  );
        addTypes(Biomes.field_150608_ab,                  MESA,     SANDY,      DRY                               );
        addTypes(Biomes.field_185440_P,                             VOID                                                    );
        addTypes(Biomes.field_185441_Q,                   PLAINS,   RARE                                          );
        addTypes(Biomes.field_185442_R,                   HOT,      DRY,        SANDY,    RARE                    );
        addTypes(Biomes.field_185443_S,            MOUNTAIN, SPARSE,     RARE                              );
        addTypes(Biomes.field_185444_T,                   FOREST,   HILLS,      RARE                              );
        addTypes(Biomes.field_150590_f,                    COLD,     CONIFEROUS, FOREST,   MOUNTAIN, RARE          );
        addTypes(Biomes.field_150599_m,                WET,      SWAMP,      HILLS,    RARE                    );
        addTypes(Biomes.field_185445_W,                COLD,     SNOWY,      HILLS,    RARE                    );
        addTypes(Biomes.field_185446_X,                   HOT,      WET,        DENSE,    JUNGLE,   MOUNTAIN, RARE);
        addTypes(Biomes.field_185447_Y,              HOT,      SPARSE,     JUNGLE,   HILLS,    RARE          );
        addTypes(Biomes.field_185448_Z,             FOREST,   DENSE,      HILLS,    RARE                    );
        addTypes(Biomes.field_185429_aa,       FOREST,   DENSE,      MOUNTAIN, RARE                    );
        addTypes(Biomes.field_185430_ab,            SPOOKY,   DENSE,      FOREST,   MOUNTAIN, RARE          );
        addTypes(Biomes.field_185431_ac,               COLD,     CONIFEROUS, FOREST,   SNOWY,    MOUNTAIN, RARE);
        addTypes(Biomes.field_185432_ad,            DENSE,    FOREST,     RARE                              );
        addTypes(Biomes.field_185433_ae,      DENSE,    FOREST,     HILLS,    RARE                    );
        addTypes(Biomes.field_185434_af, MOUNTAIN, SPARSE,     RARE                              );
        addTypes(Biomes.field_185435_ag,                  HOT,      DRY,        SPARSE,   SAVANNA,  MOUNTAIN, RARE);
        addTypes(Biomes.field_185436_ah,             HOT,      DRY,        SPARSE,   SAVANNA,  HILLS,    RARE);
        addTypes(Biomes.field_185437_ai,                     HOT,      DRY,        SPARSE,   MOUNTAIN, RARE          );
        addTypes(Biomes.field_185438_aj,                HOT,      DRY,        SPARSE,   HILLS,    RARE          );
        addTypes(Biomes.field_185439_ak,          HOT,      DRY,        SPARSE,   MOUNTAIN, RARE          );


        if (DEBUG)
        {
            FMLLog.log.debug("BiomeDictionary:");
            for (Type type : Type.byName.values())
            {
                StringBuilder buf = new StringBuilder();
                buf.append("    ").append(type.name).append(": ").append(type.biomes.stream().map(Biome::func_185359_l).collect(Collectors.joining(", ")));
                FMLLog.log.debug(buf.toString());
            }
        }
    }
}
