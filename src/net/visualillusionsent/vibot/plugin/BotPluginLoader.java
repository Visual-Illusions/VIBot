package net.visualillusionsent.vibot.plugin;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.visualillusionsent.vibot.Misc;
import net.visualillusionsent.vibot.io.Colors;
import net.visualillusionsent.vibot.plugin.hook.BaseHook;
import net.visualillusionsent.vibot.plugin.hook.HookManager;

/**
 * BotPluginLoader.java - Used to load plugins, toggle them, etc.
 * 
 * Contains code from CanaryMod PluginLoader.java
 * 
 * @author James (Original Author)
 * @author darkdiplomat
 */
public class BotPluginLoader {

    private Logger log = Logger.getLogger("VIBot");
    private static final Object lock = new Object();
    private List<BotPlugin> plugins = new ArrayList<BotPlugin>();
    // private HashMap<String, PluginInterface> customListeners = new
    // HashMap<String, PluginInterface>();
    private volatile boolean loaded = false;

    /**
     * Creates a plugin loader
     */
    public BotPluginLoader() {
    }

    /**
     * Loads all plugins.
     */
    public void loadPlugins() {
        if (loaded) {
            return;
        }
        String[] classes = new String[] { "" };
        log.info("*** Loading plugins...");
        if (Misc.getPlugins() != null) {
            classes = Misc.getPlugins();
        }

        for (String sclass : classes) {
            if (sclass.equals("")) {
                continue;
            }
            loadPlugin(sclass.trim());
        }
        log.info("*** Loaded " + plugins.size() + " plugins.");
        loaded = true;
    }

    /**
     * Loads the specified plugin
     * 
     * @param fileName
     *            file name of plugin to load
     * @return if the operation was successful
     */
    public boolean loadPlugin(String fileName) {
        if (getPlugin(fileName) != null) {
            return false; // Already exists.
        }
        return load(fileName);
    }

    /**
     * Reloads the specified plugin
     * 
     * @param fileName
     *            file name of plugin to reload
     * @return if the operation was successful
     */
    public boolean reloadPlugin(String fileName) {

        /* Not sure exactly how much of this is necessary */
        BotPlugin toNull = getPlugin(fileName);

        if (toNull != null) {
            if (toNull.isEnabled()) {
                toNull.disable();
            }
        }
        synchronized (lock) {
            plugins.remove(toNull);
        }
        try {
            File file = new File("plugins/" + fileName + ".jar");
            BotClassLoader child = null;
            child = new BotClassLoader(new URL[] { file.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            child.close();
        } catch (Exception e) {
        }

        toNull = null;

        return load(fileName);
    }

    private boolean load(String fileName) {
        String filepath = "plugins/" + fileName + ".jar";
        try {
            File pluginfile = new File(filepath);

            if (!pluginfile.exists()) {
                log.severe("Failed to find plugin file: plugins/" + fileName + ".jar. Please ensure the file exists");
                return false;
            }
            URLClassLoader child = null;

            try {
                child = new BotClassLoader(new URL[] { pluginfile.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            } catch (MalformedURLException ex) {
                log.log(Level.SEVERE, "Exception while loading class", ex);
                return false;
            }

            String filepathtemp = getPluginClassPath(pluginfile.getAbsolutePath());
            filepath = filepathtemp != null ? filepathtemp : fileName;

            Class<?> pluginclazz = Class.forName(filepath, true, child);

            BotPlugin plugin = (BotPlugin) pluginclazz.newInstance();

            plugin.setName(fileName);
            plugin.enable();

            synchronized (lock) {
                plugins.add(plugin);
                plugin.initialize();
            }

        } catch (Throwable ex) {
            log.log(Level.SEVERE, "Exception while loading plugin (" + filepath + ")", ex);
            return false;
        }
        return true;
    }

    @SuppressWarnings("resource")
    private final String getPluginClassPath(String jarpath) {
        try {
            JarFile jarjar = new JarFile(jarpath);
            Manifest manifest = jarjar.getManifest();
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Plugin-Class");
            if (value == null) {
                log.warning("Was unable to locate Plugin-Class attribute for Plugin: '".concat(jarpath).concat("'").concat(Misc.LINE_SEP).concat(" Proceeding with assumption that default package is used..."));
            }
            return value;
        }
        catch (IOException E) {
            log.warning("Was unable to read Manifest for Plugin: '".concat(jarpath).concat("'"));
            return null;
        }
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
        } else {
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
        } else { // New plugin, perhaps?
            File file = new File("plugins/" + name + ".jar");

            if (file.exists()) {
                return loadPlugin(name);
            } else {
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
                List<BaseHook> hooks = HookManager.getInstance().getAllHooks();
                for(BaseHook hook : hooks){
                    if(hook.getPlugin().getName().equals(plugin.getName())){
                        HookManager.removeHook(hook);
                    }
                }
            }
        }
    }
}
