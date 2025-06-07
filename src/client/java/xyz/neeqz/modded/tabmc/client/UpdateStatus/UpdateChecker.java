package xyz.neeqz.modded.tabmc.client.UpdateStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import xyz.neeqz.modded.tabmc.client.ChatLogColor;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdateChecker {


    private static final String MOD_ID = "tabmc-prestizafk";
    private static final String CURRENT_VERSION = "1.1";

    private static long lastCheck = 0;
    private static final long CHECK_INTERVAL = 30_000L;

    public static void checkForUpdate() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_INTERVAL) return;
        lastCheck = now;

        new Thread(() -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/" + MOD_ID + "/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
                    reader.close();

                    if (versions.size() == 0) {
                        sendMessageToClient("&cNie znaleziono wersji moda na Modrinth!");
                        return;
                    }

                    String latestVersion = null;

                    for (JsonElement versionElement : versions) {
                        latestVersion = versionElement.getAsJsonObject().get("version_number").getAsString();
                        break;
                    }

                    if (latestVersion == null) {
                        sendMessageToClient("&cBłąd podczas pobierania najnowszej wersji!");
                        return;
                    }

                    if (latestVersion.equals(CURRENT_VERSION)) {
                        sendMessageToClient("&aUżywasz najnowszej wersji moda (" + CURRENT_VERSION + ")");
                    } else {
                        sendMessageToClient("&eDostępna jest nowa wersja moda: &6" + latestVersion + " &7(aktualna: " + CURRENT_VERSION + ")");
                        sendMessageToClient("&7Pobierz ją z: &bhttps://modrinth.com/mod/" + MOD_ID);
                    }
                } //else {
//                    sendMessageToClient("&cBłąd podczas sprawdzania wersji (kod HTTP: " + conn.getResponseCode() + ")");
//  //              }
            } catch (Exception e) {
                sendMessageToClient("&cBłąd podczas sprawdzania aktualizacji: " + e.getMessage());
            }
        }).start();
    }

    private static void sendMessageToClient(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.execute(() -> mc.player.sendMessage(ChatLogColor.color(message), false));
        }
    }
}