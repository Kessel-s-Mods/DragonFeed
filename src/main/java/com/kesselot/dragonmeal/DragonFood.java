package com.kesselot.dragonmeal;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class DragonFood extends Item {

	public int amount;

	/**
	 * Create a new dragon food!
	 * 
	 * @param growth_amount The amount of ticks the dragon grows by
	 * @param p_41383_      The properties
	 */
	public DragonFood(int growth_amount, Properties p_41383_) {
		super(p_41383_);
		// TODO Auto-generated constructor stub
		this.amount = growth_amount;
	}

	/**
	 * The amount of growing power it has in ticks grown.
	 * 
	 * @return
	 */
	public int getGrowthAmount() {
		return this.amount;
	}

	/**
	 * Checks if the given mob is a baby and is assignable to one of the accepted
	 * dragon types. This method can be overridden by subclasses to change the
	 * accepted types.
	 * 
	 * @param mob the mob to check
	 * @param player the player attempting to feed the dragon
	 * @return true if it's a valid baby dragon target
	 */
	public boolean acceptableTarget(AgeableMob mob, Player player) {


	    Class<?> actualClass = mob.getClass();

	    // Accepted classes. I did it like this because I always have issues with modrinthmaven
	    String[] acceptedClassNames = {
	        "com.GACMD.isleofberk.entity.dragons.deadlynadder.DeadlyNadder",
	        "com.GACMD.isleofberk.entity.dragons.gronckle.Gronckle",
	        "com.GACMD.isleofberk.entity.dragons.lightfury.LightFury",
	        "com.GACMD.isleofberk.entity.dragons.montrous_nightmare.MonstrousNightmare",
	        "com.GACMD.isleofberk.entity.dragons.nightfury.NightFury",
	        "com.GACMD.isleofberk.entity.dragons.nightlight.NightLight",
	        "com.GACMD.isleofberk.entity.dragons.skrill.Skrill",
	        "com.GACMD.isleofberk.entity.dragons.speedstinger.SpeedStinger",
	        "com.GACMD.isleofberk.entity.dragons.stinger.Stinger",
	        "com.GACMD.isleofberk.entity.dragons.terrible_terror.TerribleTerror",
	        "com.GACMD.isleofberk.entity.dragons.triple_stryke.TripleStryke",
	        "com.GACMD.isleofberk.entity.dragons.zippleback.ZippleBack"
	    };

	    for (String className : acceptedClassNames) {
	        try {
	            Class<?> acceptedClass = Class.forName(className);
	            if (acceptedClass.isAssignableFrom(actualClass)) {
	        	    // Check if it's a baby (age < 0)
	        	    if (mob.getAge() >= 0) {
	        	        player.sendMessage(new TranslatableComponent("dragon_meal.error.too_old"),Util.NIL_UUID);
	        	        return false;
	        	    }
	            	
	                return true;
	            }
	        } catch (ClassNotFoundException e) {
	            // Mod not loaded
	            continue;
	        }
	    }

	    player.sendMessage(new TranslatableComponent("dragon_meal.error.unsupported_dragon"),Util.NIL_UUID);
	    return false;
	}

}
