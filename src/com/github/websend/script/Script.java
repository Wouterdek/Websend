package com.github.websend.script;

import com.github.websend.Main;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;

public class Script {

    public String name;
    public boolean invokeOnLoad = false;
    private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    private Class<?> main;

    public Script(String name) {
        this.name = name;
    }

    public void invoke() {
        try {
            if (main == null) {
                Main.getMainLogger().log(Level.SEVERE, "No main class found for " + name + "!");
            }
            if (main != null) {
                main.getMethod("run", new Class[]{}).invoke(main.newInstance(), new Object[]{});
            }
        } catch (InvocationTargetException ex) {
            if (ex.getCause() != null) {
                Main.getMainLogger().log(Level.SEVERE, "The '" + name + "' script failed to run", ex.getCause());
            }
        } catch (NoSuchMethodException ex) {
            Main.getMainLogger().log(Level.SEVERE, "The '" + name + "' script doesn't contain a run method!");
        } catch (Exception ex) {
            Main.getMainLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void addClass(Class<?> newClass) {
        classes.add(newClass);
    }

    public void setMainClass(Class<?> newMain) {
        main = newMain;
    }
}
