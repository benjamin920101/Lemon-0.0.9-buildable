package com.lemonclient.client.module.modules.gui;

import com.lemonclient.api.event.events.EntityUseTotemEvent;
import com.lemonclient.api.event.events.OnUpdateWalkingPlayerEvent;
import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.misc.ColorUtil;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.api.util.world.MotionUtil;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.modules.movement.SpeedPlus;
import com.lemonclient.client.module.modules.qwq.AutoEz;
import com.lemonclient.mixin.mixins.accessor.AccessorCPacketCustomPayload;
import io.netty.buffer.Unpooled;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.lwjgl.opengl.Display;

@Module.Declaration(name = "Colors", category = Category.GUI, enabled = true, drawn = false, priority = 10000)
public class ColorMain extends Module {
   public static ColorMain INSTANCE;
   public ColorSetting enabledColor = this.registerColor("Main Color", new GSColor(255, 0, 0, 255));
   public DoubleSetting rainbowSpeed = this.registerDouble("Rainbow Speed", 1.0, 0.1, 10.0);
   public ModeSetting rainbowMode = this.registerMode("Rainbow Mode", Arrays.asList("Normal", "Sin", "Tan", "Sec", "CoTan", "CoSec"), "Normal");
   public BooleanSetting customFont = this.registerBoolean("Custom Font", true);
   public BooleanSetting textFont = this.registerBoolean("Custom Text", false);
   public BooleanSetting highlightSelf = this.registerBoolean("Highlight SelfName", false);
   public ModeSetting selfColor = this.registerMode("Self Color", ColorUtil.colors, "Blue");
   public ModeSetting friendColor = this.registerMode("Friend Color", ColorUtil.colors, "Green");
   public ModeSetting enemyColor = this.registerMode("Enemy Color", ColorUtil.colors, "Red");
   public ModeSetting chatModuleColor = this.registerMode("Msg Module", ColorUtil.colors, "Aqua");
   public ModeSetting chatEnableColor = this.registerMode("Msg Enable", ColorUtil.colors, "Green");
   public ModeSetting chatDisableColor = this.registerMode("Msg Disable", ColorUtil.colors, "Red");
   public ColorSetting Title = this.registerColor("Title Color", new GSColor(90, 145, 240));
   public ColorSetting Enabled = this.registerColor("Enabled Color", new GSColor(90, 145, 240));
   public ColorSetting Disabled = this.registerColor("Disabled", new GSColor(64, 64, 64));
   public ColorSetting Background = this.registerColor("BackGround Color", new GSColor(195, 195, 195, 150), true);
   public ColorSetting Font = this.registerColor("Font Color", new GSColor(255, 255, 255));
   public ColorSetting ScrollBar = this.registerColor("ScrollBar Color", new GSColor(90, 145, 240));
   public ColorSetting Highlight = this.registerColor("Highlight Color", new GSColor(0, 0, 240));
   public ModeSetting colorModel = this.registerMode("Color Model", Arrays.asList("RGB", "HSB"), "HSB");
   Color title;
   Color enable;
   Color disable;
   Color background;
   Color font;
   Color scrollBar;
   Color highlight;
   public boolean sneaking;
   public double velocityBoost;
   public List<BlockPos> breakList = new ArrayList<>();
   HashMap<EntityPlayer, BlockPos> list = new HashMap<>();
   BlockPos lastBreak;
   @EventHandler
   private final Listener<PacketEvent.PostSend> postSendListener = new Listener<>(event -> {
      if (event.getPacket() instanceof CPacketEntityAction) {
         if (((CPacketEntityAction)event.getPacket()).getAction() == Action.START_SNEAKING) {
            this.sneaking = true;
         }

         if (((CPacketEntityAction)event.getPacket()).getAction() == Action.STOP_SNEAKING) {
            this.sneaking = false;
         }
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Send> packetSend = new Listener<>(event -> {
      if (event.getPacket() instanceof FMLProxyPacket && !mc.isSingleplayer()) {
         event.cancel();
      }

      if (event.getPacket() instanceof CPacketCustomPayload) {
         CPacketCustomPayload packet = (CPacketCustomPayload)event.getPacket();
         if (packet.getChannelName().equalsIgnoreCase("MC|Brand")) {
            ((AccessorCPacketCustomPayload)packet).setData(new PacketBuffer(Unpooled.buffer()).writeString("vanilla"));
         }
      }

      if (event.getPacket() instanceof CPacketPlayerDigging) {
         CPacketPlayerDigging packet = (CPacketPlayerDigging)event.getPacket();
         if (packet.getAction() == net.minecraft.network.play.client.CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            this.lastBreak = packet.getPosition();
         }
      }
   });
   @EventHandler
   private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
      if (mc.world != null && mc.player != null) {
         LemonClient.speedUtil.update();
         LemonClient.positionUtil.updatePosition();
      }
   });
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !EntityUtil.isDead(mc.player)) {
            if (event.getPacket() instanceof SPacketChat) {
               String message = ((SPacketChat)event.getPacket()).getChatComponent().getUnformattedText();
               Matcher matcher = Pattern.compile("<(.*?)>").matcher(message);
               String username = "";
               if (matcher.find()) {
                  username = matcher.group();
               } else if (message.contains(":")) {
                  int spaceIndex = message.indexOf(" ");
                  if (spaceIndex != -1) {
                     username = message.substring(0, spaceIndex);
                  }
               }

               username = cleanColor(username);
               if (SocialManager.isIgnore(username)) {
                  event.cancel();
               }
            }

            if (event.getPacket() instanceof SPacketBlockBreakAnim) {
               SPacketBlockBreakAnim packet = (SPacketBlockBreakAnim)event.getPacket();
               BlockPos blockPos = packet.getPosition();
               EntityPlayer entityPlayer = (EntityPlayer)mc.world.getEntityByID(packet.getBreakerId());
               if (entityPlayer == null) {
                  return;
               }

               this.list.put(entityPlayer, blockPos);
            }

            if (event.getPacket() instanceof SPacketEntityVelocity) {
               SPacketEntityVelocity packet = (SPacketEntityVelocity)event.getPacket();
               Entity entity = mc.world.getEntityByID(packet.entityID);
               if (entity != null && entity == mc.player) {
                  this.velocityBoost = SpeedPlus.INSTANCE.sum.getValue()
                     ? this.velocityBoost + Math.hypot(packet.motionX / 8000.0F, packet.motionZ / 8000.0F)
                     : Math.max(this.velocityBoost, Math.hypot(packet.motionX / 8000.0F, packet.motionZ / 8000.0F));
               }
            }
         }
      }
   );
   @EventHandler
   public Listener<EntityUseTotemEvent> listListener = new Listener<>(event -> {
      if (event.getEntity() == mc.player && mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
         mc.player.closeScreen();
      }
   });

   public ColorMain() {
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      this.enable();
   }

   @Override
   public void fast() {
      if (this.title != this.Title.getColor()
         || this.enable != this.Enabled.getColor()
         || this.disable != this.Disabled.getColor()
         || this.background != this.Background.getColor()
         || this.font != this.Font.getColor()
         || this.scrollBar != this.ScrollBar.getColor()
         || this.highlight != this.Highlight.getColor()) {
         this.title = this.Title.getColor();
         this.enable = this.Enabled.getColor();
         this.disable = this.Disabled.getColor();
         this.background = this.Background.getColor();
         this.font = this.Font.getColor();
         this.scrollBar = this.ScrollBar.getColor();
         this.highlight = this.Highlight.getColor();
         LemonClient.INSTANCE.gameSenseGUI.refresh();
      }

      if (!AutoEz.INSTANCE.hi.getValue()) {
         AutoEz.INSTANCE.hi.setValue(true);
      }

      this.breakList = new ArrayList<>();
      this.breakList.add(this.lastBreak);

      for (EntityPlayer player : mc.world.playerEntities) {
         if (this.list.containsKey(player)) {
            BlockPos pos = this.list.get(player);
            this.breakList.add(pos);
         }
      }
   }

   @Override
   public void onUpdate() {
      if (!Display.getTitle().equals("Lemon Client v0.0.9")) {
         Display.setTitle("Lemon Client v0.0.9");
         LemonClient.setWindowIcon();
      }

      if (!SpeedPlus.INSTANCE.isEnabled() && MotionUtil.moving(mc.player)) {
         this.velocityBoost = 0.0;
      }
   }

   public String highlight(String string) {
      if (string != null && this.isEnabled()) {
         String username = mc.getSession().getUsername();
         return string.replace(username, this.getSelfColor() + username)
            .replace(username.toLowerCase(), this.getSelfColor() + username.toLowerCase())
            .replace(username.toUpperCase(), this.getSelfColor() + username.toUpperCase());
      } else {
         return string;
      }
   }

   public static String cleanColor(String input) {
      return input.replaceAll("(?i)\\u00A7.", "");
   }

   public TextFormatting getSelfColor() {
      return ColorUtil.settingToTextFormatting(this.selfColor);
   }

   public TextFormatting getFriendColor() {
      return ColorUtil.settingToTextFormatting(this.friendColor);
   }

   public TextFormatting getEnemyColor() {
      return ColorUtil.settingToTextFormatting(this.enemyColor);
   }

   public TextFormatting getModuleColor() {
      return ColorUtil.settingToTextFormatting(this.chatModuleColor);
   }

   public TextFormatting getEnabledColor() {
      return ColorUtil.settingToTextFormatting(this.chatEnableColor);
   }

   public TextFormatting getDisabledColor() {
      return ColorUtil.settingToTextFormatting(this.chatDisableColor);
   }

   public GSColor getFriendGSColor() {
      return new GSColor(ColorUtil.settingToColor(this.friendColor));
   }

   public GSColor getEnemyGSColor() {
      return new GSColor(ColorUtil.settingToColor(this.enemyColor));
   }
}
