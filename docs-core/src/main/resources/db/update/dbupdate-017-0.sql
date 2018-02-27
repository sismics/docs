insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_ENABLED', 'false');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_HOSTNAME', '');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_PORT', '993');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_USERNAME', '');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_PASSWORD', '');
insert into T_CONFIG(CFG_ID_C, CFG_VALUE_C) values('INBOX_TAG', '');
update T_CONFIG set CFG_VALUE_C = '17' where CFG_ID_C = 'DB_VERSION';
