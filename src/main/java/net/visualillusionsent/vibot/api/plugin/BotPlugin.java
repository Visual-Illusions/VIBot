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
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UpdateException;
import net.visualillusionsent.utils.Updater;
import net.visualillusionsent.utils.UtilityException;
import net.visualillusionsent.utils.VersionChecker;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.events.BaseEvent;
import net.visualillusionsent.vibot.api.events.EventPriority;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Bot Plugin interface<br>
 * Extend this class to create plugins for {@link VIBot}<br>
 * When creating a plugin, you should include a constructor and call super()<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public abstract class BotPlugin {

    /**
     * The name of the Jar File for the {@code BotPlugin}
     */
    private String jarName;

    /**
     * Whether the {@code BotPlugin} is enabled or not
     */
    private boolean enabled = true;

    /**
     * The URLClassLoader for the {@code BotPlugin}
     */
    private URLClassLoader loader;

    /**
     * The {@code plugin.cfg} {@link PropertiesFile} for the {@code BotPlugin}
     */
    private PropertiesFile plugin_cfg;

    /**
     * The {@link PropertiesFile} for the {@code BotPlugin}
     */
    private PropertiesFile plugin_props;

    /**
     * Creates a VersionChecker instance for the {@code BotPlugin}, if the {@code BotPlugin} supports version checking.
     */
    private VersionChecker vercheck;

    /**
     * Creates a Updater intance for the {@code BotPlugin}, it the {@code BotPlugin} supports updating.
     */
    private Updater update;

    /**
     * The version of the {@code BotPlugin}, either auto-generated from the Manifest key: Version or set programmically
     */
    protected String version;

    /**
     * The build of the {@code BotPlugin}, either auto-generated from the Manifest key: Build or set programmically
     */
    protected String build;

    /**
     * The beta status of the {@code BotPlugin}, either auto-generated from the Manifest key: isBeta or set programmically
     */
    protected boolean beta;

    /**
     * The release canidate status of the {@code BotPlugin}, either auto-generated from the Manifest key: isRC or set programmically
     */
    protected boolean rc;

    /**
     * The name of the {@code BotPlugin}
     */
    protected String name;

    /**
     * Constructs a new {@code BotPlugin}
     */
    public BotPlugin() {
        getName();
        getJarName();
        readManifestInfo();
        createVersionChecker();
        createUpdater();
    }

    public BotPlugin(Object WAT) {
        if (!this.getClass().isAssignableFrom(VIBot.checkFake())) {
            throw new IllegalStateException("WAT Constructor should not be used!");
        }
    }

    /**
     * Runs the {@code BotPlugin} enable code to check if enabling can happen<br>
     * Typically used to load properties and other dependencies.<br>
     * Use: {@link #initialize()} to initialize {@link BaseCommand}s and {@link BaseEvent}s
     * 
     * @return {@code true} if successfully enabled; {@code false} if failed its
     *         checks
     */
    public abstract boolean enable();

    /**
     * Disables the {@code BotPlugin}
     */
    public abstract void disable();

    /**
     * Used to register {@link BaseCommand}s and {@link BaseEvent}s for the {@code BotPlugin}
     */
    public abstract void initialize();

    /**
     * Returns true if this {@code BotPlugin} is enabled
     * 
     * @return {@code true} if enabled; {@code false} otherwise
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles whether or not this {@code BotPlugin} is enabled
     * 
     * @return {@code true} if enabled; {@code false} otherwise
     */
    final boolean toggleEnabled() {
        enabled = !enabled;
        return enabled;
    }

    /**
     * Returns the name of the {@code BotPlugin}'s plugin extension class
     * 
     * @return the name of the {@code BotPlugin}'s plugin extension class
     */
    public final String getName() {
        if (name == null) {
            this.name = this.getClass().getSimpleName();
        }
        return name;
    }

    /**
     * Gets the name of the {@code BotPlugin}'s jar file
     * 
     * @return the name of the {@code BotPlugin}'s jar file; {@code null} if an exception occured
     */
    public final String getJarName() {
        if (jarName == null) {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            try {
                String path = codeSource.getLocation().toURI().getPath();
                jarName = path.substring(path.lastIndexOf("/") + 1, path.length());
            }
            catch (Exception e) {}
        }
        return jarName;
    }

    /**
     * Gets the version of this {@code BotPlugin}
     * 
     * @return version
     */
    public final String getVersioning() {
        if (version == null) {
            version = "UNDEFINED";
        }
        return version;
    }

    /**
     * Gets the build of this {@code BotPlugin}
     * 
     * @return build
     */
    public final String getBuild() {
        if (build == null) {
            build = "UNDEFINED";
        }
        return build;
    }

    public final String getVersion() {
        return getVersioning().concat("b").concat(getBuild());
    }

    /**
     * Gets the {@code BotPlugin}'s {@code MANIFEST.MF} file
     * 
     * @return manifest of the {@code BotPlugin}
     * @throws VIBotException
     *             if an exception occurrs while trying to get the manifest
     */
    protected final Manifest getPluginManifest() throws VIBotException {
        String jarpath = "plugins/".concat(getJarName());
        Manifest toRet = null;
        VIBotException vibe = null;
        JarFile jar = null;
        try {
            jar = new JarFile(jarpath);
            toRet = jar.getManifest();
        }
        catch (Exception e) {
            vibe = new VIBotException("Unable to retrieve Plugin Manifest! (Missing?): ".concat(getName()), e);
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

    /**
     * Logs a default enabled message as: "{Name} v{version} enabled!"
     */
    protected final void sendDefaultEnabledMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" enabled!"));
    }

    /**
     * Logs a default disabled message as: "{Name} v{version} disabled!"
     */
    protected final void sendDefaultDisabledMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" disabled!"));
    }

    /**
     * Logs a default disabled message as: "{Name} v{version} initialized!"
     */
    protected final void sendDefaultInitializeMessage() {
        BotLogMan.info(getName().concat(" v").concat(getVersion()).concat(" initialized!"));
    }

    /**
     * Generates the Version from the {@code BotPlugin}'s manifest entries Version and Build<br>
     * If the manifest does not contain those values it defaults to "UNDEFINED"<br>
     * Format wise it will be displayed as #.#b#
     */
    protected final void readManifestInfo() {
        try {
            Manifest manifest = getPluginManifest();
            Attributes mainAttribs = manifest.getMainAttributes();
            version = mainAttribs.getValue("Version");
            build = mainAttribs.getValue("Build");
            beta = Boolean.valueOf(mainAttribs.getValue("isBeta"));
            rc = Boolean.valueOf(mainAttribs.getValue("isRC"));
        }
        catch (Exception e) {
            BotLogMan.warning(e.getMessage());
        }
    }

    /**
     * Gets the {@code BotPlugin}'s internal plugin.cfg file<br>
     * Typically contains special information about the {@code BotPlugin}'s events' {@link EventPriority}
     * 
     * @return {@code BotPlugin}'s plugin.cfg file as a {@link PropertiesFile}
     * @throws UtilityException
     *             if an exception occurs while trying to get the file
     */
    public final PropertiesFile getPluginConfiguration() throws UtilityException {
        if (plugin_cfg == null) {
            plugin_cfg = new PropertiesFile(String.format("plugins/%s.jar", this.getName()), "plugin.cfg");
        }
        return plugin_cfg;
    }

    /**
     * Gets the default {@link PropertiesFile} for the {@code BotPlugin}<br>
     * Generated and stored in ../plugins/{pluginname}/{pluginname}.ini
     * 
     * @return {@code BotPlugin}'s {@link PropertiesFile}
     * @throws UtilityException
     */
    public final PropertiesFile getPluginProperties() throws UtilityException {
        if (plugin_props == null) {
            plugin_props = new PropertiesFile(String.format("plugins/%s/%s.ini", this.getName(), this.getName().toLowerCase()));
        }
        return plugin_props;
    }

    /**
     * Used to get plugin specific configuration files<br>
     * Generated and stored in ../plugins/{pluginname}/{file}
     * 
     * @param filename
     *            the name of the file for the file with extension
     * @return a {@link PropertiesFile} of the specified file name
     * @throws UtilityException
     */
    public final PropertiesFile getPluginConfigFile(String filename) throws UtilityException {
        return new PropertiesFile(String.format("plugins/%s/%s", getName(), filename));
    }

    public final boolean supportsversionChecker() {
        return vercheck != null;
    }

    public final Boolean isLatestVersion() {
        return vercheck.isLatest();
    }

    public final String getUpdateMessage() {
        if (vercheck != null) {
            return vercheck.getUpdateAvailibleMessage();
        }
        return null; //Version Checker not supported
    }

    private final void createVersionChecker() {
        try {
            Manifest mf = getPluginManifest();
            Attributes attr = mf.getMainAttributes();
            String url = attr.getValue("Version-Check-URL");

            if (url != null && !this.version.equals("UNDEFINED")) {
                vercheck = new VersionChecker(this.getName(), this.getVersioning(), this.getBuild(), url, beta, rc);
            }
        }
        catch (Exception ex) {} //Not worried about errors here 
    }

    public final boolean runUpdate() throws UpdateException {
        if (update != null) {
            if (update.performUpdate()) {
                return BotPluginLoader.reloadBotPlugin(this.getName());
            }
        }
        return false;
    }

    private final void createUpdater() {
        try {
            Manifest mf = getPluginManifest();
            Attributes attr = mf.getMainAttributes();
            String url = attr.getValue("Update-URL");

            if (url != null && !this.version.equals("UNDEFINED")) {
                update = new Updater(url, String.format("plugins/%s", this.getJarName()), this.getName());
            }
        }
        catch (Exception ex) {} //Not worried about errors here 
    }

    /**
     * Sets the {@link URLClassLoader} for the {@code BotPlugin} which is used later to close out the {@code BotPlugin} jar file
     * 
     * @param loader
     *            the {@code BotPlugin}'s {@link URLClassLoader}
     */
    final void setClassLoader(URLClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Gets the {@link URLClassLoader} for the {@code BotPlugin}
     * 
     * @return the {@code BotPlugin}'s {@link URLClassLoader}
     */
    final URLClassLoader getLoader() {
        return loader;
    }

    /**
     * Closes out the {@code BotPlugin}
     * 
     * @throws IOException
     *             should one occur while trying to close the {@code BotPlugin}
     */
    final void close() throws IOException {
        loader.close();
    }

    /**
     * Checks is an {@link Object} is equal to the {@code BotPlugin}
     * 
     * @return {@code true} if equal; {@code false} otherwise
     * @see Object#equals(Object)
     */
    public final boolean equals(Object obj) {
        if (!(obj instanceof BotPlugin)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        BotPlugin toCheck = (BotPlugin) obj;
        if (!toCheck.getName().equals(getName())) {
            return false;
        }
        if (!toCheck.getVersioning().equals(version)) {
            return false;
        }
        if (!toCheck.loader.equals(loader)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the {@code BotPlugin} as "%Name% v%Version%"
     * 
     * @return string representation of the {@code BotPlugin}
     * @see Object#toString()
     */
    @Override
    public final String toString() {
        return String.format("%s v%s", getName(), getVersion());
    }

    /**
     * Returns a hash code value for the {@code BotPlugin}.
     * 
     * @see Object#hashCode()
     */
    @Override
    public final int hashCode() {
        int hash = 8;
        hash = 44 * hash + (getName() != null ? getName().hashCode() : 0);
        hash = 44 * hash + (getVersion() != null ? getVersion().hashCode() : 0);
        hash = 44 * hash + loader.hashCode();
        return hash;
    }
}
