//package config;
//


 // TODO Fix jackson import
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.File;
//import java.io.IOException;
//
//public class ConfigLoader {
//    private static ConfigLoader instance;
//    private GameConfig config;
//
//    private ConfigLoader() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            config = objectMapper.readValue(new File("config.json"), GameConfig.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static ConfigLoader getInstance() {
//        if (instance == null) {
//            instance = new ConfigLoader();
//        }
//        return instance;
//    }
//
//    public GameConfig getConfig() {
//        return config;
//    }
//}
