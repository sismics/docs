alter table T_TAG add column TAG_IDPARENT_C varchar(36);
update T_CONFIG set CFG_VALUE_C = '2' where CFG_ID_C = 'DB_VERSION';
