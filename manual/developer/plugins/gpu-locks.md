---
label: GPU locks
order: 63
covers:
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/service/GpuService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/gpu
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/service/impl/GpuServiceImpl.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/model/impl/Gpu.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/model/impl/GpuLock.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/model/impl/GpuStep.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/ui/widgets/GpuLockSelect.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/ui/workspace/parts/admin/GpuPanel.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/service/impl/inference/GatedInferenceService.java
verified: f337fe83d
---

# GPU locks

A graphics card that holds one model at a time (the language model, an image server, a speech server) is shared through `GpuService`.
The administrator sets it up in *Admin → GPUs* (`GpuPanel`); an AI or a backend of a plugin names a **lock** and takes it for every job.
The user side is described in the manual, [GPUs](../../user/administration/gpus.md).

## The model

Three entities of the installation (`BaseEntity`, not owned by a user; migration `V29__gpus.sql`), changed only by an administrator
(`AdminGuard`):

| Entity | Table | |
|---|---|---|
| `Gpu` | `gpus` | A name and `usedOutside` (migration `V30__gpu_used_outside.sql`). What it is loaded with is known only in memory. |
| `GpuStep` | `gpu_steps` | An HTTP request: `url`, `method`, `headers` (`EncryptedStringConverter`), `body`, `timeoutSeconds` (0: 60, at most 600), `waitSeconds` (at most 300). Shared by locks. |
| `GpuLock` | `gpu_locks` | `name`, `gpu`, `maxConcurrent` (0: no limit) and the ordered `acquireSteps` / `releaseSteps` / `alwaysSteps` (`@ManyToMany` + `@OrderColumn`, tables `gpu_lock_acquire_steps`, `gpu_lock_release_steps` and `gpu_lock_always_steps`, the last from migration `V32__gpu_lock_always_steps.sql`). |

Deletes are hard: a GPU takes its locks with it, a step is taken out of the locks first. A uuid that names no lock any more means *no lock*.

## The turns

`GpuArbiter` (`domain.service.gpu`, no Spring and no database, tested on its own by `GpuArbiterTest`) keeps one state per GPU: the
loaded lock (null: unknown), the jobs that hold it, whether steps run, and a FIFO queue. `GpuServiceImpl` gives it the locks through a
resolver over an in-memory snapshot of the entities (read in a read-only transaction, dropped after every change).

- A job of the **loaded lock** joins at once while `maxConcurrent` allows and nobody waits ahead of it. No step runs, except the lock's
  **always steps** (`LockDef.always`, `eachJob()`), which run before every job of it (`recheck`, on a virtual thread, its place already counted;
  a failure fails the job and leaves the GPU unknown).
- A job of **another lock** waits until the holders are gone. The switch runs on a virtual thread of its own: the release steps of the
  loaded lock (`GpuStepRunner.runAllRegardless`, all of them), then the always and the acquire steps of the new one (`onSwitch()`, `runAll`, the first failure stops).
  A failed acquire leaves the GPU unknown and fails the job; a failed release fails the job and keeps the old lock loaded (the card may be
  full, nothing is started on it).
- Closing a lease runs **no** step: the GPU stays loaded (sticky). There is no timeout.
- A GPU **used outside** the application (`LockDef.acquireEachJob`, from `Gpu.usedOutside`) never trusts that state: a job of the loaded lock runs
  its always and acquire steps first (`recheck`); a failure fails the job and leaves the GPU unknown.
- The queue is fair: once a job waits for a switch, jobs of the loaded lock queue behind it.
- `unload(gpu)` queues like a job, runs the release steps and leaves the GPU unknown whatever they answered.
- `GpuLease.reload()` runs the release, the always and the acquire steps of the held lock again (a server that died in the middle of a long job).

## Using it from a plugin

```java
@Autowired
private GpuService gpuService;

// in the settings form of a backend (hidden when no lock exists)
GpuLockSelect gpuLock = new GpuLockSelect();
gpuLock.setLockUuid(config.getGpuLock());
...
if (gpuLock.isVisible()) {
    config.setGpuLock(gpuLock.getLockUuid());
}

// around one job, on a worker thread
try (GpuLease lease = gpuService.acquire(config.getGpuLock(), cancel::isCancelled)) {   // blank or unknown uuid: GpuLease.NONE at once
    runMyHooks();
    backend.work();
} catch (GpuException e) {
    // a step failed, or e.isCancelled()
}
```

`GpuLockSelect` reads the locks again in `setLockUuid` and when it is attached (`reload()` does it by hand), so a form built once per page
shows the locks the administrator added later; call `setLockUuid` every time the form shows a backend. On attach it changes nothing
while the locks are the same (resetting the items would fire value changes, which the settings page counts as unsaved edits).

`acquireAsync(lockUuid, position)` is the non-blocking form (a `CompletableFuture<GpuLease>`, cancelling it leaves the queue; `position`
hears how many jobs are ahead). `lockChoices()` / `lockChoice(uuid)` give every user the names for a picker, without steps or secrets.

Hold the lease for **one job** (an image, a batch), not for a window: a window left open must not keep the card. The Image Generator does it
in `ImageJob`, the Audiobook in `SpeechBatch` (its restart of a dead server calls `reload()`).

## The language model

`AI.gpuLock` is an `@ExtendedAttribute` (no migration) set in the general settings of the `AIDialog`. `GatedInferenceService` composes the
lease with the connection of the AI (`AIGate`): the connection first (a request never holds the GPU while it waits for a connection), then
`acquireAsync` of the lock; one permit gives back both. It is given back with the connection while a function the model called runs, so a
function that asks the same model does not wait for itself, and taken again for the next round. A failed acquire ends the request with
`onError` before anything is sent.
