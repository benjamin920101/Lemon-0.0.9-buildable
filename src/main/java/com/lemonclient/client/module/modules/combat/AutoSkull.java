package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.api.util.world.HoleUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;

@Module.Declaration(name = "AutoSkull", category = Category.Combat)
public class AutoSkull extends Module {
   BooleanSetting moving = this.registerBoolean("Moving", false);
   IntegerSetting delay = this.registerInteger("Delay", 50, 0, 1000);
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting rotate = this.registerBoolean("Rotate", false);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting onlyHoles = this.registerBoolean("Only Holes", false);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting disableAfter = this.registerBoolean("Disable After", true);
   BooleanSetting disable = this.registerBoolean("Auto Disable", true);
   Timing timer = new Timing();
   double y;
   @EventHandler
   private final Listener<InputUpdateEvent> inputUpdateEventListener = new Listener<>(
      event -> {
         if (this.disable.getValue()) {
            if (event.getMovementInput() instanceof MovementInputFromOptions) {
               if (event.getMovementInput().jump) {
                  this.disable();
               }

               if (event.getMovementInput().forwardKeyDown
                  || event.getMovementInput().backKeyDown
                  || event.getMovementInput().leftKeyDown
                  || event.getMovementInput().rightKeyDown) {
                  double posY = mc.player.posY - this.y;
                  if (posY * posY > 0.25) {
                     this.disable();
                  }
               }
            }
         }
      }
   );

   @Override
   public void onEnable() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         this.y = mc.player.posY;
      } else {
         this.disable();
      }
   }

   @Override
   public void fast() {
      if (mc.world != null && mc.player != null && !mc.player.isDead) {
         if (!this.onlyHoles.getValue() || HoleUtil.isInHole(mc.player, true, true, false)) {
            if (this.moving.getValue() || !MotionUtil.isMoving(mc.player)) {
               int slot = InventoryUtil.findSkullSlot();
               if (slot != -1) {
                  BlockPos pos = PlayerUtil.getPlayerPos();
                  if (BurrowUtil.getFirstFacing(pos) != null && BlockUtil.isAir(pos)) {
                     if (this.timer.passedMs(this.delay.getValue().intValue())) {
                        InventoryUtil.run(
                           slot,
                           this.packetSwitch.getValue(),
                           () -> BurrowUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue())
                        );
                        if (this.disableAfter.getValue()) {
                           this.disable();
                        }

                        this.timer.reset();
                     }
                  }
               }
            }
         }
      }
   }
}
