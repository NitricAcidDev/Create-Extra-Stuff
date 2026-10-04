package net.dadamalda.create_compatible_storage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CCSTags {
    // Minecraft
    public static TagKey<Block> SHULKER_BOXES = tag("minecraft:shulker_boxes");
    // Create
    public static TagKey<Block> CHEST_MOUNTED_STORAGE = tag("create:chest_mounted_storage");
    public static TagKey<Block> SIMPLE_MOUNTED_STORAGE = tag("create:simple_mounted_storage");
    public static TagKey<Block> SINGLE_BLOCK_INVENTORIES = tag("create:single_block_inventories");
    public static TagKey<Block> BRITTLE = tag("create:brittle");
    // Generic
    public static TagKey<Block> SILENT_MOUNTED_STORAGE = tag("create_trains_interactive:silent_mounted_storage");
    public static TagKey<Block> UNCOOPERATIVE_MOUNTED_STORAGE = tag("create_trains_interactive:uncooperative_mounted_storage");
    public static TagKey<Block> UNCOOPERATIVE_STATIONARY_STORAGE = tag("create_trains_interactive:uncooperative_stationary_storage");
    public static TagKey<Block> UNCOOPERATIVE_STATIONARY_CHESTS = tag("create_trains_interactive:uncooperative_stationary_chests");
    public static TagKey<Block> BARREL_SOUND = tag("create_trains_interactive:barrel_sound");
    public static TagKey<Block> BARREL_NAME = tag("create_trains_interactive:barrel_name");
    public static TagKey<Block> CHEST_SOUND = tag("create_trains_interactive:chest_sound");
    // Farmer's Delight
    public static TagKey<Block> FD_CABINETS = tag("create_trains_interactive:fd/cabinets");
    // Storage Delight
    public static TagKey<Block> SD_CABINET_SOUND = tag("create_trains_interactive:sd/cabinet_sound");
    public static TagKey<Block> SD_CABINET_VARIANTS = tag("create_trains_interactive:sd/cabinet_variants");
    public static TagKey<Block> SD_GLASS_CABINETS = tag("create_trains_interactive:sd/glass_cabinets");
    public static TagKey<Block> SD_BOOKSHELVES_WITH_DOOR = tag("create_trains_interactive:sd/bookshelves_with_door");
    public static TagKey<Block> SD_SMALL_DRAWERS = tag("create_trains_interactive:sd/small_drawers");
    public static TagKey<Block> SD_DRAWERS_WITH_BOOKS = tag("create_trains_interactive:sd/drawers_with_books");
    public static TagKey<Block> SD_DRAWERS_WITH_DOOR = tag("create_trains_interactive:sd/drawers_with_door");
    public static TagKey<Block> SD_DRAWERS = tag("create_trains_interactive:sd/drawers");
    public static  TagKey<Block> SD_CABINETS_WITH_COUNTERTOPS = tag("create_trains_interactive:sd/cabinets_with_countertops");
    // Furniture Refurbished
    public static TagKey<Block> FR_MOUNTED_STORAGE = tag("create_trains_interactive/fr/mounted_storage");
    public static TagKey<Block> FR_DRAWERS = tag("create_trains_interactive:fr/drawers");
    public static TagKey<Block> FR_KITCHEN_DRAWERS = tag("create_trains_interactive:fr/kitchen_drawers");
    public static TagKey<Block> FR_STORAGE_CABINETS = tag("create_trains_interactive:fr/storage_cabinets");
    public static TagKey<Block> FR_KITCHEN_STORAGE_CABINETS = tag("create_trains_interactive:fr/kitchen_storage_cabinets");
    public static TagKey<Block> FR_COOLERS = tag("create_trains_interactive:fr/coolers");
    public static TagKey<Block> FR_CRATES = tag("create_trains_interactive:fr/crates");
    public static TagKey<Block> FR_MAILBOXES = tag("create_trains_interactive:fr/mailboxes");
    // Another Furniture
    public static TagKey<Block> AF_DRAWERS = tag("another_furniture:drawers");
    // Ars Nouveau
    public static TagKey<Block> AN_REPOSITORY = tag("create_trains_interactive:an/repository");
    // Woodworks
    public static TagKey<Block> WW_CLOSETS = tag("create_trains_interactive:ww/closets");
    // Iron Chests
    public static TagKey<Block> IC_IRON_CHEST_MOUNTED_STORAGE = tag("create_trains_interactive:ic/iron_chest_mounted_storage");
    public static TagKey<Block> IC_CRYSTAL_CHEST = tag("create_trains_interactive:ic/crystal_chest");
    public static TagKey<Block> IC_DIRT_CHEST = tag("create_trains_interactive:ic/dirt_chest");
    public static TagKey<Block> IC_COPPER_CHEST = tag("create_trains_interactive:ic/copper_chest");
    public static TagKey<Block> IC_IRON_CHEST = tag("create_trains_interactive:ic/iron_chest");
    public static TagKey<Block> IC_GOLD_CHEST = tag("create_trains_interactive:ic/gold_chest");
    public static TagKey<Block> IC_DIAMOND_CHEST = tag("create_trains_interactive:ic/diamond_chest");
    public static TagKey<Block> IC_OBSIDIAN_CHEST = tag("create_trains_interactive:ic/obsidian_chest");
    // Iron Shulker Boxes
    public static TagKey<Block> ISB_IRON_SHULKER_BOX_MOUNTED_STORAGE = tag("create_trains_interactive:isb/iron_shulker_box_mounted_storage");
    // Alex's Caves
    public static TagKey<Block> AC_METAL_BARREL_SOUND = tag("create_trains_interactive:ac/metal_barrel_sound");
    public static TagKey<Block> AC_METAL_BARREL_NAME = tag("create_trains_interactive:ac/metal_barrel_name");
    public static TagKey<Block> AC_GINGERBARREL_NAME = tag("create_trains_interactive:ac/gingerbarrel_name");
    public static TagKey<Block> AC_GINGERBREAD_DOORS = tag("create_trains_interactive:ac/gingerbread_doors");
    // Let's Do Vinery
    public static TagKey<Block> LDV_CABINET = tag("create_trains_interactive:ldv/cabinet");
    public static TagKey<Block> LDV_DRAWER = tag("create_trains_interactive:ldv/drawer");
    public static TagKey<Block> LDV_STORAGE_POT = tag("create_trains_interactive:ldv/storage_pot");
    public static TagKey<Block> LDV_BARRELS = tag("create_trains_interactive:ldv/barrels");
    // Let's Do Beach Party
    public static TagKey<Block> LDBP_PALM_CABINET = tag("create_trains_interactive:ldbp/palm_cabinet");
    // Let's Do Bakery
    public static TagKey<Block> LDBA_CABINETS = tag("create_trains_interactive:ldba/cabinets");
    public static TagKey<Block> LDBA_DRAWER = tag("create_trains_interactive:ldba/drawer");
    // Let's Do Brewery
    public static TagKey<Block> LDBR_CABINETS = tag("create_trains_interactive:ldbr/cabinets");
    public static TagKey<Block> LDBR_DRAWER = tag("create_trains_interactive:ldbr/drawer");
    // Let's Do Meadow
    public static TagKey<Block> LDM_SHELF = tag("create_trains_interactive:ldm/shelf");
    // Let's Do Candlelight
    public static TagKey<Block> LDCL_CABINETS = tag("create_trains_interactive:ldcl/cabinets");
    public static TagKey<Block> LDCL_DRAWERS = tag("create_trains_interactive:ldcl/drawers");
    // Let's Do Furniture
    public static TagKey<Block> LDF_CABINETS = tag("create_trains_interactive:ldf/cabinets");
    public static TagKey<Block> LDF_COFFER = tag("create_trains_interactive:ldf/coffer");

    public static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.parse(path));
    }
}
