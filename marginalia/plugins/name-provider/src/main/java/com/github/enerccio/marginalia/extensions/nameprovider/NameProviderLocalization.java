package com.github.enerccio.marginalia.extensions.nameprovider;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Name Provider in the languages besides English; the English text is the key.
 */
public class NameProviderLocalization extends PluginLocalizationBundle {

    public static final NameProviderLocalization T = new NameProviderLocalization();

    @Override
    protected void loadKeys() {
        ja("Real names: the most common forenames and surnames of about 100 countries, with the original spelling. Groups (like the Nordic countries) pick a list at random for every name.", "実在する名前: 約100か国で最も一般的な名と姓を、元の綴りとともに収録しています。グループ（北欧諸国など）は、名前ごとにリストをランダムに選びます。");
        zh("Real names: the most common forenames and surnames of about 100 countries, with the original spelling. Groups (like the Nordic countries) pick a list at random for every name.", "真实姓名：约 100 个国家最常见的名和姓，附原文拼写。分组（如北欧国家）会为每个名字随机选择一个列表。");
    }
}
