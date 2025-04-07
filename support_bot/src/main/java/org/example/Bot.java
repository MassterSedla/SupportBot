package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import lombok.Getter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Bot {
    private final static String botToken = "";

    private final static Set<Long> admins = new HashSet<>();
    private final TelegramBot bot;
    private final Database database;
    private Long previousMessageId;

    @Getter
    private static String language;
    @Getter
    private static final List<String> languages = List.of("En \uD83C\uDDEC\uD83C\uDDE7",
            "Uk \uD83C\uDDFA\uD83C\uDDE6", "Az \uD83C\uDDE6\uD83C\uDDFF",
            "Kz \uD83C\uDDF0\uD83C\uDDFF", "Ru \uD83D\uDD4A");;
    @Getter
    private static List<EntityModel> entityModels;
    private List<byte[]> photos;
    private static List<String> themes;
    private static List<String> themes2;
    @Getter
    private static List<String> themesSum;
    public Map<String, String> noRes = new HashMap<>();
    private final Map<String, String> startMessage = new HashMap<>();
    private final Map<String, String> employees = new HashMap<>();
    private final Map<String, String> chooseSystem = new HashMap<>();
    private final Map<String, String> mainMenu = new HashMap<>();
    private final Map<String, String> chooseQuestion = new HashMap<>();
    private final Map<String, List<String>> answersHelp = new HashMap<>();
    private final Map<String, String> dontHaveQuestion = new HashMap<>();
    private final Map<String, String> video = new HashMap<>();
    private final Map<String, String> presentation = new HashMap<>();
    private final Map<String, String> getKey = new HashMap<>();
    private final Map<String, String> links = new HashMap<>();
    private final Map<String, String> howPlatformForKey = new HashMap<>();
    private final Map<String, String> endOfKey = new HashMap<>();

//    private final Map<String, String> closeQuestionMessage = new HashMap<>();

    private final String videoAnswer = "";
    private final String presentationAnswer = "";
    private final String linksAnswer = """
            AWAX Android Google Play - https://play.google.com/store/apps/details?id=com.awaxtech.app&hl=en
            AWAX iOS - https://apps.apple.com/ua/app/awax-block-ads-for-safari/id1485689157?l=ru\s
            AWAX Android apk - https://awaxtech.com/ru
            AWAX PC Chrome - https://chromewebstore.google.com/detail/awax/aadlckelcockpdgplkdllgokjnckncll?hl=ru
                        
            OpenTube Android apk - https://opentube.ai/ru/
            OpenTube PC Chrome - https://chromewebstore.google.com/detail/opentube/jnmphegbidojlnkglhoimenbplbkhjib?hl=ru
            """;

    private List<String> languagesToAdd;
    private String currentLanguageToAdd;
    @Getter
    private static String currentThemeToAdd;
    private int currentId;
    private String currentTheme;
    private boolean messageToAssist;
    private boolean addQuestion;

    private static final String Android = "Android \uD83E\uDD16";
    private static final String Chrome = "Chrome \uD83C\uDF10";
    private static final String iOS = "iOS \uD83C\uDF4E";



    public Bot() {
        bot = new TelegramBot(botToken);
        cleanUpdates();
        themes = List.of( "Awax", "OpenTube", "Employees", "Languages\n" + languages.get(0).substring(2) + languages.get(1).substring(2)
                + languages.get(2).substring(2) + languages.get(3).substring(2) + languages.get(4).substring(2));
        themes2 = List.of(Android, Chrome, iOS);
        themesSum = List.of(
                themes.get(0) + "-" + themes2.get(0),
                themes.get(0) + "-" + themes2.get(1),
                themes.get(0) + "-" + themes2.get(2),
                themes.get(1) + "-" + themes2.get(0),
                themes.get(1) + "-" + themes2.get(1)
        );
        language = "Ru \uD83D\uDD4A";
        database = new Database();
        noRes.put(Bot.getLanguages().get(0), "I don't have a question on this topic");
        noRes.put(Bot.getLanguages().get(1), "Мого питання на цю тему немає");
        noRes.put(Bot.getLanguages().get(2), "Bu mövzuda sualım yoxdur");
        noRes.put(Bot.getLanguages().get(3), "Бұл тақырып бойынша менің сұрағым жоқ");
        noRes.put(Bot.getLanguages().get(4), "Моего вопроса по этой теме нет");
        startMessage.put(languages.get(0), "Welcome to the support bot for the AWAX ad blocker and the OpenTube ad-free video app! Please select your operating system and question the topic. You can change the language in the menu");
        startMessage.put(languages.get(1), "Ласкаво просимо до бота підтримки блокувальника реклами AWAX і додатка для перегляду відео без реклами OpenTube! Будь ласка, виберіть вашу операційну систему та тему питання. You can change the language in the menu");
        startMessage.put(languages.get(2), "AWAX reklam blokeri və OpenTube reklamsız video tətbiqi üçün dəstək botuna xoş gəlmisiniz! Zəhmət olmasa, əməliyyat sisteminizi və sual mövzusunu seçin. You can change the language in the menu");
        startMessage.put(languages.get(3), "AWAX жарнама блоктаушысы мен OpenTube жарнамасыз бейне қолданбасына арналған қолдау ботына қош келдіңіз! Өтінемін, операциялық жүйеңіз бен сұрақ тақырыбын таңдаңыз. You can change the language in the menu");
        startMessage.put(languages.get(4), "Добро пожаловать в бот поддержки блокировщика рекламы AWAX и приложения для видео без рекламы OpenTube! Пожалуйста, выберите вашу операционную систему и тему вопроса. You can change the language in the menu");
        employees.put(languages.get(0), "Employees");
        employees.put(languages.get(1), "Співробітники");
        employees.put(languages.get(2), "İşçilər");
        employees.put(languages.get(3), "Қызметкерлер");
        employees.put(languages.get(4), "Сотрудники");
        chooseSystem.put(languages.get(0), "Select your operating system");
        chooseSystem.put(languages.get(1), "Виберіть вашу операційну систему");
        chooseSystem.put(languages.get(2), "Əməliyyat sisteminizi seçin");
        chooseSystem.put(languages.get(3), "Операциялық жүйеңізді таңдаңыз");
        chooseSystem.put(languages.get(4), "Выберите вашу операционную систему");
        mainMenu.put(languages.get(0), "Main menu");
        mainMenu.put(languages.get(1), "Головне меню");
        mainMenu.put(languages.get(2), "Əsas menyu");
        mainMenu.put(languages.get(3), "Бас мәзір");
        mainMenu.put(languages.get(4), "Главное меню");
        chooseQuestion.put(languages.get(0), "Select a question from the list:");
        chooseQuestion.put(languages.get(1), "Виберіть питання зі списку:");
        chooseQuestion.put(languages.get(2), "Siyahıdan sual seçin:");
        chooseQuestion.put(languages.get(3), "Тізімнен сұрақ таңдаңыз:");
        chooseQuestion.put(languages.get(4), "Выберите вопрос из списка:");
        answersHelp.put(languages.get(0), List.of("Did this answer help you?", "Yes", "No", "Thank you"));
        answersHelp.put(languages.get(1), List.of("Чи допомогла вам ця відповідь?", "Так", "Ні", "Дякую"));
        answersHelp.put(languages.get(2), List.of("Bu cavab sizə kömək etdi?", "Bəli", "Xeyr", "Çox sağ ol"));
        answersHelp.put(languages.get(3), List.of("Бұл жауап сізге көмектесті ме?", "Иә", "Жоқ", "Рақмет сізге"));
        answersHelp.put(languages.get(4), List.of("Помог ли вам этот ответ?", "Да", "Нет", "Спасибо"));
        dontHaveQuestion.put(languages.get(0), "Write your question to the operator https://t.me/BoxAssistant, and we will try to resolve it as soon as possible");
        dontHaveQuestion.put(languages.get(1), "Напишіть своє питання оператору https://t.me/BoxAssistant, і ми постараємося якомога швидше його вирішити");
        dontHaveQuestion.put(languages.get(2), "Operatora sualınızı yazın https://t.me/BoxAssistant, biz onu mümkün qədər tez həll etməyə çalışacağıq");
        dontHaveQuestion.put(languages.get(3), "Операторға сұрағыңызды жазыңыз https://t.me/BoxAssistant, біз оны мүмкіндігінше тез шешуге тырысамыз");
        dontHaveQuestion.put(languages.get(4), "Напишите свой вопрос оператору https://t.me/BoxAssistant и мы постараемся как можно быстрее его решить");
        video.put(languages.get(0), "Video");
        video.put(languages.get(1), "Відео");
        video.put(languages.get(2), "Video");
        video.put(languages.get(3), "Бейне");
        video.put(languages.get(4), "Видео");
        presentation.put(languages.get(0), "Presentation");
        presentation.put(languages.get(1), "Презентація");
        presentation.put(languages.get(2), "Təqdimatı");
        presentation.put(languages.get(3), "Презентациясы");
        presentation.put(languages.get(4), "Презентация");
        getKey.put(languages.get(0), "Get a key");
        getKey.put(languages.get(1), "Отримати ключ");
        getKey.put(languages.get(2), "Açar əldə etmək");
        getKey.put(languages.get(3), "Кілт алу");
        getKey.put(languages.get(4), "Получить ключ");
        links.put(languages.get(0), "Links");
        links.put(languages.get(1), "Посилання");
        links.put(languages.get(2), "Linklər");
        links.put(languages.get(3), "Сілтемелер");
        links.put(languages.get(4), "Ссылки");
        howPlatformForKey.put(languages.get(0), "Which app do you need a key for?");
        howPlatformForKey.put(languages.get(1), "Для якого додатка вам потрібен ключ?");
        howPlatformForKey.put(languages.get(2), "Hansı tətbiq üçün açar lazımdır?");
        howPlatformForKey.put(languages.get(3), "Қай қолданбаға кілт қажет?");
        howPlatformForKey.put(languages.get(4), "Для какого приложения вам нужен ключ?");
        endOfKey.put(languages.get(0), "Sorry, the keys are out of stock");
        endOfKey.put(languages.get(1), "Вибачте, ключі закінчилися");
        endOfKey.put(languages.get(2), "Üzr istəyirik, açarlar bitib");
        endOfKey.put(languages.get(3), "Кешіріңіз, кілттер аяқталды");
        endOfKey.put(languages.get(4), "Извините, ключи закончились");
//        closeQuestionMessage.put(languages.get(0), "The question was closed");
//        closeQuestionMessage.put(languages.get(1), "Питання було закрито");
//        closeQuestionMessage.put(languages.get(2), "Sual bağlandı");
//        closeQuestionMessage.put(languages.get(3), "Сұрақ жабылды");
//        closeQuestionMessage.put(languages.get(4), "Вопрос был закрыт");
        photos = new ArrayList<>();
        entityModels = new ArrayList<>();
        setBotCommands();
//        admins.add(703275333L);
        messageToAssist = false;
        addQuestion = false;
    }


    public void mainMethod() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
//                update.message().from().username();
                if (update.message() != null && (update.message().text() != null
                        || update.message().caption() != null)) {
                    String messageText;
                    MessageEntity[] formatted;
                    if (update.message().photo() != null ||
                            update.message().video() != null) {
                        messageText = update.message().caption();
                        formatted = update.message().captionEntities();
                    } else {
                        messageText = update.message().text();
                        formatted = update.message().entities();
                    }
                    long chatId = update.message().chat().id();
                    if (messageText.equals("/start")) {
                        start(update.message());
                    } else if (messageText.equals("/admin")) {
                        if (admins.contains(update.message().from().id())) {
                            admin(chatId);
                        }
                    } else if (messageText.equals("/admin_awax_24_pass")) {
                        long id = update.message().from().id();
                        admins.add(id);
                        if (admins.contains(update.message().from().id())) {
                            admin(chatId);
                        }
                    }
//                    else if (messageText.startsWith("/reply")) {
//                        replyFromAssist(messageText);
//                    } else if (messageToAssist) {
//                        bot.execute(new SendMessage(admins.entrySet().stream().findFirst().get().getValue(),
//                                "Question from user:\n\n" + messageText +
//                                "\n\nTo answer in the first line write: /reply" + chatId +
//                                "\n\nTo close question in the first line write: /reply_close" + chatId));
//                    }
                    else if (employees.containsValue(messageText)) {
                        employeesMenu(update);
                    } else if (themes.contains(messageText)) {
                        if (themes.stream().limit(2).toList().contains(messageText)) {
                            getSystem(messageText, update);
                        } else if (messageText.equals(themes.get(3))) {
                            getLanguagesChoose(chatId);
                        }
                    } else if (messageText.contains(themes.get(0)) || messageText.equals(themes.get(1))) {
                        String[] keys = messageText.split("\n");
                        String type = keys[0];
                        for (int i = 1; i < messageText.split("\n").length; i++) {
                            database.setKeys(type, keys[i]);
                        }
                    } else if (themes2.contains(messageText)) {
                        if (currentTheme == null) start(update.message());
                        else if (currentTheme.contains(themes.get(0))) currentTheme = themes.get(0);
                        else if (currentTheme.contains(themes.get(1))) currentTheme = themes.get(1);
                        getQuestion(currentTheme + "-" + messageText, update);
                    } else if (addQuestion) {
                        if (admins.contains(update.message().from().id())) {
                            EntityModel entityModel = new EntityModel(withFormatted(formatted, messageText), currentLanguageToAdd);
                            addQuestionAnswer(update.message(), entityModel);
                        }
                    } else if (mainMenu.containsValue(messageText)) {
                        start(update.message());
                    } else  if (messageText.equals(getKey.get(language))) {
                        getKeys(chatId);
                    } else if (messageText.equals(video.get(language))) {
                        bot.execute(new SendMessage(chatId, videoAnswer));
                    } else if (messageText.equals(presentation.get(language))) {
                        bot.execute(new SendMessage(chatId, presentationAnswer));
                    } else if (messageText.equals(links.get(language))) {
                        bot.execute(new SendMessage(chatId, linksAnswer));
                    }
                } else if(update.callbackQuery() != null) {
                    setUpdatesListener(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }


    public void setUpdatesListener(Update update) {
        String callbackData = update.callbackQuery().data();
        Message message = update.callbackQuery().message();
        long chatId = update.callbackQuery().maybeInaccessibleMessage().chat().id();

        if (isInteger(callbackData)) {
            database.language();
            if (Integer.parseInt(callbackData) == 99999) {
                EditMessageText edit = new EditMessageText(chatId, message.messageId(), noRes.get(language));
                bot.execute(edit);
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(mainMenu.get(language));
                replyKeyboardMarkup.resizeKeyboard(true);
                database.notQuestion(currentTheme);
                bot.execute(new SendMessage(chatId, dontHaveQuestion.get(language))
                        .replyMarkup(replyKeyboardMarkup));
            } else {
                currentId = Integer.parseInt(callbackData);
                Set<Map.Entry<String, List<byte[]>>> answer = database
                        .getAnswerByQuestion(Integer.parseInt(callbackData));
                String questionAnswer = answer.stream().findFirst().get().getKey();

                EditMessageText editMessageText = new
                        EditMessageText(chatId, message.messageId(), questionAnswer.split("\n")[0]);
                bot.execute(editMessageText.parseMode(ParseMode.MarkdownV2));

                for (byte[] photo : answer.stream().findFirst().get().getValue()) {
                    if (answer.stream().findFirst().get().getKey().contains("<photo>")) {
                        SendPhoto request = new SendPhoto(chatId, photo);
                        bot.execute(request);
                    } else if (answer.stream().findFirst().get().getKey().contains("<video>")) {
                        SendVideo request = new SendVideo(chatId, photo);
                        bot.execute(request);
                    }
                }
                SendMessage sendMessage =
                        new SendMessage(chatId, questionAnswer
                                .replaceAll(Pattern.quote(questionAnswer.split("\n")[0]), "")
                                .replaceAll("<video> ", "")
                                .replaceAll("<photo> ", "")
                                .replaceAll("([\\[\\]()`>#+\\-=|{}.!])", "\\\\$1")
                        )
                                .parseMode(ParseMode.MarkdownV2)
                                .replyMarkup(new ReplyKeyboardRemove());
                bot.execute(sendMessage);

                List<String> list = answersHelp.get(language);
                SendMessage request = new SendMessage(chatId, list.get(0))
                        .replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton(list.get(1)).callbackData("yes"),
                                new InlineKeyboardButton(list.get(2)).callbackData("no")
                        ));
                bot.execute(request);

                currentTheme = null;
            }
        } else {
            EditMessageText edit = new EditMessageText(chatId, message.messageId(), update.callbackQuery().data());
            bot.execute(edit);
            if (callbackData.equals("yes")) {
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(mainMenu.get(language));
                replyKeyboardMarkup.resizeKeyboard(true);
                bot.execute(new SendMessage(chatId, answersHelp.get(language).get(3))
                        .replyMarkup(replyKeyboardMarkup));
            } else if (callbackData.equals("no")) {
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(mainMenu.get(language));
                replyKeyboardMarkup.resizeKeyboard(true);
                database.notHelp(currentId);
                bot.execute(new SendMessage(chatId, dontHaveQuestion.get(language))
                        .replyMarkup(replyKeyboardMarkup));
            } else if (callbackData.contains("del_") &&
                    isInteger(callbackData.replaceAll("del_", ""))) {
                database.deleteAnswerQuestion(Integer
                        .parseInt(callbackData.replaceAll("del_", "")));
                bot.execute(new SendMessage(chatId, "Deleting completed"));
            } else if (callbackData.equals("add")) {
                addQuestion = true;
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                themesSum.stream().map(e -> new InlineKeyboardButton(e).callbackData(e))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(chatId, "Choose theme:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (callbackData.equals("delete")) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                themesSum.stream().map(e -> new InlineKeyboardButton(e).callbackData("del_" + e))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(chatId, "Choose theme:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (themesSum.contains(callbackData)) {
                currentThemeToAdd = callbackData;
                entityModels.clear();
                languagesToAdd = new ArrayList<>(languages);
                SendMessage request = new SendMessage(chatId, "Choose language: ")
                        .replyMarkup(new InlineKeyboardMarkup(languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                                .callbackData(e + "_toAdd")).toArray(InlineKeyboardButton[]::new)));
                bot.execute(request);
            } else if (themesSum.contains(callbackData.replaceAll("del_", ""))) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                database.getQuestionsByTheme(callbackData.replaceAll("del_", ""), message.from().id()).entrySet().stream()
                        .map(e -> new InlineKeyboardButton(e.getValue())
                                .callbackData("del_" + e.getKey().toString()))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(chatId, "Choose question:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (languages.contains(callbackData)) {
                language = callbackData;
                start(message);
            } else if (themes.stream().limit(2).toList().contains(callbackData.replaceAll("_key", ""))) {
                getKeyOfTheme(chatId, update.callbackQuery().from().id(), callbackData.replaceAll("_key", ""));
            } else if (callbackData.equals("keys")) {
                bot.execute(new SendMessage(chatId, "First paragraph - type of keys (Awax or OpenTube)\nEvery next paragraph - one key"));
            } else if (languagesToAdd.contains(callbackData.replaceAll("_toAdd", ""))) {
                currentLanguageToAdd = callbackData.replaceAll("_toAdd", "");
                languagesToAdd.remove(currentLanguageToAdd);
                SendMessage editMessageText = new SendMessage(chatId, String.format(
                        """
                                Enter the question - answer in %s language:
                                'question text (only first paragraph)'
                                <photo> 'answer text' - if answer have photo (photo.jpg)
                                <video> 'answer text' - if answer have video
                                'answer text' - if answer dont have media materials
                                photo/video materials
                                """
                        , currentLanguageToAdd));
                bot.execute(editMessageText);
            }
        }
    }

    public void start(Message message) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                    themes.stream().limit(2).toArray(String[]::new),
                    List.of(employees.get(language), themes.get(3)).toArray(String[]::new)
        );

        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(message.chat().id(), startMessage.get(language))
                .replyMarkup(replyKeyboardMarkup);
        previousMessageId = bot.execute(request).message().messageId().longValue();
    }

    public void admin(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.addRow(new InlineKeyboardButton("Add question-answer").callbackData("add"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Delete question-answer").callbackData("delete"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Add new keys").callbackData("keys"));
        SendMessage request = new SendMessage(chatId, "Choose an action:")
                .replyMarkup(inlineKeyboard);
        bot.execute(request);
    }

    public void getLanguagesChoose(long chatId) {
        SendMessage request = new SendMessage(chatId, "Choose language:")
                .replyMarkup(new InlineKeyboardMarkup(languages.stream().map(e -> new InlineKeyboardButton(e)
                        .callbackData(e)).toArray(InlineKeyboardButton[]::new)));
        bot.execute(request);
    }

    public void getKeys(long chatId) {
        SendMessage request = new SendMessage(chatId, howPlatformForKey.get(language))
                .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(2).map(e -> new InlineKeyboardButton(e)
                        .callbackData(e + "_key")).toArray(InlineKeyboardButton[]::new)));
        bot.execute(request);
    }

    public void getKeyOfTheme(long chatId, long from, String theme) {
        String key = database.getKeys(theme, (int) from);
        if (key.contains("ключи кончились")) {
            key = "закончились ключи для" + key.replaceAll("ключи кончились", "");
            bot.execute(new SendMessage(703275333, key));
            bot.execute(new SendMessage(chatId, endOfKey.get(language)));
        } else {
            bot.execute(new SendMessage(chatId, key));
        }
    }

    public void getSystem(String theme, Update update) {
        currentTheme = theme;
        ReplyKeyboardMarkup replyKeyboardMarkup;
        if (theme.equals(themes.get(0))) {
            replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(Android, iOS, Chrome).toArray(String[]::new), List.of(mainMenu.get(language)).toArray(String[]::new));
        } else {
            replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(Android, Chrome).toArray(String[]::new), List.of(mainMenu.get(language)).toArray(String[]::new));
        }
        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(update.message().chat().id(), chooseSystem.get(language))
                .replyMarkup(replyKeyboardMarkup);
        bot.execute(request);
    }

    private void getQuestion(String theme, Update update) {
        currentTheme = theme;
        long chatId = update.message().chat().id();
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        Map<Integer, String> res = database.getQuestionsByTheme(theme, update.message().from().id());
        res.entrySet().stream()
                .map(e -> new InlineKeyboardButton(e.getValue())
                        .callbackData(e.getKey().toString().toString()))
                .forEach(inlineKeyboard::addRow);
        SendMessage request = new SendMessage(chatId, chooseQuestion.get(language))
                .replyMarkup(inlineKeyboard.addRow(new InlineKeyboardButton(noRes.get(getLanguage())).callbackData(String.valueOf(99999))));
        bot.execute(request);
    }

    public void employeesMenu(Update update) {
        ReplyKeyboardMarkup replyKeyboardMarkup;
        replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(video.get(language), presentation.get(language)).toArray(String[]::new),
                List.of(getKey.get(language), links.get(language)).toArray(String[]::new), List.of(mainMenu.get(language)).toArray(String[]::new));
        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(update.message().chat().id(), employees.get(language))
                .replyMarkup(replyKeyboardMarkup);
        bot.execute(request);
    }

    public void getImage(String fileId) {
        GetFileResponse getFileResponse = bot.execute(new GetFile(fileId));
        String filePath = getFileResponse.file().filePath();

        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        try (InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                out.write(dataBuffer, 0, bytesRead); // Записываем только прочитанные байты
            }
            photos.add(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addQuestionAnswer(Message message, EntityModel entityModel) {
        if (message.photo() != null) {
            getImage(message.photo()[3].fileId());
        }
        Video video = message.video();
        if (video != null) {
            getImage(video.fileId());
        }
        entityModel.setPhotos(new ArrayList<>(photos));
        entityModels.add(entityModel);
        photos.clear();
        if (!languagesToAdd.isEmpty()) {
            SendMessage request = new SendMessage(message.chat().id(), "Choose language: ");
            request.replyMarkup(new InlineKeyboardMarkup(
                    languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                            .callbackData(e + "_toAdd")).toArray(InlineKeyboardButton[]::new)
            ));
            bot.execute(request);
        } else {
            database.saveAnswer();
            bot.execute(new SendMessage(message.chat().id(), "Adding completed"));
            addQuestion = false;
        }
    }

    public String withFormatted(MessageEntity[] entities, String messageText) {
        StringBuilder formatted = new StringBuilder(messageText);
        if (entities != null) {
            for (int i = entities.length - 1; i >= 0; i--) {
                formatted.insert(entities[i].offset() + entities[i].length(), getCharFromFormat(entities[i].type().name()));
                formatted.insert(entities[i].offset(), getCharFromFormat(entities[i].type().name()));
            }
        }
        return formatted.toString();
    }

    public String getCharFromFormat(String format) {
        return switch (format) {
            case "bold" -> "*";
            case "italic" -> "_";
            case "underline" -> "__";
            case "strikethrough" -> "~";
            default -> "";
        };
    }

//    public void replyFromAssist(String text) {
//        String title = text.split("\n")[0];
//        long chatId;
//        if (title.contains("close")) {
//            chatId = Long.parseLong(title.replaceAll("/reply_close", ""));
//            bot.execute(new SendMessage(chatId, closeQuestionMessage.get(language)));
//            messageToAssist = false;
//        } else {
//            chatId = Long.parseLong(title.replaceAll("/reply", ""));
//            bot.execute(new SendMessage(chatId, text.replaceAll(title, "")));
//        }
//    }

    public void setBotCommands() {
        String data = "{\"commands\":[{\"command\":\"start\",\"description\":\"Начать работу с ботом\"}]}";
        try {
            URL url = new URL("https://api.telegram.org/bot" + botToken + "/setMyCommands");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(
                            conn.getInputStream(), StandardCharsets.UTF_8
                    ))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void cleanUpdates() {
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0);
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        List<Update> updates = updatesResponse.updates();

        int lastUpdateId = 0;
        for (Update update : updates) {
            lastUpdateId = Math.max(lastUpdateId, update.updateId());
        }

        if (lastUpdateId > 0) {
            getUpdates.offset(lastUpdateId + 1);
            bot.execute(getUpdates);
        }
    }

    public static boolean isAdmin(Long id) {
        return admins.contains(id);
    }

}

