package net.visualillusionsent.vibot.plugin;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Bot Plugin interface<br>
 * Extend this class to create plugins for VIBot
 * 
 * @author Jason (darkdiplomat)
 */
public abstract class BotPlugin {
    private String name;
    private boolean enabled = true;
    protected String version = null;
    private BotClassLoader loader = null;

    public BotPlugin() {}

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
        if (name == null) {
            this.name = this.getClass().getSimpleName();
        }
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

    public final Manifest getPluginManifest() throws VIBotException {
        String jarpath = "plugins/".concat(getName()).concat(".jar");
        try {
            @SuppressWarnings("resource")
            JarFile jar = new JarFile(jarpath);
            return jar.getManifest();
        }
        catch (Exception e) {
            throw new VIBotException("Unable to retrieve Manifest for Plugin: ".concat(getName()).concat("! (Missing?)"), e);
        }
    }

    final void setBotClassLoader(BotClassLoader loader) {
        this.loader = loader;
    }

    final void close() {
        loader.close();
    }

    protected final void sendDefaultEnabledMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" enabled!"));
    }

    protected final void sendDefaultDisabledMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" disabled!"));
    }

    protected final void sendDefaultInitializeMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" initialized!"));
    }

    protected final void generateVersion() {
        String build = "*";
        version = "*";
        try {
            Manifest manifest = getPluginManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            version = mainAttribs.getValue("Specification-Version");
            build = mainAttribs.getValue("Implementation-Build");
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }
        if (version == null) {
            version = "*";
        }
        if (build == null) {
            build = "*";
        }
        version = version.concat(".").concat(build);
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof BotPlugin)) {
            return false;
        }
        BotPlugin toCheck = (BotPlugin) obj;
        if (!toCheck.getName().equals(getName())) {
            return false;
        }
        if (!toCheck.getVersion().equals(version)) {
            return false;
        }
        return true;
    }
}
