package com.github.websend.server.remotejava;

import com.github.websend.Main;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Session{
    private ArrayList<Object> objectCache = new ArrayList<Object>();
    private ArrayList<Method> methodCache = new ArrayList<Method>();
    private HashMap<String, Class> classCache = new HashMap<String, Class>();
    
    //TODO: make error codes uniform or provide error string.
    
    public void close(){
        this.classCache.clear();
        this.methodCache.clear();
        this.objectCache.clear();
    }
    
    /**********************/
    /**** Object cache ****/
    /**********************/
    public int storeObject(Object obj){
        if(objectCache.add(obj)){
            return objectCache.size() - 1;
        }else{
            return -1;
        }
    }
    
    public int getObject(Object obj){
        for(int i = 0;i<objectCache.size();i++){
            Object cur = objectCache.get(i);
            if(cur == obj){
                return i;
            }
        }
        return -1;
    }
    
    public Object getObject(int ID){
        if(ID < 0 || ID >= objectCache.size()){
            Main.logDebugInfo(Level.WARNING, "Error while retrieving object from objectcache: invalid objectID");
            return null;
        }
        return objectCache.get(ID);
    }
    
    public void removeObject(int ID){
        objectCache.remove(ID);
    }
    
    
    /**********************/
    /**** Method cache ****/
    /**********************/
    public int getMethodIDFromObject(int objID, String methodName, String typeString){
        if(objID < 0 || objID >= objectCache.size()){
            Main.logDebugInfo(Level.WARNING, "Error while retrieving methodID from object: invalid objectID ("+objID+")");
            return -3;
        }
        Object obj = objectCache.get(objID);
        if(obj != null){
            try {
                Class[] params = parseTypeString(typeString);
                Method result = obj.getClass().getMethod(methodName, params);
                return this.storeMethod(result);
            } catch (ClassNotFoundException ex) {
                Main.logDebug(Level.WARNING, "Error while retrieving methodID from object: invalid typestring ("+typeString+")", ex);
                return -1;
            } catch (NoSuchMethodException ex){
                Main.logDebug(Level.WARNING, "Error while retrieving methodID from object: no such method ("+methodName+")", ex);
                return -2;
            }
        }else{
            Main.logDebugInfo(Level.WARNING, "Error while retrieving methodID from object: invalid objectID ("+objID+")");
            return -3;
        }
    }
    
    public int getMethodIDFromClass(String className, String methodName, String typeString){
        try{
            Class clazz = Class.forName(className);
            try {
                Class[] params = parseTypeString(typeString);
                Method result = clazz.getMethod(methodName, params);
                return this.storeMethod(result);
            } catch (ClassNotFoundException ex) {
                Main.logDebug(Level.WARNING, "Error while retrieving methodID from class: invalid typestring ("+typeString+")", ex);
                return -1;
            } catch (NoSuchMethodException ex){
                Main.logDebug(Level.WARNING, "Error while retrieving methodID from class: no such method ("+methodName+")", ex);
                return -2;
            }
        }catch(ClassNotFoundException ex){
            Main.logDebug(Level.WARNING, "Error while retrieving methodID from class: invalid classname ("+className+")", ex);
            return -3;
        }
    }
    
    public Object invokeMethod(int objID, int methodID, Object... args){
        Method method = methodCache.get(methodID);
        if(method == null){
            Main.logDebugInfo(Level.WARNING, "Error while invoking method: invalid methodID ("+methodID+")");
            return -1;
        }
        
        Object obj = objectCache.get(objID);
        if(obj == null){
            Main.logDebugInfo(Level.WARNING, "Error while invoking method: invalid objectID ("+objID+")");
            return -2;
        }
        
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException ex) {
            Main.logDebug(Level.WARNING, "Error while invoking method: no access ("+method.getName()+")", ex);
            return -3;
        } catch (IllegalArgumentException ex) {
            Main.logDebug(Level.WARNING, "Error while invoking method: invalid arguments ("+method.getName()+")", ex);
            return -4;
        } catch (InvocationTargetException ex) {
            Main.logDebug(Level.WARNING, "Exception was thrown while invoking method: "+ex.getTargetException()+" ("+method.getName()+")", ex);
            return -5;
        }
    }
    
    public Object invokeStaticMethod(int methodID, Object... args){
        Method method = methodCache.get(methodID);
        if(method == null){
            Main.logDebugInfo(Level.WARNING, "Error while invoking static method: invalid methodID ("+methodID+")");
            return -1;
        }
        
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException ex) {
            Main.logDebug(Level.WARNING, "Error while invoking method: no access ("+method.getName()+")", ex);
            return -3;
        } catch (IllegalArgumentException ex) {
            Main.logDebug(Level.WARNING, "Error while invoking method: invalid arguments ("+method.getName()+")", ex);
            return -4;
        } catch (InvocationTargetException ex) {
            Main.logDebug(Level.WARNING, "Exception was thrown while invoking method: "+ex.getTargetException()+" ("+method.getName()+")", ex);
            return -5;
        }
    }
    
    public int storeMethod(Method method){
        methodCache.add(method);
        return methodCache.size() - 1;
    }
    
    public Method getMethod(int ID){
        if(ID < 0 || ID >= methodCache.size()){
            Main.logDebugInfo(Level.WARNING, "Error while retrieving method from methodcache: invalid methodID");
            return null;
        }
        return methodCache.get(ID);
    }
    
    public void removeMethod(int ID){
        methodCache.remove(ID);
    }
    
    
    /**********************/
    /**** Class cache ****/
    /**********************/
    public Class[] parseTypeString(String typeStr) throws ClassNotFoundException {
        typeStr = typeStr.replaceAll("/", ".");
        ArrayList<Class> types = new ArrayList<Class>();
        String curClassName = null;
        String arrayCounter = "";
        for(char curChar : typeStr.toCharArray()){
            if(curClassName != null){ //Are we reading a classname?
                if(curChar == ';'){
                    if(arrayCounter.equals("")){
                        types.add(getClassByName(curClassName));
                    }else{
                        types.add(getClassByName(arrayCounter+"L"+curClassName+";"));
                    }
                    curClassName = null;
                }else{
                    curClassName += curChar;
                    continue;
                }
            }else if(curChar == 'L'){
                curClassName = "";
                continue;
            }else if(curChar == '['){//Is this the start of an array?
                arrayCounter += "[";
                continue;
            }else if(!arrayCounter.equals("")){
                types.add(getClassByName(arrayCounter+curChar));
            }else{
                Class clazz;
                switch(curChar){
                    case 'N': clazz = null; break;
                    case 'B': clazz = Byte.TYPE; break;
                    case 'C': clazz = Character.TYPE; break;
                    case 'D': clazz = Double.TYPE; break;
                    case 'F': clazz = Float.TYPE; break;
                    case 'I': clazz = Integer.TYPE; break;
                    case 'J': clazz = Long.TYPE; break;
                    case 'S': clazz = Short.TYPE; break;
                    case 'Z': clazz = Boolean.TYPE; break;
                    default: throw new ClassNotFoundException("Invalid typelist \""+typeStr+"\":  Unrecognized char "+curChar);
                }
                types.add(clazz);
            }
            arrayCounter = "";
        }
        return types.toArray(new Class[types.size()]);
    }
    
    public String generateTypeString(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for(Object obj : objects){
            if(obj == null){
                builder.append('N');
            }else if(obj instanceof Boolean){
                builder.append('Z');
            }else if(obj instanceof Byte){
                builder.append('B');
            }else if(obj instanceof Character){
                builder.append('C');
            }else if(obj instanceof Short){
                builder.append('S');
            }else if(obj instanceof Integer){
                builder.append('I');
            }else if(obj instanceof Long){
                builder.append('J');
            }else if(obj instanceof Float){
                builder.append('F');
            }else if(obj instanceof Double){
                builder.append('D');
            }else{
                builder.append('L').append(obj.getClass().getName()).append(";");
            }
        }
        return builder.toString();
    }
    
    public Class getClassByName(String className) throws ClassNotFoundException{
        Class clazz = classCache.get(className);
        if(clazz == null){
            clazz = Class.forName(className);
            classCache.put(className, clazz);
        }
        return clazz;
    }
}
