package com.github.websend.server.remotejava;

import com.github.websend.server.CommunicationServer;
import com.github.websend.server.ComplexInputStream;
import com.github.websend.server.ComplexOutputStream;
import static com.github.websend.server.remotejava.Request.checkSession;
import java.io.IOException;

public class MiscRequest extends Request{
    public static void openSession(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        int sessionID = server.openRemoteJavaSession();
        out.writeInt(sessionID);
    }
    
    public static void closeSession(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        int sessionID = in.readInt();
        server.closeRemoteJavaSession(sessionID);
    }
    
    public static void releaseMethod(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        int sessionID = in.readInt();
        int methodID = in.readInt();

        Session session = server.getRemoteJavaSession(sessionID);
        if (checkSession(session)) {
            session.removeMethod(methodID);
        }
    }
    
    public static void releaseObject(CommunicationServer server, ComplexInputStream in, ComplexOutputStream out) throws IOException {
        int sessionID = in.readInt();
        int objID = in.readInt();

        Session session = server.getRemoteJavaSession(sessionID);
        if (checkSession(session)) {
            session.removeObject(objID);
        }
    }
}
