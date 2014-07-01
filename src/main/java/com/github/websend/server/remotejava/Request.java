package com.github.websend.server.remotejava;

import com.github.websend.Main;
import com.github.websend.server.ComplexInputStream;
import com.github.websend.server.ComplexOutputStream;
import java.io.IOException;

public abstract class Request {
    static boolean checkSession(Session session){
        if(session == null){
            Main.logWarning("Received invalid sessionID!");
            return false;
        }
        return true;
    }
    
    static Object[] deserializeObjects(ComplexInputStream in, Session session, Class... classes) throws IOException{
        Object[] objects = new Object[classes.length];
        for(int i = 0;i<objects.length;i++){
            Class clazz = classes[i];
            if(clazz == null){
                objects[i] = null;
            }else if(clazz.equals(Boolean.TYPE)){
                objects[i] = in.readBoolean();
            }else if(clazz.equals(Byte.TYPE)){
                objects[i] = in.readByte();
            }else if(clazz.equals(Character.TYPE)){
                objects[i] = in.readChar();
            }else if(clazz.equals(Short.TYPE)){
                objects[i] = in.readShort();
            }else if(clazz.equals(Integer.TYPE)){
                objects[i] = in.readInt();
            }else if(clazz.equals(Long.TYPE)){
                objects[i] = in.readLong();
            }else if(clazz.equals(Float.TYPE) || clazz.equals(Double.TYPE)){
                objects[i] = in.readDouble();
            }else if(clazz.equals(String.class)){
                objects[i] = in.readString();
            }else{
                int objID = in.readInt();
                Object obj = session.getObject(objID);
                objects[i] = obj;
            }
        }
        return objects;
    }
    
    static void serializeObjects(ComplexOutputStream out, Session session, Object... objects) throws IOException{
        String typeString = session.generateTypeString(objects);
        out.writeString(typeString);
        for(Object obj : objects){
            if(obj == null){
            }else if(obj instanceof Boolean){
                out.writeBoolean((Boolean)obj);
            }else if(obj instanceof Byte){
                out.writeByte((Byte)obj);
            }else if(obj instanceof Character){
                out.writeChar((Character)obj);
            }else if(obj instanceof Short){
                out.writeShort((Short)obj);
            }else if(obj instanceof Integer){
                out.writeInt((Integer)obj);
            }else if(obj instanceof Long){
                out.writeLong((Long)obj);
            }else if(obj instanceof Float){
                out.writeDouble(((Float)obj).doubleValue());
            }else if(obj instanceof Double){
                out.writeDouble((Double)obj);
            }else if(obj instanceof String){
                out.writeString((String)obj);
            }else{
                int ID = session.getObject(obj);
                if(ID == -1){
                    ID = session.storeObject(obj);
                }
                out.writeInt(ID);
            }
        }
    }
}
