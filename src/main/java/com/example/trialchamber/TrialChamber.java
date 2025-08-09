package com.example.trialchamber;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TrialChamber implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "trial-chamber";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Hardness configuration with lower default values
    public static float VAULT_HARDNESS = 5.0f; // Stone-like hardness
    public static float TRIAL_SPAWNER_HARDNESS = 5.0f; // Stone-like hardness
    private static final String CONFIG_FILE = "config/trial-chamber.properties";

    // Simple config instance
    public static final TrialRestockConfigModel CONFIG = new TrialRestockConfigModel();

    @Override
    public void onInitialize() {
        LOGGER.info("TrialRestock mod initialized!");

        // Load config on server start
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("TrialRestock config loaded - Cooldown: {} seconds ({} ticks)",
                    CONFIG.cooldownSeconds, CONFIG.getCooldownTicks());
        });

        // Load configuration
        loadConfig();

        // Log hardness values
        LOGGER.info("Initialized with hardness: Vault={}, Trial Spawner={}",
                VAULT_HARDNESS, TRIAL_SPAWNER_HARDNESS);

        // Intercept block attack attempts
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.getBlockState(pos).isOf(Blocks.TRIAL_SPAWNER) ||
                    world.getBlockState(pos).isOf(Blocks.VAULT)) {
                // Allow creative mode to bypass tool checks
                if (player.isCreative()) {
                    return ActionResult.PASS;
                }

                ItemStack heldItem = player.getMainHandStack();
                if (!isValidPickaxe(heldItem)) {
                    LOGGER.info("Blocking attack on {} with invalid tool: {}",
                            world.getBlockState(pos).getBlock().getName().getString(),
                            heldItem.getItem().toString());
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        // Handle block break events - BEFORE
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.isOf(Blocks.TRIAL_SPAWNER) ||
                    state.isOf(Blocks.VAULT)) {
                // Allow creative mode
                if (player.isCreative()) {
                    return true;
                }

                ItemStack heldItem = player.getMainHandStack();
                boolean hasValidPickaxe = isValidPickaxe(heldItem);

                LOGGER.info("Player {} trying to break {} with {}, valid: {}",
                        player.getName().getString(),
                        state.getBlock().getName().getString(),
                        heldItem.getItem().toString(),
                        hasValidPickaxe);

                if (!hasValidPickaxe) {
                    LOGGER.info("Blocking {} break - invalid tool", state.getBlock().getName().getString());
                    return false;
                }
                return true;
            }
            return true;
        });

        // Handle block break events - AFTER (drop items)
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            // Skip dropping in creative mode
            if (player.isCreative()) {
                return;
            }

            ItemStack heldItem = player.getMainHandStack();
            if (isValidPickaxe(heldItem)) {
                if (state.isOf(Blocks.TRIAL_SPAWNER)) {
                    // Drop only the trial spawner block (no spawn eggs)
                    dropItem(world, pos, new ItemStack(Items.TRIAL_SPAWNER));
                    LOGGER.info("Trial Spawner broken and dropped with {}", heldItem.getItem().toString());
                } else if (state.isOf(Blocks.VAULT)) {
                    dropItem(world, pos, new ItemStack(Items.VAULT));
                    LOGGER.info("Ominous Vault broken and dropped with {}", heldItem.getItem().toString());
                }
            }
        });
    }

    public void onInitializeClient() {
        LOGGER.info("TrialRestock client initialized!");
    }

    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        Properties properties = new Properties();

        try {
            if (!configFile.exists()) {
                // Create default config with lower hardness
                configFile.getParentFile().mkdirs();
                properties.setProperty("vault_hardness", String.valueOf(VAULT_HARDNESS));
                properties.setProperty("trial_spawner_hardness", String.valueOf(TRIAL_SPAWNER_HARDNESS));
                properties.store(Files.newOutputStream(configFile.toPath()),
                        "Trial Chamber Mod Configuration\n" +
                                "# Hardness values for blocks (higher = harder to break)\n" +
                                "# Default: 5.0 (similar to stone)\n" +
                                "# Minimum: 0.0, Maximum: 3600000.0");
            }

            properties.load(Files.newInputStream(configFile.toPath()));

            VAULT_HARDNESS = parseHardness(properties.getProperty("vault_hardness"), VAULT_HARDNESS);
            TRIAL_SPAWNER_HARDNESS = parseHardness(properties.getProperty("trial_spawner_hardness"), TRIAL_SPAWNER_HARDNESS);

            LOGGER.info("Loaded config: vault_hardness={}, trial_spawner_hardness={}",
                    VAULT_HARDNESS, TRIAL_SPAWNER_HARDNESS);

        } catch (IOException e) {
            LOGGER.error("Failed to load config file: {}", e.getMessage());
        }
    }

    private float parseHardness(String value, float defaultValue) {
        try {
            float hardness = Float.parseFloat(value);
            // Ensure hardness is within valid range
            return Math.max(0.0f, Math.min(hardness, 3600000.0f));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid hardness value: {}, using default: {}", value, defaultValue);
            return defaultValue;
        }
    }

    private void dropItem(World world, BlockPos pos, ItemStack itemStack) {
        if (!world.isClient) {
            ItemEntity itemEntity = new ItemEntity(world,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    itemStack);
            itemEntity.setPickupDelay(10);
            world.spawnEntity(itemEntity);
        }
    }

    public static boolean isValidPickaxe(ItemStack itemStack) {
        return itemStack.isOf(Items.WOODEN_PICKAXE) ||
                itemStack.isOf(Items.STONE_PICKAXE) ||
                itemStack.isOf(Items.IRON_PICKAXE) ||
                itemStack.isOf(Items.GOLDEN_PICKAXE) ||
                itemStack.isOf(Items.DIAMOND_PICKAXE) ||
                itemStack.isOf(Items.NETHERITE_PICKAXE);
    }
}