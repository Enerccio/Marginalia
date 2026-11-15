-- A GPU that programs or people outside Marginalia use too: the acquire steps of a lock then run before every job, not only when the GPU changes locks.
ALTER TABLE gpus ADD COLUMN used_outside boolean not null default 0;
