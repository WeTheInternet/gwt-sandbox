#!/bin/sh

ant dist-dev && cd maven && ./push-gwt.sh && cd ..
