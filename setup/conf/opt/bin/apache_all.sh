#!/bin/sh

VERSIONS=""
#VERSIONS="${VERSIONS} 1.0.0"
#VERSIONS="${VERSIONS} 1.1.0"
VERSIONS="${VERSIONS} 1.2.0"
VERSIONS="${VERSIONS} 1.3.0"
VERSIONS="${VERSIONS} 1.4.0"
VERSIONS="${VERSIONS} 1.5.0"
VERSIONS="${VERSIONS} 1.6.0"
VERSIONS="${VERSIONS} 1.7.0"
VERSIONS="${VERSIONS} 1.8.0"
VERSIONS="${VERSIONS} 1.9.1"
VERSIONS="${VERSIONS} frontend"

for version in ${VERSIONS}; do
	service "apache-subversion_${version}" "${@}"
done
