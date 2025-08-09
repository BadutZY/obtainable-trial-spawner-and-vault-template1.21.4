package com.example.trialchamber.mixin;

import com.example.trialchamber.TrialVaultServerDataAccess;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(VaultBlockEntity.class)
public class VaultBlockEntityMixin {

    @Shadow @Final private VaultServerData serverData;

    @Inject(method = "writeNbt", at = @At("RETURN"))
    void saveTrialRestockData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        TrialVaultServerDataAccess serverDataAccess = (TrialVaultServerDataAccess) serverData;

        // Simpan cooldown data
        NbtCompound cooldowns = new NbtCompound();
        for (Object2LongArrayMap.Entry<UUID> entry : serverDataAccess.trialrestock$getPlayerCooldowns().object2LongEntrySet()) {
            cooldowns.putLong(entry.getKey().toString(), entry.getLongValue());
        }
        nbt.put("trialrestock$playerCooldowns", cooldowns);

        // Simpan last failed unlock time
        nbt.putLong("trialrestock$lastFailedUnlockTime", serverDataAccess.trialrestock$getLastFailedUnlockTime());
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    void loadTrialRestockData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        TrialVaultServerDataAccess serverDataAccess = (TrialVaultServerDataAccess) serverData;

        // Load cooldown data
        if (nbt.contains("trialrestock$playerCooldowns")) {
            NbtCompound cooldowns = nbt.getCompound("trialrestock$playerCooldowns");
            Object2LongArrayMap<UUID> cooldownMap = new Object2LongArrayMap<>();
            for (String key : cooldowns.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    cooldownMap.put(uuid, cooldowns.getLong(key));
                } catch (IllegalArgumentException e) {
                    // Skip invalid UUIDs
                }
            }
            serverDataAccess.trialrestock$setPlayerCooldowns(cooldownMap);
        }

        // Load last failed unlock time
        if (nbt.contains("trialrestock$lastFailedUnlockTime")) {
            serverDataAccess.trialrestock$setLastFailedUnlockTime(nbt.getLong("trialrestock$lastFailedUnlockTime"));
        }
    }
}