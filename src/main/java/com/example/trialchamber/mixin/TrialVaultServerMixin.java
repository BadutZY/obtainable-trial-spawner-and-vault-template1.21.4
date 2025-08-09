package com.example.trialchamber.mixin;

import com.example.trialchamber.TrialVaultServerDataAccess;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static com.example.trialchamber.TrialChamber.CONFIG;

@Mixin(VaultBlockEntity.Server.class)
public class TrialVaultServerMixin {

    @Inject(method = "tryUnlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/vault/VaultServerData;markPlayerAsRewarded(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private static void checkUnlockRequirements(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        TrialVaultServerDataAccess serverDataAccess = (TrialVaultServerDataAccess) serverData;

        // Cek apakah pemain masih dalam cooldown
        if (serverDataAccess.trialrestock$getPlayerCooldowns().containsKey(player.getUuid())) {
            long cooldownEndTime = serverDataAccess.trialrestock$getPlayerCooldowns().getLong(player.getUuid());
            long currentTime = world.getTime();

            if (currentTime < cooldownEndTime) {
                // Hitung sisa waktu dalam detik
                long remainingTicks = cooldownEndTime - currentTime;
                long remainingSeconds = (remainingTicks + 19) / 20; // Round up to nearest second

                playFailedUnlockSound(world, serverData, pos);
                player.sendMessage(Text.of("Vault sedang dalam cooldown! Sisa waktu: " + remainingSeconds + " detik"), true);
                ci.cancel();
                return;
            }
        }

        // Jika mode creative, tidak perlu kunci dan tidak mengurangi stack
        if (player.getAbilities().creativeMode) {
            // Mode creative: tidak perlu kunci, langsung lanjut
            return;
        }

        // Mode survival: cek apakah pemain memiliki minimal 1 kunci
        if (stack.getCount() < 1) {
            playFailedUnlockSound(world, serverData, pos);
            player.sendMessage(Text.of("Anda memerlukan 1 kunci untuk membuka vault!"), true);
            ci.cancel();
            return;
        }

        // Mode survival: gunakan hanya 1 kunci
        stack.decrement(1);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private static void cleanupCooldowns(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, CallbackInfo ci) {
        TrialVaultServerDataAccess serverDataAccess = (TrialVaultServerDataAccess) serverData;
        Object2LongArrayMap<UUID> playerCooldowns = serverDataAccess.trialrestock$getPlayerCooldowns();

        // Hapus cooldown yang sudah berakhir
        playerCooldowns.keySet().removeIf(uuid -> world.getTime() >= playerCooldowns.getLong(uuid));
        serverDataAccess.trialrestock$setPlayerCooldowns(playerCooldowns);
    }

    private static void playFailedUnlockSound(ServerWorld world, VaultServerData serverData, BlockPos pos) {
        TrialVaultServerDataAccess serverDataAccess = (TrialVaultServerDataAccess) serverData;
        if (world.getTime() >= serverDataAccess.trialrestock$getLastFailedUnlockTime() + 15L) {
            world.playSound(null, pos, SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL, SoundCategory.BLOCKS);
            serverDataAccess.trialrestock$setLastFailedUnlockTime(world.getTime());
        }
    }
}