package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Name Provider in the languages besides English; the English text is the key.
 */
public class NameProviderLocalization extends PluginLocalizationBundle {

    public static final NameProviderLocalization T = new NameProviderLocalization();

    public static final String REAL_NAMES_THE_MOST_COMMON = "Real names: the most common forenames and surnames of about 100 countries, with the original spelling. Groups (like the Nordic countries) pick a list at random for every name.";

    @Override
    protected void loadKeys() {
        ja(REAL_NAMES_THE_MOST_COMMON, "実在する名前: 約100か国で最も一般的な名と姓を、元の綴りとともに収録しています。グループ（北欧諸国など）は、名前ごとにリストをランダムに選びます。");
        zh(REAL_NAMES_THE_MOST_COMMON, "真实姓名：约 100 个国家最常见的名和姓，附原文拼写。分组（如北欧国家）会为每个名字随机选择一个列表。");
        fr(REAL_NAMES_THE_MOST_COMMON, "Noms réels : les prénoms et noms de famille les plus courants d'environ 100 pays, dans leur graphie d'origine. Les groupes (comme les pays nordiques) choisissent une liste au hasard pour chaque nom.");
        es(REAL_NAMES_THE_MOST_COMMON, "Nombres reales: los nombres de pila y apellidos más comunes de unos 100 países, con la ortografía original. Los grupos (como los países nórdicos) eligen una lista al azar para cada nombre.");
        de(REAL_NAMES_THE_MOST_COMMON, "Echte Namen: die häufigsten Vor- und Nachnamen von etwa 100 Ländern, in der Originalschreibweise. Gruppen (wie die nordischen Länder) wählen für jeden Namen zufällig eine Liste.");
        ko(REAL_NAMES_THE_MOST_COMMON, "실제 이름: 약 100개국의 가장 흔한 이름과 성을 원래 철자로. 그룹(예: 북유럽 국가)은 이름마다 무작위로 목록을 선택합니다.");
        it(REAL_NAMES_THE_MOST_COMMON, "Nomi reali: i nomi e i cognomi più comuni di circa 100 paesi, con l'ortografia originale. I gruppi (come i paesi nordici) scelgono un elenco a caso per ogni nome.");
    }
}
