package com.example.trialchamber.mixin;

import net.minecraft.block.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VaultServerData.class)
public interface TrialVaultServerDataAccessor {

    @Invoker("getLastFailedUnlockTime")
    public long trialrestock$getLastFailedUnlockTime();
    @Invoker("setLastFailedUnlockTime")
    public void trialrestock$setLastFailedUnlockTime(long lastFailedUnlockTime);

}
