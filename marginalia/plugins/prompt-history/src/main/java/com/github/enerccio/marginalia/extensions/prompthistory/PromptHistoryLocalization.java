package com.github.enerccio.marginalia.extensions.prompthistory;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the Prompt History in the languages besides English; the English text is the key.
 */
public class PromptHistoryLocalization extends PluginLocalizationBundle {

    public static final PromptHistoryLocalization T = new PromptHistoryLocalization();

    public static final String PROMPT_HISTORY = "Prompt history";
    public static final String SHOW_PROMPT_HISTORY = "Show prompt history";
    public static final String PROMPT_HISTORY_DESCRIPTION =
            "The last %d requests sent to the models in this session, with the answers and the reasoning, a running one as it is written. They are kept (compressed) in memory only and are gone when you log out or the server restarts.";
    public static final String DATE = "Date";
    public static final String MODEL = "Model";
    public static final String REQUEST_TYPE = "Request type";
    public static final String STATUS = "Status";
    public static final String DURATION = "Duration";
    public static final String REQUEST = "Request";
    public static final String REASONING = "Reasoning";
    public static final String RESPONSE = "Response";
    public static final String FUNCTION_CALLS = "Function calls";
    public static final String REFRESH = "Refresh";
    public static final String CLEAR = "Clear";
    public static final String CLOSE = "Close";
    public static final String NO_REQUESTS_YET = "No request has been sent in this session yet.";
    public static final String RUNNING = "Running";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";
    public static final String FAILED = "Failed";
    public static final String OTHER = "other";
    public static final String ATTACHMENTS = "[%d attachment(s)]";
    public static final String ERROR = "Error: %s";
    public static final String FAILED_CALL = "failed";
    public static final String NOT_RUN = "not run";

