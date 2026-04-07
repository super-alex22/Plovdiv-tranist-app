package com.example.plovdivtransit

// Data model representing a popular place for navigation and transit
data class PopularPlace(
    val name: String,
    val lat: Double,
    val lon: Double,
    val keywords: List<String>
)

// Singleton providing the data source for popular Plovdiv locations
object PopularPlaces {
    val items: List<PopularPlace> = listOf(
        // Mandatory Places
        PopularPlace(
            name = "Rowing Canal / Гребна база",
            lat = 42.1444,
            lon = 24.7094,
            keywords = listOf("Гребна база", "Grebna baza", "Rowing canal", "Regatta venue", "Гребной канал", "парк", "спорт")
        ),
        PopularPlace(
            name = "Mall Plovdiv",
            lat = 42.1415,
            lon = 24.7214,
            keywords = listOf("Mall Plovdiv", "Мол Пловдив", "Молл Пловдив", "шопинг", "shopping", "кино", "cinema")
        ),
        PopularPlace(
            name = "Plovdiv Plaza",
            lat = 42.1408,
            lon = 24.7836,
            keywords = listOf("Plovdiv Plaza", "Пловдив Плаза", "Плаза", "Галерия", "Galeria", "молл", "mall", "shopping")
        ),
        PopularPlace(
            name = "Markovo Tepe Mall",
            lat = 42.1412,
            lon = 24.7423,
            keywords = listOf("Markovo Tepe", "Марково тепе", "Марково тепе молл", "мол", "mall")
        ),
        PopularPlace(
            name = "Central Square / Център",
            lat = 42.1420,
            lon = 24.7500,
            keywords = listOf("Център", "Center", "Central square", "Центр", "Площад Централен", "Главна", "Glavnata", "Downtown")
        ),
        PopularPlace(
            name = "Old Town / Старият град",
            lat = 42.1484,
            lon = 24.7516,
            keywords = listOf("Старият град", "Стария град", "Old Town", "Старый город", "Архитектурен резерват", "история")
        ),
        PopularPlace(
            name = "Kapana / Капана",
            lat = 42.1495,
            lon = 24.7483,
            keywords = listOf("Капана", "Kapana", "Creative district", "барове", "bars", "ресторанти", "кафе")
        ),
        PopularPlace(
            name = "International Fair / Панаир",
            lat = 42.1565,
            lon = 24.7489,
            keywords = listOf("Панаир", "Fair", "International Fair Plovdiv", "Международен панаир", "Выставка", "Пловдивски панаир")
        ),
        PopularPlace(
            name = "Bunardzhika / Alyosha",
            lat = 42.1430,
            lon = 24.7360,
            keywords = listOf("Bunardzhika", "Бунарджика", "Alyosha", "Альоша", "Алеша", "хълм", "hill", "парк", "паметник")
        ),
        PopularPlace(
            name = "Youth Hill / Младежки хълм",
            lat = 42.1339,
            lon = 24.7291,
            keywords = listOf("Youth Hill", "Младежки хълм", "Джендем тепе", "Молодежный холм", "Детска железница", "парк")
        ),
        PopularPlace(
            name = "Hotel Maritsa",
            lat = 42.1528,
            lon = 24.7461,
            keywords = listOf("Maritsa", "Хотел Марица", "hotel Maritsa area", "Марица", "река", "river", "отель")
        ),
        PopularPlace(
            name = "South Bus Station / Автогара Юг",
            lat = 42.1335,
            lon = 24.7431,
            keywords = listOf("South bus station", "Автогара Юг", "Avtogara Yug", "Автовокзал Юг", "автобус", "bus")
        ),
        PopularPlace(
            name = "Rodopi Bus Station / Автогара Родопи",
            lat = 42.1308,
            lon = 24.7419,
            keywords = listOf("Rodopi bus station area", "Автогара Родопи", "Avtogara Rodopi", "Автовокзал Родопи", "автобус", "bus")
        ),
        PopularPlace(
            name = "Central Railway Station / Централна гара",
            lat = 42.1333,
            lon = 24.7405,
            keywords = listOf("Central Railway Station", "Централна гара", "Гара Пловдив", "Центральный вокзал", "влак", "train")
        ),
        PopularPlace(
            name = "Trimontium / Тримонциум",
            lat = 42.1414,
            lon = 24.7495,
            keywords = listOf("Trimontium", "Тримонциум", "Ramada", "Рамада", "Princess", "хотел", "отель", "hotel")
        ),
        PopularPlace(
            name = "Grand Hotel Plovdiv",
            lat = 42.1555,
            lon = 24.7441,
            keywords = listOf("Grand Hotel Plovdiv", "Гранд Хотел Пловдив", "Новотел", "Novotel", "хотел", "отель")
        ),
        PopularPlace(
            name = "SPS Hotel & Hall",
            lat = 42.1411,
            lon = 24.7820,
            keywords = listOf("SPS", "СПС", "SPS hall", "хотел СПС", "отель СПС", "ЖК Тракия")
        ),
        PopularPlace(
            name = "Hristo Botev Stadium / Стадион Христо Ботев",
            lat = 42.1422,
            lon = 24.7675,
            keywords = listOf("Hristo Botev Stadium", "Стадион Христо Ботев", "Колежа", "Kolezha", "Ботев Пловдив", "стадион", "футбол")
        ),
        PopularPlace(
            name = "Kolodruma / Колодрума",
            lat = 42.1331,
            lon = 24.7602,
            keywords = listOf("Kolodruma", "Колодрума", "Многофункционална спортна зала", "Велотрек", "зала", "концерт")
        ),
        PopularPlace(
            name = "University of Plovdiv / Пловдивски университет",
            lat = 42.1384,
            lon = 24.7485,
            keywords = listOf("University of Plovdiv", "Пловдивски университет", "ПУ", "Ректорат", "Университет", "студенти")
        ),
        PopularPlace(
            name = "Medical University / Медицински университет",
            lat = 42.1365,
            lon = 24.7335,
            keywords = listOf("Medical University Plovdiv", "Медицински университет", "ВМИ", "МУ Пловдив", "Медицинский университет", "Мед")
        ),
        PopularPlace(
            name = "PUMBAS / ПУ Нова сграда",
            lat = 42.1634,
            lon = 24.7259,
            keywords = listOf("PUMBAS", "ПУ", "Нова сграда ПУ", "Паисий Хилендарски", "Спортна зала ПУ")
        ),

        // Malls, Hospitals, Major Transport & Landmarks
        PopularPlace(
            name = "UMBAL St. George / УМБАЛ Свети Георги",
            lat = 42.1427,
            lon = 24.7176,
            keywords = listOf("UMBAL St. George", "УМБАЛ Свети Георги", "Хирургиите", "Пещерско шосе", "Hospital", "Болница", "Больница")
        ),
        PopularPlace(
            name = "UMBAL Plovdiv / УМБАЛ Пловдив (Окръжна)",
            lat = 42.1584,
            lon = 24.7340,
            keywords = listOf("UMBAL Plovdiv", "УМБАЛ Пловдив", "Окръжна болница", "Okrazhna bolnitsa", "Hospital", "Больница")
        ),
        PopularPlace(
            name = "UMBAL Kaspela / УМБАЛ Каспела",
            lat = 42.1353,
            lon = 24.7198,
            keywords = listOf("Kaspela", "Каспела", "UMBAL Kaspela", "Hospital", "Болница", "Больница")
        ),
        PopularPlace(
            name = "Pulmed Hospital / УМБАЛ Пълмед",
            lat = 42.1417,
            lon = 24.7185,
            keywords = listOf("Pulmed", "Пълмед", "Hospital", "Болница", "Поликлиника", "Больница")
        ),
        PopularPlace(
            name = "Sever Bus Station / Автогара Север",
            lat = 42.1643,
            lon = 24.7397,
            keywords = listOf("Sever Bus Station", "Автогара Север", "Avtogara Sever", "Автовокзал Север", "Филипово")
        ),
        PopularPlace(
            name = "Filipovo Railway Station / Гара Филипово",
            lat = 42.1691,
            lon = 24.7410,
            keywords = listOf("Filipovo Station", "Гара Филипово", "ЖП гара", "Вокзал Филипово", "влак", "train")
        ),
        PopularPlace(
            name = "Trakia Railway Station / Гара Тракия",
            lat = 42.1264,
            lon = 24.7869,
            keywords = listOf("Trakia Station", "Гара Тракия", "Вокзал Тракия", "влак", "train", "ЖК Тракия")
        ),
        PopularPlace(
            name = "Plovdiv Airport / Летище Пловдив",
            lat = 42.0677,
            lon = 24.8510,
            keywords = listOf("Plovdiv Airport", "Летище Пловдив", "Аерогара", "Аэропорт", "Крумово", "Krumovo", "flights")
        ),
        PopularPlace(
            name = "Roman Stadium / Римски стадион",
            lat = 42.1478,
            lon = 24.7480,
            keywords = listOf("Roman Stadium", "Римски стадион", "Джумаята", "Римский стадион", "Античен стадион", "център")
        ),
        PopularPlace(
            name = "Dzhumaya Mosque / Джумая джамия",
            lat = 42.1478,
            lon = 24.7481,
            keywords = listOf("Dzhumaya", "Джумая джамия", "Мечеть", "Mosque", "център")
        ),
        PopularPlace(
            name = "Stochna Gara Intersection / Сточна гара",
            lat = 42.1378,
            lon = 24.7523,
            keywords = listOf("Stochna Gara", "Сточна гара", "Кръстовище", "Intersection", "трафик")
        ),
        PopularPlace(
            name = "Chifte Banya / Чифте баня",
            lat = 42.1517,
            lon = 24.7488,
            keywords = listOf("Chifte Banya", "Чифте баня", "Баня Старинна", "Кръстовище Шести Септември")
        ),
        PopularPlace(
            name = "Plovdiv Municipality / Община Пловдив",
            lat = 42.1432,
            lon = 24.7496,
            keywords = listOf("Municipality", "Община Пловдив", "Кметство", "Мэрия", "Копчетата")
        ),
        PopularPlace(
            name = "Agricultural University / Аграрен университет",
            lat = 42.1396,
            lon = 24.7661,
            keywords = listOf("Agricultural University", "Аграрен университет", "ВСИ", "Аграрный университет")
        ),
        PopularPlace(
            name = "University of Food Technologies / УХТ",
            lat = 42.1524,
            lon = 24.7351,
            keywords = listOf("UFT", "УХТ", "ВИХВП", "Университет по хранителни технологии", "Пищевой университет")
        ),
        PopularPlace(
            name = "Kamenitza Park",
            lat = 42.1418,
            lon = 24.7615,
            keywords = listOf("Kamenitza", "Каменица", "Каменица парк", "Бирена фабрика", "комплекс")
        ),
        PopularPlace(
            name = "Lauta Park / Парк Лаута",
            lat = 42.1360,
            lon = 24.7758,
            keywords = listOf("Lauta", "Лаута", "Парк Лаута", "Тракия", "отдих", "природа")
        ),
        PopularPlace(
            name = "Lokomotiv Stadium / Стадион Локомотив",
            lat = 42.1350,
            lon = 24.7711,
            keywords = listOf("Lokomotiv Stadium", "Стадион Локомотив", "Лаута", "Локо Пловдив", "стадион", "футбол")
        ),
        PopularPlace(
            name = "Botev Training Complex / База Коматево",
            lat = 42.1011,
            lon = 24.7081,
            keywords = listOf("Botev Complex", "Футболен комплекс Ботев", "Коматево", "база", "футбол")
        ),
        PopularPlace(
            name = "Trakia Economic Zone / ТИЗ",
            lat = 42.1866,
            lon = 24.6469,
            keywords = listOf("Trakia Economic Zone", "ТИЗ", "Индустриална зона", "Радиново", "Марица", "Industrial")
        ),
        PopularPlace(
            name = "KCM Plovdiv / КЦМ",
            lat = 42.0835,
            lon = 24.8093,
            keywords = listOf("KCM", "КЦМ Пловдив", "Завод", "Асеновградско шосе", "промишленост")
        ),
        PopularPlace(
            name = "Customs Plovdiv / Митница Пловдив",
            lat = 42.1091,
            lon = 24.7578,
            keywords = listOf("Customs", "Митница Пловдив", "Кукленско шосе", "Таможня", "Mitnitsa")
        ),
        PopularPlace(
            name = "Saturday Market / Събота пазар",
            lat = 42.1307,
            lon = 24.7486,
            keywords = listOf("Saturday Market", "Събота пазар", "Кючук Париж", "Рынок", "пазар")
        ),
        PopularPlace(
            name = "Trakia Forum / Форум Тракия",
            lat = 42.1351,
            lon = 24.7885,
            keywords = listOf("Trakia Forum", "Форум Тракия", "Център Тракия", "ЖК Тракия", "паметник")
        ),
        PopularPlace(
            name = "Kyuchuk Paris Center / Кючук Париж (Бели Брези)",
            lat = 42.1265,
            lon = 24.7443,
            keywords = listOf("Kyuchuk Paris", "Кючук Париж", "Бели Брези", "Македония", "Въстанически")
        ),
        PopularPlace(
            name = "Karshiyaka / Кършияка (Гранд Хотел)",
            lat = 42.1561,
            lon = 24.7411,
            keywords = listOf("Karshiyaka", "Кършияка", "Северен", "Лекси", "Lexi", "Северный район")
        ),
        PopularPlace(
            name = "Proslav / Прослав",
            lat = 42.1373,
            lon = 24.6853,
            keywords = listOf("Proslav", "Прослав", "квартал", "neighborhood", "район")
        ),
        PopularPlace(
            name = "Komatevo / Коматево",
            lat = 42.1037,
            lon = 24.7143,
            keywords = listOf("Komatevo", "Коматево", "Коматевско шосе", "квартал", "район")
        ),
        PopularPlace(
            name = "Billa 6th September / Billa 6-ти Септември",
            lat = 42.1497,
            lon = 24.7570,
            keywords = listOf("Billa", "Била", "супермаркет", "supermarket", "6-ти Септември", "Шести септември")
        )
    )
}