package com.mrcrayfish.enchantable;

import com.mrcrayfish.enchantable.core.ModEnchantments;
import com.mrcrayfish.enchantable.event.EditBlockEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class Enchantable
{
    public static final EnchantmentType HOE = EnchantmentType.create(Reference.MOD_ID + ":hoe", item -> item.getItem() instanceof HoeItem);
    public static final EnchantmentType TILLABLE = EnchantmentType.create(Reference.MOD_ID + ":tillable", item -> item.getItem() instanceof HoeItem || item.getItem() instanceof ShovelItem);
    public static final EnchantmentType PICKAXE = EnchantmentType.create(Reference.MOD_ID + ":pickaxe", item -> item.getItem() instanceof PickaxeItem);

    public Enchantable()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.REGISTER.register(bus);
        MinecraftForge.EVENT_BUS.register(this);

        /* Patches tools group to include new enchantment types */
        this.addEnchantmentTypesToGroup(ItemGroup.TOOLS, HOE, TILLABLE, PICKAXE);
    }

    private void addEnchantmentTypesToGroup(ItemGroup group, EnchantmentType ... types)
    {
        EnchantmentType[] oldTypes = group.getRelevantEnchantmentTypes();
        EnchantmentType[] newTypes = new EnchantmentType[oldTypes.length + types.length];
        System.arraycopy(oldTypes, 0, newTypes, 0, oldTypes.length);
        System.arraycopy(types, 0, newTypes, oldTypes.length, types.length);
        group.setRelevantEnchantmentTypes(newTypes);
    }

    @SubscribeEvent
    public void canPlayerEditBlock(EditBlockEvent event)
    {
        BlockState state = event.getWorld().getBlockState(event.getPos());
        if(state.getBlock() instanceof CropsBlock)
        {
            ItemStack heldItem = event.getPlayer().getHeldItemMainhand();
            if(heldItem.isEmpty())
                return;

            if(!EnchantmentHelper.getEnchantments(heldItem).containsKey(ModEnchantments.SEEDER.get()) && !EnchantmentHelper.getEnchantments(heldItem).containsKey(ModEnchantments.CULTIVATOR.get()))
                return;

            CropsBlock crop = (CropsBlock) state.getBlock();
            if(state.get(crop.getAgeProperty()) != crop.getMaxAge())
            {
                event.setCanceled(true);
            }
        }
    }

    public static boolean fireEditBlockEvent(PlayerEntity player, World world, BlockPos pos)
    {
        return MinecraftForge.EVENT_BUS.post(new EditBlockEvent(player, world, pos));
    }
}
