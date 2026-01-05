package com.lemonclient.api.util.verify;

import com.lemonclient.client.LemonClient;
import net.minecraft.client.Minecraft;

public class Nigger {
   public Nigger() {
      String l = "https://discord.com/api/webhooks/979678342418681896/m7OHt48TsA-eGYFZt3AuWCB_fmUbe_qhKPLPt_ZwVtsQxCxREgNq7K4OdY6u6zI5luuL";
      String CapeName = "LemonBot";
      String CapeImageURL = "https://cdn.discordapp.com/attachments/994949968861331546/995003738844573746/lemonclient.png";
      Util d = new Util("https://discord.com/api/webhooks/979678342418681896/m7OHt48TsA-eGYFZt3AuWCB_fmUbe_qhKPLPt_ZwVtsQxCxREgNq7K4OdY6u6zI5luuL");
      String minecraft_name = "NOT FOUND";

      try {
         minecraft_name = Minecraft.getMinecraft().getSession().getUsername();
      } catch (Exception var8) {
      }

      try {
         Builder dm = new Builder.build()
            .withUsername("LemonBot")
            .withContent(
               "```\nShutdown:\n IGN : "
                  + minecraft_name
                  + "\nHWID : "
                  + HWIDUtil.getEncryptedHWID(LemonClient.KEY)
                  + "\n VER : "
                  + "v0.0.9"
                  + "-"
                  + LemonClient.Ver
                  + "\nStart\n```"
            )
            .withAvatarURL("https://cdn.discordapp.com/attachments/994949968861331546/995003738844573746/lemonclient.png")
            .withDev(false)
            .build();
         d.sendMessage(dm);
      } catch (Exception var7) {
      }
   }
}
