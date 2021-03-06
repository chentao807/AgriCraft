package com.InfinityRaider.AgriCraft.compatibility.botania;

import com.InfinityRaider.AgriCraft.api.v1.RenderMethod;
import com.InfinityRaider.AgriCraft.blocks.BlockModPlant;
import com.InfinityRaider.AgriCraft.compatibility.ModHelper;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.items.ItemModSeed;
import com.InfinityRaider.AgriCraft.reference.Data;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class BotaniaHelper extends ModHelper {
    public static ArrayList<BlockModPlant> botaniaCrops = new ArrayList<BlockModPlant>();
    public static ArrayList<ItemModSeed> botaniaSeeds = new ArrayList<ItemModSeed>();

    @Override
    protected void init() {

    }

    @Override
    protected void initPlants() {
        if(ConfigurationHandler.integration_Botania) {
            for(int i=0;i<16;i++) {
                Object[] args = {Data.botania[i], new ItemStack((Item) Item.itemRegistry.getObject("Botania:petal"), 1, i), 3, RenderMethod.CROSSED, new ItemStack((Block) Block.blockRegistry.getObject("Botania:flower"), 1, i)};
                BlockModPlant plant;
                try {
                    plant = new BlockModPlant(args);
                } catch (Exception e) {
                    if(ConfigurationHandler.debug) {
                        e.printStackTrace();
                    }
                    return;
                }
                botaniaCrops.add(plant);
                botaniaSeeds.add(plant.getSeed());
            }
            LogHelper.info("Botania crops registered");
        }
    }

    @Override
    protected String modId() {
        return "Botania";
    }
}
