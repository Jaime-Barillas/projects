CREATE ROLE
    robobots_admin
WITH
    CREATEROLE
    LOGIN
    PASSWORD 'robobots_admin';


CREATE ROLE
    robobots_api
WITH
    LOGIN
    PASSWORD 'robobots_api';

CREATE DATABASE
    robobots
WITH
    OWNER = robobots_admin;


GRANT
    CONNECT,
    TEMPORARY
ON DATABASE
    robobots
TO
    robobots_api;


REVOKE
    ALL
ON DATABASE
    robobots
FROM
    PUBLIC;
