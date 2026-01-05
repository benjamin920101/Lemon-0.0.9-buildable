package com.lemonclient.client.module.modules.render;

import com.lemonclient.api.event.events.PacketEvent;
import com.lemonclient.api.event.events.RenderEvent;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.api.util.render.Interpolation;
import com.lemonclient.api.util.render.animation.AnimationMode;
import com.lemonclient.api.util.render.animation.TimeAnimation;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import org.lwjgl.opengl.GL11;

@Module.Declaration(name = "Trails", category = Category.Render)
public class Trails extends Module {
   BooleanSetting arrows = this.registerBoolean("Arrows", false);
   BooleanSetting pearls = this.registerBoolean("Pearls", false);
   BooleanSetting snowballs = this.registerBoolean("Snowballs", false);
   IntegerSetting time = this.registerInteger("Time", 1, 1, 10);
   ColorSetting color = this.registerColor("Color", new GSColor(255, 0, 0, 255));
   IntegerSetting alpha = this.registerInteger("Alpha", 255, 1, 255);
   DoubleSetting width = this.registerDouble("Width", 1.6F, 0.1F, 10.0);
   Map<Integer, TimeAnimation> ids = new ConcurrentHashMap<>();
   Map<Integer, List<Trails.Trace>> traceLists = new ConcurrentHashMap<>();
   Map<Integer, Trails.Trace> traces = new ConcurrentHashMap<>();
   public static final Vec3d ORIGIN = new Vec3d(8.0, 64.0, 8.0);
   @EventHandler
   private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(
      event -> {
         if (mc.world != null) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
               SPacketSpawnObject packet = (SPacketSpawnObject)event.getPacket();
               if (this.pearls.getValue() && packet.getType() == 65
                  || this.arrows.getValue() && packet.getType() == 60
                  || this.snowballs.getValue() && packet.getType() == 61) {
                  TimeAnimation animation = new TimeAnimation(this.time.getValue() * 1000, 0.0, this.alpha.getValue().intValue(), false, AnimationMode.LINEAR);
                  animation.stop();
                  this.ids.put(packet.getEntityID(), animation);
                  this.traceLists.put(packet.getEntityID(), new ArrayList<>());
                  this.traces
                     .put(
                        packet.getEntityID(),
                        new Trails.Trace(
                           0,
                           null,
                           mc.world.provider.getDimensionType(),
                           new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
                           new ArrayList<>()
                        )
                     );
               }
            }

            if (event.getPacket() instanceof SPacketDestroyEntities) {
               for (int id : ((SPacketDestroyEntities)event.getPacket()).getEntityIDs()) {
                  if (this.ids.containsKey(id)) {
                     this.ids.get(id).play();
                  }
               }
            }
         }
      }
   );

   @Override
   public void onTick() {
      if (mc.world != null) {
         if (!this.ids.keySet().isEmpty()) {
            for (Integer id : this.ids.keySet()) {
               if (id != null) {
                  if (mc.world.loadedEntityList == null) {
                     return;
                  }

                  if (mc.world.loadedEntityList.isEmpty()) {
                     return;
                  }

                  Trails.Trace idTrace = this.traces.get(id);
                  Entity entity = mc.world.getEntityByID(id);
                  if (entity != null) {
                     Vec3d vec = entity.getPositionVector();
                     if (vec.equals(ORIGIN)) {
                        continue;
                     }

                     if (!this.traces.containsKey(id) || idTrace == null) {
                        this.traces.put(id, new Trails.Trace(0, null, mc.world.provider.getDimensionType(), vec, new ArrayList<>()));
                        idTrace = this.traces.get(id);
                     }

                     List<Trails.Trace.TracePos> trace = idTrace.getTrace();
                     Vec3d vec3d = trace.isEmpty() ? vec : trace.get(trace.size() - 1).getPos();
                     if (!trace.isEmpty() && (vec.distanceTo(vec3d) > 100.0 || idTrace.getType() != mc.world.provider.getDimensionType())) {
                        this.traceLists.get(id).add(idTrace);
                        trace = new ArrayList<>();
                        this.traces
                           .put(
                              id,
                              new Trails.Trace(this.traceLists.get(id).size() + 1, null, mc.world.provider.getDimensionType(), vec, new ArrayList<>())
                           );
                     }

                     if (trace.isEmpty() || !vec.equals(vec3d)) {
                        trace.add(new Trails.Trace.TracePos(vec));
                     }
                  }

                  TimeAnimation animation = this.ids.get(id);
                  if (entity instanceof EntityArrow && (entity.onGround || entity.collided || !entity.isAirBorne)) {
                     animation.play();
                  }

                  if (animation != null && this.alpha.getValue().intValue() - animation.getCurrent() <= 0.0) {
                     animation.stop();
                     this.ids.remove(id);
                     this.traceLists.remove(id);
                     this.traces.remove(id);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onWorldRender(RenderEvent event) {
      for (Entry<Integer, List<Trails.Trace>> entry : this.traceLists.entrySet()) {
         GL11.glLineWidth(this.width.getValue().floatValue());
         TimeAnimation animation = this.ids.get(entry.getKey());
         animation.add();
         GL11.glColor4f(
            this.color.getColor().getRed(),
            this.color.getColor().getGreen(),
            this.color.getColor().getBlue(),
            MathHelper.clamp((float)(this.alpha.getValue().intValue() - animation.getCurrent() / 255.0), 0.0F, 255.0F)
         );
         entry.getValue().forEach(tracex -> {
            GL11.glBegin(3);
            tracex.getTrace().forEach(this::renderVec);
            GL11.glEnd();
         });
         GL11.glColor4f(
            this.color.getColor().getRed(),
            this.color.getColor().getGreen(),
            this.color.getColor().getBlue(),
            MathHelper.clamp((float)(this.alpha.getValue().intValue() - animation.getCurrent() / 255.0), 0.0F, 255.0F)
         );
         GL11.glBegin(3);
         Trails.Trace trace = this.traces.get(entry.getKey());
         if (trace != null) {
            trace.getTrace().forEach(this::renderVec);
         }

         GL11.glEnd();
      }
   }

   private void renderVec(Trails.Trace.TracePos tracePos) {
      double x = tracePos.getPos().x - Interpolation.getRenderPosX();
      double y = tracePos.getPos().y - Interpolation.getRenderPosY();
      double z = tracePos.getPos().z - Interpolation.getRenderPosZ();
      GL11.glVertex3d(x, y, z);
   }

   public static class Trace {
      private String name;
      private int index;
      private Vec3d pos;
      private final List<Trails.Trace.TracePos> trace;
      private DimensionType type;

      public Trace(int index, String name, DimensionType type, Vec3d pos, List<Trails.Trace.TracePos> trace) {
         this.index = index;
         this.name = name;
         this.type = type;
         this.pos = pos;
         this.trace = trace;
      }

      public int getIndex() {
         return this.index;
      }

      public DimensionType getType() {
         return this.type;
      }

      public List<Trails.Trace.TracePos> getTrace() {
         return this.trace;
      }

      public String getName() {
         return this.name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public void setPos(Vec3d pos) {
         this.pos = pos;
      }

      public void setIndex(int index) {
         this.index = index;
      }

      public Vec3d getPos() {
         return this.pos;
      }

      public void setType(DimensionType type) {
         this.type = type;
      }

      public static class TracePos {
         private final Vec3d pos;

         public TracePos(Vec3d pos) {
            this.pos = pos;
         }

         public Vec3d getPos() {
            return this.pos;
         }
      }
   }
}
