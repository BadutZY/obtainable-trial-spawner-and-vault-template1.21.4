package com.example.trialchamber;

public class TrialRestockConfigModel {

    public int cooldownSeconds = 60; // Default 60 detik (1 menit)

    // Method untuk mendapatkan cooldown dalam ticks (20 ticks = 1 detik)
    public int getCooldownTicks() {
        return cooldownSeconds * 20;
    }

    // Method untuk validasi nilai
    public void setCooldownSeconds(int seconds) {
        if (seconds >= 1 && seconds <= 3600) {
            this.cooldownSeconds = seconds;
        }
    }
}