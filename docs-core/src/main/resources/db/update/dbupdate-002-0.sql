alter table T_TAG add column TAG_COLOR_C varchar(6) not null;
update T_TAG set TAG_COLOR_C = '3a87ad';
update T_CONFIG set CFG_VALUE_C='2' where CFG_ID_C='DB_VERSION';
