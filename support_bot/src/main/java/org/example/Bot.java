package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import kotlin.Pair;
import lombok.Getter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot {
    private final static String botToken =
//            "6732682706:AAEHk2B03YcIR3RY-EWGt3Q8PAL_0m0CINE";
            "8177256238:AAFCZb7Fc09egBEoj7SE4ApNSJiDr0LRlJg";

    private final static Set<Long> admins = new HashSet<>();
    private final TelegramBot bot;
    private final Database database;
    private Long previousMessageId;

    @Getter
    private static final List<String> languages = List.of("En \uD83C\uDDEC\uD83C\uDDE7",
            "Uk \uD83C\uDDFA\uD83C\uDDE6", "Az \uD83C\uDDE6\uD83C\uDDFF",
            "Kz \uD83C\uDDF0\uD83C\uDDFF", "Ge \uD83C\uDDEC\uD83C\uDDEA", "Ru \uD83D\uDD4A");;
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
    private final Map<String, String> video2 = new HashMap<>();
    private final Map<String, String> presentation2 = new HashMap<>();
    private final Map<String, String> getKey = new HashMap<>();
    private final Map<String, String> news = new HashMap<>();
    private final Map<String, String> links = new HashMap<>();
    private final Map<String, String> howPlatformForKey = new HashMap<>();
    private final Map<String, String> endOfKey = new HashMap<>();
    private final Map<String, String> emister = new HashMap<>();


    private final List<String> videoAnswer = List.of("https://youtu.be/LSXD7a17IgU?si=2iIKo73VVhGqIsqp");
    private final String linksAnswer = """
            AWAX
            ▫️AWAX Android Google Play - https://play.google.com/store/apps/details?id=com.awaxtech.app&hl=en
            ▫️AWAX iOS - https://apps.apple.com/ua/app/awax-block-ads-for-safari/id1485689157?l=ru\s
            ▫️AWAX Android apk - https://awaxtech.com/ru
            ▫️AWAX PC Chrome - https://chromewebstore.google.com/detail/awax/aadlckelcockpdgplkdllgokjnckncll?hl=ru
            
            OpenTube
            ▫️OpenTube Android apk - https://opentube.ai/ru/
            ▫️OpenTube PC Chrome - https://chromewebstore.google.com/detail/opentube/jnmphegbidojlnkglhoimenbplbkhjib?hl=ru
            
            Emister
            ▫️Emister 2 AI Google Play - https://play.google.com/store/apps/details?id=com.rocketraven.emister
            ▫️Emister 2 AI iOS https://apps.apple.com/cd/app/emister-2-ai-learn-english/id6744888851
            """;

    private List<String> languagesToAdd;
    private String currentLanguageToAdd;
    private String currentLanguageToEdit;
    private int currentIdToEdit;
    @Getter
    private static String currentThemeToAdd;
    private int currentId;
    private boolean addQuestion;
    private boolean addNew;
    private boolean addPresentation;
    private boolean addVideo;

    private static final String Android = "Android \uD83E\uDD16";
    private static final String Chrome = "Chrome \uD83C\uDF10";
    private static final String iOS = "iOS \uD83C\uDF4E";
    private static final Map<String, String> grammar = new HashMap<>();


    @Getter
    private static Map<Long, String> users = new HashMap<>();
    private static Map<Long, String> currentTheme = new HashMap<>();

    public Bot() {
        bot = new TelegramBot(botToken);
        cleanUpdates();
        grammar.put(languages.get(0), "Grammar \uD83D\uDCDA");
        grammar.put(languages.get(1), "Граматика \uD83D\uDCDA");
        grammar.put(languages.get(2), "Qrammatika \uD83D\uDCDA");
        grammar.put(languages.get(3), "Грамматика \uD83D\uDCDA");
        grammar.put(languages.get(4), "გრამატიკა \uD83D\uDCDA");
        grammar.put(languages.get(5), "Грамматика \uD83D\uDCDA");
        themes = List.of( "Awax", "OpenTube", "Emister", "Employees", "Languages\n" + languages.get(0).substring(2) + languages.get(1).substring(2)
                + languages.get(2).substring(2) + languages.get(3).substring(2) + languages.get(4).substring(2) + languages.get(5).substring(2));
        themes2 = List.of(Android, Chrome, iOS, Android + "/" + iOS, grammar.get(languages.get(0)));
        themesSum = List.of(
                themes.get(0) + "-" + themes2.get(0),
                themes.get(0) + "-" + themes2.get(1),
                themes.get(0) + "-" + themes2.get(2),
                themes.get(1) + "-" + themes2.get(0),
                themes.get(1) + "-" + themes2.get(1),
                themes.get(2) + "-" + themes2.get(3),
                themes.get(2) + "-" + themes2.get(4)
        );
        database = new Database();
        noRes.put(Bot.getLanguages().get(0), "I don't have a question on this topic");
        noRes.put(Bot.getLanguages().get(1), "Мого питання на цю тему немає");
        noRes.put(Bot.getLanguages().get(2), "Bu mövzuda sualım yoxdur");
        noRes.put(Bot.getLanguages().get(3), "Бұл тақырып бойынша менің сұрағым жоқ");
        noRes.put(Bot.getLanguages().get(4), "ჩემი კითხვა სიაში არ არის");
        noRes.put(Bot.getLanguages().get(5), "Моего вопроса по этой теме нет");
        startMessage.put(languages.get(0), "Welcome to the support bot:\n" +
                "\uD83C\uDF10 AWAX — ad blocking on websites and in apps\n" +
                " \uD83C\uDFAC OpenTube — Premium ad-free viewing\n" +
                " \uD83D\uDCD8 Emister 2 AI — English learning app\n" +
                "Select your operating system and question topic.\n" +
                " You can change the language in the menu \uD83C\uDF0D\n");
        startMessage.put(languages.get(1), "Ласкаво просимо до бота підтримки:\n" +
                "\uD83C\uDF10 AWAX — блокування реклами на сайтах та в застосунках\n" +
                " \uD83C\uDFAC OpenTube — Преміум перегляд без реклами\n" +
                " \uD83D\uDCD8 Emister 2 AI — застосунок для вивчення англійської мови\n" +
                "Оберіть вашу операційну систему та тему питання.\n" +
                " You can change the language in the menu \uD83C\uDF0D\n");
        startMessage.put(languages.get(2), "Dəstək botuna xoş gəlmisiniz:\n" +
                "\uD83C\uDF10 AWAX — Saytlarda və tətbiqlərdə reklamların bloklanması\n" +
                " \uD83C\uDFAC OpenTube — Reklamsız Premium izləmə\n" +
                " \uD83D\uDCD8 Emister 2 AI — İngilis dili öyrənmək üçün tətbiq\n" +
                "Əməliyyat sisteminizi və sual mövzusunu seçin.\n" +
                " You can change the language in the menu \uD83C\uDF0D\n");
        startMessage.put(languages.get(3), "Қолдау ботына қош келдіңіз:\n" +
                "\uD83C\uDF10 AWAX — сайттар мен қолданбалардағы жарнаманы бұғаттау\n" +
                " \uD83C\uDFAC OpenTube — Жарнамасыз Premium көру\n" +
                " \uD83D\uDCD8 Emister 2 AI — Ағылшын тілін үйренуге арналған қосымша\n" +
                "Операциялық жүйені және сұрақтың тақырыбын таңдаңыз.\n" +
                " You can change the language in the menu \uD83C\uDF0D");
        startMessage.put(languages.get(4), "მოგესალმებით მხარდაჭერის ბოტში:\n" +
                "\uD83C\uDF10 AWAX — რეკლამების ბლოკირება ვებსაიტებზე და აპებში\n" +
                " \uD83C\uDFAC OpenTube — Premium ნახვა რეკლამის გარეშე\n" +
                " \uD83D\uDCD8 Emister 2 AI — ინგლისურის შესწავლის აპლიკაცია\n" +
                "აირჩიეთ ოპერაციული სისტემა და საკითხის თემა.\n" +
                " You can change the language in the menu \uD83C\uDF0D");
        startMessage.put(languages.get(5), "Добро пожаловать в бот поддержки:\n" +
                "\n" +
                "\uD83C\uDF10 AWAX — блокировка рекламы на сайтах и в приложениях\n" +
                "\uD83C\uDFAC OpenTube — Premium просмотр без рекламы\n" +
                "\uD83D\uDCD8 Emister 2 AI — приложение для изучения английского языка\n" +
                "\n" +
                "Выберите вашу операционную систему и тему вопроса.\n" +
                "You can change the language in the menu \uD83C\uDF0D\n");
        employees.put(languages.get(0), "Employees");
        employees.put(languages.get(1), "Співробітники");
        employees.put(languages.get(2), "İşçilər");
        employees.put(languages.get(3), "Қызметкерлер");
        employees.put(languages.get(4), "თანამშრომლები");
        employees.put(languages.get(5), "Сотрудники");
        chooseSystem.put(languages.get(0), "Select your operating system");
        chooseSystem.put(languages.get(1), "Виберіть вашу операційну систему");
        chooseSystem.put(languages.get(2), "Əməliyyat sisteminizi seçin");
        chooseSystem.put(languages.get(3), "Операциялық жүйеңізді таңдаңыз");
        chooseSystem.put(languages.get(4), "აირჩიეთ თქვენი საოპერაციო სისტემა");
        chooseSystem.put(languages.get(5), "Выберите вашу операционную систему");
        mainMenu.put(languages.get(0), "Main menu");
        mainMenu.put(languages.get(1), "Головне меню");
        mainMenu.put(languages.get(2), "Əsas menyu");
        mainMenu.put(languages.get(3), "Бас мәзір");
        mainMenu.put(languages.get(4), "მთავარი მენიუ");
        mainMenu.put(languages.get(5), "Главное меню");
        chooseQuestion.put(languages.get(0), "Select a question from the list:");
        chooseQuestion.put(languages.get(1), "Виберіть питання зі списку:");
        chooseQuestion.put(languages.get(2), "Siyahıdan sual seçin:");
        chooseQuestion.put(languages.get(3), "Тізімнен сұрақ таңдаңыз:");
        chooseQuestion.put(languages.get(4), "აირჩიეთ კითხვა სიიდან:");
        chooseQuestion.put(languages.get(5), "Выберите вопрос из списка:");
        answersHelp.put(languages.get(0), List.of("Did this answer help you?", "Yes", "No", "Thank you"));
        answersHelp.put(languages.get(1), List.of("Чи допомогла вам ця відповідь?", "Так", "Ні", "Дякую"));
        answersHelp.put(languages.get(2), List.of("Bu cavab sizə kömək etdi?", "Bəli", "Xeyr", "Çox sağ ol"));
        answersHelp.put(languages.get(3), List.of("Бұл жауап сізге көмектесті ме?", "Иә", "Жоқ", "Рақмет сізге"));
        answersHelp.put(languages.get(4), List.of("დაგეხმარათ ეს პასუხი?", "კი", "არა", "Спасибо"));
        answersHelp.put(languages.get(5), List.of("Помог ли вам этот ответ?", "Да", "Нет", "Спасибо"));
        dontHaveQuestion.put(languages.get(0), "Write your question to the operator https://t.me/BoxAssistant, and we will try to resolve it as soon as possible");
        dontHaveQuestion.put(languages.get(1), "Напишіть своє питання оператору https://t.me/BoxAssistant, і ми постараємося якомога швидше його вирішити");
        dontHaveQuestion.put(languages.get(2), "Operatora sualınızı yazın https://t.me/BoxAssistant, biz onu mümkün qədər tez həll etməyə çalışacağıq");
        dontHaveQuestion.put(languages.get(3), "Операторға сұрағыңызды жазыңыз https://t.me/BoxAssistant, біз оны мүмкіндігінше тез шешуге тырысамыз");
        dontHaveQuestion.put(languages.get(4), "დაწერეთ თქვენი კითხვა ოპერატორს https://t.me/BoxAssistant და ჩვენ შევეცდებით რაც შეიძლება სწრაფად გადავჭრათ");
        dontHaveQuestion.put(languages.get(5), "Напишите свой вопрос оператору https://t.me/BoxAssistant и мы постараемся как можно быстрее его решить");
        video.put(languages.get(0), "Video");
        video.put(languages.get(1), "Відео");
        video.put(languages.get(2), "Video");
        video.put(languages.get(3), "Бейне");
        video.put(languages.get(4), "ვიდეო");
        video.put(languages.get(5), "Видео");
        presentation.put(languages.get(0), "Presentation");
        presentation.put(languages.get(1), "Презентація");
        presentation.put(languages.get(2), "Təqdimatı");
        presentation.put(languages.get(3), "Презентациясы");
        presentation.put(languages.get(4), "პრეზენტაცია");
        presentation.put(languages.get(5), "Презентация");
        video2.put(languages.get(0), "Watch the product video");
        video2.put(languages.get(1), "Подивитися відео про продукт");
        video2.put(languages.get(2), "Өнім туралы бейнебаянды көру");
        video2.put(languages.get(3), "Məhsul videosuna bax");
        video2.put(languages.get(4), "პროდუქტის ვიდეოს ნახვა");
        video2.put(languages.get(5), "Посмотреть видео по продукту");
        presentation2.put(languages.get(0), "Get the product presentation");
        presentation2.put(languages.get(1), "Отримати презентацію про продукт");
        presentation2.put(languages.get(2), "Məhsul təqdimatını əldə et");
        presentation2.put(languages.get(3), "Өнім туралы презентацияны алу");
        presentation2.put(languages.get(4), "პროდუქტის პრეზენტაციის მიღება");
        presentation2.put(languages.get(5), "Получить презентацию по продукту");
        getKey.put(languages.get(0), "Get a key");
        getKey.put(languages.get(1), "Отримати ключ");
        getKey.put(languages.get(2), "Açar əldə etmək");
        getKey.put(languages.get(3), "Кілт алу");
        getKey.put(languages.get(4), "გასაღების მიღება");
        getKey.put(languages.get(5), "Получить ключ");
        links.put(languages.get(0), "Links");
        links.put(languages.get(1), "Посилання");
        links.put(languages.get(2), "Linklər");
        links.put(languages.get(3), "Сілтемелер");
        links.put(languages.get(4), "ბმულები");
        links.put(languages.get(5), "Ссылки");
        howPlatformForKey.put(languages.get(0), "Which app do you need a key for?");
        howPlatformForKey.put(languages.get(1), "Для якого додатка вам потрібен ключ?");
        howPlatformForKey.put(languages.get(2), "Hansı tətbiq üçün açar lazımdır?");
        howPlatformForKey.put(languages.get(3), "Қай қолданбаға кілт қажет?");
        howPlatformForKey.put(languages.get(4), "რომელ აპზე გჭირდებათ გასაღები?");
        howPlatformForKey.put(languages.get(5), "Для какого приложения вам нужен ключ?");
        endOfKey.put(languages.get(0), "Sorry, the keys are out of stock");
        endOfKey.put(languages.get(1), "Вибачте, ключі закінчилися");
        endOfKey.put(languages.get(2), "Üzr istəyirik, açarlar bitib");
        endOfKey.put(languages.get(3), "Кешіріңіз, кілттер аяқталды");
        endOfKey.put(languages.get(4), "უკაცრავად, გასაღებები დასრულდა");
        endOfKey.put(languages.get(5), "Извините, ключи закончились");
        news.put(languages.get(0), "News");
        news.put(languages.get(1), "Новини");
        news.put(languages.get(2), "Xəbərlər");
        news.put(languages.get(3), "Жаңалықтар");
        news.put(languages.get(4), "სიახლეები");
        news.put(languages.get(5), "Новости");
        emister.put(languages.get(0), "Choose a topic for your question");
        emister.put(languages.get(1), "Виберіть тему для свого запитання");
        emister.put(languages.get(2), "Sualınız üçün mövzunu seçin");
        emister.put(languages.get(3), "Сұрағыңыздың тақырыбын таңдаңыз");
        emister.put(languages.get(4), "აირჩიეთ თქვენი შეკითხვის თემა");
        emister.put(languages.get(5), "Выберите тему для вашего вопроса");

//        closeQuestionMessage.put(languages.get(0), "The question was closed");
//        closeQuestionMessage.put(languages.get(1), "Питання було закрито");
//        closeQuestionMessage.put(languages.get(2), "Sual bağlandı");
//        closeQuestionMessage.put(languages.get(3), "Сұрақ жабылды");
//        closeQuestionMessage.put(languages.get(4), "Вопрос был закрыт");
        photos = new ArrayList<>();
        entityModels = new ArrayList<>();
        setBotCommands();
        //        admins.add(703275333L);
        addQuestion = false;
        addNew = false;
        addPresentation = false;
        currentIdToEdit = -1;
    }


    public void mainMethod() {

            bot.setUpdatesListener(updates -> {
                try {
                    for (Update update : updates) {
                        if (update.message() != null && (update.message().text() != null
                                || update.message().caption() != null)) {
                            String messageText;
                            MessageEntity[] formatted;
                            if (update.message().text() == null) {
                                messageText = update.message().caption();
                                formatted = update.message().captionEntities();
                            } else {
                                messageText = update.message().text();
                                formatted = update.message().entities();
                            }
                            long chatId = update.message().chat().id();
                            long userId = update.message().from().id();
                            if (messageText.equals("/start")) {
                                addUserToPool(userId);
                                start(chatId, userId);
                            } else if (messageText.equals("/admin")) {
                                if (admins.contains(update.message().from().id())) {
                                    admin(chatId);
                                }
                            } else if (messageText.equals("/admin_awax_24_pass")) {
                                admins.add(userId);
                                if (admins.contains(userId)) {
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
                                employeesMenu(update, userId);
                            } else if (themes.contains(messageText)) {
                                if (themes.stream().limit(3).toList().contains(messageText)) {
                                    getSystem(messageText, update, userId);
                                } else if (messageText.equals(themes.get(4))) {
                                    getLanguagesChoose(chatId);
                                }
                            } else if (messageText.split("\n")[0].equals(themes.get(0)) || messageText.split("\n")[0].equals(themes.get(1)) || messageText.split("\n")[0].equals(themes.get(2))) {
                                String[] keys = messageText.split("\n");
                                String type = keys[0];
                                for (int i = 1; i < messageText.split("\n").length; i++) {
                                    System.out.println(keys[i]);
                                    database.setKeys(type, keys[i]);
                                }
                            } else if (grammar.containsValue(messageText)) {
                                currentTheme.put(userId, themes.get(2));
                                getQuestion(currentTheme.get(userId) + "-" + grammar.get(languages.get(0)), update, userId);
                            } else if (themes2.contains(messageText)) {
                                if (currentTheme.get(userId) == null) start(chatId, userId);
                                else if (currentTheme.get(userId).contains(themes.get(0)))
                                    currentTheme.put(userId, themes.get(0));
                                else if (currentTheme.get(userId).contains(themes.get(1)))
                                    currentTheme.put(userId, themes.get(1));
                                else if (currentTheme.get(userId).contains(themes.get(2)))
                                    currentTheme.put(userId, themes.get(2));
                                getQuestion(currentTheme.get(userId) + "-" + messageText, update, userId);
                            } else if (messageText.equals(mainMenu.get(users.get(userId)))) {
                                start(chatId, userId);
                            } else if (messageText.equals(getKey.get(users.get(userId)))) {
                                getKeys(chatId, userId);
                            } else if (messageText.equals(news.get(users.get(userId)))) {
                                news(chatId, userId);
                            } else if (messageText.equals(video.get(users.get(userId)))) {
                                bot.execute(new SendMessage(chatId, video.get(users.get(userId)))
                                        .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(3).map(e -> new InlineKeyboardButton(e)
                                                .callbackData(e + "_video")).toArray(InlineKeyboardButton[]::new)
                                        )));
                            } else if (messageText.equals(presentation.get(users.get(userId)))) {
                                bot.execute(new SendMessage(chatId, presentation2.get(users.get(userId)))
                                        .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(3).map(e -> new InlineKeyboardButton(e)
                                                .callbackData(e + "_pres")).toArray(InlineKeyboardButton[]::new)
                                        )));
                            } else if (messageText.equals(links.get(users.get(userId)))) {
                                bot.execute(new SendMessage(chatId, linksAnswer));
                            } else if (addPresentation) {
                                if (admins.contains(update.message().from().id())) {
                                    addPresentation(update.message());
                                }
                            } else if (addVideo) {
                                if (admins.contains(update.message().from().id())) {
                                    addVideo(update.message());
                                }
                            } else if (addQuestion) {
                                if (admins.contains(update.message().from().id())) {
                                    EntityModel entityModel = new EntityModel(withFormatted(formatted, messageText), messageText.split("\n")[0], currentLanguageToAdd);
                                    addQuestionAnswer(update.message(), entityModel);
                                }
                            } else if (currentIdToEdit != -1) {
                                if (admins.contains(update.message().from().id())) {
                                    EntityModel entityModel = new EntityModel(withFormatted(formatted, messageText), messageText.split("\n")[0], currentLanguageToEdit);
                                    editQuestionAnswer(update.message(), entityModel);
                                }
                            }
                        } else if (update.callbackQuery() != null) {
                            setUpdatesListener(update);
                        }
                    }
                } catch (Exception e) {
                    StringBuilder error = new StringBuilder(e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(er -> error.append(er.toString() + "\n"));
                    bot.execute(new SendMessage(703275333, error.toString()));
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
    }


    public void setUpdatesListener(Update update) {
        String callbackData = update.callbackQuery().data();
        Message message = update.callbackQuery().message();
        long userId = update.callbackQuery().maybeInaccessibleMessage().chat().id();

        if (isInteger(callbackData)) {
            database.language(userId);
            if (Integer.parseInt(callbackData) == 99999) {
                EditMessageText edit = new EditMessageText(userId, message.messageId(), noRes.get(users.get(userId)));
                bot.execute(edit);
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(mainMenu.get(users.get(userId))).resizeKeyboard(true);
                database.notQuestion(currentTheme.get(userId));
                bot.execute(new SendMessage(userId, dontHaveQuestion.get(users.get(userId))).replyMarkup(replyKeyboardMarkup));
            } else {
                currentId = Integer.parseInt(callbackData);
                Set<Map.Entry<String, List<byte[]>>> answer = database
                        .getAnswerByQuestion(Integer.parseInt(callbackData), userId);
                String questionAnswer = answer.stream().findFirst().get().getKey();
                List<byte[]> photos = answer.stream().findFirst().get().getValue();
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(mainMenu.get(users.get(userId))).resizeKeyboard(true);
                EditMessageText editMessageText = new
                        EditMessageText(userId, message.messageId(), questionAnswer.split("\n")[0]);
                bot.execute(editMessageText.parseMode(ParseMode.MarkdownV2));
                if (!photos.isEmpty()) {
                    for (byte[] photo : photos) {
                        if (questionAnswer.contains("<photo\\> ")) {
                            SendPhoto request = new SendPhoto(userId, photo).caption(replace().apply(questionAnswer)).parseMode(ParseMode.MarkdownV2);
                            bot.execute(request.replyMarkup(replyKeyboardMarkup));
                        } else if (questionAnswer.contains("<video\\> ")) {
                            SendVideo request = new SendVideo(userId, photo).caption(replace().apply(questionAnswer)).parseMode(ParseMode.MarkdownV2);
                            bot.execute(request.replyMarkup(replyKeyboardMarkup));
                        } else {
                            SendDocument document = new SendDocument(userId, photo).caption(replace().apply(questionAnswer)).parseMode(ParseMode.MarkdownV2);
                            bot.execute(document.replyMarkup(replyKeyboardMarkup));
                        }
                    }
                } else {
                    SendMessage sendMessage = new SendMessage(userId, replace().apply(questionAnswer))
                            .parseMode(ParseMode.MarkdownV2);
                    bot.execute(sendMessage.replyMarkup(replyKeyboardMarkup));
                }
                List<String> list = answersHelp.get(users.get(userId));
                SendMessage request = new SendMessage(userId, list.get(0))
                        .replyMarkup(new InlineKeyboardMarkup(
                                new InlineKeyboardButton(list.get(1)).callbackData("yes"),
                                new InlineKeyboardButton(list.get(2)).callbackData("no")
                        ));
                bot.execute(request);
                currentTheme.put(userId, null);
            }
        } else {
            EditMessageText edit = new EditMessageText(userId, message.messageId(), update.callbackQuery().data());
            bot.execute(edit);
            if (callbackData.equals("yes")) {
                bot.execute(new EditMessageText(userId, message.messageId(), answersHelp.get(users.get(userId)).get(3)));
                start(userId, userId);
            } else if (callbackData.equals("no")) {
                bot.execute(new DeleteMessage(userId, message.messageId()));
                database.notHelp(currentId);
                bot.execute(new SendMessage(userId, dontHaveQuestion.get(users.get(userId))));
            } else if (callbackData.contains("presentations")) {
                bot.execute(new EditMessageText(userId, message.messageId(), presentation.get(users.get(userId)))
                        .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(3).map(e -> new InlineKeyboardButton(e)
                                .callbackData(e + "_pres_theme")).toArray(InlineKeyboardButton[]::new)
                        )));
            } else if (callbackData.contains("videos")) {
                bot.execute(new EditMessageText(userId, message.messageId(), video.get(users.get(userId)))
                        .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(3).map(e -> new InlineKeyboardButton(e)
                                .callbackData(e + "_video_theme")).toArray(InlineKeyboardButton[]::new)
                        )));
            } else if (themes.stream().limit(3).toList().contains(callbackData.replaceAll("_pres_theme", ""))) {
                add(callbackData.replaceAll("_pres_theme", ""), userId);
                addPresentation = true;
            } else if (themes.stream().limit(3).toList().contains(callbackData.replaceAll("_video_theme", ""))) {
                add(callbackData.replaceAll("_video_theme", ""), userId);
                addVideo = true;
            } else if (callbackData.contains("del_") &&
                    isInteger(callbackData.replaceAll("del_", ""))) {
                database.deleteAnswerQuestion(Integer
                        .parseInt(callbackData.replaceAll("del_", "")));
                bot.execute(new SendMessage(userId, "Deleting completed"));
            } else if (callbackData.contains("delnew_") &&
                    isInteger(callbackData.replaceAll("delnew_", ""))) {
                database.deleteNew(Integer
                        .parseInt(callbackData.replaceAll("delnew_", "")));
                bot.execute(new SendMessage(userId, "Deleting completed"));
            } else if (callbackData.contains("_edit_")) {
                String str = callbackData.replaceAll("_edit", "");
                int index = str.indexOf('_');
                currentIdToEdit = Integer.parseInt(str.substring(0, index));
                currentLanguageToEdit = str.replaceAll(currentIdToEdit + "_", "");
                messageToAdd(userId);
            } else if (callbackData.contains("edit_") &&
                    isInteger(callbackData.replaceAll("edit_", ""))) {
                languagesToAdd = new ArrayList<>(languages);
                SendMessage request = new SendMessage(userId, "Choose language: ")
                        .replyMarkup(new InlineKeyboardMarkup(languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                                .callbackData(Integer.parseInt(callbackData.replaceAll("edit_", "")) + "_edit_" + e))
                                .toArray(InlineKeyboardButton[]::new)));
                bot.execute(request);
            } else if (callbackData.equals("add")) {
                addQuestion = true;
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                themesSum.stream().map(e -> new InlineKeyboardButton(e).callbackData(e))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose theme:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (callbackData.equals("News")) {
                addQuestion = true;
                addNew = true;
                add(callbackData, userId);
            } else if (callbackData.equals("edit")) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                themesSum.stream().map(e -> new InlineKeyboardButton(e).callbackData("edit_" + e))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose theme:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (callbackData.equals("delete")) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                themesSum.stream().map(e -> new InlineKeyboardButton(e).callbackData("del_" + e))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose theme:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (callbackData.equals("deleteNews")) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                database.getNewsByTheme("News", userId).entrySet().stream()
                        .map(e -> new InlineKeyboardButton(e.getValue())
                                .callbackData("delnew_" + e.getKey().toString()))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose new:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (themesSum.contains(callbackData)) {
                add(callbackData, userId);
            } else if (themesSum.contains(callbackData.replaceAll("del_", ""))) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                database.getQuestionsByTheme(callbackData.replaceAll("del_", ""), userId).entrySet().stream()
                        .map(e -> new InlineKeyboardButton(e.getValue())
                                .callbackData("del_" + e.getKey().toString()))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose question:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (themesSum.contains(callbackData.replaceAll("edit_", ""))) {
                InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
                database.getQuestionsByTheme(callbackData.replaceAll("edit_", ""), userId).entrySet().stream()
                        .map(e -> new InlineKeyboardButton(e.getValue())
                                .callbackData("edit_" + e.getKey().toString()))
                        .forEach(inlineKeyboard::addRow);
                SendMessage request = new SendMessage(userId, "Choose question:")
                        .replyMarkup(inlineKeyboard);
                bot.execute(request);
            } else if (languages.contains(callbackData)) {
                updateLanguageToUser(callbackData, userId);
                start(userId, userId);
            } else if (themes.stream().limit(3).toList().contains(callbackData.replaceAll("_key", ""))) {
                getKeyOfTheme(userId, userId, callbackData.replaceAll("_key", ""));
            } else if (themes.stream().limit(3).toList().contains(callbackData.replaceAll("_pres", ""))) {
                bot.execute(new DeleteMessage(userId, message.messageId()));
                Pair<String, byte[]> pair = database.getPresentation(users.get(userId) + " " + callbackData.replaceAll("_pres", ""));
                if (pair != null) {
                    bot.execute(new SendDocument(userId, pair.component2())
                            .fileName("presentation.pdf")
                            .contentType("application/pdf")
                            .caption(pair.component1()));
                }
            } else if (themes.stream().limit(3).toList().contains(callbackData.replaceAll("_video", ""))) {
                bot.execute(new DeleteMessage(userId, message.messageId()));
                String video = database.getVideos(users.get(userId) + " " + callbackData.replaceAll("_video", ""));
                bot.execute(new SendMessage(userId, video));
            } else if (callbackData.equals("keys")) {
                bot.execute(new SendMessage(userId, "First paragraph - type of keys (Awax or OpenTube or Emister)\nEvery next paragraph - one key"));
            } else if (languagesToAdd.contains(callbackData.replaceAll("_toAdd", ""))) {
                currentLanguageToAdd = callbackData.replaceAll("_toAdd", "");
                languagesToAdd.remove(currentLanguageToAdd);
                if (!addPresentation) messageToAdd(userId);
                else  bot.execute(new SendMessage(userId, "Enter your presentations and text"));
            }
        }
    }

    private void add(String callbackData, long chatId) {
        currentThemeToAdd = callbackData;
        entityModels.clear();
        languagesToAdd = new ArrayList<>(languages);
        SendMessage request = new SendMessage(chatId, "Choose language: ")
                .replyMarkup(new InlineKeyboardMarkup(languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                        .callbackData(e + "_toAdd")).toArray(InlineKeyboardButton[]::new)));
        bot.execute(request);
    }

    public void start(long chatId, long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(
                    themes.stream().limit(3).toArray(String[]::new),
                    List.of(employees.get(users.get(userId)), themes.get(4)).toArray(String[]::new)
        );

        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(chatId, startMessage.get(users.get(userId)))
                .replyMarkup(replyKeyboardMarkup);
        bot.execute(request);
        currentIdToEdit = -1;
        addNew = false;
        addQuestion = false;
        addPresentation = false;
    }

    public void admin(long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        inlineKeyboard.addRow(new InlineKeyboardButton("Add question-answer").callbackData("add"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Delete question-answer").callbackData("delete"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Edit question-answer").callbackData("edit"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Add new keys").callbackData("keys"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Add News").callbackData("News"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Delete News").callbackData("deleteNews"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Set presentation").callbackData("presentations"));
        inlineKeyboard.addRow(new InlineKeyboardButton("Set video").callbackData("videos"));
        SendMessage request = new SendMessage(chatId, "Choose an action:")
                .replyMarkup(inlineKeyboard);
        bot.execute(request);
        currentIdToEdit = -1;
        addNew = false;
        addQuestion = false;
        addPresentation = false;
    }

    public void getLanguagesChoose(long chatId) {
        SendMessage request = new SendMessage(chatId, "Choose language:")
                .replyMarkup(new InlineKeyboardMarkup(languages.stream().map(e -> new InlineKeyboardButton(e)
                        .callbackData(e)).toArray(InlineKeyboardButton[]::new)));
        bot.execute(request);
    }

    public void getKeys(long chatId, long userId) {
        SendMessage request = new SendMessage(chatId, howPlatformForKey.get(users.get(userId)))
                .replyMarkup(new InlineKeyboardMarkup(themes.stream().limit(3).map(e -> new InlineKeyboardButton(e)
                        .callbackData(e + "_key")).toArray(InlineKeyboardButton[]::new)));
        bot.execute(request);
    }

    public void news(long chatId, long userId) {
        for (EntityModel e : database.getNews(userId)) {
            sendNews(chatId, e);
        }
    }

    private void sendNews(long chatId, EntityModel e) {
        if (e.getPhotos().isEmpty()) {
            SendMessage request = new SendMessage(chatId, escapeMarkdownV2(e.getQuestion()) + "\n" + replace().apply(e.getAnswer().replaceAll(" \\^", ""))).parseMode(ParseMode.MarkdownV2);
            bot.execute(request);
        } else {
            for (byte[] photo : e.getPhotos()) {
                if (e.getAnswer().contains("<photo\\> ")) {
                    SendPhoto request = new SendPhoto(chatId, photo).caption(escapeMarkdownV2(e.getQuestion()) + "\n" + replace().apply(e.getAnswer().replaceAll(" \\^", ""))).parseMode(ParseMode.MarkdownV2);
                    bot.execute(request);
                } else if (e.getAnswer().contains("<video\\> ")) {
                    SendVideo request = new SendVideo(chatId, photo).caption(escapeMarkdownV2(e.getQuestion()) + "\n" + replace().apply(e.getAnswer().replaceAll(" \\^", ""))).parseMode(ParseMode.MarkdownV2);
                    bot.execute(request);
                } else {
                    SendDocument document = new SendDocument(chatId, photo).caption(escapeMarkdownV2(e.getQuestion()) + "\n" + replace().apply(e.getAnswer().replaceAll(" \\^", ""))).parseMode(ParseMode.MarkdownV2);
                    bot.execute(document);
                }
            }
        }
    }

    private Function<String, String> replace() {
        return (x) -> x.substring(x.split("\n")[0].length())
                .replaceAll("<video\\\\> ", "")
                .replaceAll("<photo\\\\> ", "");
    }

    public void getKeyOfTheme(long chatId, long userId, String theme) {
        String key = database.getKeys(theme, userId);
        if (key.contains("ключи кончились")) {
            key = "закончились ключи для" + key.replaceAll("ключи кончились", "");
            bot.execute(new SendMessage(461857394, key));
            bot.execute(new SendMessage(chatId, endOfKey.get(users.get(userId))));
        } else {
            bot.execute(new SendMessage(chatId, key));
        }
    }

    public void getSystem(String theme, Update update, long userId) {
        currentTheme.put(userId, theme);
        ReplyKeyboardMarkup replyKeyboardMarkup;
        String text;
        if (theme.equals(themes.get(0))) {
            replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(Android, iOS, Chrome).toArray(String[]::new), List.of(mainMenu.get(users.get(userId))).toArray(String[]::new));
            text = chooseSystem.get(users.get(userId));
        } else if (theme.equals(themes.get(1))) {
            replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(Android, Chrome).toArray(String[]::new), List.of(mainMenu.get(users.get(userId))).toArray(String[]::new));
            text = chooseSystem.get(users.get(userId));
        } else {
            replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(Android + "/" + iOS, grammar.get(users.get(userId))).toArray(String[]::new), List.of(mainMenu.get(users.get(userId))).toArray(String[]::new));
            text = emister.get(users.get(userId));
        }
        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(update.message().chat().id(), text)
                .replyMarkup(replyKeyboardMarkup);
        bot.execute(request);
    }

    private void getQuestion(String theme, Update update, long userId) {
        currentTheme.put(userId, theme);
        long chatId = update.message().chat().id();
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

        Map<Integer, String> res = database.getQuestionsByTheme(theme, userId);
        res.entrySet().stream()
                .map(e -> new InlineKeyboardButton(e.getValue())
                        .callbackData(e.getKey().toString().toString()))
                .forEach(inlineKeyboard::addRow);
        inlineKeyboard.addRow(new InlineKeyboardButton(noRes.get(users.get(userId))).callbackData(String.valueOf(99999)));
        SendMessage request = new SendMessage(chatId, chooseQuestion.get(users.get(userId)))
                .replyMarkup(inlineKeyboard);
        bot.execute(request.parseMode(ParseMode.MarkdownV2));
    }

    public void employeesMenu(Update update, long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup;
        replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(video.get(users.get(userId)), presentation.get(users.get(userId))).toArray(String[]::new),
                List.of(getKey.get(users.get(userId)), news.get(users.get(userId)), links.get(users.get(userId))).toArray(String[]::new), List.of(mainMenu.get(users.get(userId))).toArray(String[]::new));
        replyKeyboardMarkup.resizeKeyboard(true);
        SendMessage request = new SendMessage(update.message().chat().id(), employees.get(users.get(userId)))
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
        if (!addPhotoToEntity(message, entityModel)) return;
        entityModels.add(entityModel);
        if (!languagesToAdd.isEmpty()) {
            SendMessage request = new SendMessage(message.chat().id(), "Choose language: ");
            request.replyMarkup(new InlineKeyboardMarkup(
                    languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                            .callbackData(e + "_toAdd")).toArray(InlineKeyboardButton[]::new)
            ));
            bot.execute(request);
        } else {
            if (addNew) {
                try {
                    timerToNew(message.chat().id(), message.from().id());
                } catch (IndexOutOfBoundsException e) {
                    bot.execute(new SendMessage(message.chat().id(), "Неверно указан формат даты: \" ^2023-10-01 15:30:00\" \nПробел перед '^' важен\nНачните заново"));
                    admin(message.chat().id());
                }
                addNew = false;
            } else {
                database.saveAnswer();
            }
            bot.execute(new SendMessage(message.chat().id(), "Adding completed"));
            addQuestion = false;
        }
    }

    public void editQuestionAnswer(Message message, EntityModel entityModel) {
        if (addPhotoToEntity(message, entityModel)) {
            database.editAnswerQuestion(currentIdToEdit, entityModel);
            bot.execute(new SendMessage(message.chat().id(), "Edit completed"));
            currentIdToEdit = -1;
        }
    }

    private void addPresentation(Message message) {
        if (addPresentation) {
            if (message.document() != null) {
                photos.clear();
                if (!message.document().fileName().contains(".pdf")) {
                    bot.execute(new SendMessage(message.chat().id(), "Неверный формат файла, нужен .pdf"));
                    admin(message.chat().id());
                    return;
                }
                getImage(message.document().fileId());
                database.addOrUpdatePresentation(currentLanguageToAdd + " " + currentThemeToAdd, message.caption(), photos.get(0));
                updateLanguagesToAdd(message);
            } else {
                bot.execute(new SendMessage(message.chat().id(), "Файл не отправлен"));
                admin(message.chat().id());
            }
        }
        if (languagesToAdd.isEmpty()) {
            bot.execute(new SendMessage(message.chat().id(), "Презентации добавлены"));
            addPresentation = false;
        }
    }

    private void addVideo(Message message) {
        if (addVideo) {
            database.addOrUpdateVideos(currentLanguageToAdd + " " + currentThemeToAdd, message.text());
            updateLanguagesToAdd(message);
        }
        if (languagesToAdd.isEmpty()) {
            bot.execute(new SendMessage(message.chat().id(), "Видео добавлены"));
            addVideo = false;
        }
    }

    private void updateLanguagesToAdd(Message message) {
        if (!languagesToAdd.isEmpty()) {
            SendMessage request = new SendMessage(message.chat().id(), "Choose language: ");
            request.replyMarkup(new InlineKeyboardMarkup(
                    languagesToAdd.stream().map(e -> new InlineKeyboardButton(e)
                            .callbackData(e + "_toAdd")).toArray(InlineKeyboardButton[]::new)
            ));
            bot.execute(request);
        }
    }

    public boolean addPhotoToEntity(Message message, EntityModel entityModel) {
        try {
            if (entityModel.getAnswer().contains("<video\\>") || entityModel.getAnswer().contains("<photo\\>")) {
                if (message.photo() != null) {
                    if (message.photo().length == 4) getImage(message.photo()[3].fileId());
                    else getImage(message.photo()[message.photo().length - 1].fileId());
                } else if (message.video() != null) {
                    getImage(message.video().fileId());
                } else {
                    bot.execute(new SendMessage(message.chat().id(), "Фото или видео не было отправлено, начните заново"));
                    admin(message.chat().id());
                    return false;
                }
            }
        }
        catch (Exception e) {
            bot.execute(new SendMessage(message.chat().id(), "Неверный формат фото, начните заново"));
            admin(message.chat().id());
            return false;
        }
        entityModel.setPhotos(new ArrayList<>(photos));
        photos.clear();
        return true;
    }

    public void addUserToPool(long userId) {
        if (!users.containsKey(userId)) {
            database.setLanguageToUser("Ru \uD83D\uDD4A", userId);
            users.put(userId, "Ru \uD83D\uDD4A");
        }
    }

    public void updateLanguageToUser(String language, long userId) {
        database.setLanguageToUser(language, userId);
        users.put(userId, language);
    }

    private void sendNew(long newId) {
        List<Long> users = database.getUsers();
        for (long u : users) {
            sendNews(u, database.getNew(u, newId));
        }
    }

    private void timerToNew(long chatId, long userId) {
        long time = 0;
        for (EntityModel e : entityModels) {
            String datetime = e.getQuestion().split(" \\^")[1].replaceAll("\\\\", "");
            time = seconds(datetime);
            if (time < 0) {
                bot.execute(new SendMessage(chatId,  String.format("Неверный формат даты-времени: %s, начните заново", datetime)));
                admin(chatId);
                return;
            }
        }
        long newId = database.addNewToPool();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            sendNew(newId);
            scheduler.shutdown();
        };
        scheduler.schedule(task, time, TimeUnit.SECONDS);
    }

    private long seconds(String datetime) {
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
        if (!datetime.matches(regex)) {
            return -1;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(datetime, formatter);
            ZonedDateTime zonedDateTimeInput = dateTime.atZone(ZoneId.of("Europe/Moscow"));
            ZonedDateTime now = ZonedDateTime.now();
            Duration duration = Duration.between(now, zonedDateTimeInput);
            return duration.getSeconds() < 0 ? 0 : duration.getSeconds();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }


    public void messageToAdd(long chatId) {
        String messageText = addNew ?
                String.format(
                        """
                                Enter the new in %s language:
                                'theme' ^2023-10-01 15:30:00 (date&time : гггг-мм-дд чч:мм:сс) - (only first paragraph)
                                <photo> 'new text' - if new have photo (photo.jpg)
                                <video> 'new text' - if new have video
                                'new text' - if new dont have media materials
                                photo/video materials
                                """
                , currentLanguageToAdd) :
                String.format(
                        """
                                Enter the question - answer in %s language:
                                'question text' - (only first paragraph)
                                <photo> 'answer text' - if answer have photo (photo.jpg)
                                <video> 'answer text' - if answer have video
                                'answer text' - if answer dont have media materials
                                photo/video materials
                                """
                        , currentLanguageToAdd)
                ;

        SendMessage editMessageText = new SendMessage(chatId, messageText);
        bot.execute(editMessageText);    }


    public static final Pattern MDV2_SPECIAL =
            Pattern.compile("([\\[\\]\\(\\)'>\\#\\+\\-\\=\\|\\{\\}\\.\\!:/])");

    public static String withFormatted(MessageEntity[] entities, String messageText) {
        if (messageText == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(messageText);

        if (entities != null && entities.length > 0) {
            Arrays.sort(entities, Comparator.comparingInt(MessageEntity::offset).reversed());

            for (MessageEntity e : entities) {
                int off = e.offset();
                int len = e.length();
                String fmt = getCharFromFormat(e.type().name());

                if (e.type() == MessageEntity.Type.text_link) {
                    String url = e.url();
                    String linkText = sb.substring(off, off + len);
                    String escText = escapeMarkdownV2(linkText);
                    sb.delete(off, off + len);
                    String mdLink = "[" + escText + "](" + url + ")";
                    sb.insert(off, mdLink);
                    len = mdLink.length();
                } else if (e.type() == MessageEntity.Type.url) {
                    String url = sb.substring(off, off + len);
                    sb.delete(off, off + len);
                    sb.insert(off, url);
                    len = url.length();
                }

                if (!fmt.isEmpty()) {
                    sb.insert(off + len, fmt);
                    sb.insert(off, fmt);
                }
            }
        }
        return escapeMarkdownV2(sb.toString());
    }

    public static String getCharFromFormat(String format) {
        return switch (format) {
            case "bold"          -> "*";
            case "italic"        -> "_";
            case "underline"     -> "__";
            case "strikethrough" -> "~";
            default               -> "";
        };
    }

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[[^\\]]+\\]\\([^\\)]+\\)");

    public static String escapeMarkdownV2(String text) {
        if (text == null || text.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        Matcher m = LINK_PATTERN.matcher(text);
        int last = 0;

        while (m.find()) {
            sb.append(MDV2_SPECIAL.matcher(text.substring(last, m.start())).replaceAll("\\\\$1"));
            sb.append(m.group());
            last = m.end();
        }
        sb.append(MDV2_SPECIAL.matcher(text.substring(last)).replaceAll("\\\\$1"));

        return sb.toString();
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

