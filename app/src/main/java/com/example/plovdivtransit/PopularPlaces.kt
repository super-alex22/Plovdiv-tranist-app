package com.example.plovdivtransit

data class PopularPlace(
    val name: String,
    val lat: Double,
    val lon: Double,
    val category: String,
    val keywords: List<String>
)

object PopularPlaces {
    val items = listOf(
        // --- TRANSPORT ---
        PopularPlace(
            "Централна железопътна гара Пловдив",
            42.1333,
            24.7405,
            "Transport",
            listOf("Централна гара", "Central Railway Station", "Gara", "Train", "влак", "Вокзал")
        ),
        PopularPlace(
            "Автогара Юг",
            42.1335,
            24.7431,
            "Transport",
            listOf("South Bus Station", "Avtogara Yug", "bus", "автобус", "гара юг")
        ),
        PopularPlace(
            "Автогара Родопи",
            42.1308,
            24.7419,
            "Transport",
            listOf("Rodopi Bus Station", "автобус", "bus", "гара родопи")
        ),
        PopularPlace(
            "Автогара Север",
            42.1643,
            24.7397,
            "Transport",
            listOf("Sever Bus Station", "автобус", "bus", "гара север", "Филипово")
        ),
        PopularPlace(
            "Железопътна гара Филипово",
            42.1691,
            24.7410,
            "Transport",
            listOf("Gara Filipovo", "Filipovo Station", "влак", "train")
        ),
        PopularPlace(
            "Железопътна гара Тракия",
            42.1264,
            24.7869,
            "Transport",
            listOf("Gara Trakia", "Trakia Station", "влак", "train")
        ),
        PopularPlace(
            "Летище Пловдив",
            42.0677,
            24.8510,
            "Transport",
            listOf("Plovdiv Airport", "Крумово", "Krumovo", "летище")
        ),

        // --- SHOPPING ---
        PopularPlace(
            "Mall Plovdiv",
            42.1415,
            24.7214,
            "Shopping",
            listOf("Мол Пловдив", "Mall", "West", "шопинг")
        ),
        PopularPlace(
            "Plovdiv Plaza",
            42.1408,
            24.7836,
            "Shopping",
            listOf("Пловдив Плаза", "Plaza Mall", "Тракия")
        ),
        PopularPlace(
            "Mall Markovo Tepe",
            42.1412,
            24.7423,
            "Shopping",
            listOf("Марково тепе", "Markovo Tepe", "мол", "център")
        ),
        PopularPlace(
            "Гранд Търговски Център",
            42.1401,
            24.7533,
            "Shopping",
            listOf("Гранд", "Grand Shopping Center", "Капитан Райчо")
        ),
        PopularPlace(
            "Форум Тракия",
            42.1351,
            24.7885,
            "Shopping",
            listOf("Forum Trakia", "център Тракия")
        ),

        PopularPlace(
            "Супермаркет Лекси (Гигант)",
            42.1565,
            24.7365,
            "Shopping",
            listOf("Лекси Кършияка", "Lexi Karshiyaka", "Gigant", "Гигант", "Lexi", "supermarket")
        ),
        PopularPlace(
            "Супермаркет Лекси (Марица)",
            42.1534,
            24.7335,
            "Shopping",
            listOf("Лекси Марица", "Lexi Maritsa", "УХТ", "Lexi", "supermarket")
        ),
        PopularPlace(
            "Супермаркет Лекси (Тракия)",
            42.1311,
            24.7648,
            "Shopping",
            listOf(
                "Лекси Тракия",
                "Lexi Trakia",
                "Retail Park",
                "Ритейл парк",
                "Lexi",
                "supermarket"
            )
        ),

        PopularPlace(
            "Kaufland - Тракия",
            42.1305,
            24.7875,
            "Shopping",
            listOf("Кауфланд", "Kaufland Trakia")
        ),
        PopularPlace(
            "Kaufland - Кючук Париж",
            42.1188,
            24.7435,
            "Shopping",
            listOf("Кауфланд Южен", "Kaufland Kyuchuk")
        ),
        PopularPlace(
            "Kaufland - Кършияка",
            42.1555,
            24.7335,
            "Shopping",
            listOf("Кауфланд", "Kaufland Karshiyaka")
        ),
        PopularPlace(
            "Kaufland - Брезовско шосе",
            42.1645,
            24.7455,
            "Shopping",
            listOf("Кауфланд Център", "Kaufland Brezovsko")
        ),
        PopularPlace(
            "Kaufland - Смирненски",
            42.1265,
            24.7115,
            "Shopping",
            listOf("Кауфланд Смирненски", "Kaufland Smirnenski")
        ),

        PopularPlace("Lidl - Тракия", 42.1325, 24.7845, "Shopping", listOf("Лидл", "Lidl")),
        PopularPlace("Lidl - Кършияка", 42.1615, 24.7415, "Shopping", listOf("Лидл", "Lidl")),
        PopularPlace("Lidl - Център", 42.1408, 24.7578, "Shopping", listOf("Лидл", "Lidl")),
        PopularPlace("Lidl - Смирненски", 42.1345, 24.7155, "Shopping", listOf("Лидл", "Lidl")),
        PopularPlace("Lidl - Южен", 42.1155, 24.7485, "Shopping", listOf("Лидл", "Lidl")),

        PopularPlace("Metro 1 (Тракия)", 42.1385, 24.7995, "Shopping", listOf("Метро", "Metro")),
        PopularPlace("Metro 2 (Марица)", 42.1525, 24.7115, "Shopping", listOf("Метро", "Metro")),
        PopularPlace(
            "Technopolis Тракия",
            42.1395,
            24.7975,
            "Shopping",
            listOf("Технополис", "Technopolis")
        ),
        PopularPlace("Praktiker", 42.1535, 24.7135, "Shopping", listOf("Практикер", "Praktiker")),
        PopularPlace(
            "Mr. Bricolage",
            42.1315,
            24.7715,
            "Shopping",
            listOf("Бриколаж", "Bricolage")
        ),

        PopularPlace(
            "Събота пазар",
            42.1307,
            24.7486,
            "Shopping",
            listOf("Saturday Market", "Южен", "пазар")
        ),
        PopularPlace(
            "Понеделник пазар",
            42.1455,
            24.7555,
            "Shopping",
            listOf("Monday Market", "пазар")
        ),
        PopularPlace(
            "Четвъртък пазар",
            42.1485,
            24.7435,
            "Shopping",
            listOf("Thursday Market", "пазар")
        ),

        // --- EDUCATION ---
        PopularPlace(
            "Пловдивски университет „Паисий Хилендарски“ - Ректорат",
            42.1384,
            24.7485,
            "Education",
            listOf("ПУ", "Пловдивски университет", "Plovdiv University", "университет")
        ),
        PopularPlace(
            "Пловдивски университет „Паисий Хилендарски“ - Нова сграда",
            42.1634,
            24.7259,
            "Education",
            listOf("ПУ Нова сграда", "PUMBAS", "университет")
        ),
        PopularPlace(
            "Медицински университет - Пловдив",
            42.1365,
            24.7335,
            "Education",
            listOf("МУ", "Медицински университет", "ВМИ", "Medical University", "университет")
        ),
        PopularPlace(
            "Технически университет - Филиал Пловдив",
            42.1368,
            24.7505,
            "Education",
            listOf("ТУ", "Технически университет", "Technical University", "университет")
        ),
        PopularPlace(
            "Аграрен университет - Пловдив",
            42.1396,
            24.7661,
            "Education",
            listOf("АУ", "Аграрен университет", "ВСИ", "Agricultural University", "университет")
        ),
        PopularPlace(
            "Университет по хранителни технологии",
            42.1524,
            24.7351,
            "Education",
            listOf("УХТ", "ВИХВП", "Food Tech University", "университет")
        ),
        PopularPlace(
            "АМТИИ „Проф. Асен Диамандиев“",
            42.1475,
            24.7525,
            "Education",
            listOf("АМТИИ", "Музикална академия", "университет")
        ),

        PopularPlace(
            "МГ „Академик Кирил Попов“",
            42.1325,
            24.7265,
            "Education",
            listOf("Математическа гимназия", "МГ", "MG Plovdiv", "училище")
        ),
        PopularPlace(
            "ЕГ „Пловдив“",
            42.1665,
            24.7295,
            "Education",
            listOf("Английската гимназия", "English High School", "АГ", "училище")
        ),
        PopularPlace(
            "ЕГ „Иван Вазов“",
            42.1655,
            24.7285,
            "Education",
            listOf("Руската гимназия", "Russian High School", "училище")
        ),
        PopularPlace(
            "Национална търговска гимназия",
            42.1415,
            24.7405,
            "Education",
            listOf("Търговската", "НТГ", "Commercial High School", "училище")
        ),
        PopularPlace(
            "ФЕГ „Антоан дьо Сент-Екзюпери“",
            42.1615,
            24.7815,
            "Education",
            listOf("Френската гимназия", "French High School", "училище")
        ),
        PopularPlace(
            "ХГ „Св. св. Кирил и Методий“",
            42.1485,
            24.7435,
            "Education",
            listOf("Хуманитарната гимназия", "ХГ", "училище")
        ),
        PopularPlace(
            "ПГЕЕ - Пловдив",
            42.1335,
            24.7245,
            "Education",
            listOf("Електротехникум", "Електрото", "ПГЕЕ", "училище")
        ),
        PopularPlace(
            "ПГМТ „Проф. Цветан Лазаров“",
            42.1325,
            24.7345,
            "Education",
            listOf("Механотехникум", "Механото", "училище")
        ),
        PopularPlace(
            "СУ „Свети Патриарх Евтимий“",
            42.1410,
            24.7490,
            "Education",
            listOf("Лиляната", "училище")
        ),

        // --- HEALTH ---
        PopularPlace(
            "УМБАЛ „Свети Георги“ - Хирургичен блок",
            42.1345,
            24.7170,
            "Health",
            listOf("Хирургиите", "болница", "hospital", "St. George", "Пещерско шосе")
        ),
        PopularPlace(
            "УМБАЛ „Свети Георги“ - База 1",
            42.1405,
            24.7315,
            "Health",
            listOf("Свети Георги", "Васил Априлов", "болница", "hospital")
        ),
        PopularPlace(
            "УМБАЛ Пловдив",
            42.1584,
            24.7340,
            "Health",
            listOf("Окръжна болница", "hospital", "болница", "Окръжна")
        ),
        PopularPlace(
            "УМБАЛ „Пълмед“",
            42.1417,
            24.7185,
            "Health",
            listOf("Пълмед", "Pulmed", "болница", "hospital")
        ),
        PopularPlace(
            "УМБАЛ „Каспела“",
            42.1353,
            24.7198,
            "Health",
            listOf("Каспела", "Kaspela", "болница", "hospital")
        ),
        PopularPlace(
            "МБАЛ „Свети Пантелеймон“",
            42.1215,
            24.7445,
            "Health",
            listOf("Втора градска болница", "болница", "hospital", "Южен")
        ),
        PopularPlace(
            "МБАЛ „Свети Мина“",
            42.1365,
            24.7465,
            "Health",
            listOf("Първа градска болница", "болница", "hospital", "Сточна гара")
        ),
        PopularPlace(
            "МБАЛ „Еврохоспитал“",
            42.1155,
            24.7435,
            "Health",
            listOf("Еврохоспитал", "Eurohospital", "болница", "hospital")
        ),
        PopularPlace(
            "СБАЛАГ „Селена“",
            42.1425,
            24.7045,
            "Health",
            listOf("Селена", "Selena", "АГ болница", "hospital")
        ),
        PopularPlace(
            "МБАЛ „Медлайн“",
            42.1415,
            24.7565,
            "Health",
            listOf("Медлайн", "Medline", "болница", "hospital")
        ),

        // --- LANDMARKS ---
        PopularPlace(
            "Гребна база Пловдив",
            42.1472,
            24.7125,
            "Landmark",
            listOf("Rowing Canal", "Гребната", "Grebna baza", "парк")
        ),
        PopularPlace(
            "Античен театър Пловдив",
            42.1469,
            24.7510,
            "Landmark",
            listOf("Ancient Theatre", "Стария град")
        ),
        PopularPlace(
            "Римски стадион",
            42.1478,
            24.7480,
            "Landmark",
            listOf("Roman Stadium", "Джумаята", "център")
        ),
        PopularPlace(
            "Паметник на Альоша",
            42.1437,
            24.7385,
            "Landmark",
            listOf("Альоша", "Alyosha", "Бунарджика")
        ),
        PopularPlace(
            "Младежки хълм",
            42.1339,
            24.7291,
            "Landmark",
            listOf("Youth Hill", "Джендем тепе", "парк")
        ),
        PopularPlace(
            "Старият град",
            42.1484,
            24.7516,
            "Landmark",
            listOf("Old Town", "Трихълмието")
        ),
        PopularPlace(
            "Капана",
            42.1495,
            24.7483,
            "Landmark",
            listOf("Kapana", "creative district", "център")
        ),
        PopularPlace(
            "Международен панаир Пловдив",
            42.1565,
            24.7489,
            "Landmark",
            listOf("Панаира", "International Fair", "Fair")
        ),
        PopularPlace(
            "Небет тепе",
            42.1505,
            24.7525,
            "Landmark",
            listOf("Nebet Tepe", "Стария град")
        ),
        PopularPlace(
            "Сахат тепе",
            42.1455,
            24.7455,
            "Landmark",
            listOf("Sahat Tepe", "Данов хълм", "Часовникова кула")
        ),
        PopularPlace(
            "Пеещи фонтани",
            42.1415,
            24.7455,
            "Landmark",
            listOf("Singing Fountains", "Цар Симеонова градина")
        ),

        // --- SPORT & CULTURE ---
        PopularPlace(
            "Многофункционална спортна зала „Колодрума“",
            42.1331,
            24.7602,
            "Sport",
            listOf("Колодрума", "Kolodruma Arena", "зала")
        ),
        PopularPlace(
            "Стадион „Христо Ботев“",
            42.1422,
            24.7675,
            "Sport",
            listOf("Ботев", "Колежа", "стадион")
        ),
        PopularPlace(
            "Стадион „Локомотив“",
            42.1350,
            24.7711,
            "Sport",
            listOf("Локомотив", "Лаута", "стадион")
        ),
        PopularPlace(
            "Стадион „Пловдив“",
            42.1485,
            24.7205,
            "Sport",
            listOf("Стадион Пловдив", "Големия стадион")
        ),
        PopularPlace(
            "Спортен комплекс „Сила“",
            42.1415,
            24.7375,
            "Sport",
            listOf("Сила", "Sila", "комплекс")
        ),
        PopularPlace(
            "Дом на културата „Борис Христов“",
            42.1415,
            24.7455,
            "Culture",
            listOf("Борис Христов", "Синдикален дом")
        ),
        PopularPlace(
            "Народна библиотека „Иван Вазов“",
            42.1395,
            24.7485,
            "Culture",
            listOf("Библиотека", "Иван Вазов", "Library")
        ),

        // --- ADMIN ---
        PopularPlace(
            "Община Пловдив",
            42.1432,
            24.7496,
            "Admin",
            listOf("Общината", "Кметство", "Копчетата")
        ),
        PopularPlace(
            "Централна поща Пловдив",
            42.1425,
            24.7505,
            "Admin",
            listOf("Пощата", "Post Office")
        ),
        PopularPlace("НАП Пловдив", 42.1285, 24.7445, "Admin", listOf("НАП", "Данъчното", "NAP")),
        PopularPlace("КАТ Пловдив", 42.1725, 24.7525, "Admin", listOf("КАТ", "Регистрация")),
        PopularPlace("Първо РПУ", 42.1285, 24.7485, "Admin", listOf("Полиция", "Police")),
        PopularPlace("Второ РПУ", 42.1435, 24.7325, "Admin", listOf("Полиция", "Police")),
        PopularPlace("Четвърто РПУ", 42.1455, 24.7585, "Admin", listOf("Полиция", "Police")),

        // --- DISTRICTS ---
        PopularPlace(
            "Площад Централен",
            42.1420,
            24.7500,
            "District",
            listOf("Център", "Center", "Площад")
        ),
        PopularPlace(
            "Сточна гара",
            42.1378,
            24.7523,
            "District",
            listOf("Stochna Gara", "кръстовище")
        ),
        PopularPlace(
            "Район Южен (Кючук Париж)",
            42.1265,
            24.7443,
            "District",
            listOf("Кючук Париж", "Южен", "Kyuchuk")
        ),
        PopularPlace("Район Тракия", 42.1351, 24.7885, "District", listOf("Тракия", "Trakia")),
        PopularPlace(
            "Район Западен (Христо Смирненски)",
            42.1375,
            24.7125,
            "District",
            listOf("Смирненски", "Smirnenski", "Западен")
        ),
        PopularPlace(
            "Район Северен (Кършияка)",
            42.1565,
            24.7415,
            "District",
            listOf("Кършияка", "Karshiyaka", "Северен")
        ),
        PopularPlace("Район Изгрев", 42.1515, 24.7855, "District", listOf("Изгрев", "Izgrev")),
        PopularPlace(
            "Квартал Каменица",
            42.1415,
            24.7615,
            "District",
            listOf("Каменица", "Kamenitza")
        ),
        PopularPlace("Квартал Прослав", 42.1373, 24.6853, "District", listOf("Прослав", "Proslav")),
        PopularPlace(
            "Квартал Коматево",
            42.1037,
            24.7143,
            "District",
            listOf("Коматево", "Komatevo")
        ),
        PopularPlace(
            "Чифте баня",
            42.1517,
            24.7488,
            "District",
            listOf("Chifte Banya", "Баня Старинна")
        )
    )
}