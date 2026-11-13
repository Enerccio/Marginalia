package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Google Gemini provider in the languages besides English; the English text is the key.
 */
public class GoogleGenAiLocalization extends PluginLocalizationBundle {

    public static final GoogleGenAiLocalization T = new GoogleGenAiLocalization();

    public static final String URL = "URL";
    public static final String NO_MODEL_IS_SET = "No model is set";
    public static final String NO_API_KEY_IS_SET = "No API key is set";
    public static final String THE_MODEL_GAVE_NO_ANSWER = "The model gave no answer (%s)";
    public static final String THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE = "The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort";
    public static final String THE_MODEL_GAVE_NO_ANSWER_S_S = "The model gave no answer (%s): %s";
    public static final String THE_KEY_OF_GOOGLE_AI = "The key of Google AI Studio (aistudio.google.com/apikey)";
    public static final String OPTIONAL_EMPTY_USES_GOOGLES = "Optional. Empty uses Google's API, set it to go through a proxy.";
    public static final String TURN_OFF_THE_SAFETY_FILTERS = "Turn off the safety filters";
    public static final String SENDS_BLOCK_NONE = "Sends BLOCK_NONE for the adjustable categories (harassment, hate speech, sexually explicit, dangerous content). Google still blocks some content whatever is sent.";

