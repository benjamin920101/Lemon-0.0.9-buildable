package com.lemonclient.client.module.modules.qwq;

import com.lemonclient.api.event.events.TotemPopEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.misc.Timing;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.api.util.world.EntityUtil;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;

@Module.Declaration(name = "ChatLag", category = Category.qwq)
public class ChatLag extends Module {
   IntegerSetting maxPlayer = this.registerInteger("Max Player", 1, 0, 10);
   IntegerSetting range = this.registerInteger("Range", 16, 0, 256);
   BooleanSetting time = this.registerBoolean("Timing", true);
   IntegerSetting timingDelay = this.registerInteger("Timing Delay(10s)", 12, 0, 60, () -> this.time.getValue());
   IntegerSetting timingDelay2 = this.registerInteger("Timing Delay(s)", 0, 0, 10, () -> this.time.getValue());
   BooleanSetting pop = this.registerBoolean("After Pop", true);
   IntegerSetting blank = this.registerInteger("Pop Blank(s)", 5, 0, 60, () -> this.pop.getValue());
   IntegerSetting sendDelay = this.registerInteger("Send Delay(ms)", 0, 0, 1000, () -> this.pop.getValue());
   boolean popped;
   int sent;
   Timing timing = new Timing();
   Timing popTiming = new Timing();
   Timing sendTiming = new Timing();
   protected static final String LAG_MESSAGE = "āȁ́Ё\u0601܁ࠁँਁଁก༁ခᄁሁጁᐁᔁᘁᜁ᠁ᤁᨁᬁᰁᴁḁἁ ℁∁⌁␁━✁⠁⤁⨁⬁Ⰱⴁ⸁⼁、\u3101㈁㌁㐁㔁㘁㜁㠁㤁㨁㬁㰁㴁㸁㼁䀁䄁䈁䌁䐁䔁䘁䜁䠁䤁䨁䬁䰁䴁丁企倁儁刁匁吁唁嘁圁堁夁威嬁封崁币弁态愁戁持搁攁昁朁栁椁樁欁氁洁渁漁瀁焁爁猁琁甁瘁省码礁稁笁簁紁縁缁老脁舁茁萁蔁蘁蜁蠁褁訁謁谁贁踁輁送鄁鈁錁鐁锁阁霁頁餁騁鬁鰁鴁鸁鼁ꀁꄁꈁꌁꐁꔁꘁ꜁ꠁ꤁ꨁꬁ각괁긁꼁뀁넁눁댁됁딁똁뜁렁뤁먁묁밁봁";
   @EventHandler
   private final Listener<TotemPopEvent> totemPopEventListener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && this.pop.getValue() && !this.popped) {
            if (event.getEntity() != null) {
               String name = event.getEntity().getName();
               if (!SocialManager.isFriend(name) && !name.equals(mc.player.getName())) {
                  this.popped = true;
                  this.popTiming.reset();
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketChatMessage(
                           "/msg "
                              + name
                              + " "
                              + "āȁ́Ё\u0601܁ࠁँਁଁก༁ခᄁሁጁᐁᔁᘁᜁ᠁ᤁᨁᬁᰁᴁḁἁ ℁∁⌁␁━✁⠁⤁⨁⬁Ⰱⴁ⸁⼁、\u3101㈁㌁㐁㔁㘁㜁㠁㤁㨁㬁㰁㴁㸁㼁䀁䄁䈁䌁䐁䔁䘁䜁䠁䤁䨁䬁䰁䴁丁企倁儁刁匁吁唁嘁圁堁夁威嬁封崁币弁态愁戁持搁攁昁朁栁椁樁欁氁洁渁漁瀁焁爁猁琁甁瘁省码礁稁笁簁紁縁缁老脁舁茁萁蔁蘁蜁蠁褁訁謁谁贁踁輁送鄁鈁錁鐁锁阁霁頁餁騁鬁鰁鴁鸁鼁ꀁꄁꈁꌁꐁꔁꘁ꜁ꠁ꤁ꨁꬁ각괁긁꼁뀁넁눁댁됁딁똁뜁렁뤁먁묁밁봁"
                        )
                     );
               }
            }
         }
      }
   );

   @Override
   public void onUpdate() {
      if (mc.world != null && mc.player != null) {
         this.sent = 0;
         if (this.popped && this.popTiming.passedS(this.blank.getValue().intValue())) {
            this.popped = false;
         }

         if (this.time.getValue() && this.timing.passedS(this.timingDelay.getValue() * 10 + this.timingDelay2.getValue())) {
            for (EntityPlayer player : mc.world.playerEntities) {
               if (this.sent >= this.maxPlayer.getValue() && this.maxPlayer.getValue() != 0) {
                  break;
               }

               if (!EntityUtil.basicChecksEntity(player)
                  && (!(mc.player.getDistance(player) > this.range.getValue().intValue()) || this.range.getValue() == 0)) {
                  this.sent++;
                  mc.player
                     .connection
                     .sendPacket(
                        new CPacketChatMessage(
                           "/msg "
                              + player.getName()
                              + " "
                              + "āȁ́Ё\u0601܁ࠁँਁଁก༁ခᄁሁጁᐁᔁᘁᜁ᠁ᤁᨁᬁᰁᴁḁἁ ℁∁⌁␁━✁⠁⤁⨁⬁Ⰱⴁ⸁⼁、\u3101㈁㌁㐁㔁㘁㜁㠁㤁㨁㬁㰁㴁㸁㼁䀁䄁䈁䌁䐁䔁䘁䜁䠁䤁䨁䬁䰁䴁丁企倁儁刁匁吁唁嘁圁堁夁威嬁封崁币弁态愁戁持搁攁昁朁栁椁樁欁氁洁渁漁瀁焁爁猁琁甁瘁省码礁稁笁簁紁縁缁老脁舁茁萁蔁蘁蜁蠁褁訁謁谁贁踁輁送鄁鈁錁鐁锁阁霁頁餁騁鬁鰁鴁鸁鼁ꀁꄁꈁꌁꐁꔁꘁ꜁ꠁ꤁ꨁꬁ각괁긁꼁뀁넁눁댁됁딁똁뜁렁뤁먁묁밁봁"
                        )
                     );
               }
            }

            this.timing.reset();
         }
      }
   }
}
