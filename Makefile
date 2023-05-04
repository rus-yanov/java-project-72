.DEFAULT_GOAL := run-dist

run-dist: 
	make -C app run-dist

clean:
	make -C app clean

build:
	make -C app build

install:
	make -C app install

test:
	make -C app test

report:
	make -C app report

lint:
	make -C app lint

start:
	make -C app start
	
start-dist:
	make -C app start-dist
	
.PHONY: build
