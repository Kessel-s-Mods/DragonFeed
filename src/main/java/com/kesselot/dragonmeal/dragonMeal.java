package com.kesselot.dragonmeal;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
		public void growDragon(PlayerInteractEvent.EntityInteractSpecific event) {
			if (event.getWorld().isClientSide())
				return;

			Player player=event.getPlayer();
			
			
			Item usedItem = event.getItemStack().getItem();
			if (!(usedItem instanceof DragonFood food))
				return;
			if (!(event.getTarget() instanceof AgeableMob ageableMob))
				return;
			if (!food.acceptableTarget(ageableMob,player))
				return;

			ageableMob.ageUp(food.getGrowthAmount());

			if (!event.getPlayer().getAbilities().instabuild) {
				event.getItemStack().shrink(1);
			}
			event.setCanceled(true);
		}
	}

}