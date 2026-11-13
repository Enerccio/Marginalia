package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Name Generator in the languages besides English; the English text is the key.
 */
public class NameGeneratorLocalization extends PluginLocalizationBundle {

    public static final NameGeneratorLocalization T = new NameGeneratorLocalization();

    public static final String NAMES_MADE_UP_BY_MARKOV = "Names made up by Markov chains trained on lists of names: places, deities, companies, surnames of countries... Groups (like all Native American names) pick a category at random for every name.";

    @Override
    protected void loadKeys() {
        ja(NAMES_MADE_UP_BY_MARKOV, "名前リストで学習したマルコフ連鎖で作られた名前: 地名、神々、企業、各国の姓など。グループ（アメリカ先住民の名前全般など）は、名前ごとにカテゴリをランダムに選びます。");
        zh(NAMES_MADE_UP_BY_MARKOV, "由基于名称列表训练的马尔可夫链生成的名字：地名、神祇、公司、各国姓氏等。分组（如所有美洲原住民名字）会为每个名字随机选择一个类别。");
        fr(NAMES_MADE_UP_BY_MARKOV, "Noms inventés par des chaînes de Markov entraînées sur des listes de noms : lieux, divinités, entreprises, noms de famille de pays... Les groupes (comme tous les noms amérindiens) choisissent une catégorie au hasard pour chaque nom.");
        es(NAMES_MADE_UP_BY_MARKOV, "Nombres inventados por cadenas de Markov entrenadas con listas de nombres: lugares, deidades, empresas, apellidos de países... Los grupos (como todos los nombres nativos americanos) eligen una categoría al azar para cada nombre.");
        de(NAMES_MADE_UP_BY_MARKOV, "Namen, die von Markov-Ketten erfunden werden, die auf Namenslisten trainiert sind: Orte, Gottheiten, Firmen, Nachnamen von Ländern ... Gruppen (wie alle indianischen Namen) wählen für jeden Namen zufällig eine Kategorie.");
        ko(NAMES_MADE_UP_BY_MARKOV, "이름 목록(장소, 신, 회사, 국가별 성씨 등)으로 학습한 마르코프 체인이 만든 이름. 그룹(예: 모든 아메리카 원주민 이름)은 이름마다 무작위로 범주를 선택합니다.");
    }
}
