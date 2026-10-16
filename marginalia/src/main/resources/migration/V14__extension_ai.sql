-- AIs whose provider is implemented by an extension (ExtensionAI): which extension, the settings are in the extended content
create table ais_extension
(
    extensionId varchar(255) not null,
    id          bigint       not null
        primary key
);
