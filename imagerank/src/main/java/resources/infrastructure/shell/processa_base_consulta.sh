#!/bin/bash
arquivoLog="processa_base_consulta.log"

diretorioBaseCompleta="$PWD/base_completa"
diretorioBaseConsulta="$PWD/base_consulta"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares="pares"
diretorioLACDataset="lac_dataset"
diretorioExecucaoLAC="$PWD/bin/lac"
diretorioSaidaLAC="$diretorioBaseConsulta/lac_output"
diretorioSaidaScore="score_output"
totalParesGerar=500

java -Dfile.encoding=UTF-8 -cp imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankOnlineQueryProcessor $diretorioBaseCompleta $diretorioBaseConsulta $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $diretorioExecucaoLAC $diretorioSaidaLAC $diretorioSaidaScore $totalParesGerar 2&> $arquivoLog &

echo -e "Acompanhe o log do arquivo '"$arquivoLog"'..."