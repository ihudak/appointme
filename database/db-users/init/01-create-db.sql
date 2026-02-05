-- Create databases for AppointMe microservices
create database keycloak owner pguser;
create database appme_users owner pguser;
create database appme_businesses owner pguser;
create database appme_categories owner pguser;
create database appme_feedback owner pguser;

-- Enable PostGIS extension for businesses module (geospatial data)
\c appme_businesses
create extension if not exists postgis;

-- Enable PostGIS extension for categories module (if needed for future features)
\c appme_categories
create extension if not exists postgis;

