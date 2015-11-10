#!/bin/bash
horaInicio=$(date +"%H:%M:%S")
path="$PWD"
log="${path}/log.txt"

echo "***** [$horaInicio] Script iniciado. *****" > ${log}

hora=$(date +"%H:%M:%S")
echo "[$hora] Convertendo imagens para pgm.." >> ${log}
sh ${path}/convert_pgm.sh

hora=$(date +"%H:%M:%S")
echo "[$hora] Segmentando imagens.." >> ${log}
cd ${path}/imagens_pgm
sh ${path}/imagens_pgm/segmenta.sh

hora=$(date +"%H:%M:%S")
echo "[$hora] Renomeando imagens.." >> ${log}
cd ${path}/mascaras
sh ${path}/mascaras/renomeia.sh

hora=$(date +"%H:%M:%S")
echo "[$hora] Executando descritores.." >> ${log}
cd ${path}
sh ${path}/run_descriptors.sh

hora=$(date +"%H:%M:%S")
echo "[$hora] Limpando mascaras de imagens e temporarios " >> ${log}
#sh ${path}/limpa.sh

hora=$(date +"%H:%M:%S")
echo "[$horaInicio] - [$hora] Script Finalizado" >> ${log}

