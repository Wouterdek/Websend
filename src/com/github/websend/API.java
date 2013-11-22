package com.github.websend;

public class API {

    public static void registerCustomPacketHandler(PacketHandler wph) {
        Main.getCommunicationServer().addPacketHandler(wph);
    }
}
