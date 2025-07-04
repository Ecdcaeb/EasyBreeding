package mods.Hileb.easy_breeding;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class EasyBreeding {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Config(modid = Tags.MOD_ID)
    public static class EasyBreedingConfig {
        @Config.Comment("registerName(string)=search_distance(double)  -1 means never, 8 is default")
        public static Map<String, Double> distances = new HashMap<>();

        @Config.Comment("do feed the children?")
        public static boolean feedChild = true;

        public static double getDistance(EntityAnimal entityAnimal) {
            String key = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entityAnimal.getClass()).getRegistryName().toString();
            return distances.getOrDefault(key, 8d);
        }
    }

    @SubscribeEvent
    public static void onEntityInit(net.minecraftforge.event.entity.EntityEvent.EntityConstructing evt) {
        if (!evt.getEntity().world.isRemote && evt.getEntity() instanceof EntityAnimal) {
            EntityAnimal entityAnimal = (EntityAnimal) evt.getEntity();
            double searchDistance = EasyBreedingConfig.getDistance(entityAnimal);
            if (searchDistance > 0) {
                entityAnimal.tasks.addTask(2, new EntityAIEatDroppedFood(entityAnimal, searchDistance));
            }
        }
    }

    public static class EntityAIEatDroppedFood extends EntityAIBase {
        protected EntityAnimal animal;
        protected Random rand;
        protected World world;
        protected double searchDistance;

        public EntityAIEatDroppedFood(EntityAnimal ent, double searchDistance) {
            this.animal = ent;
            this.world = ent.world;
            this.rand = ent.world.rand;
            this.searchDistance = searchDistance;
        }

        @Override
        public boolean shouldExecute() {
            return (!this.animal.isInLove()) && (EasyBreeding.EasyBreedingConfig.feedChild || !this.animal.isChild()) && searchDistance > 0;
        }

        @Override
        public void updateTask()
        {
            EntityItem entityItem = whatFoodIsNear();
            if (entityItem != null && this.animal.isBreedingItem(entityItem.getItem())) {
                execute(this.animal, closeFood);
            }
        }

        protected EntityItem whatFoodIsNear() {
            List<EntityItem> items = getItems();
            if (items.isEmpty()) return null;
            else {
                items.sort((p_compare_1_, p_compare_2_) ->
                {
                    double d0 = this.animal.getDistanceSq(p_compare_1_);
                    double d1 = this.animal.getDistanceSq(p_compare_2_);

                    if (d0 < d1) {
                        return -1;
                    } else {
                        return d0 > d1 ? 1 : 0;
                    }
                });
                return items.get(0);
            }
        }

        // Gets all item entity's within one block of the animals pos, can be changed adds the to a list
        protected List<EntityItem> getItems() {
            return world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(animal.posX - searchDistance, animal.posY - searchDistance, animal.posZ - searchDistance,
                    animal.posX + searchDistance, animal.posY + searchDistance, animal.posZ + searchDistance));
        }
        
        public static boolean execute(EntityAnimal enta, EntityItem enti) {
            if (enta.getNavigator().tryMoveToXYZ(enti.posX, enti.posY, enti.posZ, 1.25F)) {
                if (enta.getDistance(enti) < 1.0F) {
                    eatOne(enti);
                    if (enta.isChild()) {
                        enta.ageUp(-enta.getGrowingAge()/200, true);
                        return true;
                    } else {
                        enta.setInLove(null);
                        return true;
                    }
                }
            }
            return false;
        }

        public static void eatOne(EntityItem enti) {
            ItemStack stack = enti.getItem();
            stack.setCount(stack.getCount() - 1);
            if (stack.getCount() == 0)
                enti.setDead();
        }
    }

}