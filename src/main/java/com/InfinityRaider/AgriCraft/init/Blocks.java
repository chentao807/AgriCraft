package com.InfinityRaider.AgriCraft.init;

import com.InfinityRaider.AgriCraft.blocks.*;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.items.ItemBlockCustomWood;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import com.InfinityRaider.AgriCraft.utility.RegisterHelper;

public class Blocks {
    public static BlockCrop blockCrop;
    public static BlockSeedAnalyzer seedAnalyzer;
    public static BlockWaterPad waterPad;
    public static BlockWaterTank blockWaterTank;
    public static BlockWaterChannel blockWaterChannel;
    public static BlockWaterChannelFull blockWaterChannelFull;
    public static BlockChannelValve blockChannelValve;
    public static BlockSprinkler blockSprinkler;
    public static BlockSeedStorage blockSeedStorage;
    public static BlockSeedStorageController blockSeedStorageController;

    public static void init() {
        blockCrop = new BlockCrop();
        RegisterHelper.registerBlock(blockCrop, Names.Objects.crops);
        seedAnalyzer = new BlockSeedAnalyzer();
        RegisterHelper.registerBlock(seedAnalyzer, Names.Objects.seedAnalyzer);
        waterPad = new BlockWaterPad();
        RegisterHelper.registerBlock(waterPad, Names.Objects.waterPad, BlockWaterPad.ItemBlock.class);
        if(!ConfigurationHandler.disableIrrigation) {
            blockWaterTank = new BlockWaterTank();
            RegisterHelper.registerBlock(blockWaterTank, Names.Objects.tank, ItemBlockCustomWood.class);
            blockWaterChannel = new BlockWaterChannel();
            RegisterHelper.registerBlock(blockWaterChannel, Names.Objects.channel, ItemBlockCustomWood.class);
            blockWaterChannelFull = new BlockWaterChannelFull();
            RegisterHelper.registerBlock(blockWaterChannelFull, Names.Objects.channelFull, ItemBlockCustomWood.class);
            blockChannelValve = new BlockChannelValve();
            RegisterHelper.registerBlock(blockChannelValve, Names.Objects.valve, ItemBlockCustomWood.class);
            blockSprinkler = new BlockSprinkler();
            RegisterHelper.registerBlock(blockSprinkler, Names.Objects.sprinkler);
        }
        if(!ConfigurationHandler.disableSeedStorage) {
            blockSeedStorage = new BlockSeedStorage();
            RegisterHelper.registerBlock(blockSeedStorage, Names.Objects.seedStorage, ItemBlockCustomWood.class);
            if(!ConfigurationHandler.disableSeedWarehouse) {
                blockSeedStorageController = new BlockSeedStorageController();
                RegisterHelper.registerBlock(blockSeedStorageController, Names.Objects.seedStorageController, ItemBlockCustomWood.class);
            }
        }
        LogHelper.debug("Blocks registered");
    }
}
