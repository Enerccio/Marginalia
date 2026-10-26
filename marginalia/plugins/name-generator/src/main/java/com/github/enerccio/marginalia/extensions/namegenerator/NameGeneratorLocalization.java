package com.github.enerccio.marginalia.extensions.namegenerator;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Name Generator in the languages besides English; the English text is the key.
 */
public class NameGeneratorLocalization extends PluginLocalizationBundle {

    public static final NameGeneratorLocalization T = new NameGeneratorLocalization();

    @Override
    protected void loadKeys() {
        ja("Names made up by Markov chains trained on lists of names: places, deities, companies, surnames of countries... Groups (like all Native American names) pick a category at random for every name.", "名前リストで学習したマルコフ連鎖で作られた名前: 地名、神々、企業、各国の姓など。グループ（アメリカ先住民の名前全般など）は、名前ごとにカテゴリをランダムに選びます。");
        zh("Names made up by Markov chains trained on lists of names: places, deities, companies, surnames of countries... Groups (like all Native American names) pick a category at random for every name.", "由基于名称列表训练的马尔可夫链生成的名字：地名、神祇、公司、各国姓氏等。分组（如所有美洲原住民名字）会为每个名字随机选择一个类别。");
    }
}
