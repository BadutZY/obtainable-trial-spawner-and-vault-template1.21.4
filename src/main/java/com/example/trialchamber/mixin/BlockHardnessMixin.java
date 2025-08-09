package com.example.trialchamber.mixin;

import com.example.trialchamber.TrialChamber;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class BlockHardnessMixin {
    @Inject(method = "calcBlockBreakingDelta(Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F",
            at = @At("HEAD"),
            cancellable = true)
    private void modifyBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (state.isOf(Blocks.VAULT) || state.isOf(Blocks.TRIAL_SPAWNER)) {
            float hardness = state.isOf(Blocks.VAULT) ? TrialChamber.VAULT_HARDNESS : TrialChamber.TRIAL_SPAWNER_HARDNESS;
            float baseDelta = player.getBlockBreakingSpeed(state) / hardness / (isValidTool(player) ? 5.0F : 100.0F);
            cir.setReturnValue(baseDelta);
        }
    }

    private boolean isValidTool(PlayerEntity player) {
        ItemStack heldItem = player.getMainHandStack();
        return TrialChamber.isValidPickaxe(heldItem);
    }
}