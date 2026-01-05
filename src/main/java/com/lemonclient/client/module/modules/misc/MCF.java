package com.lemonclient.client.module.modules.misc;

import com.lemonclient.api.util.chat.Notification;
import com.lemonclient.api.util.misc.MessageBus;
import com.lemonclient.api.util.player.social.SocialManager;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import org.lwjgl.input.Mouse;

@Module.Declaration(name = "MCF", category = Category.Misc)
public class MCF extends Module {
   @EventHandler
   private final Listener<MouseInputEvent> listener = new Listener<>(
      event -> {
         if (mc.world != null && mc.player != null && !mc.player.isDead && mc.objectMouseOver != null) {
            if (mc.objectMouseOver.typeOfHit.equals(Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer && Mouse.isButtonDown(2)) {
               if (SocialManager.isFriend(mc.objectMouseOver.entityHit.getName())) {
                  SocialManager.delFriend(mc.objectMouseOver.entityHit.getName());
                  MessageBus.sendClientPrefixMessage(
                     ModuleManager.getModule(ColorMain.class).getDisabledColor()
                        + "Removed "
                        + mc.objectMouseOver.entityHit.getName()
                        + " from friends list",
                     Notification.Type.SUCCESS
                  );
               } else {
                  SocialManager.addFriend(mc.objectMouseOver.entityHit.getName());
                  MessageBus.sendClientPrefixMessage(
                     ModuleManager.getModule(ColorMain.class).getEnabledColor()
                        + "Added "
                        + mc.objectMouseOver.entityHit.getName()
                        + " to friends list",
                     Notification.Type.SUCCESS
                  );
               }
            }
         }
      }
   );
}
