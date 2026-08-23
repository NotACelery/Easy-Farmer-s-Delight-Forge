package dev.celerbi.easyfarmersdelightcompat.registry;
import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.block.*;
import dev.celerbi.easyfarmersdelightcompat.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public final class ModBlocks {
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,EasyFarmersDelightCompat.MOD_ID);
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,EasyFarmersDelightCompat.MOD_ID);
 public static final RegistryObject<CompatFarmerBlock> PADDY_FARMER=BLOCKS.register("paddy_farmer",()->new CompatFarmerBlock(props(),FarmerVariant.PADDY));
 public static final RegistryObject<CompatFarmerBlock> RICH_FARMER=BLOCKS.register("rich_farmer",()->new CompatFarmerBlock(props(),FarmerVariant.RICH));
 public static final RegistryObject<CompatFarmerBlock> RICH_PADDY_FARMER=BLOCKS.register("rich_paddy_farmer",()->new CompatFarmerBlock(props(),FarmerVariant.RICH_PADDY));
 public static final RegistryObject<CutterBlock> CUTTER=BLOCKS.register("cutter",()->new CutterBlock(props()));
 public static final RegistryObject<VillagerNoiseSwitchBlock> VILLAGER_NOISE_SWITCH=BLOCKS.register("villager_noise_switch",()->new VillagerNoiseSwitchBlock(props()));
 public static final RegistryObject<CutterItem> CUTTER_ITEM=ITEMS.register("cutter",()->new CutterItem(CUTTER.get(),new Item.Properties()));
 public static final RegistryObject<VillagerNoiseSwitchItem> VILLAGER_NOISE_SWITCH_ITEM=ITEMS.register("villager_noise_switch",()->new VillagerNoiseSwitchItem(VILLAGER_NOISE_SWITCH.get(),new Item.Properties()));
 public static final RegistryObject<CompatFarmerItem> PADDY_FARMER_ITEM=ITEMS.register("paddy_farmer",()->new CompatFarmerItem(PADDY_FARMER.get(),new Item.Properties()));
 public static final RegistryObject<CompatFarmerItem> RICH_FARMER_ITEM=ITEMS.register("rich_farmer",()->new CompatFarmerItem(RICH_FARMER.get(),new Item.Properties()));
 public static final RegistryObject<CompatFarmerItem> RICH_PADDY_FARMER_ITEM=ITEMS.register("rich_paddy_farmer",()->new CompatFarmerItem(RICH_PADDY_FARMER.get(),new Item.Properties()));
 private static BlockBehaviour.Properties props(){return BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).noOcclusion();}
 private ModBlocks(){}
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ModCreativeTabs.register(bus);}
}
