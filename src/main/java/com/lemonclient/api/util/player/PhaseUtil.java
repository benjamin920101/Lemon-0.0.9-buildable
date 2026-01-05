package com.lemonclient.api.util.player;

import com.lemonclient.api.util.world.MotionUtil;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;

public class PhaseUtil {
   public static List<String> bound = Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward", "Flat", "LimitJitter", "Constrict", "None");
   public static String normal = "Forward";
   private static final Minecraft mc = Minecraft.getMinecraft();

   public static CPacketPlayer doBounds(String mode, boolean send) {
      CPacketPlayer packet = new PositionRotation(0.0, 0.0, 0.0, 0.0F, 0.0F, false);
      switch (mode) {
         case "Up":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY + 69420.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Down":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY - 69420.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Zero":
            packet = new PositionRotation(
               mc.player.posX, 0.0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false
            );
            break;
         case "Min":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY + 100.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Alternate":
            if (mc.player.ticksExisted % 2 == 0) {
               packet = new PositionRotation(
                  mc.player.posX,
                  mc.player.posY + 69420.0,
                  mc.player.posZ,
                  mc.player.rotationYaw,
                  mc.player.rotationPitch,
                  false
               );
            } else {
               packet = new PositionRotation(
                  mc.player.posX,
                  mc.player.posY - 69420.0,
                  mc.player.posZ,
                  mc.player.rotationYaw,
                  mc.player.rotationPitch,
                  false
               );
            }
            break;
         case "Forward": {
            double[] dir = MotionUtil.forward(67.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY + 33.4,
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         }
         case "Flat": {
            double[] dir = MotionUtil.forward(100.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY,
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         }
         case "Constrict": {
            double[] dir = MotionUtil.forward(67.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY + (mc.player.posY > 64.0 ? -33.4 : 33.4),
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
         }
      }

      mc.player.connection.sendPacket(packet);
      return packet;
   }

   public static CPacketPlayer doBounds(String mode, int c) {
      CPacketPlayer packet = new PositionRotation(0.0, 0.0, 0.0, 0.0F, 0.0F, false);
      switch (mode) {
         case "Up":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY + 69420.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Down":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY - 69420.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Zero":
            packet = new PositionRotation(
               mc.player.posX, 0.0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false
            );
            break;
         case "Min":
            packet = new PositionRotation(
               mc.player.posX,
               mc.player.posY + 100.0,
               mc.player.posZ,
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         case "Alternate":
            if (mc.player.ticksExisted % 2 == 0) {
               packet = new PositionRotation(
                  mc.player.posX,
                  mc.player.posY + 69420.0,
                  mc.player.posZ,
                  mc.player.rotationYaw,
                  mc.player.rotationPitch,
                  false
               );
            } else {
               packet = new PositionRotation(
                  mc.player.posX,
                  mc.player.posY - 69420.0,
                  mc.player.posZ,
                  mc.player.rotationYaw,
                  mc.player.rotationPitch,
                  false
               );
            }
            break;
         case "Forward": {
            double[] dir = MotionUtil.forward(67.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY + 33.4,
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         }
         case "Flat": {
            double[] dir = MotionUtil.forward(100.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY,
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
            break;
         }
         case "Constrict": {
            double[] dir = MotionUtil.forward(67.0);
            packet = new PositionRotation(
               mc.player.posX + dir[0],
               mc.player.posY + (mc.player.posY > 64.0 ? -33.4 : 33.4),
               mc.player.posZ + dir[1],
               mc.player.rotationYaw,
               mc.player.rotationPitch,
               false
            );
         }
      }

      for (int i = 1; i < c; i++) {
         mc.player.connection.sendPacket(packet);
      }

      return packet;
   }
}
