package com.kesselot.dragonmeal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DragonFertilityCharmItem extends Item {

	// Cache the ADragonBase class reflectively null if not present
	private static Class<?> DRAGON_BASE_CLASS = null;

	static {
		try {
			DRAGON_BASE_CLASS = Class.forName("com.GACMD.isleofberk.entity.base.dragon.ADragonBase");
		} catch (ClassNotFoundException ignored) {
			// Not loaded item will simply do nothing
		}
	}

	public DragonFertilityCharmItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
			InteractionHand hand) {
		if (target.level.isClientSide()) {
			return InteractionResult.PASS;
		}

		// Skip if ADragonBase isn't available
		if (DRAGON_BASE_CLASS == null) {
			return InteractionResult.PASS;
		}

		// Check assignability without direct reference
		if (!DRAGON_BASE_CLASS.isAssignableFrom(target.getClass())) {
			return InteractionResult.PASS;
		}

		if (!(target instanceof AgeableMob ageable)) {
			return InteractionResult.PASS;
		}

		int currentAge = ageable.getAge();
		if (currentAge != 0) {
			int newAge = currentAge / 2; // Halve toward zero
			ageable.setAge(newAge);

			stack.shrink(1);

			return InteractionResult.sidedSuccess(true);
		}

		return InteractionResult.PASS;
	}
}