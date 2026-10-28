package com.github.enerccio.marginalia.extensions.placeprovider;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Place Provider in the languages besides English; the English text is the key.
 */
public class PlaceProviderLocalization extends PluginLocalizationBundle {

    public static final PlaceProviderLocalization T = new PlaceProviderLocalization();

    @Override
    protected void loadKeys() {
        ja("Real places of the Earth (Natural Earth, about 18 000 notable ones): search by name, type and country, what lies around a place, the distance of two places.", "地球上の実在の場所（Natural Earth、著名な約18,000か所）: 名前・種類・国による検索、場所の周辺にあるもの、2地点間の距離。");
        zh("Real places of the Earth (Natural Earth, about 18 000 notable ones): search by name, type and country, what lies around a place, the distance of two places.", "地球上的真实地点（Natural Earth，约 18 000 个著名地点）：按名称、类型和国家搜索，查看某地周边有什么，以及两地之间的距离。");
    }
}
