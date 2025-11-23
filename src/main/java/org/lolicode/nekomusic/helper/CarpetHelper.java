// Taken from https://github.com/NikitaCartes/EasyAuth
package org.lolicode.nekomusic.helper;

import carpet.patches.EntityPlayerMPFake;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

public class CarpetHelper {
    /**
     * Checks if player is actually a fake one.
     *
     * @param player player to check
     * @return true if it's fake, otherwise false
     */
    public static boolean isPlayerFake(Player player) {
        if (FabricLoader.getInstance().isModLoaded("carpet")) {
            return player instanceof EntityPlayerMPFake;
        } else return false;
    }
}
