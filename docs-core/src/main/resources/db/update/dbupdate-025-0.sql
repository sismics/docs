insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_AUTOMATIC_TAGS', 'false');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_DELETE_IMPORTED', 'false');
update T_CONFIG set CFG_VALUE_C = '25' where CFG_ID_C = 'DB_VERSION';
