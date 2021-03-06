package com.InfinityRaider.AgriCraft.farming.mutation;

import com.InfinityRaider.AgriCraft.farming.CropPlantHandler;
import com.InfinityRaider.AgriCraft.handler.ConfigurationHandler;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCrop;
import com.InfinityRaider.AgriCraft.utility.IOHelper;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class MutationHandler {

    private static List<Mutation> mutations;

    public static void init() {
        //Read mutations & initialize the mutation arrays
        String[] data = IOHelper.getLinesArrayFromData(ConfigurationHandler.readMutationData());
        List<Mutation> list = new ArrayList<Mutation>();
        for(String line:data) {
            Mutation mutation = readMutation(line);
            if(mutation!=null && !list.contains(mutation)) {
                list.add(mutation);
            }
        }
        mutations = list;

        //print registered mutations to the log
        LogHelper.info("Registered Mutations:");
        for (Mutation mutation:mutations) {
            String result = mutation.result.getItem() != null ? (Item.itemRegistry.getNameForObject(mutation.result.getItem()) + ':' + mutation.result.getItemDamage()) : "null";
            String parent1 = mutation.parent1.getItem() != null ? (Item.itemRegistry.getNameForObject(mutation.parent1.getItem())) + ':' + mutation.parent1.getItemDamage() : "null";
            String parent2 = mutation.parent2.getItem() != null ? (Item.itemRegistry.getNameForObject(mutation.parent2.getItem())) + ':' + mutation.parent2.getItemDamage() : "null";
            String info = " - " + result + " = " + parent1 + " + " + parent2;
            LogHelper.info(info);
        }
    }

    private static Mutation readMutation(String input) {
        Mutation mutation = null;
        String[] data = IOHelper.getData(input);
        boolean success = data.length==1 || data.length==3;
        String errorMsg = "invalid number of arguments";
        if(success) {
            String mutationData = data[0];
            int indexEquals = mutationData.indexOf('=');
            int indexPlus = mutationData.indexOf('+');
            success = (indexEquals>0 && indexPlus>indexEquals);
            errorMsg = "Mutation is not defined correctly";
            if(success) {
                //read the stacks
                ItemStack resultStack = IOHelper.getStack(mutationData.substring(0,indexEquals));
                ItemStack parentStack1 = IOHelper.getStack(mutationData.substring(indexEquals + 1, indexPlus));
                ItemStack parentStack2 = IOHelper.getStack(mutationData.substring(indexPlus+1));
                success = CropPlantHandler.isValidSeed(resultStack);
                errorMsg = "resulting stack is not correct";
                if(success) {
                    success =  CropPlantHandler.isValidSeed(parentStack1);
                    errorMsg = "first parent stack is not correct";
                    if(success) {
                        success =  CropPlantHandler.isValidSeed(parentStack2);
                        errorMsg = "second parent stack is not correct";
                        if(success) {
                            try {
                                mutation = new Mutation(resultStack, parentStack1, parentStack2);
                            } catch (Exception e) {
                                LogHelper.debug("Caught exception when trying to add mutation: "+resultStack.getUnlocalizedName()+"="+parentStack1.getUnlocalizedName()+"+"+parentStack2.getUnlocalizedName()+" this seed is not registered");
                            }
                        }
                    }
                }
            }
        }
        if(!success) {
            LogHelper.info(new StringBuffer("Error when reading mutation: ").append(errorMsg).append(" (line: ").append(input).append(")"));
        }
        return mutation;
    }

    //gets all the possible crossovers
    public static Mutation[] getCrossOvers(List<TileEntityCrop> crops) {
        TileEntityCrop[] parents = MutationHandler.getParents(crops);
        ArrayList<Mutation> list = new ArrayList<Mutation>();
        switch (parents.length) {
            case 2:
                list.addAll(MutationHandler.getMutations(parents[0], parents[1]));
                break;
            case 3:
                list.addAll(MutationHandler.getMutations(parents[0], parents[1]));
                list.addAll(MutationHandler.getMutations(parents[0], parents[2]));
                list.addAll(MutationHandler.getMutations(parents[1], parents[2]));
                break;
            case 4:
                list.addAll(MutationHandler.getMutations(parents[0], parents[1]));
                list.addAll(MutationHandler.getMutations(parents[0], parents[2]));
                list.addAll(MutationHandler.getMutations(parents[0], parents[3]));
                list.addAll(MutationHandler.getMutations(parents[1], parents[2]));
                list.addAll(MutationHandler.getMutations(parents[1], parents[3]));
                list.addAll(MutationHandler.getMutations(parents[2], parents[3]));
                break;
        }
        return cleanMutationArray(list.toArray(new Mutation[list.size()]));
    }

    //gets an array of all the possible parents from the array containing all the neighbouring crops
    private static TileEntityCrop[] getParents(List<TileEntityCrop> input) {
        ArrayList<TileEntityCrop> list = new ArrayList<TileEntityCrop>();
        for(TileEntityCrop crop:input) {
            if (crop != null && crop.isMature()) {
                list.add(crop);
            }
        }
        return list.toArray(new TileEntityCrop[list.size()]);
    }

    //finds the product of two parents
    private static ArrayList<Mutation> getMutations(TileEntityCrop parent1, TileEntityCrop parent2) {
        Item seed1 = parent1.getSeedStack().getItem();
        Item seed2 = parent2.getSeedStack().getItem();
        int meta1 = parent1.getSeedStack().getItemDamage();
        int meta2 = parent2.getSeedStack().getItemDamage();
        ArrayList<Mutation> list = new ArrayList<Mutation>();
        for (Mutation mutation:mutations) {
            if ((seed1==mutation.parent1.getItem() && seed2==mutation.parent2.getItem()) && (meta1==mutation.parent1.getItemDamage() && meta2==mutation.parent2.getItemDamage())) {
                list.add(mutation);
            }
            if ((seed1==mutation.parent2.getItem() && seed2==mutation.parent1.getItem()) && (meta1==mutation.parent2.getItemDamage() && meta2==mutation.parent1.getItemDamage())) {
                list.add(mutation);
            }
        }
        return list;
    }

    //logic for stat inheritance
    public static void setResultStats(CrossOverResult result, List<TileEntityCrop> input, boolean mutation) {
        TileEntityCrop[] parents = getParents(input);
        int size = parents.length;
        int[] growth = new int[size];
        int[] gain = new int[size];
        int[] strength = new int[size];
        for(int i=0;i<size;i++) {
            int multiplier = ConfigurationHandler.spreadingDifficulty;
            if(multiplier>1) {
                //multiplier is the difficulty
                //1: this code isn't reached and all surrounding crops affect stat gain positively (multiplier = 1 for incompatible crops)
                //2: only parent/identical seeds can affect stat gain (multiplier = -1 for incompatible crops)
                //3: any neighbouring plant that isn't a parent/same seed affects stat gain negatively (multiplier = 0 for incompatible crops)
                multiplier = canInheritStats(result.getSeed(), result.getMeta(), parents[i].getSeedStack().getItem(), parents[i].getSeedStack().getItemDamage())?1:(multiplier==3?0:-1);
            }
            growth[i] = multiplier * parents[i].getGrowth();
            gain[i] = multiplier*parents[i].getGain();
            strength[i] = multiplier*parents[i].getStrength();
        }
        int meanGrowth = getMean(growth);
        int meanGain = getMean(gain);
        int meanStrength = getMean(strength);
        int divisor = mutation?ConfigurationHandler.cropStatDivisor:1;
        result.setStats(calculateStats(meanGrowth, size, divisor), calculateStats(meanGain, size, divisor), calculateStats(meanStrength, size, divisor));
    }

    /**returns the mean value of an int array, this ignores negative values in the array*/
    private static int getMean(int[] input) {
        int sum = 0;
        int total = input.length;
        int mean = 0;
        if(total>0) {
            for (int nr : input) {
                if(nr>=0) {
                    sum = sum + nr;
                }
                else {
                    total--;
                }
            }
            if(total>0) {
                mean = Math.round(((float) sum) / ((float) total));
            }
        }
        return mean;
    }

    /** calculates the new stats based on an input stat, the nr of neighbours and a divisor*/
    private static int calculateStats(int input, int neighbours, int divisor) {
        if(neighbours == 1 && ConfigurationHandler.singleSpreadsIncrement) {
            neighbours = 2;
        }
        int newStat = Math.max(1, (input + (int) Math.round(Math.abs(neighbours-1)*Math.random()))/divisor);
        return Math.min(newStat, ConfigurationHandler.cropStatCap);
    }

    private static boolean canInheritStats(Item child, int childMeta, Item seed, int seedMeta) {
        boolean b = child==seed && childMeta==seedMeta;
        if(!b) {
            for(Mutation mutation:getParentMutations(child, childMeta)) {
                if(mutation!=null) {
                    if(mutation.parent1.getItem()==seed && mutation.parent1.getItemDamage()==seedMeta) {
                        b = true;
                        break;
                    }
                    else if(mutation.parent2.getItem()==seed && mutation.parent2.getItemDamage()==seedMeta) {
                        b = true;
                        break;
                    }
                }
            }
        }
        return b;
    }

    //removes null instance from a mutations array
    private static Mutation[] cleanMutationArray(Mutation[] input) {
        ArrayList<Mutation> list = new ArrayList<Mutation>();
        for(Mutation mutation:input) {
            if (mutation.result != null) {
                list.add(mutation);
            }
        }
        return list.toArray(new Mutation[list.size()]);
    }


    //public methods to read data from the mutations and parents arrays

    //gets all the mutations
    public static Mutation[] getMutations() {
        return mutations.toArray(new Mutation[mutations.size()]);
    }

    //gets all the mutations this crop can mutate to
    public static Mutation[] getMutations(ItemStack stack) {
        ArrayList<Mutation> list = new ArrayList<Mutation>();
        for (Mutation mutation : mutations) {
            if (mutation.parent2.getItem() == stack.getItem() && mutation.parent2.getItemDamage() == stack.getItemDamage()) {
                list.add(new Mutation(mutation));
            }
            if (!(mutation.parent2.getItem() == mutation.parent1.getItem() && mutation.parent2.getItemDamage() == mutation.parent1.getItemDamage()) && (mutation.parent1.getItem() == stack.getItem() && mutation.parent1.getItemDamage() == stack.getItemDamage())) {
                list.add(new Mutation(mutation));
            }
        }

        return list.toArray(new Mutation[list.size()]);
    }

    public static Mutation[] getParentMutations(Item seed, int meta) {
        return getParentMutations(new ItemStack(seed, 1, meta));
    }

    //gets the parents this crop mutates from
    public static Mutation[] getParentMutations(ItemStack stack) {
        ArrayList<Mutation> list = new ArrayList<Mutation>();
        if(CropPlantHandler.isValidSeed(stack)) {
            for (Mutation mutation:mutations) {
                if (mutation.result.getItem() == stack.getItem() && mutation.result.getItemDamage() == stack.getItemDamage()) {
                    list.add(new Mutation(mutation));
                }
            }
        }
        return list.toArray(new Mutation[list.size()]);
    }

    /**
     * Removes all mutations where the given parameter is the result of a mutation
     * @return Removed mutations
     */
    public static List<Mutation> removeMutationsByResult(ItemStack result) {
        List<Mutation> removedMutations = new ArrayList<Mutation>();
        for (Iterator<Mutation> iter = mutations.iterator(); iter.hasNext();) {
            Mutation mutation = iter.next();
            if (mutation.result.isItemEqual(result)) {
                iter.remove();
                removedMutations.add(mutation);
            }
        }
        return removedMutations;
    }

    /** Adds the given mutation to the mutations list */
    public static void add(Mutation mutation) {
        mutations.add(mutation);
    }

    /** Removes the given mutation from the mutations list */
    public static void remove(Mutation mutation) {
        mutations.remove(mutation);
    }

    /**
     * Adds all mutations back into the mutation list. Does not perform and validation.
     */
    public static void addAll(Collection<? extends Mutation> mutationsToAdd) {
        mutations.addAll(mutationsToAdd);
    }
}
