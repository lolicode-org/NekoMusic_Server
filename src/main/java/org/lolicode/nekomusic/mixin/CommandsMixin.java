package org.lolicode.nekomusic.mixin;

import net.minecraft.commands.CommandSourceStack;
import org.lolicode.nekomusic.command.MusicCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.commands.Commands$1")
public class CommandsMixin {
    @Shadow
    @Final
    private CommandSourceStack noPermissionSource;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        MusicCommand.setNoPermissionSource(noPermissionSource);
    }
}
