#!/bin/bash
# building for other architectures

docker buildx build . \
  --platform linux/amd64 \
  --tag denisnovac/shopping-cart-backend:test-amd64

docker image ls | grep shopping-cart-backend
