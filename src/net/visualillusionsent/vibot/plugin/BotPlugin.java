package net.visualillusionsent.vibot.plugin;

/**
 * Bot Plugin interface<br>
 * Extend this class to create plugins for VIBot
 * 
 * @author Jason (darkdiplomat)
 */
public abstract class BotPlugin {
    private String name = "BotPluginImpl";
    private boolean enabled = true;
    protected String version = null;
    private BotClassLoader loader = null;

    public BotPlugin() {
        this.name = this.getClass().getSimpleName();
    }

    /**
     * Runs the Plugins enable code to check if enabling can happen
     * 
     * @return {@code true} if successfully enabled, {@code false} if failed its
     *         checks
     */
    public abstract boolean enable();

    /**
     * Disables the plugin
     */
    public abstract void disable();

    /**
     * Plugin is loaded and may now register hooks
     */
    public abstract void initialize();

    /**
     * Returns true if this plugin is enabled
     * 
     * @return boolean enabled
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles whether or not this plugin is enabled
     * 
     * @return boolean enabled
     */
    final boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }

    /**
     * Returns the name of this plugin
     * 
     * @return String name
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the version of this plugin
     * 
     * @return version
     */
    public final String getVersion() {
        if (version == null) {
            version = "*UNKNOWN-VERSION*";
        }
        return version;
    }

    final void setBotClassLoader(BotClassLoader loader) {
        this.loader = loader;
    }

    final void close() {
        loader.close();
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof BotPlugin)) {
            return false;
        }
        BotPlugin toCheck = (BotPlugin) obj;
        if (!toCheck.getName().equals(name)) {
            return false;
        }
        if (!toCheck.getVersion().equals(version)) {
            return false;
        }
        return true;
    }
}
