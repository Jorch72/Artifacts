package com.draco18s.artifacts; 

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

import com.draco18s.artifacts.api.ArtifactsAPI;
import com.draco18s.artifacts.api.WeightedRandomArtifact;
import com.draco18s.artifacts.arrowtrapbehaviors.DispenserBehaviors;
import com.draco18s.artifacts.block.*;
import com.draco18s.artifacts.client.*;
import com.draco18s.artifacts.entity.*;
import com.draco18s.artifacts.factory.*;
import com.draco18s.artifacts.item.*;
import com.draco18s.artifacts.network.ArtifactPacketPipeline;
import com.draco18s.artifacts.network.CPacketArtifactComponent;
import com.draco18s.artifacts.network.PacketHandlerClient;
import com.draco18s.artifacts.network.PacketHandlerServer;
import com.draco18s.artifacts.network.SPacketGeneral;
import com.draco18s.artifacts.worldgen.PlaceTraps;

@Mod(modid = "Artifacts", name = "Unique Artifacts", version = "1.0.0")
public class DragonArtifacts{
	@Instance("Artifacts")
    public static DragonArtifacts instance;
	public static boolean renderNamesInPedestals = false;
	public static boolean renderInvis = false;
	public static boolean boundingInvis = true;
	public static PlaceTraps worldGen;
    
    @SidedProxy(clientSide = "com.draco18s.artifacts.client.ClientProxy", serverSide = "com.draco18s.artifacts.CommonProxy")
    public static CommonProxy proxy;
    
    //Pipeline for packets
    public static ArtifactPacketPipeline packetPipeline = new ArtifactPacketPipeline();
    
