insert into T_ROLE(ROL_ID_C, ROL_NAME_C, ROL_CREATEDATE_D) values('user', 'User', NOW());
update T_CONFIG set CFG_VALUE_C='3' where CFG_ID_C='DB_VERSION';