    @Override
    protected void loadKeys() {
        ja(PROMPT_HISTORY, "プロンプト履歴");
        zh(PROMPT_HISTORY, "提示词历史");
        fr(PROMPT_HISTORY, "Historique des prompts");
        es(PROMPT_HISTORY, "Historial de prompts");
        de(PROMPT_HISTORY, "Prompt-Verlauf");
        ko(PROMPT_HISTORY, "프롬프트 기록");
        it(PROMPT_HISTORY, "Cronologia dei prompt");

        ja(SHOW_PROMPT_HISTORY, "プロンプト履歴を表示");
        zh(SHOW_PROMPT_HISTORY, "显示提示词历史");
        fr(SHOW_PROMPT_HISTORY, "Afficher l'historique des prompts");
        es(SHOW_PROMPT_HISTORY, "Mostrar el historial de prompts");
        de(SHOW_PROMPT_HISTORY, "Prompt-Verlauf anzeigen");
        ko(SHOW_PROMPT_HISTORY, "프롬프트 기록 보기");
        it(SHOW_PROMPT_HISTORY, "Mostra la cronologia dei prompt");

        ja(PROMPT_HISTORY_DESCRIPTION, "このセッションでモデルに送信された直近 %d 件のリクエストと、その回答および推論です（実行中のものは書かれている途中の内容）。圧縮してメモリ上にのみ保持され、ログアウトまたはサーバーの再起動で消えます。");
        zh(PROMPT_HISTORY_DESCRIPTION, "此会话中发送给模型的最近 %d 个请求，以及回答和推理（进行中的请求会显示已写出的内容）。以压缩形式仅保存在内存中，退出登录或服务器重启后即会消失。");
        fr(PROMPT_HISTORY_DESCRIPTION, "Les %d dernières requêtes envoyées aux modèles pendant cette session, avec les réponses et le raisonnement, celle en cours au fil de son écriture. Elles sont gardées (compressées) en mémoire seulement et disparaissent quand vous vous déconnectez ou que le serveur redémarre.");
        es(PROMPT_HISTORY_DESCRIPTION, "Las últimas %d solicitudes enviadas a los modelos en esta sesión, con las respuestas y el razonamiento; la que está en curso se muestra a medida que se escribe. Se conservan (comprimidas) solo en memoria y desaparecen al cerrar sesión o al reiniciar el servidor.");
        de(PROMPT_HISTORY_DESCRIPTION, "Die letzten %d Anfragen, die in dieser Sitzung an die Modelle gesendet wurden, mit den Antworten und dem Reasoning, eine laufende so, wie sie geschrieben wird. Sie werden (komprimiert) nur im Arbeitsspeicher aufbewahrt und sind weg, wenn du dich abmeldest oder der Server neu startet.");
        ko(PROMPT_HISTORY_DESCRIPTION, "이 세션에서 모델에 전송된 최근 %d개의 요청과 답변 및 추론이며, 실행 중인 요청은 쓰이는 대로 표시됩니다. 압축하여 메모리에만 보관되며, 로그아웃하거나 서버를 다시 시작하면 사라집니다.");
        it(PROMPT_HISTORY_DESCRIPTION, "Le ultime %d richieste inviate ai modelli in questa sessione, con le risposte e il ragionamento, quella in corso così come viene scritta. Sono conservate (compresse) solo in memoria e scompaiono quando esci o il server si riavvia.");

        ja(DATE, "日時");
        zh(DATE, "日期");
        fr(DATE, "Date");
        es(DATE, "Fecha");
        de(DATE, "Datum");
        ko(DATE, "날짜");
        it(DATE, "Data");

        ja(MODEL, "モデル");
        zh(MODEL, "模型");
        fr(MODEL, "Modèle");
        es(MODEL, "Modelo");
        de(MODEL, "Modell");
        ko(MODEL, "모델");
        it(MODEL, "Modello");

        ja(REQUEST_TYPE, "リクエストの種類");
        zh(REQUEST_TYPE, "请求类型");
        fr(REQUEST_TYPE, "Type de requête");
        es(REQUEST_TYPE, "Tipo de solicitud");
        de(REQUEST_TYPE, "Anfragetyp");
        ko(REQUEST_TYPE, "요청 유형");
        it(REQUEST_TYPE, "Tipo di richiesta");

        ja(STATUS, "状態");
        zh(STATUS, "状态");
        fr(STATUS, "Statut");
        es(STATUS, "Estado");
        de(STATUS, "Status");
        ko(STATUS, "상태");
        it(STATUS, "Stato");

        ja(DURATION, "所要時間");
        zh(DURATION, "耗时");
        fr(DURATION, "Durée");
        es(DURATION, "Duración");
        de(DURATION, "Dauer");
        ko(DURATION, "소요 시간");
        it(DURATION, "Durata");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");
        fr(REQUEST, "Requête");
        es(REQUEST, "Solicitud");
        de(REQUEST, "Anfrage");
        ko(REQUEST, "요청");
        it(REQUEST, "Richiesta");

        ja(REASONING, "推論");
        zh(REASONING, "推理");
        fr(REASONING, "Raisonnement");
        es(REASONING, "Razonamiento");
        de(REASONING, "Reasoning");
        ko(REASONING, "추론");
        it(REASONING, "Ragionamento");

        ja(RESPONSE, "レスポンス");
        zh(RESPONSE, "回答");
        fr(RESPONSE, "Réponse");
        es(RESPONSE, "Respuesta");
        de(RESPONSE, "Antwort");
        ko(RESPONSE, "응답");
        it(RESPONSE, "Risposta");

        ja(FUNCTION_CALLS, "関数呼び出し");
        zh(FUNCTION_CALLS, "函数调用");
        fr(FUNCTION_CALLS, "Appels de fonctions");
        es(FUNCTION_CALLS, "Llamadas a funciones");
        de(FUNCTION_CALLS, "Funktionsaufrufe");
        ko(FUNCTION_CALLS, "함수 호출");
        it(FUNCTION_CALLS, "Chiamate di funzione");

        ja(REFRESH, "更新");
        zh(REFRESH, "刷新");
        fr(REFRESH, "Actualiser");
        es(REFRESH, "Actualizar");
        de(REFRESH, "Aktualisieren");
        ko(REFRESH, "새로 고침");
        it(REFRESH, "Aggiorna");

        ja(CLEAR, "クリア");
        zh(CLEAR, "清除");
        fr(CLEAR, "Effacer");
        es(CLEAR, "Vaciar");
        de(CLEAR, "Leeren");
        ko(CLEAR, "지우기");
        it(CLEAR, "Svuota");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(NO_REQUESTS_YET, "このセッションではまだリクエストが送信されていません。");
        zh(NO_REQUESTS_YET, "此会话尚未发送任何请求。");
        fr(NO_REQUESTS_YET, "Aucune requête n'a encore été envoyée pendant cette session.");
        es(NO_REQUESTS_YET, "Todavía no se ha enviado ninguna solicitud en esta sesión.");
        de(NO_REQUESTS_YET, "In dieser Sitzung wurde noch keine Anfrage gesendet.");
        ko(NO_REQUESTS_YET, "이 세션에서 아직 전송된 요청이 없습니다.");
        it(NO_REQUESTS_YET, "In questa sessione non è ancora stata inviata alcuna richiesta.");

        ja(RUNNING, "実行中");
        zh(RUNNING, "进行中");
        fr(RUNNING, "En cours");
        es(RUNNING, "En ejecución");
        de(RUNNING, "Läuft");
        ko(RUNNING, "실행 중");
        it(RUNNING, "In esecuzione");

        ja(COMPLETED, "完了");
        zh(COMPLETED, "已完成");
        fr(COMPLETED, "Terminée");
        es(COMPLETED, "Completada");
        de(COMPLETED, "Abgeschlossen");
        ko(COMPLETED, "완료됨");
        it(COMPLETED, "Completato");

        ja(CANCELLED, "キャンセル");
        zh(CANCELLED, "已取消");
        fr(CANCELLED, "Annulée");
        es(CANCELLED, "Cancelada");
        de(CANCELLED, "Abgebrochen");
        ko(CANCELLED, "취소됨");
        it(CANCELLED, "Annullato");

        ja(FAILED, "失敗");
        zh(FAILED, "失败");
        fr(FAILED, "Échouée");
        es(FAILED, "Fallida");
        de(FAILED, "Fehlgeschlagen");
        ko(FAILED, "실패");
        it(FAILED, "Non riuscito");

        ja(OTHER, "その他");
        zh(OTHER, "其他");
        fr(OTHER, "autre");
        es(OTHER, "otro");
        de(OTHER, "andere");
        ko(OTHER, "기타");
        it(OTHER, "altro");

        ja(ATTACHMENTS, "[添付 %d 件]");
        zh(ATTACHMENTS, "[%d 个附件]");
        fr(ATTACHMENTS, "[%d pièce(s) jointe(s)]");
        es(ATTACHMENTS, "[%d adjunto(s)]");
        de(ATTACHMENTS, "[%d Anhang/Anhänge]");
        ko(ATTACHMENTS, "[첨부 파일 %d개]");
        it(ATTACHMENTS, "[%d allegato/i]");

        ja(ERROR, "エラー: %s");
        zh(ERROR, "错误：%s");
        fr(ERROR, "Erreur : %s");
        es(ERROR, "Error: %s");
        de(ERROR, "Fehler: %s");
        ko(ERROR, "오류: %s");
        it(ERROR, "Errore: %s");

        ja(FAILED_CALL, "失敗");
        zh(FAILED_CALL, "失败");
        fr(FAILED_CALL, "échoué");
        es(FAILED_CALL, "fallida");
        de(FAILED_CALL, "fehlgeschlagen");
        ko(FAILED_CALL, "실패");
        it(FAILED_CALL, "non riuscito");

        ja(NOT_RUN, "未実行");
        zh(NOT_RUN, "未运行");
        fr(NOT_RUN, "non exécuté");
        es(NOT_RUN, "no ejecutada");
        de(NOT_RUN, "nicht ausgeführt");
        ko(NOT_RUN, "실행되지 않음");
        it(NOT_RUN, "non eseguito");
    }
}