    public static CreativeTabs tabGeneral = new CreativeTabs("tabGeneral") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(BlockSpikes.instance);
		}
	};
	
	public static CreativeTabs tabArtifacts = new CreativeTabs("tabArtifacts") {
		@Override
		public Item getTabIconItem() {
			return ItemArtifact.instance;
		}
	};
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
			config.load();
			String armorIDh1 = "artifact_leather_helmet";
			String armorIDh2 = "artifact_chainmail_helmet";
			String armorIDh3 = "artifact_iron_helmet";
			String armorIDh4 = "artifact_golden_helmet";
			String armorIDh5 = "artifact_diamond_helmet";
			
			String armorIDc1 = "artifact_leather_chestplate";
			String armorIDc2 = "artifact_chainmail_chestplate";
			String armorIDc3 = "artifact_iron_chestplate";
			String armorIDc4 = "artifact_golden_chestplate";
			String armorIDc5 = "artifact_diamond_chestplate";
			
			String armorIDl1 = "artifact_leather_leggings";
			String armorIDl2 = "artifact_chainmail_leggings";
			String armorIDl3 = "artifact_iron_leggings";
			String armorIDl4 = "artifact_golden_leggings";
			String armorIDl5 = "artifact_diamond_leggings";
			
			String armorIDb1 = "artifact_leather_boots";
			String armorIDb2 = "artifact_chainmail_boots";
			String armorIDb3 = "artifact_iron_boots";
			String armorIDb4 = "artifact_golden_boots";
			String armorIDb5 = "artifact_diamond_boots";
			String artifactID = "artifact";
			String tb1 = "trapblade_wood";
			String tb2 = "trapblade_stone";
			String tb3 = "trapblade_iron";
			String tb4 = "trapblade_gold";
			String tb5 = "trapblade_diamond";
			String orichalcumID = "dust_orichalcum";
			String calendarID = "lunar_calendar";
			
			config.addCustomCategoryComment("WorldGen", "By default this mod alters worldgen slightly, adding more and different traps to\npyramids, temples, and strongholds as well as quicksand 'lakes'.\nThese may be disabled individually.");
			boolean pyrm = config.get("WorldGen","Pyramids",true).getBoolean(true);
			boolean temp = config.get("WorldGen","Temples",true).getBoolean(true);
			boolean strn = config.get("WorldGen","Strongholds",true).getBoolean(true);
			boolean quik = config.get("WorldGen","QuicksandPits",true).getBoolean(true);
			boolean towers = config.get("WorldGen","WizardTowers",true).getBoolean(true);
			Property conf = config.get("WorldGen","dimensionWhitelistEnable",false);
			conf.comment = "Enables the whitelist for worldgen.  If enabled, world gen objects will only generate in whitelisted dimensions.";
			boolean usewhite = conf.getBoolean(false);
			conf = config.get("WorldGen","dimensionBlacklistEnable",false);
			conf.comment = "Enables the blacklist for worldgen.  If enabled, world gen objects will never generate in blacklisted dimensions.\nBlacklist will override whitelist.  -1 and 1 (Nether and End) are always blacklisted.";
			boolean useblack = conf.getBoolean(false);
			
			Property cw = config.get("WorldGen","dimensionWhitelistList", new int[] {0});
			Property cb = config.get("WorldGen","dimensionBlacklistList", new int[] {-1,1});
			int[] white = cw.getIntList();
			int[] black = cb.getIntList();
			
			Arrays.sort(white);
			Arrays.sort(black);
			
			String a=Arrays.toString(white);
			String whitestring[]=a.substring(1,a.length()-1).split(", ");
			String b=Arrays.toString(black);
			String blackstring[]=b.substring(1,b.length()-1).split(", ");

			cw.set(whitestring);
			cb.set(blackstring);
			
    		boolean useAntibuild = config.get("WorldGen","UseAntibuilders",true).getBoolean(true);
			
			ConfigCategory longNames = config.getCategory("general");
			longNames.setComment("These settings dictate how item names are displayed.");
			Property enchName = config.get("general","Enchantments",true);
			Property matName = config.get("general","Material",true);
			Property adjName = config.get("general","Adjectives",true);
			Property renderNames = config.get("rendering","RenderNames",false);
			renderNames.comment = "Set to false to disable rendering of item names on display pedesals";
			renderNamesInPedestals = renderNames.getBoolean(true);
			
			ConfigCategory renderConf = config.getCategory("rendering");
			renderConf.setComment("Determines some options on invisible blocks");
			conf = config.get("rendering", "RenderInvis", false);
    		conf.comment = "Set this to true to render invisible blocks.  Even when false, they will render in your inventory.";
    		renderInvis = conf.getBoolean(false);
    		
    		conf = config.get("rendering", "BoundInvis", true);
    		conf.comment = "Set this to false to disable bounding boxes on invisible blocks.\nALERT: without bounding boxes you will not be able to destroy them!\nThis is only recommended for playing adventure maps.";
    		boundingInvis = conf.getBoolean(true);

    		conf = config.get("rendering", "TrapSwordPackage", "artifacts");
    		conf.comment = "Sets the package the icons should be pulled from.\nDefault is 'artifacts' which pulls the default icons.\nNot sure where this points otherwise.";
    		String bladePackage = conf.getString();
    		conf = config.get("rendering", "TrapSwordIcon", "blade");
    		conf.comment = "Sets the rendering type for swords in arrow traps.\nDefault is 'blade' which attempts to maintain the jaggy nature of the vanilla sword.\n'blade_alt' uses a smaller texture, maintaining strait lines and mirroring the vanilla item as closely as possible.\nAdditional textures can be created and set here as well, if desired, without replacing existing textures.";
    		String bladeRender = conf.getString();
    		
			String lightID = "light_block";
			String pedID = "pedestal";
    		String invis2ID = "invisible_bedrock";
    		String spikesID = "spike_block";
    		String arrowSlotID = "arrow_trap";
    		String coverplateID = "cover_plate";
    		String quickID = "quicksand";
    		String pseudoATID = "arrow_trap_item";
    		String pseudoCPID = "cover_plate_item";
    		String pseudoFBID = "fake_block_item";
    		String wallplateID = "wall_pressure_plate";
    		String owallplateID = "obsidian_wall_pressure_plate";
    		String invisppID = "invisible_pressure_plate";
    		String oinvisppID = "obsidian_invisible_pressure_plate";
    		String fakeID = "illusionary_block";
    		String invisID = "invisible_block";
    		String teSwordID = "fake_tile_entity";
    		String floatID = "floating_block";
    		String antiID = "anti_builder";
    		String ignoreID = "anti_anti_builder_stone";
    		String laserSourceID = "laser_source";
    		String laserBeamID = "laser_beam";
    		
    		config.addCustomCategoryComment("spawning", "These settings alter the spawning rarity of artifacts in the various chests.\nLower is rarer, higher is more common.  By default pyramids and temples generate ~2 total.\nCross-Mod Treasure String ('ProceeduralGeneration') is for inter-mod treasure gen.");
    		int dungRare = config.get("spawning","Dungeons",0).getInt(0);
    		int pyrRare = config.get("spawning","Pyramids",4).getInt(4);
    		int tempRare = config.get("spawning","Temples",8).getInt(8);
    		int strong1Rare = config.get("spawning","Stronghold_Library",6).getInt(6);
    		int strong2Rare = config.get("spawning","Stronghold_Corridor",1).getInt(1);
    		int strong3Rare = config.get("spawning","Stronghold_Crossing",3).getInt(3);
    		int mineRare = config.get("spawning","Mineshafts",0).getInt(0);
    		int villRare = config.get("spawning","Blacksmith",1).getInt(1);
    		int wizRare = config.get("spawning","WizTowers",10).getInt(10);
    		int procRare = config.get("spawning","crossModTreasureString_ProceeduralGeneration",5).getInt(5);
		config.save();
        ArtifactsAPI.artifacts = new FactoryArtifact();
        ArtifactsAPI.itemicons = new FactoryItemIcons();
        ArtifactsAPI.traps = new FactoryTrapBehaviors();

		ArtifactsAPI.artifacts.registerUpdateNBTKey("orePingDelay");
		ArtifactsAPI.artifacts.registerUpdateNBTKey("resCooldown");
		ArtifactsAPI.artifacts.registerUpdateNBTKey("medkitDelay");
		ArtifactsAPI.artifacts.registerUpdateNBTKey("adrenDelay");
		
		ItemArtifact.instance = new ItemArtifact().setUnlocalizedName(artifactID);
		ItemArtifactArmor.hcloth = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CLOTH, 0, 2, 0).setUnlocalizedName(armorIDh1);
		ItemArtifactArmor.hchain = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CHAIN, 1, 2, 0).setUnlocalizedName(armorIDh2);
		ItemArtifactArmor.hiron = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.IRON, 2, 2, 0).setUnlocalizedName(armorIDh3);
		ItemArtifactArmor.hgold = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.GOLD, 4, 2, 0).setUnlocalizedName(armorIDh4);
		ItemArtifactArmor.hdiamond = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.DIAMOND, 3, 2, 0).setUnlocalizedName(armorIDh5);
		ItemArtifactArmor.bcloth = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CLOTH, 0, 3, 3).setUnlocalizedName(armorIDb1);
		ItemArtifactArmor.bchain = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CHAIN, 1, 3, 3).setUnlocalizedName(armorIDb2);
		ItemArtifactArmor.biron = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.IRON, 2, 3, 3).setUnlocalizedName(armorIDb3);
		ItemArtifactArmor.bgold = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.GOLD, 4, 3, 3).setUnlocalizedName(armorIDb4);
		ItemArtifactArmor.bdiamond = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.DIAMOND, 3, 3, 3).setUnlocalizedName(armorIDb5);
		ItemArtifactArmor.ccloth = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CLOTH, 0, 4, 1).setUnlocalizedName(armorIDc1);
		ItemArtifactArmor.cchain = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CHAIN, 1, 4, 1).setUnlocalizedName(armorIDc2);
		ItemArtifactArmor.ciron = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.IRON, 2, 4, 1).setUnlocalizedName(armorIDc3);
		ItemArtifactArmor.cgold = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.GOLD, 4, 4, 1).setUnlocalizedName(armorIDc4);
		ItemArtifactArmor.cdiamond = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.DIAMOND, 3, 4, 1).setUnlocalizedName(armorIDc5);
		ItemArtifactArmor.lcloth = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CLOTH, 0, 5, 2).setUnlocalizedName(armorIDl1);
		ItemArtifactArmor.lchain = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.CHAIN, 1, 5, 2).setUnlocalizedName(armorIDl2);
		ItemArtifactArmor.liron = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.IRON, 2, 5, 2).setUnlocalizedName(armorIDl3);
		ItemArtifactArmor.lgold = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.GOLD, 4, 5, 2).setUnlocalizedName(armorIDl4);
		ItemArtifactArmor.ldiamond = (ItemArtifactArmor) new ItemArtifactArmor(ArmorMaterial.DIAMOND, 3, 5, 2).setUnlocalizedName(armorIDl5);
		ItemOrichalcumDust.instance = new ItemOrichalcumDust().setUnlocalizedName(orichalcumID);
		ItemCalendar.instance = new ItemCalendar().setUnlocalizedName(calendarID);

        ItemArtifactArmor.setupArrays();
        
		ItemFakeSwordRenderable.wood = new ItemFakeSwordRenderable(ToolMaterial.WOOD, bladePackage+":wood_"+bladeRender).setUnlocalizedName(tb1);
		ItemFakeSwordRenderable.stone = new ItemFakeSwordRenderable(ToolMaterial.STONE, bladePackage+":stone_"+bladeRender).setUnlocalizedName(tb2);
		ItemFakeSwordRenderable.iron = new ItemFakeSwordRenderable(ToolMaterial.IRON, bladePackage+":iron_"+bladeRender).setUnlocalizedName(tb3);
		ItemFakeSwordRenderable.gold = new ItemFakeSwordRenderable(ToolMaterial.GOLD, bladePackage+":gold_"+bladeRender).setUnlocalizedName(tb4);
		ItemFakeSwordRenderable.diamond = new ItemFakeSwordRenderable(ToolMaterial.EMERALD, bladePackage+":diamond_"+bladeRender).setUnlocalizedName(tb5);
        
		ItemArtifactArmor.doEnchName = ItemArtifact.doEnchName = enchName.getBoolean(true);
		ItemArtifactArmor.doMatName = ItemArtifact.doMatName = matName.getBoolean(true);
		ItemArtifactArmor.doAdjName = ItemArtifact.doAdjName = adjName.getBoolean(true);
		
		
		BlockAntibuilder.instance = new BlockAntibuilder().setBlockName(antiID);
		BlockCoverPlate.instance = new BlockCoverPlate().setBlockName(coverplateID);
		BlockIllusionary.instance = new BlockIllusionary().setBlockName(fakeID);
		BlockInvisibleBedrock.instance = new BlockInvisibleBedrock().setBlockName(invis2ID);
		BlockInvisibleBlock.instance = new BlockInvisibleBlock().setBlockName(invisID);
		BlockInvisiblePressurePlate.instance = new BlockInvisiblePressurePlate("Invisible Pressure Plate", Material.rock, BlockPressurePlate.Sensitivity.mobs).setBlockName(invisppID);
		BlockInvisiblePressurePlate.obsidian = new BlockInvisiblePressurePlate("Invisible Obsidiplate", Material.rock, BlockPressurePlate.Sensitivity.players).setBlockName(oinvisppID);
		BlockLaserBeam.instance = new BlockLaserBeam().setBlockName(laserBeamID);
		BlockLaserBeamSource.instance = (BlockLaserBeamSource) new BlockLaserBeamSource().setBlockName(laserSourceID);
		BlockLight.instance = new BlockLight().setBlockName(lightID);
		BlockPedestal.instance = (BlockPedestal) new BlockPedestal().setBlockName(pedID);
		BlockQuickSand.instance = new BlockQuickSand().setBlockName(quickID);
		BlockSolidAir.instance = new BlockSolidAir().setBlockName(floatID);
		BlockSpikes.instance = new BlockSpikes().setBlockName(spikesID);
		BlockStoneBrickMovable.instance = new BlockStoneBrickMovable().setBlockName(ignoreID);
		BlockSword.instance = new BlockSword().setBlockName(teSwordID);
		BlockTrap.instance = new BlockTrap().setBlockName(arrowSlotID);
		BlockWallPlate.instance = new BlockWallPlate("Wallplate", Material.circuits, BlockPressurePlate.Sensitivity.mobs).setBlockName(wallplateID);
		BlockWallPlate.obsidian = new BlockWallPlate("Wall Obsidiplate", Material.circuits, BlockPressurePlate.Sensitivity.players).setBlockName(owallplateID);
		PseudoBlockIllusionary.instance = new PseudoBlockIllusionary().setBlockName(pseudoFBID);
		PseudoBlockTrap.instance = new PseudoBlockTrap().setBlockName(pseudoATID);
		PseudoCoverplate.instance = new PseudoCoverplate().setBlockName(pseudoCPID);
				
		GameRegistry.registerBlock(BlockAntibuilder.instance, antiID);
		GameRegistry.registerBlock(BlockCoverPlate.instance, coverplateID);
		GameRegistry.registerBlock(BlockIllusionary.instance, fakeID);
		GameRegistry.registerBlock(BlockInvisibleBedrock.instance, invis2ID);
		GameRegistry.registerBlock(BlockInvisibleBlock.instance, invisID);
		GameRegistry.registerBlock(BlockInvisiblePressurePlate.instance, invisppID);
		GameRegistry.registerBlock(BlockInvisiblePressurePlate.obsidian, oinvisppID);
		GameRegistry.registerBlock(BlockLaserBeam.instance, laserBeamID);
		GameRegistry.registerBlock(BlockLaserBeamSource.instance, laserSourceID);
		GameRegistry.registerBlock(BlockLight.instance, lightID);
        GameRegistry.registerBlock(BlockPedestal.instance, pedID);
		GameRegistry.registerBlock(BlockQuickSand.instance, quickID);
		GameRegistry.registerBlock(BlockSolidAir.instance, floatID);
		GameRegistry.registerBlock(BlockSpikes.instance, spikesID);
		GameRegistry.registerBlock(BlockStoneBrickMovable.instance, ignoreID);
		GameRegistry.registerBlock(BlockSword.instance, teSwordID);
		GameRegistry.registerBlock(BlockTrap.instance, arrowSlotID);
		GameRegistry.registerBlock(BlockWallPlate.instance, wallplateID);
		GameRegistry.registerBlock(BlockWallPlate.obsidian, owallplateID);
		GameRegistry.registerBlock(PseudoBlockIllusionary.instance, pseudoFBID);
		GameRegistry.registerBlock(PseudoBlockTrap.instance, pseudoATID);
		GameRegistry.registerBlock(PseudoCoverplate.instance, pseudoCPID);
        
        GameRegistry.registerItem(ItemArtifact.instance, artifactID);
        GameRegistry.registerItem(ItemArtifactArmor.hcloth, armorIDh1);
        GameRegistry.registerItem(ItemArtifactArmor.hchain, armorIDh2);
        GameRegistry.registerItem(ItemArtifactArmor.hiron, armorIDh3);
        GameRegistry.registerItem(ItemArtifactArmor.hgold, armorIDh4);
        GameRegistry.registerItem(ItemArtifactArmor.hdiamond, armorIDh5);
        GameRegistry.registerItem(ItemArtifactArmor.ccloth, armorIDc1);
        GameRegistry.registerItem(ItemArtifactArmor.cchain, armorIDc2);
        GameRegistry.registerItem(ItemArtifactArmor.ciron, armorIDc3);
        GameRegistry.registerItem(ItemArtifactArmor.cgold, armorIDc4);
        GameRegistry.registerItem(ItemArtifactArmor.cdiamond, armorIDc5);
        GameRegistry.registerItem(ItemArtifactArmor.lcloth, armorIDl1);
        GameRegistry.registerItem(ItemArtifactArmor.lchain, armorIDl2);
        GameRegistry.registerItem(ItemArtifactArmor.liron, armorIDl3);
        GameRegistry.registerItem(ItemArtifactArmor.lgold, armorIDl4);
        GameRegistry.registerItem(ItemArtifactArmor.ldiamond, armorIDl5);
        GameRegistry.registerItem(ItemArtifactArmor.bcloth, armorIDb1);
        GameRegistry.registerItem(ItemArtifactArmor.bchain, armorIDb2);
        GameRegistry.registerItem(ItemArtifactArmor.biron, armorIDb3);
		GameRegistry.registerItem(ItemArtifactArmor.bgold, armorIDb4);
		GameRegistry.registerItem(ItemArtifactArmor.bdiamond, armorIDb5);
		GameRegistry.registerItem(ItemOrichalcumDust.instance, orichalcumID);
		GameRegistry.registerItem(ItemCalendar.instance, calendarID);
		GameRegistry.registerItem(ItemFakeSwordRenderable.wood, tb1);
		GameRegistry.registerItem(ItemFakeSwordRenderable.stone, tb2);
		GameRegistry.registerItem(ItemFakeSwordRenderable.iron, tb3);
		GameRegistry.registerItem(ItemFakeSwordRenderable.gold, tb4);
		GameRegistry.registerItem(ItemFakeSwordRenderable.diamond, tb5);

        worldGen = new PlaceTraps(pyrm, temp, strn, quik, towers, usewhite, useblack, white, black, useAntibuild);
        GameRegistry.registerWorldGenerator(worldGen, 10);
        
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, Math.max(6, wizRare)));
        //ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, 2));//would a second, rarer chance, be any different than a single large chance?
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(Items.enchanted_book, 0, 1, 1, 5));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(Items.diamond, 0, 2, 5, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(Items.gold_nugget, 0, 3, 7, 5));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 0, 1, 1, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 1, 1, 1, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 2, 1, 1, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 3, 1, 1, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 4, 1, 1, 3));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(Items.experience_bottle, 0, 1, 1, 2));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomChestContent(ItemOrichalcumDust.instance, 0, 4, 12, 2));
        
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hcloth, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.ccloth, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.lcloth, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.bcloth, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hchain, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.cchain, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.lchain, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.bchain, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hiron, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.ciron, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.liron, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.biron, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hgold, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.cgold, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.lgold, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.bgold, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hdiamond, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.cdiamond, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.ldiamond, 0, 1, 1, procRare));
        ChestGenHooks.getInfo("ProceeduralGeneration").addItem(new WeightedRandomArtifact(ItemArtifactArmor.bdiamond, 0, 1, 1, procRare));
        
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, pyrRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, tempRare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, strong1Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, strong2Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, strong3Rare));
        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, villRare));
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, mineRare));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomArtifact(ItemArtifact.instance, 0, 1, 1, dungRare));

        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifactArmor.hcloth, 0, 1, 1, Math.max(6, wizRare)));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifactArmor.ccloth, 0, 1, 1, Math.max(6, wizRare)));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifactArmor.lcloth, 0, 1, 1, Math.max(6, wizRare)));
        ChestGenHooks.getInfo("A_WIZARD_DID_IT").addItem(new WeightedRandomArtifact(ItemArtifactArmor.bcloth, 0, 1, 1, Math.max(6, wizRare)));

        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hchain, 0, 1, 1, dungRare));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cchain, 0, 1, 1, dungRare));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.lchain, 0, 1, 1, dungRare));
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.bchain, 0, 1, 1, dungRare));

        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hiron, 0, 1, 1, villRare));
        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomArtifact(ItemArtifactArmor.ciron, 0, 1, 1, villRare));
        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomArtifact(ItemArtifactArmor.liron, 0, 1, 1, villRare));
        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomArtifact(ItemArtifactArmor.biron, 0, 1, 1, villRare));

        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hgold, 0, 1, 1, pyrRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cgold, 0, 1, 1, pyrRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.lgold, 0, 1, 1, pyrRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.bgold, 0, 1, 1, pyrRare));

        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hdiamond, 0, 1, 1, strong2Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cdiamond, 0, 1, 1, strong2Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomArtifact(ItemArtifactArmor.ldiamond, 0, 1, 1, strong2Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomArtifact(ItemArtifactArmor.bdiamond, 0, 1, 1, strong2Rare));

        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hdiamond, 0, 1, 1, strong3Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cdiamond, 0, 1, 1, strong3Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.ldiamond, 0, 1, 1, strong3Rare));
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.bdiamond, 0, 1, 1, strong3Rare));

        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.ccloth, 0, 1, 1, mineRare));
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hiron, 0, 1, 1, mineRare));
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomArtifact(ItemArtifactArmor.hchain, 0, 1, 1, mineRare));

        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cdiamond, 0, 1, 1, tempRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.cgold, 0, 1, 1, tempRare));
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomArtifact(ItemArtifactArmor.ciron, 0, 1, 1, tempRare));
        
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockPedestal.instance,2), "ggg","g g","sss",'g', new ItemStack(Blocks.glass_pane), 's', "stone"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockSpikes.instance, 2), "i i","sss", 'i', "ingotIron", 's', new ItemStack(Blocks.stone_slab)));
		GameRegistry.addShapedRecipe(new ItemStack(BlockSpikes.instance, 2), "i i","sss", 'i', new ItemStack(Items.iron_ingot), 's', new ItemStack(Blocks.stone_slab));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockTrap.instance), new ItemStack(Items.painting), new ItemStack(Blocks.dispenser));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockWallPlate.instance, 2), "s", "s", "s", 's', "stone"));
		GameRegistry.addShapedRecipe(new ItemStack(BlockWallPlate.obsidian, 2), "s", "s", "s", 's', new ItemStack(Blocks.obsidian));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 1), "logWood", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 2), "cobblestone", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget));
		//These oredict recipes don't work with vanilla items
		/*GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 1, 2), "ingotIron", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Item.goldNugget));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 1, 3), "gemDiamond", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Item.goldNugget));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 1, 4), "ingotGold", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Item.goldNugget));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(ItemOrichalcumDust.instance, 1, 5), "leather", new ItemStack(ItemOrichalcumDust.instance, 1, 0), Item.goldNugget));*/
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 6), new ItemStack(Items.leather), new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget);
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 3), new ItemStack(Items.iron_ingot), new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget);
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 5), new ItemStack(Items.gold_ingot), new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget);
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 2, 4), new ItemStack(Items.diamond), new ItemStack(ItemOrichalcumDust.instance, 1, 0), Items.gold_nugget);
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifact.instance));
		
		for(int i = 0; i < 4; ++i) {
			GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifactArmor.clothArray[i]));
			GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifactArmor.chainArray[i]));
			GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifactArmor.ironArray[i]));
			GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifactArmor.goldArray[i]));
			GameRegistry.addShapelessRecipe(new ItemStack(ItemOrichalcumDust.instance, 4, 0), new ItemStack(ItemArtifactArmor.diamondArray[i]));
		}
		
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 2), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 3), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 4), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 5), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 6), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 7), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addShapelessRecipe(new ItemStack(BlockQuickSand.instance, 8), new ItemStack(Items.potionitem, 1, 8204), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt), new ItemStack(Blocks.dirt));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockLaserBeamSource.instance), "sss", "rog", "sss", 'o', new ItemStack(ItemOrichalcumDust.instance, 4, 0), 'g', new ItemStack(Blocks.glass_pane), 's', "stone", 'r', new ItemStack(Items.redstone)));
		GameRegistry.addShapedRecipe(new ItemStack(ItemCalendar.instance), "ppp","pcp","ppp", 'p', new ItemStack(Items.paper), 'c', new ItemStack(Items.clock));
		
		//MinecraftForge.setToolClass(ItemArtifact.instance, "pickaxe", 3); //This code changed; moved to artifact api.
		DamageSourceSword.instance = new DamageSourceSword("artifacts.trapsword");
		DamageSourceQuicksand.instance = new DamageSourceQuicksand("artifacts.quicksand");
        
        GameRegistry.registerTileEntity(TileEntityDisplayPedestal.class, "artifacts.pedestal");
		GameRegistry.registerTileEntity(TileEntitySword.class, "artifacts.tesword");
		GameRegistry.registerTileEntity(TileEntityTrap.class, "artifacts.arrowtrap");
		GameRegistry.registerTileEntity(TileEntitySpikes.class, "artifacts.spiketrap");
		GameRegistry.registerTileEntity(TileEntityAntibuilder.class, "artifacts.antibuilder");
        EntityRegistry.registerModEntity(EntityClayGolem.class, "EntClayGolem", 0, this, 350, 5, false);
        EntityRegistry.registerModEntity(EntitySpecialArrow.class, "SpecialArrow", 1, this, 64, 20, true);
        EntityList.addMapping(EntityClayGolem.class, "ClayGolem", 3, 13347172, 7033635);//13347172 is pale
		
        proxy.registerTickHandlers();
        proxy.registerEventHandlers();
		proxy.registerRenders();
		DispenserBehaviors.registerBehaviors();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
	
	@EventHandler
    public void load(FMLInitializationEvent event)
    {
		packetPipeline.initialise();
    }
	
	@EventHandler
	public void PostInit(FMLPostInitializationEvent event) {
		packetPipeline.registerPacket(CPacketArtifactComponent.class);
		packetPipeline.registerPacket(SPacketGeneral.class);
		packetPipeline.postInitialise();
	}
}