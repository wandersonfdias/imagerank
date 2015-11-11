#!/bin/bash

rules=$1;

if [ -z ${rules} ]; then

   echo "Quantidade de regras não informada.";   	
   exit;	

fi

./lazy -i "../lac_dataset/treino" -t "../lac_dataset/teste" -s 1 -c 0.01 -m "$rules" -e 10000000 > "saida/m$rules.log";
