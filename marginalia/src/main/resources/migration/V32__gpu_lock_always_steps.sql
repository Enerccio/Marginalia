-- The always steps of a GPU lock, in order: they run before every job of the lock, whether the GPU holds it or not (before its acquire steps
-- when it does not), for what must be made sure of each time (another program on the same server may have loaded a model of its own).
CREATE TABLE gpu_lock_always_steps
(
    lock_id  bigint  not null REFERENCES gpu_locks (id),
    step_id  bigint  not null REFERENCES gpu_steps (id),
    ordinal  integer not null,
    primary key (lock_id, ordinal)
);
