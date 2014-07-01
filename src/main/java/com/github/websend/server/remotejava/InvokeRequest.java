package com.github.websend.server.remotejava;

import com.github.websend.Main;
import com.github.websend.server.CommunicationServer;
import com.github.websend.server.ComplexInputStream;
import com.github.websend.server.ComplexOutputStream;
import static com.github.websend.server.remotejava.Request.checkSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class InvokeRequest extends Request{
    public static void onObject(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        handleRequest(server, in, out, false);
    }
    
    public static void onClass(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        handleRequest(server, in, out, true);
    }
    
    private static void handleRequest(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out, boolean runStatic) throws IOException {
        int sessionID = in.readInt();
        int objectID = 0; //Object only
        if(!runStatic){
            objectID = in.readInt();
        }
        int methodID = in.readInt();
        String typeStr = in.readString();
        Session session = server.getRemoteJavaSession(sessionID);
        if (checkSession(session)) {
            Object obj = null; //Object only
            if(!runStatic){
                obj = session.getObject(objectID);
            }
            Method method = session.getMethod(methodID);
            Class[] argTypes;
            try {
                argTypes = session.parseTypeString(typeStr);
            } catch (ClassNotFoundException ex) {
                Main.logWarning("Invalid class in invocation argument types!", ex);
                return;
            }
            if (!runStatic && obj == null) {
                Main.logWarning("Invalid object in invocation!");
            } else if (method == null) {
                Main.logWarning("Invalid method in invocation!");
            } else {
                Object[] args = deserializeObjects(in, session, argTypes);
                Object returnVal = runMethod(obj, method, args);
                serializeObjects(out, session, returnVal);
            }
        }
    }
    
    private static Object runMethod(Object obj, Method method, Object[] args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException ex) {
            Main.logError("Illegal Access in invocation!");
        } catch (IllegalArgumentException ex) {
            Main.logWarning("Invalid arguments in invocation!");
        } catch (InvocationTargetException ex) {
            Main.logWarning("Method threw exception: " + ex.getMessage(), ex);
        }
        return null;
    }
}
