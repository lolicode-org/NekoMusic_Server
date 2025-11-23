package org.lolicode.nekomusic.mixin;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.lolicode.nekomusic.event.PlayerLeaveCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(at = @At(value = "TAIL"), method = "onDisconnect")
    private void onPlayerLeave(DisconnectionDetails info, CallbackInfo ci) {
        PlayerLeaveCallback.EVENT.invoker().leaveServer(this.player, this.player.level().getServer());
    }
}
