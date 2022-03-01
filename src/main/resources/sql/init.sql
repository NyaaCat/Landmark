create table if not exists landmark
(
    desc    VARCHAR,
    display VARCHAR,
    item    VARCHAR,
    mark    INTEGER not null,
    name    VARCHAR not null
        primary key,
    world   VARCHAR not null,
    x       INTEGER not null,
    y       INTEGER not null,
    z       INTEGER not null
);
create table if not exists playerLandmark
(
    id       INTEGER
        primary key autoincrement,
    landmark VARCHAR not null,
    player   VARCHAR not null
);
