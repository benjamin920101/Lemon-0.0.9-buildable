package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.PlayerJoinEvent;
import com.lemonclient.api.event.events.PlayerLeaveEvent;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.misc.Timer;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.RenderUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;

@Module.Declaration(name = "LogoutSpots", category = Category.Render)
public class LogoutSpots extends Module {
   IntegerSetting range = this.registerInteger("Range", 100, 10, 260);
   BooleanSetting disconnectMsg = this.registerBoolean("Disconnect Msgs", true);
   BooleanSetting reconnectMsg = this.registerBoolean("Reconnect Msgs", true);
   BooleanSetting nameTag = this.registerBoolean("NameTag", true);
   IntegerSetting lineWidth = this.registerInteger("Width", 1, 1, 10);
   ModeSetting renderMode = this.registerMode("Render", Arrays.asList("Both", "Outline", "Fill", "None"), "Both");
   ColorSetting color = this.registerColor("Color", new GSColor(255, 0, 0, 255));
   Map<Entity, String> loggedPlayers = new ConcurrentHashMap<>();
   Set<EntityPlayer> worldPlayers = ConcurrentHashMap.newKeySet();
   Timer timer = new Timer();
   Timer timer2 = new Timer();
   @EventHandler
   private final Listener<PlayerJoinEvent> playerJoinEventListener = new Listener<>(event -> {
      if (mc.world != null) {
         this.loggedPlayers.keySet().removeIf(entity -> {
            if (entity.getName().equalsIgnoreCase(event.getName())) {
               if (this.reconnectMsg.getValue() && this.timer2.getTimePassed() / 50L >= 5L) {
                  MessageBus.sendClientPrefixMessage(event.getName() + " reconnected.", Notification.Type.INFO);
                  this.timer2.reset();
               }

               return true;
            } else {
               return false;
            }
         });
      }
   });
   @EventHandler
   private final Listener<PlayerLeaveEvent> playerLeaveEventListener = new Listener<>(event -> {
      if (mc.world != null) {
         this.worldPlayers.removeIf(entity -> {
            if (entity.getName().equalsIgnoreCase(event.getName())) {
               String date = new SimpleDateFormat("k:mm").format(new Date());
               this.loggedPlayers.put(entity, date);
               if (this.disconnectMsg.getValue() && this.timer.getTimePassed() / 50L >= 5L) {
                  String location = "(" + (int)entity.posX + "," + (int)entity.posY + "," + (int)entity.posZ + ")";
                  MessageBus.sendClientPrefixMessage(event.getName() + " disconnected at " + location + ".", Notification.Type.INFO);
                  this.timer.reset();
               }

               return true;
            } else {
               return false;
            }
         });
      }
   });
   @EventHandler
   private final Listener<Unload> unloadListener = new Listener<>(event -> {
      this.worldPlayers.clear();
      if (mc.player == null || mc.world == null) {
         this.loggedPlayers.clear();
      }
   });
   @EventHandler
   private final Listener<Load> loadListener = new Listener<>(event -> {
      this.worldPlayers.clear();
      if (mc.player == null || mc.world == null) {
         this.loggedPlayers.clear();
      }
   });

   @Override
   public void onUpdate() {
      mc.world
         .playerEntities
         .stream()
         .filter(entityPlayer -> entityPlayer != mc.player)
         .filter(entityPlayer -> entityPlayer.getDistance(mc.player) <= this.range.getValue().intValue())
         .forEach(entityPlayer -> this.worldPlayers.add(entityPlayer));
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      if (mc.player != null && mc.world != null) {
         this.loggedPlayers.forEach(this::startFunction);
      }
   }

   @Override
   public void onEnable() {
      this.loggedPlayers.clear();
      this.worldPlayers = ConcurrentHashMap.newKeySet();
   }

   @Override
   public void onDisable() {
      this.worldPlayers.clear();
   }

   private void startFunction(Entity entity, String string) {
      if (!(entity.getDistance(mc.player) > this.range.getValue().intValue())) {
         int posX = (int)entity.posX;
         int posY = (int)entity.posY;
         int posZ = (int)entity.posZ;
         String[] nameTagMessage = new String[]{entity.getName() + " (" + string + ")", "(" + posX + "," + posY + "," + posZ + ")"};
         GlStateManager.pushMatrix();
         if (this.nameTag.getValue()) {
            RenderUtil.drawNametag(entity, nameTagMessage, this.color.getValue(), 0);
         }

         String var7 = this.renderMode.getValue();
         switch (var7) {
            case "Both":
               RenderUtil.drawBoundingBox(entity.getRenderBoundingBox(), this.lineWidth.getValue().intValue(), this.color.getValue());
               RenderUtil.drawBox(entity.getRenderBoundingBox(), true, -0.4, new GSColor(this.color.getValue(), 50), 63);
               break;
            case "Outline":
               RenderUtil.drawBoundingBox(entity.getRenderBoundingBox(), this.lineWidth.getValue().intValue(), this.color.getValue());
               break;
            case "Fill":
               RenderUtil.drawBox(entity.getRenderBoundingBox(), true, -0.4, new GSColor(this.color.getValue(), 50), 63);
         }

         GlStateManager.popMatrix();
      }
   }
}
