package ir.ninjacoder;

import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Path;
import com.caverock.androidsvg.SVG;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ninjacoder.jgit.particle.SvgParticle;
import com.ninjacoder.jgit.particle.custom.CustomEffect;
import io.github.rosemoe.sora.widget.power.PowerModeEffectManager;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.ninjacoder.ghostide.core.activities.CodeEditorActivity;
import ir.ninjacoder.ghostide.core.activities.BaseCompat;
import ir.ninjacoder.ghostide.core.activities.FileManagerActivity;
import ir.ninjacoder.ghostide.core.enums.EffectTypeManager;
import ir.ninjacoder.ghostide.core.pl.PluginManagerCompat;
import ir.ninjacoder.ghostide.core.utils.DataUtil;
import ir.ninjacoder.ghostide.core.utils.FileUtil;
import ir.ninjacoder.prograsssheet.listchild.ChildIconEditorManager;
import java.util.Random;
import java.util.List;
import com.ninjacoder.jgit.particle.Particle;
import java.util.ArrayList;
import java.io.File;
import java.lang.reflect.Field;

public class EditorEffect implements PluginManagerCompat {
  private CodeEditor editor;
  private CodeEditorActivity currentActivity;
  private List<EffectModel> listeffectmodel;
  private PowerModeEffectManager powerManager;
  private int selectedEffectIndex = 0;

  @Override
  public void getEditor(CodeEditor editor) {
    this.editor = editor;
  }

