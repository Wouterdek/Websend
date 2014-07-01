package com.github.websend.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ComplexInputStream extends DataInputStream{
    public ComplexInputStream(InputStream in) {
        super(in);
    }
    
    public String readString() throws IOException {
        int stringSize = this.readInt();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < stringSize; i++) {
            buffer.append(this.readChar());
        }
        return buffer.toString();
    }
}
