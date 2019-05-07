alter table T_USER add column USE_ONBOARDING_B bit not null default 1;
update T_CONFIG set CFG_VALUE_C = '23' where CFG_ID_C = 'DB_VERSION';
