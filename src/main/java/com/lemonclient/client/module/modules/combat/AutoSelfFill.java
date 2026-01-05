package com.lemonclient.client.module.modules.combat;

import com.lemonclient.api.event.events.DeathEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.player.BurrowUtil;
import com.lemonclient.api.util.player.InventoryUtil;
import com.lemonclient.api.util.player.PlayerUtil;
import com.lemonclient.api.util.world.BlockUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "AutoSelfFill", category = Category.Combat)
public class AutoSelfFill extends Module {
   IntegerSetting delay = this.registerInteger("Delay", 10, 0, 50);
   BooleanSetting rotate = this.registerBoolean("Rotate", true);
   BooleanSetting packet = this.registerBoolean("Packet Place", true);
   BooleanSetting packetSwitch = this.registerBoolean("Packet Switch", true);
   BooleanSetting swing = this.registerBoolean("Swing", true);
   BooleanSetting obsidian = this.registerBoolean("Obsidian", true);
   BooleanSetting echest = this.registerBoolean("Ender Chest", true);
   BooleanSetting web = this.registerBoolean("Web", true);
   BooleanSetting skull = this.registerBoolean("Skull", true);
   BooleanSetting plate = this.registerBoolean("Slab", true);
   BooleanSetting upPlate = this.registerBoolean("Up Slab", true);
   BooleanSetting trapdoor = this.registerBoolean("Trapdoor", true);
   int new_slot = -1;
   int waited;
   boolean door;
   boolean block;
   @EventHandler
   private final Listener<DeathEvent> deathEventListener = new Listener<>(event -> {
      if (event.player == mc.player) {
         this.disable();
      }
   });

   @Override
   public void onUpdate() {
      if (this.waited++ >= this.delay.getValue()) {
         this.waited = 0;
         if (BlockUtil.isAir(PlayerUtil.getPlayerPos()) && mc.player.onGround && this.intersectsWithEntity(PlayerUtil.getPlayerPos())) {
            this.placeBlock();
         }
      }
   }

   public void placeBlock() {
      this.new_slot = this.find_in_hotbar();
      if (this.new_slot != -1) {
         InventoryUtil.run(
            this.new_slot,
            this.packetSwitch.getValue(),
            () -> {
               if (this.door) {
                  this.placeTrapdoor();
               } else if (this.upPlate.getValue() && this.new_slot == BurrowUtil.findHotbarBlock(BlockSlab.class)) {
                  this.burrowUp();
               } else if (this.block) {
                  this.burrow();
               } else {
                  BurrowUtil.placeBlock(
                     PlayerUtil.getPlayerPos(), EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue()
                  );
               }
            }
         );
      }
   }

   private int find_in_hotbar() {
      this.door = this.block = false;
      int newHand = -1;
      if (this.trapdoor.getValue()) {
         newHand = BurrowUtil.findHotbarBlock(BlockTrapDoor.class);
         if (newHand != -1) {
            this.door = true;
         }
      }

      if (newHand == -1 && this.skull.getValue()) {
         newHand = InventoryUtil.findSkullSlot();
      }

      if (newHand == -1 && this.web.getValue()) {
         newHand = BurrowUtil.findHotbarBlock(BlockWeb.class);
      }

      if (newHand == -1 && this.plate.getValue()) {
         newHand = BurrowUtil.findHotbarBlock(BlockSlab.class);
      }

      if (newHand == -1 && this.obsidian.getValue()) {
         newHand = BurrowUtil.findHotbarBlock(BlockObsidian.class);
         if (newHand != -1) {
            this.block = true;
         }
      }

      if (newHand == -1 && this.echest.getValue()) {
         newHand = BurrowUtil.findHotbarBlock(BlockEnderChest.class);
         if (newHand != -1) {
            this.block = true;
         }
      }

      return newHand;
   }

   private void placeTrapdoor() {
      BlockPos originalPos = PlayerUtil.getPlayerPos();
      EnumFacing facing = BurrowUtil.getTrapdoorFacing(originalPos);
      if (facing != null) {
         BlockPos neighbour = originalPos.offset(facing);
         EnumFacing opposite = facing.getOpposite();
         double x = mc.player.posX;
         double y = (int)mc.player.posY;
         double z = mc.player.posZ;
         mc.player.connection.sendPacket(new Position(x, y + 0.2F, z, mc.player.onGround));
         BurrowUtil.rightClickBlock(neighbour, opposite, new Vec3d(0.5, 0.8, 0.5), this.packet.getValue(), this.swing.getValue());
         mc.player.connection.sendPacket(new Position(x, y, z, mc.player.onGround));
      }
   }

   private void burrow() {
      BlockPos originalPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));
      BurrowUtil.placeBlock(originalPos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), false, this.swing.getValue());
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, false));
   }

   private void burrowUp() {
      BlockPos originalPos = PlayerUtil.getPlayerPos();
      BlockPos neighbour;
      EnumFacing opposite;
      if (!mc.world.isAirBlock(originalPos.south())) {
         neighbour = originalPos.offset(EnumFacing.SOUTH);
         opposite = EnumFacing.SOUTH.getOpposite();
      } else if (!mc.world.isAirBlock(originalPos.north())) {
         neighbour = originalPos.offset(EnumFacing.NORTH);
         opposite = EnumFacing.NORTH.getOpposite();
      } else if (!mc.world.isAirBlock(originalPos.east())) {
         neighbour = originalPos.offset(EnumFacing.EAST);
         opposite = EnumFacing.EAST.getOpposite();
      } else {
         if (mc.world.isAirBlock(originalPos.west())) {
            return;
         }

         neighbour = originalPos.offset(EnumFacing.WEST);
         opposite = EnumFacing.WEST.getOpposite();
      }

      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));
      BurrowUtil.rightClickBlock(neighbour, opposite, new Vec3d(0.5, 0.8, 0.5), this.packet.getValue(), this.swing.getValue());
      mc.player
         .connection
         .sendPacket(new Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, false));
   }

   private boolean intersectsWithEntity(BlockPos pos) {
      for (Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem) && entity != mc.player && new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }
}
