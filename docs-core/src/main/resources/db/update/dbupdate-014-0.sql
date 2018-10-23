alter table T_USER add column USE_DISABLEDATE_D datetime;
update T_CONFIG set CFG_VALUE_C = '14' where CFG_ID_C = 'DB_VERSION';
