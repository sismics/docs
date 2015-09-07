alter table T_USER drop column USE_IDLOCALE_C;
alter table T_USER drop column USE_THEME_C;
alter table T_USER drop column USE_FIRSTCONNECTION_B;
drop table T_LOCALE; 
update T_CONFIG set CFG_VALUE_C = '1' where CFG_ID_C = 'DB_VERSION';
