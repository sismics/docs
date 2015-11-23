alter table T_USER add column USE_STORAGEQUOTA_N bigint not null default 10000000000;
alter table T_USER add column USE_STORAGECURRENT_N bigint not null default 0;
update T_CONFIG set CFG_VALUE_C = '4' where CFG_ID_C = 'DB_VERSION';
