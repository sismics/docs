alter table T_TAG add column TAG_COLOR_C varchar(7) default '#3a87ad' not null;
update T_CONFIG set CFG_VALUE_C='2' where CFG_ID_C='DB_VERSION';
