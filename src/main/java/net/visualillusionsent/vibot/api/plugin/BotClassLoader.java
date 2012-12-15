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

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Class loader used so we can dynamically load classes. Normal class loader
 * doesn't close the .jar so you can't reload. This fixes that.
 * 
 * @author James
 */
public class BotClassLoader extends URLClassLoader {

    /**
     * Creates loader
     * 
     * @param urls
     * @param loader
     */
    public BotClassLoader(URL[] urls, ClassLoader loader) {
        super(urls, loader);
    }

    /**
     * Fix here.
     */
    @SuppressWarnings("rawtypes")
    public void close() {
        try {
            Class<?> clazz = java.net.URLClassLoader.class;
            Field ucp = clazz.getDeclaredField("ucp");

            ucp.setAccessible(true);
            Object sun_misc_URLClassPath = ucp.get(this);
            Field loaders = sun_misc_URLClassPath.getClass().getDeclaredField("loaders");

            loaders.setAccessible(true);
            Object java_util_Collection = loaders.get(sun_misc_URLClassPath);

            for (Object sun_misc_URLClassPath_JarLoader : ((java.util.Collection) java_util_Collection).toArray()) {
                try {
                    java.lang.reflect.Field loader = sun_misc_URLClassPath_JarLoader.getClass().getDeclaredField("jar");

                    loader.setAccessible(true);
                    Object java_util_jar_JarFile = loader.get(sun_misc_URLClassPath_JarLoader);

                    ((java.util.jar.JarFile) java_util_jar_JarFile).close();
                }
                catch (Throwable t) {
                    // if we got this far, this is probably not a JAR loader so
                    // skip it
                }
            }
        }
        catch (Throwable t) {
            // probably not a SUN VM
        }
        return;
    }
}