  @Override
  public void getBaseCompat(BaseCompat base) {
    if (base instanceof CodeEditorActivity) {
      CodeEditorActivity activity = (CodeEditorActivity) base;
      this.currentActivity = activity;

      activity
          .getWindow()
          .getDecorView()
          .post(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    Field editorField = CodeEditorActivity.class.getDeclaredField("editor");
                    editorField.setAccessible(true);
                    editor = (CodeEditor) editorField.get(activity);

                    if (editor == null) {
                      Log.e("PLUGIN_DEBUG", "Editor is null");
                      return;
                    }

                    editor.setPowerModeEnabled(true);
                    powerManager = editor.getPowerModeEffectManager();

                    if (powerManager != null) {
                      loadEffectSettings();
                      registerSelectedEffect();
                      powerManager.setEffect(PowerModeEffectManager.EffectType.CUSTOM);
                      EffectTypeManager.saveTheme(
                          activity, PowerModeEffectManager.EffectType.CUSTOM);
                      Log.e("PLUGIN_DEBUG", "Effect registered: " + getSelectedEffectName());
                    }
                  } catch (Exception e) {
                    Log.e("PLUGIN_DEBUG", "Error: " + e.getMessage());
                  }
                }
              });

      ChildIconEditorManager child =
          new ChildIconEditorManager(
              "/storage/emulated/0/GhostWebIDE/plugins/effect/icon/effect.png",
              (v, pos, id, using) -> {
                showEffectSelectionDialog();
              });

      activity.addChildManagerEditor(child);
    }
  }

  public static class CustomSvgEffect implements CustomEffect {
    private Random random = new Random();
    private String svgCode;
    private String effectName;

    public CustomSvgEffect(String effectName, String svgCode) {
      this.effectName = effectName;
      this.svgCode = svgCode;
    }

    @Override
    public String getName() {
      return effectName;
    }

    @Override
    public String getDescription() {
      return "Custom SVG effect: " + effectName;
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      if (svgCode == null || svgCode.isEmpty()) return particles;

      int[] effectColors = {
        Color.argb(220, 255, 100, 100),
        Color.argb(220, 100, 255, 100),
        Color.argb(220, 100, 100, 255),
        Color.argb(220, 255, 255, 100),
        Color.argb(220, 255, 100, 255),
        Color.argb(220, 100, 255, 255),
        Color.argb(220, 255, 150, 50),
        Color.argb(220, 200, 100, 255)
      };

      int count = (int) (10 * intensity);
      for (int i = 0; i < count; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 6 * intensity + 2;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed - 3;

        float size = random.nextFloat() * 60 * intensity + 30;
        float gravity = 0.1f * intensity;
        float friction = 0.97f;
        int life = (int) (40 + random.nextInt(30) * intensity);
        float rotationSpeed = (random.nextFloat() - 0.5f) * 8 * intensity;

        int particleColor = effectColors[random.nextInt(effectColors.length)];
        try {
          SVG svg = SVG.getFromString(svgCode);
          particles.add(
              new SvgParticle(
                  x, y, svg, size, dx, dy, gravity, friction, life, rotationSpeed, particleColor));
        } catch (Exception err) {
          Log.e("SVG_PARSER", "Error parsing SVG: " + err.getLocalizedMessage());
        }
      }
      return particles;
    }

    @Override
    public void initialize() {
      Log.d("SVG_EFFECT", "Initialized: " + effectName);
    }

    @Override
    public void cleanup() {
      Log.d("SVG_EFFECT", "Cleaned up: " + effectName);
    }
  }

  // 1. Thunder Storm Effect
  public static class ThunderStormEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "ThunderStorm";
    }

    @Override
    public String getDescription() {
      return "Lightning and thunder effects";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      // Create lightning bolts
      for (int i = 0; i < 2 + (int) (intensity * 2); i++) {
        float endX = x + (random.nextFloat() - 0.5f) * 300 * intensity;
        float endY = y - random.nextFloat() * 400 * intensity;
        particles.add(
            new LightningParticle(
                x, y, endX, endY, Color.argb(200, 100, 150, 255), 12, 3f * intensity));
      }

      // Rain drops
      for (int i = 0; i < 15 * intensity; i++) {
        float dx = (random.nextFloat() - 0.2f) * 8f * intensity;
        float dy = random.nextFloat() * 15f * intensity + 10f;
        particles.add(
            new RainDropParticle(
                x,
                y,
                dx,
                dy,
                Color.argb(180, 150, 180, 220),
                20,
                1.5f,
                0.4f * intensity,
                0.99f,
                40));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // 2. Fire Explosion Effect
  public static class FireExplosionEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "FireExplosion";
    }

    @Override
    public String getDescription() {
      return "Explosive fire particles";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();
      int count = (int) (25 * intensity);

      for (int i = 0; i < count; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 20f * intensity + 8f;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed;

        // Fire colors (red, orange, yellow)
        int fireColor;
        if (random.nextFloat() < 0.6f) {
          fireColor = Color.argb(220, 255, 50 + random.nextInt(100), random.nextInt(50));
        } else if (random.nextFloat() < 0.8f) {
          fireColor = Color.argb(200, 255, 150 + random.nextInt(80), random.nextInt(30));
        } else {
          fireColor = Color.argb(180, 255, 255, 100 + random.nextInt(100));
        }

        float size = random.nextFloat() * 12f * intensity + 6f;
        float gravity = 0.3f * intensity;
        float friction = 0.93f;
        int life = (int) (25 + random.nextInt(20) * intensity);

        particles.add(new FireParticle(x, y, dx, dy, fireColor, size, gravity, friction, life));
      }

      // Smoke particles
      for (int i = 0; i < 8 * intensity; i++) {
        float dx = (random.nextFloat() - 0.5f) * 6f * intensity;
        float dy = -random.nextFloat() * 8f * intensity - 2f;
        particles.add(
            new SmokeParticle(
                x,
                y,
                dx,
                dy,
                Color.argb(120, 80, 80, 80),
                random.nextFloat() * 15f + 10f,
                -0.1f * intensity,
                0.97f,
                60));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // 3. Magic Spell Effect
  public static class MagicSpellEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "MagicSpell";
    }

    @Override
    public String getDescription() {
      return "Magical sparkling particles";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      // Main spell circle
      int circleParticles = (int) (20 * intensity);
      for (int i = 0; i < circleParticles; i++) {
        float angle = (float) (i * Math.PI * 2 / circleParticles);
        float radius = 30f * intensity;
        float dx = (float) Math.cos(angle) * 3f;
        float dy = (float) Math.sin(angle) * 3f;

        int magicColor =
            Color.argb(
                200,
                random.nextInt(100) + 155,
                random.nextInt(100) + 50,
                random.nextInt(100) + 155);

        particles.add(
            new MagicParticle(
                x + (float) Math.cos(angle) * radius,
                y + (float) Math.sin(angle) * radius,
                dx,
                dy,
                magicColor,
                4f,
                0f,
                0.98f,
                50));
      }

      // Sparkles
      for (int i = 0; i < 15 * intensity; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 6f * intensity + 2f;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed;

        int sparkleColor =
            Color.argb(
                220, 200 + random.nextInt(55), 100 + random.nextInt(155), 200 + random.nextInt(55));

        particles.add(
            new SparkleParticle(
                x,
                y,
                dx,
                dy,
                sparkleColor,
                random.nextFloat() * 3f + 2f,
                0.1f * intensity,
                0.96f,
                40));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // 4. Water Splash Effect
  public static class WaterSplashEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "WaterSplash";
    }

    @Override
    public String getDescription() {
      return "Water droplets and splashes";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      // Main splash
      for (int i = 0; i < 12 * intensity; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 10f * intensity + 5f;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed;

        int waterColor = Color.argb(180, 100, 150 + random.nextInt(80), 220 + random.nextInt(35));

        particles.add(
            new WaterDropParticle(
                x,
                y,
                dx,
                dy,
                waterColor,
                random.nextFloat() * 8f + 4f,
                0.4f * intensity,
                0.95f,
                35));
      }

      // Ripples
      for (int i = 0; i < 3; i++) {
        particles.add(
            new RippleParticle(
                x,
                y,
                Color.argb(100, 100, 180, 255),
                50f * intensity,
                30 + (int) (intensity * 20)));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // 5. Electric Arc Effect
  public static class ElectricArcEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "ElectricArc";
    }

    @Override
    public String getDescription() {
      return "Electric arcs and sparks";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      // Electric arcs
      for (int i = 0; i < 3 + (int) (intensity * 2); i++) {
        float endX = x + (random.nextFloat() - 0.5f) * 200 * intensity;
        float endY = y + (random.nextFloat() - 0.5f) * 200 * intensity;

        int arcColor = Color.argb(220, 50 + random.nextInt(100), 150 + random.nextInt(100), 255);

        particles.add(new ElectricArcParticle(x, y, endX, endY, arcColor, 8, 2f * intensity));
      }

      // Sparks
      for (int i = 0; i < 20 * intensity; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 12f * intensity + 6f;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed;

        particles.add(
            new SparkParticle(
                x, y, dx, dy, Color.argb(200, 200, 220, 255), 2f, 0.2f * intensity, 0.9f, 20));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // 6. Dark Energy Effect
  public static class DarkEnergyEffect implements CustomEffect {
    private Random random = new Random();

    @Override
    public String getName() {
      return "DarkEnergy";
    }

    @Override
    public String getDescription() {
      return "Dark energy particles and orbs";
    }

    @Override
    public List<Particle> spawnParticles(float x, float y, float intensity) {
      List<Particle> particles = new ArrayList<>();

      // Dark orbs
      for (int i = 0; i < 8 * intensity; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float speed = random.nextFloat() * 4f * intensity + 1f;
        float dx = (float) Math.cos(angle) * speed;
        float dy = (float) Math.sin(angle) * speed;

        int darkColor =
            Color.argb(200, 50 + random.nextInt(50), random.nextInt(30), 80 + random.nextInt(100));

        particles.add(
            new DarkOrbParticle(
                x,
                y,
                dx,
                dy,
                darkColor,
                random.nextFloat() * 12f + 8f,
                -0.05f * intensity,
                0.98f,
                60));
      }

      // Energy tendrils
      for (int i = 0; i < 5 * intensity; i++) {
        float angle = random.nextFloat() * (float) Math.PI * 2;
        float distance = random.nextFloat() * 100f * intensity + 50f;
        float endX = x + (float) Math.cos(angle) * distance;
        float endY = y + (float) Math.sin(angle) * distance;

        particles.add(
            new EnergyTendrilParticle(
                x, y, endX, endY, Color.argb(150, 100, 50, 150), 6, 1.5f * intensity));
      }

      return particles;
    }

    @Override
    public void initialize() {}

    @Override
    public void cleanup() {}
  }

  // Custom Particle Classes
  public static class LightningParticle extends Particle {
    private float endX, endY;
    private int segments;
    private float thickness;

    public LightningParticle(
        float startX,
        float startY,
        float endX,
        float endY,
        int color,
        int segments,
        float thickness) {
      super(startX, startY, color, 15, 0, 1);
      this.endX = endX;
      this.endY = endY;
      this.segments = segments;
      this.thickness = thickness;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      paint.setStrokeWidth(thickness);

      Random random = new Random();
      float prevX = x;
      float prevY = y;

      for (int i = 1; i <= segments; i++) {
        float progress = (float) i / segments;
        float baseX = x + (endX - x) * progress;
        float baseY = y + (endY - y) * progress;

        float offsetX = (random.nextFloat() - 0.5f) * 20f;
        float offsetY = (random.nextFloat() - 0.5f) * 20f;

        canvas.drawLine(prevX, prevY, baseX + offsetX, baseY + offsetY, paint);
        prevX = baseX + offsetX;
        prevY = baseY + offsetY;
      }
    }
  }

  public static class RainDropParticle extends Particle {
    private float length;
    private float thickness;

    public RainDropParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        int life,
        float length,
        float gravity,
        float friction,
        float thickness) {
      super(x, y, color, life, gravity, friction);
      this.length = length;
      this.thickness = thickness;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      paint.setStrokeWidth(thickness);
      canvas.drawLine(x, y, x, y + length, paint);
    }
  }

  public static class FireParticle extends Particle {
    public FireParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 6, paint);
    }
  }

  public static class SmokeParticle extends Particle {
    public SmokeParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 8, paint);
    }
  }

  public static class MagicParticle extends Particle {
    public MagicParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 4, paint);
    }
  }

  public static class SparkleParticle extends Particle {
    public SparkleParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 3, paint);
    }
  }

  public static class WaterDropParticle extends Particle {
    public WaterDropParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 6, paint);
    }
  }

  public static class RippleParticle extends Particle {
    private float maxRadius;
    private float currentRadius = 0;

    public RippleParticle(float x, float y, int color, float maxRadius, int life) {
      super(x, y, color, life, 0, 1);
      this.maxRadius = maxRadius;
    }

    @Override
    public void update(float deltaTime) {
      super.update(deltaTime);
      currentRadius = maxRadius * (1 - (float) life / initialLife);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(2);
      canvas.drawCircle(x, y, currentRadius, paint);
      paint.setStyle(Paint.Style.FILL);
    }
  }

  public static class ElectricArcParticle extends Particle {
    private float endX, endY;
    private int segments;
    private float thickness;

    public ElectricArcParticle(
        float startX,
        float startY,
        float endX,
        float endY,
        int color,
        int segments,
        float thickness) {
      super(startX, startY, color, 10, 0, 1);
      this.endX = endX;
      this.endY = endY;
      this.segments = segments;
      this.thickness = thickness;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      paint.setStrokeWidth(thickness);

      Random random = new Random();
      float prevX = x;
      float prevY = y;

      for (int i = 1; i <= segments; i++) {
        float progress = (float) i / segments;
        float baseX = x + (endX - x) * progress;
        float baseY = y + (endY - y) * progress;

        float offsetX = (random.nextFloat() - 0.5f) * 15f;
        float offsetY = (random.nextFloat() - 0.5f) * 15f;

        canvas.drawLine(prevX, prevY, baseX + offsetX, baseY + offsetY, paint);
        prevX = baseX + offsetX;
        prevY = baseY + offsetY;
      }
    }
  }

  public static class SparkParticle extends Particle {
    public SparkParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 2, paint);
    }
  }

  public static class DarkOrbParticle extends Particle {
    public DarkOrbParticle(
        float x,
        float y,
        float dx,
        float dy,
        int color,
        float size,
        float gravity,
        float friction,
        int life) {
      super(x, y, color, life, gravity, friction);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      canvas.drawCircle(x, y, 10, paint);
    }
  }

  public static class EnergyTendrilParticle extends Particle {
    private float endX, endY;
    private int segments;
    private float thickness;

    public EnergyTendrilParticle(
        float startX,
        float startY,
        float endX,
        float endY,
        int color,
        int segments,
        float thickness) {
      super(startX, startY, color, 25, 0, 1);
      this.endX = endX;
      this.endY = endY;
      this.segments = segments;
      this.thickness = thickness;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
      paint.setColor(color);
      paint.setAlpha(alpha);
      paint.setStrokeWidth(thickness);

      Random random = new Random();
      float prevX = x;
      float prevY = y;

      for (int i = 1; i <= segments; i++) {
        float progress = (float) i / segments;
        float baseX = x + (endX - x) * progress;
        float baseY = y + (endY - y) * progress;

        float curve = (float) Math.sin(progress * Math.PI) * 10f;
        float offsetX = (random.nextFloat() - 0.5f) * curve;
        float offsetY = (random.nextFloat() - 0.5f) * curve;

        canvas.drawLine(prevX, prevY, baseX + offsetX, baseY + offsetY, paint);
        prevX = baseX + offsetX;
        prevY = baseY + offsetY;
      }
    }
  }

  @Override
  public void getCodeEditorAc(CodeEditorActivity activity) {
    this.currentActivity = activity;
  }

  private void registerSelectedEffect() {
    if (powerManager == null) return;

    List<CustomEffect> currentEffects = powerManager.getCustomEffects();
    for (CustomEffect effect : currentEffects) {
      powerManager.unregisterCustomEffect(effect.getName());
    }

    String selectedEffect = getSelectedEffectName();
    switch (selectedEffect) {
      case "ThunderStorm":
        powerManager.registerCustomEffect(new ThunderStormEffect());
        break;
      case "FireExplosion":
        powerManager.registerCustomEffect(new FireExplosionEffect());
        break;
      case "MagicSpell":
        powerManager.registerCustomEffect(new MagicSpellEffect());
        break;
      case "WaterSplash":
        powerManager.registerCustomEffect(new WaterSplashEffect());
        break;
      case "ElectricArc":
        powerManager.registerCustomEffect(new ElectricArcEffect());
        break;
      case "DarkEnergy":
        powerManager.registerCustomEffect(new DarkEnergyEffect());
        break;
      case "JavaSvg":
        String svgcode =
            """
       <!-- Created with Ghost ide -->
      <svg
        xmlns="http://www.w3.org/2000/svg"

        viewBox="0 0 24 24"
        fill="none">
        <path
          stroke="#000000"
          stroke-width="1.5"
          d="M 6.17481 10.3331 C 4.96738 10.7407 4.22049 11.3039 4.22049 11.926 C 4.22049 12.7742 5.60897 13.5129 7.6601 13.8968 M 7.6601 13.8968 C 6.89833 14.2823 6.44271 14.7714 6.44271 15.3037 C 6.44271 16.5473 8.93001 17.5554 11.9983 17.5554 C 12.7884 17.5554 13.54 17.4886 14.2205 17.3681 M 7.6601 13.8968 C 8.61629 14.0758 9.71648 14.1778 10.8872 14.1778 C 12.5946 14.1778 14.1521 13.9609 15.3316 13.6044 M 16.4427 10.1242 C 15.031 10.5413 13.0635 10.8001 10.8872 10.8001 C 6.5916 10.8001 3.10938 9.7919 3.10938 8.54827 C 3.10938 7.58798 5.18563 6.76809 8.10937 6.44434"
          stroke-linecap="round"
          stroke-linejoin="round" />
        <path
          stroke="#000000"
          stroke-width="1.5"
          d="M 22 19.0699 C 22 20.6882 17.5228 22 12 22 C 6.47715 22 2 20.6882 2 19.0699 C 2 17.9195 3.70729 16.9239 7 16.4444"
          stroke-linecap="round" />
        <path
          stroke="#000000"
          stroke-width="1.5"
          d="M 18.7591 8.78799 C 22.9744 7.69436 23.5765 14.2562 17.5547 16.4438"
          stroke-linecap="round"
          stroke-linejoin="round" />
        <path
          stroke="#000000"
          stroke-width="1.5"
          d="M 17.5582 2 C 16.8173 2.12346 15.4246 2.81481 15.7802 4.59259 C 16.1358 6.37037 15.6322 7.30864 15.3359 7.55556"
          stroke-linecap="round"
          stroke-linejoin="round" />
        <path
          stroke="#000000"
          stroke-width="1.5"
          d="M 13.1128 2 C 12.372 2.14815 10.9793 2.97778 11.3349 5.11111 C 11.6905 7.24444 11.1869 7.81482 10.8906 8.11111"
          stroke-linecap="round"
          stroke-linejoin="round" />
      </svg>
       """;
        powerManager.registerCustomEffect(new CustomSvgEffect("Ghostsvg", svgcode));
        break;
    }
  }

  private String getSelectedEffectName() {
    if (listeffectmodel != null && !listeffectmodel.isEmpty()) {
      for (EffectModel model : listeffectmodel) {
        if (model.getUsingmod()) {
          return model.getNameeffect();
        }
      }
    }
    return "ThunderStorm";
  }

  private void loadEffectSettings() {
    try {
      File jsonFile = new File("/storage/emulated/0/GhostWebIDE/plugins/effect/data/model.json");
      if (jsonFile.exists()) {
        String jsonContent = FileUtil.readFile(jsonFile.getAbsolutePath());
        listeffectmodel =
            new Gson().fromJson(jsonContent, new TypeToken<List<EffectModel>>() {}.getType());

        if (listeffectmodel != null && !listeffectmodel.isEmpty()) {
          for (int i = 0; i < listeffectmodel.size(); i++) {
            if (listeffectmodel.get(i).getUsingmod()) {
              selectedEffectIndex = i;
              break;
            }
          }
        }
      } else {
        createDefaultEffectList();
      }
    } catch (Exception e) {
      createDefaultEffectList();
    }
  }

  private void createDefaultEffectList() {
    listeffectmodel = new ArrayList<>();

    String[] effects = {
      "ThunderStorm",
      "FireExplosion",
      "MagicSpell",
      "WaterSplash",
      "ElectricArc",
      "DarkEnergy",
      "JavaSvg"
    };

    for (int i = 0; i < effects.length; i++) {
      EffectModel model = new EffectModel();
      model.setNameeffect(effects[i]);
      model.setUsingmod(i == 0);
      listeffectmodel.add(model);
    }

    saveEffectSettings();
  }

  private void saveEffectSettings() {
    try {
      if (listeffectmodel != null) {
        for (int i = 0; i < listeffectmodel.size(); i++) {
          listeffectmodel.get(i).setUsingmod(i == selectedEffectIndex);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonContent = gson.toJson(listeffectmodel);
        FileUtil.writeFile(
            "/storage/emulated/0/GhostWebIDE/plugins/effect/data/model.json", jsonContent);
      }
    } catch (Exception e) {
      Log.e("PLUGIN_DEBUG", "Error saving JSON: " + e.getMessage());
    }
  }

  private void showEffectSelectionDialog() {
    if (powerManager == null || currentActivity == null) return;

    String[] effectNames = {
      "ThunderStorm - Lightning and rain",
      "FireExplosion - Explosive fire particles",
      "MagicSpell - Magical sparkling effects",
      "WaterSplash - Water droplets and splashes",
      "ElectricArc - Electric arcs and sparks",
      "DarkEnergy - Dark energy particles",
      "JavaSvg"
    };

    new MaterialAlertDialogBuilder(currentActivity)
        .setTitle("Select Effect")
        .setSingleChoiceItems(
            effectNames,
            selectedEffectIndex,
            (dialog, which) -> {
              selectedEffectIndex = which;
            })
        .setPositiveButton(
            "Save",
            (dialog, which) -> {
              saveEffectSettings();
              registerSelectedEffect();
              DataUtil.showMessage(
                  currentActivity,
                  "Effect saved: " + effectNames[selectedEffectIndex].split(" - ")[0]);
            })
        .setNeutralButton(
            "Test",
            (dialog, which) -> {
              if (powerManager != null && editor != null) {
                float x = (float) (Math.random() * 500) + 100;
                float y = (float) (Math.random() * 500) + 100;
                powerManager.spawnCustomEffect(getSelectedEffectName(), x, y);
              }
            })
        .setNegativeButton("Cancel", null)
        .show();
  }

  @Override
  public String setName() {
    return "Editor Effects Plugin";
  }

  @Override
  public boolean hasuseing() {
    return true;
  }

  @Override
  public void getFileManagerAc(FileManagerActivity activity) {}

  @Override
  public String langModel() {
    return ".kts,.html,.js,.kt,.java,.dart,.css,.c,.cpp";
  }

  class EffectModel {
    private String nameeffect;
    private boolean usingmod;

    public String getNameeffect() {
      return this.nameeffect;
    }

    public void setNameeffect(String nameeffect) {
      this.nameeffect = nameeffect;
    }

    public boolean getUsingmod() {
      return this.usingmod;
    }

    public void setUsingmod(boolean usingmod) {
      this.usingmod = usingmod;
    }
  }
}
