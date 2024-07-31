package cn.ksmcbrigade.al;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Config {

    public static File config = new File("config/al-config.json");

    public JsonObject data;

    public int wait;

    public Config() throws IOException {
        new File("config").mkdirs();

        if(!config.exists()){
            JsonObject obj = new JsonObject();
            obj.addProperty("wait",3);
            obj.add("records",new JsonObject());
            FileUtils.writeStringToFile(config,obj.toString());
        }

        JsonObject context = JsonParser.parseString(FileUtils.readFileToString(config)).getAsJsonObject();
        this.data = context.getAsJsonObject("records");
        this.wait = context.get("wait").getAsInt();
    }

    public void save() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("wait",wait);
        obj.add("records",data);
        FileUtils.writeStringToFile(config, obj.toString());
    }

    public void reload() throws IOException {
        if(!config.exists()){
            JsonObject context = JsonParser.parseString(FileUtils.readFileToString(config)).getAsJsonObject();
            this.data = context.getAsJsonObject("records");
            this.wait = context.get("wait").getAsInt();
        }
    }

    @Override
    public String toString() {
        return "Config{" +
                "config=" + config.getPath() +
                "data=" + data +
                "wait=" + wait +
                '}';
    }
}
