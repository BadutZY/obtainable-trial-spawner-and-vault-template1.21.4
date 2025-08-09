package com.example.trialchamber;

import net.fabricmc.api.ClientModInitializer;

public class TrialChamberItemsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client-side initialization code here
        TrialChamber.LOGGER.info("Craftable Items client initialized!");
    }
}