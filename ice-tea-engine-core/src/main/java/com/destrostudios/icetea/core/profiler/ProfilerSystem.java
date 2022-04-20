package com.destrostudios.icetea.core.profiler;

import com.destrostudios.icetea.core.font.BitmapFont;
import com.destrostudios.icetea.core.font.BitmapText;
import com.destrostudios.icetea.core.input.KeyEvent;
import com.destrostudios.icetea.core.input.KeyListener;
import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class ProfilerSystem extends LifecycleObject implements KeyListener {

    @Setter
    private float interval = 0;
    @Setter
    private ProfilerOrder order = ProfilerOrder.AVERAGE_DURATION;
    @Setter
    private boolean showClasses = true;
    @Setter
    private int displayedResults = 30;
    private BitmapText[] bitmapTexts = new BitmapText[5];
    private String[] tmpColumns = new String[5];
    private float timeSinceCollecting = Float.MAX_VALUE;

    @Override
    protected void init() {
        super.init();
        BitmapFont bitmapFont = application.getAssetManager().loadBitmapFont("com/destrostudios/icetea/core/fonts/Verdana_12.fnt");
        for (int i = 0; i < bitmapTexts.length; i++) {
            bitmapTexts[i] = new BitmapText(bitmapFont, "");
            application.getGuiNode().add(bitmapTexts[i]);
        }
        application.getInputManager().addKeyListener(this);
    }

    @Override
    protected void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        timeSinceCollecting += tpf;
        if (timeSinceCollecting > interval) {
            updateTexts();
            application.getProfiler().clear();
            timeSinceCollecting = 0;
        }
    }

    private String formatDuration(long duration) {
        return duration + " ns";
    }

    @Override
    public void onKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == GLFW_PRESS) {
            switch (keyEvent.getKey()) {
                case GLFW_KEY_F1:
                    order = ProfilerOrder.values()[(order.ordinal() + 1) % ProfilerOrder.values().length];
                    updateTexts();
                    break;
                case GLFW_KEY_F2:
                    showClasses = !showClasses;
                    updateTexts();
                    break;
            }
        }
    }

    private void updateTexts() {
        List<ProfilerDurations> sortedResults = application.getProfiler().getSortedResults(order).stream()
                .filter(result -> result.getKey().contains("#") == !showClasses)
                .collect(Collectors.toList());
        float x = 0;
        for (int i = 0; i < bitmapTexts.length; i++) {
            switch (i) {
                case 0:
                    tmpColumns[i] ="Most expensive durations, sorted by " + order.name();
                    break;
                case 1:
                    tmpColumns[i] = "Invocations";
                    break;
                case 2:
                    tmpColumns[i] = "Maximum";
                    break;
                case 3:
                    tmpColumns[i] = "Minimum";
                    break;
                case 4:
                    tmpColumns[i] = "Average";
                    break;
            }
            int resultIndex = 0;
            for (ProfilerDurations result : sortedResults) {
                tmpColumns[i] += "\n";
                switch (i) {
                    case 0:
                        tmpColumns[i] += result.getKey();
                        break;
                    case 1:
                        tmpColumns[i] += result.getInvocations();
                        break;
                    case 2:
                        tmpColumns[i] += formatDuration(result.getMaximumDuration());
                        break;
                    case 3:
                        tmpColumns[i] += formatDuration(result.getMinimumDuration());
                        break;
                    case 4:
                        tmpColumns[i] += formatDuration(result.getAverageDuration());
                        break;
                }
                resultIndex++;
                if (resultIndex >= displayedResults) {
                    break;
                }
            }
            bitmapTexts[i].setText(tmpColumns[i]);
            bitmapTexts[i].setLocalTranslation(new Vector3f(x, 0, 0));
            x += bitmapTexts[i].getTextWidth() + 20;
        }
    }

    @Override
    protected void cleanupInternal() {
        super.cleanupInternal();
        for (BitmapText bitmapText : bitmapTexts) {
            application.getGuiNode().remove(bitmapText);
            bitmapText.cleanup();
        }
    }
}
