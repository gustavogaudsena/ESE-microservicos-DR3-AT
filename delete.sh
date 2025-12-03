#!/bin/bash

set -e

echo "======================================"
echo "Deletando namespace dr3at..."
echo "======================================"

kubectl delete namespace dr3at

echo ""
echo "======================================"
echo "Namespace dr3at deletado com sucesso!"
echo "======================================"
