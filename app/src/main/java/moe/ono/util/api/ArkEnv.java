package moe.ono.util.api;

import static moe.ono.constants.Constants.PrekCfgXXX;

import moe.ono.config.ConfigManager;

public class ArkEnv {
    public static String getSignatureAPI() {
       return ConfigManager.dGetString(PrekCfgXXX + "signAddress", "https://ark.ouom.fun/");
    }
    public static String getAuthAPI() {
        return ConfigManager.dGetString(PrekCfgXXX + "authenticationAddress", "https://q.lyhc.top/");
    }

    public static final String GET_CARD_DATA_ENDPOINT = "get/%s";
}
