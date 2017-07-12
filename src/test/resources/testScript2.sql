CREATE TABLE CB.wms_showroom_asn
(
  showroom_asn_id						NUMBER(10) NOT NULL,
  mic_com_id 								NUMBER(10) NOT NULL,
  logistics_company 				NUMBER(2),
  tracking_number						VARCHAR2(255),
  shipping_time       			DATE ,
  arrival_flag							NUMBER(1) default 0,
  arrival_time       				DATE ,
  delete_flag    						NUMBER(1) NOT NULL,
  add_no         						VARCHAR2 (50) NOT NULL,
  add_name       						VARCHAR2 (100) NOT NULL,
  add_time       						DATE NOT NULL,
  add_site       						varchar2 (10) NOT NULL,
  update_no      						VARCHAR2 (50) NOT NULL,
  update_name    						VARCHAR2 (100) NOT NULL,
  update_time    						DATE NOT NULL,
  update_site    						varchar2 (10) NOT NULL,
  delete_no      						VARCHAR2 (50),
  delete_name    						VARCHAR2 (100),
  delete_time    						DATE,
  operator_ip    						varchar2 (50),
  rep_time       						TIMESTAMP (6) WITH TIME ZONE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_wms_showroom_asn PRIMARY KEY (showroom_asn_id) USING INDEX TABLESPACE t_cb_idx
)
TABLESPACE t_cb;


-- comments

COMMENT ON TABLE CB.wms_showroom_asn IS '展厅收货单';

COMMENT ON COLUMN CB.wms_showroom_asn.showroom_asn_id IS 'id,主键';
COMMENT ON COLUMN CB.wms_showroom_asn.mic_com_id	IS 'mic公司ID';
COMMENT ON COLUMN CB.wms_showroom_asn.logistics_company	IS '物流公司';
COMMENT ON COLUMN CB.wms_showroom_asn.tracking_number IS '物流单号';
COMMENT ON COLUMN CB.wms_showroom_asn.shipping_time	IS '出运时间';
COMMENT ON COLUMN CB.wms_showroom_asn.arrival_flag	IS '是否到货:0-未到货,1-已到货';
COMMENT ON COLUMN CB.wms_showroom_asn.arrival_time	IS '到货时间';
COMMENT ON COLUMN CB.wms_showroom_asn.delete_flag	IS '删除标记：0：未删除；1：删除';
COMMENT ON COLUMN CB.wms_showroom_asn.add_no	IS '添加人';
COMMENT ON COLUMN CB.wms_showroom_asn.add_name	IS '添加人姓名';
COMMENT ON COLUMN CB.wms_showroom_asn.add_time	IS '添加时间';
COMMENT ON COLUMN CB.wms_showroom_asn.add_site	IS '添加站点 : TEL 电信、LOCAL 本地、PHX 凤凰、USA 美国';
COMMENT ON COLUMN CB.wms_showroom_asn.update_no	IS '修改人';
COMMENT ON COLUMN CB.wms_showroom_asn.update_name	IS '修改人姓名';
COMMENT ON COLUMN CB.wms_showroom_asn.update_time	IS '修改时间';
COMMENT ON COLUMN CB.wms_showroom_asn.update_site	IS '修改站点 : TEL 电信、LOCAL 本地、PHX 凤凰、USA 美国';
COMMENT ON COLUMN CB.wms_showroom_asn.delete_no	IS '删除人';
COMMENT ON COLUMN CB.wms_showroom_asn.delete_name	IS '删除人姓名';
COMMENT ON COLUMN CB.wms_showroom_asn.delete_time	IS '删除时间';
COMMENT ON COLUMN CB.wms_showroom_asn.operator_ip	IS '操作IP';
COMMENT ON COLUMN CB.wms_showroom_asn.rep_time	IS '时间戳';


--index

CREATE INDEX cb.idx_wms_showroom_asn_01 ON cb.wms_showroom_asn (mic_com_id) TABLESPACE t_cb_idx;

CREATE INDEX cb.idx_wms_showroom_asn_02 ON cb.wms_showroom_asn (tracking_number) TABLESPACE t_cb_idx;

CREATE OR REPLACE TRIGGER CB.T_wms_showroom_asn
BEFORE INSERT OR UPDATE ON CB.wms_showroom_asn FOR EACH ROW
  DECLARE
    tmpvar INTEGER;
  BEGIN
    IF :NEW.showroom_asn_id	IS NULL THEN
      SELECT CB.S_wms_showroom_asn.nextval INTO tmpvar FROM DUAL;
      :NEW.showroom_asn_id := tmpvar;
    END IF;
  END;
/

CREATE OR REPLACE TRIGGER CB.REP_wms_showroom_asn
BEFORE
INSERT OR UPDATE ON CB.wms_showroom_asn FOR EACH ROW
  BEGIN
    IF:OLD.REP_TIME IS NULL OR :OLD.REP_TIME<SYSTIMESTAMP THEN
      :NEW.REP_TIME := SYSTIMESTAMP;
    ELSE
      :NEW.REP_TIME := :OLD.REP_TIME + 1 / 86400;
    END IF;
  END;
/


