#!/bin/bash

# Script para construir as imagens Docker dos serviços

set -e

echo ""
echo "======================================"
echo "Criando Imagem Docker: userCredits..."
echo "======================================"
cd userCredits
docker build -f docker/Dockerfile -t gustavogaudesena/usercredits:latest .

echo ""
echo "======================================"
echo "Criando Imagem Docker: library..."
echo "======================================"
cd ../library
docker build -f docker/Dockerfile -t gustavogaudesena/library:latest .

echo ""
echo "======================================"
echo "Imagens criadas com sucesso!"
echo "======================================"
docker images | grep -E "gustavogaudesena/(usercredits|library)"
