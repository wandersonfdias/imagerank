#!/bin/bash
ROOT_DIR="/home/imagerank/data/imagerank"

arquivoLog="${ROOT_DIR}/gera_base_treino.log"
diretorioBase="${ROOT_DIR}/base_completa"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares="pares"
diretorioLACDataset="lac_dataset"
arquivoParesOriginal="${ROOT_DIR}/pares_originais/tag-classes-reduced.dat"
diretorioExecucaoLAC="${ROOT_DIR}/bin/lac"
diretorioSaidaLAC="$diretorioBase/lac_output"
totalParesGerar=1000000

echo "Acompannhe o log do arquivo '"$arquivoLog"'..."
java -Xms1536M  -Xmx3096M -Xss8192k -XX:+UseCompressedOops -XX:-UseParallelOldGC -Dfile.encoding=UTF-8 -cp ${ROOT_DIR}/imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankBatchProcessor $diretorioBase $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $arquivoParesOriginal $diretorioExecucaoLAC $diretorioSaidaLAC $totalParesGerar 2> $arquivoLog
