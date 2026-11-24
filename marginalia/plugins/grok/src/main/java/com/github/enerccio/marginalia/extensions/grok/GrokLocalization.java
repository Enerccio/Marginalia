package com.github.enerccio.marginalia.extensions.grok;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the xAI Grok provider in the languages besides English; the English text is the key.
 */
public class GrokLocalization extends PluginLocalizationBundle {

    public static final GrokLocalization T = new GrokLocalization();

    public static final String URL = "URL";
    public static final String NO_MODEL_IS_SET = "No model is set";
    public static final String NO_API_KEY_IS_SET = "No API key is set";
    public static final String THE_MODEL_GAVE_NO_ANSWER = "The model gave no answer (%s)";
    public static final String THE_MODEL_GAVE_NO_ANSWER_THE_RESPONSE = "The model gave no answer (%s): the response size was used up before the first word, raise it or lower the reasoning effort";
    public static final String THE_CONTEXT_WINDOW = "The context window of the model was used up";
    public static final String THE_KEY_OF_THE_XAI = "The key of the xAI Console (console.x.ai)";
    public static final String OPTIONAL_EMPTY_USES_XAIS = "Optional. Empty uses xAI's API (https://api.x.ai/v1), set it to go through a proxy.";
    public static final String THE_CONNECTION_TO_XAI = "The connection to xAI ended before the answer was complete";
    public static final String SEND_REASONING_EFFORT = "Send the reasoning effort";
    public static final String EFFORT_AUTO = "Automatic";
    public static final String EFFORT_ALWAYS = "Always";
    public static final String EFFORT_NEVER = "Never";
    public static final String ONLY_GROK_3_MINI_TAKES = "Used when reasoning is on. Automatic sends it to grok-3-mini only: the other models refuse the parameter.";

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

        ja(THE_CONTEXT_WINDOW, "モデルのコンテキストウィンドウを使い切りました");
        zh(THE_CONTEXT_WINDOW, "模型的上下文窗口已用完");
        fr(THE_CONTEXT_WINDOW, "La fenêtre de contexte du modèle a été épuisée");
        es(THE_CONTEXT_WINDOW, "Se agotó la ventana de contexto del modelo");
        de(THE_CONTEXT_WINDOW, "Das Kontextfenster des Modells war aufgebraucht");
        ko(THE_CONTEXT_WINDOW, "모델의 컨텍스트 윈도우를 모두 사용했습니다");
        it(THE_CONTEXT_WINDOW, "La finestra di contesto del modello si è esaurita");

        ja(THE_KEY_OF_THE_XAI, "xAIコンソールのキー（console.x.ai）");
        zh(THE_KEY_OF_THE_XAI, "xAI 控制台的密钥（console.x.ai）");
        fr(THE_KEY_OF_THE_XAI, "La clé de la console xAI (console.x.ai)");
        es(THE_KEY_OF_THE_XAI, "La clave de la consola de xAI (console.x.ai)");
        de(THE_KEY_OF_THE_XAI, "Der Schlüssel der xAI-Konsole (console.x.ai)");
        ko(THE_KEY_OF_THE_XAI, "xAI 콘솔의 키 (console.x.ai)");
        it(THE_KEY_OF_THE_XAI, "La chiave della console xAI (console.x.ai)");

        ja(OPTIONAL_EMPTY_USES_XAIS, "任意。空欄の場合はxAIのAPI（https://api.x.ai/v1）を使用します。プロキシ経由にする場合に設定してください。");
        zh(OPTIONAL_EMPTY_USES_XAIS, "可选。留空则使用 xAI 的 API（https://api.x.ai/v1），设置后可通过代理访问。");
        fr(OPTIONAL_EMPTY_USES_XAIS, "Facultatif. Vide, l'API de xAI (https://api.x.ai/v1) est utilisée, renseignez-le pour passer par un proxy.");
        es(OPTIONAL_EMPTY_USES_XAIS, "Opcional. Vacío usa la API de xAI (https://api.x.ai/v1), indíquelo para pasar por un proxy.");
        de(OPTIONAL_EMPTY_USES_XAIS, "Optional. Leer wird die API von xAI (https://api.x.ai/v1) verwendet, setzen Sie es, um über einen Proxy zu gehen.");
        ko(OPTIONAL_EMPTY_USES_XAIS, "선택 사항. 비워 두면 xAI의 API(https://api.x.ai/v1)를 사용하며, 프록시를 거치려면 설정하세요.");
        it(OPTIONAL_EMPTY_USES_XAIS, "Facoltativo. Vuoto usa l'API di xAI (https://api.x.ai/v1), impostalo per passare da un proxy.");

