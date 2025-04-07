package org.example;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:mysql://db-mysql-fra1-for-sed-viktor-do-user-4811370-0.h.db.ondigitalocean.com:25060/defaultdb";
    private static final String USERNAME = "doadmin";
    private static final String PASSWORD = "AVNS_qDXXsgfxcqfcSLOCZDb";

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
//            String query = "INSERT INTO themes (theme) VALUES (?)";
            conn.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            try (PreparedStatement pstmt = conn.prepareStatement(createTableThemes);
                 PreparedStatement pstmt1 = conn.prepareStatement(createTable);
                 PreparedStatement pstmt2 = conn.prepareStatement(createTableQuestion);
                 PreparedStatement pstmt3 = conn.prepareStatement(createTableKeys)
            ) {
                pstmt.execute();
                pstmt1.execute();
                pstmt2.execute();
                pstmt3.execute();
            }

//            String query = "INSERT INTO themes (theme) VALUES  (?)";
//
//            for (String s : Bot.getThemesSum()) {
//                try (PreparedStatement pstmt4 = conn.prepareStatement(query)) {
//                    pstmt4.setString(1, s);
//                    pstmt4.execute();
//                }
//            }

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

    public Map<Integer, String> getQuestionsByTheme(String theme, Long userId) {
        String query = "SELECT click FROM themes WHERE id = ?";
        String query2 = "SELECT id, click, not_help FROM questions WHERE theme_id = ?";
        String query3 = "SELECT question FROM answers WHERE question_id = ? and language = ?";
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
                pstmt3.setString(2, Bot.getLanguage());
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

    public Set<Map.Entry<String, List<byte[]>>> getAnswerByQuestion(int id) {
        String query = "SELECT answer, photos FROM answers WHERE question_id = ? and language = ?";
        String query2 = "SELECT click FROM questions WHERE id = ?";
        String query3 = "UPDATE questions SET click = ? WHERE id = ?";
        Map<String, List<byte[]>> res = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2);
             PreparedStatement pstmt3 = conn.prepareStatement(query3)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, Bot.getLanguage());
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
        String insertOrUpdate = "INSERT INTO keys_access (key_type, _key) VALUES (?, ?)";
        try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(insertOrUpdate)) {
            pstmt.setString(1, type);
            pstmt.setString(2, key);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getKeys(String type, int id) {
        String res = null;
        String query = "SELECT _key FROM keys_access WHERE tg_id = ? and key_type = ?";
        String query1 = "SELECT _key FROM keys_access WHERE key_type = ?";
        String query2 = "UPDATE keys_access SET tg_id = ? WHERE _key = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt1 = conn.prepareStatement(query1);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    res = rs.getString(1);
                } else {
                    pstmt1.setString(1, type);
                    try (ResultSet rs1 = pstmt1.executeQuery()) {
                        if (rs1.next()) {
                            res = rs1.getString(1);
                            pstmt2.setInt(1, id);
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

    public void language() {
        String query = "SELECT click FROM themes WHERE theme = ?";
        String query2 = "UPDATE themes SET click = ? WHERE theme = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement pstmt2 = conn.prepareStatement(query2)) {
            pstmt.setString(1, Bot.getLanguage());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pstmt2.setInt(1, rs.getInt(1) + 1);
                    pstmt2.setString(2, Bot.getLanguage());
                    pstmt2.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private int getThemeId(String theme) {
        int id = 0;
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
}
