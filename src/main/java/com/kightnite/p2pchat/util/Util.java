package com.kightnite.p2pchat.util;


import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    public static String[] getUserData(){
        String[] userData = new String[2]; // Userdata -> [String username, String uuid]
        String[] namePool = new String[]{"Client", "Mega", "Base", "Toad", "Name", "Boba"};


        userData[0] = namePool[ThreadLocalRandom.current().nextInt(namePool.length)]
                + "-"
                + ThreadLocalRandom.current().nextInt(1, 99);
        userData[1] = UUID.randomUUID().toString();

        return userData;
    }

}
