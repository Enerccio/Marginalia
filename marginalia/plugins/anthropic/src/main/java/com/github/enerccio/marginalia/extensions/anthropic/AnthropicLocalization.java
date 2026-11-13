package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Anthropic provider in the languages besides English; the English text is the key.
 */
public class AnthropicLocalization extends PluginLocalizationBundle {

    public static final AnthropicLocalization T = new AnthropicLocalization();

    public static final String URL = "URL";
    public static final String NO_MODEL_IS_SET = "No model is set";
    public static final String NO_API_KEY_IS_SET = "No API key is set";
    public static final String THE_MODEL_GAVE_NO_ANSWER = "The model gave no answer (%s)";
    public static final String THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE = "The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort";
    public static final String THE_MODEL_GAVE_NO_ANSWER_S_S = "The model gave no answer (%s): %s";
    public static final String THE_KEY_OF_THE_CLAUDE = "The key of the Claude Console (platform.claude.com)";
    public static final String OPTIONAL_EMPTY_USES_ANTHROPICS = "Optional. Empty uses Anthropic's API, set it to go through a proxy.";
    public static final String THE_CONNECTION_TO_ANTHROPIC = "The connection to Anthropic ended before the answer was complete";
    public static final String THE_CONTEXT_WINDOW = "The context window of the model was used up";
    public static final String THE_PAYLOAD_HAS_TO_START = "The payload has to start and end with a message of the user, Claude does not continue an answer";