        ja(THE_CONNECTION_TO_XAI, "回答が完了する前にxAIとの接続が終了しました");
        zh(THE_CONNECTION_TO_XAI, "在回答完成之前，与 xAI 的连接已结束");
        fr(THE_CONNECTION_TO_XAI, "La connexion à xAI s'est terminée avant que la réponse soit complète");
        es(THE_CONNECTION_TO_XAI, "La conexión con xAI terminó antes de que la respuesta estuviera completa");
        de(THE_CONNECTION_TO_XAI, "Die Verbindung zu xAI wurde beendet, bevor die Antwort vollständig war");
        ko(THE_CONNECTION_TO_XAI, "응답이 완료되기 전에 xAI와의 연결이 끊어졌습니다");
        it(THE_CONNECTION_TO_XAI, "La connessione a xAI si è interrotta prima che la risposta fosse completa");

        ja(SEND_REASONING_EFFORT, "推論の強度を送信する");
        zh(SEND_REASONING_EFFORT, "发送推理强度");
        fr(SEND_REASONING_EFFORT, "Envoyer l'effort de raisonnement");
        es(SEND_REASONING_EFFORT, "Enviar el esfuerzo de razonamiento");
        de(SEND_REASONING_EFFORT, "Reasoning-Aufwand senden");
        ko(SEND_REASONING_EFFORT, "추론 강도 전송");
        it(SEND_REASONING_EFFORT, "Invia l'impegno di ragionamento");

        ja(EFFORT_AUTO, "自動");
        zh(EFFORT_AUTO, "自动");
        fr(EFFORT_AUTO, "Automatique");
        es(EFFORT_AUTO, "Automático");
        de(EFFORT_AUTO, "Automatisch");
        ko(EFFORT_AUTO, "자동");
        it(EFFORT_AUTO, "Automatico");

        ja(EFFORT_ALWAYS, "常に");
        zh(EFFORT_ALWAYS, "总是");
        fr(EFFORT_ALWAYS, "Toujours");
        es(EFFORT_ALWAYS, "Siempre");
        de(EFFORT_ALWAYS, "Immer");
        ko(EFFORT_ALWAYS, "항상");
        it(EFFORT_ALWAYS, "Sempre");

        ja(EFFORT_NEVER, "送信しない");
        zh(EFFORT_NEVER, "从不");
        fr(EFFORT_NEVER, "Jamais");
        es(EFFORT_NEVER, "Nunca");
        de(EFFORT_NEVER, "Nie");
        ko(EFFORT_NEVER, "안 함");
        it(EFFORT_NEVER, "Mai");

        ja(ONLY_GROK_3_MINI_TAKES, "推論がオンのときに使用されます。「自動」はgrok-3-miniにのみ送信します。他のモデルはこのパラメータを拒否します。");
        zh(ONLY_GROK_3_MINI_TAKES, "推理开启时使用。“自动”仅发送给 grok-3-mini：其他模型会拒绝该参数。");
        fr(ONLY_GROK_3_MINI_TAKES, "Utilisé lorsque le raisonnement est activé. « Automatique » l'envoie à grok-3-mini seulement : les autres modèles refusent ce paramètre.");
        es(ONLY_GROK_3_MINI_TAKES, "Se usa cuando el razonamiento está activado. «Automático» lo envía solo a grok-3-mini: los demás modelos rechazan el parámetro.");
        de(ONLY_GROK_3_MINI_TAKES, "Wird verwendet, wenn das Reasoning aktiv ist. „Automatisch“ sendet ihn nur an grok-3-mini: die anderen Modelle lehnen den Parameter ab.");
        ko(ONLY_GROK_3_MINI_TAKES, "추론이 켜져 있을 때 사용됩니다. \"자동\"은 grok-3-mini에만 보내며, 다른 모델은 이 매개변수를 거부합니다.");
        it(ONLY_GROK_3_MINI_TAKES, "Usato quando il ragionamento è attivo. «Automatico» lo invia solo a grok-3-mini: gli altri modelli rifiutano il parametro.");
    }
}
