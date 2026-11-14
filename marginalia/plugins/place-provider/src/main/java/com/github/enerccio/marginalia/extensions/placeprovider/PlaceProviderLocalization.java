package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Place Provider in the languages besides English; the English text is the key.
 */
public class PlaceProviderLocalization extends PluginLocalizationBundle {

    public static final PlaceProviderLocalization T = new PlaceProviderLocalization();

    public static final String REAL_PLACES_OF_THE_EARTH = "Real places of the Earth (Natural Earth, about 18 000 notable ones): search by name, type and country, what lies around a place, the distance of two places.";

    @Override
    protected void loadKeys() {
        ja(REAL_PLACES_OF_THE_EARTH, "地球上の実在の場所（Natural Earth、著名な約18,000か所）: 名前・種類・国による検索、場所の周辺にあるもの、2地点間の距離。");
        zh(REAL_PLACES_OF_THE_EARTH, "地球上的真实地点（Natural Earth，约 18 000 个著名地点）：按名称、类型和国家搜索，查看某地周边有什么，以及两地之间的距离。");
        fr(REAL_PLACES_OF_THE_EARTH, "Lieux réels de la Terre (Natural Earth, environ 18 000 lieux notables) : recherche par nom, type et pays, ce qui se trouve autour d'un lieu, la distance entre deux lieux.");
        es(REAL_PLACES_OF_THE_EARTH, "Lugares reales de la Tierra (Natural Earth, unos 18 000 destacados): búsqueda por nombre, tipo y país, qué hay alrededor de un lugar, la distancia entre dos lugares.");
        de(REAL_PLACES_OF_THE_EARTH, "Echte Orte der Erde (Natural Earth, etwa 18 000 bedeutende): Suche nach Name, Typ und Land, was um einen Ort herum liegt, die Entfernung zweier Orte.");
        ko(REAL_PLACES_OF_THE_EARTH, "지구의 실제 장소 (Natural Earth, 주목할 만한 약 18,000곳): 이름, 유형, 국가로 검색하고, 장소 주변에 무엇이 있는지, 두 장소 사이의 거리를 확인합니다.");
        it(REAL_PLACES_OF_THE_EARTH, "Luoghi reali della Terra (Natural Earth, circa 18 000 notevoli): cerca per nome, tipo e paese, cosa si trova attorno a un luogo, la distanza tra due luoghi.");
    }
}
