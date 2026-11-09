# Knowledge Tracker

Tracks what the characters you choose know and do **not** know, and puts it into the prompt so the model stops giving
characters knowledge they cannot have (the job of someone they never met). The Knowledge tab of the story sidebar holds the
tracked characters and what each knows; the block goes into the prompt like the Author's Note and/or by the
`{{command::knowledge-tracker-block::<characters>}}` macro.

Done: tracked characters and items (with old knowledge), the lenient reading of the present characters field, the block with
the closed world line, insertion and macro, processing of the active branch (who has met whom, start point, cursor,
reprocess, changed-since-processed), the commands. Reading what the characters learn with the model (a setting): a run of N parts on a worker
thread, the non-modal progress panel, the review of the proposed changes, `knowledge-process ask` and
`apply-knowledge-changes` for agents. The tested prompt and the results of trying it on a real model are in
[TODO.md](TODO.md).

User manual: `manual/user/extensions/knowledge-tracker.md`.
