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

        ja(SHOW_PROMPT_HISTORY, "プロンプト履歴を表示");
        zh(SHOW_PROMPT_HISTORY, "显示提示词历史");

        ja(PROMPT_HISTORY_DESCRIPTION, "このセッションでモデルに送信された直近 %d 件のリクエストと、その回答および推論です（実行中のものは書かれている途中の内容）。圧縮してメモリ上にのみ保持され、ログアウトまたはサーバーの再起動で消えます。");
        zh(PROMPT_HISTORY_DESCRIPTION, "此会话中发送给模型的最近 %d 个请求，以及回答和推理（进行中的请求会显示已写出的内容）。以压缩形式仅保存在内存中，退出登录或服务器重启后即会消失。");

        ja(DATE, "日時");
        zh(DATE, "日期");

        ja(MODEL, "モデル");
        zh(MODEL, "模型");

        ja(REQUEST_TYPE, "リクエストの種類");
        zh(REQUEST_TYPE, "请求类型");

        ja(STATUS, "状態");
        zh(STATUS, "状态");

        ja(DURATION, "所要時間");
        zh(DURATION, "耗时");

        ja(REQUEST, "リクエスト");
        zh(REQUEST, "请求");

        ja(REASONING, "推論");
        zh(REASONING, "推理");

        ja(RESPONSE, "レスポンス");
        zh(RESPONSE, "回答");

        ja(FUNCTION_CALLS, "関数呼び出し");
        zh(FUNCTION_CALLS, "函数调用");

        ja(REFRESH, "更新");
        zh(REFRESH, "刷新");

        ja(CLEAR, "クリア");
        zh(CLEAR, "清除");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");

        ja(NO_REQUESTS_YET, "このセッションではまだリクエストが送信されていません。");
        zh(NO_REQUESTS_YET, "此会话尚未发送任何请求。");

        ja(RUNNING, "実行中");
        zh(RUNNING, "进行中");

        ja(COMPLETED, "完了");
        zh(COMPLETED, "已完成");

        ja(CANCELLED, "キャンセル");
        zh(CANCELLED, "已取消");

        ja(FAILED, "失敗");
        zh(FAILED, "失败");

        ja(OTHER, "その他");
        zh(OTHER, "其他");

        ja(ATTACHMENTS, "[添付 %d 件]");
        zh(ATTACHMENTS, "[%d 个附件]");

        ja(ERROR, "エラー: %s");
        zh(ERROR, "错误：%s");

        ja(FAILED_CALL, "失敗");
        zh(FAILED_CALL, "失败");

        ja(NOT_RUN, "未実行");
        zh(NOT_RUN, "未运行");
    }
}
