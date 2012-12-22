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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.visualillusionsent.utils.SystemUtils;
import net.visualillusionsent.vibot.CommandParser;
import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.plugin.events.EventManager;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.irc.Colors;
import net.visualillusionsent.vibot.io.logging.BotLogMan;

/**
 * Bot Plugin Loader
 * <p>
 * Used to load, enable, disable, reload plugins.
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
public final class BotPluginLoader {

    /**
     * The list of {@link BotPlugin}s
     */
    private List<BotPlugin> plugins;

    /**
     * Whether the {@code BotPluginLoader} has been loaded
     */
    private boolean loaded = false;

    /**
     * Synchronizing lock object
     */
    private static final Object lock = new Object();

    /**
     * {@code BotPluginLoader} instance
     */
    private static BotPluginLoader instance;

    /**
     * Creates a plugin loader
     */
    private BotPluginLoader() {
        plugins = new ArrayList<BotPlugin>();
    }

    public final static BotPluginLoader getInstance() {
        if (instance == null) {
            instance = new BotPluginLoader();
        }
        return instance;
    }

    /**
     * Initially loads all plugins if not already loaded
     */
    public final void loadPlugins() {
        if (loaded) {
            return;
        }

        BotLogMan.info("Loading plugins...");
        String[] plugins = BotConfig.getPlugins();
        int load = 0;
        if (plugins != null && plugins.length > 0) {
            for (String plugin : plugins) {
                if (plugin.trim().isEmpty()) {
                    //No name to load
                    continue;
                }
                else if (getPlugin(plugin.trim()) != null) {
                    //Duplicate Entry
                    continue;
                }
                else if (loadPlugin(plugin.trim())) {
                    //Loaded
                    load++;
                }
            }
        }
        BotLogMan.info("Loaded ".concat(String.valueOf(load)).concat(" plugins."));
        loaded = true;
    }

    /**
     * Loads the specified plugin
     * 
     * @param pluginName
     *            name of plugin to load
     * @return if the operation was successful
     */
    public final boolean loadPlugin(String pluginName) {
        if (getPlugin(pluginName) != null) {
            // Already exists, don't load again.
            return false;
        }
        return load(pluginName);
    }

    /**
     * Reloads the specified {@link BotPlugin}
     * 
     * @param pluginName
     *            name of plugin to reload
     * @return if the operation was successful
     */
    public final boolean reloadPlugin(String pluginName) {
        BotPlugin toNull = getPlugin(pluginName);
        if (toNull != null) {
            if (toNull.isEnabled()) {
                toNull.disable();
            }
        }
        synchronized (lock) {
            plugins.remove(toNull);
        }
        try {
            toNull.close();
        }
        catch (Exception e) {}

        toNull = null;

        return load(pluginName);
    }

    /**
     * Loads a {@link BotPlugin}
     * 
     * @param pluginName
     *            the name of the {@link BotPlugin} to be loaded
     * @return {@code true} if succesfully loaded and initialized; {@code false} otherwise
     */
    private final boolean load(String pluginName) {
        BotPlugin plugin = null;
        String filepath = String.format("plugins/%s.jar", pluginName);
        try {
            File pluginfile = new File(filepath);

            if (!pluginfile.exists()) {
                BotLogMan.severe("Failed to find plugin file: plugins/".concat(pluginName).concat(".jar. Please ensure the file exists"));
                return false;
            }
            URLClassLoader loader = null;

            try {
                loader = new URLClassLoader(new URL[] { pluginfile.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            }
            catch (MalformedURLException ex) {
                BotLogMan.severe("Exception while loading class", ex);
                return false;
            }

            String filepathtemp = getPluginClassPath(pluginfile.getAbsolutePath());
            filepath = filepathtemp != null ? filepathtemp : pluginName;

            Class<?> pluginclazz = Class.forName(filepath, true, loader);
            plugin = (BotPlugin) pluginclazz.newInstance();
            plugin.setClassLoader(loader);
            if (plugin.enable()) {
                synchronized (lock) {
                    plugins.add(plugin);
                    plugin.initialize();
                }
            }

        }
        catch (Throwable ex) {
            BotLogMan.severe("Exception while loading plugin (".concat(filepath).concat(")"), ex);
            if (plugin != null && plugin.isEnabled()) {
                plugin.toggleEnabled();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the {@link BotPlugin}'s main class
     * 
     * @param jarpath
     *            the path to the {@link BotPlugin}
     * @return the {@link BotPlugin}'s main class
     * @throws VIBotException
     *             if unable to read the manifest for the plugin
     */
    private final String getPluginClassPath(String jarpath) throws VIBotException {
        String value = null;
        JarFile jar = null;
        try {
            jar = new JarFile(jarpath);
            Manifest manifest = jar.getManifest();
            Attributes attr = manifest.getMainAttributes();
            value = attr.getValue("Plugin-Class");
            if (value == null) {
                BotLogMan.warning("Was unable to locate Plugin-Class attribute for Plugin: '".concat(jarpath).concat("'").concat(SystemUtils.LINE_SEP).concat(" Proceeding with assumption that default package is used..."));
            }
        }
        catch (Exception e) {
            throw new VIBotException("Was unable to read Manifest for Plugin: '".concat(jarpath).concat("'"));
        }
        try {
            jar.close();
        }
        catch (IOException e) {}
        return value;
    }

    /**
     * Returns the specified {@link BotPlugin}
     * 
     * @param name
     *            name of {@link BotPlugin}
     * @return {@link BotPlugin} if found; {@code null} otherwise
     */
    public final BotPlugin getPlugin(String name) {
        synchronized (lock) {
            for (BotPlugin plugin : plugins) {
                if (plugin.getName().equalsIgnoreCase(name)) {
                    return plugin;
                }
            }
        }
        return null;
    }

    /**
     * Returns a string list of {@link BotPlugin}s
     * 
     * @return String of {@link BotPlugin}s
     */
    public final String getPluginList() {
        StringBuilder sb = new StringBuilder();

        synchronized (lock) {
            for (BotPlugin plugin : plugins) {
                sb.append(plugin.isEnabled() ? Colors.GREEN : Colors.RED);
                sb.append(plugin.getName());
                sb.append(" v");
                sb.append(plugin.getVersion());
                sb.append(Colors.NORMAL + ", ");
            }
        }
        String str = sb.toString();

        if (str.length() > 1) {
            return str.substring(0, str.length() - 1);
        }
        else {
            return "No plugins installed...";
        }
    }

    /**
     * Enables the specified {@link BotPlugin} (Or adds and enables it)
     * 
     * @param name
     *            name of the {@link BotPlugin} to enable
     * @return {@code true} if enabled; {@code false} otherwise
     */
    public final boolean enablePlugin(String name) {
        BotPlugin plugin = getPlugin(name);

        if (plugin != null) {
            if (!plugin.isEnabled()) {
                plugin.toggleEnabled();
                if (plugin.enable()) {
                    plugin.initialize();
                    return true;
                }
            }
            return false;
        }
        else { // New plugin, perhaps?
            File file = new File("plugins/".concat(name).concat(".jar"));

            if (file.exists()) {
                return loadPlugin(name);
            }
            else {
                return false;
            }
        }
    }

    /**
     * Disables specified {@link BotPlugin}
     * 
     * @param name
     *            name of the {@link BotPlugin} to disable
     */
    public final void disablePlugin(String name) {
        BotPlugin plugin = getPlugin(name);

        if (plugin != null) {
            if (plugin.isEnabled()) {
                plugin.toggleEnabled();
                plugin.disable();
                EventManager.getInstance().removePluginHooks(plugin);
                CommandParser.getInstance().removePluginCommands(plugin);
            }
        }
    }

    /**
     * Disables all {@link BotPlugin}<br>
     * Typically when the {@link VIBot} is shutting down
     * 
     * @param bot
     *            the {@link VIBot} instance
     */
    public final void disableAll(VIBot bot) {
        for (BotPlugin plugin : plugins) {
            if (plugin.isEnabled()) {
                plugin.toggleEnabled();
                plugin.disable();
                EventManager.getInstance().removePluginHooks(plugin);
                CommandParser.getInstance().removePluginCommands(plugin);
            }
        }
    }
}
