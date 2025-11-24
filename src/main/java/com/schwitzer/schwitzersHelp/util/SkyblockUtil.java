package com.schwitzer.schwitzersHelp.util;

public class SkyblockUtil {

    public static void getItemFromSack(String itemname, int amount)
    {
        ChatUtil.sendMessage("/gfs " + itemname + " " + amount);
    }
}
