package com.example.trialchamber;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;

import java.util.UUID;

public interface TrialVaultServerDataAccess {

    Object2LongArrayMap<UUID> trialrestock$getPlayerCooldowns();
    void trialrestock$setPlayerCooldowns(Object2LongArrayMap<UUID> value);
    long trialrestock$getLastFailedUnlockTime();
    void trialrestock$setLastFailedUnlockTime(long time);
}