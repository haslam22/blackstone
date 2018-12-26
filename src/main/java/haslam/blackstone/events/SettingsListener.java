package haslam.blackstone.events;

/**
 * Listener interface for receiving a notification when settings have changed.
 */
public interface SettingsListener {

    /**
     * Called when the settings have changed.
     */
    void settingsChanged();

}
