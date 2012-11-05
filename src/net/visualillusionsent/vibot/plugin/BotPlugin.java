package net.visualillusionsent.vibot.plugin;

public abstract class BotPlugin {
    private String name = "";
    private boolean enabled = true;
    
    
    protected float version = 0F;

    /**
     * Enables the plugin
     */
    public abstract void enable();

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
    protected boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles whether or not this plugin is enabled
     * 
     * @return boolean enabled
     */
    protected boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }

    /**
     * Sets the name of this plugin
     * 
     * @param String
     *            name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this plugin
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    protected float getVersion() {
        return version;
    }

    protected void setVersion(float version) {
        this.version = version;
    }
}
