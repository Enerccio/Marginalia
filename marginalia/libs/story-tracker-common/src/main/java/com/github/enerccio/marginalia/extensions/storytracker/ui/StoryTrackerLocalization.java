package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.github.enerccio.marginalia.extensions.PluginLocalizationBundle;

/**
 * Texts of the library's screens (the progress panel, the review dialog) in the languages besides English; the English text is the
 * key. Each plugin that embeds the library has its own copy of the bundle.
 */
public class StoryTrackerLocalization extends PluginLocalizationBundle {

    public static final StoryTrackerLocalization T = new StoryTrackerLocalization();

    public static final String LOG = "Log";
    public static final String WHAT_THE_MODEL_THOUGHT = "What the model thought";
    public static final String THE_MODEL_IS_WRITING = "What the model is writing";
    public static final String REVIEW_CHANGES = "Review changes";
    public static final String PROCESSING_THE_STORY = "Processing the story";
    public static final String PREPARING = "Preparing…";
    public static final String ASKING_THE_MODEL_RUN = "Asking the model, run %d";
    public static final String WAITING_FOR_YOUR_REVIEW = "%d changes are waiting for your review";
    public static final String APPLYING_THE_CHANGES = "Applying the changes";
    public static final String IT_FAILED = "It failed: %s";
    public static final String TIDY_REQUEST_N_CHANGES = "Tidy request %d: %d change(s) proposed";
    public static final String REQUEST_N_CHANGES = "Request %d: %d change(s) proposed";
    public static final String THE_MODEL_SENT_NO_REASONING = "The model sent no reasoning";
    public static final String WHAT_THE_MODEL_ANSWERED = "What the model answered";
    public static final String APPLY = "Apply";
    public static final String CANCEL = "Cancel";
    public static final String CLOSE = "Close";
    public static final String CHECK_ALL = "Check all";
    public static final String UNCHECK_ALL = "Uncheck all";
    public static final String LEFT_OUT_N = "Left out (%d)";
    public static final String IN_PART_N_QUOTE = "In part %d: \"%s\"";
    public static final String WHY = "Why: %s";
    public static final String THE_QUOTE_IS_NOT_IN_THE_PART = "This quote is not in that part, check it before you accept";
    public static final String REVERTED = "Reverted";
    public static final String REVERT_THIS_CHANGE = "Revert this change";
    public static final String APPLIED_AUTOMATICALLY = "Applied automatically (%d)";
    public static final String IMPORT_FROM_THE_CHARACTER_TRACKER = "Import from the Character Tracker";
    public static final String NAME = "Name";
    public static final String TAGLINE = "Tagline";
    public static final String IMPORT = "Import";
    public static final String IMPORT_D = "Import (%d)";
    public static final String THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS = "The Character Tracker has no characters in this book.";
    public static final String EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED = "Every character of the Character Tracker is tracked already.";

