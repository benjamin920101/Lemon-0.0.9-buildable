package com.lemonclient.client.module.modules.movement;

import com.lemonclient.api.event.events.PlayerMoveEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.player.PlacementUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import java.util.Arrays;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AntiVoid", category = Category.Movement)
public class AntiVoid extends Module {
   ModeSetting mode = this.registerMode("Mode", Arrays.asList("Freeze", "Glitch", "Catch"), "Freeze");
   DoubleSetting height = this.registerDouble("Height", 2.0, 0.0, 5.0);
   BooleanSetting chorus = this.registerBoolean("Chorus", false, () -> this.mode.getValue().equals("Freeze"));
   BooleanSetting packetFly = this.registerBoolean("PacketFly", false, () -> this.mode.getValue().equals("Catch"));
   boolean chorused;
   @EventHandler
   private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(
      event -> {
         try {
            if (mc.player.posY < this.height.getValue() + 0.1
               && this.mode.getValue().equalsIgnoreCase("Freeze")
               && mc.world
                  .getBlockState(new BlockPos(mc.player.posX, 0.0, mc.player.posZ))
                  .getMaterial()
                  .isReplaceable()) {
               String var2 = this.mode.getValue();
               switch (var2) {
                  case "Freeze":
                     mc.player.posY = this.height.getValue();
                     event.setY(0.0);
                     if (mc.player.getRidingEntity() != null) {
                        mc.player.ridingEntity.setVelocity(0.0, 0.0, 0.0);
                     }

                     if (this.chorus.getValue()) {
                        int newSlot = -1;

                        for (int ix = 0; ix < 9; ix++) {
                           ItemStack stack = mc.player.inventory.getStackInSlot(ix);
                           if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemChorusFruit) {
                              newSlot = ix;
                              break;
                           }
                        }

                        if (newSlot == -1) {
                           newSlot = 1;
                           MessageBus.sendClientPrefixMessage(
                              ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of chorus!", Notification.Type.ERROR
                           );
                           this.chorused = false;
                        } else {
                           this.chorused = true;
                        }

                        if (this.chorused) {
                           mc.player.inventory.currentItem = newSlot;
                           if (mc.player.canEat(true)) {
                              mc.player.setActiveHand(EnumHand.MAIN_HAND);
                           }
                        }
                     }
                     break;
                  case "Glitch":
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + 69.0,
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                     break;
                  case "Catch":
                     int oldSlot = mc.player.inventory.currentItem;
                     int newSlot = -1;

                     for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                        if (stack != ItemStack.EMPTY
                           && stack.getItem() instanceof ItemBlock
                           && Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock()
                           && !(((ItemBlock)stack.getItem()).getBlock() instanceof BlockFalling)) {
                           newSlot = i;
                           break;
                        }
                     }

                     if (newSlot == -1) {
                        newSlot = 1;
                        MessageBus.sendClientPrefixMessage(
                           ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of valid blocks. Disabling!", Notification.Type.DISABLE
                        );
                        this.disable();
                     }

                     mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
                     PlacementUtil.place(new BlockPos(mc.player.posX, 0.0, mc.player.posZ), EnumHand.MAIN_HAND, true);
                     if (mc.world
                           .getBlockState(new BlockPos(mc.player.posX, 0.0, mc.player.posZ))
                           .getMaterial()
                           .isReplaceable()
                        && this.packetFly.getValue()) {
                        mc.player
                           .connection
                           .sendPacket(
                              new PositionRotation(
                                 mc.player.posX + mc.player.motionX,
                                 mc.player.posY + 0.0624,
                                 mc.player.posZ + mc.player.motionZ,
                                 mc.player.rotationYaw,
                                 mc.player.rotationPitch,
                                 false
                              )
                           );
                        mc.player
                           .connection
                           .sendPacket(
                              new PositionRotation(
                                 mc.player.posX,
                                 mc.player.posY + 69420.0,
                                 mc.player.posZ,
                                 mc.player.rotationYaw,
                                 mc.player.rotationPitch,
                                 false
                              )
                           );
                     }

                     mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
               }
            }
         } catch (Exception var8) {
         }
      }
   );
}
