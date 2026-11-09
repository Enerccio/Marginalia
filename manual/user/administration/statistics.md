---
label: Statistics
order: 87
---

# Statistics

The [writing statistics](../books/statistics.md) of every user (the words, the time spent, the size of each book, day by day) are kept
in the database. They record when the users worked on their books and the names and folders of the books, also of the ones that were
deleted. The **Statistics** tab of the Admin page gives you the means to limit that. Users can do the same for themselves in their
[settings](../account.md) and clear their own statistics in the statistics windows.

| | |
|---|---|
| **Record writing statistics** | Off stops the recording for every user, whatever they chose. Nothing that was recorded is deleted by it. The statistics windows say that nothing new is counted. |
| **Keep statistics for (months)** | The days older than that are deleted for good from the statistics of every user, once a day (at about 4:20, and shortly after the start) and when you save. Empty or 0 keeps everything. A user may choose a shorter time for themselves, never a longer one. |
| **Clear the statistics of the user** | Deletes all the statistics of the chosen user, after a question. Their books are not changed. |
| **Clear the statistics of everybody** | The same for all users. |

Deleted statistics cannot be brought back, except from a [database backup](database-backups.md): the statistics are part of the
database (the backup of a single book does not carry them).

The [cleanup](cleanup.md) does not touch the statistics, because they are not deleted objects: they hold the book by its id and
outlive it on purpose.
