package com.InfinityRaider.AgriCraft.handler;

import com.InfinityRaider.AgriCraft.api.v1.ICropPlant;
import com.InfinityRaider.AgriCraft.apiimpl.v1.cropplant.CropPlantAPI;
import com.InfinityRaider.AgriCraft.farming.CropPlantHandler;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.item.ItemStack;

public class InterModComsHandler {
    @Mod.EventHandler
    public void receiveMessage(FMLInterModComms.IMCEvent event) {
        for(FMLInterModComms.IMCMessage message:event.getMessages()) {
            if(message.isItemStackMessage()) {
                try {
                    Class cropPlantClass = Class.forName(message.key);
                    if(ICropPlant.class.isAssignableFrom(cropPlantClass)) {
                        ICropPlant cropPlant = null;
                        ItemStack seed = message.getItemStackValue();
                        if(seed==null || seed.getItem()==null) {
                            LogHelper.error("[IMC] CropPlant registering errored: ItemStack does not contain an item");
                            continue;
                        }
                        try {
                            cropPlant = (ICropPlant) cropPlantClass.getConstructor().newInstance(seed);
                        } catch (Exception e) {
                            LogHelper.error("[IMC] CropPlant registering errored: "+message.getStringValue()+" does not have a valid constructor, constructor should be public with ItemStack as parameter");
                        }
                        CropPlantHandler.addCropToRegister(new CropPlantAPI(cropPlant));
                        LogHelper.error("[IMC] Successfully registered CropPlant for "+seed.getUnlocalizedName());
                    } else {
                        LogHelper.error("[IMC] CropPlant registering errored: Class "+cropPlantClass.getName()+" does not implement "+ICropPlant.class.getName());
                    }
                } catch (ClassNotFoundException e) {
                    LogHelper.error("[IMC] CropPlant registering errored: No class found for "+message.key);
                }
            }
        }
    }
}
