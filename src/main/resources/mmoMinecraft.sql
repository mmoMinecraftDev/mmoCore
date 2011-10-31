--
-- Table structure for table `mmoMinecraft`
--

CREATE TABLE IF NOT EXISTS `mmoMinecraft` (
  `plugin` VARCHAR(64),
  `player` VARCHAR(64),
  `key` VARCHAR(256),
  `value` VARCHAR(2048),
  PRIMARY KEY  (`plugin`,`player`,`key`)
);
