#!/bin/bash

# Script para fazer deploy de todos os serviços no Kubernetes

set -e

echo "======================================"
echo "Criando namespace dr3at..."
echo "======================================"
kubectl apply -f k8s/namespace.yaml

echo ""
echo "======================================"
echo "Criando deploy PostgreSQL..."
echo "======================================"
kubectl apply -f k8s/postgres/configmap.yaml
kubectl apply -f k8s/postgres/secret.yaml
kubectl apply -f k8s/postgres/pvc.yaml
kubectl apply -f k8s/postgres/deployment.yaml
kubectl apply -f k8s/postgres/service.yaml

echo ""
echo "Aguardando PostgreSQL ficar pronto..."
kubectl wait --for=condition=ready pod -l app=postgres -n dr3at --timeout=120s

echo ""
echo "======================================"
echo "Criando deploy userCredits service..."
echo "======================================"
kubectl apply -f k8s/usercredits/configmap.yaml
kubectl apply -f k8s/usercredits/secret.yaml
kubectl apply -f k8s/usercredits/deployment.yaml
kubectl apply -f k8s/usercredits/service.yaml

echo ""
echo "======================================"
echo "Criando deploy library service..."
echo "======================================"
kubectl apply -f k8s/library/configmap.yaml
kubectl apply -f k8s/library/secret.yaml
kubectl apply -f k8s/library/deployment.yaml
kubectl apply -f k8s/library/service.yaml

echo ""
echo "======================================"
echo "Deployment finalizado!"
echo "======================================"
echo ""
echo "Verificando pods..."
kubectl get pods -n dr3at
echo ""
echo "Verificando services..."
kubectl get services -n dr3at
