package com.example.trialchamber.mixin;

import com.example.trialchamber.TrialVaultServerDataAccess;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

import static com.example.trialchamber.TrialChamber.CONFIG;

@Mixin(VaultServerData.class)
public class TrialVaultServerDataMixin implements TrialVaultServerDataAccess {

    @Unique
    private Object2LongArrayMap<UUID> trialrestock$playerCooldowns = new Object2LongArrayMap<>();

    @Unique
    private long trialrestock$lastFailedUnlockTime;

    @Override
    public Object2LongArrayMap<UUID> trialrestock$getPlayerCooldowns() {
        return trialrestock$playerCooldowns;
    }

    @Override
    public void trialrestock$setPlayerCooldowns(Object2LongArrayMap<UUID> value) {
        trialrestock$playerCooldowns = value;
    }

    @Override
    public long trialrestock$getLastFailedUnlockTime() {
        return trialrestock$lastFailedUnlockTime;
    }

    @Override
    public void trialrestock$setLastFailedUnlockTime(long time) {
        trialrestock$lastFailedUnlockTime = time;
    }

    // Mencegah pemain ditandai sebagai sudah diberi hadiah
    @Redirect(method = "markPlayerAsRewarded", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    public <E> boolean preventRewardMarking(Set instance, E e) {
        return false;
    }

    // Menambahkan cooldown setelah vault dibuka
    @Inject(method = "markPlayerAsRewarded", at = @At("RETURN"))
    public void addPlayerCooldown(PlayerEntity player, CallbackInfo ci) {
        // Set cooldown berdasarkan konfigurasi
        long cooldownTicks = CONFIG.getCooldownTicks();
        long cooldownEndTime = player.getWorld().getTime() + cooldownTicks;
        trialrestock$playerCooldowns.put(player.getUuid(), cooldownEndTime);
    }
}