    @Override
    protected void loadKeys() {
        ja(URL, "URL");
        zh(URL, "URL");
        fr(URL, "URL");
        es(URL, "URL");
        de(URL, "URL");
        ko(URL, "URL");

        ja(NO_MODEL_IS_SET, "モデルが設定されていません");
        zh(NO_MODEL_IS_SET, "未设置模型");
        fr(NO_MODEL_IS_SET, "Aucun modèle n'est défini");
        es(NO_MODEL_IS_SET, "No se ha definido ningún modelo");
        de(NO_MODEL_IS_SET, "Es ist kein Modell festgelegt");
        ko(NO_MODEL_IS_SET, "모델이 설정되어 있지 않습니다");

        ja(NO_API_KEY_IS_SET, "APIキーが設定されていません");
        zh(NO_API_KEY_IS_SET, "未设置 API 密钥");
        fr(NO_API_KEY_IS_SET, "Aucune clé d'API n'est définie");
        es(NO_API_KEY_IS_SET, "No se ha definido ninguna clave de API");
        de(NO_API_KEY_IS_SET, "Es ist kein API-Schlüssel festgelegt");
        ko(NO_API_KEY_IS_SET, "API 키가 설정되어 있지 않습니다");

        ja(THE_MODEL_GAVE_NO_ANSWER, "モデルから回答がありませんでした（%s）");
        zh(THE_MODEL_GAVE_NO_ANSWER, "模型没有给出回答（%s）");
        fr(THE_MODEL_GAVE_NO_ANSWER, "Le modèle n'a pas répondu (%s)");
        es(THE_MODEL_GAVE_NO_ANSWER, "El modelo no ha dado ninguna respuesta (%s)");
        de(THE_MODEL_GAVE_NO_ANSWER, "Das Modell hat nicht geantwortet (%s)");
        ko(THE_MODEL_GAVE_NO_ANSWER, "모델이 응답하지 않았습니다 (%s)");

        ja(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "モデルから回答がありませんでした（%s）: 最初の単語の前に応答サイズを使い切りました。応答サイズを増やすか、推論の強度を下げてください");
        zh(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "模型没有给出回答（%s）：在输出第一个词之前，回复长度已用完，请调高回复长度或降低推理强度");
        fr(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "Le modèle n'a pas répondu (%s) : la taille de réponse a été épuisée avant le premier mot, augmentez-la ou réduisez l'effort de raisonnement");
        es(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "El modelo no ha dado ninguna respuesta (%s): el tamaño de la respuesta se agotó antes de la primera palabra, auméntalo o reduce el esfuerzo de razonamiento");
        de(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "Das Modell hat nicht geantwortet (%s): Die Antwortgröße war aufgebraucht, bevor das erste Wort kam, erhöhe sie oder verringere den Reasoning-Aufwand");
        ko(THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE, "모델이 응답하지 않았습니다 (%s): 첫 단어가 나오기 전에 응답 크기를 모두 사용했습니다. 크기를 늘리거나 추론 강도를 낮추세요");

        ja(THE_MODEL_GAVE_NO_ANSWER_S_S, "モデルから回答がありませんでした（%s）: %s");
        zh(THE_MODEL_GAVE_NO_ANSWER_S_S, "模型没有给出回答（%s）：%s");
        fr(THE_MODEL_GAVE_NO_ANSWER_S_S, "Le modèle n'a pas répondu (%s) : %s");
        es(THE_MODEL_GAVE_NO_ANSWER_S_S, "El modelo no ha dado ninguna respuesta (%s): %s");
        de(THE_MODEL_GAVE_NO_ANSWER_S_S, "Das Modell hat nicht geantwortet (%s): %s");
        ko(THE_MODEL_GAVE_NO_ANSWER_S_S, "모델이 응답하지 않았습니다 (%s): %s");

        ja(THE_KEY_OF_THE_CLAUDE, "Claude Consoleのキー（platform.claude.com）");
        zh(THE_KEY_OF_THE_CLAUDE, "Claude 控制台的密钥（platform.claude.com）");
        fr(THE_KEY_OF_THE_CLAUDE, "La clé de la Claude Console (platform.claude.com)");
        es(THE_KEY_OF_THE_CLAUDE, "La clave de la Claude Console (platform.claude.com)");
        de(THE_KEY_OF_THE_CLAUDE, "Der Schlüssel der Claude Console (platform.claude.com)");
        ko(THE_KEY_OF_THE_CLAUDE, "Claude Console(platform.claude.com)의 키");

        ja(OPTIONAL_EMPTY_USES_ANTHROPICS, "任意。空欄の場合はAnthropicのAPIを使用します。プロキシ経由にする場合に設定してください。");
        zh(OPTIONAL_EMPTY_USES_ANTHROPICS, "可选。留空则使用 Anthropic 的 API，设置后可通过代理访问。");
        fr(OPTIONAL_EMPTY_USES_ANTHROPICS, "Facultatif. Vide : l'API d'Anthropic ; définissez-la pour passer par un proxy.");
        es(OPTIONAL_EMPTY_USES_ANTHROPICS, "Opcional. Vacío usa la API de Anthropic; indícalo para pasar por un proxy.");
        de(OPTIONAL_EMPTY_USES_ANTHROPICS, "Optional. Leer verwendet die API von Anthropic, setze es, um über einen Proxy zu gehen.");
        ko(OPTIONAL_EMPTY_USES_ANTHROPICS, "선택 사항. 비워 두면 Anthropic의 API를 사용하며, 프록시를 거치려면 설정하세요.");

        ja(THE_CONNECTION_TO_ANTHROPIC, "回答が完了する前にAnthropicとの接続が終了しました");
        zh(THE_CONNECTION_TO_ANTHROPIC, "在回答完成之前，与 Anthropic 的连接已结束");
        fr(THE_CONNECTION_TO_ANTHROPIC, "La connexion à Anthropic s'est terminée avant que la réponse soit complète");
        es(THE_CONNECTION_TO_ANTHROPIC, "La conexión con Anthropic terminó antes de que la respuesta estuviera completa");
        de(THE_CONNECTION_TO_ANTHROPIC, "Die Verbindung zu Anthropic wurde beendet, bevor die Antwort vollständig war");
        ko(THE_CONNECTION_TO_ANTHROPIC, "응답이 완료되기 전에 Anthropic과의 연결이 끊어졌습니다");

        ja(THE_CONTEXT_WINDOW, "モデルのコンテキストウィンドウを使い切りました");
        zh(THE_CONTEXT_WINDOW, "模型的上下文窗口已用完");
        fr(THE_CONTEXT_WINDOW, "La fenêtre de contexte du modèle a été épuisée");
        es(THE_CONTEXT_WINDOW, "Se agotó la ventana de contexto del modelo");
        de(THE_CONTEXT_WINDOW, "Das Kontextfenster des Modells war aufgebraucht");
        ko(THE_CONTEXT_WINDOW, "모델의 컨텍스트 윈도우를 모두 사용했습니다");

        ja(THE_PAYLOAD_HAS_TO_START, "ペイロードはユーザーのメッセージで始まり、ユーザーのメッセージで終わる必要があります。Claudeは回答の続きを書きません");
        zh(THE_PAYLOAD_HAS_TO_START, "载荷必须以用户的消息开头并以用户的消息结尾，Claude 不会续写回答");
        fr(THE_PAYLOAD_HAS_TO_START, "Le contenu envoyé doit commencer et finir par un message de l'utilisateur, Claude ne continue pas une réponse");
        es(THE_PAYLOAD_HAS_TO_START, "El contenido enviado debe empezar y terminar con un mensaje del usuario, Claude no continúa una respuesta");
        de(THE_PAYLOAD_HAS_TO_START, "Der gesendete Inhalt muss mit einer Nachricht des Benutzers beginnen und enden, Claude setzt eine Antwort nicht fort");
        ko(THE_PAYLOAD_HAS_TO_START, "페이로드는 사용자의 메시지로 시작하고 끝나야 합니다. Claude는 응답을 이어서 쓰지 않습니다");
    }
}
