package haslam.blackstone.gui.minimal;

import haslam.blackstone.core.GameController;
import haslam.blackstone.core.GameSettings;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.LabeledMatchers;

/**
 * A very basic test to ensure the GUI boots and responds to clicks.
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
}