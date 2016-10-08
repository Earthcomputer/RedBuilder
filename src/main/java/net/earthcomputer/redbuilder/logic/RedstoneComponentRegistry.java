package net.earthcomputer.redbuilder.logic;

import static net.minecraft.init.Blocks.*;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneComponentRegistry {

	private RedstoneComponentRegistry() {
	}

	private static final Map<Block, IRedstoneComponent> components = Maps.newHashMap();

	public static PowerInfo getPowerInfo(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		IRedstoneComponent component = components.get(state.getBlock());
		if (component == null) {
			return new PowerInfo();
		} else {
			return component.getPowerInfo(world, pos, state);
		}
	}

	public static void register(Block block, IRedstoneComponent component) {
		components.put(block, component);
	}

	private static void registerComponents() {
		// POWERED BLOCKS
		register(DISPENSER, new ComponentBUDPowered());
		register(DROPPER, new ComponentBUDPowered());
		register(HOPPER, new ComponentNormalPowered());
		register(PISTON, new ComponentBUDPowered());
		register(STICKY_PISTON, new ComponentBUDPowered());
		register(REDSTONE_LAMP, new ComponentNormalPowered());
		register(LIT_REDSTONE_LAMP, new ComponentNormalPowered());
		register(NOTEBLOCK, new ComponentNormalPowered());
		register(TNT, new ComponentNormalPowered());
		register(RAIL, new ComponentNormalPowered());
		register(GOLDEN_RAIL, new ComponentNormalPowered());
		register(ACTIVATOR_RAIL, new ComponentNormalPowered());
		register(OAK_DOOR, new ComponentDoor());
		register(SPRUCE_DOOR, new ComponentDoor());
		register(BIRCH_DOOR, new ComponentDoor());
		register(JUNGLE_DOOR, new ComponentDoor());
		register(ACACIA_DOOR, new ComponentDoor());
		register(DARK_OAK_DOOR, new ComponentDoor());
		register(IRON_DOOR, new ComponentDoor());
		register(OAK_FENCE_GATE, new ComponentNormalPowered());
		register(SPRUCE_FENCE_GATE, new ComponentNormalPowered());
		register(BIRCH_FENCE_GATE, new ComponentNormalPowered());
		register(JUNGLE_FENCE_GATE, new ComponentNormalPowered());
		register(ACACIA_FENCE_GATE, new ComponentNormalPowered());
		register(DARK_OAK_FENCE_GATE, new ComponentNormalPowered());
		register(TRAPDOOR, new ComponentNormalPowered());
		register(IRON_TRAPDOOR, new ComponentNormalPowered());
		register(SKULL, new ComponentDragonHead());
		register(COMMAND_BLOCK, new ComponentNormalPowered());
		register(REPEATING_COMMAND_BLOCK, new ComponentNormalPowered());
		register(CHAIN_COMMAND_BLOCK, new ComponentNormalPowered());
		register(STRUCTURE_BLOCK, new ComponentNormalPowered());

		// POWER SOURCES
		register(REDSTONE_BLOCK, new ComponentRedstoneBlock());
		register(LEVER, new ComponentLever());
		register(WOODEN_BUTTON, new ComponentButton());
		register(STONE_BUTTON, new ComponentButton());
		register(WOODEN_PRESSURE_PLATE, new ComponentPressurePlateNormal());
		register(STONE_PRESSURE_PLATE, new ComponentPressurePlateNormal());
		register(LIGHT_WEIGHTED_PRESSURE_PLATE, new ComponentPressurePlateWeighted());
		register(HEAVY_WEIGHTED_PRESSURE_PLATE, new ComponentPressurePlateWeighted());
		register(TRIPWIRE_HOOK, new ComponentTripwireHook());
		register(TRAPPED_CHEST, new ComponentTrappedChest());
		register(DAYLIGHT_DETECTOR, new ComponentDaylightSensor());
		register(DAYLIGHT_DETECTOR_INVERTED, new ComponentDaylightSensor());
		register(DETECTOR_RAIL, new ComponentRailDetector());

		// LOGIC COMPONENTS
		register(REDSTONE_WIRE, new ComponentRedstoneWire());
		register(UNLIT_REDSTONE_TORCH, new ComponentRedstoneTorch());
		register(REDSTONE_TORCH, new ComponentRedstoneTorch());
		register(UNPOWERED_REPEATER, new ComponentRepeater());
		register(POWERED_REPEATER, new ComponentRepeater());
		register(UNPOWERED_COMPARATOR, new ComponentComparator());
		register(POWERED_COMPARATOR, new ComponentComparator());
	}

	static {
		registerComponents();
	}

}
