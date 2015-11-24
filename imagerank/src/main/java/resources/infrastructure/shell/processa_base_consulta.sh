#!/bin/bash
ROOT_DIR="/home/ec2-user/data/imagerank"

arquivoLog="${ROOT_DIR}/processa_base_consulta.log"

diretorioBaseCompleta="${ROOT_DIR}/base_completa"
diretorioBaseConsulta="${ROOT_DIR}/base_consulta"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares="pares"
diretorioLACDataset="lac_dataset"
diretorioExecucaoLAC="${ROOT_DIR}/bin/lac"
diretorioSaidaLAC="${diretorioBaseConsulta}/lac_output"
diretorioSaidaScore="score_output"
totalParesGerar=4000

echo -e "Acompanhe o log do arquivo '"$arquivoLog"'..."
java -Dfile.encoding=UTF-8 -cp ${ROOT_DIR}/imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankOnlineQueryProcessor $diretorioBaseCompleta $diretorioBaseConsulta $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $diretorioExecucaoLAC $diretorioSaidaLAC $diretorioSaidaScore $totalParesGerar 2> $arquivoLog
