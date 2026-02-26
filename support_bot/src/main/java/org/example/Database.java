package org.example;

import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import kotlin.Pair;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class Database {
    private static final String URL =
//            "jdbc:mysql://localhost:3306/support";
            "jdbc:mysql://db-mysql-fra1-for-sed-viktor-do-user-4811370-0.h.db.ondigitalocean.com:25060/defaultdb";
    private static final String USERNAME =
//        "root";
        "doadmin";
    private static final String PASSWORD =
//        "Koroleva1!";
        "AVNS_qDXXsgfxcqfcSLOCZDb";

    public Database() {
        try (Connection conn = getConnection()) {
            String createTableThemes = "CREATE TABLE IF NOT EXISTS themes " +
                    "(id INTEGER PRIMARY KEY AUTO_INCREMENT, theme TEXT, click INTEGER default 0, " +
                    "not_question INTEGER default 0)";
            String createTable = "CREATE TABLE IF NOT EXISTS questions " +
                    "(id INTEGER PRIMARY KEY AUTO_INCREMENT, theme_id INTEGER, click INTEGER default 0, " +
                    "not_help INTEGER default 0, FOREIGN KEY (theme_id) REFERENCES themes(id))";
            String createTableQuestion = "CREATE TABLE IF NOT EXISTS answers (id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "language TEXT, question TEXT, answer TEXT, photos LONGBLOB, question_id INTEGER," +
                    "FOREIGN KEY (question_id) REFERENCES questions(id))";
            String createTableKeys = "CREATE TABLE IF NOT EXISTS keys_access (id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "tg_id INTEGER, _key TEXT, key_type TEXT)";
            String createTableUsersPool = "CREATE TABLE IF NOT EXISTS users_pool (id INTEGER PRIMARY KEY AUTO_INCREMENT, user_id INT UNIQUE, language TEXT)";
            String createTableNewsPool = "CREATE TABLE IF NOT EXISTS news_pool (id INTEGER PRIMARY KEY AUTO_INCREMENT, language TEXT, news_name TEXT, " +
                    "news_text TEXT, photo LONGBLOB, news_date DATETIME, news_id INTEGER, FOREIGN KEY (news_id) REFERENCES questions(id))";
            String createTablePresentations = "CREATE TABLE IF NOT EXISTS presentations (id INTEGER PRIMARY KEY AUTO_INCREMENT, language VARCHAR(30) UNIQUE, " +
                    "text TEXT, file LONGBLOB)";
            String createTableVideos = "CREATE TABLE IF NOT EXISTS videos (id INTEGER PRIMARY KEY AUTO_INCREMENT, language VARCHAR(30) UNIQUE, " +
                    "video_link TEXT)";
            String createTableKeys1 = "CREATE TABLE IF NOT EXISTS keys_access1 (id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "tg_id BIGINT, _key TEXT, key_type TEXT)";
            String createTableUsersPool1 = "CREATE TABLE IF NOT EXISTS users_pool1 (id INTEGER PRIMARY KEY AUTO_INCREMENT, user_id BIGINT UNIQUE, language TEXT)";

//            String query = "INSERT INTO themes (theme) VALUES (?)";
            conn.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            try (PreparedStatement pstmt = conn.prepareStatement(createTableThemes);
                 PreparedStatement pstmt1 = conn.prepareStatement(createTable);
                 PreparedStatement pstmt2 = conn.prepareStatement(createTableQuestion);
                 PreparedStatement pstmt3 = conn.prepareStatement(createTableKeys);
                 PreparedStatement pstmt31 = conn.prepareStatement(createTableKeys1);
                 PreparedStatement pstmt4 = conn.prepareStatement(createTableUsersPool);
                 PreparedStatement pstmt41 = conn.prepareStatement(createTableUsersPool1);
                 PreparedStatement pstmt5 = conn.prepareStatement(createTableNewsPool);
                 PreparedStatement pstmt6 = conn.prepareStatement(createTablePresentations);
                 PreparedStatement pstmt9 = conn.prepareStatement(createTableVideos)
            ) {
                pstmt.execute();
                pstmt1.execute();
                pstmt2.execute();
                pstmt3.execute();
                pstmt31.execute();
                pstmt4.execute();
                pstmt41.execute();
                pstmt5.execute();
                pstmt6.execute();
                pstmt9.execute();
            }

            String tableCreateCheck = "SELECT * FROM keys_access1";
            String tableGet1 = "SELECT * FROM keys_access";
            String tableGet2 = "SELECT * FROM users_pool";
            String tableSet1 = "INSERT INTO keys_access1 (tg_id, _key, key_type) VALUES (?, ?, ?)";
            String tableSet2 = "INSERT INTO users_pool1 (user_id, language) VALUES (?, ?)";
            try (PreparedStatement pstmt11 = conn.prepareStatement(tableCreateCheck)) {
                ResultSet rs = pstmt11.executeQuery();
                if (!rs.next()) {
                    try (PreparedStatement pstmt12 = conn.prepareStatement(tableGet1);
                         PreparedStatement pstmt13 = conn.prepareStatement(tableGet2)) {
                        ResultSet rs2 = pstmt12.executeQuery();
                        while (rs2.next()) {
                            try (PreparedStatement pstmt14 = conn.prepareStatement(tableSet1)) {
                                pstmt14.setLong(1, rs2.getInt("tg_id"));
                                pstmt14.setString(2, rs2.getString("_key"));
                                pstmt14.setString(3, rs2.getString("key_type"));
                                pstmt14.execute();
                            }
                        }
                        ResultSet rs3 = pstmt13.executeQuery();
                        while (rs3.next()) {
                            try (PreparedStatement pstmt15 = conn.prepareStatement(tableSet2)) {
                                pstmt15.setLong(1, rs3.getInt("user_id"));
                                pstmt15.setString(2, rs3.getString("language"));
                                pstmt15.execute();
                            }
                        }
                    }

                }
            }
            String query = "INSERT INTO themes (theme) VALUES  (?)";
            List<String> listThemes = new ArrayList<>();
            listThemes.add("News");
            listThemes.addAll(Bot.getThemesSum());
            for (String s : listThemes) {
                if (getThemeId(s) == -1) {
                    try (PreparedStatement pstmt7 = conn.prepareStatement(query)) {
                        pstmt7.setString(1, s);
                        pstmt7.execute();
                    }
                }
            }

            String usersQuery = "SELECT * FROM users_pool1";
            try (PreparedStatement pstmt8 = conn.prepareStatement(usersQuery)) {
                ResultSet rs = pstmt8.executeQuery();
                while (rs.next()) {
                    Bot.getUsers().put(rs.getLong("user_id"), rs.getString("language"));
                }
            }

            formatted();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveAnswer() {
        String insertOrUpdate = "INSERT INTO questions (theme_id) VALUES (?)";
        String insertQuestion = "INSERT INTO answers " +
                "(language, question, answer, question_id, photos) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt2 = conn.prepareStatement(insertOrUpdate, Statement.RETURN_GENERATED_KEYS)) {

            pstmt2.setInt(1, getThemeId(Bot.getCurrentThemeToAdd()));
            pstmt2.execute();
            try (ResultSet generatedKeys = pstmt2.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    for  (EntityModel entity : Bot.getEntityModels()){
                        try (PreparedStatement pstmt1 = conn.prepareStatement(insertQuestion)) {
                            pstmt1.setString(1, entity.getLanguage());
                            pstmt1.setString(2, entity.getQuestion());
                            pstmt1.setString(3, entity.getAnswer());
                            pstmt1.setInt(4, id);
                            pstmt1.setBytes(5, combineImages(entity.getPhotos()));
                            pstmt1.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrUpdatePresentation(String language, String text, byte[] file) {
        String query = "INSERT INTO presentations (language, text, file)" +
                "VALUES (?, ?, ?)" +
                "ON DUPLICATE KEY UPDATE" +
                "  text = VALUES(text)," +
                "  file = VALUES(file);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, language);
            pstmt.setString(2, text);
            pstmt.setBytes(3, file);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Pair<String, byte[]> getPresentation(String language) {
        Pair<String, byte[]> result = null;
        String query = "SELECT * FROM presentations WHERE language = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, language);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = new Pair<>(rs.getString("text"), rs.getBytes("file"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void addOrUpdateVideos(String language, String link) {
        String query = "INSERT INTO videos (language, video_link)" +
                "VALUES (?, ?)" +
                "ON DUPLICATE KEY UPDATE" +
                "  video_link = VALUES(video_link);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, language);
            pstmt.setString(2, link);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getVideos(String language) {
        String result = null;
        String query = "SELECT * FROM videos WHERE language = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, language);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getString("video_link");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public Map<Integer, String> getQuestionsByTheme(String theme, Long userId) {
        String query3 = "SELECT question FROM answers WHERE question_id = ? and language = ?";
        return getByTheme(theme, userId, query3);
    }

    public Map<Integer, String> getNewsByTheme(String theme, Long userId) {
        String query3 = "SELECT news_name FROM news_pool WHERE news_id = ? and language = ?";
        return getByTheme(theme, userId, query3);
    }

    private Map<Integer, String> getByTheme(String theme, Long userId, String query3) {
        String query = "SELECT click FROM themes WHERE id = ?";
        String query2 = "SELECT id, click, not_help FROM questions WHERE theme_id = ?";
        String query4 = "UPDATE themes SET click = ? WHERE id = ?";
        Map<Integer, String> res = new HashMap<>();
        int themeId = getThemeId(theme);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2);
             PreparedStatement pstmt3 = conn.prepareStatement(query3);
             PreparedStatement pstmt4 = conn.prepareStatement(query4)) {
            pstmt2.setInt(1, themeId);
            ResultSet rs = pstmt2.executeQuery();
            while (rs.next()) {
                int questionId = rs.getInt(1);
                pstmt3.setInt(1, questionId);
                pstmt3.setString(2, Bot.getUsers().get(userId));
                ResultSet rs2 = pstmt3.executeQuery();
                if (rs2.next()) {
                    String text = rs2.getString(1);
                    if (Bot.isAdmin(userId)) {
                        text = text +  "\n" + rs.getInt(2) + "cl/ " + rs.getInt(3) + "dh";
                    }
                    res.put(questionId, text);
                }
            }
            pstmt.setInt(1, themeId);
            ResultSet rs3 = pstmt.executeQuery();
            if (rs3.next()) {
                pstmt4.setInt(1, rs3.getInt(1) + 1);
                pstmt4.setInt(2, themeId);
                pstmt4.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public Set<Map.Entry<String, List<byte[]>>> getAnswerByQuestion(int id, long userId) {
        String query = "SELECT answer, photos FROM answers WHERE question_id = ? and language = ?";
        String query2 = "SELECT click FROM questions WHERE id = ?";
        String query3 = "UPDATE questions SET click = ? WHERE id = ?";
        Map<String, List<byte[]>> res = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2);
             PreparedStatement pstmt3 = conn.prepareStatement(query3)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, Bot.getUsers().get(userId));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    res.put(rs.getString(1), splitImages(rs.getBytes(2)));
                }
            }
            pstmt2.setInt(1, id);
            try (ResultSet rs = pstmt2.executeQuery()) {
                if (rs.next()) {
                    pstmt3.setInt(1, rs.getInt(1) + 1);
                    pstmt3.setInt(2, id);
                    pstmt3.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res.entrySet();
    }

    public void setKeys(String type, String key) {
        String insertOrUpdate = "INSERT INTO keys_access1 (key_type, _key) VALUES (?, ?)";
        try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(insertOrUpdate)) {
            pstmt.setString(1, type);
            pstmt.setString(2, key);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getKeys(String type, long id) {
        String res = null;
        String query = "SELECT _key FROM keys_access1 WHERE tg_id = ? and key_type = ?";
        String query1 = "SELECT _key FROM keys_access1 WHERE key_type = ? and tg_id = 0";
        String query2 = "UPDATE keys_access1 SET tg_id = ? WHERE _key = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt1 = conn.prepareStatement(query1);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setLong(1, id);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    res = rs.getString(1);
                } else {
                    pstmt1.setString(1, type);
                    try (ResultSet rs1 = pstmt1.executeQuery()) {
                        if (rs1.next()) {
                            res = rs1.getString(1);
                            pstmt2.setLong(1, id);
                            pstmt2.setString(2, res);
                            pstmt2.execute();
                        } else {
                            res = "ключи кончились " + type;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void deleteAnswerQuestion(int id) {
        String query = "DELETE FROM answers WHERE question_id = ?";
        delete(id, query);
    }

    public void deleteNew(int id) {
        String query = "DELETE FROM news_pool WHERE news_id = ?";
        delete(id, query);
    }

    private void delete(int id, String query) {
        String query2 = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setInt(1, id);
            pstmt.execute();
            pstmt2.setInt(1, id);
            pstmt2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editAnswerQuestion(int id, EntityModel entityModel) {
        String query = "Select id FROM answers WHERE question_id = ? and language = ?";
        String query1 = "UPDATE answers SET question = ?, answer = ?, photos = ?  WHERE id = ?";
        String query2 = "INSERT INTO answers " +
                "(language, question, answer, question_id, photos) VALUES (?, ?, ?, ?, ?)";;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt1 = conn.prepareStatement(query1);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, entityModel.getLanguage());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int questionId = rs.getInt(1);
                    pstmt1.setString(1, entityModel.getQuestion());
                    pstmt1.setString(2, entityModel.getAnswer());
                    pstmt1.setBytes(3, combineImages(entityModel.getPhotos()));
                    pstmt1.setInt(4, questionId);
                    pstmt1.execute();
                } else {
                    pstmt2.setString(1, entityModel.getLanguage());
                    pstmt2.setString(2, entityModel.getQuestion());
                    pstmt2.setString(3, entityModel.getAnswer());
                    pstmt2.setInt(4, id);
                    pstmt2.setBytes(5, combineImages(entityModel.getPhotos()));
                    pstmt2.execute();
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notHelp(int id) {
        String query = "SELECT not_help FROM questions WHERE id = ?";
        String query2 = "UPDATE questions SET not_help = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pstmt2.setInt(1, rs.getInt(1) + 1);
                    pstmt2.setInt(2, id);
                    pstmt2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notQuestion(String theme) {
        String query = "SELECT not_question FROM themes WHERE theme = ?";
        String query2 = "UPDATE themes SET not_question = ? WHERE theme = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setString(1, theme);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pstmt2.setInt(1, rs.getInt(1) + 1);
                    pstmt2.setString(2, theme);
                    pstmt2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void formatted() {
        String query = "SELECT id, answer FROM answers";
        String query2 = "UPDATE answers SET answer = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String answer = rs.getString(2);
                if (!containsUnescapedSpecialRegex(answer)) {
                    pstmt2.setInt(2, rs.getInt(1));
                    pstmt2.setString(1, Bot.escapeMarkdownV2(answer));
                    pstmt2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public List<String> getStat() {
//        List<String> list = new ArrayList<>();
//        String query = "SELECT * FROM themes";
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(query)) {
//            ResultSet rs = pstmt.executeQuery();
//            int i = 0;
//            while (rs.next()) {
//                if (i < 4) {
//                    list.add(rs.getString(2) + "\n"
//                            + rs.getInt(3) + "cl/" + rs.getInt(4) + "nq");
//                } else {
//                    list.add(rs.getString(2) + "\n" + rs.getInt(3));
//                }
//                i++;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }

    public void language(long userId) {
        String query = "SELECT click FROM themes WHERE theme = ?";
        String query2 = "UPDATE themes SET click = ? WHERE theme = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setString(1, Bot.getUsers().get(userId));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pstmt2.setInt(1, rs.getInt(1) + 1);
                    pstmt2.setString(2, Bot.getUsers().get(userId));
                    pstmt2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Long> getUsers() {
        List<Long> result = new ArrayList<>();
        String query = "SELECT user_id FROM users_pool1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet resultSet =  pstmt.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getLong(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public long addNewToPool() {
        int id = 0;
        String insertOrUpdate = "INSERT INTO questions (theme_id) VALUES (?)";
        String insertQuestion = "INSERT INTO news_pool " +
                "(language, news_name, news_text, news_date, news_id, photo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt2 = conn.prepareStatement(insertOrUpdate, Statement.RETURN_GENERATED_KEYS)) {

            pstmt2.setInt(1, getThemeId(Bot.getCurrentThemeToAdd()));
            pstmt2.execute();
            try (ResultSet generatedKeys = pstmt2.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                    for  (EntityModel entity : Bot.getEntityModels()){
                        try (PreparedStatement pstmt1 = conn.prepareStatement(insertQuestion)) {
                            pstmt1.setString(1, entity.getLanguage());
                            pstmt1.setString(2, entity.getQuestion().split(" \\^")[0]);
                            pstmt1.setString(3, entity.getAnswer());
                            pstmt1.setString(4, entity.getQuestion().split(" \\^")[1]);
                            pstmt1.setInt(5, id);
                            pstmt1.setBytes(6, combineImages(entity.getPhotos()));
                            pstmt1.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public List<EntityModel> getNews(long userId) {
        List<EntityModel> result = new ArrayList<>();

        String query = "SELECT * FROM news_pool WHERE language = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, Bot.getUsers().get(userId));
            ResultSet resultSet =  pstmt.executeQuery();
            while (resultSet.next()) {
                result.add(
                        new EntityModel(
                                resultSet.getString("language"),
                                resultSet.getString("news_name"),
                                resultSet.getString("news_text"),
                                splitImages(resultSet.getBytes("photo"))
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public EntityModel getNew(long userId, long newId) {
        EntityModel result = null;

        String query = "SELECT * FROM news_pool WHERE language = ? and news_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, Bot.getUsers().get(userId));
            pstmt.setLong(2, newId);
            ResultSet resultSet =  pstmt.executeQuery();
            if (resultSet.next()) {
                result = new EntityModel(
                        resultSet.getString("language"),
                        resultSet.getString("news_name"),
                        resultSet.getString("news_text"),
                        splitImages(resultSet.getBytes("photo"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private int getThemeId(String theme) {
        int id = -1;
        String query = "SELECT id FROM themes where theme = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, theme);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public String getUserLanguage(long userId) {
        String language = null;
        String query = "SELECT language FROM users_pool1 WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                language = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return language;
    }

    public void setLanguageToUser(String language, long userId) {
        String query = "INSERT INTO users_pool1 (language, user_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE language = VALUES(language)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, language);
            pstmt.setLong(2, userId);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] combineImages(List<byte[]> images) {
        int totalSize = 0;
        for (byte[] image : images) {
            totalSize += 4 + image.length; // 4 байта для длины каждого изображения
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        for (byte[] image : images) {
            buffer.putInt(image.length); // Записываем длину изображения (4 байта)
            buffer.put(image); // Записываем само изображение
        }
        return buffer.array();
    }

    private List<byte[]> splitImages(byte[] combinedPhotos) {
        List<byte[]> images = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(combinedPhotos);

        while (buffer.hasRemaining()) {
            int length = buffer.getInt(); // Читаем длину изображения (4 байта)
            byte[] image = new byte[length];
            buffer.get(image); // Читаем само изображение
            images.add(image);
        }

        return images;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private static final Pattern MDV2_ESCAPED =
            Pattern.compile("\\\\([\\[\\]\\(\\)'>\\#\\+\\-\\=\\|\\{\\}\\.\\!:/])");

    public boolean containsUnescapedSpecialRegex(String text) {
        return MDV2_ESCAPED.matcher(text).find();
    }
}
