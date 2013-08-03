alter table T_FILE add column FIL_ORDER_N int;
update T_CONFIG set CFG_VALUE_C='1' where CFG_ID_C='DB_VERSION';
