package haslam.blackstone.gui.minimal;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameSettings;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

import static org.testfx.util.DebugUtils.saveScreenshot;

/**
 * A very basic test to ensure the GUI boots and responds to clicks.
 */
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
}