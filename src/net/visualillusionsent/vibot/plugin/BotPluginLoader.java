package net.visualillusionsent.vibot.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import net.visualillusionsent.vibot.Colors;
import net.visualillusionsent.vibot.Utils;
import net.visualillusionsent.vibot.commands.CommandParser;
import net.visualillusionsent.vibot.io.configuration.BotConfig;
import net.visualillusionsent.vibot.io.exception.VIBotException;
import net.visualillusionsent.vibot.io.logging.BotLogMan;
import net.visualillusionsent.vibot.plugin.hook.HookManager;

/**
 * BotPluginLoader - Used to load plugins, toggle them, etc.
 * 
 * @author Jason (darkdiplomat)
 */
public class BotPluginLoader {
    private List<BotPlugin> plugins = new ArrayList<BotPlugin>();
    private volatile boolean loaded = false;

    private static final Object lock = new Object();
    private static BotPluginLoader instance;

    /**
     * Creates a plugin loader
     */
    private BotPluginLoader() {}

    public static BotPluginLoader getInstance() {
        if (instance == null) {
            instance = new BotPluginLoader();
        }
        return instance;
    }

    /**
     * Initially loads all plugins if not already loaded
     */
    public void loadPlugins() {
        if (loaded) {
            return;
        }

        BotLogMan.info("Loading plugins...");
        String[] plugins = BotConfig.getPlugins();
        int load = 0;
        if (plugins != null && plugins.length > 0) {
            for (String plugin : plugins) {
                if (plugin.trim().isEmpty()) {
                    continue;
                }
                if (loadPlugin(plugin.trim())) {
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
    public boolean loadPlugin(String pluginName) {
        if (getPlugin(pluginName) != null) {
            return false; // Already exists.
        }
        return load(pluginName);
    }

    /**
     * Reloads the specified plugin
     * 
     * @param pluginName
     *            name of plugin to reload
     * @return if the operation was successful
     */
    public boolean reloadPlugin(String pluginName) {
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
            File file = new File("plugins/" + pluginName + ".jar");
            BotClassLoader child = null;
            child = new BotClassLoader(new URL[] { file.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            child.close();
        }
        catch (Exception e) {}

        toNull = null;

        return load(pluginName);
    }

    private boolean load(String pluginName) {
        String filepath = "plugins/" + pluginName + ".jar";
        try {
            File pluginfile = new File(filepath);

            if (!pluginfile.exists()) {
                BotLogMan.severe("Failed to find plugin file: plugins/" + pluginName + ".jar. Please ensure the file exists");
                return false;
            }
            BotClassLoader loader = null;

            try {
                loader = new BotClassLoader(new URL[] { pluginfile.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            }
            catch (MalformedURLException ex) {
                BotLogMan.severe("Exception while loading class", ex);
                return false;
            }

            String filepathtemp = getPluginClassPath(pluginfile.getAbsolutePath());
            filepath = filepathtemp != null ? filepathtemp : pluginName;

            Class<?> pluginclazz = Class.forName(filepath, true, loader);

            BotPlugin plugin = (BotPlugin) pluginclazz.newInstance();
            plugin.setBotClassLoader(loader);
            plugin.enable();

            synchronized (lock) {
                plugins.add(plugin);
                plugin.initialize();
            }

        }
        catch (Throwable ex) {
            BotLogMan.severe("Exception while loading plugin (" + filepath + ")", ex);
            return false;
        }
        return true;
    }

    private final String getPluginClassPath(String jarpath) throws VIBotException {
        String value = null;
        try {
            @SuppressWarnings("resource")
            JarFile jar = new JarFile(jarpath);
            Manifest manifest = jar.getManifest();
            Attributes attr = manifest.getMainAttributes();
            value = attr.getValue("Plugin-Class");
            if (value == null) {
                BotLogMan.warning("Was unable to locate Plugin-Class attribute for Plugin: '".concat(jarpath).concat("'").concat(Utils.LINE_SEP).concat(" Proceeding with assumption that default package is used..."));
            }
        }
        catch (Exception e) {
            throw new VIBotException("Was unable to read Manifest for Plugin: '".concat(jarpath).concat("'"));
        }
        return value;
    }

    /**
     * Returns the specified plugin
     * 
     * @param name
     *            name of plugin
     * @return plugin
     */
    public BotPlugin getPlugin(String name) {
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
     * Returns a string list of plugins
     * 
     * @return String of plugins
     */
    public String getPluginList() {
        StringBuilder sb = new StringBuilder();

        synchronized (lock) {
            for (BotPlugin plugin : plugins) {
                sb.append(plugin.isEnabled() ? Colors.GREEN : Colors.RED);
                sb.append(plugin.getName());
                sb.append(Colors.NORMAL + ", ");
            }
        }
        String str = sb.toString();

        if (str.length() > 1) {
            return str.substring(0, str.length() - 1);
        }
        else {
            return "Empty";
        }
    }

    /**
     * Enables the specified plugin (Or adds and enables it)
     * 
     * @param name
     *            name of plugin to enable
     * @return whether or not this plugin was enabled
     */
    public boolean enablePlugin(String name) {
        BotPlugin plugin = getPlugin(name);

        if (plugin != null) {
            if (!plugin.isEnabled()) {
                plugin.toggleEnabled();
                plugin.enable();
                plugin.initialize();
            }
        }
        else { // New plugin, perhaps?
            File file = new File("plugins/" + name + ".jar");

            if (file.exists()) {
                return loadPlugin(name);
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * Disables specified plugin
     * 
     * @param name
     *            name of the plugin to disable
     */
    public void disablePlugin(String name) {
        BotPlugin plugin = getPlugin(name);

        if (plugin != null) {
            if (plugin.isEnabled()) {
                plugin.toggleEnabled();
                plugin.disable();
                HookManager.getInstance().removePluginHooks(plugin);
                CommandParser.getInstance().removePluginCommands(plugin);
            }
        }
    }
}
