#!/bin/bash

docker build . --tag denisnovac/shopping-cart-backend:test
docker image ls | grep shopping-cart-backend
