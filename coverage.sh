#!/bin/bash

sbt clean coverage test

sbt coverage it:test

sbt coverageReport
