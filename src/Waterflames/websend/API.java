package Waterflames.websend;

public class API {
    public static void registerCustomPacketHandler(PacketHandler wph){
        Main.server.addPacketHandler(wph);
    }
}
