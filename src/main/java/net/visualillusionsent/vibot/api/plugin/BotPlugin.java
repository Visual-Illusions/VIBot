/* 
 * Copyright 2012 Visual Illusions Entertainment.
 *  
 * This file is part of VIBot.
 *
 * VIBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIBot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with VIUtils.
 * If not, see http://www.gnu.org/licenses/lgpl.html
 */
package net.visualillusionsent.vibot.api.plugin;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UtilityException;
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
    private PropertiesFile plugin_cfg = null;
    private PropertiesFile plugin_props = null;

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
            version = "0.0";
        }
        return version;
    }

    public final Manifest getPluginManifest() {
        String jarpath = "plugins/".concat(getName()).concat(".jar");
        Manifest toRet = null;
        VIBotException vibe = null;
        JarFile jar = null;
        try {
            jar = new JarFile(jarpath);
            toRet = jar.getManifest();
        }
        catch (Exception e) {
            vibe = new VIBotException("Unable to retrieve Manifest! (Missing?)", e);
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException e) {}
            }
            if (vibe != null) {
                throw vibe;
            }
        }
        return toRet;
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
        String build = "0";
        version = "0";
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
            version = "0";
        }
        if (build == null) {
            build = "0";
        }
        version = version.concat(".").concat(build);
    }

    public final PropertiesFile getPluginConfiguration() throws UtilityException {
        if (plugin_cfg == null) {
            plugin_cfg = new PropertiesFile(String.format("plugins/%s.jar", this.getName()), "plugin.cfg");
        }
        return plugin_cfg;
    }

    public final PropertiesFile getPluginProperties() throws UtilityException {
        if (plugin_props == null) {
            plugin_props = new PropertiesFile(String.format("plugins/%s/%s.ini", this.getName(), this.getName().toLowerCase()));
        }
        return plugin_props;
    }

    /**
     * Used to get plugin specific configuration files
     * 
     * @param file
     * @return
     * @throws UtilityException
     */
    public final PropertiesFile getPluginConfigFile(String file) throws UtilityException {
        return new PropertiesFile(String.format("plugins/%s/%s.ini", getName(), file));
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
