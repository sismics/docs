create cached table T_PASSWORD_RECOVERY ( PWR_ID_C varchar(36) not null, PWR_USERNAME_C varchar(50) not null, PWR_CREATEDATE_D datetime, PWR_DELETEDATE_D datetime, primary key (PWR_ID_C) );
update T_CONFIG set CFG_VALUE_C = '13' where CFG_ID_C = 'DB_VERSION';