    @Override
    protected void loadKeys() {
        ja(LOG, "ログ");
        zh(LOG, "日志");
        fr(LOG, "Journal");
        es(LOG, "Registro");
        de(LOG, "Protokoll");
        ko(LOG, "로그");
        it(LOG, "Registro");

        ja(WHAT_THE_MODEL_THOUGHT, "モデルの考え");
        zh(WHAT_THE_MODEL_THOUGHT, "模型的思考");
        fr(WHAT_THE_MODEL_THOUGHT, "Ce que le modèle a pensé");
        es(WHAT_THE_MODEL_THOUGHT, "Lo que pensó el modelo");
        de(WHAT_THE_MODEL_THOUGHT, "Was das Modell gedacht hat");
        ko(WHAT_THE_MODEL_THOUGHT, "모델의 생각");
        it(WHAT_THE_MODEL_THOUGHT, "Cosa ha pensato il modello");

        ja(THE_MODEL_IS_WRITING, "モデルが書いている内容");
        zh(THE_MODEL_IS_WRITING, "模型正在写的内容");
        fr(THE_MODEL_IS_WRITING, "Ce que le modèle écrit");
        es(THE_MODEL_IS_WRITING, "Lo que está escribiendo el modelo");
        de(THE_MODEL_IS_WRITING, "Was das Modell schreibt");
        ko(THE_MODEL_IS_WRITING, "모델이 쓰는 내용");
        it(THE_MODEL_IS_WRITING, "Cosa sta scrivendo il modello");

        ja(REVIEW_CHANGES, "変更を確認");
        zh(REVIEW_CHANGES, "审阅更改");
        fr(REVIEW_CHANGES, "Examiner les modifications");
        es(REVIEW_CHANGES, "Revisar los cambios");
        de(REVIEW_CHANGES, "Änderungen prüfen");
        ko(REVIEW_CHANGES, "변경 사항 검토");
        it(REVIEW_CHANGES, "Rivedi le modifiche");

        ja(PROCESSING_THE_STORY, "物語を処理中");
        zh(PROCESSING_THE_STORY, "正在处理故事");
        fr(PROCESSING_THE_STORY, "Traitement de l'histoire");
        es(PROCESSING_THE_STORY, "Procesando la historia");
        de(PROCESSING_THE_STORY, "Die Geschichte wird verarbeitet");
        ko(PROCESSING_THE_STORY, "이야기를 처리하는 중");
        it(PROCESSING_THE_STORY, "Elaborazione della storia");

        ja(PREPARING, "準備中…");
        zh(PREPARING, "准备中…");
        fr(PREPARING, "Préparation…");
        es(PREPARING, "Preparando…");
        de(PREPARING, "Wird vorbereitet …");
        ko(PREPARING, "준비 중…");
        it(PREPARING, "Preparazione…");

        ja(ASKING_THE_MODEL_RUN, "モデルに質問中、%d 回目");
        zh(ASKING_THE_MODEL_RUN, "正在询问模型，第 %d 轮");
        fr(ASKING_THE_MODEL_RUN, "Interrogation du modèle, passe %d");
        es(ASKING_THE_MODEL_RUN, "Consultando al modelo, ejecución %d");
        de(ASKING_THE_MODEL_RUN, "Das Modell wird gefragt, Lauf %d");
        ko(ASKING_THE_MODEL_RUN, "모델에 요청하는 중, 실행 %d");
        it(ASKING_THE_MODEL_RUN, "Richiesta al modello, esecuzione %d");

        ja(WAITING_FOR_YOUR_REVIEW, "%d 件の変更が確認を待っています");
        zh(WAITING_FOR_YOUR_REVIEW, "有 %d 项更改等待您审阅");
        fr(WAITING_FOR_YOUR_REVIEW, "%d modifications attendent votre examen");
        es(WAITING_FOR_YOUR_REVIEW, "%d cambios esperan tu revisión");
        de(WAITING_FOR_YOUR_REVIEW, "%d Änderungen warten auf deine Prüfung");
        ko(WAITING_FOR_YOUR_REVIEW, "변경 %d건이 검토를 기다리고 있습니다");
        it(WAITING_FOR_YOUR_REVIEW, "%d modifiche attendono la tua revisione");

        ja(APPLYING_THE_CHANGES, "変更を適用中");
        zh(APPLYING_THE_CHANGES, "正在应用更改");
        fr(APPLYING_THE_CHANGES, "Application des modifications");
        es(APPLYING_THE_CHANGES, "Aplicando los cambios");
        de(APPLYING_THE_CHANGES, "Die Änderungen werden angewendet");
        ko(APPLYING_THE_CHANGES, "변경 사항을 적용하는 중");
        it(APPLYING_THE_CHANGES, "Applicazione delle modifiche");

        ja(IT_FAILED, "失敗しました：%s");
        zh(IT_FAILED, "失败：%s");
        fr(IT_FAILED, "Échec : %s");
        es(IT_FAILED, "Ha fallado: %s");
        de(IT_FAILED, "Fehlgeschlagen: %s");
        ko(IT_FAILED, "실패했습니다: %s");
        it(IT_FAILED, "Non è riuscito: %s");

        ja(TIDY_REQUEST_N_CHANGES, "整理リクエスト %d：提案された変更 %d 件");
        zh(TIDY_REQUEST_N_CHANGES, "整理请求 %d：提出 %d 项更改");
        fr(TIDY_REQUEST_N_CHANGES, "Requête de rangement %d : %d modification(s) proposée(s)");
        es(TIDY_REQUEST_N_CHANGES, "Solicitud de ordenación %d: %d cambio(s) propuesto(s)");
        de(TIDY_REQUEST_N_CHANGES, "Aufräum-Anfrage %d: %d Änderung(en) vorgeschlagen");
        ko(TIDY_REQUEST_N_CHANGES, "정리 요청 %d: 변경 %d건 제안됨");
        it(TIDY_REQUEST_N_CHANGES, "Richiesta di riordino %d: %d modifica/he proposta/e");

        ja(REQUEST_N_CHANGES, "リクエスト %d：提案された変更 %d 件");
        zh(REQUEST_N_CHANGES, "请求 %d：提出 %d 项更改");
        fr(REQUEST_N_CHANGES, "Requête %d : %d modification(s) proposée(s)");
        es(REQUEST_N_CHANGES, "Solicitud %d: %d cambio(s) propuesto(s)");
        de(REQUEST_N_CHANGES, "Anfrage %d: %d Änderung(en) vorgeschlagen");
        ko(REQUEST_N_CHANGES, "요청 %d: 변경 %d건 제안됨");
        it(REQUEST_N_CHANGES, "Richiesta %d: %d modifica/he proposta/e");

        ja(THE_MODEL_SENT_NO_REASONING, "モデルは推論を送っていません");
        zh(THE_MODEL_SENT_NO_REASONING, "模型没有发送推理内容");
        fr(THE_MODEL_SENT_NO_REASONING, "Le modèle n'a envoyé aucun raisonnement");
        es(THE_MODEL_SENT_NO_REASONING, "El modelo no envió razonamiento");
        de(THE_MODEL_SENT_NO_REASONING, "Das Modell hat kein Reasoning gesendet");
        ko(THE_MODEL_SENT_NO_REASONING, "모델이 추론을 보내지 않았습니다");
        it(THE_MODEL_SENT_NO_REASONING, "Il modello non ha inviato ragionamento");

        ja(WHAT_THE_MODEL_ANSWERED, "モデルの回答");
        zh(WHAT_THE_MODEL_ANSWERED, "模型的回答");
        fr(WHAT_THE_MODEL_ANSWERED, "Ce que le modèle a répondu");
        es(WHAT_THE_MODEL_ANSWERED, "Lo que respondió el modelo");
        de(WHAT_THE_MODEL_ANSWERED, "Was das Modell geantwortet hat");
        ko(WHAT_THE_MODEL_ANSWERED, "모델이 응답한 내용");
        it(WHAT_THE_MODEL_ANSWERED, "Cosa ha risposto il modello");

        ja(APPLY, "適用");
        zh(APPLY, "应用");
        fr(APPLY, "Appliquer");
        es(APPLY, "Aplicar");
        de(APPLY, "Anwenden");
        ko(APPLY, "적용");
        it(APPLY, "Applica");

        ja(CANCEL, "キャンセル");
        zh(CANCEL, "取消");
        fr(CANCEL, "Annuler");
        es(CANCEL, "Cancelar");
        de(CANCEL, "Abbrechen");
        ko(CANCEL, "취소");
        it(CANCEL, "Annulla");

        ja(CLOSE, "閉じる");
        zh(CLOSE, "关闭");
        fr(CLOSE, "Fermer");
        es(CLOSE, "Cerrar");
        de(CLOSE, "Schließen");
        ko(CLOSE, "닫기");
        it(CLOSE, "Chiudi");

        ja(CHECK_ALL, "すべて選択");
        zh(CHECK_ALL, "全选");
        fr(CHECK_ALL, "Tout cocher");
        es(CHECK_ALL, "Marcar todo");
        de(CHECK_ALL, "Alle markieren");
        ko(CHECK_ALL, "모두 체크");
        it(CHECK_ALL, "Seleziona tutto");

        ja(UNCHECK_ALL, "すべて解除");
        zh(UNCHECK_ALL, "全部取消");
        fr(UNCHECK_ALL, "Tout décocher");
        es(UNCHECK_ALL, "Desmarcar todo");
        de(UNCHECK_ALL, "Markierung aller aufheben");
        ko(UNCHECK_ALL, "모두 체크 해제");
        it(UNCHECK_ALL, "Deseleziona tutto");

        ja(LEFT_OUT_N, "除外（%d）");
        zh(LEFT_OUT_N, "已排除（%d）");
        fr(LEFT_OUT_N, "Omis (%d)");
        es(LEFT_OUT_N, "Omitidos (%d)");
        de(LEFT_OUT_N, "Weggelassen (%d)");
        ko(LEFT_OUT_N, "제외됨 (%d)");
        it(LEFT_OUT_N, "Escluso (%d)");

        ja(IN_PART_N_QUOTE, "パート %d：「%s」");
        zh(IN_PART_N_QUOTE, "第 %d 部分：“%s”");
        fr(IN_PART_N_QUOTE, "Dans la partie %d : « %s »");
        es(IN_PART_N_QUOTE, "En la parte %d: \"%s\"");
        de(IN_PART_N_QUOTE, "In Abschnitt %d: \"%s\"");
        ko(IN_PART_N_QUOTE, "파트 %d: \"%s\"");
        it(IN_PART_N_QUOTE, "Nella parte %d: \"%s\"");

        ja(WHY, "理由：%s");
        zh(WHY, "原因：%s");
        fr(WHY, "Pourquoi : %s");
        es(WHY, "Motivo: %s");
        de(WHY, "Warum: %s");
        ko(WHY, "이유: %s");
        it(WHY, "Perché: %s");

        ja(THE_QUOTE_IS_NOT_IN_THE_PART, "この引用はそのパートにありません。受け入れる前に確認してください");
        zh(THE_QUOTE_IS_NOT_IN_THE_PART, "该引文不在此部分中，接受前请先核对");
        fr(THE_QUOTE_IS_NOT_IN_THE_PART, "Cette citation n'est pas dans cette partie, vérifiez-la avant d'accepter");
        es(THE_QUOTE_IS_NOT_IN_THE_PART, "Esta cita no está en esa parte, compruébala antes de aceptar");
        de(THE_QUOTE_IS_NOT_IN_THE_PART, "Dieses Zitat steht nicht in diesem Abschnitt, prüfe es, bevor du es übernimmst");
        ko(THE_QUOTE_IS_NOT_IN_THE_PART, "이 인용문은 해당 파트에 없습니다. 수락하기 전에 확인하세요");
        it(THE_QUOTE_IS_NOT_IN_THE_PART, "Questa citazione non è in quella parte, controllala prima di accettare");
        ja(REVERTED, "元に戻しました");
        zh(REVERTED, "已撤销");
        fr(REVERTED, "Annulé");
        es(REVERTED, "Revertido");
        de(REVERTED, "Rückgängig gemacht");
        ko(REVERTED, "되돌려짐");
        it(REVERTED, "Annullato");
        ja(REVERT_THIS_CHANGE, "この変更を元に戻す");
        zh(REVERT_THIS_CHANGE, "撤销此更改");
        fr(REVERT_THIS_CHANGE, "Annuler cette modification");
        es(REVERT_THIS_CHANGE, "Revertir este cambio");
        de(REVERT_THIS_CHANGE, "Diese Änderung rückgängig machen");
        ko(REVERT_THIS_CHANGE, "이 변경 되돌리기");
        it(REVERT_THIS_CHANGE, "Annulla questa modifica");
        ja(APPLIED_AUTOMATICALLY, "自動で適用された変更 (%d)");
        zh(APPLIED_AUTOMATICALLY, "自动应用的更改 (%d)");
        fr(APPLIED_AUTOMATICALLY, "Appliqué automatiquement (%d)");
        es(APPLIED_AUTOMATICALLY, "Aplicados automáticamente (%d)");
        de(APPLIED_AUTOMATICALLY, "Automatisch angewendet (%d)");
        ko(APPLIED_AUTOMATICALLY, "자동 적용됨 (%d)");
        it(APPLIED_AUTOMATICALLY, "Applicato automaticamente (%d)");

        ja(IMPORT_FROM_THE_CHARACTER_TRACKER, "キャラクタートラッカーからインポート");
        zh(IMPORT_FROM_THE_CHARACTER_TRACKER, "从角色追踪器导入");
        fr(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importer depuis le suivi des personnages");
        es(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importar desde el Seguimiento de personajes");
        de(IMPORT_FROM_THE_CHARACTER_TRACKER, "Aus dem Figuren-Tracker importieren");
        ko(IMPORT_FROM_THE_CHARACTER_TRACKER, "캐릭터 추적기에서 가져오기");
        it(IMPORT_FROM_THE_CHARACTER_TRACKER, "Importa dal Tracciatore dei personaggi");
        ja(NAME, "名前");
        zh(NAME, "名称");
        fr(NAME, "Nom");
        es(NAME, "Nombre");
        de(NAME, "Name");
        ko(NAME, "이름");
        it(NAME, "Nome");
        ja(TAGLINE, "キャッチフレーズ");
        zh(TAGLINE, "标语");
        fr(TAGLINE, "Accroche");
        es(TAGLINE, "Lema");
        de(TAGLINE, "Kurzbeschreibung");
        ko(TAGLINE, "한 줄 소개");
        it(TAGLINE, "Slogan");
        ja(IMPORT, "インポート");
        zh(IMPORT, "导入");
        fr(IMPORT, "Importer");
        es(IMPORT, "Importar");
        de(IMPORT, "Importieren");
        ko(IMPORT, "가져오기");
        it(IMPORT, "Importa");
        ja(IMPORT_D, "インポート (%d)");
        zh(IMPORT_D, "导入 (%d)");
        fr(IMPORT_D, "Importer (%d)");
        es(IMPORT_D, "Importar (%d)");
        de(IMPORT_D, "Importieren (%d)");
        ko(IMPORT_D, "가져오기 (%d)");
        it(IMPORT_D, "Importa (%d)");
        ja(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "この本のキャラクタートラッカーにはキャラクターがいません。");
        zh(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "本书的角色追踪器中没有角色。");
        fr(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "Le suivi des personnages n'a aucun personnage dans ce livre.");
        es(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "El Seguimiento de personajes no tiene personajes en este libro.");
        de(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "Der Figuren-Tracker hat in diesem Buch keine Figuren.");
        ko(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "이 책의 캐릭터 추적기에는 캐릭터가 없습니다.");
        it(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS, "Il Tracciatore dei personaggi non ha personaggi in questo libro.");
        ja(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "キャラクタートラッカーのキャラクターはすべて追跡済みです。");
        zh(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "角色追踪器中的所有角色都已在追踪。");
        fr(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "Tous les personnages du suivi des personnages sont déjà suivis.");
        es(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "Todos los personajes del Seguimiento de personajes ya se siguen.");
        de(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "Alle Figuren des Figuren-Trackers werden bereits verfolgt.");
        ko(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "캐릭터 추적기의 모든 캐릭터를 이미 추적하고 있습니다.");
        it(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED, "Tutti i personaggi del Tracciatore dei personaggi sono già tracciati.");
    }
}
