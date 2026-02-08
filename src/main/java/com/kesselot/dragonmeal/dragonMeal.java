package com.kesselot.dragonmeal;

import net.minecraft.Util;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(dragonMeal.MODID)
public class dragonMeal {

	public static final String MODID = "dragon_meal";

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	public static final RegistryObject<Item> DRAGON_FEED = ITEMS.register("dragon_feed",
			() -> new DragonFood(120, new Item.Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(64)));
	public static final RegistryObject<Item> DRAGON_FRUIT = ITEMS.register("dragon_fruit",
			() -> new DragonFood(2400, new Item.Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(64)));

	public static final RegistryObject<Item> DRAGON_FERTILITY_CHARM_PARTIAL = ITEMS.register(
			"dragon_fertility_charm_partial",
			() -> new DragonFertilityCharmItem(DragonFertilityCharmItem.CharmType.PARTIAL,
					new Item.Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(64)));

	public static final RegistryObject<Item> DRAGON_FERTILITY_CHARM_FULL = ITEMS.register("dragon_fertility_charm_full",
			() -> new DragonFertilityCharmItem(DragonFertilityCharmItem.CharmType.FULL,
					new Item.Properties().tab(CreativeModeTab.TAB_FOOD).stacksTo(64)));

	public dragonMeal() {
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new DragonInteractionHandler());
	}

	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (event.getName().equals(new ResourceLocation("minecraft", "blocks/wheat"))) {
			LootTable table = event.getTable();

			// Dragon Feed
			LootPool feedPool = LootPool.lootPool().name("dragon_feed_pool").setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(DRAGON_FEED.get()))
					.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).setProperties(
							StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.AGE_7, 7)))
					.when(LootItemRandomChanceCondition.randomChance(0.10F)).build();// 10 per cent chance at age 7

			// Dragon Fruit
			LootPool fruitPool = LootPool.lootPool().name("dragon_fruit_pool").setRolls(ConstantValue.exactly(1))
					.add(LootItem.lootTableItem(DRAGON_FRUIT.get()))
					.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).setProperties(
							StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.AGE_7, 7)))
					.when(LootItemRandomChanceCondition.randomChance(0.01F)).build();// 1 per cent chance at age 7

			table.addPool(feedPool);
			table.addPool(fruitPool);
		}
	}



	private static class DragonInteractionHandler {
	    @SubscribeEvent
	    public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
	        if (event.getWorld().isClientSide()) return;

	        Player player = event.getPlayer();
	        ItemStack stack = event.getItemStack();
	        Item item = stack.getItem();
	        Entity target = event.getTarget();

	        // Handle Dragon Food
	        if (item instanceof DragonFood food) {
	            if (!(target instanceof AgeableMob ageable)) {
	                // Not an ageable mob — silently ignore or handle elsewhere
	                return;
	            }

	            int currentAge = ageable.getAge();
	            // Fail early if already adult or in cooldown
	            if (currentAge >= 0) {
	                player.sendMessage(new TextComponent("This dragon is already fully grown!"), Util.NIL_UUID);
	                return;
	            }

	            if (!food.acceptableTarget(ageable, player)) {
	                // acceptableTarget may send its own message (e.g., unsupported type)
	                // If you want to override that, comment out the above call and handle here
	                return;
	            }

	            int newAge = Math.min(0, currentAge + food.getGrowthAmount());
	            ageable.setAge(newAge);

	            if (newAge == 0) {
	                player.sendMessage(new TextComponent("The dragon has reached adulthood!"), Util.NIL_UUID);
	            }

	            if (!player.getAbilities().instabuild) {
	                stack.shrink(1);
	            }
	            event.setCanceled(true);
	            return;
	        }

	        // Handle Fertility Charms
	        if (item instanceof DragonFertilityCharmItem charm) {
	            Class<?> dragonBaseClass;
	            try {
	                dragonBaseClass = Class.forName("com.GACMD.isleofberk.entity.base.dragon.ADragonBase");
	            } catch (ClassNotFoundException ignored) {
	                return;
	            }

	            if (!dragonBaseClass.isAssignableFrom(target.getClass())) {
	                return;
	            }

	            if (!(target instanceof AgeableMob ageable)) {
	                return;
	            }

	            int currentAge = ageable.getAge();
	            if (currentAge <= 0) {
	                player.sendMessage(new TextComponent("This dragon is already ready to breed!"), Util.NIL_UUID);
	                return;
	            }

	            int newAge;
	            if (charm.type == DragonFertilityCharmItem.CharmType.FULL) {
	                newAge = 0;
	            } else {
	                newAge = Math.max(0, currentAge - charm.type.reductionTicks);
	            }

	            ageable.setAge(newAge);

	            if (newAge == 0) {
	                player.sendMessage(new TextComponent("The dragon is ready to breed again!"), Util.NIL_UUID);
	            }

	            if (!player.getAbilities().instabuild) {
	                stack.shrink(1);
	            }
	            event.setCanceled(true);
	        }
	    }
	}

}