package net.visualillusionsent.vibot.io.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.visualillusionsent.vibot.io.logging.BotLogMan;

public final class PropertiesFile {

    private File propsFile;
    private String filepath;
    private HashMap<String, String> props = new HashMap<String, String>();
    private HashMap<String, String[]> comments = new HashMap<String, String[]>();

    public PropertiesFile(String filepath) {
        this.filepath = filepath;
        propsFile = new File(filepath);
        if (propsFile.exists()) {
            load();
        }
        else {
            save();
        }
    }

    public void load() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(propsFile));
            String inLine;
            ArrayList<String> inComments = new ArrayList<String>();
            while ((inLine = in.readLine()) != null) {
                if (inLine.startsWith(";") || inLine.startsWith("#")) {
                    inComments.add(inLine);
                }
                else {
                    try {
                        String[] propsLine = inLine.split("=");
                        props.put(propsLine[0].trim(), propsLine[1].trim());
                        if (!inComments.isEmpty()) {
                            String[] commented = new String[inComments.size()];
                            for (int i = 0; i < inComments.size(); i++) {
                                commented[i] = inComments.get(i);
                            }
                            comments.put(propsLine[0], commented);
                            inComments.clear();
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException aioobe) {
                        inComments.clear();
                        continue;
                    }
                }
            }
            in.close();
        }
        catch (IOException IOE) {
            BotLogMan.warning("A IOException occurred in File: '" + filepath + "'");
        }
    }

    public void save() {
        try {
            propsFile.delete();
            propsFile = new File(filepath);
            BufferedWriter out = new BufferedWriter(new FileWriter(propsFile, true));
            for (String prop : props.keySet()) {
                if (comments.containsKey(prop)) {
                    for (String comment : comments.get(prop)) {
                        out.write(comment);
                        out.newLine();
                    }
                }
                out.write(prop + "=" + props.get(prop));
                out.newLine();
            }
            out.close();
        }
        catch (IOException IOE) {
            BotLogMan.warning("A IOException occurred in File: '" + filepath + "'");
        }
    }

    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    public void removeKey(String key) {
        if (props.containsKey(key)) {
            props.remove(key);
            if (comments.containsKey(key)) {
                comments.remove(key);
            }
        }
    }

    public String getString(String key) {
        if (containsKey(key)) {
            return props.get(key);
        }
        return null;
    }

    public void setString(String key, String value) {
        props.put(key, value);
    }

    public void setString(String key, String value, String... comment) {
        props.put(key, value);
        addComment(key, comment);
    }

    public int getInt(String key) {
        int value = -1;
        if (containsKey(key)) {
            try {
                value = Integer.parseInt(getString(key));
            }
            catch (NumberFormatException NFE) {
                value = -1;
                BotLogMan.warning("A NumberFormatException occurred in File: '" + filepath + "' @ KEY: " + key);
            }
        }
        return value;
    }

    public void setInt(String key, int value) {
        props.put(key, String.valueOf(value));
    }

    public void setInt(String key, int value, String... comment) {
        props.put(key, String.valueOf(value));
        addComment(key, comment);
    }

    public double getDouble(String key) {
        double value = -1;
        if (containsKey(key)) {
            try {
                value = Double.parseDouble(getString(key));
            }
            catch (NumberFormatException NFE) {
                value = -1;
                BotLogMan.warning("A NumberFormatException occurred in File: '" + filepath + "' @ KEY: " + key);
            }
        }
        return value;
    }

    public void setDouble(String key, double value) {
        props.put(key, String.valueOf(value));
    }

    public long getLong(String key) {
        long value = -1;
        if (containsKey(key)) {
            try {
                value = Long.parseLong(getString(key));
            }
            catch (NumberFormatException NFE) {
                value = -1;
                BotLogMan.warning("A NumberFormatException occurred in File: '" + filepath + "' @ KEY: " + key);
            }
        }
        return value;
    }

    public void setLong(String key, long value) {
        props.put(key, String.valueOf(value));
    }

    public void setLong(String key, long value, String... comment) {
        props.put(key, String.valueOf(value));
        addComment(key, comment);
    }

    public boolean getBoolean(String key) {
        if (containsKey(key)) {
            return Boolean.parseBoolean(getString(key));
        }

        return false;
    }

    public void setBoolean(String key, boolean value) {
        props.put(key, String.valueOf(value));
    }

    public void setBoolean(String key, boolean value, String... comment) {
        props.put(key, String.valueOf(value));
        addComment(key, comment);
    }

    public Character getCharacter(String key) {
        return getString(key).charAt(0);
    }

    public void setCharacter(String key, char ch) {
        props.put(key, String.valueOf(ch));
    }

    public void setCharacter(String key, char ch, String... comment) {
        props.put(key, String.valueOf(ch));
        addComment(key, comment);
    }

    private void addComment(String key, String... comment) {
        for (int i = 0; i < comment.length; i++) {
            if (!comment[i].startsWith(";") && !comment[i].startsWith("#")) {
                comment[i] = ";" + comment[i];
            }
        }
        comments.put(key, comment);
    }
}