    @Override
    protected void loadKeys() {
        ja(URL, "URL");
        zh(URL, "URL");
        fr(URL, "URL");
        es(URL, "URL");
        de(URL, "URL");
        ko(URL, "URL");
        it(URL, "URL");

        ja(NO_MODEL_IS_SET, "モデルが設定されていません");
        zh(NO_MODEL_IS_SET, "未设置模型");
        fr(NO_MODEL_IS_SET, "Aucun modèle n'est défini");
        es(NO_MODEL_IS_SET, "No se ha definido ningún modelo");
        de(NO_MODEL_IS_SET, "Es ist kein Modell festgelegt");
        ko(NO_MODEL_IS_SET, "모델이 설정되어 있지 않습니다");
        it(NO_MODEL_IS_SET, "Nessun modello impostato");

        ja(NO_API_KEY_IS_SET, "APIキーが設定されていません");
        zh(NO_API_KEY_IS_SET, "未设置 API 密钥");
        fr(NO_API_KEY_IS_SET, "Aucune clé d'API n'est définie");
        es(NO_API_KEY_IS_SET, "No se ha definido ninguna clave de API");
        de(NO_API_KEY_IS_SET, "Es ist kein API-Schlüssel festgelegt");
        ko(NO_API_KEY_IS_SET, "API 키가 설정되어 있지 않습니다");
        it(NO_API_KEY_IS_SET, "Nessuna chiave API impostata");

        ja(THE_MODEL_GAVE_NO_ANSWER, "モデルから回答がありませんでした（%s）");
        zh(THE_MODEL_GAVE_NO_ANSWER, "模型没有给出回答（%s）");
        fr(THE_MODEL_GAVE_NO_ANSWER, "Le modèle n'a pas répondu (%s)");
        es(THE_MODEL_GAVE_NO_ANSWER, "El modelo no ha dado ninguna respuesta (%s)");
        de(THE_MODEL_GAVE_NO_ANSWER, "Das Modell hat nicht geantwortet (%s)");
        ko(THE_MODEL_GAVE_NO_ANSWER, "모델이 응답하지 않았습니다 (%s)");
        it(THE_MODEL_GAVE_NO_ANSWER, "Il modello non ha dato risposta (%s)");

        ja(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "モデルから回答がありませんでした（%s）: 最初の単語の前に応答サイズを使い切りました。応答サイズを増やすか、推論の強度を下げてください");
        zh(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "模型没有给出回答（%s）：在输出第一个词之前，回复长度已用完，请调高回复长度或降低推理强度");
        fr(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "Le modèle n'a pas répondu (%s) : la taille de réponse a été épuisée avant le premier mot, augmentez-la ou réduisez l'effort de raisonnement");
        es(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "El modelo no ha dado ninguna respuesta (%s): el tamaño de la respuesta se agotó antes de la primera palabra, auméntalo o reduce el esfuerzo de razonamiento");
        de(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "Das Modell hat nicht geantwortet (%s): Die Antwortgröße war aufgebraucht, bevor das erste Wort kam, erhöhe sie oder verringere den Reasoning-Aufwand");
        ko(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "모델이 응답하지 않았습니다 (%s): 첫 단어가 나오기 전에 응답 크기를 모두 사용했습니다. 크기를 늘리거나 추론 강도를 낮추세요");
        it(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "Il modello non ha dato risposta (%s): la dimensione della risposta si è esaurita prima della prima parola, aumentala o riduci l'impegno di ragionamento");

        ja(THE_MODEL_GAVE_NO_ANSWER_S_S, "モデルから回答がありませんでした（%s）: %s");
        zh(THE_MODEL_GAVE_NO_ANSWER_S_S, "模型没有给出回答（%s）：%s");
        fr(THE_MODEL_GAVE_NO_ANSWER_S_S, "Le modèle n'a pas répondu (%s) : %s");
        es(THE_MODEL_GAVE_NO_ANSWER_S_S, "El modelo no ha dado ninguna respuesta (%s): %s");
        de(THE_MODEL_GAVE_NO_ANSWER_S_S, "Das Modell hat nicht geantwortet (%s): %s");
        ko(THE_MODEL_GAVE_NO_ANSWER_S_S, "모델이 응답하지 않았습니다 (%s): %s");
        it(THE_MODEL_GAVE_NO_ANSWER_S_S, "Il modello non ha dato risposta (%s): %s");

        ja(THE_KEY_OF_GOOGLE_AI, "Google AI Studioのキー（aistudio.google.com/apikey）");
        zh(THE_KEY_OF_GOOGLE_AI, "Google AI Studio 的密钥（aistudio.google.com/apikey）");
        fr(THE_KEY_OF_GOOGLE_AI, "La clé de Google AI Studio (aistudio.google.com/apikey)");
        es(THE_KEY_OF_GOOGLE_AI, "La clave de Google AI Studio (aistudio.google.com/apikey)");
        de(THE_KEY_OF_GOOGLE_AI, "Der Schlüssel von Google AI Studio (aistudio.google.com/apikey)");
        ko(THE_KEY_OF_GOOGLE_AI, "Google AI Studio의 키 (aistudio.google.com/apikey)");
        it(THE_KEY_OF_GOOGLE_AI, "La chiave di Google AI Studio (aistudio.google.com/apikey)");

        ja(OPTIONAL_EMPTY_USES_GOOGLES, "任意。空欄の場合はGoogleのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh(OPTIONAL_EMPTY_USES_GOOGLES, "可选。留空则使用 Google 的 API，设置后可通过代理访问。");
        fr(OPTIONAL_EMPTY_USES_GOOGLES, "Facultatif. Vide : l'API de Google ; définissez-la pour passer par un proxy.");
        es(OPTIONAL_EMPTY_USES_GOOGLES, "Opcional. Vacío usa la API de Google; indícalo para pasar por un proxy.");
        de(OPTIONAL_EMPTY_USES_GOOGLES, "Optional. Leer verwendet die API von Google, setze es, um über einen Proxy zu gehen.");
        ko(OPTIONAL_EMPTY_USES_GOOGLES, "선택 사항. 비워 두면 Google의 API를 사용하며, 프록시를 거치려면 설정하세요.");
        it(OPTIONAL_EMPTY_USES_GOOGLES, "Facoltativo. Se vuoto usa l'API di Google, impostalo per passare da un proxy.");

        ja(TURN_OFF_THE_SAFETY_FILTERS, "安全フィルターをオフにする");
        zh(TURN_OFF_THE_SAFETY_FILTERS, "关闭安全过滤器");
        fr(TURN_OFF_THE_SAFETY_FILTERS, "Désactiver les filtres de sécurité");
        es(TURN_OFF_THE_SAFETY_FILTERS, "Desactivar los filtros de seguridad");
        de(TURN_OFF_THE_SAFETY_FILTERS, "Sicherheitsfilter ausschalten");
        ko(TURN_OFF_THE_SAFETY_FILTERS, "안전 필터 끄기");
        it(TURN_OFF_THE_SAFETY_FILTERS, "Disattiva i filtri di sicurezza");

        ja(SENDS_BLOCK_NONE, "調整可能なカテゴリ（ハラスメント、ヘイトスピーチ、性的に露骨な内容、危険なコンテンツ）にBLOCK_NONEを送信します。送信内容に関わらず、Googleは一部のコンテンツを引き続きブロックします。");
        zh(SENDS_BLOCK_NONE, "对可调整的类别（骚扰、仇恨言论、色情内容、危险内容）发送 BLOCK_NONE。无论发送什么，Google 仍会拦截部分内容。");
        fr(SENDS_BLOCK_NONE, "Envoie BLOCK_NONE pour les catégories réglables (harcèlement, discours haineux, contenu sexuellement explicite, contenu dangereux). Google bloque quand même certains contenus quoi qu'on envoie.");
        es(SENDS_BLOCK_NONE, "Envía BLOCK_NONE para las categorías ajustables (acoso, discurso de odio, contenido sexual explícito, contenido peligroso). Google sigue bloqueando cierto contenido se envíe lo que se envíe.");
        de(SENDS_BLOCK_NONE, "Sendet BLOCK_NONE für die einstellbaren Kategorien (Belästigung, Hassrede, sexuell explizit, gefährliche Inhalte). Google blockiert weiterhin einige Inhalte, unabhängig davon, was gesendet wird.");
        ko(SENDS_BLOCK_NONE, "조절 가능한 범주(괴롭힘, 혐오 표현, 선정적 콘텐츠, 위험한 콘텐츠)에 BLOCK_NONE을 전송합니다. 전송한 값과 관계없이 Google은 일부 콘텐츠를 계속 차단합니다.");
        it(SENDS_BLOCK_NONE, "Invia BLOCK_NONE per le categorie regolabili (molestie, incitamento all'odio, contenuti sessualmente espliciti, contenuti pericolosi). Google blocca comunque alcuni contenuti qualunque cosa venga inviata.");
    }
}
