package haslam.blackstone.gui.minimal;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameSettings;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GUI integration tests. This boots up the application and simulates mouse clicks, relying on the testfx
 * library. All GUI functionality can be tested here.
 */
@Tag("GUI")
public class MinimalGUITest extends ApplicationTest {

    /**
     * Will be called with {@code @Before} semantics, i. e. before each test method.
     */
    @Override
    public void start(Stage stage) {
        MinimalGUI minimalGUI = new MinimalGUI();
        minimalGUI.launch(new GameController(GameSettings.withDefaults()),
                stage);
    }

    @Test
    public void shouldContainControlButtons() {
        FxAssert.verifyThat("#playButton", LabeledMatchers.hasText("New Game"));
        FxAssert.verifyThat("#stopButton", LabeledMatchers.hasText("Stop Game"));
        FxAssert.verifyThat("#undoButton", LabeledMatchers.hasText("Undo"));
        FxAssert.verifyThat("#settingsButton", LabeledMatchers.hasText("Settings"));
    }

    @Test
    public void shouldStartGame() {
        clickOn("#playButton");
        FxAssert.verifyThat("#playButton", (Button b) -> !b.isVisible());
        FxAssert.verifyThat("#stopButton", (Button b) -> b.isVisible());
        FxAssert.verifyThat("#settingsButton", (Button b) -> b.isDisabled());
    }

    @Test
    public void shouldShowSettings() {
        clickOn("#settingsButton");
        Set<Stage> activeStages = Stage.getWindows().stream()
                .filter(Window::isShowing)
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getTitle().contains("Settings"))
                .collect(Collectors.toSet());
        assertEquals(1, activeStages.size());
    }

    @Test
    public void shouldNotShowMultipleSettingStages() {
        clickOn("#settingsButton");
        clickOn("#settingsButton");
        clickOn("#settingsButton");
        Set<Stage> activeStages = Stage.getWindows().stream()
                .filter(Window::isShowing)
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .filter(stage -> stage.getTitle().contains("Settings"))
                .collect(Collectors.toSet());
        assertEquals(1, activeStages.size());
    }

    @Test
    public void shouldAllowChangingMoveTimeSettings() {
        clickOn("#settingsButton");
        clickOn("#gameTimeSpinner");
        type(KeyCode.BACK_SPACE, KeyCode.BACK_SPACE, KeyCode.DIGIT1, KeyCode.DIGIT0);
        clickOn("#moveTimingCheckBox");
        clickOn("#moveTimeSpinner");
        type(KeyCode.BACK_SPACE, KeyCode.BACK_SPACE, KeyCode.DIGIT2, KeyCode.DIGIT3);

        // Have to click something else to get the value to update after typing. Probably a more elegant way to do this.
        clickOn("#gameTimeSpinner");

        FxAssert.verifyThat("#gameTimeSpinner", (Spinner<Integer> b) -> b.getValue() == 10);
        FxAssert.verifyThat("#moveTimeSpinner", (Spinner<Integer> b) -> b.getValue() == 23);
    }
}