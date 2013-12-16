package com.github.websend.script;

import com.github.websend.Main;
import java.util.ArrayList;
import java.util.logging.Level;

public class Script {
    public String name;
    public boolean invokeOnLoad = false;
    private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    private Class<? extends ScriptInterface> main;

    public Script(String name) {
        this.name = name;
    }

    public void invoke() {
        try {
            if (main == null) {
                Main.getMainLogger().log(Level.SEVERE, "No main class found for " + name + "!");
            }
            ScriptInterface scriptMain = main.newInstance();
            scriptMain.run();
        } catch (InstantiationException ex) {
            Main.getMainLogger().log(Level.SEVERE, "Could not instantiate script.", ex);
        } catch (IllegalAccessException ex) {
            Main.getMainLogger().log(Level.SEVERE, "Run method in script is not accessible.", ex);
        }
    }

    public void addClass(Class<?> newClass) {
        classes.add(newClass);
    }

    public void setMainClass(Class<? extends ScriptInterface> newMain) {
        main = newMain;
    }
}
