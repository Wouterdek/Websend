package com.github.websend.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ComplexOutputStream extends DataOutputStream{
    public ComplexOutputStream(OutputStream out) {
        super(out);
    }

    public void writeString(String string) throws IOException {
        this.writeInt(string.length());
        this.writeChars(string);
    }
}
