package com.pikacnu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.PlayerManager;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
  @Inject(method = "broadcast(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
  public void boardcast(net.minecraft.text.Text message, boolean actionBar, CallbackInfo ci) {
    if (message.toString().contains("joined the game") || message.toString().contains("left the game")) {
      ci.cancel();
    }
  }
}
