package com.github.websend.server.remotejava;

import com.github.websend.server.CommunicationServer;
import com.github.websend.server.ComplexInputStream;
import com.github.websend.server.ComplexOutputStream;
import java.io.IOException;

public class MethodRequest extends Request{
    public static void byObject(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException{
        int sessionID = in.readInt();
        int objectID = in.readInt();
        String name = in.readString();
        String argTypes = in.readString();
    
        Session session = server.getRemoteJavaSession(sessionID);
        if(checkSession(session)){
            int ID = session.getMethodIDFromObject(objectID, name, argTypes);
            out.writeInt(ID);
        }
    }
    
    public static void byClass(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException{
        int sessionID = in.readInt();
        String className = in.readString();
        String name = in.readString();
        String argTypes = in.readString();

        Session session = server.getRemoteJavaSession(sessionID);
        if(checkSession(session)){
            int ID = session.getMethodIDFromClass(className, name, argTypes);
            out.writeInt(ID);
        }
    }
}
