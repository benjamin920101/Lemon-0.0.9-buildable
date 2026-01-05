package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "AntiContainer", category = Category.Misc)
public class AntiContainer extends Module {
   BooleanSetting Chest = this.registerBoolean("Chest", true);
   BooleanSetting EnderChest = this.registerBoolean("EnderChest", true);
   BooleanSetting Trapped_Chest = this.registerBoolean("Trapped_Chest", true);
   BooleanSetting Hopper = this.registerBoolean("Hopper", true);
   BooleanSetting Dispenser = this.registerBoolean("Dispenser", true);
   BooleanSetting Furnace = this.registerBoolean("Furnace", true);
   BooleanSetting Beacon = this.registerBoolean("Beacon", true);
   BooleanSetting Crafting_Table = this.registerBoolean("Crafting_Table", true);
   BooleanSetting Anvil = this.registerBoolean("Anvil", true);
   BooleanSetting Enchanting_table = this.registerBoolean("Enchanting_table", true);
   BooleanSetting Brewing_Stand = this.registerBoolean("Brewing_Stand", true);
   BooleanSetting ShulkerBox = this.registerBoolean("ShulkerBox", true);
   @EventHandler
   private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
      if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
         BlockPos pos = ((CPacketPlayerTryUseItemOnBlock)event.getPacket()).getPos();
         if (this.check(pos)) {
            event.cancel();
         }
      }
   });

   public boolean check(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() == Blocks.CHEST && this.Chest.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.ENDER_CHEST && this.EnderChest.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.TRAPPED_CHEST && this.Trapped_Chest.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.HOPPER && this.Hopper.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.DISPENSER && this.Dispenser.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.FURNACE && this.Furnace.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.BEACON && this.Beacon.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE && this.Crafting_Table.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.ANVIL && this.Anvil.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.ENCHANTING_TABLE && this.Enchanting_table.getValue()
         || mc.world.getBlockState(pos).getBlock() == Blocks.BREWING_STAND && this.Brewing_Stand.getValue()
         || mc.world.getBlockState(pos).getBlock() instanceof BlockShulkerBox && this.ShulkerBox.getValue();
   }
}
