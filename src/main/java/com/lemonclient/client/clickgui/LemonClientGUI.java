package com.lemonclient.client.clickgui;

import com.lemonclient.api.setting.Setting;
import com.lemonclient.api.setting.SettingsManager;
import com.lemonclient.api.setting.values.BooleanSetting;
import com.lemonclient.api.setting.values.ColorSetting;
import com.lemonclient.api.setting.values.DoubleSetting;
import com.lemonclient.api.setting.values.IntegerSetting;
import com.lemonclient.api.setting.values.ModeSetting;
import com.lemonclient.api.setting.values.StringSetting;
import com.lemonclient.api.util.font.FontUtil;
import com.lemonclient.api.util.render.GSColor;
import com.lemonclient.client.LemonClient;
import com.lemonclient.client.module.Category;
import com.lemonclient.client.module.HUDModule;
import com.lemonclient.client.module.Module;
import com.lemonclient.client.module.ModuleManager;
import com.lemonclient.client.module.modules.gui.ClickGuiModule;
import com.lemonclient.client.module.modules.gui.ColorMain;
import com.lukflug.panelstudio.base.Animation;
import com.lukflug.panelstudio.base.Context;
import com.lukflug.panelstudio.base.IBoolean;
import com.lukflug.panelstudio.base.IToggleable;
import com.lukflug.panelstudio.base.SettingsAnimation;
import com.lukflug.panelstudio.base.SimpleToggleable;
import com.lukflug.panelstudio.component.IComponent;
import com.lukflug.panelstudio.component.IFixedComponent;
import com.lukflug.panelstudio.component.IFixedComponentProxy;
import com.lukflug.panelstudio.component.IResizable;
import com.lukflug.panelstudio.component.IScrollSize;
import com.lukflug.panelstudio.container.IContainer;
import com.lukflug.panelstudio.hud.HUDGUI;
import com.lukflug.panelstudio.layout.CSGOLayout;
import com.lukflug.panelstudio.layout.ChildUtil;
import com.lukflug.panelstudio.layout.ComponentGenerator;
import com.lukflug.panelstudio.layout.IComponentAdder;
import com.lukflug.panelstudio.layout.IComponentGenerator;
import com.lukflug.panelstudio.layout.ILayout;
import com.lukflug.panelstudio.layout.PanelAdder;
import com.lukflug.panelstudio.layout.PanelLayout;
import com.lukflug.panelstudio.mc12.MinecraftGUI;
import com.lukflug.panelstudio.mc12.MinecraftHUDGUI;
import com.lukflug.panelstudio.popup.CenteredPositioner;
import com.lukflug.panelstudio.popup.MousePositioner;
import com.lukflug.panelstudio.popup.PanelPositioner;
import com.lukflug.panelstudio.popup.PopupTuple;
import com.lukflug.panelstudio.setting.IBooleanSetting;
import com.lukflug.panelstudio.setting.ICategory;
import com.lukflug.panelstudio.setting.IClient;
import com.lukflug.panelstudio.setting.IColorSetting;
import com.lukflug.panelstudio.setting.IEnumSetting;
import com.lukflug.panelstudio.setting.IKeybindSetting;
import com.lukflug.panelstudio.setting.ILabeled;
import com.lukflug.panelstudio.setting.IModule;
import com.lukflug.panelstudio.setting.INumberSetting;
import com.lukflug.panelstudio.setting.ISetting;
import com.lukflug.panelstudio.setting.IStringSetting;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.IColorScheme;
import com.lukflug.panelstudio.theme.ITheme;
import com.lukflug.panelstudio.theme.IThemeMultiplexer;
import com.lukflug.panelstudio.theme.ThemeTuple;
import com.lukflug.panelstudio.widget.ColorPickerComponent;
import com.lukflug.panelstudio.widget.TextField;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class LemonClientGUI extends MinecraftHUDGUI {
   public static LemonClientGUI INSTANCE;
   public static final int WIDTH = 100;
   public static final int HEIGHT = 12;
   public static final int FONT_HEIGHT = 9;
   public static final int DISTANCE = 10;
   public static final int HUD_BORDER = 2;
   public static IClient client;
   public static MinecraftGUI.GUIInterface guiInterface;
   public static HUDGUI gui;
   private final ITheme theme;
   private ITheme clearTheme;

   public LemonClientGUI() {
      INSTANCE = this;
      final ClickGuiModule clickGuiModule = ModuleManager.getModule(ClickGuiModule.class);
      final ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      guiInterface = new MinecraftGUI.GUIInterface(true) {
         @Override
         public void drawString(Point pos, int height, String s, Color c) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(pos.x, pos.y, 0.0F);
            double scale = (double)height / (FontUtil.getFontHeight(colorMain.customFont.getValue()) + (colorMain.customFont.getValue() ? 1 : 0));
            this.end(false);
            FontUtil.drawStringWithShadow(colorMain.customFont.getValue(), s, 0.0F, 0.0F, new GSColor(c));
            this.begin(false);
            GlStateManager.scale(scale, scale, 1.0);
            GlStateManager.popMatrix();
         }

         @Override
         public int getFontWidth(int height, String s) {
            double scale = (double)height / (FontUtil.getFontHeight(colorMain.customFont.getValue()) + (colorMain.customFont.getValue() ? 1 : 0));
            return (int)Math.round(FontUtil.getStringWidth(colorMain.customFont.getValue(), s) * scale);
         }

         @Override
         public double getScreenWidth() {
            return super.getScreenWidth();
         }

         @Override
         public double getScreenHeight() {
            return super.getScreenHeight();
         }

         @Override
         public String getResourcePrefix() {
            return "lemonclient:gui/";
         }
      };
      this.clearTheme = new Theme(
         new LemonClientGUI.GSColorScheme("clear", () -> true),
         colorMain.Title.getValue(),
         colorMain.Enabled.getValue(),
         colorMain.Disabled.getValue(),
         colorMain.Background.getValue(),
         colorMain.Font.getValue(),
         colorMain.ScrollBar.getValue(),
         colorMain.Highlight.getValue(),
         () -> clickGuiModule.gradient.getValue(),
         9,
         3,
         1,
         ": " + TextFormatting.GRAY
      );
      this.theme = (IThemeMultiplexer)() -> this.clearTheme;
      client = () -> Arrays.stream(Category.values()).sorted(Comparator.comparing(Enum::toString)).map(category -> new ICategory() {
         @Override
         public String getDisplayName() {
            return category.toString();
         }

         @Override
         public Stream<IModule> getModules() {
            return ModuleManager.getModulesInCategory(category).stream().sorted(Comparator.comparing(Module::getName)).map(module -> new IModule() {
               @Override
               public String getDisplayName() {
                  return module.getName();
               }

               @Override
               public IToggleable isEnabled() {
                  return new IToggleable() {
                     @Override
                     public boolean isOn() {
                        return module.isEnabled();
                     }

                     @Override
                     public void toggle() {
                        module.toggle();
                     }
                  };
               }

               @Override
               public Stream<ISetting<?>> getSettings() {
                  Stream temp = SettingsManager.getSettingsForModule(module).stream().map(setting -> LemonClientGUI.this.createSetting((Setting<?>)setting));
                  return Stream.concat(temp, Stream.concat(Stream.of(new IBooleanSetting() {
                     @Override
                     public String getDisplayName() {
                        return "Toggle Msgs";
                     }

                     @Override
                     public void toggle() {
                        module.setToggleMsg(!module.isToggleMsg());
                     }

                     @Override
                     public boolean isOn() {
                        return module.isToggleMsg();
                     }
                  }), Stream.of(new IKeybindSetting() {
                     @Override
                     public String getDisplayName() {
                        return "Keybind";
                     }

                     @Override
                     public int getKey() {
                        return module.getBind();
                     }

                     @Override
                     public void setKey(int key) {
                        module.setBind(key);
                     }

                     @Override
                     public String getKeyName() {
                        return Keyboard.getKeyName(module.getBind());
                     }
                  })));
               }
            });
         }
      });
      final IToggleable guiToggle = new SimpleToggleable(false);
      IToggleable hudToggle = new SimpleToggleable(false) {
         @Override
         public boolean isOn() {
            return guiToggle.isOn() && super.isOn() ? clickGuiModule.showHUD.getValue() : super.isOn();
         }
      };
      gui = new HUDGUI(guiInterface, this.theme.getDescriptionRenderer(), new MousePositioner(new Point(10, 10)), guiToggle, hudToggle);
      final BiFunction<Context, Integer, Integer> scrollHeight = (context, componentHeight) -> clickGuiModule.scrolling.getValue().equals("Screen")
         ? componentHeight
         : Math.min(componentHeight, Math.max(48, this.height - context.getPos().y - 12));
      Supplier<Animation> animation = () -> new SettingsAnimation(() -> clickGuiModule.animationSpeed.getValue(), () -> guiInterface.getTime());
      PopupTuple popupType = new PopupTuple(new PanelPositioner(new Point(0, 0)), false, new IScrollSize() {
         @Override
         public int getScrollHeight(Context context, int componentHeight) {
            return scrollHeight.apply(context, componentHeight);
         }
      });

      for (final Module module : ModuleManager.getModules()) {
         if (module instanceof HUDModule) {
            ((HUDModule)module).populate(this.theme);
            gui.addHUDComponent(((HUDModule)module).getComponent(), new IToggleable() {
               @Override
               public boolean isOn() {
                  return module.isEnabled();
               }

               @Override
               public void toggle() {
                  module.toggle();
               }
            }, animation.get(), this.theme, 2);
         }
      }

      IComponentAdder classicPanelAdder = new PanelAdder(new IContainer<IFixedComponent>() {
         public boolean addComponent(final IFixedComponent component) {
            return LemonClientGUI.gui.addComponent((IFixedComponent)(new IFixedComponentProxy<IFixedComponent>() {
               @Override
               public void handleScroll(Context context, int diff) {
                  IFixedComponentProxy.super.handleScroll(context, diff);
                  if (clickGuiModule.scrolling.getValue().equals("Screen")) {
                     Point p = this.getPosition(LemonClientGUI.guiInterface);
                     p.translate(0, -diff);
                     this.setPosition(LemonClientGUI.guiInterface, p);
                  }
               }

               public IFixedComponent getComponent() {
                  return component;
               }
            }));
         }

         public boolean addComponent(final IFixedComponent component, IBoolean visible) {
            return LemonClientGUI.gui.addComponent((IFixedComponent)(new IFixedComponentProxy<IFixedComponent>() {
               @Override
               public void handleScroll(Context context, int diff) {
                  IFixedComponentProxy.super.handleScroll(context, diff);
                  if (clickGuiModule.scrolling.getValue().equals("Screen")) {
                     Point p = this.getPosition(LemonClientGUI.guiInterface);
                     p.translate(0, -diff);
                     this.setPosition(LemonClientGUI.guiInterface, p);
                  }
               }

               public IFixedComponent getComponent() {
                  return component;
               }
            }), visible);
         }

         public boolean removeComponent(IFixedComponent component) {
            return LemonClientGUI.gui.removeComponent(component);
         }
      }, false, () -> !clickGuiModule.csgoLayout.getValue(), title -> title) {
         @Override
         protected IScrollSize getScrollSize(IResizable size) {
            return new IScrollSize() {
               @Override
               public int getScrollHeight(Context context, int componentHeight) {
                  return scrollHeight.apply(context, componentHeight);
               }
            };
         }
      };
      IComponentGenerator generator = new ComponentGenerator(scancode -> scancode == 211, character -> character >= 32, new TextFieldKeys()) {
         @Override
         public IComponent getColorComponent(
            IColorSetting setting, Supplier<Animation> animationx, IComponentAdder adder, ThemeTuple theme, int colorLevel, boolean isContainer
         ) {
            return new ColorPickerComponent(setting, theme);
         }

         @Override
         public IComponent getStringComponent(
            IStringSetting setting, Supplier<Animation> animationx, IComponentAdder adder, ThemeTuple theme, int colorLevel, boolean isContainer
         ) {
            return new TextField(setting, this.keys, 0, new SimpleToggleable(false), theme.getTextRenderer(false, isContainer)) {
               @Override
               public boolean allowCharacter(char character) {
                  return charFilter.test(character) && character != 127;
               }
            };
         }
      };
      ILayout classicPanelLayout = new PanelLayout(
         100, new Point(10, 10), 55, 22, animation, level -> ChildUtil.ChildMode.DOWN, level -> ChildUtil.ChildMode.DOWN, popupType
      );
      classicPanelLayout.populateGUI(classicPanelAdder, generator, client, this.theme);
      PopupTuple colorPopup = new PopupTuple(
         new CenteredPositioner(() -> new Rectangle(new Point(0, 0), guiInterface.getWindowSize())), true, new IScrollSize() {}
      );
      IComponentAdder horizontalCSGOAdder = new PanelAdder(gui, true, () -> clickGuiModule.csgoLayout.getValue(), title -> title);
      ILayout horizontalCSGOLayout = new CSGOLayout(
         new Labeled("Lemon", null, () -> true), new Point(100, 100), 480, 100, animation, "Enabled", true, true, 2, ChildUtil.ChildMode.DOWN, colorPopup
      ) {
         @Override
         public int getScrollHeight(Context context, int componentHeight) {
            return 320;
         }

         @Override
         protected boolean isUpKey(int key) {
            return key == 200;
         }

         @Override
         protected boolean isDownKey(int key) {
            return key == 208;
         }

         @Override
         protected boolean isLeftKey(int key) {
            return key == 203;
         }

         @Override
         protected boolean isRightKey(int key) {
            return key == 205;
         }
      };
      horizontalCSGOLayout.populateGUI(horizontalCSGOAdder, generator, client, this.theme);
   }

   @Override
   protected HUDGUI getGUI() {
      return gui;
   }

   private ISetting<?> createSetting(final Setting<?> setting) {
      if (setting instanceof BooleanSetting) {
         return new IBooleanSetting() {
            @Override
            public String getDisplayName() {
               return setting.getName();
            }

            @Override
            public IBoolean isVisible() {
               return () -> setting.isVisible();
            }

            @Override
            public void toggle() {
               ((BooleanSetting)setting).setValue(!((BooleanSetting)setting).getValue());
            }

            @Override
            public boolean isOn() {
               return ((BooleanSetting)setting).getValue();
            }

            @Override
            public Stream<ISetting<?>> getSubSettings() {
               return setting.getSubSettings().count() == 0L
                  ? null
                  : setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
            }
         };
      } else if (setting instanceof IntegerSetting) {
         return new INumberSetting() {
            @Override
            public String getDisplayName() {
               return setting.getName();
            }

            @Override
            public IBoolean isVisible() {
               return () -> setting.isVisible();
            }

            @Override
            public double getNumber() {
               return ((IntegerSetting)setting).getValue().intValue();
            }

            @Override
            public void setNumber(double value) {
               ((IntegerSetting)setting).setValue((int)Math.round(value));
            }

            @Override
            public double getMaximumValue() {
               return ((IntegerSetting)setting).getMax();
            }

            @Override
            public double getMinimumValue() {
               return ((IntegerSetting)setting).getMin();
            }

            @Override
            public int getPrecision() {
               return 0;
            }

            @Override
            public Stream<ISetting<?>> getSubSettings() {
               return setting.getSubSettings().count() == 0L
                  ? null
                  : setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
            }
         };
      } else if (setting instanceof DoubleSetting) {
         return new INumberSetting() {
            @Override
            public String getDisplayName() {
               return setting.getName();
            }

            @Override
            public IBoolean isVisible() {
               return () -> setting.isVisible();
            }

            @Override
            public double getNumber() {
               return ((DoubleSetting)setting).getValue();
            }

            @Override
            public void setNumber(double value) {
               ((DoubleSetting)setting).setValue(value);
            }

            @Override
            public double getMaximumValue() {
               return ((DoubleSetting)setting).getMax();
            }

            @Override
            public double getMinimumValue() {
               return ((DoubleSetting)setting).getMin();
            }

            @Override
            public int getPrecision() {
               return 2;
            }

            @Override
            public Stream<ISetting<?>> getSubSettings() {
               return setting.getSubSettings().count() == 0L
                  ? null
                  : setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
            }
         };
      } else if (setting instanceof ModeSetting) {
         return new IEnumSetting() {
            private final ILabeled[] states = ((ModeSetting)setting)
               .getModes()
               .stream()
               .map(mode -> new Labeled(mode, null, () -> true))
               .toArray(ILabeled[]::new);

            @Override
            public String getDisplayName() {
               return setting.getName();
            }

            @Override
            public IBoolean isVisible() {
               return () -> setting.isVisible();
            }

            @Override
            public void increment() {
               ((ModeSetting)setting).increment();
            }

            @Override
            public void decrement() {
               ((ModeSetting)setting).decrement();
            }

            @Override
            public String getValueName() {
               return ((ModeSetting)setting).getValue();
            }

            @Override
            public int getValueIndex() {
               return ((ModeSetting)setting).getModes().indexOf(this.getValueName());
            }

            @Override
            public void setValueIndex(int index) {
               ((ModeSetting)setting).setValue(((ModeSetting)setting).getModes().get(index));
            }

            @Override
            public ILabeled[] getAllowedValues() {
               return this.states;
            }

            @Override
            public Stream<ISetting<?>> getSubSettings() {
               return setting.getSubSettings().count() == 0L
                  ? null
                  : setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
            }
         };
      } else if (setting instanceof ColorSetting) {
         return new IColorSetting() {
            @Override
            public String getDisplayName() {
               return TextFormatting.BOLD + setting.getName();
            }

            @Override
            public IBoolean isVisible() {
               return () -> setting.isVisible();
            }

            @Override
            public Color getValue() {
               return ((ColorSetting)setting).getValue();
            }

            @Override
            public void setValue(Color value) {
               ((ColorSetting)setting).setValue(new GSColor(value));
            }

            @Override
            public Color getColor() {
               return ((ColorSetting)setting).getColor();
            }

            @Override
            public boolean getRainbow() {
               return ((ColorSetting)setting).getRainbow();
            }

            @Override
            public void setRainbow(boolean rainbow) {
               ((ColorSetting)setting).setRainbow(rainbow);
            }

            @Override
            public boolean hasAlpha() {
               return ((ColorSetting)setting).alphaEnabled();
            }

            @Override
            public boolean allowsRainbow() {
               return ((ColorSetting)setting).rainbowEnabled();
            }

            @Override
            public boolean hasHSBModel() {
               return ModuleManager.getModule(ColorMain.class).colorModel.getValue().equalsIgnoreCase("HSB");
            }

            @Override
            public Stream<ISetting<?>> getSubSettings() {
               Stream<ISetting<?>> temp = setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
               return Stream.concat(temp, Stream.of(new IBooleanSetting() {
                  @Override
                  public String getDisplayName() {
                     return "Sync Color";
                  }

                  @Override
                  public IBoolean isVisible() {
                     return () -> setting != ModuleManager.getModule(ColorMain.class).enabledColor;
                  }

                  @Override
                  public void toggle() {
                     ((ColorSetting)setting).setValue(ModuleManager.getModule(ColorMain.class).enabledColor.getColor());
                     ((ColorSetting)setting).setRainbow(ModuleManager.getModule(ColorMain.class).enabledColor.getRainbow());
                  }

                  @Override
                  public boolean isOn() {
                     return ModuleManager.getModule(ColorMain.class).enabledColor.getColor().equals(((ColorSetting)setting).getColor());
                  }
               }));
            }
         };
      } else {
         return (ISetting<?>)(setting instanceof StringSetting
            ? new IStringSetting() {
               @Override
               public String getValue() {
                  return ((StringSetting)setting).getText();
               }

               @Override
               public void setValue(String string) {
                  ((StringSetting)setting).setText(string);
               }

               @Override
               public String getDisplayName() {
                  return setting.getName();
               }
            }
            : new ISetting<Void>() {
               @Override
               public String getDisplayName() {
                  return setting.getName();
               }

               @Override
               public IBoolean isVisible() {
                  return () -> setting.isVisible();
               }

               public Void getSettingState() {
                  return null;
               }

               @Override
               public Class<Void> getSettingClass() {
                  return Void.class;
               }

               @Override
               public Stream<ISetting<?>> getSubSettings() {
                  return setting.getSubSettings().count() == 0L
                     ? null
                     : setting.getSubSettings().map(subSetting -> LemonClientGUI.this.createSetting((Setting<?>)subSetting));
               }
            });
      }
   }

   public static void renderItem(ItemStack item, Point pos) {
      LemonClient.INSTANCE.gameSenseGUI.getInterface().end(false);
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
      GL11.glPushAttrib(524288);
      GL11.glDisable(3089);
      GlStateManager.clear(256);
      GL11.glPopAttrib();
      GlStateManager.enableDepth();
      GlStateManager.disableAlpha();
      GlStateManager.pushMatrix();
      Minecraft.getMinecraft().getRenderItem().zLevel = -150.0F;
      RenderHelper.enableGUIStandardItemLighting();
      Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, pos.x, pos.y);
      Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRenderer, item, pos.x, pos.y);
      RenderHelper.disableStandardItemLighting();
      Minecraft.getMinecraft().getRenderItem().zLevel = 0.0F;
      GlStateManager.popMatrix();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      LemonClient.INSTANCE.gameSenseGUI.getInterface().begin(false);
   }

   public static void renderItemTest(ItemStack item, Point pos) {
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
      GL11.glPushAttrib(524288);
      GL11.glDisable(3089);
      GlStateManager.clear(256);
      GL11.glPopAttrib();
      GlStateManager.enableDepth();
      GlStateManager.disableAlpha();
      GlStateManager.pushMatrix();
      Minecraft.getMinecraft().getRenderItem().zLevel = -150.0F;
      RenderHelper.enableGUIStandardItemLighting();
      Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, pos.x, pos.y);
      Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRenderer, item, pos.x, pos.y);
      RenderHelper.disableStandardItemLighting();
      Minecraft.getMinecraft().getRenderItem().zLevel = 0.0F;
      GlStateManager.popMatrix();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
   }

   public static void renderEntity(EntityLivingBase entity, Point pos, int scale) {
      LemonClient.INSTANCE.gameSenseGUI.getInterface().end(false);
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
      GL11.glPushAttrib(524288);
      GL11.glDisable(3089);
      GlStateManager.clear(256);
      GL11.glPopAttrib();
      GlStateManager.enableDepth();
      GlStateManager.disableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GuiInventory.drawEntityOnScreen(pos.x, pos.y, scale, 28.0F, 60.0F, entity);
      GlStateManager.popMatrix();
      GlStateManager.disableDepth();
      GlStateManager.depthMask(false);
      LemonClient.INSTANCE.gameSenseGUI.getInterface().begin(false);
   }

   @Override
   protected MinecraftGUI.GUIInterface getInterface() {
      return guiInterface;
   }

   @Override
   protected int getScrollSpeed() {
      return ModuleManager.getModule(ClickGuiModule.class).scrollSpeed.getValue();
   }

   public void refresh() {
      ClickGuiModule clickGuiModule = ModuleManager.getModule(ClickGuiModule.class);
      ColorMain colorMain = ModuleManager.getModule(ColorMain.class);
      this.clearTheme = new Theme(
         new LemonClientGUI.GSColorScheme("clear", () -> true),
         colorMain.Title.getValue(),
         colorMain.Enabled.getValue(),
         colorMain.Disabled.getValue(),
         colorMain.Background.getValue(),
         colorMain.Font.getValue(),
         colorMain.ScrollBar.getValue(),
         colorMain.Highlight.getValue(),
         () -> clickGuiModule.gradient.getValue(),
         9,
         3,
         1,
         ": " + TextFormatting.GRAY
      );
   }

   private static final class GSColorScheme implements IColorScheme {
      private final String configName;
      private final Supplier<Boolean> isVisible;

      public GSColorScheme(String configName, Supplier<Boolean> isVisible) {
         this.configName = configName;
         this.isVisible = isVisible;
      }

      @Override
      public void createSetting(ITheme theme, String name, String description, boolean hasAlpha, boolean allowsRainbow, Color color, boolean rainbow) {
      }

      @Override
      public Color getColor(String name) {
         return new Color(255, 255, 255);
      }
   }
}
