INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'osm', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:3857', 0, 9590, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0/24 ? * *', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/prefergrouplogo', 'true', 2, 9111, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('map/isMapViewerEnabled', 'true', 2, 9592, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/allThesaurus', 'false', 2, 9160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/ui/defaultView', 'default', 0, 10100, 'n');

INSERT INTO settings (name, value, datatype, position, internal) VALUES ('system/server/log','log4j.xml',0,250,'y');

-- Drop unsupported metadata schema
DELETE FROM operationallowed WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadatarating WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadatafiledownloads WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadatafileuploads WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadatanotifications WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadatastatus WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM validation WHERE metadataid in (SELECT id FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std'));
DELETE FROM metadata WHERE schemaid in ('iso19115', 'fgdc-std');

ALTER TABLE ServiceParameters ADD COLUMN occur varchar(1) default '+';
UPDATE ServiceParameters SET occur='+';

create sequence serviceparameter_id_seq start with 1 increment by 1;
alter table serviceparameters add column id integer;
UPDATE serviceparameters SET ID=nextval('serviceparameter_id_seq');

ALTER TABLE ServiceParameters DROP CONSTRAINT serviceparameters_pkey;
ALTER TABLE ServiceParameters ADD PRIMARY KEY (id);



INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/is3DModeAllowed', 'false', 2, 9593, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/isSaveMapInCatalogAllowed', 'true', 2, 9594, 'n');



INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/draftWhenInGroup', '', 0, 100002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/oai/maxrecords', '10', 1, 7040, 'y');


CREATE TABLE metadataidentifiertemplate
(
  id integer NOT NULL,
  name character varying(32) NOT NULL,
  isprovided character(1) NOT NULL DEFAULT 'n'::bpchar,
  template character varying(255) NOT NULL,
  CONSTRAINT metadataidentifiertemplate_pkey PRIMARY KEY (id)
);

INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (0, 'Custom URN', '', 'y');
INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (1, 'Autogenerated URN', '', 'y');

DELETE FROM Settings WHERE name LIKE 'system/shib/%';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/generateUuid', 'true', 2, 9100, 'n');

ALTER TABLE Users Add enabled boolean;
UPDATE Users SET enabled = true;

ALTER TABLE Mapservers Add pushstyleinworkspace boolean;
UPDATE Mapservers set pushstyleinworkspace = 'n';



INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source', 0, 2312, 'n');

DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';

ALTER TABLE schematron RENAME file TO filename;

UPDATE Settings SET value='3.1.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';