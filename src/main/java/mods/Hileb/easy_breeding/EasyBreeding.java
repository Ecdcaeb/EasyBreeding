package mods.Hileb.easy_breeding;

import mods.Hileb.easy_breeding.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import net.minecraft.entity.passive.EntityAnimal;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class EasyBreeding {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Config(modid = Tags.MOD_ID)
    public static class EasyBreedingConfig {
        @Config.Comment("registerName(string)=search_distance(double)  -1 means never, 8 is default")
        public static Map<String, Double> customs = new HashMap<>();

        public static double getDistance(EntityAnimal entityAnimal) {
            String key = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entityAnimal.getClass()).getRegistryName().toString();
            if (customs.containsKey(key)) {
                return customs.get(key);
            } else return 8d;
        }
    }

}
