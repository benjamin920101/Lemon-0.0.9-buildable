package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.ColorUtil;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.client.manager.managers.TotemPopManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagList;

@Module.Declaration(name = "PvPInfo", category = Category.Misc)
public class PvPInfo extends Module {
   BooleanSetting visualRange = this.registerBoolean("Visual Range", false);
   BooleanSetting coords = this.registerBoolean("Coords", true, () -> this.visualRange.getValue());
   BooleanSetting pearlAlert = this.registerBoolean("Pearl Alert", false);
   BooleanSetting strengthDetect = this.registerBoolean("Strength Detect", false);
   BooleanSetting weaknessDetect = this.registerBoolean("Weakness Detect", false);
   BooleanSetting popCounter = this.registerBoolean("Pop Counter", false);
   BooleanSetting friend = this.registerBoolean("My Friend", false);
   BooleanSetting sharp32 = this.registerBoolean("sharp32", true);
   ModeSetting type = this.registerMode("Visual Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting type1 = this.registerMode("Pearl Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting type2 = this.registerMode("Strength Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting type3 = this.registerMode("Weakness Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting type4 = this.registerMode("Pop Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting type5 = this.registerMode("32k Type", Arrays.asList("Friend", "Enemy", "All"), "All");
   ModeSetting self = this.registerMode("Self", Arrays.asList("I", "Name", "Disable"), "Name");
   ModeSetting chatColor = this.registerMode("Color", ColorUtil.colors, "Light Purple");
   ModeSetting nameColor = this.registerMode("Name Color", ColorUtil.colors, "Light Purple");
   ModeSetting friColor = this.registerMode("Friend Color", ColorUtil.colors, "Light Purple");
   ModeSetting numberColor = this.registerMode("Number Color", ColorUtil.colors, "Light Purple");
   List<Entity> knownPlayers = new ArrayList<>();
   List<Entity> antiPearlList = new ArrayList<>();
   List<Entity> players;
   List<Entity> pearls;
   private final Set<EntityPlayer> strengthPlayers = Collections.newSetFromMap(new WeakHashMap<>());
   private final Set<EntityPlayer> weaknessPlayers = Collections.newSetFromMap(new WeakHashMap<>());
   private final Set<EntityPlayer> sword = Collections.newSetFromMap(new WeakHashMap<>());

   @Override
   public void onUpdate() {
      if (mc.player != null && mc.world != null) {
         TotemPopManager.INSTANCE.sendMsgs = this.popCounter.getValue();
         if (this.popCounter.getValue()) {
            TotemPopManager.INSTANCE.chatFormatting = ColorUtil.textToChatFormatting(this.chatColor);
            TotemPopManager.INSTANCE.nameFormatting = ColorUtil.textToChatFormatting(this.nameColor);
            TotemPopManager.INSTANCE.friFormatting = ColorUtil.textToChatFormatting(this.friColor);
            TotemPopManager.INSTANCE.numberFormatting = ColorUtil.textToChatFormatting(this.numberColor);
            TotemPopManager.INSTANCE.friend = this.friend.getValue();
            TotemPopManager.INSTANCE.self = this.self.getValue();
            TotemPopManager.INSTANCE.type4 = this.type4.getValue();
         }

         if (this.visualRange.getValue()) {
            this.players = mc.world
               .playerEntities
               .stream()
               .filter(entity -> !entity.getName().equals(mc.player.getName()))
               .collect(Collectors.toList());

            try {
               for (Entity e : this.players) {
                  if (!e.getName().equalsIgnoreCase("fakeplayer") && !this.knownPlayers.contains(e)) {
                     this.knownPlayers.add(e);
                     String xyz = this.coords.getValue() ? " at x:" + (int)e.posX + " y:" + (int)e.posY + " z:" + (int)e.posZ : "";
                     String name = e.getName();
                     if (name.equals("") || name.equals(" ")) {
                        return;
                     }

                     if (name.equals("I") || SocialManager.isFriend(name) && !this.type.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.chatColor)
                              + "Found ("
                              + ColorUtil.textToChatFormatting(this.friColor)
                              + name
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + ")"
                              + xyz,
                           Notification.Type.INFO,
                           "VisualRange" + name,
                           2000
                        );
                     }

                     if (!name.equals("I") && !SocialManager.isFriend(name) && !this.type.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.chatColor)
                              + "Found ("
                              + ColorUtil.textToChatFormatting(this.nameColor)
                              + name
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + ")"
                              + xyz,
                           Notification.Type.INFO,
                           "VisualRange" + name,
                           2000
                        );
                     }
                  }
               }
            } catch (Exception var7) {
            }

            try {
               for (Entity ex : this.knownPlayers) {
                  if (!ex.getName().equalsIgnoreCase("fakeplayer") && !this.players.contains(ex)) {
                     this.knownPlayers.remove(ex);
                     String xyzx = this.coords.getValue()
                        ? " at x:" + (int)ex.posX + " y:" + (int)ex.posY + " z:" + (int)ex.posZ
                        : "";
                     String namex = ex.getName();
                     if (namex.equals("") || namex.equals(" ")) {
                        return;
                     }

                     if (namex.equals("I") || SocialManager.isFriend(namex) && !this.type.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.chatColor)
                              + "Gone ("
                              + ColorUtil.textToChatFormatting(this.friColor)
                              + namex
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + ")"
                              + xyzx,
                           Notification.Type.INFO,
                           "VisualRange" + namex,
                           2000
                        );
                     }

                     if (!namex.equals("I") && !SocialManager.isFriend(namex) && !this.type.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.chatColor)
                              + "Gone ("
                              + ColorUtil.textToChatFormatting(this.nameColor)
                              + namex
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + ")"
                              + xyzx,
                           Notification.Type.INFO,
                           "VisualRange" + namex,
                           2000
                        );
                     }
                  }
               }
            } catch (Exception var6) {
            }
         }

         if (this.pearlAlert.getValue()) {
            this.pearls = mc.world.loadedEntityList.stream().filter(exx -> exx instanceof EntityEnderPearl).collect(Collectors.toList());

            try {
               for (Entity exx : this.pearls) {
                  if (exx instanceof EntityEnderPearl
                     && !exx.getEntityWorld().getClosestPlayerToEntity(exx, 3.0).getName().equalsIgnoreCase("fakeplayer")
                     && !this.antiPearlList.contains(exx)) {
                     this.antiPearlList.add(exx);
                     String faceing = exx.getHorizontalFacing().toString();
                     if (faceing.equals("west")) {
                        faceing = "east";
                     } else if (faceing.equals("east")) {
                        faceing = "west";
                     }

                     if (mc.player.getName().equals(exx.getEntityWorld().getClosestPlayerToEntity(exx, 3.0).getName())
                        && this.self.getValue().equals("Disable")) {
                        return;
                     }

                     String namexx = exx.getEntityWorld().getClosestPlayerToEntity(exx, 3.0).getName().equals(mc.player.getName())
                           && this.self.getValue().equals("I")
                        ? "I"
                        : exx.getEntityWorld().getClosestPlayerToEntity(exx, 3.0).getName();
                     if (namexx.equals("") || namexx.equals(" ")) {
                        return;
                     }

                     if (namexx.equals("I") || SocialManager.isFriend(namexx) && !this.type1.getValue().equals("Enemy")) {
                        MessageBus.sendClientPrefixMessage(
                           ColorUtil.textToChatFormatting(this.friColor)
                              + namexx
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " has just thrown a pearl! ("
                              + faceing
                              + ")",
                           Notification.Type.INFO
                        );
                     }

                     if (!namexx.equals("I") && !SocialManager.isFriend(namexx) && !this.type1.getValue().equals("Friend")) {
                        MessageBus.sendClientPrefixMessage(
                           ColorUtil.textToChatFormatting(this.nameColor)
                              + namexx
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " has just thrown a pearl! ("
                              + faceing
                              + ")",
                           Notification.Type.INFO
                        );
                     }
                  }
               }
            } catch (Exception var5) {
            }
         }

         if (this.strengthDetect.getValue()) {
            for (EntityPlayer player : mc.world.playerEntities) {
               if (!player.getName().equalsIgnoreCase("fakeplayer")) {
                  if (player.isPotionActive(MobEffects.STRENGTH) && !this.strengthPlayers.contains(player)) {
                     if (mc.player.getName().equals(player.getName()) && this.self.getValue().equals("Disable")) {
                        return;
                     }

                     String namexxx = player.getName().equals(mc.player.getName()) && this.self.getValue().equals("I")
                        ? "I"
                        : player.getName();
                     if (namexxx.equals("") || namexxx.equals(" ")) {
                        return;
                     }

                     if (namexxx.equals("I") || SocialManager.isFriend(namexxx) && !this.type2.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.friColor) + namexxx + ColorUtil.textToChatFormatting(this.chatColor) + " has drank strength",
                           Notification.Type.INFO,
                           "Strength" + namexxx,
                           2000
                        );
                     }

                     if (!namexxx.equals("I") && !SocialManager.isFriend(namexxx) && !this.type2.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor) + namexxx + ChatFormatting.RED + " has drank strength",
                           Notification.Type.INFO,
                           "Strength" + namexxx,
                           2000
                        );
                     }

                     this.strengthPlayers.add(player);
                  }

                  if (this.strengthPlayers.contains(player) && !player.isPotionActive(MobEffects.STRENGTH)) {
                     if (mc.player.getName().equals(player.getName()) && this.self.getValue().equals("Disable")) {
                        return;
                     }

                     String namexxxx = player.getName().equals(mc.player.getName()) && this.self.getValue().equals("I")
                        ? "I"
                        : player.getName();
                     if (namexxxx.equals("") || namexxxx.equals(" ")) {
                        return;
                     }

                     if (namexxxx.equals("I") || SocialManager.isFriend(namexxxx) && !this.type2.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.friColor)
                              + namexxxx
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " no longer has strength",
                           Notification.Type.INFO,
                           "Strength" + namexxxx,
                           2000
                        );
                     }

                     if (!namexxxx.equals("I") && !SocialManager.isFriend(namexxxx) && !this.type2.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor) + namexxxx + ChatFormatting.GREEN + " no longer has strength",
                           Notification.Type.INFO,
                           "Strength" + namexxxx,
                           2000
                        );
                     }

                     this.strengthPlayers.remove(player);
                  }
               }
            }
         }

         if (this.weaknessDetect.getValue()) {
            for (EntityPlayer playerx : mc.world.playerEntities) {
               if (!playerx.getName().equalsIgnoreCase("FakePlayer")) {
                  if (playerx.isPotionActive(MobEffects.WEAKNESS) && !this.weaknessPlayers.contains(playerx)) {
                     if (mc.player.getName().equals(playerx.getName()) && this.self.getValue().equals("Disable")) {
                        return;
                     }

                     String namexxxxx = playerx.getName().equals(mc.player.getName()) && this.self.getValue().equals("I")
                        ? "I"
                        : playerx.getName();
                     if (namexxxxx.isEmpty() || namexxxxx.equals(" ")) {
                        return;
                     }

                     if (namexxxxx.equals("I") || SocialManager.isFriend(namexxxxx) && !this.type3.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.friColor) + namexxxxx + ColorUtil.textToChatFormatting(this.chatColor) + " has drank weekness",
                           Notification.Type.INFO,
                           "Weakness" + namexxxxx,
                           2000
                        );
                     }

                     if (!namexxxxx.equals("I") && !SocialManager.isFriend(namexxxxx) && !this.type3.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor) + namexxxxx + ChatFormatting.GREEN + " has drank weekness",
                           Notification.Type.INFO,
                           "Weakness" + namexxxxx,
                           2000
                        );
                     }

                     this.weaknessPlayers.add(playerx);
                  }

                  if (this.weaknessPlayers.contains(playerx) && !playerx.isPotionActive(MobEffects.WEAKNESS)) {
                     if (mc.player.getName().equals(playerx.getName()) && this.self.getValue().equals("Disable")) {
                        return;
                     }

                     String namexxxxxx = playerx.getName().equals(mc.player.getName()) && this.self.getValue().equals("I")
                        ? "I"
                        : playerx.getName();
                     if (namexxxxxx.equals("") || namexxxxxx.equals(" ")) {
                        return;
                     }

                     if (namexxxxxx.equals("I") || SocialManager.isFriend(namexxxxxx) && !this.type3.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.friColor)
                              + namexxxxxx
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " no longer has weekness",
                           Notification.Type.INFO,
                           "Weakness" + namexxxxxx,
                           2000
                        );
                     }

                     if (!namexxxxxx.equals("I") && !SocialManager.isFriend(namexxxxxx) && !this.type3.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor) + namexxxxxx + ChatFormatting.RED + " no longer has weekness",
                           Notification.Type.INFO,
                           "Weakness" + namexxxxxx,
                           2000
                        );
                     }

                     this.weaknessPlayers.remove(playerx);
                  }
               }
            }
         }

         if (this.sharp32.getValue()) {
            for (EntityPlayer playerxx : mc.world.playerEntities) {
               if (!playerxx.getName().equalsIgnoreCase("fakeplayer") && !playerxx.getName().equals(mc.player.getName())) {
                  if (this.is32k(playerxx.itemStackMainHand) && !this.sword.contains(playerxx)) {
                     String namexxxxxxx = playerxx.getName();
                     if (namexxxxxxx.equals("") || namexxxxxxx.equals(" ")) {
                        return;
                     }

                     if (namexxxxxxx.equals("I") || SocialManager.isFriend(namexxxxxxx) && !this.type5.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor)
                              + namexxxxxxx
                              + " is "
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + "holding a 32k",
                           Notification.Type.INFO,
                           "32k" + namexxxxxxx,
                           2000
                        );
                     }

                     if (!namexxxxxxx.equals("I") && !SocialManager.isFriend(namexxxxxxx) && !this.type5.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor)
                              + namexxxxxxx
                              + " is "
                              + ChatFormatting.RED
                              + "holding"
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " a 32k",
                           Notification.Type.INFO,
                           "32k" + namexxxxxxx,
                           2000
                        );
                     }

                     this.sword.add(playerxx);
                  }

                  if (this.sword.contains(playerxx) && !this.is32k(playerxx.itemStackMainHand)) {
                     String namexxxxxxxx = playerxx.getName();
                     if (namexxxxxxxx.equals("") || namexxxxxxxx.equals(" ")) {
                        return;
                     }

                     if (namexxxxxxxx.equals("I") || SocialManager.isFriend(namexxxxxxxx) && !this.type5.getValue().equals("Enemy")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.friColor)
                              + namexxxxxxxx
                              + " is "
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + "no longer holding a 32k",
                           Notification.Type.INFO,
                           "32k" + namexxxxxxxx,
                           2000
                        );
                     }

                     if (!namexxxxxxxx.equals("I") && !SocialManager.isFriend(namexxxxxxxx) && !this.type5.getValue().equals("Friend")) {
                        MessageBus.sendClientDeleteMessage(
                           ColorUtil.textToChatFormatting(this.nameColor)
                              + namexxxxxxxx
                              + " is "
                              + ChatFormatting.GREEN
                              + "no longer holding"
                              + ColorUtil.textToChatFormatting(this.chatColor)
                              + " a 32k",
                           Notification.Type.INFO,
                           "32k" + namexxxxxxxx,
                           2000
                        );
                     }

                     this.sword.remove(playerxx);
                  }
               }
            }
         }
      }
   }

   private boolean is32k(ItemStack stack) {
      if (stack.getItem() instanceof ItemSword) {
         NBTTagList enchants = stack.getEnchantmentTagList();

         for (int i = 0; i < enchants.tagCount(); i++) {
            if (enchants.getCompoundTagAt(i).getShort("lvl") >= 1000) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void onDisable() {
      this.knownPlayers.clear();
      TotemPopManager.INSTANCE.sendMsgs = false;
   }
}
